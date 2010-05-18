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
package org.netbeans.modules.wsdlextensions.jdbc.wizards;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.event.ActionListener;

import java.io.File;

import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import org.openide.WizardDescriptor;

/**
 * DOCUMENT ME!
 * 
 * @author
 */
public class SimpleTargetChooserPanelGUI extends javax.swing.JPanel implements ActionListener, DocumentListener {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /** prefered dimmension of the panels */
    private static final java.awt.Dimension PREF_DIM = new java.awt.Dimension(500, 340);

    private static final String NEW_FILE_PREFIX = NbBundle.getMessage(SimpleTargetChooserPanelGUI.class,
            "LBL_SimpleTargetChooserPanelGUI_NewFilePrefix"); // NOI18N

    private final ListCellRenderer CELL_RENDERER = new GroupCellRenderer();

    private Project project;

    private String expectedExtension;

    private final List /* <ChangeListener> */listeners = new ArrayList();

    private SourceGroup[] folders;

    private boolean isFolder;

    private FileObject mFolderRoot = null;

    private static final String mFolderText = "";

    // Variables declaration - do not modify
    private javax.swing.JPanel bottomPanelContainer;

    private javax.swing.JButton browseButton;

    private javax.swing.JTextField documentNameTextField;

    private javax.swing.JTextField fileTextField;

    private javax.swing.JTextField folderTextField;

    private javax.swing.JLabel jLabel1;

    private javax.swing.JLabel jLabel2;

    private javax.swing.JLabel jLabel3;

    private javax.swing.JLabel jLabel4;

    private javax.swing.JPanel jPanel1;

    private javax.swing.JComboBox locationComboBox;

    private javax.swing.JLabel locationLabel;

    private javax.swing.JTextField projectTextField;

    private javax.swing.JSeparator targetSeparator;

    /**
     * Creates new form SimpleTargetChooserGUI
     * 
     * @param project DOCUMENT ME!
     * @param folders DOCUMENT ME!
     * @param bottomPanel DOCUMENT ME!
     * @param isFolder DOCUMENT ME!
     */
    public SimpleTargetChooserPanelGUI(Project project, SourceGroup[] folders, Component bottomPanel, boolean isFolder) {
        this.project = project;
        this.folders = folders;
        this.isFolder = isFolder;
        this.initComponents();

        this.locationComboBox.setRenderer(this.CELL_RENDERER);

        if (bottomPanel != null) {
            this.bottomPanelContainer.add(bottomPanel, java.awt.BorderLayout.CENTER);
        }

        initValues(null, null);

        this.browseButton.addActionListener(this);
        this.locationComboBox.addActionListener(this);
        this.documentNameTextField.getDocument().addDocumentListener(this);
        this.folderTextField.getDocument().addDocumentListener(this);

        this.setName(NbBundle.getMessage(SimpleTargetChooserPanelGUI.class, "LBL_SimpleTargetChooserPanel_Name")); // NOI18N
    }

