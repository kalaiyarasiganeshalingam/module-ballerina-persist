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

import io.ballerina.compiler.syntax.tree.BindingPatternNode;
import io.ballerina.compiler.syntax.tree.ChildNodeEntry;
import io.ballerina.compiler.syntax.tree.ClientResourceAccessActionNode;
import io.ballerina.compiler.syntax.tree.FromClauseNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.IntermediateClauseNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QueryPipelineNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;

/**
 *
 */
public class QueryCodeModifierTask implements ModifierTask<SourceModifierContext> {

    @Override
    public void modify(SourceModifierContext sourceModifierContext) {
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
        QueryConstructModifier queryConstructModifier = new QueryConstructModifier();
        ModulePartNode newRoot = (ModulePartNode) rootNode.apply(queryConstructModifier);

        return document.syntaxTree().modifyWith(newRoot);
    }

    private static class QueryConstructModifier extends TreeModifier {

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


            PositionalArgumentNode firstArgument = NodeFactory.createPositionalArgumentNode(
                    NodeFactory.createTemplateExpressionNode(
                            SyntaxKind.RAW_TEMPLATE_EXPRESSION, null, Constants.TokenNodes.BACKTICK_TOKEN,
                            createSeparatedNodeList(parameterizedQuery), Constants.TokenNodes.BACKTICK_TOKEN
                    )
            );
            RemoteMethodCallActionNode remoteCall = (RemoteMethodCallActionNode) fromClauseNode.expression();
            FromClauseNode modifiedFromClause = fromClauseNode.modify(
                    fromClauseNode.fromKeyword(),
                    fromClauseNode.typedBindingPattern(),
                    fromClauseNode.inKeyword(),
                    NodeFactory.createRemoteMethodCallActionNode(
                            remoteCall.expression(),
                            remoteCall.rightArrowToken(),
                            NodeFactory.createSimpleNameReferenceNode(
                                    Utils.getStringLiteralToken(Constants.EXECUTE_FUNCTION)
                            ),
                            remoteCall.openParenToken(),
                            createSeparatedNodeList(firstArgument),
                            remoteCall.closeParenToken()
                    )
            );
            NodeList<IntermediateClauseNode> processedClauses = intermediateClauseNodes;
            for (int i = 0; i < processedClauses.size(); i++) {
                if (processedClauses.get(i) instanceof WhereClauseNode) {
                    processedClauses = processedClauses.remove(i);
                    break;
                }
            }
            return queryPipelineNode.modify(
                    modifiedFromClause,
                    processedClauses
            );

        }

        private boolean isQueryUsingPersistentClient(FromClauseNode fromClauseNode) {
            // From clause should contain remote call invocation
            if (fromClauseNode.expression() instanceof ClientResourceAccessActionNode) {
                ClientResourceAccessActionNode remoteCall =
                        (ClientResourceAccessActionNode) fromClauseNode.expression();
                if (remoteCall.expression() instanceof SimpleNameReferenceNode) {
                    Collection<ChildNodeEntry> childEntries = remoteCall.childEntries();
                    if (childEntries.size() == 5) {
                        ChildNodeEntry argument = ((ChildNodeEntry) remoteCall.childEntries().toArray()[4]);
                        Optional<Node> argumentNode = argument.node();
                        if (argument.name().equals("arguments") && argumentNode.isPresent() &&
                                argumentNode.get() instanceof ParenthesizedArgList) {
                            ParenthesizedArgList parenthesizedArgList = (ParenthesizedArgList) argumentNode.get();
                            SeparatedNodeList<FunctionArgumentNode> argum = parenthesizedArgList.arguments();
                            PrintStream aser = System.out;
                            for (FunctionArgumentNode pri : argum) {
//                               argum ((NamedArgumentNode) parenthesizedArgList.arguments().get(0)).childBuckets
//                                aser
                                aser.println(pri.children().size());
                            }
                        }
                    }
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
            return expressionVisitor.getExpression();
        }
    }
}
