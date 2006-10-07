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

package org.netbeans.lib.lexer.test.simple;

import junit.framework.TestCase;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * Test character preprocessing.
 *
 * @author mmetelka
 */
public class CharPreprocessingTest extends TestCase {
    
    public CharPreprocessingTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testMaxFlySequenceLength() {
        String text = " \\u0020 public";
        TokenHierarchy hi = TokenHierarchy.create(text, SimpleLanguage.description());
        TokenSequence ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
//        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.WHITESPACE, " \\u0020 ", 0);
//        assertTrue(ts.moveNext());
//        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.PUBLIC, "public", 8);
//        assertFalse(ts.moveNext());
    }    

}
