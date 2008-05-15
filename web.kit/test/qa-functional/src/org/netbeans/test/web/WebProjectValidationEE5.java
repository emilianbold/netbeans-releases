/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.SwingUtilities;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.NewWebProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewWebProjectServerSettingsStepOperator;
import org.netbeans.jellytools.actions.EditAction;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.j2ee.nodes.J2eeServerNode;
import org.netbeans.jellytools.modules.web.nodes.WebPagesNode;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;

/**
 */
public class WebProjectValidationEE5 extends WebProjectValidation {

    protected static ProjectHelper phelper = new ProjectHelper() {

        public Node getSourceNode() {
            return new SourcePackagesNode(PROJECT_NAME);
        }
    };
    

    static {
        PROJECT_NAME = "WebJ2EE5Project"; // NOI18N
        PROJECT_FOLDER = PROJECT_LOCATION + File.separator + PROJECT_NAME;
    }

    // name of sample project
    protected static String PROJECT_NAME_JSF = "WebJSFProject";
    protected static String PROJECT_NAME_STRUTS = "WebStrutsProject";
    protected static String PROJECT_NAME_VWJSF = "WebVWJSFProject";// NOI18N
    protected static String PROJECT_NAME_SPRING = "WebSpringProject";// NOI18N
    protected static String URL_PATTERN_NULL = "The URL Pattern has to be entered.";
    protected static String URL_PATTERN_INVALID = "The URL Pattern is not valid.";
    // folder of sample project
    protected TestURLDisplayer urlDisplayer;
    private static final String BUILD_SUCCESSFUL = "BUILD SUCCESSFUL";
    private ServerInstance server;
    protected static int logIdx = 0;

