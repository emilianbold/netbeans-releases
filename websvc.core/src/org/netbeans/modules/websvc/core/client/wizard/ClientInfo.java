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

package org.netbeans.modules.websvc.core.client.wizard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.List;

import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JFileChooser;
import javax.swing.JRadioButton;
import javax.swing.filechooser.FileFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.text.JTextComponent;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.core.ClientWizardProperties;
import org.netbeans.modules.websvc.core.webservices.ui.panels.ProxySettingsDlg;
import org.netbeans.modules.websvc.core.webservices.ui.panels.WebProxySetter;
import org.netbeans.modules.websvc.core.WsdlRetriever;
import org.netbeans.modules.websvc.core.WsdlRetriever;
import org.netbeans.modules.websvc.core.jaxws.JaxWsExplorerPanel;
import org.netbeans.modules.websvc.core.JaxWsUtils;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.spi.project.ui.templates.support.Templates;

import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.api.client.ClientStubDescriptor;

import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.core.WsWsdlCookie;
import org.openide.ErrorManager;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.util.Task;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 *
 * @author Peter Williams
 */
public final class ClientInfo extends JPanel implements WsdlRetriever.MessageReceiver {
    
    private static final String PROP_ERROR_MESSAGE = "WizardPanel_errorMessage"; // NOI18N
    
    private static final int WSDL_FROM_PROJECT = 0;
    private static final int WSDL_FROM_FILE = 1;
    private static final int WSDL_FROM_URL = 2;
    
    private static final FileFilter WSDL_FILE_FILTER = new WsdlFileFilter();
    private static String previousDirectory = ""; //NOI18N
    
    private WebServiceClientWizardDescriptor descriptorPanel;
    private WizardDescriptor wizardDescriptor;
    
    private boolean settingFields;
    private int wsdlSource;
    private File wsdlTmpFile;
    
    // properties for 'get from server'
    private WsdlRetriever retriever;
    private String downloadMsg;
    
    private boolean retrieverFailed = false;
    private boolean isWaitingForScan = false;
    
    private Project project;
    private int projectType;
    
    public ClientInfo(WebServiceClientWizardDescriptor panel) {
        descriptorPanel = panel;
        
        this.settingFields = false;
        this.wsdlSource = WSDL_FROM_PROJECT;
        this.wsdlTmpFile = null;
        this.retriever = null;
        
        initComponents();
        jLblClientType.setVisible(false);
        jCbxClientType.setVisible(false);
        jComboBoxJaxVersion.setModel(new DefaultComboBoxModel(new String[] {ClientWizardProperties.JAX_WS, ClientWizardProperties.JAX_RPC}));
        initUserComponents();
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
        jRbnFilesystem = new javax.swing.JRadioButton();
        jTxtWsdlProject = new javax.swing.JTextField();
        jBtnBrowse = new javax.swing.JButton();
        jRbnProject = new javax.swing.JRadioButton();
        jTxtWsdlURL = new javax.swing.JTextField();
        jBtnProxy = new javax.swing.JButton();
        jTxtLocalFilename = new javax.swing.JTextField();
        jLblPackageDescription = new javax.swing.JLabel();
        jLblProject = new javax.swing.JLabel();
        jTxtProject = new javax.swing.JTextField();
        jLblPackageName = new javax.swing.JLabel();
        jCbxPackageName = new javax.swing.JComboBox();
        jLblClientType = new javax.swing.JLabel();
        jCbxClientType = new javax.swing.JComboBox();
        jRbnUrl = new javax.swing.JRadioButton();
        jBtnBrowse1 = new javax.swing.JButton();
        jLabelJaxVersion = new javax.swing.JLabel();
        jComboBoxJaxVersion = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLblChooseSource, NbBundle.getMessage(ClientInfo.class, "LBL_WsdlSource")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLblChooseSource, gridBagConstraints);

