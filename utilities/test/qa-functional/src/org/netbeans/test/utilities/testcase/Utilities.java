/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.test.utilities.testcase;

import java.io.File;
import java.io.IOException;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileUtil;
import org.openide.actions.SaveAllAction;

/**
 * Utilities|Search helper class
 * @author Max Sauer
 */
public class Utilities{
    
    /** Find Dialog label */
    public  static final String FIND_DIALOG = Bundle.getString(
            "org.netbeans.modules.search.Bundle", "LBL_FindInProjects");
    
    /** Find Dialog label */
    public  static final String FIND_DIALOG_REPLACE = Bundle.getString(
            "org.netbeans.modules.search.Bundle", "LBL_ReplaceInProjects");
    
    
    /** name of sample project */
    public static final String TEST_PROJECT_NAME = "UtilitiesTestProject";
    
    /** label when deleting object */
    public static final String CONFIRM_OBJECT_DELETION =
            Bundle.getString("org.openide.explorer.Bundle",
            "MSG_ConfirmDeleteObjectTitle");
    
    /** default path to bundle file */
    public static final String UTILITIES_BUNDLE = "org.netbeans.modules.search.Bundle";
    
    /** 'Test Packages' string from j2se project bundle */
    public static final String TEST_PACKAGES_PATH =
            Bundle.getString("org.netbeans.modules.java.j2seproject.Bundle",
            "NAME_test.src.dir");
    
    /** 'Run File' action label from j2se project bundle */
    public static final String RUN_FILE =
            Bundle.getString("org.netbeans.modules.java.j2seproject.Bundle",
            "ACTION_run.single");
    
    /** Test project label (j2se project context menu) */
    public static final String TEST_PROJECT =
            Bundle.getString("org.netbeans.modules.java.j2seproject.ui.Bundle",
            "LBL_TestAction_Name");
    
    /** 'Source Packages' string from j2se project bundle */
    public static final String SRC_PACKAGES_PATH =
            Bundle.getString("org.netbeans.modules.java.j2seproject.Bundle",
            "NAME_src.dir");
    
    // default timeout for actions in miliseconds
    public static final int ACTION_TIMEOUT = 1000;
    
    //set NB as main window
    public static MainWindowOperator mwo = MainWindowOperator.getDefault();
    
    // Test source code
    public static EditorOperator editor;
    public static EditorWindowOperator ewo;
    public static Node formnode;
   
    /**
     * Saves all opened files
     */
    public static void saveAll() {
        ((SaveAllAction) SaveAllAction.findObject(SaveAllAction.class, true)).performAction();
    }
    
    /**
     * Deletes a file
     * @param the file to be deleted
     */
    public static void delete(File file) {
        try {
            DataObject.find(FileUtil.toFileObject(file)).delete();
        } catch (IOException e) {
        }
    }
    
    /**
     * Deletes a node (file, package, project)
     * using pop-up menu
     */
    public static void deleteNode(String path) {
        Node pn = new ProjectsTabOperator().getProjectRootNode(
                Utilities.TEST_PROJECT_NAME);
        if(pn != null && pn.isPresent()) {
            pn.select();
            Node n = new Node(pn, path);
            n.select();
            JPopupMenuOperator jpmo = n.callPopup();
            jpmo.pushMenu("Delete");
            new NbDialogOperator(CONFIRM_OBJECT_DELETION).btYes().push(); //confirm
            takeANap(500);
        }
    }
    
    /**
     * Recursively deletes a directory
     */
    public static void deleteDirectory(File path) {
        if(path.exists()) {
            File[] f = path.listFiles();
            for(int i = 0; i < f.length ; i++) {
                if (f[i].isDirectory())
                    deleteDirectory(f[i]);
                else
                    f[i].delete();
            }
        }
        path.delete();
    }
     /** delete projects */
    public static boolean deleteProject(String projectName, String path) throws InterruptedException {
        //Project Deleting
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(projectName);
        prn.select();
        
        DeleteAction delProject = new DeleteAction();
        delProject.perform();
        
        NbDialogOperator ndo = new NbDialogOperator("Delete Project");
        JCheckBoxOperator cbo = new JCheckBoxOperator(ndo);
        cbo.changeSelection(true);
        ndo.yes();
        
        Thread.sleep(10000);
        //check if project was really deleted from disc
        File f = new File(path + projectName);
        System.out.println("adresar:"+f);
        if (f.exists()) {           
            return false;
        } else {            
            return true;
        }
    }
    
