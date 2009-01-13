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
package org.netbeans.modules.db.dataview.output;

import java.sql.Connection;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import javax.swing.table.TableModel;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.meta.DBConnectionFactory;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.netbeans.modules.db.dataview.meta.DBMetaDataFactory;
import org.netbeans.modules.db.dataview.meta.DBPrimaryKey;
import org.netbeans.modules.db.dataview.meta.DBTable;
import org.netbeans.modules.db.dataview.util.DataViewUtils;
import org.openide.util.NbBundle;

/**
 * Generates DML for editable resultset
 *
 * @author Ahimanikya Satapathy
 */
class SQLStatementGenerator {

    private DataViewDBTable tblMeta;
    private DataView dataView;

    public SQLStatementGenerator(DataView dataView) {
        this.dataView = dataView;
        this.tblMeta = dataView.getDataViewDBTable();
    }

    // TODO: Generated by default, can be overwitten by user, allow that
    String generateInsertStatement(Object[] insertedRow) throws DBException {
        StringBuilder insertSql = new StringBuilder();
        insertSql.append("INSERT INTO "); // NOI18N

        String colNames = " ("; // NOI18N
        String values = "";     // NOI18N
        String commaStr = ", "; // NOI18N
        boolean comma = false;
        for (int i = 0; i < insertedRow.length; i++) {
            DBColumn dbcol = tblMeta.getColumn(i);
            Object val = insertedRow[i];

            if (dbcol.isGenerated()) { // NOI18N
                continue;
            }

            if ((val == null || val.equals("<NULL>")) && !dbcol.isNullable()) { // NOI18N
                throw new DBException(NbBundle.getMessage(SQLStatementGenerator.class, "MSG_nullable_check"));
            }

            if (comma) {
                values += commaStr;
                colNames += commaStr;
            } else {
                comma = true;
            }

            // Check for Constant e.g <NULL>, <DEFAULT>, <CURRENT_TIMESTAMP> etc
            if (val != null && DataViewUtils.isSQLConstantString(val)) {
                String constStr = ((String) val).substring(1, ((String) val).length() - 1);
                values += constStr;
            } else { // ELSE literals
                values += insertedRow[i] == null ? " NULL " : "?"; // NOI18N
            }
            colNames += dbcol.getQualifiedName(true);
        }

        colNames += ")"; // NOI18N
        insertSql.append(tblMeta.getFullyQualifiedName(0, true) + colNames + " Values(" + values + ")"); // NOI18N

        return insertSql.toString();
    }

    String generateRawInsertStatement(Object[] insertedRow) throws DBException {
        StringBuilder rawInsertSql = new StringBuilder();
        rawInsertSql.append("INSERT INTO "); // NOI18N

        String rawcolNames = " ("; // NOI18N
        String rawvalues = "";  // NOI18N
        String commaStr = ", "; // NOI18N
        boolean comma = false;
        for (int i = 0; i < insertedRow.length; i++) {
            DBColumn dbcol = tblMeta.getColumn(i);
            Object val = insertedRow[i];

            if (dbcol.isGenerated()) { // NOI18N
                continue;
            }

            if ((val == null || val.equals("<NULL>")) && !dbcol.isNullable()) { // NOI18N
                throw new DBException(NbBundle.getMessage(SQLStatementGenerator.class, "MSG_nullable_check"));
            }

            if (comma) {
                rawvalues += commaStr;
                rawcolNames += commaStr;
            } else {
                comma = true;
            }

            // Check for Constant e.g <NULL>, <DEFAULT>, <CURRENT_TIMESTAMP> etc
            if (val != null && DataViewUtils.isSQLConstantString(val)) {
                String constStr = ((String) val).substring(1, ((String) val).length() - 1);
                rawvalues += constStr;
            } else { // ELSE literals
                rawvalues += getQualifiedValue(dbcol.getJdbcType(), insertedRow[i]);
            }
            rawcolNames += dbcol.getQualifiedName(false);
        }

        rawcolNames += ")"; // NOI18N
        rawInsertSql.append(tblMeta.getFullyQualifiedName(0, false) + rawcolNames + " \n\tVALUES (" + rawvalues + ")"); // NOI18N

        return rawInsertSql.toString();
    }

