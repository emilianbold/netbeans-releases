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
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, getText(), null);
        doc.putProperty(Language.class,TestJoinSectionsTopTokenId.language());
        
        TokenHierarchy<?> th = TokenHierarchy.get(doc);
        TokenSequence<?> ts = th.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTopTokenId.TEXT, "a{b", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTopTokenId.TAG, "<cd>", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTopTokenId.TEXT, "e}f", -1);
        
        // Get embedded tokens within TEXT tokens. There should be "a" then BRACES start "{b" then BRACES end "e}|" then "f"
        List<TokenSequence<?>> lts = th.tokenSequenceList(
                LanguagePath.get(TestJoinSectionsTopTokenId.language()).embedded(TestJoinSectionsTextTokenId.language()),
                0, Integer.MAX_VALUE);
        assertEquals(2, lts.size()); // 2 sections

        ts = lts.get(0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTextTokenId.TEXT, "a", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTextTokenId.BRACES, "{b", -1);
        Token<?> token = ts.token();
        assertEquals(PartType.START, token.partType());
        assertFalse(ts.moveNext());
        
        ts = lts.get(1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTextTokenId.BRACES, "e}", -1);
        token = ts.token();
        assertEquals(PartType.END, token.partType());
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestJoinSectionsTextTokenId.TEXT, "f", -1);
        assertFalse(ts.moveNext());
        
    }

    private String getText() {
        return "a{b<cd>e}f";
    }

}
