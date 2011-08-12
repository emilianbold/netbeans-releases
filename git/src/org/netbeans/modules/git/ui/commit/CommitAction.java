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

import java.awt.EventQueue;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.modules.versioning.util.common.VCSCommitOptions;
import org.netbeans.modules.versioning.util.common.VCSCommitTable;
import org.netbeans.modules.versioning.util.common.VCSFileNode;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRepositoryState;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.GitUser;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.ui.commit.GitCommitPanel.GitCommitPanelMerged;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.hooks.GitHook;
import org.netbeans.modules.versioning.hooks.GitHookContext;
import org.netbeans.modules.versioning.hooks.GitHookContext.LogEntry;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.common.VCSCommitFilter;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
@ActionID(id = "org.netbeans.modules.git.ui.commit.CommitAction", category = "Git")
@ActionRegistration(displayName = "#LBL_CommitAction_Name")
public class CommitAction extends SingleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(CommitAction.class.getName());

    @Override
    protected String iconResource () {
        return "org/netbeans/modules/git/resources/icons/commit.png"; // NOI18N
    }

    @Override
    protected void performAction (final File repository, final File[] roots, final VCSContext context) {
        if (!canCommit(repository)) {
            return;
        }
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        final GitRepositoryState state = info.getRepositoryState();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                
                GitUser user = null;
                try {
                    GitClient client = Git.getInstance().getClient(repository);
                    user = client.getUser();
                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, true);
                    return;
                }
                
                GitCommitPanel panel = state == GitRepositoryState.MERGING_RESOLVED
                        ? GitCommitPanelMerged.create(roots, repository, user)
                        : GitCommitPanel.create(roots, repository, user, isFromGitView(context));
                VCSCommitTable table = panel.getCommitTable();
                boolean ok = panel.open(context, new HelpCtx(CommitAction.class));

                if (ok) {
                    final List<VCSFileNode> commitFiles = table.getCommitFiles();

                    GitModuleConfig.getDefault().setLastCanceledCommitMessage(""); //NOI18N            
                    panel.getParameters().storeCommitMessage();

                    final VCSCommitFilter selectedFilter = panel.getSelectedFilter();
                    RequestProcessor rp = Git.getInstance().getRequestProcessor(repository);
                    GitProgressSupport support = new CommitProgressSupport(panel, commitFiles, selectedFilter, state);
                    support.start(rp, repository, org.openide.util.NbBundle.getMessage(CommitAction.class, "LBL_Commit_Progress")); // NOI18N
                } else if (!panel.getParameters().getCommitMessage().isEmpty()) {
                    GitModuleConfig.getDefault().setLastCanceledCommitMessage(panel.getParameters().getCommitMessage());
                }
            }
        });
    }

    private static class CommitProgressSupport extends GitProgressSupport {
        private final GitCommitPanel panel;
        private final List<VCSFileNode> commitFiles;
        private final VCSCommitFilter selectedFilter;
        private final GitRepositoryState state;

        private CommitProgressSupport (GitCommitPanel panel, List<VCSFileNode> commitFiles, VCSCommitFilter selectedFilter, GitRepositoryState state) {
            this.panel = panel;
            this.commitFiles = commitFiles;
            this.selectedFilter = selectedFilter;
            this.state = state;
        }

        @Override
        public void perform() {
            try {
                List<File> addCandidates = new LinkedList<File>();
                List<File> deleteCandidates = new LinkedList<File>();
                List<File> commitCandidates = new LinkedList<File>();
                GitCommitParameters parameters = panel.getParameters();
                GitClient client = getClient();

                populateCandidates(addCandidates, deleteCandidates, commitCandidates);

                if (isCanceled()) {
                    return;
                }

                String message = parameters.getCommitMessage();
                GitUser author = parameters.getAuthor();
                GitUser commiter = parameters.getCommiter();
                Collection<GitHook> hooks = panel.getHooks();
                try {

                    outputInRed(NbBundle.getMessage(CommitAction.class, "MSG_COMMIT_TITLE")); // NOI18N
                    outputInRed(NbBundle.getMessage(CommitAction.class, "MSG_COMMIT_TITLE_SEP")); // NOI18N

                    if(addCandidates.size() > 0) {
                        client.add(addCandidates.toArray(new File[addCandidates.size()]), this);
                    }
                    if(deleteCandidates.size() > 0) {
                        client.remove(deleteCandidates.toArray(new File[deleteCandidates.size()]), false, this);
                    }

                    if(GitModuleConfig.getDefault().getSignOff() && commiter != null) {
                        message += "\nSigned-off-by:" + GitCommitParameters.getUserString(commiter); // NOI18N
                    }
                    String origMessage = message;
                    message = beforeCommitHook(commitCandidates, hooks, message);

                    GitRevisionInfo info = commit(commitCandidates, message, author, commiter);

                    GitModuleConfig.getDefault().putRecentCommitAuthors(GitCommitParameters.getUserString(author));
                    GitModuleConfig.getDefault().putRecentCommiter(GitCommitParameters.getUserString(commiter));

                    afterCommitHook(commitCandidates, hooks, info, origMessage);

                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, true);
                } finally {
                    refreshFS(commitCandidates);
                    Git.getInstance().getFileStatusCache().refreshAllRoots(commitCandidates);
                    outputInRed(NbBundle.getMessage(CommitAction.class, "MSG_COMMIT_DONE")); // NOI18N
                    output(""); // NOI18N
                }
            } catch (GitException ex) {
                LOG.log(Level.WARNING, null, ex);
                return;
            }
        }
        
        private void populateCandidates (List<File> addCandidates, List<File> deleteCandidates, List<File> commitCandidates) {
            List<String> excPaths = new ArrayList<String>();
            List<String> incPaths = new ArrayList<String>();

            Iterator<VCSFileNode> it = commitFiles.iterator();
            while (it.hasNext()) {
                if (isCanceled()) {
                    return;
                }
                GitFileNode node = (GitFileNode) it.next();
                FileInformation info = node.getInformation();

                VCSCommitOptions option = node.getCommitOptions();
                File file = node.getFile();
                if (option != VCSCommitOptions.EXCLUDE) {
                    if (info.containsStatus(Status.NEW_INDEX_WORKING_TREE) 
                            || info.containsStatus(Status.MODIFIED_INDEX_WORKING_TREE) && selectedFilter == GitCommitPanel.FILTER_HEAD_VS_WORKING) {
                        addCandidates.add(file);
                    } else if (info.containsStatus(FileInformation.STATUS_REMOVED)) {
                        deleteCandidates.add(file);
                    }
                    commitCandidates.add(file);
                    incPaths.add(file.getAbsolutePath());
                } else {
                    excPaths.add(file.getAbsolutePath());
                }
            }

            if (!excPaths.isEmpty()) {
                GitModuleConfig.getDefault().addExclusionPaths(excPaths);
            }
            if (!incPaths.isEmpty()) {
                GitModuleConfig.getDefault().removeExclusionPaths(incPaths);
            }
        }

        private String beforeCommitHook (List<File> commitCandidates, Collection<GitHook> hooks, String message) {
            if(hooks.isEmpty()) {
                return message;
            }
            File[] hookFiles = commitCandidates.toArray(new File[commitCandidates.size()]);
            for (GitHook hook : hooks) {
                try {
                    GitHookContext context = new GitHookContext(hookFiles, message, new GitHookContext.LogEntry[] {});
                    context = hook.beforeCommit(context);

                    // XXX handle returned context - warning, ...
                    if(context != null && context.getMessage() != null && !context.getMessage().isEmpty()) {
                        // use message for next hook
                        message = context.getMessage();
                    }
                } catch (IOException ex) {
                    // XXX handle veto
                }
            }
            return message;
        }

        private void afterCommitHook(List<File> commitCandidates, Collection<GitHook> hooks, GitRevisionInfo info, String origMessage) {
            if(hooks.isEmpty()) {
                return;
            }
            File[] hookFiles = commitCandidates.toArray(new File[commitCandidates.size()]);
            LogEntry logEntry = new LogEntry(info.getFullMessage(),
                    info.getAuthor().getName(),
                    info.getRevision(),
                    new Date(info.getCommitTime()));

            GitHookContext context = new GitHookContext(hookFiles, origMessage, new LogEntry[] {logEntry});
            for (GitHook hook : hooks) {
                hook.afterCommit(context);
            }
        }

        private GitRevisionInfo commit (List<File> commitCandidates, String message, GitUser author, GitUser commiter) throws GitException {
            try {
                GitRevisionInfo info = getClient().commit(
                        state == GitRepositoryState.MERGING_RESOLVED ? new File[0] : commitCandidates.toArray(new File[commitCandidates.size()]),
                        message, author, commiter, this);
                printInfo(info);
                return info;
            } catch (GitException ex) {
                throw ex;
            }
        }

        private void printInfo (GitRevisionInfo info) {
            StringBuilder sb = new StringBuilder('\n');
            GitUtils.printInfo(sb, info);
            getLogger().output(sb.toString());
        }
    }    

    private static void refreshFS (final Collection<File> filesToRefresh) {
        Git.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                FileUtil.refreshFor(filesToRefresh.toArray(new File[filesToRefresh.size()]));
            }
        }, 100);
    }

    private boolean canCommit (File repository) {
        boolean commitPermitted = true;
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        GitRepositoryState state = info.getRepositoryState();
        if (!state.canCommit()) {
            commitPermitted = false;
            Map<File, GitStatus> conflicts = Collections.emptyMap();
            if (state.equals(GitRepositoryState.MERGING)) {
                try {
                    GitClient client = Git.getInstance().getClient(repository);
                    conflicts = client.getConflicts(new File[] { repository }, ProgressMonitor.NULL_PROGRESS_MONITOR);
                } catch (GitException ex) {
                    LOG.log(Level.INFO, null, ex);
                }
            }
            NotifyDescriptor nd;
            if (conflicts.isEmpty()) {
                nd = new NotifyDescriptor.Confirmation(NbBundle.getMessage(CommitAction.class, "LBL_CommitAction_CommitNotAllowed_State", state.toString()), //NOI18N
                        NbBundle.getMessage(CommitAction.class, "LBL_CommitAction_CannotCommit"), NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE); //NOI18N
            } else {
                nd = new NotifyDescriptor.Confirmation(NbBundle.getMessage(CommitAction.class, "LBL_CommitAction_CommitNotAllowed_Conflicts"), //NOI18N
                        NbBundle.getMessage(CommitAction.class, "LBL_CommitAction_CannotCommit"), NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE); //NOI18N
            }
            Object retval = DialogDisplayer.getDefault().notify(nd);
            if (retval == NotifyDescriptor.YES_OPTION) {
                GitUtils.openInVersioningView(conflicts.keySet(), repository, ProgressMonitor.NULL_PROGRESS_MONITOR);
            }
        }
        return commitPermitted;
    }

    protected boolean isFromGitView (VCSContext context) {
        return GitUtils.isFromInternalView(context);
    }

    public static class GitViewCommitAction extends CommitAction {
        @Override
        protected boolean isFromGitView(VCSContext context) {
            return true;
        }
    }

}
