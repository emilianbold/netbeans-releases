/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.lib.lexer.test.inc;

import java.util.ConcurrentModificationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.junit.NbTestCase;
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
        
        // Should write-lock the document
        // doc.writeLock();
        try {
            MutableTextInput input = (MutableTextInput)doc.getProperty(MutableTextInput.class);
            assertNotNull(input);
            input.tokenHierarchyControl().rebuild();
        } finally {
            // doc.writeUnlock();
        }
        
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
