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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.GitTransportUpdate;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.fetch.FetchAction", category = "Git")
@ActionRegistration(displayName = "#LBL_FetchAction_Name")
public class FetchAction extends GetRemoteChangesAction {

    @Override
    protected void performAction (File repository, File[] roots, VCSContext context) {
        fetch(repository);
    }
    
    public void fetch (final File repository, GitRemoteConfig remote) {
        if (remote.getUris().size() != 1) {
            Utils.post(new Runnable () {
                @Override
                public void run () {
                    fetch(repository);
                }
            });
        } else {
            fetch(repository, remote.getUris().get(0), remote.getFetchRefSpecs(), null);
        }
    }
    
    private void fetch (File repository) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        info.refreshRemotes();
        Map<String, GitRemoteConfig> remotes = info.getRemotes();
        FetchWizard wiz = new FetchWizard(repository, remotes);
        if (wiz.show()) {
            Utils.logVCSExternalRepository("GIT", wiz.getFetchUri()); //NOI18N
            fetch(repository, wiz.getFetchUri(), wiz.getFetchRefSpecs(), wiz.getRemoteToPersist());
        }
    }
    
    public void fetch (File repository, final String target, final List<String> fetchRefSpecs, final String remoteNameToUpdate) {
        GitProgressSupport supp = new GitProgressSupport() {
            @Override
            protected void perform () {
                try {
                    GitClient client = getClient();
                    if (remoteNameToUpdate != null) {
                        GitRemoteConfig config = client.getRemote(remoteNameToUpdate, getProgressMonitor());
                        if (isCanceled()) {
                            return;
                        }
                        config = prepareConfig(config, remoteNameToUpdate, target, fetchRefSpecs);
                        client.setRemote(config, getProgressMonitor());
                        if (isCanceled()) {
                            return;
                        }
                    }
                    Map<String, GitTransportUpdate> updates = client.fetch(target, fetchRefSpecs, getProgressMonitor());
                    log(updates, getLogger());
                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, true);
                }
            }
        };
        supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(FetchAction.class, "LBL_FetchAction.progressName")); //NOI18N
    }
    
    static GitRemoteConfig prepareConfig (GitRemoteConfig original, String remoteName, String remoteUri, List<String> fetchRefSpecs) {
        List<String> remoteUris;
        if (original != null) {
            remoteUris = new LinkedList<String>(original.getUris());
            if (!remoteUris.contains(remoteUri)) {
                remoteUris.add(remoteUri);
            }
        } else {
            remoteUris = Arrays.asList(remoteUri);
        }
        List<String> refSpecs;
        if (original != null) {
            refSpecs = new LinkedList<String>(original.getFetchRefSpecs());
            for (String refSpec : fetchRefSpecs) {
                if (!refSpecs.contains(refSpec)) {
                    refSpecs.add(refSpec);
                }
            }
        } else {
            refSpecs = fetchRefSpecs;
        }
        return new GitRemoteConfig(remoteName,
                remoteUris,
                original == null ? Collections.<String>emptyList() : original.getPushUris(),
                refSpecs,
                original == null ? Collections.<String>emptyList() : original.getPushRefSpecs());
    }
}