    String generateUpdateStatement(int row, Map<Integer, Object> changedRow, List<Object> values, List<Integer> types, TableModel tblModel) throws DBException {
        StringBuilder updateStmt = new StringBuilder();
        updateStmt.append("UPDATE ").append(tblMeta.getFullyQualifiedName(0, true)).append(" SET "); // NOI18N
        String commaStr = ", "; // NOI18N
        boolean comma = false;
        for (Integer col : changedRow.keySet()) {
            DBColumn dbcol = tblMeta.getColumn(col);
            Object value = changedRow.get(col);
            int type = dbcol.getJdbcType();

            if ((value == null || value.equals("<NULL>")) && !dbcol.isNullable()) { // NOI18N
                throw new DBException(NbBundle.getMessage(SQLStatementGenerator.class, "MSG_nullable_check"));
            }

            if (comma) {
                updateStmt.append(commaStr);
            } else {
                comma = true;
            }

            updateStmt.append(tblMeta.getQualifiedName(col, true));
            // Check for Constant e.g <NULL>, <DEFAULT>, <CURRENT_TIMESTAMP> etc
            if (value != null && DataViewUtils.isSQLConstantString(value)) {
                String constStr = ((String) value).substring(1, ((String) value).length() - 1);
                updateStmt.append(" = ").append(constStr);
            } else { // ELSE literals
                updateStmt.append(" = ?"); // NOI18N
                values.add(value);
                types.add(type);
            }
        }

        updateStmt.append(" WHERE "); // NOI18N
        generateWhereCondition(updateStmt, types, values, row, tblModel);
        return updateStmt.toString();
    }

    String generateUpdateStatement(int row, Map<Integer, Object> changedRow, TableModel tblModel) throws DBException {
        StringBuilder rawUpdateStmt = new StringBuilder();
        rawUpdateStmt.append("UPDATE ").append(tblMeta.getFullyQualifiedName(0, false)).append(" SET "); // NOI18N

        String commaStr = ", "; // NOI18N
        boolean comma = false;
        for (Integer col : changedRow.keySet()) {
            DBColumn dbcol = tblMeta.getColumn(col);
            Object value = changedRow.get(col);
            int type = dbcol.getJdbcType();

            if ((value == null || value.equals("<NULL>")) && !dbcol.isNullable()) { // NOI18N
                throw new DBException(NbBundle.getMessage(SQLStatementGenerator.class, "MSG_nullable_check"));
            }

            if (comma) {
                rawUpdateStmt.append(commaStr);
            } else {
                comma = true;
            }

            rawUpdateStmt.append(tblMeta.getQualifiedName(col, true));
            // Check for Constant e.g <NULL>, <DEFAULT>, <CURRENT_TIMESTAMP> etc
            if (value != null && DataViewUtils.isSQLConstantString(value)) {
                String constStr = ((String) value).substring(1, ((String) value).length() - 1);
                rawUpdateStmt.append(" = ").append(constStr);
            } else { // ELSE literals
                rawUpdateStmt.append(" = ").append(getQualifiedValue(type, value).toString());
            }
        }

        rawUpdateStmt.append(" WHERE "); // NOI18N
        generateWhereCondition(rawUpdateStmt, row, tblModel);
        return rawUpdateStmt.toString();
    }

    String generateDeleteStatement(List<Integer> types, List<Object> values, int rowNum, TableModel tblModel) {
        StringBuilder deleteStmt = new StringBuilder();
        deleteStmt.append("DELETE FROM ").append(tblMeta.getFullyQualifiedName(0, true)).append(" WHERE "); // NOI18N

        generateWhereCondition(deleteStmt, types, values, rowNum, tblModel);
        return deleteStmt.toString();
    }

    String generateDeleteStatement(int rowNum, TableModel tblModel) {
        StringBuilder rawDeleteStmt = new StringBuilder();
        rawDeleteStmt.append("DELETE FROM ").append(tblMeta.getFullyQualifiedName(0, false)).append(" WHERE "); // NOI18N

        generateWhereCondition(rawDeleteStmt, rowNum, tblModel);
        return rawDeleteStmt.toString();
    }

