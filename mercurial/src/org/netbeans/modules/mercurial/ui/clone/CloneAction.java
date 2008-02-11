/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import org.netbeans.modules.versioning.spi.VCSContext;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.util.HgRepositoryContextCache;
import org.netbeans.modules.mercurial.util.HgProjectUtils;
import org.netbeans.modules.mercurial.ui.clone.Clone;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

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

        performClone(root.getAbsolutePath(), clone.getOutputFileName(), projIsRepos, projFile, true);
    }

    public static void performClone(final String source, final String target, boolean projIsRepos, File projFile) {
        performClone(source, target, projIsRepos, projFile, false);
    }

    private static void performClone(final String source, final String target, 
            boolean projIsRepos, File projFile, final boolean isLocalClone) {
        final Mercurial hg = Mercurial.getInstance();
        final ProjectManager projectManager = ProjectManager.getDefault();
        final File prjFile = projFile;
        final Boolean prjIsRepos = projIsRepos;
        final File cloneFolder = new File (target);
        final File normalizedCloneFolder = FileUtil.normalizeFile(cloneFolder);
        String projName = null;
        if (projFile != null) projName = HgProjectUtils.getProjectName(projFile);
        final String prjName = projName;
        File cloneProjFile;
        if (!prjIsRepos) {
            String name = null;
            if(prjFile != null)
                name = prjFile.getAbsolutePath().substring(source.length() + 1);
            else
                name = target;
            cloneProjFile = new File (normalizedCloneFolder, name);
        } else {
            cloneProjFile = normalizedCloneFolder;
        }
        final File clonePrjFile = cloneProjFile;
        
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(source);
        HgProgressSupport support = new HgProgressSupport() {
            Runnable doOpenProject = new Runnable () {
                public void run()  {
                    // Open and set focus on the cloned project if possible
                    try {
                        FileObject cloneProj = FileUtil.toFileObject(clonePrjFile);
                        Project prj = null;
                        if(clonePrjFile != null && cloneProj != null)
                            prj = projectManager.findProject(cloneProj);
                        if(prj != null){
                            HgProjectUtils.openProject(prj, this, HgModuleConfig.getDefault().getSetMainProject());
                            hg.versionedFilesChanged();
                            hg.refreshAllAnnotations();
                        }else{
                            HgUtils.outputMercurialTabInRed( NbBundle.getMessage(CloneAction.class,
                                    "MSG_EXTERNAL_CLONE_PRJ_NOT_FOUND_CANT_SETASMAIN")); // NOI18N
                        }
            
                    } catch (java.lang.Exception ex) {
                        NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(new HgException(ex.toString()));
                        DialogDisplayer.getDefault().notifyLater(e);
                    } finally{
                       HgUtils.outputMercurialTabInRed(NbBundle.getMessage(CloneAction.class, "MSG_CLONE_DONE")); // NOI18N
                       HgUtils.outputMercurialTab(""); // NOI18N
                    }
                }
            };
            public void perform() {
                try {
                    // TODO: We need to annotate the cloned project 
                    // See http://qa.netbeans.org/issues/show_bug.cgi?id=112870
                    HgUtils.outputMercurialTabInRed(
                            NbBundle.getMessage(CloneAction.class,
                            "MSG_CLONE_TITLE")); // NOI18N
                    HgUtils.outputMercurialTabInRed(
                            NbBundle.getMessage(CloneAction.class,
                            "MSG_CLONE_TITLE_SEP")); // NOI18N
                    List<String> list = HgCommand.doClone(source, target);
                    if(list != null && !list.isEmpty()){
                        HgUtils.createIgnored(cloneFolder);
                        HgUtils.outputMercurialTab(list);
               
                        if (prjName != null) {
                            HgUtils.outputMercurialTabInRed(
                                    NbBundle.getMessage(CloneAction.class,
                                    "MSG_CLONE_FROM", prjName, source)); // NOI18N
                            HgUtils.outputMercurialTabInRed(
                                    NbBundle.getMessage(CloneAction.class,
                                    "MSG_CLONE_TO", prjName, target)); // NOI18N
                        } else {
                            HgUtils.outputMercurialTabInRed(
                                    NbBundle.getMessage(CloneAction.class,
                                    "MSG_EXTERNAL_CLONE_FROM", source)); // NOI18N
                            HgUtils.outputMercurialTabInRed(
                                    NbBundle.getMessage(CloneAction.class,
                                    "MSG_EXTERNAL_CLONE_TO", target)); // NOI18N

                        }
                        HgUtils.outputMercurialTab(""); // NOI18N

                        if (isLocalClone){
                            SwingUtilities.invokeLater(doOpenProject);
                        } else if (HgModuleConfig.getDefault().getShowCloneCompleted()) {
                            CloneCompleted cc = new CloneCompleted(cloneFolder);
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
                    //#121581: Work around for ini4j bug on Windows not handling single '\' correctly
                    // hg clone creates the default hgrc, we just overwrite it's contents with 
                    // default path contianing '\\'
                    if(isLocalClone && Utilities.isWindows()){                       
                        File f = new File(cloneFolder.getAbsolutePath() + File.separator + ".hg", "hgrc");
                        if(f.isFile() && f.canWrite()){
                            FileWriter fw = null;
                            try {
                                fw = new FileWriter(f);
                                fw.write("[paths]\n");
                                fw.write("default = " + source.replace("\\", "\\\\") + "\n");
                            } catch (IOException ex) {
                                // Ignore
                            } finally {
                                try {
                                    fw.close();
                                } catch (IOException ex) {
                                    // Ignore
                                }
                            }
                        }
                    }
                    if(!isLocalClone){
                        HgUtils.outputMercurialTabInRed(NbBundle.getMessage(CloneAction.class, "MSG_CLONE_DONE")); // NOI18N
                        HgUtils.outputMercurialTab(""); // NOI18N
                    }
                }
            }
        };
        support.start(rp, source, org.openide.util.NbBundle.getMessage(CloneAction.class, "LBL_Clone_Progress", source)); // NOI18N
    }

    public boolean isEnabled() {
        return HgUtils.getRootFile(context) != null;
    }
}
