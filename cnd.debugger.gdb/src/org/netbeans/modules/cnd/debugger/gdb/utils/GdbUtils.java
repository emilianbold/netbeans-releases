/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.debugger.gdb.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.netbeans.modules.cnd.debugger.gdb.GdbVariable;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Various miscelaneous static methods.
 *
 * @author Gordon Prieur
 */
public class GdbUtils {
    
    /**
     * Extract gdb version. We're only interested in major/minor release information so we ignore any
     * micro release information.
     *
     * @param verstring The version string returned by gdb (with extra stuff...)
     * @return ver The version in the form of major/minor
     */
    protected static double extractGdbVersion(String verstring) {
        double ver;
        int last = verstring.lastIndexOf('.');
        int first = verstring.indexOf('.');
        
        if (last != first) {
            verstring = verstring.substring(0, last); // Strip off micro
        }
        
        try {
            ver = Double.parseDouble(verstring);
        } catch (NumberFormatException ex) {
            ver = 0.0;
        }
        
        return ver;
    }
    
    /**
     * Determine if we're running Cygwin or not
     *
     * @param message The input string (without the '~')
     * @return boolean true if Cygwin else false
     */
    private static boolean isCygwin(String message) {
        return Utilities.isWindows() && message.toLowerCase().contains("cygwin"); // NOI18N
    }

    /**
     *  Ignoring array and pointer information, is this type a keyword type? We may check more
     *  than one type here as a function type will have all argument types checked.
     *
     *  @param type The type to check
     */
    public static boolean isSimple(Object type) {
        if (type == null || type instanceof Map) {
            return false;
        } else {
            StringTokenizer tok = new StringTokenizer(type.toString().replaceAll("[\\[\\]()<>,:*]", " ")); // NOI18N

            while (tok.hasMoreTokens()) {
                String token = tok.nextToken();
                if (!isSimpleTypeKeyword(token) && Character.isJavaIdentifierStart(token.charAt(0))) {
                    return false;
                }
            }
            return true;
        }
    }

    public static boolean isSimpleNonArray(Object type) {
	return type instanceof String && type.toString().indexOf('[') == -1 && isSimple(type.toString());
    }

    public static boolean isSimplePointer(String type) {
	return type != null && isSimple(type.replace('*', ' '));
    }
    
    /** Test if the type of a type is a keyword type */
    public static boolean isSimpleTypeKeyword(String type) {
        return type != null && type.equals("char") // NOI18N
            || type.equals("void") // NOI18N
            || type.equals("short") // NOI18N
            || type.equals("int") // NOI18N
            || type.equals("long") // NOI18N
            || type.equals("float") // NOI18N
            || type.equals("double") // NOI18N
            || type.equals("const") // NOI18N
            || type.equals("volatile") // NOI18N
            || type.equals("unsigned") // NOI18N
            || type.equals("signed"); // NOI18N
    }
    
    /** Test if the type of a type is a keyword type */
    public static boolean isAbstractTypeKeyword(Object o) {
        String type = null;
        return o instanceof String && (type = o.toString()) != null && type.equals("struct") // NOI18N
            || type.equals("union") // NOI18N
            || type.equals("class"); // NOI18N
    }
    
    /** Test if a variable is a struct or union */
    public static boolean isStructOrUnion(Object type) {
        return type instanceof Map || (type instanceof String && (type.toString().startsWith("struct ") || type.toString().startsWith("union "))); // NOI18N
    }
    
    /** Test if a variable is a class */
    public static boolean isClass(Object type) {
        return type instanceof Map || (type instanceof String && type.toString().startsWith("class ")); // NOI18N
    }
    
    /** Test if a variable is an array */
    public static boolean isArray(Object type) {
        return type instanceof String && type.toString().endsWith("]"); // NOI18N
    }
    
    /**
     * Test if a variable is a pointer. This method purposely ignores
     * function pointers.
     */
    public static boolean isPointer(Object type) {
        return type instanceof String && type.toString().endsWith("*"); // NOI18N
    }
    
    /** Test if a variable is a function pointer */
    public static boolean isFunctionPointer(Object type) {
        return type instanceof String && type.toString().contains("(*)("); // NOI18N
    }
    
