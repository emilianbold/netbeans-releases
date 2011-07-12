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
import org.openide.util.NbBundle;

/**
 *
 *
 */
public class AccountUserInfoPanel extends JPanel {

    private WizardPanelBase wizardPanel;

    /**
     *
     *
     */
    public AccountUserInfoPanel (WizardPanelBase wizardPanel) {
        this.wizardPanel = wizardPanel;
        setName(NbBundle.getMessage(AccountUserInfoPanel.class, "LBL_AccountUserInfoPanel_Name"));
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

        firstNameField.getDocument().addDocumentListener(docListener);
        lastNameField.getDocument().addDocumentListener(docListener);
        emailField.getDocument().addDocumentListener(docListener);
    }

    /**
     *
     *
     */
    void readSettings(Object object) {
        Account account = AccountWizardSettings.narrow(object).getAccount();
        firstNameField.setText(account.getFirstName());
        lastNameField.setText(account.getLastName());
        emailField.setText(account.getEmail());
    }

    /**
     *
     *
     */
    void storeSettings(Object object) {
        Account account = AccountWizardSettings.narrow(object).getAccount();
        account.setFirstName(firstNameField.getText().trim());
        account.setLastName(lastNameField.getText().trim());
        account.setEmail(emailField.getText().trim());
    }

    /**
     *
     *
     */
    protected void checkValidity() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();

        boolean valid = (firstName.length() > 0) && (lastName.length() > 0) && (email.length() > 0);
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
                    firstNameField.requestFocus();
                }
            }
        );
    }

    private void initAccessibility() {
        emailField.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AccountUserInfoPanel.class, "ACSD_DESC_AccountUserInfoPanel_EmailField")
        ); // NOI18N
        firstNameField.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AccountUserInfoPanel.class, "ACSD_DESC_AccountUserInfoPanel_FirstNameField")
        ); // NOI18N
        lastNameField.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AccountUserInfoPanel.class, "ACSD_DESC_AccountUserInfoPanel_LastNameField")
        ); // NOI18N   

        emailField.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(AccountUserInfoPanel.class, "ACSD_NAME_AccountUserInfoPanel_EmailField")
        ); // NOI18N
        firstNameField.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(AccountUserInfoPanel.class, "ACSD_NAME_AccountUserInfoPanel_FirstNameField")
        ); // NOI18N
        lastNameField.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(AccountUserInfoPanel.class, "ACSD_NAME_AccountUserInfoPanel_LastNameField")
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

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        firstNameLbl = new javax.swing.JLabel();
        firstNameField = new javax.swing.JTextField();
        lastNameLbl = new javax.swing.JLabel();
        lastNameField = new javax.swing.JTextField();
        emailLbl = new javax.swing.JLabel();
        emailField = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createCompoundBorder(null, javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, bundle.getString("LBL_AccountUserInfoPanel_Message")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        jPanel1.add(jLabel1, gridBagConstraints);

        firstNameLbl.setLabelFor(firstNameField);
        org.openide.awt.Mnemonics.setLocalizedText(firstNameLbl, bundle.getString("LBL_AccountUserInfoPanel_FirstName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        jPanel1.add(firstNameLbl, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 0);
        jPanel1.add(firstNameField, gridBagConstraints);

        lastNameLbl.setLabelFor(lastNameField);
        org.openide.awt.Mnemonics.setLocalizedText(lastNameLbl, bundle.getString("LBL_AccountUserInfoPanel_LastName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        jPanel1.add(lastNameLbl, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 0);
        jPanel1.add(lastNameField, gridBagConstraints);

        emailLbl.setLabelFor(emailField);
        org.openide.awt.Mnemonics.setLocalizedText(emailLbl, bundle.getString("LBL_AccountUserInfoPanel_Email")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        jPanel1.add(emailLbl, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 0);
        jPanel1.add(emailField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField emailField;
    private javax.swing.JLabel emailLbl;
    private javax.swing.JTextField firstNameField;
    private javax.swing.JLabel firstNameLbl;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField lastNameField;
    private javax.swing.JLabel lastNameLbl;
    // End of variables declaration//GEN-END:variables
}
