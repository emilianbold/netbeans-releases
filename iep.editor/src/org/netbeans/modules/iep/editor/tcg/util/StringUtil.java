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

package org.netbeans.modules.iep.editor.tcg.util;

import java.util.*;
import java.util.regex.*;

/**
 * String utilities
 *
 * @author    Bing Lu
 * @created   January 20, 2003
 * @version   1.0
 */
public class StringUtil {
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(StringUtil.class.getName());
    
    /**
    *  concat({"ab", "c", "de"}, "_") -> "ab_c_de"
     */
    public static String concat(String[] s, String delim) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length; i++) {
            if (i < s.length -1) {
                sb.append(s[i] + delim);
            } else {
                sb.append(s[i]);
            }
        }
        return sb.toString();
    }
    
    public static String[] clone(String[] s) {
        if (s == null) {
            return null;
        }
        String[] ret = new String[s.length];
        System.arraycopy(s, 0, ret, 0, s.length);
        return ret;
    }
    
    public static String[] cloneAppend(String[] s, String toAppend) {
        if (s == null) {
            return new String[]{toAppend};
        }
        String[] ret = new String[s.length + 1];
        System.arraycopy(s, 0, ret, 0, s.length);
        ret[ret.length - 1] = toAppend;
        return ret;
    }
    
    public static String[] cloneSection(String[] s, int start, int length) {
        String[] ret = new String[length];
        System.arraycopy(s, start, ret, 0, length);
        return ret;
    }

    /**
     * Gets the group attribute of the StringUtil class
     *
     * @param input  Description of the Parameter
     * @param regex  Description of the Parameter
     * @param group  Description of the Parameter
     * @return       The group value
     */
    public static String group(CharSequence input, String regex, int group) {
        String ret = null;

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        boolean bRet = matcher.find();
        if (bRet) {
            if (group <= matcher.groupCount()) {
                ret = matcher.group(group);
            }
        }
        return ret;
    }

    public static List matches(CharSequence input, String regex, int group) {
        List ret = new ArrayList();

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            if (group <= matcher.groupCount()) {
                ret.add(matcher.group(group));
            }
        }
        return ret;
    }    
    
    /**
     * Description of the Method
     *
     * @param input  Description of the Parameter
     * @param regex  Description of the Parameter
     * @return       Description of the Return Value
     */
    public static boolean contains(CharSequence input, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.find();
    }

    public static String replaceLast(CharSequence input, String regex, String replacement) {
        String ret = null;

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        int count = containsCount(input, regex);
        int index = 0;
        StringBuffer buf = new StringBuffer();
        while (matcher.find()) {
            index++;
            if (index == count) {
                matcher.appendReplacement(buf, replacement);
            }
        }
        matcher.appendTail(buf);
        ret = buf.toString();
        return ret;
    }

    public static int containsCount(CharSequence input, String regex) {
        int ret = 0;

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            ret++;
        }
        return ret;
    }

    /**
     * Appends a line to the StringBuffer
     *
     * @param buf  the StringBuffer
     */
    public static void appendln(StringBuffer buf) {
        buf.append(lineSeparator());
    }

    /**
     * Appends a line to the StringBuffer
     *
     * @param buf  the StringBuffer
     * @param o    the object
     */
    public static void appendln(StringBuffer buf, Object o) {
        buf.append(o);
        buf.append(lineSeparator());
    }

    /**
     * Returns System.getProperty("line.separator")
     *
     * @return   returns System.getProperty("line.separator")
     */
    public static String lineSeparator() {
        return System.getProperty("line.separator");
    }
    
    public static String lowerCaseFirstChar(String s) {
        if (s.length() > 0) {
            char ch = Character.toLowerCase(s.charAt(0));
            if (s.length() > 1) {
                s = Character.toString(ch) + s.substring(1);
            } else {
                s = Character.toString(ch);
            }
        }
        return s;
    }    
    
    public static List getTokenList(String s, String delim) {
        StringTokenizer st = new StringTokenizer(s, delim);
        ArrayList list = new ArrayList();
        while (st.hasMoreTokens()) {
            String t = st.nextToken();
            list.add(t);
        }
        return list;
    }
    
}

/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/
/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/

