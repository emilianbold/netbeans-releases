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

package gui;

import javax.swing.tree.TreePath;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.actions.EditAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.performance.test.utilities.PerformanceTestCase;

import org.netbeans.progress.module.Controller;
import org.netbeans.progress.spi.InternalHandle;
import org.netbeans.progress.spi.TaskModel;


/**
 * Utilities for Performance tests, workarrounds, often used methods, ...
 *
 * @author  mmirilovic@netbeans.org
 */
public class Utilities {
    
    public static final String SOURCE_PACKAGES = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.Bundle", "NAME_src.dir");
    public static final String TEST_PACKAGES = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.Bundle", "NAME_test.src.dir");
    public static final String WEB_PAGES = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.Bundle", "LBL_Node_DocBase");
    private static PerformanceTestCase test = null;
    /** Creates a new instance of Utilities */
    public Utilities() {
    }


    public static String getTimeIndex() {
        return new SimpleDateFormat("HHmmssS",Locale.US).format(new Date());
    }
    
    /**
     * Close Welcome.
     */
    public static void closeWelcome(){
        TopComponentOperator tComponent = new TopComponentOperator(Bundle.getStringTrimmed("org.netbeans.modules.welcome.Bundle","LBL_Tab_Title"));
        new JCheckBoxOperator(tComponent,Bundle.getStringTrimmed("org.netbeans.modules.welcome.resources.Bundle","LBL_ShowOnStartup")).changeSelection(false);
        tComponent.close();
    }
    
