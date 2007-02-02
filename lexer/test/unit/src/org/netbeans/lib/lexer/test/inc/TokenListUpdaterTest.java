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

import java.util.ConcurrentModificationException;
import javax.swing.text.Document;
import junit.framework.TestCase;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;
import org.netbeans.lib.lexer.test.simple.SimpleTokenId;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class TokenListUpdaterTest extends TestCase {
    
    public TokenListUpdaterTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testInsertUnfinishedLexing() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        String text = "abc+uv-xy";
        doc.insertString(0, text, null);

        doc.putProperty(Language.class, SimpleTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        assertNotNull("Null token hierarchy for document", hi);
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "abc", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.PLUS, "+", 3);
        
        // Modify before last token
        doc.insertString(3, "x", null);
        try {
            ts.moveNext();
            fail("Should not get there");
        } catch (ConcurrentModificationException e) {
            // Expected
        }

        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "abcx", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.PLUS, "+", 4);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "uv", 5);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.MINUS, "-", 7);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "xy", 8);
        assertFalse(ts.moveNext());
    }

    public void testRemoveUnfinishedLexingZeroLookaheadToken() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        String text = "a+b";
        doc.insertString(0, text, null);

        doc.putProperty(Language.class, SimpleTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "a", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.PLUS, "+", 1);
        
        // Remove "+"
        doc.remove(1, 1);
        try {
            ts.moveNext();
            fail("Should not get there");
        } catch (ConcurrentModificationException e) {
            // Expected
        }

        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "ab", 0);
        assertFalse(ts.moveNext());
    }

    public void testRemoveUnfinishedLexingRightAfterLastToken() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        String text = "a+b";
        doc.insertString(0, text, null);

        doc.putProperty(Language.class, SimpleTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "a", 0);
        
        // Remove "+"
        doc.remove(1, 1);
        try {
            ts.moveNext();
            fail("Should not get there");
        } catch (ConcurrentModificationException e) {
            // Expected
        }

        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "ab", 0);
        assertFalse(ts.moveNext());
    }

    public void testRemoveUnfinishedLexingAfterLastToken() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        String text = "a+b+";
        doc.insertString(0, text, null);

        doc.putProperty(Language.class, SimpleTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "a", 0);
        
        // Remove "b"
        doc.remove(2, 1);
        try {
            ts.moveNext();
            fail("Should not get there");
        } catch (ConcurrentModificationException e) {
            // Expected
        }

        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "a", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.PLUS, "+", 1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.PLUS, "+", 2);
        assertFalse(ts.moveNext());
    }

    public void testReadAllInsertAtEnd() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        String text = "a+";
        doc.insertString(0, text, null);

        doc.putProperty(Language.class, SimpleTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "a", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.PLUS, "+", 1);
        
        // Insert "-"
        doc.insertString(2, "-", null);
        try {
            ts.moveNext();
            fail("Should not get there");
        } catch (ConcurrentModificationException e) {
            // Expected
        }

        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "a", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.PLUS, "+", 1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.MINUS, "-", 2);
        assertFalse(ts.moveNext());
    }

    public void testReadOneInsertAtEnd() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        String text = "a+";
        doc.insertString(0, text, null);

        doc.putProperty(Language.class, SimpleTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "a", 0);
        
        // Insert "-"
        doc.insertString(2, "-", null);
        try {
            ts.moveNext();
            fail("Should not get there");
        } catch (ConcurrentModificationException e) {
            // Expected
        }

        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "a", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.PLUS, "+", 1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.MINUS, "-", 2);
        assertFalse(ts.moveNext());
    }

    public void testReadNoneInsertAtEnd() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        String text = "a+";
        doc.insertString(0, text, null);

        doc.putProperty(Language.class, SimpleTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        
        // Insert "-"
        doc.insertString(2, "-", null);

        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "a", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.PLUS, "+", 1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.MINUS, "-", 2);
        assertFalse(ts.moveNext());
    }

}
