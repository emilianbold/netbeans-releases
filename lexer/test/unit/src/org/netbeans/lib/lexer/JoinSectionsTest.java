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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.lang.TestJoinSectionsTextTokenId;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.lang.TestJoinSectionsTopTokenId;
import org.netbeans.lib.lexer.test.ModificationTextDocument;

/**
 * Test embedded sections that should be lexed together.
 *
 * @author Miloslav Metelka
 */
public class JoinSectionsTest extends NbTestCase {
    
    public JoinSectionsTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    public void testJoinSections() throws Exception {
        // Turn on detailed checking
//        Logger.getLogger(TokenHierarchyOperation.class.getName()).setLevel(Level.FINEST);

        //             000000000011111111112222222222
        //             012345678901234567890123456789
        String text = "a{b<cd>e}f<gh>i{j<kl>m}n";
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class,TestJoinSectionsTopTokenId.language());
        
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<?> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTopTokenId.TEXT, "a{b", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTopTokenId.TAG, "<cd>", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTopTokenId.TEXT, "e}f", -1);
        
        // Get embedded tokens within TEXT tokens. There should be "a" then BRACES start "{b" then BRACES end "e}|" then "f"
        LanguagePath innerLP = LanguagePath.get(TestJoinSectionsTopTokenId.language()).
                embedded(TestJoinSectionsTextTokenId.language());
        List<TokenSequence<?>> tsList = hi.tokenSequenceList(innerLP, 0, Integer.MAX_VALUE);
        checkInitialTokens(tsList);
        
        
        // Use iterator for fetching token sequences
        int i = 0;
        for (TokenSequence<?> ts2 : tsList) {
            assertSame(ts2, tsList.get(i++));
        }

        LexerTestUtilities.assertConsistency(hi);
        

        // Do modifications
        // Remove second closing brace '}'
        doc.remove(8, 1);
        LexerTestUtilities.assertConsistency(hi);
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        // before:    "a{b<cd>e}f<gh>i{j<kl>m}n";
        // after:     "a{b<cd>ef<gh>i{j<kl>m}n";
        tsList = hi.tokenSequenceList(innerLP, 0, Integer.MAX_VALUE);
        assertEquals(4, tsList.size()); // 2 sections

        // 1.section
        ts = tsList.get(0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTextTokenId.TEXT, "a", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTextTokenId.BRACES, "{b", -1);
        Token<?> token = ts.token();
        assertEquals(PartType.START, token.partType());
        assertFalse(ts.moveNext());
        
        // 2.section
        ts = tsList.get(1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTextTokenId.BRACES, "ef", -1);
        token = ts.token();
        assertEquals(PartType.MIDDLE, token.partType());
        assertFalse(ts.moveNext());
        
        // 3.section
        ts = tsList.get(2);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTextTokenId.BRACES, "i{j", -1);
        token = ts.token();
        assertEquals(PartType.MIDDLE, token.partType());
        assertFalse(ts.moveNext());
        
        // 4.section
        ts = tsList.get(3);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTextTokenId.BRACES, "m}", -1);
        token = ts.token();
        assertEquals(PartType.END, token.partType());
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTextTokenId.TEXT, "n", -1);
        assertFalse(ts.moveNext());
        

        // Re-add second closing brace '}'
        doc.insertString(8, "}", null);
        LexerTestUtilities.assertConsistency(hi);
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        // before:    "a{b<cd>ef<gh>i{j<kl>m}n";
        // after:     "a{b<cd>e}f<gh>i{j<kl>m}n";
        tsList = hi.tokenSequenceList(innerLP, 0, Integer.MAX_VALUE);
        checkInitialTokens(tsList);
        
        doc.remove(0, doc.getLength());
        LexerTestUtilities.assertConsistency(hi);
        ts = hi.tokenSequence();
        assertFalse(ts.moveNext());
        doc.insertString(0, text, null);

    }

    private void checkInitialTokens(List<TokenSequence<?>> tsList) {
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        // text:      "a{b<cd>e}f<gh>i{j<kl>m}n";
        assertEquals(4, tsList.size()); // 4 sections

        // 1.section
        TokenSequence<?> ts = tsList.get(0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTextTokenId.TEXT, "a", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTextTokenId.BRACES, "{b", -1);
        Token<?> token = ts.token();
        assertEquals(PartType.START, token.partType());
        assertFalse(ts.moveNext());
        
        // 2.section
        ts = tsList.get(1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTextTokenId.BRACES, "e}", -1);
        token = ts.token();
        assertEquals(PartType.END, token.partType());
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTextTokenId.TEXT, "f", -1);
        assertFalse(ts.moveNext());
        
        // 3.section
        ts = tsList.get(2);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTextTokenId.TEXT, "i", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTextTokenId.BRACES, "{j", -1);
        token = ts.token();
        assertEquals(PartType.START, token.partType());
        assertFalse(ts.moveNext());
        
        // 4.section
        ts = tsList.get(3);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTextTokenId.BRACES, "m}", -1);
        token = ts.token();
        assertEquals(PartType.END, token.partType());
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTextTokenId.TEXT, "n", -1);
        assertFalse(ts.moveNext());
    }

}
