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
import org.openide.ErrorManager;
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
                
            Dialog dialog = org.openide.DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.show();
            dialog.dispose();
            if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
                // first update the panel's value in case it was edited by EnhancedCustomPropertyEditor
                panel.updateValue();
                
                // get the value
                try {
                    selectedFS = (FileSystem)panel.getModel().getValue();
                } catch (Exception e) {
                    ErrorManager.getDefault().notify(e);
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
                        org.openide.DialogDisplayer.getDefault().notify(descr);
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
        
        fss = Repository.getDefault().getFileSystems();
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
        
        foJUnitTmpl = Repository.getDefault().getDefaultFileSystem().findResource("Templates/JUnit");
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
	cfg.chkAbstractImpl.setSelected(JUnitSettings.getDefault().isGenerateAbstractImpl());
        cfg.chkComments.setSelected(JUnitSettings.getDefault().isBodyComments());
        cfg.chkContent.setSelected(JUnitSettings.getDefault().isBodyContent());
        cfg.chkJavaDoc.setSelected(JUnitSettings.getDefault().isJavaDoc());        
        cfg.chkRegenerateSuite.setSelected(JUnitSettings.getDefault().isRegenerateSuiteMethod());        
        cfg.chkEnabled.setSelected(JUnitSettings.getDefault().isCfgCreateEnabled());
        
        // !-- GENERATE NbJUnit no longer supported
        //cfg.chkNbJUnit.setSelected(JUnitSettings.getDefault().isGenerateNbJUnit());
        // GENERATE NbJUnit no longer supported --!
        
        // display dialog
        DialogDescriptor descriptor = new DialogDescriptor (
            cfg,
            bundle.getString("LBL_JUnitCfgOfCreate_Title")
        );
        descriptor.setHelpCtx(new HelpCtx(JUnitCfgOfCreate.class));
        Dialog dialog = org.openide.DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.show();
        dialog.dispose();
        
        // save panel settings
        if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
            FileSystem  fs;
            FileObject  foTemplate;
            
            // store File System, add it to file systems if neccessary
            if (null != (fs = (FileSystem)((Pair)cfg.cboFileSystem.getSelectedItem()).item)) {
                if (null == Repository.getDefault().findFileSystem(fs.getSystemName()))
                    Repository.getDefault().addFileSystem(fs);
                
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
		JUnitSettings.getDefault().setGenerateAbstractImpl(cfg.chkAbstractImpl.isSelected());
            JUnitSettings.getDefault().setBodyComments(cfg.chkComments.isSelected());
            JUnitSettings.getDefault().setBodyContent(cfg.chkContent.isSelected());
            JUnitSettings.getDefault().setJavaDoc(cfg.chkJavaDoc.isSelected());
            JUnitSettings.getDefault().setCfgCreateEnabled(cfg.chkEnabled.isSelected());
            JUnitSettings.getDefault().setRegenerateSuiteMethod(cfg.chkRegenerateSuite.isSelected());
            
            // !-- GENERATE NbJUnit no longer supported
            // JUnitSettings.getDefault().setGenerateNbJUnit(cfg.chkNbJUnit.isSelected());
            // GENERATE NbJUnit no longer supported --!
            
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
        java.awt.GridBagConstraints gridBagConstraints;

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
        chkAbstractImpl = new javax.swing.JCheckBox();
        chkRegenerateSuite = new javax.swing.JCheckBox();
        chkEnabled = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        setMaximumSize(new java.awt.Dimension(500, 320));
        setMinimumSize(new java.awt.Dimension(500, 320));
        setName("null");
        setPreferredSize(new java.awt.Dimension(500, 320));
        lblFileSystem.setText(bundle.getString("JUnitCfgOfCreate.lblFileSystem.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(lblFileSystem, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 2, 2, 2);
        gridBagConstraints.weightx = 1.0;
        add(cboFileSystem, gridBagConstraints);

        cmdMount.setText(bundle.getString("JUnitCfgOfCreate.cmdMount.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 2);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(cmdMount, gridBagConstraints);

        jpTemplates.setLayout(new java.awt.GridBagLayout());

        jpTemplates.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), java.util.ResourceBundle.getBundle("org/netbeans/modules/junit/Bundle").getString("JUnitCfgOfCreate.jpTemplates.title")));
        lblSuiteClass.setText(bundle.getString("JUnitCfgOfCreate.lblSuiteClass.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 6);
        jpTemplates.add(lblSuiteClass, gridBagConstraints);

        lblTestClass.setText(bundle.getString("JUnitCfgOfCreate.lblTestClass.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 2);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jpTemplates.add(lblTestClass, gridBagConstraints);

        cboSuiteClass.setMinimumSize(new java.awt.Dimension(200, 26));
        cboSuiteClass.setPreferredSize(new java.awt.Dimension(200, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 2, 4, 2);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jpTemplates.add(cboSuiteClass, gridBagConstraints);

        cboTestClass.setMinimumSize(new java.awt.Dimension(200, 26));
        cboTestClass.setPreferredSize(new java.awt.Dimension(200, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(4, 2, 4, 2);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jpTemplates.add(cboTestClass, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        gridBagConstraints.weightx = 1.0;
        add(jpTemplates, gridBagConstraints);

        jpCodeGen.setLayout(new java.awt.GridBagLayout());

        jpCodeGen.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), java.util.ResourceBundle.getBundle("org/netbeans/modules/junit/Bundle").getString("JUnitCfgOfCreate.jpCodeGen.title")));
        chkPublic.setText(bundle.getString("JUnitCfgOfCreate.chkPublic.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jpCodeGen.add(chkPublic, gridBagConstraints);

        chkProtected.setText(bundle.getString("JUnitCfgOfCreate.chkProtected.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jpCodeGen.add(chkProtected, gridBagConstraints);

        chkPackage.setText(bundle.getString("JUnitCfgOfCreate.chkPackage.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jpCodeGen.add(chkPackage, gridBagConstraints);

        chkComments.setText(bundle.getString("JUnitCfgOfCreate.chkComments.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jpCodeGen.add(chkComments, gridBagConstraints);

        chkContent.setText(bundle.getString("JUnitCfgOfCreate.chkContent.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jpCodeGen.add(chkContent, gridBagConstraints);

        chkJavaDoc.setText(bundle.getString("JUnitCfgOfCreate.chkJavaDoc.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jpCodeGen.add(chkJavaDoc, gridBagConstraints);

        chkExceptions.setText(bundle.getString("JUnitCfgOfCreate.chkExceptions.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jpCodeGen.add(chkExceptions, gridBagConstraints);

        chkAbstractImpl.setText(bundle.getString("JUnitCfgOfCreate.chkAbstractImpl.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jpCodeGen.add(chkAbstractImpl, gridBagConstraints);

        chkRegenerateSuite.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/junit/Bundle").getString("JUnitCfgOfCreate.regenerateSuiteMethod.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jpCodeGen.add(chkRegenerateSuite, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jpCodeGen, gridBagConstraints);

        chkEnabled.setText(bundle.getString("JUnitCfgOfCreate.chkEnabled.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 13, 2, 2);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1000.0;
        add(chkEnabled, gridBagConstraints);

    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblSuiteClass;
    private javax.swing.JCheckBox chkContent;
    private javax.swing.JCheckBox chkRegenerateSuite;
    private javax.swing.JCheckBox chkComments;
    private javax.swing.JLabel lblFileSystem;
    private javax.swing.JPanel jpTemplates;
    private javax.swing.JComboBox cboTestClass;
    private javax.swing.JCheckBox chkPublic;
    private javax.swing.JLabel lblTestClass;
    private javax.swing.JCheckBox chkProtected;
    private javax.swing.JCheckBox chkPackage;
    private javax.swing.JCheckBox chkJavaDoc;
    private javax.swing.JComboBox cboFileSystem;
    private javax.swing.JCheckBox chkAbstractImpl;
    private javax.swing.JComboBox cboSuiteClass;
    private javax.swing.JButton cmdMount;
    private javax.swing.JCheckBox chkExceptions;
    private javax.swing.JPanel jpCodeGen;
    private javax.swing.JCheckBox chkEnabled;
    // End of variables declaration//GEN-END:variables
}
