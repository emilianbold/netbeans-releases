/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

/*
 * AuthPanel.java
 *
 * Created on 04.08.2009, 18:01:19
 */

package org.netbeans.modules.soa.ldap.properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.DialogDescriptor;

/**
 *
 * @author anjeleevich
 */
public class AuthenticationPanel extends javax.swing.JPanel {

    private DialogDescriptor dialogDescriptor;
    private ConnectionProperties oldConnectionProperties;

    private DocumentListener documentListener = new DocumentListener() {
        public void insertUpdate(DocumentEvent e) {
            updateValidState();
        }

        public void removeUpdate(DocumentEvent e) {
            updateValidState();
        }

        public void changedUpdate(DocumentEvent e) {
            updateValidState();
        }
    };

    /** Creates new form AuthPanel */
    public AuthenticationPanel(ConnectionProperties connectionProperties) {
        initComponents();

        this.oldConnectionProperties = connectionProperties;

        String user = connectionProperties.getUser();
        char[] password = connectionProperties.getPassword();

        methodComboBox.setSelectedItem(connectionProperties
                .getAuthenticationType());

        if (user != null) {
            userTextField.setText(user);
        }

        if (password != null) {
            passwordField.setText(new String(password));
        }
        
        savePasswordCheckBox.setSelected(connectionProperties.isSavePassword());

        userTextField.getDocument().addDocumentListener(documentListener);
        passwordField.getDocument().addDocumentListener(documentListener);

        updateValidState();
        updateEnabledState();
    }

    public void setDialogDescriptor(DialogDescriptor dialogDescriptor) {
        this.dialogDescriptor = dialogDescriptor;
        updateValidState();
    }

    public AuthenticationType getSecurityMethod() {
        return (AuthenticationType) methodComboBox.getSelectedItem();
    }

    public boolean isSavePassword() {
        return savePasswordCheckBox.isSelected();
    }

    public ConnectionProperties getNewConnectionProperties() {
        ConnectionProperties result = new ConnectionProperties(
                oldConnectionProperties);
        result.setAuthenticationType(getSecurityMethod());
        result.setUser(getUser());
        result.setPassword(getPassword());
        result.setSavePassword(isSavePassword());
        
        return result;
    }

    public String getUser() {
        if (methodComboBox.getSelectedItem() == AuthenticationType
                .NO_AUTHENTICATION)
        {
            return null;
        } 

        String user = userTextField.getText();
        return (user == null) ? "" : user.trim(); // NOI18N
    }

    public char[] getPassword() {
        if (methodComboBox.getSelectedItem() == AuthenticationType
                .NO_AUTHENTICATION)
        {
            return null;
        }

        return passwordField.getPassword();
    }

    public void updateValidState() {
        if (dialogDescriptor == null) {
            return;
        }

        boolean validData;
        if (methodComboBox.getSelectedItem()
                == AuthenticationType.NO_AUTHENTICATION)
        {
            validData = true;
        } else {
            char[] password = passwordField.getPassword();
            String user = userTextField.getText();
            
            boolean validPassword = (password != null) && (password.length > 0);
            boolean validUser = (user != null) && (user.trim().length() > 0);

            validData = validUser && validPassword;
        }

        dialogDescriptor.setValid(validData);
    }

    public void updateEnabledState() {
        if (methodComboBox.getSelectedItem() == AuthenticationType
                .NO_AUTHENTICATION)
        {
            userTextField.setEnabled(false);
            passwordField.setEnabled(false);
            savePasswordCheckBox.setEnabled(false);
        } else {
            userTextField.setEnabled(true);
            passwordField.setEnabled(true);
            savePasswordCheckBox.setEnabled(true);
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

        securityPanel = new javax.swing.JPanel();
        methodLabel = new javax.swing.JLabel();
        methodComboBox = new javax.swing.JComboBox();
        userLabel = new javax.swing.JLabel();
        userTextField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        savePasswordCheckBox = new javax.swing.JCheckBox();
        messageTextArea = new javax.swing.JTextArea();
        messageTextArea.setFont(userLabel.getFont());

        securityPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(AuthenticationPanel.class, "AuthenticationPanel.securityPanel.border.title"))); // NOI18N

        methodLabel.setLabelFor(methodComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(methodLabel, org.openide.util.NbBundle.getMessage(AuthenticationPanel.class, "AuthenticationPanel.methodLabel.text")); // NOI18N

        methodComboBox.setModel(new DefaultComboBoxModel(AuthenticationType.values()));
        methodComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                methodComboBoxActionPerformed(evt);
            }
        });

        userLabel.setLabelFor(userTextField);
        org.openide.awt.Mnemonics.setLocalizedText(userLabel, org.openide.util.NbBundle.getMessage(AuthenticationPanel.class, "AuthenticationPanel.userLabel.text")); // NOI18N

        userTextField.setText(org.openide.util.NbBundle.getMessage(AuthenticationPanel.class, "AuthenticationPanel.userTextField.text")); // NOI18N

        passwordLabel.setLabelFor(passwordField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(AuthenticationPanel.class, "AuthenticationPanel.passwordLabel.text")); // NOI18N

        passwordField.setText(org.openide.util.NbBundle.getMessage(AuthenticationPanel.class, "AuthenticationPanel.passwordField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(savePasswordCheckBox, org.openide.util.NbBundle.getMessage(AuthenticationPanel.class, "AuthenticationPanel.savePasswordCheckBox.text")); // NOI18N
        savePasswordCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                savePasswordCheckBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout securityPanelLayout = new org.jdesktop.layout.GroupLayout(securityPanel);
        securityPanel.setLayout(securityPanelLayout);
        securityPanelLayout.setHorizontalGroup(
            securityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(securityPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(securityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(securityPanelLayout.createSequentialGroup()
                        .add(securityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(userLabel)
                            .add(methodLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(securityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(methodComboBox, 0, 272, Short.MAX_VALUE)
                            .add(userTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)))
                    .add(securityPanelLayout.createSequentialGroup()
                        .add(passwordLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(securityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(savePasswordCheckBox)
                            .add(passwordField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE))))
                .addContainerGap())
        );
        securityPanelLayout.setVerticalGroup(
            securityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(securityPanelLayout.createSequentialGroup()
                .add(securityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(methodLabel)
                    .add(methodComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(securityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(userLabel)
                    .add(userTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(securityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(passwordLabel)
                    .add(passwordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(savePasswordCheckBox))
        );

        messageTextArea.setColumns(20);
        messageTextArea.setEditable(false);
        messageTextArea.setLineWrap(true);
        messageTextArea.setRows(3);
        messageTextArea.setBorder(null);
        messageTextArea.setOpaque(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, messageTextArea, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, securityPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(securityPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(messageTextArea, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void savePasswordCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_savePasswordCheckBoxActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_savePasswordCheckBoxActionPerformed

    private void methodComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_methodComboBoxActionPerformed
        updateValidState();
        updateEnabledState();
    }//GEN-LAST:event_methodComboBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea messageTextArea;
    private javax.swing.JComboBox methodComboBox;
    private javax.swing.JLabel methodLabel;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JCheckBox savePasswordCheckBox;
    private javax.swing.JPanel securityPanel;
    private javax.swing.JLabel userLabel;
    private javax.swing.JTextField userTextField;
    // End of variables declaration//GEN-END:variables

}
