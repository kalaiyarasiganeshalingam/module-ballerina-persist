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

import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.plugins.CodeModifier;
import io.ballerina.projects.plugins.CodeModifierContext;
import io.ballerina.stdlib.persist.compiler.model.Entity;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Analyzes a Ballerina Persist.
 */
public class PersistCodeModifier extends CodeModifier {

//    private final List<String> persistClientNames = new ArrayList<>();
//    private final List<Variable> variables = new ArrayList<>();
    private final List<Entity> entities = new ArrayList<>();

    @Override
    public void init(CodeModifierContext codeModifierContext) {
        PrintStream asd = System.out;
        asd.println("*********************************");
//        codeModifierContext.addSyntaxNodeAnalysisTask(new PersistQueryRemoteMethodValidator(),
//                Arrays.asList(SyntaxKind.FROM_CLAUSE));

        // Identify all persist client in the package
//        codeModifierContext.addSyntaxNodeAnalysisTask(new PersistClientIdentifierTask(persistClientNames),
//                SyntaxKind.CLASS_DEFINITION);
//        // Identify all declared variable names with type
//        codeModifierContext.addSyntaxNodeAnalysisTask(new PersistClientVariableIdentifierTask(variables),
//                Arrays.asList(SyntaxKind.LOCAL_VAR_DECL, SyntaxKind.MODULE_VAR_DECL));
        // Identify all declared entity
        codeModifierContext.addSyntaxNodeAnalysisTask(new PersistEntityIdentifierTask(entities),
                SyntaxKind.TYPE_DEFINITION);

        codeModifierContext.addSourceModifierTask(new QueryCodeModifierTask());

//        modifierContext.addSourceModifierTask(sourceModifierContext -> {
//            if (!isPersistModelDefinitionDocument(sourceModifierContext)) {
//                return;
//            }
//            // Add new function to every bal file
//            for (ModuleId moduleId : sourceModifierContext.currentPackage().moduleIds()) {
//                Module module = sourceModifierContext.currentPackage().module(moduleId);
//                for (DocumentId documentId : module.documentIds()) {
//                    Document document = module.document(documentId);
//                    NodeList<ModuleMemberDeclarationNode> members =
//                            ((ModulePartNode) document.syntaxTree().rootNode()).members();
//                    for (ModuleMemberDeclarationNode member : members) {
//                        if (member.kind() != SyntaxKind.FUNCTION_DEFINITION) {
//                            continue;
//                        }
//                        FunctionDefinitionNode functionDefinitionNode = (FunctionDefinitionNode) member;
//                        if (!functionDefinitionNode.functionName().text().trim().equals("main")) {
//                            continue;
//                        }
//                        FunctionBodyBlockNode functionBodyNode = (FunctionBodyBlockNode) functionDefinitionNode.
//                                functionBody();
//                        NodeList<StatementNode> statements = functionBodyNode.statements();
//                        for (StatementNode statement : statements) {
//                            if (statement.kind() != SyntaxKind.LOCAL_VAR_DECL) {
//                                continue;
//                            }
//                            Optional<ExpressionNode> initializer = ((VariableDeclarationNode) statement).
//                            initializer();
//                            if (initializer.isPresent()) {
//                                ExpressionNode expressionNode = initializer.get();
//                                if (expressionNode instanceof CheckExpressionNode) {
//                                    CheckExpressionNode checkExpressionNode =
//                                            (CheckExpressionNode) expressionNode;
//                                    expressionNode = checkExpressionNode.expression();
//
//                                }
//                                if (expressionNode instanceof QueryExpressionNode) {
//                                    QueryExpressionNode queryExpressionNode =
//                                            (QueryExpressionNode) expressionNode;
//                                    QueryPipelineNode pipeline = queryExpressionNode.queryPipeline();
//                                    NodeList<IntermediateClauseNode> intermediateClauses =
//                                            pipeline.intermediateClauses();
//                                    for (IntermediateClauseNode intermediateClause : intermediateClauses) {
//                                        if (intermediateClause.kind() == SyntaxKind.WHERE_CLAUSE) {
//                                            WhereClauseNode whereClauseNode =
//                                                    (WhereClauseNode) intermediateClause;
//                                            PrintStream asd = System.out;
//                                            FromClauseNode fromClause = pipeline.fromClause();
//                                            String stringValue = fromClause.expression().toString().trim();
//                                            StringBuilder finalStringValue = new StringBuilder();
//                                            finalStringValue.append(stringValue.substring(0, stringValue.length()
//                                            - 1).
//                                                    concat(", whereClause = string `"));
//                                            whereClauseNode.expression().childEntries().forEach(
//                                                    child -> {
//                                                        Optional<Node> node = child.node();
//                                                        if (node.isPresent()) {
//                                                            Node expression = node.get();
//                                                            if (expression instanceof Token) {
//                                                                finalStringValue.append(expression);
//                                                            } else {
//                                                                if (expression instanceof BinaryExpressionNode) {
//                                                                    Collection<ChildNodeEntry> binaryExpressionNode =
//                                                                            ((BinaryExpressionNode) expression).
//                                                                                    childEntries();
//                                                                    for (ChildNodeEntry childNodeEntry :
//                                                                            binaryExpressionNode) {
//                                                                        Optional<Node> node1 = childNodeEntry.node();
//                                                                        if (node1.isPresent()) {
//                                                                            Node exp1 = node1.get();
//                                                                            if (exp1 instanceof
//                                                                            FieldAccessExpressionNode) {
//                                                                                String value = exp1.toString();
//                                                                                if (value.contains(".")) {
//                                                                                    finalStringValue.append(
//                                                                                            value.split("\\.")[1]);
//                                                                                } else {
//                                                                                    finalStringValue.append(value);
//                                                                                }
//                                                                            }
//                                                                            if (exp1 instanceof
//                                                                            SimpleNameReferenceNode) {
//                                                                                finalStringValue.append("${").append(
//                                                                                                exp1.toString().
//                                                                                                trim()).
//                                                                                        append("} ");
//                                                                            } else if (exp1 instanceof
//                                                                            BasicLiteralNode) {
//                                                                                finalStringValue.
//                                                                                append(exp1.toString().
//                                                                                        substring(1, exp1.toString().
//                                                                                                length() - 2));
//                                                                            }
//                                                                            if (exp1 instanceof Token) {
//                                                                                finalStringValue.append(exp1);
//                                                                            }
//                                                                        }
//                                                                    }
//                                                                }
//                                                            }
//                                                        }
//                                                    }
//                                            );
//                                            asd.println(finalStringValue.toString().trim() + "`)");
//                                            queryExpressionNode.modify();
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    ModulePartNode rootNode = document.syntaxTree().rootNode();
//                    NodeList<ModuleMemberDeclarationNode> newMembers =
//                            rootNode.members().add(createFunctionDefNode(document));
//                    ModulePartNode newModulePart =
//                            rootNode.modify(rootNode.imports(), newMembers, rootNode.eofToken());
//                    SyntaxTree updatedSyntaxTree = document.syntaxTree().modifyWith(newModulePart);
//                    sourceModifierContext.modifySourceFile(updatedSyntaxTree.textDocument(), documentId);
//                }
//            }
//        });
    }

//    private static FunctionDefinitionNode createFunctionDefNode(Document document) {
//        List<Token> qualifierList = new ArrayList<>();
//        Token publicToken = createToken(SyntaxKind.PUBLIC_KEYWORD,
//                generateMinutiaeListWithTwoNewline(),
//                generateMinutiaeListWithWhitespace());
//        qualifierList.add(publicToken);
//
//        SimpleNameReferenceNode simpleNameRefNode = createSimpleNameReferenceNode(
//                createIdentifierToken("string", createEmptyMinutiaeList(),
//                        generateMinutiaeListWithWhitespace()));
//
//        RequiredParameterNode requiredParameterNode =
//                createRequiredParameterNode(createEmptyNodeList(), simpleNameRefNode,
//                        createIdentifierToken("params"));
//
//        OptionalTypeDescriptorNode optionalErrorTypeDescriptorNode =
//                createOptionalTypeDescriptorNode(
//                        createParameterizedTypeDescriptorNode(SyntaxKind.ERROR_TYPE_DESC,
//                                createToken(SyntaxKind.ERROR_KEYWORD), null),
//                        createToken(SyntaxKind.QUESTION_MARK_TOKEN, createEmptyMinutiaeList(),
//                                generateMinutiaeListWithWhitespace()));
//
//        ReturnTypeDescriptorNode returnTypeDescriptorNode =
//                createReturnTypeDescriptorNode(createToken(SyntaxKind.RETURNS_KEYWORD,
//                                createEmptyMinutiaeList(), generateMinutiaeListWithWhitespace()),
//                        createEmptyNodeList(), optionalErrorTypeDescriptorNode);
//
//        FunctionSignatureNode functionSignatureNode =
//                createFunctionSignatureNode(createToken(SyntaxKind.OPEN_PAREN_TOKEN),
//                        createSeparatedNodeList(requiredParameterNode),
//                        createToken(SyntaxKind.CLOSE_PAREN_TOKEN,
//                                createEmptyMinutiaeList(), generateMinutiaeListWithWhitespace()),
//                        returnTypeDescriptorNode);
//
//        FunctionBodyBlockNode emptyFunctionBodyNode =
//                createFunctionBodyBlockNode(
//                        createToken(SyntaxKind.OPEN_BRACE_TOKEN, createEmptyMinutiaeList(),
//                                generateMinutiaeListWithNewline()), null,
//                        createEmptyNodeList(),
//                        createToken(SyntaxKind.CLOSE_BRACE_TOKEN), null);
//
//        return createFunctionDefinitionNode(
//                SyntaxKind.FUNCTION_DEFINITION, null, AbstractNodeFactory.createNodeList(qualifierList),
//                createToken(SyntaxKind.FUNCTION_KEYWORD,
//                        createEmptyMinutiaeList(),
//                        generateMinutiaeListWithWhitespace()),
//                createIdentifierToken("newFunctionByCodeModifier"
//                        + document.name().replace(".bal", "").replace("/", "_")
//                        .replace("-", "_")),
//                createEmptyNodeList(), functionSignatureNode, emptyFunctionBodyNode);
//    }
//
//    private static MinutiaeList generateMinutiaeListWithWhitespace() {
//        return AbstractNodeFactory.createMinutiaeList(AbstractNodeFactory.createWhitespaceMinutiae(" "));
//    }
//
//    private static MinutiaeList generateMinutiaeListWithNewline() {
//        return AbstractNodeFactory.createMinutiaeList(AbstractNodeFactory.createWhitespaceMinutiae("\n"));
//    }
//
//    private static MinutiaeList generateMinutiaeListWithTwoNewline() {
//        return AbstractNodeFactory.createMinutiaeList(AbstractNodeFactory.createWhitespaceMinutiae("\n\n"));
//    }
//
//    private boolean isPersistModelDefinitionDocument(SourceModifierContext modifierContext) {
//        Path balFilePath = modifierContext.currentPackage().project().sourceRoot().toAbsolutePath();
//        File persistDirectory = balFilePath.resolve(PERSIST_DIRECTORY).toFile();
//        if (persistDirectory.exists()) {
//            File balProject = balFilePath.toFile();
//            if (balProject.isDirectory()) {
//                File tomlFile = balFilePath.resolve(ProjectConstants.BALLERINA_TOML).toFile();
//                return tomlFile.exists();
//            }
//        }
//        return false;
//    }
}
