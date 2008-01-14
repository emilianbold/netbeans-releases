/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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

package org.netbeans.modules.websvc.wsitconf.ui.client;

import java.text.NumberFormat;
import javax.swing.JCheckBox;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMSunModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RequiredConfigurationHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 *
 * @author Martin Grebac
 */
public class AdvancedConfigPanelClient extends SectionInnerPanel {

    private WSDLModel serviceModel;
    
    private Binding binding;
    private boolean inSync = false;

    private static final String DEFAULT_LIFETIME = "36000";                     //NOI18N
//    private static final String DEFAULT_TIMEOUT = "5000";                     //NOI18N
    private static final String DEFAULT_RMRESENDINTERVAL = "2000";              //NOI18N
    private static final String DEFAULT_RMCLOSETIMEOUT = "0";                    //NOI18N
    private static final String DEFAULT_RMREQUESTACKINTERVAL = "200";           //NOI18N
    
    private DefaultFormatterFactory lifetimeDff = null;
    private DefaultFormatterFactory closeTimeoutDff = null;
    private DefaultFormatterFactory rmSendDff = null;
    private DefaultFormatterFactory rmReqDff = null;
    private DefaultFormatterFactory timeoutDff = null;
    private DefaultFormatterFactory freshnessDff = null;
    private DefaultFormatterFactory skewDff = null;
    
    public AdvancedConfigPanelClient(SectionView view, Binding binding, WSDLModel serviceModel) {
        super(view);
        this.serviceModel = serviceModel;
        this.binding = binding;
        
        lifetimeDff = new DefaultFormatterFactory();
        NumberFormat lifetimeFormat = NumberFormat.getIntegerInstance();
        lifetimeFormat.setGroupingUsed(false);
        lifetimeFormat.setParseIntegerOnly(true);
        lifetimeFormat.setMaximumFractionDigits(0);
        NumberFormatter lifetimeFormatter = new NumberFormatter(lifetimeFormat);
        lifetimeFormatter.setCommitsOnValidEdit(true);
        lifetimeFormatter.setMinimum(0);
        lifetimeDff.setDefaultFormatter(lifetimeFormatter);

        rmSendDff = new DefaultFormatterFactory();
        NumberFormat rmSendFormat = NumberFormat.getIntegerInstance();
        rmSendFormat.setGroupingUsed(false);
        rmSendFormat.setParseIntegerOnly(true);
        rmSendFormat.setMaximumFractionDigits(0);
        NumberFormatter rmSendFormatter = new NumberFormatter(rmSendFormat);
        rmSendFormatter.setCommitsOnValidEdit(true);
        rmSendFormatter.setMinimum(0);
        rmSendDff.setDefaultFormatter(rmSendFormatter);

        rmReqDff = new DefaultFormatterFactory();
        NumberFormat rmReqFormat = NumberFormat.getIntegerInstance();
        rmReqFormat.setGroupingUsed(false);
        rmReqFormat.setParseIntegerOnly(true);
        rmReqFormat.setMaximumFractionDigits(0);
        NumberFormatter rmReqFormatter = new NumberFormatter(rmReqFormat);
        rmReqFormatter.setCommitsOnValidEdit(true);
        rmReqFormatter.setMinimum(0);
        rmReqDff.setDefaultFormatter(rmReqFormatter);

        timeoutDff = new DefaultFormatterFactory();
        NumberFormat timeoutFormat = NumberFormat.getIntegerInstance();
        timeoutFormat.setGroupingUsed(false);
        timeoutFormat.setParseIntegerOnly(true);
        timeoutFormat.setMaximumFractionDigits(0);
        NumberFormatter timeoutFormatter = new NumberFormatter(timeoutFormat);
        timeoutFormatter.setCommitsOnValidEdit(true);
        timeoutFormatter.setMinimum(0);
        timeoutDff.setDefaultFormatter(timeoutFormatter);

        closeTimeoutDff = new DefaultFormatterFactory();
        NumberFormat rmCloseTimeoutFormat = NumberFormat.getIntegerInstance();
        rmCloseTimeoutFormat.setGroupingUsed(false);
        rmCloseTimeoutFormat.setParseIntegerOnly(true);
        rmCloseTimeoutFormat.setMaximumFractionDigits(0);
        NumberFormatter rmCloseTimeoutFormatter = new NumberFormatter(rmCloseTimeoutFormat);
        rmCloseTimeoutFormatter.setCommitsOnValidEdit(true);
        rmCloseTimeoutFormatter.setMinimum(0);
        closeTimeoutDff.setDefaultFormatter(rmCloseTimeoutFormatter);

        freshnessDff = new DefaultFormatterFactory();
        NumberFormat freshnessFormat = NumberFormat.getIntegerInstance();
        freshnessFormat.setGroupingUsed(false);
        freshnessFormat.setParseIntegerOnly(true);
        freshnessFormat.setMaximumFractionDigits(0);
        NumberFormatter freshnessFormatter = new NumberFormatter(freshnessFormat);
        freshnessFormatter.setCommitsOnValidEdit(true);
        freshnessFormatter.setMinimum(0);
        freshnessDff.setDefaultFormatter(freshnessFormatter);

        skewDff = new DefaultFormatterFactory();
        NumberFormat skewFormat = NumberFormat.getIntegerInstance();
        skewFormat.setGroupingUsed(false);
        skewFormat.setParseIntegerOnly(true);
        skewFormat.setMaximumFractionDigits(0);
        NumberFormatter skewFormatter = new NumberFormatter(skewFormat);
        skewFormatter.setCommitsOnValidEdit(true);
        skewFormatter.setMinimum(0);
        skewDff.setDefaultFormatter(skewFormatter);

        initComponents();

        lifeTimeLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        lifeTimeTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
//        timestampTimeoutLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
//        timestampTimeoutField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        renewExpiredChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        requireCancelChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        rmAckRequestField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        rmAckRequestLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        rmResendField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        rmResendLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        rmCloseTimeoutLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        rmCloseTimeoutField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        maxClockSkewField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        maxClockSkewLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        freshnessField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        freshnessLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        revocationChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());

        addImmediateModifier(rmAckRequestField);
        addImmediateModifier(rmCloseTimeoutField);
        addImmediateModifier(rmResendField);
        addImmediateModifier(lifeTimeTextField);
//        addImmediateModifier(timestampTimeoutField);
        addImmediateModifier(renewExpiredChBox);
        addImmediateModifier(requireCancelChBox);
        addImmediateModifier(maxClockSkewField);
        addImmediateModifier(freshnessField);
        addImmediateModifier(revocationChBox);

        sync();
    }

