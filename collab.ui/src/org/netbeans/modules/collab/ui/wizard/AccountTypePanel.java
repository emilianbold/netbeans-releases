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
import java.awt.Cursor;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JPanel;
import org.openide.util.NbBundle;

/**
 *
 *
 */
public class AccountTypePanel extends JPanel {

    private WizardPanelBase wizardPanel;

    /**
     *
     *
     */
    public AccountTypePanel (WizardPanelBase wizardPanel) {
        this.wizardPanel = wizardPanel;
        setName(NbBundle.getMessage(AccountTypePanel.class, "LBL_AccountTypePanel_Name"));
        initComponents();
        lblAddtionalInfoLnk.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        initAccessibility();
    }

    /**
     *
     *
     */
    boolean isNewAccountSelected() {
        return newAccountBtn.isSelected();
    }
    
    /**
     *
     *
     */
    boolean isExistingAccountSelected() {
        return existingAccountBtn.isSelected();
    }

    /**
     *
     *
     */
    void readSettings(Object object) {
        AccountWizardSettings settings = AccountWizardSettings.narrow(object);

        switch (settings.getAccount().getAccountType()) {
        case Account.NEW_ACCOUNT:
            newAccountBtn.setSelected(true);
            wizardPanel.setValid(true);

            break;

        case Account.EXISTING_ACCOUNT:
            existingAccountBtn.setSelected(true);
            wizardPanel.setValid(true);

            break;

        default:
            wizardPanel.setValid(false);
        }

        messageLbl.setText(settings.getMessage());
    }

    /**
     *
     *
     */
    void storeSettings(Object object) {
        AccountWizardSettings settings = AccountWizardSettings.narrow(object);

        if (existingAccountBtn.isSelected()) {
            settings.getAccount().setAccountType(Account.EXISTING_ACCOUNT);
            settings.setNewAccount(false);
        } else if (newAccountBtn.isSelected()) {
            settings.getAccount().setAccountType(Account.NEW_ACCOUNT);
            settings.setNewAccount(true);
        }
    }

    private void initAccessibility() {
        existingAccountBtn.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AccountTypePanel.class, "ACSD_DESC_AccountTypePanel_ExistingAccount")
        ); // NOI18N     
        newAccountBtn.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AccountTypePanel.class, "ACSD_DESC_AccountTypePanel_NewAccount")
        ); // NOI18N   

        existingAccountBtn.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(AccountTypePanel.class, "ACSD_NAME_AccountTypePanel_ExistingAccount")
        ); // NOI18N     
        newAccountBtn.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(AccountTypePanel.class, "ACSD_NAME_AccountTypePanel_NewAccount")
        ); // NOI18N        

        jLabel1.setLabelFor(null);
        messageLbl.setLabelFor(null);
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

        buttonGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        newAccountBtn = new javax.swing.JRadioButton();
        existingAccountBtn = new javax.swing.JRadioButton();
        lblAdditionalInfo = new javax.swing.JLabel();
        lblAddtionalInfoLnk = new javax.swing.JLabel();
        messageLbl = new javax.swing.JLabel();

        FormListener formListener = new FormListener();

        setBorder(javax.swing.BorderFactory.createCompoundBorder(null, javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, bundle.getString("LBL_AccountTypePanel_AccountType")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        jPanel1.add(jLabel1, gridBagConstraints);

        buttonGroup.add(newAccountBtn);
        org.openide.awt.Mnemonics.setLocalizedText(newAccountBtn, bundle.getString("BTN_AccountTypePanel_NewAccount")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(newAccountBtn, gridBagConstraints);

        buttonGroup.add(existingAccountBtn);
        org.openide.awt.Mnemonics.setLocalizedText(existingAccountBtn, bundle.getString("BTN_AccountTypePanel_ExistingAccount")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(existingAccountBtn, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(lblAdditionalInfo, org.openide.util.NbBundle.getMessage(AccountTypePanel.class, "LBL_AccountTypePanel_AdditionalInfo")); // NOI18N
        lblAdditionalInfo.setFocusable(false);
        lblAdditionalInfo.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel1.add(lblAdditionalInfo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(lblAddtionalInfoLnk, org.openide.util.NbBundle.getMessage(AccountTypePanel.class, "LBL_AccountTypePanel_AdditionalInfoLnk")); // NOI18N
        lblAddtionalInfoLnk.addMouseListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel1.add(lblAddtionalInfoLnk, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jPanel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(messageLbl, " ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        add(messageLbl, gridBagConstraints);
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.MouseListener {
        FormListener() {}
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            if (evt.getSource() == lblAddtionalInfoLnk) {
                AccountTypePanel.this.lblAdditionalLinkInfoLnkHandlerMouseClicked(evt);
            }
        }

        public void mouseEntered(java.awt.event.MouseEvent evt) {
        }

        public void mouseExited(java.awt.event.MouseEvent evt) {
        }

        public void mousePressed(java.awt.event.MouseEvent evt) {
        }

        public void mouseReleased(java.awt.event.MouseEvent evt) {
        }
    }// </editor-fold>//GEN-END:initComponents

    private void lblAdditionalLinkInfoLnkHandlerMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblAdditionalLinkInfoLnkHandlerMouseClicked
        URL u = null;
        try {
            u = new URL(NbBundle.getMessage(AccountTypePanel.class, "NB_WIKI_URL")); // NOI18N
        } catch (MalformedURLException exc) {
        }
        if (u != null) {
            org.openide.awt.HtmlBrowser.URLDisplayer.getDefault().showURL(u);
        }
    }//GEN-LAST:event_lblAdditionalLinkInfoLnkHandlerMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JRadioButton existingAccountBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblAdditionalInfo;
    private javax.swing.JLabel lblAddtionalInfoLnk;
    private javax.swing.JLabel messageLbl;
    private javax.swing.JRadioButton newAccountBtn;
    // End of variables declaration//GEN-END:variables
}
