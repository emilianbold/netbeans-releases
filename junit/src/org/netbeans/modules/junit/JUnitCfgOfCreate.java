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

package org.netbeans.modules.junit;

import java.awt.Dialog;
import java.util.ResourceBundle;
import javax.swing.JPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 *
 * @author  vstejskal
 */
public class JUnitCfgOfCreate extends JPanel {

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
    
    /** Creates new form JUnitCfgOfCreate */
    private JUnitCfgOfCreate() {
        // init components
        initComponents();
        // add mnemonics (this has to be here, because of I18N and inability of the form editor
        addMnemonicsAndAD();
        
//        cmdMount.addActionListener(new JUnitCfgOfCreate.CmdMountListener());
    }
    
    
    private static char getMnemonics(ResourceBundle rb, String key) {
        return rb.getString(key).charAt(0);
    }
    
    private static int getMnemonicsIndex(ResourceBundle rb, String key) {
        String mnemonicsValue = rb.getString(key);
        try {            
            return Integer.parseInt(mnemonicsValue);
        } catch (NumberFormatException nfe) {
            // oops, there was a problem -> error message
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, nfe);
            // and return no mnemonics
            return -1;
        }
    }
        
    private void addMnemonicsAndAD() {
        
        // window
        this.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.AD"));
        
        // check boxes
        this.chkPublic.setMnemonic(getMnemonics(bundle,"JUnitCfgOfCreate.chkPublic.mne"));
        this.chkPublic.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkPublic.toolTip"));
        this.chkPublic.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkPublic.AD"));        
        
        this.chkProtected.setMnemonic(getMnemonics(bundle,"JUnitCfgOfCreate.chkProtected.mne"));
        this.chkProtected.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkProtected.toolTip"));
        this.chkProtected.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkProtected.AD"));        
        
        this.chkPackage.setMnemonic(getMnemonics(bundle,"JUnitCfgOfCreate.chkPackage.mne"));
        this.chkPackage.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkPackage.toolTip"));
        this.chkPackage.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkPackage.AD"));
        
        this.chkComments.setMnemonic(getMnemonics(bundle,"JUnitCfgOfCreate.chkComments.mne"));
        this.chkComments.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkComments.toolTip"));        
        this.chkComments.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkComments.AD"));
        
        this.chkContent.setMnemonic(getMnemonics(bundle,"JUnitCfgOfCreate.chkContent.mne"));
        this.chkContent.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkContent.toolTip"));
        this.chkContent.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkContent.AD"));
        
        this.chkJavaDoc.setMnemonic(getMnemonics(bundle,"JUnitCfgOfCreate.chkJavaDoc.mne"));
        this.chkJavaDoc.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkJavaDoc.toolTip"));
        this.chkJavaDoc.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkJavaDoc.AD"));
        
        
        this.chkExceptions.setMnemonic(getMnemonics(bundle,"JUnitCfgOfCreate.chkExceptions.mne"));
        this.chkExceptions.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkExceptions.toolTip"));
        this.chkExceptions.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkExceptions.AD"));
        
        this.chkAbstractImpl.setMnemonic(getMnemonics(bundle,"JUnitCfgOfCreate.chkAbstractImpl.mne"));
        this.chkAbstractImpl.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkAbstractImpl.toolTip"));
        this.chkAbstractImpl.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkAbstractImpl.AD"));
        
        this.chkPackagePrivateClasses.setMnemonic(getMnemonics(bundle,"JUnitCfgOfCreate.chkPackagePrivateClasses.mne"));
        this.chkPackagePrivateClasses.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkPackagePrivateClasses.toolTip"));
        this.chkPackagePrivateClasses.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkPackagePrivateClasses.AD"));
        
        this.chkGenerateSuites.setMnemonic(getMnemonics(bundle,"JUnitCfgOfCreate.chkGenerateSuites.mne"));
        this.chkGenerateSuites.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkGenerateSuites.toolTip"));
        this.chkGenerateSuites.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkGenerateSuites.AD"));
        
        this.chkEnabled.setMnemonic(getMnemonics(bundle,"JUnitCfgOfCreate.chkEnabled.mne"));
        this.chkEnabled.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkEnabled.toolTip"));
        this.chkEnabled.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkEnabled.AD"));
        
        // buttons
//       this.cmdMount.setMnemonic(getMnemonics(bundle,"JUnitCfgOfCreate.cmdMount.mne"));
        
        // labels
        
        this.lblSuiteClass.setDisplayedMnemonic(getMnemonics(bundle,"JUnitCfgOfCreate.lblSuiteClass.mne"));
        this.lblSuiteClass.setLabelFor(cboSuiteClass);
        this.cboSuiteClass.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.cboSuiteClass.AD"));
        this.cboSuiteClass.getAccessibleContext().setAccessibleName(bundle.getString("JUnitCfgOfCreate.cboSuiteClass.AN"));
        
        this.lblTestClass.setDisplayedMnemonic(getMnemonics(bundle,"JUnitCfgOfCreate.lblTestClass.mne"));
        this.lblTestClass.setLabelFor(cboTestClass);
        this.cboTestClass.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.cboTestClass.AD"));
        this.cboTestClass.getAccessibleContext().setAccessibleName(bundle.getString("JUnitCfgOfCreate.cboTestClass.AN"));
        
    }
    
    //public static final ResourceBundle bundle = ResourceBundle.getBundle("org.netbeans.modules.junit.Bundle");
    public static final ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.junit.Bundle");
    
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
            if (foTemplates[i].getPath().equals(JUnitSettings.getDefault().getSuiteTemplate()))
                cboSuiteClass.setSelectedItem(item);
    
            // add template to Class templates list
            cboTestClass.addItem(item);
            if (foTemplates[i].getPath().equals(JUnitSettings.getDefault().getClassTemplate()))
                cboTestClass.setSelectedItem(item);
        }
    }
    
    /** Displays dialog and updates JUnit options according to the user's settings. */
    // not actually the cleanes implementation with the argument, but it
    // will work for the time being ...
    public static boolean configure() {
        // check if the dialog can be displayed
        if (!JUnitSettings.getDefault().isCfgCreateEnabled())
            return true;
        
        // create panel
        JUnitCfgOfCreate cfg = new JUnitCfgOfCreate();
        
        // setup the panel
        cfg.fillTemplates();
        cfg.chkPublic.setSelected(JUnitSettings.getDefault().isMembersPublic());
        cfg.chkProtected.setSelected(JUnitSettings.getDefault().isMembersProtected());
        cfg.chkPackage.setSelected(JUnitSettings.getDefault().isMembersPackage());
        cfg.chkExceptions.setSelected(JUnitSettings.getDefault().isGenerateExceptionClasses());
	cfg.chkAbstractImpl.setSelected(JUnitSettings.getDefault().isGenerateAbstractImpl());
        cfg.chkComments.setSelected(JUnitSettings.getDefault().isBodyComments());
        cfg.chkContent.setSelected(JUnitSettings.getDefault().isBodyContent());
        cfg.chkJavaDoc.setSelected(JUnitSettings.getDefault().isJavaDoc());        
        cfg.chkGenerateSuites.setSelected(JUnitSettings.getDefault().isGenerateSuiteClasses());        
        cfg.chkEnabled.setSelected(JUnitSettings.getDefault().isCfgCreateEnabled());
        cfg.chkPackagePrivateClasses.setSelected(JUnitSettings.getDefault().isIncludePackagePrivateClasses());
        
        // display dialog
        DialogDescriptor descriptor = new DialogDescriptor (
            cfg,
            bundle.getString("JUnitCfgOfCreate.Title")
        );
        descriptor.setHelpCtx(new HelpCtx(JUnitCfgOfCreate.class));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.show();
        dialog.dispose();
        
        // save panel settings
        if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
            FileObject  foTemplate;
            
            // store Suite class template
            foTemplate = (FileObject)((Pair)cfg.cboSuiteClass.getSelectedItem()).item;
            JUnitSettings.getDefault().setSuiteTemplate(foTemplate.getPath());
            
            // store Test class template
            foTemplate = (FileObject)((Pair)cfg.cboTestClass.getSelectedItem()).item;
            JUnitSettings.getDefault().setClassTemplate(foTemplate.getPath());
            
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
            JUnitSettings.getDefault().setGenerateSuiteClasses(cfg.chkGenerateSuites.isSelected());
            JUnitSettings.getDefault().setIncludePackagePrivateClasses(cfg.chkPackagePrivateClasses.isSelected());
            
            return true;
        }
        
        return false;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

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
        chkGenerateSuites = new javax.swing.JCheckBox();
        chkPackagePrivateClasses = new javax.swing.JCheckBox();
        chkEnabled = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        setMinimumSize(new java.awt.Dimension(500, 320));
        jpTemplates.setLayout(new java.awt.GridBagLayout());

        jpTemplates.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), bundle.getString("JUnitCfgOfCreate.jpTemplates.title")));
        lblSuiteClass.setText(bundle.getString("JUnitCfgOfCreate.lblSuiteClass.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 6);
        jpTemplates.add(lblSuiteClass, gridBagConstraints);

        lblTestClass.setText(bundle.getString("JUnitCfgOfCreate.lblTestClass.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 2);
        jpTemplates.add(lblTestClass, gridBagConstraints);

        cboSuiteClass.setMinimumSize(new java.awt.Dimension(200, 26));
        cboSuiteClass.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 2, 4, 2);
        jpTemplates.add(cboSuiteClass, gridBagConstraints);

        cboTestClass.setMinimumSize(new java.awt.Dimension(200, 26));
        cboTestClass.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 2, 4, 2);
        jpTemplates.add(cboTestClass, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jpTemplates, gridBagConstraints);

        jpCodeGen.setLayout(new java.awt.GridBagLayout());

        jpCodeGen.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), bundle.getString("JUnitCfgOfCreate.jpCodeGen.title")));
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
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jpCodeGen.add(chkComments, gridBagConstraints);

        chkContent.setText(bundle.getString("JUnitCfgOfCreate.chkContent.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jpCodeGen.add(chkContent, gridBagConstraints);

        chkJavaDoc.setText(bundle.getString("JUnitCfgOfCreate.chkJavaDoc.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jpCodeGen.add(chkJavaDoc, gridBagConstraints);

        chkExceptions.setText(bundle.getString("JUnitCfgOfCreate.chkExceptions.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jpCodeGen.add(chkExceptions, gridBagConstraints);

        chkAbstractImpl.setText(bundle.getString("JUnitCfgOfCreate.chkAbstractImpl.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jpCodeGen.add(chkAbstractImpl, gridBagConstraints);

        chkGenerateSuites.setText(bundle.getString("JUnitCfgOfCreate.chkGenerateSuites.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jpCodeGen.add(chkGenerateSuites, gridBagConstraints);

        chkPackagePrivateClasses.setText(bundle.getString("JUnitCfgOfCreate.chkPackagePrivateClasses.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jpCodeGen.add(chkPackagePrivateClasses, gridBagConstraints);

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
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1000.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 13, 2, 2);
        add(chkEnabled, gridBagConstraints);

    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cboSuiteClass;
    private javax.swing.JComboBox cboTestClass;
    private javax.swing.JCheckBox chkAbstractImpl;
    private javax.swing.JCheckBox chkComments;
    private javax.swing.JCheckBox chkContent;
    private javax.swing.JCheckBox chkEnabled;
    private javax.swing.JCheckBox chkExceptions;
    private javax.swing.JCheckBox chkGenerateSuites;
    private javax.swing.JCheckBox chkJavaDoc;
    private javax.swing.JCheckBox chkPackage;
    private javax.swing.JCheckBox chkPackagePrivateClasses;
    private javax.swing.JCheckBox chkProtected;
    private javax.swing.JCheckBox chkPublic;
    private javax.swing.JPanel jpCodeGen;
    private javax.swing.JPanel jpTemplates;
    private javax.swing.JLabel lblSuiteClass;
    private javax.swing.JLabel lblTestClass;
    // End of variables declaration//GEN-END:variables
}
