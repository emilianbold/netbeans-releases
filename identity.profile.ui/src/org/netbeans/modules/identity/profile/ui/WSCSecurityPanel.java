/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.identity.profile.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.identity.profile.api.configurator.Configurator.AccessMethod;
import org.netbeans.modules.identity.profile.api.configurator.ProviderConfigurator;
import org.netbeans.modules.identity.profile.api.configurator.ProviderConfigurator.Configurable;
import org.netbeans.modules.identity.profile.api.configurator.ProviderConfigurator.Type;
import org.netbeans.modules.identity.profile.api.configurator.SecurityMechanism;
import org.netbeans.modules.identity.profile.api.configurator.SecurityMechanismHelper;
import org.netbeans.modules.identity.profile.ui.support.J2eeProjectHelper;
import org.netbeans.modules.identity.profile.ui.support.J2eeProjectHelper.ProjectType;
import org.netbeans.modules.identity.server.manager.api.ServerManager;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.util.NbBundle;

/**
 * Visual panel for the WSC security panel.
 *
 * Created on April 14, 2006, 3:03 PM
 *
 * @author  ptliu
 * @author Srividhya Narayanan
 */
public class WSCSecurityPanel extends SectionNodeInnerPanel {
    private static final String JKS_EXTENSION = ".jks"; //NOI18N
    private static final String URN = "urn:"; //NOI18N
    
    private Collection<ProviderConfigurator> configurators;
    private J2eeProjectHelper helper;
    
    /** Creates new form WSPSecurityPanel */
    public WSCSecurityPanel(SectionNodeView view, J2eeProjectHelper helper) {
        super(view);
        initComponents();
        
        errorLabel.setText("");     //NOI18N
        
        configurators = new ArrayList<ProviderConfigurator>();
        
        try {
            List<String> services = helper.getAllServiceNames();
            
            for (String service : services) {
                configurators.add(ProviderConfigurator.getConfigurator(service,
                        Type.WSC, AccessMethod.FILE, helper.getConfigPath()));
            }
            
        } catch (RuntimeException ex) {
//            errorLabel.setText(ex.getMessage());
            return;
        }
        
        this.helper = helper;
        
        if (helper.isSecurityEnabled()) {
            enableSecurityCB.setSelected(true);
        } else {
            enableSecurityCB.setSelected(false);
        }
        
        for (ProviderConfigurator configurator : configurators) {
            
            configurator.addModifier(Configurable.SECURITY_MECH, requestSecMechCB,
                    (helper.getProjectType() == ProjectType.WEB) ?
                        SecurityMechanismHelper.getDefault().getAllWSCSecurityMechanisms() :
                        SecurityMechanismHelper.getDefault().getAllMessageLevelSecurityMechanisms());
            
            configurator.addModifier(Configurable.SIGN_RESPONSE, signResponseCB);
            configurator.addModifier(Configurable.USE_DEFAULT_KEYSTORE, useDefaultKeyStoreCB);
            configurator.addModifier(Configurable.KEYSTORE_LOCATION, keystoreLocationTF);
            configurator.addModifier(Configurable.KEYSTORE_PASSWORD, keystorePasswordTF);
            configurator.addModifier(Configurable.KEY_ALIAS, keyAliasTF);
            configurator.addModifier(Configurable.KEY_PASSWORD, this.keyPasswordTF);
            configurator.addModifier(Configurable.SERVER_PROPERTIES, serverCB,
                    ServerManager.getDefault().getAllServerProperties());
            configurator.addModifier(Configurable.USERNAME, userNameTF);
            configurator.addModifier(Configurable.PASSWORD, passwordTF);
            
            configurator.addErrorComponent(errorLabel);
        }
        
        updateVisualState();
    }
    
    public JComponent getErrorComponent(String errorId) {
        return null;
    }
    
    public void setValue(JComponent source, Object value) {
        
    }
    
    public void linkButtonPressed(Object ddBean, String ddProperty) {
        
    }
    
