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

import java.util.prefs.Preferences;
import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
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
 * @author Filip.Zamboj at SUN.com
 */
public class PHPNewLineIndenterQATest extends PHPTestBase {
    public PHPNewLineIndenterQATest(String testName) {
        super(testName);
    }

    public void test172797_stableFixed() throws Exception{
        testIndentInFile("testfiles/indent/qa/issues/stable_fixedIssues/172797.php");
    }

    public void test17357_1_stableFixed() throws Exception{
        testIndentInFile("testfiles/indent/qa/issues/stable_fixedIssues/173357_1.php");
    }

    public void test173357_2_stableFixed() throws Exception{
        testIndentInFile("testfiles/indent/qa/issues/stable_fixedIssues/173357_2.php");
    }

    public void test176061_stableFixed() throws Exception{
        testIndentInFile("testfiles/indent/qa/issues/stable_fixedIssues/176061.php");
    }

    public void testClassAfterFunction_stable() throws Exception {
        testIndentInFile("testfiles/indent/qa/issues/stable_fixedIssues/classAfterFunction.php");
    }
    /**
     * issue 146247 there are 4 cases to be tested 
     * @throws Exception
     */
    public void test146247_1_stableFixed() throws Exception{
        testIndentInFile("testfiles/indent/qa/issues/stable_fixedIssues/146247_1.php");
    }
    public void test146247_2_stableFixed() throws Exception{
        testIndentInFile("testfiles/indent/qa/issues/stable_fixedIssues/146247_2.php");
    }
    public void test146247_3_stableFixed() throws Exception{
        testIndentInFile("testfiles/indent/qa/issues/stable_fixedIssues/146247_3.php");
    }
    public void test146247_4_stableFixed() throws Exception{
        testIndentInFile("testfiles/indent/qa/issues/stable_fixedIssues/146247_4.php");
    }

    /**
     * 173966 issue - a regression from 146247
     * @throws Exception
     */
    public void test173966_stableFixed() throws Exception{
        testIndentInFile("testfiles/indent/qa/issues/stable_fixedIssues/173966.php");
    }

    /**
     * 167087 issue
     * @throws Exceptioneviem
     */
    public void test167087_stableFixed() throws Exception {
        testIndentInFile("testfiles/indent/qa/issues/stable_fixedIssues/167087.php");
    }

    /**
     * 173900 issue
     * @throws Exception
     */
    public void test173900_regression() throws Exception {
        testIndentInFile("testfiles/indent/qa/issues/regressions/173900.php");
    }

    /**
     * 173937 issue
     * @throws
     */
    public void test173937_1_stableFixed() throws Exception {
        testIndentInFile("testfiles/indent/qa/issues/stable_fixedIssues/173937_1.php");
    }

    /**
     * 173937 issue
     * @throws
     */
    public void test175437() throws Exception {
        testIndentInFile("testfiles/indent/qa/issues/stable_fixedIssues/175437.php");
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
        testIndentInFile(file, null, 0);
    }

    protected void testIndentInFile(String file, IndentPrefs preferences, int initialIndent) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);
        String source = readFile(fo);

        int sourcePos = source.indexOf('^');
        assertNotNull(sourcePos);
        String sourceWithoutMarker = source.substring(0, sourcePos) + source.substring(sourcePos+1);
        Formatter formatter = getFormatter(preferences);

        JEditorPane ta = getPane(sourceWithoutMarker);
        Caret caret = ta.getCaret();
        caret.setDot(sourcePos);
        BaseDocument doc = (BaseDocument) ta.getDocument();
        if (formatter != null) {
            configureIndenters(doc, formatter, true);
        }

        setupDocumentIndentation(doc, preferences);


        Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
        prefs.putInt(FmtOptions.initialIndent, initialIndent);

        runKitAction(ta, DefaultEditorKit.insertBreakAction, "\n");

        doc.getText(0, doc.getLength());
        doc.insertString(caret.getDot(), "^", null);

        String target = doc.getText(0, doc.getLength());
        assertDescriptionMatches(file, target, false, ".indented");
    }
}
