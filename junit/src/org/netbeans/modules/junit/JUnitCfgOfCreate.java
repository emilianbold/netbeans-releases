/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * JUnitCfgOfCreate.java
 *
 * Created on January 30, 2001, 10:11 AM
 */

package org.netbeans.modules.junit;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import org.openide.*;
import org.openide.TopManager;
import org.openide.filesystems.*;
import org.openide.explorer.propertysheet.*;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;

/**
 *
 * @author  vstejskal
 * @version 1.0
 */
public class JUnitCfgOfCreate extends javax.swing.JPanel {

    private class Pair {
        public String  name;
        public Object  item;
        public Pair(String name, Object item) {
            this.name = name;
            this.item = item;
        }
        public String toString() {
            return name.toString();
        }
    }
    
    private class CmdMountListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            PropertyPanel       panel;
            DialogDescriptor    descriptor;
            FileSystem          selectedFS;
            
            panel = new PropertyPanel(JUnitCfgOfCreate.this, "newFileSystem", PropertyPanel.PREF_CUSTOM_EDITOR); // NOI18N
            descriptor = new DialogDescriptor(panel, bundle.getString("LBL_New_FS_Dialog_Title"));
                
            Dialog dialog = TopManager.getDefault().createDialog(descriptor);
            dialog.show();
            dialog.dispose();
            if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
                // first update the panel's value in case it was edited by EnhancedCustomPropertyEditor
                panel.updateValue();
                
                // get the value
                try {
                    selectedFS = (FileSystem)panel.getModel().getValue();
                } catch (Exception e) {
                    TopManager.getDefault().notifyException(e);
                    return;
                }
                
                // try to select returned new File system
                int i, iCnt;
                Pair item;
                FileSystem fs;
                
                iCnt = cboFileSystem.getItemCount();
                for(i = 0; i < iCnt; i++) {
                    item = (Pair)cboFileSystem.getItemAt(i);
                    fs = (FileSystem) item.item;
                    if (null != fs && fs.getSystemName().equals(selectedFS.getSystemName())) {
                        cboFileSystem.setSelectedItem(item);
                        break;
                    }
                }

