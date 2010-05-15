/*
 * BEGIN_HEADER - DO NOT EDIT
 * 
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * If applicable add the following below this CDDL HEADER,
 * with the fields enclosed by brackets "[]" replaced with
 * your own identifying information: Portions Copyright
 * [year] [name of copyright owner]
 */
package org.netbeans.modules.edm.editor.utils;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeMap;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.edm.model.EDMException;

/**
 * Utility class supplying lookup and conversion methods for SQL-related tasks.
 * 
 * @author Sudhendra Seshachala
 * @author Girish Patil
 *
 * @version 
 */
public class SQLUtils {

    static final String LOG_CATEGORY = SQLUtils.class.getName();
    public static final int VARCHAR_UNQUOTED = 3345336;
    private static final int ANYTYPE_CONSTANT = VARCHAR_UNQUOTED - 1;
    public static final String VARCHAR_UNQUOTED_STR = "varchar:unquoted";
    public static final int JDBCSQL_TYPE_UNDEFINED = -65535;
    private static HashMap dataTypePrecedenceMap = new HashMap();
    private static Map dbIdNameMap = new TreeMap();
    private static Map dbNameIdMap = new TreeMap();
    private static Map JDBC_SQL_MAP = new HashMap();
    private static Map SQL_JDBC_MAP = new HashMap();
    private static final List SUPPORTED_DATE_FORMATS = new ArrayList();
    private static final List SUPPORTED_DATE_PARTS = new ArrayList();
    private static final List SUPPORTED_INTERVAL_TYPES = new ArrayList();
    private static final List SUPPORTED_LITERAL_JDBC_TYPES = new ArrayList();
    private static final List SUPPORTED_CAST_JDBC_TYPES = new ArrayList();
    

    static {
        dbNameIdMap.put(DBConstants.ANSI92_STR, new Integer(DBConstants.ANSI92));
        dbNameIdMap.put(DBConstants.AXION_STR, new Integer(DBConstants.AXION));
    }
    

    static {
        dbIdNameMap.put(new Integer(DBConstants.ANSI92), DBConstants.ANSI92_STR);
        dbIdNameMap.put(new Integer(DBConstants.AXION), DBConstants.AXION_STR);
    }
    

    static {
        SUPPORTED_LITERAL_JDBC_TYPES.add("char");
        SUPPORTED_LITERAL_JDBC_TYPES.add("integer");
        SUPPORTED_LITERAL_JDBC_TYPES.add("numeric");
        SUPPORTED_LITERAL_JDBC_TYPES.add("timestamp");
        SUPPORTED_LITERAL_JDBC_TYPES.add("varchar");

        Collections.sort(SUPPORTED_LITERAL_JDBC_TYPES);
    }
    

    static {
        SUPPORTED_CAST_JDBC_TYPES.add("bigint");
        SUPPORTED_CAST_JDBC_TYPES.add("bit");
        SUPPORTED_CAST_JDBC_TYPES.add("char");
        SUPPORTED_CAST_JDBC_TYPES.add("date");
        SUPPORTED_CAST_JDBC_TYPES.add("double");
        SUPPORTED_CAST_JDBC_TYPES.add("decimal");
        SUPPORTED_CAST_JDBC_TYPES.add("float");
        SUPPORTED_CAST_JDBC_TYPES.add("integer");
        SUPPORTED_CAST_JDBC_TYPES.add("numeric");
        SUPPORTED_CAST_JDBC_TYPES.add("real");
        SUPPORTED_CAST_JDBC_TYPES.add("smallint");
        SUPPORTED_CAST_JDBC_TYPES.add("time");
        SUPPORTED_CAST_JDBC_TYPES.add("timestamp");
        SUPPORTED_CAST_JDBC_TYPES.add("tinyint");
        SUPPORTED_CAST_JDBC_TYPES.add("longvarchar");
        SUPPORTED_CAST_JDBC_TYPES.add("varchar");
        SUPPORTED_CAST_JDBC_TYPES.add("binary");
        SUPPORTED_CAST_JDBC_TYPES.add("varbinary");
        SUPPORTED_CAST_JDBC_TYPES.add("longvarbinary");

        Collections.sort(SUPPORTED_CAST_JDBC_TYPES);
    }
    

