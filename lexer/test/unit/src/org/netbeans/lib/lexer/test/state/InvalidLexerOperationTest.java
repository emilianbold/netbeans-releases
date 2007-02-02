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

package org.netbeans.lib.lexer.test.state;

import javax.swing.text.Document;
import junit.framework.TestCase;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;

/**
 * Test of invalid lexer's behavior.
 *
 * @author mmetelka
 */
public class InvalidLexerOperationTest extends TestCase {
    
    public InvalidLexerOperationTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testEarlyNullToken() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        InputAttributes attrs = new InputAttributes();
        doc.putProperty(InputAttributes.class, attrs);
        
        // Insert text into document
        String text = "abc";
        doc.insertString(0, text, null);
        // Put the language now into the document so that lexing starts from scratch
        doc.putProperty(Language.class, StateTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();

        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, StateTokenId.A, "a", 0);
        assertEquals(LexerTestUtilities.lookahead(ts), 0);
        assertEquals(LexerTestUtilities.state(ts), StateLexer.AFTER_A);

        attrs.setValue(StateTokenId.language(), "returnNullToken", Boolean.TRUE, true);
        try {
            // Lexer will return null token too early
            assertTrue(ts.moveNext());
            fail("IllegalStateException not thrown when null token returned before input end.");
        } catch (IllegalStateException e) {
            // Expected fail of lexer
        }
    }

    public void testBatchLexerRelease() throws Exception {
        String text = "ab";
        InputAttributes attrs = new InputAttributes();
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, StateTokenId.language(),
                null, attrs);
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, StateTokenId.A, "a", 0);
        assertTrue(ts.moveNext());
        LanguagePath lp = LanguagePath.get(StateTokenId.language());
        assertFalse(Boolean.TRUE.equals(attrs.getValue(lp, "lexerRelease")));
        LexerTestUtilities.assertTokenEquals(ts, StateTokenId.BMULTI, "b", 1);
        assertFalse(ts.moveNext());
        assertTrue(Boolean.TRUE.equals(attrs.getValue(lp, "lexerRelease")));

    }

    public void testIncLexerRelease() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        InputAttributes attrs = new InputAttributes();
        doc.putProperty(InputAttributes.class, attrs);
        
        // Insert initial text into document
        String text = "ab";
        doc.insertString(0, text, null);
        // Put the language now into the document so that lexing starts from scratch
        doc.putProperty(Language.class, StateTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();

        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, StateTokenId.A, "a", 0);
        LanguagePath lp = LanguagePath.get(StateTokenId.language());
        assertFalse(Boolean.TRUE.equals(attrs.getValue(lp, "lexerRelease")));
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, StateTokenId.BMULTI, "b", 1);
        assertFalse(ts.moveNext());
        assertTrue(Boolean.TRUE.equals(attrs.getValue(lp, "lexerRelease")));
        attrs.setValue(lp, "lexerRelease", Boolean.FALSE, false);

        // Do modification and check lexer release after it
        doc.insertString(1, "b", null);
        assertTrue(Boolean.TRUE.equals(attrs.getValue(lp, "lexerRelease")));
    }

}
