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

package org.netbeans.modules.versioning.system.cvss.ui.actions.tag;

import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.lib.cvsclient.command.tag.TagCommand;
import org.openide.util.NbBundle;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import java.awt.event.ActionEvent;
import java.awt.Dialog;
import java.io.File;
import java.text.MessageFormat;

/**
 * Performs the CVS 'tag -b' command on selected nodes.
 * 
 * @author Maros Sandor
 */
public class BranchAction extends AbstractSystemAction {

    private static final int enabledForStatus = FileInformation.STATUS_VERSIONED_MERGE
                    | FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY 
                    | FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY 
                    | FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY
                    | FileInformation.STATUS_VERSIONED_UPTODATE;
    
    protected String getBaseName() {
        return "CTL_MenuItem_Branch";
    }

    protected int getFileEnabledStatus() {
        return enabledForStatus;
    }

    public void actionPerformed(ActionEvent ev) {
        File [] roots = getFilesToProcess();

        TagCommand cmd = new TagCommand();
        String title;
        if (roots.length > 1) {
            title = MessageFormat.format(NbBundle.getBundle(BranchAction.class).getString("CTL_BranchDialog_Title_Multi"), 
                                         new Integer[] { new Integer(roots.length) });
        } else {
            title = MessageFormat.format(NbBundle.getBundle(BranchAction.class).getString("CTL_BranchDialog_Title"), 
                                         new Object[] { roots[0].getName() });            
        }
        
        BranchSettings settings = new BranchSettings();
        DialogDescriptor descriptor = new DialogDescriptor(settings, title);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        if (descriptor.getValue() != DialogDescriptor.OK_OPTION) return;

        settings.saveSettings();
        cmd.setMakeBranchTag(true);
        cmd.setFiles(roots);
        
        TagExecutor executor = new TagExecutor(CvsVersioningSystem.getInstance(), cmd);
        executor.execute();
    }
}
