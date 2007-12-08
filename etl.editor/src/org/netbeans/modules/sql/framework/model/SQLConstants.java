/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.sql.framework.model;

/**
 * Constants used in the SQLBuilder Repository Model API and implementation packages.
 * 
 * @author Ritesh Adval
 * @version $Revision$
 */
public class SQLConstants {

    /** int constant for operator object type */
    public static final int GENERIC_OPERATOR = 10000;

    /** int constant for join object type */
    public static final int JOIN = 10001;

    /** int constant for literal object type */
    public static final int LITERAL = 10002;

    /** int constant for case object type */
    public static final int CASE = 10003;

    /** int constant for when object type */
    public static final int WHEN = 10004;

    /** int constant for predicate object type */
    public static final int PREDICATE = 10005;

    /** int constant for source database object type */
    public static final int SOURCE_DBMODEL = 10006;

    /** int constant for destination database object type */
    public static final int TARGET_DBMODEL = 10007;

    /** int constant for source table object type */
    public static final int SOURCE_TABLE = 10008;

    /** int constant for destination table object type */
    public static final int TARGET_TABLE = 10009;

    /** int constant for source column object type */
    public static final int SOURCE_COLUMN = 10010;

    /** int constant for destination table object type */
    public static final int TARGET_COLUMN = 10011;

    /** int constant for visible predicate object type */
    public static final int VISIBLE_PREDICATE = 10012;

    /** int constant for filter object type */
    public static final int FILTER = 10013;

    /** int constant for visible literal object type */
    public static final int VISIBLE_LITERAL = 10014;

    /** int constant for cast operator object type */
    public static final int CAST_OPERATOR = 10015;

    /** int constant for cast operator object type */
    public static final int RUNTIME_ARGS = 10016;

    /** int constant for runtime input object type */
    public static final int RUNTIME_INPUT = 10017;

    /** int constant for runtime output object type */
    public static final int RUNTIME_OUTPUT = 10018;

    /** int constant for runtime dbmodel object type */
    public static final int RUNTIME_DBMODEL = 10019;

    /** int constants for column reference * */
    public static final int COLUMN_REF = 10020;

    public static final int JOIN_TABLE = 10022;

    public static final int JOIN_TABLE_COLUMN = 10023;

    public static final int JOIN_VIEW = 10024;

    public static final int DATE_DIFF_OPERATOR = 10025;

    public static final int DATE_ADD_OPERATOR = 10026;

    public static final int DATE_ARITHMETIC_OPERATOR = 10027;

    public static final int CUSTOM_OPERATOR = 10028;

    /** Operator script that takes fixed number of input arguments. */
    public static final int OPERATOR_ARGS_FIXED = -1233;

    /** Operator script that takes variable number of arguments. */
    public static final int OPERATOR_ARGS_VARIABLE = -1234;

    /**
     * Suffix string that is appened to argument name in case an operator takes variable
     * number of arguments.
     */
    public static final String OPERATOR_VARIABLE_SUFFIX = "-var-";

    /** Prefix symbol for all operator variables. */
    public static final String OPERATOR_VARIABLE_PREFIX = "$";

    /** Undefined jdbc type */
    public static final int JDBCSQL_TYPE_UNDEFINED = -65535;

    /** unknown state for type check */
    public static final int TYPE_CHECK_UNKNOWN = 0;

    /** types being compared are same. */
    public static final int TYPE_CHECK_SAME = 1;

    /** types being compared are different but compatible. */
    public static final int TYPE_CHECK_COMPATIBLE = 2;

    /** * types being compared are incompatible */
    public static final int TYPE_CHECK_INCOMPATIBLE = 3;

    /** * types being compared are incompatible */
    public static final int TYPE_CHECK_DOWNCAST_WARNING = 4;

    /** Inner Join */
    public static final int INNER_JOIN = 1;

    /** Left Outer Join */
    public static final int LEFT_OUTER_JOIN = 2;

    /** Right Outer Join */
    public static final int RIGHT_OUTER_JOIN = 3;

    /** Full Outer Join */
    public static final int FULL_OUTER_JOIN = 4;

    // --------------------- End of Join Types --------------------------

    // --------------------- Start of Operator Type Names --------------------------

    /** Generic Operator String rep. */
    public static final String STR_GENERIC_OPERATOR = "generic_operator"; // NOI18N

    /** Generic Operator String rep. */
    public static final String STR_SCALAR_OPERATOR = "scalar_operator"; // NOI18N

    /** Join Operator String Rep. */
    public static final String STR_JOIN_OPERATOR = "join_operator"; // NOI18N

    /** Literal Operator String Rep. */
    public static final String STR_LITERAL_OPERATOR = "literal"; // NOI18N

    /** Case Operator String rep. */
    public static final String STR_CASE_OPERATOR = "case_operator"; // NOI18N

    /** Predicate String Rep */
    public static final String STR_PREDICATE = "predicate"; // NOI18N

    /** When String rep */
    public static final String STR_WHEN = "when"; // NOI18N

    /** Source column String rep */
    public static final String STR_SOURCE_COLUMN = "source_column"; // NOI18N

    /** Target column String rep */
    public static final String STR_TARGET_COLUMN = "target_column"; // NOI18N

    /** source DBModel */
    public static final String STR_SOURCE_DBMODEL = "source_dbmodel"; // NOI18N

    /** Target DBModel */
    public static final String STR_TARGET_DBMODEL = "target_dbmodel"; // NOI18N

    /** source table */
    public static final String STR_SOURCE_TABLE = "source_table"; // NOI18N

    /** Target table */
    public static final String STR_TARGET_TABLE = "target_table"; // NOI18N

