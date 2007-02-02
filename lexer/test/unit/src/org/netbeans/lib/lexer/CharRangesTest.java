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

package org.netbeans.lib.lexer;

import java.util.List;
import junit.framework.TestCase;
import org.netbeans.lib.lexer.test.CharRangesDump;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class CharRangesTest extends TestCase {

    public CharRangesTest(String testName) {
        super(testName);
    }

    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testCharRanges() {
        // Check that character ranges of accepted characters for certain
        // methods of java.lang.Character match expectations

        //new CharRangesDump(new CharRangesDump.CharacterMethodAcceptor("isWhitespace")).dump();
        //new CharRangesDump(new CharRangesDump.CharacterMethodAcceptor("isWhitespace")).dumpAsserts();
        List<Integer> charRanges = new CharRangesDump(
                new CharRangesDump.CharacterMethodAcceptor("isWhitespace")).charRanges();
        TestCase.assertEquals(charRanges.get(0).intValue(), 0x9);
        TestCase.assertEquals(charRanges.get(1).intValue(), 0xd);
        TestCase.assertEquals(charRanges.get(2).intValue(), 0x1c);
        TestCase.assertEquals(charRanges.get(3).intValue(), 0x20);
        
        //new CharRangesDump(new CharRangesDump.CharacterMethodAcceptor("isJavaIdentifierStart")).dump();
        //new CharRangesDump(new CharRangesDump.CharacterMethodAcceptor("isJavaIdentifierStart")).dumpAsserts();
        charRanges = new CharRangesDump(
                new CharRangesDump.CharacterMethodAcceptor("isJavaIdentifierStart")).charRanges();
        TestCase.assertEquals(charRanges.get(0).intValue(), 0x24);
        TestCase.assertEquals(charRanges.get(1).intValue(), 0x24);
        TestCase.assertEquals(charRanges.get(2).intValue(), 0x41);
        TestCase.assertEquals(charRanges.get(3).intValue(), 0x5a);
        TestCase.assertEquals(charRanges.get(4).intValue(), 0x5f);
        TestCase.assertEquals(charRanges.get(5).intValue(), 0x5f);
        TestCase.assertEquals(charRanges.get(6).intValue(), 0x61);
        TestCase.assertEquals(charRanges.get(7).intValue(), 0x7a);
        

        //new CharRangesDump(new CharRangesDump.CharacterMethodAcceptor("isJavaIdentifierPart")).dump();
        //new CharRangesDump(new CharRangesDump.CharacterMethodAcceptor("isJavaIdentifierPart")).dumpAsserts();
        charRanges = new CharRangesDump(
                new CharRangesDump.CharacterMethodAcceptor("isJavaIdentifierPart")).charRanges();
        TestCase.assertEquals(charRanges.get(0).intValue(), 0x0);
        TestCase.assertEquals(charRanges.get(1).intValue(), 0x8);
        TestCase.assertEquals(charRanges.get(2).intValue(), 0xe);
        TestCase.assertEquals(charRanges.get(3).intValue(), 0x1b);
        TestCase.assertEquals(charRanges.get(4).intValue(), 0x24);
        TestCase.assertEquals(charRanges.get(5).intValue(), 0x24);
        TestCase.assertEquals(charRanges.get(6).intValue(), 0x30);
        TestCase.assertEquals(charRanges.get(7).intValue(), 0x39);
        TestCase.assertEquals(charRanges.get(8).intValue(), 0x41);
        TestCase.assertEquals(charRanges.get(9).intValue(), 0x5a);
        TestCase.assertEquals(charRanges.get(10).intValue(), 0x5f);
        TestCase.assertEquals(charRanges.get(11).intValue(), 0x5f);
        TestCase.assertEquals(charRanges.get(12).intValue(), 0x61);
        TestCase.assertEquals(charRanges.get(13).intValue(), 0x7a);
        TestCase.assertEquals(charRanges.get(14).intValue(), 0x7f);
        TestCase.assertEquals(charRanges.get(15).intValue(), 0x9f);

        TestCase.assertEquals((char)-1, 0xFFFF);
    }

}
