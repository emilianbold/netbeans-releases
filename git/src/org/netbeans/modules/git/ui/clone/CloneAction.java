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

package org.netbeans.modules.git.ui.clone;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.Callable;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.GitTransportUpdate;
import org.netbeans.libs.git.GitTransportUpdate.Type;
import org.netbeans.libs.git.GitURI;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.ContextHolder;
import org.netbeans.modules.git.ui.output.OutputLogger;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.ProjectUtilities;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Tomas Stupka
 */
@ActionID(id = "org.netbeans.modules.git.ui.clone.CloneAction", category = "Git")
@ActionRegistration(displayName = "#LBL_CloneAction_Name")
@ActionReferences({
   @ActionReference(path="Versioning/Git/Actions/Global", position=310)
})
@NbBundle.Messages("LBL_CloneAction_Name=&Clone...")
public class CloneAction implements ActionListener, HelpCtx.Provider {
    private final VCSContext ctx;

    public CloneAction (ContextHolder ctx) {
        this.ctx = ctx.getContext();
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.git.ui.clone.CloneAction");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        String cloneFromPath = null;
        if(ctx != null) {
            Set<File> roots = ctx.getRootFiles();
            if(roots.size() == 1) {
                Lookup l = ctx.getElements();
                Project project = null;
                if(l != null) {
                    Collection<? extends Node> nodes = l.lookupAll(Node.class);
                    if(nodes != null && !nodes.isEmpty()) {
                        project = nodes.iterator().next().getLookup().lookup(Project.class);
                    }
                }
                if(project == null) {
                    FileObject fo = FileUtil.toFileObject(roots.iterator().next());
                    if(fo != null && fo.isFolder()) {
                        try {
                            project = ProjectManager.getDefault().findProject(fo);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (IllegalArgumentException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
                if(project != null) {
                    FileObject fo = project.getProjectDirectory();
                    File file = FileUtil.toFile(fo);
                    if(file != null) {
                        if(Git.getInstance().isManaged(file) ) {
                            cloneFromPath = Git.getInstance().getRepositoryRoot(file).getAbsolutePath();
                        }
                    }
                }
            }
        }
        performClone(cloneFromPath, null);
    }

    private static void performClone(String url, PasswordAuthentication pa) throws MissingResourceException {
        performClone(url, pa, false);
    }
    
    public static File performClone(String url, PasswordAuthentication pa, boolean waitFinished) throws MissingResourceException {
        final CloneWizard wiz = new CloneWizard(pa, url);
        Boolean ok = Mutex.EVENT.readAccess(new Mutex.Action<Boolean>() {
            @Override
            public Boolean run () {
                return wiz.show();
            }
        });
        if (Boolean.TRUE.equals(ok)) {            
            final GitURI remoteUri = wiz.getRemoteURI();
            final File destination = wiz.getDestination();
            final String remoteName = wiz.getRemoteName();
            List<String> branches = wiz.getBranchNames();
            final List<String> refSpecs = new ArrayList<String>(branches.size());
            for (String branchName : branches) {
                refSpecs.add(GitUtils.getRefSpec(branchName, remoteName));
            }
            final GitBranch branch = wiz.getBranch();
            final boolean scan = wiz.scanForProjects();
            
            GitProgressSupport supp = new GitProgressSupport() {
                @Override
                protected void perform () {
                    try {
                        GitUtils.runWithoutIndexing(new Callable<Void>() {
                            @Override
                            public Void call () throws Exception {
                                GitClient client = getClient();
                                client.init(getProgressMonitor());
                                Map<String, GitTransportUpdate> updates = client.fetch(remoteUri.toPrivateString(), refSpecs, getProgressMonitor());
                                log(updates);

                                if(isCanceled()) {
                                    return null;
                                }

                                client.setRemote(new CloneRemoteConfig(remoteName, remoteUri, refSpecs).toGitRemote(), getProgressMonitor());
                                org.netbeans.modules.versioning.util.Utils.logVCSExternalRepository("GIT", remoteUri.toString()); //NOI18N
                                if (branch == null) {
                                    client.createBranch(GitUtils.MASTER, GitUtils.PREFIX_R_REMOTES + remoteName + "/" + GitUtils.MASTER, getProgressMonitor());
                                } else {
                                    client.createBranch(branch.getName(), remoteName + "/" + branch.getName(), getProgressMonitor());
                                    client.checkout(new File[0], branch.getName(), true, getProgressMonitor());
                                }

                                Git.getInstance().getFileStatusCache().refreshAllRoots(destination);
                                Git.getInstance().versionedFilesChanged();                       

                                if(scan && !isCanceled()) {
                                    scanForProjects(destination);
                                }
                                return null;
                            }
                        }, destination);
                        
                    } catch (GitException ex) {
                        GitClientExceptionHandler.notifyException(ex, true);
                    }
                }

                private void log (Map<String, GitTransportUpdate> updates) {
                    OutputLogger logger = getLogger();
                    if (updates.isEmpty()) {
                        logger.output(NbBundle.getMessage(CloneAction.class, "MSG_CloneAction.updates.noChange")); //NOI18N
                    } else {
                        for (Map.Entry<String, GitTransportUpdate> e : updates.entrySet()) {
                            GitTransportUpdate update = e.getValue();
                            if (update.getType() == Type.BRANCH) {
                                logger.output(NbBundle.getMessage(CloneAction.class, "MSG_CloneAction.updates.updateBranch", new Object[] { //NOI18N
                                    update.getLocalName(), 
                                    update.getOldObjectId(),
                                    update.getNewObjectId(),
                                    update.getResult(),
                                }));
                            } else {
                                logger.output(NbBundle.getMessage(CloneAction.class, "MSG_CloneAction.updates.updateTag", new Object[] { //NOI18N
                                    update.getLocalName(), 
                                    update.getResult(),
                                }));
                            }
                        }
                    }
                }

                public void scanForProjects (File workingFolder) {
                    Map<Project, Set<Project>> checkedOutProjects = new HashMap<Project, Set<Project>>();
                    checkedOutProjects.put(null, new HashSet<Project>()); // initialize root project container
                    File normalizedWorkingFolder = FileUtil.normalizeFile(workingFolder);
                    FileObject fo = FileUtil.toFileObject(normalizedWorkingFolder);
                    if (fo == null || !fo.isFolder()) {
                        return;
                    } else {
                        ProjectUtilities.scanForProjects(fo, checkedOutProjects);
                    }
                    if (isCanceled()) {
                        return;
                    }
                    // open project selection
                    ProjectUtilities.openClonedOutProjects(checkedOutProjects, workingFolder);
                }
            };
            Task task = supp.start(Git.getInstance().getRequestProcessor(destination), destination, NbBundle.getMessage(CloneAction.class, "LBL_CloneAction.progressName")); //NOI18N
            if(waitFinished) {
                task.waitFinished();
            }
            return destination;
        }
        return null;
    }
    
    private static class CloneRemoteConfig {
        private String remoteName;
        private GitURI remoteUri;
        private List<String> refSpecs;
        public CloneRemoteConfig(String remoteName, GitURI remoteUri, List<String> refSpecs) {
            this.remoteName = remoteName;
            this.remoteUri = remoteUri;
            this.refSpecs = refSpecs;
        }
        public String getRemoteName() {
            return remoteName;
        }
        public List<String> getUris() {
            return Arrays.asList(remoteUri.toPrivateString());
        }
        public List<String> getPushUris() {
            return Collections.emptyList();
        }
        public List<String> getFetchRefSpecs() {
            return refSpecs;
        }
        public List<String> getPushRefSpecs() {
            return Collections.emptyList();
        }

        private GitRemoteConfig toGitRemote () {
            return new GitRemoteConfig(remoteName, getUris(), getPushUris(), getFetchRefSpecs(), getPushRefSpecs());
        }
    }    
}
