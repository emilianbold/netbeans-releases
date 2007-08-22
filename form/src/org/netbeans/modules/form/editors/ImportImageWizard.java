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

package org.netbeans.modules.form.editors;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.text.MessageFormat;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.ArrayIterator;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Simple wizard for importing image files to project sources. The user selectes
 * the files to copy (in the first step) and the target folder within sources of
 * given project (in the second step). After finishing, the wizard also performs
 * the copy operation.
 * 
 * @author Tomas Pavek
 */
class ImportImageWizard extends WizardDescriptor {

    private WizardDescriptor.Iterator wizardIterator;

    private File[] selectedFiles;
    private FileObject targetFolder;
    private FileObject fileInProject;

    static String lastDirectoryUsed; // for file chooser

    /**
     * @param files pre-selected files to be copied, can be null
     * @param targetFolder pre-selected target folder, can be null
     * @param fileInProject a file identifying the project (whatever source file)
     */
    ImportImageWizard(File[] files, FileObject targetFolder, FileObject fileInProject) {
        this(new ArrayIterator(new WizardDescriptor.Panel[] {
            new SourceWizardPanel(), new TargetWizardPanel() }));

        if (targetFolder != null)
            assert FileOwnerQuery.getOwner(targetFolder) == FileOwnerQuery.getOwner(fileInProject);

        this.selectedFiles = files;
        this.targetFolder = targetFolder;
        this.fileInProject = fileInProject;
    }

    private ImportImageWizard(WizardDescriptor.Iterator iterator) {
        super(iterator);
        wizardIterator = iterator;

        putProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N
        putProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
        putProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N

        setTitle(NbBundle.getMessage(ImportImageWizard.class, "ImportImageWizard.Title")); // NOI18N
        setTitleFormat(new java.text.MessageFormat("{0}")); // NOI18N

        putProperty("WizardPanel_contentData",  // NOI18N
                    new String[] { NbBundle.getMessage(ImportImageWizard.class, "ImportImageWizard.Step1"), // NOI18N
                                   NbBundle.getMessage(ImportImageWizard.class, "ImportImageWizard.Step2") }); // NOI18N
    }

    /**
     * Shows the wizard, having the provided files and target folder pre-selected,
     * lets the user configure the source files-target folder, and if confirmed,
     * then also copies the files.
     * @return the files copied to the project, null if the wizard was canceled
     */
    FileObject[] show() {
        Dialog dialog = DialogDisplayer.getDefault().createDialog(this);
        dialog.setVisible(true);
        dialog.dispose();

        return getValue() == FINISH_OPTION ? copyFiles() : null;
    }

    void stepToNext() {
        if (wizardIterator.hasNext()) {
            wizardIterator.nextPanel();
            updateState();
        }
    }