        btnGrpWsdlSource.add(jRbnFilesystem);
        org.openide.awt.Mnemonics.setLocalizedText(jRbnFilesystem, org.openide.util.NbBundle.getMessage(ClientInfo.class, "LBL_WsdlSourceFilesystem")); // NOI18N
        jRbnFilesystem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRbnFilesystemActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(jRbnFilesystem, gridBagConstraints);
        jRbnFilesystem.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientInfo.class, "A11Y_WsdlSource")); // NOI18N

        jTxtWsdlProject.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(jTxtWsdlProject, gridBagConstraints);
        jTxtWsdlProject.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ClientInfo.class, "ACSN_WsdlSourceFilesystem")); // NOI18N
        jTxtWsdlProject.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientInfo.class, "ACSD_WsdlSourceFile")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jBtnBrowse, org.openide.util.NbBundle.getMessage(ClientInfo.class, "LBL_Browse")); // NOI18N
        jBtnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(jBtnBrowse, gridBagConstraints);
        jBtnBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(ClientInfo.class).getString("A11Y_BrowseLocalFile")); // NOI18N

        btnGrpWsdlSource.add(jRbnProject);
        jRbnProject.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jRbnProject, NbBundle.getMessage(ClientInfo.class, "LBL_ProjectUrl")); // NOI18N
        jRbnProject.setFocusable(false);
        jRbnProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRbnProjectActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(jRbnProject, gridBagConstraints);
        jRbnProject.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientInfo.class, "A11Y_WsdlSourceUrl")); // NOI18N

        jTxtWsdlURL.setColumns(30);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 6);
        add(jTxtWsdlURL, gridBagConstraints);
        jTxtWsdlURL.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ClientInfo.class, "ACSN_WsdlSourceUrl")); // NOI18N
        jTxtWsdlURL.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientInfo.class, "ACSD_WsdlSourceUrl")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jBtnProxy, org.openide.util.NbBundle.getMessage(ClientInfo.class, "LBL_ProxySettings")); // NOI18N
        jBtnProxy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnProxyActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(jBtnProxy, gridBagConstraints);
        jBtnProxy.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientInfo.class, "A11Y_ProxySettings")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(jTxtLocalFilename, gridBagConstraints);
        jTxtLocalFilename.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(ClientInfo.class).getString("ACSN_WsdlSourceLocalFile")); // NOI18N
        jTxtLocalFilename.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientInfo.class, "A11Y_LocalFilename")); // NOI18N

        jLblPackageDescription.setLabelFor(jCbxPackageName);
        org.openide.awt.Mnemonics.setLocalizedText(jLblPackageDescription, NbBundle.getMessage(ClientInfo.class, "LBL_PackageDescription")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 12, 0);
        add(jLblPackageDescription, gridBagConstraints);

        jLblProject.setLabelFor(jTxtProject);
        org.openide.awt.Mnemonics.setLocalizedText(jLblProject, org.openide.util.NbBundle.getMessage(ClientInfo.class, "LBL_Project")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(jLblProject, gridBagConstraints);

        jTxtProject.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(jTxtProject, gridBagConstraints);
        jTxtProject.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientInfo.class, "A11Y_Project")); // NOI18N

        jLblPackageName.setLabelFor(jCbxPackageName);
        org.openide.awt.Mnemonics.setLocalizedText(jLblPackageName, org.openide.util.NbBundle.getMessage(ClientInfo.class, "LBL_PackageName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(jLblPackageName, gridBagConstraints);

        jCbxPackageName.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(jCbxPackageName, gridBagConstraints);

        jLblClientType.setLabelFor(jCbxClientType);
        org.openide.awt.Mnemonics.setLocalizedText(jLblClientType, org.openide.util.NbBundle.getMessage(ClientInfo.class, "LBL_ClientType")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(jLblClientType, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(jCbxClientType, gridBagConstraints);
        jCbxClientType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientInfo.class, "A11Y_ClientType")); // NOI18N

        btnGrpWsdlSource.add(jRbnUrl);
        org.openide.awt.Mnemonics.setLocalizedText(jRbnUrl, org.openide.util.NbBundle.getMessage(ClientInfo.class, "LBL_WsdlUrl")); // NOI18N
        jRbnUrl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRbnUrlActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jRbnUrl, gridBagConstraints);
        jRbnUrl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(ClientInfo.class).getString("A11Y_WsdlURL")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jBtnBrowse1, org.openide.util.NbBundle.getMessage(ClientInfo.class, "LBL_Browse1")); // NOI18N
        jBtnBrowse1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnBrowse1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(jBtnBrowse1, gridBagConstraints);
        jBtnBrowse1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(ClientInfo.class).getString("A11Y_BrowseWSDLProject")); // NOI18N

        jLabelJaxVersion.setLabelFor(jComboBoxJaxVersion);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelJaxVersion, org.openide.util.NbBundle.getBundle(ClientInfo.class).getString("LBL_JAX_Version")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(28, 0, 0, 6);
        add(jLabelJaxVersion, gridBagConstraints);

        jComboBoxJaxVersion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jaxwsVersionHandler(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(24, 6, 0, 0);
        add(jComboBoxJaxVersion, gridBagConstraints);

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientInfo.class, "LBL_WsdlSource")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
private void jaxwsVersionHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jaxwsVersionHandler
    // TODO add your handling code here:
    descriptorPanel.fireChangeEvent();
    String jaxwsVersion = (String)this.jComboBoxJaxVersion.getSelectedItem();
    String pName = (String)this.jCbxPackageName.getSelectedItem();
    if(Util.isJavaEE5orHigher(project)||
            (projectType == 0 && jaxwsVersion.equals(ClientWizardProperties.JAX_WS)) ){
        if(pName == null || pName.trim().equals("")){
            jCbxPackageName.setToolTipText(NbBundle.getMessage(ClientInfo.class, "TOOLTIP_DEFAULT_PACKAGE"));
        } else{
            jCbxPackageName.setToolTipText("");
        }
    } else{
        jCbxPackageName.setToolTipText("");
    }
}//GEN-LAST:event_jaxwsVersionHandler

    private void jBtnBrowse1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnBrowse1ActionPerformed
        // TODO add your handling code here:
        String result = browseProjectServices();
        if (result!=null) jTxtWsdlProject.setText(result);
    }//GEN-LAST:event_jBtnBrowse1ActionPerformed
    
    private void jRbnUrlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRbnUrlActionPerformed
        // TODO add your handling code here:
        wsdlSource = WSDL_FROM_URL;
        enableWsdlSourceFields(false, false, true);
        descriptorPanel.fireChangeEvent();
    }//GEN-LAST:event_jRbnUrlActionPerformed
    
    private void jBtnProxyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnProxyActionPerformed
        ProxySettingsDlg.showDialog();
        wsdlUrlChanged();
    }//GEN-LAST:event_jBtnProxyActionPerformed
    
	private void jBtnBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnBrowseActionPerformed
            // 		System.out.println("browse for wsdl file...");
            JFileChooser chooser = new JFileChooser(previousDirectory);
            chooser.setMultiSelectionEnabled(false);
            chooser.setAcceptAllFileFilterUsed(true);
            chooser.addChoosableFileFilter(WSDL_FILE_FILTER);
            chooser.setFileFilter(WSDL_FILE_FILTER);
            
            if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File wsdlFile = chooser.getSelectedFile();
                jTxtLocalFilename.setText(wsdlFile.getAbsolutePath());
                previousDirectory = wsdlFile.getPath();
                int result = resolveImports(wsdlFile);
                if ((result & 0x02) == 0x02) {
                    String proxyHost = WebProxySetter.getInstance().getProxyHost();
                    if (proxyHost==null || proxyHost.length()==0)
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                NbBundle.getMessage(ClientInfo.class,"MSG_SetUpProxyForImports"),NotifyDescriptor.WARNING_MESSAGE)); //NOI18N
                }
                if ((result & 0x01) == 0x01) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                            NbBundle.getMessage(ClientInfo.class,"MSG_IdeGeneratedStaticStubOnly"),NotifyDescriptor.WARNING_MESSAGE)); //NOI18N
                }
                
            }
	}//GEN-LAST:event_jBtnBrowseActionPerformed
        
    private void jRbnFilesystemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRbnFilesystemActionPerformed
        //        System.out.println("get from filesystem selected.");
        wsdlSource = WSDL_FROM_FILE;
        enableWsdlSourceFields(false, true, false);
        descriptorPanel.fireChangeEvent();
    }//GEN-LAST:event_jRbnFilesystemActionPerformed
    
    private void jRbnProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRbnProjectActionPerformed
        //        System.out.println("get from url selected.");
        wsdlSource = WSDL_FROM_PROJECT;
        enableWsdlSourceFields(true, false, false);
        descriptorPanel.fireChangeEvent();
    }//GEN-LAST:event_jRbnProjectActionPerformed
    
    private void enableWsdlSourceFields(boolean fromProject, boolean fromFile, boolean fromUrl) {
        // project related fields
        jTxtWsdlProject.setEnabled(fromProject);
        jBtnBrowse1.setEnabled(fromProject);
        
        // file systam related fields
        jTxtLocalFilename.setEnabled(fromFile);
        jBtnBrowse.setEnabled(fromFile);
        
        // service related fields
        jTxtWsdlURL.setEnabled(fromUrl);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup btnGrpWsdlSource;
    private javax.swing.JButton jBtnBrowse;
    private javax.swing.JButton jBtnBrowse1;
    private javax.swing.JButton jBtnProxy;
    private javax.swing.JComboBox jCbxClientType;
    private javax.swing.JComboBox jCbxPackageName;
    private javax.swing.JComboBox jComboBoxJaxVersion;
    private javax.swing.JLabel jLabelJaxVersion;
    private javax.swing.JLabel jLblChooseSource;
    private javax.swing.JLabel jLblClientType;
    private javax.swing.JLabel jLblPackageDescription;
    private javax.swing.JLabel jLblPackageName;
    private javax.swing.JLabel jLblProject;
    private javax.swing.JRadioButton jRbnFilesystem;
    private javax.swing.JRadioButton jRbnProject;
    private javax.swing.JRadioButton jRbnUrl;
    private javax.swing.JTextField jTxtLocalFilename;
    private javax.swing.JTextField jTxtProject;
    private javax.swing.JTextField jTxtWsdlProject;
    private javax.swing.JTextField jTxtWsdlURL;
    // End of variables declaration//GEN-END:variables
    
    private void initUserComponents() {
        //        System.out.println("wizard panel created");
        
        setName(NbBundle.getMessage(ClientInfo.class, "TITLE_WebServiceClientWizard")); // NOI18N
        
        // Register listener on the textFields to make the automatic updates
        jTxtWsdlURL.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                wsdlUrlChanged();
            }
            public void insertUpdate(DocumentEvent e) {
                wsdlUrlChanged();
            }
            public void removeUpdate(DocumentEvent e) {
                wsdlUrlChanged();
            }
        });
        jTxtLocalFilename.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateTexts();
            }
            public void insertUpdate(DocumentEvent e) {
                updateTexts();
            }
            public void removeUpdate(DocumentEvent e) {
                updateTexts();
            }
        });
        jTxtWsdlProject.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                wsdlUrlChanged();
            }
            public void insertUpdate(DocumentEvent e) {
                wsdlUrlChanged();
            }
            public void removeUpdate(DocumentEvent e) {
                wsdlUrlChanged();
            }
        });
        
        Component editorComponent = jCbxPackageName.getEditor().getEditorComponent();
        if(editorComponent instanceof JTextComponent) {
            ((JTextComponent) editorComponent).getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    updateTexts();
                }
                public void insertUpdate(DocumentEvent e) {
                    updateTexts();
                }
                public void removeUpdate(DocumentEvent e) {
                    updateTexts();
                }
            });
        } else {
            // JComboBox is supposed to use a JTextComponent for editing, but in case
            // it isn't, at least do something to track changes.
            jCbxPackageName.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent evt) {
                    if(!settingFields) {
                        descriptorPanel.fireChangeEvent(); // Notify that the panel changed
                    }
                }
            });
        }
        
        jCbxPackageName.setRenderer(PackageView.listRenderer());
    }
    
    void store(WizardDescriptor d) {
        //        System.out.println("storing wizard properties");
        
        if(wsdlSource == WSDL_FROM_PROJECT || wsdlSource == WSDL_FROM_URL) {
            d.putProperty(ClientWizardProperties.WSDL_DOWNLOAD_URL, getDownloadUrl());
            d.putProperty(ClientWizardProperties.WSDL_DOWNLOAD_FILE, getDownloadWsdl());
            d.putProperty(ClientWizardProperties.WSDL_DOWNLOAD_SCHEMAS, getDownloadedSchemas());
            d.putProperty(ClientWizardProperties.WSDL_FILE_PATH, retriever == null ? "" : retriever.getWsdlFileName()); //NOI18N
        } else if(wsdlSource == WSDL_FROM_FILE) {
            d.putProperty(ClientWizardProperties.WSDL_DOWNLOAD_URL, null);
            d.putProperty(ClientWizardProperties.WSDL_DOWNLOAD_FILE, null);
            d.putProperty(ClientWizardProperties.WSDL_DOWNLOAD_SCHEMAS, null);
            d.putProperty(ClientWizardProperties.WSDL_FILE_PATH, jTxtLocalFilename.getText().trim());
        }
        d.putProperty(ClientWizardProperties.WSDL_PACKAGE_NAME, getPackageName());
        d.putProperty(ClientWizardProperties.CLIENT_STUB_TYPE, jCbxClientType.getSelectedItem());
        d.putProperty(ClientWizardProperties.JAX_VERSION, jComboBoxJaxVersion.getSelectedItem());
    }
    
    void read(WizardDescriptor d) {
        //        System.out.println("reading wizard properties");
        this.wizardDescriptor = d;
        
        project = Templates.getProject(d);
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        EjbJar em = EjbJar.getEjbJar(project.getProjectDirectory());
        Car car = Car.getCar(project.getProjectDirectory());
        if (car != null)
            projectType = 3;
        else if (em != null)
            projectType = 2;
        else if (wm != null)
            projectType = 1;
        else
            projectType = 0;
        
        if (projectType > 0) {
            
            if (!Util.isJavaEE5orHigher(project)) {
                jLblClientType.setVisible(true);
                jCbxClientType.setVisible(true);
            }
        }
        
        //test JAX-WS library
        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath classPath;
        FileObject wsimportFO = null;
        FileObject wscompileFO = null;
        if (sgs.length > 0) {
            classPath = ClassPath.getClassPath(sgs[0].getRootFolder(),ClassPath.COMPILE);
            if (classPath != null) {
                wsimportFO = classPath.findResource("com/sun/tools/ws/ant/WsImport.class"); //NOI18N
                wscompileFO = classPath.findResource("com/sun/xml/rpc/tools/ant/Wscompile.class"); //NOI18N
            }
        }
        
        boolean jsr109OldSupported = isJsr109OldSupported(project);
        boolean jsr109Supported = isJsr109Supported(project);
        boolean jwsdpSupported = isJwsdpSupported(project);
        if (projectType > 0) {
            jLabelJaxVersion.setEnabled(false);
            jComboBoxJaxVersion.setEnabled(false);
            if (projectType==3 || Util.isJavaEE5orHigher(project) || JaxWsUtils.isEjbJavaEE5orHigher(project)) //NOI18N
                jComboBoxJaxVersion.setSelectedItem(ClientWizardProperties.JAX_WS);
            else{
                if((!jsr109OldSupported && !jsr109Supported)
                        || (!jsr109Supported && jsr109OldSupported && jwsdpSupported )){
                    jComboBoxJaxVersion.setSelectedItem(ClientWizardProperties.JAX_WS);
                } else{
                    jComboBoxJaxVersion.setSelectedItem(ClientWizardProperties.JAX_RPC);
                }
            }
        } else {
            if (Util.isSourceLevel16orHigher(project)) {
                jLabelJaxVersion.setEnabled(false);
                jComboBoxJaxVersion.setEnabled(false);
                jComboBoxJaxVersion.setSelectedItem(ClientWizardProperties.JAX_WS);
            } else if (Util.getSourceLevel(project).equals("1.5")) { //NOI18N
                if (wsimportFO != null) {
                    jLabelJaxVersion.setEnabled(false);
                    jComboBoxJaxVersion.setEnabled(false);
                    jComboBoxJaxVersion.setSelectedItem(ClientWizardProperties.JAX_WS);
                } else if (wscompileFO != null) {
                    jLabelJaxVersion.setEnabled(false);
                    jComboBoxJaxVersion.setEnabled(false);
                    jComboBoxJaxVersion.setSelectedItem(ClientWizardProperties.JAX_RPC);
                } else {
                    jLabelJaxVersion.setEnabled(true);
                    jComboBoxJaxVersion.setEnabled(true);
                }
            } else {
                jLabelJaxVersion.setEnabled(false);
                jComboBoxJaxVersion.setEnabled(false);
                jComboBoxJaxVersion.setSelectedItem(ClientWizardProperties.JAX_RPC);
            }
        }
        
        try {
            settingFields = true;
            
            Project p = Templates.getProject(d);
            
            jTxtProject.setText(ProjectUtils.getInformation(p).getDisplayName());
            jTxtWsdlURL.setText((String) d.getProperty(ClientWizardProperties.WSDL_DOWNLOAD_URL));
            jTxtLocalFilename.setText(retriever != null ? retriever.getWsdlFileName() : ""); //NOI18N
            jTxtWsdlURL.setText((String) d.getProperty(ClientWizardProperties.WSDL_FILE_PATH));
            
            jCbxPackageName.setModel(getPackageModel(p));
            String pName = (String) d.getProperty(ClientWizardProperties.WSDL_PACKAGE_NAME);
            String jaxwsVersion = (String)this.jComboBoxJaxVersion.getSelectedItem();
            if(Util.isJavaEE5orHigher(project)||
                    (projectType == 0 && jaxwsVersion.equals(ClientWizardProperties.JAX_WS)) ){
                if(pName == null){
                    jCbxPackageName.setToolTipText(NbBundle.getMessage(ClientInfo.class, "TOOLTIP_DEFAULT_PACKAGE"));
                } else{
                    jCbxPackageName.setToolTipText("");
                }
            } else{
                jCbxPackageName.setToolTipText("");
            }
            jCbxPackageName.setSelectedItem(getPackageItem(pName));
            // Normalize selection, in case it's unspecified.
            Integer source = (Integer) d.getProperty(ClientWizardProperties.WSDL_SOURCE);
            if(source == null || source.intValue() < WSDL_FROM_PROJECT || source.intValue() > WSDL_FROM_URL) {
                source = new Integer(WSDL_FROM_PROJECT);
            }
            
            this.wsdlSource = source.intValue();
            this.wsdlTmpFile = null;
            this.retriever = null;
            this.downloadMsg = null;
            
            enableWsdlSourceFields(wsdlSource == WSDL_FROM_PROJECT, wsdlSource == WSDL_FROM_FILE, wsdlSource == WSDL_FROM_URL);
            btnGrpWsdlSource.setSelected(getSelectedRadioButton(wsdlSource).getModel(), true);
            
            // Retrieve stub list from current project (have to be careful with caching
            // because the user might go back and change the project.)
            // Then set the stub list and current selected stub only if there was one
            // saved *and* it's in the list that the current project supports.
            WebServicesClientSupport clientSupport =
                    WebServicesClientSupport.getWebServicesClientSupport(p.getProjectDirectory());
            
            Object selectedStub = d.getProperty(ClientWizardProperties.CLIENT_STUB_TYPE);
            DefaultComboBoxModel stubModel = new DefaultComboBoxModel();
            if(clientSupport != null) {
                List<ClientStubDescriptor> clientStubs = clientSupport.getStubDescriptors();
                for(Iterator iter = clientStubs.iterator(); iter.hasNext(); ) {
                    stubModel.addElement(iter.next());
                }
                
                if(!clientStubs.contains(selectedStub)) {
                    selectedStub = null;
                }
                
                //if platform is non-JSR109, select the JAXRPC static stub type
                //and disable the combobox
                if ((!jsr109OldSupported && !jsr109Supported)
                        || (!jsr109Supported && jsr109OldSupported && jwsdpSupported)) {
                    selectedStub = getJAXRPCClientStub(clientStubs);
                    jCbxClientType.setEnabled(false);
                }
            } else {
                selectedStub = null;
            }
            
            jCbxClientType.setModel(stubModel);
            
            if(selectedStub != null) {
                jCbxClientType.setSelectedItem(selectedStub);
            }
        } finally {
            settingFields = false;
        }
    }
    
    private ClientStubDescriptor getJAXRPCClientStub(List<ClientStubDescriptor> clientStubs){
        for(ClientStubDescriptor clientStub : clientStubs){
            if(clientStub.getName().equals(ClientStubDescriptor.JAXRPC_CLIENT_STUB)){
                return clientStub;
            }
        }
        return null;
    }
    
    public void validatePanel() throws WizardValidationException {
        if (!valid(wizardDescriptor))
            throw new WizardValidationException(this, "", ""); //NOI18N
        
        retrieverFailed = false;
        retriever = null;
        if (((projectType != 0 && !Util.isJavaEE5orHigher(project))
                || (projectType == 0 && jComboBoxJaxVersion.getSelectedItem().equals(ClientWizardProperties.JAX_RPC)))
                && (wsdlSource != WSDL_FROM_FILE)) {
            retriever = new WsdlRetriever(this,
                    wsdlSource==WSDL_FROM_PROJECT?jTxtWsdlProject.getText():jTxtWsdlURL.getText().trim());
            
            Task task = new Task(retriever);
            try {
                task.run();
                // not exceed one minute for retrieving large wsdl file
                task.waitFinished(60000);
            } catch (InterruptedException ex) {
            }
            if (retriever.getState() != WsdlRetriever.STATUS_COMPLETE) {
                retrieverFailed = true;
                throw new WizardValidationException(this, "", ""); //NOI18N
            } else
                wizardDescriptor.putProperty(ClientWizardProperties.WSDL_FILE_PATH, retriever == null ? "" : retriever.getWsdlFileName()); //NOI18N
        }
    }
    
    private ComboBoxModel getPackageModel(Project p) {
        ComboBoxModel result;
        Sources sources = ProjectUtils.getSources(p);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        
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
    
    private Object getPackageItem(String name) {
        Object result = name;
        
        ComboBoxModel model = jCbxPackageName.getModel();
        int max = model.getSize();
        for (int i = 0; i < max; i++) {
            Object item = model.getElementAt(i);
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
    
    private JRadioButton getSelectedRadioButton(int selected) {
        JRadioButton result = jRbnProject;
        
        switch(selected) {
        case WSDL_FROM_PROJECT:
            result = jRbnProject;
            break;
        case WSDL_FROM_FILE:
            result = jRbnFilesystem;
            break;
        case WSDL_FROM_URL:
            result = jRbnUrl;
            break;
        }
        
        return result;
    }
    
    private byte [] getDownloadWsdl() {
        byte [] result = null;
        if(retriever != null && retriever.getState() == WsdlRetriever.STATUS_COMPLETE) {
            result = retriever.getWsdl();
        }
        return result;
    }
    
    private List /*WsdlRetriever.SchemaInfo */ getDownloadedSchemas() {
        List result = null;
        if(retriever != null && retriever.getState() == WsdlRetriever.STATUS_COMPLETE) {
            result = retriever.getSchemas();
        }
        return result;
    }
    
    private String getDownloadUrl() {
        String result = ""; //NOI18N
        
        if(retriever != null) {
            // If we've done a download, save the URL that was actually used, not
            // what the user typed in.
            result = retriever.getWsdlUrl();
        } else {
            // If no download yet, then use what the user has typed.
            if (wsdlSource==WSDL_FROM_URL)
                result = WsdlRetriever.beautifyUrlName(jTxtWsdlURL.getText().trim());
            else if (wsdlSource==WSDL_FROM_PROJECT)
                result = jTxtWsdlProject.getText().trim();
        }
        return result;
    }
    
    boolean valid(final WizardDescriptor wizardDescriptor) {
        Project p = Templates.getProject(wizardDescriptor);
        
        // Project must currently have a target server that supports wscompile.
        /*
        if(!isWsCompileSupported(p)) {
            wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ClientInfo.class, "ERR_WsCompileNotSupportedByTargetServer")); // NOI18N
            return false; // project with web service client support, but no stub types defined.
        }
         */
        
        if(!checkNonJsr109Valid(wizardDescriptor)){
            return false;
        }
        
        // Project selected must support at least one stub type.
        
        // Commented out temporarly (until jax-rpc client support is implemented)
        //        WebServicesClientSupport clientSupport =
        //                WebServicesClientSupport.getWebServicesClientSupport(p.getProjectDirectory());
        //        List clientStubs = (clientSupport != null) ? clientSupport.getStubDescriptors() : null;
        //        if(clientStubs == null || clientStubs.size() == 0) {
        //            wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ClientInfo.class, "ERR_NoStubsDefined")); // NOI18N
        //            return false; // project with web service client support, but no stub types defined.
        //        }
        
        if (jComboBoxJaxVersion.getSelectedItem().equals(ClientWizardProperties.JAX_RPC)) {
            SourceGroup[] sgs = JaxWsClientCreator.getJavaSourceGroups(project);
            //no source root -> there must be at least one source root to create JAX-RPC client
            if (sgs.length <= 0) {
                wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ClientInfo.class,"MSG_MissingSourceRoot")); //NOI18N
                return false;
            }
        }
        
        if(wsdlSource == WSDL_FROM_PROJECT || wsdlSource == WSDL_FROM_URL) {
            String wsdlUrl = (wsdlSource == WSDL_FROM_PROJECT?jTxtWsdlProject.getText().trim():jTxtWsdlURL.getText().trim());
            if(wsdlUrl == null || wsdlUrl.length() == 0) {
                wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ClientInfo.class, "MSG_EnterURL")); // NOI18N
                return false;
            }
            
            if (retrieverFailed && retriever != null) {
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
            }
            
            // url is ok, and file is downloaded if we get here.  Now check generated local filename
            // !PW FIXME what do we want to check it for?  Existence in temp directory?
            
            // Now drop down to do package validation.
        } else if(wsdlSource == WSDL_FROM_FILE) {
            String wsdlFilePath = jTxtLocalFilename.getText().trim();
            
            if(wsdlFilePath == null || wsdlFilePath.length() == 0) {
                wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ClientInfo.class, "MSG_EnterFilename")); // NOI18N
                return false; // unspecified WSDL file
            }
            
            File f = new File(wsdlFilePath);
            if(f == null) {
                wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ClientInfo.class, "ERR_WsdlInvalid")); // NOI18N
                return false; // invalid WSDL file
            }
            
            if(!f.exists()) {
                wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ClientInfo.class, "ERR_WsdlDoesNotExist")); // NOI18N
                return false; // invalid WSDL file
            }
            
            // 50103 - could be done via xml api, but this way should be quicker and suffice the need
            FileReader fr = null;
            LineNumberReader lnReader = null;
            boolean foundWsdlNamespace = false;
            try {
                fr = new FileReader(f);
                lnReader = new LineNumberReader(fr);
                if (lnReader != null) {
                    String line = null;
                    try {
                        line = lnReader.readLine();
                    } catch (IOException ioe) {
                        //ignore
                    }
                    while (line != null) {
                        if (line.indexOf("http://schemas.xmlsoap.org/wsdl/") > 0) { //NOI18N
                            foundWsdlNamespace = true;
                        }
                        if (line.indexOf("REPLACE_WITH_ACTUAL_URL") > 0) { //NOI18N
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
            } finally{
                try{
                    if(lnReader != null){
                        lnReader.close();
                    }
                }catch(IOException e){
                    ErrorManager.getDefault().notify(e);
                }
            }
            
            if (!foundWsdlNamespace) {
                wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ClientInfo.class, "ERR_NotWsdl", f.getName())); // NOI18N
                return false;
            }
            
            // !PW FIXME should also detect if WSDL file has previously been added to
            // this project.  Note that not doing so and overwriting the existing entry
            // is the equivalent of doing an update on it.  Nothing bad will happen
            // unless it turns out the user didn't want to update the service in the
            // first place.
        }
        
        String packageName = getPackageName();
        if(packageName == null || packageName.length() == 0) {
            String jaxwsVersion = (String)this.jComboBoxJaxVersion.getSelectedItem();
            if(projectType == 0 && !jaxwsVersion.equals(ClientWizardProperties.JAX_WS)){
                wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ClientInfo.class, "MSG_EnterJavaPackageName")); // NOI18N
                return false; // unspecified WSDL file
            }
            if(!Util.isJavaEE5orHigher(project) && projectType != 0){
                wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ClientInfo.class, "MSG_EnterJavaPackageName")); // NOI18N
                return false; // unspecified WSDL file
            }
        }
        
        if(packageName != null && packageName.length() > 0 && !JaxWsUtils.isJavaPackage(packageName)) {
            wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ClientInfo.class, "ERR_PackageInvalid")); // NOI18N
            return false; // invalid package name
        }
        
        // Don't allow to create java artifacts to package already used by other service/client
        JaxWsModel jaxWsModel = (JaxWsModel)p.getLookup().lookup(JaxWsModel.class);
        if (packageName != null && packageName.length() > 0 && jaxWsModel!=null) {
            Service[] services = jaxWsModel.getServices();
            for (int i=0;i<services.length;i++) {
                // test service with java artifacts (created from WSDL file)
                if (services[i].getWsdlUrl()!=null && packageName.equals(services[i].getPackageName())) {
                    wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ClientInfo.class, "ERR_PackageUsedForService",services[i].getServiceName()));
                    return false;
                }
                // test service without java artifacts (created from java)
                String pn = getPackageNameFromClass(services[i].getImplementationClass());
                if (services[i].getWsdlUrl()==null && packageName.equals(pn)) {
                    wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ClientInfo.class, "ERR_PackageUsedForService",services[i].getServiceName()));
                    return false;
                }
            }
            Client[] clients = jaxWsModel.getClients();
            for (int i=0;i<clients.length;i++) {
                if (packageName.equals(clients[i].getPackageName())) {
                    wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ClientInfo.class, "ERR_PackageUsedForClient",clients[i].getName()));
                    return false;
                }
            }
            
        }
        
        //warning if the project directory has embedded spaces
        //TODO - Remove this when the jwsdp version that fixes this problem is available
        if(projectHasEmbeddedSpaces()){
            wizardDescriptor.putProperty(PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(ClientInfo.class, "MSG_SPACE_IN_PROJECT_PATH")); // NOI18N
        } else{
            wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, ""); //NOI18N
        }
        
        // Retouche
        //        if (JavaMetamodel.getManager().isScanInProgress()) {
        //            if (!isWaitingForScan) {
        //                isWaitingForScan = true;
        //                RequestProcessor.getDefault().post(new Runnable() {
        //                    public void run() {
        //                        JavaMetamodel.getManager().waitScanFinished();
        //                        isWaitingForScan = false;
        //                        descriptorPanel.fireChangeEvent();
        //                    }
        //                });
        //            }
        //            wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(ClientInfo.class, "MSG_ScanningInProgress")); //NOI18N
        //            return false;
        //        } else
        wizardDescriptor.putProperty("WizardPanel_errorMessage", ""); //NOI18N
        
        return true;
    }
    
    private boolean projectHasEmbeddedSpaces(){
        FileObject projectDir = project.getProjectDirectory();
        File projectDirFile = FileUtil.toFile(projectDir);
        String path = projectDirFile.getAbsolutePath();
        int index = path.indexOf(" ");
        return index != -1;
    }
    
    private J2eePlatform getJ2eePlatform(Project project){
        J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        if(provider != null){
            String serverInstanceID = provider.getServerInstanceID();
            if(serverInstanceID != null && serverInstanceID.length() > 0) {
                return Deployment.getDefault().getJ2eePlatform(serverInstanceID);
            }
        }
        return null;
    }
    
    private boolean isJsr109Supported(Project project){
        J2eePlatform j2eePlatform = getJ2eePlatform(project);
        if(j2eePlatform != null){
            return j2eePlatform.isToolSupported(J2eePlatform.TOOL_JSR109);
        }
        return false;
    }
    
    private boolean isJsr109OldSupported(Project project){
        J2eePlatform j2eePlatform = getJ2eePlatform(project);
        if(j2eePlatform != null){
            return j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSCOMPILE);
        }
        return false;
    }
    
    private boolean isJwsdpSupported(Project project){
        J2eePlatform j2eePlatform = getJ2eePlatform(project);
        if(j2eePlatform != null){
            return j2eePlatform.isToolSupported(J2eePlatform.TOOL_JWSDP);
        }
        return false;
    }
    
    /**
     * If the project the web service client is being created is not on a JSR 109 platform,
     * its Java source level must be at least 1.5
     */
    private boolean checkNonJsr109Valid(WizardDescriptor wizardDescriptor){
        Project project = Templates.getProject(wizardDescriptor);
        boolean jsr109Supported = isJsr109Supported(project);
        boolean jsr109oldSupported = isJsr109OldSupported(project);
        boolean jwsdpSupported = isJwsdpSupported(project);
        if (!jsr109Supported && !jsr109oldSupported ||
                (!jsr109Supported && jsr109oldSupported && jwsdpSupported)) {
            if (Util.isSourceLevel14orLower(project)) {
                wizardDescriptor.putProperty("WizardPanel_errorMessage",
                        NbBundle.getMessage(ClientInfo.class, "ERR_NeedProperSourceLevel")); // NOI18N
                return false;
            }
        }
        return true;
    }
    private boolean isWsCompileSupported(Project p) {
        // Determine if wscompile is supported by the current target server of
        // this project.  Default to true so that the user can still continue, if on
        // their own, in case we have difficulty getting the correct answer.
        boolean result = true;
        
        J2eeModuleProvider provider = (J2eeModuleProvider) p.getLookup().lookup(J2eeModuleProvider.class);
        if(provider != null) {
            String serverInstanceID = provider.getServerInstanceID();
            if(serverInstanceID != null && serverInstanceID.length() > 0) {
                J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstanceID);
                if(!j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSCOMPILE)) {
                    result = false;
                }
            }
        }
        
        return result;
    }
    
    private void wsdlUrlChanged() {
        // Throw away any existing retriever.  New URL means user has to download it again.
        retriever = null;
        
        updateTexts();
    }
    
    private void updateTexts() {
        if(!settingFields) {
            descriptorPanel.fireChangeEvent(); // Notify that the panel changed
        }
    }
    
    private boolean isValidUrl(String urlText) {
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
    
    public void setWsdlDownloadMessage(String m) {
        downloadMsg = m;
        
        // reenable edit control if state indicates download is completed (or failed).
        if(retriever.getState() >= WsdlRetriever.STATUS_COMPLETE) {
            jTxtWsdlURL.setEditable(true);
            jTxtLocalFilename.setText(retriever.getWsdlFileName());
        }
        
        descriptorPanel.fireChangeEvent();
    }
    
    private static class WsdlFileFilter extends FileFilter {
        public boolean accept(File f) {
            String ext = FileUtil.getExtension(f.getName());
            return f.isDirectory() || "wsdl".equalsIgnoreCase(ext) || "asmx".equalsIgnoreCase(ext); // NOI18N
        }
        
        public String getDescription() {
            return NbBundle.getMessage(ClientInfo.class, "LBL_WsdlFilterDescription"); // NOI18N
        }
    }
    
    
    /** Private method to identify wsdl imports and/or the http wsdl/schema imports
     */
    private int resolveImports(File file) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser saxParser = factory.newSAXParser();
            ImportsHandler handler= new ImportsHandler();
            saxParser.parse(new InputSource(new FileInputStream(file)), handler);
            int httpImport = (handler.isHttpImport()?1:0);
            int wsdlImport = (handler.isImportingWsdl()?1:0);
            return httpImport*2+wsdlImport;
        } catch(ParserConfigurationException ex) {
            // Bogus WSDL, return null.
        } catch(SAXException ex) {
            // Bogus WSDL, return null.
        } catch(IOException ex) {
            // Bogus WSDL, return null.
        }
        return 0;
    }
    
    /** this is the handler for local wsdl file scanning for imported wsdl/schema files
     */
    private static class ImportsHandler extends DefaultHandler {
        
        private static final String W3C_WSDL_SCHEMA = "http://schemas.xmlsoap.org/wsdl"; // NOI18N
        private static final String W3C_WSDL_SCHEMA_SLASH = "http://schemas.xmlsoap.org/wsdl/"; // NOI18N
        
        private boolean insideSchema;
        private boolean httpImport;
        private boolean importingWsdl;
        
        public void startElement(String uri, String localname, String qname, Attributes attributes) throws SAXException {
            if(W3C_WSDL_SCHEMA.equals(uri) || W3C_WSDL_SCHEMA_SLASH.equals(uri)) {
                if("types".equals(localname)) { // NOI18N
                    insideSchema=true;
                }
                if("import".equals(localname)) { // NOI18N
                    String wsdlLocation = attributes.getValue("location"); //NOI18N
                    // test if wsdl is imported using http
                    if (wsdlLocation!=null && wsdlLocation.startsWith("http://")) { //NOI18N
                        httpImport=true;
                        importingWsdl=true;
                    }
                }
            }
            
            if(insideSchema && "import".equals(localname)) { // NOI18N
                String schemaLocation = attributes.getValue("schemaLocation"); //NOI18N
                // test if schema is imported using http
                if (schemaLocation!=null && schemaLocation.startsWith("http://")) //NOI18N
                    httpImport=true;
            }
        }
        
        public void endElement(String uri, String localname, String qname) throws SAXException {
            if(W3C_WSDL_SCHEMA.equals(uri) || W3C_WSDL_SCHEMA_SLASH.equals(uri)) {
                if("types".equals(localname)) { // NOI18N
                    insideSchema=false;
                }
            }
        }
        
        boolean isHttpImport() {
            return httpImport;
        }
        
        boolean isImportingWsdl() {
            return importingWsdl;
        }
    }
    
    private String browseProjectServices() {
        JaxWsExplorerPanel explorerPanel = new JaxWsExplorerPanel();
        DialogDescriptor descriptor = new DialogDescriptor(explorerPanel,
                NbBundle.getMessage(ClientInfo.class,"TTL_SelectService")); //NOI18N
        explorerPanel.setDescriptor(descriptor);
        if(DialogDisplayer.getDefault().notify(descriptor).equals(NotifyDescriptor.OK_OPTION)) {
            Node serviceNode = explorerPanel.getSelectedService();
            WsWsdlCookie wsdlCookie = (WsWsdlCookie)serviceNode.getCookie(WsWsdlCookie.class);
            if (wsdlCookie!=null){
                return wsdlCookie.getWsdlURL();
            }
        }
        return null;
    }
    
    private String getPackageNameFromClass(String className) {
        String packageName = null;
        if (className != null) {
            int indexDot = className.lastIndexOf('.');
            if (indexDot < 0) indexDot = 0;
            packageName = className.substring(0, indexDot);
        }
        return packageName;
    }
}
