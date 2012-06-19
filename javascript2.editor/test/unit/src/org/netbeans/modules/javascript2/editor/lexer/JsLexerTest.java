/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript2.editor.lexer;

import junit.framework.TestCase;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 *
 * @author tor
 */
public class JsLexerTest extends TestCase {
    
    public JsLexerTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    @SuppressWarnings("unchecked")
    public void testString1() {
        String text = "f(\"string\")";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "f");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.BRACKET_LEFT_PAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.STRING_BEGIN, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.STRING, "string");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.STRING_END, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.BRACKET_RIGHT_PAREN, ")");
    }

    @SuppressWarnings("unchecked")
    public void testString2() {
        String text = "f('string')";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "f");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.BRACKET_LEFT_PAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.STRING_BEGIN, "'");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.STRING, "string");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.STRING_END, "'");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.BRACKET_RIGHT_PAREN, ")");
    }

    @SuppressWarnings("unchecked")
    public void testString3() {
        String text = "''";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.STRING_BEGIN, "'");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.STRING_END, "'");
    }
    
    @SuppressWarnings("unchecked")
    public void testRegexp1() {
        String text = "f(/regexp/)";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "f");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.BRACKET_LEFT_PAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_BEGIN, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP, "regexp");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_END, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.BRACKET_RIGHT_PAREN, ")");
    }

    @SuppressWarnings("unchecked")
    public void testRegexp2() {
        String text = "x=/regexp/)";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.OPERATOR_ASSIGNMENT, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_BEGIN, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP, "regexp");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_END, "/");
    }

    @SuppressWarnings("unchecked")
    public void testRegexp3() {
        String text = "x = /regexp/)";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.OPERATOR_ASSIGNMENT, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_BEGIN, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP, "regexp");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_END, "/");
    }

    @SuppressWarnings("unchecked")
    public void testRegexp4() {
        String text = ";/regexp/";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.OPERATOR_SEMICOLON, ";");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_BEGIN, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP, "regexp");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_END, "/");
    }

    @SuppressWarnings("unchecked")
    public void testRegexp5() {
        String text = "f(x,/regexp/)";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "f");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.BRACKET_LEFT_PAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.OPERATOR_COMMA, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_BEGIN, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP, "regexp");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_END, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.BRACKET_RIGHT_PAREN, ")");
    }

    @SuppressWarnings("unchecked")
    public void testRegexp6() {
        String text = "f(x,/regexp/i)";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "f");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.BRACKET_LEFT_PAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.OPERATOR_COMMA, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_BEGIN, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP, "regexp");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_END, "/i");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.BRACKET_RIGHT_PAREN, ")");
    }

    @SuppressWarnings("unchecked")
    public void testRegexp7() {
        String text = "f(x,/\\sre\\gexp/i)";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "f");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.BRACKET_LEFT_PAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.OPERATOR_COMMA, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_BEGIN, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP, "\\sre\\gexp");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_END, "/i");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.BRACKET_RIGHT_PAREN, ")");
    }

    @SuppressWarnings("unchecked")
    public void testRegexp8() {
        String text = "f(x,/[s]/)";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "f");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.BRACKET_LEFT_PAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.OPERATOR_COMMA, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_BEGIN, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP, "[s]");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_END, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.BRACKET_RIGHT_PAREN, ")");
    }

    @SuppressWarnings("unchecked")
    public void testRegexp9() {
        String text = "var escapedString = this.replace(/a\\\\/g)";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.KEYWORD_VAR, "var");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "escapedString");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.OPERATOR_ASSIGNMENT, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.KEYWORD_THIS, "this");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.OPERATOR_DOT, ".");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "replace");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.BRACKET_LEFT_PAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_BEGIN, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP, "a\\\\");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_END, "/g");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.BRACKET_RIGHT_PAREN, ")");
    }

    @SuppressWarnings("unchecked")
    public void testRegexp10() {
        String text = "/\\/i";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_BEGIN, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.UNKNOWN, "\\/i");
    }

    @SuppressWarnings("unchecked")
    public void testRegexp11() {
        String text = "/\\\\/i";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_BEGIN, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP, "\\\\");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_END, "/i");
    }

    @SuppressWarnings("unchecked")
    public void testRegexp12() {
        String text = "/[/";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_BEGIN, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.UNKNOWN, "[/");
    }

    @SuppressWarnings("unchecked")
    public void testRegexp13() {
        String text = "/a[a/";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_BEGIN, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.UNKNOWN, "a[a/");
    }

    @SuppressWarnings("unchecked")
    public void testRegexp14() {
        String text = "/[]/a";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_BEGIN, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP, "[]");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_END, "/a");
    }

    @SuppressWarnings("unchecked")
    public void testRegexp15() {
        String text = "/]/a";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_BEGIN, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP, "]");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_END, "/a");
    }

    @SuppressWarnings("unchecked")
    public void testRegexp16() {
        String text = "/\\\\\\/a";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_BEGIN, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.UNKNOWN, "\\\\\\/a");
    }

    @SuppressWarnings("unchecked")
    public void testPartialRegexp() {
        String text = "x=/";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.OPERATOR_ASSIGNMENT, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_BEGIN, "/");
    }

    @SuppressWarnings("unchecked")
    public void testPartialRegexp2() {
        String text = "x=/\n";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.OPERATOR_ASSIGNMENT, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_BEGIN, "/");
    }

    @SuppressWarnings("unchecked")
    public void testPartialRegexp3() {
        String text = "x=/foo\nx";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.OPERATOR_ASSIGNMENT, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_BEGIN, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.UNKNOWN, "foo");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.EOL, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "x");
    }

    @SuppressWarnings("unchecked")
    public void testPartialRegexp4() {
        String text = "/[ something";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.REGEXP_BEGIN, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.UNKNOWN, "[ something");
    }

    @SuppressWarnings("unchecked")
    public void testNotRegexp() {
        String text = "//foo";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.LINE_COMMENT, "//foo");
    }

    @SuppressWarnings("unchecked")
    public void testNotRegexp2() {
        String text = "x/y";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.OPERATOR_DIVISION, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "y");
    }

    @SuppressWarnings("unchecked")
    public void testNotRegexp3() {
        String text = "10 / y";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.NUMBER, "10");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.OPERATOR_DIVISION, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "y");
    }

    @SuppressWarnings("unchecked")
    public void testNotRegexp4() {
        String text = "a/=2/5";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.OPERATOR_DIVISION_ASSIGNMENT, "/=");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.NUMBER, "2");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.OPERATOR_DIVISION, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.NUMBER, "5");
    }

    @SuppressWarnings("unchecked")
    public void testComments() {
        String text = "// This is my comment";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.LINE_COMMENT, text);
    }

    @SuppressWarnings("unchecked")
    public void testComments2() {
        String text = "/* This is my comment */";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.BLOCK_COMMENT, text);
    }

    @SuppressWarnings("unchecked")
    public void testComments3() {
        String text = "// This is my comment\n";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.LINE_COMMENT, "// This is my comment");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.EOL, "\n");
    }

    @SuppressWarnings("unchecked")
    public void testComments4() {
        String text = "/* This is my\ncomment */";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.BLOCK_COMMENT, text);
    }

    @SuppressWarnings("unchecked")
    public void testComments5() {
        String text = "/* This is \n";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.UNKNOWN, text);
    }

    @SuppressWarnings("unchecked")
    public void testComments6() {
        String text = "/** This is \n";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.UNKNOWN, text);
    }

    @SuppressWarnings("unchecked")
    public void testComments7() {
        String text = "/** This is my\ndoc comment */";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.DOC_COMMENT, text);
    }

    @SuppressWarnings("unchecked")
    public void testComments8() {
        String text = "//\n";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.LINE_COMMENT, "//");
    }

    @SuppressWarnings("unchecked")
    public void testComments9() {
        String text = "//";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.LINE_COMMENT, "//");
    }
    
    @SuppressWarnings("unchecked")
    public void testComments10() {
        String text = "/**/\nfunction x(){}";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.BLOCK_COMMENT, "/**/");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.EOL, "\n");
    }

    public void testStrings() {
        String[] strings =
            new String[] {
            "\"Hello\"",
            "'Hello'"};
        for (int i = 0; i < strings.length; i++) {
            TokenHierarchy hi = TokenHierarchy.create(strings[i], CommonTokenId.javascriptLanguage());
            TokenSequence ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            assertEquals(CommonTokenId.STRING_BEGIN, ts.token().id());
            assertTrue(ts.moveNext());
            assertEquals(CommonTokenId.STRING, ts.token().id());
            assertTrue(ts.moveNext());
            assertEquals(CommonTokenId.STRING_END, ts.token().id());
        }
    }

    @SuppressWarnings("unchecked")
    public void testUnterminatedString() {
        String text = "\"Line1\nLine2\nLine3";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.STRING_BEGIN, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.UNKNOWN, "Line1");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.EOL, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "Line2");
    }

    @SuppressWarnings("unchecked")
    public void testUnterminatedString2() {
        String text = "puts \"\n\n\n";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "puts");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.STRING_BEGIN, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.EOL, "\n");

    }

    @SuppressWarnings("unchecked")
    public void testUnterminatedString2b() {
        String text = "puts(\"\n\n\n";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "puts");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.BRACKET_LEFT_PAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.STRING_BEGIN, "\"");
    }

    @SuppressWarnings("unchecked")
    public void testUnterminatedString3() {
        String text = "x = \"";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.OPERATOR_ASSIGNMENT, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.STRING_BEGIN, "\"");
        assertFalse(ts.moveNext());
    }


    @SuppressWarnings("unchecked")
    public void testErrorString1() {
        String text = "print(\"pavel)";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.IDENTIFIER, "print");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.BRACKET_LEFT_PAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.STRING_BEGIN, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.UNKNOWN, "pavel)");
        assertFalse(ts.moveNext());
    }

    @SuppressWarnings("unchecked")
    public void testMultilineString() {
        String text = "\"Hello\\\nthis is multiline\"";
        TokenHierarchy hi = TokenHierarchy.create(text, CommonTokenId.javascriptLanguage());
        TokenSequence<? extends CommonTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.STRING_BEGIN, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.STRING, "Hello\\\nthis is multiline");
        LexerTestUtilities.assertNextTokenEquals(ts, CommonTokenId.STRING_END, "\"");
        assertFalse(ts.moveNext());
    }
}
