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
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.BuildProjectAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author dk198696
 */
public class EnterpriseSamples extends JellyTestCase {

    protected static int logIdx = 0;
    protected static final String PROJECT_LOCATION = System.getProperty("xtest.userdir");
    private static final String BUILD_SUCCESSFUL = "BUILD SUCCESSFUL";

    /** Need to be defined because of JUnit */
    public EnterpriseSamples(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new EnterpriseSamples("testNewCustomerCMPSample"));
        suite.addTest(new EnterpriseSamples("testBuildCustomerCMPSample"));
        suite.addTest(new EnterpriseSamples("testCleanCustomerCMPSample"));
//        suite.addTest(new EnterpriseSamples("testRunCustomerCMPSample"));
        suite.addTest(new EnterpriseSamples("testNewAnnotationOverrideInterceptorSample"));
        suite.addTest(new EnterpriseSamples("testBuildAnnotationOverrideInterceptorSample"));
        suite.addTest(new EnterpriseSamples("testCleanAnnotationOverrideInterceptorSample"));
//        suite.addTest(new EnterpriseSamples("testRunAnnotationOverrideInterceptorSample"));
        suite.addTest(new EnterpriseSamples("testNewInterceptorStatelessSample"));
        suite.addTest(new EnterpriseSamples("testBuildInterceptorStatelessSample"));
        suite.addTest(new EnterpriseSamples("testCleanInterceptorStatelessSample"));
//        suite.addTest(new EnterpriseSamples("testRunInterceptorStatelessSample"));
        suite.addTest(new EnterpriseSamples("testNewJSFJPASample"));
        suite.addTest(new EnterpriseSamples("testBuildJSFJPASample"));
        suite.addTest(new EnterpriseSamples("testCleanJSFJPASample"));
//        suite.addTest(new EnterpriseSamples("testRunJSFJPASample"));
        suite.addTest(new EnterpriseSamples("testNewJSFJPACrudSample"));
        suite.addTest(new EnterpriseSamples("testBuildJSFJPACrudSample"));
        suite.addTest(new EnterpriseSamples("testCleanJSFJPACrudSample"));
//        suite.addTest(new EnterpriseSamples("testRunJSFJPACrudSample"));
        suite.addTest(new EnterpriseSamples("testNewLotteryAnnotationSample"));
        suite.addTest(new EnterpriseSamples("testBuildLotteryAnnotationSample"));
        suite.addTest(new EnterpriseSamples("testCleanLotteryAnnotationSample"));
//        suite.addTest(new EnterpriseSamples("testRunLotteryAnnotationSample"));
        suite.addTest(new EnterpriseSamples("testNewServletStatelessSample"));
        suite.addTest(new EnterpriseSamples("testBuildServletStatelessSample"));
        suite.addTest(new EnterpriseSamples("testCleanServletStatelessSample"));
//        suite.addTest(new EnterpriseSamples("testRunServletStatelessSample"));
        suite.addTest(new EnterpriseSamples("testNewWebJPASample"));
        suite.addTest(new EnterpriseSamples("testBuildWebJPASample"));
        suite.addTest(new EnterpriseSamples("testCleanWebJPASample"));
//        suite.addTest(new EnterpriseSamples("testRunWebJPASample"));
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
//        server = ServerInstance.getDefault();

    // extend Tomcat running check timeout
    //        TomcatManager tomcatManager = getTomcatManager();
    //        tomcatManager.getInstanceProperties().setProperty(
    //                TomcatProperties.PROP_RUNNING_CHECK_TIMEOUT, "8000");
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
    public void testNewCustomerCMPSample() throws IOException {
        NewProjectWizardOperator projectWizard = NewProjectWizardOperator.invoke();
        projectWizard.selectCategory("Samples|Enterprise");
        projectWizard.selectProject("Customer CMP");
        projectWizard.next();
        NewWebProjectNameLocationStepOperator step = new NewWebProjectNameLocationStepOperator();
        step.setProjectLocation(PROJECT_LOCATION);
        step.finish();
        sleep(5000);
    }

    public void testNewAnnotationOverrideInterceptorSample() throws IOException {
        NewProjectWizardOperator projectWizard = NewProjectWizardOperator.invoke();
        projectWizard.selectCategory("Samples|Enterprise");
        projectWizard.selectProject("Annotation Override Interceptor");
        projectWizard.next();
        NewWebProjectNameLocationStepOperator step = new NewWebProjectNameLocationStepOperator();
        step.setProjectLocation(PROJECT_LOCATION);
        step.finish();
        sleep(5000);
    }

    public void testNewInterceptorStatelessSample() throws IOException {
        NewProjectWizardOperator projectWizard = NewProjectWizardOperator.invoke();
        projectWizard.selectCategory("Samples|Enterprise");
        projectWizard.selectProject("Interceptor Stateless");
        projectWizard.next();
        NewWebProjectNameLocationStepOperator step = new NewWebProjectNameLocationStepOperator();
        step.setProjectLocation(PROJECT_LOCATION);
        step.finish();
        sleep(5000);
    }

