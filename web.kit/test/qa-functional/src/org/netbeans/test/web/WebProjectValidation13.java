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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.test.web;

import java.io.File;
import java.io.IOException;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.NewProjectAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;

/**
 *
 * @author  lm97939
 */
public class WebProjectValidation13 extends WebProjectValidation {
    static {
        PROJECT_NAME = "SampleProject13"; // NOI18N
        PROJECT_FOLDER = PROJECT_LOCATION+File.separator+PROJECT_NAME;
    }

    /** Need to be defined because of JUnit */
    public WebProjectValidation13(String name) {
        super(name);
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
        // run only selected test case
        //junit.textui.TestRunner.run(new MyModuleValidation("testT2"));
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new WebProjectValidation13("testNewWebProject"));
        suite.addTest(new WebProjectValidation13("testNewJSP"));
        suite.addTest(new WebProjectValidation13("testNewJSP2"));
        suite.addTest(new WebProjectValidation13("testNewServlet"));
        suite.addTest(new WebProjectValidation13("testNewServlet2"));
        suite.addTest(new WebProjectValidation13("testBuildProject"));
        suite.addTest(new WebProjectValidation13("testCompileAllJSP"));
        suite.addTest(new WebProjectValidation13("testCompileJSP"));
        suite.addTest(new WebProjectValidation13("testCleanProject"));
        suite.addTest(new WebProjectValidation13("testRunProject"));
        suite.addTest(new WebProjectValidation13("testRunJSP"));
        suite.addTest(new WebProjectValidation13("testRunServlet"));
        suite.addTest(new WebProjectValidation13("testCreateTLD"));
        suite.addTest(new WebProjectValidation13("testCreateTagHandler"));
        suite.addTest(new WebProjectValidation13("testRunTag"));
        suite.addTest(new WebProjectValidation13("testNewHTML"));
        suite.addTest(new WebProjectValidation13("testRunHTML"));
        suite.addTest(new WebProjectValidation13("testNewSegment"));
        suite.addTest(new WebProjectValidation13("testNewDocument"));
        suite.addTest(new WebProjectValidation13("testStopServer"));
        suite.addTest(new WebProjectValidation13("testStartServer"));
        suite.addTest(new WebProjectValidation13("testFinish"));
        return suite;
    }
    
    /** Test creation of web project.
     * - open New Project wizard from main menu (File|New Project)
     * - select Web|Web Application
     * - in the next panel type project name and project location
     * - finish the wizard
     * - wait until scanning of java files is finished
     * - check index.jsp is opened
     */
    @Override
    public void testNewWebProject() throws IOException {
        installJemmyQueue();
        new NewProjectAction().perform();
        NewProjectWizardOperator projectWizard = new NewProjectWizardOperator();
        String sWeb = Bundle.getStringTrimmed(
                "org.netbeans.modules.web.core.Bundle",
                "Templates/JSP_Servlet");
        projectWizard.selectCategory(sWeb);
        String sWeb_Application = Bundle.getStringTrimmed(
                "org.netbeans.modules.web.project.ui.wizards.Bundle",
                "Templates/Project/Web/emptyWeb.xml");
        projectWizard.selectProject(sWeb_Application);
        projectWizard.next();
        NewWebProjectNameLocationStepOperator nameStep =
                new NewWebProjectNameLocationStepOperator();
        nameStep.txtProjectName().setText("");
        nameStep.txtProjectName().typeText(PROJECT_NAME);
        nameStep.txtProjectLocation().setText("");
        nameStep.txtProjectLocation().typeText(PROJECT_LOCATION);
        String sJ2EE_1_3 = Bundle.getStringTrimmed(
                "org.netbeans.modules.web.project.ui.wizards.Bundle",
                "J2EESpecLevel_13");
        nameStep.selectJ2EEVersion(sJ2EE_1_3);
        nameStep.finish();
        Timeouts timeouts = nameStep.getTimeouts().cloneThis();
        nameStep.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 60000);
        nameStep.waitClosed();
        nameStep.setTimeouts(timeouts);
        // wait for project creation
        sleep(5000);
        ProjectSupport.waitScanFinished();
        // wait for project creation
        new EditorWindowOperator().getEditor("index.jsp");//NOI18N
        // HACK
        Node webPages = new Node(new ProjectsTabOperator().
                getProjectRootNode(PROJECT_NAME),"Web Pages");
        new Node(webPages,"index.jsp");//NOI18N
        new Node(webPages,"WEB-INF|web.xml");//NOI18N
        new Node(webPages,"META-INF|context.xml");//NOI18N
        ref(Util.dumpProjectView(PROJECT_NAME));
        compareReferenceFiles();
    }
    
    @Override
    protected String getTagHandlerCode() {
        return "try { JspWriter out = pageContext.getOut();\n out.print(\"TagOutput\");\n} catch (java.io.IOException e) {} \n";
    }
    
    @Override
    public void testCreateTagHandler() {
        // workaround due to issue #46073
        new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME).select();
        
        new ActionNoBlock("File|New File", null).perform();
        // WORKAROUND
        new EventTool().waitNoEvent(1000);
        WizardOperator newFileWizard = new WizardOperator("New File");
        new JComboBoxOperator(newFileWizard).selectItem(PROJECT_NAME);
        new Node(new JTreeOperator(newFileWizard), "Web").select();
        new JListOperator(newFileWizard, 1).selectItem("Tag Handler");
        newFileWizard.next();
        JTextFieldOperator txtName = new JTextFieldOperator(newFileWizard);
        // clear text field
        txtName.setText("");
        txtName.typeText("MyTag");
        JComboBoxOperator pkg = new JComboBoxOperator(newFileWizard,1);
        pkg.clearText();
        pkg.typeText("tags");
        newFileWizard.next();
        new JButtonOperator(newFileWizard).push();
        NbDialogOperator dialog = new NbDialogOperator("Browse Files");
        new Node(new JTreeOperator(dialog),"Web Pages|WEB-INF|tlds|MyTags.tld").select();
        new JButtonOperator(dialog,"Select File").push();
        newFileWizard.finish();
        // HACK
        new Node(phelper.getSourceNode(), "tags|MyTag.java");
        // check class is opened in Editor and then close it
        EditorOperator editor = new EditorOperator("MyTag.java");
        editor.replace("// TODO: code that performs other operations in doStartTag", getTagHandlerCode());
        editor.saveDocument();
        editor.close();
        ref(Util.dumpProjectView(PROJECT_NAME));
        //compareReferenceFiles();
    }
}
