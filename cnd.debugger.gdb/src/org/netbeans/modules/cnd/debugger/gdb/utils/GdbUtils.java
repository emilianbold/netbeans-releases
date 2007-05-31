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
import java.util.Set;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Various miscelaneous static methods.
 *
 * @author Gordon Prieur
 */
public class GdbUtils {
    // FIXME - The following is a debug method. Remove if no longer needed
//    public static TopComponent getLocalsView() {
//        for (TopComponent tc : (Set<TopComponent>)TopComponent.getRegistry().getOpened()) {
//            if (tc.getClass().getName().equals("org.netbeans.modules.debugger.ui.views.View")) {
//                System.err.println("GdbUtils.getLocalsView: Got View class [" + tc.getName() + "]");
//            }
//        }
//        return null;
//    }
    
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
     *  Parse the input string for key/value pairs. Each key should be unique so
     *  results can be stored in a map.
     *
     *  @param info A string of key/value pairs
     *  @return A HashMap containing each key/value
     */
    public static Map createMapFromString(String info) {
        HashMap map = new HashMap();
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
                tend = findEndingCurlyBrace(info, i);
            } else if (ch == '"') {
                tend = findEndOfString(info, i);
            } else if (ch == '[') {
                tend = findEndingSquareBrace(info, i);
            } else {
                //throw new IllegalStateException(NbBundle.getMessage(
                //        GdbUtils.class, "ERR_UnexpectedGDBReasonMessage")); // NOI18N
                // It is useless to throw exception, because it will be caught
                // by thread STDOUT_READER and nobody will see it, and the
                // only effect of this exception will be slow responsiveness.
                // It is better to print this message to the log file.
//                log.fine("INTERNAL ERROR: GdbLogger.createMapFromString(msg) cannot parse message. msg="+info); // NOI18N
                break;
            }
            
