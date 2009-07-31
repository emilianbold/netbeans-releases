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

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.actions.BuildJavaProjectAction;
import org.netbeans.jellytools.actions.EditAction;
import org.netbeans.jellytools.modules.web.NewJspFileNameStepOperator;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.NewWebProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewWebProjectServerSettingsStepOperator;
import org.netbeans.jellytools.NewWebProjectSourcesStepOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jellytools.modules.j2ee.nodes.J2eeServerNode;
import org.netbeans.jellytools.modules.web.NavigatorOperator;
import org.netbeans.jellytools.modules.web.nodes.WebPagesNode;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.junit.Manager;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 */
public class WebProjectValidation extends J2eeTestCase {
    protected static ProjectHelper phelper = new ProjectHelper() {
        public Node getSourceNode() {
            return new SourcePackagesNode(PROJECT_NAME);
        }
    };
    public static final String J2EE_4 = "J2EE 1.4";
    public static final String J2EE_3 = "J2EE 1.3";
    public static final String JAVA_EE_5 = "JAVA EE 5";
    // location of sample project (parent of PROJECT_FOLDER)
    protected static String PROJECT_LOCATION;
    // name of sample project
    protected static String PROJECT_NAME = "SampleProject"; // NOI18N
    // foloder of sample project
    protected static String PROJECT_FOLDER;
    protected TestURLDisplayer urlDisplayer;
    private static final String BUILD_SUCCESSFUL = "BUILD SUCCESSFUL";
    private ServerInstance server;
    protected static int logIdx = 0;
    
    /** Need to be defined because of JUnit */
    public WebProjectValidation(String name) {
        super(name);
        PROJECT_LOCATION = getProjectFolder().getAbsolutePath();
        PROJECT_FOLDER = PROJECT_LOCATION+File.separator+PROJECT_NAME;        
    }

