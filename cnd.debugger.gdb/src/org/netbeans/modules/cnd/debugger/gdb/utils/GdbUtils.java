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

package org.netbeans.modules.cnd.debugger.gdb.utils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.netbeans.modules.cnd.api.compilers.PlatformTypes;
import org.netbeans.modules.cnd.debugger.common.utils.WinPath;
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
     *  Ignoring array and pointer information, is this type a keyword type? We may check more
     *  than one type here as a function type will have all argument types checked.
     *
     *  @param type The type to check
     */
    public static boolean isSimpleType(Object type) {
        if (type == null || type instanceof Map) {
            return false;
        } else {
            StringTokenizer tok = new StringTokenizer(type.toString().replaceAll("[\\[\\]()<>,:*]", " ")); // NOI18N

            while (tok.hasMoreTokens()) {
                String token = tok.nextToken();
                if (!isSimpleTypeKeyword(token)) {
                    return false;
                }
            }
            return true;
        }
    }

//    public static boolean isSimpleArray(Object type) {
//        if (type == null || type instanceof Map) {
//            return false;
//        } else {
//            
//        }
//        return true;
//    }

//    public static boolean isSimpleNonArray(Object type) {
//	return type instanceof String && type.toString().indexOf('[') == -1 && isSimpleType(type.toString());
//    }

    public static boolean isSimplePointer(String type) {
	return type != null && isSimpleType(type.replaceFirst("[*]", " ")); // NOI18N
    }
    
    /** Test if the type of a type is a keyword type */
    private static boolean isSimpleTypeKeyword(String type) {
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
    
//    /** Test if the type of a type is a keyword type */
//    public static boolean isAbstractTypeKeyword(Object o) {
//        String type = null;
//        return o instanceof String && (type = o.toString()) != null && type.equals("struct") // NOI18N
//            || type.equals("union") // NOI18N
//            || type.equals("class"); // NOI18N
//    }
//    
//    /** Test if a variable is a struct or union */
//    public static boolean isStructOrUnion(Object type) {
//        return type instanceof Map || (type instanceof String && (type.toString().startsWith("struct ") || type.toString().startsWith("union "))); // NOI18N
//    }
    
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
        return type instanceof String &&
                (type.toString().endsWith("*") || type.toString().endsWith("* const")); // NOI18N
    }
    
    /**
     * Test if a variable is a pointer. This method purposely ignores
     * function pointers.
     */
    public static boolean isSinglePointer(Object type) {
        return isPointer(type) && !isMultiPointer(type);
    }
    
    /**
     * Test if a variable is a double pointer (ie, "char **"). This method purposely ignores
     * function pointers.
     */
    public static boolean isMultiPointer(Object type) {
        return type instanceof String &&
                (type.toString().endsWith("**") || type.toString().endsWith("** const")); // NOI18N
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
    
    private static abstract class PairProcessor {
        public abstract void onPair(String key, String value);
    }

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
    private static void processString(String info, PairProcessor processor) {
        int len = info.length();
        int i = 0;
        boolean isWindows = Utilities.isWindows();
        
        mainLoop: while (i < len) {
            int tstart = i++;

            i = info.indexOf('=', i);
            if (i == -1) {
                break;
            }

            String key = info.substring(tstart, i);

            // jump to the first symbol after =
            i++;

            int tend;
            switch (info.charAt(i)) {
                case '{':
                    tend = findMatchingCurly(info, i++);
                    break;
                case '"':
                    i++;
                    tend = findEndOfString(info, i);
                    break;
                case '[':
                    tend = findMatchingBrace(info, i++);
                    break;
                default:
                    break mainLoop;
            }

            if (tend == -1) {
                break;
            }

            // put the value in the map and prepare for the next property
            String value = info.substring(i, tend);
            if (isWindows && value.startsWith("/cygdrive/")) { // NOI18N
                value = value.toUpperCase().charAt(10) + ":" + value.substring(11); // NOI18N
            }
            if (key.equals("fullname") || key.equals("file")) { // NOI18N
                value = gdbToUserEncoding(value); // possibly convert multi-byte fields
            }
            processor.onPair(key, value);
            i = tend + 2;
        }
    }
    
    /**
     *  Parse the input string for key/value pairs. Each key should be unique so
     *  results can be stored in a map.
     *
     *  @param info A string of key/value pairs
     *  @return A HashMap containing each key/value
     */
    public static Map<String, String> createMapFromString(String info) {
        final HashMap<String, String> map = new HashMap<String, String>();
        processString(info, new PairProcessor() {
            @Override
            public void onPair(String key, String value) {
                map.put(key, value);
            }
        });
        return map;
    }
    
    public static String gdbToUserEncoding(String string) {
        // The first part transforms string to byte array
        char[] chars = string.toCharArray();
        char last = 0, next;
        ArrayList<Byte> _bytes = new ArrayList<Byte>();
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            next = (i + 1) < chars.length ? chars[i + 1] : 0;
            if (ch == '\\' && last != '\\' && next != '\\') {
                char[] charVal = {chars[++i], chars[++i], chars[++i]};
                ch = (char) Integer.parseInt(String.valueOf(charVal), 8);
            }
            _bytes.add((byte) ch);
            last = chars[i];
        }
        byte[] bytes = new byte[_bytes.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = _bytes.get(i);
        }

        // The second part performs encoding to current coding system
        try {
            string = new String(bytes, System.getProperty("sun.jnu.encoding"));
        } catch (UnsupportedEncodingException e) {
        }
        return string;
    }
    
    /*public static String utfToGdb(String string) {
        String lang = System.getenv("LANG") + System.getenv("LC_ALL"); // NOI18N
        
        if (lang != null && (lang.contains("UTF-8") || lang.contains("UTF8"))) { // NOI18N
            Charset cs = Charset.forName("UTF-8"); // NOI18N
            CharsetEncoder encoder = cs.newEncoder();
            GdbEncoder gpe = new GdbEncoder(cs, encoder);
            CharBuffer in = CharBuffer.wrap(string.toCharArray());
            ByteBuffer out = ByteBuffer.allocate(string.length() << 3);
            CoderResult cres = gpe.encode(in, out, true);
            
            if (!cres.isError()) {
                string = new String(out.array()).substring(0, out.position());
            }
        }
        return string;
    }*/
    
    public static boolean isMultiByte(String file) {
        char[] ch = file.toCharArray();
        for (int i = 0; i < file.length(); i++) {
            if (ch[i] > 0x80) {
                return true;
            }
        }
        return false;
    }
    
    /**
     *  Parse the input string for key/value pairs.
     *
     *  @param info A string of key/value pairs
     *  @return An ArrayList with each entry as a value (keys are thrown away, use createMapFromString if you need them)
     */
    public static List<String> createListFromString(String info) {
        final List<String> list = new ArrayList<String>();
        processString(info, new PairProcessor() {
            @Override
            public void onPair(String key, String value) {
                list.add(value);
            }
        });
        return list;
    }

    /*
     * Create a list from the list of values:
     * {addr="0x00001390",data=["0x00","0x01"]},
     * {addr="0x00001392",data=["0x02","0x03"]},
     * {addr="0x00001394",data=["0x04","0x05"]}
     */
    public static List<String> createListOfValues(String info) {
        List<String> list = new ArrayList<String>();
        int start = info.indexOf('{'); // NOI18N
        while (start != -1) {
            int end = findMatchingCurly(info, start);
            if (end == -1) {
                break;
            }
            list.add(info.substring(start+1, end));
            start = info.indexOf('{', end); // NOI18N
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
    public static List<GdbVariable> createLocalsList(String info) {
        String name, value; 
        List<GdbVariable> list = new ArrayList<GdbVariable>();
        int idx = 0;
        int pos = 0;
        
        while ((pos = findMatchingCurly(info, idx)) != -1 && pos < info.length()) {
            String frag = info.substring(idx, pos + 1);
            int pos2 = findNextComma(frag, 0, 1);
            if (pos2 != -1) {
                name = frag.substring(7, pos2 - 1);
                value = frag.substring(pos2 + 8, frag.length() - 2); // strip double quotes
                list.add(new GdbVariable(name, value));
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
    public static List<GdbVariable> createArgumentList(String info) {
        String name, value; 
        List<GdbVariable> list = new ArrayList<GdbVariable>();
        int len = info.length();
        int pos;
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
                    list.add(new GdbVariable(name, value));
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
        if (info != null && info.indexOf("\\n") != -1) { // NOI18N
            StringBuilder s = new StringBuilder();
            int idx = 0;
            int pos1, pos2, pos3;
            boolean inDoubleQuote = false;
            boolean inSingleQuote = false;
            
            pos1 = info.indexOf("&) @0x"); // NOI18N
            pos2 = info.indexOf('{');
            pos3 = info.indexOf(':');
            if (info.charAt(0) == '(' && pos1  != -1 && pos2 != -1 && pos3 != -1 && pos3 < pos2) {
                idx = pos2;
            }
        
            while (idx < info.length()) {
                char ch = info.charAt(idx);
                if (inDoubleQuote) {
                    if (ch == '"' && !isSlashBefore(info, idx)) {
                        inDoubleQuote = false;
                    }
                } else if (inSingleQuote) {
                    if (ch == '\'' && !isSlashBefore(info, idx)) {
                        inSingleQuote = false;
                    }
                } else if (ch == '\"' && !isSlashBefore(info, idx)) {
                    if (inDoubleQuote) {
                        inDoubleQuote = false;
                    } else {
                        inDoubleQuote = true;
                    }
                } else if (ch == '\'') {
                    inSingleQuote = true;
                } else if (ch == 'n' && !isSlashBefore(info, idx)) {
                    s.deleteCharAt(s.length() - 1);
                    ch = 0;
                } else if (info.substring(idx).startsWith("members of ")) { // NOI18N
                    pos1 = info.indexOf(':', idx);
                    pos2 = info.indexOf(' ', idx + 11);
                    if (pos1 != -1 && pos2 != -1 && pos1 < pos2) {
                        idx = pos1 + 1;
                        ch = 0;
                    }
                }
                if (ch != 0) {
                    s.append(ch);
                }
                idx++;
            }
            return s.toString();
        } else {
            return info;
        }
    }
    
    /** Find the end of a string by looking for a non-escaped double quote */
    public static int findEndOfString(String s, int idx) {
        int len = s.length();

        for (;idx < len;idx++) {
            char ch = s.charAt(idx);
            if (ch == '"' && !isSlashBefore(s, idx)) {
                return idx;
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
                if (ch == '"' && !isSlashBefore(s, idx)) {
                    inDoubleQuote = false;
                }
            } else if (inSingleQuote) {
                if (ch == '\'' && !isSlashBefore(s, idx)) {
                    inSingleQuote = false;
                }
            } else if (ch == rbrace && count == 0) {
                return idx;
            } else if (ch == '\"' && !isSlashBefore(s, idx)) {
                inDoubleQuote = !inDoubleQuote;
            } else if (ch == '\'') {
                inSingleQuote = true;
            } else {
                if (ch == lbrace) {
                    count++;
                } else if (ch == rbrace) {
                    count--;
                }
            }
            idx++;
        }
        
        return -1;
    }

    /**
     * Checks if the character at the specified pos has an odd number of slahses before
     * ///" - is ok, //" is not ok
     */
    private static boolean isSlashBefore(String source, int pos) {
        int count = 0;
        pos--;
        while (pos > 0 && source.charAt(pos) == '\\') {
            pos--;
            count++;
        }
        return count % 2 == 1;
    }
    
    /**
     * Find the next comma (ignoring ones in quotes and double quotes)
     *
     * @param s The string to search
     * @param idx The starting index
     */
    public static int findNextComma(String s, int idx) {
        return findNextComma(s, idx, 0);
    }
    
    /**
     * Find the next comma (ignoring ones in quotes and double quotes)
     *
     * @param s The string to search
     * @param idx The starting index
     * @param skipCount Number of chars to ignore at start if s[idx]
     * @param idx The starting index
     */
    public static int findNextComma(String s, int idx, int skipCount) {
        char ch;
        int i;
        boolean inDoubleQuote = false;
        boolean inSingleQuote = false;
        
        assert s != null && s.length() > 0;
        if (idx < 0) {
            return -1; // allow this to allow other find* functions to provide idx
        }
        idx += skipCount;
        
        while (idx < s.length()) {
            ch = s.charAt(idx);
            if (inDoubleQuote) {
                if (ch == '"' && !isSlashBefore(s, idx)) {
                    inDoubleQuote = false;
                }
            } else if (inSingleQuote) {
                if (ch == '\'' && !isSlashBefore(s, idx)) {
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
            } else if (ch == '\"' && !isSlashBefore(s, idx)) {
                if (inDoubleQuote) {
                    inDoubleQuote = false;
                } else {
                    inDoubleQuote = true;
                }
            } else if (ch == '\'' && !isSlashBefore(s, idx)) {
                inSingleQuote = true;
            }
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
            if (frag.startsWith("<repeats ") && frag.contains(" times>")) { // NOI18N
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
                if (ch == '"' && !isSlashBefore(s, idx)) {
                    inDoubleQuote = false;
                }
            } else if (inSingleQuote) {
                if (ch == '\'' && !isSlashBefore(s, idx)) {
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
            } else if (ch == '\"' && !isSlashBefore(s, idx)) {
                if (inDoubleQuote) {
                    inDoubleQuote = false;
                } else {
                    inDoubleQuote = true;
                }
            } else if (ch == '\'' && !isSlashBefore(s, idx)) {
                inSingleQuote = true;
            }
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
    
    public static String threadId() {
        Thread cur = Thread.currentThread();
        return cur.getName() + ':' + Long.toString(cur.getId());
    }

    public static int log10(int n) {
        int l = 1;
        while ((n = n / 10) > 0) {
            l++;
        }
        return l;
    }

    // We have the same in BreakpointsNodeModel
    private static final String ZEROS = "            "; // NOI18N

    public static String zeros(int n) {
        // Perf & mem optimization
        switch (n) {
            case 1 : return " "; // NOI18N
            case 2 : return "  "; // NOI18N
        }
        if (n < ZEROS.length()) {
            return ZEROS.substring(0, n);
        } else {
            String z = ZEROS;
            while (z.length() < n) {
                z += " ";  // NOI18N
            }
            return z;
        }
    }

    public static boolean comparePaths(int platform, String path1, String path2) {
        path1 = path1.trim();
        path2 = path2.trim();
        // we need to convert paths to unix-like style, so that normalization works correctly
        if (platform == PlatformTypes.PLATFORM_WINDOWS) {
            path1 = WinPath.win2cyg(path1).toLowerCase();
            path2 = WinPath.win2cyg(path2).toLowerCase();
        }
        // see isssue 152489, normalization is required to correctly compare paths with ..
        try {
            String norPath1 = new URI(path1).normalize().getPath();
            String norPath2 = new URI(path2).normalize().getPath();
            // use normalized paths only if both paths have been normalized correctly
            path1 = norPath1;
            path2 = norPath2;
        } catch (Exception e) {
            // do nothing
        }
        return path1.equals(path2);
    }

    /**
     * Compare the executable path from gdb (via the <i>info files</i> command to the executable path
     * from the project data. This comparison is <b>very</b> system dependent. For MacOS, all compares
     * are done in lower case. For Unix, they're done as-is. The most difficult platform is Windows,
     * where, depending on the gdb provider, I can see a Cygwin name, either forward or backward slashes,
     * either single or double backslashes (MinGW). The ".exe" may also be missing in some cases.
     *
     * @param exe1 The first path
     * @param exe2 The second path
     * @return True if exe1 and exe2 resolve to the same executable
     */
    public static boolean compareExePaths(int platform, String exe1, String exe2) {
        if (platform == PlatformTypes.PLATFORM_WINDOWS) {
            exe1 = removeExe(exe1);
            exe2 = removeExe(exe2);
        } else if (platform == PlatformTypes.PLATFORM_MACOSX) {
            exe1 = exe1.toLowerCase();
            exe2 = exe2.toLowerCase();
        }
        return comparePaths(platform, exe1, exe2);
    }

    private static String removeExe(String exe) {
        if (exe.endsWith(".exe")) { // NOI18N
            return exe.substring(0, exe.length() - 4);
        }
        return exe;
    }
}
