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
 * Utility methods.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class LexerGenUtilities {

    /**
     * Append given number of spaces to the given string buffer.
     */
    public static void appendSpaces(StringBuffer sb, int spaceCount) {
        while (--spaceCount >= 0) {
            sb.append(' ');
        }
    }

    /** Split class full name into package name and class name.
     * @param full name of the class
     * @return array containing package name and the class name.
     */
    public static String[] splitClassName(String classFullName) {
        int lastDotIndex = classFullName.lastIndexOf('.');
        return new String[] {
            (lastDotIndex >= 0) ? classFullName.substring(0, lastDotIndex) : "", // pkg name
            classFullName.substring(lastDotIndex + 1) // class name
        };
    }
    
    /** @return original string converted to uppercase with
     * hyphens converted to underscores.
     * @param s original string.
     */
    public static String idToUpperCase(String s) {
        return s.toUpperCase().replace('-', '_');
    }
    
    /** @return original string converted to lowercase with
     * underscores converted to hyphens.
     * @param s original string.
     */
    public static String idToLowerCase(String s) {
        return s.toLowerCase().replace('_', '-');
    }

    /** Convert string from the shape as it appears
     * in the source file into regular java string.
     */
    public static String fromSource(String s) {
        return LexerGenUtilitiesImpl.fromSource(s);
    }

    /**
     * Escape passed string as XML element content (<code>&lt;</code>, 
     * <code>&amp;</code> and <code>><code> in <code>]]></code> sequences).
     * @param text non-null string to be escaped
     * @return escaped text for xml
     */    
    public static String toElementContent(String text) {
        return LexerGenUtilitiesImpl.toElementContent(text);
    }

}
