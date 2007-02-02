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

package org.netbeans.modules.lexer.gen.util;

/**
 * Program that generates Unicode character ranges array
 * depending on the method being used.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class UnicodeRanges {

    public static final int IS_JAVA_IDENTIFIER_START = 1;

    public static final int IS_JAVA_IDENTIFIER_PART = 2;

    public static char[] findRanges(int testedMethod) {
        char[] ranges = new char[16]; // will grow very likely
        int rangesCount = 0;
        int rangeStart = -1;
        for (int i = 0; i < 65536; i++) {
            boolean valid = false;
            switch (testedMethod) {
                case IS_JAVA_IDENTIFIER_START:
                    valid = Character.isJavaIdentifierStart((char)i);
                    break;
                    
                case IS_JAVA_IDENTIFIER_PART:
                    valid = Character.isJavaIdentifierPart((char)i);
                    break;
                    
            }
            
            // The following code gets rid of post-handling code after for loop
            if (i == 65535 && valid) {
                if (rangeStart < 0) {
                    rangeStart = i;
                }
                i++;
                valid = false;
            }
                
            if (valid) {
                if (rangeStart < 0) {
                    rangeStart = i;
                }
                
            } else { // not valid
                if (rangeStart >= 0) {
                    // Check sufficient space in ranges array
                    if (ranges.length - rangesCount < 2) {
                        char[] tmp = new char[ranges.length * 2];
                        System.arraycopy(ranges, 0, tmp, 0, rangesCount);
                        ranges = tmp;
                    }
                    ranges[rangesCount++] = (char)rangeStart;
                    ranges[rangesCount++] = (char)(i - 1);

                    rangeStart = -1;
                }
            }
        }
        
        if (rangesCount < ranges.length) {
            char[] tmp = new char[rangesCount];
            System.arraycopy(ranges, 0, tmp, 0, rangesCount);
            ranges = tmp;
        }
        return ranges;
    }
    
    public static void appendUnicodeChar(StringBuffer sb, char ch, char quoteChar) {
        String ret = Integer.toHexString(ch);
        while (ret.length() < 4) {
            ret = "0" + ret;
        }
        sb.append(quoteChar);
        sb.append("\\u");
        sb.append(ret);
        sb.append(quoteChar);
    }
    
    public static void indent(StringBuffer sb, int indent) {
        while (indent-- > 0) {
            sb.append(' ');
        }
    }
    
    protected static String usage() {
        return "Prints ranges of characters belonging to selected category\n"
            + "arg0=Tested method:\n"
            + "        1 - Character.isJavaIdentifierStart()\n"
            + "        2 - Character.isJavaIdentifierPart()\n"
            + "arg1=Indentation e.g. 8\n";
    }
    
}
