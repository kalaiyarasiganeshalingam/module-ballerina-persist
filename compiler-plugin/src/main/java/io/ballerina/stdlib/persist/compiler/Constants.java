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

import io.ballerina.compiler.syntax.tree.LiteralValueToken;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;

/**
 * Constants class.
 */
public final class Constants {
    public static final String PERSIST_DIRECTORY = "persist";
    public static final String PERSIST = "persist";
    public static final String DATASTORE = "datastore";
    public static final String TIME_MODULE = "time";
    public static final String EMPTY_STRING = "";
    public static final String ARRAY = "[]";
    public static final String LS = System.lineSeparator();
    public static final String SPACE = " ";
    public static final String WHERE = "WHERE";
    public static final String OPEN_BRACES_WITH_SPACE = "( ";
    public static final String CLOSE_BRACES_WITH_SPACE = ") ";

    public static final String OPEN_BRACES = "(";
    public static final String CLOSE_BRACES = ")";
    public static final String CLOSE_BRACES_WITH_NEW_LINE = ")\n";
    public static final String BAL_ESCAPE_TOKEN = "'";
    public static final String BACKTICK = "`";
    public static final String PERSIST_INHERITANCE_NODE = "*persist:AbstractPersistClient;";

    private Constants() {
    }

    /**
     * Constants related to Ballerina types.
     */
    public static final class BallerinaTypes {

        public static final String INT = "int";
        public static final String STRING = "string";
        public static final String BOOLEAN = "boolean";
        public static final String DECIMAL = "decimal";
        public static final String FLOAT = "float";
        public static final String BYTE = "byte";
        public static final String ENUM = "enum";

        private BallerinaTypes() {
        }
    }

    /**
     * Constants related to Ballerina time type.
     */
    public static final class BallerinaTimeTypes {

        public static final String DATE = "Date";
        public static final String TIME_OF_DAY = "TimeOfDay";
        public static final String UTC = "Utc";
        public static final String CIVIL = "Civil";

        private BallerinaTimeTypes() {
        }
    }

    /**
     * Constants related to Persist datastores.
     */
    public static final class Datastores {

        public static final String MYSQL = "mysql";
        public static final String MSSQL = "mssql";
        public static final String IN_MEMORY = "inmemory";
        public static final String GOOGLE_SHEETS = "googlesheets";

        private Datastores() {
        }
    }

    /**
     * SQL keywords used to construct the query.
     */
    public static final class SQLKeyWords {

        private SQLKeyWords() {}

        public static final String WHERE = "WHERE";
        public static final String NOT_EQUAL_TOKEN = "<>";
        public static final String AND = "AND";
        public static final String OR = "OR";
    }

    /**
     * Constant nodes used in code modification.
     */
    public static final class TokenNodes {

        private TokenNodes() {}

        public static final Token INTERPOLATION_START_TOKEN = NodeFactory.createLiteralValueToken(
                SyntaxKind.INTERPOLATION_START_TOKEN, "${", createEmptyMinutiaeList(), createEmptyMinutiaeList());
        public static final Token INTERPOLATION_END_TOKEN = NodeFactory.createLiteralValueToken(
                SyntaxKind.CLOSE_BRACE_TOKEN, "}", createEmptyMinutiaeList(), createEmptyMinutiaeList());
        public static final Token WHERE_CLAUSE_NAME = NodeFactory.createLiteralValueToken(
                SyntaxKind.NAMED_ARG, "whereClause", createEmptyMinutiaeList(), createEmptyMinutiaeList());
        public static final Token EQUAL_TOKEN = NodeFactory.createLiteralValueToken(
                SyntaxKind.EQUAL_TOKEN, " = string ", createEmptyMinutiaeList(), createEmptyMinutiaeList());
        public static final Token COMMA_TOKEN = NodeFactory.createLiteralValueToken(
                SyntaxKind.COMMA_TOKEN, ", ", createEmptyMinutiaeList(), createEmptyMinutiaeList());
        public static final LiteralValueToken BACKTICK_TOKEN = NodeFactory.createLiteralValueToken(
                SyntaxKind.BACKTICK_TOKEN, BACKTICK, createEmptyMinutiaeList(), createEmptyMinutiaeList());
        public static final Token OPEN_PAREN_TOKEN = NodeFactory.createLiteralValueToken(SyntaxKind.OPEN_PAREN_TOKEN,
                OPEN_BRACES, createEmptyMinutiaeList(), createEmptyMinutiaeList());
        public static final Token CLOSE_PAREN_WITH_NEW_LINE_TOKEN = NodeFactory.createLiteralValueToken(
                SyntaxKind.CLOSE_PAREN_TOKEN, CLOSE_BRACES_WITH_NEW_LINE, createEmptyMinutiaeList(),
                createEmptyMinutiaeList());

    }
}