            // put the value in the map and prepare for the next property
            value = info.substring(i, tend);
            if (Utilities.isWindows() && value.startsWith("/cygdrive/")) { // NOI18N
                value = value.charAt(10) + ":" + value.substring(11);
            }
            map.put(key, value);
            i = tend + 1;
            i++;
        }
        
        return map;
    }
    
    /**
     *  We're parsing a string of the form "key=[key1=value1,key2=value2,...]".
     *
     *  @param reason The result string from gdb
     *  @param i The index into result where we should start parsing
     *  @returns The index of the closing ']' in reason
     */
    private static int findEndingSquareBrace(String s, int idx) {
        int bcount = 0;
        char ch;
        int len = s.length();
        
        while (len-- > 0) {
            if ((ch = s.charAt(idx)) == ']' && bcount == 0) {
                return idx;
            } else {
                idx++;
                if (ch == '[') {
                    bcount++;
                } else if (ch == ']') {
                    bcount--;
                }
            }
        }
        throw new IllegalStateException(NbBundle.getMessage(
                GdbUtils.class, "ERR_UnexpectedGDBReasonMessage")); // NOI18N
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
     * Find the end of a { ... } block. With gdb 6.6, we can get unmatched curly braces
     * so I've added some checking for this. If there is no matching '}', I return the
     * index of the last character.
     *
     * @param s The string to parse
     * @param idx The index to start at
     * @returns The index of the closing '}' or of the last character
     */
    private static int findEndingCurlyBrace(String s, int idx) {
        char ch;
        int len = s.length() - idx;
        int count = 0;
        
        while (len-- > 0) {
            if ((ch = s.charAt(idx)) == '}' && count == 0) {
                return idx;
            } else {
                if (ch == '{') {
                    count++;
                } else if (ch == '}') {
                    count--;
                }
                idx++;
            }
        }
        
        // I used to throw an exception here but gdb sometimes returns an unmatched curly.
        return idx - 1;
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
        int i = 0;
        char ch;
        
        while (i < len) {
            tstart = i++;
            while (info.charAt(i++) != '=') {
            }
            key = info.substring(tstart, i - 1);
            if ((ch = info.charAt(i++)) == '{') {
                tend = findEndingCurlyBrace(info, i);
            } else if (ch == '"') {
                tend = findEndOfString(info, i);
            } else {
                throw new IllegalStateException(NbBundle.getMessage(
                        GdbUtils.class, "ERR_UnexpectedGDBReasonMessage")); // NOI18N
            }
            
            // put the value in the list and prepare for the next property
            value = info.substring(i, tend);
            if (Utilities.isWindows() && value.startsWith("/cygdrive/")) { // NOI18N
                value = value.charAt(10) + ":" + value.substring(11);
            }
            list.add(key + "=" + value); // NOI18N
            i = tend + 1;
            i++;
        }
        
        return list;
    }
    /*
     * Format: [{name="...",value="..."},{name="...",value="{{...}, {...}}"}]
     * Example: [{name="lar",value="{1, 2, 3}"},{name="lar2",value="{{1, 2}, {4, 5}, {7, 8}}"},{name="a",value="{fa = 0, fb = 4}"}]
     */
    private static List createSubTreeFromAllValues(String parent, String type, String allValues) {
        List list = new ArrayList();
        String sName;
        String sType = null;
        String sValue = null;
        String info = allValues;
        final String SINGLE_QUOTE = "'"; // NOI18N
        ArrayList arrayIndexes = new ArrayList();
        int len;
        int i, j, k, l, m;
        int sep1 = '{'; // separator 1
        int sep2 = ','; // separator 2
        int sep3 = '}'; // separator 3
        int openQuotes = 1;
        
        if (info == null) return list;
        String s = "{name=\""+parent+"\",value=\"{"; // NOI18N
        
        j = info.indexOf(s);
        if (j < 0) {
            if (type.indexOf("char [") >= 0) { // NOI18N
                // This is a special case: char[]
                sType = "char"; // NOI18N
                s = "{name=\""+parent+"\",value=\""; // NOI18N
                j = info.indexOf(s);
                if (j >= 0) {
                    s = info.substring(j + s.length());
                    m = s.indexOf("\"}"); // NOI18N
                    if (m >= 0) {
                        // Transform "simple" value to a list
                        s = s.substring(0, m);
                        if (s.startsWith("\\\"")) s = s.substring(2); // NOI18N
                        if (s.endsWith("\"")) s = s.substring(0, s.length() - 2); // NOI18N
                        // Get array size
                        int maxArrayIndex = 0;
                        m = type.indexOf("char ["); // NOI18N
                        if (m >= 0) {
                            for (m = m + "char [".length(); m < type.length(); m++) { // NOI18N
                                int c = type.charAt(m);
                                if ((c >= '0') && (c <= '9'))
                                    maxArrayIndex = (maxArrayIndex * 10) + (c -'0');
                                else break;
                            }
                        }
                        if (maxArrayIndex == 0) maxArrayIndex = s.length();
                        // Generate array
                        int arrayIndex = 0;
                        for (m = 0; arrayIndex < maxArrayIndex; m++) {
                            // Generate name
                            sName = parent;
                            sName += "[" + arrayIndex + "]"; // NOI18N
                            arrayIndex++;
                            if (m >= s.length()) {
                                list.add(sName);
                                list.add(sType);
                                list.add("null"); // NOI18N
                                continue;
                            }
                            // Generate value
                            int c = s.charAt(m);
                            if (c == '\\' ) {
                                // Tripple character
                                sValue = SINGLE_QUOTE;
                                sValue += (char) c;
                                if (m++ >= s.length()) break;
                                c = s.charAt(m);
                                if (c == '\\') {
                                    sValue += (char) c;
                                    for (k=0; k < 3; k++) {
                                        if (m++ >= s.length()) break;
                                        c = s.charAt(m);
                                        sValue += (char) c;
                                    }
                                    if (m >= s.length())
                                        break;
                                } else {
                                    sValue += (char) c; // A character like '\n'
                                }
                                sValue += SINGLE_QUOTE;
                            } else {
                                sValue = SINGLE_QUOTE + (char)c + SINGLE_QUOTE;
                            }
                            list.add(sName);
                            list.add(sType);
                            list.add(sValue);
                        }
                    }
                }
            } else {
                System.err.println("DEBUG: GdbProxyCL.createSubTreeFromAllValues() Unknown type" + type); // DEBUG // NOI18N
            }
            return list;
        }
        s = info.substring(j + s.length());
        len = s.length();
        // Add first index
        arrayIndexes.add(new Integer(0));
        for (i = 0; i < len; i++) {
            k = s.charAt(i);
            // New subtree
            if (k == '{') {
                // Add index
                arrayIndexes.add(new Integer(0));
                openQuotes++;
                continue;
            }
            // Accumulate value
            if ((k != sep2) && (k != sep3)) {
                if (sValue == null) {
                    if (k == ' ') continue;
                    sValue = ""; // NOI18N
                }
                sValue += s.substring(i, i+1);
                continue;
            }
            // Save element
            if (sValue != null) {
                l = sValue.indexOf(" = "); // NOI18N
                if (l < 0) {
                    // Generate name
                    sName = parent;
                    for (m = 0; m < arrayIndexes.size(); m++) {
                        sName += "[" + arrayIndexes.get(m) + "]"; // NOI18N
                    }
                    m = type.indexOf('['); // Expected array
                    if (m > 0) {
                        sType = type.substring(0, m);
                    }
                    // Increment last index
                    Integer ind = (Integer) arrayIndexes.get(arrayIndexes.size() - 1);
                    int intind = ind.intValue() + 1;
                    arrayIndexes.set(arrayIndexes.size() - 1, new Integer(intind));
                } else {
                    sName = parent + "." + sValue.substring(0, l); // NOI18N
                    //sType = null; // Unfortunately type of field in unknown
                    sValue = sValue.substring(l+3);
                    // Create gdb variable to get type and children number
//                    gdbProxy.var_create(null, null, sName); // FIXME (shouldn't do from here)
                }
                list.add(sName);
                list.add(sType);
                list.add(sValue);
                sValue = null;
            }
            // End of subtree
            if (k == '}') {
                if (--openQuotes == 0) {
                    return list;
                }
                // Remove last index
                if (arrayIndexes.size() > 0)
                    arrayIndexes.remove(arrayIndexes.size() - 1);
                // Increment last index
                if (arrayIndexes.size() > 0) {
                    Integer ind = (Integer) arrayIndexes.get(arrayIndexes.size() - 1);
                    int intind = ind.intValue() + 1;
                    arrayIndexes.set(arrayIndexes.size() - 1, new Integer(intind));
                }
            }
        }
        return list;
    }
    
    /**
     * Transforms gdb message to a string of arguments and returns this string.
     * This string is saved in private cache (saveArgs). It is used
     * by reportStackUpdate() method, which is called later.
     *
     * @param args a string from gdb with function arguments
     *
     * @return string of arguments
     */
    public static String getArguments(String args) {
        String reply = null;
        if (args == null) return null;
        int i = args.indexOf("args=["); // NOI18N
        if (i >= 0) {
            args = args.substring(i + 6);
            i = args.indexOf("],"); // NOI18N
            if (i >= 0) {
                args = args.substring(0, i);
                args = args.replaceAll("name=\"", ""); // NOI18N
                args = args.replaceAll("\",value=\"", " = "); // NOI18N
                args = args.replace("{", ""); // NOI18N
                args = args.replace("\"}", ""); // NOI18N
                args = args.replace(",", ", "); // NOI18N
                // kill double quoting like in "0xabc123 \\\"1\\\""
                args = args.replace("\\\"", "\""); // NOI18N
                args = "(" + args + ")"; // NOI18N
                reply = args;
            }
        }
        return reply;
    }
    
