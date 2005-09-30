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
import org.openide.util.HelpCtx;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.netbeans.lib.cvsclient.command.commit.CommitCommand;
import org.netbeans.lib.cvsclient.command.add.AddCommand;
import org.netbeans.lib.cvsclient.command.KeywordSubstitutionOptions;
import org.netbeans.lib.cvsclient.command.remove.RemoveCommand;
import org.netbeans.modules.versioning.system.cvss.*;
import org.netbeans.modules.versioning.system.cvss.executor.RemoveExecutor;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.add.AddExecutor;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.VersioningEvent;

import javax.swing.*;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.*;

/**
 * Represents the "Commit" main/popup action and provides programmatic Commit action upon any context.
 *
 * @author Maros Sandor
 */
public class CommitAction extends AbstractSystemAction {
    
    private static CommitCommand   commandTemplate = new CommitCommand();

    public CommitAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    protected String getBaseName() {
        return "CTL_MenuItem_Commit";
    }

    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_LOCAL_CHANGE;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }

    public static void invokeCommit(String contentTitle, Context context) {
        ResourceBundle loc = NbBundle.getBundle(CommitAction.class);
        if (CvsVersioningSystem.getInstance().getFileTableModel(context, FileInformation.STATUS_LOCAL_CHANGE).getNodes().length == 0) {
            JOptionPane.showMessageDialog(null, loc.getString("MSG_NoFilesToCommit_Prompt"), 
                                          loc.getString("MSG_NoFilesToCommit_Title"), JOptionPane.INFORMATION_MESSAGE);
            return;   
        }
        
        CommitCommand cmd = new CommitCommand();
        cmd.setDisplayName(NbBundle.getMessage(CommitAction.class, "BK0001"));
        copy (cmd, commandTemplate);
        
        final CommitSettings settings = new CommitSettings();
        settings.setCommand(cmd);
        final JButton commit = new JButton(loc.getString("CTL_CommitForm_Action_Commit"));
        commit.setEnabled(false);
        JButton cancel = new JButton(loc.getString("CTL_CommitForm_Action_Cancel"));
        DialogDescriptor descriptor = new DialogDescriptor(
                settings, 
                MessageFormat.format(loc.getString("CTL_CommitDialog_Title"), new Object [] { contentTitle }),
                true,
                new Object [] { commit, cancel },
                commit,
                DialogDescriptor.BOTTOM_ALIGN,
                null,
                null);
        descriptor.setClosingOptions(null);
        descriptor.setHelpCtx(new HelpCtx(CommitSettings.class));
        settings.addVersioningListener(new VersioningListener() {
            public void versioningEvent(VersioningEvent event) {
                refreshCommitDialog(settings, commit);
            }
        });
        setupNodes(settings, context);
        settings.putClientProperty("contentTitle", contentTitle);
        settings.putClientProperty("DialogDescriptor", descriptor);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        if (descriptor.getValue() != commit) return;

        settings.updateCommand(cmd);
        copy(commandTemplate, cmd);
        executeCommit(settings);
    }
    
    private static void setupNodes(CommitSettings settings, Context context) {
        CvsFileNode [] filesToCommit = CvsVersioningSystem.getInstance().getFileTableModel(context, FileInformation.STATUS_LOCAL_CHANGE).getNodes();
        settings.setNodes(filesToCommit);
    }

    /**
     * User changed a commit action.
     * 
     * @param settings
     * @param commit
     */ 
    private static void refreshCommitDialog(CommitSettings settings, JButton commit) {
        ResourceBundle loc = NbBundle.getBundle(CommitAction.class);
        CommitSettings.CommitFile [] files = settings.getCommitFiles();
        Set stickyTags = new HashSet();
        for (int i = 0; i < files.length; i++) {
            CommitSettings.CommitFile file = files[i];
            if (file.getOptions() == CommitOptions.EXCLUDE) continue;
            stickyTags.add(Utils.getSticky(file.getNode().getFile()));
            int status = file.getNode().getInformation().getStatus();
            if ((status & FileInformation.STATUS_REMOTE_CHANGE) != 0 || status == FileInformation.STATUS_VERSIONED_CONFLICT) {
                commit.setEnabled(false);
                String msg = (status == FileInformation.STATUS_VERSIONED_CONFLICT) ? 
                        loc.getString("MSG_CommitForm_ErrorConflicts") :
                        loc.getString("MSG_CommitForm_ErrorRemoteChanges");
                settings.setErrorLabel("<html><font color=\"#002080\">" + msg + "</font></html>");
                return;
            }
            stickyTags.add(Utils.getSticky(file.getNode().getFile()));
        }
        
        if (stickyTags.size() > 1) {
            settings.setColumns(new String [] { CommitSettings.COLUMN_NAME_NAME, CommitSettings.COLUMN_NAME_STICKY, CommitSettings.COLUMN_NAME_STATUS, 
                                                CommitSettings.COLUMN_NAME_ACTION, CommitSettings.COLUMN_NAME_PATH });
        } else {
            settings.setColumns(new String [] { CommitSettings.COLUMN_NAME_NAME, CommitSettings.COLUMN_NAME_STATUS, 
                                                CommitSettings.COLUMN_NAME_ACTION, CommitSettings.COLUMN_NAME_PATH });
        }
        
        String contentTitle = (String) settings.getClientProperty("contentTitle");
        DialogDescriptor dd = (DialogDescriptor) settings.getClientProperty("DialogDescriptor");
        if (stickyTags.size() <= 1) {
            String stickyTag = stickyTags.size() == 0 ? null : (String) stickyTags.iterator().next(); 
            if (stickyTag == null) {
                dd.setTitle(MessageFormat.format(loc.getString("CTL_CommitDialog_Title"), new Object [] { contentTitle }));
                settings.setErrorLabel("");
            } else {
                stickyTag = stickyTag.substring(1);
                dd.setTitle(MessageFormat.format(loc.getString("CTL_CommitDialog_Title_Branch"), new Object [] { contentTitle, stickyTag }));
                String msg = MessageFormat.format(loc.getString("MSG_CommitForm_InfoBranch"), new Object [] { stickyTag });
                settings.setErrorLabel("<html><font color=\"#002080\">" + msg + "</font></html>");
            }
        } else {
            dd.setTitle(MessageFormat.format(loc.getString("CTL_CommitDialog_Title_Branches"), new Object [] { contentTitle }));
            String msg = loc.getString("MSG_CommitForm_ErrorMultipleBranches");
            settings.setErrorLabel("<html><font color=\"#CC0000\">" + msg + "</font></html>");
        }
        commit.setEnabled(true);
    }

    public void performCvsAction(ActionEvent ev) {
        invokeCommit(getContextDisplayName(), getContext());
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
        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
        CommitSettings.CommitFile [] files = settings.getCommitFiles();
        List commitBucket = new ArrayList();
        List addDefaultBucket = new ArrayList();
        List addKkvBucket = new ArrayList();
        List addKkvlBucket = new ArrayList();
        List addKkBucket = new ArrayList();
        List addKoBucket = new ArrayList();
        List addKbBucket = new ArrayList();
        List addKvBucket = new ArrayList();
        List removeBucket = new ArrayList();
        for (int i = 0; i < files.length; i++) {
            CommitSettings.CommitFile file = files[i];
            if (file.getOptions() == CommitOptions.EXCLUDE) continue;
            if (file.getOptions() == CommitOptions.ADD_TEXT) {
                addDefaultBucket.add(file.getNode().getFile());
            } else if (file.getOptions() == CommitOptions.ADD_BINARY) {
                addKbBucket.add(file.getNode().getFile());
            } else if (file.getOptions() == CommitOptions.COMMIT_REMOVE) {
                int status = cache.getStatus(file.getNode().getFile()).getStatus();
                if (status == FileInformation.STATUS_VERSIONED_DELETEDLOCALLY) {
                    removeBucket.add(file.getNode().getFile());
                }
            }
            commitBucket.add(file.getNode().getFile());
        }

        // perform
        ExecutorGroup group = new ExecutorGroup("Commiting");

        group.addExecutors(createAdd(addDefaultBucket, null));
        group.addExecutors(createAdd(addKkvBucket, KeywordSubstitutionOptions.DEFAULT));
        group.addExecutors(createAdd(addKkvlBucket, KeywordSubstitutionOptions.DEFAULT_LOCKER));
        group.addExecutors(createAdd(addKkBucket, KeywordSubstitutionOptions.ONLY_KEYWORDS));
        group.addExecutors(createAdd(addKoBucket, KeywordSubstitutionOptions.OLD_VALUES));
        group.addExecutors(createAdd(addKbBucket, KeywordSubstitutionOptions.BINARY));
        group.addExecutors(createAdd(addKvBucket, KeywordSubstitutionOptions.ONLY_VALUES));
        group.addExecutors(createRemove(removeBucket));
        group.addExecutors(createCommit(commitBucket, settings.getCommitMessage()));

        group.execute();
    }

    private static void addExecutors(Collection target, ExecutorSupport[] src) {
    }

    private static ExecutorSupport[] createCommit(List bucket, String message) {
        if (bucket.size() == 0) return null;
        CommitCommand cmd = new CommitCommand();
        cmd.setFiles((File []) bucket.toArray(new File[bucket.size()]));
        cmd.setMessage(message);
        return CommitExecutor.splitCommand(cmd, CvsVersioningSystem.getInstance(), null);
    }

    private static ExecutorSupport[] createRemove(List bucket) {
        if (bucket.size() == 0) return null;
        RemoveCommand cmd = new RemoveCommand();
        cmd.setFiles((File []) bucket.toArray(new File[bucket.size()]));
        return RemoveExecutor.splitCommand(cmd, CvsVersioningSystem.getInstance(), null);
    }

    private static ExecutorSupport[] createAdd(List bucket, KeywordSubstitutionOptions option) {
        if (bucket.size() == 0) return null;
        AddCommand cmd = new AddCommand();
        cmd.setFiles((File []) bucket.toArray(new File[bucket.size()]));
        cmd.setKeywordSubst(option);
        return AddExecutor.splitCommand(cmd, CvsVersioningSystem.getInstance(), null);

    }
}
