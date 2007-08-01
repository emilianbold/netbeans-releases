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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * EnterPasswordPanel.java
 *
 * Created on May 31, 2004
 */
package org.netbeans.modules.mobility.project.ui.security;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.modules.mobility.project.security.KeyStoreRepository;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Adam Sotona
 */
public class EnterPasswordPanel extends javax.swing.JPanel implements ActionListener {
    
    public static String getKeystorePassword(final String keystorePath) {
        if (keystorePath == null) return null;
        return getKeystorePassword(KeyStoreRepository.getDefault().getKeyStore(keystorePath, true));
    }
    
    public static String getAliasPassword(final String keystorePath, final String alias) {
        if (keystorePath == null || alias == null) return null;
        final KeyStoreRepository.KeyStoreBean ksb = KeyStoreRepository.getDefault().getKeyStore(keystorePath, false);
        if (ksb == null || !ksb.isOpened()) return null;
        return getAliasPassword(ksb,  ksb.getAlias(alias));
    }
    
    public static String getKeyfilePassword(final String keyFile) {
        final EnterPasswordPanel panel = new EnterPasswordPanel(NbBundle.getMessage(EnterPasswordPanel.class, "LBL_EnterKeyfilePassword", keyFile), null, false, true, true); //NOI18N
        final Object o = KeyStoreRepository.getDefault().getPassword(keyFile);
        if (o instanceof String) panel.jPasswordField.setText((String)o);
        final String password = panel.showDialog();
        if (KeyStoreRepository.rememberPasswords) {
            if (password != null) KeyStoreRepository.getDefault().putPassword(keyFile, password);
        } else {
            KeyStoreRepository.getDefault().removePassword(keyFile);
        }
        return password;
    }
    
    public static String[] getConnectionUsernameAndPassword(final String connectionId, String userName) {
        final EnterPasswordPanel panel = new EnterPasswordPanel(NbBundle.getMessage(EnterPasswordPanel.class, "LBL_EnterConnectionPassword", connectionId), userName == null ? "" : userName, true, false, true); //NOI18N
        final Object o = KeyStoreRepository.getDefault().getPassword(getKey(connectionId, userName));
        if (o instanceof String) panel.jPasswordField.setText((String)o);
        else if (o instanceof String[]) {
            panel.jTextFieldUsername.setText(((String[])o)[0]);
            panel.jPasswordField.setText(((String[])o)[1]);
        }
        final String password = panel.showDialog();
        userName = panel.jTextFieldUsername.getText();
        if (KeyStoreRepository.rememberPasswords) {
            if (password != null) {
                KeyStoreRepository.getDefault().putPassword(connectionId, new String[] {userName, password});
                KeyStoreRepository.getDefault().putPassword(getKey(connectionId, userName), password);
            }
        } else {
            KeyStoreRepository.getDefault().removePassword(connectionId);
            KeyStoreRepository.getDefault().removePassword(getKey(connectionId, userName));
        }
        return password == null ? null : new String[] {userName, password};
    }
    
    private static String getKey(final String connectionId, final String userName) {
        return userName == null || userName.length() == 0 ? connectionId : (userName + '@' + connectionId);
    }
    
    public static String getConnectionPassword(final String connectionId, final String userName) {
        final EnterPasswordPanel panel = new EnterPasswordPanel(NbBundle.getMessage(EnterPasswordPanel.class, "LBL_EnterConnectionPassword", connectionId), userName == null ? "" : userName, false, false, true); //NOI18N
        final Object o = KeyStoreRepository.getDefault().getPassword(getKey(connectionId, userName));
        if (o instanceof String) panel.jPasswordField.setText((String)o);
        final String password = panel.showDialog();
        if (KeyStoreRepository.rememberPasswords) {
            if (password != null) KeyStoreRepository.getDefault().putPassword(getKey(connectionId, userName), password);
        } else {
            KeyStoreRepository.getDefault().removePassword(getKey(connectionId, userName));
        }
        return password;
    }
    
    public static String getKeystorePassword(final KeyStoreRepository.KeyStoreBean ksb) {
        if (ksb == null) return null;
        if (ksb.isOpened()) return ksb.getPassword();
        final EnterPasswordPanel panel = new EnterPasswordPanel(NbBundle.getMessage(EnterPasswordPanel.class, "LBL_EnterKeystorePassword", ksb.getKeyStorePath()), null, false, false, false); //NOI18N
        String password ;
        while ((password = panel.showDialog()) != null) {
            ksb.setPassword(password);
            if (ksb.openKeyStore()) return password;
            panel.setKeystoreErrorMessage();
        }
        return null;
    }
    
