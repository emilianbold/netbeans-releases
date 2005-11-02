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

package org.netbeans.modules.versioning.system.cvss.ui.actions.diff;

import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.FileStatusCache;
import org.netbeans.modules.versioning.system.cvss.ExecutorGroup;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.openide.nodes.Node;

import java.io.File;
import org.openide.util.NbBundle;

/**
 * Show differencies between the current working copy and repository version we started from. 
 *  
 * @author Maros Sandor
 */
public class DiffAction extends AbstractSystemAction {
    
    private static final int enabledForStatus =
            FileInformation.STATUS_VERSIONED_CONFLICT |  
            FileInformation.STATUS_VERSIONED_MERGE | 
            FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY | 
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY;

    public DiffAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    protected String getBaseName() {
        return "CTL_MenuItem_Diff";  // NOI18N
    }

    protected boolean enable(Node[] nodes) {
        return CvsVersioningSystem.getInstance().getFileTableModel(Utils.getCurrentContext(nodes), enabledForStatus).getNodes().length > 0;
    }

    public void performCvsAction(Node[] nodes) {
        ExecutorGroup group = new ExecutorGroup(getRunningName());
        group.progress(NbBundle.getMessage(DiffAction.class, "BK1001"));
        Context context = getContext(nodes);
        DiffExecutor executor = new DiffExecutor(context, getContextDisplayName());
        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
        File [] files = context.getFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_REMOTE_CHANGE) == 0) {
                executor.showLocalDiff(group);
                return;
            }
        }
        executor.showRemoteDiff(group);
    }
    
    protected boolean asynchronous() {
        return false;
    }

}