    /**
     * Close BluePrints.
     */
    public static void closeBluePrints(){
        new TopComponentOperator(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.blueprints.Bundle","LBL_Tab_Title")).close();
    }
    
    /**
     * Close All Documents.
     */
    public static void closeAllDocuments(){
        new CloseAllDocumentsAction().perform();
    }
    
    /**
     * Close Memory Toolbar.
     */
    public static void closeMemoryToolbar(){
        closeToolbar(Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/View") + "|" +
                Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle","CTL_ToolbarsListAction") + "|" +
                Bundle.getStringTrimmed("org.netbeans.core.Bundle","Toolbars/Memory"));
    }
    
    private static void closeToolbar(String menu){
        MainWindowOperator mainWindow = MainWindowOperator.getDefault();
        JMenuBarOperator menuBar = new JMenuBarOperator(mainWindow.getJMenuBar());
        JMenuItemOperator menuItem = menuBar.showMenuItem(menu,"|");
        
        if(menuItem.isSelected())
            menuItem.push();
        else {
            menuItem.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
            mainWindow.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
        }
    }
    
    /**
     * Work around issue 35962 (Main menu popup accidentally rolled up)
     * Issue has been fixed for JDK 1.5, so we will use it only for JDK 1.4.X
     */
    public static void workarroundMainMenuRolledUp() {
        if(System.getProperty("java.version").indexOf("1.4") != -1) {
            String helpMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/Help") + "|" + Bundle.getStringTrimmed("org.netbeans.core.actions.Bundle" , "About");
            String about = Bundle.getStringTrimmed("org.netbeans.core.Bundle_nb", "CTL_About_Title");
            
            new ActionNoBlock(helpMenu, null).perform();
            new NbDialogOperator(about).close();
        }
    }
    
    /**
     * Open files
     *
     * @param project project which will be used as source for files to be opened
     * @param files_path path to the files to be opened
     */
    public static void openFiles(String project, String[][] files_path){
        Node[] openFileNodes = new Node[files_path.length];
        
        SourcePackagesNode sourcePackagesNode = new SourcePackagesNode(project);
        
        for(int i=0; i<files_path.length; i++) {
            openFileNodes[i] = new Node(sourcePackagesNode, files_path[i][0] + '|' + files_path[i][1]);
            
            // open file one by one, opening all files at once causes never ending loop (java+mdr)
            // new OpenAction().performAPI(openFileNodes[i]);
        }
        
        // try to come back and open all files at-once, rises another problem with refactoring, if you do open file and next expand folder,
        // it doesn't finish in the real-time -> hard to reproduced by hand
        new OpenAction().performAPI(openFileNodes);
    }
    
    /**
     * Copy file f1 to f2
     * @param f1 file 1
     * @param f2 file 2
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public static void copyFile(java.io.File f1, java.io.File f2) throws java.io.FileNotFoundException, java.io.IOException{
        int data;
        java.io.InputStream fis = new java.io.BufferedInputStream(new java.io.FileInputStream(f1));
        java.io.OutputStream fos = new java.io.BufferedOutputStream(new java.io.FileOutputStream(f2));
        
        while((data=fis.read())!=-1){
            fos.write(data);
        }
    }
    
    /**
     * Invoke open action on file and wait for editor
     * @param filename
     * @param waitforeditor
     * @return
     */
    public static EditorOperator openFile(Node fileNode, String filename, boolean waitforeditor) {
        new OpenAction().performAPI(fileNode);
        
        if (waitforeditor) {
            EditorOperator editorOperator = new EditorOperator(filename);
            return editorOperator;
        } else
            return null;
    }
    
    
    public static EditorOperator openFile(String project, String filepackage, String filename, boolean waitforeditor) {
        return openFile(new Node(new SourcePackagesNode(project), filepackage + "|" + filename), filename, waitforeditor);
    }
    
    /**
     * Invoke Edit Action on file and wait for editor
     * @param project
     * @param filepackage
     * @param filename
     * @return
     */
    public static EditorOperator editFile(String project, String filepackage, String filename) {
        Node filenode = new Node(new SourcePackagesNode(project), filepackage + "|" + filename);
        new EditAction().performAPI(filenode);
        EditorOperator editorOperator = new EditorOperator(filename);
        return editorOperator;
    }
    
    
    /**
     * open small form file in the editor
     * @return Form Designer
     */
    public static FormDesignerOperator openSmallFormFile(){
        Node openFile = new Node(new SourcePackagesNode("PerformanceTestData"),"org.netbeans.test.performance|JFrame20kB.java");
        new OpenAction().performAPI(openFile);
        return new FormDesignerOperator("JFrame20kB");
        
    }
    
    
    /**
     * Edit file and type there a text
     * @param filename file that will be eddited
     * @param line line where put the text
     * @param text write the text
     * @param save save at the and
     */
    public static void insertToFile(String filename, int line, String text, boolean save) {
        EditorOperator editorOperator = new EditorOperator(filename);
        editorOperator.setCaretPositionToLine(line);
        editorOperator.insert(text);
        
        if (save)
            editorOperator.save();
    }
    
    /**
     * Create project
     * @param category project's category
     * @param project type of the project
     * @param wait wait for background tasks
     * @return name of recently created project
     */
    public static String createproject(String category, String project, boolean wait) {
        // select Projects tab
        ProjectsTabOperator.invoke();
        
        // create a project
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.next();
        
        NewProjectNameLocationStepOperator wizard_location = new NewProjectNameLocationStepOperator();
        wizard_location.txtProjectLocation().clearText();
        wizard_location.txtProjectLocation().typeText(System.getProperty("xtest.tmpdir"));
        String pname = wizard_location.txtProjectName().getText();
        
        // if the project exists, try to generate new name
        for (int i = 0; i < 5 && !wizard.btFinish().isEnabled(); i++) {
            pname = pname+"1";
            wizard_location.txtProjectName().clearText();
            wizard_location.txtProjectName().typeText(pname);
        }
        wizard.finish();
        
        // wait 10 seconds
        waitForProjectCreation(10000, wait);
        
        return pname;
    }
    
    
    protected static void waitForProjectCreation(int delay, boolean wait){
        try {
            Thread.currentThread().sleep(delay);
        } catch (InterruptedException exc) {
            exc.printStackTrace(System.err);
        }
        
        // wait for classpath scanning finish
        if (wait) {
            waitScanFinished();
            waitForPendingBackgroundTasks();
        }
    }
    
    
    /**
     * Delete project
     * @param project project to be deleted
     */
    public static void deleteProject(String project) {
        deleteProject(project, false);
    }
    
    
    public static void deleteProject(String project, boolean waitStatus) {
        new DeleteAction().performAPI(ProjectsTabOperator.invoke().getProjectRootNode(project));
        
        //delete project
        NbDialogOperator deleteProject = new NbDialogOperator("Delete Project"); // NOI18N
        JCheckBoxOperator delete_sources = new JCheckBoxOperator(deleteProject);
        
        if(delete_sources.isEnabled())
            delete_sources.changeSelection(true);
        
        deleteProject.yes();
        
        waitForPendingBackgroundTasks();
        
        if(waitStatus)
            MainWindowOperator.getDefault().waitStatusText("Finished building "+project+" (clean)"); // NOI18N
        
        try {
            //sometimes dialog rises
            new NbDialogOperator("Question").yes(); // NOI18N
        }catch(Exception exc){
            System.err.println("No Question dialog rises - no problem this is just workarround!");
            exc.printStackTrace(System.err);
        }
        
    }
    
    
    
    /**
     * Build project and wait for finish
     * @param project
     */
    public static void buildproject(String project) {
        ProjectRootNode prn = ProjectsTabOperator.invoke().getProjectRootNode(project);
        prn.buildProject();
        MainWindowOperator.getDefault().waitStatusText("Finished building "+project); // NOI18N
    }
    
    /**
     * Invoke action on project node from popup menu
     * @param project
     * @param pushAction
     */
    public static void actionOnProject(String project, String pushAction) {
        ProjectRootNode prn = ProjectsTabOperator.invoke().getProjectRootNode(project);
        prn.callPopup().pushMenuNoBlock(pushAction);
    }
    
    /**
     * Run project
     * @param project
     */
    public static void runProject(String project) {
        actionOnProject(project,"Run Project"); // NOI18N
        // TODO MainWindowOperator.getDefault().waitStatusText("run"); // NOI18N
    }
    
    /**
     * Debug project
     * @param project
     */
    public static void debugProject(String project) {
        actionOnProject(project,"Debug Project"); // NOI18N
        // TODO MainWindowOperator.getDefault().waitStatusText("debug"); // NOI18N
    }
    
    
    /**
     * Test project
     * @param project
     */
    public static void testProject(String project) {
        actionOnProject(project, "Test Project"); // NOI18N
        // TODO MainWindowOperator.getDefault().waitStatusText("test"); // NOI18N
    }
    
    /**
     * Deploy project and wait for finish
     * @param project
     */
    public static void deployProject(String project) {
        actionOnProject(project, "Deploy Project"); // NOI18N
        waitForPendingBackgroundTasks();
        MainWindowOperator.getDefault().waitStatusText("Finished building "+project+" (run-deploy)"); // NOI18N
    }
    
    /**
     * Verify project and wait for finish
     * @param project
     */
    public static void verifyProject(String project) {
        actionOnProject(project, "Verify Project"); // NOI18N
        MainWindowOperator.getDefault().waitStatusText("Finished building "+project+" (verify)"); // NOI18N
    }
    
    
    /**
     * Open project and wait until it's scanned
     * @param projectFolder Project's location
     */
    public static void waitProjectOpenedScanFinished(String projectFolder){
        ProjectSupport.openProject(projectFolder);
        waitScanFinished();
    }
    
    public static void waitForPendingBackgroundTasks() {
        waitForPendingBackgroundTasks(5);
    }
    
    public static void waitForPendingBackgroundTasks(int n) {
        // wait maximum n minutes
        for (int i=0; i<n*60; i++) {
            if (org.netbeans.progress.module.Controller.getDefault().getModel().getSize()==0)
                return;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException exc) {
                exc.printStackTrace(System.err);
                return;
            }
        }
    }
    
