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

package org.netbeans.modules.git.ui.fetch;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitPullResult;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.GitAction;
import org.netbeans.modules.git.ui.merge.MergeRevisionAction;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.fetch.PullAction", category = "Git")
@ActionRegistration(displayName = "#LBL_PullAction_Name")
public class PullAction extends GetRemoteChangesAction {
    
    private static final Logger LOG = Logger.getLogger(PullAction.class.getName());

    @Override
    protected void performAction (File repository, File[] roots, VCSContext context) {
        pull(repository);
    }
    
    private void pull (File repository) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        info.refreshRemotes();
        Map<String, GitRemoteConfig> remotes = info.getRemotes();
        PullWizard wiz = new PullWizard(repository, remotes);
        if (wiz.show()) {
            Utils.logVCSExternalRepository("GIT", wiz.getFetchUri()); //NOI18N
            pull(repository, wiz.getFetchUri(), wiz.getFetchRefSpecs(), wiz.getBranchToMerge(), wiz.getRemoteToPersist());
        }
    }
    
    public void pull (File repository, final String target, final List<String> fetchRefSpecs, final String branchToMerge, final String remoteNameToUpdate) {
        GitProgressSupport supp = new GitProgressSupport() {
            @Override
            protected void perform () {
                File repository = getRepositoryRoot();
                LOG.log(Level.FINE, "Pulling {0}/{1} from {2}", new Object[] { fetchRefSpecs, branchToMerge, target }); //NOI18N
                try {
                    boolean cont;
                    GitClient client = getClient();
                    if (remoteNameToUpdate != null) {
                        GitRemoteConfig config = client.getRemote(remoteNameToUpdate, getProgressMonitor());
                        if (isCanceled()) {
                            return;
                        }
                        config = FetchAction.prepareConfig(config, remoteNameToUpdate, target, fetchRefSpecs);
                        client.setRemote(config, getProgressMonitor());
                        if (isCanceled()) {
                            return;
                        }
                    }
                    MergeRevisionAction.MergeResultProcessor mrp = new MergeRevisionAction.MergeResultProcessor(client, repository, branchToMerge, getLogger(), getProgressMonitor());
                    do {
                        cont = false;
                        try {
                            GitPullResult result = client.pull(target, fetchRefSpecs, branchToMerge, getProgressMonitor());
                            log(result.getFetchResult(), getLogger());
                            mrp.processResult(result.getMergeResult());
                        } catch (GitException.CheckoutConflictException ex) {
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.log(Level.FINE, "Local modifications in WT during merge: {0} - {1}", new Object[] { repository, Arrays.asList(ex.getConflicts()) }); //NOI18N
                            }
                            cont = mrp.resolveLocalChanges(ex.getConflicts());
                        }
                    } while (cont);
                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, true);
                } finally {
                    setDisplayName(NbBundle.getMessage(GitAction.class, "LBL_Progress.RefreshingStatuses")); //NOI18N
                    Git.getInstance().getFileStatusCache().refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repository, Git.getInstance().getSeenRoots(repository)));
                    GitUtils.headChanged(repository);
                }
            }
        };
        supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(PullAction.class, "LBL_PullAction.progressName")); //NOI18N
    }
}
