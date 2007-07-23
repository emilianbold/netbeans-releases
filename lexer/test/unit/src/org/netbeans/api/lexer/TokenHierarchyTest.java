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

package org.netbeans.api.lexer;

import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.lang.TestLineTokenId;
import org.netbeans.lib.lexer.lang.TestPlainTokenId;
import org.netbeans.lib.lexer.lang.TestTokenId;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;

/**
 * Test methods of token sequence.
 *
 * @author mmetelka
 */
public class TokenHierarchyTest extends NbTestCase {
    
    public TokenHierarchyTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }
    
    public void testLanguagePaths() {
        String text = "abc\ndef";
        TokenHierarchy<?> hi = TokenHierarchy.create(text,TestLineTokenId.language());
        Set<LanguagePath> lps = hi.languagePaths();
        assertNotNull(lps);
        assertEquals(1, lps.size());
        
        TokenSequence<?> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestLineTokenId.LINE, "abc\n", 0);
        ts.createEmbedding(TestPlainTokenId.language(), 0, 0);
        lps = hi.languagePaths();
        assertEquals(2, lps.size());
    }
    
    public void testSameEmbeddedToken() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        String text = "/**abc*/";
        doc.insertString(0, text, null);
        
        doc.putProperty(Language.class,TestTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        
        TokenSequence<?> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        TokenSequence<?> ets = ts.embedded();
        assertTrue(ets.moveNext());
        Token<?> et = ets.token();
        assertNotNull(et);
        
        TokenHierarchy<?> hi2 = TokenHierarchy.get(doc);
        TokenSequence<?> ts2 = hi2.tokenSequence();
        assertTrue(ts2.moveNext());
        TokenSequence<?> ets2 = ts2.embedded();
        assertTrue(ets2.moveNext());
        Token<?> et2 = ets2.token();
        assertNotNull(et2);
        
        assertSame(et, et2);
    }

}
