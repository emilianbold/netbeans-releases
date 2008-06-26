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
package org.netbeans.modules.db.dataview.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.meta.DBForeignKey;
import org.netbeans.modules.db.dataview.meta.DBTable;

/**
 * Utility class supplying lookup and conversion methods for SQL-related tasks.
 * 
 * @author Ahimanikya Satapathy
 */
public class DataViewUtils {

    public static final int JDBCSQL_TYPE_UNDEFINED = -65535;
    private static HashMap<Integer, Integer> dataTypePrecedenceMap = new HashMap<Integer, Integer>();
    private static Map<String, String> JDBC_SQL_MAP = new HashMap<String, String>();
    private static Map<String, String> SQL_JDBC_MAP = new HashMap<String, String>();


    static {
        SQL_JDBC_MAP.put("array", String.valueOf(Types.ARRAY));
        SQL_JDBC_MAP.put("bigint", String.valueOf(Types.BIGINT));
        SQL_JDBC_MAP.put("binary", String.valueOf(Types.BINARY));
        SQL_JDBC_MAP.put("boolean", String.valueOf(Types.BOOLEAN));
        SQL_JDBC_MAP.put("bit", String.valueOf(Types.BIT));
        SQL_JDBC_MAP.put("blob", String.valueOf(Types.BLOB));
        SQL_JDBC_MAP.put("char", String.valueOf(Types.CHAR));
        SQL_JDBC_MAP.put("clob", String.valueOf(Types.CLOB));
        SQL_JDBC_MAP.put("date", String.valueOf(Types.DATE));
        SQL_JDBC_MAP.put("decimal", String.valueOf(Types.DECIMAL));
        SQL_JDBC_MAP.put("distinct", String.valueOf(Types.DISTINCT));
        SQL_JDBC_MAP.put("double", String.valueOf(Types.DOUBLE));
        SQL_JDBC_MAP.put("float", String.valueOf(Types.FLOAT));
        SQL_JDBC_MAP.put("integer", String.valueOf(Types.INTEGER));
        SQL_JDBC_MAP.put("longvarbinary", String.valueOf(Types.LONGVARBINARY));
        SQL_JDBC_MAP.put("longvarchar", String.valueOf(Types.LONGVARCHAR));
        SQL_JDBC_MAP.put("numeric", String.valueOf(Types.NUMERIC));
        SQL_JDBC_MAP.put("real", String.valueOf(Types.REAL));
        SQL_JDBC_MAP.put("smallint", String.valueOf(Types.SMALLINT));
        SQL_JDBC_MAP.put("time", String.valueOf(Types.TIME));
        SQL_JDBC_MAP.put("timestamp", String.valueOf(Types.TIMESTAMP));
        SQL_JDBC_MAP.put("tinyint", String.valueOf(Types.TINYINT));
        SQL_JDBC_MAP.put("varbinary", String.valueOf(Types.VARBINARY));
        SQL_JDBC_MAP.put("varchar", String.valueOf(Types.VARCHAR));
        SQL_JDBC_MAP.put("null", String.valueOf(Types.NULL));

        JDBC_SQL_MAP.put(String.valueOf(Types.ARRAY), "array");
        JDBC_SQL_MAP.put(String.valueOf(Types.BIGINT), "bigint");
        JDBC_SQL_MAP.put(String.valueOf(Types.BINARY), "binary");
        JDBC_SQL_MAP.put(String.valueOf(Types.BIT), "bit");
        JDBC_SQL_MAP.put(String.valueOf(Types.BLOB), "blob");
        JDBC_SQL_MAP.put(String.valueOf(Types.BOOLEAN), "boolean");
        JDBC_SQL_MAP.put(String.valueOf(Types.CHAR), "char");
        JDBC_SQL_MAP.put(String.valueOf(Types.CLOB), "clob");
        JDBC_SQL_MAP.put(String.valueOf(Types.DATE), "date");
        JDBC_SQL_MAP.put(String.valueOf(Types.DECIMAL), "decimal");
        JDBC_SQL_MAP.put(String.valueOf(Types.DISTINCT), "distinct");
        JDBC_SQL_MAP.put(String.valueOf(Types.DOUBLE), "double");
        JDBC_SQL_MAP.put(String.valueOf(Types.FLOAT), "float");
        JDBC_SQL_MAP.put(String.valueOf(Types.INTEGER), "integer");
        JDBC_SQL_MAP.put(String.valueOf(Types.LONGVARBINARY), "longvarbinary");
        JDBC_SQL_MAP.put(String.valueOf(Types.LONGVARCHAR), "longvarchar");
        JDBC_SQL_MAP.put(String.valueOf(Types.NUMERIC), "numeric");
        JDBC_SQL_MAP.put(String.valueOf(Types.REAL), "real");
        JDBC_SQL_MAP.put(String.valueOf(Types.SMALLINT), "smallint");
        JDBC_SQL_MAP.put(String.valueOf(Types.TIME), "time");
        JDBC_SQL_MAP.put(String.valueOf(Types.TIMESTAMP), "timestamp");
        JDBC_SQL_MAP.put(String.valueOf(Types.TINYINT), "tinyint");
        JDBC_SQL_MAP.put(String.valueOf(Types.VARBINARY), "varbinary");
        JDBC_SQL_MAP.put(String.valueOf(Types.VARCHAR), "varchar");
        JDBC_SQL_MAP.put(String.valueOf(Types.NULL), "null");
    }
    /**
     * Data types in decreasing order of precedence 1 is hightest
     */


