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

package org.netbeans.modules.websvc.wsitconf.ui.service.profiles;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.AlgoSuiteModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProfilesModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityTokensModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.security.BootstrapPolicy;
import org.netbeans.modules.websvc.wsitmodelext.security.WssElement;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.ProtectionToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SecureConversationToken;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

/**
 *
 * @author  Martin Grebac
 */
public class MessageAuthentication extends javax.swing.JPanel {

    private boolean inSync = false;

    private WSDLComponent comp;
    
    /**
     * Creates new form MessageAuthentication
     */
    public MessageAuthentication(WSDLComponent comp) {
        super();
        initComponents();
        this.comp = comp;

        inSync = true;
        supportTokenCombo.removeAllItems();
        supportTokenCombo.addItem(ComboConstants.X509);
        supportTokenCombo.addItem(ComboConstants.USERNAME);

        wssVersionCombo.removeAllItems();
        wssVersionCombo.addItem(ComboConstants.WSS10);
        wssVersionCombo.addItem(ComboConstants.WSS11);

        layoutCombo.removeAllItems();
        layoutCombo.addItem(ComboConstants.STRICT);
        layoutCombo.addItem(ComboConstants.LAX);
        layoutCombo.addItem(ComboConstants.LAXTSFIRST);
        layoutCombo.addItem(ComboConstants.LAXTSLAST);
        
        algoSuiteCombo.removeAllItems();
        algoSuiteCombo.addItem(ComboConstants.BASIC256);
        algoSuiteCombo.addItem(ComboConstants.BASIC192);
        algoSuiteCombo.addItem(ComboConstants.BASIC128);
        algoSuiteCombo.addItem(ComboConstants.TRIPLEDES);
        algoSuiteCombo.addItem(ComboConstants.BASIC256RSA15);
        algoSuiteCombo.addItem(ComboConstants.BASIC192RSA15);
        algoSuiteCombo.addItem(ComboConstants.BASIC128RSA15);
        algoSuiteCombo.addItem(ComboConstants.TRIPLEDESRSA15);
//        algoSuiteCombo.addItem(ComboConstants.BASIC256SHA256);
//        algoSuiteCombo.addItem(ComboConstants.BASIC192SHA256);
//        algoSuiteCombo.addItem(ComboConstants.BASIC128SHA256);
//        algoSuiteCombo.addItem(ComboConstants.TRIPLEDESSHA256);
//        algoSuiteCombo.addItem(ComboConstants.BASIC256SHA256RSA15);
//        algoSuiteCombo.addItem(ComboConstants.BASIC192SHA256RSA15);
//        algoSuiteCombo.addItem(ComboConstants.BASIC128SHA256RSA15);
//        algoSuiteCombo.addItem(ComboConstants.TRIPLEDESSHA256RSA15);
        
        inSync = false;
        
        sync();
    }
    
    private void sync() {
        inSync = true;

        WSDLComponent secBinding = null;
        WSDLComponent topSecBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);
        WSDLComponent protTokenKind = SecurityTokensModelHelper.getTokenElement(topSecBinding, ProtectionToken.class);
        WSDLComponent protToken = SecurityTokensModelHelper.getTokenTypeElement(protTokenKind);
        
        boolean secConv = (protToken instanceof SecureConversationToken);

