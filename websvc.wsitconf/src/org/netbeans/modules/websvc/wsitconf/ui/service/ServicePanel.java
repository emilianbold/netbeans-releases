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

import java.awt.Color;
import java.awt.Dialog;
import java.util.Set;
import javax.swing.undo.UndoManager;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityProfile;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityProfileRegistry;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.AdvancedRMPanel;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.KeystorePanel;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.STSConfigServicePanel;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.TruststorePanel;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.ValidatorsPanel;
import org.netbeans.modules.websvc.wsitconf.util.UndoCounter;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
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
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import javax.swing.*;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityCheckerRegistry;
import org.netbeans.modules.websvc.wsitconf.util.Util;
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
    private Service service;
    private JaxWsModel jaxwsmodel;

    private String oldProfile;

    private boolean doNotSync = false;

    private boolean inSync = false;
    private boolean isFromJava = true;

    private final Color RED = new java.awt.Color(255, 0, 0);
    private Color REGULAR = new java.awt.Color(0, 0, 0);
            
    public ServicePanel(SectionView view, Node node, Project p, Binding binding, UndoManager undoManager, JaxWsModel jaxwsmodel) {
        super(view);
        this.model = binding.getModel();
        this.project = p;
        this.node = node;
        this.undoManager = undoManager;
        this.binding = binding;
        this.jaxwsmodel = jaxwsmodel;
        initComponents();

        REGULAR = profileInfoField.getForeground();
        
        if (node != null) {
            service = node.getLookup().lookup(Service.class);
            if (service != null) {
                String wsdlUrl = service.getWsdlUrl();
                if (wsdlUrl != null) { // WS from WSDL
                    isFromJava = false;
                }
            }
        } else {
            isFromJava = false;
        }
        
        mtomChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        rmChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        orderedChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        securityChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        profileComboLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        profileCombo.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        profileInfoField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        profileInfoField.setFont(mtomChBox.getFont());
        stsChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        tcpChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        fiChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        devDefaultsChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
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
        addImmediateModifier(devDefaultsChBox);

        sync();

        model.addComponentListener(new ComponentListener() {
            public void valueChanged(ComponentEvent evt) {
                if (!doNotSync) {
                    sync();
                }
            }
            public void childrenAdded(ComponentEvent evt) {
                if (!doNotSync) {
                    sync();
                }
            }
            public void childrenDeleted(ComponentEvent evt) {
                if (!doNotSync) {
                    sync();
                }
            }
        });
    }

    private void fillProfileCombo(boolean sts) {
        profileCombo.removeAllItems();
        Set<SecurityProfile> profiles = SecurityProfileRegistry.getDefault().getSecurityProfiles();
        for (SecurityProfile profile : profiles) {
            if (profile.isProfileSupported(project, binding, sts)) {
                profileCombo.addItem(profile.getDisplayName());
            }
        }
    }
    
    private void sync() {
        inSync = true;
        
        boolean mtomEnabled = TransportModelHelper.isMtomEnabled(binding);
        setChBox(mtomChBox, mtomEnabled);
        
        boolean fiEnabled = TransportModelHelper.isFIEnabled(binding);
        setChBox(fiChBox, !fiEnabled);

        boolean tcpEnabled = TransportModelHelper.isTCPEnabled(binding);
        setChBox(tcpChBox, tcpEnabled);

        boolean rmEnabled = RMModelHelper.isRMEnabled(binding);
        setChBox(rmChBox, rmEnabled);        
        setChBox(orderedChBox, RMSunModelHelper.isOrderedEnabled(binding));

        boolean stsEnabled = ProprietarySecurityPolicyModelHelper.isSTSEnabled(binding);
        setChBox(stsChBox, stsEnabled);
        
        fillProfileCombo(stsEnabled);
        
        boolean securityEnabled = SecurityPolicyModelHelper.isSecurityEnabled(binding);
        setChBox(securityChBox, securityEnabled);
        if (securityEnabled) {
            String profile = ProfilesModelHelper.getSecurityProfile(binding);
            setSecurityProfile(profile);
            boolean defaults = ProfilesModelHelper.isServiceDefaultSetupUsed(profile, binding, project);
            setChBox(devDefaultsChBox, defaults);
        } else {
            setSecurityProfile(ComboConstants.PROF_USERNAME);
            setChBox(devDefaultsChBox, true);
        }

        enableDisable();
        inSync = false;
    }

    @Override
    public void setValue(javax.swing.JComponent source, Object value) {
        if (inSync) return;
        
        if (source.equals(rmChBox)) {
            if (rmChBox.isSelected()) {
                if (!(RMModelHelper.isRMEnabled(binding))) {
                    RMModelHelper.enableRM(binding);
                    if (securityChBox.isSelected() && !ProfilesModelHelper.isSCEnabled(binding)) {
                        String profile = (String) profileCombo.getSelectedItem();
                        ProfilesModelHelper.enableSecureConversation(binding, true, profile);
                    }
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
            if (!fiChBox.isSelected()) {
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
            boolean jsr109 = isJsr109Supported();
            if (tcpChBox.isSelected()) {
                if (!(TransportModelHelper.isTCPEnabled(binding))) {
                    TransportModelHelper.enableTCP(service, isFromJava, binding, project, true, jsr109);
                }
            } else {
                if (TransportModelHelper.isTCPEnabled(binding)) {
                    TransportModelHelper.enableTCP(service, isFromJava, binding, project, false, jsr109);
                }
            }
        }
        
        if (source.equals(securityChBox)) {
            String profile = (String) profileCombo.getSelectedItem();
            if (securityChBox.isSelected()) {
                profileCombo.setSelectedItem(profile);
                if (devDefaultsChBox.isSelected()) {
                    Util.fillDefaults(project, false);
                    ProfilesModelHelper.setServiceDefaults((String) profileCombo.getSelectedItem(), binding, project);
                    if (ProfilesModelHelper.isSSLProfile(profile)) {
                        ProfilesModelHelper.setSSLAttributes(binding);
                    }
                }
            } else {
                if (devDefaultsChBox.isSelected()) {
                    if (ProfilesModelHelper.isSSLProfile(profile)) {
                        ProfilesModelHelper.unsetSSLAttributes(binding);
                    }
                }
                Util.unfillDefaults(project);
                SecurityPolicyModelHelper.disableSecurity(binding, true);
            }
            oldProfile = profile;
        }

        if (source.equals(devDefaultsChBox)) {
            if (devDefaultsChBox.isSelected()) {
                Util.fillDefaults(project, false);
                ProfilesModelHelper.setServiceDefaults((String) profileCombo.getSelectedItem(), binding, project);
            } else {
                Util.unfillDefaults(project);
            }
        }
        
        if (source.equals(stsChBox)) {
            if (stsChBox.isSelected()) {
                ProprietarySecurityPolicyModelHelper.enableSTS(binding);
                inSync = true; fillProfileCombo(true); inSync = false;
            } else {
                ProprietarySecurityPolicyModelHelper.disableSTS(binding);
                inSync = true; fillProfileCombo(false); inSync = false;
            }
        }

        if (source.equals(profileCombo)) {
            doNotSync = true;
            try {
                String profile = (String) profileCombo.getSelectedItem();
                ProfilesModelHelper.setSecurityProfile(binding, profile, oldProfile);
                if (devDefaultsChBox.isSelected()) {
                    ProfilesModelHelper.setServiceDefaults(profile, binding, project);
                    if (ProfilesModelHelper.isSSLProfile(profile) && !ProfilesModelHelper.isSSLProfile(oldProfile)) {
                        ProfilesModelHelper.setSSLAttributes(binding);
                    } 
                    if (!ProfilesModelHelper.isSSLProfile(profile) && ProfilesModelHelper.isSSLProfile(oldProfile)) {
                        ProfilesModelHelper.unsetSSLAttributes(binding);
                    }
                }
                boolean defUsed = ProfilesModelHelper.isServiceDefaultSetupUsed(profile, binding, project);
                inSync = true; devDefaultsChBox.setSelected(defUsed); inSync = false;
                profileInfoField.setText(SecurityProfileRegistry.getDefault().getProfile(profile).getDescription());
                oldProfile = profile;
            } finally {
                doNotSync = false;
            }
        }
        
        enableDisable();
    }

    public Boolean getChBox(JCheckBox chBox) {
        if (chBox.isSelected()) {
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
        SecurityProfile sp = SecurityProfileRegistry.getDefault().getProfile(profile);
        this.profileInfoField.setText(sp.getDescription());
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

        boolean isTomcat = Util.isTomcat(project);
        tcpChBox.setEnabled(!isTomcat);
        
        boolean amSec = SecurityCheckerRegistry.getDefault().isNonWsitSecurityEnabled(node, jaxwsmodel);
        
        // everything is ok, disable security
        if (!amSec) {

            boolean gf = Util.isGlassfish(project);
            
            securityChBox.setEnabled(true);
            profileInfoField.setForeground(REGULAR);
            
            boolean secSelected = securityChBox.isSelected();
                
            devDefaultsChBox.setEnabled(true);
            boolean defaults = devDefaultsChBox.isSelected();
            
            profileComboLabel.setEnabled(secSelected);
            profileCombo.setEnabled(secSelected);
            profileInfoField.setEnabled(secSelected);
            profConfigButton.setEnabled(secSelected);

            boolean storeConfigRequired = true;
            boolean keyStoreConfigRequired = true;
            boolean trustStoreConfigRequired = true;
            boolean validatorsRequired = true;
            boolean stsAllowed = true;
            
            if (secSelected) {                

                String secProfile = ProfilesModelHelper.getSecurityProfile(binding);
                boolean isSSL = ProfilesModelHelper.isSSLProfile(secProfile);
                if (isSSL) {
                    keyStoreConfigRequired = false;
                    trustStoreConfigRequired = false;
                }

                if (stsAllowed) {
                    if (ComboConstants.PROF_SAMLHOLDER.equals(secProfile) || 
                        ComboConstants.PROF_SAMLSENDER.equals(secProfile) ||
                        ComboConstants.PROF_SAMLSSL.equals(secProfile)) {
                            stsAllowed = false;
                    }
                }
                
                if (trustStoreConfigRequired && gf) {
                    if (ComboConstants.PROF_USERNAME.equals(secProfile) || 
                        ComboConstants.PROF_MUTUALCERT.equals(secProfile) ||
                        ComboConstants.PROF_ENDORSCERT.equals(secProfile) ||
                        ComboConstants.PROF_SAMLSENDER.equals(secProfile) ||
                        ComboConstants.PROF_SAMLHOLDER.equals(secProfile) ||
                        ComboConstants.PROF_STSISSUED.equals(secProfile) ||
                        ComboConstants.PROF_STSISSUEDCERT.equals(secProfile) ||
                        ComboConstants.PROF_STSISSUEDENDORSE.equals(secProfile)
                        ) {
                            trustStoreConfigRequired = false;
                    }
                }
                
                if (validatorsRequired) {
                    if (ComboConstants.PROF_STSISSUED.equals(secProfile) || 
                        ComboConstants.PROF_STSISSUEDCERT.equals(secProfile) ||
                        ComboConstants.PROF_STSISSUEDENDORSE.equals(secProfile)) {
                            validatorsRequired = false;
                    }
                }
            } else {
                devDefaultsChBox.setEnabled(false);
            }
                        
            stsChBox.setEnabled(secSelected && !isFromJava && stsAllowed);

            boolean stsSelected = stsChBox.isSelected();
            stsConfigButton.setEnabled(stsSelected);

            if (stsSelected) {
                trustStoreConfigRequired = true;
                keyStoreConfigRequired = true;
                validatorsRequired = true;
            }
            
            validatorsButton.setEnabled(secSelected && !gf && !defaults && validatorsRequired);
            keyButton.setEnabled(storeConfigRequired && secSelected && keyStoreConfigRequired && !defaults);
            trustButton.setEnabled(storeConfigRequired && secSelected && trustStoreConfigRequired && !defaults);
        } else { // no wsit fun, there's access manager security selected
            profileComboLabel.setEnabled(false);
            profileCombo.setEnabled(false);
            profileInfoField.setEnabled(false);
            profConfigButton.setEnabled(false);
            stsChBox.setEnabled(false);
            devDefaultsChBox.setEnabled(false);
            stsConfigButton.setEnabled(false);
            securityChBox.setEnabled(false);
            validatorsButton.setEnabled(false);
            keyButton.setEnabled(false);
            trustButton.setEnabled(false);
            profileInfoField.setEnabled(true);
            profileInfoField.setForeground(RED);
            profileInfoField.setText(NbBundle.getMessage(ServicePanel.class, "TXT_AMSecSelected"));
        }
    }


    private boolean isJsr109Supported(){
        J2eePlatform j2eePlatform = Util.getJ2eePlatform(project);
        if (j2eePlatform != null) {
            return j2eePlatform.isToolSupported(J2eePlatform.TOOL_JSR109);
        }
        return false;
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
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        stsChBox = new javax.swing.JCheckBox();
        tcpChBox = new javax.swing.JCheckBox();
        jSeparator3 = new javax.swing.JSeparator();
        keyButton = new javax.swing.JButton();
        trustButton = new javax.swing.JButton();
        stsConfigButton = new javax.swing.JButton();
        profConfigButton = new javax.swing.JButton();
        fiChBox = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        profileInfoField = new javax.swing.JTextArea();
        validatorsButton = new javax.swing.JButton();
        devDefaultsChBox = new javax.swing.JCheckBox();

        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });
        addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                formAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(mtomChBox, org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_mtomChBox")); // NOI18N
        mtomChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(rmChBox, org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_rmChBox")); // NOI18N
        rmChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(securityChBox, org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_securityChBox")); // NOI18N
        securityChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(orderedChBox, org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_OrderedChBox")); // NOI18N
        orderedChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        profileComboLabel.setLabelFor(profileCombo);
        org.openide.awt.Mnemonics.setLocalizedText(profileComboLabel, org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_profileComboLabel")); // NOI18N

        profileCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "SAML Sender Vouches With Certificates", "Anonymous with Bilateral Certificates" }));

        org.openide.awt.Mnemonics.setLocalizedText(rmAdvanced, org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_Advanced")); // NOI18N
        rmAdvanced.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rmAdvancedActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(stsChBox, org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_stsChBox")); // NOI18N
        stsChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(tcpChBox, org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_tcpChBox")); // NOI18N
        tcpChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(keyButton, org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_keystoreButton")); // NOI18N
        keyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(trustButton, org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_truststoreButton")); // NOI18N
        trustButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trustButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(stsConfigButton, org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_stsConfigButton")); // NOI18N
        stsConfigButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stsConfigButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(profConfigButton, org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_keyConfigButton")); // NOI18N
        profConfigButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profConfigButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(fiChBox, org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_fiChBox")); // NOI18N
        fiChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        profileInfoField.setEditable(false);
        profileInfoField.setLineWrap(true);
        profileInfoField.setText("This is a text This is a text This is a text This is a text This is a text This is a text This is");
        profileInfoField.setWrapStyleWord(true);
        profileInfoField.setAutoscrolls(false);
        profileInfoField.setOpaque(false);
        jScrollPane1.setViewportView(profileInfoField);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/wsitconf/ui/service/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(validatorsButton, bundle.getString("LBL_validatorsButton")); // NOI18N
        validatorsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validatorsButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(devDefaultsChBox, org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_Defaults")); // NOI18N
        devDefaultsChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
                    .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
                    .add(mtomChBox)
                    .add(rmChBox)
                    .add(layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(rmAdvanced)
                            .add(orderedChBox))
                        .add(79, 79, 79))
                    .add(securityChBox)
                    .add(layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(12, 12, 12)
                                .add(jScrollPane1))
                            .add(layout.createSequentialGroup()
                                .add(profileComboLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(profileCombo, 0, 160, Short.MAX_VALUE)
                                .add(6, 6, 6)
                                .add(profConfigButton))))
                    .add(layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(devDefaultsChBox))
                    .add(jSeparator3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(stsChBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(stsConfigButton))
                    .add(tcpChBox)
                    .add(fiChBox)
                    .add(layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(keyButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(trustButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(validatorsButton)))
                .addContainerGap())
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
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 58, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(devDefaultsChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(trustButton)
                    .add(keyButton)
                    .add(validatorsButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(stsChBox)
                    .add(stsConfigButton))
                .add(11, 11, 11)
                .add(jSeparator3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(8, 8, 8)
                .add(tcpChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fiChBox)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        mtomChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_mtomChBox_ACSN")); // NOI18N
        mtomChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_mtomChBox_ACSD")); // NOI18N
        rmChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_rmChBox_ACSN")); // NOI18N
        rmChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_rmChBox_ACSD")); // NOI18N
        securityChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_securityChBox_ACSN")); // NOI18N
        securityChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_securityChBox_ACSD")); // NOI18N
        orderedChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_OrderedChBox_ACSN")); // NOI18N
        orderedChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_OrderedChBox_ACSD")); // NOI18N
        profileComboLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_profileComboLabel_ACSN")); // NOI18N
        profileComboLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_profileComboLabel_ACSD")); // NOI18N
        rmAdvanced.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_Advanced_ACSN")); // NOI18N
        rmAdvanced.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_Advanced_ACSD")); // NOI18N
        stsChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_stsChBox_ACSN")); // NOI18N
        stsChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_stsChBox_ACSD")); // NOI18N
        tcpChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_tcpChBox_ACSN")); // NOI18N
        tcpChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_tcpChBox_ACSD")); // NOI18N
        keyButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_keystoreButton_ACSN")); // NOI18N
        keyButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_keystoreButton_ACSD")); // NOI18N
        trustButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_truststoreButton_ACSN")); // NOI18N
        trustButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_truststoreButton_ACSD")); // NOI18N
        stsConfigButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_stsConfigButton_ACSN")); // NOI18N
        stsConfigButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_stsConfigButton_ACSD")); // NOI18N
        profConfigButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_keyConfigButton_ACSN")); // NOI18N
        profConfigButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_keyConfigButton_ACSD")); // NOI18N
        fiChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_fiChBox_ACSN")); // NOI18N
        fiChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_fiChBox_ACSD")); // NOI18N
        validatorsButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_validatorsButton_ACSN")); // NOI18N
        validatorsButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_validatorsButton_ACSD")); // NOI18N
        devDefaultsChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_Defaults_ACSN")); // NOI18N
        devDefaultsChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServicePanel.class, "LBL_Section_Service_Defaults_ACSD")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServicePanel.class, "Panel_ACSN")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServicePanel.class, "Panel_ACSD")); // NOI18N
        getAccessibleContext().setAccessibleParent(this);
    }// </editor-fold>//GEN-END:initComponents

private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
        enableDisable();
}//GEN-LAST:event_formFocusGained

private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
        enableDisable();
}//GEN-LAST:event_formAncestorAdded

    private void validatorsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validatorsButtonActionPerformed
        String profile = (String) profileCombo.getSelectedItem();
        ValidatorsPanel vPanel = new ValidatorsPanel(binding, project, profile); //NOI18N
        DialogDescriptor dlgDesc = new DialogDescriptor(vPanel, 
                NbBundle.getMessage(ServicePanel.class, "LBL_Validators_Panel_Title")); //NOI18N
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);
        
        dlg.setVisible(true); 
        
        if (dlgDesc.getValue() == dlgDesc.OK_OPTION) {
            vPanel.storeState();
        }

    }//GEN-LAST:event_validatorsButtonActionPerformed

    private void profConfigButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profConfigButtonActionPerformed
        String prof = (String) profileCombo.getSelectedItem();
        SecurityProfile p = SecurityProfileRegistry.getDefault().getProfile(prof);
        p.displayConfig(binding, undoManager);
    }//GEN-LAST:event_profConfigButtonActionPerformed

    private void stsConfigButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stsConfigButtonActionPerformed
        UndoCounter undoCounter = new UndoCounter();
        model.addUndoableEditListener(undoCounter);

        STSConfigServicePanel stsConfigPanel = new STSConfigServicePanel(model, project, binding);
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
        boolean jsr109 = isJsr109Supported();
        String profile = (String) profileCombo.getSelectedItem();
        TruststorePanel storePanel = new TruststorePanel(binding, project, jsr109, profile, false, null);
        DialogDescriptor dlgDesc = new DialogDescriptor(storePanel, 
                NbBundle.getMessage(ServicePanel.class, "LBL_Truststore_Panel_Title")); //NOI18N
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);
        
        dlg.setVisible(true); 
        if (dlgDesc.getValue() == dlgDesc.OK_OPTION) {
            storePanel.storeState();
        }
    }//GEN-LAST:event_trustButtonActionPerformed

    private void keyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyButtonActionPerformed
        boolean jsr109 = isJsr109Supported();
        KeystorePanel storePanel = new KeystorePanel(binding, project, jsr109, false, null);
        DialogDescriptor dlgDesc = new DialogDescriptor(storePanel, 
                NbBundle.getMessage(ServicePanel.class, "LBL_Keystore_Panel_Title")); //NOI18N
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);
        
        dlg.setVisible(true); 
        
        if (dlgDesc.getValue() == dlgDesc.OK_OPTION) {
            storePanel.storeState();
        }
    }//GEN-LAST:event_keyButtonActionPerformed

    private void rmAdvancedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rmAdvancedActionPerformed
        AdvancedRMPanel advancedRMPanel = new AdvancedRMPanel(binding); //NOI18N
        DialogDescriptor dlgDesc = new DialogDescriptor(advancedRMPanel, 
                NbBundle.getMessage(ServicePanel.class, "LBL_AdvancedRM_Title")); //NOI18N
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);
        
        dlg.setVisible(true); 

        if (dlgDesc.getValue() == dlgDesc.OK_OPTION) {
            advancedRMPanel.storeState();
        }
    }//GEN-LAST:event_rmAdvancedActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox devDefaultsChBox;
    private javax.swing.JCheckBox fiChBox;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JButton keyButton;
    private javax.swing.JCheckBox mtomChBox;
    private javax.swing.JCheckBox orderedChBox;
    private javax.swing.JButton profConfigButton;
    private javax.swing.JComboBox profileCombo;
    private javax.swing.JLabel profileComboLabel;
    private javax.swing.JTextArea profileInfoField;
    private javax.swing.JButton rmAdvanced;
    private javax.swing.JCheckBox rmChBox;
    private javax.swing.JCheckBox securityChBox;
    private javax.swing.JCheckBox stsChBox;
    private javax.swing.JButton stsConfigButton;
    private javax.swing.JCheckBox tcpChBox;
    private javax.swing.JButton trustButton;
    private javax.swing.JButton validatorsButton;
    // End of variables declaration//GEN-END:variables
    
}
