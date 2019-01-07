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
package org.netbeans.modules.git.remote.ui.checkout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.progress.FileListener;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.client.GitClient;
import org.netbeans.modules.git.remote.client.GitClientExceptionHandler;
import org.netbeans.modules.git.remote.client.GitProgressSupport;
import org.netbeans.modules.git.remote.client.GitProgressSupport.DefaultFileListener;
import org.netbeans.modules.git.remote.ui.actions.GitAction;
import org.netbeans.modules.git.remote.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.remote.ui.output.OutputLogger;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 */
public abstract class AbstractCheckoutAction extends SingleRepositoryAction {
    
    public static final String PREF_KEY_RECENT_BRANCHES = "recentlySwitchedBranches"; //NOI18N
    
    protected AbstractCheckoutAction () {
        this(null);
    }

    protected AbstractCheckoutAction (String iconResource) {
        super(iconResource);
    }
    
    private static final Logger LOG = Logger.getLogger(CheckoutRevisionAction.class.getName());
    
    protected final void checkoutRevision (final VCSFileProxy repository, AbstractCheckoutRevision checkout, String progressLabelKey, HelpCtx helpCtx) {
        if (checkout.show(helpCtx)) {
            checkoutRevision(repository, checkout.getRevision(), checkout.isCreateBranchSelected() ? checkout.getBranchName() : null,
                    NbBundle.getMessage(CheckoutRevisionAction.class, progressLabelKey));
        }
    }
    
