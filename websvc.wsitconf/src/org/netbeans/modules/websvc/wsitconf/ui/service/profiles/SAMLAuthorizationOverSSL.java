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
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityTokensModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.security.WssElement;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.HttpsToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.TransportToken;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

/**
 *
 * @author  Martin Grebac
 */
public class SAMLAuthorizationOverSSL extends javax.swing.JPanel implements ComboConstants {

    private boolean inSync = false;

    private WSDLComponent comp;
    
    /**
     * Creates new form MessageAuthentication
     */
    public SAMLAuthorizationOverSSL(WSDLComponent comp) {
        super();
        initComponents();
        this.comp = comp;

        inSync = true;
        samlVersionCombo.removeAllItems();
        samlVersionCombo.addItem(ComboConstants.SAML_V1010);
        samlVersionCombo.addItem(ComboConstants.SAML_V1011);
        samlVersionCombo.addItem(ComboConstants.SAML_V1110);
        samlVersionCombo.addItem(ComboConstants.SAML_V1111);
        samlVersionCombo.addItem(ComboConstants.SAML_V2011);

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

        WSDLComponent secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);        

        if (comp instanceof Binding) {
            WSDLComponent tokenKind = SecurityTokensModelHelper.getSupportingToken(comp, SecurityTokensModelHelper.SIGNED_SUPPORTING);
            WSDLComponent token = SecurityTokensModelHelper.getTokenTypeElement(tokenKind);
            String samlVersion = SecurityTokensModelHelper.getTokenProfileVersion(token);
            setCombo(samlVersionCombo, samlVersion);
        }
        
        setCombo(wssVersionCombo, SecurityPolicyModelHelper.isWss11(comp));
        setCombo(algoSuiteCombo, AlgoSuiteModelHelper.getAlgorithmSuite(secBinding));
        setCombo(layoutCombo, SecurityPolicyModelHelper.getMessageLayout(comp));
        setChBox(reqSigConfChBox, SecurityPolicyModelHelper.isRequireSignatureConfirmation(comp));
        setChBox(encryptSignatureChBox, SecurityPolicyModelHelper.isEncryptSignature(comp));

        enableDisable();
        
