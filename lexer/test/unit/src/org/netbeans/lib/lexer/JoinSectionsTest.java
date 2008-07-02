/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.lib.lexer;

import java.util.List;
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
        
        // Check tokenSequenceList() with explicit offsets
        // Check correct TSs bounds
        tsList = hi.tokenSequenceList(innerLP, 0, 7);
        assertEquals(1, tsList.size());
        tsList = hi.tokenSequenceList(innerLP, 0, 8);
        assertEquals(2, tsList.size());
        
        
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

        // 1.section "a{b"
        ts = tsList.get(0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTextTokenId.TEXT, "a", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTextTokenId.BRACES, "{b", -1);
        Token<?> token = ts.token();
        assertEquals(PartType.START, token.partType());
        assertFalse(ts.moveNext());
        
        // 2.section "ef"
        ts = tsList.get(1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTextTokenId.BRACES, "ef", -1);
        token = ts.token();
        assertEquals(PartType.MIDDLE, token.partType());
        assertFalse(ts.moveNext());
        
        // 3.section "i{j"
        ts = tsList.get(2);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTextTokenId.BRACES, "i{j", -1);
        token = ts.token();
        assertEquals(PartType.MIDDLE, token.partType());
        assertFalse(ts.moveNext());
        
        // 4.section "m}n"
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