    public void sync() {
        inSync = true;

        String lifeTime = ProprietarySecurityPolicyModelHelper.getLifeTime(binding, true);
        if (lifeTime == null) {
            lifeTimeTextField.setText(DEFAULT_LIFETIME);
        } else {
            lifeTimeTextField.setText(lifeTime);
        } 

        String skew = ProprietarySecurityPolicyModelHelper.getMaxClockSkew(binding);
        if (skew == null) {
            maxClockSkewField.setText(ProprietarySecurityPolicyModelHelper.DEFAULT_MAXCLOCKSKEW);
        } else {
            maxClockSkewField.setText(skew);
        } 

        String freshness = ProprietarySecurityPolicyModelHelper.getTimestampFreshness(binding);
        if (freshness == null) {
            freshnessField.setText(ProprietarySecurityPolicyModelHelper.DEFAULT_TIMESTAMPFRESHNESS);
        } else {
            freshnessField.setText(freshness);
        } 
        
//        String timeout = ProprietarySecurityPolicyModelHelper.getTimestampTimeout(binding, true);
//        if (timeout == null) {
//            timestampTimeoutField.setText(DEFAULT_TIMEOUT);
//        } else {
//            timestampTimeoutField.setText(timeout);
//        } 
        
        String rmResendInterval = RMSunModelHelper.getResendInterval(binding);
        if (rmResendInterval == null) {
            rmResendField.setText(DEFAULT_RMRESENDINTERVAL);
        } else {
            rmResendField.setText(rmResendInterval);
        }

        String rmCloseTimeout = RMSunModelHelper.getCloseTimeout(binding);
        if (rmCloseTimeout == null) {
            rmCloseTimeoutField.setText(DEFAULT_RMCLOSETIMEOUT);
        } else {
            rmCloseTimeoutField.setText(rmCloseTimeout);
        }
        
        String rmAckRequest = RMSunModelHelper.getAckRequestInterval(binding);
        if (rmAckRequest == null) {
            rmAckRequestField.setText(DEFAULT_RMREQUESTACKINTERVAL);
        } else {
            rmAckRequestField.setText(rmAckRequest);
        }

        setChBox(renewExpiredChBox, ProprietarySecurityPolicyModelHelper.isRenewExpired(binding));
        setChBox(requireCancelChBox, ProprietarySecurityPolicyModelHelper.isRequireCancel(binding));

        setChBox(revocationChBox, ProprietarySecurityPolicyModelHelper.isRevocationEnabled(binding));

        enableDisable();
        
        inSync = false;
    }

