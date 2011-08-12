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
package org.netbeans.modules.collab.ui.wizard;

import com.sun.collablet.Account;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.collab.ui.DefaultUserInterface;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 *
 */
public class AccountInfoPanel extends JPanel {
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPasswordField confirmPasswordField;
    private javax.swing.JLabel confirmPasswordLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JTextField userNameField;
    private javax.swing.JLabel userNameLbl;
    // End of variables declaration//GEN-END:variables
    private AccountWizardSettings settings;

    private WizardPanelBase wizardPanel;

    /**
     *
     *
     */
    public AccountInfoPanel (WizardPanelBase wizardPanel) {
        this.wizardPanel = wizardPanel;
        setName(NbBundle.getMessage(AccountInfoPanel.class, "LBL_AccountInfoPanel_Name"));
        initComponents();
        initAccessibility();

        DocumentListener docListener = new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                }

                public void insertUpdate(DocumentEvent e) {
                    checkValidity();
                }

                public void removeUpdate(DocumentEvent e) {
                    checkValidity();
                }
            };

        userNameField.getDocument().addDocumentListener(docListener);
        passwordField.getDocument().addDocumentListener(docListener);
        confirmPasswordField.getDocument().addDocumentListener(docListener);
    }

    /**
     *
     *
     */
    void readSettings(Object object) {
        settings = AccountWizardSettings.narrow(object);

        boolean isNewAccount = settings.isNewAccount();
        passwordField.setVisible(isNewAccount);
        passwordLabel.setVisible(isNewAccount);
        confirmPasswordField.setVisible(isNewAccount);
        confirmPasswordLabel.setVisible(isNewAccount);

        Account account = settings.getAccount();
        userNameField.setText(account.getUserName());
        passwordField.setText(account.getPassword());
        confirmPasswordField.setText(account.getPassword());
    }

    /**
     *
     *
     */
    void storeSettings(Object object) {
        if (object instanceof AccountWizardSettings) {
            Account account = AccountWizardSettings.narrow(object).getAccount();
            account.setUserName(userNameField.getText().trim());
            account.setPassword(passwordField.getText().trim());
        }
    }

    /**
     *
     *
     */
    protected void checkValidity() {
        String userName = userNameField.getText().trim();
        boolean valid = userName.length() > 0;

        if (settings.isNewAccount()) {
            String message = "";

            // TEMP, check ascii value to prevent multibyte and some special 
            // characters in user id
            if (!DefaultUserInterface.isValidJID(userName)) {
                valid = false;
                message = NbBundle.getMessage(AccountInfoPanel.class, "MSG_AccountInfoPanel_MultiByteNotAllowed"); // NOI18N
            }

            String password = passwordField.getText().trim();
            String passwordConfirmation = confirmPasswordField.getText().trim();
            valid = valid && (password.length() > 0) && (passwordConfirmation.length() > 0);

            if (valid && !password.equals(passwordConfirmation)) {
                valid = false;
                message = NbBundle.getMessage(AccountInfoPanel.class, "MSG_AccountInfoPanel_PasswordMismatch"); // NOI18N
            }

            // Display a hint to the user
            settings.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message); // NOI18N
        }

        wizardPanel.setValid(valid);
    }

    /**
     *
     *
     */
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    userNameField.requestFocus();
                }
            }
        );
    }

    private void initAccessibility() {
        confirmPasswordField.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AccountUserInfoPanel.class, "ACSD_DESC_AccountInfoPanel_ConfirmPasswordField")
        ); // NOI18N
        passwordField.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AccountUserInfoPanel.class, "ACSD_DESC_AccountInfoPanel_PasswordField")
        ); // NOI18N
        userNameField.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AccountUserInfoPanel.class, "ACSD_DESC_AccountInfoPanel_UserNameField")
        ); // NOI18N   

        confirmPasswordField.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(AccountUserInfoPanel.class, "ACSD_NAME_AccountInfoPanel_ConfirmPasswordField")
        ); // NOI18N
        passwordField.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(AccountUserInfoPanel.class, "ACSD_NAME_AccountInfoPanel_PasswordField")
        ); // NOI18N
        userNameField.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(AccountUserInfoPanel.class, "ACSD_NAME_AccountInfoPanel_UserNameField")
        ); // NOI18N         
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel2 = new javax.swing.JPanel();
        messageLabel = new javax.swing.JLabel();
        userNameLbl = new javax.swing.JLabel();
        userNameField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        confirmPasswordLabel = new javax.swing.JLabel();
        confirmPasswordField = new javax.swing.JPasswordField();

        setBorder(javax.swing.BorderFactory.createCompoundBorder(null, javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        setLayout(new java.awt.GridBagLayout());

        jPanel2.setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, bundle.getString("LBL_AccountInfoPanel_Message")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        jPanel2.add(messageLabel, gridBagConstraints);

        userNameLbl.setLabelFor(userNameField);
        org.openide.awt.Mnemonics.setLocalizedText(userNameLbl, bundle.getString("LBL_AccountInfoPanel_UserName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        jPanel2.add(userNameLbl, gridBagConstraints);

        userNameField.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 0);
        jPanel2.add(userNameField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, bundle.getString("LBL_AccountInfoPanel_Example")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel2.add(jLabel1, gridBagConstraints);

        passwordLabel.setLabelFor(passwordField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, bundle.getString("LBL_AccountInfoPanel_Password")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        jPanel2.add(passwordLabel, gridBagConstraints);

        passwordField.setMinimumSize(new java.awt.Dimension(11, 20));
        passwordField.setPreferredSize(new java.awt.Dimension(11, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 0);
        jPanel2.add(passwordField, gridBagConstraints);

        confirmPasswordLabel.setLabelFor(confirmPasswordField);
        org.openide.awt.Mnemonics.setLocalizedText(confirmPasswordLabel, bundle.getString("LBL_AccountInfoPanel_Confirm_Password")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(confirmPasswordLabel, gridBagConstraints);

        confirmPasswordField.setMinimumSize(new java.awt.Dimension(11, 20));
        confirmPasswordField.setPreferredSize(new java.awt.Dimension(11, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel2.add(confirmPasswordField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
}