    // TODO: Support for FK, and other constraint and Index recreation.
    String generateCreateStatement(DBTable table) throws DBException, Exception {

        Connection conn = DBConnectionFactory.getInstance().getConnection(dataView.getDatabaseConnection());
        String msg = "";
        if (conn == null) {
            Throwable ex = DBConnectionFactory.getInstance().getLastException();
            if (ex != null) {
                msg = ex.getMessage();
            } else {
                msg = NbBundle.getMessage(SQLExecutionHelper.class, "MSG_connection_failure", dataView.getDatabaseConnection());
            }
            dataView.setErrorStatusText(new DBException(msg));
            throw new DBException(msg);
        }

        boolean isdb2 = table.getParentObject().getDBType() == DBMetaDataFactory.DB2 ? true : false;

        StringBuffer sql = new StringBuffer();
        List<DBColumn> columns = table.getColumnList();
        sql.append("CREATE TABLE ").append(table.getQualifiedName(false)).append(" ("); // NOI18N
        int count = 0;
        for (DBColumn col : columns) {
            if (count++ > 0) {
                sql.append(", "); // NOI18N
            }

            String typeName = col.getTypeName();
            sql.append(col.getQualifiedName(false)).append(" ");

            int scale = col.getScale();
            int precision = col.getPrecision();
            if (precision > 0 && DataViewUtils.isPrecisionRequired(col.getJdbcType(), isdb2)) {
                if (typeName.contains("(")) { // Handle MySQL Binary Type // NOI18N
                    sql.append(typeName.replace("(", "(" + precision)); // NOI18N
                } else {
                    sql.append(typeName).append("(").append(precision); // NOI18N
                    if (scale > 0 && DataViewUtils.isScaleRequired(col.getJdbcType())) {
                        sql.append(", ").append(scale).append(")"); // NOI18N
                    } else {
                        sql.append(")"); // NOI18N
                    }
                }
            } else {
                sql.append(typeName);
            }

            if (DataViewUtils.isBinary(col.getJdbcType()) && isdb2) {
                sql.append("  FOR BIT DATA "); // NOI18N
            }

            if (col.hasDefault()) {
                sql.append(" DEFAULT ").append(col.getDefaultValue()).append(" "); // NOI18N
            }

            if (!col.isNullable()) {
                sql.append(" NOT NULL"); // NOI18N
            }

            if (col.isGenerated()) {
                sql.append(" ").append(getAutoIncrementText(table.getParentObject().getDBType()));
            }
        }

        DBPrimaryKey pk = table.getPrimaryKey();
        if (pk != null) {
            count = 0;
            sql.append(", PRIMARY KEY ("); // NOI18N
            for (String col : pk.getColumnNames()) {
                if (count++ > 0) {
                    sql.append(", "); // NOI18N
                }
                sql.append(table.getQuoter().quoteIfNeeded(col));
            }
            sql.append(")"); // NOI18N
        }
        sql.append(")"); // NOI18N

        return sql.toString();
    }

    static String getCountSQLQuery(String queryString) {
        // User may type "FROM" in either lower, upper or mixed case
        String[] splitByFrom = queryString.toUpperCase().split("FROM"); // NOI18N
        queryString = queryString.substring(splitByFrom[0].length());

        String[] splitByOrderBy = queryString.toUpperCase().split("ORDER BY"); // NOI18N
        queryString = queryString.substring(0, splitByOrderBy[0].length());
        return "SELECT COUNT(*) " + queryString; // NOI18N
    }

    static String getCountAsSubQuery(String queryString) {
        String[] splitByOrderBy = queryString.toUpperCase().split("ORDER BY"); // NOI18N
        queryString = queryString.substring(0, splitByOrderBy[0].length());
        return "SELECT COUNT(*) FROM (" + queryString + ") C2668"; // NOI18N
    }

