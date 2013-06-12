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

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.MultipleRepositoryAction;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import static org.netbeans.modules.git.ui.push.Bundle.*;
import org.netbeans.modules.git.utils.GitUtils;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.push.PushToUpstreamAction", category = "Git")
@ActionRegistration(displayName = "#LBL_PushToUpstreamAction_Name")
@Messages("LBL_PushToUpstreamAction_Name=Pu&sh to Upstream")
public class PushToUpstreamAction extends MultipleRepositoryAction {
    
    @Override
    protected RequestProcessor.Task performAction (File repository, File[] roots, VCSContext context) {
        return push(repository);
    }
    
    @NbBundle.Messages({"LBL_Push.pushToUpstreamFailed=Push to Upstream Failed",
        "LBL_PushToUpstreamAction.preparing=Preparing Push...",
        "MSG_Err.unknownRemoteBranchName=Cannot guess remote branch name for {0}"})
    private Task push (final File repository) {
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
                GitBranch trackedBranch = getTrackedBranch(activeBranch, errorLabel);
                if (trackedBranch == null) {
                    return;
                }
                GitRemoteConfig cfg = getRemoteConfigForActiveBranch(trackedBranch, info, errorLabel);                        
                if (cfg == null) {
                    return;
                }
                String uri = cfg.getPushUris().isEmpty() ? cfg.getUris().get(0) : cfg.getPushUris().get(0);
                List<PushMapping> pushMappings = new LinkedList<PushMapping>();
                List<String> fetchSpecs = cfg.getFetchRefSpecs();
                String remoteBranchName = guessRemoteBranchName(fetchSpecs, trackedBranch.getName(), cfg.getRemoteName());
                if (remoteBranchName == null) {
                    GitUtils.notifyError(errorLabel, MSG_Err_unknownRemoteBranchName(trackedBranch.getName()));
                }
                pushMappings.add(new PushMapping.PushBranchMapping(remoteBranchName, trackedBranch.getId(), activeBranch, false, false));
                Utils.logVCSExternalRepository("GIT", uri); //NOI18N
                if (!isCanceled()) {
                    t[0] = SystemAction.get(PushAction.class).push(repository, uri, pushMappings, cfg.getFetchRefSpecs());
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

    @Messages({"# {0} - branch name", "MSG_Err.noTrackedBranch=No tracked remote branch specified for local {0}",
        "# {0} - branch name", "MSG_Err.trackedBranchLocal=Tracked branch {0} is not a remote branch"})
    protected GitBranch getTrackedBranch (GitBranch activeBranch, String errorLabel) {
        GitBranch trackedBranch = activeBranch.getTrackedBranch();
        if (trackedBranch == null) {
            GitUtils.notifyError(errorLabel,
                    MSG_Err_noTrackedBranch(activeBranch.getName())); //NOI18N
            return null;
        }
        if (!trackedBranch.isRemote()) {
            GitUtils.notifyError(errorLabel, MSG_Err_trackedBranchLocal(trackedBranch.getName())); //NOI18N
            return null;
        }
        return trackedBranch;
    }

    @Messages({"# {0} - branch name", "MSG_Err.noRemote=No remote found for branch {0}",
        "# {0} - branch name", "MSG_Err.noUri=No URI specified for remote {0}",
        "# {0} - branch name", "MSG_Err.noSpecs=No fetch ref specs specified for remote {0}"})
    protected static GitRemoteConfig getRemoteConfigForActiveBranch (GitBranch trackedBranch, RepositoryInfo info, String errorLabel) {
        Map<String, GitRemoteConfig> remotes = info.getRemotes();
        String remoteName = parseRemote(trackedBranch.getName());
        GitRemoteConfig cfg = remoteName == null ? null : remotes.get(remoteName);
        if (cfg == null) {
            GitUtils.notifyError(errorLabel, MSG_Err_noRemote(trackedBranch.getName()));
            return null;
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

    private static String guessRemoteBranchName (List<String> fetchSpecs, String branchName, String remoteName) {
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
    
}
