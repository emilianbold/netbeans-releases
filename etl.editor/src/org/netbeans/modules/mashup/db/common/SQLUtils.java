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
package org.netbeans.modules.mashup.db.common;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class supplying lookup and conversion methods for SQL-related tasks.
 * 
 * @author Sudhendra Seshachala
 * @version $Revision$
 */
public class SQLUtils {

    /** Designates undefined JDBC typecode. */
    public static final int JDBCSQL_TYPE_UNDEFINED = -65536;

    /* Map of String SQL types to JDBC typecodes */
    private static final Map SQL_JDBC_MAP = new HashMap();
    private static final Map JDBC_SQL_MAP = new HashMap();

    static {
        SQL_JDBC_MAP.put("numeric", String.valueOf(Types.NUMERIC));
        SQL_JDBC_MAP.put("time", String.valueOf(Types.TIME));
        SQL_JDBC_MAP.put("timestamp", String.valueOf(Types.TIMESTAMP));
        SQL_JDBC_MAP.put("varchar", String.valueOf(Types.VARCHAR));
    }

    static {
        JDBC_SQL_MAP.put(String.valueOf(Types.NUMERIC), "numeric");
        JDBC_SQL_MAP.put(String.valueOf(Types.TIME), "time");
        JDBC_SQL_MAP.put(String.valueOf(Types.TIMESTAMP), "timestamp");
        JDBC_SQL_MAP.put(String.valueOf(Types.VARCHAR), "varchar");
    }

    /**
     * Gets JDBC int type, if any, corresponding to the given SQL datatype string.
     * 
     * @param dataType SQL datatype whose equivalent JDBC int type is sought
     * @return java.sql.Types value equivalent to dataType
     */
    public static int getStdJdbcType(String dataType) {
        if (dataType == null) {
            dataType = "";
        }

        Object intStr = SQL_JDBC_MAP.get(dataType.toLowerCase().trim());
        try {
            return Integer.parseInt(intStr.toString());
        } catch (Exception e) {
            return JDBCSQL_TYPE_UNDEFINED;
        }
    }

    /**
     * Gets SQL datatype string, if any, corresponding to the given JDBC int value.
     * 
     * @param dataType SQL datatype whose corresopnding JDBC int type is sought
     * @return SQL datatype string corresponding to dataType, or null if no such datatype
     *         is mapped to dataType
     */
    public static String getStdSqlType(int dataType) {
        return (String) JDBC_SQL_MAP.get(String.valueOf(dataType));
    }

    /**
     * Gets the stdJdbcType attribute of the Database class
     * 
     * @param jdbcType instance of Types
     * @return The stdJdbcType value
     */
    public static synchronized boolean isStdJdbcType(int jdbcType) {
        return JDBC_SQL_MAP.containsKey(String.valueOf(jdbcType));
    }

    /**
     * Gets List of Strings representing standard SQL datatypes.
     * 
     * @return List of standard SQL datatypes.
     */
    public static List getStdSqlTypes() {
        return new ArrayList(SQL_JDBC_MAP.keySet());
    }

    /* Private no-arg constructor; this class should not be instantiable. */
    private SQLUtils() {
    }
}
