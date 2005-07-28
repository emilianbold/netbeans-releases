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
import java.awt.event.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import org.netbeans.modules.collab.*;


/**
 *
 *
 */
public class AccountServerPanel extends WizardPanelBase {
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JLabel displayNameLbl;
    private javax.swing.JRadioButton httpProxyButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel layoutHack;
    private javax.swing.JRadioButton noProxyButton;
    private javax.swing.JPasswordField proxyPassword;
    private javax.swing.JLabel proxyPasswordLabel;
    private javax.swing.JTextField proxyServer;
    private javax.swing.JLabel proxyServerExample;
    private javax.swing.JLabel proxyServerLabel;
    private javax.swing.JPanel proxyServerPanel;
    private javax.swing.JPanel proxyTypePanel;
    private javax.swing.JTextField proxyUserName;
    private javax.swing.JLabel proxyUserNameLabel;
    private javax.swing.JTextField serverField;
    private javax.swing.JLabel serverLbl;
    private javax.swing.JRadioButton socksProxyButton;

    /**
     *
     *
     */
    public AccountServerPanel() {
        super(NbBundle.getMessage(AccountServerPanel.class, "LBL_AccountServerPanel_Name")); // NOI18N

        initComponents();

        //		setPreferredSize(AccountWizardDescriptor.PREFERRED_DIMENSION);
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

        serverField.getDocument().addDocumentListener(docListener);
        proxyServer.getDocument().addDocumentListener(docListener);
        proxyUserName.getDocument().addDocumentListener(docListener);
        proxyPassword.getDocument().addDocumentListener(docListener);

        ItemListener itemListener = new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    checkValidity();
                    updateEnabledState();
                }
            };

        noProxyButton.addItemListener(itemListener);
        httpProxyButton.addItemListener(itemListener);
        socksProxyButton.addItemListener(itemListener);
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
                    serverField.requestFocus();
                }
            }
        );
    }

    /**
     *
     *
     */

    /*pkg*/ boolean isProxySelected() {
        return !noProxyButton.isSelected();
    }

    /**
     *
     *
     */
    public void readSettings(Object object) {
        AccountWizardSettings settings = AccountWizardSettings.narrow(object);
        Account account = settings.getAccount();
        String prepopulated_serverURL = NbBundle.getMessage(
                AccountServerPanel.class, "PROP_AccountServerPanel_ServerURL"
            ); // NOI18N
        serverField.setEditable(true);

        if (settings.getAccount().getAccountType() == Account.NEW_PUBLIC_SERVER_ACCOUNT) {
            serverField.setText(prepopulated_serverURL);
            serverField.setEditable(false);
        } else {
            String serverURL = account.getServer();

            if ((serverURL != null) && !serverURL.trim().equals("")) {
                serverField.setText(serverURL);
            }
        }

        switch (account.getProxyType()) {
        case Account.PROXY_HTTPS:
            httpProxyButton.setSelected(true);

            break;

        case Account.PROXY_SOCKS_5:
            socksProxyButton.setSelected(true);

            break;

        case Account.PROXY_NONE:default:
            noProxyButton.setSelected(true);
        }

        proxyServer.setText(account.getProxyServer());
        proxyUserName.setText(account.getProxyUserName());
        proxyPassword.setText(account.getProxyPassword());

        checkValidity();
        updateEnabledState();
    }

    /**
     *
     *
     */
    public void storeSettings(Object object) {
        storeSettings(AccountWizardSettings.narrow(object).getAccount());
    }

    /**
     *
     *
     */
    public void storeSettings(Account account) {
        account.setServer(serverField.getText().trim());

        if (!isProxySelected()) {
            account.setProxyType(Account.PROXY_NONE);
        } else {
            if (httpProxyButton.isSelected()) {
                account.setProxyType(Account.PROXY_HTTPS);
            } else if (socksProxyButton.isSelected()) {
                account.setProxyType(Account.PROXY_SOCKS_5);
            } else {
                assert false : "Invalid proxy selection (should not happen)";
            }

            account.setProxyServer(proxyServer.getText().trim());
            account.setProxyUserName(proxyUserName.getText().trim());
            account.setProxyPassword(proxyPassword.getText().trim());
        }
    }

    /**
     *
     *
     */
    protected void checkValidity() {
        if (serverField.getText().trim().length() == 0) {
            setValid(false);

            return;
        }

        if (!isProxySelected()) {
            setValid(true);
        } else {
            boolean hasServerText = proxyServer.getText().trim().length() > 0;

            // If either auth field has text, then the other must also
            boolean userNameHasText = proxyUserName.getText().trim().length() > 0;
            boolean neitherHaveText = (proxyUserName.getText().trim().length() == 0) &&
                (proxyPassword.getText().trim().length() == 0);

            setValid(hasServerText && (neitherHaveText || userNameHasText));
        }
    }

    /**
     *
     *
     */
    protected void updateEnabledState() {
        proxyServerPanel.setVisible(isProxySelected());

        // Modify the example based on the current selection
        String key = "LBL_AccountProxyPanel_ProxyServer_HttpsExample";

        if (socksProxyButton.isSelected()) {
            key = "LBL_AccountProxyPanel_ProxyServer_SocksExample";
        }

        proxyServerExample.setText(NbBundle.getMessage(AccountServerPanel.class, key));
    }

    public void initAccessibility() {
        httpProxyButton.setMnemonic(
            NbBundle.getMessage(AccountServerPanel.class, "MNE_AccountProxyPanel_HttpProxy").charAt(0)
        ); // NOI18N
        noProxyButton.setMnemonic(
            NbBundle.getMessage(AccountServerPanel.class, "MNE_AccountProxyPanel_NoProxy").charAt(0)
        ); // NOI18N
        socksProxyButton.setMnemonic(
            NbBundle.getMessage(AccountServerPanel.class, "MNE_AccountProxyPanel_SocksProxy").charAt(0)
        ); // NOI18N

        httpProxyButton.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AccountServerPanel.class, "ACSD_DESC_AccountProxyPanel_HttpProxy")
        ); // NOI18N
        noProxyButton.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AccountServerPanel.class, "ACSD_DESC_AccountProxyPanel_NoProxy")
        ); // NOI18N
        socksProxyButton.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AccountServerPanel.class, "ACSD_DESC_AccountProxyPanel_SocksProxy")
        ); // NOI18N

        httpProxyButton.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(AccountServerPanel.class, "ACSD_NAME_AccountProxyPanel_HttpProxy")
        ); // NOI18N
        noProxyButton.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(AccountServerPanel.class, "ACSD_NAME_AccountProxyPanel_NoProxy")
        ); // NOI18N
        socksProxyButton.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(AccountServerPanel.class, "ACSD_NAME_AccountProxyPanel_SocksProxy")
        ); // NOI18N

        serverLbl.setLabelFor(serverField);
        proxyPasswordLabel.setLabelFor(proxyPassword);
        proxyServerLabel.setLabelFor(proxyServer);
        proxyUserNameLabel.setLabelFor(proxyUserName);

        displayNameLbl.setLabelFor(null);
        layoutHack.setLabelFor(null);
        jLabel1.setLabelFor(null);
        proxyServerExample.setLabelFor(null);

        serverField.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AccountServerPanel.class, "ACSD_DESC_AccountProxyPanel_ServerField")
        ); // NOI18N
        proxyPassword.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AccountServerPanel.class, "ACSD_DESC_AccountProxyPanel_ProxyPassword")
        ); // NOI18N
        proxyServer.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AccountServerPanel.class, "ACSD_DESC_AccountProxyPanel_ProxyServer")
        ); // NOI18N 
        proxyUserName.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AccountServerPanel.class, "ACSD_DESC_AccountProxyPanel_ProxyUserName")
        ); // NOI18N 

        serverField.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(AccountServerPanel.class, "ACSD_NAME_AccountProxyPanel_ServerField")
        ); // NOI18N
        proxyPassword.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(AccountServerPanel.class, "ACSD_NAME_AccountProxyPanel_ProxyPassword")
        ); // NOI18N
        proxyServer.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(AccountServerPanel.class, "ACSD_NAME_AccountProxyPanel_ProxyServer")
        ); // NOI18N 
        proxyUserName.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(AccountServerPanel.class, "ACSD_NAME_AccountProxyPanel_ProxyUserName")
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

        buttonGroup = new javax.swing.ButtonGroup();
        jPanel2 = new javax.swing.JPanel();
        displayNameLbl = new javax.swing.JLabel();
        serverLbl = new javax.swing.JLabel();
        serverField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        proxyTypePanel = new javax.swing.JPanel();
        noProxyButton = new javax.swing.JRadioButton();
        httpProxyButton = new javax.swing.JRadioButton();
        socksProxyButton = new javax.swing.JRadioButton();
        proxyServerPanel = new javax.swing.JPanel();
        proxyServerLabel = new javax.swing.JLabel();
        proxyServer = new javax.swing.JTextField();
        proxyServerExample = new javax.swing.JLabel();
        proxyUserNameLabel = new javax.swing.JLabel();
        proxyUserName = new javax.swing.JTextField();
        proxyPasswordLabel = new javax.swing.JLabel();
        proxyPassword = new javax.swing.JPasswordField();
        layoutHack = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        setBorder(
            new javax.swing.border.CompoundBorder(
                null, new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5))
            )
        );
        jPanel2.setLayout(new java.awt.GridBagLayout());

        displayNameLbl.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "LBL_AccountServerPanel_Message"
            )
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        jPanel2.add(displayNameLbl, gridBagConstraints);

        serverLbl.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "LBL_AccountServerPanel_ServerURL"
            )
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        jPanel2.add(serverLbl, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 0);
        jPanel2.add(serverField, gridBagConstraints);

        jLabel1.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "MSG_AccountServerPanel_Example"
            )
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel2.add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(jPanel2, gridBagConstraints);

        proxyTypePanel.setLayout(new java.awt.GridBagLayout());

        proxyTypePanel.setBorder(
            new javax.swing.border.CompoundBorder(
                new javax.swing.border.TitledBorder(
                    java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                        "LBL_AccountProxyPanel_ProxyTypePanel"
                    )
                ), new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 5, 5, 5))
            )
        );
        noProxyButton.setSelected(true);
        noProxyButton.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "LBL_AccountProxyPanel_NoProxy"
            )
        );
        buttonGroup.add(noProxyButton);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        proxyTypePanel.add(noProxyButton, gridBagConstraints);

        httpProxyButton.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "LBL_AccountProxyPanel_HttpProxy"
            )
        );
        buttonGroup.add(httpProxyButton);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        proxyTypePanel.add(httpProxyButton, gridBagConstraints);

        socksProxyButton.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "LBL_AccountProxyPanel_SocksProxy"
            )
        );
        buttonGroup.add(socksProxyButton);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        proxyTypePanel.add(socksProxyButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        add(proxyTypePanel, gridBagConstraints);

        proxyServerPanel.setLayout(new java.awt.GridBagLayout());

        proxyServerPanel.setBorder(
            new javax.swing.border.CompoundBorder(
                new javax.swing.border.TitledBorder(
                    java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                        "LBL_AccountProxyPanel_ProxyServerPanel"
                    )
                ), new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5))
            )
        );
        proxyServerLabel.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "LBL_AccountProxyPanel_ProxyServer"
            )
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        proxyServerPanel.add(proxyServerLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 0);
        proxyServerPanel.add(proxyServer, gridBagConstraints);

        proxyServerExample.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "LBL_AccountProxyPanel_ProxyServer_HttpsExample"
            )
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 0);
        proxyServerPanel.add(proxyServerExample, gridBagConstraints);

        proxyUserNameLabel.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "LBL_AccountProxyPanel_ProxyAuthPanel_ProxyUserName"
            )
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        proxyServerPanel.add(proxyUserNameLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 0);
        proxyServerPanel.add(proxyUserName, gridBagConstraints);

        proxyPasswordLabel.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "LBL_AccountProxyPanel_ProxyAuthPanel_ProxyPassword"
            )
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        proxyServerPanel.add(proxyPasswordLabel, gridBagConstraints);

        proxyPassword.setMinimumSize(new java.awt.Dimension(11, 20));
        proxyPassword.setPreferredSize(new java.awt.Dimension(11, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 0);
        proxyServerPanel.add(proxyPassword, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(proxyServerPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(layoutHack, gridBagConstraints);
    } //GEN-END:initComponents

    // End of variables declaration//GEN-END:variables
}
