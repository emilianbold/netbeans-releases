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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitconf.ui.service;

import java.awt.Component;
import java.awt.Dialog;
import javax.swing.undo.UndoManager;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.EndorsingCertificate;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.Generic;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.KerberosAuthentication;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.MessageAuthentication;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.MutualCertificates;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.SAMLAuthorizationOverSSL;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.SAMLHolderOfKey;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.STSIssued;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.STSIssuedCert;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.STSIssuedEndorsing;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.SenderVouches;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.TransportSecurity;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.UsernameAuthentication;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.AdvancedRMPanel;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.KeystorePanel;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.STSConfigServicePanel;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.TargetsPanel;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.TruststorePanel;
import org.netbeans.modules.websvc.wsitconf.util.UndoCounter;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.TransportModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProfilesModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMMSModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMSunModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;

import javax.swing.*;
import org.netbeans.api.project.Project;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac
 */
public class ServicePanel extends SectionInnerPanel {

    private WSDLModel model;
    private Node node;
    private Binding binding;
    private UndoManager undoManager;
    private Project project;
    private SectionView view;
    
    private boolean inSync = false;
    private boolean isFromJava = true;
    
    public ServicePanel(SectionView view, Node node, Project p, Binding binding, UndoManager undoManager) {
        super(view);
        this.model = binding.getModel();
        this.project = p;
        this.node = node;
        this.undoManager = undoManager;
        this.binding = binding;
        initComponents();

        Service service = (Service)node.getLookup().lookup(Service.class);
        if (service != null) {
            String wsdlUrl = service.getWsdlUrl();
            if (wsdlUrl != null) { // WS from WSDL
                isFromJava = false;
            }
        }
        
        mtomChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        rmChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        orderedChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        rmAdvanced.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        securityChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        profileComboLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        profileCombo.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        profileConfigPanel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        targetsButton.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        stsChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        stsConfigButton.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        tcpChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        fiChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        jSeparator1.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        jSeparator2.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        jSeparator3.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        
        addImmediateModifier(mtomChBox);
        addImmediateModifier(rmChBox);
        addImmediateModifier(orderedChBox);
        addImmediateModifier(securityChBox);
        addImmediateModifier(profileCombo);
        addImmediateModifier(stsChBox);
        addImmediateModifier(tcpChBox);
        addImmediateModifier(fiChBox);

        inSync = true;
        profileCombo.removeAllItems();
        profileCombo.addItem(ComboConstants.PROF_TRANSPORT);
        profileCombo.addItem(ComboConstants.PROF_MSGAUTHSSL);
        profileCombo.addItem(ComboConstants.PROF_SAMLSSL);
        profileCombo.addItem(ComboConstants.PROF_USERNAME);
        profileCombo.addItem(ComboConstants.PROF_MUTUALCERT);
        profileCombo.addItem(ComboConstants.PROF_ENDORSCERT);
        profileCombo.addItem(ComboConstants.PROF_SAMLSENDER);
        profileCombo.addItem(ComboConstants.PROF_SAMLHOLDER);
//        profileCombo.addItem(ComboConstants.PROF_KERBEROS);
        profileCombo.addItem(ComboConstants.PROF_STSISSUED);
        profileCombo.addItem(ComboConstants.PROF_STSISSUEDCERT);
        profileCombo.addItem(ComboConstants.PROF_STSISSUEDENDORSE);
//        profileCombo.addItem(ComboConstants.PROF_GENERIC);

        inSync = false;
        sync();
    }

    private void sync() {
        inSync = true;
        
        boolean mtomEnabled = TransportModelHelper.isMtomEnabled(binding);
        setChBox(mtomChBox, mtomEnabled);
        
        boolean fiEnabled = TransportModelHelper.isFIEnabled(binding);
        setChBox(fiChBox, fiEnabled);

        boolean tcpEnabled = TransportModelHelper.isTCPEnabled(binding);
        setChBox(tcpChBox, tcpEnabled);

        boolean rmEnabled = RMModelHelper.isRMEnabled(binding);
        setChBox(rmChBox, rmEnabled);        
        setChBox(orderedChBox, RMSunModelHelper.isOrderedEnabled(binding));
        
        boolean securityEnabled = SecurityPolicyModelHelper.isSecurityEnabled(binding);
        setChBox(securityChBox, securityEnabled);
        if (securityEnabled) {
            setSecurityProfile(ProfilesModelHelper.getSecurityProfile(binding));
        } else {
            setSecurityProfile(ComboConstants.PROF_USERNAME);
        }

        enableDisable();
        inSync = false;
    }

//    private void refreshPanels() {
//        updateLayout();
//        revalidate();
//        repaint();
//    }

