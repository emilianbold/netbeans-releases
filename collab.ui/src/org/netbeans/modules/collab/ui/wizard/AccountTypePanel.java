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
import org.openide.awt.*;
import org.openide.util.*;

import java.awt.Component;
import java.awt.Cursor;

import java.io.*;

import java.net.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.core.Debug;


/**
 *
 *
 */
public class AccountTypePanel extends WizardPanelBase {
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox acceptCheckBox;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JRadioButton existingAccountBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel messageLbl;
    private javax.swing.JRadioButton newAccountBtn;
    private javax.swing.JRadioButton newHostedServerAccountBtn;
    private javax.swing.JLabel termsOfConditionLbl;

    /**
     *
     *
     */
    public AccountTypePanel() {
        super(NbBundle.getMessage(AccountTypePanel.class, "LBL_AccountTypePanel_Name")); // NOI18N
        initComponents();
        termsOfConditionLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
    boolean isNewHostedServerAccountSelected() {
        return newHostedServerAccountBtn.isSelected();
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
    public void readSettings(Object object) {
        AccountWizardSettings settings = AccountWizardSettings.narrow(object);

        switch (settings.getAccount().getAccountType()) {
        case Account.NEW_ACCOUNT:
            newAccountBtn.setSelected(true);
            setValid(true);

            break;

        case Account.NEW_PUBLIC_SERVER_ACCOUNT:
            newHostedServerAccountBtn.setSelected(true);
            setValid(acceptCheckBox.isSelected());

            break;

        case Account.EXISTING_ACCOUNT:
            existingAccountBtn.setSelected(true);
            setValid(true);

            break;

        default:
            newHostedServerAccountBtn.setSelected(true);
            setValid(false);
        }

        messageLbl.setText(settings.getMessage());
    }

    /**
     *
     *
     */
    public void storeSettings(Object object) {
        AccountWizardSettings settings = AccountWizardSettings.narrow(object);

        if (existingAccountBtn.isSelected()) {
            settings.getAccount().setAccountType(Account.EXISTING_ACCOUNT);
            settings.setNewAccount(false);
        } else if (newAccountBtn.isSelected()) {
            settings.getAccount().setAccountType(Account.NEW_ACCOUNT);
            settings.setNewAccount(true);
        } else {
            settings.getAccount().setAccountType(Account.NEW_PUBLIC_SERVER_ACCOUNT);
            settings.setNewAccount(true);
        }
    }

    public void initAccessibility() {
        // MCF Notes
        // I am not expert here, but it seems like there are a number of things
        // which can/should be set
        // 1. mnemonics are set thus
        // fooLabel.setDisplayedMnemonic(bundle.getString (
        //   "MNE_App_Name_Selection_Panel.name.mnemonic").charAt(0)); // NOI18N
        // 2. a11y description and name set thus
        // addButton.getAccessibleContext().setAccessibleDescription(
        //  bundle.getString("ACSD_Map_Editor_Add_Button_DESC"));
        // addButton.getAccessibleContext().setAccessibleName(
        //  bundle.getString("ACSD_Map_Editor_Add_Button_NAME"));
        // And i do not know if setLabelFor is for a11y or other situation?
        //  appNameLabel.setLabelFor(appNameTextField);
        newHostedServerAccountBtn.setMnemonic(
            NbBundle.getMessage(AccountTypePanel.class, "MNE_AccountTypePanel_NewHostedServerAccount").charAt(0)
        ); // NOI18N		
        existingAccountBtn.setMnemonic(
            NbBundle.getMessage(AccountTypePanel.class, "MNE_AccountTypePanel_ExistingAccount").charAt(0)
        ); // NOI18N     
        newAccountBtn.setMnemonic(
            NbBundle.getMessage(AccountTypePanel.class, "MNE_AccountTypePanel_NewAccount").charAt(0)
        ); // NOI18N  

        newHostedServerAccountBtn.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AccountTypePanel.class, "ACSD_DESC_AccountTypePanel_NewHostedServerAccount")
        ); // NOI18N		
        existingAccountBtn.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AccountTypePanel.class, "ACSD_DESC_AccountTypePanel_ExistingAccount")
        ); // NOI18N     
        newAccountBtn.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AccountTypePanel.class, "ACSD_DESC_AccountTypePanel_NewAccount")
        ); // NOI18N   

        newHostedServerAccountBtn.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(AccountTypePanel.class, "ACSD_NAME_AccountTypePanel_NewHostedServerAccount")
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

    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        newHostedServerAccountBtn = new javax.swing.JRadioButton();
        acceptCheckBox = new javax.swing.JCheckBox();
        termsOfConditionLbl = new javax.swing.JLabel();
        newAccountBtn = new javax.swing.JRadioButton();
        existingAccountBtn = new javax.swing.JRadioButton();
        messageLbl = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        setBorder(
            new javax.swing.border.CompoundBorder(
                null, new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5))
            )
        );
        setPreferredSize(new java.awt.Dimension(450, 300));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "LBL_AccountTypePanel_AccountType"
            )
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        jPanel1.add(jLabel1, gridBagConstraints);

        buttonGroup.add(newHostedServerAccountBtn);
        newHostedServerAccountBtn.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "BTN_AccountTypePanel_NewHostedServerAccount"
            )
        );
        newHostedServerAccountBtn.addChangeListener(
            new javax.swing.event.ChangeListener() {
                public void stateChanged(javax.swing.event.ChangeEvent evt) {
                    newHostedServerAccountBtnStateChanged(evt);
                }
            }
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(newHostedServerAccountBtn, gridBagConstraints);

        acceptCheckBox.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "LBL_AccountTypePanel_AcceptCheckBox"
            )
        );
        acceptCheckBox.setEnabled(false);
        acceptCheckBox.setFocusPainted(false);
        acceptCheckBox.setMargin(new java.awt.Insets(2, 22, 2, 2));
        acceptCheckBox.addChangeListener(
            new javax.swing.event.ChangeListener() {
                public void stateChanged(javax.swing.event.ChangeEvent evt) {
                    acceptCheckBoxStateChanged(evt);
                }
            }
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(acceptCheckBox, gridBagConstraints);

        termsOfConditionLbl.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "LBL_AccountTypePanel_TermsOfConditionLnk"
            )
        );
        termsOfConditionLbl.addMouseListener(
            new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    termsOfConditionLblMouseClicked(evt);
                }
            }
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(termsOfConditionLbl, gridBagConstraints);

        buttonGroup.add(newAccountBtn);
        newAccountBtn.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "BTN_AccountTypePanel_NewAccount"
            )
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(newAccountBtn, gridBagConstraints);

        buttonGroup.add(existingAccountBtn);
        existingAccountBtn.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "BTN_AccountTypePanel_ExistingAccount"
            )
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(existingAccountBtn, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jPanel1, gridBagConstraints);

        messageLbl.setForeground(new java.awt.Color(255, 51, 0));
        messageLbl.setText(" ");
        messageLbl.setMaximumSize(new java.awt.Dimension(2147483647, 15));
        messageLbl.setMinimumSize(new java.awt.Dimension(61, 35));
        messageLbl.setPreferredSize(new java.awt.Dimension(644, 15));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        add(messageLbl, gridBagConstraints);
    }

    // </editor-fold>//GEN-END:initComponents
    private void acceptCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) { //GEN-FIRST:event_acceptCheckBoxStateChanged
        setValid(acceptCheckBox.isSelected());
    } //GEN-LAST:event_acceptCheckBoxStateChanged

    private void newHostedServerAccountBtnStateChanged(javax.swing.event.ChangeEvent evt) { //GEN-FIRST:event_newHostedServerAccountBtnStateChanged

        if (newHostedServerAccountBtn.isSelected()) {
            acceptCheckBox.setEnabled(true);
        } else {
            acceptCheckBox.setSelected(false);
            acceptCheckBox.setEnabled(false);
            setValid(true);
        }
    } //GEN-LAST:event_newHostedServerAccountBtnStateChanged

    private void termsOfConditionLblMouseClicked(java.awt.event.MouseEvent evt) { //GEN-FIRST:event_termsOfConditionLblMouseClicked

        String text = NbBundle.getMessage(AccountTypePanel.class, "MSG_AccountTypePanel_TermsOfCondition"); // NOI18N
        String userHome = System.getProperty("netbeans.user"); // NOI18N
        String legalFileDir = userHome + File.separator + "collab" + // NOI18N
            File.separator + "java.net"; // NOI18N
        String legalFileName = legalFileDir + File.separator + "terms_of_condition.html"; // NOI18N
        File legalFile = new File(legalFileName);

        if (!legalFile.exists()) {
            File legalFileDirectory = new File(legalFileDir);
            legalFileDirectory.mkdirs();

            try {
                FileOutputStream output = new FileOutputStream(legalFile);
                output.write(text.getBytes());
                output.flush();
                output.close();
            } catch (FileNotFoundException fnf) {
                Debug.debugNotify(fnf);
            } catch (IOException ioe) {
                Debug.debugNotify(ioe);
            }
        }

        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(legalFile.toURI().toURL());
        } catch (MalformedURLException me) {
            Debug.debugNotify(me);
        }
    } //GEN-LAST:event_termsOfConditionLblMouseClicked

    // End of variables declaration//GEN-END:variables
}
