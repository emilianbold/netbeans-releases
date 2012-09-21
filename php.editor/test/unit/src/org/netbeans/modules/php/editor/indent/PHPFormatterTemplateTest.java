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
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.css.editor.indent.CssIndentTaskFactory;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.indent.HtmlIndentTaskFactory;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

/**
 *
 * @author Petr Pisl
 */
public class PHPFormatterTemplateTest extends PHPFormatterTestBase {

    public PHPFormatterTemplateTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        assert Lookup.getDefault().lookup(TestLanguageProvider.class) != null;

        try {
            TestLanguageProvider.register(CssTokenId.language());
            TestLanguageProvider.register(HTMLTokenId.language());
            TestLanguageProvider.register(PHPTokenId.language());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
            System.out.println("neco spatne");
        }
        CssIndentTaskFactory cssFactory = new CssIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/css"), cssFactory);
        HtmlIndentTaskFactory htmlReformatFactory = new HtmlIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/html"), htmlReformatFactory, new HtmlKit("text/html"));
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    @Override
    protected BaseDocument getDocument(FileObject fo, String mimeType, Language language) {
        // for some reason GsfTestBase is not using DataObjects for BaseDocument construction
        // which means that for example other formatter which does call EditorCookie to retrieve
        // document will get difference instance of BaseDocument for indentation
        try {
            DataObject dobj = DataObject.find(fo);
            assertNotNull(dobj);

            EditorCookie ec = (EditorCookie) dobj.getLookup().lookup(EditorCookie.class);
            assertNotNull(ec);

            return (BaseDocument) ec.openDocument();
        } catch (Exception ex) {
            fail(ex.toString());
            return null;
        }
    }

