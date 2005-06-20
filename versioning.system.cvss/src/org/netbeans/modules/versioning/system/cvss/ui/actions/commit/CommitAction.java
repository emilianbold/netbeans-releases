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

package org.netbeans.modules.versioning.system.cvss.ui.actions.commit;

import org.openide.util.NbBundle;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.netbeans.lib.cvsclient.command.commit.CommitCommand;
import org.netbeans.lib.cvsclient.command.add.AddCommand;
import org.netbeans.lib.cvsclient.command.KeywordSubstitutionOptions;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.ExecutorSupport;
import org.netbeans.modules.versioning.system.cvss.FileStatusCache;
import org.netbeans.modules.versioning.system.cvss.settings.CvsModuleConfig;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.add.AddExecutor;
import org.netbeans.modules.versioning.spi.VersioningListener;
import org.netbeans.modules.versioning.spi.VersioningEvent;

import javax.swing.*;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.*;

/**
 * @author Maros Sandor
 */
public class CommitAction extends AbstractSystemAction {
    
    private static final ResourceBundle loc = NbBundle.getBundle(CommitAction.class);
    
    private static CommitCommand   commandTemplate = new CommitCommand();
    private static final int enabledForStatus = 
            FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | 
            FileInformation.STATUS_VERSIONED_ADDEDLOCALLY | 
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY | 
            FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY;

    public CommitAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    protected String getBaseName() {
        return "CTL_MenuItem_Commit";
    }

    // handle exclude from commit status which is not not modeled as status
    protected File [] getFilesToProcess() {
        int enabledStatus = getFileEnabledStatus();
        int dirEnabledStatus = getDirectoryEnabledStatus();
        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
        CvsModuleConfig config = CvsModuleConfig.getDefault();
        File [] files = Utils.getActivatedFiles();
        boolean atLeastOneUnexcluded = false;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            FileInformation fi = cache.getStatus(file);
            if (file.isDirectory()) {
                if ((fi.getStatus() & dirEnabledStatus) == 0) return new File[0];
            } else {
                if ((fi.getStatus() & enabledStatus) == 0) return new File[0];
            }

            if (config.isExcludedFromCommit(files[i].getAbsolutePath())) {
                continue;
            }
            atLeastOneUnexcluded = true;
        }

