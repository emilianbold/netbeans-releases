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

package org.netbeans.lib.lexer.test.inc;

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
public class TokenHierarchySnapshotTest extends TestCase {
    
    public TokenHierarchySnapshotTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testSnapshot() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        doc.putProperty(Language.class, SimpleTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        assertNotNull("Null token hierarchy for document", hi);
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        assertFalse(ts.moveNext());
        
        // Insert text into document
        String text = "abc+def-xyz";
        doc.insertString(0, text, null);

        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "abc", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.PLUS, "+", 3);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "def", 4);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.MINUS, "-", 7);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "xyz", 8);
        assertFalse(ts.moveNext());
        LexerTestUtilities.incCheck(doc, false);

        // Create snapshot1 and check hierarchy
        String hi1text = doc.getText(0, doc.getLength());
        TokenHierarchy<?> hi1 = TokenHierarchy.create(hi1text, SimpleTokenId.language());
        TokenHierarchy<?> snapshot1 = hi.createSnapshot();
        assertEquals(snapshot1.snapshotOf(), hi);
        assertFalse(snapshot1.isSnapshotReleased());

        // Check that all the non-fly tokens are mutable
        ts = snapshot1.tokenSequence();
        assertEquals(0, ts.moveIndex(0));
        assertTrue(ts.moveNext());
        
        LexerTestUtilities.assertTokenSequencesEqual(hi1.tokenSequence(), hi1,
                snapshot1.tokenSequence(), snapshot1, false);

        doc.insertString(4, "+", null);

        // Check that the snapshot token sequence can be further operated.
        assertEquals(0, ts.moveIndex(0));
        assertTrue(ts.moveNext());
        assertNotNull(ts.token());

        // Check that the tokens except '+' are live
        ts = snapshot1.tokenSequence();

        LexerTestUtilities.assertTokenSequencesEqual(hi1.tokenSequence(), hi1,
                snapshot1.tokenSequence(), snapshot1, true);

        // Create snapshot2 and check hierarchy
        String hi2text = doc.getText(0, doc.getLength());
        TokenHierarchy<?> hi2 = TokenHierarchy.create(hi2text, SimpleTokenId.language());
        TokenHierarchy<?> snapshot2 = hi.createSnapshot();
        assertEquals(snapshot2.snapshotOf(), hi);

        // Check that all the non-fly tokens are mutable
        ts = snapshot2.tokenSequence();
        assertEquals(0, ts.moveIndex(0));
        assertTrue(ts.moveNext());

        LexerTestUtilities.assertTokenSequencesEqual(hi2.tokenSequence(), hi2,
                snapshot2.tokenSequence(), snapshot2, false);

        doc.remove(8, 1);

        LexerTestUtilities.assertTokenSequencesEqual(hi1.tokenSequence(), hi1,
                snapshot1.tokenSequence(), snapshot1, false);
        LexerTestUtilities.assertTokenSequencesEqual(hi2.tokenSequence(), hi2,
                snapshot2.tokenSequence(), snapshot2, false);
    }
    
}
