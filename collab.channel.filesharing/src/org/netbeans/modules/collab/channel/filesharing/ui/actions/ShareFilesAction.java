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
package org.netbeans.modules.collab.channel.filesharing.ui.actions;

import com.sun.collablet.CollabException;

import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.netbeans.modules.collab.channel.filesharing.FilesharingCollablet;
import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.core.Debug;


/**
 *
 * @author
 */
public class ShareFilesAction extends CookieAction {
    /** Creates a new instance of ShareFilesAction */
    public ShareFilesAction() {
    }

    public String getName() {
        return NbBundle.getMessage(ShareFilesAction.class, "LBL_Action_ShareFiles");
    }

    protected String iconResource() {
        return "org/openide/resources/actions/empty.gif";
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ShareFilesAction.class);
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
        if (!super.enable(nodes)) {
            return false;
        }

        if (nodes.length == 0) {
            return false;
        }

        for (int i = 0; i < nodes.length; i++) {
            if (!isEnabledOnNode(nodes[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true iff the node represents a source file or package.
     * @param node the Node to query
     * @return true or false
     */
    private static boolean isEnabledOnNode(Node node) {
        return true;
    }

    protected void performAction(Node[] nodes) {
        Debug.out.println("ShareFilesAction, performAction"); //NoI18n		

        FilesharingContext context = FilesharingCollablet.getActivatedComponentContext();

        if ((context != null) && context.isValid()) {
            try {
                //clone nodes
                Node[] nue = new Node[nodes.length];

                for (int i = 0; i < nue.length; i++) {
                    nue[i] = nodes[i].cloneNode();
                }

                context.getFilesystemExplorer().getRootNode().createProjectNode(nue);
            } catch (CollabException ce) {
            }
        }
    }
}
