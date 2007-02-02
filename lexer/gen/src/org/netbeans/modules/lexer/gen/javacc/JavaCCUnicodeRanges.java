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

package org.netbeans.modules.lexer.gen.javacc;

import org.netbeans.modules.lexer.gen.util.UnicodeRanges;

/**
 * Program that writes Unicode character ranges to the output
 * depending on the method being used.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class JavaCCUnicodeRanges extends UnicodeRanges {

    public static String findRangesDescription(int testedMethod, int indent) {
        StringBuffer sb = new StringBuffer();
        char[] ranges = findRanges(testedMethod);
        for (int i = 0; i < ranges.length;) {
            if (i > 0) {
                sb.append(",\n");
            }
            indent(sb, indent);
            int rangeStart = ranges[i++];
            int rangeEnd = ranges[i++];
            appendUnicodeChar(sb, (char)rangeStart, '"');
            if (rangeStart < rangeEnd) { // interval
                sb.append(" - ");
                appendUnicodeChar(sb, (char)(rangeEnd), '"');
            }
        }
        
        return sb.toString();
    }
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println(usage());
        }

        System.out.println(findRangesDescription(
            Integer.parseInt(args[0]),
            Integer.parseInt(args[1])
        ));
    }

}
