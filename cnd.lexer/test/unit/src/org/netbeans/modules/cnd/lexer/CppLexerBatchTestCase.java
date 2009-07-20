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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.cnd.lexer;

import junit.framework.TestCase;
import org.netbeans.cnd.api.lexer.CppStringTokenId;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.DoxygenTokenId;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * Test several lexer inputs.
 *
 * @author Vladimir Voskresensky
 */
public class CppLexerBatchTestCase extends TestCase {

    public CppLexerBatchTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    @Override
    protected void tearDown() throws java.lang.Exception {
    }

    public void testAloneBackSlash() {
        String text = "\\\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();

        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ESCAPED_LINE, "\\\n");
        assertFalse("No more tokens", ts.moveNext());
    }

    public void testPreprocEmbedding() {
        String text = "#define C 1 \"/*\" /* \n@see C */";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();

        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_DIRECTIVE, "#define C 1 \"/*\" /* \n@see C */");

        TokenSequence<?> ep = ts.embedded();
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.PREPROCESSOR_START, "#");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.PREPROCESSOR_DEFINE, "define");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.PREPROCESSOR_IDENTIFIER, "C");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.INT_LITERAL, "1");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.STRING_LITERAL, "\"/*\"");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.BLOCK_COMMENT, "/* \n@see C */");
        assertFalse("No more tokens", ep.moveNext());

        assertFalse("No more tokens", ts.moveNext());

    }

    public void testComments() {
        String text = "/// doxygen line comment\n/*ml-comment*//**//***//*! doxygen*//**\n*doxygen-comment*//* a";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DOXYGEN_LINE_COMMENT, "/// doxygen line comment");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.BLOCK_COMMENT, "/*ml-comment*/");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.BLOCK_COMMENT, "/**/");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DOXYGEN_COMMENT, "/***/");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DOXYGEN_COMMENT, "/*! doxygen*/");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DOXYGEN_COMMENT, "/**\n*doxygen-comment*/");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.BLOCK_COMMENT, "/* a");
        assertEquals(PartType.START, ts.token().partType());
    }

    public void testIdentifiers() {
        String text = "a ab aB2 2a x\nyZ\r\nz";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "ab");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "aB2");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT_LITERAL, "2");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "yZ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\r\n");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "z");
    }

    public void testCharLiterals() {
        String text = "'' 'a''' '\\'' '\\\\' '\\\\\\'' '\\n' 'a";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CHAR_LITERAL, "''");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CHAR_LITERAL, "'a'");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CHAR_LITERAL, "''");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CHAR_LITERAL, "'\\''");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CHAR_LITERAL, "'\\\\'");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CHAR_LITERAL, "'\\\\\\''");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CHAR_LITERAL, "'\\n'");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CHAR_LITERAL, "'a");
        assertEquals(PartType.START, ts.token().partType());
    }

    public void testStringLiterals() {
        String text = "\"\" \"a\"\"\" \"\\\"\" \"\\\\\" \"\\\\\\\"\" \"\\n\" \"a";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "\"a\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "\"\\\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "\"\\\\\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "\"\\\\\\\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "\"\\n\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "\"a");
        assertEquals(PartType.START, ts.token().partType());
    }

    public void testNumberLiterals() {
        String text = "0 00 09 1 12 0L 1l 12L 0LL 1ll 0x1 0xf 0XdE 0Xbcy" +
                " 09.5 1.5f 1.6F 6u 7U 7e3 6.1E-7f .3 3614090360UL 3614090360ul 0xffffffffull" +
                " 0x1234l 0x1234L 0.0747474774773784e-4L 0.0747474774773784e-4l";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT_LITERAL, "0");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT_LITERAL, "00");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT_LITERAL, "09");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT_LITERAL, "1");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT_LITERAL, "12");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LONG_LITERAL, "0L");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LONG_LITERAL, "1l");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LONG_LITERAL, "12L");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LONG_LONG_LITERAL, "0LL");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LONG_LONG_LITERAL, "1ll");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT_LITERAL, "0x1");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT_LITERAL, "0xf");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT_LITERAL, "0XdE");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT_LITERAL, "0Xbc");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "y");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DOUBLE_LITERAL, "09.5");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.FLOAT_LITERAL, "1.5f");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.FLOAT_LITERAL, "1.6F");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.UNSIGNED_LITERAL, "6u");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.UNSIGNED_LITERAL, "7U");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DOUBLE_LITERAL, "7e3");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.FLOAT_LITERAL, "6.1E-7f");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DOUBLE_LITERAL, ".3");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.UNSIGNED_LONG_LITERAL, "3614090360UL");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.UNSIGNED_LONG_LITERAL, "3614090360ul");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.UNSIGNED_LONG_LONG_LITERAL, "0xffffffffull");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LONG_LITERAL, "0x1234l");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LONG_LITERAL, "0x1234L");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DOUBLE_LITERAL, "0.0747474774773784e-4L");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DOUBLE_LITERAL, "0.0747474774773784e-4l");
        assertFalse("No more tokens", ts.moveNext());
    }

    public void testOperators() {
        String text = "= > < ! ~ ? : == <= >= != && || ++ -- + - * / & | ^ % << >> += -= *= /= &= |= ^= %= <<= >>=";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.EQ, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.GT, ">");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LT, "<");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NOT, "!");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.TILDE, "~");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.QUESTION, "?");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.COLON, ":");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.EQEQ, "==");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LTEQ, "<=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.GTEQ, ">=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NOTEQ, "!=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.AMPAMP, "&&");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.BARBAR, "||");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PLUSPLUS, "++");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.MINUSMINUS, "--");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PLUS, "+");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.MINUS, "-");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STAR, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.SLASH, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.AMP, "&");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.BAR, "|");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CARET, "^");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PERCENT, "%");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LTLT, "<<");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.GTGT, ">>");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PLUSEQ, "+=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.MINUSEQ, "-=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STAREQ, "*=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.SLASHEQ, "/=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.AMPEQ, "&=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.BAREQ, "|=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CARETEQ, "^=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PERCENTEQ, "%=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LTLTEQ, "<<=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.GTGTEQ, ">>=");

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testLineContinuation() {
        String text = "  #de\\\n" +
                      "fine A\\\r" +
                      "AA 1 // comment \\\n" +
                      " comment-again\n" +
                      "ch\\\n" +
                      "ar* a = \"str\\\n" +
                      "0\"\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, "  ");
        assertEquals(0, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_DIRECTIVE, "#de\\\nfine A\\\rAA 1 // comment \\\n comment-again\n");
        assertEquals(2, ts.offset());
        TokenSequence<?> ep = ts.embedded();

        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.PREPROCESSOR_START, "#");
        assertEquals(2, ep.offset());
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.PREPROCESSOR_DEFINE, "de\\\nfine");
        assertEquals(3, ep.offset());
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.WHITESPACE, " ");
        assertEquals(11, ep.offset());
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.PREPROCESSOR_IDENTIFIER, "A\\\rAA");
        assertEquals(12, ep.offset());
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.WHITESPACE, " ");
        assertEquals(17, ep.offset());
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.INT_LITERAL, "1");
        assertEquals(18, ep.offset());
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.WHITESPACE, " ");
        assertEquals(19, ep.offset());
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.LINE_COMMENT, "// comment \\\n comment-again");
        assertEquals(20, ep.offset());
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.NEW_LINE, "\n");
        assertEquals(47, ep.offset());
        assertFalse("No more tokens", ep.moveNext());

        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CHAR, "ch\\\nar");
        assertEquals(48, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STAR, "*");
        assertEquals(54, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        assertEquals(55, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "a");
        assertEquals(56, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        assertEquals(57, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.EQ, "=");
        assertEquals(58, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        assertEquals(59, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "\"str\\\n0\"");
        assertEquals(60, ts.offset());

        TokenSequence<?> es = ts.embedded();

        LexerTestUtilities.assertNextTokenEquals(es, CppStringTokenId.DOUBLE_QUOTE, "\"");
        LexerTestUtilities.assertNextTokenEquals(es, CppStringTokenId.TEXT, "str\\\n0");
        LexerTestUtilities.assertNextTokenEquals(es, CppStringTokenId.DOUBLE_QUOTE, "\"");
        assertEquals(67, es.offset());

        assertFalse("No more tokens", es.moveNext());

        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");
        assertEquals(68, ts.offset());
        assertFalse("No more tokens", ts.moveNext());

    }

    public void testLineContinuationAfterSlash() {
        String text = "\n" +
                      "    /\\\n\n" +
                      "#define /\\\n    \n\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, "    ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.SLASH, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ESCAPED_LINE, "\\\n");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_DIRECTIVE, "#define /\\\n    \n");

        TokenSequence<?> ep = ts.embedded();
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.PREPROCESSOR_START, "#");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.PREPROCESSOR_DEFINE, "define");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.SLASH, "/");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.ESCAPED_LINE, "\\\n");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.WHITESPACE, "    ");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ep.moveNext());

        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");
        assertFalse("No more tokens", ts.moveNext());
    }

    private String getAllKeywords() {
        return "asm auto bool break case catch char class const const_cast continue " +
            "default delete do double dynamic_cast else enum finally float for friend goto if " +
            "inline int long mutable namespace new operator " +
            "private protected public register reinterpret_cast restrict return " +
            "short signed sizeof static static_cast struct switch " +
            "template this throw try typedef typeid typename typeof " +
            "union unsigned using virtual void volatile wchar_t while " +
            "_Bool _Complex _Imaginary " +
            "null true false";
    }

    public void testCppKeywords() {
        String text = getAllKeywords();
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ASM, "asm");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.AUTO, "auto");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.BOOL, "bool");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.BREAK, "break");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CASE, "case");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CATCH, "catch");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CHAR, "char");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CLASS, "class");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CONST, "const");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CONST_CAST, "const_cast");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CONTINUE, "continue");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DEFAULT, "default");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DELETE, "delete");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DO, "do");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DOUBLE, "double");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DYNAMIC_CAST, "dynamic_cast");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ELSE, "else");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ENUM, "enum");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.FINALLY, "finally");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.FLOAT, "float");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.FOR, "for");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.FRIEND, "friend");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.GOTO, "goto");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IF, "if");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INLINE, "inline");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT, "int");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LONG, "long");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.MUTABLE, "mutable");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NAMESPACE, "namespace");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW, "new");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.OPERATOR, "operator");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PRIVATE, "private");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PROTECTED, "protected");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PUBLIC, "public");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.REGISTER, "register");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.REINTERPRET_CAST, "reinterpret_cast");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "restrict"); // C
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.RETURN, "return");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.SHORT, "short");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.SIGNED, "signed");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.SIZEOF, "sizeof");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STATIC, "static");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STATIC_CAST, "static_cast");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRUCT, "struct");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.SWITCH, "switch");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.TEMPLATE, "template");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.THIS, "this");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.THROW, "throw");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.TRY, "try");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.TYPEDEF, "typedef");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.TYPEID, "typeid");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.TYPENAME, "typename");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.TYPEOF, "typeof");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.UNION, "union");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.UNSIGNED, "unsigned");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.USING, "using");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.VIRTUAL, "virtual");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.VOID, "void");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.VOLATILE, "volatile");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WCHAR_T, "wchar_t");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHILE, "while");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");

        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "_Bool"); //C
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "_Complex"); //C
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "_Imaginary"); //C
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");

        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "null");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.TRUE, "true");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.FALSE, "false");
    }

    public void testCKeywords() {
        String text = getAllKeywords();
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageC());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "asm"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.AUTO, "auto");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "bool"); //C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.BREAK, "break");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CASE, "case");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "catch"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CHAR, "char");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "class"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CONST, "const");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "const_cast"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CONTINUE, "continue");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DEFAULT, "default");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "delete"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DO, "do");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DOUBLE, "double");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "dynamic_cast"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ELSE, "else");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ENUM, "enum");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "finally"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.FLOAT, "float");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.FOR, "for");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "friend"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.GOTO, "goto");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IF, "if");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INLINE, "inline");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT, "int");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LONG, "long");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "mutable"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "namespace"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "new"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "operator"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "private"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "protected"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "public"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.REGISTER, "register");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "reinterpret_cast"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.RESTRICT, "restrict"); // C
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.RETURN, "return");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.SHORT, "short");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.SIGNED, "signed");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.SIZEOF, "sizeof");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STATIC, "static");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "static_cast"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRUCT, "struct");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.SWITCH, "switch");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "template"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "this"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "throw"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "try"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.TYPEDEF, "typedef");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "typeid"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "typename"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "typeof"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.UNION, "union");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.UNSIGNED, "unsigned");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "using"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "virtual"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.VOID, "void");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.VOLATILE, "volatile");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "wchar_t"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHILE, "while");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");

        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId._BOOL, "_Bool"); //C
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId._COMPLEX, "_Complex"); //C
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId._IMAGINARY, "_Imaginary"); //C
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");

        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "null");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "true"); // C++
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "false"); // C++
    }

    public void testNonKeywords() {
        String text = "asma autos b br car dou doubl finall im i ifa inti throwx ";

        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "asma");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "autos");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "b");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "br");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "car");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "dou");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "doubl");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "finall");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "im");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "i");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "ifa");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "inti");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "throwx");
    }

    public void testEmbedding() {
        String text = "ddx \"d\\t\\br\" /** @see X */ L\"Lex\" L2";

        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();

        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "ddx");
        assertEquals(0, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        assertEquals(3, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "\"d\\t\\br\"");
        assertEquals(4, ts.offset());

        TokenSequence<?> es = ts.embedded();

        LexerTestUtilities.assertNextTokenEquals(es, CppStringTokenId.DOUBLE_QUOTE, "\"");
        assertEquals(4, es.offset());
        LexerTestUtilities.assertNextTokenEquals(es, CppStringTokenId.TEXT, "d");
        assertEquals(5, es.offset());
        LexerTestUtilities.assertNextTokenEquals(es, CppStringTokenId.TAB, "\\t");
        assertEquals(6, es.offset());
        LexerTestUtilities.assertNextTokenEquals(es, CppStringTokenId.BACKSPACE, "\\b");
        assertEquals(8, es.offset());
        LexerTestUtilities.assertNextTokenEquals(es, CppStringTokenId.TEXT, "r");
        assertEquals(10, es.offset());
        LexerTestUtilities.assertNextTokenEquals(es, CppStringTokenId.DOUBLE_QUOTE, "\"");
        assertEquals(11, es.offset());

        assertFalse(es.moveNext());

        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        assertEquals(12, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DOXYGEN_COMMENT, "/** @see X */");
        assertEquals(13, ts.offset());

        TokenSequence<?> ds = ts.embedded();

        LexerTestUtilities.assertNextTokenEquals(ds, DoxygenTokenId.OTHER_TEXT, " ");
        assertEquals(16, ds.offset());
        LexerTestUtilities.assertNextTokenEquals(ds, DoxygenTokenId.TAG, "@see");
        assertEquals(17, ds.offset());
        LexerTestUtilities.assertNextTokenEquals(ds, DoxygenTokenId.OTHER_TEXT, " ");
        assertEquals(21, ds.offset());
        LexerTestUtilities.assertNextTokenEquals(ds, DoxygenTokenId.IDENT, "X");
        assertEquals(22, ds.offset());
        LexerTestUtilities.assertNextTokenEquals(ds, DoxygenTokenId.OTHER_TEXT, " ");
        assertEquals(23, ds.offset());

        assertFalse(ds.moveNext());

        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        assertEquals(26, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "L\"Lex\"");
        assertEquals(27, ts.offset());

        es = ts.embedded();

        LexerTestUtilities.assertNextTokenEquals(es, CppStringTokenId.PREFIX, "L");
        assertEquals(27, es.offset());
        LexerTestUtilities.assertNextTokenEquals(es, CppStringTokenId.DOUBLE_QUOTE, "\"");
        assertEquals(28, es.offset());
        LexerTestUtilities.assertNextTokenEquals(es, CppStringTokenId.TEXT, "Lex");
        assertEquals(29, es.offset());
        LexerTestUtilities.assertNextTokenEquals(es, CppStringTokenId.DOUBLE_QUOTE, "\"");
        assertEquals(32, es.offset());

        assertFalse(es.moveNext());

        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        assertEquals(33, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "L2");

        assertFalse(ts.moveNext());
    }

    public void testStrings() {
        // IZ#144221: IDE highlights L' ' and L" " as wrong code
        String text = "L\"\\x20\\x9\\xD\\xA\" L' ' L'\\x20'";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();      
        
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "L\"\\x20\\x9\\xD\\xA\"");  
        TokenSequence<?> es = ts.embedded();

        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.PREFIX, "L");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.DOUBLE_QUOTE, "\"");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.HEX_ESCAPE, "\\x20");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.HEX_ESCAPE, "\\x9");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.HEX_ESCAPE, "\\xD");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.HEX_ESCAPE, "\\xA");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.DOUBLE_QUOTE, "\"");
        assertFalse("No more tokens", es.moveNext());

        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.CHAR_LITERAL, "L' '");
        es = ts.embedded();
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.PREFIX, "L");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.SINGLE_QUOTE, "'");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.SINGLE_QUOTE, "'");
        assertFalse("No more tokens", es.moveNext());
        
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.CHAR_LITERAL, "L'\\x20'");
        es = ts.embedded();
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.PREFIX, "L");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.SINGLE_QUOTE, "'");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.HEX_ESCAPE, "\\x20");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.SINGLE_QUOTE, "'");
        assertFalse("No more tokens", es.moveNext());
        
        assertFalse("No more tokens", ts.moveNext());
    }

    public void testSpecial() {
        String text = "\\ ... $ @";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.BACK_SLASH, "\\");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ELLIPSIS, "...");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DOLLAR, "$");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.AT, "@");

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testSeparators() {
        String text = "( ) { } [ ] ; , . .* :: -> ->*";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LBRACE, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.RBRACE, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LBRACKET, "[");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.RBRACKET, "]");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.SEMICOLON, ";");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.COMMA, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DOT, ".");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DOTMBR, ".*");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.SCOPE, "::");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ARROW, "->");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ARROWMBR, "->*");

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testComment2() {
        // IZ#83566: "*/" string is highlighted as error
        String text = "const/*    */int*/*       */i = 0; */";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();

        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CONST, "const");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.BLOCK_COMMENT, "/*    */");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT, "int");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STAR, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.BLOCK_COMMENT, "/*       */");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "i");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.EQ, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT_LITERAL, "0");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.SEMICOLON, ";");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INVALID_COMMENT_END, "*/");

        assertFalse("No more tokens", ts.moveNext());

    }
}
