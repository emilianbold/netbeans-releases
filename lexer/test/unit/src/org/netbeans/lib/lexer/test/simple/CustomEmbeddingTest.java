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

import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.simple.SimplePlainTokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class CustomEmbeddingTest extends NbTestCase {
    
    public CustomEmbeddingTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testCreateEmbedding() {
        String text = "abc/*def ghi */// line comment";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, SimpleTokenId.language());
        THListener listener = new THListener();
        hi.addTokenHierarchyListener(listener);
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "abc", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.BLOCK_COMMENT, "/*def ghi */", 3);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.LINE_COMMENT, "// line comment", 15);
        assertTrue(ts.createEmbedding(SimpleTokenId.language(), 3, 0));
        
        // Check the fired event
        TokenHierarchyEvent evt = listener.fetchLastEvent();
        assertNotNull(evt);
        TokenChange<? extends TokenId> tc = evt.tokenChange();
        assertNotNull(tc);
        assertEquals(2, tc.index());
        assertEquals(15, tc.offset());
        assertEquals(0, tc.addedTokenCount());
        assertEquals(0, tc.removedTokenCount());
        assertEquals(SimpleTokenId.language(), tc.language());
        assertEquals(1, tc.embeddedChangeCount());
        TokenChange<? extends TokenId> etc = tc.embeddedChange(0);
        assertEquals(0, etc.index());
        assertEquals(18, etc.offset());
        assertEquals(0, etc.addedTokenCount()); // 0 to allow for lazy lexing where this would be unknowns
        assertEquals(0, etc.removedTokenCount());
        assertEquals(SimpleTokenId.language(), etc.language());
        assertEquals(0, etc.embeddedChangeCount());
        
        // Test the contents of the embedded sequence
        TokenSequence<? extends TokenId> ets = ts.embedded();
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets, SimpleTokenId.IDENTIFIER, "line", 18);
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets, SimpleTokenId.WHITESPACE, " ", 22);
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets, SimpleTokenId.IDENTIFIER, "comment", 23);
        assertFalse(ets.moveNext());
        
        // Move main TS back and try extra embedding on comment
        assertTrue(ts.movePrevious());
        assertTrue(ts.createEmbedding(SimpleTokenId.language(), 2, 2));
        ets = ts.embedded(); // Should be the explicit one
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets, SimpleTokenId.IDENTIFIER, "def", 5);
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets, SimpleTokenId.WHITESPACE, " ", 8);
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets, SimpleTokenId.IDENTIFIER, "ghi", 9);
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets, SimpleTokenId.WHITESPACE, " ", 12);
        assertFalse(ets.moveNext());
        
        // Get the default embedding - should be available as well
        ets = ts.embedded(SimplePlainTokenId.language()); // Should be the explicit one
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets, SimplePlainTokenId.WORD, "def", 5);
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets, SimplePlainTokenId.WHITESPACE, " ", 8);
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets, SimplePlainTokenId.WORD, "ghi", 9);
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets, SimplePlainTokenId.WHITESPACE, " ", 12);
        assertFalse(ets.moveNext());
    }
    
    public void testEmbeddingCaching() throws Exception {
        LanguageEmbedding<? extends TokenId> e = LanguageEmbedding.create(SimpleTokenId.language(), 2, 1);
        assertSame(SimpleTokenId.language(), e.language());
        assertSame(2, e.startSkipLength());
        assertSame(1, e.endSkipLength());
        LanguageEmbedding<? extends TokenId> e2 = LanguageEmbedding.create(SimpleTokenId.language(), 2, 1);
        assertSame(e, e2);
    }
    
    private static final class THListener implements TokenHierarchyListener {
        
        private TokenHierarchyEvent lastEvent;
    
        public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
            this.lastEvent = evt;
        }
        
        public TokenHierarchyEvent fetchLastEvent() {
            TokenHierarchyEvent evt = lastEvent;
            lastEvent = null;
            return evt;
        }

    }

}
