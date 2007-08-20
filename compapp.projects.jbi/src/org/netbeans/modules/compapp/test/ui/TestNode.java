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


package org.netbeans.modules.compapp.test.ui;

import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.netbeans.modules.compapp.test.ui.actions.AddTestcaseAction;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.netbeans.modules.compapp.test.ui.actions.TestCookie;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.compapp.test.ui.actions.TestResultsDeleteAction;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.loaders.DataFolder;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 * DOCUMENT ME!
 *
 * @author 
 * @version 
 */
public class TestNode extends FilterNode {
    private static final java.util.logging.Logger mLogger =
            java.util.logging.Logger.getLogger("org.netbeans.modules.compapp.projects.jbi.ui.TestNode"); // NOI18N
    
    private static Image JBI_TEST_BADGE = Utilities.loadImage(
            "org/netbeans/modules/compapp/test/ui/resources/testCases_badge.png", true); // NOI18N
    
    private JbiProject mProject;
    private FileObject mTestDir;
    private TestChildren mChildren;
    private FileChangeListener mFileChangeListener;
    private TestCookie mTestCookie;
    
    // whether the current test is being run (#84900)
    private boolean testRunning = false;
    
    /**
     * Creates a new TestNode object.
     * @param jpp DOCUMENT ME!
     * @param mProject DOCUMENT ME!
     */
    public TestNode(JbiProjectProperties jpp, JbiProject project) {
        super(getTestFolder(jpp, project).getNodeDelegate(),
                new TestChildren(project, getTestDir(jpp, project)));
        mProject = project;
        
        // set the model listener
        mFileChangeListener = new FileChangeAdapter() {
            private void update() {
                //log("ModView: Contents changed.");
                RequestProcessor.getDefault().post(
                        new Runnable() {
                    public void run() {
                        try {
                            updateChildren();
                        } catch (Exception e) {
                            // ignore on purpose
                            // This could happen during the whole test case deletion.
                        }
                    }
                }
                );
            }
            
            public void fileFolderCreated(FileEvent fe) {
                update();
            }
            
            public void fileDeleted(FileEvent fe) {
                update();
            }
            
            public void fileRenamed(FileRenameEvent fe) {
                update();
            }
        };
        
        mTestDir = getTestDir(jpp, project);
        mTestDir.addFileChangeListener(mFileChangeListener);
        
        mChildren = (TestChildren) getChildren();
        mTestCookie = new TestCookie(this);
    }
    
    private void updateChildren() {
        if (mChildren != null) {
            mChildren.addNotify();
        }
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(TestNode.class, "LBL_TestNode"); // NOI18N
    }
    
    // @overwrite
    public boolean canDestroy() {
        return false;
    }
     
    // @overwrite
    public boolean canRename() {
        return false;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Image getIcon(int type) {
        return computeIcon(false, type);
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Image getOpenedIcon(int type) {
        return computeIcon(true, type);
    }
    
    private Image computeIcon(boolean opened, int type) {
        DataFolder projectFolder = getProjectFolder(mProject);
        Node folderNode = projectFolder.getNodeDelegate();
        Image image = opened ? folderNode.getOpenedIcon(type) : folderNode.getIcon(type);

        return Utilities.mergeImages(image, JBI_TEST_BADGE, 7, 7);
    }
    
    // Create the popup menu:
    public Action[] getActions(boolean context) {
        List<Action> actionList = new ArrayList<Action>();
        actionList.add(SystemAction.get(AddTestcaseAction.class));
        actionList.add(null);
        actionList.add(SystemAction.get(TestResultsDeleteAction.class));
        
        return actionList.toArray(new Action[0]);
    }
    
    public Node.Cookie getCookie(Class type) {
        if (type == TestCookie.class) {
            return mTestCookie;
        }
        return super.getCookie(type);
    }
    
    public FileChangeListener getFileChangeListener() {
        return mFileChangeListener;
    }
    
    public JbiProject getProject() {
        return mProject;
    }
    
    public FileObject getTestDir() {
        return mTestDir;
    }
    
    public boolean isTestRunning() {
        return testRunning;
    }
    
    public void setTestRunning(boolean testRunning) {
        this.testRunning = testRunning;
    }
    
    private static DataFolder getProjectFolder(JbiProject project) {
        DataFolder projectFolder = null;
        FileObject projectDir = project.getProjectDirectory();
        
        if (projectDir.isFolder()) {
            projectFolder = DataFolder.findFolder(projectDir);
        }
        
        return projectFolder;
    }
    
    private static DataFolder getTestFolder(JbiProjectProperties jpp, JbiProject project) {
        DataFolder testFolder = null;
        FileObject testDir = getTestDir(jpp, project);
        if (testDir.isFolder()) {
            testFolder = DataFolder.findFolder(testDir);
        }
        return testFolder;
    }
    
    private static FileObject getTestDir(JbiProjectProperties jpp, JbiProject project) {
        FileObject testDir = null;
        FileObject projectDir = project.getProjectDirectory();
        Object t = jpp.get(JbiProjectProperties.TEST_DIR);
        testDir = projectDir.getFileObject((String)t);
        
        return testDir;
    }
}