    /** Visible predicate */
    public static final String STR_VISIBLE_PREDICATE = "visible_predicate"; // NOI18N

    /** Filter */
    public static final String STR_FILTER = "filter"; // NOI18N

    /** Visible literal */
    public static final String STR_VISIBLE_LITERAL = "visible_literal"; // NOI18N

    /** cast operator */
    public static final String STR_CAST_OPERATOR = "cast_operator"; // NOI18N

    /** runtime operator */
    public static final String STR_RUNTIME_ARGS = "runtime_args"; // NOI18N

    /** runtime input operator */
    public static final String STR_RUNTIME_INPUT = "runtime_input"; // NOI18N

    /** runtime output operator */
    public static final String STR_RUNTIME_OUTPUT = "runtime_output"; // NOI18N

    /** runtime dbmodel */
    public static final String STR_RUNTIME_DBMODEL = "runtime_dbmodel"; // NOI18N

    /** column ref */
    public static final String STR_COLUMN_REF = "column_ref"; // NOI18N

    public static final String STR_JOIN_VIEW = "join_view";

    public static final String STR_JOIN_TABLE = "join_table";

    public static final String STR_JOIN_TABLE_COLUMN = "join_table_column";

    public static final String STR_DATEDIFF_OPERATOR = "datediff_operator";

    public static final String STR_DATEADD_OPERATOR = "dateadd_operator";

    public static final String STR_CUSTOM_OPERATOR = "custom_operator"; // NOI18N

    // --------------------- End of Operator Type Names --------------------------

    // --------------------- Start of Operator Display Names --------------------------

    /** Display string for operator */
    public static final String DISPLAY_STR_GENERIC_OPERATOR = "operator";

    /** Display string for operator */
    public static final String DISPLAY_STR_CAST_OPERATOR = "cast operator";

    /** Display string for operator */
    public static final String DISPLAY_RUNTIME_ARGS = "runtime argument";

    /** Display string for join */
    public static final String DISPLAY_STR_JOIN_OPERATOR = "join operator";

    /** Display string for literal / visible literal */
    public static final String DISPLAY_STR_LITERAL_OPERATOR = "literal";

    /** Display string for case-when operator */
    public static final String DISPLAY_STR_CASE_OPERATOR = "case-when operator";

    /** Display string for predicate / visible predicate. */
    public static final String DISPLAY_STR_PREDICATE = "predicate";

    /** Display string for when element in case-when operator */
    public static final String DISPLAY_STR_WHEN = "when element";

    /** Display string for source column */
    public static final String DISPLAY_STR_SOURCE_COLUMN = "source column";

    /** Display string for target column */
    public static final String DISPLAY_STR_TARGET_COLUMN = "target column";

    /** Display string for source database model */
    public static final String DISPLAY_STR_SOURCE_DBMODEL = "source database model";

    /** Display string for target database model */
    public static final String DISPLAY_STR_TARGET_DBMODEL = "target database model";

    /** Display string for source table */
    public static final String DISPLAY_STR_SOURCE_TABLE = "source table";

    /** Display string for target table */
    public static final String DISPLAY_STR_TARGET_TABLE = "target table";

    /** Display string for filter */
    public static final String DISPLAY_STR_FILTER = "filter";

    public static final String DISPLAY_STR_RUNTIME_INPUT = "runtime input";

    public static final String DISPLAY_STR_RUNTIME_OUTPUT = "runtime output";

    public static final String DISPLAY_STR_RUNTIME_DBMODEL = "runtime database model";

    public static final String DISPLAY_STR_COLUMN_REF = "column";

    public static final String DISPLAY_STR_JOIN_VIEW = "join view";

    public static final String DISPLAY_STR_JOIN_TABLE = "join table";

    public static final String DISPLAY_STR_JOIN_TABLE_COLUMN = "join table column";

    public static final String DISPLAY_STR_DATEDIFF_OPERATOR = "datediff operator";

    public static final String DISPLAY_STR_DATEADD_OPERATOR = "dateadd operator";

    public static final String DISPLAY_STR_USER_FUNCTION = "user function";

    // --------------------- End of Operator Display Names --------------------------

    /** Maximum allowable arguments in an operator script. */
    public static final int MAX_SCRIPT_ARGUMENT_COUNT = 100;

    /** String representation of Operator "=" */
    public static final String OPERATOR_STR_EQUAL = "=";

    /** NULL string */
    public static final String STR_NULL = "NULL";

    // --------------------- End of Operator Constants --------------------------

    /**
     * constants for statement type in target table
     */
    public static final int INSERT_STATEMENT = 0;

    /** insert and update statement using merge syntax */
    public static final int INSERT_UPDATE_STATEMENT = 1;

    /** update statement by set column syntax */
    public static final int UPDATE_STATEMENT = 2;

    /** delete statement */
    public static final int DELETE_STATEMENT = 3;

    /**
     * constants for statement type in target table
     */
    public static final String STR_INSERT_STATEMENT = "insert";

    /** insert and update statement using merge syntax */
    public static final String STR_INSERT_UPDATE_STATEMENT = "insert_update";

    /** update statement by set column syntax */
    public static final String STR_UPDATE_STATEMENT = "update";

    /** delete statement */
    public static final String STR_DELETE_STATEMENT = "delete";

    public static final String EXTRACTION_FULL = "full";

    public static final String EXTRACTION_CONDITIONAL = "conditional";

    public static final String SOURCE_DB_MODEL_NAME_SUFFIX = "-Source";

    public static final String TARGET_DB_MODEL_NAME_SUFFIX = "-Target";
}

