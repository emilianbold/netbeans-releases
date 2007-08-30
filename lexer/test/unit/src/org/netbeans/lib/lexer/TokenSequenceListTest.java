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

import java.util.List;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;
import org.netbeans.lib.lexer.lang.TestJavadocTokenId;
import org.netbeans.lib.lexer.lang.TestTokenId;
import org.netbeans.lib.lexer.lang.TestHTMLTagTokenId;

/**
 * Test collecting and maintaining of token sequence lists.
 *
 * @author Miloslav Metelka
 */
public class TokenSequenceListTest extends NbTestCase {
    
    public TokenSequenceListTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    public void testBatch() throws Exception {
        TokenHierarchy<?> tokenHierarchy = TokenHierarchy.create(getText1(),TestTokenId.language());
        testHierarchy(tokenHierarchy);
    }

    public void testInc() throws Exception {
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, getText1(), null);
        doc.putProperty(Language.class,TestTokenId.language());
        testHierarchy(TokenHierarchy.get(doc));
    }
    
    public void testBoundaries() throws Exception {
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, getText1(), null);
        doc.putProperty(Language.class,TestTokenId.language());
        
        LanguagePath lp = LanguagePath.get(TestTokenId.language()).
            embedded(TestJavadocTokenId.language()).
            embedded(TestHTMLTagTokenId.language());
        
        TokenHierarchy<?> tokenHierarchy = TokenHierarchy.get(doc);
        List<TokenSequence<? extends TokenId>> tsList = tokenHierarchy.tokenSequenceList(lp, 35, 48);

        assertEquals(3, tsList.size());
        TokenSequence<?> ts = tsList.get(0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, TestHTMLTagTokenId.TEXT, "tq", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, TestHTMLTagTokenId.GT, ">", -1);

        ts = tsList.get(1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, TestHTMLTagTokenId.LT, "<", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, TestHTMLTagTokenId.TEXT, "/tq", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, TestHTMLTagTokenId.GT, ">", -1);

        ts = tsList.get(2);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, TestHTMLTagTokenId.LT, "<", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, TestHTMLTagTokenId.TEXT, "code", -1);
    }
    
    private String getText1() {
        return "ab/**<t> x*/c/**u<t2>v*/jkl/**hey<tq>aaa</tq><code>sample</code>*/";
    }

    private void testHierarchy(TokenHierarchy<?> tokenHierarchy) throws Exception {
        LanguagePath lp = LanguagePath.get(TestTokenId.language());
        List<TokenSequence<?>> tsList = tokenHierarchy.tokenSequenceList(lp, 0, Integer.MAX_VALUE);
        assertEquals(1, tsList.size());
        TokenSequence<?> ts = tsList.get(0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "ab", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.JAVADOC_COMMENT, "/**<t> x*/", 2);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "c", 12);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.JAVADOC_COMMENT, "/**u<t2>v*/", 13);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "jkl", 24);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.JAVADOC_COMMENT, "/**hey<tq>aaa</tq><code>sample</code>*/", 27);

        // Collect all javadocs
        lp = lp.embedded(TestJavadocTokenId.language());
        tsList = tokenHierarchy.tokenSequenceList(lp, 0, Integer.MAX_VALUE);
        assertEquals(3, tsList.size());
        ts = tsList.get(0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJavadocTokenId.HTML_TAG, "<t>", 5);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJavadocTokenId.OTHER_TEXT, " ", 8);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJavadocTokenId.IDENT, "x", 9);
        
        ts = tsList.get(1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJavadocTokenId.IDENT, "u", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJavadocTokenId.HTML_TAG, "<t2>", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJavadocTokenId.IDENT, "v", -1);

        ts = tsList.get(2);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJavadocTokenId.IDENT, "hey", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJavadocTokenId.HTML_TAG, "<tq>", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJavadocTokenId.IDENT, "aaa", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJavadocTokenId.HTML_TAG, "</tq>", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJavadocTokenId.HTML_TAG, "<code>", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJavadocTokenId.IDENT, "sample", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJavadocTokenId.HTML_TAG, "</code>", -1);
        
        // Collect embedded html tags
        lp = lp.embedded(TestHTMLTagTokenId.language());
        tsList = tokenHierarchy.tokenSequenceList(lp, 0, Integer.MAX_VALUE);
        assertEquals(6, tsList.size());
        ts = tsList.get(0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, TestHTMLTagTokenId.LT, "<", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, TestHTMLTagTokenId.TEXT, "t", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, TestHTMLTagTokenId.GT, ">", -1);
        
        ts = tsList.get(1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, TestHTMLTagTokenId.LT, "<", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, TestHTMLTagTokenId.TEXT, "t2", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, TestHTMLTagTokenId.GT, ">", -1);
        
        ts = tsList.get(2);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, TestHTMLTagTokenId.LT, "<", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, TestHTMLTagTokenId.TEXT, "tq", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, TestHTMLTagTokenId.GT, ">", -1);
        
        ts = tsList.get(3);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, TestHTMLTagTokenId.LT, "<", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, TestHTMLTagTokenId.TEXT, "/tq", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, TestHTMLTagTokenId.GT, ">", -1);
        
        ts = tsList.get(4);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, TestHTMLTagTokenId.LT, "<", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, TestHTMLTagTokenId.TEXT, "code", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, TestHTMLTagTokenId.GT, ">", -1);
        
        ts = tsList.get(5);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, TestHTMLTagTokenId.LT, "<", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, TestHTMLTagTokenId.TEXT, "/code", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, TestHTMLTagTokenId.GT, ">", -1);
        
    }

}
