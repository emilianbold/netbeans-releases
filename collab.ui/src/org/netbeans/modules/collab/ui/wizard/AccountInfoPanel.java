/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.ui.wizard;

import com.sun.collablet.Account;

import org.openide.*;
import org.openide.util.*;

import java.awt.Component;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.ui.*;


/**
 *
 *
 */
public class AccountInfoPanel extends WizardPanelBase {
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

    /**
     *
     *
     */
    public AccountInfoPanel() {
        super(NbBundle.getMessage(AccountInfoPanel.class, "LBL_AccountInfoPanel_Name")); // NOI18N
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
    public void readSettings(Object object) {
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
    public void storeSettings(Object object) {
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
            settings.getWizardDescriptor().putProperty("WizardPanel_errorMessage", message); // NOI18N
        }

        setValid(valid);
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

    public void initAccessibility() {
        confirmPasswordLabel.setLabelFor(confirmPasswordField);
        passwordLabel.setLabelFor(passwordField);
        userNameLbl.setLabelFor(userNameField);
        jLabel1.setLabelFor(null);
        messageLabel.setLabelFor(null);

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
    private void initComponents() { //GEN-BEGIN:initComponents

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

        setLayout(new java.awt.GridBagLayout());

        setBorder(
            new javax.swing.border.CompoundBorder(
                null, new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5))
            )
        );
        jPanel2.setLayout(new java.awt.GridBagLayout());

        messageLabel.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "LBL_AccountInfoPanel_Message"
            )
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        jPanel2.add(messageLabel, gridBagConstraints);

        userNameLbl.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "LBL_AccountInfoPanel_UserName"
            )
        );
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

        jLabel1.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "LBL_AccountInfoPanel_Example"
            )
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel2.add(jLabel1, gridBagConstraints);

        passwordLabel.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "LBL_AccountInfoPanel_Password"
            )
        );
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

        confirmPasswordLabel.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "LBL_AccountInfoPanel_Confirm_Password"
            )
        );
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
    } //GEN-END:initComponents
}
