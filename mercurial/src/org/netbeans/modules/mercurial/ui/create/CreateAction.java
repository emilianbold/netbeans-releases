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
package org.netbeans.modules.mercurial.ui.create;

import java.awt.Dialog;
import java.io.File;
import java.util.Calendar;
import java.util.logging.Level;
import javax.swing.event.DocumentEvent;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.openide.util.RequestProcessor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.netbeans.api.project.Project;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;

/**
 * Create action for mercurial: 
 * hg init - create a new repository in the given directory
 * 
 * @author John Rice
 */
public class CreateAction extends ContextAction {
    
    private final VCSContext context;
    Map<File, FileInformation> repositoryFiles = new HashMap<File, FileInformation>();

    public CreateAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }

    public boolean isEnabled() {
        // If it is not a mercurial managed repository enable action
        File root = HgUtils.getRootFile(context);
        File [] files = context.getRootFiles().toArray(new File[context.getRootFiles().size()]);
        if ( files == null || files.length == 0) 
            return false;
        
        if (root == null)
            return true;
        else
            return false;
    } 

    private File getCommonAncestor(File firstFile, File secondFile) {
        if (firstFile.equals(secondFile)) return firstFile;

        File tempFirstFile = firstFile;
        while (tempFirstFile != null) {
            File tempSecondFile = secondFile;
            while (tempSecondFile != null) {
                if (tempFirstFile.equals(tempSecondFile))
                    return tempSecondFile;
                tempSecondFile = tempSecondFile.getParentFile();
            }
            tempFirstFile = tempFirstFile.getParentFile();
        }
        return null;
    }

    private File getCommonAncestor(File[] files) {
        File f1 = files[0];

        for (int i = 1; i < files.length; i++) {
            File f = getCommonAncestor(f1, files[i]);
            if (f == null) {
                Mercurial.LOG.log(Level.SEVERE, "Unable to get common parent of {0} and {1} ", // NOI18N
                        new Object[] {f1.getAbsolutePath(), files[i].getAbsolutePath()});
                // XXX not sure wat to do at this point
            } else {
                f1 = f;
            }
        }
        return f1;
    }

    public void performAction(ActionEvent e) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                performCreate();
            }
        });
    }

    private void performCreate () {
        final Mercurial hg = Mercurial.getInstance();

        final File rootToManage = selectRootToManage();
        if (rootToManage == null) {
            return;
        }

        RequestProcessor rp = hg.getRequestProcessor(rootToManage);
        HgProgressSupport supportCreate = new HgProgressSupport() {
            public void perform() {

                try {
                    OutputLogger logger = getLogger();
                    logger.outputInRed(
                            NbBundle.getMessage(CreateAction.class, "MSG_CREATE_TITLE")); // NOI18N
                    logger.outputInRed(
                            NbBundle.getMessage(CreateAction.class, "MSG_CREATE_TITLE_SEP")); // NOI18N
                    logger.output(
                            NbBundle.getMessage(CreateAction.class,
                            "MSG_CREATE_INIT", rootToManage)); // NOI18N
                    HgCommand.doCreate(rootToManage, logger);
                    hg.versionedFilesChanged();
                    hg.refreshAllAnnotations();
                } catch (HgException ex) {
                    NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
                    DialogDisplayer.getDefault().notifyLater(e);
                }
            }
        };
        supportCreate.start(rp, rootToManage,
                org.openide.util.NbBundle.getMessage(CreateAction.class, "MSG_Create_Progress")); // NOI18N

        HgProgressSupport supportAdd = new HgProgressSupport() {
            public void perform() {
                OutputLogger logger = getLogger();
                try {
                    FileStatusCache cache = hg.getFileStatusCache();
                    Calendar start = Calendar.getInstance();
                    repositoryFiles = HgCommand.getUnknownStatus(rootToManage, rootToManage);
                    Calendar end = Calendar.getInstance();
                    Mercurial.LOG.log(Level.FINE, "getUnknownStatus took {0} millisecs", end.getTimeInMillis() - start.getTimeInMillis()); // NOI18N
                    logger.output(
                            NbBundle.getMessage(CreateAction.class,
                            "MSG_CREATE_ADD", repositoryFiles.keySet().size())); // NOI18N
                    start = Calendar.getInstance();
                    cache.addToCache(repositoryFiles.keySet());
                    end = Calendar.getInstance();
                    Mercurial.LOG.log(Level.FINE, "addUnknownsToCache took {0} millisecs", end.getTimeInMillis() - start.getTimeInMillis()); // NOI18N
                    if (repositoryFiles.keySet().size() < OutputLogger.MAX_LINES_TO_PRINT) {
                        for (File f : repositoryFiles.keySet()) {
                            logger.output("\t" + f.getAbsolutePath());  //NOI18N
                        }
                    }
                    HgUtils.createIgnored(rootToManage);
                    logger.output(""); // NOI18N
                    logger.outputInRed(NbBundle.getMessage(CreateAction.class, "MSG_CREATE_DONE_WARNING")); // NOI18N
                } catch (HgException ex) {
                    NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
                    DialogDisplayer.getDefault().notifyLater(e);
                } finally {
                    logger.outputInRed(NbBundle.getMessage(CreateAction.class, "MSG_CREATE_DONE")); // NOI18N
                    logger.output(""); // NOI18N
                }
            }
        };
        supportAdd.start(rp, rootToManage,
                org.openide.util.NbBundle.getMessage(CreateAction.class, "MSG_Create_Add_Progress")); // NOI18N
    }

    private File selectRootToManage() {
        File rootPath = getSuggestedRoot();

        final CreatePanel panel = new CreatePanel();
        panel.lblMessage.setVisible(false);
        final DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(CreateAction.class, "LBL_Create_Panel_Label"), //NOI18N
                true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(CreatePanel.class), null);
        dd.setValid(false);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);

        final RequestProcessor.Task validateTask = Mercurial.getInstance().getRequestProcessor().create(new Runnable() {
            public void run() {
                String validatedPath = panel.tfRootPath.getText();
                String errorMessage = null;
                boolean valid = true;
                File dir = new File(validatedPath);
                // must be an existing directory
                if (!dir.isDirectory()) {
                    errorMessage = NbBundle.getMessage(CreateAction.class, "LBL_Create_Panel_Error_Directory"); //NOI18N
                    if (Mercurial.LOG.isLoggable(Level.FINE) && dir.exists()) {
                        Mercurial.LOG.fine("CreateAction.selectRootToManage.validateTask: selected a file: " + dir); //NOI18N
                    }
                    valid = false;
                }
                if (valid) {
                    if (Thread.interrupted()) {
                        return;
                    }
                    // children can't be versioned
                    File[] children = dir.listFiles();
                    for (File f : children) {
                        File repoRoot = null;
                        if (f.isDirectory() && (repoRoot = Mercurial.getInstance().getRepositoryRoot(f)) != null) {
                            valid = false;
                            if (Mercurial.LOG.isLoggable(Level.FINE) && dir.exists()) {
                                Mercurial.LOG.fine("CreateAction.selectRootToManage.validateTask: file is versioned: " + f + ", root: " + repoRoot); //NOI18N
                            }
                            errorMessage = NbBundle.getMessage(CreateAction.class, "LBL_Create_Panel_Error_Versioned"); //NOI18N
                            break;
                        }
                    }
                }
                if (Thread.interrupted()) {
                    return;
                }
                if (valid) {
                    // warning message (validation does not fail) for directories under a project
                    FileObject fo = FileUtil.toFileObject(dir);
                    Project p = FileOwnerQuery.getOwner(fo);
                    if (p != null) {
                        FileObject projectDir = p.getProjectDirectory();
                        if (FileUtil.isParentOf(projectDir, fo)) {
                            errorMessage = NbBundle.getMessage(CreateAction.class, "LBL_Create_Panel_Warning_Under_Project"); //NOI18N
                        }
                    }
                }
                if (Thread.interrupted()) {
                    return;
                }
                dd.setValid(valid);
                if (errorMessage != null) {
                    panel.lblMessage.setText(errorMessage);
                    panel.lblMessage.setForeground(javax.swing.UIManager.getDefaults().getColor(valid ? "nb.warningForeground" : "nb.errorForeground")); //NOI18N
                }
                panel.lblMessage.setVisible(errorMessage != null);
                panel.invalidate();
            }
        });

        panel.tfRootPath.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                validate();
            }

            public void removeUpdate(DocumentEvent e) {
                validate();
            }

            public void changedUpdate(DocumentEvent e) {
                validate();
            }

            private void validate () {
                validateTask.cancel();
                dd.setValid(false);
                validateTask.schedule(300);
            }
        });
        panel.tfRootPath.setText(rootPath == null ? "" : rootPath.getAbsolutePath()); //NOI18N
        do {
            dialog.setVisible(true);
            if (dd.getValue() == DialogDescriptor.OK_OPTION) {
                rootPath = new File(panel.tfRootPath.getText());
                validateTask.run();
            } else {
                rootPath = null;
            }
        } while (!dd.isValid() && dd.getValue() == DialogDescriptor.OK_OPTION);

        return rootPath;
    }

    /**
     * Returns a common ancestor for all context rootfiles
     * If these belong to a project, returns a common ancestor of all rootfiles and the project folder
     * @return
     */
    private File getSuggestedRoot () {
        final File [] files = context.getRootFiles().toArray(new File[context.getRootFiles().size()]);
        if (files == null || files.length == 0) return null;

        final Project proj = HgUtils.getProject(context);
        final File projFile = HgUtils.getProjectFile(proj);

        File root = null;
        root = getCommonAncestor(files);
        if (Mercurial.LOG.isLoggable(Level.FINER)) {
            Mercurial.LOG.finer("CreateAction.getSuggestedRoot: common root for " + context.getRootFiles() + ": " + root); //NOI18N
        }

        if (projFile != null) {
            root = getCommonAncestor(root, projFile);
            if (Mercurial.LOG.isLoggable(Level.FINER)) {
                Mercurial.LOG.finer("CreateAction.getSuggestedRoot: root with project at " + projFile + ": " + root); //NOI18N
            }
        }
        return root;
    }
}
