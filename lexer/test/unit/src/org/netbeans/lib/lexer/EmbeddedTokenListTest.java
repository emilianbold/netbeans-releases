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
package org.netbeans.lib.lexer;

import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import junit.framework.TestCase;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.simple.SimpleJavadocTokenId;
import org.netbeans.lib.lexer.test.simple.SimpleTokenId;

/**
 *
 * @author Jan Lahoda
 */
public class EmbeddedTokenListTest extends TestCase {
    
    public EmbeddedTokenListTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    public void testUpdateStartOffset() throws Exception {
        Document d = new PlainDocument();
        
        d.putProperty(Language.class, SimpleTokenId.language());
        
        d.insertString(0, "ident ident /** @see X */", null);
        
        TokenHierarchy<?> h = TokenHierarchy.get(d);
        TokenSequence<? extends TokenId> ts = h.tokenSequence();
        
        LexerTestUtilities.assertNextTokenEquals(ts, SimpleTokenId.IDENTIFIER, "ident");
        assertEquals(0, ts.offset());
        
        LexerTestUtilities.assertNextTokenEquals(ts, SimpleTokenId.WHITESPACE, " ");
        assertEquals(5, ts.offset());
        
        LexerTestUtilities.assertNextTokenEquals(ts, SimpleTokenId.IDENTIFIER, "ident");
        assertEquals(6, ts.offset());
        
        LexerTestUtilities.assertNextTokenEquals(ts, SimpleTokenId.WHITESPACE, " ");
        assertEquals(11, ts.offset());
        
        LexerTestUtilities.assertNextTokenEquals(ts, SimpleTokenId.JAVADOC_COMMENT, "/** @see X */");
        assertEquals(12, ts.offset());
        
        TokenSequence<? extends TokenId> inner = ts.embedded();
        
        assertNotNull(inner);
        
        LexerTestUtilities.assertNextTokenEquals(inner, SimpleJavadocTokenId.OTHER_TEXT, " ");
        assertEquals(15, inner.offset());
        
        LexerTestUtilities.assertNextTokenEquals(inner, SimpleJavadocTokenId.TAG, "@see");
        assertEquals(16, inner.offset());
        
        LexerTestUtilities.assertNextTokenEquals(inner, SimpleJavadocTokenId.OTHER_TEXT, " ");
        assertEquals(20, inner.offset());
        
        LexerTestUtilities.assertNextTokenEquals(inner, SimpleJavadocTokenId.IDENT, "X");
        assertEquals(21, inner.offset());
        
        LexerTestUtilities.assertNextTokenEquals(inner, SimpleJavadocTokenId.OTHER_TEXT, " ");
        assertEquals(22, inner.offset());
    }

    public void testSnapshots() throws Exception {
        Document d = new PlainDocument();
        
        d.putProperty(Language.class, SimpleTokenId.language());
        
        d.insertString(0, "ident ident /** @see X */", null);
        
        TokenHierarchy<?> h = TokenHierarchy.get(d);
        TokenSequence<? extends TokenId> ts = h.tokenSequence();
        
        LexerTestUtilities.assertNextTokenEquals(ts, SimpleTokenId.IDENTIFIER, "ident");
        assertEquals(0, ts.offset());
        
        LexerTestUtilities.assertNextTokenEquals(ts, SimpleTokenId.WHITESPACE, " ");
        assertEquals(5, ts.offset());
        
        LexerTestUtilities.assertNextTokenEquals(ts, SimpleTokenId.IDENTIFIER, "ident");
        assertEquals(6, ts.offset());
        
        LexerTestUtilities.assertNextTokenEquals(ts, SimpleTokenId.WHITESPACE, " ");
        assertEquals(11, ts.offset());
        
        LexerTestUtilities.assertNextTokenEquals(ts, SimpleTokenId.JAVADOC_COMMENT, "/** @see X */");
        assertEquals(12, ts.offset());
        
        TokenSequence<? extends TokenId> inner = ts.embedded();
        
        assertNotNull(inner);
        
        LexerTestUtilities.assertNextTokenEquals(inner, SimpleJavadocTokenId.OTHER_TEXT, " ");
        assertEquals(15, inner.offset());
        
        LexerTestUtilities.assertNextTokenEquals(inner, SimpleJavadocTokenId.TAG, "@see");
        assertEquals(16, inner.offset());
        
        LexerTestUtilities.assertNextTokenEquals(inner, SimpleJavadocTokenId.OTHER_TEXT, " ");
        assertEquals(20, inner.offset());
        
        LexerTestUtilities.assertNextTokenEquals(inner, SimpleJavadocTokenId.IDENT, "X");
        assertEquals(21, inner.offset());
        
        LexerTestUtilities.assertNextTokenEquals(inner, SimpleJavadocTokenId.OTHER_TEXT, " ");
        assertEquals(22, inner.offset());
        
        
        h = TokenHierarchy.get(d).createSnapshot();
        ts = h.tokenSequence();
        
        LexerTestUtilities.assertNextTokenEquals(ts, SimpleTokenId.IDENTIFIER, "ident");
        assertEquals(0, ts.offset());
        
        LexerTestUtilities.assertNextTokenEquals(ts, SimpleTokenId.WHITESPACE, " ");
        assertEquals(5, ts.offset());
        
        LexerTestUtilities.assertNextTokenEquals(ts, SimpleTokenId.IDENTIFIER, "ident");
        assertEquals(6, ts.offset());
        
        LexerTestUtilities.assertNextTokenEquals(ts, SimpleTokenId.WHITESPACE, " ");
        assertEquals(11, ts.offset());
        
        LexerTestUtilities.assertNextTokenEquals(ts, SimpleTokenId.JAVADOC_COMMENT, "/** @see X */");
        assertEquals(12, ts.offset());
        
        inner = ts.embedded();
        
        assertNotNull(inner);
        
        LexerTestUtilities.assertNextTokenEquals(inner, SimpleJavadocTokenId.OTHER_TEXT, " ");
        assertEquals(15, inner.offset());
        
        LexerTestUtilities.assertNextTokenEquals(inner, SimpleJavadocTokenId.TAG, "@see");
        assertEquals(16, inner.offset());
        
        LexerTestUtilities.assertNextTokenEquals(inner, SimpleJavadocTokenId.OTHER_TEXT, " ");
        assertEquals(20, inner.offset());
        
        LexerTestUtilities.assertNextTokenEquals(inner, SimpleJavadocTokenId.IDENT, "X");
        assertEquals(21, inner.offset());
        
        LexerTestUtilities.assertNextTokenEquals(inner, SimpleJavadocTokenId.OTHER_TEXT, " ");
        assertEquals(22, inner.offset());
    }
}
