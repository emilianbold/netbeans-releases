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

package org.netbeans.modules.versioning.system.cvss.ui.actions.tag;

import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.UpdateExecutor;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.ExecutorSupport;
import org.netbeans.modules.versioning.system.cvss.ExecutorGroup;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.lib.cvsclient.command.tag.TagCommand;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.HelpCtx;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;

import javax.swing.*;
import java.awt.Dialog;
import java.io.File;
import java.text.MessageFormat;
import java.util.*;

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
    
    protected String getBaseName(Node [] activatedNodes) {
        return "CTL_MenuItem_Branch";  // NOI18N
    }

    protected int getFileEnabledStatus() {
        return enabledForStatus;
    }

    protected boolean asynchronous() {
        return false;
    }
    
    public void performCvsAction(Node[] nodes) {
        Context context = getContext(nodes);
        if (context.getFiles().length == 0) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                    NbBundle.getBundle(BranchAction.class).getString("CTL_BranchDialogNone_Prompt"),
                    NbBundle.getBundle(BranchAction.class).getString("CTL_BranchDialogNone_Title"),
                    NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE, null, null));
            return;
        }

        String title = MessageFormat.format(NbBundle.getBundle(BranchAction.class).getString("CTL_BranchDialog_Title"), getContextDisplayName(nodes));

        JButton branch = new JButton(NbBundle.getMessage(BranchAction.class, "CTL_BranchDialog_Action_Branch"));
        branch.setToolTipText(NbBundle.getMessage(BranchAction.class,  "TT_BranchDialog_Action_Branch"));
        BranchSettings settings = new BranchSettings(context.getFiles());
        settings.putClientProperty("OKButton", branch);
        settings.onBranchNameChange();
        DialogDescriptor descriptor = new DialogDescriptor(
                settings,
                title,
                true,
                new Object [] { branch, DialogDescriptor.CANCEL_OPTION },
                branch,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(BranchAction.class),
                null);
        descriptor.setClosingOptions(null);

        settings.putClientProperty("org.openide.DialogDescriptor", descriptor);  // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BranchAction.class,  "ACSD_BranchDialog"));
        dialog.setVisible(true);
        if (descriptor.getValue() != branch) return;

        settings.saveSettings();

        RequestProcessor.getDefault().post(new BranchExecutor(context, settings, getRunningName(nodes)));
    }
    
    private static class BranchExecutor implements Runnable {

        private final Context context;
        private final BranchSettings settings;
        private final String name;

        public BranchExecutor(Context context, BranchSettings settings, String name) {
            this.context = context;
            this.settings = settings;
            this.name = name;
        }

        public void run() {
            ExecutorGroup group = new ExecutorGroup(name);
            if (settings.isTaggingBase()) {
                group.addExecutors(tag(context.getFiles(), settings.getBaseTagName()));
                group.addBarrier(null);
            }
            group.addExecutors(branch(context.getFiles(), settings.getBranchName()));
            group.addBarrier(null);
            if (settings.isCheckingOutBranch()) {
                group.addExecutors(update(context, settings.getBranchName()));
            }
            group.execute();
        }

        private ExecutorSupport[] update(Context context, String revision) {
            File[][] files = Utils.splitFlatOthers(context.getRootFiles());
            List<ExecutorSupport> executors = new ArrayList<ExecutorSupport>();
            executors.addAll(update(files[0], revision, false));
            executors.addAll(update(files[1], revision, true));
            return (ExecutorSupport[]) executors.toArray(new ExecutorSupport[executors.size()]);
        }

        private List<UpdateExecutor> update(File[] roots, String revision, boolean recursive) {
            if (roots.length == 0) return Collections.emptyList();
            UpdateCommand cmd = new UpdateCommand();

            GlobalOptions options = CvsVersioningSystem.createGlobalOptions();
            if (context.getExclusions().size() > 0) {
                options.setExclusions((File[]) context.getExclusions().toArray(new File[context.getExclusions().size()]));
            }
            cmd.setUpdateByRevision(revision);
            cmd.setFiles(roots);
            cmd.setRecursive(recursive);
        
            return Arrays.asList(UpdateExecutor.splitCommand(cmd, CvsVersioningSystem.getInstance(), options, null));
        }
        
        private ExecutorSupport[] branch(File[] roots, String branchName) {
            File[][] files = Utils.splitFlatOthers(roots);
            List<ExecutorSupport> executors = new ArrayList<ExecutorSupport>();
            executors.addAll(tag(files[0], branchName, true, false));
            executors.addAll(tag(files[1], branchName, true, true));
            return (ExecutorSupport[]) executors.toArray(new ExecutorSupport[executors.size()]);
        }

        private ExecutorSupport[] tag(File[] roots, String tagName) {
            File[][] files = Utils.splitFlatOthers(roots);
            List<ExecutorSupport> executors = new ArrayList<ExecutorSupport>();
            executors.addAll(tag(files[0], tagName, false, false));
            executors.addAll(tag(files[1], tagName, false, true));
            return (ExecutorSupport[]) executors.toArray(new ExecutorSupport[executors.size()]);
        }

        private List<TagExecutor> tag(File[] roots, String branchName, boolean isBranch, boolean recursive) {
            if (roots.length == 0) return Collections.emptyList();
            TagCommand cmd = new TagCommand();
            cmd.setMakeBranchTag(isBranch);
            cmd.setFiles(roots);
            cmd.setTag(branchName);
            cmd.setRecursive(recursive);
            return Arrays.asList(TagExecutor.splitCommand(cmd, CvsVersioningSystem.getInstance(), null));
        }
    }
}
