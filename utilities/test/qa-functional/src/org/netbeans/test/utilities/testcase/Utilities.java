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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.test.utilities.testcase;

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
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileUtil;
import org.openide.actions.SaveAllAction;

/**
 * Utilities|Search helper class
 * @author Max Sauer
 */
public class Utilities {
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
    
    /**
     * Sleeps for waitTimeout miliseconds to avoid incorrect test failures.
     */
    public static void takeANap(long waitTimeout) {
        new org.netbeans.jemmy.EventTool().waitNoEvent(waitTimeout);
    }
}
