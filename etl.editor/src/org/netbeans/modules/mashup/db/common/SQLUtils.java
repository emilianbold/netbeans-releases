/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