    private void updateVisualState() {
        if (helper.isWsitSecurityEnabled()) {
            enableSecurityCB.setEnabled(false);
            errorLabel.setText(NbBundle.getMessage(WSCSecurityPanel.class, 
                    "MSG_WsitEnabled"));
        } else {
            enableSecurityCB.setEnabled(true);
            errorLabel.setText("");         //NOI18N
        }
        
        if (enableSecurityCB.isSelected() &&
                enableSecurityCB.isEnabled()) {
            secMechLabel.setEnabled(true);
            requestLabel.setEnabled(true);
            requestSecMechCB.setEnabled(true);
            userNameLabel.setEnabled(true);
            userNameTF.setEnabled(true);
            passwordLabel.setEnabled(true);
            passwordTF.setEnabled(true);
            responseLabel.setEnabled(true);
            signResponseCB.setEnabled(true);
            certSettingsLabel.setEnabled(true);
            useDefaultKeyStoreCB.setEnabled(true);
            
            if (!useDefaultKeyStoreCB.isSelected()) {
                keystoreLocationLabel.setEnabled(true);
                keystoreLocationTF.setEnabled(true);
                keystorePasswordLabel.setEnabled(true);
                keystorePasswordTF.setEnabled(true);
                keyAliasLabel.setEnabled(true);
                keyAliasTF.setEnabled(true);
                keyAliasPasswordLabel.setEnabled(true);
                keyPasswordTF.setEnabled(true);
                browseButton.setEnabled(true);
            } else {
                keystoreLocationLabel.setEnabled(false);
                keystoreLocationTF.setEnabled(false);
                keystorePasswordLabel.setEnabled(false);
                keystorePasswordTF.setEnabled(false);
                keyAliasLabel.setEnabled(false);
                keyAliasTF.setEnabled(false);
                keyAliasPasswordLabel.setEnabled(false);
                keyPasswordTF.setEnabled(false);
                browseButton.setEnabled(false);
            }
            serverLabel.setEnabled(true);
            serverCB.setEnabled(true);
        } else {
            secMechLabel.setEnabled(false);
            requestLabel.setEnabled(false);
            requestSecMechCB.setEnabled(false);
            userNameLabel.setEnabled(false);
            userNameTF.setEnabled(false);
            passwordLabel.setEnabled(false);
            passwordTF.setEnabled(false);
            responseLabel.setEnabled(false);
            signResponseCB.setEnabled(false);
            certSettingsLabel.setEnabled(false);
            useDefaultKeyStoreCB.setEnabled(false);
            keystoreLocationLabel.setEnabled(false);
            keystoreLocationTF.setEnabled(false);
            keystorePasswordLabel.setEnabled(false);
            keystorePasswordTF.setEnabled(false);
            keyAliasLabel.setEnabled(false);
            keyAliasTF.setEnabled(false);
            keyAliasPasswordLabel.setEnabled(false);
            keyPasswordTF.setEnabled(false);
            serverLabel.setEnabled(false);
            serverCB.setEnabled(false);
            browseButton.setEnabled(false);
        }
        
        SecurityMechanism secMech = (SecurityMechanism) requestSecMechCB.getSelectedItem();
        
        if (secMech.isPasswordCredentialRequired() &&
                requestSecMechCB.isEnabled()) {
            userNameLabel.setVisible(true);
            userNameTF.setVisible(true);
            passwordLabel.setVisible(true);
            passwordTF.setVisible(true);
        } else {
            userNameLabel.setVisible(false);
            userNameTF.setVisible(false);
            passwordLabel.setVisible(false);
            passwordTF.setVisible(false);
        }
    }
    
    public void save() {
        if (enableSecurityCB.isSelected()) {
            for (ProviderConfigurator configurator : configurators) {
                configurator.save();
            }
          /*
            if (isLiberty()) {
                helper.addAMSecurityConstraint();
            } else {
                helper.removeAMSecurityConstraint();
            }
           */
            helper.enableWSCSecurity(isLiberty());
            
        } else {
            //helper.removeAMSecurityConstraint();
            helper.disableWSCSecurity();
        }
        
        helper.clearTransientState();
    }
    
    public void cancel() {
        helper.clearTransientState();
    }
    
