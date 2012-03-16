/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.project;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.*;

/**
 *
 * @author Anton Chechel <anton.chechel@oracle.com>
 */
public class ConfigureFXMLPanelVisual extends JPanel implements ActionListener, DocumentListener {
    
    private Panel observer;
    private Project project;
    private SourceGroup groups[];

    private File[] srcRoots;
    private File rootFolder;
    private boolean ignoreRootCombo;
    private RequestProcessor.Task updatePackagesTask;
    private static final ComboBoxModel WAIT_MODEL;
    
    static {
        WAIT_MODEL = new DefaultComboBoxModel(
                new String[]{
                    NbBundle.getMessage(ConfigureFXMLPanelVisual.class,
                        "LBL_ConfigureFXMLPanel_PackageName_PleaseWait") // NOI18N
                });
    }

    private ConfigureFXMLPanelVisual(Panel observer, Project project, SourceGroup[] groups) {
        this.observer = observer;
        this.project = project;
        this.groups = groups;
        
        srcRoots = new File[groups.length];
        for (int i = 0; i < groups.length; i++) {
            srcRoots[i] = FileUtil.toFile(groups[i].getRootFolder());
        }
        
        initComponents(); // Matisse
        initComponents2(); // My own
    }

    private void fireChange() {
        this.observer.fireChangeEvent();
    }

    private void initComponents2() {
        fxmlNameTextField.getDocument().addDocumentListener(this);
        
        packageComboBox.getEditor().addActionListener(this);
        Component packageEditor = packageComboBox.getEditor().getEditorComponent();
        if (packageEditor instanceof JTextField) {
            ((JTextField) packageEditor).getDocument().addDocumentListener(this);
        }

        locationComboBox.setRenderer(new GroupListCellRenderer());
        packageComboBox.setRenderer(PackageView.listRenderer());
        locationComboBox.addActionListener(this);
    }

    public void initValues(FileObject template, FileObject preselectedFolder) {
        if (template == null) {
                throw new IllegalArgumentException(
                        NbBundle.getMessage(ConfigureFXMLPanelVisual.class,
                            "MSG_ConfigureFXMLPanel_Template_Error")); // NOI18N
        }

        // Show name of the project
        projectTextField.setText(ProjectUtils.getInformation(project).getDisplayName());
        
        String displayName;
        try {
            DataObject templateDo = DataObject.find(template);
            displayName = templateDo.getNodeDelegate().getDisplayName();
        } catch (DataObjectNotFoundException ex) {
            displayName = template.getName();
        }
        putClientProperty("NewFileWizard_Title", displayName); // NOI18N        

        // Setup comboboxes 
        locationComboBox.setModel(new DefaultComboBoxModel(groups));
        SourceGroup preselectedGroup = getPreselectedGroup(preselectedFolder);
        ignoreRootCombo = true;
        locationComboBox.setSelectedItem(preselectedGroup);
        ignoreRootCombo = false;
        Object preselectedPackage = FXMLTemplateWizardIterator.getPreselectedPackage(preselectedGroup, preselectedFolder);
        if (preselectedPackage != null) {
            packageComboBox.getEditor().setItem(preselectedPackage);
        }
        if (template != null) {
            if (fxmlNameTextField.getText().trim().length() == 0) { // To preserve the fxml name on back in the wiazard
                final String baseName = template.getName();
                String activeName = baseName;
                if (preselectedFolder != null) {
                    int index = 0;
                    while (true) {
                        FileObject fo = preselectedFolder.getFileObject(activeName, JFXProjectProperties.FXML_EXTENSION);
                        if (fo == null) {
                            break;
                        }
                        activeName = baseName + ++index;
                    }
                }
                fxmlNameTextField.setText(activeName);
                fxmlNameTextField.selectAll();
            }
        }
        
        updatePackages();
        updateText();
    }
    
    private File[] getSrcRoots() {
        return srcRoots;
    }
    
    private File getRootFolder() {
        return rootFolder;
    }
    
    public FileObject getLocationFolder() {
        final Object selectedItem  = locationComboBox.getSelectedItem();
        return (selectedItem instanceof SourceGroup) ? ((SourceGroup)selectedItem).getRootFolder() : null;
    }

    public String getPackageFileName() {
        String packageName = packageComboBox.getEditor().getItem().toString();
        return packageName.replace('.', '/'); // NOI18N
    }

    /**
     * Name of selected package, or "" for default package.
     */
    String getPackageName() {
        return packageComboBox.getEditor().getItem().toString();
    }