    /** Need to be defined because of JUnit */
    public WebProjectValidation() {
        super("WebProjectValidation");
    }
    
    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(WebProjectValidation.class);
        conf = addServerTests(J2eeTestCase.Server.TOMCAT, conf,
                "testPreconditions", "testNewWebProject", "testRegisterTomcat",
                "testNewJSP", "testNewJSP2", "testNewServlet", "testNewServlet2",
                "testNewHTML", "testCreateTLD", "testCreateTagHandler", "testNewSegment", "testNewDocument",
                "testJSPNavigator", "testHTMLNavigator", "testCompileAllJSP", "testCompileJSP",
                "testCleanAndBuildProject", "testRunProject", "testRunJSP", "testViewServlet",
                "testRunServlet", "testRunHTML", "testRunTag",
                "testStopServer", "testStartServer", "testBrowserSettings",
                "testFinish"
                );
        conf = conf.enableModules(".*").clusters(".*");
        return NbModuleSuite.create(conf);
    }

    protected  File getProjectFolder() {
        File dataDir = null;
        try {
            dataDir = new WebProjectValidation().getWorkDir();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Manager.normalizeFile(dataDir);
    }

    @Override
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
        JemmyProperties.setCurrentTimeout(
                "ComponentOperator.WaitComponentTimeout", 180000);
        JemmyProperties.setCurrentTimeout(
                "FrameWaiter.WaitFrameTimeout", 180000);
        JemmyProperties.setCurrentTimeout(
                "DialogWaiter.WaitDialogTimeout",180000);
        server = ServerInstance.getDefault();
    }
    
    @Override
    public void tearDown() {
        logAndCloseOutputs();
    }
    
    public void testRegisterTomcat() {
        assertNotNull(getServerNode(Server.TOMCAT));
    }
    
    /** checks if the Server ports are not used */
    public void testPreconditions() throws Exception {
        File projectLoc = new File(PROJECT_LOCATION);
        if (projectLoc.exists()){
            FileObject fo = FileUtil.createData(projectLoc);
            fo.delete();
        }
        URLConnection connection = server.getServerURL().openConnection();
        try {
            connection.connect();
            fail("Port: "+server.getServerURL()+" is used by different server.");
        } catch (ConnectException e) {  }
        URL url = new URL("http://localhost:8025");
        connection = url.openConnection();
        try {
            connection.connect();
            //server is running -> try to stop it
            server.stop();
            verifyServerIsStopped();
        } catch (ConnectException e) {  }
        initDisplayer();
    }
    
    /** Test creation of web project.
     * - open New Project wizard from main menu (File|New Project)
     * - select Web|Web Application
     * - in the next panel type project name and project location
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
        serverStep.cboServer().selectItem(0);
        serverStep.selectJavaEEVersion(J2EE_4);
        serverStep.next();
        NewWebProjectSourcesStepOperator frameworkStep =  new NewWebProjectSourcesStepOperator();
        frameworkStep.finish();
        // wait for project creation
        sleep(5000);
        ProjectSupport.waitScanFinished();
        verifyWebPagesNode("index.jsp");
        verifyWebPagesNode("WEB-INF|web.xml");
        verifyWebPagesNode("META-INF|context.xml");
    }
    
    protected void verifyProjectNode(String nodePath) {
        Node prjNode = ProjectsTabOperator.invoke().getProjectRootNode(PROJECT_NAME);
        Node node = new Node(prjNode,nodePath);//NOI18N
        assertTrue(node.isPresent());
        node.select();
    }

    protected void verifySourcePackageNode(String nodePath){
        SourcePackagesNode sourceNode = new SourcePackagesNode(PROJECT_NAME);
        Node node = new Node(sourceNode, nodePath);
        assertTrue(node.isPresent());
        node.select();
    }
    
    protected void verifyWebPagesNode(String nodePath) {
        WebPagesNode webPages = new WebPagesNode(PROJECT_NAME);
        Node node = new Node(webPages,nodePath);//NOI18N
        assertTrue(node.isPresent());
        node.select();
    }
    /** Test new JSP wizard.
     * - open New File wizard from main menu (File|New File)
     * - select sample project as target
     * - select Web|JSP file type
     * - in the next panel type name
     * - finish the wizard
     * - check file is open in editor and close all opened documents
     */
    public void testNewJSP() throws IOException {
        NewJspFileNameStepOperator nameStep = NewJspFileNameStepOperator.invoke();
        nameStep.setJSPFileName("page1");
        nameStep.finish();
        // check class is opened in Editor and then close it
        EditorOperator jsp1Editor = new EditorOperator("page1.jsp");
        jsp1Editor.save();
        jsp1Editor.close();
        //new EditorOperator("page1.jsp").close();
        ref(Util.dumpProjectView(PROJECT_NAME));
        //compareReferenceFiles();
    }
    
    /** Test new JSP wizard.
     * - open New File wizard from context menu on Web Pages (New|File)
     * - select JSP file type
     * - in the next panel type name in
     * - finish the wizard
     * - check file is open in editor and close all opened documents
     */
    public void testNewJSP2() throws IOException {
        Node webPages = new WebPagesNode(PROJECT_NAME);
        // create new .jsp
        new ActionNoBlock(null, "New|JSP").perform(webPages);
        NewJspFileNameStepOperator nameStep = new NewJspFileNameStepOperator();
        nameStep.setJSPFileName("page2");
        nameStep.finish();
        // check class is opened in Editor and then close the  document
        new EditorOperator("page2.jsp").close();
        ref(Util.dumpProjectView(PROJECT_NAME));
        //compareReferenceFiles();
        //compareDD();
    }
    
    /** Test new servlet wizard.
     * - open New File wizard from main menu (File|New File)
     * - select sample project as target
     * - select Web|Servlet file type
     * - in the next panel type name
     * - finish the wizard
     * - check file is open in editor and close it
     */
    public void testNewServlet() throws IOException {
        // create a new package
        new ActionNoBlock("File|New File", null).perform();
        // WORKAROUND
        new EventTool().waitNoEvent(1000);
        WizardOperator newFileWizard = new WizardOperator("New File");
        new JComboBoxOperator(newFileWizard).selectItem(PROJECT_NAME);
        new Node(new JTreeOperator(newFileWizard), "Web").select();
        new JListOperator(newFileWizard, 1).selectItem("Servlet");
        newFileWizard.next();
        JTextFieldOperator txtPackageName = new JTextFieldOperator(newFileWizard);
        // clear text field
        txtPackageName.setText("");
        txtPackageName.typeText("Servlet1");
        JComboBoxOperator txtPackage = new JComboBoxOperator(newFileWizard,1);
        // clear text field
        txtPackage.clearText();
        txtPackage.typeText("test1");
        newFileWizard.next();
        newFileWizard.finish();
        // check class is opened in Editor and close it
        new EditorOperator("Servlet1.java").close();
        // check the servlet is specified in web.xml
        WebPagesNode webPages = new WebPagesNode(PROJECT_NAME);
        webPages.setComparator(new Operator.DefaultStringComparator(true, true));
        Node webXml = new Node(webPages, "WEB-INF|web.xml");
        new EditAction().performPopup(webXml);
        String xmlText = new EditorOperator("web.xml").getText();
        new EditorOperator("web.xml").closeAllDocuments();
        String[] content = new String[] {
            "<servlet-name>Servlet1</servlet-name>",
            "<servlet-class>test1.Servlet1</servlet-class>",
            "<url-pattern>/Servlet1</url-pattern>"
        };
        for (int i=0; i<content.length; i++) {
            assertTrue("Servlet is not correctly specifeid in web.xml." +
                    " Following line is missing in the web.xml:\n"+content[i],
                    xmlText.indexOf(content[i]) != -1);
        }
        ref(Util.dumpProjectView(PROJECT_NAME));
        //compareReferenceFiles();
        //compareDD();
    }
    
    /** Test new Servlet wizard.
     * - open New File wizard from main menu (File|New File)
     * - select sample project as target
     * - select Java|Package file type
     * - in the next panel type package name in
     * - finish the wizard
     * - open New File wizard from context menu on created package node (New|File)
     * - select Web|Servlet file type
     * - in the next panel type class name in
     * - finish the wizard
     * - check class is open in editor and close it
     */
    public void testNewServlet2() throws IOException {
        createJavaPackage(PROJECT_NAME, "test2");
        Node sample1Node = new Node(phelper.getSourceNode(), "test2");
        // create a new class
        new ActionNoBlock(null, "New|Servlet").perform(sample1Node);
        WizardOperator newFileWizard = new WizardOperator("New Servlet");
        new JTextFieldOperator(newFileWizard).typeText("Servlet2");
        newFileWizard.next();
        new JTextFieldOperator(newFileWizard,1).setText("");
        new JTextFieldOperator(newFileWizard,1).typeText("Servlet2Name");
        new JTextFieldOperator(newFileWizard,2).setText("");
        new JTextFieldOperator(newFileWizard,2).typeText("/Servlet2URL");
        newFileWizard.finish();
        // check class is opened in Editor and then close it
        new EditorOperator("Servlet2.java").close();
        ref(Util.dumpProjectView(PROJECT_NAME));
        sleep(1000);
        //compareReferenceFiles();
        //compareDD();
    }
    
    // TODO workaround for defective:
    // NewFileWizardOperator.create(PROJECT_NAME, "Java", "Java Package", null, "test2");
    // Should be fixed in jemmy/jelly
    private void createJavaPackage(String projectName, String pkgName) {
        String wizardTitle = Bundle.getString("org.netbeans.modules.project.ui.Bundle",
                                              "LBL_NewFileWizard_Title");
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke(wizardTitle);
        nfwo.selectProject(projectName);
        nfwo.setComparator(new Operator.DefaultStringComparator(true, true));
        nfwo.selectCategory("Java");
        nfwo.selectFileType("Java Package");
        nfwo.next();
        NewJavaFileNameLocationStepOperator nfnlso = new NewJavaFileNameLocationStepOperator();
        nfnlso.setObjectName(pkgName);
        nfnlso.finish();
    }
    
    public void testCleanAndBuildProject() {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        Util.cleanStatusBar();
        new Action(null, "Clean and Build").perform(rootNode);
        MainWindowOperator.getDefault().waitStatusText("Finished building");
    }

    public void testBuildProject() {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        Util.cleanStatusBar();
        new BuildJavaProjectAction().perform(rootNode);
        MainWindowOperator.getDefault().waitStatusText("Finished building");
        ref(Util.dumpFiles(new File(PROJECT_FOLDER)));
        //compareReferenceFiles();
    }
    
    public void testCompileAllJSP() {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        new PropertiesAction().perform(rootNode);
        NbDialogOperator properties = new NbDialogOperator("Project Properties");
        new Node(new JTreeOperator(properties), "Build|Compiling").select();
        new JCheckBoxOperator(properties,2).changeSelection(true);
        properties.ok();
        
        testCleanAndBuildProject();
        logAndCloseOutputs();
        testCleanAndBuildProject();
        logAndCloseOutputs();
        
        new Action(null,"Properties").perform(rootNode);
        properties = new NbDialogOperator("Project Properties");
        new Node(new JTreeOperator(properties), "Build|Compiling").select();
        new JCheckBoxOperator(properties,2).changeSelection(false);
        properties.ok();
    }
    
    public void testCompileJSP() {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        Node jspNode = new Node(rootNode,"Web Pages|page2.jsp");
        Util.cleanStatusBar();
        new Action(null,"Compile File").perform(jspNode);
        MainWindowOperator.getDefault().waitStatusText("Finished building");
        ref(Util.dumpFiles(new File(PROJECT_FOLDER)));
        //compareReferenceFiles();
    }
    
    public void testCleanProject() {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        Action clean = new Action(null,"Clean and Build");
        // can clash with 'Clean and Build' action
        clean.setComparator(new Operator.DefaultStringComparator(true, true));
        Util.cleanStatusBar();
        clean.perform(rootNode);
        MainWindowOperator.getDefault().waitStatusText("Finished building");
        ref(Util.dumpFiles(new File(PROJECT_FOLDER)));
        //compareReferenceFiles();
    }
    
    public void testRunProject() throws Exception {
        initDisplayer();
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        new Node(rootNode,"Web Pages|index.jsp").performPopupAction("Open");
        EditorOperator editor = new EditorOperator("index.jsp");
        editor.replace("<title>JSP Page</title>",
                "<title>SampleProject Index Page</title>");
        editor.insert("Running Project\n",12,1);
        new Action(null,"Run").perform(rootNode);
        waitBuildSuccessful();
        assertDisplayerContent("<title>SampleProject Index Page</title>");
        editor.deleteLine(12);
        editor.save();
        EditorOperator.closeDiscardAll();
    }
    
    public void testRunJSP() {
        initDisplayer();
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        new Node(rootNode,"Web Pages|page2.jsp").performPopupAction("Open");
        EditorOperator editor = new EditorOperator("page2.jsp");
        //editor.replace("<title>JSP Page/title>", "<title>Page 2</title>");
        editor.deleteLine(14);
        editor.insert("<title>Page 2</title>\n",14,1);
        editor.insert("Running JSP\n",12,1);
           sleep(2000);
        editor.save();
        new Action("Run|Run File",null).perform();
        waitBuildSuccessful();
        assertDisplayerContent("<title>Page 2</title>");
        editor.deleteLine(12);
        editor.save();
        editor.close();
    }
    
    public void testViewServlet() throws IOException{
        initDisplayer();
        String jspCode ="new String().toString();";
        String runningViewServlet = "Running View Servlet";
        String newTitle = "<h1>View Page</h1>";
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        new Node(rootNode,"Web Pages|page2.jsp").performPopupAction("Open");
        EditorOperator editor = new EditorOperator("page2.jsp");
        editor.insert(newTitle+"\n",13,6);
        editor.insert(runningViewServlet+"\n", 14, 1);
        editor.insert("<% " + jspCode + " %>\n" , 19, 9);
        sleep(5000);
        editor.saveDocument();
        new Action("Run|Run File",null).perform();
        waitBuildSuccessful();
        assertDisplayerContent("<title>Page 2</title>");
        new Node(rootNode,"Web Pages|page2.jsp").performPopupAction("View Servlet");
        EditorOperator servlet = new EditorOperator("page2_jsp.java");
        assertNotNull("SERVLET CONTEXT SHOULD BE SHOWN", servlet);
        String text = "file: page2_jsp.java\n"+servlet.getText();
        for (String str : new String[] {"<h1>",runningViewServlet,"JspWriter out",jspCode}) {
            assertContains(text, str);
        }
        editor.deleteLine(12);
        editor.deleteLine(13);
        editor.deleteLine(19);
        sleep(5000);        
        editor.save();
        editor.close();
        servlet.close();
    }
    
    public void testRunServlet() {
        initDisplayer();
        Node sourceNode = phelper.getSourceNode();
        new Node(sourceNode,"test1|Servlet1.java").performPopupAction("Open");
        EditorOperator editor = new EditorOperator("Servlet1.java");
        String expected = "response.setContentType(\"text/html;charset=UTF-8\");";
        assertTrue("Servlet1.java should contain "+expected, editor.contains(expected));
        editor.replace("/* TODO output your page here", "");
        editor.replace("            */", "");
        editor.replace("out.println(\"<title>Servlet Servlet1</title>\");",
                "out.println(\"<title>Servlet with name=\"+request.getParameter(\"name\")+\"</title>\");");
        new ActionNoBlock("Run|Run File",null).perform();
        NbDialogOperator dialog = new NbDialogOperator("Set Servlet Execution URI");
        JComboBoxOperator combo = new JComboBoxOperator(dialog);
        combo.setSelectedItem(combo.getSelectedItem()+"?name=Servlet1");
        dialog.ok();
        //waitBuildSuccessful();
        editor.close();
        getServerNode(Server.ANY).waitFinished();
        sleep(2000);
        assertDisplayerContent("<title>Servlet with name=Servlet1</title>");
    }
    
    public void testCreateTLD() {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        rootNode.select();
        new ActionNoBlock("File|New File", null).perform();
        // WORKAROUND
        new EventTool().waitNoEvent(1000);
        WizardOperator newFileWizard = new WizardOperator("New File");
        new JComboBoxOperator(newFileWizard).selectItem(PROJECT_NAME);
        new Node(new JTreeOperator(newFileWizard), "Web").select();
        new JListOperator(newFileWizard, 1).selectItem("Tag Library Descriptor");
        newFileWizard.next();
        JTextFieldOperator txtName = new JTextFieldOperator(newFileWizard);
        // clear text field
        txtName.setText("");
        txtName.typeText("MyTags");
        newFileWizard.finish();
        //XXX try { Thread.currentThread().sleep(5000); } catch (InterruptedException e) {}
        //XXX HACK #48865
        new Node(new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME), "Web Pages|WEB-INF|tlds|MyTags.tld");
        // check class is opened in Editor and then close it
        new EditorOperator("MyTags.tld").close();
        ref(Util.dumpProjectView(PROJECT_NAME));
        //compareReferenceFiles();
    }
    
    public void testCreateTagHandler() throws InterruptedException {
        new ActionNoBlock("File|New File", null).perform();
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
        newFileWizard.btNext().waitComponentEnabled();
        newFileWizard.next();
        new JButtonOperator(newFileWizard, "Browse").push();
        NbDialogOperator dialog = new NbDialogOperator("Browse Files");
        new Node(new JTreeOperator(dialog),"Web Pages|WEB-INF|tlds|MyTags.tld").select();
        new JButtonOperator(dialog,"Select File").push();
        newFileWizard.finish();
        // check class is opened in Editor and then close it
        EditorOperator editor = new EditorOperator("MyTag.java");
        editor.replace("// out.println(\"    </blockquote>\");", getTagHandlerCode());
        editor.saveDocument();
        editor.close();
        ref(Util.dumpProjectView(PROJECT_NAME));
        //compareReferenceFiles();
    }

    protected String getTagHandlerCode() {
        return "out.print(\"TagOutput\"); \n";
    }
    
    public void testRunTag() throws Throwable {
        initDisplayer();
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        Node jspNode = new Node(rootNode,"Web Pages|page2.jsp");
        jspNode.performPopupAction("Open");
        EditorOperator editor = new EditorOperator("page2.jsp");
        editor.replace("<title>Page 2</title>",
                "<title><my:MyTag/></title>");
        editor.insert("<%@taglib prefix=\"my\" uri=\"/WEB-INF/tlds/MyTags\"%>\n", 6, 1);
        sleep(5000);
        editor.save();
        new Action(null,"Run File").perform(jspNode);
        waitBuildSuccessful();
        editor.close();
        assertDisplayerContent("<title>TagOutput</title>");
    }
    
    public void testNewHTML() throws IOException {
        // workaround due to issue #46073
        new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME).select();
        
        new ActionNoBlock("File|New File", null).perform();
        // WORKAROUND
        new EventTool().waitNoEvent(1000);
        WizardOperator newFileWizard = new WizardOperator("New File");
        new JComboBoxOperator(newFileWizard).selectItem(PROJECT_NAME);
        new Node(new JTreeOperator(newFileWizard), "Web").select();
        JListOperator fileTypeList = new JListOperator(newFileWizard, 1);
        fileTypeList.setComparator(new Operator.DefaultStringComparator(true, true));
        fileTypeList.selectItem("HTML");
        newFileWizard.next();
        JTextFieldOperator txtPackageName = new JTextFieldOperator(newFileWizard);
        // clear text field
        txtPackageName.setText("");
        txtPackageName.typeText("HTML");
        newFileWizard.finish();
        // check class is opened in Editor and then close it
        new EditorOperator("HTML.html").close();
        ref(Util.dumpProjectView(PROJECT_NAME));
        new Node(new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME),"Web Pages|HTML.html");
        //compareReferenceFiles();
        //compareDD();
    }
    
    public void testHTMLNavigator()throws Exception{
        String fileName = "navigatorHTML.html";
        open(fileName);
        EditorOperator eop = new EditorOperator(fileName);
        String htmlText = new EditorOperator(fileName).getText();
        int caretPosition = verifyNavigator(eop);
        assertEquals("NAVIGATION TARGET", 144, caretPosition);
        openFromProject("HTML.html", htmlText);
        caretPosition = verifyNavigator(new EditorOperator("HTML.html"));
        assertEquals("NAVIGATION TARGET", 144, caretPosition);
        EditorOperator.closeDiscardAll();
        compareReferenceFiles();
    }
    
    public void testJSPNavigator()throws Exception{
        String fileName = "navigatorJSP.jsp";
        open(fileName);
        EditorOperator eop = new EditorOperator(fileName);
        String jspText = eop.getText();
        int caretPosition = verifyNavigator(eop);
        assertEquals("NAVIGATION TARGET", 325, caretPosition);
        openFromProject("page2.jsp", jspText);
        caretPosition = verifyNavigator(new EditorOperator("page2.jsp"));
        assertEquals("NAVIGATION TARGET", 325, caretPosition);
        EditorOperator.closeDiscardAll();
        compareReferenceFiles();
    }

    private void dumpNode(TreeModel model, Object node, int offset) {
        for (int i = 0; i < offset; i++) {
            getRef().print("\t");
        }
        getRef().println(node.toString());
        if (model.isLeaf(node)){
            return;
        }
        for (int i = 0; i < model.getChildCount(node); i++) {
            Object object = model.getChild(node, i);
            dumpNode(model, object, offset + 1);
        }
    }
    
    private void open(String fileName)throws Exception{
        DataObject dataObj = DataObject.find(FileUtil.toFileObject(new File(getDataDir(), fileName)));
        EditorCookie ed = dataObj.getCookie(EditorCookie.class);
        ed.open();
    }
    
    private void openFromProject(String fileName, String text)throws Exception{
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        new Node(rootNode,"Web Pages|"+fileName).performPopupAction("Open");
        EditorOperator eop = new EditorOperator(fileName);
        eop.delete(0, eop.getText().length());
        eop.insert(text);
    }
    
    private int verifyNavigator(EditorOperator eOperator)throws Exception{
        eOperator.insert("<h1>JSP Page</h1>\n<table border=\"1\">\n <tr> \n<td></td> \n  <td></td>\n</tr> \n" +
                "<tr>\n<td></td>\n<td></td>\n</tr>\n</table>\n", 10, 5);
        eOperator.save();
        //refresh navigator
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        new Node(rootNode,"Web Pages|index.jsp").performPopupAction("Open");
        new EditorOperator("index.jsp").closeDiscard();
        //wait for editor update
        Thread.sleep(1000);
        int startCaretPos = eOperator.txtEditorPane().getCaretPosition();
        NavigatorOperator navigatorOperator = NavigatorOperator.invokeNavigator();
        assertNotNull(navigatorOperator);
        JTreeOperator treeOperator = navigatorOperator.getTree();
        TreeModel model = treeOperator.getModel();
        Object root = model.getRoot();
        assertNotNull(root);
        dumpNode(model, root, 0);
//        assertEquals(1, treeOperator.getChildCount(root));
        Object htmlChild = treeOperator.getChild(root, 0);//HTML
        assertNotNull(htmlChild);
//        assertEquals(2, treeOperator.getChildCount(htmlChild));// HEAD, BODY
        Object bodyChild = treeOperator.getChild(htmlChild, 1);
//        assertEquals(2, treeOperator.getChildCount(bodyChild));// H1, TABLE
        Object tableChild = treeOperator.getChild(bodyChild, 1);
//        assertEquals(2, treeOperator.getChildCount(tableChild));// 2 rows
        Object[] pathObjects = {root, htmlChild, bodyChild, tableChild};
        TreePath path = new TreePath(pathObjects);
        if (root.toString() != null && "Wait".contains(root.toString())){
            Thread.sleep(5000);
        }
        treeOperator.clickOnPath(path, 2);
        // wait for editor update
        Thread.sleep(1000);
        int finalCaretPossition = eOperator.txtEditorPane().getCaretPosition();
        assertFalse("move in document", finalCaretPossition == startCaretPos);
        return finalCaretPossition;
    }

    public void testRunHTML() {
        initDisplayer();
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        new Node(rootNode,"Web Pages|HTML.html").performPopupAction("Open");
        new EditorOperator("HTML.html").replace("<title></title>",
                "<title>HTML Page</title>");
        new Action("Run|Run File",null).perform();
        //waitBuildSuccessful();
        new EditorOperator("HTML.html").close();
        assertDisplayerContent("<title>HTML Page</title>");
    }
    
    public void testNewSegment() throws IOException {
        Node sample1Node = new Node(new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME),"Web Pages");
        // create a new class
        new ActionNoBlock(null, "New|JSP").perform(sample1Node);
        // WORKAROUND
        new EventTool().waitNoEvent(1000);
        WizardOperator newFileWizard = new WizardOperator("New JSP File");
        new JTextFieldOperator(newFileWizard).typeText("segment");
        new JCheckBoxOperator(newFileWizard).changeSelection(true);
        newFileWizard.finish();
        // check class is opened in Editor and then close all documents
        new EditorOperator("segment.jspf").close();
        ref(Util.dumpProjectView(PROJECT_NAME));
        new Node(new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME),"Web Pages|segment.jspf");
        //compareReferenceFiles();
        //compareDD();
    }
    
    public void testNewDocument() throws IOException {
        Node sample1Node = new Node(new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME),"Web Pages");
        // create a new class
        new ActionNoBlock(null, "New|JSP").perform(sample1Node);
        // WORKAROUND
        new EventTool().waitNoEvent(1000);
        WizardOperator newFileWizard = new WizardOperator("New JSP File");
        new JTextFieldOperator(newFileWizard).typeText("document");
        new JRadioButtonOperator(newFileWizard,1).changeSelection(true);
        newFileWizard.finish();
        // check class is opened in Editor and then close all documents
        new EditorOperator("document.jspx").close();
        ref(Util.dumpProjectView(PROJECT_NAME));
        new Node(new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME),"Web Pages|document.jspx");
        //compareReferenceFiles();
        //compareDD();
    }
    
    public void testStopServer() throws Exception {
        server.stop();
        verifyServerIsStopped();
    }
    
    public void testStartServer() throws Exception {
        server.start();
        URL url = server.getServerURL();
        URLConnection connection = url.openConnection();
        connection.connect();
    }
    
    public void testBrowserSettings() {
        OptionsOperator optionsOper = OptionsOperator.invoke();
        optionsOper.selectGeneral();
        // "Web Browser:"
        String webBrowserLabel = Bundle.getStringTrimmed(
                "org.netbeans.core.ui.options.general.Bundle",
                "CTL_Web_Browser");
        JLabelOperator jloWebBrowser = new JLabelOperator(optionsOper, webBrowserLabel);
        JComboBoxOperator combo = new JComboBoxOperator((JComboBox)jloWebBrowser.getLabelFor());
        for (int i=0; i<combo.getItemCount(); i++) {
            String item = combo.getItemAt(i).toString();
            log("Browser: "+item);
            if (!item.equals("<Default System Browser>") &&
                    !item.equals("Mozilla") &&
                    !item.equals("Netscape") &&
                    !item.equals("Swing HTML Browser") &&
                    !item.equals("Internet Explorer") &&
                    !item.equals("Firefox") &&
                    !item.equals("External Browser (command-line)"))
                fail("Unrecognized settings in Web Browser Options");
        }
        optionsOper.close();
    }
    
    public void testFinish() {
        server.stop();
        new ActionNoBlock(null, "Close").perform(new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME));
    }
    
    //********************************************************
    
    protected void sleep(int milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException ex) {
            throw new JemmyException("Interrupted", ex);
        }
    }
    
    public void logAndCloseOutputs() {
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
                String logName = "Output"+logIdx+++".log";
                log(logName, outputTab.getName()+"\n-------------\n\n"+outputTab.getText());
                outputTab.close();
            } while(true);
        } finally {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", timeout);
        }
    }

    public void waitBuildSuccessfulInActualTab() {
        final OutputOperator oo = new OutputOperator();
        oo.waitState(new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                return oo.getText().contains(BUILD_SUCCESSFUL);
            }
            public String getDescription() {
                return("\"" + BUILD_SUCCESSFUL + "\" text");
            }
        });
    }

    public void waitBuildSuccessful() {
        OutputTabOperator console = new OutputTabOperator(PROJECT_NAME);
        console.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 180000);
        boolean ok = false;
        try{
            console.waitText(BUILD_SUCCESSFUL);
            ok = true;
        }finally{
            if (!ok){
                System.err.println(console.getText());
            }
        }
    }

    public void initDisplayer() {
        if (urlDisplayer == null) {
            urlDisplayer = TestURLDisplayer.getInstance();
        }
        urlDisplayer.invalidateURL();
    }
    
    public void assertDisplayerContent(String substr) {
        try {
            urlDisplayer.waitURL();
        } catch (InterruptedException ex) {
            throw new JemmyException("Waiting interrupted.", ex);
        }
        String page = urlDisplayer.readURL();
        boolean contains = page.indexOf(substr) > -1;
        if (!contains) {
            log("DUMP OF: "+urlDisplayer.getURL()+"\n");
            log(page);
        }
        assertTrue("The '"+urlDisplayer.getURL()+"' page does not contain '"+substr+"'", contains);
    }
    
    public void assertContains(String text, String value) {
        assertTrue("Assertation failed, cannot find:\n"+value+"\nin the following text:\n"+text, text.contains(value));
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
                instance.serverPort = 8084;
                instance.nodeName = "Tomcat";
                instance.userName = "tomcat";
                instance.password = "tomcat";
            }
            return instance;
        }
        
        public URL getServerURL() {
            if (serverURL == null) {
                try {
                    serverURL = new URL("http",host,serverPort,"");
                } catch (MalformedURLException mue) {
                    throw new JemmyException("Cannot create server URL.", mue);
                }
            }
            System.out.println("serverurl:" + serverURL);
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

    private void verifyServerIsStopped() throws IOException {
        URL url = server.getServerURL();
        URLConnection connection = url.openConnection();
        try {
            connection.connect();
            fail("Connection to: "+url+" established, but the server" +
                    " should not be running.");
        } catch (ConnectException e) {  }
    }
}

