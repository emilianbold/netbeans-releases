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
                tend = findMatchingBrace("{}", info, i) - 1; // NOI18N
            } else if (ch == '"') {
                tend = findEndOfString(info, i);
            } else if (ch == '[') {
                tend = findMatchingBrace("[]", info, i); // NOI18N
            } else {
//                log.fine("INTERNAL ERROR: GdbLogger.createMapFromString(msg) cannot parse message. msg="+info); // NOI18N
                break;
            }
            
            // put the value in the map and prepare for the next property
            value = info.substring(i, tend);
            if (Utilities.isWindows() && value.startsWith("/cygdrive/")) { // NOI18N
                value = value.charAt(10) + ":" + value.substring(11);
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
        int i = 0;
        char ch;
        
        while (i < len) {
            tstart = i++;
            while (info.charAt(i++) != '=') {
            }
            key = info.substring(tstart, i - 1);
            if ((ch = info.charAt(i++)) == '{') {
                tend = findMatchingBrace("{}", info, i) - 1; // NOI18N
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
    
    /**
     * Called with the response of -stack-list-locals. Read the locals string and get
     * the variable information.
     *
     * @param info The string returned from -stack-list-locals with the header removed
     * @return A List containing GdbVariables of each argument
     */
    public static List createLocalsList(String info) {
        String name, type, value;   // used to create GdbVariable
        List<GdbVariable> list = new ArrayList();
        int len = info.length();
        int pos, pos2;
        int idx = 0;
        
        while (len > 0) {
            String frag = info.substring(idx, findMatchingBrace("{}", info, idx)); // NOI18N
            idx += frag.length() + 1;
            len -= frag.length() + 1;
            // {name=\"len\",type=\"int\",value=\"0\"},{...}
            if (frag.startsWith("{name=\"") && frag.endsWith("\"}")) { // NOI18N
                pos = frag.indexOf("\",type=\""); // NOI18N
                if (pos > 0) {
                    name = frag.substring(7, pos);
                    pos2 = frag.indexOf("\",value=\"", pos); // NOI18N
                    if (pos2 > 0) {
                        type = frag.substring(pos + 8, pos2);
                        value = frag.substring(pos2 + 9, frag.length() - 2);
                    } else {
                        type = frag.substring(pos + 8, frag.length() - 2);
                        value = null;
                    }
                    list.add(new GdbVariable(name, type, value));
                }
            }
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
        String name, type, value;   // used to create GdbVariable
        List<GdbVariable> list = new ArrayList();
        int len = info.length();
        int pos, pos2;
        int idx = 0;
        
        while (len > 0) {
            String frag = info.substring(idx, findMatchingBrace("{}", info, idx)); // NOI18N
            idx += frag.length() + 1;
            len -= frag.length() + 1;
            // {name=\"argc\",value=\"1\"},{name=\"argv\",value=\"(char **) 0x6625f8\"}
            if (frag.startsWith("{name=\"") && frag.endsWith("\"}")) { // NOI18N
                pos = frag.indexOf("\",value=\""); // NOI18N
                if (pos > 0) {
                    name = frag.substring(7, pos);
                    String rem = frag.substring(pos + 9, frag.length() - 2);
                    if (rem.charAt(0) == '(') {
                        pos2 = findMatchingBrace("()", rem, 0); // NOI18N
                        type = rem.substring(1, pos2 - 1);
                        value = rem.substring(pos2, rem.length() - 1);
                    } else if (rem.charAt(0) == '{') {
                        type = null;
                        value = rem;
                    } else {
                        type = null;
                        value = rem;
                    }
                    list.add(new GdbVariable(name, type, value));
                }
            }
        }
        
        return list;
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
     * @param pair A 2 character string with a beginning char and an end char
     * @param s The string to parse
     * @param idx The index to start at
     * @returns The index of the closing '}' or of the last character
     */
    private static int findMatchingBrace(String pair, String s, int idx) {
        char lbrace = pair.charAt(0);
        char rbrace = pair.charAt(1);
        char last = ' ';
        int len = s.length() - idx;
        int count = 0;
        boolean inDoubleQuote = false;
        boolean inSingleQuote = false;
        
        assert pair != null && pair.length() == 2;
        assert s != null && s.length() > 0;
        assert idx >= 0 && idx < s.length();
        
        if (s.charAt(idx) == lbrace) {
            idx++;
            len--;
        }
        
        while (len-- > 0) {
            char ch = s.charAt(idx++);
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
            } else if (ch == '\"') {
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
        }
        
        // I used to throw an exception here but gdb sometimes returns an unmatched curly.
        return idx - 1;
    }
}
