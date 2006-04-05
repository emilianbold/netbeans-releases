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

import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.*;
import java.io.File;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.nodes.Node;
import org.tigris.subversion.svnclientadapter.*;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.util.RequestProcessor;

/**
 * Update action
 *
 * @author Petr Kuzel
 */ 
public class UpdateAction extends ContextAction {

    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_Update";    // NOI18N
    }

    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_IN_REPOSITORY;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED 
             & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED 
             & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }
    
    protected void performContextAction(Node[] nodes) {        
        performUpdate(nodes);
    }

    public void performUpdate(final Node[] nodes) {
        // FIXME add non-recursive folders splitting
        // FIXME add shalow logic allowing to ignore nested projects
        final Context ctx = getContext(nodes);
        final File[] roots = ctx.getRootFiles();
        final SVNUrl repositoryUrl = SvnUtils.getRepositoryRootUrl(roots[0]);

        ContextAction.ProgressSupport support = new ContextAction.ProgressSupport(this, createRequestProcessor(nodes), nodes) {
            public void perform() {

                ISVNClientAdapter client;
                try {
                    client = Subversion.getInstance().getClient(repositoryUrl);
                } catch (SVNClientException ex) {
                    ex.printStackTrace(); // should not hapen
                    return;
                }

                try {

                    // XXX how to detect conflicts
                    boolean conflict = false;

roots_loop:
                    for (int i = 0; i<roots.length; i++) {
                        if(isCanceled()) {
                            return;
                        }
                        client.update(roots[i], SVNRevision.HEAD, true);
                        ISVNStatus status[] = client.getStatus(roots[i], true, false);
                        for (int k = 0; k<status.length; k++) {
                            ISVNStatus s = status[k];
                            if (SVNStatusUtils.isTextConflicted(s) || SVNStatusUtils.isPropConflicted(s)) {
                                conflict = true;
                                break roots_loop;
                            }
                        }
                    }

                    if (conflict) {
                        StatusDisplayer.getDefault().setStatusText("Subversion update caused conflicts!");
                    } else {
                        StatusDisplayer.getDefault().setStatusText("Subversion update completed");
                    }
                } catch (SVNClientException e1) {
                    ErrorManager err = ErrorManager.getDefault();
                    err.annotate(e1, "Can not update");
                    err.notify(e1);
                }
        
            }
        };            
        support.start();
    }

}
