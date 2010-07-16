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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Static class that contains utility methods for searching and replacing through strings.
 *
 * @author Ahimanikya Satapathy
 * @author Girish Patil
 */
public class StringUtil {

    public static final String ALPHA_NUMERIC_REGEX = "([_a-zA-Z0-9-]+)";
    public static final String EMAIL_REGEX = "([_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)+)";
    public static final String FILE_NAME_REGEX = "([_.a-zA-Z0-9-]+)";
    public static final String[][] FILE_TO_TABLE_MAPPINGS = new String[][]{{".", "_"}, {":", "_"}, {";", "_"}, {",", "_"}, {" ", "_"}, {"'", "_"}, {"\"", "_"},
        {"-", "_"},
    };
    public static HashMap formats = new HashMap();
    public static final String NAME_REGEX = "([ _a-zA-Z0-9-]+)";
    public static final String NUMERIC_REGEX = "([0-9]+(\\.[0-9]+)+)";
    public static final String SQL_IDENTIFIER_REGEX = "[a-zA-Z]+[_a-zA-Z0-9]*";
    public static final String URL_REGEX = "\\w+:\\/\\/([^:\\/]+)(:\\d+)?(\\/.*)?";
    private static final String[][] CONTROL_CHAR_MAPPINGS = new String[][]{{"\r", "\\r"}, {"\n", "\\n"}, {"\t", "\\t"}};
    private static final String[][] FIELD_TO_COLUMN_MAPPINGS = FILE_TO_TABLE_MAPPINGS;

    public static String createColumnNameFromFieldName(final String victim) {
        return StringUtil.substituteFromMapping(victim.toUpperCase().trim(), FIELD_TO_COLUMN_MAPPINGS);
    }

    public static final synchronized String createDelimitedStringFrom(List strings) {
        return StringUtil.createDelimitedStringFrom(strings, ',');
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

    public static String createSQLIdentifier(final String victim) {
        // Remove leading spaces from name.
        String workingName = victim.toUpperCase().trim();

        // Then remove any non-alphabetic chars from the first position of the name, and
        // substitute underscores for non-alphanumeric, non-underscore characters within
        // the resulting string.
        return workingName.replaceAll("^[^A-Za-z]+", "").replaceAll("[^A-Za-z0-9_]", "_");
    }

    public static final List createStringListFrom(String delimitedList) {
        return StringUtil.createStringListFrom(delimitedList, ',');
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

    public static String escapeControlChars(String raw) {
        return StringUtil.substituteFromMapping(raw, CONTROL_CHAR_MAPPINGS, false);
    }

    public static int getInt(String type) {
        if (StringUtil.isNullString(type) == false) {
            try {
                return Integer.parseInt(type.trim());
            } catch (NumberFormatException ignore) {
                Logger.global.log(Level.FINE, ignore.toString());
            }
        }

        return Integer.MIN_VALUE;
    }

    public static boolean isIdentical(String one, String two) {
        return (one == null) ? (two == null) : (two != null && one.compareTo(two) == 0);
    }

    public static boolean isIdentical(String one, String two, boolean emptStringEqualsNull) {
        String empty = "";

        if (emptStringEqualsNull) {
            if (one == null) {
                one = empty;
            }

            if (two == null) {
                two = empty;
            }
        }

        return isIdentical(one, two);
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
            Logger.global.log(Level.FINE, e.toString());
        }
        return false;
    }

    public static String replace(String str, Map pairs, String regExpPrefix) {
        if (isNullString(str) || pairs == null || pairs.size() == 0 || isNullString(regExpPrefix)) {
            return str;
        }

        try {
            String result = str;

            Object[] keys = pairs.keySet().toArray();
            for (int i = 0; i < keys.length; i++) {
                String regexp = "(\\" + regExpPrefix + (String) keys[i] + ")";
                result = replaceAll(result, (String) pairs.get(keys[i]), regexp);
            }

            return result;
        } catch (Exception e) {
            Logger.global.log(Level.FINE, e.toString());
            return (str);
        }
    }

    private static String replaceAll(String strToReplace, String strReplaceWith, String regexp) {
        Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(strToReplace);
        return matcher.replaceAll(escapeJavaRegexpChars(strReplaceWith));
    }

    public static String replaceFirst(String strToReplace, String strReplaceWith, String regexp) {
        Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(strToReplace);
        return matcher.replaceFirst(escapeJavaRegexpChars(strReplaceWith));
    }

    public static String replaceTagString(String tagString, String line, String replacement) {
        try {
            int b = line.indexOf(tagString);
            int e = b + tagString.length();
            String begin = line.substring(0, b);
            String end = line.substring(e);
            return begin + replacement + end;
        } catch (StringIndexOutOfBoundsException ex) {
            return null;
        }
    }

    public static String replaceInString(String originalString, String victim, String replacement) {
        return replaceInString(originalString, new String[]{victim}, new String[]{replacement});
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

    private static String substituteFromMapping(final String raw, String[][] mappings) {
        return StringUtil.substituteFromMapping(raw, mappings, false);
    }

    private static String escapeJavaRegexpChars(String rawString) {
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
}
