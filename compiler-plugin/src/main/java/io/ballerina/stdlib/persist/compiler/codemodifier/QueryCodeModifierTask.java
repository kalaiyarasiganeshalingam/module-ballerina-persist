/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.stdlib.persist.compiler.codemodifier;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.BindingPatternNode;
import io.ballerina.compiler.syntax.tree.ChildNodeEntry;
import io.ballerina.compiler.syntax.tree.ClientResourceAccessActionNode;
import io.ballerina.compiler.syntax.tree.FromClauseNode;
import io.ballerina.compiler.syntax.tree.IntermediateClauseNode;
import io.ballerina.compiler.syntax.tree.LiteralValueToken;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QueryPipelineNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TreeModifier;
import io.ballerina.compiler.syntax.tree.WhereClauseNode;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.plugins.ModifierTask;
import io.ballerina.projects.plugins.SourceModifierContext;
import io.ballerina.stdlib.persist.compiler.Constants;
import io.ballerina.stdlib.persist.compiler.exception.NotSupportedExpressionException;
import io.ballerina.stdlib.persist.compiler.expression.ExpressionBuilder;
import io.ballerina.stdlib.persist.compiler.expression.ExpressionVisitor;
import io.ballerina.stdlib.persist.compiler.utils.Utils;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 */
public class QueryCodeModifierTask implements ModifierTask<SourceModifierContext> {

    private final Map<String, String> variables;
    private final List<String> entities;
    private final List<String> persistClientNames;
    private final List<String> persistClientVariables = new ArrayList<>();
    private boolean isClientVariablesProcessed = false;

    public QueryCodeModifierTask(List<String> persistClientNames, List<String> entities,
                                 Map<String, String> variables) {
        this.entities = entities;
        this.variables = variables;
        this.persistClientNames = persistClientNames;
    }

    @Override
    public void modify(SourceModifierContext sourceModifierContext) {
        if (persistClientNames.isEmpty()) {
            return;
        }
        if (!isClientVariablesProcessed) {
            processPersistClientVariables();
        }

        if (this.persistClientVariables.isEmpty()) {
            return;
        }

        Package pkg = sourceModifierContext.currentPackage();
        for (ModuleId moduleId : pkg.moduleIds()) {
            Module module = pkg.module(moduleId);
            for (DocumentId documentId : module.documentIds()) {
                sourceModifierContext.modifySourceFile(getUpdatedSyntaxTree(module, documentId).textDocument(),
                        documentId);
            }
            for (DocumentId documentId : module.testDocumentIds()) {
                sourceModifierContext.modifyTestSourceFile(getUpdatedSyntaxTree(module, documentId).textDocument(),
                        documentId);
            }
        }
    }

    private SyntaxTree getUpdatedSyntaxTree(Module module, DocumentId documentId) {
        Document document = module.document(documentId);
        ModulePartNode rootNode = document.syntaxTree().rootNode();
        QueryConstructModifier queryConstructModifier = new QueryConstructModifier(entities, persistClientVariables);
        ModulePartNode newRoot = (ModulePartNode) rootNode.apply(queryConstructModifier);
        SyntaxTree syntaxTree = document.syntaxTree().modifyWith(newRoot);
        try {
            Formatter.format(syntaxTree);
        } catch (FormatterException e) {
            // throw new RuntimeException("Syntax tree formatting failed for the file: " + document.name());
        }
        return syntaxTree;
    }

    private void processPersistClientVariables() {
        for (Map.Entry<String, String> entry : this.variables.entrySet()) {
            String[] strings = entry.getValue().split(":");
            if (persistClientNames.contains(strings[strings.length - 1])) {
                persistClientVariables.add(entry.getKey());
            }
        }
        isClientVariablesProcessed = true;
    }

    private static class QueryConstructModifier extends TreeModifier {
        private final List<String> persistClientVariables;
        private final List<String> entities;

        public QueryConstructModifier(List<String> entities, List<String> persistClientVariables) {
            this.entities = entities;
            this.persistClientVariables = persistClientVariables;
        }

