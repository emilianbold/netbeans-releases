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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.ui.actions;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.ErrorManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.spi.project.support.ProjectOperations;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Project Copy operation implementation is copied from 
 * org.netbeans.modules.project.uiapi.DefaultProjectOperationsImplementation
 * and modified to be able to use new PhpProjectCopyPanel.hasExternalSources();
 * It should return true only if src dir is outside of project dir.
 * Previously it returned true if src dir is outside or equal to project dir.
 * We want to copy project with src dir equal to project dir.
 * 
 * @author avk
 * @see org.netbeans.modules.project.uiapi.DefaultProjectOperationsImplementation
 */
public class PhpProjectOperationsImplementation {

    //fractions how many time will be spent in some phases of the move and copy operation
    //the rename and delete operation use a different approach:
    private static final double NOTIFY_WORK = 0.1;
    private static final double FIND_PROJECT_WORK = 0.1;
    static final int MAX_WORK = 100;

    /* (non-Javadoc)
     * copied from 
     * org.netbeans.modules.project.uiapi.DefaultProjectOperationsImplementation.
     * reason: default copy doesn't process project if source dir = project dir.
     */
    public static void copyProject(final Project project) {
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(PhpProjectOperationsImplementation.class, "LBL_Copy_Project_Handle"));
        final ProjectCopyPanel panel = new ProjectCopyPanel(handle, project, false);
        //#76559
        handle.start(MAX_WORK);

