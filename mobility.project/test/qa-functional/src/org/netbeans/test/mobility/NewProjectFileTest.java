/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.test.mobility;


import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import javax.swing.JDialog;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.operators.DialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.CompileAction;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JDialogOperator;
//import org.netbeans.jemmy.operators.JToggleButtonOperator;
/**
 */
public class NewProjectFileTest extends JellyTestCase {
    
    public static final String ITEM_VISUALMIDLET = Bundle.getStringTrimmed("org.netbeans.modules.vmd.midp.resources.Bundle", "Templates/MIDP/VisualMIDlet.java");
    public static final String ITEM_MIDLET = Bundle.getStringTrimmed("org.netbeans.modules.mobility.project.ui.wizard.Bundle", "Templates/MIDP/Midlet.java");
    public static final String ITEM_MIDPCANVAS = Bundle.getStringTrimmed("org.netbeans.modules.mobility.project.ui.wizard.Bundle", "Templates/MIDP/MIDPCanvas.java");
    public static final String ITEM_LOCALIZATIONSUPPORTCLASS = "Localization Support Class";
    //public static final String ITEM_HELLOVISUALMIDLET = "Hello Visual Midlet"; //template removed
    
    public static final String CATEGORY_MIDP = Bundle.getStringTrimmed("org.netbeans.modules.mobility.project.ui.wizard.Bundle", "Templates/MIDP");
    public static final String CATEGORY_CDC = "CDC"; //TODO I18N
    
    public static final String PROJECT_MOBILE_APP = Bundle.getStringTrimmed("org.netbeans.modules.mobility.project.ui.wizard.Bundle", "Templates/Project/J2ME/MobileApplication");
    public static final String PROJECT_CDC_APP = "CDC Application";//Bundle.getStringTrimmed("org.netbeans.modules.mobility.project.ui.wizard.Bundle", "Templates/Project/J2ME/MobileApplication");
        
    public static final HashSet disabledNodes = new HashSet();
    private static String projectDirectory;
    
    public static final String SAMPLE_MOBILE_PROJECT = "SampleMobileApplication";
    public static final String PROJECT_TO_BE_CREATED = "NewCreatedMobileProject";
    public static final String PROJECT_TO_BE_CLOSED = "ProjectToBeClosedMobile";
    public static final String PROJECT_TO_BE_OPENED = "OpenMobileProject";
    public static final String PROJECT_FOR_NEW_FILES = "CreateNewFilesMobile";
    public static final String PROJECT_OPENED = "MobileApplicationVisualMIDlet";
    
    public NewProjectFileTest(String name) {
        super(name);
    }
    
