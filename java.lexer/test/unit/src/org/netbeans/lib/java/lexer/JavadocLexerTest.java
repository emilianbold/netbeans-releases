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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.lib.java.lexer;

import javax.swing.text.PlainDocument;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 *
 * @author Jan Lahoda
 */
public class JavadocLexerTest extends NbTestCase {

    public JavadocLexerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    public void testNextToken1() {
        String text = "@param aaa <code>aaa</code> xyz {@link org.Aaa#aaa()}";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavadocTokenId.language());
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.TAG, "@param");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "aaa");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.HTML_TAG, "<code>");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "aaa");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.HTML_TAG, "</code>");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "xyz");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, " {");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.TAG, "@link");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "org");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.DOT, ".");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "Aaa");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.HASH, "#");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "aaa");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, "()}");
    }

//    public void testModification1() throws Exception {
//        PlainDocument doc = new PlainDocument();
//        doc.putProperty(Language.class, JavadocTokenId.language());
//        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
//        
//        {
//            TokenSequence<? extends TokenId> ts = hi.tokenSequence();
//            ts.moveStart();
//            assertFalse(ts.moveNext());
//        }
//        
//        doc.insertString(0, "@", null);
//    }
}
