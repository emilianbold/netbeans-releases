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

package org.netbeans.lib.lexer.inc;

import org.netbeans.lib.lexer.lang.TestTokenId;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.lib.lexer.test.ModificationTextDocument;
import org.netbeans.lib.lexer.test.simple.*;
import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.lang.TestPlainTokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class EmbeddingUpdateTest extends NbTestCase {
    
    public EmbeddingUpdateTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testEmbeddingUpdate() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        doc.putProperty(Language.class,TestTokenId.language());
        doc.insertString(0, "a/*abc def*/", null);
        LexerTestUtilities.initLastTokenHierarchyEventListening(doc);
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "a", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.BLOCK_COMMENT, "/*abc def*/", 1);
        TokenSequence<? extends TokenId> ets = ts.embedded();
        assertNotNull(ets);
        assertFalse(ts.moveNext());
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets,TestPlainTokenId.WORD, "abc", 3);
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets,TestPlainTokenId.WHITESPACE, " ", 6);
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets,TestPlainTokenId.WORD, "def", 7);
        assertFalse(ets.moveNext());

        // Make "axbc" inside the comment
        doc.insertString(4, "x", null);
        
        TokenHierarchyEvent evt = LexerTestUtilities.getLastTokenHierarchyEvent(doc);
        assertNotNull(evt);
        TokenChange<? extends TokenId> tc = evt.tokenChange();
        assertNotNull(tc);
        assertEquals(1, tc.index());
        assertEquals(1, tc.offset());
        assertEquals(1, tc.addedTokenCount());
        assertEquals(1, tc.removedTokenCount());
        assertEquals(TestTokenId.language(), tc.language());
        assertEquals(1, tc.embeddedChangeCount());
        TokenChange<? extends TokenId> etc = tc.embeddedChange(0);
        assertEquals(0, etc.index());
        assertEquals(3, etc.offset());
        assertEquals(1, etc.addedTokenCount()); // 0 to allow for lazy lexing where this would be unknowns
        assertEquals(1, etc.removedTokenCount());
        assertEquals(TestPlainTokenId.language(), etc.language());
        assertEquals(0, etc.embeddedChangeCount());
        
        
        doc.remove(3, 8); // there will be empty /**/ so test empty embedded sequence
        doc.insertString(3, "x", null); // there will be empty /**/
    }        
        
}