    public void testNewJSFJPASample() throws IOException {
        NewProjectWizardOperator projectWizard = NewProjectWizardOperator.invoke();
        projectWizard.selectCategory("Samples|Enterprise");
        projectWizard.selectProject("JSF JPA");
        projectWizard.next();
        NewWebProjectNameLocationStepOperator step = new NewWebProjectNameLocationStepOperator();
        step.setProjectLocation(PROJECT_LOCATION);
        step.finish();
        sleep(5000);
    }

    public void testNewJSFJPACrudSample() throws IOException {
        NewProjectWizardOperator projectWizard = NewProjectWizardOperator.invoke();
        projectWizard.selectCategory("Samples|Enterprise");
        projectWizard.selectProject("JSF JPA CRUD");
        projectWizard.next();
        NewWebProjectNameLocationStepOperator step = new NewWebProjectNameLocationStepOperator();
        step.setProjectLocation(PROJECT_LOCATION);
        step.finish();
        sleep(5000);
    }

    public void testNewLotteryAnnotationSample() throws IOException {
        NewProjectWizardOperator projectWizard = NewProjectWizardOperator.invoke();
        projectWizard.selectCategory("Samples|Enterprise");
        projectWizard.selectProject("Lottery Annotation");
        projectWizard.next();
        NewWebProjectNameLocationStepOperator step = new NewWebProjectNameLocationStepOperator();
        step.setProjectLocation(PROJECT_LOCATION);
        step.finish();
        sleep(5000);
    }

    public void testNewServletStatelessSample() throws IOException {
        NewProjectWizardOperator projectWizard = NewProjectWizardOperator.invoke();
        projectWizard.selectCategory("Samples|Enterprise");
        projectWizard.selectProject("Servlet Stateless");
        projectWizard.next();
        NewWebProjectNameLocationStepOperator step = new NewWebProjectNameLocationStepOperator();
        step.setProjectLocation(PROJECT_LOCATION);
        step.finish();
        sleep(5000);
    }

    public void testNewWebJPASample() throws IOException {
        NewProjectWizardOperator projectWizard = NewProjectWizardOperator.invoke();
        projectWizard.selectCategory("Samples|Enterprise");
        projectWizard.selectProject("Web JPA");
        projectWizard.next();
        NewWebProjectNameLocationStepOperator step = new NewWebProjectNameLocationStepOperator();
        step.setProjectLocation(PROJECT_LOCATION);
        step.finish();
        sleep(5000);
    }

    public void testBuildCustomerCMPSample() throws IOException {
        testBuildProject("CustomerCMP");
    }

    public void testCleanCustomerCMPSample() throws IOException {
        testCleanProject("CustomerCMP");
    }

    public void testRunCustomerCMPSample() throws IOException {
        testRunProject("CustomerCMP");
    }

    public void testBuildAnnotationOverrideInterceptorSample() throws IOException {
        testBuildProject("AnnotOvdInterceptor");
    }

    public void testCleanAnnotationOverrideInterceptorSample() throws IOException {
        testCleanProject("AnnotOvdInterceptor");
    }

    public void testRunAnnotationOverrideInterceptorSample() throws IOException {
        testRunProject("AnnotOvdInterceptor");
    }

    public void testBuildInterceptorStatelessSample() throws IOException {
        testBuildProject("InterceptorStateless");
    }

    public void testCleanInterceptorStatelessSample() throws IOException {
        testCleanProject("InterceptorStateless");
    }

    public void testRunInterceptorStatelessSample() throws IOException {
        testRunProject("InterceptorStateless");
    }

    public void testBuildJSFJPASample() throws IOException {
        testBuildProject("JsfJpa");
    }

    public void testCleanJSFJPASample() throws IOException {
        testCleanProject("JsfJpa");
    }

    public void testRunJSFJPASample() throws IOException {
        testRunProject("JsfJpa");
    }

    public void testBuildJSFJPACrudSample() throws IOException {
        testBuildProject("JsfJpaCrud");
    }

    public void testCleanJSFJPACrudSample() throws IOException {
        testCleanProject("JsfJpaCrud");
    }

    public void testRunJSFJPACrudSample() throws IOException {
        testRunProject("JsfJpaCrud");
    }

    public void testBuildLotteryAnnotationSample() throws IOException {
        testBuildProject("LotteryAnnotation");
    }

    public void testCleanLotteryAnnotationSample() throws IOException {
        testCleanProject("LotteryAnnotation");
    }

    public void testRunLotteryAnnotationSample() throws IOException {
        testRunProject("LotteryAnnotation");
    }

    public void testBuildServletStatelessSample() throws IOException {
        testBuildProject("ServletStateless");
    }

    public void testCleanServletStatelessSample() throws IOException {
        testCleanProject("ServletStateless");
    }

    public void testRunServletStatelessSample() throws IOException {
        testRunProject("ServletStateless");
    }

    public void testBuildWebJPASample() throws IOException {
        testBuildProject("WebJpa");
    }
    
    public void testCleanWebJPASample() throws IOException {
        testCleanProject("WebJpa");
    }

    public void testRunWebJPASample() throws IOException {
        testRunProject("WebJpa");
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
        new BuildProjectAction().perform(rootNode);
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
}
