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

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.NewWebProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewWebProjectServerSettingsStepOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.ide.ProjectSupport;

/**
 *
 * @author dk198696
 */
public class WebSpringProjectValidation extends WebProjectValidationEE5 {

    static {
        PROJECT_NAME = "WebSpringProject";
//        PROJECT_FOLDER = PROJECT_LOCATION + File.separator + PROJECT_NAME;
    }

    /** Need to be defined because of JUnit */
    public WebSpringProjectValidation(String name) {
        super(name);
    }

    /** Need to be defined because of JUnit */
    public WebSpringProjectValidation() {
        super();
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(WebSpringProjectValidation.class);
        conf = addServerTests(Server.GLASSFISH, conf,
        "testPreconditions", "testNewSpringWebProject", "testRedeployProject", 
                "testCleanAndBuildProject", "testCompileAllJSP", "testStopServer");
        conf = conf.enableModules(".*").clusters(".*");
        return NbModuleSuite.create(conf);        
//        NbTestSuite suite = new NbTestSuite();
//        suite.addTest(new WebSpringProjectValidation("testNewJSP"));
//        suite.addTest(new WebSpringProjectValidation("testNewJSP2"));
//        suite.addTest(new WebSpringProjectValidation("testJSPNavigator"));
//        suite.addTest(new WebSpringProjectValidation("testNewServlet"));
//        suite.addTest(new WebSpringProjectValidation("testNewServlet2"));
//        suite.addTest(new WebSpringProjectValidation("testCompileJSP"));
//        suite.addTest(new WebSpringProjectValidation("testRunProject"));
//        suite.addTest(new WebSpringProjectValidation("testRunJSP"));
//        suite.addTest(new WebSpringProjectValidation("testViewServlet"));
//        suite.addTest(new WebSpringProjectValidation("testRunServlet"));
//        suite.addTest(new WebSpringProjectValidation("testCreateTLD"));
//        suite.addTest(new WebSpringProjectValidation("testCreateTagHandler"));
//        suite.addTest(new WebSpringProjectValidation("testRunTag"));
//        suite.addTest(new WebSpringProjectValidation("testNewHTML"));
//        suite.addTest(new WebSpringProjectValidation("testHTMLNavigator"));
//        suite.addTest(new WebSpringProjectValidation("testRunHTML"));
//        suite.addTest(new WebSpringProjectValidation("testNewSegment"));
//        suite.addTest(new WebSpringProjectValidation("testNewDocument"));
//        suite.addTest(new WebSpringProjectValidation("testStartServer"));
//        suite.addTest(new WebSpringProjectValidation("testBrowserSettings"));
//        suite.addTest(new WebSpringProjectValidation("testFinish"));
//        return suite;
    }

    /** Test creation of web project.
     * - open New Project wizard from main menu (File|New Project)
     * - select Web|Web Application
     * - in the next panel type project name and project location
     * - in next panel sets server to Glassfish and J2EE version to Java EE 5
     * - in Framework panel set Spring framework
     * - finish the wizard
     * - wait until scanning of java files is finished
     * - check index.jsp is opened
     */
    public void testNewSpringWebProject() throws IOException {
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
        String sFolder = getProjectFolder(PROJECT_NAME);
        nameStep.txtProjectLocation().typeText(sFolder);
        nameStep.next();
        NewWebProjectServerSettingsStepOperator serverStep = new NewWebProjectServerSettingsStepOperator();
        serverStep.selectServer(getServerNode(Server.ANY).getText());
        serverStep.selectJavaEEVersion(JAVA_EE_5);
        serverStep.next();

        NewWebProjectSpringFrameworkStepOperator frameworkStep = new NewWebProjectSpringFrameworkStepOperator();
        assertTrue("Spring framework not present!", frameworkStep.setSpringFrameworkCheckbox());
        frameworkStep.setJTextField("");
        assertEquals(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.spring.webmvc.Bundle", "MSG_DispatcherNameIsEmpty"), frameworkStep.lblTheDispatcherNameHasToBeEntered().getText());
        frameworkStep.txtJTextField().typeText("hhhhh*");
        assertEquals("The name entered contains invalid characters for a filename.", frameworkStep.lblTheNameEnteredContainsInvalidCharactersForAFilename().getText());
        frameworkStep.setJTextField("");
        frameworkStep.txtJTextField().typeText("hhhhh");
        frameworkStep.setJTextField2("");
        assertEquals(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.spring.webmvc.Bundle", "MSG_DispatcherMappingPatternIsEmpty"), frameworkStep.lblTheDispatcherMappingHasToBeEntered().getText());
        frameworkStep.txtJTextField2().typeText("hhhh*");
        assertEquals(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.spring.webmvc.Bundle", "MSG_DispatcherMappingPatternIsNotValid"), frameworkStep.lblTheDispatcherMappingIsNotValid().getText());
        frameworkStep.setJTextField2("");
        frameworkStep.txtJTextField2().typeText("*.htm");
        frameworkStep.selectPageLibraries();
//        frameworkStep.cbIncludeJSTL().push();

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
        verifyWebPagesNode("redirect.jsp");
        verifyWebPagesNode("WEB-INF|jsp|index.jsp");//NOI18N
        verifyWebPagesNode("WEB-INF|applicationContext.xml");//NOI18N
        verifyWebPagesNode("WEB-INF|hhhhh-servlet.xml");//NOI18N
        verifyWebPagesNode("WEB-INF|sun-web.xml");//NOI18N
        verifyWebPagesNode("WEB-INF|web.xml");//NOI18N
    }

}
