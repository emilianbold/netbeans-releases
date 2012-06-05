/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.web.client.rest.wizard;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.List;

import java.awt.Component;
import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URL;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JFileChooser;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;


import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

import org.netbeans.api.options.OptionsDisplayer;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.websvc.rest.client.RESTExplorerPanel;
import org.netbeans.modules.websvc.rest.client.RESTResourcesPanel;
import org.netbeans.modules.websvc.rest.client.SaasExplorerPanel;
import org.netbeans.spi.project.ui.templates.support.Templates;

/*import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.templates.support.Templates;

import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;

import org.netbeans.modules.websvc.core.ProjectInfo;
import org.netbeans.modules.websvc.core.WsWsdlCookie;
import org.netbeans.modules.websvc.saas.model.Saas.State;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.netbeans.modules.websvc.saas.model.WsdlSaas;
import org.netbeans.modules.xml.retriever.catalog.Utilities;*/
import org.openide.NotifyDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;

/**
 *
 * @author ads
 */
public final class RestPanelVisual extends JPanel  {

    private static final String PROP_ERROR_MESSAGE = WizardDescriptor.PROP_ERROR_MESSAGE;
    private static final String PROP_INFO_MESSAGE = WizardDescriptor.PROP_INFO_MESSAGE;
    
    private static final int REST_FROM_PROJECT = 0;
    private static final int REST_FROM_FILE = 1;
    private static final int REST_FROM_URL = 2;
    private static final int REST_FROM_SAAS =3;
            
   
    public RestPanelVisual(RestPanel panel) {
        this.wadlSource = REST_FROM_PROJECT;
        myPanel = panel;
        initComponents();
        initUserComponents();
        enableRestSourceFields();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnGrpWsdlSource = new javax.swing.ButtonGroup();
        jLblChooseSource = new javax.swing.JLabel();
        jRbnFilesystem = new javax.swing.JRadioButton();
        jTxtRestProject = new javax.swing.JTextField();
        jBtnBrowse = new javax.swing.JButton();
        jRbnProject = new javax.swing.JRadioButton();
        jTxtWadlURL = new javax.swing.JTextField();
        jBtnProxy = new javax.swing.JButton();
        jTxtLocalFilename = new javax.swing.JTextField();
        jLblProjectName = new javax.swing.JLabel();
        jRbnUrl = new javax.swing.JRadioButton();
        jBtnBrowse1 = new javax.swing.JButton();
        saasWs = new javax.swing.JRadioButton();
        saasTextField = new javax.swing.JTextField();
        saasBrowse = new javax.swing.JButton();
        backboneCheckBox = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(jLblChooseSource, NbBundle.getMessage(RestPanelVisual.class, "LBL_WadlSource")); // NOI18N

        btnGrpWsdlSource.add(jRbnFilesystem);
        org.openide.awt.Mnemonics.setLocalizedText(jRbnFilesystem, org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "LBL_LocalFile")); // NOI18N
        jRbnFilesystem.setActionCommand("Local file");
        jRbnFilesystem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRbnFilesystemActionPerformed(evt);
            }
        });

        jTxtRestProject.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(jBtnBrowse, org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "LBL_Browse")); // NOI18N
        jBtnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnBrowseActionPerformed(evt);
            }
        });

        btnGrpWsdlSource.add(jRbnProject);
        jRbnProject.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jRbnProject, NbBundle.getMessage(RestPanelVisual.class, "LBL_Project")); // NOI18N
        jRbnProject.setFocusable(false);
        jRbnProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRbnProjectActionPerformed(evt);
            }
        });

        jTxtWadlURL.setColumns(30);

        org.openide.awt.Mnemonics.setLocalizedText(jBtnProxy, org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "LBL_ProxySettings")); // NOI18N
        jBtnProxy.setActionCommand("Set Proxy");
        jBtnProxy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnProxyActionPerformed(evt);
            }
        });

        btnGrpWsdlSource.add(jRbnUrl);
        org.openide.awt.Mnemonics.setLocalizedText(jRbnUrl, org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "LBL_WadlUrl")); // NOI18N
        jRbnUrl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRbnUrlActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jBtnBrowse1, org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "LBL_BrowseProject")); // NOI18N
        jBtnBrowse1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnBrowse1ActionPerformed(evt);
            }
        });

        btnGrpWsdlSource.add(saasWs);
        org.openide.awt.Mnemonics.setLocalizedText(saasWs, org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "LBL_Saas")); // NOI18N
        saasWs.setActionCommand("saas");
        saasWs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saasSelected(evt);
            }
        });

        saasTextField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(saasBrowse, org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "LBL_SaasBrowse")); // NOI18N
        saasBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saasBrowse(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(backboneCheckBox, org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "LBL_Backbone")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLblChooseSource, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLblProjectName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jRbnUrl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(saasWs, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTxtWadlURL, javax.swing.GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE)
                            .addComponent(saasTextField))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jBtnProxy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(saasBrowse, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jRbnProject, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jRbnFilesystem, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTxtRestProject)
                            .addComponent(jTxtLocalFilename))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jBtnBrowse, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
                            .addComponent(jBtnBrowse1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(backboneCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLblChooseSource)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLblProjectName)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jRbnProject)
                        .addComponent(jTxtRestProject, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jBtnBrowse1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRbnFilesystem)
                    .addComponent(jTxtLocalFilename, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jBtnBrowse))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jBtnProxy)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jRbnUrl)
                        .addComponent(jTxtWadlURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(saasTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(saasBrowse))
                    .addComponent(saasWs))
                .addGap(18, 18, 18)
                .addComponent(backboneCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLblChooseSource.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "ACSN_WadlSource")); // NOI18N
        jLblChooseSource.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "ACSD_WadlSource")); // NOI18N
        jRbnFilesystem.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "ACSN_LocalFile")); // NOI18N
        jRbnFilesystem.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "ACSD_LocalFile")); // NOI18N
        jTxtRestProject.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "ACSN_ProjectPath")); // NOI18N
        jTxtRestProject.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "ACSD_ProjectPath")); // NOI18N
        jBtnBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(RestPanelVisual.class).getString("A11Y_BrowseLocalFile")); // NOI18N
        jRbnProject.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "ACSN_Project")); // NOI18N
        jRbnProject.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "ACSD_Project")); // NOI18N
        jTxtWadlURL.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "ACSN_WadlUrl")); // NOI18N
        jTxtWadlURL.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "ACSD_WadlUrl")); // NOI18N
        jBtnProxy.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "A11Y_ProxySettings")); // NOI18N
        jTxtLocalFilename.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(RestPanelVisual.class).getString("ACSN_LocalFilePath")); // NOI18N
        jTxtLocalFilename.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "ACSD_LocalFilePath")); // NOI18N
        jRbnUrl.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "ACSN_WadlUrlRadio")); // NOI18N
        jRbnUrl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(RestPanelVisual.class).getString("ACSD_WadlUrlRadio")); // NOI18N
        jBtnBrowse1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(RestPanelVisual.class).getString("A11Y_BrowseWSDLProject")); // NOI18N
        saasWs.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "ACSN_Saas")); // NOI18N
        saasWs.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "ACSD_Saas")); // NOI18N
        saasTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "ACSN_SaasTextField")); // NOI18N
        saasTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "ACSD_SaasTextField")); // NOI18N
        saasBrowse.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "ACSN_Browse")); // NOI18N
        saasBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "ACSD_Browse")); // NOI18N
        backboneCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "ACSN_Backbone")); // NOI18N
        backboneCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "ACSD_Backbone")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RestPanelVisual.class, "LBL_RestSource")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void jBtnBrowse1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnBrowse1ActionPerformed
        browseProjectServices();
        myPanel.fireChangeEvent();
        
    }//GEN-LAST:event_jBtnBrowse1ActionPerformed
    
    private void browseProjectServices() {
        RESTExplorerPanel panel = new RESTExplorerPanel();
        DialogDescriptor descriptor = new DialogDescriptor(panel,
                NbBundle.getMessage(RestPanelVisual.class,"TTL_RESTResources")); //NOI18N
        panel.setDescriptor(descriptor);
        if (DialogDisplayer.getDefault().notify(descriptor).equals(NotifyDescriptor.OK_OPTION)) {
            myRestNode = panel.getSelectedService();
            Project project = myRestNode.getLookup().lookup(Project.class);
            String projectName = project.getLookup().lookup(ProjectInformation.class).getDisplayName();
            jTxtRestProject.setText("<"+projectName+">/"+myRestNode.getDisplayName());
        }
    }

    private void jRbnUrlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRbnUrlActionPerformed
        // TODO add your handling code here:
        wadlSource = REST_FROM_URL;
        enableRestSourceFields();
        myPanel.fireChangeEvent();
    }//GEN-LAST:event_jRbnUrlActionPerformed
    
    private void jBtnProxyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnProxyActionPerformed
        OptionsDisplayer.getDefault().open("General");//NOI18N
        wadlUrlChanged();
    }//GEN-LAST:event_jBtnProxyActionPerformed
    
	private void jBtnBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnBrowseActionPerformed
            JFileChooser chooser = new JFileChooser(myPreviousDirectory);
            chooser.setMultiSelectionEnabled(false);
            chooser.setAcceptAllFileFilterUsed(true);
            chooser.addChoosableFileFilter(WSDL_FILE_FILTER);
            chooser.setFileFilter(WSDL_FILE_FILTER);
            if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File wadlFile = chooser.getSelectedFile();
                jTxtLocalFilename.setText(wadlFile.getAbsolutePath());
                myPreviousDirectory = wadlFile.getPath();
            } 
	}//GEN-LAST:event_jBtnBrowseActionPerformed
        
    private void jRbnFilesystemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRbnFilesystemActionPerformed
        //        System.out.println("get from filesystem selected.");
        wadlSource = REST_FROM_FILE;
        enableRestSourceFields();
        myPanel.fireChangeEvent();
    }//GEN-LAST:event_jRbnFilesystemActionPerformed
    
    private void jRbnProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRbnProjectActionPerformed
        //        System.out.println("get from url selected.");
        wadlSource = REST_FROM_PROJECT;
        enableRestSourceFields();
        myPanel.fireChangeEvent();
    }//GEN-LAST:event_jRbnProjectActionPerformed