    static {
        dataTypePrecedenceMap.put(new Integer(Types.DOUBLE), new Integer(1));
        dataTypePrecedenceMap.put(new Integer(Types.FLOAT), new Integer(2));
        dataTypePrecedenceMap.put(new Integer(Types.REAL), new Integer(3));
        dataTypePrecedenceMap.put(new Integer(Types.NUMERIC), new Integer(4));
        dataTypePrecedenceMap.put(new Integer(Types.DECIMAL), new Integer(5));
        dataTypePrecedenceMap.put(new Integer(Types.BIGINT), new Integer(6));
        dataTypePrecedenceMap.put(new Integer(Types.INTEGER), new Integer(7));
        dataTypePrecedenceMap.put(new Integer(Types.SMALLINT), new Integer(8));
        dataTypePrecedenceMap.put(new Integer(Types.TINYINT), new Integer(9));
        dataTypePrecedenceMap.put(new Integer(Types.BIT), new Integer(10));
        dataTypePrecedenceMap.put(new Integer(Types.TIMESTAMP), new Integer(11));
        dataTypePrecedenceMap.put(new Integer(Types.CLOB), new Integer(12));
        dataTypePrecedenceMap.put(new Integer(Types.VARCHAR), new Integer(13));
        dataTypePrecedenceMap.put(new Integer(Types.CHAR), new Integer(14));
        dataTypePrecedenceMap.put(new Integer(Types.VARBINARY), new Integer(15));
        dataTypePrecedenceMap.put(new Integer(Types.BINARY), new Integer(16));
    }

    public static String getStdSqlType(int dataType) throws IllegalArgumentException {
        Object o = JDBC_SQL_MAP.get(String.valueOf(dataType));
        if (o instanceof String) {
            return (String) o;
        }
        return null;
    }

    public static boolean isNumeric(int jdbcType) {
        switch (jdbcType) {
            case Types.BIT:
            case Types.BIGINT:
            case Types.BOOLEAN:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
            case Types.FLOAT:
            case Types.REAL:
            case Types.DOUBLE:
                return true;

            default:
                return false;
        }
    }

    public static boolean isNullString(String str) {
        return (str == null || str.trim().length() == 0);
    }

    public static void closeResources(PreparedStatement pstmt) {
        try {
            if (pstmt != null) {
                pstmt.close();
            }
        } catch (SQLException ex) {
            //ignore
        }
    }

