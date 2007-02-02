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
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;
import org.netbeans.lib.lexer.test.simple.SimpleTokenId;
import org.netbeans.spi.lexer.MutableTextInput;

/**
 *
 * @author Miloslav Metelka
 */
public class TokenHierarchyRebuildTest extends NbTestCase {
    
    /** Creates a new instance of DocumentUpdateTest */
    public TokenHierarchyRebuildTest(String name) {
        super(name);
    }
    
    public void testRebuild() throws Exception {
        Document doc = new ModificationTextDocument();
        doc.putProperty(Language.class, SimpleTokenId.language());
        doc.insertString(0, "abc def", null);
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<?> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        Token<?> t = ts.token();
        assertNotNull(t);
        String tText = t.text().toString();
        LexerTestUtilities.initLastTokenHierarchyEventListening(doc);
        
        // Should write-lock the document
        // doc.writeLock();
        try {
            MutableTextInput input = (MutableTextInput)doc.getProperty(MutableTextInput.class);
            assertNotNull(input);
            input.tokenHierarchyControl().rebuild();
            LexerTestUtilities.initLastDocumentEventListening(doc);
        } finally {
            // doc.writeUnlock();
        }
        
        // Check the fired token hierarchy event
        int docLen = doc.getLength();
        TokenHierarchyEvent evt = LexerTestUtilities.getLastTokenHierarchyEvent(doc);
        assertEquals(0, evt.affectedStartOffset());
        assertEquals(docLen, evt.affectedEndOffset());
        
        try { // ts should no longer work
            ts.moveNext();
            fail("ConcurrentModificationException not thrown.");
        } catch (ConcurrentModificationException e) {
            // Expected
        }
        
        TokenHierarchy<?> hi2 = TokenHierarchy.get(doc);
        assertSame(hi, hi2);
        TokenSequence<?> ts2 = hi2.tokenSequence();
        assertTrue(ts2.moveNext());
        Token<?> t2  = ts2.token();
        
        assertNotSame(t, t2);
        assertSame(t.id(), t2.id());
        assertTrue(TokenUtilities.equals(tText, t2.text()));
        
    }
    
}
