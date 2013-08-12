/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.ui.push;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitPushResult;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitTransportUpdate;
import org.netbeans.libs.git.GitTransportUpdate.Type;
import org.netbeans.libs.git.SearchCriteria;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.ui.output.OutputLogger;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.hooks.GitHook;
import org.netbeans.modules.versioning.hooks.GitHookContext;
import org.netbeans.modules.versioning.hooks.VCSHooks;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.push.PushAction", category = "Git")
@ActionRegistration(displayName = "#LBL_PushAction_Name")
@NbBundle.Messages({"#PushAction", "LBL_PushAction_Name=Pus&h..."})
public class PushAction extends SingleRepositoryAction {
    
    private static final String ICON_RESOURCE = "org/netbeans/modules/git/resources/icons/push-setting.png"; //NOI18N
    
    public PushAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }
    
    private static final Logger LOG = Logger.getLogger(PushAction.class.getName());

    @Override
    protected void performAction (File repository, File[] roots, VCSContext context) {
        push(repository);
    }
    
    private void push (final File repository) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        try {
            info.refreshRemotes();
        } catch (GitException ex) {
            GitClientExceptionHandler.notifyException(ex, true);
        }
        final Map<String, GitRemoteConfig> remotes = info.getRemotes();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run () {
                PushWizard wiz = new PushWizard(repository, remotes);
                if (wiz.show()) {
                    Utils.logVCSExternalRepository("GIT", wiz.getPushUri()); //NOI18N
                    push(repository, wiz.getPushUri(), wiz.getPushMappings(), wiz.getFetchRefSpecs());
                }
            }
        });
    }
    
    @NbBundle.Messages({
        "# {0} - repository name", "LBL_PushAction.progressName=Pushing - {0}",
        "# {0} - branch name", "MSG_PushAction.branchDeleted=Branch {0} deleted in the local repository.",
        "# {0} - branch name", "# {1} - branch head id", "# {2} - result of the update",
        "MSG_PushAction.updates.deleteBranch=Branch Delete : {0}\n"
            + "Id            : {1}\n"
            + "Result        : {2}\n",
        "# {0} - branch name", "# {1} - branch head id", "# {2} - result of the update",
        "MSG_PushAction.updates.addBranch=Branch Add : {0}\n"
            + "Id         : {1}\n"
            + "Result     : {2}\n",
        "# {0} - branch name", "# {1} - branch old head id", "# {2} - branch new head id", "# {3} - result of the update",
        "MSG_PushAction.updates.updateBranch=Branch Update : {0}\n"
            + "Old Id        : {1}\n"
            + "New Id        : {2}\n"
            + "Result        : {3}\n"
    })
    public Task push (File repository, final String remote, final Collection<PushMapping> pushMappins, final List<String> fetchRefSpecs) {
        GitProgressSupport supp = new GitProgressSupport() {
            @Override
            protected void perform () {
                List<String> pushRefSpecs = new LinkedList<String>();
                for (PushMapping b : pushMappins) {
                    pushRefSpecs.add(b.getRefSpec());
                }
                final Set<String> toDelete = new HashSet<String>();
                for(ListIterator<String> it = fetchRefSpecs.listIterator(); it.hasNext(); ) {
                    String refSpec = it.next();
                    if (refSpec.startsWith(GitUtils.REF_SPEC_DEL_PREFIX)) {
                        // branches are deleted separately
                        it.remove();
                        toDelete.add(refSpec.substring(GitUtils.REF_SPEC_DEL_PREFIX.length()));
                    }
                }
                LOG.log(Level.FINE, "Pushing {0}/{1} to {2}", new Object[] { pushRefSpecs, fetchRefSpecs, remote }); //NOI18N
                try {
                    GitClient client = getClient();
                    // init push hooks
                    Collection<GitHook> hooks = VCSHooks.getInstance().getHooks(GitHook.class);
                    beforePush(hooks, pushMappins);
                    if (isCanceled()) {
                        return;
                    }
                    for (String branch : toDelete) {
                        client.deleteBranch(branch, true, getProgressMonitor());
                        getLogger().output(Bundle.MSG_PushAction_branchDeleted(branch));
                    }
                    // push
                    GitPushResult result = client.push(remote, pushRefSpecs, fetchRefSpecs, getProgressMonitor());
                    reportRemoteConflicts(result.getRemoteRepositoryUpdates());
                    logUpdates(result.getRemoteRepositoryUpdates(), "MSG_PushAction.updates.remoteUpdates"); //NOI18N
                    logUpdates(result.getLocalRepositoryUpdates(), "MSG_PushAction.updates.localUpdates"); //NOI18N
                    if (isCanceled()) {
                        return;
                    }
                    // after-push hooks
                    setProgress(NbBundle.getMessage(PushAction.class, "MSG_PushAction.finalizing")); //NOI18N
                    afterPush(hooks, result.getRemoteRepositoryUpdates());
                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, true);
                }
            }
            
            protected void logUpdates (Map<String, GitTransportUpdate> updates, String titleBundleName) {
                OutputLogger logger = getLogger();
                logger.output(NbBundle.getMessage(PushAction.class, titleBundleName));
                if (updates.isEmpty()) {
                    logger.output(NbBundle.getMessage(PushAction.class, "MSG_PushAction.updates.noChange")); //NOI18N
                } else {
                    for (Map.Entry<String, GitTransportUpdate> e : updates.entrySet()) {
                        GitTransportUpdate update = e.getValue();
                        if (update.getType() == Type.BRANCH) {
                            if (update.getNewObjectId() == null && update.getOldObjectId() != null) {
                                // delete
                                logger.output(Bundle.MSG_PushAction_updates_deleteBranch(update.getRemoteName(),
                                        update.getOldObjectId(), update.getResult()));
                            } else if (update.getNewObjectId() != null && update.getOldObjectId() == null) {
                                // add
                                logger.output(Bundle.MSG_PushAction_updates_addBranch(update.getLocalName(),
                                        update.getNewObjectId(), update.getResult()));
                            } else {
                                logger.output(Bundle.MSG_PushAction_updates_updateBranch(update.getLocalName(),
                                        update.getOldObjectId(), update.getNewObjectId(), update.getResult()));
                            }
                        } else {
                            logger.output(NbBundle.getMessage(PushAction.class, "MSG_PushAction.updates.updateTag", new Object[] { //NOI18N
                                update.getLocalName(), 
                                update.getResult(),
                            }));
                        }
                    }
                }
            }

            private void beforePush (Collection<GitHook> hooks, Collection<PushMapping> pushMapping) throws GitException {
                if (hooks.size() > 0) {
                    List<GitRevisionInfo> messages = getOutgoingRevisions(pushMapping);
                    if(!isCanceled() && !messages.isEmpty()) {
                        GitHookContext context = initializeHookContext(messages);
                        for (GitHook gitHook : hooks) {
                            try {
                                // XXX handle returned context
                                gitHook.beforePush(context);
                            } catch (IOException ex) {
                                // XXX handle veto
                            }
                        }
                    }
                }
            }

            private List<GitRevisionInfo> getOutgoingRevisions (Collection<PushMapping> pushMappings) throws GitException {
                List<GitRevisionInfo> revisionList = new LinkedList<GitRevisionInfo>();
                Set<String> visitedRevisions = new HashSet<String>();
                GitClient client = Git.getInstance().getClient(getRepositoryRoot()); // do not use progresssupport's client, that one logs into output
                try {
                    for (PushMapping mapping : pushMappings) {
                        if (mapping instanceof PushMapping.PushBranchMapping) {
                            PushMapping.PushBranchMapping branchMapping = (PushMapping.PushBranchMapping) mapping;
                            String remoteRevisionId = branchMapping.getRemoteRepositoryBranchHeadId();
                            String localRevisionId = branchMapping.getLocalRepositoryBranchHeadId();
                            revisionList.addAll(addRevisions(client, visitedRevisions, remoteRevisionId, localRevisionId));
                        }
                        if (isCanceled()) {
                            break;
                        }
                    }
                } finally {
                    if (client != null) {
                        client.release();
                    }
                }
                return revisionList;
            }

            private List<GitRevisionInfo> getPushedRevisions (Map<String, GitTransportUpdate> remoteRepositoryUpdates) throws GitException {
                List<GitRevisionInfo> revisionList = new LinkedList<GitRevisionInfo>();
                Set<String> visitedRevisions = new HashSet<String>();
                GitClient client = Git.getInstance().getClient(getRepositoryRoot()); // do not use progresssupport's client, that one logs into output
                try {
                    for (Map.Entry<String, GitTransportUpdate> update : remoteRepositoryUpdates.entrySet()) {
                        String remoteRevisionId = update.getValue().getOldObjectId();
                        String localRevisionId = update.getValue().getNewObjectId();
                        revisionList.addAll(addRevisions(client, visitedRevisions, remoteRevisionId, localRevisionId));
                        if (isCanceled()) {
                            break;
                        }
                    }
                } finally {
                    if (client != null) {
                        client.release();
                    }
                }
                return revisionList;
            }

            private void afterPush (Collection<GitHook> hooks, Map<String, GitTransportUpdate> remoteRepositoryUpdates) throws GitException {
                if (hooks.size() > 0) {
                    List<GitRevisionInfo> messages = getPushedRevisions(remoteRepositoryUpdates);
                    if(!isCanceled() && !messages.isEmpty()) {
                        GitHookContext context = initializeHookContext(messages);
                        for (GitHook gitHook : hooks) {
                            gitHook.afterPush(context);
                        }
                    }
                }
            }

            private GitHookContext initializeHookContext (List<GitRevisionInfo> messages) {
                List<GitHookContext.LogEntry> entries = new LinkedList<GitHookContext.LogEntry>();
                for (GitRevisionInfo message : messages) {
                    entries.add(new GitHookContext.LogEntry(
                            message.getFullMessage(),
                            message.getAuthor().toString(),
                            message.getRevision(),
                            new Date(message.getCommitTime())));
                }
                GitHookContext context = new GitHookContext(new File[] { getRepositoryRoot() }, null, entries.toArray(new GitHookContext.LogEntry[entries.size()]));
                return context;
            }

            private List<GitRevisionInfo> addRevisions (GitClient client, Set<String> visitedRevisions, String remoteRevisionId, String localRevisionId) throws GitException {
                List<GitRevisionInfo> list = new LinkedList<GitRevisionInfo>();
                SearchCriteria crit = null;
                if (localRevisionId == null) {
                    // delete branch, do nothing
                } else if (remoteRevisionId == null) {
                    // adding branch, add all revisions in the branch
                    crit = new SearchCriteria();
                    crit.setRevisionTo(localRevisionId);
                } else {
                    // updating branch, add all new revisions
                    crit = new SearchCriteria();
                    crit.setRevisionFrom(remoteRevisionId);
                    crit.setRevisionTo(localRevisionId);
                }
                if (crit != null) {
                    final GitProgressSupport supp = this;
                    try {
                        GitRevisionInfo[] revisions = client.log(crit, new ProgressMonitor() {
                            @Override
                            public boolean isCanceled () {
                                return supp.isCanceled();
                            }

                            @Override
                            public void started (String command) {}

                            @Override
                            public void finished () {}

                            @Override
                            public void preparationsFailed (String message) {}

                            @Override
                            public void notifyError (String message) {}

                            @Override
                            public void notifyWarning (String message) {}
                        });
                        for (GitRevisionInfo rev : revisions) {
                            boolean firstTime = visitedRevisions.add(rev.getRevision());
                            if (firstTime) {
                                list.add(rev);
                            }
                        }
                    } catch (GitException.MissingObjectException ex) {
                        if (remoteRevisionId != null && remoteRevisionId.equals(ex.getObjectName())) {
                            // probably not a fast-forward push, what should we do next?
                            // list all revisions? that could take a loooot of time and memory
                            // get a common parent? and how?
                            // for now let's do... nothing
                        } else {
                            throw ex;
                        }
                    }
                }
                return list;
            }

            private void reportRemoteConflicts (Map<String, GitTransportUpdate> updates) {
                List<GitTransportUpdate> errors = new LinkedList<GitTransportUpdate>();
                List<GitTransportUpdate> conflicts = new LinkedList<GitTransportUpdate>();
                for (Map.Entry<String, GitTransportUpdate> e : updates.entrySet()) {
                    GitTransportUpdate update = e.getValue();
                    switch (update.getResult()){
                        case OK:
                        case UP_TO_DATE:
                            break;
                        case REJECTED_NONFASTFORWARD:
                            conflicts.add(update);
                            break;
                        default:
                            errors.add(update);
                    }
                }
                String message = null;
                if (!errors.isEmpty()) {
                    message = NbBundle.getMessage(PushAction.class, "MSG_PushAction.report.errors"); //NOI18N
                } else if (!conflicts.isEmpty()) {
                    message = NbBundle.getMessage(PushAction.class, "MSG_PushAction.report.conflicts"); //NOI18N
                }
                if (message != null) {
                    JButton outputBtn = new JButton();
                    Mnemonics.setLocalizedText(outputBtn, NbBundle.getMessage(PushAction.class, "CTL_PushAction.report.outputButton.text")); //NOI18N
                    outputBtn.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PushAction.class, "CTL_PushAction.report.outputButton.desc")); //NOI18N
                    Object o = DialogDisplayer.getDefault().notify(new NotifyDescriptor(message, NbBundle.getMessage(PushAction.class, "LBL_PushAction.report.error.title"), //NOI18N
                            NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.ERROR_MESSAGE, new Object[] { outputBtn, NotifyDescriptor.CANCEL_OPTION }, outputBtn));
                    if (o == outputBtn) {
                        getLogger().getOpenOutputAction().actionPerformed(new ActionEvent(PushAction.this, ActionEvent.ACTION_PERFORMED, null));
                    }
                }
            }
        };
        return supp.start(Git.getInstance().getRequestProcessor(repository), repository, Bundle.LBL_PushAction_progressName(repository.getName()));
    }
}
