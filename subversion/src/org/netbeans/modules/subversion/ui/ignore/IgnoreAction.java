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

package org.netbeans.modules.subversion.ui.ignore;

import java.util.*;
import org.netbeans.modules.subversion.*;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.ui.actions.*;
import org.netbeans.modules.subversion.util.*;
import org.openide.*;
import org.openide.nodes.Node;

import java.io.File;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.tigris.subversion.svnclientadapter.*;

/**
 * Adds/removes files to svn:ignore property.
 * It does not support patterns.
 * 
 * @author Maros Sandor
 */
public class IgnoreAction extends ContextAction {
    
    public static final int UNDEFINED  = 0;
    public static final int IGNORING   = 1;
    public static final int UNIGNORING = 2;
    
    protected String getBaseName(Node [] activatedNodes) {
        int actionStatus = getActionStatus(activatedNodes);
        switch (actionStatus) {
        case UNDEFINED:
        case IGNORING:
            return "CTL_MenuItem_Ignore";  // NOI18N
        case UNIGNORING:
            return "CTL_MenuItem_Unignore"; // NOI18N
        default:
            throw new RuntimeException("Invalid action status: " + actionStatus); // NOI18N
        }
    }

    public int getActionStatus(Node [] nodes) {
        return getActionStatus(SvnUtils.getCurrentContext(nodes).getFiles());
    }

    public int getActionStatus(File [] files) {
        int actionStatus = -1;
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().equals(".svn")) { // NOI18N
                actionStatus = UNDEFINED;
                break;
            }
            FileInformation info = cache.getStatus(files[i]);
            if (info.getStatus() == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) {
                actionStatus = (actionStatus == -1 || actionStatus == IGNORING) ?
                        IGNORING : UNDEFINED;
            } else if (info.getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
                actionStatus = ((actionStatus == -1 || actionStatus == UNIGNORING)
                        && canBeUnignored(files[i])) ?
                        UNIGNORING : UNDEFINED;
            } else {
                actionStatus = UNDEFINED;
                break;
            }
        }
        return actionStatus == -1 ? UNDEFINED : actionStatus;
    }
    
    private boolean canBeUnignored(File file) {
        File parent = file.getParentFile();
        try {
            List patterns = Subversion.getInstance().getClient().getIgnoredPatterns(parent);
            return patterns.contains(file.getName());
        } catch (SVNClientException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return false;
        }
    }

    protected boolean enable(Node[] nodes) {
        return getActionStatus(nodes) != UNDEFINED;
    }

    public void performContextAction(final Node[] nodes) {

        final File files[] = SvnUtils.getCurrentContext(nodes).getFiles();
        final int actionStatus = getActionStatus(nodes);
        ContextAction.ProgressSupport support = new ContextAction.ProgressSupport(this, createRequestProcessor(nodes), nodes) {
            public void perform() {

                SvnClient client = Subversion.getInstance().getClient();               
                for (int i = 0; i<files.length; i++) {
                    if(isCanceled()) {
                        return;
                    }
                    File file = files[i];
                    File parent = file.getParentFile();
                    if (actionStatus == IGNORING) {
                        try {
                            client.addToIgnoredPatterns(parent, file.getName());
                            // XXX for sure ignored
                            Subversion.getInstance().getStatusCache().refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                        } catch (SVNClientException ex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        }
                    } else if (actionStatus == UNIGNORING) {
                        try {
                            List patterns = Subversion.getInstance().getClient().getIgnoredPatterns(parent);
                            patterns.remove(file.getName());
                            client.setIgnoredPatterns(parent, patterns);
                            Subversion.getInstance().getStatusCache().refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                        } catch (SVNClientException ex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        }
                    } else {
                        throw new RuntimeException("Invalid action status: " + actionStatus); // NOI18N
                    }
                }
            }
        };            
        support.start();
    }

    protected boolean asynchronous() {
        return false;
    }

}