    // [TODO: progress indication, way to cancel, warning for replace, allow to rename...
    //  align with the global resource editor]
    private FileObject[] copyFiles() {
        if (selectedFiles == null || selectedFiles.length == 0 || targetFolder == null)
            return null;

        final FileObject[] copied = new FileObject[selectedFiles.length];
        try {
            Repository.getDefault().getDefaultFileSystem().runAtomicAction(
            new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    for (int i=0; i < selectedFiles.length; i++) {
                        File f = selectedFiles[i];
                        String fileName = f.getName();
                        FileObject targetFile = targetFolder.getFileObject(fileName);
                        if (targetFile != null && targetFile.isFolder()) {
                            targetFile = null;
                        }
                        if (targetFile == null || canRewriteTarget(f, targetFile)) {
                            if (targetFile != null) {
                                targetFile.delete();
                            }
                            FileInputStream is = new FileInputStream(f);
                            targetFile = targetFolder.createData(fileName);
                            FileLock lock = targetFile.lock();
                            OutputStream os = targetFile.getOutputStream(lock);

                            byte[] buf = new byte[4096];
                            int count;
                            try {
                                while ((count = is.read(buf)) != -1) {
                                    os.write(buf, 0, count);
                                }
                            }
                            finally {
                                os.close();
                                lock.releaseLock();
                            }
                        }
                        copied[i] = targetFile;
                    }
                }
            });
        }
        catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return copied;
    }

    private boolean canRewriteTarget(File source, FileObject targetFO) {
        FileObject sourceFO = FileUtil.toFileObject(source);
        if (sourceFO != null && sourceFO.equals(targetFO)) {
            return false;
        }
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                MessageFormat.format(NbBundle.getMessage(ImportImageWizard.class, "FMT_ReplaceExistingFileQuestion"), // NOI18N
                                     source.getName()),
                NbBundle.getMessage(ImportImageWizard.class, "TITLE_FileAlreadyExists"), // NOI18N
                NotifyDescriptor.YES_NO_OPTION);
        return DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.YES_OPTION;
    }

    // -----

    private static class SourceWizardPanel implements WizardDescriptor.Panel,
                                                      WizardDescriptor.FinishablePanel
    {
        private ImportImageWizard wizard;
        private JFileChooser fileChooser;

        private EventListenerList listenerList;
        private boolean setSelectedFiles;

        public Component getComponent() {
            if (fileChooser == null) {
                fileChooser = new JFileChooser(lastDirectoryUsed);
                fileChooser.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setAcceptAllFileFilterUsed(true);
                fileChooser.setControlButtonsAreShown(false);
                fileChooser.setMultiSelectionEnabled(true);

                fileChooser.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        if (JFileChooser.APPROVE_SELECTION.equals(ev.getActionCommand()))
                            wizard.stepToNext();
                        else if (JFileChooser.CANCEL_SELECTION.equals(ev.getActionCommand()))
                            fileChooser.getTopLevelAncestor().setVisible(false);
                    }
                });

                fileChooser.addPropertyChangeListener(new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent ev) {
                        if (!setSelectedFiles && JFileChooser.SELECTED_FILES_CHANGED_PROPERTY
                                            .equals(ev.getPropertyName()))
                            fireStateChanged();
                    }
                });

                fileChooser.setName(NbBundle.getMessage(ImportImageWizard.class, "ImportImageWizard.Step1")); // NOI18N
                fileChooser.putClientProperty("WizardPanel_contentSelectedIndex", 0); // NOI18N
            }
            if (setSelectedFiles) {
                fileChooser.setSelectedFiles(wizard.selectedFiles);
                setSelectedFiles = false;
            }
            return fileChooser;
        }

        public HelpCtx getHelp() {
            return HelpCtx.DEFAULT_HELP;
        }

        // readSettings is called before getComponent
        public void readSettings(Object settings) {
            wizard = (ImportImageWizard) settings;
            setSelectedFiles = true; // set only once when getComponent is called
        }

        public void storeSettings(Object settings) {
            if (fileChooser != null) {
                File[] files = fileChooser.getSelectedFiles();
                ((ImportImageWizard)settings).selectedFiles = files;
                if (files != null && files.length > 0) {
                    lastDirectoryUsed = files[0].getParent();
                }
            }
        }

        public boolean isValid() {
            return fileChooser != null && fileChooser.getSelectedFiles().length > 0;
        }


        public boolean isFinishPanel() {
            return wizard != null && wizard.targetFolder != null;
        }

        public void addChangeListener(ChangeListener l) {
            if (listenerList == null)
                listenerList = new EventListenerList();
            listenerList.add(ChangeListener.class, l);
        }

        public void removeChangeListener(ChangeListener l) {
            if (listenerList != null)
                listenerList.remove(ChangeListener.class, l);
        }

        void fireStateChanged() {
            if (listenerList == null)
                return;

            ChangeEvent e = null;
            Object[] listeners = listenerList.getListenerList();
            for (int i=listeners.length-2; i >= 0; i-=2) {
                if (listeners[i] == ChangeListener.class) {
                    if (e == null)
                        e = new ChangeEvent(this);
                    ((ChangeListener)listeners[i+1]).stateChanged(e);
                }
            }
        }
    }

    // -----

    private static class TargetWizardPanel implements WizardDescriptor.Panel {

        private ImportImageWizard wizard;
        private ClassPathFileChooser cpfChooser;

        private EventListenerList listenerList;
        private boolean setTargetFolder;

        public Component getComponent() {
            if (cpfChooser == null) {
                cpfChooser = new ClassPathFileChooser(
                        wizard.fileInProject,
                        new ClassPathFileChooser.Filter() {
                            public boolean accept(FileObject fo) {
                                return fo.isFolder();
                            }
                        },
                        true, false);

                cpfChooser.addPropertyChangeListener(new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent ev) {
                        if (!setTargetFolder && ClassPathFileChooser.PROP_SELECTED_FILE
                                    .equals(ev.getPropertyName()))
                            fireStateChanged();
                    }
                });
                cpfChooser.setPreferredSize(new Dimension(200, 200)); // TreeView wants to be too big

                cpfChooser.setName(NbBundle.getMessage(ImportImageWizard.class, "ImportImageWizard.Step2")); // NOI18N
                cpfChooser.putClientProperty("WizardPanel_contentSelectedIndex", 1); // NOI18N
            }
            if (setTargetFolder) {
                cpfChooser.setSelectedFile(wizard.targetFolder);
                setTargetFolder = false;
            }
            return cpfChooser;
        }

        public HelpCtx getHelp() {
            return HelpCtx.DEFAULT_HELP;
        }

        // readSettings is called before getComponent
        public void readSettings(Object settings) {
            wizard = (ImportImageWizard) settings;
            setTargetFolder = true; // set only once when getComponent is called
        }

        public void storeSettings(Object settings) {
            if (cpfChooser != null)
                ((ImportImageWizard)wizard).targetFolder = cpfChooser.getSelectedFile();
        }

        public boolean isValid() {
            if (cpfChooser != null) {
                FileObject fo = cpfChooser.getSelectedFile();
                return fo != null && fo.isFolder();
            }
            else if (wizard != null) {
                return wizard.targetFolder != null;
            }
            return false;
        }

        public void addChangeListener(ChangeListener l) {
            if (listenerList == null)
                listenerList = new EventListenerList();
            listenerList.add(ChangeListener.class, l);
        }

        public void removeChangeListener(ChangeListener l) {
            if (listenerList != null)
                listenerList.remove(ChangeListener.class, l);
        }

        void fireStateChanged() {
            if (listenerList == null)
                return;

            ChangeEvent e = null;
            Object[] listeners = listenerList.getListenerList();
            for (int i=listeners.length-2; i >= 0; i-=2) {
                if (listeners[i] == ChangeListener.class) {
                    if (e == null)
                        e = new ChangeEvent(this);
                    ((ChangeListener)listeners[i+1]).stateChanged(e);
                }
            }
        }
    }
}