    /**
     * DOCUMENT ME!
     * 
     * @param template DOCUMENT ME!
     * @param preselectedFolder DOCUMENT ME!
     */
    public void initValues(FileObject template, FileObject preselectedFolder ) {
        assert project != null;

        projectTextField.setText(ProjectUtils.getInformation(project).getDisplayName());

        Sources sources = ProjectUtils.getSources(project);

        folders = sources.getSourceGroups(
                Sources.TYPE_GENERIC
            );

        if ((folders == null) || (folders.length < 1)) {
            folders = sources.getSourceGroups(Sources.TYPE_GENERIC);
        }

        if (folders.length < 2) {
            // one source group i.e. hide Location
            locationLabel.setVisible(false);
            locationComboBox.setVisible(false);
        } else {
            // more source groups user needs to select location
            locationLabel.setVisible(true);
            locationComboBox.setVisible(true);
        }

        locationComboBox.setModel(new DefaultComboBoxModel(folders));

        // Guess the group we want to create the file in
        SourceGroup preselectedGroup = getPreselectedGroup(folders, preselectedFolder);
        locationComboBox.setSelectedItem(preselectedGroup);

        // Create OS dependent relative name
        folderTextField.setText(
            getRelativeNativeName(preselectedGroup.getRootFolder(), preselectedFolder)
        );

        
           mFolderRoot = preselectedFolder;
        /*
         * mFolderText = ( getRelativeNativeName( preselectedGroup.getRootFolder(),
         * preselectedFolder ) ); folderTextField.setText("");
         */
        String ext = (template == null) ? "" : template.getExt(); // NOI18N
        expectedExtension = (ext.length() == 0) ? "" : ("." + ext); // NOI18N

        String displayName = null;

        try {
            if (template != null) {
                DataObject templateDo = DataObject.find(template);
                displayName = templateDo.getNodeDelegate().getDisplayName();
            }
        } catch (DataObjectNotFoundException ex) {
            displayName = template.getName();
        }

        putClientProperty("NewDtelWizard_Title", displayName); // NOI18N
		
        if (template != null) {
            documentNameTextField.setText(NEW_FILE_PREFIX + template.getName());
            documentNameTextField.selectAll();
        }

        if (isFolder) {
            jLabel3.setText(
                NbBundle.getMessage(
                    SimpleTargetChooserPanelGUI.class, "LABEL_TargetChooser_FolderName_Label"
                )
            ); // NOI18N
            jLabel3.setDisplayedMnemonic(
                NbBundle.getMessage(
                    SimpleTargetChooserPanelGUI.class, "MNE_TargetChooser_FileName_Label"
                ).charAt(0)
            ); // NOI18N
            jLabel2.setText(
                NbBundle.getMessage(
                    SimpleTargetChooserPanelGUI.class, "LABEL_TargetChooser_ParentFolder_Label"
                )
            ); // NOI18N
            jLabel2.setDisplayedMnemonic(
                NbBundle.getMessage(
                    SimpleTargetChooserPanelGUI.class, "MNE_TargetChooser_ParentFolder_Label"
                ).charAt(0)
            ); // NOI18N
            jLabel4.setText(
                NbBundle.getMessage(
                    SimpleTargetChooserPanelGUI.class, "LABEL_TargetChooser_CreatedFolder_Label"
                )
            ); // NOI18N
            jLabel4.setDisplayedMnemonic(
                NbBundle.getMessage(
                    SimpleTargetChooserPanelGUI.class, "MNE_TargetChooser_CreatedFolder_Label"
                ).charAt(0)
            ); // NOI18N
        } else {
            jLabel3.setText(
                NbBundle.getMessage(
                    SimpleTargetChooserPanelGUI.class, "LABEL_TargetChooser_FileName_Label"
                )
            ); // NOI18N
            jLabel2.setText(
                NbBundle.getMessage(
                    SimpleTargetChooserPanelGUI.class, "LABEL_TargetChooser_Folder_Label"
                )
            ); // NOI18N
            jLabel4.setText(
                NbBundle.getMessage(
                    SimpleTargetChooserPanelGUI.class, "LABEL_TargetChooser_CreatedFile_Label"
                )
            ); // NOI18N
            jLabel3.setDisplayedMnemonic(
                NbBundle.getMessage(
                    SimpleTargetChooserPanelGUI.class, "MNE_TargetChooser_FileName_Label"
                ).charAt(0)
            ); // NOI18N
            jLabel2.setDisplayedMnemonic(
                NbBundle.getMessage(
                    SimpleTargetChooserPanelGUI.class, "MNE_TargetChooser_Folder_Label"
                ).charAt(0)
            ); // NOI18N
            jLabel4.setDisplayedMnemonic(
                NbBundle.getMessage(
                    SimpleTargetChooserPanelGUI.class, "MNE_TargetChooser_CreatedFile_Label"
                ).charAt(0)
            ); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public SourceGroup getTargetGroup() {
        return (SourceGroup) this.locationComboBox.getSelectedItem();
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getTargetFolder() {
        /*
         * String fName = folderTextField.getText().trim(); String folderName = mFolderText + (
         * fName.startsWith("/") || fName.startsWith( File.separator ) ? "" : "/" ) + // NOI18N
         * fName; // folderTextField.getText().trim();
         */
        final String folderName = this.folderTextField.getText().trim();

        if (folderName.length() == 0) {
            return null;
        } else {
            return folderName.replace(File.separatorChar, '/'); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getTargetName() {
        final String text = this.documentNameTextField.getText().trim();

        if (text.length() == 0) {
            return null;
        } else {
            return text;
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public java.awt.Dimension getPreferredSize() {
        return SimpleTargetChooserPanelGUI.PREF_DIM;
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
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    private void initComponents() {// GEN-BEGIN:initComponents

        java.awt.GridBagConstraints gridBagConstraints;

        this.jPanel1 = new javax.swing.JPanel();
        this.jLabel3 = new javax.swing.JLabel();
        this.documentNameTextField = new javax.swing.JTextField();
        this.jLabel1 = new javax.swing.JLabel();
        this.projectTextField = new javax.swing.JTextField();
        this.locationLabel = new javax.swing.JLabel();
        this.locationComboBox = new javax.swing.JComboBox();
        this.jLabel2 = new javax.swing.JLabel();
        this.folderTextField = new javax.swing.JTextField();
        this.browseButton = new javax.swing.JButton();
        this.jLabel4 = new javax.swing.JLabel();
        this.fileTextField = new javax.swing.JTextField();
        this.targetSeparator = new javax.swing.JSeparator();
        this.bottomPanelContainer = new javax.swing.JPanel();

        this.setLayout(new java.awt.GridBagLayout());

        this.getAccessibleContext().setAccessibleDescription(
                org.openide.util.NbBundle.getBundle(SimpleTargetChooserPanelGUI.class).getString(
                        "AD_SimpleTargetChooserPanelGUI"));
        this.jPanel1.setLayout(new java.awt.GridBagLayout());

        this.jLabel3.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class,
                "MNE_TargetChooser_FileName_Label").charAt(0));
        this.jLabel3.setLabelFor(this.documentNameTextField);
        this.jLabel3.setText(org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class,
                "LABEL_TargetChooser_FileName_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        this.jPanel1.add(this.jLabel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        this.jPanel1.add(this.documentNameTextField, gridBagConstraints);
        this.documentNameTextField.getAccessibleContext().setAccessibleDescription(
                org.openide.util.NbBundle.getBundle(SimpleTargetChooserPanelGUI.class).getString(
                        "AD_documentNameTextField"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 24, 0);
        this.add(this.jPanel1, gridBagConstraints);

        this.jLabel1.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class,
                "MNE_TargetChooser_Project_Label").charAt(0));
        this.jLabel1.setText(org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class,
                "LABEL_TargetChooser_Project_Label"));
        this.jLabel1.setLabelFor(this.projectTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        this.add(this.jLabel1, gridBagConstraints);

        this.projectTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        this.add(this.projectTextField, gridBagConstraints);
        this.projectTextField.getAccessibleContext().setAccessibleDescription(
                org.openide.util.NbBundle.getBundle(SimpleTargetChooserPanelGUI.class).getString("AD_projectTextField"));

        this.locationLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class,
                "MNE_TargetChooser_Location_Label").charAt(0));
        this.locationLabel.setLabelFor(this.locationComboBox);
        this.locationLabel.setText(org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class,
                "LABEL_TargetChooser_Location_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        this.add(this.locationLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 0);
        this.add(this.locationComboBox, gridBagConstraints);
        this.locationComboBox.getAccessibleContext().setAccessibleDescription(
                org.openide.util.NbBundle.getBundle(SimpleTargetChooserPanelGUI.class).getString("AD_locationComboBox"));

        this.jLabel2.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class,
                "MNE_TargetChooser_ParentFolder_Label").charAt(0));
        this.jLabel2.setLabelFor(this.folderTextField);
        this.jLabel2.setText(org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class,
                "LABEL_TargetChooser_Folder_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        this.add(this.jLabel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        this.add(this.folderTextField, gridBagConstraints);
        this.folderTextField.getAccessibleContext().setAccessibleDescription(
                org.openide.util.NbBundle.getBundle(SimpleTargetChooserPanelGUI.class).getString("AD_folderTextField"));

        this.browseButton.setMnemonic(org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class,
                "MNE_TargetChooser_Browse_Button").charAt(0));
        this.browseButton.setText(org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class,
                "LABEL_TargetChooser_Browse_Button"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        this.add(this.browseButton, gridBagConstraints);
        this.browseButton.getAccessibleContext().setAccessibleDescription(
                org.openide.util.NbBundle.getBundle(SimpleTargetChooserPanelGUI.class).getString("AD_browseButton"));

        this.jLabel4.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class,
                "MNE_TargetChooser_CreatedFile_Label").charAt(0));
        this.jLabel4.setText(org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class,
                "LABEL_TargetChooser_CreatedFile_Label"));
        this.jLabel4.setLabelFor(this.fileTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        this.add(this.jLabel4, gridBagConstraints);

        this.fileTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        this.add(this.fileTextField, gridBagConstraints);
        this.fileTextField.getAccessibleContext().setAccessibleDescription(
                org.openide.util.NbBundle.getBundle(SimpleTargetChooserPanelGUI.class).getString("AD_fileTextField"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        this.add(this.targetSeparator, gridBagConstraints);

        this.bottomPanelContainer.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        this.add(this.bottomPanelContainer, gridBagConstraints);
    }// GEN-END:initComponents

    // End of variables declaration
    private SourceGroup getPreselectedGroup(SourceGroup[] groups, FileObject folder) {
        for (int i = 0; folder != null && i < groups.length; i++) {
            if (FileUtil.isParentOf(groups[i].getRootFolder(), folder)) {
                return groups[i];
            }
        }

        return groups[0];
    }

    private String getRelativeNativeName(FileObject root, FileObject folder) {
        if (root == null) {
            throw new IllegalArgumentException("null root passed to getRelativeNativeName"); // NOI18N
        }

        String path;

        if (folder == null) {
            path = ""; // NOI18N
        } else {
            path = FileUtil.getRelativePath(root, folder);
        }

        return path == null ? "" : path.replace('/', File.separatorChar); // NOI18N
    }

    private void updateCreatedFolder() {
        FileObject root = ((SourceGroup) this.locationComboBox.getSelectedItem()).getRootFolder();

        final String folderName = this.folderTextField.getText().trim();
        final String documentName = this.documentNameTextField.getText().trim();

        final String createdFileName = FileUtil.getFileDisplayName(root) +
        /*
         * ( mFolderText.startsWith("/") || mFolderText.startsWith( File.separator ) ? "" : "/" ) + //
         * NOI18N mFolderText +
         */
        (folderName.startsWith("/") || folderName.startsWith(File.separator) ? "" : "/")
                + // NOI18N
                folderName
                + (folderName.endsWith("/") || folderName.endsWith(File.separator) || folderName.length() == 0 ? ""
                        : "/") + // NOI18N
                documentName + this.expectedExtension;

        this.fileTextField.setText(createdFileName.replace('/', File.separatorChar)); // NOI18N

        this.fireChange();
    }

    // ActionListener implementation -------------------------------------------
    public void actionPerformed(final java.awt.event.ActionEvent e) {
        if (this.browseButton == e.getSource()) {
            FileObject fo = null;

            // Show the browse dialog
            SourceGroup group = (SourceGroup) this.locationComboBox.getSelectedItem();

            fo = BrowseFolders.showDialog(new SourceGroup[] { group }, project, this.folderTextField.getText().replace(
                    File.separatorChar, '/')); // NOI18N

            if (fo != null && fo.isFolder()) {
                final String relPath = FileUtil.getRelativePath(group.getRootFolder(), fo);
                this.folderTextField.setText(relPath.replace('/', File.separatorChar)); // NOI18N
            }
        } else if (this.locationComboBox == e.getSource()) {
            this.updateCreatedFolder();
        }
    }

    // DocumentListener implementation -----------------------------------------
    public void changedUpdate(final javax.swing.event.DocumentEvent e) {
        this.updateCreatedFolder();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param e DOCUMENT ME!
     */
    public void insertUpdate(final javax.swing.event.DocumentEvent e) {
        this.updateCreatedFolder();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param e DOCUMENT ME!
     */
    public void removeUpdate(final javax.swing.event.DocumentEvent e) {
        this.updateCreatedFolder();
    }

    // Rendering of the location combo box -------------------------------------
    private class GroupCellRenderer extends JLabel implements ListCellRenderer {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         * Creates a new GroupCellRenderer object.
         */
        public GroupCellRenderer() {
            this.setOpaque(true);
        }

        /**
         * DOCUMENT ME!
         * 
         * @param list DOCUMENT ME!
         * @param value DOCUMENT ME!
         * @param index DOCUMENT ME!
         * @param isSelected DOCUMENT ME!
         * @param cellHasFocus DOCUMENT ME!
         * @return DOCUMENT ME!
         */
        public Component getListCellRendererComponent(final JList list,
                                                      final Object value,
                                                      final int index,
                                                      final boolean isSelected,
                                                      final boolean cellHasFocus) {
            if (value instanceof SourceGroup) {
                SourceGroup group = (SourceGroup) value;
                final String projectDisplayName = ProjectUtils.getInformation(project).getDisplayName();
                final String groupDisplayName = group.getDisplayName();

                if (projectDisplayName.equals(groupDisplayName)) {
                    this.setText(groupDisplayName);
                } else {
                    /*
                     * setText( MessageFormat.format(
                     * org.netbeans.modules.project.ui.PhysicalView.GroupNode.GROUP_NAME_PATTERN,
                     * new Object[] { groupDisplayName, projectDisplayName,
                     * group.getRootFolder().getName() } ) );
                     */
                    this.setText(MessageFormat.format(NbBundle.getMessage(SimpleTargetChooserPanelGUI.class,
                            "FMT_TargetChooser_GroupProjectNameBadge"), // NOI18N
                            new Object[] { groupDisplayName, projectDisplayName }));
                }

                this.setIcon(group.getIcon(false));
            } else {
                this.setText(value.toString());
                this.setIcon(null);
            }

            if (isSelected) {
                this.setBackground(list.getSelectionBackground());
                this.setForeground(list.getSelectionForeground());
            } else {
                this.setBackground(list.getBackground());
                this.setForeground(list.getForeground());
            }

            return this;
        }
    }
}
