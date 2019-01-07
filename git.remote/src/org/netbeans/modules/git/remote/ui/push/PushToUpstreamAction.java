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

package org.netbeans.modules.git.remote.ui.push;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.cli.GitRemoteConfig;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.client.GitProgressSupport;
import org.netbeans.modules.git.remote.ui.actions.MultipleRepositoryAction;
import static org.netbeans.modules.git.remote.ui.push.Bundle.LBL_PushToUpstreamAction_preparing;
import static org.netbeans.modules.git.remote.ui.push.Bundle.LBL_Push_pushToUpstreamFailed;
import static org.netbeans.modules.git.remote.ui.push.Bundle.MSG_Err_moreRemotes;
import static org.netbeans.modules.git.remote.ui.push.Bundle.MSG_Err_noBranchState;
import static org.netbeans.modules.git.remote.ui.push.Bundle.MSG_Err_noRemote;
import static org.netbeans.modules.git.remote.ui.push.Bundle.MSG_Err_noRemoteBranch;
import static org.netbeans.modules.git.remote.ui.push.Bundle.MSG_Err_noSpecs;
import static org.netbeans.modules.git.remote.ui.push.Bundle.MSG_Err_noUri;
import static org.netbeans.modules.git.remote.ui.push.Bundle.MSG_Err_unknownRemoteBranchName;
import org.netbeans.modules.git.remote.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.remote.ui.repository.RepositoryInfo.PushMode;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.actions.SystemAction;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.git.remote.ui.push.PushToUpstreamAction", category = "GitRemote")
@ActionRegistration(displayName = "#LBL_PushToUpstreamAction_Name")
@Messages("LBL_PushToUpstreamAction_Name=Pu&sh to Upstream")
public class PushToUpstreamAction extends MultipleRepositoryAction {
    
    private static final String ICON_RESOURCE = "org/netbeans/modules/git/remote/resources/icons/push.png"; //NOI18N
    
