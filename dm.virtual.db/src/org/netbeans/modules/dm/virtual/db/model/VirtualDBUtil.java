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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dm.virtual.db.model;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.NbBundle;

/**
 * Utility class supplying lookup and conversion methods for SQL-related tasks.
 * 
 * @author Ahimanikya Satapathy
 *
 */
public class VirtualDBUtil {

    static final String LOG_CATEGORY = VirtualDBUtil.class.getName();
    public static final int VARCHAR_UNQUOTED = 3345336;
    private static final int ANYTYPE_CONSTANT = VARCHAR_UNQUOTED - 1;
    public static final String VARCHAR_UNQUOTED_STR = "varchar:unquoted";
    public static final int JDBCSQL_TYPE_UNDEFINED = -65535;
    private static Map<String, String> JDBC_SQL_MAP = new HashMap<String, String>();
    private static Map<String, String> SQL_JDBC_MAP = new HashMap<String, String>();
    private static Map<String, String> WIZARD_SQL_JDBC_MAP = new HashMap<String, String>();
    private static final List<String> SUPPORTED_LITERAL_JDBC_TYPES = new ArrayList<String>();
    

    static {
        WIZARD_SQL_JDBC_MAP.put("numeric", String.valueOf(Types.NUMERIC));
        WIZARD_SQL_JDBC_MAP.put("time", String.valueOf(Types.TIME));
        WIZARD_SQL_JDBC_MAP.put("timestamp", String.valueOf(Types.TIMESTAMP));
        WIZARD_SQL_JDBC_MAP.put("varchar", String.valueOf(Types.VARCHAR));
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
    
     public static String createSQLIdentifier(final String victim) {
        // Remove leading spaces from name.
        String workingName = victim.toUpperCase().trim();

        // Then remove any non-alphabetic chars from the first position of the name, and
        // substitute underscores for non-alphanumeric, non-underscore characters within
        // the resulting string.
        return workingName.replaceAll("^[^A-Za-z]+", "").replaceAll("[^A-Za-z0-9_]", "_");
    }

    public static int getStdJdbcType(String dataType) throws IllegalArgumentException {
        if (VirtualDBUtil.isNullString(dataType)) {
            throw new IllegalArgumentException(NbBundle.getMessage(VirtualDBUtil.class, "MSG_Empty_dataType"));
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

    public static List<String> getStdSqlTypes() {
        return new ArrayList<String>(WIZARD_SQL_JDBC_MAP.keySet());
    }

    public static synchronized boolean isStdJdbcType(int jdbcType) {
        return SQL_JDBC_MAP.containsValue(String.valueOf(jdbcType));
    }
    
    // String Utils
    
    public static final String[][] FILE_TO_TABLE_MAPPINGS = new String[][]{{".", "_"}, {":", "_"}, {";", "_"}, {",", "_"}, {" ", "_"}, {"'", "_"}, {"\"", "_"},
        {"-", "_"},
    };
    private static final String[][] FIELD_TO_COLUMN_MAPPINGS = FILE_TO_TABLE_MAPPINGS;
    private static final String[][] CONTROL_CHAR_MAPPINGS = new String[][]{{"\r", "\\r"}, {"\n", "\\n"}, {"\t", "\\t"}};

    public static final synchronized String createDelimitedStringFrom(List strings) {
        return VirtualDBUtil.createDelimitedStringFrom(strings, ',');
    }

    public static final synchronized String createDelimitedStringFrom(List strings, char delimiter) {
        if (strings == null || strings.size() == 0) {
            return "";
        }

        StringBuffer buf = new StringBuffer(strings.size() * 10);

        for (int i = 0; i < strings.size(); i++) {
            if (i != 0) {
                buf.append(delimiter);
            }
            buf.append(((String) strings.get(i)).trim());
        }

        return buf.toString();
    }

    public static String createTableNameFromFileName(final String victim) {
        return VirtualDBUtil.substituteFromMapping(victim.toUpperCase().trim(), FILE_TO_TABLE_MAPPINGS);
    }

    public static String escapeNonAlphaNumericCharacters(String iStr) {
        StringBuffer sb = new StringBuffer();
        int len = iStr.length();
        String hexStr = null;
        for (int i = 0; i < len; i++) {
            if (!Character.isLetterOrDigit(iStr.charAt(i)) && (iStr.charAt(i) != '.')) {
                sb.append("_u");
                hexStr = Integer.toHexString(iStr.charAt(i));
                if (hexStr.length() < 4) {
                    sb.append(FOUR_ZEROS.substring(0, 4 - hexStr.length()));
                }
                sb.append(hexStr);
            } else {
                sb.append(iStr.charAt(i));
            }

        }
        return sb.toString();
    }
    private static final String FOUR_ZEROS = "0000";

    public static String escapeControlChars(String raw) {
        return VirtualDBUtil.substituteFromMapping(raw, CONTROL_CHAR_MAPPINGS, false);
    }

    public static String unescapeControlChars(String cooked) {
        return VirtualDBUtil.substituteFromMapping(cooked, CONTROL_CHAR_MAPPINGS, true);
    }

    public static String replaceFirst(String strToReplace, String strReplaceWith, String regexp) {
        Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(strToReplace);
        return matcher.replaceFirst(escapeJavaRegexpChars(strReplaceWith));
    }

    public static String escapeJavaRegexpChars(String rawString) {
        String cookedString = null;

        if (rawString != null) {
            // Escape \            
            cookedString = rawString.replaceAll("\\\\", "\\\\\\\\");
            // Escape $
            cookedString = cookedString.replaceAll("\\$", "\\\\\\$");
            // Escape ?
            cookedString = cookedString.replaceAll("\\?", "\\\\\\?");
        }

        return cookedString;
    }

    public static boolean isNullString(String str) {
        return (str == null || str.trim().length() == 0);
    }

    public static boolean isValid(String str, String regexp) {
        if (isNullString(str) || isNullString(regexp)) {
            return false;
        }

        try {
            return str.matches(regexp);
        } catch (Exception e) {
        }
        return false;
    }

    public static final List createStringListFrom(String delimitedList) {
        return VirtualDBUtil.createStringListFrom(delimitedList, ',');
    }

    public static final List createStringListFrom(String delimitedList, char delimiter) {
        if (delimitedList == null || delimitedList.trim().length() == 0) {
            return Collections.EMPTY_LIST;
        }

        List strings = Collections.EMPTY_LIST;
        StringTokenizer tok = new StringTokenizer(delimitedList, String.valueOf(delimiter));
        if (tok.hasMoreTokens()) {
            strings = new ArrayList();
            do {
                strings.add(tok.nextToken().trim());
            } while (tok.hasMoreTokens());
        }

        return strings;
    }

    public static String createColumnNameFromFieldName(final String victim) {
        return VirtualDBUtil.substituteFromMapping(victim.toUpperCase().trim(), FIELD_TO_COLUMN_MAPPINGS);
    }

    public static String substituteFromMapping(final String raw, String[][] mappings) {
        return VirtualDBUtil.substituteFromMapping(raw, mappings, false);
    }

    private static String substituteFromMapping(final String raw, String[][] mappings, boolean useReverseMap) {
        String cooked = raw;

        if (mappings != null) {
            int toggle = useReverseMap ? 0 : 1;
            for (int i = 0; i < mappings.length; i++) {
                cooked = replaceInString(cooked, mappings[i][1 - toggle], mappings[i][toggle]);
            }
        }

        return cooked;
    }

    public static String replaceInString(String originalString, String[] victims, String[] replacements) {

        StringBuffer resultBuffer = new StringBuffer();
        boolean bReplaced = false;

        // For all characters in the original string
        for (int charPosition = 0; charPosition < originalString.length(); charPosition++) {

            // Walk through all the replacement candidates.
            for (int nSelected = 0; !bReplaced && (nSelected < victims.length); nSelected++) {

                // If charPosition designates a replacement.
                if (originalString.startsWith(victims[nSelected], charPosition)) {

                    // Add the new replacement.
                    resultBuffer.append(replacements[nSelected]);

                    // Mark this position as a replacement.
                    bReplaced = true;

                    // Step over the replaced string.
                    charPosition += victims[nSelected].length() - 1;
                }
            }

            if (!bReplaced) {
                resultBuffer.append(originalString.charAt(charPosition));
            } else {
                // Reset for the next character.
                bReplaced = false;
            }
        }

        // Return the result as a string
        return resultBuffer.toString();
    }

    public static String replaceInString(String originalString, String victim, String replacement) {
        return replaceInString(originalString, new String[]{victim}, new String[]{replacement});
    }

    private VirtualDBUtil() {
    }
}
