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
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectConstants;
import org.netbeans.modules.compapp.test.ui.TestcaseNode;
import org.netbeans.modules.compapp.test.ui.wizards.NewTestcaseConstants;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.openide.*;
import org.openide.execution.ExecutorTask;
import org.openide.nodes.Node;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.actions.NodeAction;

/**
 * DOCUMENT ME!
 *
 * @author jqian
 */
public class TestCaseDebugAction extends NodeAction implements NewTestcaseConstants {
    private static final java.util.logging.Logger mLog =
            java.util.logging.Logger.getLogger("org.netbeans.modules.compapp.test.ui.actions.TestCaseDebugAction"); // NOI18N
    
    /**
     * DOCUMENT ME!
     *
     * @param activatedNodes DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected boolean enable(Node[] activatedNodes) {
        return (activatedNodes.length == 1) &&
                (activatedNodes[0].getCookie(TestcaseCookie.class) != null);
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected boolean asynchronous() {
        return false;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param activatedNodes DOCUMENT ME!
     */
    protected void performAction(final Node[] activatedNodes) {
        TestcaseCookie tc = ((TestcaseCookie) activatedNodes[0].getCookie(TestcaseCookie.class));
        if (tc == null) {
            return;
        }
        /*tc.getTestcaseNode().showDiffTopComponentVisible();*/
        String targetNames[] = {JbiProjectConstants.COMMAND_DEBUG_SINGLE};
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
            if(!testFolderName.equals("results") && !testFolderName.equalsIgnoreCase("cvs")) { // NOI18N
                testCasesCSV +=  testFolderName + ","; // NOI18N
            }
        }
        testCasesCSV = testCasesCSV.substring(0, testCasesCSV.length() - 1);
        String testCasesProperty = "testcases=" + testCasesCSV; // NOI18N
        
        final int activatedNodeCount = activatedNodes.length;
        String selectedTestCasesCSV = ""; // NOI18N
        for(int nodeIndex = 0; nodeIndex < activatedNodeCount; nodeIndex++) {
            TestcaseCookie cookie = (TestcaseCookie)activatedNodes[nodeIndex].getCookie(TestcaseCookie.class);
            TestcaseNode node = cookie.getTestcaseNode();
            String testFolderName = node.getTestCaseDir().getName();
            selectedTestCasesCSV +=  testFolderName + ","; // NOI18N
        }
        selectedTestCasesCSV = selectedTestCasesCSV.substring(0, selectedTestCasesCSV.length() - 1);
        String selectedTestCasesProperty = "testcases=" + selectedTestCasesCSV;
        
        //Properties props = new Properties();
        //props.put("testcases", "NewTestcase0");
        //System.setProperty("testcases", "NewTestcase0");
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
            
            ExecutorTask task = ActionUtils.runTarget(buildXMLFile, targetNames, null);
            
            task.addTaskListener(new TaskListener() {
                public void taskFinished(Task task) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            for (int nodeIndex = 0; nodeIndex < activatedNodeCount; nodeIndex++) {
                                TestcaseCookie cookie = (TestcaseCookie)activatedNodes[nodeIndex].getCookie(TestcaseCookie.class);
                                TestcaseNode node = cookie.getTestcaseNode();
                                if (node.isDiffTopComponentVisible()) {
                                    node.refreshDiffTopComponent();
                                }
                            }
                        }
                    });
                }
            });
            
            //fileName = testDir.getPath() + "/selected-tests.properties";
            //fw = new BufferedWriter(new FileWriter(fileName));
            //fw.write(testCasesProperty);
            //fw.close();
            
            
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getName() {
        return NbBundle.getMessage(TestCaseDebugAction.class, "LBL_TestCaseDebugAction_Name");  // NOI18N
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        
        // If you will provide context help then use:
        // return new HelpCtx(AddTestcaseAction.class);
    }
    
    
}
