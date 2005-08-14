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
import org.netbeans.modules.versioning.system.cvss.CvsFileNode;
import org.netbeans.modules.versioning.system.cvss.settings.CvsModuleConfig;
import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.openide.util.NbBundle;

import java.io.File;
import java.awt.event.ActionEvent;
import java.util.Set;
import java.util.HashSet;

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
        return "CTL_MenuItem_Diff";
    }

    /**
     * Diff action should disabled only if current selection contains only files and these
     * files are not all changed locally/remotely. This scenario is not supported by {@link AbstractSystemAction}
     * so this method is overriden to return custom set of files to process.
     *
     * @return File[] all changed files in the current context
     */
    protected File [] getFilesToProcess() {
        CvsModuleConfig config = CvsModuleConfig.getDefault();
        CvsFileNode [] nodes = CvsVersioningSystem.getInstance().getFileTableModel(
                super.getFilesToProcess(), enabledForStatus).getNodes();
        Set modifiedFiles = new HashSet();
        for (int i = 0; i < nodes.length; i++) {
            File file = nodes[i].getFile();
            if (!config.isExcludedFromCommit(file.getAbsolutePath())) {
                modifiedFiles.add(file);
            }
        }
        return (File[]) modifiedFiles.toArray(new File[modifiedFiles.size()]);
    }

    public void actionPerformed(ActionEvent ev) {
        File [] files = getFilesToProcess();
        String title = NbBundle.getMessage(DiffAction.class, "CTL_DiffDialogLocal_Title", getContextDisplayName());
        DiffExecutor executor = new DiffExecutor(files, title);
        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_REMOTE_CHANGE) == 0) {
                executor.showLocalDiff();
                return;
            }
        }
        executor.showRemoteDiff();
    }
}
