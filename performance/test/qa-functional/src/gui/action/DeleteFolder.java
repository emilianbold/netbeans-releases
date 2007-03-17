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

package gui.action;

import java.io.File;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.actions.MaximizeWindowAction;
import org.netbeans.jellytools.actions.RestoreWindowAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of Delete nodes/folders in the Explorer.
 *
 * @author  mmirilovic@netbeans.org
 */
public class DeleteFolder extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    /** Projects tab */
    private static ProjectsTabOperator projectTab;
    
    /** Name of the folder which test creates and expands */
    private static String folderToBeDeleted;
    
    /** Node represantation of the folder which test creates and delete */
    private static Node nodeToBeDeleted;
    
    /**
     * Creates a new instance of DeleteFolder
     * @param testName the name of the test
     */
    public DeleteFolder(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /**
     * Creates a new instance of DeleteFolder
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public DeleteFolder(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void testDeleteFolderWith50JavaFiles(){
        WAIT_AFTER_PREPARE = 10000;
        WAIT_AFTER_OPEN = 10000;
        folderToBeDeleted = "javaFolder50";
        doMeasurement();
    }
    
    public void testDeleteFolderWith100JavaFiles(){
        WAIT_AFTER_PREPARE = 15000;
        WAIT_AFTER_OPEN = 15000;
        folderToBeDeleted = "javaFolder100";
        doMeasurement();
    }
    
    public void initialize(){
        projectTab = new ProjectsTabOperator();
        new MaximizeWindowAction().performAPI(projectTab);
        
        projectTab.getProjectRootNode("PerformanceTestData").collapse();
        projectTab.getProjectRootNode("jEdit").collapse();
        
        repaintManager().setOnlyExplorer(true);
        turnBadgesOff();
    }
    
    public ComponentOperator open(){
        new DeleteAction().performPopup(nodeToBeDeleted);
        new NbDialogOperator(Bundle.getStringTrimmed("org.openide.explorer.Bundle","MSG_ConfirmDeleteObjectTitle")).yes();
        return null;
    }
    
    public void close(){
        //do nothing
    }
    
    public void prepare(){
        try {
            String projectPath = System.getProperty("xtest.tmpdir") + File.separator + "PerformanceTestFoldersData";
            log("========== Projects path =" + projectPath);
            
            File foldersDir = new File(projectPath + File.separator + "src" + File.separator + "folders");
            log("========== Folders path ="+foldersDir.getPath());
            
            
            File originalDir = new File(foldersDir, folderToBeDeleted);
            File copyDeleteDir = new File(foldersDir, folderToBeDeleted + "_delete");
            copyDeleteDir.mkdir();
            
            log("========== Original Dir path =" + originalDir.getPath());
            log("========== Copy Delete Dir path =" + copyDeleteDir.getPath());

            gui.Utilities.copyFile(new File (foldersDir, "Test.java"),new File (copyDeleteDir, "Test.java"));
            waitNoEvent(1000);
            
            SourcePackagesNode sourcePackagesNode = new SourcePackagesNode("PerformanceTestFoldersData");
            
            Node foldersNode = new Node(sourcePackagesNode, "|folders");
            foldersNode.expand();
            
            /* no more present after retouche merge
            new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenu(Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/File")+"|"+Bundle.getStringTrimmed("org.netbeans.modules.javacore.Bundle","LBL_RescanAction"),"|"); //File|Refresh All Files
            waitNoEvent(500);
             */

            nodeToBeDeleted = new Node(sourcePackagesNode, "folders." + folderToBeDeleted + "_delete");
            
            File[] files = originalDir.listFiles();
            log("=============== There is [" + files.length + "] number of files in directory = "+ originalDir.getPath());
            
            File copyFile;
            
            for(int i=0; i<files.length; i++) {
                copyFile = new File(copyDeleteDir,files[i].getName());
                log("================== Create file ="+copyFile.getPath());
                
                gui.Utilities.copyFile(files[i], copyFile);
            }
            
            /* no more present after retouche merge
            new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenu(Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/File")+"|"+Bundle.getStringTrimmed("org.netbeans.modules.javacore.Bundle","LBL_RescanAction"),"|"); //File|Refresh All Files
            waitNoEvent(500);
             */
            
            nodeToBeDeleted.expand();
            
        }catch(Exception exc){
            log("========================================= \nImpossible create files: "+exc.getMessage());
            exc.printStackTrace(getLog());
            log("\n=========================================");
        }
    }

    public void shutdown() {
        new RestoreWindowAction().performAPI(projectTab);
        repaintManager().setOnlyExplorer(false);
        turnBadgesOn();
    }
    
    /**
     * turn badges off
     */
    protected void turnBadgesOff() {
        System.setProperty("perf.dont.resolve.java.badges", "true");
    }

    /**
     * turn badges on
     */
    protected void turnBadgesOn() {
        System.setProperty("perf.dont.resolve.java.badges", "false");
    }
    
    
    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new DeleteFolder("testDeleteFolderWith50JavaFiles"));
    }
}
