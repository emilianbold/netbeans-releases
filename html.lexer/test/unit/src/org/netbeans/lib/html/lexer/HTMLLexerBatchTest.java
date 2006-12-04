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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.html.lexer;

import junit.framework.TestCase;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * Test HTML lexer analyzis
 *
 * @author Marek Fukala
 */
public class HTMLLexerBatchTest extends TestCase {

    public HTMLLexerBatchTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testJspTags() {
        String text = "<jsp:useBean name=\"pkg.myBean\"/><!--comment-->abc&gt;def<tag attr=\"value\"></tag>";
                
        TokenHierarchy<?> hi = TokenHierarchy.create(text, HTMLTokenId.language());
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TAG_OPEN_SYMBOL, "<");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TAG_OPEN, "jsp:useBean");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.WS, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.ARGUMENT, "name");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.OPERATOR, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.VALUE, "\"pkg.myBean\"");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TAG_CLOSE_SYMBOL, "/>");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.BLOCK_COMMENT, "<!--comment-->");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TEXT, "abc");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.CHARACTER, "&gt;");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TEXT, "def");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TAG_OPEN_SYMBOL, "<");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TAG_OPEN, "tag");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.WS, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.ARGUMENT, "attr");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.OPERATOR, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.VALUE, "\"value\"");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TAG_CLOSE_SYMBOL, ">");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TAG_OPEN_SYMBOL, "</");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TAG_CLOSE, "tag");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TAG_CLOSE_SYMBOL, ">");
    }
    
}
