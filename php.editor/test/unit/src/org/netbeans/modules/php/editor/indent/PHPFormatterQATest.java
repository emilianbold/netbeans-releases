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
 * @author Filip.Zamboj at Sun.com
 */
public class PHPFormatterQATest extends PHPTestBase {
    private String FORMAT_START_MARK = "/*FORMAT_START*/"; //NOI18N
    private String FORMAT_END_MARK = "/*FORMAT_END*/"; //NOI18N

    public PHPFormatterQATest(String testName) {
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
  
     public void testSpacesAfterObjectRefereneces_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/spacesAfterObjectReferences.php");
    }

    public void test173354_1_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173354_1.php");
    }
    public void test173354_2_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173354_2.php");
    }
    public void test173354_3_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173354_3.php");
    }

    public void test173107_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173107.php");
    }

    /**
     * issue 160996
     * @throws Exception
     */
      
    public void test160996_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/160996.php");
    }

    /**
     * issue 162320
     * @throws Exception
     */

    public void test162320_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/162320.php");
    }

    /**
     * issue 162586
     * @throws Exception
     */

    public void test162586_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/162586.php");
    }

    /**
     * issue 173899
     * @throws Exception
     */

    public void test173899_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173899.php");
    }
    /**
     * issue 173903
     * @throws Exception
     */
    public void test173903_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173903.php");
    }

    /**
     * issue 173906
     * @throws Exception
     */
    public void test173906_172475_1_stableFixedIssue() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173906_172475_1.php");
    }
    public void test173906_172475_2_stableFixedIssue() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173906_172475_2.php");
    }
    public void test173906_172475_3_stableFixedIssue() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173906_172475_3.php");
    }


     /**
     * issue 173908
     * @throws Exception
     */
    public void test173908_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173908.php");
    }

    /**
     * issue 174579
     * @throws Exception
     */
    public void test174579_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/174579.php");
    }

    /**
     * issue 174578
     * @throws Exception
     */
    public void test174578_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/174578.php");
    }

    /**
     * issue 124273 - partially only
     * @throws Exception
     */

//    public void test124273_1() throws Exception {
//        reformatFileContents("testfiles/formatting/qa/issues/unstable_reopenedIssues/124273_1.php");
//    }
//
//    public void test124273_2() throws Exception {
//        reformatFileContents("testfiles/formatting/qa/issues/unstable_reopenedIssues/124273_2.php");
//    }

    /**
     * issue 175427 and 124273 where 17527 is a regression
     * @throws Exception
     */
    public void test124273_175247_regression() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/qa/issues/regressions/124273_175247.php", options);
    }

    /**
     * issue 175229
     * @throws Exception
     */
//    public void test175229 throws Exception {
//        reformatFileContents("testfiles/formatting/qa/issues/unstable_reopenedIssues/175229_1.php");
//    }

    /**
     * issue 174653 
     * @throws Exception
     */

    public void test174563_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/174563_1.php");
    }



    public void testIfElseStatement_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/else_if.php");
    }

   
    /**
     * issue 174595
     * @throws Exception
     */
//    public void test174595_175229() throws Exception {
//        reformatFileContents("testfiles/formatting/qa/issues/unstable_reopenedIssues/174595_175229.php");
//    }

    /**
     * issue 174873 - test for missing $e variable only, so created
     * with respect to #173906 that is reopened.
     * @throws Exception
     */
    public void test174873_173906_stablePartial() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/qa/issues/stable_partialTests/174873_173906.php", options);
    }

    public void test174873_173906_1_stablePartial() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_partialTests/174873_173906_1.php");
    }

    private void reformatFileContents(String file) throws Exception {
        reformatFileContents(file, new IndentPrefs(2, 2));
    }

    @Override
    protected void reformatFileContents(String file, IndentPrefs preferences) throws Exception {
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
        format(doc, formatter, formatStart, formatEnd, false);

        String after = doc.getText(0, doc.getLength());
        assertDescriptionMatches(file, after, false, ".formatted");
    }

    protected void reformatFileContents(String file, Map<String, Object> options) throws Exception {
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
        }

        format(doc, formatter, formatStart, formatEnd, false);

        String after = doc.getText(0, doc.getLength());
        assertDescriptionMatches(file, after, false, ".formatted");
    }
}
