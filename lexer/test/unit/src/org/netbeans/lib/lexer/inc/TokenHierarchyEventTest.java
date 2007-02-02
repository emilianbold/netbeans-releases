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
        LexerTestUtilities.initLastTokenHierarchyEventListening(doc);
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
        TokenHierarchyEvent evt = LexerTestUtilities.getLastTokenHierarchyEvent(doc);
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
    
}
