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

package org.netbeans.modules.apisupport.project.ui.wizard.wizard;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * The second panel in the <em>New Wizard Wizard</em>.
 *
 * @author Martin Krauskopf
 */
final class NameIconLocationPanel extends BasicWizardIterator.Panel {
    
    private boolean firstTime = true;
    private DataModel data;
    
    private static final String ENTER_LABEL =
            NbBundle.getMessage(NameIconLocationPanel.class, "CTL_EnterLabel");
    private static final String NONE_LABEL =
            NbBundle.getMessage(NameIconLocationPanel.class, "CTL_None");
    
    
    /** Creates new NameIconLocationPanel */
    public NameIconLocationPanel(final WizardDescriptor setting, final DataModel data) {
        super(setting);
        this.data = data;
        initComponents();
        DocumentListener updateListener = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                updateData();
            }
        };
        classNamePrefix.getDocument().addDocumentListener(updateListener);
        displayName.getDocument().addDocumentListener(updateListener);
        Component editorComp = packageName.getEditor().getEditorComponent();
        if (editorComp instanceof JTextComponent) {
            ((JTextComponent) editorComp).getDocument().addDocumentListener(updateListener);
        }
        if (category.getEditor().getEditorComponent() instanceof JTextField) {
            JTextComponent txt = (JTextComponent) category.getEditor().getEditorComponent();
            txt.getDocument().addDocumentListener(updateListener);
        }
    }
    
    protected String getPanelName() {
        return getMessage("LBL_NameIconLocation_Title"); // NOI18N
    }
    
    protected void storeToDataModel() {
        data.setClassNamePrefix(getClassNamePrefix());
        data.setDisplayName(displayName.getText());
        data.setIcon(icon.getText().equals(NONE_LABEL) ? null : icon.getText());
        data.setPackageName(packageName.getEditor().getItem().toString());
        Object item = category.getSelectedItem();
        if (item != null && item instanceof UIUtil.LayerFolderPresenter) {
            data.setCategory(((UIUtil.LayerFolderPresenter) item).getCategoryPath());
        } else {
            data.setCategory("Templates/Other"); // NOI18N
        }
    }
    
    protected void readFromDataModel() {
        boolean visible = data.isFileTemplateType();
        displayName.setVisible(visible);
        displayNameTxt.setVisible(visible);
        category.setVisible(visible);
        categoryTxt.setVisible(visible);
        icon.setVisible(visible);
        iconButton.setVisible(visible);
        iconTxt.setVisible(visible);
        if (firstTime) {
            loadCategories();
            firstTime = false;
            setValid(Boolean.FALSE);
        } else {
            updateData();
        }
    }
    
    private void updateData() {
        storeToDataModel();
        if (checkValidity()) {
            CreatedModifiedFiles files = data.getCreatedModifiedFiles();
            createdFiles.setText(UIUtil.generateTextAreaContent(files.getCreatedPaths()));
            modifiedFiles.setText(UIUtil.generateTextAreaContent(files.getModifiedPaths()));
        }
    }
    
    private boolean checkValidity() {
        boolean valid = false;
        if (!Utilities.isJavaIdentifier(getClassNamePrefix())) {
            setErrorMessage(getMessage("MSG_ClassNameMustBeValidJavaIdentifier")); // NOI18N
        } else if (data.isFileTemplateType() &&
                (getDisplayName().equals("") || getDisplayName().equals(ENTER_LABEL))) {
            setErrorMessage(getMessage("MSG_DisplayNameMustBeEntered")); // NOI18N
        } else {
            setErrorMessage(null);
            valid = true;
        }
        return valid;
    }
    
    private String getDisplayName() {
        return displayName.getText().trim();
    }
    
    private String getClassNamePrefix() {
        return classNamePrefix.getText().trim();
    }
    
    private void loadCategories() {
        category.setModel(UIUtil.createLayerPresenterComboModel(
                data.getProject(), "Templates")); // NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        classNamePrefixTxt = new javax.swing.JLabel();
        classNamePrefix = new javax.swing.JTextField();
        displayNameTxt = new javax.swing.JLabel();
        displayName = new javax.swing.JTextField();
        iconTxt = new javax.swing.JLabel();
        icon = new javax.swing.JTextField();
        iconButton = new javax.swing.JButton();
        projectTxt = new javax.swing.JLabel();
        project = new JTextField(ProjectUtils.getInformation(data.getProject()).getDisplayName());
        packageNameTxt = new javax.swing.JLabel();
        packageName = UIUtil.createPackageComboBox(data.getSourceRootGroup());
        createdFilesTxt = new javax.swing.JLabel();
        createdFiles = new javax.swing.JTextArea();
        modifiedFilesTxt = new javax.swing.JLabel();
        modifiedFiles = new javax.swing.JTextArea();
        categoryTxt = new javax.swing.JLabel();
        category = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        classNamePrefixTxt.setLabelFor(classNamePrefix);
        org.openide.awt.Mnemonics.setLocalizedText(classNamePrefixTxt, org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "LBL_ClassNamePrefix"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 12);
        add(classNamePrefixTxt, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(classNamePrefix, gridBagConstraints);

        displayNameTxt.setLabelFor(displayName);
        org.openide.awt.Mnemonics.setLocalizedText(displayNameTxt, org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "LBL_DisplayName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 12);
        add(displayNameTxt, gridBagConstraints);

        displayName.setText(org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "CTL_EnterLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(displayName, gridBagConstraints);

        iconTxt.setLabelFor(icon);
        org.openide.awt.Mnemonics.setLocalizedText(iconTxt, org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "LBL_Icon"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 12);
        add(iconTxt, gridBagConstraints);

        icon.setEditable(false);
        icon.setText(org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "CTL_None"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 0);
        add(icon, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(iconButton, org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "LBL_Icon_Browse"));
        iconButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                iconButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(18, 12, 0, 0);
        add(iconButton, gridBagConstraints);

        projectTxt.setLabelFor(project);
        org.openide.awt.Mnemonics.setLocalizedText(projectTxt, org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "LBL_ProjectName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 6, 12);
        add(projectTxt, gridBagConstraints);

        project.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 6, 0);
        add(project, gridBagConstraints);

        packageNameTxt.setLabelFor(packageName);
        org.openide.awt.Mnemonics.setLocalizedText(packageNameTxt, org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "LBL_PackageName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(packageNameTxt, gridBagConstraints);

        packageName.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(packageName, gridBagConstraints);

        createdFilesTxt.setLabelFor(createdFiles);
        org.openide.awt.Mnemonics.setLocalizedText(createdFilesTxt, org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "LBL_CreatedFiles"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(36, 0, 6, 12);
        add(createdFilesTxt, gridBagConstraints);

        createdFiles.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        createdFiles.setColumns(20);
        createdFiles.setEditable(false);
        createdFiles.setRows(5);
        createdFiles.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(36, 0, 6, 0);
        add(createdFiles, gridBagConstraints);

        modifiedFilesTxt.setLabelFor(modifiedFiles);
        org.openide.awt.Mnemonics.setLocalizedText(modifiedFilesTxt, org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "LBL_ModifiedFiles"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(modifiedFilesTxt, gridBagConstraints);

        modifiedFiles.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        modifiedFiles.setColumns(20);
        modifiedFiles.setEditable(false);
        modifiedFiles.setRows(5);
        modifiedFiles.setToolTipText("modifiedFilesValue");
        modifiedFiles.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        add(modifiedFiles, gridBagConstraints);

        categoryTxt.setLabelFor(category);
        org.openide.awt.Mnemonics.setLocalizedText(categoryTxt, org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "LBL_Category"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 12);
        add(categoryTxt, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        add(category, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    private void iconButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iconButtonActionPerformed
        JFileChooser chooser = UIUtil.getIconFileChooser();
        int ret = chooser.showDialog(this, getMessage("LBL_Select")); // NOI18N
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file =  chooser.getSelectedFile();
            icon.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_iconButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox category;
    private javax.swing.JLabel categoryTxt;
    private javax.swing.JTextField classNamePrefix;
    private javax.swing.JLabel classNamePrefixTxt;
    private javax.swing.JTextArea createdFiles;
    private javax.swing.JLabel createdFilesTxt;
    private javax.swing.JTextField displayName;
    private javax.swing.JLabel displayNameTxt;
    private javax.swing.JTextField icon;
    private javax.swing.JButton iconButton;
    private javax.swing.JLabel iconTxt;
    private javax.swing.JTextArea modifiedFiles;
    private javax.swing.JLabel modifiedFilesTxt;
    private javax.swing.JComboBox packageName;
    private javax.swing.JLabel packageNameTxt;
    private javax.swing.JTextField project;
    private javax.swing.JLabel projectTxt;
    // End of variables declaration//GEN-END:variables
    
}
