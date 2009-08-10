/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.php.editor.indent;

import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class PHPFormatterTest extends PHPTestBase {

    public PHPFormatterTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        try {
            TestLanguageProvider.register(HTMLTokenId.language());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }
        try {
            TestLanguageProvider.register(PHPTokenId.language());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }
    }
  
     public void testSpacesAfterObjectRefereneces() throws Exception {
        reformatFileContents("testfiles/formatting/real_life/spacesAfterObjectReferences.php");
    }

//      *** These are test cases for known problems, they were never passing ***
      
//    public void testStatementsWithoutSpaces() throws Exception {
//        reformatFileContents("testfiles/formatting/real_life/statementsWithoutSpaces.php");
//    }

//    public void testCommentsInStatements() throws Exception {
//        reformatFileContents("testfiles/formatting/real_life/comments_in_statements.php");
//    }

    public void testIfElseStatement() throws Exception {
        reformatFileContents("testfiles/formatting/real_life/else_if.php");
    }

    public void testContinuedExpression() throws Exception {
        reformatFileContents("testfiles/formatting/continued_expression.php");
    }

    public void testContinuedExpression2() throws Exception {
        reformatFileContents("testfiles/formatting/continued_expression2.php");
    }

    public void testIfelseNobrackets() throws Exception {
        reformatFileContents("testfiles/formatting/ifelse_nobrackets.php");
    }

    public void testMultilineFunctionHeader() throws Exception {
        reformatFileContents("testfiles/formatting/multiline_function_header.php");
    }

    public void testSimpleClassDef() throws Exception {
        reformatFileContents("testfiles/formatting/simple_class_def.php");
    }

    public void testSwitchStmt() throws Exception {
        reformatFileContents("testfiles/formatting/switch_stmt.php");
    }

    public void testArrays1() throws Exception {
        reformatFileContents("testfiles/formatting/arrays1.php");
    }

    public void testArrays2() throws Exception {
        reformatFileContents("testfiles/formatting/arrays2.php");
    }

    public void testSubsequentQuotes() throws Exception {
        reformatFileContents("testfiles/formatting/subsequentquotes.php");
    }

    public void testMultilineString() throws Exception {
        reformatFileContents("testfiles/formatting/multiline_string.php");
    }

    public void testIfElseAlternativeSyntax() throws Exception {
        reformatFileContents("testfiles/formatting/ifelse_alternative_syntax.php");
    }

    public void testNamespaces1() throws Exception {
        reformatFileContents("testfiles/formatting/namespaces1.php");
    }

    public void test161049() throws Exception {
        reformatFileContents("testfiles/formatting/issue161049.php");
    }

    public void test162126() throws Exception {
        reformatFileContents("testfiles/formatting/issue162126.php");
    }

    public void test162785() throws Exception {
        reformatFileContents("testfiles/formatting/issue162785.php");
    }
    
    public void test159339_161408() throws Exception {
        reformatFileContents("testfiles/formatting/issues_159339_161408.php");
    }

    public void test164219() throws Exception {
        reformatFileContents("testfiles/formatting/issue164219.php");
    }

    public void test162320() throws Exception {
        reformatFileContents("testfiles/formatting/issue162320.php");
    }

    private void reformatFileContents(String file) throws Exception {
        reformatFileContents(file, new IndentPrefs(2, 2));
    }
}
