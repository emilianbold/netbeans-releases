/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.sdoc;

import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class SDocLexerTest extends NbTestCase {

    public SDocLexerTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    @SuppressWarnings("unchecked")
    public void testCommonDocComment01() {
        String text = "/** comment */";
        TokenHierarchy hi = TokenHierarchy.create(text, SDocTokenId.language());
        TokenSequence<?extends SDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.OTHER, "comment");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_END, "*/");
    }

    @SuppressWarnings("unchecked")
    public void testCommonDocComment02() {
        String text = "/**comment*/";
        TokenHierarchy hi = TokenHierarchy.create(text, SDocTokenId.language());
        TokenSequence<?extends SDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.OTHER, "comment");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_END, "*/");
    }

    @SuppressWarnings("unchecked")
    public void testCommonDocComment03() {
        String text = "/** \n\n */";
        TokenHierarchy hi = TokenHierarchy.create(text, SDocTokenId.language());
        TokenSequence<?extends SDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.EOL, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.EOL, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_END, "*/");
    }

    @SuppressWarnings("unchecked")
    public void testCommonDocComment04() {
        String text = "/**   @  */";
        TokenHierarchy hi = TokenHierarchy.create(text, SDocTokenId.language());
        TokenSequence<?extends SDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, "   ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.AT, "@");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, "  ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_END, "*/");
    }

    @SuppressWarnings("unchecked")
    public void testCommonDocComment05() {
        String text = "/** @p */";
        TokenHierarchy hi = TokenHierarchy.create(text, SDocTokenId.language());
        TokenSequence<?extends SDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.KEYWORD, "@p");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_END, "*/");
    }

    @SuppressWarnings("unchecked")
    public void testCommonDocComment06() {
        String text = "/** \n * @param */";
        TokenHierarchy hi = TokenHierarchy.create(text, SDocTokenId.language());
        TokenSequence<?extends SDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.EOL, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.ASTERISK, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.KEYWORD, "@param");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_END, "*/");
    }

    @SuppressWarnings("unchecked")
    public void testCommonDocComment07() {
        String text = "/** \n *@param */";
        TokenHierarchy hi = TokenHierarchy.create(text, SDocTokenId.language());
        TokenSequence<?extends SDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.EOL, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.ASTERISK, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.KEYWORD, "@param");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_END, "*/");
    }

    @SuppressWarnings("unchecked")
    public void testCommonDocComment08() {
        String text = "/** \n *@ */";
        TokenHierarchy hi = TokenHierarchy.create(text, SDocTokenId.language());
        TokenSequence<?extends SDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.EOL, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.ASTERISK, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.AT, "@");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_END, "*/");
    }

    @SuppressWarnings("unchecked")
    public void testCommonDocComment09() {
        String text = "/**\n * @param {String, Date} [myDate] Specifies the date, if applicable. */";
        TokenHierarchy hi = TokenHierarchy.create(text, SDocTokenId.language());
        TokenSequence<?extends SDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.EOL, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.ASTERISK, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.KEYWORD, "@param");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.BRACKET_LEFT_CURLY, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.OTHER, "String");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMA, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.OTHER, "Date");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.BRACKET_RIGHT_CURLY, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.BRACKET_LEFT_BRACKET, "[");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.OTHER, "myDate");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.BRACKET_RIGHT_BRACKET, "]");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.OTHER, "Specifies");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.OTHER, "the");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.OTHER, "date");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMA, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.OTHER, "if");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.OTHER, "applicable.");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_END, "*/");
    }

    // Not necessary since it's called just on documentation comments like /** */
//    @SuppressWarnings("unchecked")
//    public void testComment01() {
//        String text = "/***/";
//        TokenHierarchy hi = TokenHierarchy.create(text, SDocTokenId.language());
//        TokenSequence<?extends SDocTokenId> ts = hi.tokenSequence();
//        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_CODE, "/***/");
//    }
//
//    @SuppressWarnings("unchecked")
//    public void testComment02() {
//        String text = "/**/";
//        TokenHierarchy hi = TokenHierarchy.create(text, SDocTokenId.language());
//        TokenSequence<?extends SDocTokenId> ts = hi.tokenSequence();
//        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_CODE, "/**/");
//    }
//
//    @SuppressWarnings("unchecked")
//    public void testComment03() {
//        String text = "/* muj comment */";
//        TokenHierarchy hi = TokenHierarchy.create(text, SDocTokenId.language());
//        TokenSequence<?extends SDocTokenId> ts = hi.tokenSequence();
//        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_CODE, "/* muj comment */");
//    }

    @SuppressWarnings("unchecked")
    public void testUnfinishedComment01() {
        String text = "/* \n var Carrot = {";
        TokenHierarchy hi = TokenHierarchy.create(text, SDocTokenId.language());
        TokenSequence<?extends SDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.UNKNOWN, "/* \n var Carrot = {");
    }

    @SuppressWarnings("unchecked")
    public void testUnfinishedComment02() {
        String text = "/** getColor: function () {}, ";
        TokenHierarchy hi = TokenHierarchy.create(text, SDocTokenId.language());
        TokenSequence<?extends SDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.OTHER, "getColor:");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.OTHER, "function");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.OTHER, "()");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.BRACKET_LEFT_CURLY, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.BRACKET_RIGHT_CURLY, "}");
    }

    @SuppressWarnings("unchecked")
    public void testHtmlComment01() {
        String text = "/** <b>text</b> */";
        TokenHierarchy hi = TokenHierarchy.create(text, SDocTokenId.language());
        TokenSequence<?extends SDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.HTML, "<b>");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.OTHER, "text");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.HTML, "</b>");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_END, "*/");
    }

    @SuppressWarnings("unchecked")
    public void testHtmlComment02() {
        String text = "/** <a href=\"mailto:marfous@netbeans.org\">href</a> */";
        TokenHierarchy hi = TokenHierarchy.create(text, SDocTokenId.language());
        TokenSequence<?extends SDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.HTML, "<a href=\"mailto:marfous@netbeans.org\">");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.OTHER, "href");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.HTML, "</a>");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_END, "*/");
    }

    @SuppressWarnings("unchecked")
    public void testHtmlComment03() {
        String text = "/** <a */";
        TokenHierarchy hi = TokenHierarchy.create(text, SDocTokenId.language());
        TokenSequence<?extends SDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.HTML, "<a ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_END, "*/");
    }

    @SuppressWarnings("unchecked")
    public void testHtmlComment04() {
        String text = "/** < ";
        TokenHierarchy hi = TokenHierarchy.create(text, SDocTokenId.language());
        TokenSequence<?extends SDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.HTML, "< ");
    }

    @SuppressWarnings("unchecked")
    public void testCommentWithString() {
        String text = "/** @param ident \"cokoliv\" \n @param */";
        TokenHierarchy hi = TokenHierarchy.create(text, SDocTokenId.language());
        TokenSequence<?extends SDocTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_START, "/**");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.KEYWORD, "@param");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.OTHER, "ident");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.STRING_BEGIN, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.STRING, "cokoliv");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.STRING_END, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.EOL, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.KEYWORD, "@param");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, SDocTokenId.COMMENT_END, "*/");
    }
}
