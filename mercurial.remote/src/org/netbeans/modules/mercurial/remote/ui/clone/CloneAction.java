/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.mercurial.remote.ui.clone;

import java.net.URISyntaxException;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.mercurial.remote.HgException;
import org.netbeans.modules.mercurial.remote.HgProgressSupport;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.OutputLogger;
import org.netbeans.modules.mercurial.remote.config.HgConfigFiles;
import org.netbeans.modules.mercurial.remote.ui.actions.ContextAction;
import static org.netbeans.modules.mercurial.remote.ui.properties.HgProperties.HGPROPNAME_DEFAULT_PULL;
import static org.netbeans.modules.mercurial.remote.ui.properties.HgProperties.HGPROPNAME_DEFAULT_PUSH;
import org.netbeans.modules.mercurial.remote.ui.repository.HgURL;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.mercurial.remote.util.HgProjectUtils;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 * Clone action for mercurial: 
 * hg clone - Create a copy of an existing repository in a new directory.
 * 
 * 
 */
@Messages({
    "# {0} - repository folder name",
    "CTL_MenuItem_CloneLocal=&Clone - {0}",
    "CTL_MenuItem_CloneRepository=&Clone - Selected Repository..."
})
public class CloneAction extends ContextAction {
    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        return HgUtils.isFromHgRepository(context);
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        VCSContext ctx = HgUtils.getCurrentContext(nodes);
        Set<VCSFileProxy> roots = HgUtils.getRepositoryRoots(ctx);
        return roots.size() == 1 ? "CTL_MenuItem_CloneLocal" : "CTL_MenuItem_CloneRepository"; // NOI18N
    }

    @Override
    public String getName(String role, Node[] activatedNodes) {
        VCSContext ctx = HgUtils.getCurrentContext(activatedNodes);
        Set<VCSFileProxy> roots = HgUtils.getRepositoryRoots(ctx);
        String name = getBaseName(activatedNodes);
        return roots.size() == 1 ? NbBundle.getMessage(CloneAction.class, name, roots.iterator().next().getName())
                : NbBundle.getMessage(CloneAction.class, name);
    }

    @Override
    protected void performContextAction (final Node[] nodes) {
        final VCSContext context = HgUtils.getCurrentContext(nodes);
        final VCSFileProxy roots[] = HgUtils.getActionRoots(context);
        if (roots == null || roots.length == 0) {
            return;
        }
        HgUtils.runIfHgAvailable(roots[0], new Runnable() {
            @Override
            public void run () {
                Utils.logVCSActionEvent("HG"); //NOI18N
                final VCSFileProxy root = Mercurial.getInstance().getRepositoryRoot(roots[0]);

                // Get unused Clone Folder name
                VCSFileProxy tmp = root.getParentFile();
                VCSFileProxy projFile = org.netbeans.modules.mercurial.remote.versioning.util.Utils.getProjectFile(context);
                String folderName = root.getName();
                Boolean projIsRepos = true;
                if (!root.equals(projFile))  {
                    // Mercurial Repository is not the same as project root
                    projIsRepos = false;
                }
                for(int i = 0; i < 10000; i++){
                    if (!VCSFileProxy.createFileProxy(tmp,folderName+"_clone"+i).exists()){ // NOI18N
                        tmp = VCSFileProxy.createFileProxy(tmp, folderName +"_clone"+i); // NOI18N
                        break;
                    }
                }
                Clone clone = new Clone(root, tmp);
                if (!clone.showDialog()) {
                    return;
                }
                performClone(new HgURL(root), clone.getTargetDir(), projIsRepos, projFile, true, null, null, true);
            }
        });
    }

    /**
     * 
     * @param source password is nulled
     * @param target
     * @param projIsRepos
     * @param projFile
     * @param pullPath password is nulled
     * @param pushPath password is nulled
     * @param scanForProjects
     * @return
     */
    public static RequestProcessor.Task performClone(final HgURL source, final VCSFileProxy target, boolean projIsRepos,
            VCSFileProxy projFile, final HgURL pullPath, final HgURL pushPath, boolean scanForProjects) {
        return performClone(source, target, projIsRepos, projFile, false, pullPath, pushPath, scanForProjects);
    }

    private static RequestProcessor.Task performClone(final HgURL source, final VCSFileProxy target,
            final boolean projIsRepos, final VCSFileProxy projFile, final boolean isLocalClone, final HgURL pullPath, final HgURL pushPath,
            final boolean scanForProjects) {

        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(source);
        final HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() {
                String projName = (projFile != null)
                                  ? HgProjectUtils.getProjectName(projFile)
                                  : null;

                OutputLogger logger = getLogger();
                try {
                    // TODO: We need to annotate the cloned project 
                    // See http://qa.netbeans.org/issues/show_bug.cgi?id=112870
                    logger.outputInRed(
                            NbBundle.getMessage(CloneAction.class,
                            "MSG_CLONE_TITLE")); // NOI18N
                    logger.outputInRed(
                            NbBundle.getMessage(CloneAction.class,
                            "MSG_CLONE_TITLE_SEP")); // NOI18N
                    List<String> list = HgUtils.runWithoutIndexing(new Callable<List<String>>() {
                        @Override
                        public List<String> call () throws Exception {
                            return HgCommand.doClone(source, target, getLogger());
                        }
                    }, target);
                    if (!VCSFileProxySupport.canRead(VCSFileProxy.createFileProxy(HgUtils.getHgFolderForRoot(target), HgConfigFiles.HG_RC_FILE)) || list.contains("transaction abort!")) { //NOI18N
                        // does not seem to be really cloned
                        logger.output(list);
                        Mercurial.LOG.log(Level.WARNING, "Hg clone seems to fail: {0}", list); //NOI18N
                        return;
                    }
                    if(list != null && !list.isEmpty()){
                        HgUtils.createIgnored(target);
                        logger.output(list);
               
                        if (projName != null) {
                            logger.outputInRed(
                                    NbBundle.getMessage(CloneAction.class,
                                    "MSG_CLONE_FROM", projName, source)); // NOI18N
                            logger.outputInRed(
                                    NbBundle.getMessage(CloneAction.class,
                                    "MSG_CLONE_TO", projName, target)); // NOI18N
                        } else {
                            logger.outputInRed(
                                    NbBundle.getMessage(CloneAction.class,
                                    "MSG_EXTERNAL_CLONE_FROM", source)); // NOI18N
                            logger.outputInRed(
                                    NbBundle.getMessage(CloneAction.class,
                                    "MSG_EXTERNAL_CLONE_TO", target)); // NOI18N

                        }
                        logger.output(""); // NOI18N

                        if (isLocalClone){
                            Mercurial hg = Mercurial.getInstance();
                            ProjectManager projectManager = ProjectManager.getDefault();
                            VCSFileProxy normalizedCloneFolder = target.normalizeFile();
                            VCSFileProxy cloneProjFile;
                            if (!projIsRepos) {
                                String name = (projFile != null)
                                              ? projFile.getPath().substring(source.getPath().length() + 1)
                                              : target.getPath();
                                cloneProjFile = VCSFileProxy.createFileProxy(normalizedCloneFolder, name);
                            } else {
                                cloneProjFile = normalizedCloneFolder;
                            }
                            openProject(cloneProjFile, projectManager, hg);
                        } else if (scanForProjects) {
                            CloneCompleted cc = new CloneCompleted(target);
                            if (!isCanceled()) {
                                cc.scanForProjects(this);
                            }
                        }
                    }
                    HgConfigFiles hgConfigFiles = new HgConfigFiles(target);
                    if (hgConfigFiles.getException() == null) {
                        Utils.logVCSExternalRepository("HG", source.toHgCommandUrlStringWithoutUserInfo()); //NOI18N
                        initializeDefaultPullPushUrl(hgConfigFiles);
                    } else {
                        Mercurial.LOG.log(Level.WARNING, "{0}: Cannot set default push and pull path", this.getClass().getName()); // NOI18N
                        Mercurial.LOG.log(Level.INFO, null, hgConfigFiles.getException());
                    }
                } catch (HgException.HgCommandCanceledException ex) {
                    // canceled by user, do nothing
                } catch (HgException ex) {
                    HgUtils.notifyException(ex);
                }finally {    
                    if(!isLocalClone){
                        logger.outputInRed(NbBundle.getMessage(CloneAction.class, "MSG_CLONE_DONE")); // NOI18N
                        logger.output(""); // NOI18N
                    }
                    source.clearPassword();
                    if (pullPath != null) {
                        pullPath.clearPassword();
                    }
                    if (pushPath != null) {
                        pushPath.clearPassword();
                    }
                }
            }

            private void initializeDefaultPullPushUrl(HgConfigFiles hgConfigFiles) {
                /*
                 * Mercurial itself sets just "default" in 'hgrc' file.
                 * We make sure that "default-push" is set, too - see
                 * bug #125835 ("default-push should be set
                 * automatically").
                 */
                String defaultPull = hgConfigFiles.getDefaultPull(false);
                if (defaultPull == null) {
                    return;
                }
                HgURL defaultPullURL;
                try {
                    defaultPullURL = new HgURL(defaultPull);
                    if ((pullPath == null) && (pushPath == null)) {
                        hgConfigFiles.setProperty(HGPROPNAME_DEFAULT_PUSH, defaultPull);
                    } else if ((pullPath != null) && (pushPath == null)) {
                        defaultPull = new HgURL(pullPath.toHgCommandUrlStringWithoutUserInfo(),
                                defaultPullURL.getUserInfo(), null).toHgCommandUrlString();
                        hgConfigFiles.setProperty(HGPROPNAME_DEFAULT_PULL, defaultPull);
                        hgConfigFiles.setProperty(HGPROPNAME_DEFAULT_PUSH, defaultPull);
                    } else if ((pullPath == null) && (pushPath != null)) {
                        String defaultPush = new HgURL(pushPath.toHgCommandUrlStringWithoutUserInfo(),
                                defaultPullURL.getUserInfo(), null).toHgCommandUrlString();
                        hgConfigFiles.setProperty(HGPROPNAME_DEFAULT_PUSH, defaultPush);
                    } else if ((pullPath != null) && (pushPath != null)) {
                        defaultPull = new HgURL(pullPath.toHgCommandUrlStringWithoutUserInfo(),
                                defaultPullURL.getUserInfo(), null).toHgCommandUrlString();
                        String defaultPush = new HgURL(pushPath.toHgCommandUrlStringWithoutUserInfo(),
                                defaultPullURL.getUserInfo(), null).toHgCommandUrlString();
                        hgConfigFiles.setProperty(HGPROPNAME_DEFAULT_PULL, defaultPull);
                        hgConfigFiles.setProperty(HGPROPNAME_DEFAULT_PUSH, defaultPush);
                    }
                } catch (URISyntaxException ex) {
                    Mercurial.LOG.log(Level.INFO, null, ex);
                }
            }

            private void openProject(final VCSFileProxy clonePrjFile, final ProjectManager projectManager, final Mercurial hg) throws MissingResourceException {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        // Open and set focus on the cloned project if possible
                        OutputLogger logger = getLogger();
                        try {
                            FileObject cloneProj = clonePrjFile.toFileObject();
                            Project prj = null;
                            if (clonePrjFile != null && cloneProj != null) {
                                prj = projectManager.findProject(cloneProj);
                            }
                            if (prj != null) {
                                HgProjectUtils.openProject(prj, this);
                                hg.versionedFilesChanged();
                                hg.refreshAllAnnotations();
                            } else {
                                logger.outputInRed(NbBundle.getMessage(CloneAction.class, "MSG_EXTERNAL_CLONE_PRJ_NOT_FOUND_CANT_SETASMAIN")); // NOI18N
                            }
                        } catch (java.lang.Exception ex) {
                            HgUtils.notifyException(ex);
                        } finally {
                            logger.outputInRed(NbBundle.getMessage(CloneAction.class, "MSG_CLONE_DONE")); // NOI18N
                            logger.output(""); // NOI18N
                        }
                    }
                });
            }
        };
        support.setRepositoryRoot(source);
        support.setCancellableDelegate(new Cancellable(){
            @Override
            public boolean cancel() {
                return true;
            }
        });
        return support.start(rp, source, org.openide.util.NbBundle.getMessage(CloneAction.class, "LBL_Clone_Progress", source)); // NOI18N
    }
   
}
