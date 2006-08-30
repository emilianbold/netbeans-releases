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

package org.netbeans.modules.subversion.ui.status;

import java.io.File;
import org.netbeans.modules.subversion.util.*;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.FileStatusCache;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;

/**
 * Context sensitive status action. It opens the Subversion
 * view and sets its context.
 *
 * @author Petr Kuzel
 */
public class StatusAction  extends ContextAction {
    
    private static final int enabledForStatus = FileInformation.STATUS_MANAGED;  
    
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_ShowChanges"; // NOI18N
    }

    protected int getFileEnabledStatus() {
        return enabledForStatus;
    }

    public void performContextAction(Node[] nodes) {
        Context ctx = SvnUtils.getCurrentContext(nodes);
        final SvnVersioningTopComponent stc = SvnVersioningTopComponent.getInstance();
        stc.setContentTitle(getContextDisplayName(nodes));
        stc.setContext(ctx);
        stc.open(); 
        stc.requestActive();
        stc.performRefreshAction();
    }

    /**
     * Connects to repository and gets recent status.
     */
    public static void executeStatus(final Context context, SvnProgressSupport support) {

        if (context == null || context.getRoots().size() == 0) {
            return;
        }
            
        try {
            SvnClient client;            
            try {
                client = Subversion.getInstance().getClient(context, support);
            } catch (SVNClientException ex) {
                ErrorManager.getDefault().notify(ex);
                return;
            }

            Subversion.getInstance().getStatusCache().refreshCached(context);
            File[] roots = context.getRootFiles();
            for (int i=0; i<roots.length; i++) {
                if(support.isCanceled()) {
                    return;
                }
                File root = roots[i];
                ISVNStatus[] statuses = client.getStatus(root, true, false, true);  // cache refires events
                if(support.isCanceled()) {
                    return;
                }

                for (int s = 0; s < statuses.length; s++) {
                    if(support.isCanceled()) {
                        return;
                    }
                    ISVNStatus status = statuses[s];
                    FileStatusCache cache = Subversion.getInstance().getStatusCache();
                    File file = status.getFile();
                    SVNStatusKind kind = status.getRepositoryTextStatus();
//                    System.err.println(" File: " + file.getAbsolutePath() + " repo-status:" + kind );
                    cache.refresh(file, status);
                }
            }
        } catch (SVNClientException ex) {
            support.annotate(ex);
        }
    }
}