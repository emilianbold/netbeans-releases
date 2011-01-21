/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

/*
 * CustomizerGeneral.java
 *
 * Created on 19.07.2010, 17:30:53
 */

package org.netbeans.modules.j2ee.weblogic9.ui.nodes;

import java.awt.Font;
import java.util.Properties;


import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLJpa2SwitchSupport;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author ads
 */
class CustomizerGeneral extends javax.swing.JPanel {

    private static final long serialVersionUID = 748111929912200475L;
    
    CustomizerGeneral(WLDeploymentManager manager) {
        this.manager = manager;
        initComponents();
        
        initValues();
    }

    private void initValues() {
        String userNameValue = manager.getInstanceProperties().getProperty(
                InstanceProperties.USERNAME_ATTR);
        userName.setText(userNameValue);
        userName.getDocument().addDocumentListener( 
                new PropertyDocumentListener(manager, InstanceProperties.USERNAME_ATTR,
                        userName));
        String passwd = manager.getInstanceProperties().getProperty(
                InstanceProperties.PASSWORD_ATTR);
        passwordField.setText( passwd );
        passwordField.getDocument().addDocumentListener( 
                new PropertyDocumentListener(manager, InstanceProperties.PASSWORD_ATTR,
                        passwordField));
        
        String domainRoot = manager.getInstanceProperties().getProperty( 
                WLPluginProperties.DOMAIN_ROOT_ATTR);
        domainFolder.setText( domainRoot );
        String domain = manager.getInstanceProperties().getProperty( WLPluginProperties.DOMAIN_NAME);
        String port = manager.getInstanceProperties().getProperty( WLPluginProperties.PORT_ATTR);
        Properties properties = null;
        if ( domain== null || port == null ){
            properties = WLPluginProperties.getDomainProperties(domainRoot);
        }
        if ( domain == null ){
            domain = properties.getProperty(WLPluginProperties.DOMAIN_NAME);
        }
        if ( port == null ){
            port = properties.getProperty(WLPluginProperties.PORT_ATTR);
        }
        if ( domain!= null ) {
            domainName.setText( domain );
        }
        if ( port!= null){
            serverPort.setText( port );
        }
        WLJpa2SwitchSupport support = manager.getJpa2SwitchSupport();
        if (support.isEnabledViaSmartUpdate()) {
            jpa2Button.setEnabled(false);
        } else {
            if(support.isEnabled()){
                org.openide.awt.Mnemonics.setLocalizedText(jpa2Button, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "LBL_DisableJPA2")); // NOI18N
            } else {
                org.openide.awt.Mnemonics.setLocalizedText(jpa2Button, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "LBL_EnableJPA2")); // NOI18N
            }
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

        domainNameLabel = new javax.swing.JLabel();
        domainName = new javax.swing.JTextField();
        domainFolderLabel = new javax.swing.JLabel();
        domainFolder = new javax.swing.JTextField();
        adminInfoLabel = new javax.swing.JLabel();
        userNameLabel = new javax.swing.JLabel();
        userName = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        showButton = new javax.swing.JButton();
        serverPortLabel = new javax.swing.JLabel();
        NoteChangesLabel = new javax.swing.JLabel();
        serverPort = new javax.swing.JTextField();
        jpa2Button = new javax.swing.JButton();