        @Override
        public QueryPipelineNode transform(QueryPipelineNode queryPipelineNode) {
            FromClauseNode fromClauseNode = queryPipelineNode.fromClause();
            if (!isQueryUsingPersistentClient(fromClauseNode)) {
                return queryPipelineNode;
            }

            NodeList<IntermediateClauseNode> intermediateClauseNodes = queryPipelineNode.intermediateClauses();
            List<IntermediateClauseNode> whereClauseNode = intermediateClauseNodes.stream()
                    .filter((node) -> node instanceof WhereClauseNode)
                    .collect(Collectors.toList());
            boolean isWhereClauseUsed = whereClauseNode.size() != 0;

            if (!isWhereClauseUsed) {
                return queryPipelineNode;
            }

            List<Node> parameterizedQuery = new ArrayList<>();
            parameterizedQuery.add(Utils.getStringLiteralToken(Constants.SPACE));

            try {
                List<Node> whereClause = processWhereClause(((WhereClauseNode) whereClauseNode.get(0)),
                        fromClauseNode.typedBindingPattern().bindingPattern());
                parameterizedQuery.addAll(whereClause);
            } catch (NotSupportedExpressionException e) {
                // Need to
                return queryPipelineNode;
            }

            PositionalArgumentNode parameterizedQueryForWhere = NodeFactory.createPositionalArgumentNode(
                    NodeFactory.createTemplateExpressionNode(
                            SyntaxKind.RAW_TEMPLATE_EXPRESSION, null,
                            Constants.TokenNodes.BACKTICK_TOKEN,
                            AbstractNodeFactory.createSeparatedNodeList(parameterizedQuery),
                            Constants.TokenNodes.BACKTICK_TOKEN
                    )
            );

            ClientResourceAccessActionNode clientResourceAccessActionNode =
                    (ClientResourceAccessActionNode) fromClauseNode.expression();
            NamedArgumentNode whereClause = NodeFactory.createNamedArgumentNode(
                    NodeFactory.createSimpleNameReferenceNode(
                    Constants.TokenNodes.WHERE_CLAUSE_NAME), Constants.TokenNodes.EQUAL_TOKEN,
                    parameterizedQueryForWhere.expression());
            SimpleNameReferenceNode comma = NodeFactory.createSimpleNameReferenceNode(Constants.TokenNodes.COMMA_TOKEN);
            Optional<ParenthesizedArgList> arguments = clientResourceAccessActionNode.arguments();
            if (arguments.isPresent()) {
                ParenthesizedArgList whereClauseArgList = NodeFactory.createParenthesizedArgList(
                        Constants.TokenNodes.OPEN_PAREN_TOKEN,
                        NodeFactory.createSeparatedNodeList(arguments.get().arguments().get(0), comma, whereClause),
                        Constants.TokenNodes.CLOSE_PAREN_WITH_NEW_LINE_TOKEN
                );
                FromClauseNode modifiedFromClause = fromClauseNode.modify(
                        fromClauseNode.fromKeyword(),
                        fromClauseNode.typedBindingPattern(),
                        fromClauseNode.inKeyword(),
                        NodeFactory.createClientResourceAccessActionNode(
                                clientResourceAccessActionNode.expression(),
                                clientResourceAccessActionNode.rightArrowToken(),
                                clientResourceAccessActionNode.slashToken(),
                                clientResourceAccessActionNode.resourceAccessPath(),
                                null,
                                null,
                                whereClauseArgList
                        )
                );
                return queryPipelineNode.modify(
                        modifiedFromClause,
                        intermediateClauseNodes
                );
            }
            return queryPipelineNode;
        }

        private boolean isQueryUsingPersistentClient(FromClauseNode fromClauseNode) {
            // From clause should contain resource call invocation
            if (fromClauseNode.expression() instanceof ClientResourceAccessActionNode) {
                ClientResourceAccessActionNode remoteCall =
                        (ClientResourceAccessActionNode) fromClauseNode.expression();
                Collection<ChildNodeEntry> clientResourceChildEntries = remoteCall.childEntries();
                if (clientResourceChildEntries.size() != 5) {
                    return false;
                }
                if (remoteCall.expression() instanceof SimpleNameReferenceNode) {
                    SimpleNameReferenceNode clientName = (SimpleNameReferenceNode) remoteCall.expression();
                    if (!this.persistClientVariables.contains(clientName.name().text().trim())) {
                        return false;
                    }
                    Optional<Node> resourcePath = ((ChildNodeEntry) clientResourceChildEntries.toArray()[3]).node();
                    return resourcePath.isPresent() && this.entities.contains(resourcePath.get().toString().trim());
                }
            }
            return false;
        }

        private List<Node> processWhereClause(WhereClauseNode whereClauseNode, BindingPatternNode bindingPatternNode)
                throws NotSupportedExpressionException {
            ExpressionBuilder expressionBuilder = new ExpressionBuilder(whereClauseNode.expression(),
                    bindingPatternNode);
            ExpressionVisitor expressionVisitor = new ExpressionVisitor();
            expressionBuilder.build(expressionVisitor);
            List<Node> expression = expressionVisitor.getExpression();
            LiteralValueToken where = (LiteralValueToken) expression.get(0);
            where = where.modify(where.text().replace(Constants.WHERE, Constants.EMPTY_STRING).trim() +
                    Constants.SPACE);
            expression.add(0, where);
            expression.remove(1);
            return expression;
        }
    }
}