//    /**
//     *  Parse the input string for key=value pairs. The keys are not unique
//     *  and the order is very important. Each value is extracted and stored
//     *  in an ArrayList as a 3-Strings entry for each variable (name, type, value)
//     *  Input formats:
//     *   {name="A",type="int",value="1"},{name="A",type="int",value="1"},...
//     *   {name="a",value="{fa = 1}"},{name="b",value="{fs1 = {fa = 2}}"},...
//     *
//     *  @param info A string of key=value pairs. Format {name="A",type="int",value="1"},...
//     *
//     *  @return ArrayList with 3-Strings entry for each variable (name, type, value)
//     */
//    public static List createListFromLocalsStrings(String simpleValues, String allValues) {
//        List list = new ArrayList();
//        String sName, sType, sValue;
//        List lValue = null;
//        final String keyName1  = "{name=\"";     // NOI18N
//        final String keyNameN  = "\"},{name=\""; // NOI18N
//        final String keyType   = "\",type=\"";   // NOI18N
//        final String keyValue  = "\",value=\"";  // NOI18N
//        final String keyComma  = "\","; // NOI18N
//        final String keyOpenF  = "{";   // NOI18N
//        final String keyCloseF = "\"}"; // NOI18N
//        String info = simpleValues;
//        int len = info.length();
//        int i = 0;
//        int j, k, l, m , n, o;
//        
//        // Add parameters to locals
//        // Format: (arg1=value1, arg2=value2, ...)
//        String v = saveArgs;
//        if (!currentStackFrame.equals(TOPFRAME)) {
//                v = null; // Not implemented yet
//        }
//        if (v != null) {
//            if (v.startsWith("(")) { // NOI18N
//                v = v.substring(1);
//            }
//            if (v.endsWith(")")) { // NOI18N
//                v = v.substring(0, v.length() - 1);
//            }
//            for (int cp = 0; cp < v.length(); ) {
//                int index = v.indexOf(" = ", cp); // NOI18N
//                if (index < 0) break;
//                sName = v.substring(cp, index);
//                gdbProxy.var_create(null, null, sName); // Sends request to create variable
//                sType = gdbProxy.getVariableType(sName);
//                cp = index + 3;
//                index = v.indexOf(", ", cp); // NOI18N
//                if (index < 0) index = v.length();
//                sValue = v.substring(cp, index);
//                List leaf = new ArrayList();
//                leaf.add(sName);
//                leaf.add(sType);
//                leaf.add(sValue);
//                list.add(leaf);
//                cp = index + 2;
//            }
//        }
// 
//        if (info.equals("=[")) // NOI18N
//            return list; // Function without locals
//        while (i < len) {
//            sValue = null; // in case there is a non simple value
//            j = info.indexOf(keyName1, i);
//            if (j < 0) break; // End of list. Done.
//            j += keyName1.length();       // beginning of name
//            k = info.indexOf(keyType, j); // end of name
//            if (k < 0) break; // String is truncated
//            l = k + keyType.length();      // beginning of type
//            m = info.indexOf(keyValue, l); // end of type
//            if (m < 0) {
//                // Not a primitive variable
//                m = info.indexOf(keyNameN, l); // end of type
//                if (m < 0) {
//                    // probably the last one
//                    m = info.indexOf(keyCloseF, l); // end of type
//                    if (m < 0) break; // Something wrong
//                }
//                n = m + 2; // fake beginning of value
//                o = m + 2; // fake end of value
//            } else {
//                // Check if it is not a primitive variable
//                n = info.indexOf(keyCloseF, l); // end of value
//                if (n < 0) break; // Something wrong
//                if (m > n) {
//                    // Not a primitive variable
//                    m = n;
//                    n = m + 2; // fake beginning of value
//                    o = m + 2; // fake end of value
//                } else {
//                    n = m + keyValue.length();     // beginning of value
//                    o = info.indexOf(keyNameN, n); // end of value
//                    if (o < 0) {
//                        // probably the last one
//                        o = info.indexOf(keyCloseF, n); // end of value
//                        if (o < 0) break; // Something wrong
//                    }
//                }
//            }
//            sName = info.substring(j, k);
//            sType = info.substring(l, m);
//            if ( n < o) {
//                sValue = info.substring(n, o);
//            } else {
//                // Find value in allValues
//                lValue = createSubTreeFromAllValues(sName, sType, allValues);
//            }            
//            // Create a "leaf" list, initialize it with 3 strings (name, type, value),
//            // and add it to the list.
//            List leaf = new ArrayList();
//            leaf.add(sName);
//            leaf.add(sType);
//            if ( n < o) {
//                leaf.add(sValue);
//            } else {
//                if (lValue.size() < 3) {
//                    // Special case. Does it happen?
//                    sValue = (String) lValue.get(0);
//                    leaf.add(sValue);
//                } else {
//                    leaf.add(lValue);
//                }
//            }
//            list.add(leaf);
//            i = o + 1;
//        }
//        return list;
//    }
}
