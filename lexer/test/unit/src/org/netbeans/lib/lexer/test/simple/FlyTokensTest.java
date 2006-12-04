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
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class FlyTokensTest extends TestCase {
    
    public FlyTokensTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testMaxFlySequenceLength() {
        // Both "public" and " " are flyweight
        String text = "public public public public public public public ";
        int commentTextStartOffset = 5;
        TokenHierarchy<?> hi = TokenHierarchy.create(text, SimpleTokenId.language());
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        int firstNonFlyIndex = -1;
        int secondNonFlyIndex = -1;
        int tokenIndex = 0;
        for (int i = 0; i < 7; i++) {
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.PUBLIC, "public", -1);
            if (!ts.token().isFlyweight()) {
                if (firstNonFlyIndex == -1) {
                    firstNonFlyIndex = tokenIndex;
                } else if (secondNonFlyIndex == -1) {
                    secondNonFlyIndex = tokenIndex;
                }
            }
            tokenIndex++;

            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.WHITESPACE, " ", -1);
            if (!ts.token().isFlyweight()) {
                if (firstNonFlyIndex == -1) {
                    firstNonFlyIndex = tokenIndex;
                } else if (secondNonFlyIndex == -1) {
                    secondNonFlyIndex = tokenIndex;
                }
            }
            tokenIndex++;
        }
        assertEquals(LexerUtilsConstants.MAX_FLY_SEQUENCE_LENGTH, firstNonFlyIndex);
        assertEquals(LexerUtilsConstants.MAX_FLY_SEQUENCE_LENGTH * 2 + 1, secondNonFlyIndex);
    }    
}
