/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.php.editor.indent;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.html.parser.api.HtmlVersion;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class PHPFormatterTest extends PHPTestBase {
    private String FORMAT_START_MARK = "/*FORMAT_START*/"; //NOI18N
    private String FORMAT_END_MARK = "/*FORMAT_END*/"; //NOI18N

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
        HtmlVersion.DEFAULT_VERSION_UNIT_TESTS_OVERRIDE = HtmlVersion.HTML41_TRANSATIONAL;
    }
    
      public void test174595() throws Exception {
        reformatFileContents("testfiles/formatting/issue174595.php");
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

    public void testLineSplitting1() throws Exception {
        reformatFileContents("testfiles/formatting/line_splitting1.php");
    }

    public void testLineSplitting2() throws Exception {
        reformatFileContents("testfiles/formatting/line_splitting2.php");
    }

    public void testHereDoc() throws Exception {
        reformatFileContents("testfiles/formatting/heredoc.php");
    }

    public void testSimpleClassDef() throws Exception {
        reformatFileContents("testfiles/formatting/simple_class_def.php");
    }

    public void testSwitchStmt() throws Exception {
        reformatFileContents("testfiles/formatting/switch_stmt.php");
    }

    public void testSwitchStmt01() throws Exception {
        reformatFileContents("testfiles/formatting/switch_stmt01.php");
    }
    
    public void testArrays1() throws Exception {
        reformatFileContents("testfiles/formatting/arrays1.php");
    }

    public void testArrays2() throws Exception {
        reformatFileContents("testfiles/formatting/arrays2.php");
    }

    public void testArrays3() throws Exception {
        reformatFileContents("testfiles/formatting/arrays3.php");
    }

    public void testArrays4() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.initialIndent, 4);
        options.put(FmtOptions.itemsInArrayDeclarationIndentSize, 6);
        reformatFileContents("testfiles/formatting/arrays4.php", options);
    }

    public void testArrays05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/arrays5.php", options);
    }
    
    public void testFragment1() throws Exception {
        reformatFileContents("testfiles/formatting/format_fragment1.php");
    }

    public void testNestedArrays1() throws Exception {
        reformatFileContents("testfiles/formatting/nested_array1.php");
    }

    public void testSubsequentQuotes() throws Exception {
        reformatFileContents("testfiles/formatting/subsequentquotes.php");
    }

    public void testMultilineString() throws Exception {
        reformatFileContents("testfiles/formatting/multiline_string.php");
    }

    public void testInitialIndent1() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.initialIndent, 5);
        reformatFileContents("testfiles/formatting/initial_indent1.php", options);
    }

   public void testInitialIndent01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/initialIndent01.php", options);
    }

    public void testIfElseAlternativeSyntax() throws Exception {
        reformatFileContents("testfiles/formatting/ifelse_alternative_syntax.php");
    }

    public void testNamespaces1() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/namespaces1.php", options);
    }

    public void testNamespaces02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/namespaces_02.php", options);
    }

    public void testNamespaces03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.ifBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.forBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.whileBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.switchBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.catchBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/namespaces_03.php", options);
    }

    public void testNamespaces04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
	options.put(FmtOptions.ifBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
	options.put(FmtOptions.forBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
	options.put(FmtOptions.whileBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
	options.put(FmtOptions.switchBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
	options.put(FmtOptions.catchBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        reformatFileContents("testfiles/formatting/namespaces_04.php", options);
    }

    public void testNamespaces05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/namespaces_05.php", options);
    }

    public void test161049() throws Exception {
        reformatFileContents("testfiles/formatting/issue161049.php");
    }

    public void test172259() throws Exception {
        reformatFileContents("testfiles/formatting/issue172259.php");
    }
    public void test171309() throws Exception {
        reformatFileContents("testfiles/formatting/issue171309.php");
    }

    public void test162126() throws Exception {
        reformatFileContents("testfiles/formatting/issue162126.php");
    }

    public void test162785() throws Exception {
        reformatFileContents("testfiles/formatting/issue162785.php");
    }

    public void test162586() throws Exception {
        reformatFileContents("testfiles/formatting/issue162586.php");
    }

    public void test176453() throws Exception {
        reformatFileContents("testfiles/formatting/issue176453.php");
    }

    public void test165762() throws Exception {
        reformatFileContents("testfiles/formatting/issue165762.php");
    }

    public void test166550() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue166550.php", options);
    }
    
    public void test159339_161408() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issues_159339_161408.php", options);
    }

    public void test164219() throws Exception {
        reformatFileContents("testfiles/formatting/issue164219.php");
    }

    public void test162320() throws Exception {
        reformatFileContents("testfiles/formatting/issue162320.php");
    }

    public void test173906_dowhile() throws Exception {
        reformatFileContents("testfiles/formatting/issue173906_dowhile.php");
    }

    public void test164381() throws Exception {
        reformatFileContents("testfiles/formatting/issue164381.php");
    }

    public void test174544() throws Exception {
        reformatFileContents("testfiles/formatting/issue174544.php");
    }

    public void test174563() throws Exception {
        reformatFileContents("testfiles/formatting/issue174563.php");
    }

    public void test172475() throws Exception {
        reformatFileContents("testfiles/formatting/issue172475.php");
    }

    public void test167791() throws Exception {
        reformatFileContents("testfiles/formatting/issue167791.php", 5);
    }

    public void test176224() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue176224.php", options);
    }

    public void testBracePlacement01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.ifBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.forBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.whileBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.switchBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.catchBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/blankLines/BracePlacement01.php", options);
    }

    public void testBracePlacement02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
	options.put(FmtOptions.ifBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
	options.put(FmtOptions.forBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
	options.put(FmtOptions.whileBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
	options.put(FmtOptions.switchBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
	options.put(FmtOptions.catchBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        reformatFileContents("testfiles/formatting/blankLines/BracePlacement02.php", options);
    }

    public void testBracePlacement03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
	options.put(FmtOptions.ifBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
	options.put(FmtOptions.forBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
	options.put(FmtOptions.whileBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
	options.put(FmtOptions.switchBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
	options.put(FmtOptions.catchBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/blankLines/BracePlacement03.php", options);
    }

    public void testAlternativeSyntaxPlacement01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/AlternativeSyntaxPlacement01.php", options);
    }

    // blank lines
    public void testBLClass01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Class01.php", options);
    }

    public void testBLClass02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
	options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.spaceBeforeClassDeclLeftBrace, false);
        reformatFileContents("testfiles/formatting/blankLines/Class02.php", options);
    }

    public void testBLClass03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
	options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
	options.put(FmtOptions.spaceBeforeClassDeclLeftBrace, false);
        reformatFileContents("testfiles/formatting/blankLines/Class03.php", options);
    }

    public void testBLFields01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceBeforeClassDeclLeftBrace, true);
	options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Fields01.php", options);
    }

    public void testBLFields02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Fields02.php", options);
    }

    public void testBLFields03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Fields03.php", options);
    }

    public void testBLFields04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Fields04.php", options);
    }

    public void testBLFields05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Fields05.php", options);
    }

    public void testBLFields06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Fields06.php", options);
    }

    public void testBLFunction01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Function01.php", options);
    }

    public void testBLFunction02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Function02.php", options);
    }

    public void testBLFunction04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Function04.php", options);
    }

    public void testBLNamespace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Namespace01.php", options);
    }

    public void testBLNamespace02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Namespace02.php", options);
    }

    public void testBLNamespace03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Namespace03.php", options);
    }

    public void testBLSimpleClass01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass01.php", options);
    }

    public void testBLSimpleClass02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass02.php", options);
    }

    public void testBLSimpleClass03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass03.php", options);
    }

    public void testBLSimpleClass04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass04.php", options);
    }

    public void testBLSimpleClass05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
	options.put(FmtOptions.blankLinesBeforeClass, 1);
	options.put(FmtOptions.blankLinesAfterClassHeader, 0);
	options.put(FmtOptions.blankLinesBeforeClassEnd, 0);
	options.put(FmtOptions.blankLinesAfterClass, 0);
	options.put(FmtOptions.blankLinesBeforeFunction, 0);
	options.put(FmtOptions.blankLinesBeforeFunctionEnd, 0);
	options.put(FmtOptions.blankLinesAfterFunction, 0);
	options.put(FmtOptions.blankLinesBeforeField, 0);
	options.put(FmtOptions.blankLinesAfterField, 0);
	options.put(FmtOptions.blankLinesBeforeNamespace, 0);
	options.put(FmtOptions.blankLinesAfterNamespace, 0);
	options.put(FmtOptions.blankLinesBeforeUse, 0);
	options.put(FmtOptions.blankLinesAfterUse, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass05.php", options);
    }

    public void testBLSimpleClass06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
	options.put(FmtOptions.blankLinesBeforeClass, 0);
	options.put(FmtOptions.blankLinesAfterClassHeader, 1);
	options.put(FmtOptions.blankLinesBeforeClassEnd, 0);
	options.put(FmtOptions.blankLinesAfterClass, 0);
	options.put(FmtOptions.blankLinesBeforeFunction, 0);
	options.put(FmtOptions.blankLinesBeforeFunctionEnd, 0);
	options.put(FmtOptions.blankLinesAfterFunction, 0);
	options.put(FmtOptions.blankLinesBeforeField, 0);
	options.put(FmtOptions.blankLinesAfterField, 0);
	options.put(FmtOptions.blankLinesBeforeNamespace, 0);
	options.put(FmtOptions.blankLinesAfterNamespace, 0);
	options.put(FmtOptions.blankLinesBeforeUse, 0);
	options.put(FmtOptions.blankLinesAfterUse, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass06.php", options);
    }

    public void testBLSimpleClass07() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
	options.put(FmtOptions.blankLinesBeforeClass, 0);
	options.put(FmtOptions.blankLinesAfterClassHeader, 0);
	options.put(FmtOptions.blankLinesBeforeClassEnd, 1);
	options.put(FmtOptions.blankLinesAfterClass, 0);
	options.put(FmtOptions.blankLinesBeforeFunction, 0);
	options.put(FmtOptions.blankLinesBeforeFunctionEnd, 0);
	options.put(FmtOptions.blankLinesAfterFunction, 0);
	options.put(FmtOptions.blankLinesBeforeField, 0);
	options.put(FmtOptions.blankLinesAfterField, 0);
	options.put(FmtOptions.blankLinesBeforeNamespace, 0);
	options.put(FmtOptions.blankLinesAfterNamespace, 0);
	options.put(FmtOptions.blankLinesBeforeUse, 0);
	options.put(FmtOptions.blankLinesAfterUse, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass07.php", options);
    }

    public void testBLSimpleClass08() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
	options.put(FmtOptions.blankLinesBeforeClass, 0);
	options.put(FmtOptions.blankLinesAfterClassHeader, 0);
	options.put(FmtOptions.blankLinesBeforeClassEnd, 0);
	options.put(FmtOptions.blankLinesAfterClass, 1);
	options.put(FmtOptions.blankLinesBeforeFunction, 0);
	options.put(FmtOptions.blankLinesBeforeFunctionEnd, 0);
	options.put(FmtOptions.blankLinesAfterFunction, 0);
	options.put(FmtOptions.blankLinesBeforeField, 0);
	options.put(FmtOptions.blankLinesAfterField, 0);
	options.put(FmtOptions.blankLinesBeforeNamespace, 0);
	options.put(FmtOptions.blankLinesAfterNamespace, 0);
	options.put(FmtOptions.blankLinesBeforeUse, 0);
	options.put(FmtOptions.blankLinesAfterUse, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass08.php", options);
    }

    public void testBLSimpleClass09() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
	options.put(FmtOptions.blankLinesBeforeClass, 0);
	options.put(FmtOptions.blankLinesAfterClassHeader, 0);
	options.put(FmtOptions.blankLinesBeforeClassEnd, 0);
	options.put(FmtOptions.blankLinesAfterClass, 0);
	options.put(FmtOptions.blankLinesBeforeFunction, 1);
	options.put(FmtOptions.blankLinesBeforeFunctionEnd, 0);
	options.put(FmtOptions.blankLinesAfterFunction, 0);
	options.put(FmtOptions.blankLinesBeforeField, 0);
	options.put(FmtOptions.blankLinesAfterField, 0);
	options.put(FmtOptions.blankLinesBeforeNamespace, 0);
	options.put(FmtOptions.blankLinesAfterNamespace, 0);
	options.put(FmtOptions.blankLinesBeforeUse, 0);
	options.put(FmtOptions.blankLinesAfterUse, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass09.php", options);
    }

    public void testBLSimpleClass10() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
	options.put(FmtOptions.blankLinesBeforeClass, 0);
	options.put(FmtOptions.blankLinesAfterClassHeader, 0);
	options.put(FmtOptions.blankLinesBeforeClassEnd, 0);
	options.put(FmtOptions.blankLinesAfterClass, 0);
	options.put(FmtOptions.blankLinesBeforeFunction, 0);
	options.put(FmtOptions.blankLinesBeforeFunctionEnd, 1);
	options.put(FmtOptions.blankLinesAfterFunction, 0);
	options.put(FmtOptions.blankLinesBeforeField, 0);
	options.put(FmtOptions.blankLinesAfterField, 0);
	options.put(FmtOptions.blankLinesBeforeNamespace, 0);
	options.put(FmtOptions.blankLinesAfterNamespace, 0);
	options.put(FmtOptions.blankLinesBeforeUse, 0);
	options.put(FmtOptions.blankLinesAfterUse, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass10.php", options);
    }

    public void testBLSimpleClass11() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
	options.put(FmtOptions.blankLinesBeforeClass, 0);
	options.put(FmtOptions.blankLinesAfterClassHeader, 0);
	options.put(FmtOptions.blankLinesBeforeClassEnd, 0);
	options.put(FmtOptions.blankLinesAfterClass, 0);
	options.put(FmtOptions.blankLinesBeforeFunction, 0);
	options.put(FmtOptions.blankLinesBeforeFunctionEnd, 0);
	options.put(FmtOptions.blankLinesAfterFunction, 1);
	options.put(FmtOptions.blankLinesBeforeField, 0);
	options.put(FmtOptions.blankLinesAfterField, 0);
	options.put(FmtOptions.blankLinesBeforeNamespace, 0);
	options.put(FmtOptions.blankLinesAfterNamespace, 0);
	options.put(FmtOptions.blankLinesBeforeUse, 0);
	options.put(FmtOptions.blankLinesAfterUse, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass11.php", options);
    }

    public void testBLSimpleClass12() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
	options.put(FmtOptions.blankLinesBeforeClass, 0);
	options.put(FmtOptions.blankLinesAfterClassHeader, 1);
	options.put(FmtOptions.blankLinesBeforeClassEnd, 1);
	options.put(FmtOptions.blankLinesAfterClass, 0);
	options.put(FmtOptions.blankLinesBeforeFunction, 1);
	options.put(FmtOptions.blankLinesBeforeFunctionEnd, 1);
	options.put(FmtOptions.blankLinesAfterFunction, 1);
	options.put(FmtOptions.blankLinesBeforeField, 0);
	options.put(FmtOptions.blankLinesAfterField, 0);
	options.put(FmtOptions.blankLinesBeforeNamespace, 0);
	options.put(FmtOptions.blankLinesAfterNamespace, 0);
	options.put(FmtOptions.blankLinesBeforeUse, 0);
	options.put(FmtOptions.blankLinesAfterUse, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass12.php", options);
    }

    public void testBLSimpleClass13() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass13.php", options);
    }

    public void testBLSimpleClass14() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass14.php", options);
    }

    public void testBLSimpleClass15() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass15.php", options);
    }

    public void testBLSimpleClass16() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass16.php", options);
    }

    public void testBLSimpleClass17() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 4);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass17.php", options);
    }

    public void testBLSimpleUse01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Use01.php", options);
    }

    public void testBLSimpleUse02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Use02.php", options);
    }

    public void testBLSimpleUse03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Use03.php", options);
    }

    public void testBLSimpleUse04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Use04.php", options);
    }

    public void testOpenClosePHPTag01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/OpenClosePHPTag01.php", options);
    }

    public void testOpenClosePHPTag02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/OpenClosePHPTag02.php", options);
    }

    public void testOpenClosePHPTag03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.initialIndent, 4);
        reformatFileContents("testfiles/formatting/blankLines/OpenClosePHPTag03.php", options);
    }

    public void testOpenClosePHPTag04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.initialIndent, 4);
        reformatFileContents("testfiles/formatting/blankLines/OpenClosePHPTag04.php", options);
    }

    public void testOpenClosePHPTag05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.initialIndent, 4);
        reformatFileContents("testfiles/formatting/blankLines/OpenClosePHPTag05.php", options);
    }

    public void testSpacesBeforeClassDecLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeClassDeclLeftBrace, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeClassDecLeftBrace01.php", options);
    }

    public void testSpacesBeforeClassDecLeftBrace02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeClassDeclLeftBrace, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeClassDecLeftBrace02.php", options);
    }

    public void testSpacesBeforeClassDecLeftBrace03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeClassDeclLeftBrace, new Boolean(false));
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeClassDecLeftBrace03.php", options);
    }

    public void testSpacesBeforeMethodDeclLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeMethodDeclLeftBrace, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodDeclLeftBrace01.php", options);
    }

    public void testSpacesBeforeMethodDeclLeftBrace02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeMethodDeclLeftBrace, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodDeclLeftBrace02.php", options);
    }

    public void testSpacesBeforeMethodDeclLeftBrace03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeMethodDeclLeftBrace, true);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodDeclLeftBrace03.php", options);
    }

    public void testSpacesBeforeIfElseIfLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeClassDeclLeftBrace, false);
        options.put(FmtOptions.spaceBeforeMethodDeclLeftBrace, false);
        options.put(FmtOptions.spaceBeforeIfLeftBrace, true);
        options.put(FmtOptions.spaceBeforeElseLeftBrace, false);
        options.put(FmtOptions.spaceBeforeWhileLeftBrace, false);
        options.put(FmtOptions.spaceBeforeDoLeftBrace, false);
        options.put(FmtOptions.spaceBeforeForLeftBrace, false);
        options.put(FmtOptions.spaceBeforeSwitchLeftBrace, false);
        options.put(FmtOptions.spaceBeforeTryLeftBrace, false);
        options.put(FmtOptions.spaceBeforeCatchLeftBrace, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeIfElseIfLeftBrace01.php", options);
    }

    public void testSpacesBeforeElseLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeClassDeclLeftBrace, false);
        options.put(FmtOptions.spaceBeforeMethodDeclLeftBrace, false);
        options.put(FmtOptions.spaceBeforeIfLeftBrace, false);
        options.put(FmtOptions.spaceBeforeElseLeftBrace, true);
        options.put(FmtOptions.spaceBeforeWhileLeftBrace, false);
        options.put(FmtOptions.spaceBeforeDoLeftBrace, false);
        options.put(FmtOptions.spaceBeforeForLeftBrace, false);
        options.put(FmtOptions.spaceBeforeSwitchLeftBrace, false);
        options.put(FmtOptions.spaceBeforeTryLeftBrace, false);
        options.put(FmtOptions.spaceBeforeCatchLeftBrace, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeElseLeftBrace01.php", options);
    }

    public void testSpacesBeforeWhileLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeClassDeclLeftBrace, false);
        options.put(FmtOptions.spaceBeforeMethodDeclLeftBrace, false);
        options.put(FmtOptions.spaceBeforeIfLeftBrace, false);
        options.put(FmtOptions.spaceBeforeElseLeftBrace, false);
        options.put(FmtOptions.spaceBeforeWhileLeftBrace, true);
        options.put(FmtOptions.spaceBeforeDoLeftBrace, false);
        options.put(FmtOptions.spaceBeforeForLeftBrace, false);
        options.put(FmtOptions.spaceBeforeSwitchLeftBrace, false);
        options.put(FmtOptions.spaceBeforeTryLeftBrace, false);
        options.put(FmtOptions.spaceBeforeCatchLeftBrace, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeWhileLeftBrace01.php", options);
    }

    public void testSpacesBeforeDoLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeClassDeclLeftBrace, false);
        options.put(FmtOptions.spaceBeforeMethodDeclLeftBrace, false);
        options.put(FmtOptions.spaceBeforeIfLeftBrace, false);
        options.put(FmtOptions.spaceBeforeElseLeftBrace, false);
        options.put(FmtOptions.spaceBeforeWhileLeftBrace, false);
        options.put(FmtOptions.spaceBeforeDoLeftBrace, true);
        options.put(FmtOptions.spaceBeforeForLeftBrace, false);
        options.put(FmtOptions.spaceBeforeSwitchLeftBrace, false);
        options.put(FmtOptions.spaceBeforeTryLeftBrace, false);
        options.put(FmtOptions.spaceBeforeCatchLeftBrace, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeDoLeftBrace01.php", options);
    }

    public void testSpacesBeforeForLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeClassDeclLeftBrace, false);
        options.put(FmtOptions.spaceBeforeMethodDeclLeftBrace, false);
        options.put(FmtOptions.spaceBeforeIfLeftBrace, false);
        options.put(FmtOptions.spaceBeforeElseLeftBrace, false);
        options.put(FmtOptions.spaceBeforeWhileLeftBrace, false);
        options.put(FmtOptions.spaceBeforeDoLeftBrace, false);
        options.put(FmtOptions.spaceBeforeForLeftBrace, true);
        options.put(FmtOptions.spaceBeforeSwitchLeftBrace, false);
        options.put(FmtOptions.spaceBeforeTryLeftBrace, false);
        options.put(FmtOptions.spaceBeforeCatchLeftBrace, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeForLeftBrace01.php", options);
    }

    public void testSpacesBeforeSwitchLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeClassDeclLeftBrace, false);
        options.put(FmtOptions.spaceBeforeMethodDeclLeftBrace, false);
        options.put(FmtOptions.spaceBeforeIfLeftBrace, false);
        options.put(FmtOptions.spaceBeforeElseLeftBrace, false);
        options.put(FmtOptions.spaceBeforeWhileLeftBrace, false);
        options.put(FmtOptions.spaceBeforeDoLeftBrace, false);
        options.put(FmtOptions.spaceBeforeForLeftBrace, false);
        options.put(FmtOptions.spaceBeforeSwitchLeftBrace, true);
        options.put(FmtOptions.spaceBeforeTryLeftBrace, false);
        options.put(FmtOptions.spaceBeforeCatchLeftBrace, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeSwitchLeftBrace01.php", options);
    }

    public void testSpacesBeforeTryLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeClassDeclLeftBrace, false);
        options.put(FmtOptions.spaceBeforeMethodDeclLeftBrace, false);
        options.put(FmtOptions.spaceBeforeIfLeftBrace, false);
        options.put(FmtOptions.spaceBeforeElseLeftBrace, false);
        options.put(FmtOptions.spaceBeforeWhileLeftBrace, false);
        options.put(FmtOptions.spaceBeforeDoLeftBrace, false);
        options.put(FmtOptions.spaceBeforeForLeftBrace, false);
        options.put(FmtOptions.spaceBeforeSwitchLeftBrace, false);
        options.put(FmtOptions.spaceBeforeTryLeftBrace, true);
        options.put(FmtOptions.spaceBeforeCatchLeftBrace, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeTryLeftBrace01.php", options);
    }

    public void testSpacesBeforeCatchLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeClassDeclLeftBrace, false);
        options.put(FmtOptions.spaceBeforeMethodDeclLeftBrace, false);
        options.put(FmtOptions.spaceBeforeIfLeftBrace, false);
        options.put(FmtOptions.spaceBeforeElseLeftBrace, false);
        options.put(FmtOptions.spaceBeforeWhileLeftBrace, false);
        options.put(FmtOptions.spaceBeforeDoLeftBrace, false);
        options.put(FmtOptions.spaceBeforeForLeftBrace, false);
        options.put(FmtOptions.spaceBeforeSwitchLeftBrace, false);
        options.put(FmtOptions.spaceBeforeTryLeftBrace, false);
        options.put(FmtOptions.spaceBeforeCatchLeftBrace, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeCatchLeftBrace01.php", options);
    }

    public void testSpacesBeforeWhile01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeWhile, true);
	options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeWhile01.php", options);
    }

    public void testSpacesBeforeWhile02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeWhile, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeWhile02.php", options);
    }

    public void testSpacesBeforeWhile03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeWhile, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeWhile03.php", options);
    }

    public void testSpacesBeforeElse01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeElse, true);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeElse01.php", options);
    }

    public void testSpacesBeforeElse02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeElse, true);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeElse02.php", options);
    }

    public void testSpacesBeforeElse03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeElse, false);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeElse03.php", options);
    }

    public void testSpacesBeforeElse04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeElse, true);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeElse04.php", options);
    }

    public void testSpacesBeforeCatch01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeCatch, true);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeCatch01.php", options);
    }

    public void testSpacesBeforeCatch02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeCatch, false);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeCatch02.php", options);
    }

    public void testSpacesBeforeMethodCallParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeMethodCallParen, true);
	options.put(FmtOptions.spaceBeforeClassDeclLeftBrace, true);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodCallParen01.php", options);
    }

    public void testSpacesBeforeMethodCallParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeMethodCallParen, false);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodCallParen02.php", options);
    }

    public void testSpacesBeforeMethodDeclParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeMethodDeclParen, true);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodDeclParen01.php", options);
    }

    public void testSpacesBeforeMethodDeclParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeMethodDeclParen, false);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodDeclParen02.php", options);
    }

    public void testSpacesBeforeIfParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeIfParen, true);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeIfParen01.php", options);
    }

    public void testSpacesBeforeIfParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeIfParen, false);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeIfParen02.php", options);
    }

    public void testSpacesBeforeForParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeForParen, true);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeForParen01.php", options);
    }

    public void testSpacesBeforeForParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeForParen, false);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeForParen02.php", options);
    }

    public void testSpacesBeforeWhileParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeWhileParen, true);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeWhileParen01.php", options);
    }

    public void testSpacesBeforeWhileParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeWhileParen, false);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeWhileParen02.php", options);
    }

    public void testSpacesBeforeCatchParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeCatchParen, true);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeCatchParen01.php", options);
    }

    public void testSpacesBeforeCatchParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeCatchParen, false);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeCatchParen02.php", options);
    }

    public void testSpacesBeforeSwitchParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeSwitchParen, true);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeSwitchParen01.php", options);
    }

    public void testSpacesBeforeSwitchParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeSwitchParen, false);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeSwitchParen02.php", options);
    }

    public void testSpacesAroundStringConcat01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeSwitchParen, true);
	options.put(FmtOptions.spaceAroundStringConcatOps, false);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundStringConcat01.php", options);
    }

    public void testSpacesAroundTernaryOp01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceAroundTernaryOps, false);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundTernaryOp01.php", options);
    }

    public void testSpacesAroundTernaryOp02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceAroundTernaryOps, false);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundTernaryOp02.php", options);
    }

    public void testSpacesAroundTernaryOp03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceAroundTernaryOps, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundTernaryOp03.php", options);
    }

    public void testSpacesAroundKeyValue01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceAroundKeyValueOps, false);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundKeyValueOp01.php", options);
    }

    public void testSpacesAroundKeyValue02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceAroundKeyValueOps, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundKeyValueOp02.php", options);
    }

    public void testSpacesWithinIfParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinIfParens, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens01.php", options);
    }

    public void testSpacesWithinForParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinForParens, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens02.php", options);
    }

    public void testSpacesWithinWhileParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinWhileParens, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens03.php", options);
    }

    public void testSpacesWithinSwitchParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinSwitchParens, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens04.php", options);
    }

    public void testSpacesWithinCatchParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinCatchParens, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens05.php", options);
    }

    public void testSpacesWithinParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens06.php", options);
    }

    public void testSpacesWithinMethodDeclParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinMethodDeclParens, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens07.php", options);
    }

    public void testSpacesWithinMethodCallParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinMethodCallParens, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens08.php", options);
    }

    public void testSpacesWithinMethodDeclParens02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinMethodDeclParens, true);
        options.put(FmtOptions.spaceWithinMethodCallParens, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinMethodDecl01.php", options);
    }

    public void testSpacesWithinMethodDeclParens03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinMethodDeclParens, false);
        options.put(FmtOptions.spaceWithinMethodCallParens, false);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinMethodDecl02.php", options);
    }

    public void testSpacesWithinTypeCastParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinTypeCastParens, false);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinTypeCastParens01.php", options);
    }

    public void testSpacesWithinTypeCastParens02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinTypeCastParens, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinTypeCastParens02.php", options);
    }

    public void testSpacesWithinArrayDeclParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinArrayDeclParens, false);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinArrayDeclParens01.php", options);
    }

    public void testSpacesWithinArrayDeclParens02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinArrayDeclParens, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinArrayDeclParens02.php", options);
    }

    public void testSpacesWithinArrayBrackets01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinArrayBrackets, false);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinArrayBrackets01.php", options);
    }

    public void testSpacesWithinArrayBrackets02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinArrayBrackets, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinArrayBrackets02.php", options);
    }

    public void testSpacesWithinArrayBrackets03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinArrayBrackets, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinArrayBrackets03.php", options);
    }

    public void testSpacesWithinArrayBrackets04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinArrayBrackets, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinArrayBrackets04.php", options);
    }
    
    public void testSpacesAfterTypeCast01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceAfterTypeCast, false);
        reformatFileContents("testfiles/formatting/spaces/spaceAfterTypeCast01.php", options);
    }

    public void testSpacesAfterTypeCast02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceAfterTypeCast, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAfterTypeCast02.php", options);
    }

    public void testSpacesBeforeAfterComma01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeComma, false);
	options.put(FmtOptions.spaceAfterComma, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterComma01.php", options);
    }

    public void testSpacesBeforeAfterComma02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeComma, false);
	options.put(FmtOptions.spaceAfterComma, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterComma02.php", options);
    }

    public void testSpacesBeforeAfterComma03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeComma, true);
	options.put(FmtOptions.spaceAfterComma, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterComma03.php", options);
    }

    public void testSpacesBeforeAfterComma04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeComma, true);
	options.put(FmtOptions.spaceAfterComma, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterComma04.php", options);
    }

    public void testSpacesBeforeUnaryOps01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundUnaryOps01.php", options);
    }

    public void testSpacesBeforeUnaryOps02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceWithinIfParens, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundUnaryOps02.php", options);
    }

    public void testSpacesBeforeUnaryOps03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceWithinIfParens, true);
	options.put(FmtOptions.spaceAroundUnaryOps, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundUnaryOps03.php", options);
    }

    public void testSpacesBeforeKeywords01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceBeforeElse, true);
	options.put(FmtOptions.spaceBeforeWhile, true);
	options.put(FmtOptions.spaceBeforeCatch, true);
	options.put(FmtOptions.placeElseOnNewLine, false);
	options.put(FmtOptions.placeWhileOnNewLine, false);
	options.put(FmtOptions.placeCatchOnNewLine, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeKeywords01.php", options);
    }

    public void testSpacesBeforeKeywords02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceBeforeElse, true);
	options.put(FmtOptions.spaceBeforeWhile, true);
	options.put(FmtOptions.spaceBeforeCatch, true);
	options.put(FmtOptions.placeElseOnNewLine, true);
	options.put(FmtOptions.placeWhileOnNewLine, true);
	options.put(FmtOptions.placeCatchOnNewLine, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeKeywords02.php", options);
    }

    public void testSpacesBeforeKeywords03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceBeforeElse, false);
	options.put(FmtOptions.spaceBeforeWhile, false);
	options.put(FmtOptions.spaceBeforeCatch, false);
	options.put(FmtOptions.placeElseOnNewLine, false);
	options.put(FmtOptions.placeWhileOnNewLine, false);
	options.put(FmtOptions.placeCatchOnNewLine, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeKeywords03.php", options);
    }

    public void testSpacesBeforeKeywords04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceBeforeElse, true);
	options.put(FmtOptions.spaceBeforeWhile, true);
	options.put(FmtOptions.spaceBeforeCatch, true);
	options.put(FmtOptions.placeElseOnNewLine, false);
	options.put(FmtOptions.placeWhileOnNewLine, false);
	options.put(FmtOptions.placeCatchOnNewLine, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeKeywords04.php", options);
    }

    public void testIssue180859_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceAroundBinaryOps, true);
        reformatFileContents("testfiles/formatting/spaces/issue180859_01.php", options);
    }

    public void testIssue180859_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceAroundBinaryOps, false);
        reformatFileContents("testfiles/formatting/spaces/issue180859_02.php", options);
    }

    public void testSpacesBeforeAfterSemi01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeSemi, false);
	options.put(FmtOptions.spaceAfterSemi, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterSemi01.php", options);
    }

    public void testSpacesBeforeAfterSemi02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeSemi, false);
	options.put(FmtOptions.spaceAfterSemi, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterSemi02.php", options);
    }

    public void testSpacesBeforeAfterSemi03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeSemi, true);
	options.put(FmtOptions.spaceAfterSemi, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterSemi03.php", options);
    }

    public void testSpacesBeforeAfterSemi04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeSemi, true);
	options.put(FmtOptions.spaceAfterSemi, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterSemi04.php", options);
    }

    public void xxxtestSpacesCheckAfterKeywords01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceCheckAfterKeywords, true);
        reformatFileContents("testfiles/formatting/spaces/spaceCheckAfterKeywords01.php", options);
    }

    public void xxxtestSpacesCheckAfterKeywords02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceCheckAfterKeywords, true);
        reformatFileContents("testfiles/formatting/spaces/spaceCheckAfterKeywords02.php", options);
    }

    public void testIssue181003_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/issue181003_01.php", options);
    }

    public void testIssue181003_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issue181003_02.php", options);
    }

    public void testIssue181003_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.blankLinesAfterClassHeader, 0);
	options.put(FmtOptions.blankLinesBeforeClassEnd, 0);
	options.put(FmtOptions.blankLinesBeforeFunction, 0);
	options.put(FmtOptions.blankLinesAfterFunction, 0);
	options.put(FmtOptions.blankLinesBeforeFunctionEnd, 0);
        reformatFileContents("testfiles/formatting/blankLines/issue181003_03.php", options);
    }

    public void testIssue181003_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.blankLinesAfterClassHeader, 0);
	options.put(FmtOptions.blankLinesBeforeClassEnd, 0);
	options.put(FmtOptions.blankLinesBeforeFunction, 0);
	options.put(FmtOptions.blankLinesAfterFunction, 0);
	options.put(FmtOptions.blankLinesBeforeFunctionEnd, 0);
        reformatFileContents("testfiles/formatting/blankLines/issue181003_04.php", options);
    }

    public void testAlignmentKeywords01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.placeWhileOnNewLine, true);
	options.put(FmtOptions.placeElseOnNewLine, true);
	options.put(FmtOptions.placeCatchOnNewLine, true);
	options.put(FmtOptions.placeNewLineAfterModifiers, true);
        reformatFileContents("testfiles/formatting/alignment/alignmentKeywords01.php", options);
    }

    public void testAlignmentKeywords02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.placeWhileOnNewLine, true);
	options.put(FmtOptions.placeElseOnNewLine, true);
	options.put(FmtOptions.placeCatchOnNewLine, true);
	options.put(FmtOptions.placeNewLineAfterModifiers, true);

	options.put(FmtOptions.spaceBeforeElse, false);
	options.put(FmtOptions.spaceBeforeCatch, false);
	options.put(FmtOptions.spaceBeforeWhile, false);
        reformatFileContents("testfiles/formatting/alignment/alignmentKeywords02.php", options);
    }

    public void testAlignmentKeywords03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.placeWhileOnNewLine, true);
	options.put(FmtOptions.placeElseOnNewLine, true);
	options.put(FmtOptions.placeCatchOnNewLine, true);
	options.put(FmtOptions.placeNewLineAfterModifiers, true);

	options.put(FmtOptions.spaceBeforeElse, false);
	options.put(FmtOptions.spaceBeforeCatch, false);
	options.put(FmtOptions.spaceBeforeWhile, false);

	options.put(FmtOptions.classDeclBracePlacement, FmtOptions.OBRACE_NEWLINE);
	options.put(FmtOptions.methodDeclBracePlacement, FmtOptions.OBRACE_NEWLINE);
	options.put(FmtOptions.ifBracePlacement, FmtOptions.OBRACE_NEWLINE);
	options.put(FmtOptions.whileBracePlacement, FmtOptions.OBRACE_NEWLINE);
	options.put(FmtOptions.catchBracePlacement, FmtOptions.OBRACE_NEWLINE);
	options.put(FmtOptions.otherBracePlacement, FmtOptions.OBRACE_NEWLINE);

        reformatFileContents("testfiles/formatting/alignment/alignmentKeywords03.php", options);
    }

    public void testAlignmentKeywords04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.placeWhileOnNewLine, false);
	options.put(FmtOptions.placeElseOnNewLine, false);
	options.put(FmtOptions.placeCatchOnNewLine, false);
	options.put(FmtOptions.placeNewLineAfterModifiers, false);

	options.put(FmtOptions.spaceBeforeElse, false);
	options.put(FmtOptions.spaceBeforeCatch, false);
	options.put(FmtOptions.spaceBeforeWhile, false);

	options.put(FmtOptions.classDeclBracePlacement, FmtOptions.OBRACE_NEWLINE);
	options.put(FmtOptions.methodDeclBracePlacement, FmtOptions.OBRACE_NEWLINE);
	options.put(FmtOptions.ifBracePlacement, FmtOptions.OBRACE_NEWLINE);
	options.put(FmtOptions.whileBracePlacement, FmtOptions.OBRACE_NEWLINE);
	options.put(FmtOptions.catchBracePlacement, FmtOptions.OBRACE_NEWLINE);
	options.put(FmtOptions.otherBracePlacement, FmtOptions.OBRACE_NEWLINE);

        reformatFileContents("testfiles/formatting/alignment/alignmentKeywords04.php", options);
    }

    public void testIssue181624_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue181624_01.php", options);
    }

    public void testWrapMethodCallArg01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/wrapping/methodCallArg01.php", options);
    }

    public void testWrapMethodCallArg02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/wrapping/methodCallArg02.php", options);
    }

    public void testWrapMethodCallArg03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/wrapping/methodCallArg03.php", options);
    }

    public void testWrapMethodCallArg04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/wrapping/methodCallArg04.php", options);
    }

    public void testWrapMethodCallArg05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/wrapping/methodCallArg05.php", options);
    }

    public void testWrapMethodParams01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/methodParams01.php", options);
    }

    public void testWrapMethodParams02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/methodParams02.php", options);
    }

    public void testWrapMethodParams03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/methodParams03.php", options);
    }

    public void testWrapMethodParams04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/methodParams04.php", options);
    }

    public void testWrapMethodParams05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/methodParams05.php", options);
    }

    public void testWrapMethodParams06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_NEVER);
	options.put(FmtOptions.alignMultilineMethodParams, true);
        reformatFileContents("testfiles/formatting/wrapping/methodParams06.php", options);
    }

    public void testWrapMethodParams07() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/wrapping/methodParams07.php", options);
    }

    public void testWrapInterfaces01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapExtendsImplementsKeyword, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/interfaces01.php", options);
    }

    public void testWrapInterfaces02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapExtendsImplementsKeyword, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/wrapping/interfaces02.php", options);
    }

    public void testWrapInterfaces03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapExtendsImplementsKeyword, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/wrapping/interfaces03.php", options);
    }

    public void testWrapInterfaces04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapExtendsImplementsKeyword, CodeStyle.WrapStyle.WRAP_IF_LONG);
	options.put(FmtOptions.wrapExtendsImplementsList, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/interfaces04.php", options);
    }

    public void testWrapInterfaces05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapExtendsImplementsKeyword, CodeStyle.WrapStyle.WRAP_ALWAYS);
	options.put(FmtOptions.wrapExtendsImplementsList, CodeStyle.WrapStyle.WRAP_ALWAYS);
	options.put(FmtOptions.alignMultilineImplements, true);
        reformatFileContents("testfiles/formatting/wrapping/interfaces05.php", options);
    }

    public void testMethodChainCall01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapChainedMethodCalls, CodeStyle.WrapStyle.WRAP_NEVER);
	options.put(FmtOptions.spaceAroundObjectOps, false);
        reformatFileContents("testfiles/formatting/wrapping/methodChainCall_01.php", options);
    }

    public void testWrappingForStatement01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/wrapping/forStatement01.php", options);
    }

    public void testWrappingForStatement02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/wrapping/forStatement02.php", options);
    }

    public void testWrappingForStatement03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_NEVER);
	options.put(FmtOptions.initialIndent, 6);
        reformatFileContents("testfiles/formatting/wrapping/forStatement03.php", options);
    }

    public void testWrappingForStatement04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/forStatement04.php", options);
    }

    public void testWrappingForStatement05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/forStatement05.php", options);
    }

    public void testWrappingForStatement06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/wrapping/forStatement06.php", options);
    }

    public void testWrappingForStatement07() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
	options.put(FmtOptions.initialIndent, 6);
        reformatFileContents("testfiles/formatting/wrapping/forStatement07.php", options);
    }

    public void testWrappingForStatement08() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
	options.put(FmtOptions.initialIndent, 5);
        reformatFileContents("testfiles/formatting/wrapping/forStatement08.php", options);
    }

    public void testWrappingForStatement09() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/wrapping/forStatement09.php", options);
    }

    public void testWrappingForStatement10() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/wrapping/forStatement10.php", options);
    }

    public void testWrappingWhileStatement01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapWhileStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/wrapping/whileStatement01.php", options);
    }

    public void testWrappingWhileStatement02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapWhileStatement, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/whileStatement02.php", options);
    }

    public void testWrappingWhileStatement03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapWhileStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
	options.put(FmtOptions.initialIndent, 5);
        reformatFileContents("testfiles/formatting/wrapping/whileStatement03.php", options);
    }

    public void testWrappingDoWhileStatement01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapDoWhileStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/wrapping/doStatement01.php", options);
    }

    public void testWrappingDoWhileStatement02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapDoWhileStatement, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/doStatement02.php", options);
    }

    public void testWrappingDoWhileStatement03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapDoWhileStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/wrapping/doStatement03.php", options);
    }

    public void testWrappingIfStatement01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapIfStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/wrapping/ifStatement01.php", options);
    }

    public void testWrappingIfStatement02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapIfStatement, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/ifStatement02.php", options);
    }

    public void testWrappingIfStatement03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapIfStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
	options.put(FmtOptions.initialIndent, 54);
        reformatFileContents("testfiles/formatting/wrapping/ifStatement03.php", options);
    }

    public void testWrappingFor01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapFor, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/wrapping/for01.php", options);
    }

    public void testWrappingFor02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapFor, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/for02.php", options);
    }

    public void testWrappingBlock01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/wrapping/block01.php", options);
    }

    public void testWrappingBlock02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapBlockBraces, false);
        reformatFileContents("testfiles/formatting/wrapping/block02.php", options);
    }

    public void testWrappingBlock03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapBlockBraces, true);
        reformatFileContents("testfiles/formatting/wrapping/block03.php", options);
    }

    public void testWrappingBlock04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapBlockBraces, false);
        reformatFileContents("testfiles/formatting/wrapping/block04.php", options);
    }

    public void testWrappingBlock05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapBlockBraces, true);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.ifBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.forBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.whileBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.switchBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.catchBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/wrapping/block05.php", options);
    }

    public void testWrappingBlock06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapBlockBraces, false);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.ifBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.forBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.whileBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.switchBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.catchBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/wrapping/block06.php", options);
    }

    public void testWrappingStatements01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/wrapping/statements01.php", options);
    }

    public void testWrappingStatements02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapStatementsOnTheLine, false);
        options.put(FmtOptions.spaceAfterSemi, true);
        reformatFileContents("testfiles/formatting/wrapping/statements02.php", options);
    }

    public void testWrappingStatements03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapStatementsOnTheLine, false);
        options.put(FmtOptions.spaceAfterSemi, false);
        reformatFileContents("testfiles/formatting/wrapping/statements03.php", options);
    }

    public void testWrappingStatements04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/wrapping/statements04.php", options);
    }
    
    public void testTernaryOp01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapTernaryOps, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/wrapping/ternaryOp01.php", options);
    }

    public void testTernaryOp02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapTernaryOps, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/ternaryOp02.php", options);
    }

    public void testTernaryOp03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapTernaryOps, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/wrapping/ternaryOp03.php", options);
    }

    public void testIssue181588() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/issue181588.php", options);
    }

    public void testLineComment01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/lineComment01.php", options);
    }

    public void testLineComment02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/lineComment02.php", options);
    }

    public void testLineComment03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/lineComment03.php", options);
    }

    public void testLineComment04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/lineComment04.php", options);
    }

    public void testLineComment05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/lineComment05.php", options);
    }

    public void testComment01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment01.php", options);
    }

    public void testComment02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment02.php", options);
    }

    public void testComment03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment03.php", options);
    }

    public void testComment04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment04.php", options);
    }

    public void testComment05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment05.php", options);
    }

    public void testComment06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment06.php", options);
    }

    public void testComment07() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        // Be careful during editing the test file. The space after /*  is important.
	reformatFileContents("testfiles/formatting/comment07.php", options);
    }

    public void testComment08() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment08.php", options);
    }

    public void testComment09() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment09.php", options);
    }

    public void testComment10() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment10.php", options);
    }

    public void testComment11() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment11.php", options);
    }

    // the html tests doesn't work properly, the results are deferent then in the ide. i don't know why. 
    public void testHtml01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/html/html01.php", options);
    }

    public void testHtml02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.initialIndent, 4);
	reformatFileContents("testfiles/formatting/html/html02.php", options);
    }

    public void testHtml03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.initialIndent, 4);
	reformatFileContents("testfiles/formatting/html/html03.php", options);
    }

    public void testHtml04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/html/html04.php", options);
    }

    public void testHtml05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/html/html05.php", options);
    }

    public void testIssue175229() throws Exception {
        
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/html/issue175229.php", options);
    }

    public void testIssue183268() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/html/issue183268.php", options);
    }

    public void testIssue179108_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/html/issue179108_01.php", options);
    }

    public void testIssue179108_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/html/issue179108_02.php", options);
    }

    public void testIssue187309() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/html/issue187309.php", options);
    }

    public void testIssue190652() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/html/issue190652.php", options);
    }

    public void test183200_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue183200_01.php", options);
    }

    public void test183200_02() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue183200_02.php", options);
    }

    public void test182072_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue182072_01.php", options);
    }

    public void test180332_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue180332_01.php", options);
    }

    public void test168396_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue168396_01.php", options);
    }

    public void testIssue184687_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 4);
        reformatFileContents("testfiles/formatting/issue184687_01.php", options);
    }

    public void testIssue184687_02() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 4);
        reformatFileContents("testfiles/formatting/issue184687_02.php", options);
    }

    public void testIssue185353_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue185353_01.php", options);
    }
    
    public void testIssue185353_02() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue185353_02.php", options);
    }
    
    public void testIssue185353_03() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue185353_03.php", options);
    }
    
    public void testIssue185353_04() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue185353_04.php", options);
    }
    
    public void testIssue185353_05() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue185353_05.php", options);
    }

    public void testIssue186183_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue186183_01.php", options);
    }

    public void testIssue186461_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/issue186461_01.php", options);
    }

    public void testIssue186461_02() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/issue186461_02.php", options);
    }

    public void testIssue186738_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/issue186738_01.php", options);
    }

    public void testIssue187264_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/issue187264_01.php", options);
    }

    public void testIssue187264_02() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/issue187264_02.php", options);
    }

    public void testIssue187665_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue187665_01.php", options);
    }

    public void testIssue187665_02() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue187665_02.php", options);
    }

    public void testIssue187888_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue187888_01.php", options);
    }

    public void testIssue187888_02() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue187888_02.php", options);
    }

    public void testIssue187864_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue187864_01.php", options);
    }

    public void testIssue189002_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/html/issue189002_01.php", options);
    }

    public void testIssue189002_02() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/html/issue189002_02.php", options);
    }

    public void testHtmlIf_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/html/if_01.php", options);
    }

    public void testHtmlIf_02() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/html/if_02.php", options);
    }

    public void testHtmlIf_03() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/html/if_03.php", options);
    }

    public void testIssue188810_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue188810_01.php", options);
    }

    public void testIssue189850() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/html/issue189850.php", options);
    }

    public void testIssue189835_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue189835_01.php", options);
    }

    public void testIssue189835_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue189835_02.php", options);
    }

    public void testIssue189835_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue189835_03.php", options);
    }

    public void testIssue189835_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue189835_04.php", options);
    }

    public void testIssue189835_05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue189835_05.php", options);
    }

    public void testIssue189835_06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue189835_06.php", options);
    }

    public void testIssue190544() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/html/issue190544.php", options);
    }

    public void testIssue179184() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/html/issue179184.php", options);
    }
    
    public void testIssue179184_02() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/html/issue179184_02.php", options);
    }

    public void testIssue190426() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue190426.php", options);
    }

    private void reformatFileContents(String file) throws Exception {
        reformatFileContents(file, new IndentPrefs(2, 2));
    }

    private void reformatFileContents(String file, int initialIndent) throws Exception {
        reformatFileContents(file, new IndentPrefs(2, 2), initialIndent);
    }

    @Override
    protected void reformatFileContents(String file, IndentPrefs preferences) throws Exception {
        reformatFileContents(file, preferences, 0);
    }

    protected void reformatFileContents(String file, IndentPrefs preferences, int initialIndent) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);
        BaseDocument doc = getDocument(fo);
        assertNotNull(doc);
        String fullTxt = doc.getText(0, doc.getLength());
        int formatStart = 0;
        int formatEnd = doc.getLength();
        int startMarkPos = fullTxt.indexOf(FORMAT_START_MARK);

        if (startMarkPos >= 0){
            formatStart = startMarkPos + FORMAT_START_MARK.length();
            formatEnd = fullTxt.indexOf(FORMAT_END_MARK);

            if (formatEnd == -1){
                throw new IllegalStateException();
            }
        }

        Formatter formatter = getFormatter(preferences);
        //assertNotNull("getFormatter must be implemented", formatter);

        setupDocumentIndentation(doc, preferences);

        Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
        prefs.putInt(FmtOptions.initialIndent, initialIndent);
        prefs.putInt(FmtOptions.continuationIndentSize, 4);

        format(doc, formatter, formatStart, formatEnd, false);

        String after = doc.getText(0, doc.getLength());
        assertDescriptionMatches(file, after, false, ".formatted");
    }

    protected void reformatFileContents(String file, Map<String, Object> options) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);

        String text = read(fo);

        int formatStart = 0;
        int formatEnd = text.length();
        int startMarkPos = text.indexOf(FORMAT_START_MARK);

        if (startMarkPos >= 0){
            formatStart = startMarkPos;
            text = text.substring(0, formatStart) + text.substring(formatStart + FORMAT_START_MARK.length());
            formatEnd = text.indexOf(FORMAT_END_MARK);
            text = text.substring(0, formatEnd) + text.substring(formatEnd + FORMAT_END_MARK.length());
            formatEnd --;
            if (formatEnd == -1){
                throw new IllegalStateException();
            }
        }

        BaseDocument doc = getDocument(text);
        assertNotNull(doc);
        

        IndentPrefs preferences = new IndentPrefs(4, 4);
        Formatter formatter = getFormatter(preferences);
        //assertNotNull("getFormatter must be implemented", formatter);

        setupDocumentIndentation(doc, preferences);

        Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
        for (String option : options.keySet()) {
            Object value = options.get(option);
            if (value instanceof Integer) {
                prefs.putInt(option, ((Integer)value).intValue());
            }
            else if (value instanceof String) {
                prefs.put(option, (String)value);
            }
            else if (value instanceof Boolean) {
                prefs.put(option, ((Boolean)value).toString());
            }
	    else if (value instanceof CodeStyle.BracePlacement) {
		prefs.put(option, ((CodeStyle.BracePlacement)value).name());
	    }
	    else if (value instanceof CodeStyle.WrapStyle) {
		prefs.put(option, ((CodeStyle.WrapStyle)value).name());
	    }
        }

        format(doc, formatter, formatStart, formatEnd, false);
        String after = doc.getText(0, doc.getLength());
        assertDescriptionMatches(file, after, false, ".formatted");
    }
}
