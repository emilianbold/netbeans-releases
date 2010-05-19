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
 * NewJPanel.java
 *
 * Created on 27.04.2010, 22:10:44
 */
package org.netbeans.modules.nativeexecution.support.ui;

import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.nativeexecution.ConnectionManagerAccessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.PasswordManager;
import org.netbeans.modules.nativeexecution.api.util.ValidateablePanel;
import org.netbeans.modules.nativeexecution.support.Authentication;
import org.netbeans.modules.nativeexecution.support.SSHKeyFileChooser;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author ak119685
 */
public class AuthenticationSettingsPanel extends ValidateablePanel {

    private final ExecutionEnvironment env;
    private final Task validationTask;
    private String problem;
    private final Authentication auth;

    public AuthenticationSettingsPanel(Authentication auth, boolean showClearPwdButton) {
        this.env = auth.getEnv();
        this.auth = auth;
        initComponents();

        pwdClearButton.setVisible(showClearPwdButton);
        pwdStoredLbl.setVisible(showClearPwdButton);

        if (env != null) {
            loginLabel.setText(env.getUser() + "@" + env.getHost() + // NOI18N
                    ((env.getSSHPort() == 22) ? "" : env.getSSHPort())); // NOI18N
        } else {
            loginPanel.setVisible(false);
        }

        if (auth.getType() == Authentication.Type.SSH_KEY) {
            keyRadioButton.setSelected(true);
            pwdRadioButton.setSelected(false);
        } else {
            keyRadioButton.setSelected(false);
            pwdRadioButton.setSelected(true);
        }

        keyFld.setText(auth.getKey());

        if (env != null) {
            boolean stored = PasswordManager.getInstance().isRememberPassword(env);
            pwdClearButton.setEnabled(stored);
            pwdStoredLbl.setVisible(stored);
        }

        validationTask = new RequestProcessor("", 1).create(new ValidationTask(), true);

        keyFld.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                validationTask.schedule(0);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validationTask.schedule(0);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                validationTask.schedule(0);
            }
        });

        enableControls();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        pwdRadioButton.setEnabled(enabled);
        keyRadioButton.setEnabled(enabled);
        if (keyRadioButton.isSelected()) {
            keyFld.setEnabled(enabled);
            keyBrowseButton.setEnabled(enabled);
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        loginPanel = new javax.swing.JPanel();
        loginLabel = new javax.swing.JLabel();
        authPanel = new javax.swing.JPanel();
        pwdRadioButton = new javax.swing.JRadioButton();
        pwdStoredLbl = new javax.swing.JLabel();
        pwdClearButton = new javax.swing.JButton();
        keyRadioButton = new javax.swing.JRadioButton();
        keyFld = new javax.swing.JTextField();
        keyBrowseButton = new javax.swing.JButton();

        loginPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.loginPanel.border.title"))); // NOI18N

        loginLabel.setText(org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.loginLabel.text")); // NOI18N

        javax.swing.GroupLayout loginPanelLayout = new javax.swing.GroupLayout(loginPanel);
        loginPanel.setLayout(loginPanelLayout);
        loginPanelLayout.setHorizontalGroup(
            loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loginPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(loginLabel)
                .addContainerGap(285, Short.MAX_VALUE))
        );
        loginPanelLayout.setVerticalGroup(
            loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loginPanelLayout.createSequentialGroup()
                .addComponent(loginLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        authPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.authPanel.border.title"))); // NOI18N

        buttonGroup1.add(pwdRadioButton);
        pwdRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(pwdRadioButton, org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.pwdRadioButton.text")); // NOI18N
        pwdRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pwdRadioButtonActionPerformed(evt);
            }
        });

        pwdStoredLbl.setText(org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.pwdStoredLbl.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(pwdClearButton, org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.pwdClearButton.text_1")); // NOI18N
        pwdClearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pwdClearButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(keyRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(keyRadioButton, org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.keyRadioButton.text")); // NOI18N
        keyRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyRadioButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(keyBrowseButton, org.openide.util.NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.keyBrowseButton.text_1")); // NOI18N
        keyBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyBrowseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout authPanelLayout = new javax.swing.GroupLayout(authPanel);
        authPanel.setLayout(authPanelLayout);
        authPanelLayout.setHorizontalGroup(
            authPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(authPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(authPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pwdRadioButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(keyRadioButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(authPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pwdStoredLbl, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                    .addComponent(keyFld, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(authPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(pwdClearButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(keyBrowseButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        authPanelLayout.setVerticalGroup(
            authPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(authPanelLayout.createSequentialGroup()
                .addGroup(authPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pwdRadioButton)
                    .addComponent(pwdClearButton)
                    .addComponent(pwdStoredLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(authPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keyRadioButton)
                    .addComponent(keyFld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(keyBrowseButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(loginPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(authPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(loginPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(authPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void keyRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyRadioButtonActionPerformed
        enableControls();
        keyFld.requestFocus();
    }//GEN-LAST:event_keyRadioButtonActionPerformed

    private void pwdClearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pwdClearButtonActionPerformed
        PasswordManager.getInstance().forceClearPassword(env);
        pwdStoredLbl.setVisible(false);
        pwdClearButton.setEnabled(false);
    }//GEN-LAST:event_pwdClearButtonActionPerformed

    private void keyBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyBrowseButtonActionPerformed
        JFileChooser chooser = new SSHKeyFileChooser(keyFld.getText());
        int result = chooser.showOpenDialog(this);

        if (JFileChooser.APPROVE_OPTION == result) {
            keyFld.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_keyBrowseButtonActionPerformed

    private void pwdRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pwdRadioButtonActionPerformed
        enableControls();
    }//GEN-LAST:event_pwdRadioButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel authPanel;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton keyBrowseButton;
    private javax.swing.JTextField keyFld;
    private javax.swing.JRadioButton keyRadioButton;
    private javax.swing.JLabel loginLabel;
    private javax.swing.JPanel loginPanel;
    private javax.swing.JButton pwdClearButton;
    private javax.swing.JRadioButton pwdRadioButton;
    private javax.swing.JLabel pwdStoredLbl;
    // End of variables declaration//GEN-END:variables

    private void enableControls() {
        keyBrowseButton.setEnabled(keyRadioButton.isSelected());
        keyFld.setEnabled(keyRadioButton.isSelected());
        validationTask.schedule(0);
    }

    @Override
    public boolean hasProblem() {
        return problem != null;
    }

    @Override
    public String getProblem() {
        return problem;
    }

    @Override
    public void applyChanges(Object customData) {
        ConnectionManagerAccessor access = ConnectionManagerAccessor.getDefault();

        if (customData instanceof ExecutionEnvironment) {
            ExecutionEnvironment e = (ExecutionEnvironment) customData;
            Authentication a = Authentication.getFor(e);
            if (auth.getType() == Authentication.Type.SSH_KEY) {
                a.setSSHKey(auth.getKey());
            } else {
                a.setPassword();
            }
            a.store();
            access.changeAuth(e, a);
        } else if (env != null) {
            auth.store();
            access.changeAuth(env, auth);
        }
    }

    private class ValidationTask implements Runnable {

        @Override
        public void run() {
            validate();
            fireChange();
        }

        private boolean validate() {
            if (pwdRadioButton.isSelected()) {
                problem = null;
                auth.setPassword();
                return true;
            }

            String key = keyFld.getText();
            if (key.length() == 0) {
                problem = NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.validationError.emptyKey.text");
                return false;
            }

            if (!Authentication.isValidKey(key)) {
                problem = NbBundle.getMessage(AuthenticationSettingsPanel.class, "AuthenticationSettingsPanel.validationError.invalidKey.text", key);
                return false;
            }

            auth.setSSHKey(key);
            problem = null;
            return true;
        }
    }
}
