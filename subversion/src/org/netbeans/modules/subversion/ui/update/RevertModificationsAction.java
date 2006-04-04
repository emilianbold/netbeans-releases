/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion.ui.update;

import java.io.File;
import org.netbeans.modules.subversion.*;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.ui.actions.PlaceholderAction;
import org.netbeans.modules.subversion.util.Context;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 * Reverts local changes.
 *
 * @author Petr Kuzel
 */
public class RevertModificationsAction extends ContextAction {
    
    /** Creates a new instance of RevertModificationsAction */
    public RevertModificationsAction() {        
    }

    protected String getBaseName(Node[] activatedNodes) {
        return "CTL_MenuItem_Revert"; // NOI18N
    }

    protected void performContextAction(final Node[] nodes) {
        final Context ctx = getContext(nodes);
        Runnable run = new Runnable() {
            public void run() {
                Object pair = startProgress(nodes);
                try {
                    performRevert(ctx);
                } finally {
                    finished(pair);
                }
            }
        };
        Subversion.getInstance().postRequest(run);
    }

    /** Recursive revert */
    public static void performRevert(final Context ctx) {
        SvnClient client;
        try {
            client = Subversion.getInstance().getClient(ctx);
        } catch (SVNClientException ex) {
            ErrorManager.getDefault().notify(ex);
            return;
        }

        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        File files[] = ctx.getFiles();
        for (int i= 0; i<files.length; i++) {
            try {
                client.revert(files[i], true);
            } catch (SVNClientException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }
}
