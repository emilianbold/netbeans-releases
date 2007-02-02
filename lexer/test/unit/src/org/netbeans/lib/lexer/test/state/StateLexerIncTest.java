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
