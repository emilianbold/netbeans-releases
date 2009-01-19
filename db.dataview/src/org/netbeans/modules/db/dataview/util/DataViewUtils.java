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
import org.openide.util.NbBundle;

/**
 * Utility class supplying lookup and conversion methods for SQL-related tasks.
 * 
 * @author Ahimanikya Satapathy
 */
public class DataViewUtils {

    public static final int JDBCSQL_TYPE_UNDEFINED = -65535;
    private static Map<String, String> JDBC_SQL_MAP = new HashMap<String, String>();


    static {
        JDBC_SQL_MAP.put(String.valueOf(Types.ARRAY), "array"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.STRUCT), "struct"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.BIGINT), "bigint"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.BINARY), "binary"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.BIT), "bit"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.BLOB), "blob"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.BOOLEAN), "boolean"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.CHAR), "char"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.CLOB), "clob"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.DATE), "date"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.DECIMAL), "decimal"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.DISTINCT), "distinct"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.DOUBLE), "double"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.FLOAT), "float"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.INTEGER), "integer"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.LONGVARBINARY), "longvarbinary"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.LONGVARCHAR), "longvarchar"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.NUMERIC), "numeric"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.REAL), "real"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.SMALLINT), "smallint"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.TIME), "time");  // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.TIMESTAMP), "timestamp");  // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.TINYINT), "tinyint"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.VARBINARY), "varbinary"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.VARCHAR), "varchar"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.JAVA_OBJECT), "java_object"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.OTHER), "other"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.REF), "ref"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(Types.DATALINK), "datalink"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(-9 /* NVARCHAR */), "nvarchar"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(-8 /* ROWID */), "rowid"); // NOI18N
        JDBC_SQL_MAP.put(String.valueOf(-15 /*NCHAR */), "nchar"); // NOI18N

        JDBC_SQL_MAP.put(String.valueOf(Types.NULL), "null"); // NOI18N
    }

    public static String getStdSqlType(int dataType) throws IllegalArgumentException {
        Object o = JDBC_SQL_MAP.get(String.valueOf(dataType));
        if (o instanceof String) {
            return (String) o;
        }
        return "OTHER"; // NOI18N
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

    public static boolean isPrecisionRequired(int jdbcType, boolean isdb2) {
        if (isdb2 && jdbcType == Types.BLOB || jdbcType == Types.CLOB) {
            return true;
        } else {
            return isPrecisionRequired(jdbcType);
        }
    }

    public static boolean isPrecisionRequired(int jdbcType) {
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
            case Types.DATE:
            case Types.TIMESTAMP:
            case Types.JAVA_OBJECT:
            case Types.LONGVARCHAR:
            case Types.LONGVARBINARY:
            case Types.BLOB:
            case Types.CLOB:
            case Types.ARRAY:
            case Types.STRUCT:
            case Types.DISTINCT:
            case Types.REF:
            case Types.DATALINK:
                return false;

            default:
                return true;
        }
    }

    public static boolean isScaleRequired(int type) {
        switch (type) {
            case java.sql.Types.DECIMAL:
            case java.sql.Types.NUMERIC:
                return true;
            default:
                return false;
        }
    }

    public static boolean isBinary(int jdbcType) {
        switch (jdbcType) {
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return true;
            default:
                return false;
        }
    }

    public static boolean isString(int jdbcType) {
        switch (jdbcType) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case -9:  //NVARCHAR
            case -8:  //ROWID
            case -15: //NCHAR
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
        StringBuilder strBuf = new StringBuilder("<html> " +
                "<table border=0 cellspacing=0 cellpadding=0 >");

        strBuf.append("<tr> <td>&nbsp;").append(NbBundle.getMessage(DataViewUtils.class, "TOOLTIP_column_name")).append("</td> <td> &nbsp; : &nbsp; <b>");
        strBuf.append(column.getName()).append("</b> </td> </tr>");

        strBuf.append("<tr> <td>&nbsp;").append(NbBundle.getMessage(DataViewUtils.class, "TOOLTIP_column_type")).append("</td> <td> &nbsp; : &nbsp; <b>");

        strBuf.append(DataViewUtils.getStdSqlType(column.getJdbcType()).toUpperCase()).append("</b> </td> </tr>");

        switch (column.getJdbcType()) {
            case Types.CHAR:
            case Types.VARCHAR:
                strBuf.append("<tr> <td>&nbsp;").append(NbBundle.getMessage(DataViewUtils.class, "TOOLTIP_column_length")).append("</td> <td> &nbsp; : &nbsp; <b>");
                break;
            default:
                strBuf.append("<tr> <td>&nbsp;").append(NbBundle.getMessage(DataViewUtils.class, "TOOLTIP_column_precision")).append("</td> <td> &nbsp; : &nbsp; <b>");
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
                strBuf.append("<tr> <td>&nbsp;").append(NbBundle.getMessage(DataViewUtils.class, "TOOLTIP_column_scale")).append("</td> <td> &nbsp; : &nbsp; <b>");
                strBuf.append(column.getScale()).append("</b> </td> </tr>");
        }

        if (pk) {
            strBuf.append("<tr> <td>&nbsp;").append(NbBundle.getMessage(DataViewUtils.class, "TOOLTIP_column_PK")).append("</td> <td> &nbsp; : &nbsp; <b> Yes </b> </td> </tr>");
        }
        if (fk) {
            strBuf.append("<tr> <td>&nbsp;").append(NbBundle.getMessage(DataViewUtils.class, "TOOLTIP_column_FK")).append("</td> <td> &nbsp; : &nbsp; <b>" + getForeignKeyString(column)).append("</b>").append("</td> </tr>");
        }

        if (!isNullable) {
            strBuf.append("<tr> <td>&nbsp;").append(NbBundle.getMessage(DataViewUtils.class, "TOOLTIP_column_nullable")).append("</td> <td> &nbsp; : &nbsp; <b> No </b> </td> </tr>");
        }

        if (generated) {
            strBuf.append("<tr> <td>&nbsp;").append(NbBundle.getMessage(DataViewUtils.class, "TOOLTIP_column_generated")).append("</td> <td> &nbsp; : &nbsp; <b> Yes </b> </td> </tr>");
        }
        strBuf.append("</table> </html>");
        return strBuf.toString();
    }

    public static String getForeignKeyString(DBColumn column) {
        String refString = column.getName() + " --> "; // NOI18N
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
                        str.append(", "); // NOI18N
                    }
                }
            }
        }

        return str.toString();
    }

    /* Private no-arg constructor; this class should not be instantiable. */
    private DataViewUtils() {
    }
}