    @Override
    public void setValue(javax.swing.JComponent source, Object value) {
        if (!inSync) {
            if (source.equals(lifeTimeTextField)) {
                String lifeTime = ((Integer) lifeTimeTextField.getValue()).toString();
                if ((lifeTime == null) || (lifeTime.length() == 0) || (DEFAULT_LIFETIME.equals(lifeTime))) {
                    ProprietarySecurityPolicyModelHelper.setLifeTime(binding, null, true);
                } else {
                    ProprietarySecurityPolicyModelHelper.setLifeTime(binding, lifeTime, true);
                }
            }

            if (source.equals(maxClockSkewField)) {
                String skew = ((Integer) maxClockSkewField.getValue()).toString();
                if ((skew == null) || (skew.length() == 0) || 
                    (ProprietarySecurityPolicyModelHelper.DEFAULT_MAXCLOCKSKEW.equals(skew))) {
                        ProprietarySecurityPolicyModelHelper.setMaxClockSkew(binding, null, true);
                } else {
                    ProprietarySecurityPolicyModelHelper.setMaxClockSkew(binding, skew, true);
                }
            }

            if (source.equals(freshnessField)) {
                String freshness = ((Integer) freshnessField.getValue()).toString();
                if ((freshness == null) || (freshness.length() == 0) || 
                    (ProprietarySecurityPolicyModelHelper.DEFAULT_TIMESTAMPFRESHNESS.equals(freshness))) {
                        ProprietarySecurityPolicyModelHelper.setTimestampFreshness(binding, null, true);
                } else {
                    ProprietarySecurityPolicyModelHelper.setTimestampFreshness(binding, freshness, true);
                }
            }
            
//            if (source.equals(timestampTimeoutField)) {
//                String timeout = timestampTimeoutField.getText();
//                if ((timeout == null) || (timeout.length() == 0) || (DEFAULT_TIMEOUT.equals(timeout))) {
//                    ProprietarySecurityPolicyModelHelper.setTimestampTimeout(binding, null, true);
//                } else {
//                    ProprietarySecurityPolicyModelHelper.setTimestampTimeout(binding, timeout, true);
//                }
//            }
            
            if (source.equals(rmResendField)) {
                String resendInt = ((Integer)rmResendField.getValue()).toString();
                if ((resendInt == null) || (resendInt.length() == 0) || (DEFAULT_RMRESENDINTERVAL.equals(resendInt))) {
                    RMSunModelHelper.setResendInterval(binding, null);
                } else {
                    RMSunModelHelper.setResendInterval(binding, resendInt);
                }
            }

            if (source.equals(rmCloseTimeoutField)) {
                String closeTimeout = ((Integer)rmCloseTimeoutField.getValue()).toString();
                if ((closeTimeout == null) || (closeTimeout.length() == 0) || (DEFAULT_RMCLOSETIMEOUT.equals(closeTimeout))) {
                    RMSunModelHelper.setCloseTimeout(binding, null);
                } else {
                    RMSunModelHelper.setCloseTimeout(binding, closeTimeout);
                }
            }
            
            if (source.equals(rmAckRequestField)) {
                String ackRequestInt = ((Integer)rmAckRequestField.getValue()).toString();
                if ((ackRequestInt == null) || (ackRequestInt.length() == 0) || (DEFAULT_RMREQUESTACKINTERVAL.equals(ackRequestInt))) {
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

            if (source.equals(revocationChBox)) {
                ProprietarySecurityPolicyModelHelper.setRevocation(binding, revocationChBox.isSelected(), true);
            }
        }
    }

    private void enableDisable() {
        Binding serviceBinding = PolicyModelHelper.getBinding(serviceModel, binding.getName());
        boolean rmEnabled = RMModelHelper.isRMEnabled(serviceBinding);
//        boolean timestampEnabled = SecurityPolicyModelHelper.isIncludeTimestamp(serviceBinding); 
        boolean secConvConfigRequired = RequiredConfigurationHelper.isSecureConversationParamRequired(serviceBinding);

        rmAckRequestLabel.setEnabled(rmEnabled);
        rmAckRequestField.setEnabled(rmEnabled);
        rmResendLabel.setEnabled(rmEnabled);
        rmResendField.setEnabled(rmEnabled);
        rmCloseTimeoutField.setEnabled(rmEnabled);
        rmCloseTimeoutLabel.setEnabled(rmEnabled);

        lifeTimeTextField.setEnabled(secConvConfigRequired);
        lifeTimeLabel.setEnabled(secConvConfigRequired);
        renewExpiredChBox.setEnabled(secConvConfigRequired);
        requireCancelChBox.setEnabled(secConvConfigRequired);

        boolean security = SecurityPolicyModelHelper.isSecurityEnabled(serviceBinding);
        requireCancelChBox.setEnabled(security);
        maxClockSkewField.setEnabled(security);
        freshnessField.setEnabled(security);
        
//        timestampTimeoutLabel.setEnabled(timestampEnabled);
//        timestampTimeoutField.setEnabled(timestampEnabled);
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lifeTimeLabel = new javax.swing.JLabel();
        renewExpiredChBox = new javax.swing.JCheckBox();
        requireCancelChBox = new javax.swing.JCheckBox();
        rmResendLabel = new javax.swing.JLabel();
        rmAckRequestLabel = new javax.swing.JLabel();
        rmResendField = new javax.swing.JFormattedTextField();
        rmAckRequestField = new javax.swing.JFormattedTextField();
        lifeTimeTextField = new javax.swing.JFormattedTextField();
        rmCloseTimeoutLabel = new javax.swing.JLabel();
        rmCloseTimeoutField = new javax.swing.JFormattedTextField();
        maxClockSkewLabel = new javax.swing.JLabel();
        maxClockSkewField = new javax.swing.JFormattedTextField();
        freshnessLabel = new javax.swing.JLabel();
        freshnessField = new javax.swing.JFormattedTextField();
        revocationChBox = new javax.swing.JCheckBox();

        lifeTimeLabel.setLabelFor(lifeTimeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(lifeTimeLabel, org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "LBL_AdvancedConfigPanel_SCTokenLifeTime")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(renewExpiredChBox, org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "LBL_AdvancedConfigPanel_RenewExpired")); // NOI18N
        renewExpiredChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        renewExpiredChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(requireCancelChBox, org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "LBL_AdvancedConfigPanel_RequireCancel")); // NOI18N
        requireCancelChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        requireCancelChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        rmResendLabel.setLabelFor(rmResendField);
        org.openide.awt.Mnemonics.setLocalizedText(rmResendLabel, org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "LBL_AdvancedConfigPanel_ResendInterval")); // NOI18N

        rmAckRequestLabel.setLabelFor(rmAckRequestField);
        org.openide.awt.Mnemonics.setLocalizedText(rmAckRequestLabel, org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "LBL_AdvancedConfigPanel_AckRequestInterval")); // NOI18N

