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

import java.awt.Dialog;
import javax.swing.undo.UndoManager;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.Generic;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.KerberosAuthentication;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.MutualCertificates;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.STSIssued;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.STSIssuedCert;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.STSIssuedEndorsing;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.UsernameAuthentication;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.TxModelHelper;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.Node;

import javax.swing.*;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.EndorsingCertificate;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.MessageAuthentication;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.SAMLAuthorizationOverSSL;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.SAMLHolderOfKey;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.SenderVouches;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.TransportSecurity;
import org.netbeans.modules.websvc.wsitconf.util.UndoCounter;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProfilesModelHelper;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac
 */
public class OperationPanel extends SectionInnerPanel {

    private WSDLModel model;
    private Node node;
    private Binding binding;
    private BindingOperation operation;
    private UndoManager undoManager;
    private boolean inSync = false;
    private Project project;
    
    public OperationPanel(SectionView view, Node node, Project p, BindingOperation operation, UndoManager undoManager) {
        super(view);
        this.model = operation.getModel();
        this.node = node;
        this.binding = (Binding)operation.getParent();
        this.undoManager = undoManager;
        this.project = p;
        this.operation = operation;
        initComponents();
        
        txCombo.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        txLbl.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        securityChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        profileComboLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        profileCombo.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        profileConfigPanel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        keystoreButton.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        trustButton.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        jSeparator2.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        
        addImmediateModifier(securityChBox);
        addImmediateModifier(profileCombo);
        addImmediateModifier(txCombo);

        inSync = true;
        txCombo.removeAllItems();
//        txCombo.addItem(ComboConstants.TX_NEVER);
        txCombo.addItem(ComboConstants.TX_NOTSUPPORTED);
        txCombo.addItem(ComboConstants.TX_MANDATORY);
        txCombo.addItem(ComboConstants.TX_REQUIRED);
        txCombo.addItem(ComboConstants.TX_REQUIRESNEW);
        txCombo.addItem(ComboConstants.TX_SUPPORTED);

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
        
        String txValue = TxModelHelper.getTx(operation, node);
        txCombo.setSelectedItem(txValue);
                
        boolean securityEnabled = SecurityPolicyModelHelper.isSecurityEnabled(operation);
        setSecurity(securityEnabled);
        if (securityEnabled) {
            setSecurityProfile(ProfilesModelHelper.getSecurityProfile(operation));
        } else {
            setSecurityProfile(ComboConstants.PROF_USERNAME);
        }

        enableDisable();
        inSync = false;
    }

    // SECURITY
    private void setSecurity(Boolean enable) {
        if (enable == null) {
            this.securityChBox.setSelected(false);
        } else {
            this.securityChBox.setSelected(enable);
        }
    }

