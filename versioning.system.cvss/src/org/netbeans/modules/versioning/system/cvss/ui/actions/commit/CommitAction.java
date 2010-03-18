/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.versioning.system.cvss.ui.actions.commit;

import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.netbeans.lib.cvsclient.command.commit.CommitCommand;
import org.netbeans.lib.cvsclient.command.add.AddCommand;
import org.netbeans.lib.cvsclient.command.KeywordSubstitutionOptions;
import org.netbeans.lib.cvsclient.command.remove.RemoveCommand;
import org.netbeans.modules.versioning.system.cvss.*;
import org.netbeans.modules.versioning.system.cvss.CvsModuleConfig;
import org.netbeans.modules.versioning.system.cvss.executor.RemoveExecutor;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.add.AddExecutor;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.VersioningEvent;
import javax.swing.*;
import java.awt.Dialog;
import java.io.File;
import java.text.MessageFormat;
import java.util.*;
import org.netbeans.modules.versioning.util.DialogBoundsPreserver;

/**
 * Represents the "Commit" main/popup action and provides programmatic Commit action upon any context.
 *
 * @author Maros Sandor
 */
public class CommitAction extends AbstractSystemAction {
    
    static final String RECENT_COMMIT_MESSAGES = "commitAction.commitMessage";  // NOI18N

    public CommitAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    protected String getBaseName(Node [] activatedNodes) {
        return "CTL_MenuItem_Commit";  // NOI18N
    }

    protected boolean enable(Node[] nodes) {
        return CvsVersioningSystem.getInstance().getFileTableModel(Utils.getCurrentContext(nodes), FileInformation.STATUS_LOCAL_CHANGE).getNodes().length > 0;
    }