    /*
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        //suite.addTest(new NewProjectFileTest("testNewProject"));//TODO can be used only when a J2ME platform is available
        suite.addTest(new NewProjectFileTest("testCreateNewFiles"));
        suite.addTest(new NewProjectFileTest("testOpenProject"));
        suite.addTest(new NewProjectFileTest("testCloseProject")); //must be last. It close the SampleMobileApplication project that is used in other tests.
        return suite;
    }
     */
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        
        junit.textui.TestRunner.run(new NewProjectFileTest("testCreateNewMIDPFilesValidation"));
    }
    
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }
    
    public void tearDown() {
    }
    
    
    public void testNewProject() throws IOException {
        //TODO it's necessary to have a J2ME platform to be able create Mobile Project
        //TODO we need a fake platform that can be used even when the mobility pack is built from sources and no regular platform is available
        createNewProject(PROJECT_MOBILE_APP, PROJECT_TO_BE_CREATED);
        // wait project appear in projects view
        new ProjectsTabOperator().getProjectRootNode(PROJECT_TO_BE_CREATED);
    }
    
    public String createNewFile(String category, String template, String name, String packageName) {
        NewFileWizardOperator newFile = NewFileWizardOperator.invoke(); 
        newFile.selectCategory(category);
        newFile.selectFileType(template);
        newFile.next();
        NewFileNameLocationStepOperator op = new NewFileNameLocationStepOperator();
        op.setObjectName(name); //TODO doesn't work with New > MIDP Canvas. It doesn;t change the name
        if(packageName != null) 
            op.setPackage(packageName);
        String fileLocation = op.txtCreatedFile().getText();
        op.finish();
        return fileLocation;
    }
    
    public String createNewProject(String projectType, String projectName) {
        MainWindowOperator mainWindow = MainWindowOperator.getDefault();
        NewProjectWizardOperator npwop = NewProjectWizardOperator.invoke();
        npwop.selectCategory(Bundle.getStringTrimmed("org.netbeans.modules.mobility.project.ui.wizard.Bundle", "Templates/Project/J2ME")); 
        npwop.selectProject(projectType); 
        npwop.next();
        NewProjectNameLocationStepOperator step = new NewProjectNameLocationStepOperator();
        step.txtProjectLocation().setText(getWorkDirPath());
        step.txtProjectName().setText(projectName);//NOI18N
        String projectLocation = step.txtProjectFolder().getText();
        sleep(1000);
        step.finish();
        return projectLocation;
    }
    
    public void testCloseProject() {
        //EditorOperator.closeDiscardAll();
        //select projects tab
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        JTreeOperator tree = pto.tree();
        //select the project node
        Node projectNode = pto.getProjectRootNode(PROJECT_TO_BE_CLOSED);
        projectNode.select();
        //close it
        projectNode.performPopupAction("Close Project"); //TODO - I18N - get it from bundle
        //check that project is closed
        pto.getProjectRootNode(PROJECT_TO_BE_CLOSED);
        
    }
    
    public void testOpenProject() throws IOException {
        MainWindowOperator mainWindow = MainWindowOperator.getDefault();
        mainWindow.getToolbarButton(mainWindow.getToolbar("File"),"Open Project").push();//TODO I18N
        
        DialogOperator dia = new DialogOperator("Open Project"); //TODO I18N
        JTextFieldOperator text = new JTextFieldOperator(dia, 1);
        text.setText(getDataDir().getPath() +File.separator+PROJECT_TO_BE_OPENED);
        new JButtonOperator(dia, "Open Project Folder").push();//TODO I18N
        sleep(2000);
        JDialog resolveDialog = NbDialogOperator.findJDialog("Open Project", true, true);//TODO I18N
        if (resolveDialog != null) {
            new JButtonOperator(new JDialogOperator(resolveDialog), "Close").push();//TODO I18N
        }
        
        JMenuBarOperator menubar = mainWindow.menuBar();
        
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        
        // get the tree if needed
        JTreeOperator tree = pto.tree();
        
        // Open HelloMidlet and Inspector
        Node projectNode = pto.getProjectRootNode("MobileApplication|hello|HelloMidlet.java");
        projectNode.select();
        projectNode.performPopupActionNoBlock("Open");
        sleep(2000);
        
        new PropertySheetOperator().close();
        EditorOperator.closeDiscardAll();
        
    }
    
    public void testCreateNewFiles() {
        //select the project in project view
        new ProjectsTabOperator().getProjectRootNode(PROJECT_FOR_NEW_FILES).select();
        //create all new files in the project
        createNewFile(CATEGORY_MIDP, ITEM_VISUALMIDLET, "NewVisualMidlet", "myPackage"); // NOI18N
        createNewFile(CATEGORY_MIDP, ITEM_MIDLET, "NewMIDlet", "myPackage"); // NOI18N
        createNewFile(CATEGORY_MIDP, ITEM_MIDPCANVAS, "MIDPCanvas", "myPackage"); // NOI18N
        
        
        //test that files are created and opened in editor
        new TopComponentOperator("NewVisualMidlet.java").close(); // NOI18N
        new EditorOperator("NewMIDlet.java").close(); // NOI18N
        new EditorOperator("MIDPCanvas.java").close();    // NOI18N    
        
    }
    
    public void testCreateNewMobileApplication(){
        //by default is the "Create HelloMidlet" checked when creating new project 
        String projectName = "NewMobileApplication"; // NOI18N
        //create
        String location = createNewProject(PROJECT_MOBILE_APP, projectName);
        System.out.println("project created : " + location); // NOI18N
        //check
        new ProjectsTabOperator().getProjectRootNode(projectName);
    }
    
    public void testCreateNewCDCApplication() {
        String projectName = "NewCDCApplication"; // NOI18N
        //create
        String location = createNewProject(PROJECT_CDC_APP, projectName);
        System.out.println("project created : " + location); // NOI18N
        //check
        new ProjectsTabOperator().getProjectRootNode(projectName);
    }
    