                if (i == iCnt) {
                    // selected FS was not found - check if it is accaptable for tests
                    if (!TestUtil.isSupportedFileSystem(selectedFS)) {
                        String msg = NbBundle.getMessage(JUnitCfgOfCreate.class, "MSG_fs_not_acceptable");
                        NotifyDescriptor descr = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                        TopManager.getDefault().notify(descr);
                        return;
                    }
                    
                    // new mounted one - add it
                    item = new Pair(selectedFS.getDisplayName(), selectedFS);
                    cboFileSystem.addItem(item);
                    cboFileSystem.setSelectedItem(item);
                }
            }
        }
    }
        
    /** Creates new form JUnitCfgOfCreate */
    private JUnitCfgOfCreate() {
        initComponents();
        cmdMount.addActionListener(new JUnitCfgOfCreate.CmdMountListener());
    }
    
    public static final ResourceBundle bundle = ResourceBundle.getBundle("org.netbeans.modules.junit.Bundle");
    private FileSystem fileSystem;
    
    private void fillFileSystems() {
        Pair        item;
        Enumeration fss;
        
        // insert the default value
        item = new Pair(NbBundle.getMessage(JUnitCfgOfCreate.class, "LBL_no_file_system_selected"), null);
        cboFileSystem.addItem(item);
        cboFileSystem.setSelectedItem(item);
        
        fss = TopManager.getDefault().getRepository().getFileSystems();
        while (fss.hasMoreElements()) {
            FileSystem fs = (FileSystem) fss.nextElement();
            if (TestUtil.isSupportedFileSystem(fs)) {
                item = new Pair(fs.getDisplayName(), fs);
                cboFileSystem.addItem(item);
                if (fs.getSystemName().equals(JUnitSettings.getDefault().getFileSystem())) //replace('\\', '/')
                    cboFileSystem.setSelectedItem(item);
            }
        }
    }
    
    private void fillTemplates() {
        Pair        item;
        FileObject  foJUnitTmpl;
        FileObject  foTemplates[];
        
        foJUnitTmpl = TopManager.getDefault().getRepository().getDefaultFileSystem().findResource("Templates/JUnit");
        if (null == foJUnitTmpl) return;
        
        foTemplates = foJUnitTmpl.getChildren();
        for(int i = 0; i < foTemplates.length; i++) {
            if (!foTemplates[i].getExt().equals("java"))
                continue;
            
            item = new Pair(foTemplates[i].getName(), foTemplates[i]);
            // add template to Suite templates list
            cboSuiteClass.addItem(item);
            if (foTemplates[i].getPackageNameExt('/', '.').equals(JUnitSettings.getDefault().getSuiteTemplate()))
                cboSuiteClass.setSelectedItem(item);
    
            // add template to Class templates list
            cboTestClass.addItem(item);
            if (foTemplates[i].getPackageNameExt('/', '.').equals(JUnitSettings.getDefault().getClassTemplate()))
                cboTestClass.setSelectedItem(item);
        }
    }
    
    /** Displays dialog and updates JUnit options according to the user's settings. */
    public static boolean configure() {
        // check if the dialog can be displayed
        if (!JUnitSettings.getDefault().isCfgCreateEnabled())
            return true;
        
        // create panel
        JUnitCfgOfCreate cfg = new JUnitCfgOfCreate();
        
        // setup the panel
        cfg.fillFileSystems();
        cfg.fillTemplates();
        cfg.chkPublic.setSelected(JUnitSettings.getDefault().isMembersPublic());
        cfg.chkProtected.setSelected(JUnitSettings.getDefault().isMembersProtected());
        cfg.chkPackage.setSelected(JUnitSettings.getDefault().isMembersPackage());
        cfg.chkExceptions.setSelected(JUnitSettings.getDefault().isGenerateExceptionClasses());
        cfg.chkComments.setSelected(JUnitSettings.getDefault().isBodyComments());
        cfg.chkContent.setSelected(JUnitSettings.getDefault().isBodyContent());
        cfg.chkJavaDoc.setSelected(JUnitSettings.getDefault().isJavaDoc());
        
        cfg.chkEnabled.setSelected(JUnitSettings.getDefault().isCfgCreateEnabled());
        
        // display dialog
        DialogDescriptor descriptor = new DialogDescriptor (
            cfg,
            bundle.getString("LBL_JUnitCfgOfCreate_Title")
        );
        descriptor.setHelpCtx(new HelpCtx(JUnitCfgOfCreate.class));
        Dialog dialog = TopManager.getDefault().createDialog(descriptor);
        dialog.show();
        dialog.dispose();
        
        // save panel settings
        if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
            FileSystem  fs;
            FileObject  foTemplate;
            
            // store File System, add it to file systems if neccessary
            if (null != (fs = (FileSystem)((Pair)cfg.cboFileSystem.getSelectedItem()).item)) {
                if (null == TopManager.getDefault().getRepository().findFileSystem(fs.getSystemName()))
                    TopManager.getDefault().getRepository().addFileSystem(fs);
                
                JUnitSettings.getDefault().setFileSystem(fs.getSystemName());
            }
            else
                JUnitSettings.getDefault().setFileSystem("");
            
            // store Suite class template
            foTemplate = (FileObject)((Pair)cfg.cboSuiteClass.getSelectedItem()).item;
            JUnitSettings.getDefault().setSuiteTemplate(foTemplate.getPackageNameExt('/', '.'));
            
            // store Test class template
            foTemplate = (FileObject)((Pair)cfg.cboTestClass.getSelectedItem()).item;
            JUnitSettings.getDefault().setClassTemplate(foTemplate.getPackageNameExt('/', '.'));
            
            // store code generation options
            JUnitSettings.getDefault().setMembersPublic(cfg.chkPublic.isSelected());
            JUnitSettings.getDefault().setMembersProtected(cfg.chkProtected.isSelected());
            JUnitSettings.getDefault().setMembersPackage(cfg.chkPackage.isSelected());
            JUnitSettings.getDefault().setGenerateExceptionClasses(cfg.chkExceptions.isSelected());
            JUnitSettings.getDefault().setBodyComments(cfg.chkComments.isSelected());
            JUnitSettings.getDefault().setBodyContent(cfg.chkContent.isSelected());
            JUnitSettings.getDefault().setJavaDoc(cfg.chkJavaDoc.isSelected());
            JUnitSettings.getDefault().setCfgCreateEnabled(cfg.chkEnabled.isSelected());
            return true;
        }
        
        return false;
    }
    
    /**
     * Setter for fileSystem - used by PropertyPanel
     */
    public void setNewFileSystem(FileSystem f) {
        firePropertyChange("newFileSystem", fileSystem, f);
        fileSystem = f;
    }
    /**
     * Getter for fileSystem - used by PropertyPanel
     * @return new fileSystem
     */
    public FileSystem getNewFileSystem() {
        return fileSystem;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        lblFileSystem = new javax.swing.JLabel();
        cboFileSystem = new javax.swing.JComboBox();
        cmdMount = new javax.swing.JButton();
        jpTemplates = new javax.swing.JPanel();
        lblSuiteClass = new javax.swing.JLabel();
        lblTestClass = new javax.swing.JLabel();
        cboSuiteClass = new javax.swing.JComboBox();
        cboTestClass = new javax.swing.JComboBox();
        jpCodeGen = new javax.swing.JPanel();
        chkPublic = new javax.swing.JCheckBox();
        chkProtected = new javax.swing.JCheckBox();
        chkPackage = new javax.swing.JCheckBox();
        chkComments = new javax.swing.JCheckBox();
        chkContent = new javax.swing.JCheckBox();
        chkJavaDoc = new javax.swing.JCheckBox();
        chkExceptions = new javax.swing.JCheckBox();
        chkEnabled = new javax.swing.JCheckBox();
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        setPreferredSize(new java.awt.Dimension(500, 300));
        setMinimumSize(new java.awt.Dimension(500, 300));
        setMaximumSize(new java.awt.Dimension(500, 300));
        
        lblFileSystem.setText(bundle.getString("JUnitCfgOfCreate.lblFileSystem.text"));
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(0, 2, 0, 2);
        add(lblFileSystem, gridBagConstraints1);
        
        
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(2, 2, 2, 2);
        gridBagConstraints1.weightx = 1.0;
        add(cboFileSystem, gridBagConstraints1);
        
        
        cmdMount.setText(bundle.getString("JUnitCfgOfCreate.cmdMount.text"));
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(0, 2, 0, 2);
        add(cmdMount, gridBagConstraints1);
        
        
        jpTemplates.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints2;
        jpTemplates.setBorder(new javax.swing.border.TitledBorder("Templates"));
        
        lblSuiteClass.setText(bundle.getString("JUnitCfgOfCreate.lblSuiteClass.text"));
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.insets = new java.awt.Insets(0, 2, 0, 2);
        jpTemplates.add(lblSuiteClass, gridBagConstraints2);
        
        
        lblTestClass.setText(bundle.getString("JUnitCfgOfCreate.lblTestClass.text"));
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 1;
        gridBagConstraints2.insets = new java.awt.Insets(0, 2, 0, 2);
        jpTemplates.add(lblTestClass, gridBagConstraints2);
        
        
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets(4, 2, 4, 2);
        gridBagConstraints2.weightx = 1.0;
        jpTemplates.add(cboSuiteClass, gridBagConstraints2);
        
        
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridx = 1;
        gridBagConstraints2.gridy = 1;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets(4, 2, 4, 2);
        gridBagConstraints2.weightx = 1.0;
        jpTemplates.add(cboTestClass, gridBagConstraints2);
        
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.gridwidth = 3;
        gridBagConstraints1.gridheight = 2;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(2, 2, 2, 2);
        gridBagConstraints1.weightx = 1.0;
        add(jpTemplates, gridBagConstraints1);
        
        
        jpCodeGen.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints3;
        jpCodeGen.setBorder(new javax.swing.border.TitledBorder("Code generation"));
        
        chkPublic.setText(bundle.getString("JUnitCfgOfCreate.chkPublic.text"));
        gridBagConstraints3 = new java.awt.GridBagConstraints();
        gridBagConstraints3.insets = new java.awt.Insets(0, 4, 0, 4);
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints3.weightx = 0.5;
        jpCodeGen.add(chkPublic, gridBagConstraints3);
        
        
        chkProtected.setText(bundle.getString("JUnitCfgOfCreate.chkProtected.text"));
        gridBagConstraints3 = new java.awt.GridBagConstraints();
        gridBagConstraints3.gridx = 0;
        gridBagConstraints3.gridy = 1;
        gridBagConstraints3.insets = new java.awt.Insets(0, 4, 0, 4);
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
        jpCodeGen.add(chkProtected, gridBagConstraints3);
        
        
        chkPackage.setText(bundle.getString("JUnitCfgOfCreate.chkPackage.text"));
        gridBagConstraints3 = new java.awt.GridBagConstraints();
        gridBagConstraints3.gridx = 0;
        gridBagConstraints3.gridy = 2;
        gridBagConstraints3.insets = new java.awt.Insets(0, 4, 0, 4);
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
        jpCodeGen.add(chkPackage, gridBagConstraints3);
        
        
        chkComments.setText(bundle.getString("JUnitCfgOfCreate.chkComments.text"));
        gridBagConstraints3 = new java.awt.GridBagConstraints();
        gridBagConstraints3.insets = new java.awt.Insets(0, 4, 0, 4);
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints3.weightx = 0.5;
        jpCodeGen.add(chkComments, gridBagConstraints3);
        
        
        chkContent.setText(bundle.getString("JUnitCfgOfCreate.chkContent.text"));
        gridBagConstraints3 = new java.awt.GridBagConstraints();
        gridBagConstraints3.gridx = 1;
        gridBagConstraints3.gridy = 1;
        gridBagConstraints3.insets = new java.awt.Insets(0, 4, 0, 4);
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
        jpCodeGen.add(chkContent, gridBagConstraints3);
        
        
        chkJavaDoc.setText(bundle.getString("JUnitCfgOfCreate.chkJavaDoc.text"));
        gridBagConstraints3 = new java.awt.GridBagConstraints();
        gridBagConstraints3.gridx = 1;
        gridBagConstraints3.gridy = 2;
        gridBagConstraints3.insets = new java.awt.Insets(0, 4, 0, 4);
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
        jpCodeGen.add(chkJavaDoc, gridBagConstraints3);
        
        
        chkExceptions.setText(bundle.getString("JUnitCfgOfCreate.chkExceptions.text"));
        gridBagConstraints3 = new java.awt.GridBagConstraints();
        gridBagConstraints3.gridx = 0;
        gridBagConstraints3.gridy = 3;
        gridBagConstraints3.insets = new java.awt.Insets(0, 4, 0, 4);
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
        jpCodeGen.add(chkExceptions, gridBagConstraints3);
        
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.gridwidth = 3;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(2, 2, 2, 2);
        gridBagConstraints1.weightx = 1.0;
        add(jpCodeGen, gridBagConstraints1);
        
        
        chkEnabled.setText(bundle.getString("JUnitCfgOfCreate.chkEnabled.text"));
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 4;
        gridBagConstraints1.gridwidth = 3;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(2, 13, 2, 2);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(chkEnabled, gridBagConstraints1);
        
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblFileSystem;
    private javax.swing.JComboBox cboFileSystem;
    private javax.swing.JButton cmdMount;
    private javax.swing.JPanel jpTemplates;
    private javax.swing.JLabel lblSuiteClass;
    private javax.swing.JLabel lblTestClass;
    private javax.swing.JComboBox cboSuiteClass;
    private javax.swing.JComboBox cboTestClass;
    private javax.swing.JPanel jpCodeGen;
    private javax.swing.JCheckBox chkPublic;
    private javax.swing.JCheckBox chkProtected;
    private javax.swing.JCheckBox chkPackage;
    private javax.swing.JCheckBox chkComments;
    private javax.swing.JCheckBox chkContent;
    private javax.swing.JCheckBox chkJavaDoc;
    private javax.swing.JCheckBox chkExceptions;
    private javax.swing.JCheckBox chkEnabled;
    // End of variables declaration//GEN-END:variables
}
