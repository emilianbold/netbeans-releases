/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.collab.channel.filesharing.ui.actions;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

import java.io.IOException;

import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.ui.ProjectsRootNode;
import org.netbeans.modules.collab.core.Debug;


/**
 *
 */
public class NewFolderAction extends CookieAction {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////	
    public static final String NEW_FOLDER_ACTION = NbBundle.getMessage(
            NewFolderAction.class, "LBL_NewFolderAction_ActionName"
        ); //"Folder"		

    ////////////////////////////////////////////////////////////////////////////
    // Instant variables
    ////////////////////////////////////////////////////////////////////////////	
    FilesharingContext context = null;
    Node node = null;

    public NewFolderAction() {
    }

    public NewFolderAction(FilesharingContext context, Node node) {
        this.context = context;
        this.node = node;
    }

    /* public members */
    public String getName() {
        return NbBundle.getMessage(NewFolderAction.class, "LBL_NewFolderAction_ActionName"); //NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /* protected members */
    protected Class[] cookieClasses() {
        return new Class[] { DataFolder.class };
    }

    protected int mode() {
        return MODE_ANY; // allow creation of tests for multiple selected nodes (classes, packages)
    }

    public boolean asynchronous() {
        return true; // yes, this action should run asynchronously

        // would be better to rewrite it to synchronous (running in AWT thread),
        // just replanning test generation to RequestProcessor
    }

    /** Perform special enablement check in addition to the normal one.
     * protected boolean enable (Node[] nodes) {
     * if (! super.enable (nodes)) return false;
     * if (...) ...;
     * }
     */
    protected boolean enable(Node[] nodes) {
        return true;
    }

    /**
     * Returns true iff the node represents a source file or package.
     * @param node the Node to query
     * @return true or false
     */
    private static boolean isEnabledOnNode(Node node) {
        DataObject dob = (DataObject) node.getCookie(DataObject.class);
        FileObject fo = null;

        if (dob != null) {
            fo = dob.getPrimaryFile();
        }

        if (fo == null) {
            return false;
        }

        return true;
    }

    protected String iconResource() {
        return "org/netbeans/modules/collab/channel/filesharing/resources/" //NOI18N
         +"folder.gif"; //NOI18N
    }

    protected void performAction(Node[] nodes) {
        Debug.out.println(NEW_FOLDER_ACTION + ", actionPerformed"); //NoI18n		

        NotifyDescriptor descriptor = new NotifyDescriptor.InputLine(
                NbBundle.getMessage(NewFolderAction.class, "MSG_NewFolderAction_NewFolderName"),
                NbBundle.getMessage(NewFolderAction.class, "MSG_NewFolderAction_NewFolderTitle"),
                NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE
            );

        if (DialogDisplayer.getDefault().notify(descriptor) != NotifyDescriptor.OK_OPTION) {
            return;
        }

        final String folderName = ((NotifyDescriptor.InputLine) descriptor).getInputText();

        try {
            FileSystem fs = context.getCollabFilesystem();
            fs.runAtomicAction(
                new FileSystem.AtomicAction() {
                    public void run() throws IOException {
                        try {
                            Node projectNode = context.createProjectNode(
                                    context.getLoginUser(), ProjectsRootNode.SHARED_COMMON_DIR
                                );

                            if (projectNode != null) {
                                //expand node
                                context.getFilesystemExplorer().expandTreeNode(projectNode, "");

                                DataObject d = (DataObject) node.getCookie(DataObject.class);
                                FileObject fObj = null;

                                if (node instanceof ProjectsRootNode || node instanceof ProjectsRootNode.ProjectNode) {
                                    fObj = ((ProjectsRootNode.ProjectNode) projectNode).getProjectDir();
                                } else if ((d != null) && (d.getPrimaryFile() != null)) {
                                    fObj = d.getPrimaryFile();
                                }

                                //expand node
                                context.getFilesystemExplorer().expandTreeNode(node, "");

                                //create folder
                                if (fObj != null) {
                                    fObj.createFolder(folderName);
                                }
                            }
                        } catch (IOException iox) {
                            Debug.log("ProjectsRootNode", "ProjectsRootNode, ex: " + iox);
                            iox.printStackTrace(Debug.out);
                        }
                    }
                }
            );
        } catch (IOException iox) {
            iox.printStackTrace(Debug.out);
        }
    }
}
