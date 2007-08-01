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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.mobility.jsr172.wizard;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.awt.Component;
import java.awt.Dialog;
import java.io.BufferedOutputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;
import javax.swing.JPanel;
import javax.swing.JFileChooser;
import javax.swing.JRadioButton;
import javax.swing.filechooser.FileFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.e2e.api.wsdl.wsdl2java.WSDL2JavaFactory;
import org.netbeans.modules.mobility.jsr172.generator.Jsr172Generator;
import org.netbeans.modules.mobility.end2end.util.Util;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.e2e.api.schema.SchemaException;
import org.netbeans.modules.e2e.api.wsdl.wsdl2java.WSDL2Java;
import org.netbeans.modules.e2e.wsdl.wsdl2java.WSDL2JavaImpl;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDescriptor;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;


/**
 *
 * @author Peter Williams
 */
public final class ClientInfo extends JPanel implements WsdlRetriever.MessageReceiver {
    
    private static final String PROP_ERROR_MESSAGE = "WizardPanel_errorMessage"; // NOI18N
    
    private static final int WSDL_FROM_FILE = 1;
    private static final int WSDL_FROM_SERVICE = 2;
    
    private static final FileFilter WSDL_FILE_FILTER = new WsdlFileFilter();
    private static String previousDirectory = "";
    
    final protected WebServiceClientWizardDescriptor descriptorPanel;
    
    protected boolean settingFields;
    private int wsdlSource;
    private FileObject root;
    
    // properties for 'get from server'
    private WsdlRetriever retriever;
    private String downloadMsg;
    
    private RequestProcessor rp;
    
    private String wsiErrorMsg;
    private boolean wsdlValidated = false;
    protected boolean wsdlValid = false;
    protected boolean validating = false;
    
    private RequestProcessor validatingRP;
    