        if (atLeastOneUnexcluded) {
            return files;
        } else {
            return new File[0];
        }
    }

    protected int getFileEnabledStatus() {
        return enabledForStatus;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }

    public static void invokeCommit(String title, File [] roots) {
        if (CvsVersioningSystem.getInstance().getFileTableModel(roots, FileInformation.STATUS_LOCAL_CHANGE).getNodes().length == 0) {
            JOptionPane.showMessageDialog(null, loc.getString("MSG_NoFilesToCommit_Prompt"), 
                                          loc.getString("MSG_NoFilesToCommit_Title"), JOptionPane.INFORMATION_MESSAGE);
            return;   
        }
        
        CommitCommand cmd = new CommitCommand();
        cmd.setDisplayName(NbBundle.getMessage(CommitAction.class, "BK0001"));
        copy (cmd, commandTemplate);
        
        final CommitSettings settings = new CommitSettings(roots);
        settings.setCommand(cmd);
        final JButton commit = new JButton(loc.getString("CTL_CommitForm_Action_Commit"));
        commit.setEnabled(false);
        JButton cancel = new JButton(loc.getString("CTL_CommitForm_Action_Cancel"));
        DialogDescriptor descriptor = new DialogDescriptor(
                settings, 
                title,
                true,
                new Object [] { commit, cancel },
                commit,
                DialogDescriptor.BOTTOM_ALIGN,
                null,
                null); 
        descriptor.setClosingOptions(null);
        settings.addVersioningListener(new VersioningListener() {
            public void versioningEvent(VersioningEvent event) {
                enableCommit(settings, commit);
            }
        });
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        if (descriptor.getValue() != commit) return;

        settings.updateCommand(cmd);
        copy(commandTemplate, cmd);
        cmd.setFiles(roots);
        executeCommit(settings);
    }

    private static void enableCommit(CommitSettings settings, JButton commit) {
        CommitSettings.CommitFile [] files = settings.getCommitFiles();
        for (int i = 0; i < files.length; i++) {
            CommitSettings.CommitFile file = files[i];
            if (file.getOptions() == CommitOptions.EXCLUDE) continue;
            int status = file.getNode().getInformation().getStatus();
            if ((status & FileInformation.STATUS_REMOTE_CHANGE) != 0 || status == FileInformation.STATUS_VERSIONED_CONFLICT) {
                commit.setEnabled(false);
                String msg = (status == FileInformation.STATUS_VERSIONED_CONFLICT) ? 
                        loc.getString("MSG_CommitForm_ErrorConflicts") :
                        loc.getString("MSG_CommitForm_ErrorRemoteChanges");
                settings.setErrorLabel("<html><font color=\"#002080\">" + msg + "</font></html>");
                return;
            }
        }
        settings.setErrorLabel("");
        commit.setEnabled(true);
    }

    public void actionPerformed(ActionEvent ev) {
        File [] roots = getFilesToProcess();
        String title;
        if (roots.length > 1) {
            title = MessageFormat.format(NbBundle.getBundle(CommitAction.class).getString("CTL_CommitDialog_Title_Multi"), 
                                         new Integer[] { new Integer(roots.length) });
        } else {
            title = MessageFormat.format(NbBundle.getBundle(CommitAction.class).getString("CTL_CommitDialog_Title"), 
                                         new Object[] { roots[0].getName() });            
        }
        invokeCommit(title, roots);
    }
    
    private static void copy(CommitCommand c1, CommitCommand c2) {
        c1.setMessage(c2.getMessage());
        c1.setRecursive(c2.isRecursive());
        c1.setForceCommit(c2.isForceCommit());
        c1.setLogMessageFromFile(c2.getLogMessageFromFile());
        c1.setNoModuleProgram(c2.isNoModuleProgram());
        c1.setToRevisionOrBranch(c2.getToRevisionOrBranch());
        c1.setDisplayName(c2.getDisplayName());
    }

    /**
     * Executes add/commit actions based on settings in the Commit dialog.
     * 
     * @param settings user settings
     */ 
    public static void executeCommit(CommitSettings settings) {
        CommitSettings.CommitFile [] files = settings.getCommitFiles();
        List commitBucket = new ArrayList();
        List addDefaultBucket = new ArrayList();
        List addKkvBucket = new ArrayList();
        List addKkvlBucket = new ArrayList();
        List addKkBucket = new ArrayList();
        List addKoBucket = new ArrayList();
        List addKbBucket = new ArrayList();
        List addKvBucket = new ArrayList();
        for (int i = 0; i < files.length; i++) {
            CommitSettings.CommitFile file = files[i];
            if (file.getOptions() == CommitOptions.EXCLUDE) continue;
            if (file.getOptions() == CommitOptions.ADD_TEXT) {
                addDefaultBucket.add(file.getNode().getFile());
            } else if (file.getOptions() == CommitOptions.ADD_BINARY) {
                addKbBucket.add(file.getNode().getFile());
            }
            commitBucket.add(file.getNode().getFile());
        }
        executeAdd(addDefaultBucket, null);
        executeAdd(addKkvBucket, KeywordSubstitutionOptions.DEFAULT);
        executeAdd(addKkvlBucket, KeywordSubstitutionOptions.DEFAULT_LOCKER);
        executeAdd(addKkBucket, KeywordSubstitutionOptions.ONLY_KEYWORDS);
        executeAdd(addKoBucket, KeywordSubstitutionOptions.OLD_VALUES);
        executeAdd(addKbBucket, KeywordSubstitutionOptions.BINARY);
        executeAdd(addKvBucket, KeywordSubstitutionOptions.ONLY_VALUES);
        executeCommit(commitBucket, settings.getCommitMessage());
    }

    private static void executeCommit(List bucket, String message) {
        if (bucket.size() == 0) return;
        CommitCommand cmd = new CommitCommand();
        cmd.setFiles((File []) bucket.toArray(new File[bucket.size()]));
        cmd.setMessage(message);
        CommitExecutor [] executors = CommitExecutor.executeCommand(cmd, CvsVersioningSystem.getInstance(), null);
        ExecutorSupport.notifyError(executors);
    }

    private static void executeAdd(List bucket, KeywordSubstitutionOptions option) {
        if (bucket.size() == 0) return;
        AddCommand cmd = new AddCommand();
        cmd.setFiles((File []) bucket.toArray(new File[bucket.size()]));
        cmd.setKeywordSubst(option);
        AddExecutor [] executors = AddExecutor.executeCommand(cmd, CvsVersioningSystem.getInstance(), null);
        ExecutorSupport.notifyError(executors);
    }
}
