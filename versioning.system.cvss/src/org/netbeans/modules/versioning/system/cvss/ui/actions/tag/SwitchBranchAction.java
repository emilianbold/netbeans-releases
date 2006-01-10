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
import org.netbeans.modules.versioning.system.cvss.*;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.lib.cvsclient.command.add.AddCommand;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.nodes.Node;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
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
    
    protected String getBaseName(Node [] activatedNodes) {
        return "CTL_MenuItem_SwitchBranch";  // NOI18N
    }

    protected int getFileEnabledStatus() {
        return enabledForStatus;
    }

    public void performCvsAction(final Node[] nodes) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                switchBranch(nodes);
            }
        });
    }

    protected boolean asynchronous() {
        return false;
    }

    private void switchBranch(Node[] nodes) {
        Context context = getContext(nodes);

        String title = MessageFormat.format(NbBundle.getBundle(SwitchBranchAction.class).getString("CTL_SwitchBranchDialog_Title"), 
                                         new Object[] { getContextDisplayName(nodes) });
        
        final SwitchBranchPanel settings = new SwitchBranchPanel(context.getFiles());

        JButton swich = new JButton(NbBundle.getMessage(SwitchBranchAction.class, "CTL_SwitchBranchDialog_Action_Switch"));
        swich.setToolTipText(NbBundle.getMessage(SwitchBranchAction.class, "TT_SwitchBranchDialog_Action_Switch"));
        DialogDescriptor descriptor = new DialogDescriptor(
                settings,
                title,
                true,
                new Object [] { swich, DialogDescriptor.CANCEL_OPTION },
                swich,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(SwitchBranchAction.class),
                null);
        descriptor.setClosingOptions(null);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SwitchBranchAction.class, "ACSD_SwitchBranchDialog"));
        dialog.setVisible(true);
        if (descriptor.getValue() != swich) return;

        settings.saveSettings();
        
        List newFolders = new ArrayList();
        List others = new ArrayList();
        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();

        ExecutorGroup group = new ExecutorGroup(getRunningName(nodes));
        File [][] flatRecursive = Utils.splitFlatOthers(context.getRootFiles());
        if (flatRecursive[0].length > 0) {
            File[] flat = flatRecursive[0];
            UpdateCommand cmd = new UpdateCommand();
            if (settings.isSwitchToTrunk()) {
                cmd.setResetStickyOnes(true);
            } else {
                cmd.setUpdateByRevision(settings.getBranchName());
            }

            cmd.setBuildDirectories(true);
            cmd.setPruneDirectories(true);
            cmd.setRecursive(false);
            cmd.setFiles(flat);

            GlobalOptions options = CvsVersioningSystem.createGlobalOptions();
            if (context.getExclusions().size() > 0) {
                options.setExclusions((File[]) context.getExclusions().toArray(new File[context.getExclusions().size()]));
            }

            group.addExecutors(UpdateExecutor.splitCommand(cmd, CvsVersioningSystem.getInstance(), options));

        }
        if (flatRecursive[1].length > 0) {
            File [] roots = flatRecursive[1];
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
                final File [] files = (File[]) newFolders.toArray(new File[newFolders.size()]);
                acmd.setFiles(files);
                group.addExecutors(AddExecutor.splitCommand(acmd, CvsVersioningSystem.getInstance(), null));
                Runnable action = new Runnable() {
                    public void run() {
                        if (settings.isSwitchToTrunk()) {
                            setSticky(files, null);
                        } else {
                            setSticky(files, settings.getBranchName());
                        }
                    }
                };
                group.addBarrier(action);
                others.addAll(newFolders);
            }

            if (others.size() > 0) {
                UpdateCommand cmd = new UpdateCommand();
                if (settings.isSwitchToTrunk()) {
                    cmd.setResetStickyOnes(true);
                } else {
                    cmd.setUpdateByRevision(settings.getBranchName());
                }

                cmd.setBuildDirectories(true);
                cmd.setPruneDirectories(true);
                cmd.setFiles((File[]) others.toArray(new File[others.size()]));

                GlobalOptions options = CvsVersioningSystem.createGlobalOptions();
                if (context.getExclusions().size() > 0) {
                    options.setExclusions((File[]) context.getExclusions().toArray(new File[context.getExclusions().size()]));
                }

                group.addExecutors(UpdateExecutor.splitCommand(cmd, CvsVersioningSystem.getInstance(), options));
            }
        }
        group.execute();
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
        File tag = new File(file, "CVS/Tag");  // NOI18N
        tag.delete();
        if ("HEAD".equals(sticky)) {  // NOI18N
            return;
        }
        if (sticky != null) {
            FileWriter w = new FileWriter(tag);
            w.write("T");  // NOI18N
            w.write(sticky);
            w.write(System.getProperty("line.separator")); // NOI18N
            w.close();
        }
    }
}
