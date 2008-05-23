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

package org.netbeans.test.web;

import java.io.File;
import java.io.IOException;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.NewWebProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewWebProjectServerSettingsStepOperator;
import org.netbeans.jellytools.actions.EditAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.web.nodes.WebPagesNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;

/**
 *
 * @author dkolar
 */
public class WebStrutsProjectValidation extends WebProjectValidationEE5 {
        protected static ProjectHelper phelper = new ProjectHelper() {

        public Node getSourceNode() {
            return new SourcePackagesNode(PROJECT_NAME);
        }
    };
    

    static {
        PROJECT_NAME = "WebStrutsProject";
        PROJECT_FOLDER = PROJECT_LOCATION + File.separator + PROJECT_NAME;
    }

    // folder of sample project
    protected TestURLDisplayer urlDisplayer;
    private static final String BUILD_SUCCESSFUL = "BUILD SUCCESSFUL";
    protected static int logIdx = 0;

    /** Need to be defined because of JUnit */
    public WebStrutsProjectValidation(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new WebStrutsProjectValidation("testPreconditions"));
        suite.addTest(new WebStrutsProjectValidation("testNewStrutsWebProject"));
//        suite.addTest(new WebVWJSFProjectValidation("testNewJSP"));
//        suite.addTest(new WebVWJSFProjectValidation("testNewJSP2"));
//        suite.addTest(new WebVWJSFProjectValidation("testJSPNavigator"));
//        suite.addTest(new WebVWJSFProjectValidation("testNewServlet"));
//        suite.addTest(new WebVWJSFProjectValidation("testNewServlet2"));
        suite.addTest(new WebStrutsProjectValidation("testBuildProject"));
        suite.addTest(new WebStrutsProjectValidation("testCompileAllJSP"));
//        suite.addTest(new WebVWJSFProjectValidation("testCompileJSP"));
        suite.addTest(new WebStrutsProjectValidation("testCleanProject"));
//        suite.addTest(new WebVWJSFProjectValidation("testRunProject"));
//        suite.addTest(new WebVWJSFProjectValidation("testRunJSP"));
//        suite.addTest(new WebVWJSFProjectValidation("testViewServlet"));
//        suite.addTest(new WebVWJSFProjectValidation("testRunServlet"));
//        suite.addTest(new WebVWJSFProjectValidation("testCreateTLD"));
//        suite.addTest(new WebVWJSFProjectValidation("testCreateTagHandler"));
//        suite.addTest(new WebVWJSFProjectValidation("testRunTag"));
//        suite.addTest(new WebVWJSFProjectValidation("testNewHTML"));
//        suite.addTest(new WebVWJSFProjectValidation("testHTMLNavigator"));
//        suite.addTest(new WebVWJSFProjectValidation("testRunHTML"));
//        suite.addTest(new WebVWJSFProjectValidation("testNewSegment"));
//        suite.addTest(new WebVWJSFProjectValidation("testNewDocument"));
//        suite.addTest(new WebVWJSFProjectValidation("testStopServer"));
//        suite.addTest(new WebVWJSFProjectValidation("testStartServer"));
//        suite.addTest(new WebVWJSFProjectValidation("testBrowserSettings"));
//        suite.addTest(new WebVWJSFProjectValidation("testFinish"));
        return suite;
    }

    /** Test creation of web project.
     * - open New Project wizard from main menu (File|New Project)
     * - select Web|Web Application
     * - in the next panel type project name and project location
     * - in next panel sets server to Glassfish and J2EE version to Java EE 5
     * - in Framework panel set Struts framework
     * - finish the wizard
     * - wait until scanning of java files is finished
     */
    public void testNewStrutsWebProject() throws IOException {
        NewProjectWizardOperator projectWizard = NewProjectWizardOperator.invoke();
        String category = Bundle.getStringTrimmed(
                "org.netbeans.modules.web.core.Bundle",
                "Templates/JSP_Servlet");
        projectWizard.selectCategory(category);
        projectWizard.next();
        NewWebProjectNameLocationStepOperator nameStep =
                new NewWebProjectNameLocationStepOperator();
        nameStep.txtProjectName().setText("");
        nameStep.txtProjectName().typeText(PROJECT_NAME);
        nameStep.txtProjectLocation().setText("");
        nameStep.txtProjectLocation().typeText(getProjectFolder(PROJECT_NAME));
        nameStep.next();
        NewWebProjectServerSettingsStepOperator serverStep = new NewWebProjectServerSettingsStepOperator();
        serverStep.selectServer("GlassFish V2");
        serverStep.selectJavaEEVersion(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.j2ee.common.project.ui.Bundle", "JavaEESpecLevel_50"));
        serverStep.next();

        NewWebProjectStrutsFrameworkStepOperator frameworkStep = new NewWebProjectStrutsFrameworkStepOperator();
        assertTrue("Struts framework not present!", frameworkStep.setStrutsFrameworkCheckbox());
        // set ApplicationResource location
        frameworkStep.cboActionURLPattern().clearText();

        String err1 = frameworkStep.lblTheURLPatternHasToBeEntered().getText();
        assertEquals(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.web.struts.ui.Bundle", "MSG_URLPatternIsEmpty"), err1);
        frameworkStep.cboActionURLPattern().getTextField().typeText("*");
        String err2 = frameworkStep.lblTheURLPatternIsNotValid().getText();
        assertEquals(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.web.struts.ui.Bundle", "MSG_URLPatternIsNotValid"), err2);
        frameworkStep.cboActionURLPattern().getTextField().typeText(".do");
        frameworkStep.cbAddStrutsTLDs().push();
        frameworkStep.finish();
        frameworkStep.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 60000);
        frameworkStep.waitClosed();
        // Opening Projects
        String openingProjectsTitle = Bundle.getString(
                "org.netbeans.modules.project.ui.Bundle",
                "LBL_Opening_Projects_Progress");
        try {
            // wait at most 60 second until progress dialog dismiss
            NbDialogOperator openingOper = new NbDialogOperator(openingProjectsTitle);
            frameworkStep.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 60000);
            openingOper.waitClosed();
        } catch (TimeoutExpiredException e) {
            // ignore when progress dialog was closed before we started to wait for it
        }
        ProjectSupport.waitScanFinished();
        // Check project contains all needed files.
        WebPagesNode webPages = new WebPagesNode(PROJECT_NAME);
        new Node(webPages, "welcomeStruts.jsp");
        Node strutsConfig = new Node(webPages, "WEB-INF|struts-config.xml");
        new OpenAction().performAPI(strutsConfig);
        webPages.setComparator(new DefaultStringComparator(true, true));
        Node webXML = new Node(webPages, "WEB-INF|web.xml");
        new EditAction().performAPI(webXML);
        EditorOperator webXMLEditor = new EditorOperator("web.xml");
        String expected = "<servlet-class>org.apache.struts.action.ActionServlet</servlet-class>";
        assertTrue("ActionServlet should be created in web.xml.", webXMLEditor.getText().indexOf(expected) > -1);
        webXMLEditor.replace("index.jsp", "login.jsp");
        webXMLEditor.save();
        ref(Util.dumpProjectView(PROJECT_NAME));
        compareReferenceFiles();
//        assertEquals(true,ProjectSupport.closeProject(PROJECT_NAME));
    }
}