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

package org.netbeans.modules.websvc.wsitconf.ui.service.subpanels;

import java.text.NumberFormat;
import java.util.Set;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import org.jdesktop.layout.GroupLayout;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.ui.ClassDialog;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.STSConfiguration;
import org.netbeans.modules.xml.wsdl.model.Binding;

/**
 *
 * @author Martin Grebac
 */
public class STSConfigServicePanel extends JPanel {
    
    private Binding binding;
    private Project project;

    private boolean inSync = false;
    
    private DefaultFormatterFactory lifeTimeDff = null;
    
    /**
     * Creates new form STSConfigServicePanel
     */
    public STSConfigServicePanel( Project p, Binding binding) {
        this.project = p;
        this.binding = binding;

        lifeTimeDff = new DefaultFormatterFactory();
        NumberFormat lifetimeFormat = NumberFormat.getIntegerInstance();
        lifetimeFormat.setGroupingUsed(false);
        lifetimeFormat.setParseIntegerOnly(true);
        lifetimeFormat.setMaximumFractionDigits(0);
        NumberFormatter lifetimeFormatter = new NumberFormatter(lifetimeFormat);
        lifetimeFormatter.setCommitsOnValidEdit(true);
        lifetimeFormatter.setMinimum(0);
        lifeTimeDff.setDefaultFormatter(lifetimeFormatter);

        initComponents();

        inSync = true;
        ServiceProvidersTablePanel.ServiceProvidersTableModel tablemodel = new ServiceProvidersTablePanel.ServiceProvidersTableModel();
        this.remove(serviceProvidersPanel);
        
        STSConfiguration stsConfig = ProprietarySecurityPolicyModelHelper.getSTSConfiguration(binding);
        if (stsConfig == null) {
            stsConfig = ProprietarySecurityPolicyModelHelper.createSTSConfiguration(binding);
        }
        serviceProvidersPanel = new ServiceProvidersTablePanel(tablemodel, stsConfig);
        ((ServiceProvidersTablePanel)serviceProvidersPanel).populateModel();
        inSync = false;

        sync();
        
    }

    private void sync() {
        inSync = true;

        String lifeTime = ProprietarySecurityPolicyModelHelper.getSTSLifeTime(binding);
        if (lifeTime == null) { // no setup exists yet - set the default
            setLifeTime(ProprietarySecurityPolicyModelHelper.DEFAULT_LIFETIME);
            ProprietarySecurityPolicyModelHelper.setSTSLifeTime(binding, ProprietarySecurityPolicyModelHelper.DEFAULT_LIFETIME);
        } else {
            setLifeTime(lifeTime);
        } 

        boolean encryptKey = ProprietarySecurityPolicyModelHelper.getSTSEncryptKey(binding);
        setChBox(encryptKeyChBox, encryptKey);

        boolean encryptToken = ProprietarySecurityPolicyModelHelper.getSTSEncryptToken(binding);
        setChBox(encryptTokenChBox, encryptToken);
        
        String issuer = ProprietarySecurityPolicyModelHelper.getSTSIssuer(binding);
        if (issuer != null) {
            setIssuer(issuer);
        } 
        
        String cclass = ProprietarySecurityPolicyModelHelper.getSTSContractClass(binding);
        if (cclass == null) { // no setup exists yet - set the default
            setContractClass(ProprietarySecurityPolicyModelHelper.DEFAULT_CONTRACT_CLASS);
            ProprietarySecurityPolicyModelHelper.setSTSContractClass(binding, ProprietarySecurityPolicyModelHelper.DEFAULT_CONTRACT_CLASS);
        } else {
            setContractClass(cclass);
        } 
        
        refreshPanels();
        
        inSync = false;
    }
    
    private void setLifeTime(String time) {
        this.lifeTimeTextField.setText(time);
    }

    private void setIssuer(String issuer) {
        this.issuerField.setText(issuer);
    }
    
    private void setChBox(JCheckBox chBox, Boolean enable) {
        if (enable == null) {
            chBox.setSelected(false);
        } else {
            chBox.setSelected(enable);
        }
    }
    
    private void setContractClass(String classname) {
        this.contractTextField.setText(classname);
    }

    private void refreshPanels() {
        updateLayout();
    }
    
    private void updateLayout() {
        GroupLayout layout = (GroupLayout)this.getLayout();
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(serviceProvidersPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(2, 2, 2)
                                .add(lifeTimeLabel))
                            .add(contractLabel)
                            .add(issuerLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lifeTimeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(contractTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(contractButton))
                            .add(issuerField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE))
                        .add(10, 10, 10))
                    .add(layout.createSequentialGroup()
                        .add(encryptKeyChBox)
                        .addContainerGap(493, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(encryptTokenChBox)
                        .addContainerGap(483, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(issuerLabel)
                    .add(issuerField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(contractLabel)
                    .add(contractButton)
                    .add(contractTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(8, 8, 8)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lifeTimeLabel)
                    .add(lifeTimeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(encryptKeyChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(encryptTokenChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(serviceProvidersPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        serviceProvidersPanel = new javax.swing.JPanel();
        lifeTimeLabel = new javax.swing.JLabel();
        contractLabel = new javax.swing.JLabel();
        contractTextField = new javax.swing.JTextField();
        contractButton = new javax.swing.JButton();
        issuerLabel = new javax.swing.JLabel();
        issuerField = new javax.swing.JTextField();
        encryptKeyChBox = new javax.swing.JCheckBox();
        encryptTokenChBox = new javax.swing.JCheckBox();
        lifeTimeTextField = new javax.swing.JFormattedTextField();

        serviceProvidersPanel.setAutoscrolls(true);

        org.jdesktop.layout.GroupLayout serviceProvidersPanelLayout = new org.jdesktop.layout.GroupLayout(serviceProvidersPanel);
        serviceProvidersPanel.setLayout(serviceProvidersPanelLayout);
        serviceProvidersPanelLayout.setHorizontalGroup(
            serviceProvidersPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 648, Short.MAX_VALUE)
        );
        serviceProvidersPanelLayout.setVerticalGroup(
            serviceProvidersPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 90, Short.MAX_VALUE)
        );

        lifeTimeLabel.setText(org.openide.util.NbBundle.getMessage(STSConfigServicePanel.class, "LBL_STSConfig_Lifetime")); // NOI18N

        contractLabel.setText(org.openide.util.NbBundle.getMessage(STSConfigServicePanel.class, "LBL_STSConfig_Contract")); // NOI18N

        contractTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                contractTextFieldKeyReleased(evt);
            }
        });

        contractButton.setText(org.openide.util.NbBundle.getMessage(STSConfigServicePanel.class, "LBL_STSConfig_Browse")); // NOI18N
        contractButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contractButtonActionPerformed(evt);
            }
        });