    public static void closeResources(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException sqex) {
                // ignore
            }
        }
    }

    public static void closeResources(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException sqex) {
                // ignore
            }
        }
    }

    /**
     * Generates HTML-formatted String containing detailed information on the given
     * SQLDBColumn instance.
     *
     * @param column SQLDBColumn whose metadata are to be displayed in the tooltip
     * @return String containing HTML-formatted column metadata
     */
    public static String getColumnToolTip(DBColumn column) {
        boolean pk = column.isPrimaryKey();
        boolean fk = column.isForeignKey();
        boolean isNullable = column.isNullable();
        boolean generated = column.isGenerated();

        StringBuilder strBuf = new StringBuilder("<html> <table border=0 cellspacing=0 cellpadding=0 >");
        strBuf.append("<tr> <td>&nbsp; Name </td> <td> &nbsp; : &nbsp; <b>");
        strBuf.append(column.getName()).append("</b> </td> </tr>");
        strBuf.append("<tr> <td>&nbsp; Type </td> <td> &nbsp; : &nbsp; <b>");
        strBuf.append(DataViewUtils.getStdSqlType(column.getJdbcType())).append("</b> </td> </tr>");
        switch (column.getJdbcType()) {
            case Types.CHAR:
            case Types.VARCHAR:
                strBuf.append("<tr> <td>&nbsp; Length  </td> <td> &nbsp; : &nbsp; <b>");
                break;
            default:
                strBuf.append("<tr> <td>&nbsp; Precision  </td> <td> &nbsp; : &nbsp; <b>");
        }
        strBuf.append(column.getPrecision()).append("</b> </td> </tr>");

        switch (column.getJdbcType()) {
            case Types.CHAR:
            case Types.DATE:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TIME:
            case Types.TIMESTAMP:
            case Types.TINYINT:
            case Types.VARCHAR:
            case Types.VARBINARY:

                // Do nothing - scale is meaningless for these types.
                break;

            default:
                strBuf.append("<tr> <td>&nbsp; Scale </td> <td> &nbsp; : &nbsp; <b>");
                strBuf.append(column.getScale()).append("</b> </td> </tr>");
        }

        if (pk) {
            strBuf.append("<tr> <td>&nbsp; PK </td> <td> &nbsp; : &nbsp; <b> Yes </b> </td> </tr>");
        }
        if (fk) {
            strBuf.append("<tr> <td>&nbsp; FK  </td> <td> &nbsp; : &nbsp; <b>" + getForeignKeyString(column)).append("</b>").append("</td> </tr>");
        }

        if (!isNullable) {
            strBuf.append("<tr> <td>&nbsp; Nullable </td> <td> &nbsp; : &nbsp; <b> No </b> </td> </tr>");
        }

        if (generated) {
            strBuf.append("<tr> <td>&nbsp; Generated </td> <td> &nbsp; : &nbsp; <b> Yes </b> </td> </tr>");
        }
        strBuf.append("</table> </html>");
        return strBuf.toString();
    }

    public static String getForeignKeyString(DBColumn column) {
        String refString = column.getName() + " --> ";
        StringBuilder str = new StringBuilder(refString);
        DBTable table = column.getParentObject();
        List list = table.getForeignKeys();

        Iterator it = list.iterator();
        while (it.hasNext()) {
            DBForeignKey fk = (DBForeignKey) it.next();
            if (fk.contains(column)) {
                List pkColumnList = fk.getPKColumnNames();
                Iterator it1 = pkColumnList.iterator();
                while (it1.hasNext()) {
                    String pkColName = (String) it1.next();
                    str.append(pkColName);
                    if (it1.hasNext()) {
                        str.append(", ");
                    }
                }
            }
        }

        return str.toString();
    }

    /**
     * Generates HTML-formatted String containing detailed information on the given
     * SQLDBTable instance.
     *
     * @param table SQLDBTable whose metadata are to be displayed in the tooltip
     * @return String containing HTML-formatted table metadata
     */
    public static String getTableToolTip(DBTable table) {
        StringBuilder strBuf = new StringBuilder("<html> <table border=0 cellspacing=0 cellpadding=0 >");
        strBuf.append("<tr> <td>&nbsp; Table </td> <td> &nbsp; : &nbsp; <b>");
        strBuf.append(table.getName());
        strBuf.append("</b> </td> </tr>");

        String schema = table.getSchema();
        if (!DataViewUtils.isNullString(schema)) {
            strBuf.append("<tr> <td>&nbsp; Schema  </td> <td> &nbsp; : &nbsp; <b>");
            strBuf.append(schema.trim());
            strBuf.append("</b> </td> </tr>");
        }

        String catalog = table.getCatalog();
        if (!DataViewUtils.isNullString(catalog)) {
            strBuf.append("<tr> <td>&nbsp; Catalog  </td> <td> &nbsp; : &nbsp; <b>");
            strBuf.append(catalog.trim());
            strBuf.append("</b> </td> </tr>");
        }

        strBuf.append("</table> </html>");
        return strBuf.toString();
    }

    /* Private no-arg constructor; this class should not be instantiable. */
    private DataViewUtils() {
    }
}