////------------------------------------- validation --------------
    
    public void testCreateNewMIDPFilesValidation() {
        //select the project in project view
        new ProjectsTabOperator().getProjectRootNode("NewMobileApplication").select(); // NOI18N
        //create all new files in the project

        createNewFile(CATEGORY_MIDP, ITEM_MIDLET, "NewMIDlet", "validation"); // NOI18N
        createNewFile(CATEGORY_MIDP, ITEM_MIDPCANVAS, "NewMIDPCanvas", "validation");    // NOI18N
        createNewFile(CATEGORY_MIDP, ITEM_VISUALMIDLET, "NewVisualMidlet", "validation"); // NOI18N
        
        //test that files are created and opened in editor
        new EditorOperator("NewMIDlet.java").close(); // NOI18N
        new EditorOperator("MIDPCanvas.java").close();  // NOI18N  //TODO: workaround for unchanged canvas name
        new TopComponentOperator("NewVisualMidlet.java").close(); // NOI18N //TODO: seems that this doesn't work :(
    }
    
    public void testCompileMobileMIDPProject() {
        // start to track Main Window status bar
        MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault().getStatusTextTracer();
        stt.start();
        // call Build on project node
        new ProjectsTabOperator().getProjectRootNode("NewMobileApplication").buildProject(); // NOI18N

        // wait message "Building NewMobileApplication (compile-single)..."
        stt.waitText("Building", true); // NOI18N
        // wait message "Finished building NewMobileApplication (compile-single)"
        stt.waitText("Finished building", true); // NOI18N
        stt.stop();
    }
////---------------------------------------------------------------
    public void testCreateNewMIDPCanvas() {
        //select the project in project view
        new ProjectsTabOperator().getProjectRootNode("NewMobileApplication").select(); // NOI18N
        //create new Canvas in the project

        createNewFile(CATEGORY_MIDP, ITEM_MIDPCANVAS, "NewMIDPCanvas", "validation");    // NOI18N
        
        //test that files are created and opened in editor
        new EditorOperator("NewMIDPCanvas.java").close();  // NOI18N
    }
    public void testCreateNewProjectIssue95668() {
        String projectName = PROJECT_TO_BE_CREATED + System.currentTimeMillis() ;
        System.out.println("creating project " + projectName); // NOI18N
        System.out.println("project created " + createNewProject(PROJECT_MOBILE_APP, projectName)); // NOI18N
        for(int i=0;i<50;i++) {
           new ProjectsTabOperator().getProjectRootNode(projectName).select();
           createNewFile(CATEGORY_MIDP, ITEM_VISUALMIDLET, "file"+i, "issue95668"); // NOI18N
        }
    }
    
//        public void testCreateNewProjectIssue95668() {
//        String projectName = PROJECT_TO_BE_CREATED + System.currentTimeMillis() ;
//        System.out.println("creating project " + projectName);
//        System.out.println("project created " + createNewProject(projectName));
//        for(int i=0;i<50;i++) {
//           new ProjectsTabOperator().getProjectRootNode(projectName).select();
//           createNewFile(ITEM_VISUALMIDLET, "file"+i);
//        }
//    }
    
    public void testVMClosesImmediatellyIssue101539() {
        createNewProject(PROJECT_MOBILE_APP, "issue101539"); // NOI18N
        for (int i = 0; i < 20; i++) {
            createNewFile(CATEGORY_MIDP, ITEM_VISUALMIDLET, "issue101539_"+i, "testing"); // NOI18N
        }
        //EditorOperator.
    }
    public void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}