        issuerLabel.setText(org.openide.util.NbBundle.getMessage(STSConfigServicePanel.class, "LBL_STSConfig_Issuer")); // NOI18N

        issuerField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                issuerFieldKeyReleased(evt);
            }
        });

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/wsitconf/ui/service/subpanels/Bundle"); // NOI18N
        encryptKeyChBox.setText(bundle.getString("LBL_STSConfig_EncryptKey")); // NOI18N
        encryptKeyChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        encryptKeyChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        encryptKeyChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                encryptKeyChBoxActionPerformed(evt);
            }
        });

        encryptTokenChBox.setText(bundle.getString("LBL_STSConfig_EncryptToken")); // NOI18N
        encryptTokenChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        encryptTokenChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        encryptTokenChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                encryptTokenChBoxActionPerformed(evt);
            }
        });

        lifeTimeTextField.setFormatterFactory(lifeTimeDff);
        lifeTimeTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                lifeTimeTextFieldKeyReleased(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(serviceProvidersPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(2, 2, 2)
                                .add(lifeTimeLabel))
                            .add(contractLabel)
                            .add(issuerLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(issuerField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 446, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(lifeTimeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 95, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(layout.createSequentialGroup()
                                        .add(contractTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(contractButton)))))
                        .add(10, 10, 10))
                    .add(layout.createSequentialGroup()
                        .add(encryptKeyChBox)
                        .addContainerGap(523, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(encryptTokenChBox)
                        .addContainerGap(509, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(issuerLabel)
                    .add(issuerField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(contractLabel)
                    .add(contractButton)
                    .add(contractTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(8, 8, 8)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lifeTimeLabel)
                    .add(lifeTimeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(encryptKeyChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(encryptTokenChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(serviceProvidersPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void lifeTimeTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_lifeTimeTextFieldKeyReleased
        Object o = lifeTimeTextField.getValue();
        if (o instanceof Integer) {
            String ltime = o.toString();
            ProprietarySecurityPolicyModelHelper.setSTSLifeTime(binding, ltime);
        }
}//GEN-LAST:event_lifeTimeTextFieldKeyReleased

    private void encryptTokenChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_encryptTokenChBoxActionPerformed
        ProprietarySecurityPolicyModelHelper.setSTSEncryptKey(binding, encryptTokenChBox.isSelected());
    }//GEN-LAST:event_encryptTokenChBoxActionPerformed

    private void encryptKeyChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_encryptKeyChBoxActionPerformed
        ProprietarySecurityPolicyModelHelper.setSTSEncryptToken(binding, encryptKeyChBox.isSelected());
    }//GEN-LAST:event_encryptKeyChBoxActionPerformed

    private void contractTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_contractTextFieldKeyReleased
        String c = contractTextField.getText();
        ProprietarySecurityPolicyModelHelper.setSTSContractClass(binding, c);
    }//GEN-LAST:event_contractTextFieldKeyReleased

    private void issuerFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_issuerFieldKeyReleased
        String issuer = issuerField.getText();
        ProprietarySecurityPolicyModelHelper.setSTSIssuer(binding, issuer);
    }//GEN-LAST:event_issuerFieldKeyReleased

    private void contractButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contractButtonActionPerformed
        if (project != null) {
            ClassDialog classDialog = new ClassDialog(project, "com.sun.xml.ws.trust.WSTrustContract"); //NOI18N
            classDialog.show();
            if (classDialog.okButtonPressed()) {
                Set<String> selectedClasses = classDialog.getSelectedClasses();
                for (String selectedClass : selectedClasses) {
                    setContractClass(selectedClass);
                    ProprietarySecurityPolicyModelHelper.setSTSContractClass(binding, selectedClass);
                    break;
                }
            }
        }
    }//GEN-LAST:event_contractButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton contractButton;
    private javax.swing.JLabel contractLabel;
    private javax.swing.JTextField contractTextField;
    private javax.swing.JCheckBox encryptKeyChBox;
    private javax.swing.JCheckBox encryptTokenChBox;
    private javax.swing.JTextField issuerField;
    private javax.swing.JLabel issuerLabel;
    private javax.swing.JLabel lifeTimeLabel;
    private javax.swing.JFormattedTextField lifeTimeTextField;
    private javax.swing.JPanel serviceProvidersPanel;
    // End of variables declaration//GEN-END:variables
    
}
