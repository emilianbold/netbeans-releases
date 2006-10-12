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

package org.netbeans.modules.derby.ui;

import java.awt.Color;
import java.io.File;
import javax.swing.UIManager;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.derby.api.DerbyDatabases;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;
/**
 *
 * @author Andrei Badea
 */
public class CreateDatabasePanel extends javax.swing.JPanel {

    private File derbySystemHome;
    private DialogDescriptor descriptor;
    private Color nbErrorForeground;
    private Color nbWarningForeground;

    private DocumentListener docListener = new DocumentListener() {
        
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            validateDatabaseName();
        }

        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            validateDatabaseName();
        }

        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            validateDatabaseName();
        }
    };
    
    public CreateDatabasePanel(String derbySystemHome) {
        // copied from WizardDescriptor
        nbErrorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (nbErrorForeground == null) {
            //nbErrorForeground = new Color(89, 79, 191); // RGB suggested by Bruce in #28466
            nbErrorForeground = new Color(255, 0, 0); // RGB suggested by jdinga in #65358
        }
        nbWarningForeground = UIManager.getColor("nb.warningForeground"); //NOI18N
        if (nbWarningForeground == null) {
            nbWarningForeground = new Color(51, 51, 51); // Label.foreground
        }

        this.derbySystemHome = new File(derbySystemHome);
        
        initComponents();
        databaseNameTextField.getDocument().addDocumentListener(docListener);
        userTextField.getDocument().addDocumentListener(docListener);
        passwordTextField.getDocument().addDocumentListener(docListener);
        updateLocation();
    }
    
    public void setDialogDescriptor(DialogDescriptor descriptor) {
        this.descriptor = descriptor;
        validateDatabaseName();
    }

    public String getDatabaseName() {
        return databaseNameTextField.getText().trim();
    }
    
    public String getUser() {
        String user = userTextField.getText().trim();
        return user.length() > 0 ? user : null;
    }
    
    public String getPassword() {
        String password = passwordTextField.getText().trim();
        return password.length() > 0 ? password : null;
    }
    
    private void validateDatabaseName() {
        if (descriptor == null) {
            return;
        }
        
        String error = null;
        String warning = null;
        
        String databaseName = getDatabaseName();
        int illegalChar = DerbyDatabases.getFirstIllegalCharacter(databaseName);
        // workaround for issue 69265
        int unsupportedChar = getFirstUnsupportedCharacter(databaseName);
        
        if (databaseName.length() <= 0) { // NOI18N
            error = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_DatabaseNameEmpty");
        } else if (illegalChar >= 0) {
            error = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_DatabaseNameIllegalChar", new Character((char)illegalChar));
        } else if (databaseName.length() > 0 && new File(derbySystemHome, databaseName).exists()) { // NOI18N
            error = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_DatabaseDirectoryExists", databaseName);
        } else if (unsupportedChar >= 0) {
            warning = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_DatabaseNameUnsupportedChar", new Character((char)unsupportedChar));
        } else if (getUser() == null || getPassword() == null) {
            warning = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_UserNamePasswordRecommended");
        }
        
        if (error != null) {
            messageLabel.setForeground(nbErrorForeground);
            messageLabel.setText(error);
            messageLabel.setToolTipText(error);
            descriptor.setValid(false);
        } else if (warning != null) {
            messageLabel.setForeground(nbWarningForeground);
            messageLabel.setText(warning);
            messageLabel.setToolTipText(warning);
            descriptor.setValid(true);
        } else {
            messageLabel.setText(" "); // NOI18N
            descriptor.setValid(true);
        }
    }
    
    private void updateLocation() {
        databaseLocationTextField.setText(derbySystemHome.getAbsolutePath());
    }
    
    private int getFirstUnsupportedCharacter(String databaseName) {
        for (int i = 0; i < databaseName.length(); i++) {
            char ch = databaseName.charAt(i);
            if (ch < '\u0020' || ch > '\u00ff') {
                return (int)ch;
            }
        }
        return -1;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        databaseNameLabel = new javax.swing.JLabel();
        databaseNameTextField = new javax.swing.JTextField();
        infoScrollPane = new javax.swing.JScrollPane();
        infoTextArea = new javax.swing.JTextArea();
        databaseLocationLabel = new javax.swing.JLabel();
        databaseLocationTextField = new javax.swing.JTextField();
        userLabel = new javax.swing.JLabel();
        userTextField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordTextField = new javax.swing.JTextField();
        messageLabel = new javax.swing.JLabel();

        databaseNameLabel.setLabelFor(databaseNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(databaseNameLabel, org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "LBL_DatabaseName"));

        databaseNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "ACSD_CreateDatabasePanel_databaseNameTextField"));

        infoScrollPane.setBorder(null);
        infoScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        infoScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        infoTextArea.setColumns(20);
        infoTextArea.setEditable(false);
        infoTextArea.setLineWrap(true);
        infoTextArea.setRows(5);
        infoTextArea.setText(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "LBL_DatabaseLocationInfo"));
        infoTextArea.setWrapStyleWord(true);
        infoTextArea.setFocusable(false);
        infoTextArea.setOpaque(false);
        infoScrollPane.setViewportView(infoTextArea);

        databaseLocationLabel.setLabelFor(databaseLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(databaseLocationLabel, org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "LBL_DatabaseLocation"));

        databaseLocationTextField.setEditable(false);
        databaseLocationTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "ACSD_CreateDatabasePanel_databaseLocationTextField"));

        userLabel.setLabelFor(userTextField);
        org.openide.awt.Mnemonics.setLocalizedText(userLabel, org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "LBL_UserName"));

        userTextField.setColumns(15);
        userTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "ACSD_CreateDatabasePanel_userTextField"));

        passwordLabel.setLabelFor(passwordTextField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "LBL_Password"));

        passwordTextField.setColumns(15);
        passwordTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "ACSD_CreateDatabasePanel_passwordTextField"));

        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, " ");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(infoScrollPane, 0, 440, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(databaseNameLabel)
                            .add(userLabel)
                            .add(passwordLabel)
                            .add(databaseLocationLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, passwordTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, userTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, databaseNameTextField, 0, 310, Short.MAX_VALUE)
                            .add(databaseLocationTextField, 0, 310, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, messageLabel, 0, 440, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(databaseNameLabel)
                    .add(databaseNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(11, 11, 11)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(userLabel)
                    .add(userTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(passwordLabel)
                    .add(passwordTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(11, 11, 11)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(databaseLocationTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(databaseLocationLabel))
                .add(11, 11, 11)
                .add(infoScrollPane, 0, 66, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(messageLabel)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel databaseLocationLabel;
    public javax.swing.JTextField databaseLocationTextField;
    public javax.swing.JLabel databaseNameLabel;
    public javax.swing.JTextField databaseNameTextField;
    public javax.swing.JScrollPane infoScrollPane;
    public javax.swing.JTextArea infoTextArea;
    public javax.swing.JLabel messageLabel;
    public javax.swing.JLabel passwordLabel;
    public javax.swing.JTextField passwordTextField;
    public javax.swing.JLabel userLabel;
    public javax.swing.JTextField userTextField;
    // End of variables declaration//GEN-END:variables
    
}
