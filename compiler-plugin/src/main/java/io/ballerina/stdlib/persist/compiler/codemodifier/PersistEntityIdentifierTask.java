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

import io.ballerina.projects.ModuleId;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.stdlib.persist.compiler.model.Entity;
import io.ballerina.stdlib.persist.compiler.utils.Utils;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Analysis task to identify all declared entities.
 */
public class PersistEntityIdentifierTask implements AnalysisTask<SyntaxNodeAnalysisContext>  {

//    private final List<Entity> entities;

    PersistEntityIdentifierTask(List<Entity> entities) {
//        this.entities = entities;
    }

    @Override
    public void perform(SyntaxNodeAnalysisContext ctx) {
        PrintStream asd = System.out;
        asd.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        if (Utils.hasCompilationErrors(ctx)) {
            return;
        }
        asd.println(ctx.syntaxTree().toSourceCode());
        asd.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        Collection<ModuleId> documentPath = ctx.currentPackage().moduleIds();

//        if (documentPath.isPresent()) {
            asd.println(Arrays.toString(documentPath.toArray()));
//            Path parent = documentPath.get().getParent();
//            asd.println(documentPath.get());
//            if (parent != null  && parent.endsWith("persist")) {
//                asd.println("@@@@@@@@@@@@@@@@@@@@@@@");
//            }
//        }

    }
}
