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

package org.netbeans.modules.git.ui.actions;

import java.io.File;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.nodes.Node;

/**
 *
 * @author ondra
 */
public abstract class SingleRepositoryAction extends GitAction {

    private static final Logger LOG = Logger.getLogger(SingleRepositoryAction.class.getName());
    private static final Set<File> loggedRepositories = new HashSet<File>();

    @Override
    protected final void performContextAction (final Node[] nodes) {
        Utils.postParallel(new Runnable () {
            @Override
            public void run() {
                VCSContext context = getCurrentContext(nodes);
                performAction(context);
            }
        }, 0);
    }

    public final void performAction (VCSContext context) {
        Map.Entry<File, File[]> actionRoots = getActionRoots(context);
        if (actionRoots != null) {
            logRemoteRepositoryAccess(actionRoots.getKey());
            performAction(actionRoots.getKey(), actionRoots.getValue(), context);
        }
    }
    
    protected abstract void performAction (File repository, File[] roots, VCSContext context);

    protected static Entry<File, File[]> getActionRoots (VCSContext context) {
        Set<File> repositories = GitUtils.getRepositoryRoots(context);
        if (repositories.isEmpty()) {
            LOG.log(Level.FINE, "No repository in the given context: {0}", context.getRootFiles()); //NOI18N
            return null;
        }
        SimpleImmutableEntry<File, File[]> actionRoots = GitUtils.getActionRoots(context);
        if (actionRoots != null) {
            File repository = actionRoots.getKey();
            if (repositories.size() > 1) {
                LOG.log(Level.FINE, "Multiple repositories in the given context: {0}, selected {1}", new Object[] { context.getRootFiles(), repository }); //NOI18N
            }
        }
        return actionRoots;
    }
    
    /**
     * Reads all remotes in a local repository's config and logs remote repository urls.
     * Does this only once per a NB session and repository
     * @param repositoryRoot root of the local repository
     */
    private void logRemoteRepositoryAccess (final File repositoryRoot) {
        if (loggedRepositories.add(repositoryRoot)) {
            Git.getInstance().getRequestProcessor(repositoryRoot).post(new Runnable() {
                @Override
                public void run () {
                    Set<String> urls = new HashSet<String>();
                    try {
                        GitClient client = Git.getInstance().getClient(repositoryRoot);
                        Map<String, GitRemoteConfig> cfgs = client.getRemotes(ProgressMonitor.NULL_PROGRESS_MONITOR);
                        for (Map.Entry<String, GitRemoteConfig> e : cfgs.entrySet()) {
                            GitRemoteConfig cfg = e.getValue();
                            for (List<String> uris : Arrays.asList(cfg.getUris(), cfg.getPushUris())) {
                                if (!uris.isEmpty()) {
                                    urls.addAll(uris);
                                }
                            }
                        }
                    } catch (GitException ex) {
                        // not interested
                    }
                    for (String url : urls) {
                        if (!url.trim().isEmpty()) {
                            Utils.logVCSExternalRepository("GIT", url); //NOI18N
                        }
                    }
                }
            });
        }
    }
}
