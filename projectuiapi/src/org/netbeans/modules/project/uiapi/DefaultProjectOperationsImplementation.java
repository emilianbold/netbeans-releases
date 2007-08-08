/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.uiapi;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.netbeans.spi.project.support.ProjectOperations;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ContextAwareAction;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 * @author Jan Lahoda
 */
public final class DefaultProjectOperationsImplementation {
    
    private static final ErrorManager ERR = ErrorManager.getDefault(); // NOI18N
    
    //fractions how many time will be spent in some phases of the move and copy operation
    //the rename and delete operation use a different approach:
    private static final double NOTIFY_WORK = 0.1;
    private static final double FIND_PROJECT_WORK = 0.1;
    static final int    MAX_WORK = 100;
    
    private DefaultProjectOperationsImplementation() {
    }
    
    private static String getDisplayName(Project project) {
        return ProjectUtils.getInformation(project).getDisplayName();
    }
 
    //<editor-fold defaultstate="collapsed" desc="Delete Operation">
    /**
     * @return true if success
     */
    private static boolean performDelete(Project project, List<FileObject> toDelete, ProgressHandle handle) throws Exception {
        try {
            handle.start(toDelete.size() + 1 /*clean*/);
            
            int done = 0;
            
            handle.progress(NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Progress_Cleaning_Project"));
            
            ProjectOperations.notifyDeleting(project);
            
            handle.progress(++done);
            
            for (FileObject f : toDelete) {
                handle.progress(NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Progress_Deleting_File", FileUtil.getFileDisplayName(f)));
                
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
        } catch (Exception e) {
            String displayName = getDisplayName(project);
            String message     = NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Project_cannot_be_deleted.", displayName);
            
            ErrorManager.getDefault().annotate(e, message);
            
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
        
        final List<FileObject> metadataFiles = ProjectOperations.getMetadataFiles(project);
        final List<FileObject> dataFiles = ProjectOperations.getDataFiles(project);
        final List<FileObject> allFiles = new ArrayList<FileObject>();
        
        allFiles.addAll(metadataFiles);
        allFiles.addAll(dataFiles);
        
        for (Iterator<FileObject> i = allFiles.iterator(); i.hasNext(); ) {
            FileObject f = i.next();
            if (!FileUtil.isParentOf(projectFolder, f)) {
                i.remove();
            }
        }
        
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Delete_Project_Caption"));
        final DefaultProjectDeletePanel deletePanel = new DefaultProjectDeletePanel(handle, displayName, FileUtil.getFileDisplayName(projectFolder), !dataFiles.isEmpty());
        
        String caption = NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Delete_Project_Caption");
        
        handler.showConfirmationDialog(deletePanel, project, caption, "Yes_Button", "No_Button", true, new Executor() { // NOI18N
            public void execute() throws Exception {
                close(project);
                
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
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Copy_Project_Handle"));
        final ProjectCopyPanel panel = new ProjectCopyPanel(handle, project, false);
        //#76559
        handle.start(MAX_WORK);
        
        showConfirmationDialog(panel, project, NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Copy_Project_Caption"), "Copy_Button", null, false, new Executor() { // NOI18N
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
    
    /*package private for tests*/ static void doCopyProject(ProgressHandle handle, Project project, String nueName, FileObject newTarget) throws Exception {
        try {
            int totalWork = MAX_WORK;
            
            
            double currentWorkDone = 0;
            
            handle.progress((int) currentWorkDone);
            
            ProjectOperations.notifyCopying(project);
            
            handle.progress((int) (currentWorkDone = totalWork * NOTIFY_WORK));
            
            FileObject target = newTarget.createFolder(nueName);
            FileObject projectDirectory = project.getProjectDirectory();
            List<FileObject> toCopyList = new ArrayList<FileObject>();
            for (FileObject child : projectDirectory.getChildren()) {
                if (child.isValid()) {
                    toCopyList.add(child);
                }
            }
            
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
            handle.finish();
        } catch (Exception e) {
            ErrorManager.getDefault().annotate(e, NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "ERR_Cannot_Move", e.getLocalizedMessage()));
            throw e;
        }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Move Operation">
    public static void moveProject(final Project project) {
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Move_Project_Handle"));
        final ProjectCopyPanel panel = new ProjectCopyPanel(handle, project, true);
        //#76559
        handle.start(MAX_WORK);
        
        showConfirmationDialog(panel, project, NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Move_Project_Caption"), "Move_Button", null, false, new Executor() { // NOI18N
            public void execute() throws Exception {
                String nueFolderName = panel.getProjectFolderName();
                String nueProjectName = panel.getNewName();
                File newTarget = FileUtil.normalizeFile(panel.getNewDirectory());
                
                FileObject newTargetFO = FileUtil.toFileObject(newTarget);
                if (newTargetFO == null) {
                    newTargetFO = createFolder(newTarget.getParentFile(), newTarget.getName());
                }
                

                doMoveProject(handle, project, nueFolderName, nueProjectName, newTargetFO, "ERR_Cannot_Move");
            }
        });
    }
    
    public static void renameProject(Project project) {
        renameProject(project, null);
    }
    
    public static void renameProject(final Project project, final String nueName) {
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Rename_Project_Handle"));
        final DefaultProjectRenamePanel panel = new DefaultProjectRenamePanel(handle, project, nueName);

        //#76559
        handle.start(MAX_WORK);
        
        showConfirmationDialog(panel, project, NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Rename_Project_Caption"), "Rename_Button", null, false, new Executor() { // NOI18N
            public void execute() throws Exception {
                String nueName = panel.getNewName();
                
                if (panel.getRenameProjectFolder()) {

                    doMoveProject(handle, project, nueName, nueName, project.getProjectDirectory().getParent(), "ERR_Cannot_Rename");
                } else {
                    boolean originalOK = true;
                    Project main    = OpenProjects.getDefault().getMainProject();
                    boolean wasMain = main != null && project.getProjectDirectory().equals(main.getProjectDirectory());
                    Project nue = null;
                    
                    try {
                        handle.switchToIndeterminate();
                        handle.switchToDeterminate(5);
                        
                        int currentWorkDone = 0;
                        
                        FileObject projectDirectory = project.getProjectDirectory();
                        File       projectDirectoryFile = FileUtil.toFile(project.getProjectDirectory());
                        Collection<? extends MoveOperationImplementation> operations = project.getLookup().lookupAll(MoveOperationImplementation.class);
                        
                        close(project);
                        
                        handle.progress(++currentWorkDone);
                        
                        for (MoveOperationImplementation o : operations) {
                            o.notifyMoving();
                        }
                        
                        handle.progress(++currentWorkDone);
                        
                        for (MoveOperationImplementation o : operations) {
                            o.notifyMoved(null, projectDirectoryFile, nueName);
                        }
                        
                        handle.progress(++currentWorkDone);
                        
                        //#64264: the non-project cache can be filled with incorrect data (gathered during the project copy phase), clear it:
                        ProjectManager.getDefault().clearNonProjectCache();
                        
                        nue = ProjectManager.getDefault().findProject(projectDirectory);
                        
                        assert nue != null;
                        
                        originalOK = false;
                        
                        handle.progress(++currentWorkDone);
                        
                        operations = nue.getLookup().lookupAll(MoveOperationImplementation.class);
                        
                        for (MoveOperationImplementation o : operations) {
                            o.notifyMoved(project, projectDirectoryFile, nueName);
                        }
                        
                        ProjectManager.getDefault().saveProject(nue);
                        
                        open(nue, wasMain);
                        
                        handle.progress(++currentWorkDone);
                        
                        handle.finish();
                    } catch (Exception e) {
                        if (originalOK) {
                            open(project, wasMain);
                        } else {
                            assert nue != null;
                            open(nue, wasMain);
                        }
                        ErrorManager.getDefault().annotate(e, NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "ERR_Cannot_Rename", e.getLocalizedMessage()));
                        throw e;
                    }
		}
            }
        });
    }
    
    /*package private for tests*/ static void doMoveProject(ProgressHandle handle, Project project, String nueFolderName, String nueProjectName, FileObject newTarget, String errorKey) throws Exception {
        boolean originalOK = true;
        Project main    = OpenProjects.getDefault().getMainProject();
        boolean wasMain = main != null && project.getProjectDirectory().equals(main.getProjectDirectory());
	FileObject target = null;
        
        try {
            
            int totalWork = MAX_WORK;
            double currentWorkDone = 0;
            
            handle.progress((int) currentWorkDone);
            
            ProjectOperations.notifyMoving(project);
            
            close(project);
            
            handle.progress((int) (currentWorkDone = totalWork * NOTIFY_WORK));
            
            FileObject projectDirectory = project.getProjectDirectory();
            List<FileObject> toMoveList = new ArrayList<FileObject>();
            for (FileObject child : projectDirectory.getChildren()) {
                if (child.isValid()) {
                    toMoveList.add(child);
                }
            }
            
            double workPerFileAndOperation = (totalWork * (1.0 - 2 * NOTIFY_WORK - FIND_PROJECT_WORK) / toMoveList.size()) / 2;
            
            target = newTarget.createFolder(nueFolderName);

            for (FileObject toCopy : toMoveList) {
                doCopy(project, toCopy, target);
                
                int lastWorkDone = (int) currentWorkDone;
                
                currentWorkDone += workPerFileAndOperation;
                
                if (lastWorkDone < (int) currentWorkDone) {
                    handle.progress((int) currentWorkDone);
                }
            }
            
            originalOK = false;
            
            for (FileObject toCopy : toMoveList) {
                doDelete(project, toCopy);
                
                int lastWorkDone = (int) currentWorkDone;
                
                currentWorkDone += workPerFileAndOperation;
                
                if (lastWorkDone < (int) currentWorkDone) {
                    handle.progress((int) currentWorkDone);
                }
            }
            
            if (projectDirectory.getChildren().length == 0) {
                projectDirectory.delete();
            }
            
            //#64264: the non-project cache can be filled with incorrect data (gathered during the project copy phase), clear it:
            ProjectManager.getDefault().clearNonProjectCache();
            Project nue = ProjectManager.getDefault().findProject(target);
            
            handle.progress((int) (currentWorkDone += totalWork * FIND_PROJECT_WORK));
            
            assert nue != null;
            
            ProjectOperations.notifyMoved(project, nue, FileUtil.toFile(project.getProjectDirectory()), nueProjectName);
            
            handle.progress((int) (currentWorkDone += totalWork * NOTIFY_WORK));
            
            ProjectManager.getDefault().saveProject(nue);
            
            open(nue, wasMain);
            
            handle.progress(totalWork);
            handle.finish();
        } catch (Exception e) {
            if (originalOK) {
                open(project, wasMain);
            } else {
		assert target != null;
		
		//#64264: the non-project cache can be filled with incorrect data (gathered during the project copy phase), clear it:
		ProjectManager.getDefault().clearNonProjectCache();
		Project nue = ProjectManager.getDefault().findProject(target);
		
		assert nue != null;
		
                open(nue, wasMain);
            }
            ErrorManager.getDefault().annotate(e, NbBundle.getMessage(DefaultProjectOperationsImplementation.class, errorKey, e.getLocalizedMessage()));
            throw e;
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
    
    private static FileObject createFolder(File parent, String name) throws IOException {
        FileObject path = FileUtil.toFileObject(parent);
        if (path != null) {
            return path.createFolder(name);
        } else {
            return createFolder(parent.getParentFile(), parent.getName()).createFolder(name);
        }
    }
    
    private static boolean doDelete(Project original, FileObject toDelete) throws IOException {
        if (!original.getProjectDirectory().equals(FileOwnerQuery.getOwner(toDelete).getProjectDirectory())) {
            return false;
        }
        
        if (toDelete.isFolder()) {
            boolean delete = true;

            for (FileObject kid : toDelete.getChildren()) {
                delete &= doDelete(original, kid);
            }
            
            if (delete) {
                //#83958
                DataFolder.findFolder(toDelete).delete();
            }
            
            return delete;
        } else {
            assert toDelete.isData();
            try {
                //#83958
                DataObject dobj = DataObject.find(toDelete);
                dobj.delete();
            } catch (DataObjectNotFoundException ex) {
                toDelete.delete();
            }
            return true;
        }
    }
    
    private static JComponent wrapPanel(JComponent component) {
        component.setBorder(new EmptyBorder(12, 12, 12, 12));
        
        return component;
    }
    
    private static void showConfirmationDialog(final JComponent panel, Project project, String caption, String confirmButton, String cancelButton, boolean doSetMessageType, final Executor executor) {
        final JButton confirm = new JButton();
        Mnemonics.setLocalizedText(confirm, NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_" + confirmButton));
        final JButton cancel  = new JButton(cancelButton == null ?
              NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_Cancel_Button")
            : NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "LBL_" + cancelButton));
        
        confirm.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "ACSD_" + confirmButton));
        cancel.getAccessibleContext().setAccessibleDescription(cancelButton == null ?
              NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "ACSD_Cancel_Button")
            : NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "ACSD_" + cancelButton));
        
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
    
    static String computeError(File location, String projectNameText, boolean pureRename) {
        return computeError(location, projectNameText, null, pureRename);
    }
    
    static String computeError(File location, String projectNameText, String projectFolderText, boolean pureRename) {
        File parent = location;
        if (!location.exists()) {
            //if some dirs in teh chain are not created, consider it ok.
            parent = location.getParentFile();
            while (parent != null && !parent.exists()) {
                parent = parent.getParentFile();
            }
            if (parent == null) {
                return NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "ERR_Location_Does_Not_Exist");
            }
        }
        
        if (!parent.canWrite()) {
            return NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "ERR_Location_Read_Only");
        }
        
        if (projectNameText.length() == 0) {
            return NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "ERR_Project_Name_Must_Entered");
        }
        
        File projectFolderFile = null;
        if (projectFolderText == null) {
            projectFolderFile = new File(location, projectNameText);
        } else {
            projectFolderFile = new File(projectFolderText);
        }
        
        if (projectFolderFile.exists() && !pureRename) {
            return NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "ERR_Project_Folder_Exists");
        }
        
	if (projectNameText.indexOf('/') != -1 || projectNameText.indexOf('\\') != -1) {
	    return NbBundle.getMessage(DefaultProjectOperationsImplementation.class, "ERR_Not_Valid_Filename", projectNameText);
	}
        
        return null;
    }
    
    private static void close(final Project prj) {
        Mutex.EVENT.readAccess(new Mutex.Action<Void>() {
            public Void run() {
		LifecycleManager.getDefault().saveAll();
		
                Action closeAction = CommonProjectActions.closeProjectAction();
                closeAction = closeAction instanceof ContextAwareAction ? ((ContextAwareAction) closeAction).createContextAwareInstance(Lookups.fixed(prj)) : null;
                
                if (closeAction != null && closeAction.isEnabled()) {
                    closeAction.actionPerformed(new ActionEvent(prj, -1, "")); // NOI18N
                } else {
                    //fallback:
                    OpenProjects.getDefault().close(new Project[] {prj});
                }
                
                return null;
            }
        });
    }
    
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
    
    static interface Executor {
        public void execute() throws Exception;
    }
    
    public static interface InvalidablePanel {
        public void addChangeListener(ChangeListener l);
        public void removeChangeListener(ChangeListener l);
        public boolean isPanelValid();
        public void showProgress();
    }
    //</editor-fold>
    
}
