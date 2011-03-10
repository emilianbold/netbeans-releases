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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitTransportUpdate;
import org.netbeans.libs.git.GitTransportUpdate.Type;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.GitAction;
import org.netbeans.modules.git.ui.selectors.BranchesSelector.Branch;
import org.netbeans.modules.git.ui.fetch.FetchAction;
import org.netbeans.modules.git.ui.output.OutputLogger;
import org.netbeans.modules.versioning.util.ProjectUtilities;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
@ActionID(id = "org.netbeans.modules.git.ui.clone.CloneAction", category = "Git")
@ActionRegistration(displayName = "#LBL_CloneAction_Name")
public class CloneAction extends GitAction {

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        
        String cloneFromPath = null;
        if(nodes.length == 1) {
            Project project =  nodes[0].getLookup().lookup(Project.class);
            if(project != null) {
                FileObject fo = project.getProjectDirectory();
                File file = FileUtil.toFile(fo);
                if(file != null) {
                    cloneFromPath = file.getAbsolutePath();
                }
            }
        }
        
        CloneWizard wiz = new CloneWizard(cloneFromPath);
        if (wiz.show()) {
            
            final String remoteUri = wiz.getRemoteUri();
            final File destination = wiz.getDestination();
            final String remoteName = wiz.getRemoteName();
            List<Branch> branches = wiz.getBranches();
            final List<String> refSpecs = new ArrayList<String>(branches.size());
            for (Branch branch : branches) {
                refSpecs.add(branch.getRefSpec(remoteName));
            }
            final Branch branch = wiz.getBranch();
            final boolean scan = wiz.scanForProjects();
            
            GitProgressSupport supp = new GitProgressSupport() {
                @Override
                protected void perform () {
                    try {
                        GitClient client = getClient();
                        client.init(this);
                        Map<String, GitTransportUpdate> updates = client.fetch(remoteUri, refSpecs, this);
                        log(updates);
                        
                        if(isCanceled()) {
                            return;
                        }
                        
                        client.createBranch(branch.getName(), remoteName + "/" + branch.getName(), this);
                        client.checkoutRevision(branch.getName(), true, this);

                        Git.getInstance().getFileStatusCache().refreshAllRoots(destination);
                        Git.getInstance().versionedFilesChanged();                       
                        
                        if(scan && !isCanceled()) {
                            scanForProjects(destination, this);
                        }
                        
                    } catch (GitException ex) {
                        GitClientExceptionHandler.notifyException(ex, true);
                    }
                }

                private void log (Map<String, GitTransportUpdate> updates) {
                    OutputLogger logger = getLogger();
                    if (updates.isEmpty()) {
                        logger.output(NbBundle.getMessage(FetchAction.class, "MSG_FetchAction.updates.noChange")); //NOI18N
                    } else {
                        for (Map.Entry<String, GitTransportUpdate> e : updates.entrySet()) {
                            GitTransportUpdate update = e.getValue();
                            if (update.getType() == Type.BRANCH) {
                                logger.output(NbBundle.getMessage(FetchAction.class, "MSG_FetchAction.updates.updateBranch", new Object[] { //NOI18N
                                    update.getLocalName(), 
                                    update.getOldObjectId(),
                                    update.getNewObjectId(),
                                    update.getResult(),
                                }));
                            } else {
                                logger.output(NbBundle.getMessage(FetchAction.class, "MSG_FetchAction.updates.updateTag", new Object[] { //NOI18N
                                    update.getLocalName(), 
                                    update.getResult(),
                                }));
                            }
                        }
                    }
                }
            };
            supp.start(Git.getInstance().getRequestProcessor(destination), destination, NbBundle.getMessage(FetchAction.class, "LBL_FetchAction.progressName")); //NOI18N
        }
            
            
    }

    public void scanForProjects(File workingFolder, GitProgressSupport support) {
        Map<Project, Set<Project>> checkedOutProjects = new HashMap<Project, Set<Project>>();
        checkedOutProjects.put(null, new HashSet<Project>()); // initialize root project container
        File normalizedWorkingFolder = FileUtil.normalizeFile(workingFolder);
        FileObject fo = FileUtil.toFileObject(normalizedWorkingFolder);
        if (fo != null) {
            ProjectUtilities.scanForProjects(fo, checkedOutProjects);
        }
        if (support != null && support.isCanceled()) {
            return;
        }
        // open project selection
        ProjectUtilities.openCheckedOutProjects(checkedOutProjects, workingFolder);
    }    
}
