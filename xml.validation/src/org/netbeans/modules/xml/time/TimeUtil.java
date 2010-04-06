/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
package org.netbeans.modules.xml.time;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import static org.netbeans.modules.xml.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.04.14
 */
public final class TimeUtil {

    private TimeUtil() {}

    public static Deadline parseDeadline(String value, boolean throwException) throws IllegalArgumentException {
//out("PARSE Deadline: " + value);

        if (value == null || value.trim().length() == 0) {
            return throwException("FIX_Empty_Value", throwException); // NOI18N
        }
        value = value.trim();

        // # 178306
        if (value.contains("(") && value.contains(")") && value.contains(":") && !value.startsWith("'")) { // NOI18N
            return null;
        }
        if (value.equals("'")) { // NOI18N
            return throwException("FIX_Bad_Value", throwException); // NOI18N
        }
        if ( !value.startsWith("'") && value.startsWith("$")) { // NOI18N
//out("   return 1");
            return null;
        }
        // # 171480
        if (value.contains("${")) { // NOI18N
            return null;
        }
        if ( !value.startsWith(QUOTE) && !value.endsWith(QUOTE)) {
//out("   return 2");
            return throwException("FIX_Quotes", throwException); // NOI18N
        }
        value = removeQuotes(value);

        Date date = parseDate(prepare(value), throwException);
//out("date: " + date);

        if (date == null) {
//out("   return 3");
            return null;
        }
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        return new Deadline(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), second(calendar.get(Calendar.SECOND), calendar.get(Calendar.MILLISECOND)), timeZone(date));
    }

    private static Date parseDate(String value, boolean throwException) throws IllegalArgumentException {
//out("value: " + value);
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSZ"); // NOI18N
            format.setLenient(true);
            return format.parse(value);
        }
        catch (ParseException e) {
            try {
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ"); // NOI18N
                format.setLenient(true);
                return format.parse(value);
            }
            catch (ParseException ee) {
                if (throwException) {
                    throw new IllegalArgumentException(e.getMessage());
                }
            }
        }
        return null;
    }

    private static String prepare(String value) {
        value = value.replace(T_DELIM.charAt(0), ' ');
        value = addTimeZone(value);
        int k = value.length();

        if (k < 2 + 2 + 2) {
            return value;
        }
        if (value.charAt(k - 2 - 1) != ':') {
            return value;
        }
        if (value.charAt(k - 2 - 2 - 2) != '-' && value.charAt(k - 2 - 2 - 2) != '+') {
            return value;
        }
        return value.substring(0, value.length() - 2 - 1) + value.substring(value.length() - 2, value.length());
    }

    private static String addTimeZone(String value) {
        int k = value.length();

        if (k >= 2 + 2 + 2 && value.charAt(k - 2 - 1) == ':' && ((value.charAt(k - 2 - 2 - 2) == '-' || value.charAt(k - 2 - 2 - 2) == '+'))) {
            return value;
        }
        return value + "+00:00"; // NOI18N
    }

    private static String timeZone(Date date) {
        return timeZone(new SimpleDateFormat("Z").format(date)); // NOI18N
    }

    private static String timeZone(String value) {
        if (value == null) {
            return ""; // NOI18N
        }
        if (value.length() < 2 + 2 + 1) {
            return value;
        }
        return value.substring(0, value.length() - 2) + ":" + value.substring(value.length() - 2, value.length());
    }

    private static double second(int second, int millisecond) {
        return ((double) second) + (((double) millisecond) / STO);
    }

    public static Duration parseDuration(String value, boolean throwException) {
        return parseDuration(value, throwException, true);
    }

    public static Duration parseDuration(String value, boolean throwException, boolean checkQuote) throws IllegalArgumentException {
//out();
//out("PARSE duration: " + value);
        Object years = ZERO_INT;
        Object months = ZERO_INT;
        Object days = ZERO_INT;
        Object hours = ZERO_INT;
        Object minutes = ZERO_INT;
        Object seconds = ZERO_DBL;
        boolean isNegative = false;
//out("1");
        if (value == null || value.trim().length() == 0) {
            return throwException("FIX_Empty_Value", throwException, years, months, days, hours, minutes, seconds, isNegative); // NOI18N
        }
        value = value.trim();

        // # 178306
        if (value.contains("(") && value.contains(")") && value.contains(":") && !value.startsWith("'P")) { // NOI18N
            return null;
        }
//out("    checkQuote: " + checkQuote);
//out("throwException: " + throwException);

        if (checkQuote) {
            if (!value.startsWith(QUOTE) && !value.endsWith(QUOTE)) {
                if (throwException && !value.startsWith("$")) { // NOI18N
                    return throwException("FIX_Invalid_Value", value, throwException, years, months, days, hours, minutes, seconds, isNegative); // NOI18N
                }
                return new Duration(years, months, days, hours, minutes, seconds, isNegative);
            }
            if (value.length() == 1 && value.startsWith(QUOTE)) {
                return throwException("FIX_Invalid_Value", value, throwException, years, months, days, hours, minutes, seconds, isNegative); // NOI18N
            }
            if (value.startsWith(QUOTE) && !value.endsWith(QUOTE)) {
                return throwException("FIX_Invalid_Value", value, throwException, years, months, days, hours, minutes, seconds, isNegative); // NOI18N
            }
            if (!value.startsWith(QUOTE) && value.endsWith(QUOTE)) {
                return throwException("FIX_Invalid_Value", value, throwException, years, months, days, hours, minutes, seconds, isNegative); // NOI18N
            }
        }
        value = removeQuotes(value);

        if (value.length() == 0) {
            return throwException("FIX_Empty_Value", throwException, years, months, days, hours, minutes, seconds, isNegative); // NOI18N
        }
//out("3");
        int k;
        boolean wasDesignator = false;
        boolean wasDesignatorT = false;

        // minus
        if (value.charAt(0) == MINUS.charAt(0)) {
            isNegative = true;
            value = value.substring(1);
        }
        // P
        if (value.charAt(0) != P_DELIM.charAt(0)) {
            // # 174989
            if (value.length() >= 2+1 && value.charAt(0) == '$' && value.charAt(1) == '{') { // NOI18N
                return null;
            }
            return throwException("FIX_P_symbol", throwException, years, months, days, hours, minutes, seconds, isNegative); // NOI18N
        }
        value = value.substring(1);

        // years
        k = value.indexOf(Y_DELIM);

        if (k != -1) {
            wasDesignator = true;
            years = parseInt(value.substring(0, k));

            if (years == null) {
                return throwException("FIX_Year", throwException, years, months, days, hours, minutes, seconds, isNegative); // NOI18N
            }
            value = value.substring(k + 1);
        }
        // months
        k = value.indexOf(M_DELIM);
        int t = value.indexOf(T_DELIM);
//out("4");

        if (k != -1) {
            months = parseInt(value.substring(0, k));

            if (months == null) {
                if (t == -1 || k < t) {
                    return throwException("FIX_Months", throwException, years, months, days, hours, minutes, seconds, isNegative); // NOI18N
                }
                months = ZERO_INT;
            }
            else {
                wasDesignator = true;
                value = value.substring(k + 1);
            }
        }
        // days
        k = value.indexOf(D_DELIM);

        if (k != -1) {
            wasDesignator = true;
            days = parseInt(value.substring(0, k));

            if (days == null) {
                return throwException("FIX_Days", throwException, years, months, days, hours, minutes, seconds, isNegative); // NOI18N
            }
            value = value.substring(k + 1);
        }
        if (value.length() == 0) {
            if ( !wasDesignator) {
                return throwException("FIX_Designator", throwException, years, months, days, hours, minutes, seconds, isNegative); // NOI18N
            }
            return new Duration(years, months, days, hours, minutes, seconds, isNegative);
        }
//out("T: " + value);
        // T
        if (value.charAt(0) != T_DELIM.charAt(0)) {
            return throwException("FIX_T_symbol", throwException, years, months, days, hours, minutes, seconds, isNegative); // NOI18N
        }
        value = value.substring(1);

        // hours
        k = value.indexOf(H_DELIM);

        if (k != -1) {
            wasDesignator = true;
            wasDesignatorT = true;
            hours = parseInt(value.substring(0, k));

            if (hours == null) {
                return throwException("FIX_Hours", throwException, years, months, days, hours, minutes, seconds, isNegative); // NOI18N
            }
            value = value.substring(k + 1);
        }
        // minutes
        k = value.indexOf(M_DELIM);

        if (k != -1) {
            wasDesignator = true;
            wasDesignatorT = true;
            minutes = parseInt(value.substring(0, k));

            if (minutes == null) {
                return throwException("FIX_Minutes", throwException, years, months, days, hours, minutes, seconds, isNegative); // NOI18N
            }
            value = value.substring(k + 1);
        }
        // seconds
        k = value.indexOf(S_DELIM);

        if (k != -1) {
            wasDesignator = true;
            wasDesignatorT = true;
            seconds = parseDouble(value.substring(0, k));

            if (seconds == null) {
                return throwException("FIX_Seconds", throwException, years, months, days, hours, minutes, seconds, isNegative); // NOI18N
            }
            value = value.substring(k + 1);
        }
        if (!wasDesignatorT) {
            return throwException("FIX_Absent_T_symbol", throwException, years, months, days, hours, minutes, seconds, isNegative); // NOI18N
        }
        if (!wasDesignator) {
            return throwException("FIX_Designator", throwException, years, months, days, hours, minutes, seconds, isNegative); // NOI18N
        }
//out("6: " + years + " " + months + " " + days + " " + hours + " " + minutes + " " + seconds);
        return new Duration(years, months, days, hours, minutes, seconds, isNegative);
    }

    private static Deadline throwException(String key, boolean throwException) {
        if (throwException) {
            throw new IllegalArgumentException(i18n(TimeUtil.class, key));
        }
        return null;
    }

    private static Duration throwException(String key, boolean throwException, Object years, Object months, Object days, Object hours, Object minutes, Object seconds, boolean isNegative) {
        return throwExceptionMsg(i18n(TimeUtil.class, key), throwException, years, months, days, hours, minutes, seconds, isNegative);
    }

    private static Duration throwException(String key, String param, boolean throwException, Object years, Object months, Object days, Object hours, Object minutes, Object seconds, boolean isNegative) {
        return throwExceptionMsg(i18n(TimeUtil.class, key, param), throwException, years, months, days, hours, minutes, seconds, isNegative);
    }

    private static Duration throwExceptionMsg(String message, boolean throwException, Object years, Object months, Object days, Object hours, Object minutes, Object seconds, boolean isNegative) {
//out("ERROR: " + message);
        if (throwException) {
            throw new IllegalArgumentException(message);
        }
        return new Duration(years, months, days, hours, minutes, seconds, isNegative);
    }

    public static String addQuotes(String value) {
        if (value == null) {
            return value;
        }
        if ( !value.startsWith(QUOTE)) {
            value = QUOTE + value;
        }
        if ( !value.endsWith(QUOTE)) {
            value = value + QUOTE;
        }
        return value;
    }

    public static String removeQuotes(String value) {
        if (value == null) {
            return null;
        }
        if (value.startsWith(QUOTE)) {
            value = value.substring(1).trim();
        }
        if (value.endsWith(QUOTE)) {
            value = value.substring(0, value.length() - 1).trim();
        }
        return value.trim();
    }

    public static String getForValue(Object year, Object month, Object day, Object hour, Object minute, Object second, boolean isNegative) {
        StringBuilder content = new StringBuilder();

        if (isNegative) {
            content.append(MINUS);
        }
        content.append(P_DELIM);
        content.append(year);
        content.append(Y_DELIM);
        content.append(month);
        content.append(M_DELIM);
        content.append(day);
        content.append(D_DELIM);
        content.append(T_DELIM);
        content.append(hour);
        content.append(H_DELIM);
        content.append(minute);
        content.append(M_DELIM);
        content.append(second);
        content.append(S_DELIM);

        return content.toString();
    }

    public static String getUntilValue(Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return getUntilValue(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), second(calendar.get(Calendar.SECOND), calendar.get(Calendar.MILLISECOND)), timeZone(date));
    }

    public static String getUntilValue(Object year, Object month, Object day, Object hour, Object minute, Object second, String timeZone) {
        StringBuilder content = new StringBuilder();

        content.append(getString(year));
        content.append(MINUS);
        content.append(getString(month));
        content.append(MINUS);
        content.append(getString(day));
        content.append(T_DELIM);
        content.append(getString(hour));
        content.append(COLON);
        content.append(getString(minute));
        content.append(COLON);
        content.append(second);

        if (timeZone != null) {
            content.append(timeZone.trim());
        }
        return content.toString();
    }

    public static Object parseInt(String value) {
        value = value.trim();

        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e) {
            if (isApplicationVariable(value)) {
                return value;
            }
        }
        return null;
    }

    public static Object parseDouble(String value) {
        value = value.trim();

        try {
            return Double.parseDouble(value);
        }
        catch (NumberFormatException e) {
            if (isApplicationVariable(value)) {
                return value;
            }
        }
        return null;
    }

    static boolean isApplicationVariable(String value) {
        return value.startsWith("${") && value.endsWith("}") && !value.equals("${}"); // NOI18N
    }

    private static String getString(Object value) {
        if ( !(value instanceof Integer)) {
            return value.toString();
        }
        int k = ((Integer) value).intValue();


        if (0 <= k && k <= NINE) {
            return ZERO + k;
        }
        return value.toString();
    }

    private static final int NINE = 9;
    private static final String ZERO = "0"; // NOI18N
    private static final String MINUS = "-"; // NOI18N
    private static final String COLON = ":"; // NOI18N
    private static final String QUOTE = "'"; // NOI18N
    private static final String D_DELIM = "D"; // NOI18N
    private static final String H_DELIM = "H"; // NOI18N
    private static final String M_DELIM = "M"; // NOI18N
    private static final String P_DELIM = "P"; // NOI18N
    private static final String S_DELIM = "S"; // NOI18N
    private static final String Y_DELIM = "Y"; // NOI18N
    private static final Object ZERO_INT = new Integer(0);
    private static final Object ZERO_DBL = new Double(0.0);
    private static final double STO = 100.0;
    public static final String T_DELIM = "T"; // NOI18N
}