        domainNameLabel.setLabelFor(domainName);
        org.openide.awt.Mnemonics.setLocalizedText(domainNameLabel, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "LBL_CustomizerDomainName")); // NOI18N

        domainName.setEditable(false);

        domainFolderLabel.setLabelFor(domainFolder);
        org.openide.awt.Mnemonics.setLocalizedText(domainFolderLabel, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "LBL_DomainFolder")); // NOI18N

        domainFolder.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(adminInfoLabel, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "LBL_AdminInfo")); // NOI18N

        userNameLabel.setLabelFor(userName);
        org.openide.awt.Mnemonics.setLocalizedText(userNameLabel, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "LBL_UserName")); // NOI18N

        passwordLabel.setLabelFor(passwordField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "LBL_Password")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(showButton, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "LBL_ShowButton")); // NOI18N
        showButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showButtonActionPerformed(evt);
            }
        });

        serverPortLabel.setLabelFor(serverPort);
        org.openide.awt.Mnemonics.setLocalizedText(serverPortLabel, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "LBL_ServerPort")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(NoteChangesLabel, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "LBL_Note")); // NOI18N

        serverPort.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(jpa2Button, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "LBL_EnableJPA2")); // NOI18N
        jpa2Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jpa2ButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(adminInfoLabel)
                    .addComponent(NoteChangesLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(domainNameLabel)
                            .addComponent(domainFolderLabel)
                            .addComponent(userNameLabel)
                            .addComponent(passwordLabel)
                            .addComponent(serverPortLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(serverPort, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(domainFolder, javax.swing.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                            .addComponent(domainName, javax.swing.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(passwordField, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(userName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(showButton))))
                    .addComponent(jpa2Button))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(domainNameLabel)
                    .addComponent(domainName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(domainFolderLabel)
                    .addComponent(domainFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addComponent(adminInfoLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userNameLabel)
                    .addComponent(userName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(showButton))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverPortLabel)
                    .addComponent(serverPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 44, Short.MAX_VALUE)
                .addComponent(jpa2Button)
                .addGap(18, 18, 18)
                .addComponent(NoteChangesLabel)
                .addContainerGap())
        );

        domainNameLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACN_CustomizerDomainName")); // NOI18N
        domainNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_DomainName")); // NOI18N
        domainName.getAccessibleContext().setAccessibleName(domainNameLabel.getAccessibleContext().getAccessibleName());
        domainName.getAccessibleContext().setAccessibleDescription(domainNameLabel.getAccessibleContext().getAccessibleDescription());
        domainFolderLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_DomainFolder")); // NOI18N
        domainFolder.getAccessibleContext().setAccessibleName(domainFolderLabel.getAccessibleContext().getAccessibleName());
        domainFolder.getAccessibleContext().setAccessibleDescription(domainFolderLabel.getAccessibleContext().getAccessibleDescription());
        userNameLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSN_UserName")); // NOI18N
        userNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_Username")); // NOI18N
        userName.getAccessibleContext().setAccessibleName(userNameLabel.getAccessibleContext().getAccessibleName());
        userName.getAccessibleContext().setAccessibleDescription(userNameLabel.getAccessibleContext().getAccessibleDescription());
        passwordLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSN_Password")); // NOI18N
        passwordLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_Password")); // NOI18N
        passwordField.getAccessibleContext().setAccessibleName(passwordLabel.getAccessibleContext().getAccessibleName());
        passwordField.getAccessibleContext().setAccessibleDescription(passwordLabel.getAccessibleContext().getAccessibleDescription());
        showButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSN_ShowButton")); // NOI18N
        showButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_ShowButton")); // NOI18N
        serverPortLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSN_ServerPort")); // NOI18N
        serverPortLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_ServerPort")); // NOI18N
        NoteChangesLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSN_Note")); // NOI18N
        NoteChangesLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_Note")); // NOI18N
        serverPort.getAccessibleContext().setAccessibleName(serverPortLabel.getAccessibleContext().getAccessibleName());
        serverPort.getAccessibleContext().setAccessibleDescription(serverPortLabel.getAccessibleContext().getAccessibleDescription());
    }// </editor-fold>//GEN-END:initComponents

    private void showButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showButtonActionPerformed
        if (!passwordVisible) {
            passwordVisible = true;
            originalFont = passwordField.getFont();
            passwordField.setFont(userName.getFont());
            originalEchoChar = passwordField.getEchoChar();
            passwordField.setEchoChar((char) 0);
            Mnemonics.setLocalizedText(showButton, 
                    NbBundle.getMessage(CustomizerGeneral.class, 
                    "LBL_ShowButtonHide"));                         // NOI18N
            showButton.setToolTipText(NbBundle.getMessage(CustomizerGeneral.class, 
                    "LBL_ShowButtonHide_ToolTip"));                 // NOI18N
        } else {
            passwordVisible = false;
            passwordField.setFont(originalFont);
            passwordField.setEchoChar(originalEchoChar);
            Mnemonics.setLocalizedText(showButton, NbBundle.getMessage(
                    CustomizerGeneral.class, "LBL_ShowButton"));    // NOI18N
            showButton.setToolTipText(NbBundle.getMessage(CustomizerGeneral.class, 
                    "LBL_ShowButton_ToolTip"));                     // NOI18N

        }
    }//GEN-LAST:event_showButtonActionPerformed

    private void jpa2ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jpa2ButtonActionPerformed
        // TODO add your handling code here:
        WLJpa2SwitchSupport support = manager.getJpa2SwitchSupport();
        if (support.isEnabled()) {
            support.disable();
        } else {
            support.enable();
        }
    }//GEN-LAST:event_jpa2ButtonActionPerformed
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel NoteChangesLabel;
    private javax.swing.JLabel adminInfoLabel;
    private javax.swing.JTextField domainFolder;
    private javax.swing.JLabel domainFolderLabel;
    private javax.swing.JTextField domainName;
    private javax.swing.JLabel domainNameLabel;
    private javax.swing.JButton jpa2Button;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JTextField serverPort;
    private javax.swing.JLabel serverPortLabel;
    private javax.swing.JButton showButton;
    private javax.swing.JTextField userName;
    private javax.swing.JLabel userNameLabel;
    // End of variables declaration//GEN-END:variables

    private boolean passwordVisible;
    private char originalEchoChar;
    private Font originalFont;
    private WLDeploymentManager manager;
}
