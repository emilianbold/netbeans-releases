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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * 
 * Copyright 2005 Sun Microsystems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.netbeans.modules.jdbcwizard.wizards;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import java.awt.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * DOCUMENT ME!
 * 
 * @author
 */
final class SimpleTargetChooserPanel implements WizardDescriptor.FinishablePanel, ChangeListener {
    private final List /* <ChangeListener> */listeners = new ArrayList();

    private SimpleTargetChooserPanelGUI gui;

    private Project project;

    private SourceGroup[] folders;

    private final WizardDescriptor.Panel bottomPanel;

    private WizardDescriptor wizard;

    private final boolean isFolder;

    /**
     * Creates a new SimpleTargetChooserPanel object.
     * 
     * @param project DOCUMENT ME!
     * @param folders DOCUMENT ME!
     * @param bottomPanel DOCUMENT ME!
     * @param isFolder DOCUMENT ME!
     */
    SimpleTargetChooserPanel(Project project, SourceGroup[] folders, WizardDescriptor.Panel bottomPanel,
            boolean isFolder) {
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
        if (this.gui == null) {
            this.gui = new SimpleTargetChooserPanelGUI(project, folders, this.bottomPanel == null ? null
                    : this.bottomPanel.getComponent(), this.isFolder);
            this.gui.addChangeListener(this);
        }

        return this.gui;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public HelpCtx getHelp() {
		return new HelpCtx(SimpleTargetChooserPanel.class);
    }

    /**
     * Checks if the given file name can be created in the target folder.
     * 
     * @param targetFolder target folder (e.g. source group)
     * @param folderName name of the folder relative to target folder
     * @param newObjectName name of created file
     * @param extension extension of created file
     * @return localized error message or null if all right
     */
    private String canUseFileName(FileObject targetFolder, String folderName, String newObjectName, String extension) {
        if (extension != null && extension.length() > 0) {
            StringBuffer sb = new StringBuffer();
            sb.append(newObjectName);
            sb.append('.'); // NOI18N
            sb.append(extension);
            newObjectName = sb.toString();
        }

        String relFileName = folderName == null ? newObjectName : folderName + "/" + newObjectName; // NOI18N

        // test whether the selected folder on selected filesystem already exists
        if (targetFolder == null) {
            return NbBundle.getMessage(SimpleTargetChooserPanel.class, "MSG_fs_or_folder_does_not_exist"); // NOI18N
        }

        // target filesystem should be writable
        if (!targetFolder.canWrite()) {
            return NbBundle.getMessage(SimpleTargetChooserPanel.class, "MSG_fs_is_readonly"); // NOI18N
        }

        if (existFileName(targetFolder, relFileName)) {
            return NbBundle.getMessage(SimpleTargetChooserPanel.class, "MSG_file_already_exist", newObjectName); // NOI18N
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
        boolean ok = this.gui != null && this.gui.getTargetName() != null && (this.bottomPanel == null || this.bottomPanel.isValid());

        if (!ok) {
            return false;
        }

        // check if the file name can be created
        FileObject template = Templates.getTemplate(this.wizard);

        // String errorMessage = ProjectUtilities.canUseFileName
        // (gui.getTargetGroup().getRootFolder(), gui.getTargetFolder(), gui.getTargetName(),
        // template.getExt ());
        final String errorMessage = canUseFileName(this.gui.getTargetGroup().getRootFolder(), this.gui.getTargetFolder(),
                this.gui.getTargetName(), template.getExt());
        this.wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, errorMessage); // NOI18N

        return errorMessage == null;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param l DOCUMENT ME!
     */
    public synchronized void addChangeListener(final ChangeListener l) {
        this.listeners.add(l);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param l DOCUMENT ME!
     */
    public synchronized void removeChangeListener(final ChangeListener l) {
        this.listeners.remove(l);
    }

    private void fireChange() {
        final ChangeEvent e = new ChangeEvent(this);
        List templist;

        synchronized (this) {
            templist = new ArrayList(this.listeners);
        }

        final Iterator it = templist.iterator();

        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(e);
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param settings DOCUMENT ME!
     */
    public void readSettings(final Object settings) {
        this.wizard = (WizardDescriptor) settings;

        if (this.gui == null) {
            this.getComponent();
        }

        // Try to preselect a folder
        FileObject preselectedTarget = Templates.getTargetFolder(this.wizard);

        // Init values
        this.gui.initValues(Templates.getTemplate(this.wizard), preselectedTarget);

        // XXX hack, TemplateWizard in final setTemplateImpl() forces new generic's title
        // this name is used in NewFileWizard to modify the title
        final Object substitute = this.gui.getClientProperty("NewDtelWizard_Title"); // NOI18N

        if (substitute != null) {
            this.wizard.putProperty("NewDtelWizard_Title", substitute); // NOI18N
        }

        this.wizard.putProperty(WizardDescriptor.PROP_CONTENT_DATA, new String[] { // NOI18N

                        NbBundle.getBundle(SimpleTargetChooserPanel.class).getString("LBL_TemplatesPanel_Name"), // NOI18N
                        NbBundle.getBundle(SimpleTargetChooserPanel.class).getString(
                                "LBL_SimpleTargetChooserPanel_Name") }); // NOI18N

        if (this.bottomPanel != null) {
            this.bottomPanel.readSettings(settings);
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param settings DOCUMENT ME!
     */
    public void storeSettings(final Object settings) {
        if (WizardDescriptor.PREVIOUS_OPTION.equals(((WizardDescriptor) settings).getValue())) {
            return;
        }

        if (this.isValid()) {
            if (this.bottomPanel != null) {
                this.bottomPanel.storeSettings(settings);
            }

            Templates.setTargetFolder((WizardDescriptor) settings, getTargetFolderFromGUI());
            Templates.setTargetName((WizardDescriptor) settings, this.gui.getTargetName());
        }

        ((WizardDescriptor) settings).putProperty(JDBCWizardContext.COLLABORATION_NAME, this.gui.getTargetName());
        ((WizardDescriptor) settings).putProperty(JDBCWizardContext.TARGETFOLDER_PATH,
                (FileUtil.toFile(getTargetFolderFromGUI())).getAbsolutePath());
        ((WizardDescriptor) settings).putProperty("NewDtelWizard_Title", null); // NOI18N
    }

    /**
     * DOCUMENT ME!
     * 
     * @param e DOCUMENT ME!
     */
    public void stateChanged(final ChangeEvent e) {
        this.fireChange();
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
