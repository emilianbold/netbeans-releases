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

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.logging.Logger;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.meta.DBException;

/**
 *
 * @author Ahimanikya Satapathy
 */
public class DBReadWriteHelper {

    private static Logger mLogger = Logger.getLogger(DBReadWriteHelper.class.getName());

    @SuppressWarnings(value = "fallthrough")
    public static Object readResultSet(ResultSet rs, int colType, int index) throws SQLException {
        switch (colType) {
            case Types.BIT:
            case Types.BOOLEAN: {
                boolean bdata = rs.getBoolean(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return new Boolean(bdata);
                }
            }
            case Types.TIME: {
                Time tdata = rs.getTime(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return tdata;
                }
            }
            case Types.DATE: {
                Date ddata = rs.getDate(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return ddata;
                }
            }
            case Types.TIMESTAMP:
            case -100: // -100 = Oracle timestamp
            {
                Timestamp tsdata = rs.getTimestamp(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return tsdata;
                }
            }
            case Types.BIGINT: {
                long ldata = rs.getLong(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return new Long(ldata);
                }
            }
            case Types.DOUBLE:
            case Types.FLOAT: {
                double fdata = rs.getDouble(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return new Double(fdata);
                }
            }
            case Types.REAL: {
                float rdata = rs.getFloat(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return new Float(rdata);
                }
            }
            case Types.DECIMAL:
            case Types.NUMERIC: {
                BigDecimal bddata = rs.getBigDecimal(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return bddata;
                }
            }
            case Types.INTEGER: {
                int idata = rs.getInt(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return new Integer(idata);
                }
            }
            case Types.SMALLINT: {
                short sidata = rs.getShort(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return new Short(sidata);
                }
            }
            case Types.TINYINT: {
                byte tidata = rs.getByte(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return new Byte(tidata);
                }
            }
            // JDBC/ODBC bridge JDK1.4 brings back -9 for nvarchar columns in
            // MS SQL Server tables.
            // -8 is ROWID in Oracle.
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case -9:
            case -8: {
                String sdata = rs.getString(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return sdata;
                }
            }
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY: {
                byte[] bdata = rs.getBytes(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    Byte[] internal = new Byte[bdata.length];
                    for (int i = 0; i < bdata.length; i++) {
                        internal[i] = new Byte(bdata[i]);
                    }
                    return BinaryToStringConverter.convertToString(internal, BinaryToStringConverter.BINARY, true);
                }
            }
            case Types.BLOB: {
                // We always get the BLOB, even when we are not reading the contents.
                // Since the BLOB is just a pointer to the BLOB data rather than the
                // data itself, this operation should not take much time (as opposed
                // to getting all of the data in the blob).
                Blob blob = rs.getBlob(index);

                if (rs.wasNull()) {
                    return null;
                }
                // BLOB exists, so try to read the data from it
                byte[] blobData = null;
                if (blob != null) {
                    blobData = blob.getBytes(1, 255);
                }
                Byte[] internal = new Byte[blobData.length];
                for (int i = 0; i < blobData.length; i++) {
                    internal[i] = new Byte(blobData[i]);
                }
                return BinaryToStringConverter.convertToString(internal, BinaryToStringConverter.HEX, false);
            }
            case Types.CLOB: {
                // We always get the CLOB, even when we are not reading the contents.
                // Since the CLOB is just a pointer to the CLOB data rather than the
                // data itself, this operation should not take much time (as opposed
                // to getting all of the data in the clob).
                Clob clob = rs.getClob(index);

                if (rs.wasNull()) {
                    return null;
                }
                // CLOB exists, so try to read the data from it
                if (clob != null) {
                    return clob.getSubString(1, 255);
                }
            }
            case Types.OTHER:
            default:
                return rs.getObject(index);
        }
    }