    public PushToUpstreamAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }
    
    @Override
    protected RequestProcessor.Task performAction (VCSFileProxy repository, VCSFileProxy[] roots, VCSContext context) {
        return push(repository, GitUtils.getRepositoryRoots(context));
    }
    
    @NbBundle.Messages({"LBL_Push.pushToUpstreamFailed=Push to Upstream Failed",
        "LBL_PushToUpstreamAction.preparing=Preparing Push...",
        "MSG_Err.noBranchState=You are not on a branch, push cannot continue.",
        "# {0} - local branch", "MSG_Err.unknownRemoteBranchName=Cannot guess remote branch name for {0}"
    })
    Task push (final VCSFileProxy repository, final Set<VCSFileProxy> repositories) {
        final Task[] t = new Task[1];
        GitProgressSupport supp = new GitProgressSupport.NoOutputLogging() {
            @Override
            protected void perform () {
                RepositoryInfo info = RepositoryInfo.getInstance(repository);
                info.refresh();
                GitBranch activeBranch = info.getActiveBranch();
                if (activeBranch == null) {
                    return;
                }
                String errorLabel = LBL_Push_pushToUpstreamFailed();
                if (GitBranch.NO_BRANCH.equals(activeBranch.getName())) {
                    GitUtils.notifyError(errorLabel, MSG_Err_noBranchState());
                    return;
                }
                RepositoryInfo.PushMode pushMode = info.getPushMode();
                GitBranch trackedBranch = getTrackedBranch(activeBranch, pushMode, errorLabel);
                GitRemoteConfig cfg = getRemoteConfigForActiveBranch(trackedBranch, info, errorLabel);                        
                if (cfg == null) {
                    return;
                }
                String uri = cfg.getPushUris().isEmpty() ? cfg.getUris().get(0) : cfg.getPushUris().get(0);
                List<PushMapping> pushMappings = new LinkedList<>();
                List<String> fetchSpecs = cfg.getFetchRefSpecs();
                String remoteBranchName;
                String trackedBranchId = null;
                if (trackedBranch == null) {
                    if (shallCreateNewBranch(activeBranch)) {
                        remoteBranchName = activeBranch.getName();
                    } else {
                        return;
                    }
                } else {
                    trackedBranchId = trackedBranch.getId();
                    remoteBranchName = guessRemoteBranchName(fetchSpecs, trackedBranch.getName(), cfg.getRemoteName());
                    if (remoteBranchName == null) {
                        GitUtils.notifyError(errorLabel, MSG_Err_unknownRemoteBranchName(trackedBranch.getName()));
                        return;
                    }
                }
                pushMappings.add(new PushMapping.PushBranchMapping(remoteBranchName, trackedBranchId, activeBranch, false, false));
                Utils.logVCSExternalRepository("GIT", uri); //NOI18N
                if (!isCanceled()) {
                    // Push command work wrongly if remote is URL (creates wrong config "branch.name.remote=.")
                    // Change URL to remote name (origin).
                    t[0] = SystemAction.get(PushAction.class).push(repository, cfg.getRemoteName()/*uri*/, pushMappings,
                            cfg.getFetchRefSpecs(), null, repositories, true);
                }
            }
        };
        supp.start(Git.getInstance().getRequestProcessor(repository), repository, LBL_PushToUpstreamAction_preparing()).waitFinished();
        return t[0];
    }
        
    private static String parseRemote (String branchName) {
        int pos = branchName.indexOf('/');
        String remoteName = null;
        if (pos > 0) {
            remoteName = branchName.substring(0, pos);
        }
        return remoteName;
    }

    protected GitBranch getTrackedBranch (GitBranch activeBranch, PushMode pushMode, String errorLabel) {
        GitBranch trackedBranch = activeBranch.getTrackedBranch();
        if (trackedBranch != null && !trackedBranch.isRemote()) {
            trackedBranch = null;
        }
        if (trackedBranch != null && pushMode == PushMode.ASK) {
            if (!("" + parseRemote(trackedBranch.getName()) + "/" + activeBranch.getName()).equals(trackedBranch.getName())) {
                trackedBranch = null;
            }
        }
        return trackedBranch;
    }

    @Messages({"# {0} - branch name", "MSG_Err.noRemoteBranch=No remote found for branch {0}",
        "MSG_Err.noRemote=No remote defined in repository configuration",
        "# {0} - remote count", "MSG_Err.moreRemotes=Cannot choose from {0} remotes",
        "# {0} - branch name", "MSG_Err.noUri=No URI specified for remote {0}",
        "# {0} - branch name", "MSG_Err.noSpecs=No fetch ref specs specified for remote {0}"})
    protected static GitRemoteConfig getRemoteConfigForActiveBranch (GitBranch trackedBranch, RepositoryInfo info, String errorLabel) {
        Map<String, GitRemoteConfig> remotes = info.getRemotes();
        GitRemoteConfig cfg;
        if (trackedBranch == null) {
            if (remotes.size() == 1) {
                cfg = remotes.values().iterator().next();
            } else if (remotes.isEmpty()) {
                GitUtils.notifyError(errorLabel, MSG_Err_noRemote());
                return null;
            } else {
                GitUtils.notifyError(errorLabel, MSG_Err_moreRemotes(remotes.size()));
                return null;
            }
        } else {
            String remoteName = parseRemote(trackedBranch.getName());
            cfg = remoteName == null ? null : remotes.get(remoteName);
            if (cfg == null) {
                GitUtils.notifyError(errorLabel, MSG_Err_noRemoteBranch(trackedBranch.getName()));
                return null;
            }
        }
        if (cfg.getPushUris().isEmpty() && cfg.getUris().isEmpty()) {
            GitUtils.notifyError(errorLabel, MSG_Err_noUri(cfg.getRemoteName()));
            return null;
        }
        if (cfg.getFetchRefSpecs().isEmpty()) {
            GitUtils.notifyError(errorLabel, MSG_Err_noSpecs(cfg.getRemoteName()));
            return null;
        }
        return cfg;
    }

    public static String guessRemoteBranchName (List<String> fetchSpecs, String branchName, String remoteName) {
        String remoteBranchName = null;
        String branchShortName = branchName.startsWith(remoteName) 
                ? branchName.substring(remoteName.length() + 1)
                : branchName.substring(branchName.indexOf('/') + 1);
        for (String spec : fetchSpecs) {
            if (spec.startsWith("+")) { //NOI18N
                spec = spec.substring(1);
            }
            int pos = spec.lastIndexOf(':');
            if (pos > 0) {
                String left = spec.substring(0, pos);
                String right = spec.substring(pos + 1);
                if (right.endsWith(GitUtils.PREFIX_R_REMOTES + branchName)
                        || right.endsWith(GitUtils.PREFIX_R_REMOTES + remoteName + "/*")) { //NOI18N
                    if (left.endsWith("/*")) { //NOI18N
                        remoteBranchName = branchShortName;
                        break;
                    } else if (left.startsWith(GitUtils.PREFIX_R_HEADS)) {
                        remoteBranchName = left.substring(GitUtils.PREFIX_R_HEADS.length());
                        break;
                    }
                }
            }
        }
        return remoteBranchName;
    }

    @NbBundle.Messages({
        "LBL_Push.createNewBranch=Create New Branch?",
        "# {0} - branch name", "MSG_Push.createNewBranch=Push is about to create a new branch \"{0}\" in the remote repository.\n"
                + "Do you want to continue and create the branch?"
    })
    private static boolean shallCreateNewBranch (GitBranch branch) {
        return NotifyDescriptor.YES_OPTION == DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                Bundle.MSG_Push_createNewBranch(branch.getName()),
                Bundle.LBL_Push_createNewBranch(),
                NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE));
    }
    
}
