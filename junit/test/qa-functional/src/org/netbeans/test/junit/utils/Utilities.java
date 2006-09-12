/*
 * Utilities.java
 *
 * Created on August 23, 2006, 5:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.junit.utils;

import java.io.File;
import java.io.IOException;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.junit.NbTestCase;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileUtil;
import org.openide.actions.SaveAllAction;

/**
 *
 * @author ms159439
 */
public class Utilities {
    // name of sample project
    public static final String TEST_PROJECT_NAME = "JunitTestProject";
    
    // name of sample class
    public static final String TEST_CLASS_NAME = "TestClass";
    
    /** label when deleting object */
    public static final String CONFIRM_OBJECT_DELETION = 
            Bundle.getString("org.openide.explorer.Bundle", 
            "MSG_ConfirmDeleteObjectTitle");
   
    // default path to bundle file
    public static final String JUNIT_BUNDLE = "org.netbeans.modules.junit.Bundle";
    
    /** 'Test Packages' string from j2se project bundle */
    public static final String TEST_PACKAGES_PATH = 
            Bundle.getString("org.netbeans.modules.java.j2seproject.Bundle",
            "NAME_test.src.dir");
    
    /** 'Run File' action label from j2se project bundle */
    public static final String RUN_FILE = 
            Bundle.getString("org.netbeans.modules.java.j2seproject.Bundle",
            "ACTION_run.single");
            
    /** 'Source Packages' string from j2se project bundle */
    public static final String SRC_PACKAGES_PATH = 
            Bundle.getString("org.netbeans.modules.java.j2seproject.Bundle",
            "NAME_src.dir");
    
    // default timeout for actions in miliseconds
    public static final int ACTION_TIMEOUT = 1000;
    
    
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
     * Deletes a node (file, package)
     * using pop-up menu
     */ 
    public static void deleteNode(String path) {
        Node pn = new ProjectsTabOperator().getProjectRootNode(
                Utilities.TEST_PROJECT_NAME);
        pn.select();
        Node n = new Node(pn, path);
        n.select();
        JPopupMenuOperator jpmo = n.callPopup();
        jpmo.pushMenu("Delete");
        new NbDialogOperator(CONFIRM_OBJECT_DELETION).btYes().push(); //confirm
        takeANap(500);
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
    
    /**
     * Opens a file from TEST_PROJECT_NAME
     * @param Filename the file to be opened
     */
    public static Node openFile(String path) {
        Node pn = new ProjectsTabOperator().getProjectRootNode(
                Utilities.TEST_PROJECT_NAME);
        pn.select();
        Node n = new Node(pn, path);
        n.select();
        new OpenAction().perform();
        new EventTool().waitNoEvent(ACTION_TIMEOUT);
        return n;
    }
    
    /**
     * Pushes Tools|Create Junit tests over a node
     * @param n the node where the action will be invoked
     */ 
    public static void pushCreateTestsPopup(Node n) {
        JPopupMenuOperator jpmo = n.callPopup();
        String[] path = {"Tools", Bundle.getString(Utilities.JUNIT_BUNDLE,
                "LBL_Action_CreateTest")};
        jpmo.pushMenu(path);
    }
    
    /**
     * Sets all checkboxes inside Junit create tests dialog to checked
     */ 
    public static void checkAllCheckboxes(NbDialogOperator ndo) {
        for(int i = 0; i < 7; i++) {
            new JCheckBoxOperator(ndo, i).setSelected(true);
        }
    }
    
    /**
     * Sleeps for waitTimeout miliseconds to avoid incorrect test failures.
     */
    public static void takeANap(long waitTimeout) {
        new org.netbeans.jemmy.EventTool().waitNoEvent(waitTimeout);
    }
}
