/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.channel.filesharing.ui.actions;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.SourceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
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
        return new Class[] { DataFolder.class, SourceCookie.class };
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
