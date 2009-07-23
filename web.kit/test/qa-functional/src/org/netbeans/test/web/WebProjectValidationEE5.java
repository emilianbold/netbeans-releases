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
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.NewWebProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewWebProjectServerSettingsStepOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.actions.RedeployProjectAction;
import org.netbeans.jellytools.modules.j2ee.nodes.J2eeServerNode;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.ide.ProjectSupport;

/**
 */
public class WebProjectValidationEE5 extends WebProjectValidation {

    static {
        PROJECT_NAME = "WebJ2EE5Project"; // NOI18N
    }

    private ServerInstance server;

    /** Need to be defined because of JUnit */
    public WebProjectValidationEE5(String name) {
        super(name);
    }

    /** Need to be defined because of JUnit */
    public WebProjectValidationEE5() {
        super();
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(WebProjectValidationEE5.class);
        conf = addServerTests(Server.GLASSFISH, conf, 
              "testPreconditions", "testNewWebProject", "testRedeployProject",
              "testNewJSP", "testNewJSP2", "testNewServlet", "testNewServlet2",
              "testCompileAllJSP", "testCompileJSP",
              "testCleanAndBuildProject", "testRunProject", "testRunJSP", "testViewServlet",
              "testRunServlet", "testCreateTLD", "testCreateTagHandler", "testRunTag",
              "testNewHTML", "testRunHTML", "testNewSegment", "testNewDocument",
              "testStopServer", "testStartServer", "testBrowserSettings", "testFinish"
               );
        conf = conf.enableModules(".*").clusters(".*");
        return NbModuleSuite.create(conf); 
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
    @Override
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
    @Override
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
        serverStep.selectServer(getServerNode(Server.GLASSFISH).getText());
        serverStep.selectJavaEEVersion(JAVA_EE_5);
        serverStep.next();
        NewWebProjectJSFFrameworkStepOperator frameworkStep = new NewWebProjectJSFFrameworkStepOperator();
        frameworkStep.finish();
        // wait for project creation
        sleep(5000);
        ProjectSupport.waitScanFinished();
        verifyWebPagesNode("index.jsp");
        verifyWebPagesNode("WEB-INF|web.xml");
        verifyWebPagesNode("WEB-INF|sun-web.xml");
    }

    @Override
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

    public void testRedeployProject() throws IOException {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        Util.cleanStatusBar();
        new RedeployProjectAction().perform(rootNode);
        waitBuildSuccessful();
        logAndCloseOutputs();
    //MainWindowOperator.getDefault().waitStatusText("Finished building");
    }

    @Override
    public void testStopServer() throws Exception {
        server.stop();
        //try { Thread.currentThread().sleep(5000); } catch (InterruptedException e) {}
        URL url = server.getServerURL();
        URLConnection connection = url.openConnection();
        try {
            connection.connect();
            fail("Connection to: " + url + " established, but the server" +
                    " should not be running.");
        } catch (ConnectException e) {
            System.out.println("Exception in testStopServer occured!");
        }
    }

    @Override
    public void testStartServer() throws Exception {
        server.start();
        URL url = server.getServerURL();
        URLConnection connection = url.openConnection();
        connection.connect();
    }

    @Override
    public void testFinish() {
        server.stop();
        new ActionNoBlock(null, "Close").perform(new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME));
    }

    //********************************************************
    public String getProjectFolder(String project) {
        String strLocation = PROJECT_LOCATION + File.separator + project;
        return strLocation;
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

