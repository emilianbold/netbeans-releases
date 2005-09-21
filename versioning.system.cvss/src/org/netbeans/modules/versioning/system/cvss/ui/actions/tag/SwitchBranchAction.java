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
import org.netbeans.modules.versioning.system.cvss.ui.actions.add.AddExecutor;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.UpdateExecutor;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.ExecutorSupport;
import org.netbeans.modules.versioning.system.cvss.FileStatusCache;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.lib.cvsclient.command.add.AddCommand;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.Dialog;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

/**
 * Performs the CVS 'update -r branch' command on selected nodes.
 * 
 * @author Maros Sandor
 */
public class SwitchBranchAction extends AbstractSystemAction {

    private static final int enabledForStatus = FileInformation.STATUS_VERSIONED_MERGE
                    | FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY 
                    | FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY 
                    | FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY
                    | FileInformation.STATUS_VERSIONED_UPTODATE;
    
    protected String getBaseName() {
        return "CTL_MenuItem_SwitchBranch";
    }

    protected int getFileEnabledStatus() {
        return enabledForStatus;
    }

    public void performCvsAction(ActionEvent ev) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                switchBranch();
            }
        });
    }
    
    private void switchBranch() {
        Context context = getContext();

        UpdateCommand cmd = new UpdateCommand();
        String title = MessageFormat.format(NbBundle.getBundle(SwitchBranchAction.class).getString("CTL_SwitchBranchDialog_Title"), 
                                         new Object[] { getContextDisplayName() });
        
        SwitchBranchPanel settings = new SwitchBranchPanel(context.getFiles());

        JButton swich = new JButton(NbBundle.getMessage(SwitchBranchAction.class, "CTL_SwitchBranchDialog_Action_Switch"));
        JButton cancel = new JButton(NbBundle.getMessage(SwitchBranchAction.class, "CTL_SwitchBranchDialog_Action_Cancel"));
        DialogDescriptor descriptor = new DialogDescriptor(
                settings,
                title,
                true,
                new Object [] { swich, cancel },
                swich,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(SwitchBranchAction.class),
                null);
        descriptor.setClosingOptions(null);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        if (descriptor.getValue() != swich) return;

        settings.saveSettings();
        
        List newFolders = new ArrayList();
        List others = new ArrayList();
        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
        
        File [] roots = context.getRootFiles();
        for (int i = 0; i < roots.length; i++) {
            File root = roots[i];
            // FIXME this check fails on workdir root, it's incorectly recognides as locally new
            // console: cvs [add aborted]: there is no version here; do 'cvs checkout' first
            // see #64103
            if (root.isDirectory() && cache.getStatus(root).getStatus() == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) {
                newFolders.add(root);
            } else {
                others.add(root);
            }
        }

        // Special treatment for Locally New folders. Ww cannot switch them to branch with the Update command.
        // Workaround: add the folder to CVS, then manually create CVS/Tag inside
        if (newFolders.size() > 0) {
            AddCommand acmd = new AddCommand();
            File [] files = (File[]) newFolders.toArray(new File[newFolders.size()]);
            acmd.setFiles(files);
            AddExecutor [] aexecutors = AddExecutor.executeCommand(acmd, CvsVersioningSystem.getInstance(), null);
            ExecutorSupport.wait(aexecutors);
            if (settings.isSwitchToTrunk()) {
                setSticky(files, null);
            } else {
                setSticky(files, settings.getBranchName());
            }
        }
        
        if (others.size() > 0) {
            if (settings.isSwitchToTrunk()) {
                cmd.setResetStickyOnes(true);
            } else {
                cmd.setUpdateByRevision(settings.getBranchName());
            }
            
            cmd.setBuildDirectories(true);
            cmd.setPruneDirectories(true);
            cmd.setFiles((File[]) others.toArray(new File[others.size()]));
            
            GlobalOptions options = new GlobalOptions();
            if (context.getExclusions().size() > 0) {
                options.setExclusions((File[]) context.getExclusions().toArray(new File[context.getExclusions().size()]));
            }
            
            UpdateExecutor [] executors = UpdateExecutor.executeCommand(cmd, CvsVersioningSystem.getInstance(), options);
            ExecutorSupport.notifyError(executors);
        }
    }

    private void setSticky(File[] files, String sticky) {
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            try {
                setSticky(file, sticky);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }

    /**
     * Creates CVS/Tag file under the given directory with Tsticky string. If sticky is null, it deleted CVS/Tag file. 
     * 
     * @param file directory to tag
     * @param sticky sitcky tag to use
     * @throws IOException if some I/O operation fails
     */ 
    private void setSticky(File file, String sticky) throws IOException {
        File tag = new File(file, "CVS/Tag");
        tag.delete();
        if ("HEAD".equals(sticky)) {  // NOI18N
            return;
        }
        if (sticky != null) {
            FileWriter w = new FileWriter(tag);
            w.write("T");
            w.write(sticky);
            w.write(System.getProperty("line.separator"));
            w.close();
        }
    }
}
