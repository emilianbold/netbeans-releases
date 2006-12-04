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

package org.netbeans.lib.lexer.test.state;

import javax.swing.text.Document;
import junit.framework.TestCase;
import org.netbeans.api.lexer.InputAttributes;
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
public class StateLexerIncTest extends TestCase {
    
    public StateLexerIncTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void test() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        InputAttributes attrs = new InputAttributes();
        doc.putProperty(InputAttributes.class, attrs);
        doc.putProperty(Language.class, StateTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        assertFalse(ts.moveNext());
        
        // Insert text into document
        String text = "abc";
        doc.insertString(0, text, null);

        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, StateTokenId.A, "a", 0);
        assertEquals(LexerTestUtilities.lookahead(ts), 0);
        assertEquals(LexerTestUtilities.state(ts), StateLexer.AFTER_A);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, StateTokenId.BMULTI, "b", 1);
        assertEquals(LexerTestUtilities.state(ts), StateLexer.AFTER_B);
        assertEquals(LexerTestUtilities.lookahead(ts), 1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, StateTokenId.ERROR, "c", 2);
        assertEquals(LexerTestUtilities.state(ts), null);
        assertFalse(ts.moveNext());
        
        LexerTestUtilities.incCheck(doc, false);
        
        // Should relex "b" so restart state should be AFTER_A
        attrs.setValue(StateTokenId.language(), "restartState", StateLexer.AFTER_A, true);
        doc.insertString(2, "b", null);
        
    }

}