    public static Node getApplicationServerNode(){
        RuntimeTabOperator rto = new RuntimeTabOperator().invoke();
        
        TreePath path = null;
        
        // create exactly (full match) and case sensitively comparing comparator
//        Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(false, false);
//        StringComparator previousComparator = rto.tree().getComparator();
//        rto.setComparator(comparator);
        JTreeOperator runtimeTree = rto.tree();
        long oldTimeout = runtimeTree.getTimeouts().getTimeout("JTreeOperator.WaitNextNodeTimeout");
        runtimeTree.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", 60000);
        try {
            log("Looking path = Servers");
            path = runtimeTree.findPath("Servers");
            runtimeTree.selectPath(path);
            log("Looking path = Servers|GlassFish V2");
            path = runtimeTree.findPath("Servers|GlassFish V2"); // NOI18N
            runtimeTree.selectPath(path);
        } catch (Exception exc) {
            exc.printStackTrace(System.err);
            throw new Error("Cannot find Application Server Node: "+exc.getMessage());
        }
        runtimeTree.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", oldTimeout);
//        rto.setComparator(previousComparator);
        return new Node(runtimeTree,path);
    }
    
    
    public static Node startApplicationServer() {
        Node node = performApplicationServerAction("Start", "Starting");  // NOI18N
        new EventTool().waitNoEvent(80000);
        return node;
    }
    
