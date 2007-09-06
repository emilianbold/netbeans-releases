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

package org.netbeans.modules.versioning.system.cvss.ui.actions.diff;

import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.FileStatusCache;
import org.netbeans.modules.versioning.system.cvss.ExecutorGroup;
import org.netbeans.modules.versioning.system.cvss.util.Context;
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
    
    public DiffAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    protected String getBaseName(Node [] activatedNodes) {
        return "CTL_MenuItem_Diff";  // NOI18N
    }

    protected int getFileEnabledStatus() {
        return getDirectoryEnabledStatus();
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED; 
    }

    public void performCvsAction(Node[] nodes) {
        ExecutorGroup group = new ExecutorGroup(getRunningName(nodes));
        group.progress(NbBundle.getMessage(DiffAction.class, "BK1001"));
        Context context = getContext(nodes);
        DiffExecutor executor = new DiffExecutor(context, getContextDisplayName(nodes));
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

    @Override
    protected String iconResource() {
        return "org/netbeans/modules/versioning/system/cvss/resources/icons/diff.png"; // NOI18N
    }
}