    static {
        SUPPORTED_INTERVAL_TYPES.add("second");
        SUPPORTED_INTERVAL_TYPES.add("minute");
        SUPPORTED_INTERVAL_TYPES.add("hour");
        SUPPORTED_INTERVAL_TYPES.add("day");
        SUPPORTED_INTERVAL_TYPES.add("week");
        SUPPORTED_INTERVAL_TYPES.add("month");
        SUPPORTED_INTERVAL_TYPES.add("quarter");
        SUPPORTED_INTERVAL_TYPES.add("year");

        Collections.sort(SUPPORTED_INTERVAL_TYPES);
    }
    

    static {
        SUPPORTED_DATE_FORMATS.add("MON DD YYYY HH:MIAM");
        SUPPORTED_DATE_FORMATS.add("MM/DD/YY");
        SUPPORTED_DATE_FORMATS.add("MM/DD/YYYY");
        SUPPORTED_DATE_FORMATS.add("YY.MM.DD");
        SUPPORTED_DATE_FORMATS.add("YYYY.MM.DD");
        SUPPORTED_DATE_FORMATS.add("DD/MM/YY");
        SUPPORTED_DATE_FORMATS.add("DD/MM/YYYY");
        SUPPORTED_DATE_FORMATS.add("DD.MM.YY");
        SUPPORTED_DATE_FORMATS.add("DD.MM.YYYY");
        SUPPORTED_DATE_FORMATS.add("DD-MM-YY");
        SUPPORTED_DATE_FORMATS.add("DD-MM-YYYY");
        SUPPORTED_DATE_FORMATS.add("DD MON YY");
        SUPPORTED_DATE_FORMATS.add("DD MON YYYY");
        SUPPORTED_DATE_FORMATS.add("MON DD, YY");
        SUPPORTED_DATE_FORMATS.add("MON DD, YYYY");
        SUPPORTED_DATE_FORMATS.add("HH:MI:SS");
        SUPPORTED_DATE_FORMATS.add("MM-DD-YY");
        SUPPORTED_DATE_FORMATS.add("MM-DD-YYYY");
        SUPPORTED_DATE_FORMATS.add("YY/MM/DD");
        SUPPORTED_DATE_FORMATS.add("YYYY/MM/DD");
        SUPPORTED_DATE_FORMATS.add("YYMMDD");
        SUPPORTED_DATE_FORMATS.add("YYYYMMDD");
        SUPPORTED_DATE_FORMATS.add("DD MON YYYY HH24:MI:SS.FF");
        SUPPORTED_DATE_FORMATS.add("HH24:MI:SS:FF");
        SUPPORTED_DATE_FORMATS.add("DD MON YYYY HH24:MI:SS");
        SUPPORTED_DATE_FORMATS.add("HH24:MI:SS");
        SUPPORTED_DATE_FORMATS.add("YYYY-MM-DD HH24:MI:SS.FF");
        SUPPORTED_DATE_FORMATS.add("YYYY-MM-DDTHH24:MI:SS");
        SUPPORTED_DATE_FORMATS.add("YYYYMMDDTHH24MISS");
        SUPPORTED_DATE_FORMATS.add("DD MON YYYY HH:MI:SS.FFFAM");
        SUPPORTED_DATE_FORMATS.add("DD/MM/YYYY HH:MI:SS.FFFAM");
        Collections.sort(SUPPORTED_DATE_FORMATS);
    }
    

