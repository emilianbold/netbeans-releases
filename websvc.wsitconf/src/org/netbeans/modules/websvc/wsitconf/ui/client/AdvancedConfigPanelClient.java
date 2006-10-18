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

package org.netbeans.modules.websvc.wsitconf.ui.client;

import javax.swing.JCheckBox;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMSunModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityTokensModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.ProtectionToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SecureConversationToken;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.Node;

/**
 *
 * @author Martin Grebac
 */
public class AdvancedConfigPanelClient extends SectionInnerPanel {

    private WSDLModel clientModel;
    private WSDLModel serviceModel;
    
    private Node node;
    private Binding binding;
    private boolean inSync = false;

    private static final String DEFAULT_LIFETIME = "36000";                     //NOI18N
    private static final String DEFAULT_TIMEOUT = "5000";                     //NOI18N
    private static final String DEFAULT_RMRESENDINTERVAL = "2000";              //NOI18N
    private static final String DEFAULT_RMREQUESTACKINTERVAL = "200";           //NOI18N
    
    public AdvancedConfigPanelClient(SectionView view, Node node, Binding binding, WSDLModel serviceModel) {
        super(view);
        this.clientModel = binding.getModel();
        this.serviceModel = serviceModel;
        this.node = node;
        this.binding = binding;
        
        initComponents();

        lifeTimeLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        lifeTimeTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        timestampTimeoutLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        timestampTimeoutField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        renewExpiredChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        requireCancelChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        rmAckRequestField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        rmAckRequestLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        rmResendField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        rmResendLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());

        addImmediateModifier(rmAckRequestField);
        addImmediateModifier(rmResendField);
        addImmediateModifier(lifeTimeTextField);
        addImmediateModifier(timestampTimeoutField);
        addImmediateModifier(renewExpiredChBox);
        addImmediateModifier(requireCancelChBox);

        addValidatee(lifeTimeTextField);
        addValidatee(timestampTimeoutField);
        addValidatee(rmAckRequestField);
        addValidatee(rmResendField);
        
