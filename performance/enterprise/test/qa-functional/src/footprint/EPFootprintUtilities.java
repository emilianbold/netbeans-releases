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

package footprint;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;

import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.actions.EditAction;
import org.netbeans.jellytools.actions.OpenAction;

import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;

import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;

import org.netbeans.junit.ide.ProjectSupport;


/**
 * Utilities for Memory footprint tests
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class EPFootprintUtilities extends gui.Utilities{
    
    static void collapseProject(String project) {
        ProjectRootNode prn = new ProjectsTabOperator().getProjectRootNode(project);
        prn.collapse();
    }
    
    static void deleteProject(String project) {
        deleteProject(project, true);
    }
    
    static void deleteProject(String project, boolean waitStatus) {
        new DeleteAction().performAPI(ProjectsTabOperator.invoke().getProjectRootNode(project));

        //delete project
        NbDialogOperator deleteProject = new NbDialogOperator("Delete Project");
        JCheckBoxOperator delete_sources = new JCheckBoxOperator(deleteProject);
        
        if(delete_sources.isEnabled())
            delete_sources.changeSelection(true);
        
        deleteProject.yes();
        
        waitForPendingBackgroundTasks();
        
        if(waitStatus)
            MainWindowOperator.getDefault().waitStatusText("Finished building "+project+" (clean)");

        try {
            //sometimes dialog rises
            new NbDialogOperator("Question").yes();
        }catch(Exception exc){
            System.err.println("No Question dialog rises - no problem this is just workarround!");
            exc.printStackTrace(System.err);
        }
        
    }
    
    static String creatJ2EEeproject(String category, String project, boolean wait) {
        return createProjectGeneral(category, project, wait, true);
    }
    
    static String createproject(String category, String project, boolean wait) {
        return createProjectGeneral(category, project, wait, false);
    }
    
    private static String createProjectGeneral(String category, String project, boolean wait, boolean j2eeProject) {
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

        if(j2eeProject) {
            new JComboBoxOperator(wizard_location,1).selectItem(1);
            new JCheckBoxOperator(wizard_location,"Create Application Client module:").setSelected(true);
        }
        
        // if the project exists, try to generate new name
        int iter = 0;
        while(!wizard.btFinish().isEnabled() && iter < 5){
            pname = pname+"1";
            wizard_location.txtProjectName().clearText();
            wizard_location.txtProjectName().typeText(pname);
            iter++;
        }
        wizard.finish();
        
        // wait for 10 seconds
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
        }
        
        // wait for classpath scanning finish
        if (wait) {
            new QueueTool().waitEmpty(1000);
            //checkScanFinished();
            waitForPendingBackgroundTasks();
            //} else {
            //    ProjectSupport.waitScanFinished();
        }
        
        return pname;
    }
    
    static void buildproject(String project) {
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        ProjectRootNode prn = pto.getProjectRootNode(project);
        prn.buildProject();
        MainWindowOperator.getDefault().waitStatusText("Finished building "+project);
    }
    
    static void actionOnProject(String project, String pushAction) {
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        ProjectRootNode prn = pto.getProjectRootNode(project);
        prn.callPopup().pushMenuNoBlock(pushAction);
    }
    
    static void runProject(String project) {
        actionOnProject(project,"Run Project");
        //MainWindowOperator.getDefault().waitStatusText("run");
    }
    
    static void debugProject(String project) {
        actionOnProject(project,"Debug Project");
        //MainWindowOperator.getDefault().waitStatusText("debug");
    }
    
    
    static void testProject(String project) {
        actionOnProject(project, "Test Project");
        //MainWindowOperator.getDefault().waitStatusText("test");
    }
    
    static void deployProject(String project) {
        actionOnProject(project, "Deploy Project");
        MainWindowOperator.getDefault().waitStatusText("Finished building "+project+" (run-deploy)");
    }
    
    static void verifyProject(String project) {
        actionOnProject(project, "Verify Project");
        MainWindowOperator.getDefault().waitStatusText("Finished building "+project+" (verify)");
    }
    
    
    static void killRunOnProject(String project) {
        killProcessOnProject(project, "run");
    }
    
    static void killDebugOnProject(String project) {
        killProcessOnProject(project, "debug");
    }
    
    private static void killProcessOnProject(String project, String process) {
        // prepare Runtime tab
        RuntimeTabOperator runtime = RuntimeTabOperator.invoke();
        
        // kill the execution
        Node node = new Node(runtime.getRootNode(), "Processes|"+project+ " (" + process + ")");
        node.select();
        node.performPopupAction("Terminate Process");
    }
    
    static EditorOperator openFile(String project, String path, String filename, boolean waitforeditor) {
        return openFile(new Node(new ProjectsTabOperator().getProjectRootNode(project), path + "|" + filename), filename, waitforeditor);
    }
    
    static EditorOperator openFile(Node fileNode, String fileName, boolean waitforeditor){
        new OpenAction().perform(fileNode);
        if (waitforeditor) {
            EditorOperator editorOperator = new EditorOperator(fileName);
            editorOperator.activateWindow();
            return editorOperator;
        } else
            return null;
    }
    
    static EditorOperator editFile(String project, String path, String filename) {
        Node filenode = new Node(new ProjectsTabOperator().getProjectRootNode(project), path + "|" + filename);
        new EditAction().perform(filenode);
        EditorOperator editorOperator = new EditorOperator(filename);
        editorOperator.activateWindow();
        return editorOperator;
    }
    
    static void closeFile(String filename, boolean save) {
        new EditorOperator(filename).close(save);
    }
    
    static void insertToFile(String filename, int line, String text, boolean save) {
        EditorOperator editorOperator = new EditorOperator(filename);
        editorOperator.activateWindow();
        editorOperator.setCaretPositionToLine(line);
        editorOperator.insert(text);
        if (save) editorOperator.save();
    }
    
    static void waitForPendingBackgroundTasks() {
        // wait maximum 5 minutes
        for (int i=0; i<5*60; i++) {
            if (org.netbeans.progress.module.Controller.getDefault().getModel().getSize()==0)
                return;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
            
        }
    }
    
}
