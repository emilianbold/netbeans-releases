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

package org.netbeans.lib.lexer.test.inc;

import junit.framework.TestCase;
import org.netbeans.lib.lexer.inc.OriginalText;

/**
 * Test for the text that emulates state of a mutable text input
 * before a particular modification.
 *
 * @author mmetelka
 */
public class OriginalTextTest extends TestCase {

    public OriginalTextTest(String testName) {
        super(testName);
    }

    public void test() throws Exception {
        String orig = "abcdef";
        check(orig, 0, 2, "xyz");
        check(orig, 0, 2, "x");
        check(orig, 0, 0, "");
        check(orig, 0, 0, "klmnopqrst");
        check(orig, orig.length(), 0, "");
        check(orig, orig.length(), 0, "klmnopqrst");
        check(orig, orig.length(), 0, "x");
        check(orig, 3, 0, "x");
        check(orig, 3, 1, "xyz");
        check(orig, 3, 3, "xy");
        check(orig, 1, 0, "x");
        check(orig, 1, 1, "xyz");
        check(orig, 1, 3, "xy");
        check(orig, 4, 0, "x");
        check(orig, 4, 1, "xy");
        check(orig, 4, 2, "x");
    }

    private void check(String text, int removeIndex, int removeLength, String insertText) {
        String modText = text.substring(0, removeIndex) + insertText + text.substring(removeIndex + removeLength);
        OriginalText ot = new OriginalText(modText, removeIndex, text.substring(removeIndex, removeIndex + removeLength), insertText.length());
        assertEquals(text.length(), ot.length());
        for (int i = 0; i < text.length(); i++) {
            assertEquals(String.valueOf(i), text.charAt(i), ot.charAt(i));
        }
        for (int i = 0; i < text.length(); i++) {
            for (int j = i; j < text.length(); j++) {
                assertEquals(text.substring(i, j), String.valueOf(ot.toCharArray(i, j)));
            }
        }
        assertEquals(text, ot.toString());
    }
    
}
