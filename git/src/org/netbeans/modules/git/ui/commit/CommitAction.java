/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.ui.commit;

import org.netbeans.modules.versioning.util.common.VCSFileNode;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.FileStatusCache;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.hooks.HgHook;
import org.netbeans.modules.versioning.hooks.HgHookContext;
import org.netbeans.modules.versioning.hooks.VCSHooks;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.DialogBoundsPreserver;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.common.CommitOptions;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class CommitAction extends SingleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(CommitAction.class.getName());

    @Override
    protected void performAction (File repository, File[] roots, VCSContext context) {
        commit(repository, roots, context);
    }

    public static void commit(File repository, File[] roots, VCSContext context) {
        // show commit dialog
        final CommitPanel panel = new CommitPanel();
        String contentTitle = Utils.getContextDisplayName(context);

        final Collection<HgHook> hooks = VCSHooks.getInstance().getHooks(HgHook.class);

        panel.setHooks(hooks, new HgHookContext(context.getRootFiles().toArray( new File[context.getRootFiles().size()]), null, new HgHookContext.LogEntry[] {}));
        final CommitTable data = new CommitTable(panel.filesLabel, CommitTable.COMMIT_COLUMNS, new String[] {CommitTableModel.COLUMN_NAME_PATH });

        panel.setCommitTable(data);
        data.setCommitPanel(panel);

        final JButton commitButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(commitButton, org.openide.util.NbBundle.getMessage(CommitAction.class, "CTL_Commit_Action_Commit"));
        commitButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CommitAction.class, "ACSN_Commit_Action_Commit"));
        commitButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CommitAction.class, "ACSD_Commit_Action_Commit"));
        final JButton cancelButton = new JButton(org.openide.util.NbBundle.getMessage(CommitAction.class, "CTL_Commit_Action_Cancel")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(CommitAction.class, "CTL_Commit_Action_Cancel"));
        cancelButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CommitAction.class, "ACSN_Commit_Action_Cancel"));
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CommitAction.class, "ACSD_Commit_Action_Cancel"));

        final DialogDescriptor dd = new DialogDescriptor(panel,
              org.openide.util.NbBundle.getMessage(CommitAction.class, "CTL_CommitDialog_Title", contentTitle), // NOI18N
              true,
              new Object[] {commitButton, cancelButton},
              commitButton,
              DialogDescriptor.DEFAULT_ALIGN,
              new HelpCtx(CommitAction.class),
              null);
        ActionListener al;
        dd.setButtonListener(al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // XXX
//                dd.setClosingOptions(new Object[] {commitButton, cancelButton});
//                SaveCookie[] saveCookies = panel.getSaveCookies();
//                if (cancelButton == e.getSource()) {
//                    if (saveCookies.length > 0) {
//                        if (SaveBeforeClosingDiffConfirmation.allSaved(saveCookies) || !panel.isShowing()) {
//                            EditorCookie[] editorCookies = panel.getEditorCookies();
//                            for (EditorCookie cookie : editorCookies) {
//                                cookie.open();
//                            }
//                        } else {
//                            dd.setClosingOptions(new Object[0]);
//                        }
//                    }
//                    dd.setValue(cancelButton);
//                } else if (commitButton == e.getSource()) {
//                    if (saveCookies.length > 0 && !SaveBeforeCommitConfirmation.allSaved(saveCookies)) {
//                        dd.setClosingOptions(new Object[0]);
//                    } else if (!panel.canCommit()) {
//                        dd.setClosingOptions(new Object[0]);
//                    }
//                    dd.setValue(commitButton);
//                }
            }
        });
        computeNodes(data, panel, roots, repository, cancelButton);
        commitButton.setEnabled(false);
        panel.addVersioningListener(new VersioningListener() {
            @Override
            public void versioningEvent(VersioningEvent event) {
//                refreshCommitDialog(panel, data, commitButton);
            }
        });
        data.getTableModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
