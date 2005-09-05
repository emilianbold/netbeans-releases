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
import org.netbeans.modules.versioning.system.cvss.CvsFileNode;
import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

import java.io.File;
import java.awt.event.ActionEvent;

/**
 * Opens the Visual Merge component. 
 *  
 * @author Maros Sandor
 */
public class ResolveConflictsAction extends AbstractSystemAction {
    
    private static final int enabledForStatus = FileInformation.STATUS_VERSIONED_CONFLICT;  

    public ResolveConflictsAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    protected String getBaseName() {
        return "CTL_MenuItem_ResolveConflicts";
    }

    protected int getFileEnabledStatus() {
        return enabledForStatus;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }

    public void performCvsAction(ActionEvent ev) {
        File [] files = getFilesToProcess();
        CvsFileNode [] nodes = CvsVersioningSystem.getInstance().getFileTableModel(files, FileInformation.STATUS_VERSIONED_CONFLICT).getNodes();
        if (nodes.length == 0) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                NbBundle.getMessage(ResolveConflictsAction.class, "MSG_NoConflicts")));
            return;
        }
        for (int i = 0; i < nodes.length; i++) {
            ResolveConflictsExecutor rce = new ResolveConflictsExecutor();
            rce.exec(nodes[i].getFile());
        }
    }
}