    public Boolean getSecurity() {
        if (securityChBox.isSelected()) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
    
    @Override
    public void setValue(javax.swing.JComponent source, Object value) {
        if (inSync) return;
        if (source.equals(txCombo)) {
            String selected = (String) txCombo.getSelectedItem();
            if ((selected != null) && (!selected.equals(TxModelHelper.getTx(operation, node)))) {
                TxModelHelper.setTx(operation, node, selected);
            }
        }

        if (source.equals(securityChBox)) {
            if (securityChBox.isSelected()) {
                profileCombo.setSelectedItem((String) profileCombo.getSelectedItem());
            } else {
                SecurityPolicyModelHelper.disableSecurity(operation);
            }
        }

        if (source.equals(profileCombo)) {
            ProfilesModelHelper.setSecurityProfile(operation, (String) profileCombo.getSelectedItem());
//            this.remove(profileConfigPanel);
//            profileConfigPanel = getProfilePanel((String)profileCombo.getSelectedItem());
//            profileConfigPanel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
//            refreshPanels();
        }
        enableDisable();
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

//    private void refreshPanels() {
//        updateLayout();
//        revalidate();
//        repaint();
//    }
//    
    private void enableDisable() {
        boolean secSelected = securityChBox.isSelected();
        profileComboLabel.setEnabled(secSelected);
        profileCombo.setEnabled(secSelected);
        profileConfigPanel.setEnabled(secSelected);

        boolean storeConfigRequired = isStoreConfigRequired();
        keystoreButton.setEnabled(storeConfigRequired && secSelected);
        trustButton.setEnabled(storeConfigRequired && secSelected);
    }

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

    // SECURITY PROFILE
    private void setSecurityProfile(String profile) {
        this.profileCombo.setSelectedItem(profile);
        this.profileConfigPanel = getProfilePanel(profile);
    }
    
    private JPanel getProfilePanel(String profile) {
        if (ComboConstants.PROF_GENERIC.equals(profile)) return new Generic(operation);
        if (ComboConstants.PROF_TRANSPORT.equals(profile)) return new TransportSecurity(operation);
        if (ComboConstants.PROF_MSGAUTHSSL.equals(profile)) return new MessageAuthentication(operation);
        if (ComboConstants.PROF_SAMLSSL.equals(profile)) return new SAMLAuthorizationOverSSL(operation);
        if (ComboConstants.PROF_USERNAME.equals(profile)) return new UsernameAuthentication(operation);
        if (ComboConstants.PROF_MUTUALCERT.equals(profile)) return new MutualCertificates(operation);
        if (ComboConstants.PROF_ENDORSCERT.equals(profile)) return new EndorsingCertificate(operation);
        if (ComboConstants.PROF_SAMLSENDER.equals(profile)) return new SenderVouches(operation);
        if (ComboConstants.PROF_SAMLHOLDER.equals(profile)) return new SAMLHolderOfKey(operation);
        if (ComboConstants.PROF_KERBEROS.equals(profile)) return new KerberosAuthentication(operation);
        if (ComboConstants.PROF_STSISSUED.equals(profile)) return new STSIssued(operation);
        if (ComboConstants.PROF_STSISSUEDCERT.equals(profile)) return new STSIssuedCert(operation);
        if (ComboConstants.PROF_STSISSUEDENDORSE.equals(profile)) return new STSIssuedEndorsing(operation);
        return null;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        securityChBox = new javax.swing.JCheckBox();
        profileComboLabel = new javax.swing.JLabel();
        profileCombo = new javax.swing.JComboBox();
        profileConfigPanel = new javax.swing.JPanel();
        jSeparator2 = new javax.swing.JSeparator();
        keystoreButton = new javax.swing.JButton();
        txLbl = new javax.swing.JLabel();
        txCombo = new javax.swing.JComboBox();
        trustButton = new javax.swing.JButton();
        profConfigButton = new javax.swing.JButton();

        securityChBox.setText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "LBL_Section_Operation_securityChBox")); // NOI18N
        securityChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        securityChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        securityChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                securityChBoxActionPerformed(evt);
            }
        });

        profileComboLabel.setLabelFor(profileCombo);
        profileComboLabel.setText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "LBL_profileComboLabel")); // NOI18N

        profileCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "SAML Sender Vouches With Certificates", "Anonymous with Bilateral Certificates" }));

        org.jdesktop.layout.GroupLayout profileConfigPanelLayout = new org.jdesktop.layout.GroupLayout(profileConfigPanel);
        profileConfigPanel.setLayout(profileConfigPanelLayout);
        profileConfigPanelLayout.setHorizontalGroup(
            profileConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 445, Short.MAX_VALUE)
        );
        profileConfigPanelLayout.setVerticalGroup(
            profileConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 12, Short.MAX_VALUE)
        );

        keystoreButton.setText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "LBL_keystoreButton")); // NOI18N

        txLbl.setText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "LBL_Section_Operation_Tx")); // NOI18N

        txCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "Not Supported", "Required", "Requires New", "Mandatory", "Supported" }));

        trustButton.setText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "LBL_truststoreButton")); // NOI18N

        profConfigButton.setText(org.openide.util.NbBundle.getMessage(OperationPanel.class, "LBL_keyConfigButton")); // NOI18N
        profConfigButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profConfigButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(37, 37, 37)
                        .add(profileConfigPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(layout.createSequentialGroup()
                                .add(txLbl)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(txCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(securityChBox)
                            .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(17, 17, 17)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(profileComboLabel)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(profileCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 221, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(profConfigButton))
                                    .add(layout.createSequentialGroup()
                                        .add(keystoreButton)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(trustButton)))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(txLbl)
                    .add(txCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(securityChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(profileComboLabel)
                    .add(profileCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(profConfigButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(profileConfigPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keystoreButton)
                    .add(trustButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void profConfigButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profConfigButtonActionPerformed
        UndoCounter undoCounter = new UndoCounter();
        model.addUndoableEditListener(undoCounter);

        JPanel profConfigPanel = getProfilePanel((String)profileCombo.getSelectedItem());
        DialogDescriptor dlgDesc = new DialogDescriptor(profConfigPanel, 
                NbBundle.getMessage(OperationPanel.class, "LBL_AdvancedProfile_Title")); //NOI18N
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

    private void securityChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_securityChBoxActionPerformed
        enableDisable();
    }//GEN-LAST:event_securityChBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JButton keystoreButton;
    private javax.swing.JButton profConfigButton;
    private javax.swing.JComboBox profileCombo;
    private javax.swing.JLabel profileComboLabel;
    private javax.swing.JPanel profileConfigPanel;
    private javax.swing.JCheckBox securityChBox;
    private javax.swing.JButton trustButton;
    private javax.swing.JComboBox txCombo;
    private javax.swing.JLabel txLbl;
    // End of variables declaration//GEN-END:variables
    
}
