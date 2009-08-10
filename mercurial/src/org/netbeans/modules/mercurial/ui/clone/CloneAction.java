/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.mercurial.ui.clone;

import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.MissingResourceException;
import org.netbeans.modules.versioning.spi.VCSContext;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgKenaiSupport;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.config.HgConfigFiles;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.util.HgProjectUtils;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import static org.netbeans.modules.mercurial.ui.properties.HgProperties.HGPROPNAME_DEFAULT_PULL;
import static org.netbeans.modules.mercurial.ui.properties.HgProperties.HGPROPNAME_DEFAULT_PUSH;
import static org.netbeans.modules.mercurial.ui.properties.HgProperties.HGPROPNAME_USERNAME;

/**
 * Clone action for mercurial: 
 * hg clone - Create a copy of an existing repository in a new directory.
 * 
 * @author John Rice
 */
public class CloneAction extends ContextAction {
    private final VCSContext context;

    public CloneAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }
    
    public void performAction(ActionEvent ev){
        final File root = HgUtils.getRootFile(context);
        if (root == null) return;
        
        // Get unused Clone Folder name
        File tmp = root.getParentFile();
        File projFile = HgUtils.getProjectFile(context);
        String folderName = root.getName();
        Boolean projIsRepos = true;
        if (!root.equals(projFile))  {
            // Mercurial Repository is not the same as project root
            projIsRepos = false;
        }
        for(int i = 0; i < 10000; i++){
            if (!new File(tmp,folderName+"_clone"+i).exists()){ // NOI18N
                tmp = new File(tmp, folderName +"_clone"+i); // NOI18N
                break;
            }
        }
        Clone clone = new Clone(root, tmp);
        if (!clone.showDialog()) {
            return;
        }
        performClone(new HgURL(root), clone.getTargetDir(), projIsRepos, projFile, true, null, null, true);
    }

    public static RequestProcessor.Task performClone(final HgURL source, final File target, boolean projIsRepos,
            File projFile, final HgURL pullPath, final HgURL pushPath, boolean scanForProjects) {
        return performClone(source, target, projIsRepos, projFile, false, pullPath, pushPath, scanForProjects);
    }

    private static RequestProcessor.Task performClone(final HgURL source, final File target,
            final boolean projIsRepos, final File projFile, final boolean isLocalClone, final HgURL pullPath, final HgURL pushPath,
            final boolean scanForProjects) {

        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(source);
        final HgProgressSupport support = new HgProgressSupport() {
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
                    List<String> list = HgCommand.doClone(source, target, logger);
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
                            File normalizedCloneFolder = FileUtil.normalizeFile(target);
                            File cloneProjFile;
                            if (!projIsRepos) {
                                String name = (projFile != null)
                                              ? projFile.getAbsolutePath().substring(source.getPath().length() + 1)
                                              : target.getAbsolutePath();
                                cloneProjFile = new File (normalizedCloneFolder, name);
                            } else {
                                cloneProjFile = normalizedCloneFolder;
                            }
                            openProject(cloneProjFile, projectManager, hg);
                        } else if (scanForProjects) {
                            CloneCompleted cc = new CloneCompleted(target);
                            if (isCanceled()) {
                                return;
                            }
                            cc.scanForProjects(this);
                        }
                    }
                } catch (HgException ex) {
                    NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
                    DialogDisplayer.getDefault().notifyLater(e);
                }finally {
                    HgConfigFiles hgConfigFiles = new HgConfigFiles(target);
                    if (hgConfigFiles.getException() == null) {
                        if (source.isKenaiURL()) {
                            initializeDefaultPullPushUrlForKenai(hgConfigFiles);
                            String kenaiUserName = getKenaiUserName();
                            if (kenaiUserName != null) {
                                hgConfigFiles.setProperty(HGPROPNAME_USERNAME, kenaiUserName);
                            }
                        } else {
                            initializeDefaultPullPushUrl(hgConfigFiles);
                        }
                    } else {
                        Mercurial.LOG.log(Level.WARNING, this.getClass().getName() + ": Cannot set default push and pull path"); // NOI18N
                        Mercurial.LOG.log(Level.INFO, null, hgConfigFiles.getException());
                    }
                        
                    if(!isLocalClone){
                        logger.outputInRed(NbBundle.getMessage(CloneAction.class, "MSG_CLONE_DONE")); // NOI18N
                        logger.output(""); // NOI18N
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
                if ((pullPath == null) && (pushPath == null)) {
                    String defaultPull = hgConfigFiles.getDefaultPull(false);
                    hgConfigFiles.setProperty(HGPROPNAME_DEFAULT_PUSH, defaultPull);

                } else if ((pullPath != null) && (pushPath == null)) {
                    String defaultPull = pullPath.toHgCommandUrlString();
                    hgConfigFiles.setProperty(HGPROPNAME_DEFAULT_PULL, defaultPull);
                    hgConfigFiles.setProperty(HGPROPNAME_DEFAULT_PUSH, defaultPull);

                } else if ((pullPath == null) && (pushPath != null)) {
                    String defaultPush = pushPath.toHgCommandUrlString();
                    hgConfigFiles.setProperty(HGPROPNAME_DEFAULT_PUSH, defaultPush);

                } else if ((pullPath != null) && (pushPath != null)) {
                    String defaultPull = pullPath.toHgCommandUrlString();
                    String defaultPush = pushPath.toHgCommandUrlString();
                    hgConfigFiles.setProperty(HGPROPNAME_DEFAULT_PULL, defaultPull);
                    hgConfigFiles.setProperty(HGPROPNAME_DEFAULT_PUSH, defaultPush);
                }
            }

            private void initializeDefaultPullPushUrlForKenai(HgConfigFiles hgConfigFiles) {
                /*
                 * Mercurial itself sets just "default" in 'hgrc' file.
                 * We make sure that "default-push" is set, too - see
                 * bug #125835 ("default-push should be set
                 * automatically"). Because Kenai username and password
                 * should not be saved, we also modify the "default" - we
                 * strip the userdata (username and password) if any.
                 */
                if ((pullPath == null) && (pushPath == null)) {
                    String defaultPull = hgConfigFiles.getDefaultPull(false);
                    String modifiedPullUrl = HgURL.stripUserInfo(defaultPull);
                    hgConfigFiles.setProperty(HGPROPNAME_DEFAULT_PULL, modifiedPullUrl);
                    hgConfigFiles.setProperty(HGPROPNAME_DEFAULT_PUSH, modifiedPullUrl);

                } else if ((pullPath != null) && (pushPath == null)) {
                    String defaultPull = pullPath.toHgCommandUrlStringWithoutUserInfo();
                    hgConfigFiles.setProperty(HGPROPNAME_DEFAULT_PULL, defaultPull);
                    hgConfigFiles.setProperty(HGPROPNAME_DEFAULT_PUSH, defaultPull);

                } else if ((pullPath == null) && (pushPath != null)) {
                    String defaultPull = hgConfigFiles.getDefaultPull(false);
                    String modifiedPullUrl = HgURL.stripUserInfo(defaultPull);
                    String defaultPush = pushPath.toHgCommandUrlStringWithoutUserInfo();
                    hgConfigFiles.setProperty(HGPROPNAME_DEFAULT_PULL, modifiedPullUrl);
                    hgConfigFiles.setProperty(HGPROPNAME_DEFAULT_PUSH, defaultPush);

                } else if ((pullPath != null) && (pushPath != null)) {
                    String defaultPull = pullPath.toHgCommandUrlStringWithoutUserInfo();
                    String defaultPush = pushPath.toHgCommandUrlStringWithoutUserInfo();
                    hgConfigFiles.setProperty(HGPROPNAME_DEFAULT_PULL, defaultPull);
                    hgConfigFiles.setProperty(HGPROPNAME_DEFAULT_PUSH, defaultPush);
                }
            }

            private String getKenaiUserName() {
                PasswordAuthentication passwordAuthentication
                        = HgKenaiSupport.getInstance().getPasswordAuthentication(
                                            source.toUrlStringWithoutUserInfo(),
                                            false);
                return (passwordAuthentication != null)
                       ? passwordAuthentication.getUserName()
                       : null;
            }

            private void openProject(final File clonePrjFile, final ProjectManager projectManager, final Mercurial hg) throws MissingResourceException {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        // Open and set focus on the cloned project if possible
                        OutputLogger logger = getLogger();
                        try {
                            FileObject cloneProj = FileUtil.toFileObject(clonePrjFile);
                            Project prj = null;
                            if (clonePrjFile != null && cloneProj != null) {
                                prj = projectManager.findProject(cloneProj);
                            }
                            if (prj != null) {
                                HgProjectUtils.openProject(prj, this, HgModuleConfig.getDefault().getSetMainProject());
                                hg.versionedFilesChanged();
                                hg.refreshAllAnnotations();
                            } else {
                                logger.outputInRed(NbBundle.getMessage(CloneAction.class, "MSG_EXTERNAL_CLONE_PRJ_NOT_FOUND_CANT_SETASMAIN")); // NOI18N
                            }
                        } catch (java.lang.Exception ex) {
                            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(new HgException(ex.toString()));
                            DialogDisplayer.getDefault().notifyLater(e);
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
            public boolean cancel() {
                if(!Utilities.isWindows()) 
                    return true;
                
                OutputLogger logger = support.getLogger();
                logger.outputInRed(NbBundle.getMessage(CloneAction.class, "MSG_CLONE_CANCEL_ATTEMPT")); // NOI18N
                JOptionPane.showMessageDialog(null,
                    NbBundle.getMessage(CloneAction.class, "MSG_CLONE_CANCEL_NOT_SUPPORTED"),// NOI18N
                    NbBundle.getMessage(CloneAction.class, "MSG_CLONE_CANCEL_NOT_SUPPORTED_TITLE"),// NOI18N
                    JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
        });
        return support.start(rp, source, org.openide.util.NbBundle.getMessage(CloneAction.class, "LBL_Clone_Progress", source)); // NOI18N
    }

    public boolean isEnabled() {
        return HgUtils.getRootFile(context) != null;
    }
   
    private static final String HG_PATHS_SECTION_ENCLOSED = "[" + HgConfigFiles.HG_PATHS_SECTION + "]";// NOI18N
    private static void fixLocalPullPushPathsOnWindows(File root) {
        File hgrcFile = null;
        File tempFile = null;
        BufferedReader br = null;
        PrintWriter pw = null;
        
        try {
            hgrcFile = new File(new File(root, HgConfigFiles.HG_REPO_DIR), HgConfigFiles.HG_RC_FILE);
            if (!hgrcFile.isFile() || !hgrcFile.canWrite()) return;
            
            tempFile = new File(hgrcFile.getAbsolutePath() + ".tmp"); // NOI18N
            if (tempFile == null) return;
            
            br = new BufferedReader(new FileReader(hgrcFile));
            pw = new PrintWriter(new FileWriter(tempFile));

            String line = null;
            
            boolean bInPaths = false;
            boolean bPullDone = false;
            boolean bPushDone = false;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(HG_PATHS_SECTION_ENCLOSED)) {
                    bInPaths = true;
                }else if (line.startsWith("[")) { // NOI18N
                    bInPaths = false;
                }

                if (bInPaths && !bPullDone && line.startsWith(HgConfigFiles.HG_DEFAULT_PULL_VALUE) && 
                        !line.startsWith(HgConfigFiles.HG_DEFAULT_PUSH_VALUE)) {                    
                    pw.println(line.replace("\\", "\\\\"));
                    bPullDone = true;
                } else if (bInPaths && !bPullDone && line.startsWith(HgConfigFiles.HG_DEFAULT_PULL)) {
                    pw.println(line.replace("\\", "\\\\"));
                    bPullDone = true;
                } else if (bInPaths && !bPushDone && line.startsWith(HgConfigFiles.HG_DEFAULT_PUSH_VALUE)) {
                    pw.println(line.replace("\\", "\\\\"));
                    bPushDone = true;
                } else {
                    pw.println(line);
                    pw.flush();
                }
            }
        } catch (IOException ex) {
            // Ignore
        } finally {
            try {
                if(pw != null) pw.close();
                if(br != null) br.close();
                if(tempFile != null && tempFile.isFile() && tempFile.canWrite() && hgrcFile != null){ 
                    hgrcFile.delete();
                    tempFile.renameTo(hgrcFile);
                }
            } catch (IOException ex) {
            // Ignore
            }
        }
    }
}
