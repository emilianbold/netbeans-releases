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

package org.netbeans.lib.lexer.inc;

import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;
import org.netbeans.lib.lexer.test.simple.SimplePlainTokenId;
import org.netbeans.lib.lexer.test.simple.SimpleTokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class TokenHierarchyEventTest extends NbTestCase {
    
    public TokenHierarchyEventTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testCreateEmbedding() throws Exception {
        Document doc = new ModificationTextDocument();
        String text = "abc def ghi";
        doc.insertString(0, text, null);
        // Assign a language to the document
        doc.putProperty(Language.class, SimpleTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        THListener listener = new THListener();
        hi.addTokenHierarchyListener(listener);
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();

        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "abc", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.WHITESPACE, " ", 3);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "def", 4);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.WHITESPACE, " ", 7);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "ghi", 8);
        assertFalse(ts.moveNext());
        
        // Do insert
        doc.insertString(5, "x", null);
        
        // Check the fired event
        TokenHierarchyEvent evt = listener.fetchLastEvent();
        assertNotNull(evt);
        TokenChange<? extends TokenId> tc = evt.tokenChange();
        assertNotNull(tc);
        assertEquals(2, tc.index());
        assertEquals(4, tc.offset());
        assertEquals(1, tc.addedTokenCount());
        assertEquals(1, tc.removedTokenCount());
        assertEquals(SimpleTokenId.language(), tc.language());
        assertEquals(0, tc.embeddedChangeCount());
        
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