    static {
        SUPPORTED_DATE_PARTS.add("WEEKDAY");
        SUPPORTED_DATE_PARTS.add("WEEKDAY3");
        SUPPORTED_DATE_PARTS.add("WEEKDAYFULL");
        SUPPORTED_DATE_PARTS.add("DAY");
        SUPPORTED_DATE_PARTS.add("MONTH");
        SUPPORTED_DATE_PARTS.add("MONTH3");
        SUPPORTED_DATE_PARTS.add("MONTHFULL");
        SUPPORTED_DATE_PARTS.add("YEAR");
        SUPPORTED_DATE_PARTS.add("HOUR");
        SUPPORTED_DATE_PARTS.add("HOUR12");
        SUPPORTED_DATE_PARTS.add("HOUR24");
        SUPPORTED_DATE_PARTS.add("MINUTE");
        SUPPORTED_DATE_PARTS.add("SECOND");
        SUPPORTED_DATE_PARTS.add("WEEK");
        SUPPORTED_DATE_PARTS.add("QUARTER");
        SUPPORTED_DATE_PARTS.add("MILLISECOND");
        SUPPORTED_DATE_PARTS.add("AMPM");
    }
    

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
        SQL_JDBC_MAP.put(VARCHAR_UNQUOTED_STR, String.valueOf(VARCHAR_UNQUOTED));
        SQL_JDBC_MAP.put("anytype", String.valueOf(ANYTYPE_CONSTANT));

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
        JDBC_SQL_MAP.put(String.valueOf(VARCHAR_UNQUOTED), VARCHAR_UNQUOTED_STR);
        JDBC_SQL_MAP.put(String.valueOf(ANYTYPE_CONSTANT), "anytype");
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
    private static final String dbLinkSql = "CREATE DATABASE LINK \"{0}\" ( DRIVER={1} URL={2} USERNAME={3} PASSWORD={4}  )";

    public static long convertFromIso8601(String isoDateTime) {
        return getCalendar(isoDateTime).getTimeInMillis();
    }

    public static String createPreparedStatement(String rawSql, Map attrMap, List paramList) {
        Iterator iter = attrMap.values().iterator();
        if (!iter.hasNext()) {
            return rawSql;
        }

        if (paramList != null) {
            List orderedSymbolList = SQLUtils.getOrderedSymbolList(rawSql, attrMap);
            paramList.clear();
            paramList.addAll(orderedSymbolList);
        }
        String processedSql = rawSql;

        do {
            RuntimeAttribute attr = (RuntimeAttribute) iter.next();
            boolean flag = false;
            do {
                processedSql = StringUtil.replaceFirst(processedSql, "?", "\\$" + attr.getAttributeName());
                if (!rawSql.equals(processedSql)) {
                    flag = true;
                } else {
                    flag = false;
                }

                rawSql = processedSql;
            } while (flag);
        } while (iter.hasNext());

        Logger.global.log(Level.FINE, ">>> Generated PreparedStatement: \n" + processedSql);
        return processedSql;
    }

    public static String createPreparedStatement(String rawSql, final List symbols, List orderedSymbols) {
        String symbol = null;
        boolean noMore = false;
        Iterator iter = symbols.iterator();
        if (!iter.hasNext()) {
            return rawSql;
        }

        if (orderedSymbols != null) {
            List orderedSymbolList = SQLUtils.getOrderedSymbolList(rawSql, symbols);
            orderedSymbols.clear();
            orderedSymbols.addAll(orderedSymbolList);
        }
        String processedSql = rawSql;

        do {
            symbol = (String) iter.next();
            if ((symbol != null) && (symbol.startsWith("$"))) {
                symbol = "\\" + symbol;
            }

            noMore = false;
            do {
                processedSql = StringUtil.replaceFirst(processedSql, "?", symbol);
                if (!rawSql.equals(processedSql)) {
                    noMore = true;
                } else {
                    noMore = false;
                }

                rawSql = processedSql;
            } while (noMore);
        } while (iter.hasNext());

        Logger.global.log(Level.FINE, ">>> Generated PreparedStatement: \n" + processedSql);
        return processedSql;
    }

