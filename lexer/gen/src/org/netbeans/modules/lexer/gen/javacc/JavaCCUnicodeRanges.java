/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
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
