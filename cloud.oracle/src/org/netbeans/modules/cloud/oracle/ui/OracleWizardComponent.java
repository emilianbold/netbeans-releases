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

package org.netbeans.modules.cloud.oracle.ui;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.libs.oracle.cloud.api.CloudSDKHelper;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 */
public class OracleWizardComponent extends javax.swing.JPanel implements DocumentListener {

    private ChangeListener l;
    private static final String ADMIN_URL = "https://javaservices.{0}.cloud.oracle.com"; // NOI18N
    
    static final boolean SHOW_CLOUD_URLS = Boolean.getBoolean("oracle.cloud.dev");
    
    private static String[] dataCenters = new String[] { 
        "us1 [US Commercial 1]",  // NOI18N
        "us2 [US Commercial 2]",  // NOI18N
        "em1 [EMEA Commercial 1]",  // NOI18N
        "em2 [EMEA Commercial 2]",  // NOI18N
        "ap1 [APAC Commercial 1]",  // NOI18N
        "ap2 [APAC Commercial 2]",  // NOI18N
        " " };  // NOI18N
    
    /** Creates new form OracleWizardComponent */
    public OracleWizardComponent() {
        initComponents();
        jDataCenterComboBox.setModel(new javax.swing.DefaultComboBoxModel(dataCenters));
        adminLabel.setVisible(SHOW_CLOUD_URLS);
        adminURLTextField.setVisible(SHOW_CLOUD_URLS);
        
        String folder = CloudSDKHelper.getSDKFolder();
        if (folder.length() != 0) {
            sdkTextField.setText(folder);
        }
        
        setName(NbBundle.getBundle(OracleWizardComponent.class).getString("LBL_Name")); // NOI18N

        if (!SHOW_CLOUD_URLS) {
            adminURLTextField.setText(String.format(ADMIN_URL, getDataCenterCode())); // NOI18N
        }
        adminURLTextField.getDocument().addDocumentListener(this);
        passwordField.getDocument().addDocumentListener(this);
        userNameTextField.getDocument().addDocumentListener(this);
        identityDomainTextField.getDocument().addDocumentListener(this);
        serviceInstanceTextField.getDocument().addDocumentListener(this);
        sdkTextField.getDocument().addDocumentListener(this);
        ((JTextField)jDataCenterComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
    }

    void disableModifications(boolean disable) {
        adminURLTextField.setEditable(!disable);
        passwordField.setEditable(!disable);
        identityDomainTextField.setEditable(!disable);
        serviceInstanceTextField.setEditable(!disable);
        userNameTextField.setEditable(!disable);
    }
    
    public void attachSingleListener(ChangeListener l) {
        this.l = l;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        adminLabel = new javax.swing.JLabel();
        adminURLTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        userNameTextField = new javax.swing.JTextField();
        passwordField = new javax.swing.JPasswordField();
        jLabel7 = new javax.swing.JLabel();
        sdkLabel = new javax.swing.JLabel();
        serviceInstanceTextField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        identityDomainTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        configureButton = new javax.swing.JButton();
        sdkTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        dbServiceNameTextField = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jDataCenterComboBox = new javax.swing.JComboBox();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(OracleWizardComponent.class, "OracleWizardComponent.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(adminLabel, org.openide.util.NbBundle.getMessage(OracleWizardComponent.class, "OracleWizardComponent.adminLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(OracleWizardComponent.class, "OracleWizardComponent.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(OracleWizardComponent.class, "OracleWizardComponent.jLabel4.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(OracleWizardComponent.class, "OracleWizardComponent.jLabel5.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(sdkLabel, org.openide.util.NbBundle.getMessage(OracleWizardComponent.class, "OracleWizardComponent.sdkLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(OracleWizardComponent.class, "OracleWizardComponent.jLabel7.text")); // NOI18N

        jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getSize()-2f));
        jLabel1.setForeground(java.awt.Color.blue);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(OracleWizardComponent.class, "OracleWizardComponent.jLabel1.text")); // NOI18N
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel1MouseClicked(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(configureButton, org.openide.util.NbBundle.getMessage(OracleWizardComponent.class, "OracleWizardComponent.configureButton.text")); // NOI18N
        configureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configureButtonActionPerformed(evt);
            }
        });

        sdkTextField.setEditable(false);
        sdkTextField.setText(org.openide.util.NbBundle.getMessage(OracleWizardComponent.class, "OracleWizardComponent.sdkTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(OracleWizardComponent.class, "OracleWizardComponent.jLabel6.text")); // NOI18N

        dbServiceNameTextField.setText(org.openide.util.NbBundle.getMessage(OracleWizardComponent.class, "OracleWizardComponent.dbServiceNameTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(OracleWizardComponent.class, "OracleWizardComponent.jLabel8.text")); // NOI18N

        jDataCenterComboBox.setEditable(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(adminLabel)
                    .addComponent(jLabel5)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4)
                    .addComponent(sdkLabel)
                    .addComponent(jLabel6)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(adminURLTextField)
                    .addComponent(passwordField)
                    .addComponent(userNameTextField)
                    .addComponent(identityDomainTextField)
                    .addComponent(serviceInstanceTextField)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sdkTextField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(configureButton))
                    .addComponent(dbServiceNameTextField)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jDataCenterComboBox, 0, 221, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jDataCenterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(identityDomainTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serviceInstanceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(dbServiceNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(configureButton)
                    .addComponent(sdkTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sdkLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(adminLabel)
                    .addComponent(adminURLTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

private void jLabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseClicked
        try {
            URLDisplayer.getDefault().showURL(new URL("http://cloud.oracle.com"));
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
}//GEN-LAST:event_jLabel1MouseClicked

    private void configureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureButtonActionPerformed
        File f = CloudSDKHelper.showConfigureSDKDialog(this);
        if (f != null) {
            sdkTextField.setText(f.getAbsolutePath());
        }
    }//GEN-LAST:event_configureButtonActionPerformed

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel adminLabel;
    private javax.swing.JTextField adminURLTextField;
    private javax.swing.JButton configureButton;
    private javax.swing.JTextField dbServiceNameTextField;
    private javax.swing.JTextField identityDomainTextField;
    private javax.swing.JComboBox jDataCenterComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel sdkLabel;
    private javax.swing.JTextField sdkTextField;
    private javax.swing.JTextField serviceInstanceTextField;
    private javax.swing.JTextField userNameTextField;
    // End of variables declaration//GEN-END:variables

    public String getAdminUrl() {
        return adminURLTextField.getText();
    }
    
    public String getUserName() {
        return userNameTextField.getText().trim();
    }
    
    public static String getPrefixedUserName(String prefix, String username) {
        if (username.startsWith(prefix+".")) {
            return username;
        } else {
            return prefix + "." + username;
        }
    }
    
    public String getDataCenter() {
        String s = ((JTextField)jDataCenterComboBox.getEditor()).getText().trim();
        return s;
    }
    
    private String getDataCenterCode() {
        String s = getDataCenter();
        if (s.length() > 3) {
            s = s.substring(0, 3);
        }
        return s;
    }
    
    public static String getUnprefixedUserName(String prefix, String username) {
        if (username.startsWith(prefix+".")) {
            return username.substring(prefix.length()+1);
        } else {
            return username;
        }
    }
    
    public String getPassword() {
        return new String(passwordField.getPassword()).trim();
    }
    
    public String getIdentityDomain() {
        return identityDomainTextField.getText().trim();
    }

    public String getJavaServiceName() {
        return serviceInstanceTextField.getText().trim();
    }

    public String getDatabaseServiceName() {
        return dbServiceNameTextField.getText().trim();
    }

    public String getSDKFolder() {
        return sdkTextField.getText();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        update(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        update(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        update(e);
    }
    
    private void update(DocumentEvent e) {
        if (!SHOW_CLOUD_URLS) {
            adminURLTextField.setText(String.format(ADMIN_URL, getDataCenterCode())); // NOI18N
        }
        if (l != null) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

}
