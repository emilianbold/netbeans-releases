/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.actions;

import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.csl.core.CslEditorKit;
import org.netbeans.modules.php.editor.PHPCodeCompletionTestBase;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class ToggleBlockCommentActionTest extends PHPCodeCompletionTestBase {

    public ToggleBlockCommentActionTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
	//System.setProperty("org.netbeans.editor.linewrap.disable", "true");
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

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testIssue198269_01()throws Exception {
        testInFile("testfiles/actions/issue198269_01.php");
    }

    public void testIssue198269_02()throws Exception {
        testInFile("testfiles/actions/issue198269_02.php");
    }

    public void testIssue198269_03()throws Exception {
        testInFile("testfiles/actions/issue198269_03.php");
    }

    public void testIssue198269_04()throws Exception {
        testInFile("testfiles/actions/issue198269_04.php");
    }

    public void testIsue207153()throws Exception {
        testInFile("testfiles/actions/issue207153.php");
    }

    public void testIssue213706_01()throws Exception {
        testInFile("testfiles/actions/issue213706_01.php");
    }

    public void testIssue213706_02()throws Exception {
        testInFile("testfiles/actions/issue213706_02.php");
    }

    public void testIssue213706_03()throws Exception {
        testInFile("testfiles/actions/issue213706_03.php");
    }

    public void testIssue218830_01()throws Exception {
        testInFile("testfiles/actions/toggleComment/issue218830_01.php");
    }

    public void testIssue218830_02()throws Exception {
        testInFile("testfiles/actions/toggleComment/issue218830_02.php");
    }

    public void testIssue218830_03()throws Exception {
        testInFile("testfiles/actions/toggleComment/issue218830_03.php");
    }

    public void testIssue218830_04()throws Exception {
        testInFile("testfiles/actions/toggleComment/issue218830_04.php");
    }

    public void testIssue218830_05()throws Exception {
        testInFile("testfiles/actions/toggleComment/issue218830_05.php");
    }

    protected void testInFile(String file) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);
        String source = readFile(fo);

        int sourcePos = source.indexOf('^');
        assertNotNull(sourcePos);
        String sourceWithoutMarker = source.substring(0, sourcePos) + source.substring(sourcePos+1);

        JEditorPane ta = getPane(sourceWithoutMarker);
        Caret caret = ta.getCaret();
        caret.setDot(sourcePos);
        BaseDocument doc = (BaseDocument) ta.getDocument();

        runKitAction(ta, CslEditorKit.toggleCommentAction, null);

        doc.getText(0, doc.getLength());
        doc.insertString(caret.getDot(), "^", null);

        String target = doc.getText(0, doc.getLength());
        assertDescriptionMatches(file, target, false, ".toggleComment");
    }
}