        rmResendField.setFormatterFactory(rmSendDff);

        rmAckRequestField.setFormatterFactory(rmReqDff);

        lifeTimeTextField.setFormatterFactory(lifetimeDff);

        rmCloseTimeoutLabel.setLabelFor(rmCloseTimeoutField);
        org.openide.awt.Mnemonics.setLocalizedText(rmCloseTimeoutLabel, org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "LBL_AdvancedConfigPanel_CloseTimeout")); // NOI18N

        rmCloseTimeoutField.setFormatterFactory(closeTimeoutDff);

        maxClockSkewLabel.setLabelFor(rmResendField);
        org.openide.awt.Mnemonics.setLocalizedText(maxClockSkewLabel, org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "LBL_AdvancedConfigPanel_MaxClockSkew")); // NOI18N

        maxClockSkewField.setFormatterFactory(skewDff);

        freshnessLabel.setLabelFor(rmResendField);
        org.openide.awt.Mnemonics.setLocalizedText(freshnessLabel, org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "LBL_AdvancedConfigPanel_TimestampFreshness")); // NOI18N

        freshnessField.setFormatterFactory(freshnessDff);

        org.openide.awt.Mnemonics.setLocalizedText(revocationChBox, org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "LBL_AdvancedConfigPanel_Revocation")); // NOI18N
        revocationChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        revocationChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(renewExpiredChBox)
                    .add(requireCancelChBox)
                    .add(revocationChBox)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(maxClockSkewLabel)
                            .add(freshnessLabel))
                        .add(37, 37, 37)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(freshnessField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                            .add(maxClockSkewField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(rmResendLabel)
                            .add(rmCloseTimeoutLabel)
                            .add(rmAckRequestLabel)
                            .add(lifeTimeLabel))
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lifeTimeTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                            .add(rmResendField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                            .add(rmCloseTimeoutField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                            .add(rmAckRequestField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rmResendLabel)
                    .add(rmResendField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rmCloseTimeoutLabel)
                    .add(rmCloseTimeoutField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rmAckRequestLabel)
                    .add(rmAckRequestField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lifeTimeLabel)
                    .add(lifeTimeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(renewExpiredChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(requireCancelChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(maxClockSkewLabel)
                    .add(maxClockSkewField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(freshnessLabel)
                    .add(freshnessField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(revocationChBox)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {freshnessField, lifeTimeTextField, maxClockSkewField, rmAckRequestField, rmCloseTimeoutField, rmResendField}, org.jdesktop.layout.GroupLayout.VERTICAL);

        lifeTimeLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "ACSN_AdvancedConfigPanel_SCTokenLifeTime")); // NOI18N
        lifeTimeLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "ACSD_AdvancedConfigPanel_SCTokenLifeTime")); // NOI18N
        renewExpiredChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "ACSN_AdvancedConfigPanel_RenewExpired")); // NOI18N
        renewExpiredChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "ACSD_AdvancedConfigPanel_RenewExpired")); // NOI18N
        requireCancelChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "ACSN_AdvancedConfigPanel_RequireCancel")); // NOI18N
        requireCancelChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "ACSD_AdvancedConfigPanel_RequireCancel")); // NOI18N
        rmResendLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "ACSN_AdvancedConfigPanel_ResendInterval")); // NOI18N
        rmResendLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "ACSD_AdvancedConfigPanel_ResendInterval")); // NOI18N
        rmAckRequestLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "ACSN_AdvancedConfigPanel_AckRequestInterval")); // NOI18N
        rmAckRequestLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "ACSD_AdvancedConfigPanel_AckRequestInterval")); // NOI18N
        rmCloseTimeoutLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "ACSN_AdvancedConfigPanel_CloseTimeout")); // NOI18N
        rmCloseTimeoutLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "ACSD_AdvancedConfigPanel_CloseTimeout")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFormattedTextField freshnessField;
    private javax.swing.JLabel freshnessLabel;
    private javax.swing.JLabel lifeTimeLabel;
    private javax.swing.JFormattedTextField lifeTimeTextField;
    private javax.swing.JFormattedTextField maxClockSkewField;
    private javax.swing.JLabel maxClockSkewLabel;
    private javax.swing.JCheckBox renewExpiredChBox;
    private javax.swing.JCheckBox requireCancelChBox;
    private javax.swing.JCheckBox revocationChBox;
    private javax.swing.JFormattedTextField rmAckRequestField;
    private javax.swing.JLabel rmAckRequestLabel;
    private javax.swing.JFormattedTextField rmCloseTimeoutField;
    private javax.swing.JLabel rmCloseTimeoutLabel;
    private javax.swing.JFormattedTextField rmResendField;
    private javax.swing.JLabel rmResendLabel;
    // End of variables declaration//GEN-END:variables
    
}
