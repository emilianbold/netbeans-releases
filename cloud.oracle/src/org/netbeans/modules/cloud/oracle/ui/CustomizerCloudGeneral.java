/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

/*
 * CustomizerGeneral.java
 *
 * Created on Sep 1, 2011, 11:24:16 AM
 */
package org.netbeans.modules.cloud.oracle.ui;

import java.io.File;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.libs.oracle.cloud.api.CloudSDKHelper;
import org.netbeans.modules.cloud.oracle.OracleInstance;
import org.netbeans.modules.cloud.oracle.OracleInstanceManager;

/**
 *
 * @author Petr Hejl
 */
public class CustomizerCloudGeneral extends javax.swing.JPanel implements DocumentListener {

    private final OracleInstance instance;
    
    /** Creates new form CustomizerGeneral */
    public CustomizerCloudGeneral(OracleInstance instance) {
        initComponents();
    
        adminUrlLabel.setVisible(OracleWizardComponent.SHOW_CLOUD_URLS);
        adminUrlField.setVisible(OracleWizardComponent.SHOW_CLOUD_URLS);
        instanceUrlLabel.setVisible(false);
        instanceUrlField.setVisible(false);
        cloudUrlLabel.setVisible(false);
        cloudUrlField.setVisible(false);
        
        this.instance = instance;
        serviceInstanceField.setText(instance.getServiceInstance());
        identityDomainField.setText(instance.getIdentityDomain());
        adminUrlField.setText(instance.getAdminURL());
        instanceUrlField.setText(instance.getInstanceURL());
        cloudUrlField.setText(instance.getCloudURL());
        usernameField.setText(OracleWizardComponent.getUnprefixedUserName(instance.getIdentityDomain(), instance.getUser()));
        passwordField.setText(instance.getPassword());
        sdkTextField.setText(instance.getSDKFolder());
        
        adminUrlField.getDocument().addDocumentListener(this);
        instanceUrlField.getDocument().addDocumentListener(this);
        cloudUrlField.getDocument().addDocumentListener(this);
        usernameField.getDocument().addDocumentListener(this);
        passwordField.getDocument().addDocumentListener(this);
        sdkTextField.getDocument().addDocumentListener(this);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        update(e);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        update(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        update(e);
    }

    private void update(DocumentEvent e) {
        if (adminUrlField.getDocument().equals(e.getDocument())
                && !adminUrlField.getText().equals(instance.getAdminURL())) {
            instance.setAdminURL(adminUrlField.getText());
            OracleInstanceManager.getDefault().update(instance);
            return;
        }
        
        if (instanceUrlField.getDocument().equals(e.getDocument())
                && !instanceUrlField.getText().equals(instance.getInstanceURL())) {
            instance.setInstanceURL(instanceUrlField.getText());
            OracleInstanceManager.getDefault().update(instance);
            return;
        }
        
        if (cloudUrlField.getDocument().equals(e.getDocument())
                && !cloudUrlField.getText().equals(instance.getCloudURL())) {
            instance.setCloudURL(cloudUrlField.getText());
            OracleInstanceManager.getDefault().update(instance);
            return;
        }
        
        if (usernameField.getDocument().equals(e.getDocument())
                && !usernameField.getText().equals(instance.getUser())) {
            instance.setUser(OracleWizardComponent.getPrefixedUserName(identityDomainField.getText(), usernameField.getText()));
            OracleInstanceManager.getDefault().update(instance);
            return;
        }
        
        if (passwordField.getDocument().equals(e.getDocument())
                && !String.valueOf(passwordField.getPassword()).equals(instance.getPassword())) {
            instance.setPassword(String.valueOf(passwordField.getPassword()));
            OracleInstanceManager.getDefault().update(instance);
            return;
        }
        
        if (sdkTextField.getDocument().equals(e.getDocument())
                && !sdkTextField.getText().equals(instance.getSDKFolder())) {
            instance.setSDKFolder(sdkTextField.getText());
            OracleInstanceManager.getDefault().update(instance);
            return;
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        adminUrlLabel = new javax.swing.JLabel();
        adminUrlField = new javax.swing.JTextField();
        instanceUrlLabel = new javax.swing.JLabel();
        instanceUrlField = new javax.swing.JTextField();
        cloudUrlLabel = new javax.swing.JLabel();
        cloudUrlField = new javax.swing.JTextField();
        usernameLabel = new javax.swing.JLabel();
        usernameField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        identityDomainLabel = new javax.swing.JLabel();
        serviceInstanceField = new javax.swing.JTextField();
        serviceInstanceLabel = new javax.swing.JLabel();
        identityDomainField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        sdkTextField = new javax.swing.JTextField();
        configureButton = new javax.swing.JButton();

        adminUrlLabel.setLabelFor(adminUrlField);
        org.openide.awt.Mnemonics.setLocalizedText(adminUrlLabel, org.openide.util.NbBundle.getMessage(CustomizerCloudGeneral.class, "CustomizerCloudGeneral.adminUrlLabel.text")); // NOI18N

        instanceUrlLabel.setLabelFor(instanceUrlField);
        org.openide.awt.Mnemonics.setLocalizedText(instanceUrlLabel, org.openide.util.NbBundle.getMessage(CustomizerCloudGeneral.class, "CustomizerCloudGeneral.instanceUrlLabel.text")); // NOI18N

        cloudUrlLabel.setLabelFor(cloudUrlField);
        org.openide.awt.Mnemonics.setLocalizedText(cloudUrlLabel, org.openide.util.NbBundle.getMessage(CustomizerCloudGeneral.class, "CustomizerCloudGeneral.cloudUrlLabel.text")); // NOI18N

        usernameLabel.setLabelFor(usernameField);
        org.openide.awt.Mnemonics.setLocalizedText(usernameLabel, org.openide.util.NbBundle.getMessage(CustomizerCloudGeneral.class, "CustomizerCloudGeneral.usernameLabel.text")); // NOI18N

        passwordLabel.setLabelFor(passwordField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(CustomizerCloudGeneral.class, "CustomizerCloudGeneral.passwordLabel.text")); // NOI18N

        identityDomainLabel.setLabelFor(identityDomainField);
        org.openide.awt.Mnemonics.setLocalizedText(identityDomainLabel, org.openide.util.NbBundle.getMessage(CustomizerCloudGeneral.class, "CustomizerCloudGeneral.identityDomainLabel.text")); // NOI18N

        serviceInstanceField.setEditable(false);

        serviceInstanceLabel.setLabelFor(serviceInstanceField);
        org.openide.awt.Mnemonics.setLocalizedText(serviceInstanceLabel, org.openide.util.NbBundle.getMessage(CustomizerCloudGeneral.class, "CustomizerCloudGeneral.serviceInstanceLabel.text")); // NOI18N

        identityDomainField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CustomizerCloudGeneral.class, "CustomizerCloudGeneral.jLabel1.text")); // NOI18N

        sdkTextField.setEditable(false);
        sdkTextField.setText(org.openide.util.NbBundle.getMessage(CustomizerCloudGeneral.class, "CustomizerCloudGeneral.sdkTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(configureButton, org.openide.util.NbBundle.getMessage(CustomizerCloudGeneral.class, "CustomizerCloudGeneral.configureButton.text")); // NOI18N
        configureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configureButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(instanceUrlLabel)
                    .addComponent(adminUrlLabel)
                    .addComponent(cloudUrlLabel)
                    .addComponent(passwordLabel)
                    .addComponent(usernameLabel)
                    .addComponent(serviceInstanceLabel)
                    .addComponent(identityDomainLabel)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sdkTextField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(configureButton))
                    .addComponent(cloudUrlField)
                    .addComponent(adminUrlField)
                    .addComponent(instanceUrlField)
                    .addComponent(serviceInstanceField)
                    .addComponent(identityDomainField, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(usernameField, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                            .addComponent(passwordField))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(identityDomainLabel)
                    .addComponent(identityDomainField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serviceInstanceLabel)
                    .addComponent(serviceInstanceField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(adminUrlLabel)
                    .addComponent(adminUrlField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(instanceUrlLabel)
                    .addComponent(instanceUrlField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cloudUrlLabel)
                    .addComponent(cloudUrlField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(sdkTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(configureButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameLabel)
                    .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void configureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureButtonActionPerformed
        File f = CloudSDKHelper.showConfigureSDKDialog(this);
        if (f != null) {
            sdkTextField.setText(f.getAbsolutePath());
        }
    }//GEN-LAST:event_configureButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField adminUrlField;
    private javax.swing.JLabel adminUrlLabel;
    private javax.swing.JTextField cloudUrlField;
    private javax.swing.JLabel cloudUrlLabel;
    private javax.swing.JButton configureButton;
    private javax.swing.JTextField identityDomainField;
    private javax.swing.JLabel identityDomainLabel;
    private javax.swing.JTextField instanceUrlField;
    private javax.swing.JLabel instanceUrlLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JTextField sdkTextField;
    private javax.swing.JTextField serviceInstanceField;
    private javax.swing.JLabel serviceInstanceLabel;
    private javax.swing.JTextField usernameField;
    private javax.swing.JLabel usernameLabel;
    // End of variables declaration//GEN-END:variables
}