    public static String getAliasPassword(final KeyStoreRepository.KeyStoreBean ksb,final  KeyStoreRepository.KeyStoreBean.KeyAliasBean kab) {
        if (ksb == null || kab == null || !ksb.isOpened()) return null;
        if (kab.isOpened()) return kab.getPassword();
        final EnterPasswordPanel panel = new EnterPasswordPanel(NbBundle.getMessage(EnterPasswordPanel.class, "LBL_EnterAliasPassword", ksb.getKeyStorePath(), kab.getAlias()), null, false, false, false); //NOI18N
        String password;
        while ((password = panel.showDialog()) != null) {
            kab.setPassword(password.length() == 0 ? ksb.getPassword() : password);
            if (kab.open()) return kab.getPassword();
            panel.setAliasErrorMessage();
        }
        return null;
    }
    
    /** Creates new form EnterPasswordPanel */
    private EnterPasswordPanel(String label, String userName, boolean userNameEditable, boolean passphrase, boolean showRemember) {
        initComponents();
        initAccessibility();
        jLabelMessage.setText(label);
        if (userName == null) {
            jLabelUsername.setVisible(false);
            jTextFieldUsername.setVisible(false);
        } else {
            jTextFieldUsername.setText(userName);
            if (!userNameEditable) jTextFieldUsername.setEditable(false);
        }
        if (passphrase) jLabelPassword.setText(NbBundle.getMessage(EnterPasswordPanel.class, "LBL_Password_Passphrase")); //NOI18N
        if (showRemember) {
            jCheckBoxRemember.setSelected(KeyStoreRepository.rememberPasswords);
            jCheckBoxRemember.addActionListener(this);
        } else {
            jCheckBoxRemember.setVisible(false);
        }
    }
    
    private String showDialog() {
        final DialogDescriptor dd = new DialogDescriptor(this, NbBundle.getMessage(EnterPasswordPanel.class, "TITLE_EnterPassword"), true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, null); // NOI18N
        dd.setHelpCtx(new HelpCtx(EnterPasswordPanel.class));
        if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dd))) {
            return String.valueOf(jPasswordField.getPassword());
        }
        return null;
    }
    
    private void setKeystoreErrorMessage() {
        errorPanel1.setErrorMessage(NbBundle.getMessage(EnterPasswordPanel.class, "ERR_KeystoreError")); // NOI18N
    }
    
    private void setAliasErrorMessage() {
        errorPanel1.setErrorMessage(NbBundle.getMessage(EnterPasswordPanel.class, "ERR_AliasError")); // NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelMessage = new javax.swing.JLabel();
        jLabelUsername = new javax.swing.JLabel();
        jTextFieldUsername = new javax.swing.JTextField();
        jLabelPassword = new javax.swing.JLabel();
        jPasswordField = new javax.swing.JPasswordField();
        jCheckBoxRemember = new javax.swing.JCheckBox();
        errorPanel1 = new org.netbeans.modules.mobility.project.ui.customizer.ErrorPanel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabelMessage, gridBagConstraints);

        jLabelUsername.setLabelFor(jTextFieldUsername);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelUsername, NbBundle.getMessage(EnterPasswordPanel.class, "LBL_PasswordPanel_Username")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabelUsername, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 12, 0);
        add(jTextFieldUsername, gridBagConstraints);

        jLabelPassword.setLabelFor(jPasswordField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelPassword, org.openide.util.NbBundle.getMessage(EnterPasswordPanel.class, "LBL_Password_Password")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabelPassword, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 12, 0);
        add(jPasswordField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxRemember, NbBundle.getMessage(EnterPasswordPanel.class, "LBL_PasswordPanel_Remember")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jCheckBoxRemember, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(errorPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(EnterPasswordPanel.class, "ACSN_EnterKeystorePassword"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(EnterPasswordPanel.class, "ACSD_EnterKeystorePassword"));
    }
    
    public void actionPerformed(@SuppressWarnings("unused")
	final ActionEvent actionEvent) {
        KeyStoreRepository.rememberPasswords = jCheckBoxRemember.isSelected();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.netbeans.modules.mobility.project.ui.customizer.ErrorPanel errorPanel1;
    private javax.swing.JCheckBox jCheckBoxRemember;
    private javax.swing.JLabel jLabelMessage;
    private javax.swing.JLabel jLabelPassword;
    private javax.swing.JLabel jLabelUsername;
    private javax.swing.JPasswordField jPasswordField;
    private javax.swing.JTextField jTextFieldUsername;
    // End of variables declaration//GEN-END:variables
    
}
