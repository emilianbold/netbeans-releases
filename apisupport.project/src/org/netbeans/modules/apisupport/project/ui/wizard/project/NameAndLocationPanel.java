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

package org.netbeans.modules.apisupport.project.ui.wizard.project;

import java.awt.Color;
import java.io.IOException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.apisupport.project.layers.LayerUtils.LayerHandle;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;



/**
 * the second panel in project template wizard.
 *
 * @author Milos Kleint
 */
final class NameAndLocationPanel extends BasicWizardIterator.Panel {
    private NewProjectIterator.DataModel data;
    FileSystem fs;
    /** Creates new NameAndLocationPanel */
    public NameAndLocationPanel(WizardDescriptor setting, NewProjectIterator.DataModel data) {
        super(setting);
        this.data = data;
        initComponents();
        Color lblBgr = UIManager.getColor("Label.background"); // NOI18N
        putClientProperty("NewFileWizard_Title", getMessage("LBL_ProjectWizardTitle"));
        modifiedFilesValue.setBackground(lblBgr);
        createdFilesValue.setBackground(lblBgr);
        modifiedFilesValue.setEditable(false);
        createdFilesValue.setEditable(false);
        
        DocumentListener dListener = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                checkValidity();
                updateData();
            }
        };
        txtName.getDocument().addDocumentListener(dListener);
        txtDisplayName.getDocument().addDocumentListener(dListener);
        if (comCategory.getEditor().getEditorComponent() instanceof JTextField) {
            JTextField txt = (JTextField)comCategory.getEditor().getEditorComponent();
            txt.getDocument().addDocumentListener(dListener);
        }
        
        if (comPackageName.getEditor().getEditorComponent() instanceof JTextField) {
            JTextField txt = (JTextField)comPackageName.getEditor().getEditorComponent();
            txt.getDocument().addDocumentListener(dListener);
        }
    }
    
    protected void storeToDataModel() {
        updateData();
    }
    
    private void updateData() {
        data.setPackageName(comPackageName.getEditor().getItem().toString());
        data.setName(txtName.getText().trim());
        data.setDisplayName(txtDisplayName.getText().trim());
        Object item = comCategory.getSelectedItem();
        if (item != null && item instanceof CategoryWrapper) {
            data.setCategory(((CategoryWrapper)item).getCategoryPath());
        } else {
            data.setCategory("Templates/Project/Other"); //NOI18N
        }
        NewProjectIterator.generateFileChanges(data);
        CreatedModifiedFiles fls = data.getCreatedModifiedFiles();
        createdFilesValue.setText(generateText(fls.getCreatedPaths()));
        modifiedFilesValue.setText(generateText(fls.getModifiedPaths()));
    }
    
    protected void readFromDataModel() {
        loadCombo();
        checkValidity();
    }
    
    private static JComboBox createComboBox(SourceGroup srcRoot) {
        JComboBox packagesComboBox = new JComboBox(PackageView.createListView(srcRoot));
        packagesComboBox.setRenderer(PackageView.listRenderer());
        return packagesComboBox;
    }
    
    protected String getPanelName() {
        return getMessage("LBL_NameLocation_Title");
    }
    
    private void checkValidity() {
        //TODO:
        setValid(Boolean.TRUE);
    }
    
    private void loadCombo() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        try {
            FileSystem fs = LayerUtils.getEffectiveSystemFilesystem(data.getProject());
            FileObject fo = fs.getRoot().getFileObject("Templates/Project"); //NOI18N
            if (fo != null) {
                addCategoryToModel(model, null, fo);
            }
        } catch (IOException exc) {
            //TODO fallback to hardwired values?
            ErrorManager.getDefault().notify(exc);
        }
        comCategory.setModel(model);
    }
    
    private void addCategoryToModel(DefaultComboBoxModel model, CategoryWrapper parent, FileObject current) {
        FileObject[] fos = current.getChildren();
        for (int i = 0; i < fos.length; i++) {
            if (fos[i].isFolder()) {
                CategoryWrapper wrapper;
                if (parent == null) {
                    wrapper = new CategoryWrapper(fos[i]);
                } else {
                    wrapper = new CategoryWrapper(fos[i], parent);
                }
                model.addElement(wrapper);
                addCategoryToModel(model, wrapper, fos[i]);
            }
        }
    }
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblName = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        lblDisplayName = new javax.swing.JLabel();
        txtDisplayName = new javax.swing.JTextField();
        lblCategory = new javax.swing.JLabel();
        comCategory = new javax.swing.JComboBox();
        lblProjectName = new javax.swing.JLabel();
        txtProjectName = new JTextField(ProjectUtils.getInformation(this.data.getProject()).getDisplayName());
        lblPackageName = new javax.swing.JLabel();
        comPackageName = this.createComboBox(this.data.getSourceRootGroup());
        createdFiles = new javax.swing.JLabel();
        modifiedFiles = new javax.swing.JLabel();
        filler = new javax.swing.JLabel();
        createdFilesValue = new javax.swing.JTextArea();
        modifiedFilesValue = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        lblName.setLabelFor(txtName);
        org.openide.awt.Mnemonics.setLocalizedText(lblName, org.openide.util.NbBundle.getMessage(NameAndLocationPanel.class, "LBL_Name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(lblName, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(txtName, gridBagConstraints);

        lblDisplayName.setLabelFor(txtDisplayName);
        org.openide.awt.Mnemonics.setLocalizedText(lblDisplayName, org.openide.util.NbBundle.getMessage(NameAndLocationPanel.class, "LBL_DisplayName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(lblDisplayName, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(txtDisplayName, gridBagConstraints);

        lblCategory.setLabelFor(comCategory);
        org.openide.awt.Mnemonics.setLocalizedText(lblCategory, org.openide.util.NbBundle.getMessage(NameAndLocationPanel.class, "LBL_Category"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(lblCategory, gridBagConstraints);

        comCategory.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(comCategory, gridBagConstraints);

        lblProjectName.setLabelFor(txtProjectName);
        org.openide.awt.Mnemonics.setLocalizedText(lblProjectName, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/librarydescriptor/Bundle").getString("LBL_ProjectName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 6, 12);
        add(lblProjectName, gridBagConstraints);

        txtProjectName.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 6, 0);
        add(txtProjectName, gridBagConstraints);

        lblPackageName.setLabelFor(comPackageName);
        org.openide.awt.Mnemonics.setLocalizedText(lblPackageName, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/librarydescriptor/Bundle").getString("LBL_PackageName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(lblPackageName, gridBagConstraints);

        comPackageName.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(comPackageName, gridBagConstraints);

        createdFiles.setLabelFor(createdFilesValue);
        org.openide.awt.Mnemonics.setLocalizedText(createdFiles, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/librarydescriptor/Bundle").getString("LBL_CreatedFiles"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(36, 0, 6, 12);
        add(createdFiles, gridBagConstraints);

        modifiedFiles.setLabelFor(modifiedFilesValue);
        org.openide.awt.Mnemonics.setLocalizedText(modifiedFiles, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/librarydescriptor/Bundle").getString("LBL_ModifiedFiles"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(modifiedFiles, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(filler, gridBagConstraints);

        createdFilesValue.setColumns(20);
        createdFilesValue.setRows(5);
        createdFilesValue.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(36, 0, 6, 0);
        add(createdFilesValue, gridBagConstraints);

        modifiedFilesValue.setColumns(20);
        modifiedFilesValue.setRows(5);
        modifiedFilesValue.setToolTipText("modifiedFilesValue");
        modifiedFilesValue.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(modifiedFilesValue, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox comCategory;
    private javax.swing.JComboBox comPackageName;
    private javax.swing.JLabel createdFiles;
    private javax.swing.JTextArea createdFilesValue;
    private javax.swing.JLabel filler;
    private javax.swing.JLabel lblCategory;
    private javax.swing.JLabel lblDisplayName;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblPackageName;
    private javax.swing.JLabel lblProjectName;
    private javax.swing.JLabel modifiedFiles;
    private javax.swing.JTextArea modifiedFilesValue;
    private javax.swing.JTextField txtDisplayName;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtProjectName;
    // End of variables declaration//GEN-END:variables
    
    private static String generateText(String[] relPaths) {
        StringBuffer sb = new StringBuffer();
        if (relPaths.length > 0) {
            for (int i = 0; i < relPaths.length; i++) {
                if (i > 0) {
                    sb.append("\n");//NOI18N
                }
                sb.append(relPaths[i]);
            }
        }
        
        return sb.toString();
    }
    
    private class CategoryWrapper {
        private FileObject fo;
        private String path;
        private CategoryWrapper parent;
        
        public CategoryWrapper(FileObject fil) {
            fo = fil;
        }
        
        public CategoryWrapper(FileObject fil, CategoryWrapper parent) {
            this(fil);
            this.parent = parent;
        }
        
        public String getCategoryPath() {
            return fo.getPath();
        }
        
        public String toString() {
            String toRet = "";
            if (parent != null) {
                toRet = parent.toString() + " | ";
            }
            if (path == null) {
                Object str = fo.getAttribute("SystemFileSystem.localizingBundle");
                if (str != null) {
                    //TODO
                    toRet = toRet + fo.getNameExt();
                } else {
                    toRet = toRet + fo.getNameExt();
                }
            }
            return toRet;
        }
    }
    
    
}