    public static String replaceTableNameFromRuntimeArguments(String sqlStr, Map attrMap) {
        Iterator iter = attrMap.values().iterator();
        if (!iter.hasNext()) {
            return sqlStr;
        }
        List paramList = new ArrayList();
        List orderedSymbolList = SQLUtils.getOrderedSymbolList(sqlStr, attrMap);
        paramList.clear();
        paramList.addAll(orderedSymbolList);

        String processedSql = sqlStr;

        do {
            RuntimeAttribute attr = (RuntimeAttribute) iter.next();
            boolean flag = false;
            do {
                //processedSql = StringUtil.replaceFirst(processedSql, attr.getAttributeObject().toString(), "\\$" + attr.getAttributeName()+"\\$");
                String tmpStr = StringUtil.replaceTagString("$" + attr.getAttributeName() + "$", processedSql, attr.getAttributeObject().toString());
                if (tmpStr != null) {
                    processedSql = tmpStr;
                }
                if (!sqlStr.equals(processedSql)) {
                    flag = true;
                } else {
                    flag = false;
                }

                sqlStr = processedSql;
            } while (flag);
        } while (iter.hasNext());

        Logger.global.log(Level.FINE, ">>> Generated Statement: \n" + processedSql);

        return processedSql;
    }

    public static GregorianCalendar getCalendar(String isodate) {
        // YYYY-MM-DDThh:mm:ss.sTZD
        StringTokenizer st = new StringTokenizer(isodate, "-T:.+Z", true);

        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.clear();

        try {
            // Year
            if (st.hasMoreTokens()) {
                int year = Integer.parseInt(st.nextToken());
                calendar.set(Calendar.YEAR, year);
            } else {
                return calendar;
            }

            // Month
            if (check(st, "-") && (st.hasMoreTokens())) {
                int month = Integer.parseInt(st.nextToken()) - 1;
                calendar.set(Calendar.MONTH, month);
            } else {
                return calendar;
            }

            // Day
            if (check(st, "-") && (st.hasMoreTokens())) {
                int day = Integer.parseInt(st.nextToken());
                calendar.set(Calendar.DAY_OF_MONTH, day);
            } else {
                return calendar;
            }

            // Hour
            if (check(st, "T") && (st.hasMoreTokens())) {
                int hour = Integer.parseInt(st.nextToken());
                calendar.set(Calendar.HOUR_OF_DAY, hour);
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                return calendar;
            }

            // Minutes
            if (check(st, ":") && (st.hasMoreTokens())) {
                int minutes = Integer.parseInt(st.nextToken());
                calendar.set(Calendar.MINUTE, minutes);
            } else {
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                return calendar;
            }

            //
            // Not mandatory now
            //
            // Secondes
            if (!st.hasMoreTokens()) {
                return calendar;
            }

            String tok = st.nextToken();

            if (tok.equals(":")) { // seconds

                if (st.hasMoreTokens()) {
                    int secondes = Integer.parseInt(st.nextToken());
                    calendar.set(Calendar.SECOND, secondes);

                    if (!st.hasMoreTokens()) {
                        return calendar;
                    }

                    // frac sec
                    tok = st.nextToken();

                    if (tok.equals(".")) {
                        // bug fixed, thx to Martin Bottcher
                        String nt = st.nextToken();

                        while (nt.length() < 3) {
                            nt += "0";
                        }

                        nt = nt.substring(0, 3); // Cut trailing chars..

                        int millisec = Integer.parseInt(nt);

                        // int millisec = Integer.parseInt(st.nextToken()) * 10;
                        calendar.set(Calendar.MILLISECOND, millisec);

                        if (!st.hasMoreTokens()) {
                            return calendar;
                        }

                        tok = st.nextToken();
                    } else {
                        calendar.set(Calendar.MILLISECOND, 0);
                    }
                } else {
                    throw new RuntimeException("No secondes specified");
                }
            } else {
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
            }

            // Timezone
            if (!tok.equals("Z")) { // UTC

                if (!(tok.equals("+") || tok.equals("-"))) {
                    throw new RuntimeException("only Z, + or - allowed");
                }

                boolean plus = tok.equals("+");

                if (!st.hasMoreTokens()) {
                    throw new RuntimeException("Missing hour field");
                }

                int tzhour = Integer.parseInt(st.nextToken());
                int tzmin;

                if (check(st, ":") && (st.hasMoreTokens())) {
                    tzmin = Integer.parseInt(st.nextToken());
                } else {
                    throw new RuntimeException("Missing minute field");
                }

                // Since the time is represented at UTC (tz 0) format
                // we need to convert the local time to UTC timezone
                // for example if PST (-8) is 1.00 PM then UTC is 9.00 PM
                if (!plus) {
                    calendar.add(Calendar.HOUR, tzhour);
                    calendar.add(Calendar.MINUTE, tzmin);
                } else {
                    calendar.add(Calendar.HOUR, -tzhour);
                    calendar.add(Calendar.MINUTE, -tzmin);
                }
            }
        } catch (NumberFormatException ex) {
            throw new RuntimeException("[" + ex.getMessage() + "] is not an integer");
        }

        return calendar;
    }

