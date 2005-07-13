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
import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.openide.util.NbBundle;

import java.io.File;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.*;

/**
 * Show differencies between the current working copy and repository version we started from. 
 *  
 * @author Maros Sandor
 */
public class DiffAction extends AbstractSystemAction {
    
    private static final ResourceBundle loc = NbBundle.getBundle(DiffAction.class);
    
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

    protected int getFileEnabledStatus() {
        return enabledForStatus;
    }

    public void actionPerformed(ActionEvent ev) {
        File [] files = getFilesToProcess();
        String title = MessageFormat.format(loc.getString("CTL_DiffDialogLocal_Title"), new Object [] { getContextDisplayName() }); 
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
