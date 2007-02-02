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

import java.util.ConcurrentModificationException;
import javax.swing.text.Document;
import junit.framework.TestCase;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class SimpleLexerIncTest extends TestCase {
    
    public SimpleLexerIncTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void test() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        doc.putProperty(Language.class, SimpleTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        assertNotNull("Null token hierarchy for document", hi);
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        assertFalse(ts.moveNext());
        
        // Insert text into document
        String commentText = "/* test comment  */";
        String text = "abc+uv-xy +-+" + commentText + "def";
        int commentTextStartOffset = 13;
        doc.insertString(0, text, null);

        // Last token sequence should throw exception - new must be obtained
        try {
            ts.moveNext();
            fail("TokenSequence.moveNext() did not throw exception as expected.");
        } catch (ConcurrentModificationException e) {
            // Expected exception
        }
        
        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "abc", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.PLUS, "+", 3);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "uv", 4);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.MINUS, "-", 6);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "xy", 7);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.WHITESPACE, " ", 9);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.PLUS_MINUS_PLUS, "+-+", 10);
        assertTrue(ts.moveNext());
        int offset = commentTextStartOffset;
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.BLOCK_COMMENT, commentText, offset);
        offset += commentText.length();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "def", offset);
        assertFalse(ts.moveNext());

        LexerTestUtilities.incCheck(doc, false);
        
        // Check TokenSequence.move()
        int relOffset = ts.move(50); // past the end of all tokens
        assertEquals(relOffset, 50 - (offset + 3));
        assertTrue(ts.movePrevious());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "def", offset);

        relOffset = ts.move(6); // right at begining of "-"
        assertEquals(relOffset, 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.MINUS, "-", 6);

        relOffset = ts.move(-5); // to first token "abc"
        assertEquals(relOffset, -5);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "abc", 0);

        relOffset = ts.move(5); // to first token "abc"
        assertEquals(relOffset, 1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "uv", 4);


        doc.insertString(2, "d", null); // should be "abdc"

        // Last token sequence should throw exception - new must be obtained
        try {
            ts.moveNext();
            fail("TokenSequence.moveNext() did not throw exception as expected.");
        } catch (ConcurrentModificationException e) {
            // Expected exception
        }
        
        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "abdc", 0);
        LexerTestUtilities.incCheck(doc, false);
        
        // Remove added 'd' to become "abc" again
        doc.remove(2, 1); // should be "abc" again

        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "abc", 0);
        LexerTestUtilities.incCheck(doc, false);

        
        // Now insert right at the end of first token - identifier with lookahead 1
        doc.insertString(3, "x", null); // should become "abcx"
        
        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "abcx", 0);
        LexerTestUtilities.incCheck(doc, false);

        doc.remove(3, 1); // return back to "abc"

        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "abc", 0);
        LexerTestUtilities.incCheck(doc, false);

        
        // Now insert right at the end of "+" token - operator with lookahead 1 (because of "+-+" operator)
        doc.insertString(4, "z", null); // should become "abc" "+" "zuv"
        
        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "abc", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.PLUS, "+", 3);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "zuv", 4);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.MINUS, "-", 7);
        LexerTestUtilities.incCheck(doc, false);

        doc.remove(4, 1); // return back to "abc" "+" "uv"

        LexerTestUtilities.incCheck(doc, false);

        // Now insert right after "-" - operator with lookahead 0
        doc.insertString(7, "z", null);
        
        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "abc", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.PLUS, "+", 3);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "uv", 4);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.MINUS, "-", 6);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "zxy", 7);
        LexerTestUtilities.incCheck(doc, false);

        doc.remove(7, 1); // return back to "abc" "+" "uv"

        LexerTestUtilities.incCheck(doc, false);

        // Now insert between "+-" and "+" in "+-+" - operator with lookahead 0
        doc.insertString(12, "z", null);
        LexerTestUtilities.incCheck(doc, false);
        doc.remove(12, 1);
        LexerTestUtilities.incCheck(doc, false);

        // Now insert "-" at the end of the document
        doc.insertString(doc.getLength(), "-", null);
        LexerTestUtilities.incCheck(doc, false);
        // Insert again "-" at the end of the document (now lookahead of preceding is zero)
        doc.insertString(doc.getLength(), "-", null);
        LexerTestUtilities.incCheck(doc, false);
        // Insert again "+-+" at the end of the document (now lookahead of preceding is zero)
        doc.insertString(doc.getLength(), "+-+", null);
        LexerTestUtilities.incCheck(doc, false);
        // Remove the ending "+" so that "+-+" becomes "+" "-"
        doc.remove(doc.getLength() - 1, 1);
        LexerTestUtilities.incCheck(doc, false);
        
        doc.insertString(5, "a+b-c", null); // multiple tokens
        LexerTestUtilities.incCheck(doc, false);
        doc.insertString(7, "-++", null); // complete PLUS_MINUS_PLUS
        LexerTestUtilities.incCheck(doc, false);
    }

}