    public static Node stopApplicationServer() {
        Node node = performApplicationServerAction("Stop", "Stopping");  // NOI18N
        new EventTool().waitNoEvent(50000);
        return node;
    }
    
    /**
     * Invoke action on Application server node (start/stop/...)
     * @param action Action to be invoked on the Application server node
     */
    private static Node performApplicationServerAction(String action, String message) {
        Node asNode = getApplicationServerNode();
        asNode.select();
        
        String serverIDEName = asNode.getText();
        log("ServerNode name = "+serverIDEName);
        JPopupMenuOperator popup = asNode.callPopup();
        if (popup == null) {
            throw new Error("Cannot get context menu for Application server node ");
        }
        boolean startEnabled = popup.showMenuItem(action).isEnabled();
        if(startEnabled) {
            popup.pushMenuNoBlock(action);
        }
        
        waitForAppServerTask(message, serverIDEName);
        
        return asNode;
    }
    
    private static void waitForAppServerTask(String taskName, String serverIDEName) {
        Controller controller = Controller.getDefault();
        TaskModel model = controller.getModel();
        
        InternalHandle task = waitServerTaskHandle(model,taskName+" "+serverIDEName);
        long taskTimestamp = task.getTimeStampStarted();
        
        log("task started at : "+taskTimestamp);
        int i=0;
        while(i<12000) { // max 12000*50=600000=10 min
            i++;
            int state = task.getState();
            if(state == task.STATE_FINISHED) { return; }
            try {
                Thread.sleep(50);
            } catch (InterruptedException exc) {
                exc.printStackTrace(System.err);
                return;
            }
        }
    }
    
    private static InternalHandle waitServerTaskHandle(TaskModel model, String serverIDEName) {
        for(int i=0; i<12000; i++) { // max 12000*50=600000=10 min
            InternalHandle[] handles =  model.getHandles();
            InternalHandle  serverTask = getServerTaskHandle(handles,serverIDEName);
            if(serverTask != null) {
                log("Returning task handle");
                return serverTask;
            }
            
            try {
                Thread.sleep(50);
            } catch (InterruptedException exc) {
                exc.printStackTrace(System.err);
            }
        }
        
        return null;
    }
    
    private static InternalHandle getServerTaskHandle(InternalHandle[] handles, String taskName) {
        if(handles.length == 0)  {
            log("Empty tasks queue");
            return null;
        }
        
        for (InternalHandle internalHandle : handles) {
            if(internalHandle.getDisplayName().equals(taskName)) {
                log("Expected task found...");
                return internalHandle;
            }
        }
        return null;
    }
    
    /**
     * Wait finished scan - repeatedly
     */
    public static void waitScanFinished(){
        ProjectSupport.waitScanFinished();
        new QueueTool().waitEmpty(1000);
        ProjectSupport.waitScanFinished();
    }
    public static void initLog(PerformanceTestCase testCase) {
        test = testCase;
    }
    public static void closeLog() {
        test = null;
    }
    private static void log(String logMessage) {
        System.out.println("Utilities::"+logMessage);
        if( test != null  ) { test.log("Utilities::"+logMessage); }
    }
    
}