    public static int getResultantDataType(int dataType1, int dataType2) {

        Integer dPrecedence1 = (Integer) dataTypePrecedenceMap.get(new Integer(dataType1));
        Integer dPrecedence2 = (Integer) dataTypePrecedenceMap.get(new Integer(dataType1));

        int retDataType;

        if (dPrecedence1 != null && dPrecedence2 != null) {
            retDataType = dPrecedence1.intValue() > dPrecedence2.intValue() ? dataType1 : dataType2;
        } else if (dPrecedence1 != null) {
            retDataType = dataType1;
        } else if (dPrecedence2 != null) {
            retDataType = dataType2;
        } else {
            retDataType = dataType1;
        }

        return retDataType;
    }

    public static int getStdJdbcType(String dataType) throws IllegalArgumentException {
        if (StringUtil.isNullString(dataType)) {
            throw new IllegalArgumentException("Must supply non-empty String value for dataType.");
        }

        Object intStr = SQL_JDBC_MAP.get(dataType.toLowerCase().trim());
        if (intStr instanceof String) {
            return Integer.parseInt((String) intStr);
        }
        return JDBCSQL_TYPE_UNDEFINED;
    }

    public static String getStdSqlType(int dataType) throws IllegalArgumentException {
        Object o = JDBC_SQL_MAP.get(String.valueOf(dataType));
        if (o instanceof String) {
            return (String) o;
        }
        return null;
    }

    public static List getStdSqlTypes() {
        return new ArrayList(JDBC_SQL_MAP.keySet());
    }

    public static List getSupportedDateParts() {
        return SUPPORTED_DATE_PARTS;
    }

    public static int getSupportedDBType(final String dbName) {
        String normalizedName = dbName.toUpperCase().trim();
        // WT #65169: DB2 eWay returns "Db29" as data type; map over to "DB2V7"
        if (normalizedName.startsWith(DBConstants.DB2_STR)) {
            normalizedName = DBConstants.DB2V7_STR;
        }

        Integer dbType = (Integer) dbNameIdMap.get(normalizedName);
        if (dbType != null) {
            return dbType.intValue();
        }

        return DBConstants.JDBC;
    }

    public static String getSupportedDBType(int dbType) {
        String dbName = (String) dbIdNameMap.get(new Integer(dbType));
        if (dbName != null) {
            return dbName;
        }
        return DBConstants.JDBC_STR;
    }

    public static Set getSupportedDBTypes() {
        return dbNameIdMap.keySet();
    }

    public static List getSupportedFormatTypes() {
        return SUPPORTED_DATE_FORMATS;
    }

    public static List getSupportedIntervalTypes() {
        return SUPPORTED_INTERVAL_TYPES;
    }

    public static List getSupportedLiteralTypes() {
        return SUPPORTED_LITERAL_JDBC_TYPES;
    }

    public static List getSupportedCastTypes() {
        return SUPPORTED_CAST_JDBC_TYPES;
    }

    public static synchronized boolean isStdJdbcType(int jdbcType) {
        return SQL_JDBC_MAP.containsValue(String.valueOf(jdbcType));
    }

    public static String makeStringOracleSafe(String value) {
        if (value.indexOf("'") == -1) {
            return value;
        // nothing to escape
        }

        // the string contains a "'"
        StringBuffer newValue = new StringBuffer();
        for (int i = 0; i < value.length(); i++) {
            char currChar = value.charAt(i);
            if (currChar == '\'') {
                // Append it twice to escape it
                newValue.append(currChar);
            }
            newValue.append(currChar);
        }
        return newValue.toString();
    }

