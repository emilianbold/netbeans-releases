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

import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * tests for preprocessor directive lexer
 *
 */
public class PreprocLexerTestCase extends NbTestCase {

    public PreprocLexerTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        LexerTestUtilities.setTesting(true);
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    private void doTestIfdef(boolean altStartToken) {
        String startText = altStartToken ? "%:" : "#";
        String text = startText + "ifdef MACRO\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();

        if (altStartToken) {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START_ALT, "%:");
        } else {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        }
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IFDEF, "ifdef");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "MACRO");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testIfdef() {
        doTestIfdef(true);
        doTestIfdef(false);
    }

    private void doTestIfndef(boolean altStartToken) {
        String startText = altStartToken ? "%:" : "#";
        String text = startText + " ifndef MACRO\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();

        if (altStartToken) {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START_ALT, "%:");
        } else {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        }
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IFNDEF, "ifndef");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "MACRO");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testIfndef() {
        doTestIfndef(true);
        doTestIfndef(false);
    }

    private void doTestIf(boolean altStartToken) {
        String startText = altStartToken ? "%:" : "#";
        String text = startText + " if defined MACRO\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        if (altStartToken) {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START_ALT, "%:");
        } else {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        }
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IF, "if");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_DEFINED, "defined");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "MACRO");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testIf() {
        doTestIf(true);
        doTestIf(false);
    }

    private void doTestElif(boolean altStartToken) {
        String startText = altStartToken ? "%:" : "#";
        String text = startText + "elif !defined(MACRO)\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        if (altStartToken) {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START_ALT, "%:");
        } else {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        }
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_ELIF, "elif");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NOT, "!");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_DEFINED, "defined");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "MACRO");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testElif() {
        doTestElif(true);
        doTestElif(false);
    }

    private void doTestElif2(boolean altStartToken) {
        String startText = altStartToken ? "%:" : "#";
        String text = startText + "elif!defined(MACRO)\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        if (altStartToken) {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START_ALT, "%:");
        } else {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        }
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_ELIF, "elif");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NOT, "!");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_DEFINED, "defined");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "MACRO");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testElif2() {
        doTestElif2(true);
        doTestElif2(false);
    }

    private void doTestElse(boolean altStartToken) {
        String startText = altStartToken ? "%:" : "#";
        String text = startText + "else //comment\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        if (altStartToken) {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START_ALT, "%:");
        } else {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        }
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_ELSE, "else");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LINE_COMMENT, "//comment");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testElse() {
        doTestElse(true);
        doTestElse(false);
    }

    private void doTestEndif(boolean altStartToken) {
        String startText = altStartToken ? "%:" : "#";
        String text = startText + "endif defined// MACRO\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        if (altStartToken) {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START_ALT, "%:");
        } else {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        }
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_ENDIF, "endif");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "defined");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LINE_COMMENT, "// MACRO");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());

    }

    public void testEndif() {
        doTestEndif(true);
        doTestEndif(false);
    }

    private void doTestDefine(boolean altStartToken) {
        String startText = altStartToken ? "%:" : "#";
        String text = startText + "define MAX(x,y) \\\n (((x)<(y)) ? (y) : (x))\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        if (altStartToken) {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START_ALT, "%:");
        } else {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        }
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_DEFINE, "define");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "MAX");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.COMMA, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "y");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ESCAPED_WHITESPACE, " \\\n ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LT, "<");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "y");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.QUESTION, "?");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "y");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.COLON, ":");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testDefine() {
        doTestDefine(true);
        doTestDefine(false);
    }

    private void doTestUndef(boolean altStartToken) {
        String startText = altStartToken ? "%:" : "#";
        String text = startText + "undef MACRO\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        if (altStartToken) {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START_ALT, "%:");
        } else {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        }
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_UNDEF, "undef");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "MACRO");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testUndef() {
        doTestUndef(true);
        doTestUndef(false);
    }

    private void doTestPragma(boolean altStartToken) {
        String startText = altStartToken ? "%:" : "#";
        String text = startText + "pragma omp parallel for shared(array, array1, array2, dim) private(ii, jj, kk)\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        if (altStartToken) {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START_ALT, "%:");
        } else {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        }
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_PRAGMA, "pragma");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PRAGMA_OMP_START, "omp");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PRAGMA_OMP_PARALLEL, "parallel");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PRAGMA_OMP_FOR, "for");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PRAGMA_OMP_SHARED, "shared");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "array");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.COMMA, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "array1");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.COMMA, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "array2");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.COMMA, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "dim");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PRAGMA_OMP_PRIVATE, "private");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "ii");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.COMMA, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "jj");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.COMMA, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "kk");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testPragma() {
        doTestPragma(true);
        doTestPragma(false);
    }

    private void doTestError(boolean altStartToken) {
        String startText = altStartToken ? "%:" : "#";
        String text = startText + "error \"message\"  \n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        if (altStartToken) {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START_ALT, "%:");
        } else {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        }
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_ERROR, "error");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "\"message\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, "  ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testError() {
        doTestError(true);
        doTestError(false);
    }

    private void doTestLine(boolean altStartToken) {
        String startText = altStartToken ? "%:" : "#";
        String text = startText + "line 100 //change line\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        if (altStartToken) {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START_ALT, "%:");
        } else {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        }
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_LINE, "line");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT_LITERAL, "100");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LINE_COMMENT, "//change line");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testLine() {
        doTestLine(true);
        doTestLine(false);
    }

    private void doTestSysIncludeNext(boolean altStartToken) {
        String startText = altStartToken ? "%:" : "#";
        String text = startText + "  include_next <file.h>\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        if (altStartToken) {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START_ALT, "%:");
        } else {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        }
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, "  ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_INCLUDE_NEXT, "include_next");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_SYS_INCLUDE, "<file.h>");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testSysIncludeNext() {
        doTestSysIncludeNext(true);
        doTestSysIncludeNext(false);
    }

    private void doTestSysInclude(boolean altStartToken) {
        String startText = altStartToken ? "%:" : "#";
        String text = startText + "  include <file.h>\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        if (altStartToken) {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START_ALT, "%:");
        } else {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        }
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, "  ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_INCLUDE, "include");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_SYS_INCLUDE, "<file.h>");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testSysInclude() {
        doTestSysInclude(true);
        doTestSysInclude(false);
    }

    private void doTestSysIncludeNext2(boolean altStartToken) {
        String startText = altStartToken ? "%:" : "#";
        String text = startText + "  include_next<file.h>\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        if (altStartToken) {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START_ALT, "%:");
        } else {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        }
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, "  ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_INCLUDE_NEXT, "include_next");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_SYS_INCLUDE, "<file.h>");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testSysIncludeNext2() {
        doTestSysIncludeNext2(true);
        doTestSysIncludeNext2(false);
    }

    private void doTestSysInclude2(boolean altStartToken) {
        String startText = altStartToken ? "%:" : "#";
        String text = startText + "  include<file.h>\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        if (altStartToken) {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START_ALT, "%:");
        } else {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        }
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, "  ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_INCLUDE, "include");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_SYS_INCLUDE, "<file.h>");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());

    }

    public void testSysInclude2() {
        doTestSysInclude2(true);
        doTestSysInclude2(false);
    }

    private void doTestUsrIncludeNext(boolean altStartToken) {
        String startText = altStartToken ? "%:" : "#";
        String text = startText + "  include_next \"file.h\"\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        if (altStartToken) {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START_ALT, "%:");
        } else {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        }
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, "  ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_INCLUDE_NEXT, "include_next");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_USER_INCLUDE, "\"file.h\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testUsrIncludeNext() {
        doTestUsrIncludeNext(true);
        doTestUsrIncludeNext(false);
    }

    private void doTestUsrInclude(boolean altStartToken) {
        String startText = altStartToken ? "%:" : "#";
        String text = startText + "  include \"file.h\"\n";

        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        if (altStartToken) {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START_ALT, "%:");
        } else {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        }
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, "  ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_INCLUDE, "include");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_USER_INCLUDE, "\"file.h\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testUsrInclude() {
        doTestUsrInclude(true);
        doTestUsrInclude(false);
    }

    private void doTestUsrIncludeNext2(boolean altStartToken) {
        String startText = altStartToken ? "%:" : "#";
        String text = startText + "  include_next\"file.h\"\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        if (altStartToken) {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START_ALT, "%:");
        } else {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        }
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, "  ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_INCLUDE_NEXT, "include_next");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_USER_INCLUDE, "\"file.h\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testUsrIncludeNext2() {
        doTestUsrIncludeNext2(true);
        doTestUsrIncludeNext2(false);
    }

    private void doTestUsrInclude2(boolean altStartToken) {
        String startText = altStartToken ? "%:" : "#";
        String text = startText + "  include\"file.h\"\n";

        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        if (altStartToken) {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START_ALT, "%:");
        } else {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        }
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, "  ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_INCLUDE, "include");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_USER_INCLUDE, "\"file.h\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testUsrInclude2() {
        doTestUsrInclude2(true);
        doTestUsrInclude2(false);
    }

    private void doTestIncludeMacro(boolean altStartToken) {
        String startText = altStartToken ? "%:" : "#";
        String text = startText + "include AA(x,7)\n";

        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        if (altStartToken) {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START_ALT, "%:");
        } else {
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        }
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_INCLUDE, "include");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "AA");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.COMMA, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT_LITERAL, "7");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testIncludeMacro() {
        doTestIncludeMacro(true);
        doTestIncludeMacro(false);
    }
}
