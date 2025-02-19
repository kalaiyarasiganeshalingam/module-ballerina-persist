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

package io.ballerina.stdlib.persist.compiler;

import io.ballerina.projects.CodeModifierResult;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Package;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Code modifier related test cases.
 */
public class CodeModifierTest {

    private static ProjectEnvironmentBuilder getEnvironmentBuilder() {
        Path distributionPath = Paths.get("../", "target", "ballerina-runtime")
                .toAbsolutePath();
        Environment environment = EnvironmentBuilder.getBuilder().setBallerinaHome(distributionPath).build();
        return ProjectEnvironmentBuilder.getBuilder(environment);
    }

    private Package loadPackage(String path) {
        Path projectDirPath = Paths.get("src", "test", "resources", "codemodifier").
                toAbsolutePath().resolve(path);
        BuildProject project = BuildProject.load(getEnvironmentBuilder(), projectDirPath);
        return project.currentPackage();
    }

    @Test
    public void testCodeModifier() {

        Package newPackage = getModifiedPackage("project_1");

        for (DocumentId documentId : newPackage.getDefaultModule().documentIds()) {
            Document document = newPackage.getDefaultModule().document(documentId);

            if (document.name().equals("main.bal")) {
                String sourceCode = document.syntaxTree().toSourceCode();
                String modifiedFunction =
                        "entities:Product[] products = check from var e in mcClient->/products(targetType = " +
                                "entities:Product, whereClause = string ` id = ${value}  OR id = \"s\" `)\n" +
                                "        where e.id == value || e.id == \"s\"\n" +
                                "        select e;\n";
                String modifiedFunction1 =
                        "entities:Product[]|error result = from var e in mcClient->/products(targetType = " +
                                "entities:Product, whereClause = string ` id = ${value}  AND id = \"test\" `)\n" +
                                "            where e.id == value && e.id == \"test\"\n" +
                                "            select e;\n";
                String modifiedFunction2 = "products = check from var e in mcClient->/products(targetType = " +
                        "entities:Product, whereClause = string ` ( id = ${value}  OR id = \"s\")  AND id <> " +
                        "\"test\" `)\n" +
                        "            where (e.id == value || e.id == \"s\") && e.id != \"test\"\n" +
                        "            select e;\n";
                Assert.assertTrue(sourceCode.contains(modifiedFunction));
                Assert.assertTrue(sourceCode.contains(modifiedFunction1));
                Assert.assertTrue(sourceCode.contains(modifiedFunction2));
            }
        }
    }

    private Package getModifiedPackage(String path) {
        Package currentPackage = loadPackage(path);
        DiagnosticResult diagnosticResult = currentPackage.getCompilation().diagnosticResult();
        Assert.assertEquals(diagnosticResult.errorCount(), 0);
        // Running the code generation
        CodeModifierResult codeModifierResult = currentPackage.runCodeModifierPlugins();
        Assert.assertEquals(codeModifierResult.reportedDiagnostics().errorCount(), 0);
        return codeModifierResult.updatedPackage().orElse(currentPackage);
    }
}