    private boolean addSeparator(boolean and, StringBuilder sql, String sep) {
        if (and) {
            sql.append(sep);
            return true;
        } else {
            return true;
        }
    }

    private void generateNameValue(int i, StringBuilder sql, Object value, List<Object> values, List<Integer> types) {
        sql.append(tblMeta.getQualifiedName(i, true));
        if (value != null) {
            values.add(value);
            types.add(tblMeta.getColumnType(i));
            sql.append(" = ? "); // NOI18N
        } else { // Handle NULL value in where condition
            sql.append(" IS NULL "); // NOI18N
        }
    }

    private void generateNameValue(int i, StringBuilder sql, Object value) {
        String columnName = tblMeta.getQualifiedName(i, false);
        int type = tblMeta.getColumnType(i);

        sql.append(columnName);
        if (value != null) {
            sql.append(" = ").append(getQualifiedValue(type, value)); // NOI18N
        } else { // Handle NULL value in where condition
            sql.append(" IS NULL"); // NOI18N
        }
    }

    private void generateWhereCondition(StringBuilder result, List<Integer> types, List<Object> values, int rowNum, TableModel model) {
        DBPrimaryKey key = tblMeta.geTable(0).getPrimaryKey();
        boolean keySelected = false;
        boolean and = false;

        if (key != null) {
            for (String keyName : key.getColumnNames()) {
                for (int i = 0; i < model.getColumnCount(); i++) {
                    String columnName = tblMeta.getColumnName(i);
                    if (columnName.equals(keyName)) {
                        Object val = dataView.getDataViewPageContext().getColumnData(rowNum, i);
                        if (val != null) {
                            keySelected = true;
                            and = addSeparator(and, result, " AND "); // NOI18N
                            generateNameValue(i, result, val, values, types);
                            break;
                        }
                    }
                }
            }
        }

        if (key == null || !keySelected) {
            for (int i = 0; i < model.getColumnCount(); i++) {
                Object val = dataView.getDataViewPageContext().getColumnData(rowNum, i);
                and = addSeparator(and, result, " AND "); // NOI18N
                generateNameValue(i, result, val, values, types);
            }
        }
    }

    private void generateWhereCondition(StringBuilder sql, int rowNum, TableModel model) {
        DBPrimaryKey key = tblMeta.geTable(0).getPrimaryKey();
        boolean keySelected = false;
        boolean and = false;

        if (key != null) {
            for (String keyName : key.getColumnNames()) {
                for (int i = 0; i < model.getColumnCount(); i++) {
                    String columnName = tblMeta.getColumnName(i);
                    if (columnName.equals(keyName)) {
                        Object val = dataView.getDataViewPageContext().getColumnData(rowNum, i);
                        if (val != null) {
                            keySelected = true;
                            and = addSeparator(and, sql, " AND "); // NOI18N
                            generateNameValue(i, sql, val);
                            break;
                        }
                    }
                }
            }
        }

        if (key == null || !keySelected) {
            for (int i = 0; i < model.getColumnCount(); i++) {
                Object val = dataView.getDataViewPageContext().getColumnData(rowNum, i);
                and = addSeparator(and, sql, " AND "); // NOI18N
                generateNameValue(i, sql, val);
            }
        }
    }

    private Object getQualifiedValue(int type, Object val) {
        if (val == null) {
            return "NULL"; // NOI18N
        }
        if (type == Types.BIT && !(val instanceof Boolean)) {
            return "b'" + val + "'"; // NOI18N
        } else if (DataViewUtils.isNumeric(type)) {
            return val;
        } else {
            return "'" + val + "'"; // NOI18N
        }
    }

    private String getAutoIncrementText(int dbType) throws Exception {
        switch (dbType) {
            case DBMetaDataFactory.MYSQL:
                return "AUTO_INCREMENT"; // NOI18N

            case DBMetaDataFactory.PostgreSQL:
                return "SERIAL"; // NOI18N

            case DBMetaDataFactory.SQLSERVER:
                return "IDENTITY"; // NOI18N
            default:
                return "GENERATED ALWAYS AS IDENTITY"; // NOI18N
        }
    }
}
