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

import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.plugins.ModifierTask;
import io.ballerina.projects.plugins.SourceModifierContext;

import java.io.PrintStream;
import java.util.Map;

/**
 *
 */
public class PersistSourceModifier implements ModifierTask<SourceModifierContext> {

    private final Map<DocumentId, String> modifierContextMap;
//    private SourceModifierContext context;

    public static final String PACKAGE_NAME = "graphql";
    public static final String PACKAGE_ORG = "ballerina";
    
    public PersistSourceModifier(Map<DocumentId, String> nodeMap) {
        this.modifierContextMap = nodeMap;
    }

    @Override
    public void modify(SourceModifierContext sourceModifierContext) {
//        this.context = sourceModifierContext;
        for (Map.Entry<DocumentId, String> entry : this.modifierContextMap.entrySet()) {
            DocumentId documentId = entry.getKey();
//            String modifierContext = entry.getValue();
            Module module = sourceModifierContext.currentPackage().module(documentId.moduleId());
            ModulePartNode rootNode = module.document(documentId).syntaxTree().rootNode();
            String updatedRootNode = getGraphqlModulePrefix(rootNode);
            PrintStream asd = System.out;
            asd.println(updatedRootNode);
//            rootNode.members().
//            ModulePartNode updatedRootNode = modifyDocument(sourceModifierContext, rootNode, modifierContext);
//            updatedRootNode = addImportsIfMissing(updatedRootNode);
//            SyntaxTree syntaxTree = module.document(documentId).syntaxTree().modifyWith(updatedRootNode);
//            TextDocument textDocument = syntaxTree.textDocument();
//            if (module.documentIds().contains(documentId)) {
//                sourceModifierContext.modifySourceFile(textDocument, documentId);
//            } else {
//                sourceModifierContext.modifyTestSourceFile(textDocument, documentId);
//            }
        }
    }

    private String getGraphqlModulePrefix(ModulePartNode rootNode) {
        for (ImportDeclarationNode importDeclarationNode : rootNode.imports()) {
            if (!isGraphqlImportNode(importDeclarationNode)) {
                continue;
            }
            if (importDeclarationNode.prefix().isPresent()) {
                return importDeclarationNode.prefix().get().prefix().text();
            }
        }
        return PACKAGE_NAME;
    }

    private static boolean isGraphqlImportNode(ImportDeclarationNode importNode) {
        if (importNode.orgName().isEmpty()) {
            return false;
        }
        if (!PACKAGE_ORG.equals(importNode.orgName().get().orgName().text())) {
            return false;
        }
        if (importNode.moduleName().size() != 1) {
            return false;
        }
        return PACKAGE_NAME.equals(importNode.moduleName().get(0).text());
    }

}