private void saasSelected(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saasSelected
        wadlSource = REST_FROM_SAAS;
        enableRestSourceFields();
        myPanel.fireChangeEvent();
}//GEN-LAST:event_saasSelected

private void saasBrowse(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saasBrowse
    SaasExplorerPanel explorerPanel = new SaasExplorerPanel();
    DialogDescriptor desc = new DialogDescriptor(explorerPanel,
            NbBundle.getMessage(RESTResourcesPanel.class,"TTL_RESTResources")); //NOI18N
    explorerPanel.setDescriptor(desc);
    if (DialogDisplayer.getDefault().notify(desc).equals(NotifyDescriptor.OK_OPTION)) {
        myRestNode = explorerPanel.getSelectedService();
        saasTextField.setText(RESTResourcesPanel.getSaasResourceName(myRestNode));
    }
}//GEN-LAST:event_saasBrowse


    
    private void enableRestSourceFields() {
        // project related fields
        jTxtRestProject.setEnabled(wadlSource == REST_FROM_PROJECT);
        jBtnBrowse1.setEnabled(wadlSource == REST_FROM_PROJECT);
        
        // file systam related fields
        jTxtLocalFilename.setEnabled(wadlSource == REST_FROM_FILE);
        jBtnBrowse.setEnabled(wadlSource == REST_FROM_FILE);
        
        // service related fields
        jTxtWadlURL.setEnabled(wadlSource == REST_FROM_URL);
        
        saasTextField.setEnabled( wadlSource == REST_FROM_SAAS);
        saasBrowse.setEnabled( wadlSource == REST_FROM_SAAS );
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox backboneCheckBox;
    private javax.swing.ButtonGroup btnGrpWsdlSource;
    private javax.swing.JButton jBtnBrowse;
    private javax.swing.JButton jBtnBrowse1;
    private javax.swing.JButton jBtnProxy;
    private javax.swing.JLabel jLblChooseSource;
    private javax.swing.JLabel jLblProjectName;
    private javax.swing.JRadioButton jRbnFilesystem;
    private javax.swing.JRadioButton jRbnProject;
    private javax.swing.JRadioButton jRbnUrl;
    private javax.swing.JTextField jTxtLocalFilename;
    private javax.swing.JTextField jTxtRestProject;
    private javax.swing.JTextField jTxtWadlURL;
    private javax.swing.JButton saasBrowse;
    private javax.swing.JTextField saasTextField;
    private javax.swing.JRadioButton saasWs;
    // End of variables declaration//GEN-END:variables
    
    private void initUserComponents() {
        setName(NbBundle.getMessage(RestPanelVisual.class, "TITLE_RestClientWizard")); // NOI18N
        
        // Register listener on the textFields to make the automatic updates
        jTxtWadlURL.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                wadlUrlChanged();
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                wadlUrlChanged();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                wadlUrlChanged();
            }
        });
        jTxtLocalFilename.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateTexts();
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateTexts();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateTexts();
            }
        });  
    }
    
    void store(WizardDescriptor descriptor) {
          descriptor.putProperty(RestPanel.REST_SOURCE, wadlSource);
          if ( wadlSource == REST_FROM_FILE){
              descriptor.putProperty(RestPanel.WADL_PATH, jTxtLocalFilename.getText());
          }
          else if ( wadlSource == REST_FROM_URL ){
              descriptor.putProperty(RestPanel.WADL_PATH, jTxtWadlURL.getText());
              /* to do: retrieve WADL in the  validatePanel() method
               * along with schema files and put downloaded files in the stored properties   
               */
          }
          if ( backboneCheckBox.isVisible() && backboneCheckBox.isSelected() ){
              descriptor.putProperty(RestPanel.BACKBONE, Boolean.TRUE);
          }
    }
    
    void read(WizardDescriptor wizardDescriptor) {
        Project project = Templates.getProject(wizardDescriptor);
        FileObject projectDirectory = project.getProjectDirectory();
        FileObject libs = projectDirectory.getFileObject("js/libs");
        boolean backboneExists = false; 
        if ( libs != null ){
            FileObject[] children = libs.getChildren();
            for (FileObject child : children) {
                String name = child.getName();
                if ( name.startsWith( "backbone.js-")){
                    backboneExists = true;
                }
            }
        }
        if ( backboneExists ){
            Mutex.EVENT.readAccess( new Runnable() {
                @Override
                public void run() {
                    backboneCheckBox.setVisible(false);                    
                }
            });
        }
    }

    
    void validatePanel(WizardDescriptor descriptor) throws WizardValidationException {
        if (!valid(descriptor)){
            throw new WizardValidationException(this, "", ""); //NOI18N
        }
        /*
         * TODO : retrieve WADL file along with schemas 
         */
    }
    
    boolean valid(final WizardDescriptor wizardDescriptor) {
        
        return true;
    }
    
    Node getRestNode(){
        return myRestNode;
    }

    
    private JRadioButton getSelectedRadioButton(int selected) {
        JRadioButton result = jRbnProject;
        
        switch(selected) {
            case REST_FROM_PROJECT:
                result = jRbnProject;
                break;
            case REST_FROM_FILE:
                result = jRbnFilesystem;
                break;
            case REST_FROM_URL:
                result = jRbnUrl;
                break;
            case REST_FROM_SAAS:
                result = saasWs;
                break;
        }
        
        return result;
    }
    
    private void wadlUrlChanged() {
    }
    
    private void updateTexts() {
        myPanel.fireChangeEvent();
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
    
    private static class WadlFileFilter extends FileFilter {
        @Override
        public boolean accept(File f) {
            String ext = FileUtil.getExtension(f.getName());
            return f.isDirectory() || "wadl".equalsIgnoreCase(ext) ; // NOI18N
        }
        
        @Override
        public String getDescription() {
            return NbBundle.getMessage(RestPanelVisual.class, "LBL_WadlFilterDescription"); // NOI18N
        }
    }
    
    private int wadlSource;

    private String downloadMsg;
    private boolean retrieverFailed = false;
    private int projectType;
    private RestPanel myPanel;
    private Node myRestNode;
    private String myPreviousDirectory;
    private static final FileFilter WSDL_FILE_FILTER = new WadlFileFilter();
}