    /** Need to be defined because of JUnit */
    public WebProjectValidationEE5(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new WebProjectValidationEE5("testPreconditions"));
        suite.addTest(new WebProjectValidationEE5("testNewWebProject"));
        suite.addTest(new WebProjectValidationEE5("testNewJSFWebProject"));
        suite.addTest(new WebProjectValidationEE5("testNewVWJSFWebProject"));
        suite.addTest(new WebProjectValidationEE5("testNewSpringWebProject"));
        suite.addTest(new WebProjectValidationEE5("testNewStrutsWebProject"));
//        suite.addTest(new WebProjectValidationEE5("testNewJSP"));
//        suite.addTest(new WebProjectValidationEE5("testNewJSP2"));
////        suite.addTest(new WebProjectValidationEE5("testJSPNavigator"));
//        suite.addTest(new WebProjectValidationEE5("testNewServlet"));
//        suite.addTest(new WebProjectValidationEE5("testNewServlet2"));
//        suite.addTest(new WebProjectValidationEE5("testBuildProject"));
//        suite.addTest(new WebProjectValidationEE5("testCompileAllJSP"));
//        suite.addTest(new WebProjectValidationEE5("testCompileJSP"));
//        suite.addTest(new WebProjectValidationEE5("testCleanProject"));
//        suite.addTest(new WebProjectValidationEE5("testRunProject"));
//        suite.addTest(new WebProjectValidationEE5("testRunJSP"));
//        suite.addTest(new WebProjectValidationEE5("testViewServlet"));
//        suite.addTest(new WebProjectValidationEE5("testRunServlet"));
//        suite.addTest(new WebProjectValidationEE5("testCreateTLD"));
//        suite.addTest(new WebProjectValidationEE5("testCreateTagHandler"));
//        suite.addTest(new WebProjectValidationEE5("testRunTag"));
//        suite.addTest(new WebProjectValidationEE5("testNewHTML"));
////        suite.addTest(new WebProjectValidationEE5("testHTMLNavigator"));
//        suite.addTest(new WebProjectValidationEE5("testRunHTML"));
//        suite.addTest(new WebProjectValidationEE5("testNewSegment"));
//        suite.addTest(new WebProjectValidationEE5("testNewDocument"));
//        suite.addTest(new WebProjectValidationEE5("testStopServer"));
//        suite.addTest(new WebProjectValidationEE5("testStartServer"));
//        suite.addTest(new WebProjectValidationEE5("testBrowserSettings"));
//        suite.addTest(new WebProjectValidationEE5("testFinish"));
        return suite;
    }

    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
    }

    @Override
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
        JemmyProperties.setCurrentTimeout(
                "ComponentOperator.WaitComponentTimeout", 180000);
        JemmyProperties.setCurrentTimeout(
                "FrameWaiter.WaitFrameTimeout", 180000);
        JemmyProperties.setCurrentTimeout(
                "DialogWaiter.WaitDialogTimeout", 180000);
        server = ServerInstance.getDefault();
    }

    @Override
    public void tearDown() {
        logAndCloseOutputs();
    }

    /** checks if the Server ports are not used */
    public void testPreconditions() throws Exception {
        URLConnection connection = server.getServerURL().openConnection();
        try {
            connection.connect();
            fail("Port: " + server.getServerURL() + " is used by different server.");
        } catch (ConnectException e) {
        }
        URL url = new URL("http://localhost:8025");
        connection = url.openConnection();
        try {
            connection.connect();
            fail("Connection to http://localhost:8025 established, but tomcat should not be running.");
        } catch (ConnectException e) {
        }
    }

    /** Test creation of web project.
     * - open New Project wizard from main menu (File|New Project)
     * - select Web|Web Application
     * - in the next panel type project name and project location
     * - in next panel sets server to Glassfish and J2EE version to Java EE 5     
     * - finish the wizard
     * - wait until scanning of java files is finished
     * - check index.jsp is opened
     */
    public void testNewWebProject() throws IOException {
        installJemmyQueue();
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
        nameStep.txtProjectLocation().typeText(PROJECT_LOCATION);
        nameStep.next();
        NewWebProjectServerSettingsStepOperator serverStep = new NewWebProjectServerSettingsStepOperator();
        serverStep.selectServer("GlassFish V2");
        serverStep.selectJavaEEVersion(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.j2ee.common.project.ui.Bundle", "JavaEESpecLevel_50"));
        serverStep.next();
        NewWebProjectJSFFrameworkStepOperator frameworkStep = new NewWebProjectJSFFrameworkStepOperator();
        assertTrue("Struts framework not present!", frameworkStep.setStrutsFrameworkCheckbox());
        assertTrue("Spring framework not present!", frameworkStep.setSpringFrameworkCheckbox());
        assertTrue("VWJSF framework not present!", frameworkStep.setVWJSFFrameworkCheckbox());
        assertTrue("JSF framework not present!", frameworkStep.setJSFFrameworkCheckbox());

        frameworkStep.finish();
        // wait for project creation
        sleep(5000);
        ProjectSupport.waitScanFinished();
//        EditorWindowOperator.getEditor("index.jsp");//NOI18N
//        ProjectSupport.waitScanFinished();
//        // XXX HACK
//        WebPagesNode webPages = new WebPagesNode(PROJECT_NAME);
//        new Node(webPages,"index.jsp");//NOI18N
//        new Node(webPages,"WEB-INF|web.xml");//NOI18N
//        new Node(webPages,"META-INF|context.xml");//NOI18N
//        ref(Util.dumpProjectView(PROJECT_NAME));
//        compareReferenceFiles();
    }

    public void testNewDocument() throws IOException {
        Node sample1Node = new Node(new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME), "Web Pages");
        // create a new class
        new ActionNoBlock(null, "New|JSP").perform(sample1Node);
        // WORKAROUND
        new EventTool().waitNoEvent(1000);
        WizardOperator newFileWizard = new WizardOperator("New JSP File");
        new JTextFieldOperator(newFileWizard).typeText("document");
        new JRadioButtonOperator(newFileWizard, 1).changeSelection(true);
        newFileWizard.finish();
        // check class is opened in Editor and then close all documents
        new EditorOperator("document.jspx").close();
        ref(Util.dumpProjectView(PROJECT_NAME));
        new Node(new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME), "Web Pages|document.jspx");
    //compareReferenceFiles();
    //compareDD();
    }

    /** Test creation of web project.
     * - open New Project wizard from main menu (File|New Project)
     * - select Web|Web Application
     * - in the next panel type project name and project location
     * - in next panel sets server to Glassfish and J2EE version to Java EE 5
     * - in next panel chooses JSF framework to be added
     * - finish the wizard
     * - wait until scanning of java files is finished
     * - check index.jsp is opened
     */
    public void testNewJSFWebProject() throws IOException {
        NewProjectWizardOperator projectWizard = NewProjectWizardOperator.invoke();
        String category = Bundle.getStringTrimmed(
                "org.netbeans.modules.web.core.Bundle",
                "Templates/JSP_Servlet");
        projectWizard.selectCategory(category);
        projectWizard.next();
        NewWebProjectNameLocationStepOperator nameStep = new NewWebProjectNameLocationStepOperator();
        nameStep.txtProjectName().setText("");
        nameStep.txtProjectName().typeText(PROJECT_NAME_JSF);
        nameStep.txtProjectLocation().setText("");
        nameStep.txtProjectLocation().typeText(getProjectFolder(PROJECT_NAME_JSF));
        nameStep.next();
        NewWebProjectServerSettingsStepOperator serverStep = new NewWebProjectServerSettingsStepOperator();
        serverStep.selectServer("GlassFish V2");
        serverStep.selectJavaEEVersion(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.j2ee.common.project.ui.Bundle", "JavaEESpecLevel_50"));
        serverStep.next();

        NewWebProjectJSFFrameworkStepOperator frameworkStep = new NewWebProjectJSFFrameworkStepOperator();
        assertTrue("JSF framework not present!", frameworkStep.setJSFFrameworkCheckbox());
        frameworkStep.txtServletURLMapping().setText("");
        assertEquals(URL_PATTERN_NULL, frameworkStep.lblTheURLPatternHasToBeEntered().getText());
        frameworkStep.txtServletURLMapping().typeText("hhhhhh*");
        assertEquals(URL_PATTERN_INVALID, frameworkStep.lblTheURLPatternIsNotValid().getText());
        frameworkStep.txtServletURLMapping().setText("");
        frameworkStep.txtServletURLMapping().typeText("/faces/*");
        frameworkStep.selectPageLibraries();
        frameworkStep.rbCreateNewLibrary().push();
        assertEquals("\"\" is not valid path for a folder.", frameworkStep.lblIsNotValidPathForAFolder().getText());
        frameworkStep.rbRegisteredLibraries().push();
        frameworkStep.rbDoNotAppendAnyLibrary().push();

        frameworkStep.finish();
        // wait for project creation
        sleep(5000);
        ProjectSupport.waitScanFinished();
//        EditorWindowOperator.getEditor("index.jsp");//NOI18N
//        ProjectSupport.waitScanFinished();
//        // XXX HACK
        WebPagesNode webPages = new WebPagesNode(PROJECT_NAME_JSF);
        new Node(webPages, "welcomeJSF.jsp");//NOI18N
        new Node(webPages, "WEB-INF|web.xml");//NOI18N
//        new Node(webPages,"META-INF|context.xml");//NOI18N
//        ref(Util.dumpProjectView(PROJECT_NAME));
//        compareReferenceFiles();
        ProjectSupport.closeProject(PROJECT_NAME_JSF);
    }

    /** Test creation of web project.
     * - open New Project wizard from main menu (File|New Project)
     * - select Web|Web Application
     * - in the next panel type project name and project location
     * - in next panel sets server to Glassfish and J2EE version to Java EE 5
     * - in next panel chooses JSF framework to be added
     * - finish the wizard
     * - wait until scanning of java files is finished
     * - check index.jsp is opened
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
        nameStep.txtProjectName().typeText(PROJECT_NAME_VWJSF);
        nameStep.txtProjectLocation().setText("");
        nameStep.txtProjectLocation().typeText(getProjectFolder(PROJECT_NAME_VWJSF));
        nameStep.next();
        NewWebProjectServerSettingsStepOperator serverStep = new NewWebProjectServerSettingsStepOperator();
        serverStep.selectServer("GlassFish V2");
        serverStep.selectJavaEEVersion(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.j2ee.common.project.ui.Bundle", "JavaEESpecLevel_50"));
        serverStep.next();

        NewWebProjectVWJSFFrameworkStepOperator frameworkStep = new NewWebProjectVWJSFFrameworkStepOperator();
        assertTrue("VW JSF framework not present!", frameworkStep.setVWJSFFrameworkCheckbox());
        frameworkStep.txtDefaultJavaPackage().setText("");
        frameworkStep.txtDefaultJavaPackage().typeText("gggg*");
        assertEquals("Default java package name is invalid", frameworkStep.lblDefaultJavaPackageNameIsInvalid().getText());
        frameworkStep.txtDefaultJavaPackage().setText("");
        frameworkStep.txtDefaultJavaPackage().typeText("myproject");
        System.out.println("text:" + frameworkStep.txtServletURLMapping().getText());
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
//        EditorWindowOperator.getEditor("Page1.jsp");//NOI18N
        // XXX HACK
        WebPagesNode webPages = new WebPagesNode(PROJECT_NAME_VWJSF);
        new Node(webPages, "Page1.jsp");//NOI18N
        new Node(webPages, "WEB-INF|web.xml");//NOI18N
//        new Node(webPages,"META-INF|context.xml");//NOI18N
//        ref(Util.dumpProjectView(PROJECT_NAME));
//        compareReferenceFiles();
        ProjectSupport.closeProject(PROJECT_NAME_VWJSF);
    }

    /** Create web application with struts support and check correctness. */
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
        nameStep.txtProjectName().typeText(PROJECT_NAME_STRUTS);
        nameStep.txtProjectLocation().setText("");
        nameStep.txtProjectLocation().typeText(getProjectFolder(PROJECT_NAME_STRUTS));
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
        WebPagesNode webPages = new WebPagesNode(PROJECT_NAME_STRUTS);
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
        ProjectSupport.closeProject(PROJECT_NAME_STRUTS);
    }

    /** Create web application with spring support and check correctness. */
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
        nameStep.txtProjectName().typeText(PROJECT_NAME_SPRING);
        nameStep.txtProjectLocation().setText("");
        nameStep.txtProjectLocation().typeText(getProjectFolder(PROJECT_NAME_SPRING));
        nameStep.next();
        NewWebProjectServerSettingsStepOperator serverStep = new NewWebProjectServerSettingsStepOperator();
        serverStep.selectServer("GlassFish V2");
        serverStep.selectJavaEEVersion(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.j2ee.common.project.ui.Bundle", "JavaEESpecLevel_50"));
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
        frameworkStep.cbIncludeJSTL().push();

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
        ProjectSupport.closeProject(PROJECT_NAME_SPRING);
    // Check project contains all needed files.
//        WebPagesNode webPages = new WebPagesNode(PROJECT_NAME_SPRING);
//        System.out.println("+");
//        new Node(webPages, "redirect.jsp");
//        System.out.println("+");
//        new Node(webPages,"WEB-INF|jsp|index.jsp");//NOI18N
//        System.out.println("+");        
//        new Node(webPages,"WEB-INF|applicationContext.xml");//NOI18N
//        System.out.println("+");
//        new Node(webPages,"WEB-INF|hhhhh-servlet.xml");//NOI18N
//        System.out.println("+");
//        new Node(webPages,"WEB-INF|sun-web.xml");//NOI18N
//        System.out.println("+");
//        new Node(webPages,"WEB-INF|web.xml");//NOI18N
//        System.out.println("+");
//        new OpenAction().performAPI(strutsConfig);
//        System.out.println("+");
//        webPages.setComparator(new DefaultStringComparator(true, true));
//        System.out.println("+");
//        Node webXML = new Node(webPages, "WEB-INF|web.xml");
//        System.out.println("+");
//        new EditAction().performAPI(webXML);
//        System.out.println("+");
//        EditorOperator webXMLEditor = new EditorOperator("web.xml");
//        System.out.println("+");
//        String expected = "<servlet-class>org.apache.struts.action.ActionServlet</servlet-class>";
//        assertTrue("ActionServlet should be created in web.xml.", webXMLEditor.getText().indexOf(expected) > -1);
//        webXMLEditor.replace("index.jsp", "login.jsp");
//        webXMLEditor.save();
    }

    //********************************************************
    protected void sleep(int milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException ex) {
            throw new JemmyException("Interrupted", ex);
        }
    }

    private void logAndCloseOutputs() {
        OutputTabOperator outputTab;
        long timeout = JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        try {
            do {
                try {
                    outputTab = new OutputTabOperator("");
                } catch (TimeoutExpiredException e) {
                    // probably no more tabs so ignore it and continue
                    break;
                }
                String logName = "Output" + logIdx++ + ".log";
                log(logName, outputTab.getName() + "\n-------------\n\n" + outputTab.getText());
                outputTab.close();
            } while (true);
        } finally {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", timeout);
        }
    }

    private void waitBuildSuccessful() {
        OutputTabOperator console = new OutputTabOperator(PROJECT_NAME);
        console.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 180000);
        console.waitText(BUILD_SUCCESSFUL);
    }

    private void initDisplayer() {
        if (urlDisplayer == null) {
            urlDisplayer = TestURLDisplayer.getInstance();
        }
        urlDisplayer.invalidateURL();
    }

    private void assertDisplayerContent(String substr) {
        try {
            urlDisplayer.waitURL();
        } catch (InterruptedException ex) {
            throw new JemmyException("Waiting interrupted.", ex);
        }
        String page = urlDisplayer.readURL();
        boolean contains = page.indexOf(substr) > -1;
        if (!contains) {
            log("DUMP OF: " + urlDisplayer.getURL() + "\n");
            log(page);
        }
        assertTrue("The '" + urlDisplayer.getURL() + "' page does not contain '" + substr + "'", contains);
    }

    private void assertContains(String text, String value) {
        assertTrue("Assertation failed, cannot find:\n" + value + "\nin the following text:\n" + text, text.contains(value));
    }

    public String getProjectFolder(String project) {
        return PROJECT_LOCATION + File.separator + project;
    }

    protected void installJemmyQueue() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    QueueTool.installQueue();
                }
            });
        } catch (Exception ex) {
            throw new JemmyException("Cannot install Jemmy Queue.", ex);
        }
    }

    private static class ServerInstance {

        private String host;
        private int serverPort;
        private String nodeName;
        private String userName;
        private String password;
        private URL serverURL;
        private static ServerInstance instance;

        private ServerInstance() {
        }

        public static ServerInstance getDefault() {
            if (instance == null) {
                instance = new ServerInstance();
                instance.host = "localhost";
                instance.serverPort = 8080;
                instance.nodeName = "GlassFish V2";
                instance.userName = "admin";
                instance.password = "adminadmin";
            }
            return instance;
        }

        public URL getServerURL() {
            if (serverURL == null) {
                try {
                    serverURL = new URL("http", host, serverPort, "");
                } catch (MalformedURLException mue) {
                    throw new JemmyException("Cannot create server URL.", mue);
                }
            }
            return serverURL;
        }

        public J2eeServerNode getServerNode() {
            return J2eeServerNode.invoke(nodeName);
        }

        public void stop() {
            getServerNode().stop();
        }

        public void start() {
            getServerNode().start();
        }
    }
}

