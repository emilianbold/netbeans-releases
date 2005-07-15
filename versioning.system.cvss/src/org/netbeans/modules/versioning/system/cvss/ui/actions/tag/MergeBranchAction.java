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
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.UpdateExecutor;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.ExecutorSupport;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.lib.cvsclient.command.tag.TagCommand;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import java.awt.event.ActionEvent;
import java.awt.Dialog;
import java.io.File;
import java.text.MessageFormat;

/**
 * Performs the CVS 'update -j' command on selected nodes.
 * 
 * @author Maros Sandor
 */
public class MergeBranchAction extends AbstractSystemAction {

    private static final int enabledForStatus = FileInformation.STATUS_VERSIONED_MERGE
                    | FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY 
                    | FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY 
                    | FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY
                    | FileInformation.STATUS_VERSIONED_UPTODATE;
    
    protected String getBaseName() {
        return "CTL_MenuItem_MergeBranch";
    }

    protected int getFileEnabledStatus() {
        return enabledForStatus;
    }

    public void actionPerformed(ActionEvent ev) {
        File [] roots = getFilesToProcess();

        String title = MessageFormat.format(NbBundle.getBundle(MergeBranchAction.class).getString("CTL_MergeBranchDialog_Title"), 
                                         new Object[] { getContextDisplayName() });
        
        MergeBranchPanel settings = new MergeBranchPanel(roots);
        DialogDescriptor descriptor = new DialogDescriptor(settings, title);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        if (descriptor.getValue() != DialogDescriptor.OK_OPTION) return;

        settings.saveSettings();
       
        RequestProcessor.getDefault().post(new MergeBranchExecutor(roots, settings));
    }
 
    private static class MergeBranchExecutor implements Runnable {

        private final File[] roots;
        private final MergeBranchPanel settings;

        public MergeBranchExecutor(File[] roots, MergeBranchPanel settings) {
            this.roots = roots;
            this.settings = settings;
        }

        public void run() {
            if (!update()) return;
            tag();
        }

        private boolean update() {
            UpdateCommand cmd = new UpdateCommand();

            if (settings.isMergingFromTrunk()) {
                
            } else {
                
            }
            
            cmd.setFiles(roots);
        
            UpdateExecutor [] executors = UpdateExecutor.executeCommand(cmd, CvsVersioningSystem.getInstance(), null);
            return ExecutorSupport.wait(executors);
        }

        private boolean tag() {
            TagCommand cmd = new TagCommand();
        
            cmd.setFiles(roots);
//            cmd.setTag(tagName);
        
            TagExecutor [] executors = TagExecutor.executeCommand(cmd, CvsVersioningSystem.getInstance(), null);
            return ExecutorSupport.wait(executors);
        }
    }
    
}