    /**
     * Given a typename, strip off array and pointer information and return the root type.
     *
     * @param type The complete type (possibly including array and pointer information)
     * @returns The base name of the string (or null if type is null)
     */
    public static String getBaseType(String type) {
        if (type != null) {
            type = type.replace("const ", ""); // NOI18N
            type = type.replace("volatile ", ""); // NOI18N
            type = type.replace("static ", ""); // NOI18N
            int len = type.length();
            for (int i = 0; i < len; i++) {
                char ch = type.charAt(i);
                if (!Character.isLetter(ch) && !Character.isDigit(ch) && !isOneOf(ch, " _:<>,")) { // NOI18N
                    return type.substring(0, i).trim();
                }
            }
            return type.trim();
        } else {
            return null;
        }
    }
    
    public static boolean containesOneOf(String str, String chars) {
        for (int i = 0; i < str.length(); i++) {
            if (isOneOf(str.charAt(i), chars)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isOneOf(char ch, String chars) {
        for (int i = 0; i < chars.length(); i++) {
            if (ch == chars.charAt(i)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     *  Parse the input string for key/value pairs. Each key should be unique so
     *  results can be stored in a map.
     *
     *  @param info A string of key/value pairs
     *  @return A HashMap containing each key/value
     */
    public static Map<String, String> createMapFromString(String info) {
        HashMap<String, String> map = new HashMap();
        String key, value;
        int tstart, tend;
        int len = info.length();
        int i = 0;
        char ch;
        
        // Debugger gdb can send different messages
        // Examples:
        // 1. at breakpoint
        //  reason="breakpoint-hit",bkptno="3",thread-id="1",
        //  frame={addr="0x0040132a",func="main",
        //  args=[{name="argc",value="1"},{name="argv",value="0x6c1f38"}],
        //  file="mp.cc",line="38"}
        // 2. after "Step Into" and "Step Over"
        //  reason="end-stepping-range",thread-id="1",
        //  frame={addr="0x004011e8",func="main",
        //  args=[{name="argc",value="1"},{name="argv",value="0x6c1f38"}],
        //  file="mp.cc",line="20"}
        // 3. after "Step Out"
        //  reason="function-finished",thread-id="1",
        //  frame={addr="0x00403e03",func="main",
        //  args=[{name="argc",value="1"},{name="argv",value="0x6f19a8"}],
        //  file="quote.cc",fullname="g:/tmp/nik/Quote1/quote.cc",
        //  line="131"},gdb-result-var="$1",return-value="-1"
        
        while (i < len) {
            tstart = i++;
            while (info.charAt(i++) != '=') {
            }
            key = info.substring(tstart, i - 1);
            if ((ch = info.charAt(i++)) == '{') {
                tend = findMatchingCurly(info, i) - 1;
            } else if (ch == '"') {
                tend = findEndOfString(info, i);
            } else if (ch == '[') {
                tend = findMatchingBrace(info, i);
            } else {
//                log.fine("INTERNAL ERROR: GdbLogger.createMapFromString(msg) cannot parse message. msg="+info); // NOI18N
                break;
            }
            
            // put the value in the map and prepare for the next property
            value = info.substring(i, tend);
            if (Utilities.isWindows() && value.startsWith("/cygdrive/")) { // NOI18N
                value = value.charAt(10) + ":" + value.substring(11); // NOI18N
            }
            map.put(key, value);
            i = tend + 2;
        }
        
        return map;
    }
    
    /**
     *  Parse the input string for key/value pairs. The keys are not guaranteed
     *  to be unique and order may be important, so each key/value pair is stored
     *  in an ArrayList.
     *
     *  @param info A string of key/value pairs where each key/value
     *  @return An ArrayList with each entry of the form key=value
     */
    public static List createListFromString(String info) {
        List list = new ArrayList();
        String key, value;
        int tstart, tend;
        int len = info.length();
        int idx = 0;
        char ch;
        
        while (idx < len) {
            tstart = idx++;
            while (info.charAt(idx++) != '=') {
            }
            key = info.substring(tstart, idx - 1);
            if ((ch = info.charAt(idx++)) == '{') {
                tend = findMatchingCurly(info, idx);
            } else if (ch == '"') {
                tend = findEndOfString(info, idx);
            } else {
                throw new IllegalStateException(NbBundle.getMessage(
                        GdbUtils.class, "ERR_UnexpectedGDBReasonMessage")); // NOI18N
            }
            
            // put the value in the list and prepare for the next property
            value = info.substring(idx, tend);
            if (Utilities.isWindows() && value.startsWith("/cygdrive/")) { // NOI18N
                value = value.charAt(10) + ":" + value.substring(11); // NOI18N
            }
            list.add(key + "=" + value); // NOI18N
            idx = tend + 1;
            idx++;
        }
        
        return list;
    }
    
    /**
     * Called with the response of -stack-list-locals. Read the locals string and get
     * the variable information. The information is one or more comma separated name/value
     * pairs:
     *
     *    {name="name1",value="value1"},{name="name2",value="value2"},{...}
     *
     * @param info The string returned from -stack-list-locals with the header removed
     * @return A List containing GdbVariables for each name/value pair
     */
    public static List createLocalsList(String info) {
        String name, value; 
        List<GdbVariable> list = new ArrayList();
        int idx = 0;
        int pos = 0;
        
        while ((pos = findMatchingCurly(info, idx)) != -1 && pos < info.length()) {
            String frag = info.substring(idx, pos + 1);
            int pos2 = findNextComma(frag, 0, 1, 1);
            if (pos2 != -1) {
                name = frag.substring(7, pos2 - 1);
                value = frag.substring(pos2 + 8, frag.length() - 2); // strip double quotes
                list.add(new GdbVariable(name, null, value));
            }
            idx = info.indexOf('{', pos);
        }
        
        return list;
    }
    
    /**
     * Called with the response of -stack-list-arguments. Read the argument string and get
     * the variable information.
     *
     * @param info The string returned from -stack-list-arguments with the header removed
     * @return A List containing GdbVariables of each argument
     */
    public static List createArgumentList(String info) {
        String name, value; 
        List<GdbVariable> list = new ArrayList();
        int len = info.length();
        int pos, pos2;
        int idx = 0;
        
        while (len > 0) {
            String frag = info.substring(idx, findMatchingCurly(info, idx) + 1);
            idx += frag.length() + 1;
            len -= frag.length() + 1;
            // {name=\"argc\",value=\"1\"},{name=\"argv\",value=\"(char **) 0x6625f8\"}
            if (frag.startsWith("{name=\"") && frag.endsWith("\"}")) { // NOI18N
                pos = frag.indexOf("\",value=\""); // NOI18N
                if (pos > 0) {
                    name = frag.substring(7, pos);
                    value = frag.substring(pos + 9, frag.length() - 2);
                    list.add(new GdbVariable(name, null, value));
                }
            }
        }
        
        return list;
    }
    
    /**
     * Value strings on the Mac have embedded newlines which break gdb-lite parsing.
     * So before setting the value string we strip these extraneous newlines.
     */
    public static String mackHack(String info) {
        if (info != null && info.indexOf("\\n") != -1) {
            StringBuilder s = new StringBuilder();
            int idx = 0;
            char last = 0;
            boolean inDoubleQuote = false;
            boolean inSingleQuote = false;
        
            while (idx < info.length()) {
                char ch = info.charAt(idx);
                if (inDoubleQuote) {
                    if (ch == '"' && last != '\\') {
                        inDoubleQuote = false;
                    }
                } else if (inSingleQuote) {
                    if (ch == '\'' && last != '\\') {
                        inSingleQuote = false;
                    }
                } else if (ch == '\"' && last != '\\') {
                    if (inDoubleQuote) {
                        inDoubleQuote = false;
                    } else {
                        inDoubleQuote = true;
                    }
                } else if (ch == '\'') {
                    inSingleQuote = true;
                } else if (ch == 'n' && last == '\\') {
                    s.deleteCharAt(s.length() - 1);
                    ch = 0;
                }
                if (ch != 0) {
                    s.append(ch);
                }
                last = ch;
                idx++;
            }
            return s.toString();
        } else {
            return info;
        }
    }
    
    /** Find the end of a string by looking for a non-escaped double quote */
    private static int findEndOfString(String s, int idx) {
        char last = '\0';
        char ch;
        int len = s.length();
        
        while (len-- > 0) {
            if ((ch = s.charAt(idx)) == '"' && last != '\\') {
                return idx;
            } else {
                idx++;
                last = ch;
            }
        }
        throw new IllegalStateException(NbBundle.getMessage(
                GdbUtils.class, "ERR_UnexpectedGDBStopMessage")); // NOI18N
    }
    
    /**
     * Find the end of a [ ... ] block
     *
     * @param s The string to parse
     * @param idx The index to start at
     * @returns The index of the closing ']' or -1 if no match is found
     */
    public static int findMatchingBrace(String s, int idx) {
        return findMatchingPair("[]", s, idx); // NOI18N
    }
    
    /**
     * Find the end of a { ... } block
     *
     * @param s The string to parse
     * @param idx The index to start at
     * @returns The index of the closing '}' or -1 if no match is found
     */
    public static int findMatchingCurly(String s, int idx) {
        return findMatchingPair("{}", s, idx); // NOI18N
    }
    
    /**
     * Find the end of a ( ... ) block
     *
     * @param s The string to parse
     * @param idx The index to start at
     * @returns The index of the closing ')' or -1 if no match is found
     */
    public static int findMatchingParen(String s, int idx) {
        return findMatchingPair("()", s, idx); // NOI18N
    }
    
    /**
     * Find the end of a < ... > block
     *
     * @param s The string to parse
     * @param idx The index to start at
     * @returns The index of the closing ')' or -1 if no match is found
     */
    public static int findMatchingLtGt(String s, int idx) {
        return findMatchingPair("<>", s, idx); // NOI18N
    }
    
    /**
     * Find the end of a [ ... ] or { ... } block.
     *
     * @param pair A 2 character string with a beginning char and an end char
     * @param s The string to parse
     * @param idx The index to start at
     * @returns The index of the closing ']' or -1 if no match is found
     */
    private static int findMatchingPair(String pair, String s, int idx) {
        char lbrace = pair.charAt(0);
        char rbrace = pair.charAt(1);
        char last = ' ';
        int count = 0;
        boolean inDoubleQuote = false;
        boolean inSingleQuote = false;
        
        if (s == null || s.length() == 0 || idx < 0) {
            return -1;
        }
        if (s.charAt(idx) == lbrace) {
            idx++;
        }
        
        while (idx < s.length()) {
            char ch = s.charAt(idx);
            if (inDoubleQuote) {
                if (ch == '"' && last != '\\') {
                    inDoubleQuote = false;
                }
            } else if (inSingleQuote) {
                if (ch == '\'' && last != '\\') {
                    inSingleQuote = false;
                }
            } else if (ch == rbrace && count == 0) {
                return idx;
            } else if (ch == '\"' && last != '\\') {
                if (inDoubleQuote) {
                    inDoubleQuote = false;
                } else {
                    inDoubleQuote = true;
                }
            } else if (ch == '\'') {
                inSingleQuote = true;
            } else {
                if (ch == lbrace) {
                    count++;
                } else if (ch == rbrace) {
                    count--;
                }
            }
            last = ch;
            idx++;
        }
        
        return -1;
    }
    
    /**
     * Find the next comma (ignoring ones in quotes and double quotes)
     *
     * @param s The string to search
     * @param idx The starting index
     * @param idx The starting index
     */
    public static int findNextComma(String s, int idx) {
        return findNextComma(s, idx, 0, 0);
    }
    
    /**
     * Find the next comma (ignoring ones in quotes and double quotes)
     *
     * @param s The string to search
     * @param idx The starting index
     * @param startSkipCount Number of chars to ignore at start if s[idx]
     * @param endSkipCount Number of chars to ignore at the end of s
     * @param idx The starting index
     */
    public static int findNextComma(String s, int idx, int startSkipCount, int endSkipCount) {
        char last = ' ';
        char ch;
        int i;
        int slen = s.length() - endSkipCount;
        boolean inDoubleQuote = false;
        boolean inSingleQuote = false;
        
        assert s != null && s.length() > 0;
        if (idx < 0) {
            return -1; // allow this to allow other find* functions to provide idx
        }
        idx += startSkipCount;
        
        while (idx < s.length()) {
            ch = s.charAt(idx);
            if (inDoubleQuote) {
                if (ch == '"' && last != '\\') {
                    inDoubleQuote = false;
                }
            } else if (inSingleQuote) {
                if (ch == '\'' && last != '\\') {
                    inSingleQuote = false;
                }
            } else if (ch == '{') {
                i = GdbUtils.findMatchingCurly(s, idx);
                if (i == -1) {
                    break;
                } else {
                    idx = i;
                }
            } else if (ch == '<') {
                i = GdbUtils.findMatchingLtGt(s, idx);
                if (i == -1) {
                    break;
                } else {
                    idx = i;
                }
            } else if (ch == '[') {
                i = GdbUtils.findMatchingBrace(s, idx);
                if (i == -1) {
                    break;
                } else {
                    idx = i;
                }
            } else if (ch == ',' && !isMultiString(s, idx)) {
                return idx;
            } else if (ch == '\"' && last != '\\') {
                if (inDoubleQuote) {
                    inDoubleQuote = false;
                } else {
                    inDoubleQuote = true;
                }
            } else if (ch == '\'' && last != '\\') {
                inSingleQuote = true;
            }
            last = ch;
            idx++;
        }
        
        return -1;
    }
 
    /**
     * Gdb sometimes returns strings with an extra ',' followed by a single character and
     * "<repeats xx times>" (where xx is some int). See if this is such a comman and return
     * true if it is.
     */
    private static boolean isMultiString(String s, int idx) {
        String frag;
        if (++idx < s.length()) {
            int pos = s.indexOf(',', idx);
            if (pos == -1) {
                frag = s.substring(idx);
            } else {
                frag = s.substring(idx, pos);
            }
            if (frag.contains("<repeats ") && frag.contains(" times>")) { // NOI18N
                return true;
            }
        }
        return false;
    }
    
    /**
     * Find the next simicolon (ignoring ones in quotes and double quotes)
     *
     * @param s The string to search
     * @param idx The starting index
     */
    public static int findNextSemi(String s, int idx) {
        char last = ' ';
        char ch;
        int i;
        boolean inDoubleQuote = false;
        boolean inSingleQuote = false;
        
        assert s != null && s.length() > 0;
        if (idx < 0) {
            return -1;
        }
        
        ch = s.charAt(idx);
        if (ch == ';' || ch == '{') { // skip 1st char in this case
            idx++;
        }
        
        while (idx < s.length()) {
            ch = s.charAt(idx);
            if (inDoubleQuote) {
                if (ch == '"' && last != '\\') {
                    inDoubleQuote = false;
                }
            } else if (inSingleQuote) {
                if (ch == '\'' && last != '\\') {
                    inSingleQuote = false;
                }
            } else if (ch == '{') {
                i = GdbUtils.findMatchingCurly(s, idx);
                if (i == -1) {
                    break;
                } else {
                    idx = i;
                }
            } else if (ch == '<') {
                i = GdbUtils.findMatchingLtGt(s, idx);
                if (i == -1) {
                    break;
                } else {
                    idx = i;
                }
            } else if (ch == '[') {
                i = GdbUtils.findMatchingBrace(s, idx);
                if (i == -1) {
                    break;
                } else {
                    idx = i;
                }
            } else if (ch == ';') {
                return idx;
            } else if (ch == '\"' && last != '\\') {
                if (inDoubleQuote) {
                    inDoubleQuote = false;
                } else {
                    inDoubleQuote = true;
                }
            } else if (ch == '\'' && last != '\\') {
                inSingleQuote = true;
            }
            last = ch;
            idx++;
        }
        
        return -1;
    }
    
    /**
     * Find the 1st non-whitespace character from the given starting point in the string
     *
     * @param info The string to look in
     * @param idx The starting position in the string
     * @return The position of the 1st non-white character or -1
     */
    public static int firstNonWhite(String info, int idx) {
        char ch;
        int len = info.length();
        if (idx >= 0 && idx < len) {
            while (idx < len && ((ch = info.charAt(idx)) == ' ' || ch == '\t' || ch == '\n' || ch == '\r')) {
                idx++;
            }
            if (idx < len) {
                return idx;
            }
        }
        return -1;
    }
}
