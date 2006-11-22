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
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.modules.el.lexer.api.ELTokenId;

/**
 * Test HTML lexer analyzis
 *
 * @author Marek Fukala
 */
public class ELLexerBatchTest extends TestCase {

    public ELLexerBatchTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testExpressions() {
        String text = "session";
        TokenSequence ts = TokenHierarchy.create(text, ELTokenId.language()).tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, ELTokenId.IDENTIFIER, "session");
        
        text = "(6 * 0x5) + 05";
        ts = TokenHierarchy.create(text, ELTokenId.language()).tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, ELTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, ELTokenId.INT_LITERAL, "6");
        LexerTestUtilities.assertNextTokenEquals(ts, ELTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, ELTokenId.MUL, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, ELTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, ELTokenId.HEX_LITERAL, "0x5");
        LexerTestUtilities.assertNextTokenEquals(ts, ELTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, ELTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, ELTokenId.PLUS, "+");
        LexerTestUtilities.assertNextTokenEquals(ts, ELTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, ELTokenId.OCTAL_LITERAL, "05");
        
        
    }
    
}