    public static void populatePreparedStatement(PreparedStatement ps, Map attrMap, List paramList) throws EDMException {
        ListIterator iter = paramList.listIterator();
        try {
            while (iter.hasNext()) {
                String attrName = (String) iter.next();
                RuntimeAttribute attr = (RuntimeAttribute) attrMap.get(attrName);
                int index = iter.nextIndex();
                int jdbcType = attr.getJdbcType();
                Object valueObj = attr.getAttributeObject();
                Number numberObj = null;

                switch (jdbcType) {

                    case Types.DOUBLE:
                        numberObj = (valueObj instanceof Number) ? (Number) valueObj : Double.valueOf(valueObj.toString());
                        ps.setDouble(index, numberObj.doubleValue());
                        break;

                    case Types.FLOAT:
                        numberObj = (valueObj instanceof Number) ? (Number) valueObj : Float.valueOf(valueObj.toString());
                        ps.setFloat(index, numberObj.floatValue());
                        break;

                    case Types.INTEGER:
                        numberObj = (valueObj instanceof Number) ? (Number) valueObj : Integer.valueOf(valueObj.toString());
                        ps.setInt(index, numberObj.intValue());
                        break;

                    case Types.TIMESTAMP:
                        long ts = org.netbeans.modules.edm.editor.utils.SQLUtils.convertFromIso8601(valueObj.toString());
                        Logger.global.log(Level.FINE, "**** timestamp **** " + ts);
                        try {
                            ps.setTimestamp(index, new java.sql.Timestamp(ts));
                        } catch (java.sql.SQLException e) {
                            ps.setDate(index, new java.sql.Date(ts));
                        }
                        break;

                    case Types.CHAR:
                    case Types.VARCHAR:
                    default:
                        ps.setString(index, valueObj.toString());
                        break;
                }
            }
        } catch (Exception e) {
            String details = e.getMessage();
            if (StringUtil.isNullString(details)) {
                details = e.toString();
            }
            Logger.global.log(Level.FINE, details, e);
            throw new EDMException(details, e);
        }
    }

    public static Map getRuntimeInputNameValueMap(Map attribMap) {
        Map values = new HashMap();
        RuntimeAttribute ra = null;
        int jdbcType = 0;
        Object valueObj = null;
        Number numberObj = null;

        if (attribMap != null) {
            Set keys = attribMap.keySet();
            Iterator itr = keys.iterator();
            String name = null;

            while (itr.hasNext()) {
                name = (String) itr.next();
                ra = (RuntimeAttribute) attribMap.get(name);
                jdbcType = ra.getJdbcType();
                valueObj = ra.getAttributeObject();
                numberObj = null;

                switch (jdbcType) {

                    case Types.DOUBLE:
                        numberObj = (valueObj instanceof Number) ? (Number) valueObj : Double.valueOf(valueObj.toString());
                        values.put(name, numberObj);
                        break;

                    case Types.FLOAT:
                        numberObj = (valueObj instanceof Number) ? (Number) valueObj : Float.valueOf(valueObj.toString());
                        values.put(name, numberObj);
                        break;

                    case Types.INTEGER:
                        numberObj = (valueObj instanceof Number) ? (Number) valueObj : Integer.valueOf(valueObj.toString());
                        values.put(name, numberObj);
                        break;

                    case Types.TIMESTAMP:
                        long ts = org.netbeans.modules.edm.editor.utils.SQLUtils.convertFromIso8601(valueObj.toString());
                        Logger.global.log(Level.FINE, "**** timestamp **** " + ts);
                        try {
                            values.put(name, new java.sql.Timestamp(ts));
                        } catch (Exception e) {
                            values.put(name, new java.sql.Date(ts));
                        }
                        break;

                    case Types.CHAR:
                    case Types.VARCHAR:
                    default:
                        values.put(name, valueObj.toString());
                        break;
                }
            }
        }

        return values;
    }