    private boolean isLiberty() {
        SecurityMechanism secMech = (SecurityMechanism) requestSecMechCB.getSelectedItem();
        
        return secMech.isLiberty();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jSeparator2 = new javax.swing.JSeparator();
        enableSecurityCB = new javax.swing.JCheckBox();
        secMechLabel = new javax.swing.JLabel();
        requestLabel = new javax.swing.JLabel();
        requestSecMechCB = new javax.swing.JComboBox();
        userNameLabel = new javax.swing.JLabel();
        userNameTF = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        responseLabel = new javax.swing.JLabel();
        signResponseCB = new javax.swing.JCheckBox();
        certSettingsLabel = new javax.swing.JLabel();
        keystoreLocationLabel = new javax.swing.JLabel();
        keystoreLocationTF = new javax.swing.JTextField();
        keystorePasswordLabel = new javax.swing.JLabel();
        keyAliasLabel = new javax.swing.JLabel();
        keyAliasTF = new javax.swing.JTextField();
        keyAliasPasswordLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        serverLabel = new javax.swing.JLabel();
        serverCB = new javax.swing.JComboBox();
        browseButton = new javax.swing.JButton();
        keystorePasswordTF = new javax.swing.JPasswordField();
        keyPasswordTF = new javax.swing.JPasswordField();
        useDefaultKeyStoreCB = new javax.swing.JCheckBox();
        passwordTF = new javax.swing.JPasswordField();
        errorLabel = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();

        setEnabled(false);
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

        org.openide.awt.Mnemonics.setLocalizedText(enableSecurityCB, java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/profile/ui/Bundle").getString("LBL_EnableSecurity"));
        enableSecurityCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        enableSecurityCB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        enableSecurityCB.setOpaque(false);
        enableSecurityCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableSecurityCBActionPerformed(evt);
            }
        });

        secMechLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/profile/ui/Bundle").getString("LBL_SecurityMechanisms"));

        requestLabel.setLabelFor(requestSecMechCB);
        org.openide.awt.Mnemonics.setLocalizedText(requestLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/profile/ui/Bundle").getString("LBL_Request"));

        requestSecMechCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                requestSecMechCBActionPerformed(evt);
            }
        });

        userNameLabel.setLabelFor(userNameTF);
        org.openide.awt.Mnemonics.setLocalizedText(userNameLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/profile/ui/Bundle").getString("LBL_UserName"));

        passwordLabel.setLabelFor(passwordTF);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/profile/ui/Bundle").getString("LBL_Password"));

        responseLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/profile/ui/Bundle").getString("LBL_Response"));

        org.openide.awt.Mnemonics.setLocalizedText(signResponseCB, java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/profile/ui/Bundle").getString("LBL_VerifyResponse"));
        signResponseCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        signResponseCB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        signResponseCB.setOpaque(false);

        certSettingsLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/profile/ui/Bundle").getString("LBL_CertificateSettings"));

        keystoreLocationLabel.setLabelFor(keystoreLocationTF);
        org.openide.awt.Mnemonics.setLocalizedText(keystoreLocationLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/profile/ui/Bundle").getString("LBL_KeyStoreLocation"));

        keystorePasswordLabel.setLabelFor(keystorePasswordTF);
        org.openide.awt.Mnemonics.setLocalizedText(keystorePasswordLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/profile/ui/Bundle").getString("LBL_KeystorePassword"));

        keyAliasLabel.setLabelFor(keyAliasTF);
        org.openide.awt.Mnemonics.setLocalizedText(keyAliasLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/profile/ui/Bundle").getString("LBL_KeyAlias"));

        keyAliasPasswordLabel.setLabelFor(keyPasswordTF);
        org.openide.awt.Mnemonics.setLocalizedText(keyAliasPasswordLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/profile/ui/Bundle").getString("LBL_KeyAliasPassword"));

        serverLabel.setLabelFor(serverCB);
        org.openide.awt.Mnemonics.setLocalizedText(serverLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/profile/ui/Bundle").getString("LBL_Server"));

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/profile/ui/Bundle").getString("LBL_Browse"));
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(useDefaultKeyStoreCB, java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/profile/ui/Bundle").getString("LBL_UseDefaultKeyStore"));
        useDefaultKeyStoreCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useDefaultKeyStoreCB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useDefaultKeyStoreCB.setOpaque(false);
        useDefaultKeyStoreCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useDefaultKeyStoreCBActionPerformed(evt);
            }
        });

        errorLabel.setForeground(java.awt.Color.red);
        errorLabel.setText("Error:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(secMechLabel)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(requestLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(requestSecMechCB, 0, 322, Short.MAX_VALUE))
                            .add(layout.createSequentialGroup()
                                .add(10, 10, 10)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(passwordLabel)
                                    .add(userNameLabel))
                                .add(6, 6, 6)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(passwordTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                                    .add(userNameTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                            .add(layout.createSequentialGroup()
                                .add(responseLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(signResponseCB))))
                    .add(certSettingsLabel)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(keystoreLocationLabel)
                                .add(10, 10, 10)
                                .add(keystoreLocationTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE))
                            .add(useDefaultKeyStoreCB)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(keystorePasswordLabel)
                                    .add(keyAliasLabel)
                                    .add(keyAliasPasswordLabel))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(keyPasswordTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE))
                                    .add(keyAliasTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                                    .add(keystorePasswordTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE))))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton))
                    .add(enableSecurityCB)
                    .add(layout.createSequentialGroup()
                        .add(serverLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(serverCB, 0, 340, Short.MAX_VALUE))
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .add(errorLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(enableSecurityCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(secMechLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(requestLabel)
                    .add(requestSecMechCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(userNameTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(userNameLabel))
                .add(11, 11, 11)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(passwordLabel)
                    .add(passwordTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(responseLabel)
                    .add(signResponseCB))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(certSettingsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(useDefaultKeyStoreCB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keystoreLocationTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseButton)
                    .add(keystoreLocationLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keystorePasswordLabel)
                    .add(keystorePasswordTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keyAliasLabel)
                    .add(keyAliasTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keyAliasPasswordLabel)
                    .add(keyPasswordTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(11, 11, 11)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(serverLabel)
                    .add(serverCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(errorLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
// TODO add your handling code here:
        updateVisualState();
    }//GEN-LAST:event_formFocusGained
    
    private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
// TODO add your handling code here:
        requestFocusInWindow();
    }//GEN-LAST:event_formAncestorAdded
    
    private void useDefaultKeyStoreCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useDefaultKeyStoreCBActionPerformed
// TODO add your handling code here:
        updateVisualState();
    }//GEN-LAST:event_useDefaultKeyStoreCBActionPerformed
    
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
// TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File file) {
                if (file.isFile()) {
                    if (file.getName().endsWith(JKS_EXTENSION)) {
                        return true;
                    } else {
                        return false;
                    }
                }
                
                return true;
            }
            
            public String getDescription() {
                return NbBundle.getMessage(WSCSecurityPanel.class, "TXT_JavaKeyStore");
            }
        });
        
        int returnVal = chooser.showOpenDialog(this);
        
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            keystoreLocationTF.setText(chooser.getSelectedFile().getPath());
        }
    }//GEN-LAST:event_browseButtonActionPerformed
    
    private void requestSecMechCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_requestSecMechCBActionPerformed
// TODO add your handling code here:
        updateVisualState();
        
        if (isLiberty()) {
            List<String> endpointURIs = helper.getEndpointURI();
            int i = 0;
            
            for (ProviderConfigurator configurator : configurators) {
                configurator.setValue(Configurable.SERVICE_TYPE, URN + endpointURIs.get(i));
                i++;
            }
        } else {
            for (ProviderConfigurator configurator : configurators) {
                configurator.setValue(Configurable.SERVICE_TYPE, null);
            }
        }
    }//GEN-LAST:event_requestSecMechCBActionPerformed
    
    private void enableSecurityCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableSecurityCBActionPerformed
// TODO add your handling code here:
        if (enableSecurityCB.isSelected()) {
            helper.setTransientState(true);
        } else {
            helper.setTransientState(false);
        }
        
        updateVisualState();
    }//GEN-LAST:event_enableSecurityCBActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel certSettingsLabel;
    private javax.swing.JCheckBox enableSecurityCB;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel keyAliasLabel;
    private javax.swing.JLabel keyAliasPasswordLabel;
    private javax.swing.JTextField keyAliasTF;
    private javax.swing.JPasswordField keyPasswordTF;
    private javax.swing.JLabel keystoreLocationLabel;
    private javax.swing.JTextField keystoreLocationTF;
    private javax.swing.JLabel keystorePasswordLabel;
    private javax.swing.JPasswordField keystorePasswordTF;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JPasswordField passwordTF;
    private javax.swing.JLabel requestLabel;
    private javax.swing.JComboBox requestSecMechCB;
    private javax.swing.JLabel responseLabel;
    private javax.swing.JLabel secMechLabel;
    private javax.swing.JComboBox serverCB;
    private javax.swing.JLabel serverLabel;
    private javax.swing.JCheckBox signResponseCB;
    private javax.swing.JCheckBox useDefaultKeyStoreCB;
    private javax.swing.JLabel userNameLabel;
    private javax.swing.JTextField userNameTF;
    // End of variables declaration//GEN-END:variables
    
}
