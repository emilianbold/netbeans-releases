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
import org.netbeans.lib.lexer.test.simple.SimpleJavadocTokenId;
import org.netbeans.lib.lexer.test.simple.SimpleTokenId;
import org.netbeans.lib.lexer.test.simple.TestHTMLTagTokenId;

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
        TokenHierarchy<?> tokenHierarchy = TokenHierarchy.create(getText1(), SimpleTokenId.language());
        testHierarchy(tokenHierarchy);
    }

    public void testInc() throws Exception {
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, getText1(), null);
        doc.putProperty(Language.class, SimpleTokenId.language());
        testHierarchy(TokenHierarchy.get(doc));
    }
    
    private String getText1() {
        return "ab/**<t> x*/c/**u<t2>v*/";
    }

    private void testHierarchy(TokenHierarchy<?> tokenHierarchy) throws Exception {
        LanguagePath lp = LanguagePath.get(SimpleTokenId.language());
        List<TokenSequence<? extends TokenId>> tsList = tokenHierarchy.tokenSequenceList(lp, 0, Integer.MAX_VALUE);
        assertEquals(1, tsList.size());
        TokenSequence<?> ts = tsList.get(0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "ab", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.JAVADOC_COMMENT, "/**<t> x*/", 2);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "c", 12);

        // Collect both javadocs
        lp = lp.embedded(SimpleJavadocTokenId.language());
        tsList = tokenHierarchy.tokenSequenceList(lp, 0, Integer.MAX_VALUE);
        assertEquals(2, tsList.size());
        ts = tsList.get(0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleJavadocTokenId.HTML_TAG, "<t>", 5);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleJavadocTokenId.OTHER_TEXT, " ", 8);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleJavadocTokenId.IDENT, "x", 9);
        
        ts = tsList.get(1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleJavadocTokenId.IDENT, "u", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleJavadocTokenId.HTML_TAG, "<t2>", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleJavadocTokenId.IDENT, "v", -1);
        
        // Collect embedded html tags
        lp = lp.embedded(TestHTMLTagTokenId.language());
        tsList = tokenHierarchy.tokenSequenceList(lp, 0, Integer.MAX_VALUE);
        assertEquals(2, tsList.size());
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
        
    }

}
