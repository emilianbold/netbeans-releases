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
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Anton Chechel <anton.chechel@oracle.com>
 */
// Used JavaTargetChooserPanelGUI
public class ConfigureFXMLPanelVisual extends JPanel implements ActionListener, DocumentListener {
    private static final String FXML_FILE_EXTENSION = ".fxml"; // NOI18N
    
    private final List<ChangeListener> listeners;
    private Project project;
    private SourceGroup groups[];

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

    public ConfigureFXMLPanelVisual(Project project, SourceGroup[] groups) {
        this.project = project;
        if (project == null) {
                throw new IllegalArgumentException(
                        NbBundle.getMessage(ConfigureFXMLPanelVisual.class,
                            "MSG_ConfigureFXMLPanel_Project_Error")); // NOI18N
        }

        this.groups = groups;
        for (SourceGroup sourceGroup : groups) {
            if (sourceGroup == null) {
                throw new IllegalArgumentException(
                        NbBundle.getMessage(ConfigureFXMLPanelVisual.class,
                            "MSG_ConfigureFXMLPanel_SG_Error")); // NOI18N
            }
        }
        
        listeners = new ArrayList<ChangeListener>();

        initComponents(); // Matisse
        initComponents2(); // My own
    }

    private void initComponents2() {
        fxmlNameTextField.getDocument().addDocumentListener(this);
        controllerNameTextField.getDocument().addDocumentListener(this);
        cssNameTextField.getDocument().addDocumentListener(this);
        
        packageComboBox.getEditor().addActionListener(this);
        Component packageEditor = packageComboBox.getEditor().getEditorComponent();
        if (packageEditor instanceof JTextField) {
            ((JTextField) packageEditor).getDocument().addDocumentListener(this);
        }

        locationComboBox.setRenderer(new GroupListCellRenderer());
        packageComboBox.setRenderer(PackageView.listRenderer());
        locationComboBox.addActionListener(this);

        setName(name());
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
        Object preselectedPackage = getPreselectedPackage(preselectedGroup, preselectedFolder, packageComboBox.getModel());
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
                        FileObject fo = preselectedFolder.getFileObject(activeName, template.getExt()); // NOI18N
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
    
    public String name() {
        return NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "LBL_ConfigureFXMLPanel_Name"); // NOI18N
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

    boolean isControllerEnabled() {
        return controllerCheckBox.isSelected();
    }

    String getControllerName() {
        String text = controllerNameTextField.getText().trim();
        return text.length() == 0 ? null : text;
    }

    boolean isCSSEnabled() {
        return cssCheckBox.isSelected();
    }

    String getCSSName() {
        String text = cssNameTextField.getText().trim();
        return text.length() == 0 ? null : text;
    }

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
    
    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener l : listeners) {
            l.stateChanged(e);
        }
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
        controllerCheckBox = new javax.swing.JCheckBox();
        controllerNameLabel = new javax.swing.JLabel();
        controllerNameTextField = new javax.swing.JTextField();
        cssCheckBox = new javax.swing.JCheckBox();
        cssNameLabel = new javax.swing.JLabel();
        cssNameTextField = new javax.swing.JTextField();
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

        org.openide.awt.Mnemonics.setLocalizedText(controllerCheckBox, org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.controllerCheckBox.text")); // NOI18N
        controllerCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                controllerCheckBoxItemStateChanged(evt);
            }
        });

        controllerNameLabel.setLabelFor(controllerNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(controllerNameLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.controllerNameLabel.text")); // NOI18N
        controllerNameLabel.setEnabled(false);

        controllerNameTextField.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(cssCheckBox, org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.cssCheckBox.text")); // NOI18N
        cssCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cssCheckBoxItemStateChanged(evt);
            }
        });

        cssNameLabel.setLabelFor(cssNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(cssNameLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.cssNameLabel.text")); // NOI18N
        cssNameLabel.setEnabled(false);

        cssNameTextField.setEnabled(false);

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
                .addComponent(fxmlNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fxmlNameTextField))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addComponent(cssNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(controllerNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(controllerCheckBox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cssCheckBox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cssNameTextField)
                    .addComponent(controllerNameTextField)))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(projectLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(locationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(packageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(resultLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(projectTextField)
                    .addComponent(locationComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(resultTextField)
                    .addComponent(packageComboBox, 0, 420, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fxmlNameLabel)
                    .addComponent(fxmlNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(controllerCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(controllerNameLabel)
                    .addComponent(controllerNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cssCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cssNameLabel)
                    .addComponent(cssNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                .addContainerGap(110, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void controllerCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_controllerCheckBoxItemStateChanged
        controllerNameLabel.setEnabled(controllerCheckBox.isSelected());
        controllerNameTextField.setEnabled(controllerCheckBox.isSelected());
        fireChange();
    }//GEN-LAST:event_controllerCheckBoxItemStateChanged

    private void cssCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cssCheckBoxItemStateChanged
        cssNameLabel.setEnabled(cssCheckBox.isSelected());
        cssNameTextField.setEnabled(cssCheckBox.isSelected());
        fireChange();
    }//GEN-LAST:event_cssCheckBoxItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox controllerCheckBox;
    private javax.swing.JLabel controllerNameLabel;
    private javax.swing.JTextField controllerNameTextField;
    private javax.swing.JCheckBox cssCheckBox;
    private javax.swing.JLabel cssNameLabel;
    private javax.swing.JTextField cssNameTextField;
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
            FileObject rootFolder = g.getRootFolder();
            String packageName = getPackageFileName();
            String fxmlName = fxmlNameTextField.getText().trim();
            if (fxmlName.length() > 0) {
                fxmlName = fxmlName + FXML_FILE_EXTENSION;
            }
            createdFileName = FileUtil.getFileDisplayName(rootFolder)
                    + (packageName.startsWith("/") || packageName.startsWith(File.separator) ? "" : "/") + // NOI18N
                    packageName
                    + (packageName.endsWith("/") || packageName.endsWith(File.separator) || packageName.length() == 0 ? "" : "/") + // NOI18N
                    fxmlName;
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
    
    /**
     * Get a package combo model item for the package the user selected before
     * opening the wizard. May return null if it cannot find it; or a String
     * instance if there is a well-defined package but it is not listed among
     * the packages shown in the list model.
     */
    private Object getPreselectedPackage(SourceGroup group, FileObject folder, ListModel model) {
        if (folder == null) {
            return null;
        }

        FileObject root = group.getRootFolder();
        String relPath = FileUtil.getRelativePath(root, folder);
        if (relPath == null) {
            // Group Root folder is not a parent of the preselected folder
            // No package should be selected
            return null;
        } else {
            // Find the right item.            
            String name = relPath.replace('/', '.'); // NOI18N
            return name;
        }
    }

    // Private innerclasses ----------------------------------------------------

    /**
     * Displays a {@link SourceGroup} in {@link #rootComboBox}.
     */
    private static final class GroupListCellRenderer extends DefaultListCellRenderer {

        public GroupListCellRenderer() {
        }

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
}