    @Override
    public void setValue(javax.swing.JComponent source, Object value) {
        if (inSync) return; 
        if (source.equals(rmChBox)) {
            if (rmChBox.isSelected()) {
                if (!(RMModelHelper.isRMEnabled(binding))) {
                    RMModelHelper.enableRM(binding);
                }
            } else {
                if (RMModelHelper.isRMEnabled(binding)) {
                    RMModelHelper.disableRM(binding);
                    RMMSModelHelper.disableFlowControl(binding);
                    RMSunModelHelper.disableOrdered(binding);
                    RMModelHelper.setInactivityTimeout(binding, null);
                    RMMSModelHelper.setMaxReceiveBufferSize(binding, null);
                }
            }
        }

        if (source.equals(orderedChBox)) {
            if (orderedChBox.isSelected()) {
                if (!(RMSunModelHelper.isOrderedEnabled(binding))) {
                    RMSunModelHelper.enableOrdered(binding);
                }
            } else {
                if (RMSunModelHelper.isOrderedEnabled(binding)) {
                    RMSunModelHelper.disableOrdered(binding);
                }
            }
        }

        if (source.equals(mtomChBox)) {
            if (mtomChBox.isSelected()) {
                if (!(TransportModelHelper.isMtomEnabled(binding))) {
                    TransportModelHelper.enableMtom(binding);
                }
            } else {
                if (TransportModelHelper.isMtomEnabled(binding)) {
                    TransportModelHelper.disableMtom(binding);
                }
            }
        }

        if (source.equals(fiChBox)) {
            if (fiChBox.isSelected()) {
                if (!(TransportModelHelper.isFIEnabled(binding))) {
                    TransportModelHelper.enableFI(binding, true);
                }
            } else {
                if (TransportModelHelper.isFIEnabled(binding)) {
                    TransportModelHelper.enableFI(binding, false);
                }
            }
        }
        
        if (source.equals(tcpChBox)) {
            if (tcpChBox.isSelected()) {
                if (!(TransportModelHelper.isTCPEnabled(binding))) {
                    TransportModelHelper.enableTCP(binding, true);
                }
            } else {
                if (TransportModelHelper.isTCPEnabled(binding)) {
                    TransportModelHelper.enableTCP(binding, false);
                }
            }
        }
        
        if (source.equals(securityChBox)) {
            if (securityChBox.isSelected()) {
                profileCombo.setSelectedItem((String) profileCombo.getSelectedItem());
            } else {
                SecurityPolicyModelHelper.disableSecurity(binding);
            }
        }

        if (source.equals(profileCombo)) {
            ProfilesModelHelper.setSecurityProfile(binding, (String) profileCombo.getSelectedItem());
//            this.remove(profileConfigPanel);
//            profileConfigPanel = getProfilePanel((String)profileCombo.getSelectedItem());
//            profileConfigPanel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
//            refreshPanels();
        }
    
        enableDisable();
    }