    //The testing file can not be edited in NetBeans due to trailing spaces. It's important to keep spaces on the empty lines.
    public void testFore_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/fore_01.php", options, true);
    }

    //The testing file can not be edited in NetBeans due to trailing spaces. It's important to keep spaces on the empty lines.
    public void testFore_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinForParens, true);
        reformatFileContents("testfiles/formatting/templates/fore_02.php", options, true);
    }

    //The testing file can not be edited in NetBeans due to trailing spaces. It's important to keep spaces on the empty lines.
    public void testFore_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinForParens, true);
        options.put(FmtOptions.forBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/templates/fore_03.php", options, true);
    }

    //The testing file can not be edited in NetBeans due to trailing spaces. It's important to keep spaces on the empty lines.
    public void testFore_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinForParens, true);
        options.put(FmtOptions.forBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/templates/fore_04.php", options, true);
    }

    //The testing file can not be edited in NetBeans due to trailing spaces. It's important to keep spaces on the empty lines.
    public void testFore_05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinForParens, true);
        options.put(FmtOptions.forBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/templates/fore_05.php", options, true);
    }

    //The testing file can not be edited in NetBeans due to trailing spaces. It's important to keep spaces on the empty lines.
    public void testFore_06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceWithinForParens, true);
        options.put(FmtOptions.forBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.initialIndent, 4);
        reformatFileContents("testfiles/formatting/templates/fore_06.php", options, true);
    }

    //The testing file can not be edited in NetBeans due to trailing spaces. It's important to keep spaces on the empty lines.
    public void testIssue184481_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue184481_01.php", options, true);
    }

    //The testing file can not be edited in NetBeans due to trailing spaces. It's important to keep spaces on the empty lines.
    public void testIssue184481_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue184481_02.php", options, true);
    }

    //The testing file can not be edited in NetBeans due to trailing spaces. It's important to keep spaces on the empty lines.
    public void testIssue184481_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue184481_03.php", options, true);
    }

    //The testing file can not be edited in NetBeans due to trailing spaces. It's important to keep spaces on the empty lines.
    public void testIssue184481_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue184481_04.php", options, true);
    }

    public void testIssue184070_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue184070_01.php", options, true);
    }

    public void testIssue184690_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue184690_01.php", options, true);
    }

    public void testPrivate_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 4);
        reformatFileContents("testfiles/formatting/templates/private_01.php", options, true);
    }

    public void testPrivate_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 4);
        reformatFileContents("testfiles/formatting/templates/private_02.php", options, true);
    }

    public void testFncTemplate_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 4);
        reformatFileContents("testfiles/formatting/templates/function_01.php", options, true);
    }

    public void testImplementsOverwriteTemplate_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 4);
        reformatFileContents("testfiles/formatting/templates/implementsOverwrite_01.php", options, true);
    }

    public void testImplementsOverwriteTemplate_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 4);
        options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/templates/implementsOverwrite_02.php", options, true);
    }

    public void testImplementsOverwriteTemplate_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 4);
        options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        reformatFileContents("testfiles/formatting/templates/implementsOverwrite_03.php", options, true);
    }

    public void testIssue184141() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 4);
        reformatFileContents("testfiles/formatting/templates/issue184141.php", options, true);
    }

    public void testIssue185353_06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue185353_06.php", options, true);
    }

    // The test file containscharacters that are converted by default setting of netbeans.
    // Don't edit the test file in NetBeans!!!
    public void testIssue185435_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.expandTabToSpaces, true);
        reformatFileContents("testfiles/formatting/templates/issue185435_01.php", options, true);
    }

    // The test file containscharacters that are converted by default setting of netbeans.
    // Don't edit the test file in NetBeans!!!
    public void testIssue185435_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue185435_02.php", options, true);
    }

    // The test file containscharacters that are converted by default setting of netbeans.
    // Don't edit the test file in NetBeans!!!
    public void testIssue185435_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.expandTabToSpaces, false);
        reformatFileContents("testfiles/formatting/templates/issue185435_03.php", options, true);
    }

    // The test file containscharacters that are converted by default setting of netbeans.
    // Don't edit the test file in NetBeans!!!
    public void testIssue185435_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.expandTabToSpaces, false);
        options.put(FmtOptions.tabSize, 3);
        reformatFileContents("testfiles/formatting/templates/issue185435_04.php", options, true);
    }

    // The test file containscharacters that are converted by default setting of netbeans.
    // Don't edit the test file in NetBeans!!!
    public void testIssue185435_05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.expandTabToSpaces, false);
        options.put(FmtOptions.tabSize, 3);
        reformatFileContents("testfiles/formatting/templates/issue185435_05.php", options, true);
    }

    public void testIssue185438_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue185438_01.php", options, true);
    }

    public void testIssue185438_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue185438_02.php", options, true);
    }

    public void testIssue186008_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue186008_01.php", options, true);
    }

    public void testIssue186008_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue186008_02.php", options, true);
    }

    public void testIssue186008_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue186008_03.php", options, true);
    }

    public void testFirstLineInHTML() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/firstLineInHTML_01.php", options, true);
    }

    public void testIssue187665_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue187665_01.php", options, true);
    }

    public void testIssue187665_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue187665_02.php", options, true);
    }

    public void testIssue188656_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue188656_01.php", options, true);
    }

    public void testIssue188656_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue188656_02.php", options, true);
    }

    public void testIssue188656_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue188656_03.php", options, true);
    }

    public void testIssue188656_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.tabSize, 4);
        options.put(FmtOptions.expandTabToSpaces, true);
        options.put(FmtOptions.continuationIndentSize, 4);
        options.put(FmtOptions.indentSize, 4);
        options.put(FmtOptions.initialIndent, 4);
        options.put(FmtOptions.spacesPerTab, 4);
        reformatFileContents("testfiles/formatting/templates/issue188656_04.php", options, true);
    }

    public void testIssue188656_05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.tabSize, 4);
        options.put(FmtOptions.expandTabToSpaces, true);
        options.put(FmtOptions.continuationIndentSize, 4);
        options.put(FmtOptions.indentSize, 4);
        options.put(FmtOptions.initialIndent, 4);
        options.put(FmtOptions.spacesPerTab, 4);
        reformatFileContents("testfiles/formatting/templates/issue188656_05.php", options, true);
    }

    public void testIssue188656_06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.tabSize, 4);
        options.put(FmtOptions.expandTabToSpaces, true);
        options.put(FmtOptions.continuationIndentSize, 4);
        options.put(FmtOptions.indentSize, 4);
        options.put(FmtOptions.initialIndent, 4);
        options.put(FmtOptions.spacesPerTab, 4);
        reformatFileContents("testfiles/formatting/templates/issue188656_06.php", options, true);
    }

    public void testIssue188656_07() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.tabSize, 4);
        options.put(FmtOptions.expandTabToSpaces, true);
        options.put(FmtOptions.continuationIndentSize, 4);
        options.put(FmtOptions.indentSize, 4);
        options.put(FmtOptions.initialIndent, 4);
        options.put(FmtOptions.spacesPerTab, 4);
        reformatFileContents("testfiles/formatting/templates/issue188656_07.php", options, true);
    }

    public void testIssue188656_08() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.tabSize, 4);
        options.put(FmtOptions.expandTabToSpaces, true);
        options.put(FmtOptions.continuationIndentSize, 4);
        options.put(FmtOptions.indentSize, 4);
        options.put(FmtOptions.initialIndent, 4);
        options.put(FmtOptions.spacesPerTab, 4);
        reformatFileContents("testfiles/formatting/templates/issue188656_08.php", options, true);
    }

    public void testIssue188656_09() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.tabSize, 4);
        options.put(FmtOptions.expandTabToSpaces, true);
        options.put(FmtOptions.continuationIndentSize, 4);
        options.put(FmtOptions.indentSize, 4);
        options.put(FmtOptions.initialIndent, 4);
        options.put(FmtOptions.spacesPerTab, 4);
        reformatFileContents("testfiles/formatting/templates/issue188656_09.php", options, true);
    }

    public void testIssue191565_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.tabSize, 4);
        options.put(FmtOptions.expandTabToSpaces, false);
        options.put(FmtOptions.continuationIndentSize, 4);
        options.put(FmtOptions.spacesPerTab, 4);
        reformatFileContents("testfiles/formatting/templates/issue191565_01.php", options, true);
    }

    public void testIssue192220() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue192220.php", options, true);
    }

    public void testIssue198616() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/templates/issue198616.php", options, true);
    }

    public void testIssue187757() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue187757.php", options, true);
    }
}
