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

import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * tests for preprocessor directive lexer
 * 
 * @author Vladimir Voskresensky
 */
public class PreprocLexerTestCase extends NbTestCase {

    public PreprocLexerTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        LexerTestUtilities.setTesting(true);
    }
    

    
    public void testIfdef() {
        String text = "#ifdef MACRO\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IFDEF, "ifdef");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "MACRO");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");
        
        assertFalse("No more tokens", ts.moveNext());
    }
    
    public void testIfndef() {
        String text = "# ifndef MACRO\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IFNDEF, "ifndef");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "MACRO");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");
        
        assertFalse("No more tokens", ts.moveNext());
    }    

    public void testIf() {
        String text = "# if defined MACRO\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IF, "if");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_DEFINED, "defined");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "MACRO");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }
    
    public void testElif() {
        String text = "#elif !defined(MACRO)\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
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
 
    public void testElif2() {
        String text = "#elif!defined(MACRO)\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_ELIF, "elif");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NOT, "!");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_DEFINED, "defined");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "MACRO");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }
    
    public void testElse() {
        String text = "#else //comment\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_ELSE, "else");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LINE_COMMENT, "//comment");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }
    
    public void testEndif() {
        String text = "#endif defined// MACRO\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_ENDIF, "endif");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "defined");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LINE_COMMENT, "// MACRO");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());

    }
    
    public void testDefine() {
        String text = "#define MAX(x,y) \\\n (((x)<(y)) ? (y) : (x))\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
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
    
    public void testUndef() {
        String text = "#undef MACRO\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_UNDEF, "undef");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_IDENTIFIER, "MACRO");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }
 
    public void testPragma() {
        String text = "#pragma omp parallel for shared(array, array1, array2, dim) private(ii, jj, kk)\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
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
    
    public void testError() {
        String text = "#error \"message\"  \n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_ERROR, "error");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "\"message\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, "  ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }
    
    public void testLine() {
        String text = "#line 100 //change line\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_LINE, "line");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT_LITERAL, "100");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LINE_COMMENT, "//change line");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }
    
    public void testSysIncludeNext() {
        String text = "#  include_next <file.h>\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, "  ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_INCLUDE_NEXT, "include_next");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_SYS_INCLUDE, "<file.h>");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }
    
    public void testSysInclude() {
        String text = "#  include <file.h>\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, "  ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_INCLUDE, "include");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_SYS_INCLUDE, "<file.h>");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testSysIncludeNext2() {
        String text = "#  include_next<file.h>\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, "  ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_INCLUDE_NEXT, "include_next");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_SYS_INCLUDE, "<file.h>");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }
    
    public void testSysInclude2() {
        String text = "#  include<file.h>\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, "  ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_INCLUDE, "include");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_SYS_INCLUDE, "<file.h>");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());

    }  
    
    public void testUsrIncludeNext() {
        String text = "#  include_next \"file.h\"\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, "  ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_INCLUDE_NEXT, "include_next");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_USER_INCLUDE, "\"file.h\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());    
    }
    
    public void testUsrInclude() {
        String text = "#  include \"file.h\"\n";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, "  ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_INCLUDE, "include");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_USER_INCLUDE, "\"file.h\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }
    
    public void testUsrIncludeNext2() {
        String text = "#  include_next\"file.h\"\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, "  ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_INCLUDE_NEXT, "include_next");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_USER_INCLUDE, "\"file.h\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }
    
    public void testUsrInclude2() {
        String text = "#  include\"file.h\"\n";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, "  ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_INCLUDE, "include");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_USER_INCLUDE, "\"file.h\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }
    
    public void testIncludeMacro() {
        String text = "#include AA(x,7)\n";

        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languagePreproc());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_START, "#");
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
}
