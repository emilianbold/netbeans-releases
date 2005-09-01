/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.uiapi;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.support.ProjectOperations;
import org.netbeans.api.queries.VisibilityQuery;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Jan Lahoda
 */
public final class DefaultProjectOperationsImplementation {
    
    private static final ErrorManager ERR = ErrorManager.getDefault(); // NOI18N
    
    private DefaultProjectOperationsImplementation() {
    }
    
    private static String getDisplayName(Project project) {
        return ProjectUtils.getInformation(project).getDisplayName();
    }
 
    //<editor-fold defaultstate="collapsed" desc="Delete Operation">
    /**
     * @return true if success
     */
    private static boolean performDelete(Project project, List/*FileObject>*/ toDelete, ProgressHandle handle) {
        try {
            handle.start(toDelete.size() + 1 /*clean*/);
            
            int done = 0;
            
            handle.progress(NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Progress_Cleaning_Project"));
            
            ProjectOperations.notifyDeleting(project);
            
            handle.progress(++done);
            
            for (Iterator i = toDelete.iterator(); i.hasNext(); ) {
                FileObject f = (FileObject) i.next();
                
                handle.progress(NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Progress_Deleting_File", new Object[] {FileUtil.getFileDisplayName(f)}));
                
                if (f != null)
                    f.delete();
                
                handle.progress(++done);
            }
            
            FileObject projectFolder = project.getProjectDirectory();
            
            if (projectFolder.getChildren().length == 0) {
                //empty, delete:
                projectFolder.delete();
            }
            
            handle.finish();
            
            ProjectOperations.notifyDeleted(project);
            return true;
        } catch (IOException e) {
            String displayName = getDisplayName(project);
            String message     = NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Project_cannot_be_deleted.", new Object[] {displayName});
            
            ErrorManager.getDefault().annotate(e, message);
            ErrorManager.getDefault().notify(ErrorManager.USER, e);
            
            return false;
        }
    }
    
    public static void deleteProject(final Project project) {
        deleteProject(project, new GUIUserInputHandler());
    }
    
    static void deleteProject(final Project project, UserInputHandler handler) {
        String displayName = getDisplayName(project);
        FileObject projectFolder = project.getProjectDirectory();
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "delete started: " + displayName); // NOI18N
        }
        
        final List/*<FileObject>*/ metadataFiles = ProjectOperations.getMetadataFiles(project);
        final List/*<FileObject>*/ dataFiles = ProjectOperations.getDataFiles(project);
        final List/*<FileObject>*/ allFiles = new ArrayList/*<FileObject>*/();
        
        allFiles.addAll(metadataFiles);
        allFiles.addAll(dataFiles);
        
        for (Iterator i = allFiles.iterator(); i.hasNext(); ) {
            FileObject f = (FileObject) i.next();
            
            if (!FileUtil.isParentOf(projectFolder, f)) {
                i.remove();
            }
        }
        
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Delete_Project_Caption"));
        final DefaultProjectDeletePanel deletePanel = new DefaultProjectDeletePanel(handle, displayName, FileUtil.getFileDisplayName(projectFolder), !dataFiles.isEmpty());
        
        String caption = NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Delete_Project_Caption");
        
        handler.showConfirmationDialog(deletePanel, project, caption, "Yes", "No", true, new Executor() {
            public void execute() {
                OpenProjects.getDefault().close(new Project[] {project});
                
                if (deletePanel.isDeleteSources()) {
                    performDelete(project, allFiles, handle);
                } else {
                    performDelete(project, metadataFiles, handle);
                }
            }
        });
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "delete done: " + displayName); // NOI18N
        }
    }
    
    static interface UserInputHandler {
        void showConfirmationDialog(final JComponent panel, Project project, String caption, String confirmButton, String cancelButton, boolean doSetMessageType, final Executor executor);
    }
    
    private static final class GUIUserInputHandler implements UserInputHandler {
        
        public void showConfirmationDialog(final JComponent panel, Project project, String caption, String confirmButton, String cancelButton, boolean doSetMessageType, final Executor executor) {
            DefaultProjectOperationsImplementation.showConfirmationDialog(panel, project, caption, confirmButton, cancelButton, doSetMessageType, executor);
        }
        
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Copy Operation">
    public static void copyProject(final Project project) {
        final ProgressHandle handle = ProgressHandleFactory.createHandle("Copy Project");
        final ProjectCopyPanel panel = new ProjectCopyPanel(handle, project, false);
        
        showConfirmationDialog(panel, project, "Copy Project", "Copy", null, false, new Executor() {
            public void execute() {
                String nueName = panel.getNewName();
                File newTarget = panel.getNewDirectory();
                FileObject newTargetFO = FileUtil.toFileObject(newTarget);
                
                doCopyProject(handle, project, nueName, newTargetFO);
            }
        });
    }
    
    /*package private for tests*/ static void doCopyProject(ProgressHandle handle, Project project, String nueName, FileObject newTarget) {
        try {
            ProjectOperations.notifyCopying(project);
            
            FileObject target = newTarget.createFolder(nueName);
            FileObject projectDirectory = project.getProjectDirectory();
            List/*<FileObject>*/ toCopyList = Arrays.asList(projectDirectory.getChildren());
            
            handle.start(toCopyList.size());
            
            int workDone = 0;
            
            for (Iterator i = toCopyList.iterator(); i.hasNext(); ) {
                FileObject toCopy = (FileObject) i.next();
                File       toCopyFile = FileUtil.toFile(toCopy);
                
                doCopy(project, toCopy, target);
                handle.progress(++workDone);
            }
            
            Project nue = ProjectManager.getDefault().findProject(target);
            
            assert nue != null;
            
            ProjectOperations.notifyCopied(project, nue, FileUtil.toFile(project.getProjectDirectory()), nueName);
            
            ProjectManager.getDefault().saveProject(nue);
            
            OpenProjects.getDefault().open(new Project[] {nue}, false);
            
            handle.finish();
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            e.printStackTrace();
        }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Move Operation">
    public static void moveProject(final Project project) {
        final ProgressHandle handle = ProgressHandleFactory.createHandle("Move Project");
        final ProjectCopyPanel panel = new ProjectCopyPanel(handle, project, true);
        
        showConfirmationDialog(panel, project, "Move Project", "Move", null, false, new Executor() {
            public void execute() {
                String nueName = panel.getNewName();
                File newTarget = panel.getNewDirectory();
                FileObject newTargetFO = FileUtil.toFileObject(newTarget);

                doMoveProject(handle, project, nueName, newTargetFO);
            }
        });
    }
    
    public static void renameProject(Project project) {
        renameProject(project, null);
    }
    
    public static void renameProject(final Project project, final String nueName) {
        final ProgressHandle handle = ProgressHandleFactory.createHandle("Rename Project");
        final DefaultProjectRenamePanel panel = new DefaultProjectRenamePanel(handle, project, nueName);
        
        showConfirmationDialog(panel, project, "Rename Project", "Rename", null, false, new Executor() {
            
            public void execute() {
                String nueName = panel.getNewName();
                
                doMoveProject(handle, project, nueName, project.getProjectDirectory().getParent());
            }
        });
    }
    
    /*package private for tests*/ static void doMoveProject(ProgressHandle handle, Project project, String nueName, FileObject newTarget) {
        try {
            Project main    = OpenProjects.getDefault().getMainProject();
            boolean wasMain = main != null && project.getProjectDirectory().equals(main.getProjectDirectory());
            
            ProjectOperations.notifyMoving(project);
            
            OpenProjects.getDefault().close(new Project[] {project});
            
            FileObject target = newTarget.createFolder(nueName);
            FileObject projectDirectory = project.getProjectDirectory();
            List/*<FileObject>*/ toMoveList = Arrays.asList(projectDirectory.getChildren());
            
            for (Iterator i = toMoveList.iterator(); i.hasNext(); ) {
                FileObject toCopy = (FileObject) i.next();
                
                doCopy(project, toCopy, target);
            }
            
            for (Iterator i = toMoveList.iterator(); i.hasNext(); ) {
                FileObject toCopy = (FileObject) i.next();
                File       toCopyFile = FileUtil.toFile(toCopy);
                
                doDelete(project, toCopy);
            }
            
            if (projectDirectory.getChildren().length == 0) {
                projectDirectory.delete();
            }
                
            ProjectOperations.notifyDeleted(project);
            
            Project nue = ProjectManager.getDefault().findProject(target);
            
            assert nue != null;
            
            ProjectOperations.notifyMoved(project, nue, FileUtil.toFile(project.getProjectDirectory()), nueName);
            
            ProjectManager.getDefault().saveProject(nue);
            
            OpenProjects.getDefault().open(new Project[] {nue}, false);
            
            if (wasMain) {
                OpenProjects.getDefault().setMainProject(nue);
            }
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Copy Move Utilities">
    private static void doCopy(Project original, FileObject from, FileObject toParent) throws IOException {
        if (!VisibilityQuery.getDefault().isVisible(from)) {
            //Do not copy invisible files/folders.
            return ;
        }
        
        if (!original.getProjectDirectory().equals(FileOwnerQuery.getOwner(from).getProjectDirectory())) {
            return ;
        }
        
        if (from.isFolder()) {
            FileObject copy = toParent.createFolder(from.getNameExt());
            FileObject[] kids = from.getChildren();
            for (int i = 0; i < kids.length; i++) {
                doCopy(original, kids[i], copy);
            }
        } else {
            assert from.isData();
            FileObject target = FileUtil.copyFile(from, toParent, from.getName(), from.getExt());
        }
    }
    
    private static boolean doDelete(Project original, FileObject toDelete) throws IOException {
        if (!original.getProjectDirectory().equals(FileOwnerQuery.getOwner(toDelete).getProjectDirectory())) {
            return false;
        }
        
        if (toDelete.isFolder()) {
            FileObject[] kids = toDelete.getChildren();
            boolean delete = true;
            
            for (int i = 0; i < kids.length; i++) {
                delete &= doDelete(original, kids[i]);
            }
            
            if (delete) {
                toDelete.delete();
            }
            
            return delete;
        } else {
            assert toDelete.isData();
            toDelete.delete();
            return true;
        }
    }
    
    private static JComponent wrapPanel(JComponent component) {
        JPanel result = new JPanel();
        
        result.setLayout(new GridBagLayout());
        
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        
        gridBagConstraints.insets = new Insets(12, 12, 12, 12);
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        result.add(component, gridBagConstraints);
        
        return result;
    }
    
    private static void showConfirmationDialog(final JComponent panel, Project project, String caption, String confirmButton, String cancelButton, boolean doSetMessageType, final Executor executor) {
        final JButton confirm = new JButton(confirmButton);
        final JButton cancel  = new JButton(cancelButton == null ? "Cancel" : cancelButton);
        
        assert panel instanceof InvalidablePanel;
        
        ((InvalidablePanel) panel).addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                confirm.setEnabled(((InvalidablePanel) panel).isPanelValid());
            }
        });
        
        confirm.setEnabled(((InvalidablePanel) panel).isPanelValid());
        
        final Dialog[] dialog = new Dialog[1];
        
        DialogDescriptor dd = new DialogDescriptor(doSetMessageType ? panel : wrapPanel(panel), caption, true, new Object[] {confirm, cancel}, cancelButton != null ? cancel : confirm, DialogDescriptor.DEFAULT_ALIGN, null, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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
                            executor.execute();
                            
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    dialog[0].setVisible(false);
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
        
        dialog[0] = DialogDisplayer.getDefault().createDialog(dd);
        
        dialog[0].setVisible(true);
        
        dialog[0] = null;
    }
    
    static interface Executor {
        public void execute();
    }
    
    public static interface InvalidablePanel {
        public void addChangeListener(ChangeListener l);
        public void removeChangeListener(ChangeListener l);
        public boolean isPanelValid();
        public void showProgress();
    }
    //</editor-fold>
    
}
