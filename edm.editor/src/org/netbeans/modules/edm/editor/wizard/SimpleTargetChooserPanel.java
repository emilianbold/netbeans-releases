/*
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). You may not use this 
 * file except in compliance with the License.  You can obtain a copy of the
 *  License at http://www.netbeans.org/cddl.html
 *
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
 *
 */

package org.netbeans.modules.edm.editor.wizard;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;

import org.netbeans.spi.project.ui.templates.support.Templates;

import org.openide.WizardDescriptor;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.openide.util.HelpCtx;

import java.awt.Component;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.logging.Logger;


/**
 * DOCUMENT ME!
 *
 * @author Petr Hrebejk
 */
final class SimpleTargetChooserPanel implements WizardDescriptor.FinishablePanel, ChangeListener {
    private final List /*<ChangeListener>*/ listeners = new ArrayList();
    private SimpleTargetChooserPanelGUI gui;
    private Project project;
    private SourceGroup[] folders;
    private WizardDescriptor.Panel bottomPanel;
    private WizardDescriptor wizard;
    private boolean isFolder;
    private static final Logger mLogger = Logger.getLogger(SimpleTargetChooserPanel.class.getName());

    /**
     * Creates a new SimpleTargetChooserPanel object.
     *
     * @param project DOCUMENT ME!
     * @param folders DOCUMENT ME!
     * @param bottomPanel DOCUMENT ME!
     * @param isFolder DOCUMENT ME!
     */
    SimpleTargetChooserPanel(
        Project project, SourceGroup[] folders, WizardDescriptor.Panel bottomPanel, boolean isFolder
    ) {
        this.folders = folders;
        this.project = project;
        this.bottomPanel = bottomPanel;

        if (bottomPanel != null) {
            bottomPanel.addChangeListener(this);
        }

        this.isFolder = isFolder;
        this.gui = null;
    }

   public boolean isFinishPanel() {
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Component getComponent() {
        if (gui == null) {
            gui = new SimpleTargetChooserPanelGUI(
                    project, folders, (bottomPanel == null) ? null : bottomPanel.getComponent(),
                    isFolder
                );
            gui.addChangeListener(this);
        }

        return gui;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public HelpCtx getHelp() {
        if (bottomPanel != null) {
            HelpCtx bottomHelp = bottomPanel.getHelp();

            if (bottomHelp != null) {
                return bottomHelp;
            }
        }

        //XXX
        return null;
    }

    /**
     * Checks if the given file name can be created in the target folder.
     *
     * @param targetFolder target folder (e.g. source group)
     * @param folderName name of the folder relative to target folder
     * @param newObjectName name of created file
     * @param extension extension of created file
     *
     * @return localized error message or null if all right
     */
    private String canUseFileName(
        FileObject targetFolder, String folderName, String newObjectName, String extension
    ) {
        if ((extension != null) && (extension.length() > 0)) {
            StringBuilder sb = new StringBuilder();
            sb.append(newObjectName);
            sb.append('.'); // NOI18N
            sb.append(extension);
            newObjectName = sb.toString();
        }

        String relFileName = (folderName == null) ? newObjectName : (
                folderName + "/" + newObjectName
            ); // NOI18N

        // test whether the selected folder on selected filesystem already exists
        if (targetFolder == null) {
            return "The target folder does not exist."; // NOI18N
        }

        // target filesystem should be writable
        if (!targetFolder.canWrite()) {
            return "The target folder is read-only."; // NOI18N
        }

        if (existFileName(targetFolder, relFileName)) {
            return "The file {0} already exists."+newObjectName; // NOI18N
        }

      // all ok
        return null;
    }

    private static boolean existFileName(FileObject targetFolder, String relFileName) {
        boolean result = false;
        File fileForTargetFolder = FileUtil.toFile(targetFolder);

        if (fileForTargetFolder.exists()) {
            result = new File(fileForTargetFolder, relFileName).exists();
        } else {
            result = targetFolder.getFileObject(relFileName) != null;
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isValid() {
        boolean ok = ((gui != null) && (gui.getTargetName() != null) &&
            ((bottomPanel == null) || bottomPanel.isValid()));

        if (!ok) {
            return false;
        }

        // check if the file name can be created
        FileObject template = Templates.getTemplate(wizard);

        // String errorMessage = ProjectUtilities.canUseFileName (gui.getTargetGroup().getRootFolder(), gui.getTargetFolder(), gui.getTargetName(), template.getExt ());
        String errorMessage = canUseFileName(
                gui.getTargetGroup().getRootFolder(), gui.getTargetFolder(), gui.getTargetName(),
                template.getExt()
            );
        wizard.putProperty("WizardPanel_errorMessage", errorMessage); // NOI18N

        return errorMessage == null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param l DOCUMENT ME!
     */
    public synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    /**
     * DOCUMENT ME!
     *
     * @param l DOCUMENT ME!
     */
    public synchronized void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        List templist;

        synchronized (this) {
            templist = new ArrayList(listeners);
        }

        Iterator it = templist.iterator();

        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param settings DOCUMENT ME!
     */
    public void readSettings(Object settings) {
        wizard = (WizardDescriptor) settings;

        if (gui == null) {
            getComponent();
        }

        // Try to preselect a folder
        FileObject preselectedTarget = Templates.getTargetFolder(wizard);

        // Init values
        gui.initValues(Templates.getTemplate(wizard), preselectedTarget);

        // XXX hack, TemplateWizard in final setTemplateImpl() forces new generic's title
        // this name is used in NewFileWizard to modify the title
        Object substitute = gui.getClientProperty("NewEtlWizard_Title"); // NOI18N

        if (substitute != null) {
            wizard.putProperty("NewEtlWizard_Title", substitute); // NOI18N
        }
        wizard.putProperty(
            "WizardPanel_contentData",
            new String[] { // NOI18N
               "Choose File Type", // NOI18N
               "Name and Location"
            }
        ); // NOI18N

        if (bottomPanel != null) {
            bottomPanel.readSettings(settings);
        }
        String collabName = (String) wizard.getProperty("collaboration_name");
        if(!(collabName == null || "".equals(collabName))) {
            gui.setDocumentName(collabName);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param settings DOCUMENT ME!
     */
    public void storeSettings(Object settings) {
        if (WizardDescriptor.PREVIOUS_OPTION.equals(((WizardDescriptor) settings).getValue())) {
            return;
        }

        if (isValid()) {
            if (bottomPanel != null) {
                bottomPanel.storeSettings(settings);
            }

            Templates.setTargetFolder((WizardDescriptor) settings, getTargetFolderFromGUI());
            Templates.setTargetName((WizardDescriptor) settings, gui.getTargetName());
        }

        ((WizardDescriptor) settings).putProperty("collaboration_name", gui.getTargetName());
        
        ((WizardDescriptor) settings).putProperty("NewDtelWizard_Title", null); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void stateChanged(ChangeEvent e) {
        fireChange();
    }

    private FileObject getTargetFolderFromGUI() {
        FileObject rootFolder = gui.getTargetGroup().getRootFolder();
        String folderName = gui.getTargetFolder();

        FileObject targetFolder;

        if (folderName == null) {
            targetFolder = rootFolder;
        } else {
            targetFolder = rootFolder.getFileObject(folderName);
        }

        if (targetFolder == null) {
            // XXX add deletion of the file in uninitalize ow the generic
            try {
                targetFolder = FileUtil.createFolder(rootFolder, folderName);
            } catch (IOException ioe) {
                // XXX
                // Can't create the folder
            }
        }

        return targetFolder;
    }
}
