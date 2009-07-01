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
package org.netbeans.test.j2ee;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.BuildJavaProjectAction;
import org.netbeans.jellytools.actions.RedeployProjectAction;
import org.netbeans.jellytools.modules.j2ee.nodes.J2eeServerNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Dan.Kolar@sun.com
 */
public class VisualJSFSamples extends JellyTestCase {

    protected static int logIdx = 0;
    protected static final String PROJECT_LOCATION = System.getProperty("xtest.userdir");
    private static final String BUILD_SUCCESSFUL = "BUILD SUCCESSFUL";
    private ServerInstance server;

    /** Need to be defined because of JUnit */
    public VisualJSFSamples(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new VisualJSFSamples("testNewCorporateTravelCenterSample"));
        suite.addTest(new VisualJSFSamples("testBuildCorporateTravelCenterSample"));
        suite.addTest(new VisualJSFSamples("testCleanCorporateTravelCenterSample"));
        suite.addTest(new VisualJSFSamples("testRedeployCorporateTravelCenterSample"));
        suite.addTest(new VisualJSFSamples("testNewSinglePageCrudWithTableSample"));
        suite.addTest(new VisualJSFSamples("testBuildSinglePageCrudWithTableSample"));
        suite.addTest(new VisualJSFSamples("testCleanSinglePageCrudWithTableSample"));
        suite.addTest(new VisualJSFSamples("testRedeploySinglePageCrudWithTableSample"));
        suite.addTest(new VisualJSFSamples("testNewSinglePageCrudWithFormSample"));
        suite.addTest(new VisualJSFSamples("testBuildSinglePageCrudWithFormSample"));
        suite.addTest(new VisualJSFSamples("testCleanSinglePageCrudWithFormSample"));
        suite.addTest(new VisualJSFSamples("testRedeploySinglePageCrudWithFormSample"));
        suite.addTest(new VisualJSFSamples("testNewTwoPageCrudWithTableSample"));
        suite.addTest(new VisualJSFSamples("testBuildTwoPageCrudWithTableSample"));
        suite.addTest(new VisualJSFSamples("testCleanTwoPageCrudWithTableSample"));
        suite.addTest(new VisualJSFSamples("testRedeployTwoPageCrudWithTableSample"));
        suite.addTest(new VisualJSFSamples("testNewMovieAdministrationSample"));
        suite.addTest(new VisualJSFSamples("testBuildMovieAdministrationSample"));
        suite.addTest(new VisualJSFSamples("testCleanMovieAdministrationSample"));
        suite.addTest(new VisualJSFSamples("testRedeployMovieAdministrationSample"));
        suite.addTest(new VisualJSFSamples("testNewVIRASample"));
        suite.addTest(new VisualJSFSamples("testBuildVIRASample"));
        suite.addTest(new VisualJSFSamples("testCleanVIRASample"));
        suite.addTest(new VisualJSFSamples("testRedeployVIRASample"));
        suite.addTest(new VisualJSFSamples("testStopServer"));
        return suite;
    }

    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        //junit.textui.TestRunner.run(suite());
        //WebProjectValidation val = new WebProjectValidation("test");
        //val.setUp();
        //val.testStartServer();
        // run only selected test case
        //junit.textui.TestRunner.run(new MyModuleValidation("testT2"));
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

    /** Test creation of web project.
     * - open New Project wizard from main menu (File|New Project)
     * - select Samples|Enterprise
     * - finish the wizard
     * - wait until scanning of java files is finished
     * - check index.jsp is opened
     */
    public void testNewCorporateTravelCenterSample() throws IOException {
        NewProjectWizardOperator projectWizard = NewProjectWizardOperator.invoke();
        projectWizard.selectCategory("Samples|Java Web (Visual JSF)"); // XXX use Bundle.getString instead
        projectWizard.selectProject("Corporate Travel Center");
        projectWizard.next();
        NewWebProjectNameLocationStepOperator step = new NewWebProjectNameLocationStepOperator();
        step.setProjectLocation(PROJECT_LOCATION);
        step.finish();
        sleep(5000);
    }

    public void testNewSinglePageCrudWithTableSample() throws IOException {
        NewProjectWizardOperator projectWizard = NewProjectWizardOperator.invoke();
        projectWizard.selectCategory("Samples|Java Web (Visual JSF)"); // XXX use Bundle.getString instead
        projectWizard.selectProject("Single Page CRUD With Table");
        projectWizard.next();
        NewWebProjectNameLocationStepOperator step = new NewWebProjectNameLocationStepOperator();
        step.setProjectLocation(PROJECT_LOCATION);
        step.finish();
        sleep(5000);
    }

    public void testNewSinglePageCrudWithFormSample() throws IOException {
        NewProjectWizardOperator projectWizard = NewProjectWizardOperator.invoke();
        projectWizard.selectCategory("Samples|Java Web (Visual JSF)"); // XXX use Bundle.getString instead
        projectWizard.selectProject("Single Page CRUD With Form");
        projectWizard.next();
        NewWebProjectNameLocationStepOperator step = new NewWebProjectNameLocationStepOperator();
        step.setProjectLocation(PROJECT_LOCATION);
        step.finish();
        sleep(5000);
    }

    public void testNewTwoPageCrudWithTableSample() throws IOException {
        NewProjectWizardOperator projectWizard = NewProjectWizardOperator.invoke();
        projectWizard.selectCategory("Samples|Java Web (Visual JSF)"); // XXX use Bundle.getString instead
        projectWizard.selectProject("Two Page CRUD With Table");
        projectWizard.next();
        NewWebProjectNameLocationStepOperator step = new NewWebProjectNameLocationStepOperator();
        step.setProjectLocation(PROJECT_LOCATION);
        step.finish();
        sleep(5000);
    }

    public void testNewMovieAdministrationSample() throws IOException {
        NewProjectWizardOperator projectWizard = NewProjectWizardOperator.invoke();
        projectWizard.selectCategory("Samples|Java Web (Visual JSF)"); // XXX use Bundle.getString instead
        projectWizard.selectProject("Movie Administration");
        projectWizard.next();
        NewWebProjectNameLocationStepOperator step = new NewWebProjectNameLocationStepOperator();
        step.setProjectLocation(PROJECT_LOCATION);
        step.finish();
        sleep(5000);
    }

    public void testNewVIRASample() throws IOException {
        NewProjectWizardOperator projectWizard = NewProjectWizardOperator.invoke();
        projectWizard.selectCategory("Samples|Java Web (Visual JSF)"); // XXX use Bundle.getString instead
        projectWizard.selectProject("Vehicle Incident Report Application");
        projectWizard.next();
        NewWebProjectNameLocationStepOperator step = new NewWebProjectNameLocationStepOperator();
        step.setProjectLocation(PROJECT_LOCATION);
        step.finish();
        sleep(5000);
    }

    public void testBuildCorporateTravelCenterSample() throws IOException {
        testBuildProject("TravelCenter");
    }

    public void testCleanCorporateTravelCenterSample() throws IOException {
        testCleanProject("TravelCenter");
    }

    public void testRedeployCorporateTravelCenterSample() throws IOException {
        testRedeployProject("TravelCenter");
    }

    public void testBuildSinglePageCrudWithTableSample() throws IOException {
        testBuildProject("SinglePageCrudTable");
    }

    public void testCleanSinglePageCrudWithTableSample() throws IOException {
        testCleanProject("SinglePageCrudTable");
    }

    public void testRedeploySinglePageCrudWithTableSample() throws IOException {
        testRedeployProject("SinglePageCrudTable");
    }

    public void testBuildSinglePageCrudWithFormSample() throws IOException {
        testBuildProject("SinglePageCrudForm");
    }

    public void testCleanSinglePageCrudWithFormSample() throws IOException {
        testCleanProject("SinglePageCrudForm");
    }

    public void testRedeploySinglePageCrudWithFormSample() throws IOException {
        testRedeployProject("SinglePageCrudForm");
    }

    public void testBuildTwoPageCrudWithTableSample() throws IOException {
        testBuildProject("TwoPageCrudTable");
    }

    public void testCleanTwoPageCrudWithTableSample() throws IOException {
        testCleanProject("TwoPageCrudTable");
    }

    public void testRedeployTwoPageCrudWithTableSample() throws IOException {
        testRedeployProject("TwoPageCrudTable");
    }

    public void testBuildMovieAdministrationSample() throws IOException {
        testBuildProject("MovieAdmin");
    }

    public void testCleanMovieAdministrationSample() throws IOException {
        testCleanProject("MovieAdmin");
    }

    public void testRedeployMovieAdministrationSample() throws IOException {
        testRedeployProject("MovieAdmin");
    }

    public void testBuildVIRASample() throws IOException {
        testBuildProject("VehicleIncidentReportApplication");
    }

    public void testCleanVIRASample() throws IOException {
        testCleanProject("VehicleIncidentReportApplication");
    }

    public void testRedeployVIRASample() throws IOException {
        testRedeployProject("VehicleIncidentReportApplication");
    }

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

    public void testBuildProject(String PROJECT_NAME) {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        Util.cleanStatusBar();
        new BuildJavaProjectAction().perform(rootNode);
        MainWindowOperator.getDefault().waitStatusText("Finished building");
//        ref(Util.dumpFiles(new File(PROJECT_FOLDER)));
    //compareReferenceFiles();
    }

    public void testCleanProject(String PROJECT_NAME) {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        Action clean = new Action(null, "Clean");
        // can clash with 'Clean and Build' action
        clean.setComparator(new Operator.DefaultStringComparator(true, true));
        Util.cleanStatusBar();
        clean.perform(rootNode);
        MainWindowOperator.getDefault().waitStatusText("Finished building");
//        ref(Util.dumpFiles(new File(getProjectFolder(PROJECT_NAME)));
    //compareReferenceFiles();
    }

    public void testRunProject(String PROJECT_NAME) {
//        initDisplayer();
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
//        new Node(rootNode, "Web Pages|index.jsp").performPopupAction("Open");
//        EditorOperator editor = new EditorOperator("index.jsp");
//        editor.replace("<title>JSP Page</title>",
//                "<title>SampleProject Index Page</title>");
//        editor.insert("Running Project\n", 12, 1);
        new Action(null, "Run").perform(rootNode);
        waitBuildSuccessful(PROJECT_NAME);
//        assertDisplayerContent("<title>SampleProject Index Page</title>");
//        editor.deleteLine(12);
//        editor.save();
//        editor.closeDiscardAll();
    }

    public void testRedeployProject(String PROJECT_NAME) throws IOException {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        Util.cleanStatusBar();
        new RedeployProjectAction().perform(rootNode);
        waitBuildSuccessful(PROJECT_NAME);
        logAndCloseOutputs();
    //MainWindowOperator.getDefault().waitStatusText("Finished building");
    }

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
    
    public String getProjectFolder(String PROJECT_NAME) {
        return PROJECT_LOCATION + File.separator + PROJECT_NAME;
    }

    private void waitBuildSuccessful(String PROJECT_NAME) {
        OutputTabOperator console = new OutputTabOperator(PROJECT_NAME);
        console.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 180000);
        console.waitText(BUILD_SUCCESSFUL);
    }

//    private void initDisplayer() {
//        if (urlDisplayer == null) {
//            urlDisplayer = TestURLDisplayer.getInstance();
//        }
//        urlDisplayer.invalidateURL();
//    }
//
//    private void assertDisplayerContent(String substr) {
//        try {
//            urlDisplayer.waitURL();
//        } catch (InterruptedException ex) {
//            throw new JemmyException("Waiting interrupted.", ex);
//        }
//        String page = urlDisplayer.readURL();
//        boolean contains = page.indexOf(substr) > -1;
//        if (!contains) {
//            log("DUMP OF: "+urlDisplayer.getURL()+"\n");
//            log(page);
//        }
//        assertTrue("The '"+urlDisplayer.getURL()+"' page does not contain '"+substr+"'", contains);
//    }
    private void assertContains(String text, String value) {
        assertTrue("Assertation failed, cannot find:\n" + value + "\nin the following text:\n" + text, text.contains(value));
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