    public String getFXMLName() {
        String text = fxmlNameTextField.getText().trim();
        return text.length() == 0 ? null : text;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fxmlNameLabel = new javax.swing.JLabel();
        fxmlNameTextField = new javax.swing.JTextField();
        projectLabel = new javax.swing.JLabel();
        locationLabel = new javax.swing.JLabel();
        packageLabel = new javax.swing.JLabel();
        resultLabel = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        locationComboBox = new javax.swing.JComboBox();
        resultTextField = new javax.swing.JTextField();
        packageComboBox = new javax.swing.JComboBox();

        setPreferredSize(new java.awt.Dimension(500, 340));

        fxmlNameLabel.setLabelFor(fxmlNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(fxmlNameLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.fxmlNameLabel.text")); // NOI18N

        projectLabel.setLabelFor(projectTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.projectLabel.text")); // NOI18N

        locationLabel.setLabelFor(locationComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.locationLabel.text")); // NOI18N

        packageLabel.setLabelFor(packageComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(packageLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.packageLabel.text")); // NOI18N

        resultLabel.setLabelFor(resultTextField);
        org.openide.awt.Mnemonics.setLocalizedText(resultLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.resultLabel.text")); // NOI18N

        projectTextField.setEditable(false);
        projectTextField.setEnabled(false);

        resultTextField.setEditable(false);
        resultTextField.setEnabled(false);

        packageComboBox.setEditable(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(projectLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(locationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(packageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(resultLabel))
                    .addComponent(fxmlNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fxmlNameTextField)
                    .addComponent(projectTextField)
                    .addComponent(locationComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(resultTextField)
                    .addComponent(packageComboBox, 0, 409, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fxmlNameLabel)
                    .addComponent(fxmlNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectLabel)
                    .addComponent(projectTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(locationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(packageLabel)
                    .addComponent(packageComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(resultLabel)
                    .addComponent(resultTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(204, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel fxmlNameLabel;
    private javax.swing.JTextField fxmlNameTextField;
    private javax.swing.JComboBox locationComboBox;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JComboBox packageComboBox;
    private javax.swing.JLabel packageLabel;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JLabel resultLabel;
    private javax.swing.JTextField resultTextField;
    // End of variables declaration//GEN-END:variables

    // ActionListener implementation -------------------------------------------
    @Override
    public void actionPerformed(ActionEvent e) {
        if (locationComboBox == e.getSource()) {
            if (!ignoreRootCombo) {
                updatePackages();
            }
            updateText();
            fireChange();
        } else if (packageComboBox == e.getSource()) {
            updateText();
            fireChange();
        } else if (packageComboBox.getEditor() == e.getSource()) {
            updateText();
            fireChange();
        }
    }

    // DocumentListener implementation -----------------------------------------
    @Override
    public void changedUpdate(DocumentEvent e) {
        updateText();
        fireChange();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    // Private methods ---------------------------------------------------------
    private void updatePackages() {
        final Object item = locationComboBox.getSelectedItem();
        if (!(item instanceof SourceGroup)) {
            return;
        }
        WAIT_MODEL.setSelectedItem(packageComboBox.getEditor().getItem());
        packageComboBox.setModel(WAIT_MODEL);

        if (updatePackagesTask != null) {
            updatePackagesTask.cancel();
        }

        updatePackagesTask = new RequestProcessor("ComboUpdatePackages").post(new Runnable() { // NOI18N
            @Override
            public void run() {
                final ComboBoxModel model = PackageView.createListView((SourceGroup) item);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        model.setSelectedItem(packageComboBox.getEditor().getItem());
                        packageComboBox.setModel(model);
                    }
                });
            }
        });
    }

    private void updateText() {
        final Object selectedItem = locationComboBox.getSelectedItem();
        String createdFileName;
        if (selectedItem instanceof SourceGroup) {
            SourceGroup g = (SourceGroup) selectedItem;
            FileObject rf = g.getRootFolder();
            String packageName = getPackageFileName();
            String fxmlName = getFXMLName();
            if (fxmlName != null && fxmlName.length() > 0) {
                fxmlName = fxmlName + FXMLTemplateWizardIterator.FXML_FILE_EXTENSION;
            }
            String packagePath = (packageName.startsWith("/") || packageName.startsWith(File.separator) ? "" : "/") + // NOI18N
                    packageName
                    + (packageName.endsWith("/") || packageName.endsWith(File.separator) || packageName.length() == 0 ? "" : "/"); // NOI18N
            createdFileName = FileUtil.getFileDisplayName(rf) + packagePath + fxmlName;
            rootFolder = new File(rf.getPath() + packagePath);
        } else {
            //May be null if nothing selected
            createdFileName = "";   //NOI18N
        }
        resultTextField.setText(createdFileName.replace('/', File.separatorChar)); // NOI18N
    }

    private SourceGroup getPreselectedGroup(FileObject folder) {
        for(int i = 0; folder != null && i < groups.length; i++) {
            FileObject root = groups[i].getRootFolder();
            if (root.equals(folder) || FileUtil.isParentOf(root, folder)) {
                return groups[i];
            }
        }
        return groups[0];
    }
    
    // Private innerclasses ----------------------------------------------------

    /**
     * Displays a {@link SourceGroup} in {@link #rootComboBox}.
     */
    private static final class GroupListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String name;
            Icon icon;
            if (value == null) {
                name = ""; //NOI18N
                icon = null;
            } else {
                assert value instanceof SourceGroup;
                SourceGroup g = (SourceGroup) value;
                name = g.getDisplayName();
                icon = g.getIcon(false);
            }
            super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
            setIcon(icon);
            
            return this;
        }
    }
    
    static class Panel implements WizardDescriptor.Panel<WizardDescriptor> {
        
        private ConfigureFXMLPanelVisual component;
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private WizardDescriptor settings;

        public Panel(Project project, SourceGroup[] groups) {
            component = new ConfigureFXMLPanelVisual(this, project, groups);
        }

        @Override
        public Component getComponent() {
            return component;
        }

        @Override
        public HelpCtx getHelp() {
            return new HelpCtx(ConfigureFXMLPanelVisual.class);
        }

        @Override
        public void readSettings(WizardDescriptor settings) {
            this.settings = settings;
            // Try to preselect a folder
            FileObject preselectedFolder = Templates.getTargetFolder(settings);
            // Init values
            component.initValues(Templates.getTemplate(settings), preselectedFolder);

            // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
            // this name is used in NewFileWizard to modify the title
            Object substitute = component.getClientProperty("NewFileWizard_Title"); // NOI18N
            if (substitute != null) {
                settings.putProperty("NewFileWizard_Title", substitute); // NOI18N
            }
        }

        @Override
        public void storeSettings(WizardDescriptor settings) {
            Object value = settings.getValue();
            if (WizardDescriptor.PREVIOUS_OPTION.equals(value)
                    || WizardDescriptor.CANCEL_OPTION.equals(value)
                    || WizardDescriptor.CLOSED_OPTION.equals(value)) {
                return;
            }
            if (isValid()) {
                Templates.setTargetFolder(settings, getTargetFolderFromView());
                Templates.setTargetName(settings, component.getFXMLName());
                settings.putProperty(FXMLTemplateWizardIterator.PROP_SRC_ROOTS, component.getSrcRoots());
                settings.putProperty(FXMLTemplateWizardIterator.PROP_ROOT_FOLDER, component.getRootFolder());
            }
            settings.putProperty("NewFileWizard_Title", null); // NOI18N
        }

        @Override
        public boolean isValid() {
            if (component.getFXMLName() == null) {
                FXMLTemplateWizardIterator.setInfoMessage("WARN_ConfigureFXMLPanel_Provide_FXML_Name", settings); // NOI18N
                return false;
            }
            
            if (!FXMLTemplateWizardIterator.isValidPackageName(component.getPackageName())) {
                FXMLTemplateWizardIterator.setErrorMessage("WARN_ConfigureFXMLPanel_Provide_Package_Name", settings); // NOI18N
                return false;
            }

            if (!FXMLTemplateWizardIterator.isValidPackage(component.getLocationFolder(), component.getPackageName())) {
                FXMLTemplateWizardIterator.setErrorMessage("WARN_ConfigureFXMLPanel_Package_Invalid", settings); // NOI18N
                return false;
            }

            FileObject rootFolder = component.getLocationFolder();
            String errorMessage = FXMLTemplateWizardIterator.canUseFileName(rootFolder, 
                    component.getPackageFileName(), component.getFXMLName(), JFXProjectProperties.FXML_EXTENSION);
            settings.getNotificationLineSupport().setErrorMessage(errorMessage);
            if (errorMessage != null) {
                return false;
            }

            return true;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        private void fireChangeEvent() {
            changeSupport.fireChange();
        }

        private FileObject getTargetFolderFromView() {
            FileObject rootFolder = component.getLocationFolder();
            String packageFileName = component.getPackageFileName();
            FileObject folder = rootFolder.getFileObject(packageFileName);
            if (folder == null) {
                try {
                    folder = rootFolder;
                    StringTokenizer tk = new StringTokenizer(packageFileName, "/"); // NOI18N
                    String name = null;
                    while (tk.hasMoreTokens()) {
                        name = tk.nextToken();
                        FileObject fo = folder.getFileObject(name, ""); // NOI18N
                        if (fo == null) {
                            break;
                        }
                        folder = fo;
                    }
                    folder = folder.createFolder(name);
                    while (tk.hasMoreTokens()) {
                        name = tk.nextToken();
                        folder = folder.createFolder(name);
                    }
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
            return folder;
        }
    }
}
