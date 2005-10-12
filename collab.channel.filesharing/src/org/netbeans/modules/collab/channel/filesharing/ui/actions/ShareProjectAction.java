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

import com.sun.collablet.CollabException;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.netbeans.modules.collab.channel.filesharing.FilesharingCollablet;
import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.api.project.Project;

/**
 * An action sensitive to Project cookie allowing to share the selected project
 * over the last active conversation.
 *
 * @author
 */
public class ShareProjectAction extends CookieAction {
    /** Creates a new instance of ShareFilesAction */
    public ShareProjectAction() {
    }

    public String getName() {
        return NbBundle.getMessage(ShareFilesAction.class, "LBL_Action_ShareProject");
    }

    protected String iconResource() {
        return "org/openide/resources/actions/empty.gif";
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ShareProjectAction.class);
    }

    /* protected members */
    protected Class[] cookieClasses() {
        return new Class[] { Project.class };
    }

    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    public boolean asynchronous() {
        return false; // until I figure out why it doesn't work out of AWT
    }

    protected boolean enable(Node[] nodes) {
        return super.enable(nodes) && hasValidContext();
    }

    private static boolean hasValidContext() {
        FilesharingContext context = FilesharingCollablet.getActivatedComponentContext();
        return (context != null) && context.isValid();
    }
    
    protected void performAction(Node[] nodes) {
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
                ErrorManager.getDefault().notify(ce);
            }
        }
    }
}
