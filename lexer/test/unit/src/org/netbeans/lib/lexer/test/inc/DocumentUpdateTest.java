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

import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;
import org.netbeans.lib.lexer.test.simple.SimpleStringTokenId;
import org.netbeans.lib.lexer.test.simple.SimpleTokenId;

/**
 *
 * @author Jan Lahoda
 */
public class DocumentUpdateTest extends NbTestCase {
    
    /** Creates a new instance of DocumentUpdateTest */
    public DocumentUpdateTest(String name) {
        super(name);
    }
    
    public void testUpdate1() throws Exception {
        Document d = new ModificationTextDocument();
        
        d.putProperty(Language.class, SimpleTokenId.language());
        
        d.insertString(0, "\"\\t\\b\\t test\"", null);
        
        TokenHierarchy<?> h = TokenHierarchy.get(d);
        
        h.tokenSequence().tokenCount();
        
        TokenSequence<? extends TokenId> s = h.tokenSequence();
        
        assertTrue(s.moveNext());
        
        s.embedded();
        
        d.insertString(5, "t", null);
    }
    
    public void testUpdate2() throws Exception {
        Document d = new ModificationTextDocument();
        
        d.putProperty(Language.class, SimpleTokenId.language());
        
        d.insertString(0, "\"\\t\\b\\b\\t sfdsffffffffff\"", null);
        
        TokenHierarchy<?> h = TokenHierarchy.get(d);
        
        h.tokenSequence().tokenCount();
        
        TokenSequence<? extends TokenId> s = h.tokenSequence();
        
        assertTrue(s.moveNext());
        
        s.embedded();
        
        d.insertString(10, "t", null);
    }
    
    public void testUpdate3() throws Exception {
        Document d = new ModificationTextDocument();
        
        d.putProperty(Language.class, SimpleTokenId.language());
        
        d.insertString(0, "\"t\"", null);
        
        TokenHierarchy<?> h = TokenHierarchy.get(d);
        
        h.tokenSequence().tokenCount();
        
        TokenSequence<? extends TokenId> s = h.tokenSequence();
        
        assertTrue(s.moveNext());
        
        assertNotNull(s.embedded());
        
        d.insertString(1, "\\", null);
        
        System.err.println("d=" + d.getText(0, d.getLength()));
        
        LexerTestUtilities.assertNextTokenEquals(h.tokenSequence(), SimpleTokenId.STRING_LITERAL, "\"\\t\"");
        
        s = h.tokenSequence();
        
        assertTrue(s.moveNext());
        
        TokenSequence<? extends TokenId> e = s.embedded();
        
        assertNotNull(e);
        
        assertTrue(e.moveNext());
        
        assertEquals(e.token().id(), SimpleStringTokenId.TAB);
    }
    
}