        showConfirmationDialog(panel, project, NbBundle.getMessage(PhpProjectOperationsImplementation.class, "LBL_Copy_Project_Caption"), "Copy_Button", null, false, new Executor() { // NOI18N
            public void execute() throws Exception {
                String nueName = panel.getNewName();
                File newTarget = FileUtil.normalizeFile(panel.getNewDirectory());

                FileObject newTargetFO = FileUtil.toFileObject(newTarget);
                if (newTargetFO == null) {
                    newTargetFO = createFolder(newTarget.getParentFile(), newTarget.getName());
                }
                doCopyProject(handle, project, nueName, newTargetFO);
            }
        });
    }

    /* (non-Javadoc)
     * copied from 
     * org.netbeans.modules.project.uiapi.DefaultProjectOperationsImplementation
     */
    static void doCopyProject(ProgressHandle handle, Project project, String nueName, FileObject newTarget) throws Exception {
        try {
            int totalWork = MAX_WORK;


            double currentWorkDone = 0;

            handle.progress((int) currentWorkDone);

            ProjectOperations.notifyCopying(project);

            handle.progress((int) (currentWorkDone = totalWork * NOTIFY_WORK));

            FileObject target = newTarget.createFolder(nueName);
            FileObject projectDirectory = project.getProjectDirectory();
            List<FileObject> toCopyList = Arrays.asList(projectDirectory.getChildren());

            double workPerFileAndOperation = totalWork * (1.0 - 2 * NOTIFY_WORK - FIND_PROJECT_WORK) / toCopyList.size();

            for (FileObject toCopy : toCopyList) {
                doCopy(project, toCopy, target);

                int lastWorkDone = (int) currentWorkDone;

                currentWorkDone += workPerFileAndOperation;

                if (lastWorkDone < (int) currentWorkDone) {
                    handle.progress((int) currentWorkDone);
                }
            }

            //#64264: the non-project cache can be filled with incorrect data (gathered during the project copy phase), clear it:
            ProjectManager.getDefault().clearNonProjectCache();
            Project nue = ProjectManager.getDefault().findProject(target);

            assert nue != null;

            handle.progress((int) (currentWorkDone += totalWork * FIND_PROJECT_WORK));

            ProjectOperations.notifyCopied(project, nue, FileUtil.toFile(project.getProjectDirectory()), nueName);

            handle.progress((int) (currentWorkDone += totalWork * NOTIFY_WORK));

            ProjectManager.getDefault().saveProject(nue);

            open(nue, false);

            handle.progress(totalWork);
        } catch (Exception e) {
            ErrorManager.getDefault().annotate(e, NbBundle.getMessage(PhpProjectOperationsImplementation.class, "ERR_Cannot_Move", e.getLocalizedMessage()));
            throw e;
        } finally {
            handle.finish();
        }
    }

    /* (non-Javadoc)
     * copied from 
     * org.netbeans.modules.project.uiapi.DefaultProjectOperationsImplementation
     */
    private static void doCopy(Project original, FileObject from, FileObject toParent) throws IOException {
        if (!VisibilityQuery.getDefault().isVisible(from)) {
            //Do not copy invisible files/folders.
            return ;
        }
        
        if (!original.getProjectDirectory().equals(FileOwnerQuery.getOwner(from).getProjectDirectory())) {
            return ;
        }
        
        //#109580
        if (SharabilityQuery.getSharability(FileUtil.toFile(from)) == SharabilityQuery.NOT_SHARABLE) {
            return;
        }
        
        if (from.isFolder()) {
            FileObject copy = toParent.createFolder(from.getNameExt());
            for (FileObject kid : from.getChildren()) {
                doCopy(original, kid, copy);
            }
        } else {
            assert from.isData();
            FileObject target = FileUtil.copyFile(from, toParent, from.getName(), from.getExt());
        }
    }

    /* (non-Javadoc)
     * copied from 
     * org.netbeans.modules.project.uiapi.DefaultProjectOperationsImplementation
     */
    private static FileObject createFolder(File parent, String name) throws IOException {
        FileObject path = FileUtil.toFileObject(parent);
        if (path != null) {
            return path.createFolder(name);
        } else {
            return createFolder(parent.getParentFile(), parent.getName()).createFolder(name);
        }
    }
    
    
    /* (non-Javadoc)
     * copied from 
     * org.netbeans.modules.project.uiapi.DefaultProjectOperationsImplementation
     */
    private static JComponent wrapPanel(JComponent component) {
        component.setBorder(new EmptyBorder(12, 12, 12, 12));
        
        return component;
    }
    
    /* (non-Javadoc)
     * copied from 
     * org.netbeans.modules.project.uiapi.DefaultProjectOperationsImplementation
     */
    private static void showConfirmationDialog(final JComponent panel, Project project, String caption, String confirmButton, String cancelButton, boolean doSetMessageType, final Executor executor) {
        final JButton confirm = new JButton();
        Mnemonics.setLocalizedText(confirm, NbBundle.getMessage(PhpProjectOperationsImplementation.class, "LBL_" + confirmButton));
        final JButton cancel  = new JButton(cancelButton == null ?
              NbBundle.getMessage(PhpProjectOperationsImplementation.class, "LBL_Cancel_Button")
            : NbBundle.getMessage(PhpProjectOperationsImplementation.class, "LBL_" + cancelButton));
        
        confirm.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpProjectOperationsImplementation.class, "ACSD_" + confirmButton));
        cancel.getAccessibleContext().setAccessibleDescription(cancelButton == null ?
              NbBundle.getMessage(PhpProjectOperationsImplementation.class, "ACSD_Cancel_Button")
            : NbBundle.getMessage(PhpProjectOperationsImplementation.class, "ACSD_" + cancelButton));
        
        assert panel instanceof InvalidablePanel;
        
        ((InvalidablePanel) panel).addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                confirm.setEnabled(((InvalidablePanel) panel).isPanelValid());
            }
        });
        
        confirm.setEnabled(((InvalidablePanel) panel).isPanelValid());
        
        final Dialog[] dialog = new Dialog[1];
        
        DialogDescriptor dd = new DialogDescriptor(doSetMessageType ? panel : wrapPanel(panel), caption, true, new Object[] {confirm, cancel}, cancelButton != null ? cancel : confirm, DialogDescriptor.DEFAULT_ALIGN, null, new ActionListener() {
            private boolean operationRunning;
            public void actionPerformed(ActionEvent e) {
                //#65634: making sure that the user cannot close the dialog before the operation is finished:
                if (operationRunning) {
                    return ;
                }
                
                if (dialog[0] instanceof JDialog) {
                    ((JDialog) dialog[0]).getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
                    ((JDialog) dialog[0]).setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                }
                
                operationRunning = true;
		
                if (e.getSource() == confirm) {
                    confirm.setEnabled(false);
                    cancel.setEnabled(false);
                    ((InvalidablePanel) panel).showProgress();
                    
                    Component findParent = panel;
                    
                    while (findParent != null && !(findParent instanceof Window)) {
                        findParent = findParent.getParent();
                    }
                    
                    if (findParent != null) {
                        ((Window) findParent).pack();
                    }
                    
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            Exception e = null;
                            
                            try {
                                executor.execute();
                            } catch (Exception ex) {
                                e = ex;
                            }
                            
                            final Exception ex = e;
                            
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    dialog[0].setVisible(false);
                                    
                                    if (ex != null) {
                                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                                        ErrorManager.getDefault().notify(ErrorManager.USER, ex);
                                    }
                                }
                            });
                        }
                    });
                } else {
                    dialog[0].setVisible(false);
                }
            }
        });
        
        if (doSetMessageType) {
            dd.setMessageType(NotifyDescriptor.QUESTION_MESSAGE);
        }
        
        dd.setClosingOptions(new Object[0]);
        
        dialog[0] = DialogDisplayer.getDefault().createDialog(dd);
        
        dialog[0].setVisible(true);
        
        dialog[0].dispose();
        dialog[0] = null;
    }
    
    
    /* (non-Javadoc)
     * copied from 
     * org.netbeans.modules.project.uiapi.DefaultProjectOperationsImplementation
     */
    static String computeError(File location, String projectNameText, boolean pureRename) {
        return computeError(location, projectNameText, null, pureRename);
    }
    
    /* (non-Javadoc)
     * copied from 
     * org.netbeans.modules.project.uiapi.DefaultProjectOperationsImplementation
     */
    static String computeError(File location, String projectNameText, String projectFolderText, boolean pureRename) {
        File parent = location;
        if (!location.exists()) {
            //if some dirs in teh chain are not created, consider it ok.
            parent = location.getParentFile();
            while (parent != null && !parent.exists()) {
                parent = parent.getParentFile();
            }
            if (parent == null) {
                return NbBundle.getMessage(PhpProjectOperationsImplementation.class, "ERR_Location_Does_Not_Exist");
            }
        }
        
        if (!parent.canWrite()) {
            return NbBundle.getMessage(PhpProjectOperationsImplementation.class, "ERR_Location_Read_Only");
        }
        
        if (projectNameText.length() == 0) {
            return NbBundle.getMessage(PhpProjectOperationsImplementation.class, "ERR_Project_Name_Must_Entered");
        }
        
        File projectFolderFile = null;
        if (projectFolderText == null) {
            projectFolderFile = new File(location, projectNameText);
        } else {
            projectFolderFile = new File(projectFolderText);
        }
        
        if (projectFolderFile.exists() && !pureRename) {
            return NbBundle.getMessage(PhpProjectOperationsImplementation.class, "ERR_Project_Folder_Exists");
        }
        
	if (projectNameText.indexOf('/') != -1 || projectNameText.indexOf('\\') != -1) {
	    return NbBundle.getMessage(PhpProjectOperationsImplementation.class, "ERR_Not_Valid_Filename", projectNameText);
	}
        
        return null;
    }
    
    
    /* (non-Javadoc)
     * copied from 
     * org.netbeans.modules.project.uiapi.DefaultProjectOperationsImplementation
     */
    private static void open(final Project prj, final boolean setAsMain) {
        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                OpenProjects.getDefault().open(new Project[] {prj}, false);
                if (setAsMain) {
                    OpenProjects.getDefault().setMainProject(prj);
                }
            }
        });
    }
    
    /* (non-Javadoc)
     * copied from 
     * org.netbeans.modules.project.uiapi.DefaultProjectOperationsImplementation
     */
    static interface Executor {
        public void execute() throws Exception;
    }
    
    /* (non-Javadoc)
     * copied from 
     * org.netbeans.modules.project.uiapi.DefaultProjectOperationsImplementation
     */
    public static interface InvalidablePanel {
        public void addChangeListener(ChangeListener l);
        public void removeChangeListener(ChangeListener l);
        public boolean isPanelValid();
        public void showProgress();
    }
    //</editor-fold>
}