    public Boolean getMtom() {
        if (mtomChBox.isSelected()) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
    
    public Boolean getRM() {
        if (rmChBox.isSelected()) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public Boolean getOrdered() {
        if (orderedChBox.isSelected()) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public Boolean getSecurity() {
        if (securityChBox.isSelected()) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private void setChBox(JCheckBox chBox, Boolean enable) {
        if (enable == null) {
            chBox.setSelected(false);
        } else {
            chBox.setSelected(enable);
        }
    }
    
    // SECURITY PROFILE
    private void setSecurityProfile(String profile) {
        this.profileCombo.setSelectedItem(profile);
        this.profileConfigPanel = getProfilePanel(profile);
    }
    
    private JPanel getProfilePanel(String profile) {
        if (ComboConstants.PROF_GENERIC.equals(profile)) return new Generic(binding);
        if (ComboConstants.PROF_TRANSPORT.equals(profile)) return new TransportSecurity(binding);
        if (ComboConstants.PROF_MSGAUTHSSL.equals(profile)) return new MessageAuthentication(binding);
        if (ComboConstants.PROF_SAMLSSL.equals(profile)) return new SAMLAuthorizationOverSSL(binding);
        if (ComboConstants.PROF_USERNAME.equals(profile)) return new UsernameAuthentication(binding);
        if (ComboConstants.PROF_MUTUALCERT.equals(profile)) return new MutualCertificates(binding);
        if (ComboConstants.PROF_ENDORSCERT.equals(profile)) return new EndorsingCertificate(binding);
        if (ComboConstants.PROF_SAMLSENDER.equals(profile)) return new SenderVouches(binding);
        if (ComboConstants.PROF_SAMLHOLDER.equals(profile)) return new SAMLHolderOfKey(binding);
        if (ComboConstants.PROF_KERBEROS.equals(profile)) return new KerberosAuthentication(binding);
        if (ComboConstants.PROF_STSISSUED.equals(profile)) return new STSIssued(binding);
        if (ComboConstants.PROF_STSISSUEDCERT.equals(profile)) return new STSIssuedCert(binding);
        if (ComboConstants.PROF_STSISSUEDENDORSE.equals(profile)) return new STSIssuedEndorsing(binding);
        return null;
    }

    @Override
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        SectionView view = getSectionView();
        enableDisable();
        if (view != null) {
            view.getErrorPanel().clearError();
        }
    }

    @Override
    public void rollbackValue(javax.swing.text.JTextComponent source) {
    }
    
    @Override
    protected void endUIChange() { }

    public void linkButtonPressed(Object ddBean, String ddProperty) { }

    public javax.swing.JComponent getErrorComponent(String errorId) {
        return new JButton();
    }
    
    private void enableDisable() {
        boolean relSelected = rmChBox.isSelected();
        orderedChBox.setEnabled(relSelected);
        rmAdvanced.setEnabled(relSelected);

        boolean secSelected = securityChBox.isSelected();
        profileComboLabel.setEnabled(secSelected);
        profileCombo.setEnabled(secSelected);
        profileConfigPanel.setEnabled(secSelected);
        profConfigButton.setEnabled(secSelected);
        Component[] comps = profileConfigPanel.getComponents();
        for (Component comp : comps) {
            comp.setEnabled(secSelected);
        }
        targetsButton.setEnabled(secSelected);
        stsChBox.setEnabled(secSelected);

        boolean stsSelected = stsChBox.isSelected();
        stsConfigButton.setEnabled(stsSelected);
        
        boolean storeConfigRequired = isStoreConfigRequired();
        keyButton.setEnabled(storeConfigRequired && secSelected);
        trustButton.setEnabled(storeConfigRequired && secSelected);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        mtomChBox = new javax.swing.JCheckBox();
        rmChBox = new javax.swing.JCheckBox();
        securityChBox = new javax.swing.JCheckBox();
        orderedChBox = new javax.swing.JCheckBox();
        profileComboLabel = new javax.swing.JLabel();
        profileCombo = new javax.swing.JComboBox();
        rmAdvanced = new javax.swing.JButton();
        profileConfigPanel = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        targetsButton = new javax.swing.JButton();
        stsChBox = new javax.swing.JCheckBox();
        tcpChBox = new javax.swing.JCheckBox();
        jSeparator3 = new javax.swing.JSeparator();
        keyButton = new javax.swing.JButton();
        trustButton = new javax.swing.JButton();
        stsConfigButton = new javax.swing.JButton();
        profConfigButton = new javax.swing.JButton();
        fiChBox = new javax.swing.JCheckBox();

        mtomChBox.setText(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_mtomChBox")); // NOI18N
        mtomChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mtomChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        mtomChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mtomChBoxActionPerformed(evt);
            }
        });

        rmChBox.setText(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_rmChBox")); // NOI18N
        rmChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rmChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rmChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rmChBoxActionPerformed(evt);
            }
        });

