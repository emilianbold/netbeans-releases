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
import org.netbeans.modules.compapp.test.ui.actions.TestCaseOutputCookie;
import org.netbeans.modules.compapp.test.ui.actions.TestCaseSaveRecentResultAsOutputAction;
import org.netbeans.modules.compapp.test.ui.actions.TestCookie;
import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.openide.actions.EditAction;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.RequestProcessor;


/**
 * DOCUMENT ME!
 *
 * @author Jun Qian
 */
public class TestCaseOutputNode extends FilterNode {
    private static final java.util.logging.Logger mLogger =
            java.util.logging.Logger.getLogger("org.netbeans.modules.compapp.projects.jbi.ui.TestCaseOutputNode"); // NOI18N
    
    private static final Image OUTPUT_ICON = Utilities.loadImage(
            "org/netbeans/modules/compapp/test/ui/resources/output.png", true); // NOI18N
    
    private static final Image WARNING_BADGE = Utilities.loadImage(
            "org/netbeans/modules/compapp/test/ui/resources/warningBadge.gif", true); // NOI18N
    
    private JbiProject mProject;    // FIXME probably not needed
    private FileObject mOutputFile;
    private FileChangeListener mFileChangeListener;
    
    private TestCaseOutputCookie mTestCaseOutputCookie;
    
    /**
     * Creates a new TestInputNode object.
     *
     * @param jpp DOCUMENT ME!
     * @param mProject DOCUMENT ME!
     */
    public TestCaseOutputNode(JbiProject project, DataObject outputDataObject) {
        super(outputDataObject.getNodeDelegate(), Children.LEAF);
        mProject = project;
        mOutputFile = outputDataObject.getPrimaryFile();
        
        // set the model listener
        mFileChangeListener = new FileChangeAdapter() {
            public void fileChanged(FileEvent fe) {
                fireIconChange();
            }
        };
        
        mOutputFile.addFileChangeListener(mFileChangeListener);
        
        mTestCaseOutputCookie = new TestCaseOutputCookie(this);
    }
        
    public String getDisplayName() {
        return NbBundle.getMessage(TestCaseInputNode.class, "LBL_TestOutputNode"); // NOI18N
    }
    
    public boolean canCut() {
        return false;
    }
    
//    public boolean canCopy() {
//        return false;
//    }
    
    public boolean canDestroy() {
        return false;
    }
    
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
    
    private Image computeIcon(boolean opened, int type) {
        Image image = OUTPUT_ICON;
        File file = FileUtil.toFile(mOutputFile);
        if (file.length() == 0) {
            return Utilities.mergeImages(image, WARNING_BADGE, 15, 8); //7, 5);
        } else {
            return image;
        }
    }
    
    // Create the popup menu:
    public Action[] getActions(boolean context) {
        return new Action[] { 
            SystemAction.get(EditAction.class),
            SystemAction.get(TestCaseSaveRecentResultAsOutputAction.class)
        };
    }
    
    public Node.Cookie getCookie(Class type) {
        if (type == TestCaseOutputCookie.class) {
            return mTestCaseOutputCookie;
        }
        return super.getCookie(type);
    }
}