//                refreshCommitDialog(panel, data, commitButton);
            }
        });
        commitButton.setEnabled(containsCommitable(data));

        panel.putClientProperty("contentTitle", contentTitle);  // NOI18N
        panel.putClientProperty("DialogDescriptor", dd); // NOI18N
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);

        dialog.addWindowListener(new DialogBoundsPreserver(GitModuleConfig.getDefault().getPreferences(), "git.commit.dialog")); // NOI18N
        dialog.pack();
        dialog.setVisible(true);

        final String message = panel.getCommitMessage().trim();
        if (dd.getValue() != commitButton && !message.isEmpty()) {
//      XXX      GitModuleConfig.getDefault().setLastCanceledCommitMessage(message);
        }
        if (dd.getValue() == DialogDescriptor.CLOSED_OPTION) {
            al.actionPerformed(new ActionEvent(cancelButton, ActionEvent.ACTION_PERFORMED, null));
        } else if (dd.getValue() == commitButton) {
// XXX
//            final Map<VCSFileNode, CommitOptions> commitFiles = data.getCommitFiles();
//            final Map<File, Set<File>> rootFiles = HgUtils.sortUnderRepository(context, true);
//            XXX GitModuleConfig.getDefault().setLastCanceledCommitMessage(""); //NOI18N
//             org.netbeans.modules.versioning.util.Utils.insert(GitModuleConfig.getDefault().getPreferences(), RECENT_COMMIT_MESSAGES, message.trim(), 20);
//            RequestProcessor rp = Git.getInstance().getRequestProcessor(repository);
//            GitProgressSupport support = new GitProgressSupport() {
//                @Override
//                public void perform() {
//                    OutputLogger logger = getLogger();
//                    performCommit(message, commitFiles, rootFiles, this, logger, hooks);
//                }
//            };
//            support.start(rp, repository, org.openide.util.NbBundle.getMessage(CommitAction.class, "LBL_Commit_Progress")); // NOI18N
        }
    }

    private static void computeNodes(final CommitTable table, final CommitPanel panel, final File[] roots, final File repository, JButton cancel) {
        RequestProcessor rp = Git.getInstance().getRequestProcessor(repository);
        final GitProgressSupport support = new GitProgressSupport( /*, cancel*/) {
            @Override
            public void perform() {
                try {
                    panel.progressPanel.setVisible(true);
                    // Ensure that cache is uptodate
                    FileStatusCache cache = Git.getInstance().getFileStatusCache();
                    cache.refreshAllRoots(roots);

                    File[][] split = Utils.splitFlatOthers(roots);
                    List<File> fileList = new ArrayList<File>();
                    for (int c = 0; c < split.length; c++) {
                        File[] splitRoots = split[c];
                        boolean recursive = c == 1;
                        if (recursive) {
//                            Set<File> repositories = HgUtils.getRepositoryRoots(ctx);
                            File[] files = cache.listFiles(splitRoots, FileInformation.STATUS_LOCAL_CHANGES);
                            for (int i = 0; i < files.length; i++) {
                                for(int r = 0; r < splitRoots.length; r++) {
                                    if(Utils.isAncestorOrEqual(splitRoots[r], files[i]))
                                    {
                                        if(!fileList.contains(files[i])) {
                                            fileList.add(files[i]);
                                        }
                                    }
                                }
                            }
                        } else {
                            File[] files = GitUtils.flatten(splitRoots, FileInformation.STATUS_LOCAL_CHANGES);
                            for (int i= 0; i<files.length; i++) {
                                if(!fileList.contains(files[i])) {
                                    fileList.add(files[i]);
                                }
                            }
                        }
                    }
                    if(fileList.isEmpty()) {
                        return;
                    }

                    ArrayList<VCSFileNode> nodesList = new ArrayList<VCSFileNode>(fileList.size());

                    for (Iterator<File> it = fileList.iterator(); it.hasNext();) {
                        File file = it.next();
                        VCSFileNode node = new GitFileNode(repository, file);
                        nodesList.add(node);
                    }
                    final VCSFileNode[] nodes = nodesList.toArray(new VCSFileNode[fileList.size()]);
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            table.setNodes(nodes);
                        }
                    });
                } finally {
                    panel.progressPanel.setVisible(false);
                }
            }
        };
//      XXX  panel.progressPanel.add(support.getProgressComponent());
        panel.progressPanel.setVisible(true);
        support.start(rp, repository, NbBundle.getMessage(CommitAction.class, "Progress_Preparing_Commit"));
    }

    private static boolean containsCommitable(CommitTable data) {
        Map<VCSFileNode, CommitOptions> map = data.getCommitFiles();
        for(CommitOptions co : map.values()) {
            if(co != CommitOptions.EXCLUDE) {
                return true;
            }
        }
        return false;
    }
}
