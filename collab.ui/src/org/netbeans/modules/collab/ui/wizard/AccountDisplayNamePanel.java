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
public class AccountDisplayNamePanel extends JPanel {

    private WizardPanelBase wizardPanel;

    /**
     *
     *
     */
    public AccountDisplayNamePanel (WizardPanelBase wizardPanel) {
        this.wizardPanel = wizardPanel;
        setName(NbBundle.getMessage(AccountDisplayNamePanel.class, "LBL_AccountDisplayNamePanel_Name"));

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

        displayNameField.getDocument().addDocumentListener(docListener);
    }

    /**
     *
     *
     */
    void readSettings(Object object) {
        Account account = AccountWizardSettings.narrow(object).getAccount();

        displayNameField.setText(account.getDisplayName());

        if (account.getAccountType() == Account.EXISTING_ACCOUNT) {
            String msg = NbBundle.getMessage(
                    AccountDisplayNamePanel.class, "LBL_AccountDisplayNamePanel_ExistingAccount_Message"
                ); // NOI18N
            messageLabel.setText(msg);
        } else {
            String msg = NbBundle.getMessage(AccountDisplayNamePanel.class, "LBL_AccountDisplayNamePanel_Message"); // NOI18N
            messageLabel.setText(msg);
        }

        /*
                        if (account.getDisplayName() != null &&
                                account.getDisplayName().trim().length()>0)
                        {
                                displayNameField.setText(account.getDisplayName());
                        }
                        else
                        {
                                displayNameField.setText(
                                        NbBundle.getMessage(AccountDisplayNamePanel.class,
                                        "LBL_AccountDisplayNamePanel_Name"));
                                displayNameField.selectAll();
                        }
                        displayNameField.requestFocus();
         */
    }

    /**
     *
     *
     */
    void storeSettings(Object object) {
        if (object instanceof AccountWizardSettings) {
            Account account = AccountWizardSettings.narrow(object).getAccount();
            account.setDisplayName(displayNameField.getText().trim());
        }
    }

    /**
     *
     *
     */
    protected void checkValidity() {
        wizardPanel.setValid(displayNameField.getText().trim().length() > 0);
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
                    displayNameField.requestFocus();
                }
            }
        );
    }

    private void initAccessibility() {
        displayNameField.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AccountUserInfoPanel.class, "ACSD_DESC_AccountDisplayNamePanel_DisplayNameField")
        ); // NOI18N
        displayNameField.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(AccountUserInfoPanel.class, "ACSD_NAME_AccountDisplayNamePanel_DisplayNameField")
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
        displayNameLabel = new javax.swing.JLabel();
        displayNameField = new javax.swing.JTextField();
        exampleLabel = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createCompoundBorder(null, javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        setLayout(new java.awt.GridBagLayout());

        jPanel2.setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, bundle.getString("LBL_AccountDisplayNamePanel_Message")); // NOI18N
        messageLabel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        jPanel2.add(messageLabel, gridBagConstraints);

        displayNameLabel.setLabelFor(displayNameField);
        org.openide.awt.Mnemonics.setLocalizedText(displayNameLabel, bundle.getString("LBL_AccountDisplayNamePanel_ServerURL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        jPanel2.add(displayNameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 0);
        jPanel2.add(displayNameField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(exampleLabel, bundle.getString("MSG_AccountDisplayNamePanel_Example")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel2.add(exampleLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField displayNameField;
    private javax.swing.JLabel displayNameLabel;
    private javax.swing.JLabel exampleLabel;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel messageLabel;
    // End of variables declaration//GEN-END:variables
}
