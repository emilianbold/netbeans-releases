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
import org.netbeans.modules.compapp.test.ui.actions.TestCookie;
import java.awt.Image;
import java.util.ArrayList;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.openide.actions.EditAction;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 * DOCUMENT ME!
 *
 * @author Jun Qian
 */
public class TestCaseInputNode extends FilterNode {
    private static final java.util.logging.Logger mLogger =
            java.util.logging.Logger.getLogger("org.netbeans.modules.compapp.projects.jbi.ui.TestCaseInputNode"); // NOI18N
        
    private static final Image INPUT_ICON = Utilities.loadImage(
            "org/netbeans/modules/compapp/test/ui/resources/input.png"); // NOI18N
    
    private JbiProject mProject;    
    
    /**
     * Creates a new TestCaseInputNode object.
     *
     * @param jpp DOCUMENT ME!
     * @param mProject DOCUMENT ME!
     */
    public TestCaseInputNode(JbiProject project, DataObject inputDataObject) {
        super(inputDataObject.getNodeDelegate(), Children.LEAF);
        mProject = project;        
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(TestCaseInputNode.class, "LBL_TestInputNode"); // NOI18N
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
        return INPUT_ICON;
    }    
    
    // Create the popup menu:
    public Action[] getActions(boolean context) {
        return new Action[] { SystemAction.get(EditAction.class),
//        SystemAction.get(org.openide.actions.OpenAction.class),
//        SystemAction.get(org.openide.actions.ViewAction.class)
        };
    }
    
//    public Node.Cookie getCookie(Class type) {
////        if (type == TestCookie.class) {
////            return mTestCookie;
////        }
//        if (type == OpenCookie.class) {
//            return new OpenCookie() {
//                public void open() {
//                    System.out.println("opening ...");
//                }
//            };
//        }
//        return super.getCookie(type);
//    }
    
//    public JbiProject getProject() {
//        return mProject;
//    }
}