        if (secConv) {
            WSDLComponent bootPolicy = SecurityTokensModelHelper.getTokenElement(protToken, BootstrapPolicy.class);
            secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(bootPolicy);
            Policy p = (Policy) secBinding.getParent();
            setChBox(secConvChBox, true);
            setChBox(derivedKeysSecConvChBox, SecurityPolicyModelHelper.isRequireDerivedKeys(protToken));
            setCombo(wssVersionCombo, SecurityPolicyModelHelper.isWss11(p));
            setChBox(reqSigConfChBox, SecurityPolicyModelHelper.isRequireSignatureConfirmation(p));
            setChBox(encryptSignatureChBox, SecurityPolicyModelHelper.isEncryptSignature(bootPolicy));
            p = PolicyModelHelper.getTopLevelElement(bootPolicy, Policy.class);
            WSDLComponent tokenKind = SecurityTokensModelHelper.getSupportingToken(p, SecurityTokensModelHelper.SIGNED_SUPPORTING);
            String tokenType = SecurityTokensModelHelper.getTokenType(tokenKind);
            setCombo(supportTokenCombo, tokenType);
        } else {
            secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);
            setChBox(secConvChBox, false);
            setChBox(derivedKeysSecConvChBox, false);
            setCombo(wssVersionCombo, SecurityPolicyModelHelper.isWss11(comp));
            setChBox(reqSigConfChBox, SecurityPolicyModelHelper.isRequireSignatureConfirmation(comp));
            setChBox(encryptSignatureChBox, SecurityPolicyModelHelper.isEncryptSignature(comp));
            WSDLComponent tokenKind = SecurityTokensModelHelper.getSupportingToken(comp, SecurityTokensModelHelper.SIGNED_SUPPORTING);
            String tokenType = SecurityTokensModelHelper.getTokenType(tokenKind);
            setCombo(supportTokenCombo, tokenType);
        }

        setCombo(algoSuiteCombo, AlgoSuiteModelHelper.getAlgorithmSuite(secBinding));
        setCombo(layoutCombo, SecurityPolicyModelHelper.getMessageLayout(secBinding));

        enableDisable();

        inSync = false;
    }

    public void setValue(javax.swing.JComponent source) {

        if (inSync) return;
            
        WSDLComponent secBinding = null;
        WSDLComponent topSecBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);
        WSDLComponent protTokenKind = SecurityTokensModelHelper.getTokenElement(topSecBinding, ProtectionToken.class);
        WSDLComponent protToken = SecurityTokensModelHelper.getTokenTypeElement(protTokenKind);
        
        boolean secConv = (protToken instanceof SecureConversationToken);

        if (source.equals(secConvChBox)) {
            ProfilesModelHelper.enableSecureConversation(comp, secConvChBox.isSelected(), ComboConstants.PROF_MSGAUTHSSL);
            sync();
        }
        
        if (secConv) {
            WSDLComponent bootPolicy = SecurityTokensModelHelper.getTokenElement(protToken, BootstrapPolicy.class);
            secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(bootPolicy);
            Policy p = (Policy) secBinding.getParent();
            if (source.equals(reqSigConfChBox)) {
                SecurityPolicyModelHelper.enableRequireSignatureConfirmation(
                        SecurityPolicyModelHelper.getWss11(p), reqSigConfChBox.isSelected());
            }
            if (source.equals(derivedKeysSecConvChBox)) {
                SecurityPolicyModelHelper.enableRequireDerivedKeys(protToken, derivedKeysSecConvChBox.isSelected());
            }
            if (source.equals(wssVersionCombo)) {
                boolean wss11 = ComboConstants.WSS11.equals(wssVersionCombo.getSelectedItem());
                WssElement wss = SecurityPolicyModelHelper.enableWss(p, wss11);
                if (wss11) {
                    SecurityPolicyModelHelper.enableRequireSignatureConfirmation(
                            SecurityPolicyModelHelper.getWss11(p), reqSigConfChBox.isSelected());
                }
                SecurityPolicyModelHelper.enableMustSupportRefKeyIdentifier(wss, true);
            }
            p = PolicyModelHelper.getTopLevelElement(bootPolicy, Policy.class);
            if (source.equals(supportTokenCombo)) {
                SecurityTokensModelHelper.setSupportingTokens(p, 
                        (String)supportTokenCombo.getSelectedItem(), 
                        SecurityTokensModelHelper.SIGNED_SUPPORTING);
            }
        } else {
            secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);
            if (source.equals(reqSigConfChBox)) {
                SecurityPolicyModelHelper.enableRequireSignatureConfirmation(
                        SecurityPolicyModelHelper.getWss11(comp), reqSigConfChBox.isSelected());
            }
            if (source.equals(wssVersionCombo)) {
                boolean wss11 = ComboConstants.WSS11.equals(wssVersionCombo.getSelectedItem());
                WssElement wss = SecurityPolicyModelHelper.enableWss(comp, wss11);
                if (wss11) {
                    SecurityPolicyModelHelper.enableRequireSignatureConfirmation(
                            SecurityPolicyModelHelper.getWss11(comp), reqSigConfChBox.isSelected());
                }
                SecurityPolicyModelHelper.enableMustSupportRefKeyIdentifier(wss, true);
            }
            if (source.equals(supportTokenCombo)) {
                SecurityTokensModelHelper.setSupportingTokens(comp, 
                        (String)supportTokenCombo.getSelectedItem(), 
                        SecurityTokensModelHelper.SIGNED_SUPPORTING);
            }
        }

        if (source.equals(layoutCombo)) {
            SecurityPolicyModelHelper.setLayout(secBinding, (String) layoutCombo.getSelectedItem());
            if (secConv) {
                SecurityPolicyModelHelper.setLayout(topSecBinding, (String) layoutCombo.getSelectedItem());
            }
        }
        if (source.equals(algoSuiteCombo)) {
            AlgoSuiteModelHelper.setAlgorithmSuite(secBinding, (String) algoSuiteCombo.getSelectedItem());
            if (secConv) {
                AlgoSuiteModelHelper.setAlgorithmSuite(topSecBinding, (String) algoSuiteCombo.getSelectedItem());
            }
        }
        if (source.equals(encryptSignatureChBox)) {
            SecurityPolicyModelHelper.enableEncryptSignature(secBinding, encryptSignatureChBox.isSelected());
            if (secConv) {
                SecurityPolicyModelHelper.enableEncryptSignature(topSecBinding, encryptSignatureChBox.isSelected());
            }
        }
        
        enableDisable();
    }

    private void enableDisable() {
        boolean secConvEnabled = secConvChBox.isSelected();
        derivedKeysSecConvChBox.setEnabled(secConvEnabled);
        boolean wss11 = ComboConstants.WSS11.equals(wssVersionCombo.getSelectedItem());
        reqSigConfChBox.setEnabled(wss11);
    }
    
    private void setCombo(JComboBox combo, String item) {
        if (item == null) {
            combo.setSelectedIndex(0);
        } else {
            combo.setSelectedItem(item);
        }
    }

    private void setCombo(JComboBox combo, boolean second) {
        combo.setSelectedIndex(second ? 1 : 0);
    }
        
    private void setChBox(JCheckBox chBox, Boolean enable) {
        if (enable == null) {
            chBox.setSelected(false);
        } else {
            chBox.setSelected(enable);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        supportTokenLabel = new javax.swing.JLabel();
        supportTokenCombo = new javax.swing.JComboBox();
        secConvChBox = new javax.swing.JCheckBox();
        reqSigConfChBox = new javax.swing.JCheckBox();
        derivedKeysSecConvChBox = new javax.swing.JCheckBox();
        wssVersionLabel = new javax.swing.JLabel();
        wssVersionCombo = new javax.swing.JComboBox();
        algoSuiteLabel = new javax.swing.JLabel();
        algoSuiteCombo = new javax.swing.JComboBox();
        layoutLabel = new javax.swing.JLabel();
        layoutCombo = new javax.swing.JComboBox();
        encryptSignatureChBox = new javax.swing.JCheckBox();

        supportTokenLabel.setLabelFor(supportTokenCombo);
        org.openide.awt.Mnemonics.setLocalizedText(supportTokenLabel, org.openide.util.NbBundle.getMessage(MessageAuthentication.class, "LBL_AuthToken")); // NOI18N

        supportTokenCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                supportTokenComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(secConvChBox, org.openide.util.NbBundle.getMessage(MessageAuthentication.class, "LBL_SecConvLabel")); // NOI18N
        secConvChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        secConvChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        secConvChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                secConvChBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(reqSigConfChBox, org.openide.util.NbBundle.getMessage(MessageAuthentication.class, "LBL_RequireSigConfirmation")); // NOI18N
        reqSigConfChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        reqSigConfChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        reqSigConfChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reqSigConfChBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(derivedKeysSecConvChBox, org.openide.util.NbBundle.getMessage(MessageAuthentication.class, "LBL_RequireDerivedKeysForSecConv")); // NOI18N
        derivedKeysSecConvChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        derivedKeysSecConvChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        derivedKeysSecConvChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                derivedKeysSecConvChBoxActionPerformed(evt);
            }
        });

        wssVersionLabel.setLabelFor(wssVersionCombo);
        org.openide.awt.Mnemonics.setLocalizedText(wssVersionLabel, org.openide.util.NbBundle.getMessage(MessageAuthentication.class, "LBL_WSSVersionLabel")); // NOI18N

        wssVersionCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wssVersionComboActionPerformed(evt);
            }
        });

        algoSuiteLabel.setLabelFor(algoSuiteCombo);
        org.openide.awt.Mnemonics.setLocalizedText(algoSuiteLabel, org.openide.util.NbBundle.getMessage(MessageAuthentication.class, "LBL_AlgoSuiteLabel")); // NOI18N

        algoSuiteCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                algoSuiteComboActionPerformed(evt);
            }
        });

        layoutLabel.setLabelFor(layoutCombo);
        org.openide.awt.Mnemonics.setLocalizedText(layoutLabel, org.openide.util.NbBundle.getMessage(MessageAuthentication.class, "LBL_LayoutLabel")); // NOI18N

        layoutCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                layoutComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(encryptSignatureChBox, org.openide.util.NbBundle.getMessage(MessageAuthentication.class, "LBL_EncryptSignatureLabel")); // NOI18N
        encryptSignatureChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        encryptSignatureChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        encryptSignatureChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                encryptSignatureChBox(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(reqSigConfChBox)
                    .add(secConvChBox)
                    .add(derivedKeysSecConvChBox)
                    .add(encryptSignatureChBox)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layoutLabel)
                            .add(supportTokenLabel)
                            .add(wssVersionLabel)
                            .add(algoSuiteLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(algoSuiteCombo, 0, 145, Short.MAX_VALUE)
                            .add(layoutCombo, 0, 145, Short.MAX_VALUE)
                            .add(wssVersionCombo, 0, 145, Short.MAX_VALUE)
                            .add(supportTokenCombo, 0, 145, Short.MAX_VALUE))))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {algoSuiteCombo, layoutCombo, supportTokenCombo, wssVersionCombo}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(supportTokenLabel)
                    .add(supportTokenCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(wssVersionLabel)
                    .add(wssVersionCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(algoSuiteLabel)
                    .add(algoSuiteCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(layoutLabel)
                    .add(layoutCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(secConvChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(derivedKeysSecConvChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(reqSigConfChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(encryptSignatureChBox)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {algoSuiteCombo, layoutCombo, supportTokenCombo, wssVersionCombo}, org.jdesktop.layout.GroupLayout.VERTICAL);

    }// </editor-fold>//GEN-END:initComponents

    private void reqSigConfChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reqSigConfChBoxActionPerformed
         setValue(reqSigConfChBox);
    }//GEN-LAST:event_reqSigConfChBoxActionPerformed

    private void derivedKeysSecConvChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_derivedKeysSecConvChBoxActionPerformed
         setValue(derivedKeysSecConvChBox);
    }//GEN-LAST:event_derivedKeysSecConvChBoxActionPerformed

    private void secConvChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secConvChBoxActionPerformed
        setValue(secConvChBox);
    }//GEN-LAST:event_secConvChBoxActionPerformed

    private void wssVersionComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wssVersionComboActionPerformed
        setValue(wssVersionCombo);
    }//GEN-LAST:event_wssVersionComboActionPerformed

    private void supportTokenComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_supportTokenComboActionPerformed
        setValue(supportTokenCombo);
    }//GEN-LAST:event_supportTokenComboActionPerformed

    private void encryptSignatureChBox(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_encryptSignatureChBox
        setValue(encryptSignatureChBox);
    }//GEN-LAST:event_encryptSignatureChBox

    private void layoutComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_layoutComboActionPerformed
        setValue(layoutCombo);
    }//GEN-LAST:event_layoutComboActionPerformed

    private void algoSuiteComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_algoSuiteComboActionPerformed
        setValue(algoSuiteCombo);
    }//GEN-LAST:event_algoSuiteComboActionPerformed
            
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox algoSuiteCombo;
    private javax.swing.JLabel algoSuiteLabel;
    private javax.swing.JCheckBox derivedKeysSecConvChBox;
    private javax.swing.JCheckBox encryptSignatureChBox;
    private javax.swing.JComboBox layoutCombo;
    private javax.swing.JLabel layoutLabel;
    private javax.swing.JCheckBox reqSigConfChBox;
    private javax.swing.JCheckBox secConvChBox;
    private javax.swing.JComboBox supportTokenCombo;
    private javax.swing.JLabel supportTokenLabel;
    private javax.swing.JComboBox wssVersionCombo;
    private javax.swing.JLabel wssVersionLabel;
    // End of variables declaration//GEN-END:variables
    
}
