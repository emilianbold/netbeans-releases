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
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.SwingUtilities;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.NewWebProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewWebProjectServerSettingsStepOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.modules.j2ee.nodes.J2eeServerNode;
import org.netbeans.jellytools.modules.web.nodes.WebPagesNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;

/**
 *
 * @author dk198696
 */
public class WebSpringProjectValidation extends WebProjectValidationEE5 {
        protected static ProjectHelper phelper = new ProjectHelper() {

        public Node getSourceNode() {
            return new SourcePackagesNode(PROJECT_NAME);
        }
    };
    

    static {
        PROJECT_NAME = "WebSpringProject";
        PROJECT_FOLDER = PROJECT_LOCATION + File.separator + PROJECT_NAME;
    }

    // folder of sample project
    protected TestURLDisplayer urlDisplayer;
    private static final String BUILD_SUCCESSFUL = "BUILD SUCCESSFUL";
    private ServerInstance server;
    protected static int logIdx = 0;

    /** Need to be defined because of JUnit */
    public WebSpringProjectValidation(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new WebSpringProjectValidation("testPreconditions"));
        suite.addTest(new WebSpringProjectValidation("testNewSpringWebProject"));
//        suite.addTest(new WebVWJSFProjectValidation("testNewJSP"));
//        suite.addTest(new WebVWJSFProjectValidation("testNewJSP2"));
//        suite.addTest(new WebVWJSFProjectValidation("testJSPNavigator"));
//        suite.addTest(new WebVWJSFProjectValidation("testNewServlet"));
//        suite.addTest(new WebVWJSFProjectValidation("testNewServlet2"));
        suite.addTest(new WebSpringProjectValidation("testBuildProject"));
        suite.addTest(new WebSpringProjectValidation("testCompileAllJSP"));
//        suite.addTest(new WebVWJSFProjectValidation("testCompileJSP"));
        suite.addTest(new WebSpringProjectValidation("testCleanProject"));
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
        nameStep.txtProjectLocation().typeText(getProjectFolder(PROJECT_NAME));
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
    // Check project contains all needed files.
        WebPagesNode webPages = new WebPagesNode(PROJECT_NAME);
        new Node(webPages, "redirect.jsp");
        new Node(webPages,"WEB-INF|jsp|index.jsp");//NOI18N
        new Node(webPages,"WEB-INF|applicationContext.xml");//NOI18N
        new Node(webPages,"WEB-INF|hhhhh-servlet.xml");//NOI18N
        new Node(webPages,"WEB-INF|sun-web.xml");//NOI18N
        new Node(webPages,"WEB-INF|web.xml");//NOI18N
        ref(Util.dumpProjectView(PROJECT_NAME));
        compareReferenceFiles();
//        ProjectSupport.closeProject(PROJECT_NAME);
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
