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
        Runnable r = new Runnable() {
            public void run() {
                deleteProject(project, new GUIUserInputHandler());
            }
        };
        
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }
    
    /*package private*/static void deleteProject(final Project project, final UserInputHandler handler) {
        String displayName = getDisplayName(project);
        FileObject projectFolder = project.getProjectDirectory();
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "delete started: " + displayName); // NOI18N
        }
        
        List/*<FileObject>*/ metadataFiles = ProjectOperations.getMetadataFiles(project);
        List/*<FileObject>*/ dataFiles = ProjectOperations.getDataFiles(project);
        List/*<FileObject>*/ allFiles = new ArrayList/*<FileObject>*/();
        
        allFiles.addAll(metadataFiles);
        allFiles.addAll(dataFiles);
        
        for (Iterator i = allFiles.iterator(); i.hasNext(); ) {
            FileObject f = (FileObject) i.next();
            
            if (!FileUtil.isParentOf(projectFolder, f)) {
                i.remove();
            }
        }
        
        int userAnswer = handler.userConfirmation(displayName, FileUtil.getFileDisplayName(projectFolder), !dataFiles.isEmpty());
        List/*<FileObject>*/ toDeleteImpl = null;
        
        switch (userAnswer) {
            case UserInputHandler.USER_CANCEL:
                return ;
            case UserInputHandler.USER_OK_METADATA:
                toDeleteImpl = metadataFiles;
                break;
            case UserInputHandler.USER_OK_ALL:
                toDeleteImpl = allFiles;
                break;
            default:
                throw new IllegalStateException("Invalid user answer: " + userAnswer);
        }
        
        final ProgressHandle handle = handler.getProgressHandle();
        final List/*<FileObject>*/ toDelete = toDeleteImpl;
        final boolean[] result = new boolean[1];
        
        OpenProjects.getDefault().close(new Project[] {project});
        
        handler.delete(new Runnable() {
            public void run() {
                result[0] = performDelete(project, toDelete, handle);
            }
        });
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "delete done: " + displayName); // NOI18N
        }
    }
    
    /*package private*/interface UserInputHandler {
        
        public int USER_CANCEL = 1;
        public int USER_OK_METADATA = 2;
        public int USER_OK_ALL = 3;
        
        public abstract int userConfirmation(String displayName, String projectFolder, boolean enableData);
        
        public abstract ProgressHandle getProgressHandle();
        
        public abstract void delete(Runnable r);
        
    }
    
    private static final class GUIUserInputHandler implements UserInputHandler {
        
        public int userConfirmation(String displayName, String projectFolder, boolean enableData) {
            DefaultProjectDeletePanel deletePanel = new DefaultProjectDeletePanel(displayName, projectFolder, enableData);
            
            String caption = NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Delete_Project_Caption");
            
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(deletePanel, caption, NotifyDescriptor.YES_NO_OPTION);
            
            if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION) {
                if (deletePanel.isDeleteSources()) {
                    return USER_OK_ALL;
                } else {
                    return USER_OK_METADATA;
                }
            } else {
                return USER_CANCEL;
            }
        }
        
        private ProgressHandle handle = null;
        
        public synchronized ProgressHandle getProgressHandle() {
            if (handle == null) {
                handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Delete_Project_Caption"));
            }
            
            return handle;
        }
        
        public void delete(final Runnable r) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    r.run();
		    
                    if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                        ERR.log(ErrorManager.INFORMATIONAL, "delete finished"); // NOI18N
                    }
                }
            });
        }
        
    }
    
    private static JComponent createProgressDialog(ProgressHandle handle) {
        JPanel dialog = new JPanel();
        
        GridBagConstraints gridBagConstraints;
        
        JLabel jLabel1 = new JLabel();
        JComponent progress = ProgressHandleFactory.createProgressComponent(handle);
        JPanel padding = new JPanel();
        
        dialog.setLayout(new java.awt.GridBagLayout());
        
        jLabel1.setText(NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Deleting_Project"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(12, 12, 0, 12);
        dialog.add(jLabel1, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(12, 12, 0, 12);
        dialog.add(progress, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL | GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(12, 12, 12, 12);
        dialog.add(padding, gridBagConstraints);
        
        return dialog;
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Copy Operation">
    public static void copyProject(final Project project) {
        final ProgressHandle handle = ProgressHandleFactory.createHandle("Copy Project");
        final ProjectCopyPanel panel = new ProjectCopyPanel(handle, project, false);
        
        showConfirmationDialog(panel, project, "Copy Project", "Copy", new Executor() {
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
        
        showConfirmationDialog(panel, project, "Move Project", "Move", new Executor() {
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
        
        showConfirmationDialog(panel, project, "Rename Project", "Rename", new Executor() {
            
            public void execute() {
                String nueName = panel.getNewName();
                
                doMoveProject(handle, project, nueName, project.getProjectDirectory().getParent());
            }
        });
    }
    
    /*package private for tests*/ static void doMoveProject(ProgressHandle handle, Project project, String nueName, FileObject newTarget) {
        try {
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
                
                toCopy.delete();
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
    
    private static void showConfirmationDialog(final JComponent panel, Project project, String caption, String confirmButton, final Executor executor) {
        final JButton confirm = new JButton(confirmButton);
        final JButton cancel  = new JButton("Cancel");
        
        assert panel instanceof InvalidablePanel;
        
        ((InvalidablePanel) panel).addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                confirm.setEnabled(((InvalidablePanel) panel).isValid());
            }
        });
        
        confirm.setEnabled(panel.isValid());
        
        final Dialog[] dialog = new Dialog[1];
        
        DialogDescriptor dd = new DialogDescriptor(wrapPanel(panel), caption, true, new Object[] {confirm, cancel}, confirm, DialogDescriptor.DEFAULT_ALIGN, null, new ActionListener() {
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
                }
            }
        });
        
        dialog[0] = DialogDisplayer.getDefault().createDialog(dd);
        
        dialog[0].setVisible(true);
        
        dialog[0] = null;
    }
    
    private static interface Executor {
        public void execute();
    }
    
    public static interface InvalidablePanel {
        public void addChangeListener(ChangeListener l);
        public void removeChangeListener(ChangeListener l);
        public boolean isValid();
        public void showProgress();
    }
    //</editor-fold>
    
}
