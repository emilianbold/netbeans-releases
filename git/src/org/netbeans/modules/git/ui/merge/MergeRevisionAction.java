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

package org.netbeans.modules.git.ui.merge;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JButton;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitMergeResult;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.GitAction;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.ui.conflicts.ResolveConflictsAction;
import org.netbeans.modules.git.ui.conflicts.ResolveConflictsExecutor;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.merge.MergeRevisionAction", category = "Git")
@ActionRegistration(displayName = "#LBL_MergeRevisionAction_Name")
public class MergeRevisionAction extends SingleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(MergeRevisionAction.class.getName());

    @Override
    protected void performAction (File repository, File[] roots, VCSContext context) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        mergeRevision(repository, info.getActiveBranch().getName().equals(GitBranch.NO_BRANCH) ? GitUtils.HEAD : info.getActiveBranch().getName());
    }

    public void mergeRevision (final File repository, String preselectedRevision) {
        final MergeRevision mergeRevision = new MergeRevision(repository, new File[0], preselectedRevision);
        if (mergeRevision.show()) {
            GitProgressSupport supp = new GitProgressSupport() {
                private String revision;
                
                @Override
                protected void perform () {
                    try {
                        GitClient client = getClient();
                        client.addNotificationListener(new DefaultFileListener(new File[] { repository }));
                        revision = mergeRevision.getRevision();
                        LOG.log(Level.FINE, "Merging revision {0} into HEAD", revision); //NOI18N
                        GitMergeResult result = client.merge(revision, this);
                        processResult(result);
                    } catch (GitException ex) {
                        GitClientExceptionHandler.notifyException(ex, true);
                    } finally {
                        setDisplayName(NbBundle.getMessage(GitAction.class, "LBL_Progress.RefreshingStatuses")); //NOI18N
                        Git.getInstance().getFileStatusCache().refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repository, Git.getInstance().getSeenRoots(repository)));
                        GitUtils.headChanged(repository);
                    }
                }

                private void processResult (GitMergeResult result) {
                    StringBuilder sb = new StringBuilder(NbBundle.getMessage(MergeRevisionAction.class, "MSG_MergeRevisionAction.result", result.getMergeStatus().toString())); //NOI18N
                    GitRevisionInfo info = null;
                    if (result.getNewHead() != null) {
                        try {
                            info = getClient().log(result.getNewHead(), NULL_PROGRESS_MONITOR);
                        } catch (GitException ex) {
                            GitClientExceptionHandler.notifyException(ex, true);
                        }
                    }
                    switch (result.getMergeStatus()) {
                        case ALREADY_UP_TO_DATE:
                            sb.append(NbBundle.getMessage(MergeRevisionAction.class, "MSG_MergeRevisionAction.result.alreadyUpToDate", revision)); //NOI18N
                            break;
                        case FAST_FORWARD:
                            sb.append(NbBundle.getMessage(MergeRevisionAction.class, "MSG_MergeRevisionAction.result.fastForward", revision)); //NOI18N
                            GitUtils.printInfo(sb, info);
                            break;
                        case MERGED:
                            sb.append(NbBundle.getMessage(MergeRevisionAction.class, "MSG_MergeRevisionAction.result.merged", revision)); //NOI18N
                            GitUtils.printInfo(sb, info);
                            break;
                        case CONFLICTING:
                            sb.append(NbBundle.getMessage(MergeRevisionAction.class, "MSG_MergeRevisionAction.result.conflict", revision)); //NOI18N
                            printConflicts(sb, result.getConflicts());
                            resolveConflicts(result);
                            break;
                        case FAILED:
                            final Action openAction = getLogger().getOpenOutputAction();
                            if (openAction != null) {
                                try {
                                    EventQueue.invokeAndWait(new Runnable() {
                                        @Override
                                        public void run () {
                                            openAction.actionPerformed(new ActionEvent(MergeRevisionAction.this, ActionEvent.ACTION_PERFORMED, null));
                                        }
                                    });
                                } catch (InterruptedException ex) {
                                } catch (InvocationTargetException ex) {
                                }
                            }
                            sb.append(NbBundle.getMessage(MergeRevisionAction.class, "MSG_MergeRevisionAction.result.failedFiles", revision)); //NOI18N
                            printConflicts(sb, result.getConflicts());
                            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(
                                    NbBundle.getMessage(MergeRevisionAction.class, "MSG_MergeRevisionAction.result.failed"), NotifyDescriptor.ERROR_MESSAGE)); //NOI18N
                            break;
                        case NOT_SUPPORTED:
                            sb.append(NbBundle.getMessage(MergeRevisionAction.class, "MSG_MergeRevisionAction.result.unsupported")); //NOI18N
                            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(
                                    NbBundle.getMessage(MergeRevisionAction.class, "MSG_MergeRevisionAction.result.unsupported"), NotifyDescriptor.ERROR_MESSAGE)); //NOI18N
                            break;
                    }
                    getLogger().output(sb.toString());
                }

                private void printConflicts (StringBuilder sb, Collection<File> conflicts) {
                    for (File f : conflicts) {
                        sb.append(f.getAbsolutePath()).append('\n');
                    }
                }

                private void resolveConflicts (GitMergeResult result) {
                    JButton resolve = new JButton();
                    Mnemonics.setLocalizedText(resolve, NbBundle.getMessage(MergeRevisionAction.class, "LBL_MergeRevisionAction.resolveButton.text")); //NOI18N
                    resolve.setToolTipText(NbBundle.getMessage(MergeRevisionAction.class, "LBL_MergeRevisionAction.resolveButton.TTtext")); //NOI18N
                    JButton review = new JButton();
                    Mnemonics.setLocalizedText(review, NbBundle.getMessage(MergeRevisionAction.class, "LBL_MergeRevisionAction.reviewButton.text")); //NOI18N
                    review.setToolTipText(NbBundle.getMessage(MergeRevisionAction.class, "LBL_MergeRevisionAction.reviewButton.TTtext")); //NOI18N
                    Object o = DialogDisplayer.getDefault().notify(new NotifyDescriptor(NbBundle.getMessage(MergeRevisionAction.class, "MSG_MergeRevisionAction.resolveConflicts"), //NOI18N
                            NbBundle.getMessage(MergeRevisionAction.class, "LBL_MergeRevisionAction.resolveConflicts"), //NOI18N
                            NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE, new Object[] { resolve, review, NotifyDescriptor.CANCEL_OPTION }, resolve));
                    if (o == review) {
                        setDisplayName(NbBundle.getMessage(MergeRevisionAction.class, "LBL_MergeRevisionAction.reviewingConflicts"));
                        GitUtils.openInVersioningView(result.getConflicts(), repository, this);
                    } else if (o == resolve) {
                        GitProgressSupport supp = new ResolveConflictsExecutor(result.getConflicts().toArray(new File[result.getConflicts().size()]));
                        supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(ResolveConflictsAction.class, "MSG_PreparingMerge")); //NOI18N
                    }
                }
            };
            supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(MergeRevisionAction.class, "LBL_MergeRevisionAction.progressName"));
        }
    }

}
