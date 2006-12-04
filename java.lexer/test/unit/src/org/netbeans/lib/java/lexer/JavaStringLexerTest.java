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
package org.netbeans.lib.java.lexer;

import org.netbeans.api.java.lexer.JavaStringTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 *
 * @author Jan Lahoda
 */
public class JavaStringLexerTest extends NbTestCase {

    public JavaStringLexerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    public void testNextToken1() {
        String text = "t";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaStringTokenId.language());
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavaStringTokenId.TEXT, "t");
    }
    
    public void testNextToken2() {
        String text = "\\t\\b\\b\\t \\tabc\\rsddfdsffffffffff";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaStringTokenId.language());
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavaStringTokenId.TAB, "\\t");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaStringTokenId.BACKSPACE, "\\b");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaStringTokenId.BACKSPACE, "\\b");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaStringTokenId.TAB, "\\t");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaStringTokenId.TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaStringTokenId.TAB, "\\t");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaStringTokenId.TEXT, "abc");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaStringTokenId.CR, "\\r");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaStringTokenId.TEXT, "sddfdsffffffffff");
    }
    
}