        sync();
    }

    public void sync() {
        inSync = true;

        String lifeTime = ProprietarySecurityPolicyModelHelper.getLifeTime(binding, true);
        if (lifeTime == null) { // no setup exists yet - set the default
            lifeTimeTextField.setText(DEFAULT_LIFETIME);
            ProprietarySecurityPolicyModelHelper.setLifeTime(binding, DEFAULT_LIFETIME, true);
        } else {
            lifeTimeTextField.setText(lifeTime);
        } 

        String timeout = ProprietarySecurityPolicyModelHelper.getTimestampTimeout(binding, true);
        if (timeout == null) { // no setup exists yet - set the default
            timestampTimeoutField.setText(DEFAULT_TIMEOUT);
            ProprietarySecurityPolicyModelHelper.setTimestampTimeout(binding, DEFAULT_TIMEOUT, true);
        } else {
            timestampTimeoutField.setText(timeout);
        } 
        
        String rmResendInterval = RMSunModelHelper.getResendInterval(binding);
        if (rmResendInterval == null) { // no setup exists yet - set the default
            rmResendField.setText(DEFAULT_RMRESENDINTERVAL);
            RMSunModelHelper.setResendInterval(binding, DEFAULT_RMRESENDINTERVAL);
        } else {
            rmResendField.setText(rmResendInterval);
        }
        
        String rmAckRequest = RMSunModelHelper.getAckRequestInterval(binding);
        if (rmAckRequest == null) { // no setup exists yet - set the default
            rmAckRequestField.setText(DEFAULT_RMREQUESTACKINTERVAL);
            RMSunModelHelper.setAckRequestInterval(binding, DEFAULT_RMREQUESTACKINTERVAL);
        } else {
            rmAckRequestField.setText(rmAckRequest);
        }

        setChBox(renewExpiredChBox, ProprietarySecurityPolicyModelHelper.isRenewExpired(binding));
        setChBox(requireCancelChBox, ProprietarySecurityPolicyModelHelper.isRequireCancel(binding));

        enableDisable();
        
        inSync = false;
    }

    @Override
    public void setValue(javax.swing.JComponent source, Object value) {
        if (!inSync) {
            if (source.equals(lifeTimeTextField)) {
                String lifeTime = lifeTimeTextField.getText();
                if ((lifeTime != null) && (lifeTime.length() == 0)) {
                    ProprietarySecurityPolicyModelHelper.setLifeTime(binding, null, true);
                } else {
                    ProprietarySecurityPolicyModelHelper.setLifeTime(binding, lifeTime, true);
                }
            }

            if (source.equals(timestampTimeoutField)) {
                String timeout = timestampTimeoutField.getText();
                if ((timeout != null) && (timeout.length() == 0)) {
                    ProprietarySecurityPolicyModelHelper.setTimestampTimeout(binding, null, true);
                } else {
                    ProprietarySecurityPolicyModelHelper.setTimestampTimeout(binding, timeout, true);
                }
            }
            
            if (source.equals(rmResendField)) {
                String resendInt = rmResendField.getText();
                if ((resendInt != null) && (resendInt.length() == 0)) {
                    RMSunModelHelper.setResendInterval(binding, null);
                } else {
                    RMSunModelHelper.setResendInterval(binding, resendInt);
                }
            }

            if (source.equals(rmAckRequestField)) {
                String ackRequestInt = rmAckRequestField.getText();
                if ((ackRequestInt != null) && (ackRequestInt.length() == 0)) {
                    RMSunModelHelper.setAckRequestInterval(binding, null);
                } else {
                    RMSunModelHelper.setAckRequestInterval(binding, ackRequestInt);
                }
            }
            
            if (source.equals(renewExpiredChBox)) {
                ProprietarySecurityPolicyModelHelper.setRenewExpired(binding, renewExpiredChBox.isSelected());
            }

            if (source.equals(requireCancelChBox)) {
                ProprietarySecurityPolicyModelHelper.setRequireCancel(binding, requireCancelChBox.isSelected());
            }
        }
    }

    private void enableDisable() {
        Binding serviceBinding = PolicyModelHelper.getBinding(serviceModel, binding.getName());
        boolean rmEnabled = RMModelHelper.isRMEnabled(serviceBinding);
        boolean securityEnabled = SecurityPolicyModelHelper.isSecurityEnabled(serviceBinding); 
        boolean timestampEnabled = SecurityPolicyModelHelper.isIncludeTimestamp(serviceBinding); 
        boolean secConvEnabled = false;
        if (securityEnabled) {
            WSDLComponent secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(serviceBinding);
            WSDLComponent tokenKind = SecurityTokensModelHelper.getTokenElement(secBinding, ProtectionToken.class);
            WSDLComponent tokenType = SecurityTokensModelHelper.getTokenTypeElement(tokenKind);
            secConvEnabled = (tokenType instanceof SecureConversationToken);
        }
        
        rmAckRequestLabel.setEnabled(rmEnabled);
        rmAckRequestField.setEnabled(rmEnabled);
        rmResendLabel.setEnabled(rmEnabled);
        rmResendField.setEnabled(rmEnabled);

        lifeTimeTextField.setEnabled(secConvEnabled);
        lifeTimeLabel.setEnabled(secConvEnabled);
        renewExpiredChBox.setEnabled(secConvEnabled);
        requireCancelChBox.setEnabled(secConvEnabled);
                
        timestampTimeoutLabel.setEnabled(timestampEnabled);
        timestampTimeoutField.setEnabled(timestampEnabled);        

    }
    
    private void setChBox(JCheckBox chBox, Boolean enable) {
        if (enable == null) {
            chBox.setSelected(false);
        } else {
            chBox.setSelected(enable);
        }
    }
    
    @Override
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
    }

    @Override
    public void rollbackValue(javax.swing.text.JTextComponent source) {
    }
    
    @Override
    protected void endUIChange() {
    }

    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }

    public javax.swing.JComponent getErrorComponent(String errorId) {
        return null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        lifeTimeLabel = new javax.swing.JLabel();
        lifeTimeTextField = new javax.swing.JTextField();
        renewExpiredChBox = new javax.swing.JCheckBox();
        requireCancelChBox = new javax.swing.JCheckBox();
        rmResendLabel = new javax.swing.JLabel();
        rmAckRequestLabel = new javax.swing.JLabel();
        rmResendField = new javax.swing.JTextField();
        rmAckRequestField = new javax.swing.JTextField();
        timestampTimeoutLabel = new javax.swing.JLabel();
        timestampTimeoutField = new javax.swing.JTextField();

        lifeTimeLabel.setText(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "LBL_AdvancedConfigPanel_SCTokenLifeTime")); // NOI18N

        renewExpiredChBox.setText(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "LBL_AdvancedConfigPanel_RenewExpired")); // NOI18N
        renewExpiredChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        renewExpiredChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        requireCancelChBox.setText(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "LBL_AdvancedConfigPanel_RequireCancel")); // NOI18N
        requireCancelChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        requireCancelChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        rmResendLabel.setText(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "LBL_AdvancedConfigPanel_ResendInterval")); // NOI18N

        rmAckRequestLabel.setText(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "LBL_AdvancedConfigPanel_AckRequestInterval")); // NOI18N

        timestampTimeoutLabel.setText(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "LBL_AdvancedConfigPanel_TimestampTimeout")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(rmResendLabel)
                            .add(rmAckRequestLabel)
                            .add(timestampTimeoutLabel)
                            .add(lifeTimeLabel))
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lifeTimeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 53, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(timestampTimeoutField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(rmAckRequestField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 53, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(rmResendField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 53, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(requireCancelChBox)
                    .add(renewExpiredChBox))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {lifeTimeTextField, rmAckRequestField, rmResendField, timestampTimeoutField}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rmResendLabel)
                    .add(rmResendField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rmAckRequestLabel)
                    .add(rmAckRequestField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(timestampTimeoutLabel)
                    .add(timestampTimeoutField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lifeTimeLabel)
                    .add(lifeTimeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(renewExpiredChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(requireCancelChBox)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lifeTimeLabel;
    private javax.swing.JTextField lifeTimeTextField;
    private javax.swing.JCheckBox renewExpiredChBox;
    private javax.swing.JCheckBox requireCancelChBox;
    private javax.swing.JTextField rmAckRequestField;
    private javax.swing.JLabel rmAckRequestLabel;
    private javax.swing.JTextField rmResendField;
    private javax.swing.JLabel rmResendLabel;
    private javax.swing.JTextField timestampTimeoutField;
    private javax.swing.JLabel timestampTimeoutLabel;
    // End of variables declaration//GEN-END:variables
    
}
