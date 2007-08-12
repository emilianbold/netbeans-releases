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

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import junit.framework.TestCase;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.lang.TestSaveTokensInLATokenId;
import org.netbeans.lib.lexer.lang.TestTokenId;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;

/**
 * Tests of token list behavior in certain special situations.
 *
 * @author mmetelka
 */
public class TokenListUpdaterExtraTest extends TestCase {
    
    public TokenListUpdaterExtraTest(String testName) {
        super(testName);
    }

    public void testSaveTokensWithinLookahead() throws Exception {
        Document doc = new ModificationTextDocument();
        String text = "aabc";
        doc.insertString(0, text, null);
        
        doc.putProperty(Language.class, TestSaveTokensInLATokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<?> ts = hi.tokenSequence();
        ts.moveEnd(); // Force creation of all tokens
        System.out.println("ts:\n" + ts);
        LexerTestUtilities.initLastTokenHierarchyEventListening(doc);
        doc.remove(1, 1);
        TokenHierarchyEvent evt = LexerTestUtilities.getLastTokenHierarchyEvent(doc);
        TokenChange<?> change = evt.tokenChange();
        assertEquals(1, change.addedTokenCount());
    }

    public void testEmbeddedModInStartSkipLength() throws Exception {
        Document doc = commentDoc();
        doc.insertString(1, "*", null);
    }
    
    public void testEmbeddedModInEndSkipLength()  throws Exception {
        Document doc = commentDoc();
        doc.insertString(7, "*", null);
    }
    
    private Document commentDoc() throws BadLocationException {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        String text = "/**abc*/";
        doc.insertString(0, text, null);
        
        doc.putProperty(Language.class,TestTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        
        TokenSequence<?> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        assertNotNull(ts.embedded());
        return doc;
    }

    public void testTokenCountWasCalledInUpdater() throws Exception {
        Document doc = new ModificationTextDocument();
        String text = "+/* */";
        doc.insertString(0, text, null);
        
        doc.putProperty(Language.class, TestTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<?> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS, "+", -1);
        doc.remove(1, 3); // Remove "/* "
        ts = hi.tokenSequence();
        ts.moveEnd();
        assertTrue(ts.movePrevious());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.DIV, "/", -1);
    }


}
