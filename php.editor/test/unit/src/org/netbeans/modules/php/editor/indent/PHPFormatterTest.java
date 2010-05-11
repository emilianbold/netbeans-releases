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

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.editor.BaseDocument;
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
    }
    
      public void xtest174595() throws Exception {
        reformatFileContents("testfiles/formatting/issue174595.php");
    }

    public void xtestContinuedExpression() throws Exception {
        reformatFileContents("testfiles/formatting/continued_expression.php");
    }

    public void xtestContinuedExpression2() throws Exception {
        reformatFileContents("testfiles/formatting/continued_expression2.php");
    }

    public void xtestIfelseNobrackets() throws Exception {
        reformatFileContents("testfiles/formatting/ifelse_nobrackets.php");
    }

    public void xtestMultilineFunctionHeader() throws Exception {
        reformatFileContents("testfiles/formatting/multiline_function_header.php");
    }

    public void etestLineSplitting1() throws Exception {
        reformatFileContents("testfiles/formatting/line_splitting1.php");
    }

    public void etestLineSplitting2() throws Exception {
        reformatFileContents("testfiles/formatting/line_splitting2.php");
    }

    public void xtestHereDoc() throws Exception {
        reformatFileContents("testfiles/formatting/heredoc.php");
    }

    public void xtestSimpleClassDef() throws Exception {
        reformatFileContents("testfiles/formatting/simple_class_def.php");
    }

    public void xtestSwitchStmt() throws Exception {
        reformatFileContents("testfiles/formatting/switch_stmt.php");
    }

    public void xtestSwitchStmt01() throws Exception {
        reformatFileContents("testfiles/formatting/switch_stmt01.php");
    }
    
    public void xtestArrays1() throws Exception {
        reformatFileContents("testfiles/formatting/arrays1.php");
    }

    public void xtestArrays2() throws Exception {
        reformatFileContents("testfiles/formatting/arrays2.php");
    }

    public void xtestArrays3() throws Exception {
        reformatFileContents("testfiles/formatting/arrays3.php");
    }

    public void xtestArrays4() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.initialIndent, 4);
        options.put(FmtOptions.itemsInArrayDeclarationIndentSize, 6);
        reformatFileContents("testfiles/formatting/arrays4.php", options);
    }

    public void xtestArrays05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/arrays5.php", options);
    }
    
    public void xtestFragment1() throws Exception {
        reformatFileContents("testfiles/formatting/format_fragment1.php");
    }

    public void xtestNestedArrays1() throws Exception {
        reformatFileContents("testfiles/formatting/nested_array1.php");
    }

    public void xtestSubsequentQuotes() throws Exception {
        reformatFileContents("testfiles/formatting/subsequentquotes.php");
    }

    public void xtestMultilineString() throws Exception {
        reformatFileContents("testfiles/formatting/multiline_string.php");
    }

    public void xtestInitialIndent1() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.initialIndent, 5);
        reformatFileContents("testfiles/formatting/initial_indent1.php", options);
    }

   public void xtestInitialIndent01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/initialIndent01.php", options);
    }

    public void xtestIfElseAlternativeSyntax() throws Exception {
        reformatFileContents("testfiles/formatting/ifelse_alternative_syntax.php");
    }

    public void xtestNamespaces1() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/namespaces1.php", options);
    }

    public void xtestNamespaces02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/namespaces_02.php", options);
    }

    public void xtestNamespaces03() throws Exception {
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

    public void xtestNamespaces04() throws Exception {
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

    public void xtestNamespaces05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/namespaces_05.php", options);
    }

    public void xtest161049() throws Exception {
        reformatFileContents("testfiles/formatting/issue161049.php");
    }

    public void xtest172259() throws Exception {
        reformatFileContents("testfiles/formatting/issue172259.php");
    }
    public void xtest171309() throws Exception {
        reformatFileContents("testfiles/formatting/issue171309.php");
    }

    public void xtest162126() throws Exception {
        reformatFileContents("testfiles/formatting/issue162126.php");
    }

    public void xtest162785() throws Exception {
        reformatFileContents("testfiles/formatting/issue162785.php");
    }

    public void xtest162586() throws Exception {
        reformatFileContents("testfiles/formatting/issue162586.php");
    }

    public void xtest176453() throws Exception {
        reformatFileContents("testfiles/formatting/issue176453.php");
    }

    public void xtest165762() throws Exception {
        reformatFileContents("testfiles/formatting/issue165762.php");
    }

    public void xtest166550() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue166550.php", options);
    }
    
    public void xtest159339_161408() throws Exception {
        reformatFileContents("testfiles/formatting/issues_159339_161408.php");
    }

    public void xtest164219() throws Exception {
        reformatFileContents("testfiles/formatting/issue164219.php");
    }

    public void xtest162320() throws Exception {
        reformatFileContents("testfiles/formatting/issue162320.php");
    }

    public void xtest173906_dowhile() throws Exception {
        reformatFileContents("testfiles/formatting/issue173906_dowhile.php");
    }

    public void xtest164381() throws Exception {
        reformatFileContents("testfiles/formatting/issue164381.php");
    }

    public void xtest174544() throws Exception {
        reformatFileContents("testfiles/formatting/issue174544.php");
    }

    public void xtest174563() throws Exception {
        reformatFileContents("testfiles/formatting/issue174563.php");
    }

    public void xtest172475() throws Exception {
        reformatFileContents("testfiles/formatting/issue172475.php");
    }

    public void xtest167791() throws Exception {
        reformatFileContents("testfiles/formatting/issue167791.php", 5);
    }

    public void xtest176224() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue176224.php", options);
    }

    public void xtestBracePlacement01() throws Exception {
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

    public void xtestBracePlacement02() throws Exception {
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

    public void xtestBracePlacement03() throws Exception {
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

    public void xtestAlternativeSyntaxPlacement01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/AlternativeSyntaxPlacement01.php", options);
    }

    // blank lines
    public void xtestBLClass01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Class01.php", options);
    }

    public void xtestBLClass02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
	options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.spaceBeforeClassDeclLeftBrace, false);
        reformatFileContents("testfiles/formatting/blankLines/Class02.php", options);
    }

    public void xtestBLClass03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
	options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
	options.put(FmtOptions.spaceBeforeClassDeclLeftBrace, false);
        reformatFileContents("testfiles/formatting/blankLines/Class03.php", options);
    }

    public void xtestBLFields01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceBeforeClassDeclLeftBrace, true);
	options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Fields01.php", options);
    }

    public void xtestBLFields02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Fields02.php", options);
    }

    public void xtestBLFields03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Fields03.php", options);
    }

    public void xtestBLFields04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Fields04.php", options);
    }

    public void xtestBLFields05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Fields05.php", options);
    }

    public void xtestBLFields06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Fields06.php", options);
    }

    public void xtestBLFunction01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Function01.php", options);
    }

    public void xtestBLFunction02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Function02.php", options);
    }

    public void xtestBLFunction04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Function04.php", options);
    }

    public void xtestBLNamespace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Namespace01.php", options);
    }

    public void xtestBLNamespace02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Namespace02.php", options);
    }

    public void xtestBLNamespace03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Namespace03.php", options);
    }

    public void xtestBLSimpleClass01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass01.php", options);
    }

    public void xtestBLSimpleClass02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass02.php", options);
    }

    public void xtestBLSimpleClass03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass03.php", options);
    }

    public void xtestBLSimpleClass04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass04.php", options);
    }

    public void xtestBLSimpleClass05() throws Exception {
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

    public void xtestBLSimpleClass06() throws Exception {
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

    public void xtestBLSimpleClass07() throws Exception {
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

    public void xtestBLSimpleClass08() throws Exception {
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

    public void xtestBLSimpleClass09() throws Exception {
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

    public void xtestBLSimpleClass10() throws Exception {
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

    public void xtestBLSimpleClass11() throws Exception {
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

    public void xtestBLSimpleClass12() throws Exception {
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

    public void xtestBLSimpleClass13() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass13.php", options);
    }

    public void xtestBLSimpleClass14() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass14.php", options);
    }

    public void xtestBLSimpleClass15() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass15.php", options);
    }

    public void xtestBLSimpleClass16() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass16.php", options);
    }

    public void xtestBLSimpleClass17() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 4);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass17.php", options);
    }

    public void xtestBLSimpleUse01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Use01.php", options);
    }

    public void xtestBLSimpleUse02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Use02.php", options);
    }

    public void xtestBLSimpleUse03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Use03.php", options);
    }

    public void xtestBLSimpleUse04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Use04.php", options);
    }

    public void xtestOpenClosePHPTag01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/OpenClosePHPTag01.php", options);
    }

    public void xtestOpenClosePHPTag02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/OpenClosePHPTag02.php", options);
    }

    public void xtestOpenClosePHPTag03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.initialIndent, 4);
        reformatFileContents("testfiles/formatting/blankLines/OpenClosePHPTag03.php", options);
    }

    public void xtestOpenClosePHPTag04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.initialIndent, 4);
        reformatFileContents("testfiles/formatting/blankLines/OpenClosePHPTag04.php", options);
    }

    public void xtestOpenClosePHPTag05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.initialIndent, 4);
        reformatFileContents("testfiles/formatting/blankLines/OpenClosePHPTag05.php", options);
    }

    public void xtestSpacesBeforeClassDecLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeClassDeclLeftBrace, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeClassDecLeftBrace01.php", options);
    }

    public void xtestSpacesBeforeClassDecLeftBrace02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeClassDeclLeftBrace, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeClassDecLeftBrace02.php", options);
    }

    public void xtestSpacesBeforeClassDecLeftBrace03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeClassDeclLeftBrace, new Boolean(false));
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeClassDecLeftBrace03.php", options);
    }

    public void xtestSpacesBeforeMethodDeclLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeMethodDeclLeftBrace, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodDeclLeftBrace01.php", options);
    }

    public void xtestSpacesBeforeMethodDeclLeftBrace02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeMethodDeclLeftBrace, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodDeclLeftBrace02.php", options);
    }

    public void xtestSpacesBeforeMethodDeclLeftBrace03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeMethodDeclLeftBrace, true);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodDeclLeftBrace03.php", options);
    }

    public void xtestSpacesBeforeWhile01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeWhile, true);
	options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeWhile01.php", options);
    }

    public void xtestSpacesBeforeWhile02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeWhile, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeWhile02.php", options);
    }

    public void xtestSpacesBeforeWhile03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeWhile, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeWhile03.php", options);
    }

    public void xtestSpacesBeforeElse01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeElse, true);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeElse01.php", options);
    }

    public void xtestSpacesBeforeElse02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeElse, true);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeElse02.php", options);
    }

    public void xtestSpacesBeforeElse03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeElse, false);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeElse03.php", options);
    }

    public void xtestSpacesBeforeElse04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeElse, true);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeElse04.php", options);
    }

    public void xtestSpacesBeforeCatch01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeCatch, true);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeCatch01.php", options);
    }

    public void xtestSpacesBeforeCatch02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeCatch, false);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeCatch02.php", options);
    }

    public void xtestSpacesBeforeMethodCallParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeMethodCallParen, true);
	options.put(FmtOptions.spaceBeforeClassDeclLeftBrace, true);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodCallParen01.php", options);
    }

    public void xtestSpacesBeforeMethodCallParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeMethodCallParen, false);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodCallParen02.php", options);
    }

    public void xtestSpacesBeforeMethodDeclParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeMethodDeclParen, true);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodDeclParen01.php", options);
    }

    public void xtestSpacesBeforeMethodDeclParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeMethodDeclParen, false);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodDeclParen02.php", options);
    }

    public void xtestSpacesBeforeIfParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeIfParen, true);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeIfParen01.php", options);
    }

    public void xtestSpacesBeforeIfParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeIfParen, false);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeIfParen02.php", options);
    }

    public void xtestSpacesBeforeForParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeForParen, true);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeForParen01.php", options);
    }

    public void xtestSpacesBeforeForParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeForParen, false);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeForParen02.php", options);
    }

    public void xtestSpacesBeforeWhileParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeWhileParen, true);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeWhileParen01.php", options);
    }

    public void xtestSpacesBeforeWhileParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeWhileParen, false);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeWhileParen02.php", options);
    }

    public void xtestSpacesBeforeCatchParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeCatchParen, true);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeCatchParen01.php", options);
    }

    public void xtestSpacesBeforeCatchParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeCatchParen, false);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeCatchParen02.php", options);
    }

    public void xtestSpacesBeforeSwitchParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeSwitchParen, true);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeSwitchParen01.php", options);
    }

    public void xtestSpacesBeforeSwitchParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeSwitchParen, false);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeSwitchParen02.php", options);
    }

    public void xtestSpacesAroundStringConcat01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeSwitchParen, true);
	options.put(FmtOptions.spaceAroundStringConcatOps, false);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
	options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundStringConcat01.php", options);
    }

    public void xtestSpacesAroundTernaryOp01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceAroundTernaryOps, false);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundTernaryOp01.php", options);
    }

    public void xtestSpacesAroundTernaryOp02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceAroundTernaryOps, false);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundTernaryOp02.php", options);
    }

    public void xtestSpacesAroundTernaryOp03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceAroundTernaryOps, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundTernaryOp03.php", options);
    }

    public void xtestSpacesAroundKeyValue01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceAroundKeyValueOps, false);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundKeyValueOp01.php", options);
    }

    public void xtestSpacesAroundKeyValue02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceAroundKeyValueOps, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundKeyValueOp02.php", options);
    }

    public void xtestSpacesWithinIfParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinIfParens, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens01.php", options);
    }

    public void xtestSpacesWithinForParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinForParens, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens02.php", options);
    }

    public void xtestSpacesWithinWhileParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinWhileParens, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens03.php", options);
    }

    public void xtestSpacesWithinSwitchParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinSwitchParens, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens04.php", options);
    }

    public void xtestSpacesWithinCatchParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinCatchParens, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens05.php", options);
    }

    public void xtestSpacesWithinParens01() throws Exception {
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

    public void xtestSpacesWithinTypeCastParens02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinTypeCastParens, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinTypeCastParens02.php", options);
    }

    public void xtestSpacesWithinArrayDeclParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinArrayDeclParens, false);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinArrayDeclParens01.php", options);
    }

    public void xtestSpacesWithinArrayDeclParens02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinArrayDeclParens, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinArrayDeclParens02.php", options);
    }

    public void xtestSpacesWithinArrayBrackets01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinArrayBrackets, false);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinArrayBrackets01.php", options);
    }

    public void xtestSpacesWithinArrayBrackets02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinArrayBrackets, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinArrayBrackets02.php", options);
    }

    public void xtestSpacesWithinArrayBrackets03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinArrayBrackets, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinArrayBrackets03.php", options);
    }

    public void xtestSpacesWithinArrayBrackets04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinArrayBrackets, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinArrayBrackets04.php", options);
    }
    
    public void xtestSpacesAfterTypeCast01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceAfterTypeCast, false);
        reformatFileContents("testfiles/formatting/spaces/spaceAfterTypeCast01.php", options);
    }

    public void xtestSpacesAfterTypeCast02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceAfterTypeCast, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAfterTypeCast02.php", options);
    }

    public void xtestSpacesBeforeAfterComma01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeComma, false);
	options.put(FmtOptions.spaceAfterComma, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterComma01.php", options);
    }

    public void xtestSpacesBeforeAfterComma02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeComma, false);
	options.put(FmtOptions.spaceAfterComma, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterComma02.php", options);
    }

    public void xtestSpacesBeforeAfterComma03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeComma, true);
	options.put(FmtOptions.spaceAfterComma, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterComma03.php", options);
    }

    public void xtestSpacesBeforeAfterComma04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeComma, true);
	options.put(FmtOptions.spaceAfterComma, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterComma04.php", options);
    }

    public void xtestSpacesBeforeUnaryOps01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundUnaryOps01.php", options);
    }

    public void xtestSpacesBeforeUnaryOps02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceWithinIfParens, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundUnaryOps02.php", options);
    }

    public void xtestSpacesBeforeUnaryOps03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceWithinIfParens, true);
	options.put(FmtOptions.spaceAroundUnaryOps, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundUnaryOps03.php", options);
    }

    public void xtestSpacesBeforeKeywords01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceBeforeElse, true);
	options.put(FmtOptions.spaceBeforeWhile, true);
	options.put(FmtOptions.spaceBeforeCatch, true);
	options.put(FmtOptions.placeElseOnNewLine, false);
	options.put(FmtOptions.placeWhileOnNewLine, false);
	options.put(FmtOptions.placeCatchOnNewLine, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeKeywords01.php", options);
    }

    public void xtestSpacesBeforeKeywords02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceBeforeElse, true);
	options.put(FmtOptions.spaceBeforeWhile, true);
	options.put(FmtOptions.spaceBeforeCatch, true);
	options.put(FmtOptions.placeElseOnNewLine, true);
	options.put(FmtOptions.placeWhileOnNewLine, true);
	options.put(FmtOptions.placeCatchOnNewLine, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeKeywords02.php", options);
    }

    public void xtestSpacesBeforeKeywords03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceBeforeElse, false);
	options.put(FmtOptions.spaceBeforeWhile, false);
	options.put(FmtOptions.spaceBeforeCatch, false);
	options.put(FmtOptions.placeElseOnNewLine, false);
	options.put(FmtOptions.placeWhileOnNewLine, false);
	options.put(FmtOptions.placeCatchOnNewLine, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeKeywords03.php", options);
    }

    public void xtestSpacesBeforeKeywords04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceBeforeElse, true);
	options.put(FmtOptions.spaceBeforeWhile, true);
	options.put(FmtOptions.spaceBeforeCatch, true);
	options.put(FmtOptions.placeElseOnNewLine, false);
	options.put(FmtOptions.placeWhileOnNewLine, false);
	options.put(FmtOptions.placeCatchOnNewLine, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeKeywords04.php", options);
    }

    public void xtestIssue180859_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceAroundBinaryOps, true);
        reformatFileContents("testfiles/formatting/spaces/issue180859_01.php", options);
    }

    public void xtestIssue180859_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceAroundBinaryOps, false);
        reformatFileContents("testfiles/formatting/spaces/issue180859_02.php", options);
    }

    public void xtestSpacesBeforeAfterSemi01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeSemi, false);
	options.put(FmtOptions.spaceAfterSemi, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterSemi01.php", options);
    }

    public void xtestSpacesBeforeAfterSemi02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeSemi, false);
	options.put(FmtOptions.spaceAfterSemi, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterSemi02.php", options);
    }

    public void xtestSpacesBeforeAfterSemi03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeSemi, true);
	options.put(FmtOptions.spaceAfterSemi, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterSemi03.php", options);
    }

    public void xtestSpacesBeforeAfterSemi04() throws Exception {
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

    public void xtestIssue181003_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/issue181003_01.php", options);
    }

    public void xtestIssue181003_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issue181003_02.php", options);
    }

    public void xtestIssue181003_03() throws Exception {
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

    public void xtestIssue181003_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.blankLinesAfterClassHeader, 0);
	options.put(FmtOptions.blankLinesBeforeClassEnd, 0);
	options.put(FmtOptions.blankLinesBeforeFunction, 0);
	options.put(FmtOptions.blankLinesAfterFunction, 0);
	options.put(FmtOptions.blankLinesBeforeFunctionEnd, 0);
        reformatFileContents("testfiles/formatting/blankLines/issue181003_04.php", options);
    }

    public void xtestAlignmentKeywords01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.placeWhileOnNewLine, true);
	options.put(FmtOptions.placeElseOnNewLine, true);
	options.put(FmtOptions.placeCatchOnNewLine, true);
	options.put(FmtOptions.placeNewLineAfterModifiers, true);
        reformatFileContents("testfiles/formatting/alignment/alignmentKeywords01.php", options);
    }

    public void xtestAlignmentKeywords02() throws Exception {
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

    public void xtestAlignmentKeywords03() throws Exception {
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

    public void xtestAlignmentKeywords04() throws Exception {
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

    public void xtestIssue181624_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue181624_01.php", options);
    }

    public void xtestWrapMethodParams01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/methodParams01.php", options);
    }

    public void xtestWrapMethodParams02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/methodParams02.php", options);
    }

    public void xtestWrapMethodParams03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/methodParams03.php", options);
    }

    public void xtestWrapMethodParams04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/methodParams04.php", options);
    }

    public void xtestWrapMethodParams05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/methodParams05.php", options);
    }

    public void xtestWrapMethodParams06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_NEVER);
	options.put(FmtOptions.alignMultilineMethodParams, true);
        reformatFileContents("testfiles/formatting/wrapping/methodParams06.php", options);
    }

    public void xtestWrapMethodParams07() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/wrapping/methodParams07.php", options);
    }

    public void xtestWrapInterfaces01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapExtendsImplementsKeyword, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/interfaces01.php", options);
    }

    public void xtestWrapInterfaces02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapExtendsImplementsKeyword, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/wrapping/interfaces02.php", options);
    }

    public void xtestWrapInterfaces03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapExtendsImplementsKeyword, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/wrapping/interfaces03.php", options);
    }

    public void xtestWrapInterfaces04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapExtendsImplementsKeyword, CodeStyle.WrapStyle.WRAP_IF_LONG);
	options.put(FmtOptions.wrapExtendsImplementsList, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/interfaces04.php", options);
    }

    public void xtestWrapInterfaces05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapExtendsImplementsKeyword, CodeStyle.WrapStyle.WRAP_ALWAYS);
	options.put(FmtOptions.wrapExtendsImplementsList, CodeStyle.WrapStyle.WRAP_ALWAYS);
	options.put(FmtOptions.alignMultilineImplements, true);
        reformatFileContents("testfiles/formatting/wrapping/interfaces05.php", options);
    }

    public void xtestMethodChainCall01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapChainedMethodCalls, CodeStyle.WrapStyle.WRAP_NEVER);
	options.put(FmtOptions.spaceAroundObjectOps, false);
        reformatFileContents("testfiles/formatting/wrapping/methodChainCall_01.php", options);
    }

    public void xtestWrappingForStatement01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/wrapping/forStatement01.php", options);
    }

    public void xtestWrappingForStatement02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/wrapping/forStatement02.php", options);
    }

    public void xtestWrappingForStatement03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_NEVER);
	options.put(FmtOptions.initialIndent, 6);
        reformatFileContents("testfiles/formatting/wrapping/forStatement03.php", options);
    }

    public void xtestWrappingForStatement04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/forStatement04.php", options);
    }

    public void xtestWrappingForStatement05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/forStatement05.php", options);
    }

    public void xtestWrappingForStatement06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/wrapping/forStatement06.php", options);
    }

    public void xtestWrappingForStatement07() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
	options.put(FmtOptions.initialIndent, 6);
        reformatFileContents("testfiles/formatting/wrapping/forStatement07.php", options);
    }

    public void xtestWrappingForStatement08() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
	options.put(FmtOptions.initialIndent, 5);
        reformatFileContents("testfiles/formatting/wrapping/forStatement08.php", options);
    }

    public void xtestWrappingForStatement09() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/wrapping/forStatement09.php", options);
    }

    public void xtestWrappingForStatement10() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/wrapping/forStatement10.php", options);
    }

    public void xtestWrappingWhileStatement01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapWhileStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/wrapping/whileStatement01.php", options);
    }

    public void xtestWrappingWhileStatement02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapWhileStatement, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/whileStatement02.php", options);
    }

    public void xtestWrappingWhileStatement03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapWhileStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
	options.put(FmtOptions.initialIndent, 5);
        reformatFileContents("testfiles/formatting/wrapping/whileStatement03.php", options);
    }

    public void xtestWrappingDoWhileStatement01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapDoWhileStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/wrapping/doStatement01.php", options);
    }

    public void xtestWrappingDoWhileStatement02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapDoWhileStatement, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/doStatement02.php", options);
    }

    public void xtestWrappingDoWhileStatement03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapDoWhileStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/wrapping/doStatement03.php", options);
    }

    public void xtestWrappingIfStatement01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapIfStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/wrapping/ifStatement01.php", options);
    }

    public void xtestWrappingIfStatement02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapIfStatement, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/ifStatement02.php", options);
    }

    public void xtestWrappingIfStatement03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapIfStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
	options.put(FmtOptions.initialIndent, 54);
        reformatFileContents("testfiles/formatting/wrapping/ifStatement03.php", options);
    }

    public void xtestWrappingFor01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapFor, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/wrapping/for01.php", options);
    }

    public void xtestWrappingFor02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapFor, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/for02.php", options);
    }

    public void xtestWrappingBlock01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/wrapping/block01.php", options);
    }

    public void xtestWrappingBlock02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapBlockBraces, false);
        reformatFileContents("testfiles/formatting/wrapping/block02.php", options);
    }

    public void xtestWrappingBlock03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapBlockBraces, true);
        reformatFileContents("testfiles/formatting/wrapping/block03.php", options);
    }

    public void xtestWrappingBlock04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapBlockBraces, false);
        reformatFileContents("testfiles/formatting/wrapping/block04.php", options);
    }

    public void xtestWrappingBlock05() throws Exception {
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

    public void xtestWrappingBlock06() throws Exception {
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

    public void xtestWrappingStatements01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/wrapping/statements01.php", options);
    }

    public void xtestWrappingStatements02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapStatementsOnTheLine, false);
        options.put(FmtOptions.spaceAfterSemi, true);
        reformatFileContents("testfiles/formatting/wrapping/statements02.php", options);
    }

    public void xtestWrappingStatements03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapStatementsOnTheLine, false);
        options.put(FmtOptions.spaceAfterSemi, false);
        reformatFileContents("testfiles/formatting/wrapping/statements03.php", options);
    }

    public void xtestWrappingStatements04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/wrapping/statements04.php", options);
    }
    
    public void xtestTernaryOp01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapTernaryOps, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/wrapping/ternaryOp01.php", options);
    }

    public void xtestTernaryOp02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapTernaryOps, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/ternaryOp02.php", options);
    }

    public void xtestTernaryOp03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.wrapTernaryOps, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/wrapping/ternaryOp03.php", options);
    }

    public void xtestIssue181588() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/issue181588.php", options);
    }

    public void xtestLineComment01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/lineComment01.php", options);
    }

    public void xtestLineComment02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/lineComment02.php", options);
    }

    public void xtestLineComment03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/lineComment03.php", options);
    }

    public void xtestLineComment04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/lineComment04.php", options);
    }

    public void xtestLineComment05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/lineComment05.php", options);
    }

    public void xtestComment01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment01.php", options);
    }

    public void xtestComment02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment02.php", options);
    }

    public void xtestComment03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment03.php", options);
    }

    public void xtestComment04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment04.php", options);
    }

    public void xtestComment05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment05.php", options);
    }

    public void xtestComment06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment06.php", options);
    }

    public void xtestComment07() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        // Be careful during editing the test file. The space after /*  is important.
	reformatFileContents("testfiles/formatting/comment07.php", options);
    }

    public void xtestComment08() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment08.php", options);
    }

    public void xtestComment09() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment09.php", options);
    }

    public void xtestComment10() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment10.php", options);
    }

    public void xtestComment11() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment11.php", options);
    }

    // the html tests doesn't work properly, the results are deferent then in the ide. i don't know why. 
    public void xtestHtml01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/html/html01.php", options);
    }

    public void xtestHtml02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.initialIndent, 4);
	reformatFileContents("testfiles/formatting/html/html02.php", options);
    }

    public void xtestHtml03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.initialIndent, 4);
	reformatFileContents("testfiles/formatting/html/html03.php", options);
    }

    public void xtestHtml04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/html/html04.php", options);
    }

    public void xtestIssue175229() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/html/issue175229.php", options);
    }

    public void xtestIssue183268() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/html/issue183268.php", options);
    }

    public void xtestIssue179108_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/html/issue179108_01.php", options);
    }

    public void xtestIssue179108_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/html/issue179108_02.php", options);
    }

    public void xtest183200_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue183200_01.php", options);
    }

    public void xtest183200_02() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue183200_02.php", options);
    }

    public void xtest182072_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue182072_01.php", options);
    }

    public void xtest180332_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue180332_01.php", options);
    }

    public void xtest168396_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue168396_01.php", options);
    }

    public void xtestIssue184687_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 4);
        reformatFileContents("testfiles/formatting/issue184687_01.php", options);
    }

    public void xtestIssue184687_02() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 4);
        reformatFileContents("testfiles/formatting/issue184687_02.php", options);
    }

    public void xtestIssue185353_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue185353_01.php", options);
    }
    
    public void xtestIssue185353_02() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue185353_02.php", options);
    }
    
    public void xtestIssue185353_03() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue185353_03.php", options);
    }
    
    public void xtestIssue185353_04() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue185353_04.php", options);
    }
    
    public void xtestIssue185353_05() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue185353_05.php", options);
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
