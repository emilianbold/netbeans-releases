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

package org.netbeans.modules.identity.profile.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.identity.profile.api.configurator.Configurator.AccessMethod;
import org.netbeans.modules.identity.profile.api.configurator.ConfiguratorException;
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
    private boolean disabled = false;

    /** Creates new form WSPSecurityPanel */
    public WSCSecurityPanel(SectionNodeView view, J2eeProjectHelper helper) {
        super(view);
        initComponents();

        errorLabel.setText(""); //NOI18N
        configurators = new ArrayList<ProviderConfigurator>();

        try {
            List<String> services = helper.getAllServiceNames();

            for (String service : services) {
                configurators.add(ProviderConfigurator.getConfigurator(service, Type.WSC, AccessMethod.FILE, helper.getConfigPath(), helper.getServerID()));
            }
        } catch (ConfiguratorException ex) {
            errorLabel.setText(ex.getMessage());
            disabled = true;
        }

        this.helper = helper;

        if (!disabled) {
            if (helper.isSecurityEnabled()) {
                enableSecurityCB.setSelected(true);
            } else {
                enableSecurityCB.setSelected(false);
            }

            for (ProviderConfigurator configurator : configurators) {
                configurator.addModifier(Configurable.SECURITY_MECH, requestSecMechCB, (helper.getProjectType() == ProjectType.WEB) ? configurator.getSecMechHelper().getAllWSCSecurityMechanisms() : configurator.getSecMechHelper().getAllMessageLevelSecurityMechanisms());

                configurator.addModifier(Configurable.SIGN_RESPONSE, signResponseCB);
                configurator.addModifier(Configurable.USE_DEFAULT_KEYSTORE, useDefaultKeyStoreCB);
                configurator.addModifier(Configurable.KEYSTORE_LOCATION, keystoreLocationTF);
                configurator.addModifier(Configurable.KEYSTORE_PASSWORD, keystorePasswordTF);
                configurator.addModifier(Configurable.KEY_ALIAS, keyAliasTF);
                configurator.addModifier(Configurable.KEY_PASSWORD, this.keyPasswordTF);
                configurator.addModifier(Configurable.USERNAME, userNameTF);
                configurator.addModifier(Configurable.PASSWORD, passwordTF);

                configurator.addErrorComponent(errorLabel);
            }
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
        if (disabled) {
            disableAll();
            enableSecurityCB.setEnabled(false);
            return;
        }

        if (helper.isWsitSecurityEnabled()) {
            enableSecurityCB.setEnabled(false);
            errorLabel.setText(NbBundle.getMessage(WSCSecurityPanel.class, "MSG_WsitEnabled"));
        } else {
            enableSecurityCB.setEnabled(true);
            errorLabel.setText(""); //NOI18N
        }

        if (enableSecurityCB.isSelected() && enableSecurityCB.isEnabled()) {
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
        } else {
            disableAll();
        }

        SecurityMechanism secMech = (SecurityMechanism) requestSecMechCB.getSelectedItem();

        if (secMech.isPasswordCredentialRequired() && requestSecMechCB.isEnabled()) {
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

    private void disableAll() {
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
        browseButton.setEnabled(false);
        userNameLabel.setVisible(false);
        userNameTF.setVisible(false);
        passwordLabel.setVisible(false);
        passwordTF.setVisible(false);
    }

    public void save() {
        if (!disabled) {
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
                for (ProviderConfigurator configurator : configurators) {
                    configurator.disable();
                    configurator.save();
                }
            }
        }

        for (ProviderConfigurator configurator : configurators) {
            configurator.close();
        }
        helper.clearTransientState();
    }

    public void cancel() {
        for (ProviderConfigurator configurator : configurators) {
            configurator.close();
        }
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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
        browseButton = new javax.swing.JButton();
        keystorePasswordTF = new javax.swing.JPasswordField();
        keyPasswordTF = new javax.swing.JPasswordField();
        useDefaultKeyStoreCB = new javax.swing.JCheckBox();
        passwordTF = new javax.swing.JPasswordField();
        errorLabel = new javax.swing.JLabel();

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

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/profile/ui/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(enableSecurityCB, bundle.getString("LBL_EnableSecurity")); // NOI18N
        enableSecurityCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        enableSecurityCB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        enableSecurityCB.setOpaque(false);
        enableSecurityCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableSecurityCBActionPerformed(evt);
            }
        });

        secMechLabel.setText(bundle.getString("LBL_SecurityMechanisms")); // NOI18N

        requestLabel.setLabelFor(requestSecMechCB);
        org.openide.awt.Mnemonics.setLocalizedText(requestLabel, bundle.getString("LBL_Request")); // NOI18N

        requestSecMechCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                requestSecMechCBActionPerformed(evt);
            }
        });

        userNameLabel.setLabelFor(userNameTF);
        org.openide.awt.Mnemonics.setLocalizedText(userNameLabel, bundle.getString("LBL_UserName")); // NOI18N

        passwordLabel.setLabelFor(passwordTF);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, bundle.getString("LBL_Password")); // NOI18N

        responseLabel.setText(bundle.getString("LBL_Response")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(signResponseCB, bundle.getString("LBL_VerifyResponse")); // NOI18N
        signResponseCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        signResponseCB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        signResponseCB.setOpaque(false);

        certSettingsLabel.setText(bundle.getString("LBL_CertificateSettings")); // NOI18N

        keystoreLocationLabel.setLabelFor(keystoreLocationTF);
        org.openide.awt.Mnemonics.setLocalizedText(keystoreLocationLabel, bundle.getString("LBL_KeyStoreLocation")); // NOI18N

        keystorePasswordLabel.setLabelFor(keystorePasswordTF);
        org.openide.awt.Mnemonics.setLocalizedText(keystorePasswordLabel, bundle.getString("LBL_KeystorePassword")); // NOI18N

        keyAliasLabel.setLabelFor(keyAliasTF);
        org.openide.awt.Mnemonics.setLocalizedText(keyAliasLabel, bundle.getString("LBL_KeyAlias")); // NOI18N

        keyAliasPasswordLabel.setLabelFor(keyPasswordTF);
        org.openide.awt.Mnemonics.setLocalizedText(keyAliasPasswordLabel, bundle.getString("LBL_KeyAliasPassword")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, bundle.getString("LBL_Browse")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(useDefaultKeyStoreCB, bundle.getString("LBL_UseDefaultKeyStore")); // NOI18N
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(secMechLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(requestLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(requestSecMechCB, 0, 370, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(passwordLabel)
                                    .addComponent(userNameLabel))
                                .addGap(6, 6, 6)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(passwordTF, javax.swing.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE)
                                    .addComponent(userNameTF, javax.swing.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(responseLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(signResponseCB))))
                    .addComponent(certSettingsLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(keystoreLocationLabel)
                                .addGap(10, 10, 10)
                                .addComponent(keystoreLocationTF, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE))
                            .addComponent(useDefaultKeyStoreCB)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(keystorePasswordLabel)
                                    .addComponent(keyAliasLabel)
                                    .addComponent(keyAliasPasswordLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(keyPasswordTF, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                                    .addComponent(keyAliasTF, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                                    .addComponent(keystorePasswordTF, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseButton))
                    .addComponent(enableSecurityCB)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE)
                    .addComponent(errorLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(enableSecurityCB, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(secMechLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(requestLabel)
                    .addComponent(requestSecMechCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userNameTF, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(userNameLabel))
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(passwordTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(responseLabel)
                    .addComponent(signResponseCB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(certSettingsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(useDefaultKeyStoreCB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keystoreLocationTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton)
                    .addComponent(keystoreLocationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keystorePasswordLabel)
                    .addComponent(keystorePasswordTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keyAliasLabel)
                    .addComponent(keyAliasTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keyAliasPasswordLabel)
                    .addComponent(keyPasswordTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(errorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
        // TODO add your handling code here:
        updateVisualState();
    }//GEN-LAST:event_formFocusGained

    private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
        // TODO add your handling code here:
        //requestFocusInWindow();
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

        if (returnVal == JFileChooser.APPROVE_OPTION) {
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
    private javax.swing.JCheckBox signResponseCB;
    private javax.swing.JCheckBox useDefaultKeyStoreCB;
    private javax.swing.JLabel userNameLabel;
    private javax.swing.JTextField userNameTF;
    // End of variables declaration//GEN-END:variables
}
