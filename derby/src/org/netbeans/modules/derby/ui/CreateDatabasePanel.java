/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.derby.ui;

import java.io.File;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.derby.DerbyOptions;
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
        String password = new String(passwordTextField.getPassword()).trim();
        return password.length() > 0 ? password : null;
    }

    public void setIntroduction() {
        String info = NbBundle.getMessage(CreateDatabasePanel.class, "INFO_DatabaseNameEmpty");
        descriptor.getNotificationLineSupport().setInformationMessage(info);
        descriptor.setValid(false);
    }

    private void validateDatabaseName() {
        if (descriptor == null) {
            return;
        }
        
        String error = null;
        String warning = null;
        String info = null;
        
        String databaseName = getDatabaseName();
        int illegalChar = DerbyDatabases.getFirstIllegalCharacter(databaseName);
        // workaround for issue 69265
        int unsupportedChar = getFirstUnsupportedCharacter(databaseName);
        
        if (databaseName.length() <= 0) { // NOI18N
            warning = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_DatabaseNameEmpty");
        } else if (illegalChar >= 0) {
            error = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_DatabaseNameIllegalChar", new Character((char)illegalChar));
        } else if (databaseName.length() > 0 && new File(derbySystemHome, databaseName).exists()) { // NOI18N
            error = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_DatabaseDirectoryExists", databaseName);
        } else if (unsupportedChar >= 0) {
            error = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_DatabaseNameUnsupportedChar", new Character((char)unsupportedChar));
        } else if (getUser() == null || getPassword() == null) {
            info = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_UserNamePasswordRecommended");
        }
        
        if (error != null) {
            descriptor.getNotificationLineSupport().setErrorMessage(error);
            descriptor.setValid(false);
        } else if (warning != null) {
            descriptor.getNotificationLineSupport().setWarningMessage(warning);
            descriptor.setValid(false);
        } else if (info != null) {
            descriptor.getNotificationLineSupport().setInformationMessage(info);
            descriptor.setValid(true);
        } else {
            descriptor.getNotificationLineSupport().clearMessages();
            descriptor.setValid(true);
        }
    }
    
    private void updateLocation() {
        databaseLocationValueLabel.setText(derbySystemHome.getAbsolutePath());
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        databaseNameLabel = new javax.swing.JLabel();
        databaseNameTextField = new javax.swing.JTextField();
        databaseLocationLabel = new javax.swing.JLabel();
        userLabel = new javax.swing.JLabel();
        userTextField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordTextField = new javax.swing.JPasswordField();
        propertiesButton = new javax.swing.JButton();
        databaseLocationValueLabel = new javax.swing.JLabel();

        databaseNameLabel.setLabelFor(databaseNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(databaseNameLabel, org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "LBL_DatabaseName")); // NOI18N

        databaseLocationLabel.setLabelFor(databaseLocationValueLabel);
        org.openide.awt.Mnemonics.setLocalizedText(databaseLocationLabel, org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "LBL_DatabaseLocation")); // NOI18N

        userLabel.setLabelFor(userTextField);
        org.openide.awt.Mnemonics.setLocalizedText(userLabel, org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "LBL_UserName")); // NOI18N

        userTextField.setColumns(15);

        passwordLabel.setLabelFor(passwordTextField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "LBL_Password")); // NOI18N

        passwordTextField.setColumns(15);

        org.openide.awt.Mnemonics.setLocalizedText(propertiesButton, org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "LBL_Properties")); // NOI18N
        propertiesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                propertiesButtonActionPerformed(evt);
            }
        });

        databaseLocationValueLabel.setToolTipText(derbySystemHome.getAbsolutePath());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(databaseNameLabel)
                    .add(passwordLabel)
                    .add(userLabel)
                    .add(databaseLocationLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, databaseNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, passwordTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, userTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                    .add(databaseLocationValueLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 296, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(propertiesButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(databaseNameLabel)
                    .add(databaseNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(11, 11, 11)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(userLabel)
                    .add(userTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(passwordLabel)
                    .add(passwordTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(databaseLocationLabel)
                    .add(propertiesButton)
                    .add(databaseLocationValueLabel))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {databaseNameTextField, passwordTextField, userTextField}, org.jdesktop.layout.GroupLayout.VERTICAL);

        databaseNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "ACSD_CreateDatabasePanel_databaseNameTextField")); // NOI18N
        userTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "ACSD_CreateDatabasePanel_userTextField")); // NOI18N
        passwordTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "ACSD_CreateDatabasePanel_passwordTextField")); // NOI18N
        propertiesButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "ACSN_CreateDatabasePanel_propertiesButton")); // NOI18N
        propertiesButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "ACSD_CreateDatabasePanel_propertiesButton")); // NOI18N
        databaseLocationValueLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "ACSN_CreateDatabasePanel_databaseLocationValueLabel")); // NOI18N
        databaseLocationValueLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "ACSD_CreateDatabasePanel_databaseLocationValueLabel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void propertiesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_propertiesButtonActionPerformed
        DerbyPropertiesPanel.showDerbyProperties();
        String newLocation = DerbyOptions.getDefault().getSystemHome();
        databaseLocationValueLabel.setText(newLocation);
        databaseLocationValueLabel.setToolTipText(newLocation);
}//GEN-LAST:event_propertiesButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel databaseLocationLabel;
    public javax.swing.JLabel databaseLocationValueLabel;
    public javax.swing.JLabel databaseNameLabel;
    public javax.swing.JTextField databaseNameTextField;
    public javax.swing.JLabel passwordLabel;
    public javax.swing.JPasswordField passwordTextField;
    public javax.swing.JButton propertiesButton;
    public javax.swing.JLabel userLabel;
    public javax.swing.JTextField userTextField;
    // End of variables declaration//GEN-END:variables
    
}