    public final void checkoutRevision (final VCSFileProxy repository, final String revisionToCheckout, final String newBranchName, String progressLabel) {
        GitProgressSupport supp = new GitProgressSupport() {

            private String revision;
            private final Collection<VCSFileProxy> notifiedFiles = new HashSet<>();

            @Override
            protected void perform () {
                Collection<VCSFileProxy> seenRoots = Git.getInstance().getSeenRoots(repository);
                final Set<String> seenPaths = new HashSet<>(GitUtils.getRelativePaths(repository, seenRoots.toArray(new VCSFileProxy[seenRoots.size()])));
                try {
                    final GitClient client = getClient();
                    revision = revisionToCheckout;
                    if (newBranchName != null) {
                        revision = newBranchName;
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE, "Creating branch: {0}:{1}", new Object[] { revision, revisionToCheckout }); //NOI18N
                        }
                        GitBranch branch = client.createBranch(revision, revisionToCheckout, getProgressMonitor());
                        log(revisionToCheckout, branch);

                    }
                    client.addNotificationListener(new FileListener() {
                        @Override
                        public void notifyFile (VCSFileProxy file, String relativePathToRoot) {
                            if (isUnderRoots(relativePathToRoot)) {
                                notifiedFiles.add(file);
                            }
                        }

                        private boolean isUnderRoots (String relativePathToRoot) {
                            boolean underRoot = seenPaths.isEmpty() || seenPaths.contains(relativePathToRoot);
                            if (!underRoot) {
                                for (String path : seenPaths) {
                                    if (relativePathToRoot.startsWith(path + "/")) {
                                        underRoot = true;
                                        break;
                                    }
                                }
                            }
                            return underRoot;
                        }
                    });
                    client.addNotificationListener(new DefaultFileListener(new VCSFileProxy[] { repository }));
                    GitUtils.runWithoutIndexing(new Callable<Void>() {

                        @Override
                        public Void call () throws Exception {
                            LOG.log(Level.FINE, "Checking out commit: {0}", revision); //NOI18N
                            boolean failOnConflict = true;
                            boolean cont = true;
                            while (cont) {
                                cont = false;
                                try {
                                    client.checkoutRevision(revision, failOnConflict, getProgressMonitor());
                                    if (!isCanceled() && isBranch(revision, client.getBranches(true, GitUtils.NULL_PROGRESS_MONITOR))) {
                                        Utils.insert(NbPreferences.forModule(AbstractCheckoutAction.class), PREF_KEY_RECENT_BRANCHES + repository.getPath(), revision, 5);
                                    }
                                } catch (GitException.CheckoutConflictException ex) {
                                    if (LOG.isLoggable(Level.FINE)) {
                                        LOG.log(Level.FINE, "Conflicts during checkout: {0} - {1}", new Object[] { repository, Arrays.asList(ex.getConflicts()) }); //NOI18N
                                    }
                                    VCSFileProxy[] conflicts = getFilesInConflict(ex.getConflicts());
                                    if (resolveConflicts(conflicts, failOnConflict)) {
                                        cont = true;
                                        failOnConflict = false;
                                    }
                                }
                            }
                            return null;
                        }
                    }, repository);
                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, true);
                } finally {
                    if (!notifiedFiles.isEmpty()) {
                        setDisplayName(NbBundle.getMessage(GitAction.class, "LBL_Progress.RefreshingStatuses")); //NOI18N
                        Git.getInstance().getFileStatusCache().refreshAllRoots(Collections.singletonMap(repository, notifiedFiles));
                        GitUtils.headChanged(repository);
                    }
                }
            }

            private boolean isBranch (String revision, Map<String, GitBranch> branches) {
                GitBranch b = branches.get(revision);
                return b != null && b.getName() != GitBranch.NO_BRANCH;
            }

            private void log (String revision, GitBranch branch) {
                OutputLogger logger = getLogger();
                logger.outputLine(NbBundle.getMessage(CheckoutRevisionAction.class, "MSG_CheckoutRevisionAction.branchCreated", new Object[] { branch.getName(), revision, branch.getId() })); //NOI18N
            }

            private boolean resolveConflicts (VCSFileProxy[] conflicts, boolean mergeAllowed) throws GitException {
                JButton merge = new JButton();
                Mnemonics.setLocalizedText(merge, NbBundle.getMessage(CheckoutRevisionAction.class, "LBL_CheckoutRevisionAction.mergeButton.text")); //NOI18N
                merge.setToolTipText(NbBundle.getMessage(CheckoutRevisionAction.class, "LBL_CheckoutRevisionAction.mergeButton.TTtext")); //NOI18N
                JButton revert = new JButton();
                Mnemonics.setLocalizedText(revert, NbBundle.getMessage(CheckoutRevisionAction.class, "LBL_CheckoutRevisionAction.revertButton.text")); //NOI18N
                revert.setToolTipText(NbBundle.getMessage(CheckoutRevisionAction.class, "LBL_CheckoutRevisionAction.revertButton.TTtext")); //NOI18N
                JButton review = new JButton();
                Mnemonics.setLocalizedText(review, NbBundle.getMessage(CheckoutRevisionAction.class, "LBL_CheckoutRevisionAction.reviewButton.text")); //NOI18N
                review.setToolTipText(NbBundle.getMessage(CheckoutRevisionAction.class, "LBL_CheckoutRevisionAction.reviewButton.TTtext")); //NOI18N
                Object initialValue;
                Object[] buttons;
                if (mergeAllowed) {
                    initialValue = merge;
                    buttons = new Object[] { merge, revert, review, NotifyDescriptor.CANCEL_OPTION };                    
                } else {
                    initialValue = review;
                    buttons = new Object[] { revert, review, NotifyDescriptor.CANCEL_OPTION };
                }
                Object o = DialogDisplayer.getDefault().notify(new NotifyDescriptor(NbBundle.getMessage(CheckoutRevisionAction.class, "MSG_CheckoutRevisionAction.checkoutConflicts"), //NOI18N
                        NbBundle.getMessage(CheckoutRevisionAction.class, "LBL_CheckoutRevisionAction.checkoutConflicts"), //NOI18N
                        NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE, buttons, initialValue));
                if (o == merge) {
                    return true;
                } else if (o == revert) {
                    GitClient client = getClient();
                    LOG.log(Level.FINE, "Checking out paths from HEAD"); //NOI18N
                    client.checkout(conflicts, GitUtils.HEAD, true, getProgressMonitor());
                    LOG.log(Level.FINE, "Cleanup new files"); //NOI18N
                    client.clean(conflicts, getProgressMonitor());
                    LOG.log(Level.FINE, "Checking out branch: {0}, second shot", revision); //NOI18N
                    client.checkoutRevision(revision, true, getProgressMonitor());
                    notifiedFiles.addAll(Arrays.asList(conflicts));
                } else if (o == review) {
                    setDisplayName(NbBundle.getMessage(GitAction.class, "LBL_Progress.RefreshingStatuses")); //NOI18N
                    GitUtils.openInVersioningView(Arrays.asList(conflicts), repository, getProgressMonitor());
                }
                return false;
            }

            private VCSFileProxy[] getFilesInConflict (String[] conflicts) {
                List<VCSFileProxy> files = new ArrayList<>(conflicts.length);
                for (String path : conflicts) {
                    files.add(VCSFileProxy.createFileProxy(repository, path));
                }
                return files.toArray(new VCSFileProxy[files.size()]);
            }
        };
        supp.start(Git.getInstance().getRequestProcessor(repository), repository, progressLabel);
    }
}
