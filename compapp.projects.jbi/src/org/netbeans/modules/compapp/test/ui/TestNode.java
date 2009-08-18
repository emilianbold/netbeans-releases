/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
import org.openide.util.ImageUtilities;
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
    
    private static Image JBI_TEST_BADGE = ImageUtilities.loadImage(
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
            
            @Override
            public void fileFolderCreated(FileEvent fe) {
                update();
            }
            
            @Override
            public void fileDeleted(FileEvent fe) {
                update();
            }
            
            @Override
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
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(TestNode.class, "LBL_TestNode"); // NOI18N
    }
    
    // @overwrite
    @Override
    public boolean canDestroy() {
        return false;
    }
     
    // @overwrite
    @Override
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
    @Override
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
    @Override
    public Image getOpenedIcon(int type) {
        return computeIcon(true, type);
    }
    
    private Image computeIcon(boolean opened, int type) {
        DataFolder projectFolder = getProjectFolder(mProject);
        Node folderNode = projectFolder.getNodeDelegate();
        Image image = opened ? folderNode.getOpenedIcon(type) : folderNode.getIcon(type);

        return ImageUtilities.mergeImages(image, JBI_TEST_BADGE, 7, 7);
    }
    
    // Create the popup menu:
    @Override
    public Action[] getActions(boolean context) {
        List<Action> actionList = new ArrayList<Action>();
        actionList.add(SystemAction.get(AddTestcaseAction.class));
        actionList.add(null);
        actionList.add(SystemAction.get(TestResultsDeleteAction.class));
        
        return actionList.toArray(new Action[0]);
    }
    
    @Override
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