    public static void setAttributeValue(PreparedStatement ps, int index, int jdbcType, Object valueObj) throws DBException {
        Number numberObj = null;

        try {

            if (valueObj == null) {
                ps.setNull(index, jdbcType);
                return;
            }

            switch (jdbcType) {

                case Types.DOUBLE:
                    numberObj = (valueObj instanceof Number) ? (Number) valueObj : Double.valueOf(valueObj.toString());
                    ps.setDouble(index, numberObj.doubleValue());
                    break;

                case Types.DECIMAL:
                    numberObj = (valueObj instanceof Number) ? (Number) valueObj : new BigDecimal(valueObj.toString());
                    ps.setDouble(index, numberObj.doubleValue());
                    break;

                case Types.BIGINT:
                case Types.NUMERIC:
                    BigDecimal bigDec = new BigDecimal(valueObj.toString());
                    ps.setBigDecimal(index, bigDec);
                    break;

                case Types.FLOAT:
                case Types.REAL:
                    numberObj = (valueObj instanceof Number) ? (Number) valueObj : Float.valueOf(valueObj.toString());
                    ps.setFloat(index, numberObj.floatValue());
                    break;

                case Types.INTEGER:
                    numberObj = (valueObj instanceof Number) ? (Number) valueObj : Integer.valueOf(valueObj.toString());
                    ps.setInt(index, numberObj.intValue());
                    break;

                case Types.SMALLINT:
                case Types.TINYINT:
                    numberObj = (valueObj instanceof Number) ? (Number) valueObj : Short.valueOf(valueObj.toString());
                    ps.setShort(index, numberObj.shortValue());
                    break;

                case Types.TIMESTAMP:
                    long ts = DBReadWriteHelper.convertFromIso8601(valueObj.toString());
                    mLogger.info("EDIT093: **** timestamp **** " + ts);
                    try {
                        ps.setTimestamp(index, new java.sql.Timestamp(ts));
                    } catch (java.sql.SQLException e) {
                        ps.setDate(index, new java.sql.Date(ts));
                    }
                    break;

                case Types.DATE:
                    ts = DBReadWriteHelper.convertFromIso8601(valueObj.toString());
                    mLogger.info("EDIT093: **** timestamp **** " + ts);
                    ps.setDate(index, new java.sql.Date(ts));
                    break;

                case Types.TIME:
                    ts = DBReadWriteHelper.convertFromIso8601(valueObj.toString());
                    mLogger.info("EDIT093: **** timestamp **** " + ts);
                    ps.setTime(index, new Time(ts));
                    break;

                case Types.BINARY:
                case Types.VARBINARY:
                    ps.setBytes(index, valueObj.toString().getBytes());
                    break;

                case Types.LONGVARBINARY:
                case Types.BLOB:
                    byte[] byteval = valueObj.toString().getBytes();
                    ps.setBinaryStream(index, new ByteArrayInputStream(byteval), byteval.length);
                    break;

                case Types.CHAR:
                case Types.VARCHAR:
                    ps.setString(index, valueObj.toString());
                    break;

                case Types.CLOB:
                case Types.LONGVARCHAR:
                    String charVal = valueObj.toString();
                    ps.setCharacterStream(index, new StringReader(charVal), charVal.length());
                    break;

                default:
                    ps.setObject(index, valueObj, jdbcType);
            }
        } catch (Exception e) {
            mLogger.severe("Invalid Data for " + jdbcType + " type --" + e);
            throw new DBException("Invalid Data for " + jdbcType + " type ", e);
        }

    }

    public static boolean validate(Object valueObj, DBColumn col) throws DBException {
        int jdbcType = col.getJdbcType();
        try {
            if (valueObj == null) {
                return true;
            }

            switch (jdbcType) {
                case Types.DOUBLE:
                    Number numberObj = (valueObj instanceof Number) ? (Number) valueObj : Double.valueOf(valueObj.toString());
                    break;

                case Types.BIGINT:
                case Types.NUMERIC:
                case Types.DECIMAL:
                    numberObj = (valueObj instanceof Number) ? (Number) valueObj : new BigDecimal(valueObj.toString());
                    break;

                case Types.FLOAT:
                case Types.REAL:
                    numberObj = (valueObj instanceof Number) ? (Number) valueObj : Float.valueOf(valueObj.toString());
                    break;

                case Types.INTEGER:
                case Types.SMALLINT:
                case Types.TINYINT:
                    numberObj = (valueObj instanceof Number) ? (Number) valueObj : Integer.valueOf(valueObj.toString());
                    break;

                case Types.TIMESTAMP:
                case Types.DATE:
                case Types.TIME:
                    DBReadWriteHelper.convertFromIso8601(valueObj.toString());
                    break;

                case Types.CHAR:
                case Types.VARCHAR:
                    if (valueObj.toString().length() > col.getPrecision()) {
                        String colName = col.getQualifiedName();
                        String errMsg = "Too large data \'" + valueObj + "\' for column " + colName;
                        throw new DBException(errMsg);
                    }
            }
            return true;
        } catch (Exception e) {
            String type = DataViewUtils.getStdSqlType(jdbcType);
            String colName = col.getQualifiedName();
            String errMsg = "Invalid data \'" + valueObj + "\' for column " + colName + "\nEnter Valid data of " + type + " type";
            throw new DBException(errMsg, e);
        }
    }

    public static boolean isNullString(String str) {
        return (str == null || str.trim().length() == 0);
    }

    public static long convertFromIso8601(String isoDateTime) throws DBException {
        return getCalendar(isoDateTime).getTimeInMillis();
    }

    /**
     * returns a Gregorian Calendar of given iso date
     * 
     * @param isodate date in YYYY-MM-DDThh:mm:ss.sTZD format
     * @return GregorianCalendar
     */
    public static GregorianCalendar getCalendar(String isodate) throws DBException {
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
            throw new DBException("[" + ex.getMessage() + "] is not an integer");
        }

        return calendar;
    }

    /**
     * Make a data string oracle "safe" by escaping single quote marks which are used to
     * start and end strings.
     * 
     * @param value to be made oracle String safe.
     * @return String which is Oracle safe
     */
    public static String makeStringOracleSafe(String value) {
        if (value.indexOf("'") == -1) {
            return value;
        // nothing to escape
        }

        // the string contains a "'"
        StringBuilder newValue = new StringBuilder();
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

    private static boolean check(StringTokenizer st, String token) throws DBException {
        try {
            if (st.nextToken().equals(token)) {
                return true;
            }
            throw new DBException("Missing [" + token + "]");
        } catch (NoSuchElementException ex) {
            return false;
        }
    }
}
