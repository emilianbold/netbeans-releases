/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.indent;

import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class PHPNewLineIndenterTest extends PHPTestBase {
    public PHPNewLineIndenterTest(String testName) {
        super(testName);
    }

    public void testSmartEnter() throws Exception{
        testIndentInFile("testfiles/indent/smart_enter.php");
    }

    public void testHtmlIndentInPHP() throws Exception{
        testIndentInFile("testfiles/indent/html_indent_in_php.php");
    }

    public void testTrivialRepeatedIndent() throws Exception{
        testIndentInFile("testfiles/indent/trivial_repeated_indent.php");
    }

    public void testAfterSwitchCase() throws Exception{
        testIndentInFile("testfiles/indent/after_switch_case.php");
    }

    public void testAfterSwitchBreak() throws Exception{
        testIndentInFile("testfiles/indent/after_switch_break.php");
    }

    public void testAfterSwitchBreak1() throws Exception{
        testIndentInFile("testfiles/indent/after_switch_break_1.php");
    }

    public void testBreakInWhile() throws Exception{
        testIndentInFile("testfiles/indent/break_in_while.php");
    }

    public void testMultilineFunctionCall() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call.php");
    }


    public void testIndentAfterClosingBracket() throws Exception{
        testIndentInFile("testfiles/indent/indent_after_closing_bracket.php");
    }

    public void testControlStmtWithoutBracket1() throws Exception{
        testIndentInFile("testfiles/indent/control_stmt_without_bracket1.php");
    }

    public void testControlStmtWithoutBracket2() throws Exception{
        testIndentInFile("testfiles/indent/control_stmt_without_bracket2.php");
    }

    public void testControlStmtWithoutBracket3() throws Exception{
        testIndentInFile("testfiles/indent/control_stmt_without_bracket3.php");
    }

    public void testIndentAfterMultilineStmt1() throws Exception{
        testIndentInFile("testfiles/indent/indent_after_multiline_stmt1.php");
    }

    public void testArrays1() throws Exception{
        testIndentInFile("testfiles/indent/arrays1.php");
    }

    public void testArrays2() throws Exception{
        testIndentInFile("testfiles/indent/arrays2.php");
    }

    public void testArrays3() throws Exception{
        testIndentInFile("testfiles/indent/arrays3.php");
    }
    
    public void test157137() throws Exception{
        testIndentInFile("testfiles/indent/issue157137.php");
    }

    public void test162586() throws Exception{
        testIndentInFile("testfiles/indent/issue162586.php");
    }

    public void test166552() throws Exception{
        testIndentInFile("testfiles/indent/issue166552.php");
    }
    
    public void test168908() throws Exception{
        testIndentInFile("testfiles/indent/issue168908.php");
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
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

    protected void testIndentInFile(String file) throws Exception {
        testIndentInFile(file, null);
    }

    protected void testIndentInFile(String file, IndentPrefs preferences) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);
        String source = readFile(fo);

        int sourcePos = source.indexOf('^');     
        assertNotNull(sourcePos);
        String sourceWithoutMarker = source.substring(0, sourcePos) + source.substring(sourcePos+1);
        Formatter formatter = getFormatter(null);
        
        JEditorPane ta = getPane(sourceWithoutMarker);
        Caret caret = ta.getCaret();
        caret.setDot(sourcePos);
        BaseDocument doc = (BaseDocument) ta.getDocument();
        if (formatter != null) {
            configureIndenters(doc, formatter, true);
        }

        setupDocumentIndentation(doc, preferences);

        runKitAction(ta, DefaultEditorKit.insertBreakAction, "\n");

        doc.getText(0, doc.getLength());
        doc.insertString(caret.getDot(), "^", null);

        String target = doc.getText(0, doc.getLength());
        assertDescriptionMatches(file, target, false, ".indented");
    }
}