        securityChBox.setText(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_securityChBox")); // NOI18N
        securityChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        securityChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        securityChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                securityChBoxActionPerformed(evt);
            }
        });

        orderedChBox.setText(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_OrderedChBox")); // NOI18N
        orderedChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        orderedChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        profileComboLabel.setLabelFor(profileCombo);
        profileComboLabel.setText(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_profileComboLabel")); // NOI18N

        profileCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "SAML Sender Vouches With Certificates", "Anonymous with Bilateral Certificates" }));

        rmAdvanced.setText(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_Advanced")); // NOI18N
        rmAdvanced.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rmAdvancedActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout profileConfigPanelLayout = new org.jdesktop.layout.GroupLayout(profileConfigPanel);
        profileConfigPanel.setLayout(profileConfigPanelLayout);
        profileConfigPanelLayout.setHorizontalGroup(
            profileConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 364, Short.MAX_VALUE)
        );
        profileConfigPanelLayout.setVerticalGroup(
            profileConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );

        targetsButton.setText(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_SignEncrypt")); // NOI18N
        targetsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                targetsButtonActionPerformed(evt);
            }
        });

        stsChBox.setText(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_stsChBox")); // NOI18N
        stsChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        stsChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        tcpChBox.setText(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_tcpChBox")); // NOI18N
        tcpChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tcpChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        keyButton.setText(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_keystoreButton")); // NOI18N
        keyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyButtonActionPerformed(evt);
            }
        });

        trustButton.setText(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_truststoreButton")); // NOI18N
        trustButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trustButtonActionPerformed(evt);
            }
        });

        stsConfigButton.setText(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_stsConfigButton")); // NOI18N
        stsConfigButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stsConfigButtonActionPerformed(evt);
            }
        });

        profConfigButton.setText(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_keyConfigButton")); // NOI18N
        profConfigButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profConfigButtonActionPerformed(evt);
            }
        });

        fiChBox.setText(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_fiChBox")); // NOI18N
        fiChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        fiChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jSeparator3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 405, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(stsChBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(stsConfigButton))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(tcpChBox))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator1)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, mtomChBox)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, rmChBox)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(17, 17, 17)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(rmAdvanced)
                                    .add(orderedChBox))
                                .add(79, 79, 79))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, securityChBox)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(17, 17, 17)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(profileComboLabel)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(profileCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 228, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(6, 6, 6)
                                        .add(profConfigButton))
                                    .add(layout.createSequentialGroup()
                                        .add(targetsButton)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(keyButton)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(trustButton))))))
                    .add(layout.createSequentialGroup()
                        .add(37, 37, 37)
                        .add(profileConfigPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(fiChBox)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(mtomChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rmChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(orderedChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rmAdvanced)
                .add(8, 8, 8)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(securityChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(profileComboLabel)
                    .add(profileCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(profConfigButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(profileConfigPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(targetsButton)
                    .add(keyButton)
                    .add(trustButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(stsChBox)
                    .add(stsConfigButton))
                .add(11, 11, 11)
                .add(jSeparator3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tcpChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fiChBox)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void profConfigButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profConfigButtonActionPerformed
        UndoCounter undoCounter = new UndoCounter();
        model.addUndoableEditListener(undoCounter);

        JPanel profConfigPanel = getProfilePanel((String)profileCombo.getSelectedItem());
        DialogDescriptor dlgDesc = new DialogDescriptor(profConfigPanel, 
                NbBundle.getMessage(ServicePanel.class, "LBL_AdvancedProfile_Title")); //NOI18N
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);

        dlg.setVisible(true); 
        if (dlgDesc.getValue() == dlgDesc.CANCEL_OPTION) {
            for (int i=0; i<undoCounter.getCounter();i++) {
                if (undoManager.canUndo()) {
                    undoManager.undo();
                }
            }
        }
        
        model.removeUndoableEditListener(undoCounter);
    }//GEN-LAST:event_profConfigButtonActionPerformed

    private void stsConfigButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stsConfigButtonActionPerformed
        UndoCounter undoCounter = new UndoCounter();
        model.addUndoableEditListener(undoCounter);

        STSConfigServicePanel stsConfigPanel = new STSConfigServicePanel(model, node, binding); //NOI18N
        DialogDescriptor dlgDesc = new DialogDescriptor(stsConfigPanel, 
                NbBundle.getMessage(ServicePanel.class, "LBL_STSConfig_Panel_Title")); //NOI18N
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);
        
        dlg.setVisible(true); 
        if (dlgDesc.getValue() == dlgDesc.CANCEL_OPTION) {
            for (int i=0; i<undoCounter.getCounter();i++) {
                if (undoManager.canUndo()) {
                    undoManager.undo();
                }
            }
        }
        
        model.removeUndoableEditListener(undoCounter);
    }//GEN-LAST:event_stsConfigButtonActionPerformed

    private void trustButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trustButtonActionPerformed
        UndoCounter undoCounter = new UndoCounter();
        model.addUndoableEditListener(undoCounter);

        TruststorePanel storePanel = new TruststorePanel(binding); //NOI18N
        DialogDescriptor dlgDesc = new DialogDescriptor(storePanel, 
                NbBundle.getMessage(ServicePanel.class, "LBL_Truststore_Panel_Title")); //NOI18N
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);
        
        dlg.setVisible(true); 
        if (dlgDesc.getValue() == dlgDesc.CANCEL_OPTION) {
            for (int i=0; i<undoCounter.getCounter();i++) {
                if (undoManager.canUndo()) {
                    undoManager.undo();
                }
            }
        }
        
        model.removeUndoableEditListener(undoCounter);
    }//GEN-LAST:event_trustButtonActionPerformed

    private void keyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyButtonActionPerformed
        UndoCounter undoCounter = new UndoCounter();
        model.addUndoableEditListener(undoCounter);

        KeystorePanel storePanel = new KeystorePanel(binding); //NOI18N
        DialogDescriptor dlgDesc = new DialogDescriptor(storePanel, 
                NbBundle.getMessage(ServicePanel.class, "LBL_Keystore_Panel_Title")); //NOI18N
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);
        
        dlg.setVisible(true); 
        if (dlgDesc.getValue() == dlgDesc.CANCEL_OPTION) {
            for (int i=0; i<undoCounter.getCounter();i++) {
                if (undoManager.canUndo()) {
                    undoManager.undo();
                }
            }
        }
        
        model.removeUndoableEditListener(undoCounter);
    }//GEN-LAST:event_keyButtonActionPerformed

    private void targetsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_targetsButtonActionPerformed
        UndoCounter undoCounter = new UndoCounter();
        model.addUndoableEditListener(undoCounter);

        TargetsPanel targetsPanel = new TargetsPanel(binding); //NOI18N
        DialogDescriptor dlgDesc = new DialogDescriptor(targetsPanel, 
                NbBundle.getMessage(ServicePanel.class, "LBL_Targets_Panel_Title")); //NOI18N
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);
        
        dlg.setVisible(true); 
        if (dlgDesc.getValue() == dlgDesc.CANCEL_OPTION) {
            for (int i=0; i<undoCounter.getCounter();i++) {
                if (undoManager.canUndo()) {
                    undoManager.undo();
                }
            }
        } else {
            SecurityPolicyModelHelper.setTargets(binding, targetsPanel.getTargetsModel());
        }
        
        model.removeUndoableEditListener(undoCounter);
    }//GEN-LAST:event_targetsButtonActionPerformed

    private void rmAdvancedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rmAdvancedActionPerformed
        UndoCounter undoCounter = new UndoCounter();
        model.addUndoableEditListener(undoCounter);

        AdvancedRMPanel advancedRMPanel = new AdvancedRMPanel(binding); //NOI18N
        DialogDescriptor dlgDesc = new DialogDescriptor(advancedRMPanel, 
                NbBundle.getMessage(ServicePanel.class, "LBL_AdvancedRM_Title")); //NOI18N
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);
        
        dlg.setVisible(true); 
        if (dlgDesc.getValue() == dlgDesc.CANCEL_OPTION) {
            for (int i=0; i<undoCounter.getCounter();i++) {
                if (undoManager.canUndo()) {
                    undoManager.undo();
                }
            }
        }
        
        model.removeUndoableEditListener(undoCounter);
    }//GEN-LAST:event_rmAdvancedActionPerformed

    private void rmChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rmChBoxActionPerformed
        enableDisable();
    }//GEN-LAST:event_rmChBoxActionPerformed

    private void securityChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_securityChBoxActionPerformed
        enableDisable();
    }//GEN-LAST:event_securityChBoxActionPerformed

    private void mtomChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mtomChBoxActionPerformed
        enableDisable();
    }//GEN-LAST:event_mtomChBoxActionPerformed

    private boolean isStoreConfigRequired() {
        if (project != null) {
            J2eeModuleProvider mp = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
            String id = mp.getServerID();
            String instid = mp.getServerInstanceID();
            if ((instid != null) && (instid.toLowerCase().contains("sun:appserver"))) {     //NOI18N
                return false;
            }
        }
        return true;
    }
            
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox fiChBox;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JButton keyButton;
    private javax.swing.JCheckBox mtomChBox;
    private javax.swing.JCheckBox orderedChBox;
    private javax.swing.JButton profConfigButton;
    private javax.swing.JComboBox profileCombo;
    private javax.swing.JLabel profileComboLabel;
    private javax.swing.JPanel profileConfigPanel;
    private javax.swing.JButton rmAdvanced;
    private javax.swing.JCheckBox rmChBox;
    private javax.swing.JCheckBox securityChBox;
    private javax.swing.JCheckBox stsChBox;
    private javax.swing.JButton stsConfigButton;
    private javax.swing.JButton targetsButton;
    private javax.swing.JCheckBox tcpChBox;
    private javax.swing.JButton trustButton;
    // End of variables declaration//GEN-END:variables
    
}
