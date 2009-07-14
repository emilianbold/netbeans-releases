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
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.NewWebProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewWebProjectServerSettingsStepOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author dkolar
 */
public class WebVWJSFProjectValidation extends WebProjectValidationEE5 {

    static {
        PROJECT_NAME = "WebVWJSFProject";
//        PROJECT_FOLDER = PROJECT_LOCATION + File.separator + PROJECT_NAME;
    }

    // name of sample project
    
    protected static String URL_PATTERN_NULL = "The URL Pattern has to be entered.";
    protected static String URL_PATTERN_INVALID = "The URL Pattern is not valid.";

    /** Need to be defined because of JUnit */
    public WebVWJSFProjectValidation(String name) {
        super(name);
    }

    /** Need to be defined because of JUnit */
    public WebVWJSFProjectValidation() {
        super();
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(WebVWJSFProjectValidation.class);
        conf = addServerTests(Server.GLASSFISH, conf, 
        "testPreconditions", "testNewVWJSFWebProject", "testRedeployProject",
                "testCleanAndBuildProject", "testCompileAllJSP", "testStopServer");
        conf = conf.enableModules(".*").clusters(".*");
        return NbModuleSuite.create(conf);        
//        suite.addTest(new WebVWJSFProjectValidation("testNewJSP"));
//        suite.addTest(new WebVWJSFProjectValidation("testNewJSP2"));
//        suite.addTest(new WebVWJSFProjectValidation("testJSPNavigator"));
//        suite.addTest(new WebVWJSFProjectValidation("testNewServlet"));
//        suite.addTest(new WebVWJSFProjectValidation("testNewServlet2"));
//        suite.addTest(new WebVWJSFProjectValidation("testCompileJSP"));
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
//        suite.addTest(new WebVWJSFProjectValidation("testStartServer"));
//        suite.addTest(new WebVWJSFProjectValidation("testBrowserSettings"));
//        suite.addTest(new WebVWJSFProjectValidation("testFinish"));
    }

    /** Test creation of web project.
     * - open New Project wizard from main menu (File|New Project)
     * - select Web|Web Application
     * - in the next panel type project name and project location
     * - in next panel sets server to Glassfish and J2EE version to Java EE 5
     * - in Framework panel set Visual JSF framework
     * - finish the wizard
     * - wait until scanning of java files is finished
     * - compare files and directories created
     */
    public void testNewVWJSFWebProject() throws IOException {
        NewProjectWizardOperator projectWizard = NewProjectWizardOperator.invoke();
        String category = Bundle.getStringTrimmed(
                "org.netbeans.modules.web.core.Bundle",
                "Templates/JSP_Servlet");
        projectWizard.selectCategory(category);
        projectWizard.next();
        NewWebProjectNameLocationStepOperator nameStep = new NewWebProjectNameLocationStepOperator();
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

        NewWebProjectVWJSFFrameworkStepOperator frameworkStep = new NewWebProjectVWJSFFrameworkStepOperator();
        // set VW JSF framework and download it from update center
        frameworkStep.tabSelectTheFrameworksYouWantToUseInYourWebApplication().selectCell(0, 0);
        new JButtonOperator(frameworkStep, "Download & Install").doClick();
        try {
            Thread.sleep(180000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        assertTrue("VW JSF framework not present!", frameworkStep.setVWJSFFrameworkCheckbox());
        frameworkStep.txtDefaultJavaPackage().setText("");
        frameworkStep.txtDefaultJavaPackage().typeText("gggg*");
        assertEquals("Default java package name is invalid", frameworkStep.lblDefaultJavaPackageNameIsInvalid().getText());
        frameworkStep.txtDefaultJavaPackage().setText("");
        frameworkStep.txtDefaultJavaPackage().typeText("myproject");
        frameworkStep.txtServletURLMapping().setText("");
        assertEquals(URL_PATTERN_NULL, frameworkStep.lblTheURLPatternHasToBeEntered().getText());
        frameworkStep.txtServletURLMapping().typeText("hhhhhh*");
        assertEquals(URL_PATTERN_INVALID, frameworkStep.lblTheURLPatternIsNotValid().getText());
        frameworkStep.txtServletURLMapping().setText("");
        frameworkStep.txtServletURLMapping().typeText("/faces/*");

        frameworkStep.finish();
        // wait for project creation
        sleep(5000);
        ProjectSupport.waitScanFinished();
        verifyWebPagesNode("WEB-INF|faces-config.xml");//NOI18N
        verifyWebPagesNode("resources");//NOI18N
        verifyWebPagesNode("resources|stylesheet.css");//NOI18N
        verifyWebPagesNode("Page1.jsp");//NOI18N
        verifySourcePackageNode("myproject");
        verifySourcePackageNode("myproject|ApplicationBean1.java");
        verifySourcePackageNode("myproject|Bundle.properties");
        verifySourcePackageNode("myproject|Page1.java");
        verifySourcePackageNode("myproject|RequestBean1.java");
        verifySourcePackageNode("myproject|SessionBean1.java");
    }
}
