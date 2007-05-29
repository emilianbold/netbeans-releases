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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.search;

import java.util.regex.Pattern;

/**
 * Parser of simple regular expressions with only three supported special
 * characters {@code '*'} (zero or more), {@code '?'} (zero or one)
 * and {@code '\\'} (quotes the following character).
 *
 * @author  Marian Petras
 */
final class RegexpMaker {
    
    /**
     * Translates the given simple pattern to a regular expression.
     * 
     * @param  simplePattern  pattern to be translated
     * @return  regular expression corresponding to the simple pattern
     */
    static String makeRegexp(String simplePattern) {
        if (simplePattern.length() == 0) {              //trivial case
            return simplePattern;
        }
        
        if (Pattern.matches("[a-zA-Z0-9 ]*", simplePattern)) {          //NOI18N
            return simplePattern;                       //trivial case
        }
        
        StringBuilder buf = new StringBuilder(simplePattern.length() + 16);
        boolean quoted = false;
        boolean starPresent = false;
        for (char c : simplePattern.toCharArray()) {
            if (quoted) {
                if (!isSimpleCharacter(c)) {
                    buf.append('\\');
                }
                buf.append(c);
                quoted = false;
            } else if (c == '?') {
                buf.append('.');
            } else if (c == '*') {
                starPresent = true;
            } else if (c == '\\') {
                if (starPresent) {
                    buf.append('.').append('*').append('?');
                    starPresent = false;
                }
                quoted = true;
            } else {
                if (starPresent) {
                    buf.append('.').append('*').append('?');
                    starPresent = false;
                }
                if (!isSimpleCharacter(c)) {
                    buf.append('\\');
                }
                buf.append(c);
            }
        }
        if (quoted) {
            buf.append('\\').append('\\');
            quoted = false;
        } else if (starPresent) {
            buf.append('.').append('*');
            starPresent = false;
        }
        return buf.toString();
    }
    
    /**
     * Translates the given simple pattern (or several patterns) to a single
     * regular expression.
     * 
     * @param  simplePatternList  pattern list to be translated
     * @return  regular expression corresponding to the simple pattern
     *          (or to the list of simple patterns)
     */
    static String makeMultiRegexp(String simplePatternList) {
        if (simplePatternList.length() == 0) {              //trivial case
            return simplePatternList;
        }
        
        if (Pattern.matches("[a-zA-Z0-9]*", simplePatternList)) {       //NOI18N
            return simplePatternList;                       //trivial case
        }
        
        StringBuilder buf = new StringBuilder(simplePatternList.length() + 16);
        boolean lastWasSeparator = false;
        boolean quoted = false;
        boolean starPresent = false;
        for (char c : simplePatternList.toCharArray()) {
            if (quoted) {
                if (!isSimpleCharacter(c)) {
                    buf.append('\\');
                }
                buf.append(c);
                quoted = false;
            } else if ((c == ',') || (c == ' ')) {
                if (starPresent) {
                    buf.append('.').append('*');
                    starPresent = false;
                }
                lastWasSeparator = true;
            } else {
                if (lastWasSeparator && (buf.length() != 0)) {
                    buf.append('|');
                }
                if (c == '?') {
                    buf.append('.');
                } else if (c == '*') {
                    starPresent = true;
                } else {
                    if (starPresent) {
                        buf.append('.').append('*');
                        starPresent = false;
                    }
                    if (c == '\\') {
                        quoted = true;
                    } else {
                        if (!isSimpleCharacter(c)) {
                            buf.append('\\');
                        }
                        buf.append(c);
                    }
                }
                lastWasSeparator = false;
            }
        }
        if (quoted) {
            buf.append('\\').append('\\');
            quoted = false;
        } else if (starPresent) {
            buf.append('.').append('*');
            starPresent = false;
        }
        return buf.toString();
    }
    
    private static boolean isSimpleCharacter(char c) {
        int cint = (int) c;
        return (cint == 0x20)                               //space
                || (cint > 0x7f)                            //non-ASCII
                || (cint >= 0x30) && (cint <= 0x39)          //'0' .. '9'
                || (cint & ~0x7f) == 0
                    && ((cint &= ~0x20) >= 0x41) && (cint <= 0x5a); //a..z,A..Z
    }

}