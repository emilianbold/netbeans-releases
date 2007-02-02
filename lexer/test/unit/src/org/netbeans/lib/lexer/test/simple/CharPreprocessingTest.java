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
        TokenHierarchy hi = TokenHierarchy.create(text, SimpleTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
//        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.WHITESPACE, " \\u0020 ", 0);
//        assertTrue(ts.moveNext());
//        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.PUBLIC, "public", 8);
//        assertFalse(ts.moveNext());
    }    

}