    /**
     * Shows commit dialog UI and handles selected option.
     */
    public static void invokeCommit(String contentTitle, Context context, String runningName) {
        ResourceBundle loc = NbBundle.getBundle(CommitAction.class);
        if (CvsVersioningSystem.getInstance().getFileTableModel(context, FileInformation.STATUS_LOCAL_CHANGE).getNodes().length == 0) {
            JOptionPane.showMessageDialog(null, loc.getString("MSG_NoFilesToCommit_Prompt"), 
                                          loc.getString("MSG_NoFilesToCommit_Title"), JOptionPane.INFORMATION_MESSAGE);
            return;   
        }
        
        CommitCommand cmd = new CommitCommand();
        cmd.setDisplayName(NbBundle.getMessage(CommitAction.class, "BK0001"));  // NOI18N
        
        final CommitSettings settings = new CommitSettings();
        final JButton commit = new JButton(loc.getString("CTL_CommitForm_Action_Commit"));
        commit.setToolTipText(NbBundle.getMessage(CommitAction.class, "TT_CommitDialog_Action_Commit"));  // NOI18N
        commit.setEnabled(false);
        JButton cancel = new JButton(loc.getString("CTL_CommitForm_Action_Cancel"));
        cancel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CommitAction.class, "ACSD_CommitDialog_Action_Cancel"));  // NOI18N
        DialogDescriptor descriptor = new DialogDescriptor(
                settings, 
                MessageFormat.format(loc.getString("CTL_CommitDialog_Title"), contentTitle),
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
        settings.putClientProperty("contentTitle", contentTitle);  // NOI18N
        settings.putClientProperty("DialogDescriptor", descriptor); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.addWindowListener(new DialogBoundsPreserver(CvsModuleConfig.getDefault().getPreferences(), "svn.commit.dialog"));  // NOI18N
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CommitAction.class, "ACSD_CommitDialog"));  // NOI18N
        dialog.setVisible(true);
        final String message = settings.getCommitMessage().trim();
        if (descriptor.getValue() != commit && !message.isEmpty()) {
            CvsModuleConfig.getDefault().setLastCanceledCommitMessage(message);
        }
        if (descriptor.getValue() != commit) return;

        CvsModuleConfig.getDefault().setLastCanceledCommitMessage(""); //NOI18N
        saveExclusions(settings);

        cmd.setMessage(message);
        org.netbeans.modules.versioning.util.Utils.insert(
                CvsModuleConfig.getDefault().getPreferences(),
                RECENT_COMMIT_MESSAGES,
                message != null ? message.trim() : "",
                20);
        if (runningName == null) {
            runningName = NbBundle.getMessage(CommitAction.class, "BK0002");  // NOI18N
        }
        ExecutorGroup group = new ExecutorGroup(runningName);
        addCommit(group, settings);
        group.execute();
    }
    
    private static void saveExclusions(CommitSettings settings) {
        CommitSettings.CommitFile [] files = settings.getCommitFiles();
        for (int i = 0; i < files.length; i++) {
            CommitSettings.CommitFile file = files[i];
            if (file.getOptions() == CommitOptions.EXCLUDE) {
                CvsModuleConfig.getDefault().addExclusion(file.getNode().getFile());
            } else {
                CvsModuleConfig.getDefault().removeExclusion(file.getNode().getFile());
            }
        }
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
        Set<String> stickyTags = new HashSet<String>();
        boolean conflicts = false;
        int filesToCommit = 0;
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
                settings.setErrorLabel("<html><font color=\"#002080\">" + msg + "</font></html>");  // NOI18N
                conflicts = true;
            } else {
                filesToCommit++;                
            }
            stickyTags.add(Utils.getSticky(file.getNode().getFile()));
        }
        
        if (stickyTags.size() > 1) {
            settings.setColumns(new String [] { CommitSettings.COLUMN_NAME_COMMIT, CommitSettings.COLUMN_NAME_NAME, CommitSettings.COLUMN_NAME_STICKY, CommitSettings.COLUMN_NAME_STATUS,
                                                CommitSettings.COLUMN_NAME_ACTION, CommitSettings.COLUMN_NAME_PATH });
        } else {
            settings.setColumns(new String [] { CommitSettings.COLUMN_NAME_COMMIT, CommitSettings.COLUMN_NAME_NAME, CommitSettings.COLUMN_NAME_STATUS,
                                                CommitSettings.COLUMN_NAME_ACTION, CommitSettings.COLUMN_NAME_PATH });
        }
        
        String contentTitle = (String) settings.getClientProperty("contentTitle"); // NOI18N
        DialogDescriptor dd = (DialogDescriptor) settings.getClientProperty("DialogDescriptor"); // NOI18N
        String errorLabel;
        if (stickyTags.size() <= 1) {
            String stickyTag = stickyTags.size() == 0 ? null : (String) stickyTags.iterator().next(); 
            if (stickyTag == null) {
                dd.setTitle(MessageFormat.format(loc.getString("CTL_CommitDialog_Title"), contentTitle));
                errorLabel = ""; // NOI18N
            } else {
                dd.setTitle(MessageFormat.format(loc.getString("CTL_CommitDialog_Title_Branch"), contentTitle, stickyTag));
                String msg = MessageFormat.format(loc.getString("MSG_CommitForm_InfoBranch"), stickyTag);
                errorLabel = "<html><font color=\"#002080\">" + msg + "</font></html>"; // NOI18N
            }
        } else {
            dd.setTitle(MessageFormat.format(loc.getString("CTL_CommitDialog_Title_Branches"), contentTitle));
            String msg = loc.getString("MSG_CommitForm_ErrorMultipleBranches");
            errorLabel = "<html><font color=\"#CC0000\">" + msg + "</font></html>"; // NOI18N
        }
        if (!conflicts) {
            settings.setErrorLabel(errorLabel);
            commit.setEnabled(filesToCommit > 0);
        }
    }

    public void performCvsAction(Node[] nodes) {
        invokeCommit(getContextDisplayName(nodes), getContext(nodes), getRunningName(nodes));
    }

    protected boolean asynchronous() {
        return false;
    }
    
    /**
     * Prepares add/commit actions based on settings in the Commit dialog.
     *
     * @param group where commit is added
     * @param settings user settings
     */
    public static void addCommit(ExecutorGroup group, CommitSettings settings) {
        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
        CommitSettings.CommitFile [] files = settings.getCommitFiles();
        List<File> commitBucket = new ArrayList<File>();
        List<File> addDefaultBucket = new ArrayList<File>();
        List<File> addKkvBucket = new ArrayList<File>();
        List<File> addKkvlBucket = new ArrayList<File>();
        List<File> addKkBucket = new ArrayList<File>();
        List<File> addKoBucket = new ArrayList<File>();
        List<File> addKbBucket = new ArrayList<File>();
        List<File> addKvBucket = new ArrayList<File>();
        List<File> removeBucket = new ArrayList<File>();
                
        String ks = System.getProperty("cvs.keywordsubstitution", "");
        for (int i = 0; i < files.length; i++) {
            CommitSettings.CommitFile file = files[i];
            if (file.getOptions() == CommitOptions.EXCLUDE) continue;
            if (file.getOptions() == CommitOptions.ADD_TEXT) {
                List<File> bucket;
                if(ks.equals("kkv")) {
                    bucket = addKkvBucket;                
                } else if (ks.equals("kkvl")) {
                    bucket = addKkvlBucket;
                } else if (ks.equals("kk")) {
                    bucket = addKkBucket;
                } else if (ks.equals("ko")) {
                    bucket = addKoBucket;
                } else if (ks.equals("kb")) {
                    bucket = addKbBucket;
                } else if (ks.equals("kv")) {
                    bucket = addKvBucket;
                } else {
                    bucket = addDefaultBucket;   
                }         
                bucket.add(file.getNode().getFile());
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
        group.addExecutors(createAdd(addDefaultBucket, null));
        group.addExecutors(createAdd(addKkvBucket, KeywordSubstitutionOptions.DEFAULT));
        group.addExecutors(createAdd(addKkvlBucket, KeywordSubstitutionOptions.DEFAULT_LOCKER));
        group.addExecutors(createAdd(addKkBucket, KeywordSubstitutionOptions.ONLY_KEYWORDS));
        group.addExecutors(createAdd(addKoBucket, KeywordSubstitutionOptions.OLD_VALUES));
        group.addExecutors(createAdd(addKbBucket, KeywordSubstitutionOptions.BINARY));
        group.addExecutors(createAdd(addKvBucket, KeywordSubstitutionOptions.ONLY_VALUES));
        group.addExecutors(createRemove(removeBucket));
        group.addExecutors(createCommit(commitBucket, settings.getCommitMessage()));
    }

    private static ExecutorSupport[] createCommit(List<File> bucket, String message) {
        if (bucket.size() == 0) return null;
        CommitCommand cmd = new CommitCommand();
        cmd.setFiles((File []) bucket.toArray(new File[bucket.size()]));
        cmd.setMessage(org.netbeans.modules.versioning.util.Utils.wordWrap(message, CvsModuleConfig.getDefault().getWrapCommitMessagelength()));
        return CommitExecutor.splitCommand(cmd, CvsVersioningSystem.getInstance(), null);
    }

    private static ExecutorSupport[] createRemove(List<File> bucket) {
        if (bucket.size() == 0) return null;
        RemoveCommand cmd = new RemoveCommand();
        cmd.setFiles((File []) bucket.toArray(new File[bucket.size()]));
        return RemoveExecutor.splitCommand(cmd, CvsVersioningSystem.getInstance(), null);
    }

    private static ExecutorSupport[] createAdd(List<File> bucket, KeywordSubstitutionOptions option) {
        if (bucket.size() == 0) return null;
        AddCommand cmd = new AddCommand();
        cmd.setFiles((File []) bucket.toArray(new File[bucket.size()]));
        cmd.setKeywordSubst(option);
        return AddExecutor.splitCommand(cmd, CvsVersioningSystem.getInstance(), null);

    }
    @Override
    protected String iconResource() {
        return "org/netbeans/modules/versioning/system/cvss/resources/icons/commit.png"; // NOI18N
    }
}
