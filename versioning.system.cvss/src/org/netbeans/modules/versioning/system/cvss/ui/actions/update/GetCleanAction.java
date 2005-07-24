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

package org.netbeans.modules.versioning.system.cvss.ui.actions.update;

import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.netbeans.modules.versioning.system.cvss.*;
import org.netbeans.lib.cvsclient.file.FileUtils;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.admin.AdminHandler;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

/**
 * Revert modifications action.
 * 
 * @author Maros Sandor
 */
public class GetCleanAction extends AbstractSystemAction {

    protected String getBaseName() {
        return "CTL_MenuItem_GetClean";
    }

    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_IN_REPOSITORY & 
                ~FileInformation.STATUS_VERSIONED_UPTODATE;
    }
    
    public void actionPerformed(ActionEvent ev) {
        int res = JOptionPane.showConfirmDialog(
                null, 
                NbBundle.getMessage(GetCleanAction.class, "CTL_RevertModifications_Prompt"),
                NbBundle.getMessage(GetCleanAction.class, "CTL_RevertModifications_Title"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (res != JOptionPane.YES_OPTION) return;
        
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                revertModifications();
            }
        });
    }

    private void revertModifications() {
        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
        AdminHandler ah = CvsVersioningSystem.getInstance().getAdminHandler();
        File [] files = getFilesToProcess();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) continue;
            Entry entry = null;
            try {
                entry = ah.getEntry(file);
            } catch (IOException e) {
                // non-fatal, we have no entry for this file
            }
            try {
                File cleanFile = VersionsCache.getInstance().getRemoteFile(file, VersionsCache.REVISION_BASE);
                FileUtils.copyFile(cleanFile, file);
                if (entry != null && entry.isUserFileToBeRemoved()) {
                    entry.setRevision(entry.getRevision().substring(1));
                    ah.setEntry(file, entry);
                }
                FileObject fo = FileUtil.toFileObject(file);
                if (fo != null) {
                    fo.refresh();
                }
                cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UPTODATE);
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }
}
