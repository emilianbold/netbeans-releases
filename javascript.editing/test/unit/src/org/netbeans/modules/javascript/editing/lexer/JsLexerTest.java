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

package org.netbeans.modules.javascript.editing.lexer;

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

    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    @SuppressWarnings("unchecked")
    public void testString1() {
        String text = "f(\"string\")";
        TokenHierarchy hi = TokenHierarchy.create(text, JsTokenId.language());
        TokenSequence<?extends JsTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.IDENTIFIER, "f");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.STRING_BEGIN, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.STRING_LITERAL, "string");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.STRING_END, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.RPAREN, ")");
    }

    @SuppressWarnings("unchecked")
    public void testString2() {
        String text = "f('string')";
        TokenHierarchy hi = TokenHierarchy.create(text, JsTokenId.language());
        TokenSequence<?extends JsTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.IDENTIFIER, "f");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.STRING_BEGIN, "'");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.STRING_LITERAL, "string");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.STRING_END, "'");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.RPAREN, ")");
    }

    @SuppressWarnings("unchecked")
    public void testString3() {
        String text = "''";
        TokenHierarchy hi = TokenHierarchy.create(text, JsTokenId.language());
        TokenSequence<?extends JsTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.STRING_BEGIN, "'");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.STRING_END, "'");
    }
    
    @SuppressWarnings("unchecked")
    public void testRegexp1() {
        String text = "f(/regexp/)";
        TokenHierarchy hi = TokenHierarchy.create(text, JsTokenId.language());
        TokenSequence<?extends JsTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.IDENTIFIER, "f");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.REGEXP_BEGIN, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.REGEXP_LITERAL, "regexp");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.REGEXP_END, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.RPAREN, ")");
    }

    @SuppressWarnings("unchecked")
    public void testRegexp2() {
        String text = "x=/regexp/)";
        TokenHierarchy hi = TokenHierarchy.create(text, JsTokenId.language());
        TokenSequence<?extends JsTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.NONUNARY_OP, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.REGEXP_BEGIN, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.REGEXP_LITERAL, "regexp");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.REGEXP_END, "/");
    }

    @SuppressWarnings("unchecked")
    public void testRegexp3() {
        String text = "x = /regexp/)";
        TokenHierarchy hi = TokenHierarchy.create(text, JsTokenId.language());
        TokenSequence<?extends JsTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.NONUNARY_OP, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.REGEXP_BEGIN, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.REGEXP_LITERAL, "regexp");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.REGEXP_END, "/");
    }

    @SuppressWarnings("unchecked")
    public void testRegexp4() {
        String text = ";/regexp/";
        TokenHierarchy hi = TokenHierarchy.create(text, JsTokenId.language());
        TokenSequence<?extends JsTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.SEMI, ";");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.REGEXP_BEGIN, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.REGEXP_LITERAL, "regexp");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.REGEXP_END, "/");
    }
    
    @SuppressWarnings("unchecked")
    public void testRegexp5() {
        String text = "f(x,/regexp/)";
        TokenHierarchy hi = TokenHierarchy.create(text, JsTokenId.language());
        TokenSequence<?extends JsTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.IDENTIFIER, "f");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.ANY_OPERATOR, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.REGEXP_BEGIN, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.REGEXP_LITERAL, "regexp");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.REGEXP_END, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.RPAREN, ")");
    }

    @SuppressWarnings("unchecked")
    public void testRegexp6() {
        String text = "f(x,/regexp/i)";
        TokenHierarchy hi = TokenHierarchy.create(text, JsTokenId.language());
        TokenSequence<?extends JsTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.IDENTIFIER, "f");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.ANY_OPERATOR, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.REGEXP_BEGIN, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.REGEXP_LITERAL, "regexp");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.REGEXP_END, "/i");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.RPAREN, ")");
    }

    @SuppressWarnings("unchecked")
    public void testPartialRegexp() {
        String text = "x=/";
        TokenHierarchy hi = TokenHierarchy.create(text, JsTokenId.language());
        TokenSequence<?extends JsTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.NONUNARY_OP, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.REGEXP_BEGIN, "/");
    }

    @SuppressWarnings("unchecked")
    public void testPartialRegexp2() {
        String text = "x=/\n";
        TokenHierarchy hi = TokenHierarchy.create(text, JsTokenId.language());
        TokenSequence<?extends JsTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.NONUNARY_OP, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.REGEXP_BEGIN, "/");
    }

    @SuppressWarnings("unchecked")
    public void testPartialRegexp3() {
        String text = "x=/foo\nx";
        TokenHierarchy hi = TokenHierarchy.create(text, JsTokenId.language());
        TokenSequence<?extends JsTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.NONUNARY_OP, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.REGEXP_BEGIN, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.ERROR, "foo");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.EOL, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.IDENTIFIER, "x");
    }

    @SuppressWarnings("unchecked")
    public void testNotRegexp() {
        String text = "//foo";
        TokenHierarchy hi = TokenHierarchy.create(text, JsTokenId.language());
        TokenSequence<?extends JsTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.LINE_COMMENT, "//foo");
    }

    @SuppressWarnings("unchecked")
    public void testNotRegexp2() {
        String text = "x/y";
        TokenHierarchy hi = TokenHierarchy.create(text, JsTokenId.language());
        TokenSequence<?extends JsTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.NONUNARY_OP, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.IDENTIFIER, "y");
    }
    
    @SuppressWarnings("unchecked")
    public void testNotRegexp3() {
        String text = "10 / y";
        TokenHierarchy hi = TokenHierarchy.create(text, JsTokenId.language());
        TokenSequence<?extends JsTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.FLOAT_LITERAL, "10");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.NONUNARY_OP, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.IDENTIFIER, "y");
    }
    
    @SuppressWarnings("unchecked")
    public void testComments() {
        String text = "// This is my comment";
        TokenHierarchy hi = TokenHierarchy.create(text, JsTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.LINE_COMMENT, text);
    }

    @SuppressWarnings("unchecked")
    public void testComments2() {
        String text = "/* This is my comment */";
        TokenHierarchy hi = TokenHierarchy.create(text, JsTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.BLOCK_COMMENT, text);
    }

    public void testStrings() {
        String[] strings =
            new String[] { 
            "\"Hello\"",
            "'Hello'"};
        for (int i = 0; i < strings.length; i++) {
            TokenHierarchy hi = TokenHierarchy.create(strings[i], JsTokenId.language());
            TokenSequence ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            assertEquals(JsTokenId.STRING_BEGIN, ts.token().id());
            assertTrue(ts.moveNext());
            assertEquals(JsTokenId.STRING_LITERAL, ts.token().id());
            assertTrue(ts.moveNext());
            assertEquals(JsTokenId.STRING_END, ts.token().id());
        }
    }

    @SuppressWarnings("unchecked")
    public void testUnterminatedString() {
        String text = "\"Line1\nLine2\nLine3";
        TokenHierarchy hi = TokenHierarchy.create(text, JsTokenId.language());
        TokenSequence<?extends JsTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.STRING_BEGIN, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.ERROR, "Line1");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.EOL, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.IDENTIFIER, "Line2");
    }

    @SuppressWarnings("unchecked")
    public void testUnterminatedString2() {
        String text = "puts \"\n\n\n";
        TokenHierarchy hi = TokenHierarchy.create(text, JsTokenId.language());
        TokenSequence<?extends JsTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.IDENTIFIER, "puts");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.WHITESPACE, " ");
        //LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.ERROR, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.STRING_BEGIN, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.EOL, "\n");
        
    }    

    @SuppressWarnings("unchecked")
    public void testUnterminatedString2b() {
        String text = "puts(\"\n\n\n";
        TokenHierarchy hi = TokenHierarchy.create(text, JsTokenId.language());
        TokenSequence<?extends JsTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.IDENTIFIER, "puts");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.LPAREN, "(");
        //LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.ERROR, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.STRING_BEGIN, "\"");
    }    

    @SuppressWarnings("unchecked")
    public void testUnterminatedString3() {
        String text = "x = \"";
        TokenHierarchy hi = TokenHierarchy.create(text, JsTokenId.language());
        TokenSequence<?extends JsTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.NONUNARY_OP, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.WHITESPACE, " ");
        //LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.ERROR, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.STRING_BEGIN, "\"");
        assertFalse(ts.moveNext());
    }
    

    @SuppressWarnings("unchecked")
    public void testErrorString1() {
        String text = "print(\"pavel)";
        TokenHierarchy hi = TokenHierarchy.create(text, JsTokenId.language());
        TokenSequence<?extends JsTokenId> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.IDENTIFIER, "print");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.STRING_BEGIN, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, JsTokenId.ERROR, "pavel)");
        assertFalse(ts.moveNext());
    }
}