        inSync = false;
    }

    private void enableDisable() {
        boolean wss11 = ComboConstants.WSS11.equals(wssVersionCombo.getSelectedItem());
        reqSigConfChBox.setEnabled(wss11);
    }
    
    public void setValue(javax.swing.JComponent source) {

        if (inSync) return;
            
        WSDLComponent secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);        

        if (source.equals(reqClientCertChBox)) {
            WSDLComponent tokenKind = SecurityTokensModelHelper.getTokenElement(secBinding, TransportToken.class);
            HttpsToken token = (HttpsToken) SecurityTokensModelHelper.getTokenTypeElement(tokenKind);
            SecurityTokensModelHelper.setRequireClientCertificate(token, reqClientCertChBox.isSelected());
        }
        if (source.equals(encryptSignatureChBox)) {
            SecurityPolicyModelHelper.enableEncryptSignature(secBinding, encryptSignatureChBox.isSelected());
        }
        if (source.equals(reqSigConfChBox)) {
            SecurityPolicyModelHelper.enableRequireSignatureConfirmation(SecurityPolicyModelHelper.getWss11(comp), reqSigConfChBox.isSelected());
        }
        if (source.equals(layoutCombo)) {
            SecurityPolicyModelHelper.setLayout(secBinding, (String) layoutCombo.getSelectedItem());
        }
        if (source.equals(algoSuiteCombo)) {
            AlgoSuiteModelHelper.setAlgorithmSuite(secBinding, (String) algoSuiteCombo.getSelectedItem());
        }
        if (source.equals(wssVersionCombo)) {
            boolean wss11 = ComboConstants.WSS11.equals(wssVersionCombo.getSelectedItem());
            WssElement wss = SecurityPolicyModelHelper.enableWss(comp, wss11);
            if (wss11) {
                SecurityPolicyModelHelper.enableRequireSignatureConfirmation(SecurityPolicyModelHelper.getWss11(comp), reqSigConfChBox.isSelected());
            }
            SecurityPolicyModelHelper.enableMustSupportRefKeyIdentifier(wss, true);
        }
        if (source.equals(samlVersionCombo)) {            
            if (comp instanceof Binding) {
                WSDLComponent tokenKind = SecurityTokensModelHelper.getSupportingToken(comp, 
                                            SecurityTokensModelHelper.SIGNED_SUPPORTING);
                WSDLComponent token = SecurityTokensModelHelper.getTokenTypeElement(tokenKind);
                SecurityTokensModelHelper.setTokenProfileVersion(token, (String) samlVersionCombo.getSelectedItem());
            }
        }
        enableDisable();
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

        samlVersionLabel = new javax.swing.JLabel();
        samlVersionCombo = new javax.swing.JComboBox();
        reqSigConfChBox = new javax.swing.JCheckBox();
        reqClientCertChBox = new javax.swing.JCheckBox();
        wssVersionLabel = new javax.swing.JLabel();
        wssVersionCombo = new javax.swing.JComboBox();
        algoSuiteLabel = new javax.swing.JLabel();
        algoSuiteCombo = new javax.swing.JComboBox();
        layoutLabel = new javax.swing.JLabel();
        layoutCombo = new javax.swing.JComboBox();
        encryptSignatureChBox = new javax.swing.JCheckBox();

        samlVersionLabel.setLabelFor(samlVersionCombo);
        org.openide.awt.Mnemonics.setLocalizedText(samlVersionLabel, org.openide.util.NbBundle.getMessage(SAMLAuthorizationOverSSL.class, "LBL_SamlVersion")); // NOI18N

        samlVersionCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                samlVersionComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(reqSigConfChBox, org.openide.util.NbBundle.getMessage(SAMLAuthorizationOverSSL.class, "LBL_RequireSigConfirmation")); // NOI18N
        reqSigConfChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        reqSigConfChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        reqSigConfChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reqSigConfChBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(reqClientCertChBox, org.openide.util.NbBundle.getMessage(SAMLAuthorizationOverSSL.class, "LBL_RequireClientCertificate")); // NOI18N
        reqClientCertChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        reqClientCertChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        reqClientCertChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reqClientCertChBoxActionPerformed(evt);
            }
        });

        wssVersionLabel.setLabelFor(wssVersionCombo);
        org.openide.awt.Mnemonics.setLocalizedText(wssVersionLabel, org.openide.util.NbBundle.getMessage(SAMLAuthorizationOverSSL.class, "LBL_WSSVersionLabel")); // NOI18N

        wssVersionCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wssVersionComboActionPerformed(evt);
            }
        });

        algoSuiteLabel.setLabelFor(algoSuiteCombo);
        org.openide.awt.Mnemonics.setLocalizedText(algoSuiteLabel, org.openide.util.NbBundle.getMessage(SAMLAuthorizationOverSSL.class, "LBL_AlgoSuiteLabel")); // NOI18N

        algoSuiteCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                algoSuiteComboActionPerformed(evt);
            }
        });

        layoutLabel.setLabelFor(layoutCombo);
        org.openide.awt.Mnemonics.setLocalizedText(layoutLabel, org.openide.util.NbBundle.getMessage(SAMLAuthorizationOverSSL.class, "LBL_LayoutLabel")); // NOI18N

        layoutCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                layoutComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(encryptSignatureChBox, org.openide.util.NbBundle.getMessage(SAMLAuthorizationOverSSL.class, "LBL_EncryptSignatureLabel")); // NOI18N
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
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(reqClientCertChBox)
                    .add(encryptSignatureChBox)
                    .add(reqSigConfChBox)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(samlVersionLabel)
                            .add(wssVersionLabel)
                            .add(algoSuiteLabel)
                            .add(layoutLabel))
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(samlVersionCombo, 0, 126, Short.MAX_VALUE)
                            .add(wssVersionCombo, 0, 126, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, algoSuiteCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 126, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layoutCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 126, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {algoSuiteCombo, layoutCombo, samlVersionCombo, wssVersionCombo}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(samlVersionLabel)
                    .add(samlVersionCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(wssVersionLabel)
                    .add(wssVersionCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(algoSuiteLabel)
                    .add(algoSuiteCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(layoutLabel)
                    .add(layoutCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(reqClientCertChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(reqSigConfChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(encryptSignatureChBox)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {algoSuiteCombo, layoutCombo, samlVersionCombo, wssVersionCombo}, org.jdesktop.layout.GroupLayout.VERTICAL);

    }// </editor-fold>//GEN-END:initComponents

    private void reqSigConfChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reqSigConfChBoxActionPerformed
        setValue(reqSigConfChBox);
    }//GEN-LAST:event_reqSigConfChBoxActionPerformed

    private void reqClientCertChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reqClientCertChBoxActionPerformed
        setValue(reqClientCertChBox);
    }//GEN-LAST:event_reqClientCertChBoxActionPerformed

    private void wssVersionComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wssVersionComboActionPerformed
        setValue(wssVersionCombo);
    }//GEN-LAST:event_wssVersionComboActionPerformed

    private void samlVersionComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_samlVersionComboActionPerformed
        setValue(samlVersionCombo);
    }//GEN-LAST:event_samlVersionComboActionPerformed

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
    private javax.swing.JCheckBox encryptSignatureChBox;
    private javax.swing.JComboBox layoutCombo;
    private javax.swing.JLabel layoutLabel;
    private javax.swing.JCheckBox reqClientCertChBox;
    private javax.swing.JCheckBox reqSigConfChBox;
    private javax.swing.JComboBox samlVersionCombo;
    private javax.swing.JLabel samlVersionLabel;
    private javax.swing.JComboBox wssVersionCombo;
    private javax.swing.JLabel wssVersionLabel;
    // End of variables declaration//GEN-END:variables
    
}
