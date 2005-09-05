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

import org.openide.util.NbBundle;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;

import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Performs the CVS 'update' command on selected nodes.
 * 
 * @author Maros Sandor
 */
public class UpdateAction extends AbstractSystemAction {
    
    protected String getBaseName() {
        return "CTL_MenuItem_Update";
    }

    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_IN_REPOSITORY;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }

    public void performCvsAction(ActionEvent ev) {

        File [][] flatRecursive = Utils.splitFlatOthers(getFilesToProcess());
        if (flatRecursive[0].length > 0) {
            UpdateCommand cmd = new UpdateCommand();
            cmd.setDisplayName(NbBundle.getMessage(UpdateAction.class, "BK0001"));
            cmd.setBuildDirectories(false);
            cmd.setPruneDirectories(false);
            cmd.setRecursive(false);
            cmd.setFiles(flatRecursive[0]);
            UpdateExecutor.executeCommand(cmd, CvsVersioningSystem.getInstance(), null);
        }
        if (flatRecursive[1].length > 0) {
            UpdateCommand cmd = new UpdateCommand();
            cmd.setDisplayName(NbBundle.getMessage(UpdateAction.class, "BK0001"));
            cmd.setBuildDirectories(true);
            cmd.setPruneDirectories(true);
            cmd.setFiles(flatRecursive[1]);
            UpdateExecutor.executeCommand(cmd, CvsVersioningSystem.getInstance(), null);
        }
    }
}
