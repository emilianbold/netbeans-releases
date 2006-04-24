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
import org.netbeans.modules.subversion.client.ExceptionHandler;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.commit.CommitAction;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.tigris.subversion.svnclientadapter.*;
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
        // FIXME PETR add non-recursive folders splitting
        // FIXME PETR add shalow logic allowing to ignore nested projects
        final Context ctx = getContext(nodes);
        final File[] roots = ctx.getRootFiles();
        final SVNUrl repositoryUrl = SvnUtils.getRepositoryRootUrl(roots[0]);

        ContextAction.ProgressSupport support = new ContextAction.ProgressSupport(this, nodes) {
            public void perform() {

                ISVNClientAdapter client;
                try {
                    client = Subversion.getInstance().getClient(repositoryUrl);
                } catch (SVNClientException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex); // should not hapen
                    return;
                }

                try {

                    // XXX PETR how to detect conflicts
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
                    ExceptionHandler eh = new ExceptionHandler (e1);
                    eh.annotate();
                }
        
            }
        };            
        support.start(createRequestProcessor(nodes));
    }

    public static void performUpdate(final Context context) {
        if (context == null || context.getRoots().size() == 0) {
            return;
        }        
        SVNUrl repository = getSvnUrl(context);
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repository);
        SvnProgressSupport support = new SvnProgressSupport() {
            public void perform() {                                                
                try {
                    SvnClient client = Subversion.getInstance().getClient(context, this);
                    File[] roots = context.getRootFiles();
                    for (int i=0; i<roots.length; i++) {
                        if(isCanceled()) {
                            return;
                        }
                        File root = roots[i];
                        client.update(root, SVNRevision.HEAD, true);
                    }
                } catch (SVNClientException ex) {
                    ExceptionHandler eh = new ExceptionHandler(ex);
                    eh.annotate();
                }
            }
        };
        support.start(rp, "Updating...");
    }

}