    public static void setAttributeValue(PreparedStatement ps, int index, int jdbcType, Object valueObj) throws EDMException {
        Number numberObj = null;

        try {
            switch (jdbcType) {

                case Types.DOUBLE:
                    numberObj = (valueObj instanceof Number) ? (Number) valueObj : Double.valueOf(valueObj.toString());
                    ps.setDouble(index, numberObj.doubleValue());
                    break;

                case Types.FLOAT:
                    numberObj = (valueObj instanceof Number) ? (Number) valueObj : Float.valueOf(valueObj.toString());
                    ps.setFloat(index, numberObj.floatValue());
                    break;

                case Types.INTEGER:
                    numberObj = (valueObj instanceof Number) ? (Number) valueObj : Integer.valueOf(valueObj.toString());
                    ps.setInt(index, numberObj.intValue());
                    break;

                case Types.TIMESTAMP:
                    long ts = org.netbeans.modules.edm.editor.utils.SQLUtils.convertFromIso8601(valueObj.toString());
                    Logger.global.log(Level.FINE, "**** timestamp **** " + ts);
                    try {
                        ps.setTimestamp(index, new java.sql.Timestamp(ts));
                    } catch (java.sql.SQLException e) {
                        ps.setDate(index, new java.sql.Date(ts));
                    }
                    break;

                case Types.CHAR:
                case Types.VARCHAR:
                default:
                    ps.setString(index, valueObj.toString());
                    break;
            }
        } catch (Exception e) {
            String details = e.getMessage();
            if (StringUtil.isNullString(details)) {
                details = e.toString();
            }
            throw new EDMException(details, e);
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
            case Types.TIME:
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

    private static boolean check(StringTokenizer st, String token) throws RuntimeException {
        try {
            if (st.nextToken().equals(token)) {
                return true;
            }
            throw new RuntimeException("Missing [" + token + "]");
        } catch (NoSuchElementException ex) {
            return false;
        }
    }

    private static List getOrderedSymbolList(String rawSql, final List symbolList) {
        Map map = new TreeMap();
        String symbolName = null;

        if ((rawSql != null) && (symbolList != null)) {
            Iterator iter = symbolList.iterator();

            int pos = -1;
            int indexFrom = 0;

            while (iter.hasNext()) {
                symbolName = (String) iter.next();
                indexFrom = 0;
                boolean morePresent = true;
                while (morePresent) {
                    pos = rawSql.indexOf(symbolName, indexFrom);
                    if (pos >= 0) {
                        map.put(new Integer(pos), symbolName);
                        indexFrom = pos + symbolName.length();
                    } else {
                        morePresent = false;
                    }
                }
            }
        }

        return new ArrayList(map.values());
    }

    private static List getOrderedSymbolList(String rawSql, Map attrMap) {
        Map map = new TreeMap();

        if ((rawSql != null) && (attrMap != null)) {
            Iterator iter = attrMap.values().iterator();

            RuntimeAttribute attr = null;

            int pos = -1;
            int indexFrom = 0;

            while (iter.hasNext()) {
                attr = (RuntimeAttribute) iter.next();
                indexFrom = 0;
                boolean morePresent = true;
                while (morePresent) {
                    pos = rawSql.indexOf("$" + attr.getAttributeName(), indexFrom);
                    if (pos >= 0) {
                        map.put(new Integer(pos), attr.getAttributeName());
                        indexFrom = pos + attr.getAttributeName().length();
                    } else {
                        morePresent = false;
                    }
                }
            }
        }

        return new ArrayList(map.values());
    }

    private static String quoteString(String strToWrap) {
        return "'" + strToWrap + "'";
    }

    public static String createDBLinkSQL(String tableName, String driver, String url, String user, String password) {
        MessageFormat form = new MessageFormat(dbLinkSql);
        String qt = "'";
        Object[] args = new Object[]{tableName, quoteString(driver), quoteString(url), quoteString(user), quoteString(password)};
        return form.format(args);
    }

    private SQLUtils() {
    }
}