    /**
     * Opens a file from TEST_PROJECT_NAME
     * @param Filename the file to be opened
     */
    public static Node openFile(String path, String projectName) {
        Node pn = new ProjectsTabOperator().getProjectRootNode(
                projectName);
        pn.select();        
        Node n = new Node(pn, path);
        n.select();
        new OpenAction().perform();
        new EventTool().waitNoEvent(ACTION_TIMEOUT);
        return n;
    }
    
    /**
     * Test whole project (presses 'Test Project from explorer's context menu
     */
    public static void testWholeProject() {
        Node n = new ProjectsTabOperator().getProjectRootNode(
                Utilities.TEST_PROJECT_NAME);
        n.callPopup().pushMenu(TEST_PROJECT);
    }
    
    /**
     * Pushes Tools|Create Junit tests over a node
     * @param n the node where the action will be invoked
     */
    public static void pushFindPopup(Node n) {
        JPopupMenuOperator jpmo = n.callPopup();
        jpmo.pushMenuNoBlock(Bundle.getString(UTILITIES_BUNDLE,
                "TEXT_TITLE_CUSTOMIZE"));
    }
    
    /**
     * Sets all checkboxes inside Junit create tests dialog to checked
     */
    public static void checkAllCheckboxes(NbDialogOperator ndo) {
        for(int i = 0; i < 7; i++) {
            new JCheckBoxOperator(ndo, i).setSelected(true);
        }
    }
    /** Select wanted Project node in ProjectTab */
    public static NbDialogOperator getFindDialog(String projectName) {
        
        Node pn = new ProjectsTabOperator().getProjectRootNode(projectName);
        pn.select();
        Utilities.takeANap(1000);
        Utilities.pushFindPopup(pn);
        return new NbDialogOperator(FIND_DIALOG);
    }
    
    /**
     * Invoke Find in Project Dialog from main menu
     */
    public static NbDialogOperator getFindDialogMainMenu() {
        mwo.menuBar().pushMenuNoBlock("Edit|" + FIND_DIALOG + "...");
         return new NbDialogOperator(FIND_DIALOG);
    }
    
    public static NbDialogOperator getFindAndReplaceMainMenu() {
        mwo.menuBar().pushMenuNoBlock("Edit|" + FIND_DIALOG_REPLACE + "...");
        return new NbDialogOperator(FIND_DIALOG_REPLACE);
    }
    
//    public static NbDialogOperator getFindDialog() {
//        Node pn = new ProjectsTabOperator().getProjectRootNode(
//                Utilities.TEST_PROJECT_NAME);
//        pn.select();
//        Node n = new Node(pn, Utilities.SRC_PACKAGES_PATH);
//        n.select();
//        Utilities.takeANap(1000);
//        Utilities.pushFindPopup(n);
//        
//        return new NbDialogOperator(FIND_DIALOG);
//    }
    
    /** 
     * This method create new Java Application project
     */
    
    public static String createNewProject(String projectName, String dataProjectName, String workdirpath){
        NewProjectWizardOperator nfwo = NewProjectWizardOperator.invoke();
        nfwo.selectCategory(projectName);
        nfwo.selectProject("Java Application");
        nfwo.next();
        
        JTextFieldOperator tfo_name = new JTextFieldOperator(nfwo, 0);
        tfo_name.clearText();
        tfo_name.typeText(dataProjectName);
        
        JTextFieldOperator tfo1_location = new JTextFieldOperator(nfwo, 1);
        
        tfo1_location.clearText();
        tfo1_location.typeText(workdirpath);
        
        JButtonOperator bo = new JButtonOperator(nfwo, "Finish");
        //bo.getSource().requestFocus();
        bo.push();
        return dataProjectName;
    }
    
    /**
     * Method which select appropriate radio buttion in scope section
    */
    public static void scopeSelection(NbDialogOperator name, String selection){
        JRadioButtonOperator rbo = new JRadioButtonOperator(name , selection);
        rbo.setSelected(true);
    }   
    /**
     * Sleeps for waitTimeout miliseconds to avoid incorrect test failures.
     */
    public static void takeANap(long waitTimeout) {
        new org.netbeans.jemmy.EventTool().waitNoEvent(waitTimeout);
    }
    
    /**
     * Method which scan java code in editor
     */
    public static boolean checkEditor(String regexp) {       
    
        editor = ewo.getEditor();
        takeANap(100);
        String editortext = editor.getText();
        
        java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(regexp,",");
        int pos = -1;
        boolean result = true;
        while(tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            pos = editortext.indexOf(token,pos);
            if (pos == -1) {
                result = false;
                break;
            }
            pos += token.length();
        }
        System.out.println("Result: " + result);
        return result;
    }
}