    public ClientInfo(WebServiceClientWizardDescriptor panel) {
        descriptorPanel = panel;
        
        this.settingFields = false;
        this.wsdlSource = WSDL_FROM_SERVICE;
        this.retriever = null;
        
        initComponents();
        initUserComponents();
        
        initAccessibility();
    }
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName( NbBundle.getMessage( ClientInfo.class, "ACSN_Web_Service_Client_Wizard" ));
        getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( ClientInfo.class, "ACSD_Web_Service_Client_Wizard" ));
        
        jRbnServiceURL.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( ClientInfo.class, "ACSD_WsdlSourceUrl" ));
        jRbnFilesystem.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( ClientInfo.class, "ACSD_WsdlSourceFilesystem" ));
        
        jTxtProject.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( ClientInfo.class, "ACSD_Project" ));
        jTxtLocalFilename.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( ClientInfo.class, "ACSD_LocalFilename" ));
        jTxtCreatedFile.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( ClientInfo.class, "ACSD_CreatedFile" ));
        
        jBtnBrowse.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( ClientInfo.class, "ACSD_Browse" ));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        btnGrpWsdlSource = new javax.swing.ButtonGroup();
        jLblChooseSource = new javax.swing.JLabel();
        jRbnServiceURL = new javax.swing.JRadioButton();
        jLblWsdlURL = new javax.swing.JLabel();
        jTxtWsdlURL = new javax.swing.JTextField();
        jBtnGetWsdl = new javax.swing.JButton();
        jLblLocalFNDescription = new javax.swing.JLabel();
        jBtnProxy = new javax.swing.JButton();
        jLblLocalFilename = new javax.swing.JLabel();
        jTxtLocalFilename = new javax.swing.JTextField();
        jLblDummy = new javax.swing.JLabel();
        jRbnFilesystem = new javax.swing.JRadioButton();
        jLblWsdlFile = new javax.swing.JLabel();
        jTxtWsdlFile = new javax.swing.JTextField();
        jBtnBrowse = new javax.swing.JButton();
        jSeparator = new javax.swing.JSeparator();
        jLblClientName = new javax.swing.JLabel();
        jTxtClientName = new javax.swing.JTextField();
        jLblProject = new javax.swing.JLabel();
        jTxtProject = new javax.swing.JTextField();
        jLblPackageName = new javax.swing.JLabel();
        jCbxPackageName = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jTxtCreatedFile = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jCheckGenerateDataBinding = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLblChooseSource, NbBundle.getMessage(ClientInfo.class, "LBL_WsdlSource")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 6);
        add(jLblChooseSource, gridBagConstraints);

        btnGrpWsdlSource.add(jRbnServiceURL);
        org.openide.awt.Mnemonics.setLocalizedText(jRbnServiceURL, NbBundle.getMessage(ClientInfo.class, "LBL_WsdlSourceUrl")); // NOI18N
        jRbnServiceURL.setFocusable(false);
        jRbnServiceURL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRbnServiceURLActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jRbnServiceURL, gridBagConstraints);

        jLblWsdlURL.setLabelFor(jTxtWsdlURL);
        org.openide.awt.Mnemonics.setLocalizedText(jLblWsdlURL, org.openide.util.NbBundle.getMessage(ClientInfo.class, "LBL_WsdlUrl")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 22, 6, 6);
        add(jLblWsdlURL, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(jTxtWsdlURL, gridBagConstraints);
        jTxtWsdlURL.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ClientInfo.class, "ACSN_WsdlSourceUrl")); // NOI18N
        jTxtWsdlURL.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientInfo.class, "ACSD_WsdlSourceUrl")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jBtnGetWsdl, org.openide.util.NbBundle.getMessage(ClientInfo.class, "LBL_RetrieveWsdl")); // NOI18N
        jBtnGetWsdl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnGetWsdlActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(jBtnGetWsdl, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLblLocalFNDescription, org.openide.util.NbBundle.getMessage(ClientInfo.class, "LBL_LocalFNDescription")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 22, 6, 6);
        add(jLblLocalFNDescription, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jBtnProxy, org.openide.util.NbBundle.getMessage(ClientInfo.class, "LBL_ProxySettings")); // NOI18N
        jBtnProxy.setFocusable(false);
        jBtnProxy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnProxyActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(jBtnProxy, gridBagConstraints);

        jLblLocalFilename.setLabelFor(jTxtLocalFilename);
        jLblLocalFilename.setText(NbBundle.getMessage(ClientInfo.class, "LBL_LocalFilename")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 22, 12, 6);
        add(jLblLocalFilename, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 6);
        add(jTxtLocalFilename, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        add(jLblDummy, gridBagConstraints);

        btnGrpWsdlSource.add(jRbnFilesystem);
        jRbnFilesystem.setText(org.openide.util.NbBundle.getMessage(ClientInfo.class, "LBL_WsdlSourceFilesystem")); // NOI18N
        jRbnFilesystem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRbnFilesystemActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jRbnFilesystem, gridBagConstraints);

        jLblWsdlFile.setLabelFor(jTxtWsdlFile);
        jLblWsdlFile.setText(org.openide.util.NbBundle.getMessage(ClientInfo.class, "LBL_WsdlFilename")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 22, 12, 6);
        add(jLblWsdlFile, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 6);
        add(jTxtWsdlFile, gridBagConstraints);
        jTxtWsdlFile.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ClientInfo.class, "ACSN_WsdlSourceFilesystem")); // NOI18N
        jTxtWsdlFile.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientInfo.class, "ACSD_WsdlSourceFile")); // NOI18N

        jBtnBrowse.setText(org.openide.util.NbBundle.getMessage(ClientInfo.class, "LBL_Browse")); // NOI18N
        jBtnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 6);
        add(jBtnBrowse, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jSeparator, gridBagConstraints);

        jLblClientName.setLabelFor(jTxtClientName);
        jLblClientName.setText(org.openide.util.NbBundle.getMessage(ClientInfo.class, "LBL_ClientName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 6);
        add(jLblClientName, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 6);
        add(jTxtClientName, gridBagConstraints);
        jTxtClientName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ClientInfo.class, "ACSN_ClientName")); // NOI18N
        jTxtClientName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientInfo.class, "ACSD_ClientName")); // NOI18N

        jLblProject.setLabelFor(jTxtProject);
        jLblProject.setText(org.openide.util.NbBundle.getMessage(ClientInfo.class, "LBL_Project")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(jLblProject, gridBagConstraints);

        jTxtProject.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 6);
        add(jTxtProject, gridBagConstraints);

        jLblPackageName.setLabelFor(jCbxPackageName);
        jLblPackageName.setText(org.openide.util.NbBundle.getMessage(ClientInfo.class, "LBL_PackageName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(jLblPackageName, gridBagConstraints);

        jCbxPackageName.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 6);
        add(jCbxPackageName, gridBagConstraints);

        jLabel1.setLabelFor(jTxtCreatedFile);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(ClientInfo.class, "LBL_CreatedFile")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(jLabel1, gridBagConstraints);

        jTxtCreatedFile.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 6);
        add(jTxtCreatedFile, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jSeparator1, gridBagConstraints);

        jCheckGenerateDataBinding.setText(org.openide.util.NbBundle.getMessage(ClientInfo.class, "LBL_SampleMidlet")); // NOI18N
        jCheckGenerateDataBinding.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckGenerateDataBinding.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        add(jCheckGenerateDataBinding, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void jBtnProxyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnProxyActionPerformed
        OptionsDisplayer.getDefault().open( "General" );//NOI18N
    }//GEN-LAST:event_jBtnProxyActionPerformed
    
    private void jBtnGetWsdlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnGetWsdlActionPerformed
        jTxtWsdlURL.setEditable(false);
        wsdlValidated = false;
        retriever = new WsdlRetriever(this, jTxtWsdlURL.getText().trim());
        if (rp == null){
            rp  = new RequestProcessor();
        }
        rp.post(retriever);
    }//GEN-LAST:event_jBtnGetWsdlActionPerformed
    
	private void jBtnBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnBrowseActionPerformed
            final JFileChooser chooser = new JFileChooser(previousDirectory);
            chooser.setMultiSelectionEnabled(false);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.addChoosableFileFilter(WSDL_FILE_FILTER);
            chooser.setFileFilter(WSDL_FILE_FILTER);
            
            if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                wsdlValidated = false;
                final File wsdlFile = chooser.getSelectedFile();
                jTxtWsdlFile.setText(wsdlFile.getAbsolutePath());
                previousDirectory = wsdlFile.getPath();
                updateHelperValues();
            }
	}//GEN-LAST:event_jBtnBrowseActionPerformed
        
    private void jRbnFilesystemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRbnFilesystemActionPerformed
        wsdlSource = WSDL_FROM_FILE;
        enableWsdlSourceFields(false, true);
        descriptorPanel.fireChangeEvent();
    }//GEN-LAST:event_jRbnFilesystemActionPerformed
    
	private void jRbnServiceURLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRbnServiceURLActionPerformed
            wsdlSource = WSDL_FROM_SERVICE;
            enableWsdlSourceFields(true, false);
            descriptorPanel.fireChangeEvent();
	}//GEN-LAST:event_jRbnServiceURLActionPerformed
        
    private void enableWsdlSourceFields(final boolean fromService, final boolean fromFile) {
        // file related fields
        jLblWsdlFile.setEnabled(fromFile);
        jTxtWsdlFile.setEnabled(fromFile);
        jBtnBrowse.setEnabled(fromFile);
        
        // service related fields
        jLblLocalFNDescription.setEnabled(fromService);
        jLblWsdlURL.setEnabled(fromService);
        jTxtWsdlURL.setEnabled(fromService);
        final String wsdlUrlText = jTxtWsdlURL.getText().trim();
        jBtnGetWsdl.setEnabled(fromService && isValidUrl(wsdlUrlText));
        jBtnProxy.setEnabled(fromService);
        jLblLocalFilename.setEnabled(fromService);
        jTxtLocalFilename.setEnabled(fromService);
    }
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup btnGrpWsdlSource;
    private javax.swing.JButton jBtnBrowse;
    private javax.swing.JButton jBtnGetWsdl;
    private javax.swing.JButton jBtnProxy;
    private javax.swing.JComboBox jCbxPackageName;
    private javax.swing.JCheckBox jCheckGenerateDataBinding;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLblChooseSource;
    private javax.swing.JLabel jLblClientName;
    private javax.swing.JLabel jLblDummy;
    private javax.swing.JLabel jLblLocalFNDescription;
    private javax.swing.JLabel jLblLocalFilename;
    private javax.swing.JLabel jLblPackageName;
    private javax.swing.JLabel jLblProject;
    private javax.swing.JLabel jLblWsdlFile;
    private javax.swing.JLabel jLblWsdlURL;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jRbnFilesystem;
    private javax.swing.JRadioButton jRbnServiceURL;
    private javax.swing.JSeparator jSeparator;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jTxtClientName;
    private javax.swing.JTextField jTxtCreatedFile;
    private javax.swing.JTextField jTxtLocalFilename;
    private javax.swing.JTextField jTxtProject;
    private javax.swing.JTextField jTxtWsdlFile;
    private javax.swing.JTextField jTxtWsdlURL;
    // End of variables declaration//GEN-END:variables
    
    private void initUserComponents() {
//        System.out.println("wizard panel created");
        setName(NbBundle.getMessage(ClientInfo.class, "TITLE_WebServiceClientWizard")); // NOI18N
        
        // Register listener on the textFields to make the automatic updates
        jTxtWsdlURL.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(@SuppressWarnings("unused") DocumentEvent e) {
                wsdlUrlChanged();
            }
            public void insertUpdate(@SuppressWarnings("unused") DocumentEvent e) {
                wsdlUrlChanged();
            }
            public void removeUpdate(@SuppressWarnings("unused") DocumentEvent e) {
                wsdlUrlChanged();
            }
        });
        
        jTxtLocalFilename.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(@SuppressWarnings("unused") DocumentEvent e) {
                updateTexts();
            }
            public void insertUpdate(@SuppressWarnings("unused") DocumentEvent e) {
                updateTexts();
            }
            public void removeUpdate(@SuppressWarnings("unused") DocumentEvent e) {
                updateTexts();
            }
        });
        jTxtWsdlFile.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(@SuppressWarnings("unused") DocumentEvent e) {
                updateTexts();
            }
            public void insertUpdate(@SuppressWarnings("unused") DocumentEvent e) {
                updateTexts();
            }
            public void removeUpdate(@SuppressWarnings("unused") DocumentEvent e) {
                updateTexts();
            }
        });
        jTxtClientName.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(@SuppressWarnings("unused") DocumentEvent e) {
                updateTexts();
            }
            public void insertUpdate(@SuppressWarnings("unused") DocumentEvent e) {
                updateTexts();
            }
            public void removeUpdate(@SuppressWarnings("unused") DocumentEvent e) {
                updateTexts();
            }
        });
        
        final Component editorComponent = jCbxPackageName.getEditor().getEditorComponent();
        if(editorComponent instanceof JTextComponent) {
            ((JTextComponent) editorComponent).getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(@SuppressWarnings("unused") DocumentEvent e) {
                    updateTexts();
                }
                public void insertUpdate(@SuppressWarnings("unused") DocumentEvent e) {
                    updateTexts();
                }
                public void removeUpdate(@SuppressWarnings("unused") DocumentEvent e) {
                    updateTexts();
                }
            });
        } else {
            // JComboBox is supposed to use a JTextComponent for editing, but in case
            // it isn't, at least do something to track changes.
            jCbxPackageName.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(@SuppressWarnings("unused") java.awt.event.ItemEvent evt) {
                    if(!settingFields) {
                        descriptorPanel.fireChangeEvent(); // Notify that the panel changed
                    }
                }
            });
        }
        
        jCbxPackageName.setRenderer(PackageView.listRenderer());
    }
    
    void store (final WizardDescriptor d) {
        if(wsdlSource == WSDL_FROM_SERVICE) {
            d.putProperty(WebServiceClientWizardIterator.WSDL_DOWNLOAD_URL, getDownloadUrl());
            d.putProperty(WebServiceClientWizardIterator.WSDL_DOWNLOAD_FILE, getDownloadWsdl());
            d.putProperty(WebServiceClientWizardIterator.WSDL_FILE_PATH, jTxtLocalFilename.getText().trim());
        } else if(wsdlSource == WSDL_FROM_FILE) {
            d.putProperty(WebServiceClientWizardIterator.WSDL_DOWNLOAD_URL, null);
            d.putProperty(WebServiceClientWizardIterator.WSDL_DOWNLOAD_FILE, null);
            d.putProperty(WebServiceClientWizardIterator.WSDL_FILE_PATH, jTxtWsdlFile.getText().trim());
        }
        d.putProperty(WebServiceClientWizardIterator.WSDL_PACKAGE_NAME, getPackageName());
        d.putProperty(WebServiceClientWizardIterator.JSR172_CLIENT_NAME, jTxtClientName.getText().trim());
        d.putProperty( WebServiceClientWizardIterator.PROP_DATABINDING, new Boolean( jCheckGenerateDataBinding.isSelected()));
    }
    
    void read(final WizardDescriptor d) {
        try {
            settingFields = true;
            
            final Project p = Templates.getProject(d);
            
            jTxtProject.setText(ProjectUtils.getInformation(p).getDisplayName());
            jTxtWsdlURL.setText((String) d.getProperty(WebServiceClientWizardIterator.WSDL_DOWNLOAD_URL));
            jTxtLocalFilename.setText(retriever != null ? retriever.getWsdlFileName() : "");
            jTxtClientName.setText(d.getProperty(WebServiceClientWizardIterator.JSR172_CLIENT_NAME) != null ?
                (String)d.getProperty(WebServiceClientWizardIterator.JSR172_CLIENT_NAME) : "");
            jTxtWsdlFile.setText((String) d.getProperty(WebServiceClientWizardIterator.WSDL_FILE_PATH));
            
            jCbxPackageName.setModel(getPackageModel(p));
            jCbxPackageName.setSelectedItem(getPackageItem((String) d.getProperty(WebServiceClientWizardIterator.WSDL_PACKAGE_NAME)));
            
            final Sources sources = p.getLookup().lookup(Sources.class);
            final SourceGroup sg = sources.getSourceGroups( JavaProjectConstants.SOURCES_TYPE_JAVA )[0]; //only one source root for mobile project
            root = sg.getRootFolder();
            final Object sel = jCbxPackageName.getSelectedItem();
            final String created = FileUtil.toFile(root).getAbsolutePath() +
                    "/" + ((sel != null) ? (sel.toString().replace('.', '/')) : "") +
                    "/" + jTxtClientName.getText().replace('.', '/');
            jTxtCreatedFile.setText( created + ".wsclient");
            // Normalize selection, in case it's unspecified.
            Integer source = (Integer) d.getProperty(WebServiceClientWizardIterator.WSDL_SOURCE);
            if(source == null || source.intValue() < WSDL_FROM_SERVICE || source.intValue() > WSDL_FROM_FILE) {
                source = new Integer(WSDL_FROM_SERVICE);
            }
            
            this.wsdlSource = source.intValue();
            this.retriever = null;
            this.downloadMsg = null;
            if (rp != null){
                rp.stop();
            }
            rp = null;
            enableWsdlSourceFields(wsdlSource == WSDL_FROM_SERVICE, wsdlSource == WSDL_FROM_FILE);
            btnGrpWsdlSource.setSelected(getSelectedRadioButton(wsdlSource).getModel(), true);
            
            final Boolean b = (Boolean) d.getProperty( WebServiceClientWizardIterator.PROP_DATABINDING );
            if( b != null && b.booleanValue()) {
                jCheckGenerateDataBinding.setSelected( true );
            } else {
                jCheckGenerateDataBinding.setSelected( false );
            }                        
        } finally {
            settingFields = false;
        }
    }
    
    private ComboBoxModel getPackageModel(final Project p) {
        ComboBoxModel result;
        final Sources sources = ProjectUtils.getSources(p);
        final SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        
        if(groups.length > 1) {
// !PW We cannot make the distinction between source and test source roots, so I don't
// want to merge all the packages at this time.  For now, just pick the first one,
// and maybe we can do better in the next version.
//            DefaultComboBoxModel packageModel = new DefaultComboBoxModel();
//            for(int i = 0; i < groups.length; i++) {
//                ComboBoxModel model = PackageView.createListView(groups[i]);
//                for(int j = 0, m = model.getSize(); j < m; j++) {
//                    packageModel.addElement(model.getElementAt(j));
//                }
//            }
//            result = packageModel;
            // Default to showing packages from first source root only for now.
            result = PackageView.createListView(groups[0]);
        } else if(groups.length == 1) {
            // Only one group, no processing needed.
            result = PackageView.createListView(groups[0]);
        } else {
            result = new DefaultComboBoxModel();
        }
        
        return result;
    }
    
    private Object getPackageItem(final String name) {
        Object result = name;
        
        final ComboBoxModel model = jCbxPackageName.getModel();
        final int max = model.getSize();
        for (int i = 0; i < max; i++) {
            final Object item = model.getElementAt(i);
            if(item.toString().equals(name)) {
                result = item;
                break;
            }
        }
        
        return result;
    }
    
    private String getPackageName() {
        return jCbxPackageName.getEditor().getItem().toString().trim();
    }
    
    private JRadioButton getSelectedRadioButton(final int selected) {
        JRadioButton result = jRbnServiceURL;
        
        switch(selected) {
            case WSDL_FROM_FILE:
                result = jRbnFilesystem;
                break;
            case WSDL_FROM_SERVICE:
                result = jRbnServiceURL;
                break;
        }
        
        return result;
    }
    
    protected byte [] getDownloadWsdl() {
        byte [] result = null;
        if(retriever != null && retriever.getState() == WsdlRetriever.STATUS_COMPLETE) {
            result = retriever.getWsdl();
        }
        return result;
    }
    
    private String getDownloadUrl() {
        String result;
        
        if(retriever != null) {
            // If we've done a download, save the URL that was actually used, not
            // what the user typed in.
            result = retriever.getWsdlUrl();
        } else {
            // If no download yet, then use what the user has typed.
            result = jTxtWsdlURL.getText().trim();
        }
        
        return result;
    }
    
    synchronized boolean valid(final WizardDescriptor wizardDescriptor) {
        final Project p = Templates.getProject(wizardDescriptor);
        
        // Project must currently have a target server that supports wscompile.
        if(!isWsCompileSupported(p)) {
            wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ClientInfo.class, "ERR_WsCompileNotSupportedByTargetServer")); // NOI18N
            return false; // project with web service client support, but no stub types defined.
        }
        
        if(wsdlSource == WSDL_FROM_SERVICE) {
            final String wsdlUrl = jTxtWsdlURL.getText().trim();
            if(wsdlUrl == null || wsdlUrl.length() == 0) {
                wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ClientInfo.class, "MSG_EnterURL")); // NOI18N
                return false;
            }
            
            if(retriever == null) {
                wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ClientInfo.class, "MSG_RetrieveWSDL")); // NOI18N
                return false;
            }
            
            if(retriever.getState() < WsdlRetriever.STATUS_COMPLETE) {
                wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ClientInfo.class, "MSG_DownloadProgress",  // NOI18N
                        ((downloadMsg != null) ? downloadMsg : NbBundle.getMessage(ClientInfo.class, "LBL_Unknown")))); // NOI18N
                return false;
            }
            
            if(retriever.getState() > WsdlRetriever.STATUS_COMPLETE) {
                if(downloadMsg != null) {
                    wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ClientInfo.class, "ERR_DownloadFailed", downloadMsg)); // NOI18N
                } else {
                    wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ClientInfo.class, "ERR_DownloadFailedUnknown")); // NOI18N
                }
                return false;
            }
            
            if (!wsdlValidated && !validating){
                wsdlValidated = true;
                validating = true;
                if (validatingRP == null) {
                    validatingRP = new RequestProcessor();
                }
                validatingRP.post(new Runnable() {
                    public void run() {
//                        wsdlValid = isWSICompliant(getDownloadWsdl());
                        wsdlValid = checkJSR172Compliant( getDownloadWsdl());
                        validating = false;
                        descriptorPanel.fireChangeEvent();
                    }
                }, 300);
            }
            
            if (validating){
                wizardDescriptor.putProperty(PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(WebServiceClientWizardDescriptor.class,"MSG_Validating", jTxtLocalFilename.getText()));
                return false;
            }
            
            if (!wsdlValid){
                wizardDescriptor.putProperty(PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(WebServiceClientWizardDescriptor.class,"MSG_NotWSI", jTxtLocalFilename.getText()));
                return false;
            }
            
            // url is ok, and file is downloaded if we get here.  Now check generated local filename
            // !PW FIXME what do we want to check it for?  Existence in temp directory?
            
            // Now drop down to do package validation.
        } else if(wsdlSource == WSDL_FROM_FILE) {
            final String wsdlFilePath = jTxtWsdlFile.getText().trim();
            
            if(wsdlFilePath == null || wsdlFilePath.length() == 0) {
                wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ClientInfo.class, "MSG_EnterFilename")); // NOI18N
                return false; // unspecified WSDL file
            }
            
            final File f = new File(wsdlFilePath);
            if(f == null) {
                wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ClientInfo.class, "ERR_WsdlInvalid")); // NOI18N
                return false; // invalid WSDL file
            }
            
            if(!f.exists()) {
                wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ClientInfo.class, "ERR_WsdlDoesNotExist")); // NOI18N
                wsdlValidated = false;
                return false; // invalid WSDL file
            }
            
            // 50103 - could be done via xml api, but this way should be quicker and suffice the need
            FileReader fr = null;
            try {
                fr = new FileReader(f);
                final LineNumberReader lnReader = new LineNumberReader(fr);
                if (lnReader != null) {
                    String line = null;
                    try {
                        line = lnReader.readLine();
                    } catch (IOException ioe) {
                        //ignore
                    }
                    while (line != null) {
                        if (line.indexOf("REPLACE_WITH_ACTUAL_URL") > 0) {
                            wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ClientInfo.class, "ERR_WrongWsdl")); // NOI18N
                            return false;
                        } //NOI18N
                        try {
                            line = lnReader.readLine();
                        } catch (IOException ioe) {
                            //ignore
                        }
                    }
                }
            } catch (FileNotFoundException fne) {
                wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ClientInfo.class, "ERR_WsdlDoesNotExist")); // NOI18N
            }
            
            if (!wsdlValidated && !validating){
                wsdlValidated = true;
                validating = true;
                if (validatingRP == null) {
                    validatingRP = new RequestProcessor();
                }
                validatingRP.post(new Runnable() {
                    public void run() {
                        wsdlValid = checkJSR172Compliant( f );
                        validating = false;
                        descriptorPanel.fireChangeEvent();
                    }
                }, 300);
            }
            if (validating){
                wizardDescriptor.putProperty(PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(WebServiceClientWizardDescriptor.class,"MSG_Validating", jTxtLocalFilename.getText()));
                return false;
            }
            
            if (!wsdlValid){
                wizardDescriptor.putProperty(PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(WebServiceClientWizardDescriptor.class,"MSG_NotWSI", f.getName()));
                return false;
            }
            // !PW FIXME should also detect if WSDL file has previously been added to
            // this project.  Note that not doing so and overwriting the existing entry
            // is the equivalent of doing an update on it.  Nothing bad will happen
            // unless it turns out the user didn't want to update the service in the
            // first place.
        }
        
        final String packageName = getPackageName();
        if(packageName == null || packageName.length() == 0) {
            wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ClientInfo.class, "MSG_EnterJavaPackageName")); // NOI18N
            return false; // unspecified WSDL file
        }
        
        if(!isJavaPackage(packageName)) {
            wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ClientInfo.class, "ERR_PackageInvalid")); // NOI18N
            return false; // invalid package name
        }
        
        final String clientName = jTxtClientName.getText().trim();
        if (!isValidTypeIdentifier(clientName)){
            if ("".equals(clientName)){
                wizardDescriptor.putProperty(PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(WebServiceClientWizardDescriptor.class,"MSG_NoName"));
            } else {
                wizardDescriptor.putProperty(PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(WebServiceClientWizardDescriptor.class,"MSG_InvalidName"));
            }
            return false;
        } else if (isClientExists()){
            wizardDescriptor.putProperty(PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(WebServiceClientWizardDescriptor.class,"MSG_AlreadyExists", jTxtCreatedFile.getText()));
            return false;
        }
        wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, " "); //NOI18N
        
        return true;
    }
    
    private static boolean isValidTypeIdentifier(final String ident) {
        if (ident == null || "".equals(ident) || !Utilities.isJavaIdentifier(ident)) {
            return false;
        } 
        return true;
    }
    
    private boolean isClientExists(){
        return new File(jTxtCreatedFile.getText()).exists();
    }
    
    private boolean isWsCompileSupported(@SuppressWarnings("unused")
	final Project p) {
        // Determine if wscompile is supported by the current target server of
        // this project.  Default to true so that the user can still continue, if on
        // their own, in case we have difficulty getting the correct answer.
        final boolean result = true;
        
//        J2eeModuleProvider provider = (J2eeModuleProvider) p.getLookup().lookup(J2eeModuleProvider.class);
//        if(provider != null) {
//            String serverInstanceID = provider.getServerInstanceID();
//            if(serverInstanceID != null && serverInstanceID.length() > 0) {
//                J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstanceID);
//                if(!j2eePlatform.isToolSupported(WebServiceClientWizardIterator.WSCOMPILE)) {
//                    result = false;
//                }
//            }
//        }
        
        return result;
    }
    
    protected void wsdlUrlChanged() {
        // Throw away any existing retriever.  New URL means user has to download it again.
        retriever = null;
        if (rp != null){
            rp.stop();
        }
        rp = null;
        
        // Only enable retrieval button if there is a URL specified.
        final String wsdlUrlText = jTxtWsdlURL.getText().trim();
        jBtnGetWsdl.setEnabled(isValidUrl(wsdlUrlText));
        
        updateHelperValues();
        updateTexts();
    }
    
    protected void updateTexts() {
        if(!settingFields) {
            final Object editorComponent = jCbxPackageName.getEditor().getEditorComponent();
            if (editorComponent instanceof JTextComponent){
                final String created = FileUtil.toFile(root).getAbsolutePath() +
                        "/" + ((JTextComponent)editorComponent).getText().replace('.', '/') +
                        "/" + jTxtClientName.getText().replace('.', '/');
                if (jTxtClientName.getText().trim().length() != 0)
                    jTxtCreatedFile.setText(created + ".wsclient");
                else
                    jTxtCreatedFile.setText(created);
                descriptorPanel.fireChangeEvent(); // Notify that the panel changed
            }
        }
    }
    
    private boolean isValidUrl(final String urlText) {
        if(urlText == null || urlText.length() == 0) {
            return false;
        }
        
        // !PW Be very careful adding conditions to this method (such as seeing if
        // conversion of url text to URL would throw a MalformedURLException and
        // reporting it to the user early.)  It is a non-trivial change that would
        // require significant synchronization with code in the retriever object.
        // as well as the valid() method of this object.  See IZ 52685.
        return true;
    }
    
    public void setWsdlDownloadMessage(final String m) {
        downloadMsg = m;
        
        // reenable edit control if state indicates download is completed (or failed).
        if(retriever.getState() >= WsdlRetriever.STATUS_COMPLETE) {
            jTxtWsdlURL.setEditable(true);
            jTxtLocalFilename.setText(retriever.getWsdlFileName());
            
            updateHelperValues();
        }
        
        descriptorPanel.fireChangeEvent();
    }
    
    /** Package name validation
     */
    private static boolean isJavaPackage(final String pkg) {
        boolean result = false;
        
        if(pkg != null && pkg.length() > 0) {
            int state = 0;
            for(int i = 0, pkglength = pkg.length(); i < pkglength && state < 2; i++) {
                switch(state) {
                    case 0:
                        if(Character.isJavaIdentifierStart(pkg.charAt(i))) {
                            state = 1;
                        } else {
                            state = 2;
                        }
                        break;
                    case 1:
                        if(pkg.charAt(i) == '.') {
                            state = 0;
                        } else if(!Character.isJavaIdentifierPart(pkg.charAt(i))) {
                            state = 2;
                        }
                        break;
                }
            }
            
            if(state == 1) {
                result = true;
            }
        }
        
        return result;
    }
    
    private synchronized boolean checkJSR172Compliant( final File wsdlFile ) {
        try {
            WSDL2Java.Configuration config = new WSDL2Java.Configuration();
            config.setWSDLFileName( wsdlFile.toURL().toString());
            WSDL2Java wsdl2java = WSDL2JavaFactory.getWSDL2Java( config );
            
            List<WSDL2Java.ValidationResult> validationResults = wsdl2java.validate();
            
            return showValidationResults( validationResults );
            
        } catch( IOException e ) {
            
        }
        
        return true;
    }
    
    private synchronized boolean checkJSR172Compliant( final byte[] b ) {
        File tempWSDL = null;
        try {
            tempWSDL = File.createTempFile( "jsr172validate", "wsdl" ); //NOI18N
            final BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream( tempWSDL ));
            bos.write( b );
            bos.close();
            
            WSDL2Java.Configuration config = new WSDL2Java.Configuration();
            config.setWSDLFileName( tempWSDL.toURL().toString());
            WSDL2Java wsdl2java = WSDL2JavaFactory.getWSDL2Java( config );
            
            List<WSDL2Java.ValidationResult> validationResults = wsdl2java.validate();
            
            return showValidationResults( validationResults );
            
        } catch( IOException e ) {
        } catch( Exception e ) {
            System.err.println(" --- ----");
            e.printStackTrace();
        } finally {
            if( tempWSDL != null ) {
                tempWSDL.delete();
            }
        }
        
        return true;
    }
        
    private boolean showValidationResults( List<WSDL2Java.ValidationResult> validationResults ) {
        
        boolean presentWarnings = false;
        boolean presentErrors = false;
        
        for( WSDL2Java.ValidationResult v : validationResults ) {
            if( v.getErrorLevel() == WSDL2Java.ValidationResult.ErrorLevel.FATAL ) {
                presentErrors = true;
                break;
            }
            if( v.getErrorLevel() == WSDL2Java.ValidationResult.ErrorLevel.WARNING ) {
                presentWarnings = true;
                break;
            }
        }
        if( presentWarnings | presentErrors ) {
            Dialog d = DialogDisplayer.getDefault().createDialog( new DialogDescriptor( new ValidationNotifier( validationResults ), "Validation Results" ));        
            d.setModal( true );
            d.setVisible( true );
        }
        
        return !presentErrors;
    }
    
    private void updateHelperValues() {
        String possibleClientName;
        if(wsdlSource == WSDL_FROM_SERVICE) {
            possibleClientName = retriever != null ? retriever.getWsdlFileName() : ""; //NOI18N
        } else {
            possibleClientName = jTxtWsdlFile.getText();
            possibleClientName = possibleClientName.replace('\\','/'); //NOI18N
            
            if (possibleClientName.lastIndexOf('/') != -1){
                possibleClientName = possibleClientName.substring(possibleClientName.lastIndexOf('/') + 1);
            }
        }
        if (possibleClientName == null || possibleClientName.length() == 0){
            return;
        }
        
        if (possibleClientName.indexOf('.') != -1)
            possibleClientName = possibleClientName.substring(0, possibleClientName.indexOf('.'));
        
        //if (jTxtClientName.getText().trim().length() == 0 ){
        jTxtClientName.setText(possibleClientName);
        //}
        final Object editorComponent = jCbxPackageName.getEditor().getEditorComponent();
        if (editorComponent instanceof JTextComponent){
            //if(((JTextComponent)editorComponent).getText().trim().length() == 0 ){
            ((JTextComponent)editorComponent).setText(possibleClientName.toLowerCase());
            //}
        }
    }
    
    private static class WsdlFileFilter extends FileFilter {
        WsdlFileFilter() {
            //to avoid creation of accessor class
        }
        
        public boolean accept(final File f) {
            boolean result;
            if(f.isDirectory() || "wsdl".equalsIgnoreCase(FileUtil.getExtension(f.getName()))) { // NOI18N
                result = true;
            } else {
                result = false;
            }
            return result;
        }
        
        public String getDescription() {
            return NbBundle.getMessage(ClientInfo.class, "LBL_WsdlFilterDescription"); // NOI18N
        }
    }
}
