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

package org.netbeans.modules.git.ui.repository.remote;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author ondra
 */
public class SetRemoteConfigAction extends SingleRepositoryAction {

    public void updateRemote (File repository, final GitRemoteConfig remote, final Runnable runnable) {
        GitProgressSupport supp = new GitProgressSupport() {
            @Override
            protected void perform () {
                try {
                    GitClient client = getClient();
                    client.setRemote(remote, this);
                    if (runnable != null) {
                        runnable.run();
                    }
                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, true);
                }
            }
        };
        supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(SetRemoteConfigAction.class, "LBL_SetRemoteConfig.progressName")); //NOI18N
    }

    @Override
    protected void performAction (File repository, File[] roots, VCSContext context) {
        String selectedRemote = getSelectedRemote(context.getElements().lookupAll(Node.class));
        setRemote(repository, selectedRemote);
    }

    public void setRemote (File repository, String selectedRemote) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        info.refreshRemotes();
        Map<String, GitRemoteConfig> remotes = info.getRemotes();
        SetupRemoteWizard wiz = new SetupRemoteWizard(repository, remotes, selectedRemote);
        if (wiz.show()) {
            GitRemoteConfig remote = wiz.getRemote();
            updateRemote(repository, remote, null);
        }
    }

    @Override
    public String getName () {
        Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
        if (getSelectedRemote(nodes == null ? Collections.<Node>emptySet() : Arrays.asList(nodes)) == null) {
            return super.getName();
        } else {
            return NbBundle.getMessage(getClass(), "LBL_" + getClass().getSimpleName() + "_Name.remoteKnown"); //NOI18N
        }
    }
    
    private String getSelectedRemote (Collection<? extends Node> nodes) {
        String selectedRemote = null;
        for (Node node : nodes) {
            GitRemoteConfig config = node.getLookup().lookup(GitRemoteConfig.class);
            if (config != null) {
                if (selectedRemote == null) {
                    selectedRemote = config.getRemoteName();
                } else {
                    // only one remote can be updated
                    selectedRemote = null;
                    break;
                }
            }
        }
        return selectedRemote;
    }
}
