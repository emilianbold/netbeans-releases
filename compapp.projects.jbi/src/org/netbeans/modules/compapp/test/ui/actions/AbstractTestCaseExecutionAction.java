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

package org.netbeans.modules.compapp.test.ui.actions;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import javax.swing.SwingUtilities;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.netbeans.modules.compapp.test.ui.TestcaseNode;
import org.netbeans.modules.compapp.test.ui.wizards.NewTestcaseConstants;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.*;
import org.openide.execution.ExecutorTask;
import org.openide.nodes.Node;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.actions.NodeAction;

/**
 * An abstract action for test case execution.
 *
 * @author jqian
 */
public abstract class AbstractTestCaseExecutionAction extends NodeAction 
        implements NewTestcaseConstants {
    
    protected boolean enable(Node[] activatedNodes) {
         for (Node activatedNode : activatedNodes) {
            TestcaseCookie cookie = activatedNode.getCookie(TestcaseCookie.class);
            if (cookie != null) {
                TestcaseNode node = cookie.getTestcaseNode();
                if (node.isTestCaseRunning()) {
                    return false;
                }
            }
         }
        return true;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    protected void performAction(final Node[] activatedNodes) {
        TestcaseCookie tc = activatedNodes[0].getCookie(TestcaseCookie.class);
        if (tc == null) {
            return;
        }
        
        // REFACTOR ME
        /*tc.getTestcaseNode().showDiffTopComponentVisible();*/        
        JbiProject project = tc.getTestcaseNode().getProject();
        FileObject projectDir = project.getProjectDirectory();
        FileObject buildXMLFile = projectDir.getFileObject(project.getBuildXmlName());
        
        FileObject testDir = project.getTestDirectory();
        Enumeration testFolders = testDir.getFolders(true);
        String testCasesCSV = ""; // NOI18N
        while(testFolders.hasMoreElements()) {
            FileObject testFolder = (FileObject)testFolders.nextElement();
            String testFolderName = testFolder.getName();
            //accumulate everything except "results" folder and other well-known folders
            if(!testFolderName.equals("results") && 
                    !testFolderName.equalsIgnoreCase("cvs")) { // NOI18N
                testCasesCSV +=  testFolderName + ","; // NOI18N
            }
        }
        testCasesCSV = testCasesCSV.substring(0, testCasesCSV.length() - 1);
        String testCasesProperty = "testcases=" + testCasesCSV; // NOI18N
        
        String selectedTestCasesCSV = ""; // NOI18N
        for (Node activatedNode : activatedNodes) {
            TestcaseCookie cookie = activatedNode.getCookie(TestcaseCookie.class);
            TestcaseNode node = cookie.getTestcaseNode();
            String testFolderName = node.getTestCaseDir().getName();
            selectedTestCasesCSV +=  testFolderName + ","; // NOI18N
        }
        selectedTestCasesCSV = selectedTestCasesCSV.substring(0, selectedTestCasesCSV.length() - 1);
        String selectedTestCasesProperty = "testcases=" + selectedTestCasesCSV;
        
        try {
            String testDirPath = FileUtil.toFile(testDir).getAbsolutePath();
            String fileName = testDirPath + "/all-tests.properties"; // NOI18N
            BufferedWriter fw = new BufferedWriter(new FileWriter(fileName));
            fw.write(testCasesProperty);
            fw.close();
            
            fileName = testDirPath + "/selected-tests.properties"; // NOI18N
            fw = new BufferedWriter(new FileWriter(fileName));
            fw.write(selectedTestCasesProperty);
            fw.close();
            
            for (Node activatedNode : activatedNodes) {
                TestcaseCookie cookie = activatedNode.getCookie(TestcaseCookie.class);
                TestcaseNode node = cookie.getTestcaseNode();
                node.setTestCaseRunning(true);
            }
            
            String[] targetNames = { getAntTargetName() };
            ExecutorTask task = ActionUtils.runTarget(buildXMLFile, targetNames, null);
            
            task.addTaskListener(new TaskListener() {
                public void taskFinished(Task task) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {                            
                            for (Node activatedNode : activatedNodes) {
                                TestcaseCookie cookie = 
                                        activatedNode.getCookie(TestcaseCookie.class);
                                TestcaseNode node = cookie.getTestcaseNode();
                                node.setTestCaseRunning(false);
                                if (node.isDiffTopComponentVisible()) {
                                    node.refreshDiffTopComponent();
                                }
                            }
                        }
                    });
                }
            });
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
          
    /**
     * Gets the ant script target name.
     * 
     * @return ant target name
     */
    protected abstract String getAntTargetName();
}
