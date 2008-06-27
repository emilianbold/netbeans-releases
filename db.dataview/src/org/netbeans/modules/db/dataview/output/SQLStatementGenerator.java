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

import java.util.List;
import javax.swing.table.TableModel;
import org.netbeans.modules.db.dataview.logger.Localizer;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.netbeans.modules.db.dataview.meta.DBPrimaryKey;
import org.netbeans.modules.db.dataview.util.DataViewUtils;

/**
 * Generates DML for editable resultset
 *
 * @author Ahimanikya Satapathy
 */
class SQLStatementGenerator {

    private static transient final Localizer mLoc = Localizer.get();
    private DataViewDBTable tblMeta;

    public SQLStatementGenerator(DataViewDBTable tblMeta) {
        this.tblMeta = tblMeta;
    }

    String[] generateInsertStatement(Object[] insertedRow) throws DBException {
        StringBuilder insertSql = new StringBuilder();
        StringBuilder rawInsertSql = new StringBuilder();
        insertSql.append("INSERT INTO ");
        rawInsertSql.append(insertSql.toString());

        String colNames = " (";
        String values = "";
        String rawvalues = "";
        String commaStr = ", ";
        boolean comma = false;
        for (int i = 0; i < insertedRow.length; i++) {
            DBColumn dbcol = tblMeta.getColumn(i);
            if (insertedRow[i] == null && !dbcol.isNullable() && !dbcol.isGenerated()) {
                    String nbBundle58 = mLoc.t("RESC058: Please enter valid values for not nullable columns");
                    throw new DBException(nbBundle58.substring(15));
            }
            if (comma) {
                values += commaStr;
                rawvalues += commaStr;
                colNames += commaStr;
            } else {
                comma = true;
            }

            values += insertedRow[i] == null ? " NULL " : "?";
            rawvalues += getQualifiedValue(dbcol.getJdbcType(), insertedRow[i]);
            colNames += dbcol.getQualifiedName();
        }

        colNames += ") ";
        String tableName = tblMeta.getFullyQualifiedName(0);
        insertSql.append(tableName + colNames + " Values(" + values + ")");
        rawInsertSql.append(tableName.trim() + "\n\t" + colNames + "\nVALUES\n\t (" + rawvalues + ")");

        return new String[]{insertSql.toString(), rawInsertSql.toString()};
    }

    String[] generateUpdateStatement(int row, int col, Object value, List<Object> values, List<Integer> types, TableModel tblModel) throws DBException {
        DBColumn dbcol = tblMeta.getColumn(col);
        int type = dbcol.getJdbcType();

        if (!dbcol.isNullable() && value == null) {
            String nbBundle58 = mLoc.t("RESC058: Please enter valid values for not nullable columns");
            throw new DBException(nbBundle58.substring(15));
        }

        StringBuilder updateStmt = new StringBuilder();
        StringBuilder rawUpdateStmt = new StringBuilder();

        updateStmt.append("UPDATE ").append(tblMeta.getFullyQualifiedName(0)).append(" SET ");

        rawUpdateStmt.append(updateStmt.toString()).append(tblMeta.getQualifiedName(col)).append(" = ").append(getQualifiedValue(type, value).toString()).append(" WHERE ");

        updateStmt.append(tblMeta.getQualifiedName(col)).append(" = ? WHERE ");
        values.add(value);
        types.add(type);

        generateWhereCondition(updateStmt, rawUpdateStmt, types, values, row, tblModel);

        return new String[]{updateStmt.toString(), rawUpdateStmt.toString()};
    }

    String[] generateDeleteStatement(List<Integer> types, List<Object> values, int rowNum, TableModel tblModel) {
        StringBuilder deleteStmt = new StringBuilder();
        StringBuilder rawDeleteStmt = new StringBuilder();

        deleteStmt.append("DELETE FROM ").append(tblMeta.getFullyQualifiedName(0)).append(" WHERE ");
        rawDeleteStmt.append(deleteStmt.toString());

        generateWhereCondition(deleteStmt, rawDeleteStmt, types, values, rowNum, tblModel);
        return new String[]{deleteStmt.toString(), rawDeleteStmt.toString()};
    }

    static String getCountSQLQuery(String queryString) {
        // User may type "FROM" in either lower, upper or mixed case
        String[] splitByFrom = queryString.toUpperCase().split("FROM");
        return "SELECT COUNT(*) " + queryString.substring(splitByFrom[0].length());
    }

    private boolean addSeparator(boolean and, StringBuilder result, StringBuilder raw, String sep) {
        if (and) {
            result.append(sep);
            raw.append(sep);
            return true;
        } else {
            return true;
        }
    }

    private void generateNameValue(int i, StringBuilder result, StringBuilder raw, Object value, List<Object> values, List<Integer> types) {
        String columnName = tblMeta.getQualifiedName(i);
        int type = tblMeta.getColumnType(i);

        if(value != null) {
            values.add(value);
            types.add(type);
            result.append(columnName + " = ? ");
            raw.append(columnName).append(" = ").append(getQualifiedValue(type, value));
        } else { // Handle NULL value in where condition
            result.append(columnName + " IS NULL ");
            raw.append(columnName).append(" IS ").append(getQualifiedValue(type, value));
        }
    }

    private void generateWhereCondition(StringBuilder result, StringBuilder raw, List<Integer> types, List<Object> values, int rowNum, TableModel model) {
        DBPrimaryKey key = tblMeta.geTable(0).getPrimaryKey();
        boolean and = false;

        if (key != null) {
            for (String keyName : key.getColumnNames()) {
                and = addSeparator(and, result, raw, " AND ");
                for (int i = 0; i < model.getColumnCount(); i++) {
                    String columnName = tblMeta.getColumnName(i);
                    if (columnName.equals(keyName)) {
                        Object val = model.getValueAt(rowNum, i);
                        if (val != null) {
                            generateNameValue(i, result, raw, val, values, types);
                            break;
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < model.getColumnCount(); i++) {
                Object val = model.getValueAt(rowNum, i);
                and = addSeparator(and, result, raw, " AND ");
                generateNameValue(i, result, raw, val, values, types);
            }
        }
    }

    private Object getQualifiedValue(int type, Object val) {
        if(val == null) {
            return "NULL";
        }
        if (DataViewUtils.isNumeric(type)) {
            return val;
        } else {
            return "'" + val + "'";
        }
    }
}
