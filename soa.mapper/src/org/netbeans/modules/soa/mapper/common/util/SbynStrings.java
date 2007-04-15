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

package org.netbeans.modules.soa.mapper.common.util;

import java.util.ArrayList;
import java.util.List;

import java.util.StringTokenizer;
import java.util.Vector;

/**
 * A Class utility for String related manipulations
 *
 * @author    <programmer name>
 *
 *
 * @created   December 3, 2002
 * @version   
 */
public class SbynStrings {
    private SbynStrings() { }

    private static final String[] EMPTY_ARRAY = new String[0];

    /**
     * parse white-space separated string into tokens return tokens
     * found, empty string if nothing found
     *
     * @param input  Description of the Parameter
     * @return       Description of the Return Value
     */
    public static String[] tokenize(String input) {
        Vector v = new Vector();
        StringTokenizer t = new StringTokenizer(input);
        String tokens[] = EMPTY_ARRAY;

        while (t.hasMoreTokens()) {
            v.addElement(t.nextToken());
        }

        int nTokens = v.size();
        if (nTokens > 0) {
            tokens = new String[nTokens];
            v.copyInto(tokens);
        }

        return tokens;
    }

    /**
     * parses the string with the given delimiters into tokens return
     * tokens found, empty string if nothing found
     *
     * @param input       Description of the Parameter
     * @param delimiters  Description of the Parameter
     * @return            Description of the Return Value
     */
    public static String[] tokenize(String input, String delimiters) {
        Vector v = new Vector();
        StringTokenizer t = new StringTokenizer(input, delimiters);
        String tokens[] = EMPTY_ARRAY;

        while (t.hasMoreTokens()) {
            v.addElement(t.nextToken());
        }

        int nTokens = v.size();
        if (nTokens > 0) {
            tokens = new String[nTokens];
            v.copyInto(tokens);
        }

        return tokens;
    }

    /**
     * Description of the Method
     *
     * @param sourceStr    Description of the Parameter
     * @param key          Description of the Parameter
     * @param replacement  Description of the Parameter
     * @return             Description of the Return Value
     */
    public static String replaceAll(String sourceStr, String key, String replacement) {

        int index = sourceStr.indexOf(key);
        if (index < 0) {
            return sourceStr;
        }
        String subStr = sourceStr.substring(0, index);
        subStr = subStr + replacement;
        int nextIndex = index + key.length();
        subStr = subStr + replaceAll(sourceStr.substring(nextIndex), key, replacement);
        return subStr;

    }

    /**
     * Gets the stringNullEmpty attribute of the SbynStrings class
     *
     * @param string  Description of the Parameter
     * @return        The stringNullEmpty value
     */
    public static boolean isStringNullEmpty(String string) {
        if (string == null) {
            return true;
        }
        if (string.trim().equals("")) {
            return true;
        }
        return false;
    }

    /**
     * parses the string with the given delimiters into tokens return a
     * list of tokens or an empty list.
     *
     * @param input       Description of the Parameter
     * @param delimiters  Description of the Parameter
     * @return            Description of the Return Value
     */
    public static List tokenizeToList(String input, String delimiters) {
        List list = new ArrayList();
        StringTokenizer t = new StringTokenizer(input, delimiters);

        while (t.hasMoreTokens()) {
            list.add(t.nextToken());
        }

        return list;
    }
}
