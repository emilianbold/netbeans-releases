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
package org.netbeans.modules.collab.ui;

import com.sun.collablet.Account;
import com.sun.collablet.AccountManager;
import com.sun.collablet.CollabManager;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.*;

import java.beans.*;

import java.io.*;

import java.util.*;

import javax.swing.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.ui.actions.*;


/**
 *
 * @author  todd
 */
public class LoginAccountPanel extends JPanel {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    private static final boolean SHOW_BACKGROUND = false;

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox accountComboBox;
    private javax.swing.JLabel accountLabel;
    private javax.swing.JPanel accountPanel;
    private javax.swing.JScrollPane accountScrollPane;
    private javax.swing.JCheckBox autoLoginCheckBox;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JButton loginButton;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JLabel newAccountLink;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JCheckBox rememberPasswordCheckBox;

    // End of variables declaration//GEN-END:variables
    private Image backgroundImage;
    private Set pendingLogins = Collections.synchronizedSet(new HashSet());
    private Account newAccount;
    private int locks;

    /**
     *
     *
     */
    public LoginAccountPanel() {
        super();
        initialize();
        setHelpCtx();
    }

    /**
     *
     *
     */
    private LoginAccountPanel(boolean embeded) {
        this();

        // HACK
        if (embeded) {
            loginButton.setVisible(false);
            cancelButton.setVisible(false);
        }

        setHelpCtx();
    }

    /**
     *set help ctx map id for context sensitive help
     *
     */
    private void setHelpCtx() {
        HelpCtx.setHelpIDString(this, "collab_about_collab"); //NOI18n

        if (accountComboBox != null) {
            HelpCtx.setHelpIDString(accountComboBox, "collab_about_collab"); //NOI18n
        }
    }

    /**
     *
     *
     */
    private void initialize() {
        initComponents();
        newAccountLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        accountScrollPane.getVerticalScrollBar().setUnitIncrement(25);

        // Set the renderer for the account combo box
        accountComboBox.setRenderer(new AccountListRenderer());

        updateAccountList();

        if (SHOW_BACKGROUND) {
            backgroundImage = Utilities.loadImage("/org/netbeans/modules/collab/ui/resources/login_bg.jpg");

            if (backgroundImage == null) {
                Debug.out.println("Couldn't load background image");
            }
        }

        // Listen to changes in the session list
        if (CollabManager.getDefault() != null) {
            CollabManager.getDefault().addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        if (event.getPropertyName().equals(CollabManager.PROP_SESSIONS)) {
                            cancelButton.setEnabled(((CollabManager) event.getSource()).getSessions().length > 0);
                        }
                    }
                }
            );
        }

        // Listen to changes to the account list
        AccountManager.getDefault().addPropertyChangeListener(
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent event) {
                    if (event.getPropertyName().equals(AccountManager.PROP_ACCOUNTS)) {
                        updateAccountList();
                    }
                }
            }
        );
    }

    /**
     * Called explicitly from CollabExplorerPanel when this component is shown
     *
     */
    public void showNotify() {
        updateSelectedAccountStatus(getSelectedAccount());
        accountComboBox.requestFocus();
    }

    /**
     *
     *
     */
    public void lock(String message) {
        if (++locks == 1) {
            accountComboBox.setEnabled(false);
            loginButton.setEnabled(false);
            passwordLabel.setEnabled(false);
            passwordField.setEnabled(false);
            rememberPasswordCheckBox.setEnabled(false);
            autoLoginCheckBox.setEnabled(false);

            if (message != null) {
                messageLabel.setText(message);
            }
        }
    }

    /**
     *
     *
     */
    public void unlock() {
        if (--locks == 0) {
            messageLabel.setText("");
            accountComboBox.setEnabled(true);
            updateAccountList();
        }
    }

    /**
     *
     *
     */
    public void updateAccountList() {
        try {
            Account[] accounts = AccountManager.getDefault().getAccounts();
            Account defaultAccount = CollabManager.getDefault().getUserInterface().getDefaultAccount();

            // Sort the account list
            Arrays.sort(accounts, new AccountManager.AccountComparator());

            accountComboBox.removeAllItems();

            boolean addedAccount = false;
            int firstValidAccountIndex = -1;
            int selectedAccountIndex = -1;

            for (int i = 0; i < accounts.length; i++) {
                // Remember the first valid account index
                if ((firstValidAccountIndex == -1) && accounts[i].isValid()) {
                    firstValidAccountIndex = i;
                }

                if (selectedAccountIndex == -1) {
                    // If there is an explicit new account, select it. 
                    // Otherwise, select the default account
                    if (newAccount != null) {
                        if (accounts[i] == newAccount) {
                            selectedAccountIndex = i;
                        }
                    } else if (accounts[i] == defaultAccount) {
                        selectedAccountIndex = i;
                    }
                }

                accountComboBox.addItem(new AccountItem(accounts[i]));
                addedAccount = true;
            }

            // Let go of the new account now that we know if we should select it
            newAccount = null;

            if (addedAccount) {
                // set link label to "Manage Accounts"
                newAccountLink.setText(
                    NbBundle.getMessage(LoginAccountPanel.class, "LBL_LoginAccountForm_manageAccountLink")
                ); // NOI18N

                // If the default wasn't found, select the first valid account,
                // or simply the first account if none are valid
                if (selectedAccountIndex == -1) {
                    selectedAccountIndex = (firstValidAccountIndex != -1) ? firstValidAccountIndex : 0;
                }

                accountComboBox.setSelectedIndex(selectedAccountIndex);

                // Show status about the currently selected account, if any
                updateSelectedAccountStatus(getSelectedAccount());
            } else {
                messageLabel.setText(""); // NOI18N
            }

            accountPanel.setVisible(addedAccount);
        } catch (NullPointerException e) {
            Debug.debugNotify(
                e,
                "Be sure to close any open collab " + // NOI18N
                "windows when reinstalling the module in order to " + // NOI18N
                "avoid this problem"
            ); // NOI18N
        }
    }

    /**
     *
     *
     */
    public Account getSelectedAccount() {
        if (!accountComboBox.isEnabled()) {
            return null;
        }

        Object item = accountComboBox.getSelectedItem();

        if (item == null) {
            return null;
        }

        return ((AccountItem) item).getAccount();
    }

    /**
     *
     *
     */
    protected boolean updateSelectedAccountStatus(Account account) {
        if (account != getSelectedAccount()) {
            return false;
        }

        CollabManager manager = CollabManager.getDefault();
        assert manager != null : "Could not find default CollabManager"; // NOI18N

        if (manager.getSession(account) != null) {
            // Account already logged in
            loginButton.setEnabled(false);
            passwordLabel.setEnabled(false);
            passwordField.setEnabled(false);
            rememberPasswordCheckBox.setEnabled(false);
            autoLoginCheckBox.setEnabled(false);

            messageLabel.setText(NbBundle.getMessage(LoginAccountPanel.class, "MSG_LoginAccountPanel_AlreadyLoggedIn")); // NOI18N

            return false;
        } else if (pendingLogins.contains(account)) {
            loginButton.setEnabled(false);
            passwordLabel.setEnabled(false);
            passwordField.setEnabled(false);
            autoLoginCheckBox.setEnabled(false);
            rememberPasswordCheckBox.setEnabled(false);

            messageLabel.setText(NbBundle.getMessage(LoginAccountPanel.class, "MSG_LoginAccountPanel_LoggingIn")); // NOI18N

            return false;
        } else {
            // Reset everything to recover from pending login
            loginButton.setEnabled(true);
            passwordLabel.setEnabled(true);
            passwordField.setEnabled(true);
            autoLoginCheckBox.setEnabled(true);
            rememberPasswordCheckBox.setEnabled(true);
            messageLabel.setText(""); // NOI18N

            boolean result = (account != null) && account.isValid();
            boolean isDefault = account == manager.getUserInterface().getDefaultAccount();
            autoLoginCheckBox.setSelected(manager.getUserInterface().isAutoLoginAccount(account));

            loginButton.setEnabled(result);

            if (result) {
                messageLabel.setText(""); // NOI18N
                passwordLabel.setEnabled(true);
                passwordField.setEnabled(true);
                rememberPasswordCheckBox.setEnabled(true);

                if (account.getPassword() != null) {
                    passwordField.setText(account.getPassword());
                    rememberPasswordCheckBox.setSelected(true);
                } else {
                    passwordField.setText(""); // NOI18N
                    rememberPasswordCheckBox.setSelected(false);
                }
            } else {
                if (account != null) {
                    messageLabel.setText(
                        NbBundle.getMessage(LoginAccountPanel.class, "MSG_LoginAccountPanel_InvalidAccount")
                    ); // NOI18N
                }

                passwordLabel.setEnabled(false);
                passwordField.setEnabled(false);
                passwordField.setText("");
                rememberPasswordCheckBox.setSelected(false);
                rememberPasswordCheckBox.setEnabled(false);

                // Override the default account enabled state if invalid
                autoLoginCheckBox.setEnabled(false);
            }

            return result;
        }
    }

    /**
     *
     *
     */
    public void login(Account account) {
        CollabManager manager = CollabManager.getDefault();
        assert manager != null : "Could not find default CollabManager"; // NOI18N

        loginButton.setEnabled(false);

        // Prompt for the login
        if (account != null) {
            String password = passwordField.getText();
            assert password != null : "Password field return null text"; // NOI18N

            boolean rememberPassword = rememberPasswordCheckBox.isSelected();
            loginButton.setEnabled(false);

            // Remember that we are currently trying to log into this account
            pendingLogins.add(account);
            updateSelectedAccountStatus(account);

            // Perform the login in a different thread
            manager.getUserInterface().login(
                account, password, new LoginSuccessTask(account, password, rememberPassword),
                new LoginAccountPanel.LoginFailureTask(account)
            );
        }
    }

    /**
     *
     *
     */
    public void login(Account account, String password) {
        CollabManager manager = CollabManager.getDefault();
        assert manager != null : "Could not find default CollabManager"; // NOI18N

        loginButton.setEnabled(false);

        // Prompt for the login
        if (account != null) {
            loginButton.setEnabled(false);

            // Remember that we are currently trying to log into this account
            pendingLogins.add(account);
            updateSelectedAccountStatus(account);

            // Perform the login in a different thread
            manager.getUserInterface().login(
                account, password, new LoginSuccessTask(account, password, true),
                new LoginAccountPanel.LoginFailureTask(account)
            );
        }
    }

    /**
     *
     *
     */
    public void paint(Graphics g) {
        final Color BACKGROUND = Color.decode("#DBEAFA"); // NOI18N

        if (backgroundImage != null) {
            Color originalColor = g.getColor();
            g.setColor(BACKGROUND);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.drawImage(backgroundImage, 0, 0, null);
            g.setColor(originalColor);
        }

        super.paint(g);
    }

    //	/**
    //	 *
    //	 *
    //	 */
    //	public static Login promptForLogin(Login defaults)
    //	{
    //		// HACK: This is a hack because we have to replicate the login
    //		// logic between here and the LoginAction
    //		LoginAccountPanel form=new LoginAccountPanel(true);
    //		form.setLogin(defaults);
    //
    //		DialogDescriptor descriptor=new DialogDescriptor(form,
    //			NbBundle.getMessage(LoginForm.class,"TITLE_LoginForm"));
    //
    //		Dialog dialog=DialogDisplayer.getDefault().createDialog(descriptor);
    //		dialog.show();
    //
    //		if (descriptor.getValue()==DialogDescriptor.OK_OPTION)
    //			return form.getLogin();
    //		else
    //			return null;
    //	}
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        accountScrollPane = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        newAccountLink = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        accountPanel = new javax.swing.JPanel();
        accountLabel = new javax.swing.JLabel();
        accountComboBox = new javax.swing.JComboBox();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        rememberPasswordCheckBox = new javax.swing.JCheckBox();
        autoLoginCheckBox = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        messageLabel = new javax.swing.JLabel();
        loginButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));
        setPreferredSize(new java.awt.Dimension(275, 300));
        setRequestFocusEnabled(false);
        setOpaque(false);
        accountScrollPane.setBorder(null);
        accountScrollPane.setMinimumSize(new java.awt.Dimension(200, 22));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        newAccountLink.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        newAccountLink.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/Bundle").getString(
                "LBL_LoginAccountForm_newAccountLink"
            )
        );
        newAccountLink.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
        newAccountLink.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        newAccountLink.setMinimumSize(new java.awt.Dimension(109, 15));
        newAccountLink.addMouseListener(
            new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    newAccountLinkMouseClicked(evt);
                }
            }
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.weighty = 0.01;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        jPanel2.add(newAccountLink, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(jSeparator2, gridBagConstraints);

        accountPanel.setLayout(new java.awt.GridBagLayout());

        accountPanel.setOpaque(false);
        accountLabel.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/Bundle").getString(
                "LBL_LoginAccountPanel_Account"
            )
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        accountPanel.add(accountLabel, gridBagConstraints);

        accountComboBox.setMinimumSize(new java.awt.Dimension(24, 22));
        accountComboBox.setPreferredSize(new java.awt.Dimension(24, 22));
        accountComboBox.addActionListener(
            new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    accountComboBoxActionPerformed(evt);
                }
            }
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        accountPanel.add(accountComboBox, gridBagConstraints);

        passwordLabel.setLabelFor(passwordField);
        passwordLabel.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/Bundle").getString(
                "LBL_LoginAccountPanel_Password"
            )
        );
        passwordLabel.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        accountPanel.add(passwordLabel, gridBagConstraints);

        passwordField.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 5, 0);
        accountPanel.add(passwordField, gridBagConstraints);

        rememberPasswordCheckBox.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/Bundle").getString(
                "LBL_LoginAccountPanel_RememberPassword"
            )
        );
        rememberPasswordCheckBox.setOpaque(false);
        rememberPasswordCheckBox.addItemListener(
            new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent evt) {
                    rememberPasswordCheckBoxItemStateChanged(evt);
                }
            }
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        accountPanel.add(rememberPasswordCheckBox, gridBagConstraints);

        autoLoginCheckBox.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/Bundle").getString(
                "LBL_LoginAccountPanel_AutoLogin"
            )
        );
        autoLoginCheckBox.addItemListener(
            new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent evt) {
                    autoLoginCheckBoxItemStateChanged(evt);
                }
            }
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        accountPanel.add(autoLoginCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(accountPanel, gridBagConstraints);

        accountScrollPane.setViewportView(jPanel2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(accountScrollPane, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanel1.setOpaque(false);
        messageLabel.setForeground(new java.awt.Color(89, 78, 191));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(messageLabel, gridBagConstraints);

        loginButton.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/Bundle").getString(
                "BTN_LoginAccountPanel_Login"
            )
        );
        loginButton.addActionListener(
            new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    loginButtonActionPerformed(evt);
                }
            }
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel1.add(loginButton, gridBagConstraints);

        cancelButton.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/Bundle").getString(
                "LBL_LoginAccountPanel_cancelButton"
            )
        );
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(
            new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    cancelButtonActionPerformed(evt);
                }
            }
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
        jPanel1.add(cancelButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jPanel1, gridBagConstraints);
    }

    // </editor-fold>//GEN-END:initComponents
    private void rememberPasswordCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) { //GEN-FIRST:event_rememberPasswordCheckBoxItemStateChanged

        if (rememberPasswordCheckBox.isSelected()) {
            autoLoginCheckBox.setEnabled(true);
        } else {
            autoLoginCheckBox.setEnabled(false);
            autoLoginCheckBox.setSelected(false);
        }
    } //GEN-LAST:event_rememberPasswordCheckBoxItemStateChanged

    private void autoLoginCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) { //GEN-FIRST:event_autoLoginCheckBoxItemStateChanged

        if ((getSelectedAccount() != null) && getSelectedAccount().isValid()) {
            CollabManager.getDefault().getUserInterface().setAutoLoginAccount(
                getSelectedAccount(), evt.getStateChange() == ItemEvent.SELECTED
            );
        }
    } //GEN-LAST:event_autoLoginCheckBoxItemStateChanged

    private void accountComboBoxActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_accountComboBoxActionPerformed

        Account account = getSelectedAccount();

        if (account != null) {
            updateSelectedAccountStatus(account);
        }
    } //GEN-LAST:event_accountComboBoxActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cancelButtonActionPerformed

        // Show the collab explorer
        CollabExplorerPanel.getInstance().showComponent(CollabExplorerPanel.COMPONENT_EXPLORER);
    } //GEN-LAST:event_cancelButtonActionPerformed

    private void newAccountLinkMouseClicked(java.awt.event.MouseEvent evt) { //GEN-FIRST:event_newAccountLinkMouseClicked

        if (accountPanel.isVisible()) // bring up account managment dialog
         {
            CollabManager.getDefault().getUserInterface().manageAccounts(
                (getSelectedAccount() == null) ? null : getSelectedAccount()
            );
        } else // Prompt user to create a new account
         {
            newAccount = CollabManager.getDefault().getUserInterface().createNewAccount(null, null);
        }
    } //GEN-LAST:event_newAccountLinkMouseClicked

    private void loginButtonActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_loginButtonActionPerformed
        login(getSelectedAccount());
    } //GEN-LAST:event_loginButtonActionPerformed

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    protected class AccountItem extends Object {
        private Account account;

        /**
         *
         *
         */
        public AccountItem(Account account) {
            super();
            this.account = account;
        }

        /**
         *
         *
         */
        public String toString() {
            return NbBundle.getMessage(
                LoginAccountPanel.class, "LBL_LoginAccountPanel_AccountItem", // NOI18N
                account.getDisplayName(), account.getUserName() + "@" + account.getServer()
            ); // NOI18N
        }

        /**
         *
         *
         */
        public Account getAccount() {
            return account;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public final class AccountListRenderer extends DefaultListCellRenderer {
        private final Image IMAGE = Utilities.loadImage("org/netbeans/modules/collab/core/resources/account_png.gif"); // NOI18N
        private final Image DISABLED_IMAGE = GrayFilter.createDisabledImage(IMAGE);
        private final Icon ICON = new ImageIcon(IMAGE);
        private final Icon DISABLED_ICON = new ImageIcon(DISABLED_IMAGE);

        /**
         *
         *
         */
        public Component getListCellRendererComponent(
            JList list, Object value, int index, boolean isSelected, boolean cellHasFocus
        ) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            AccountItem item = (AccountItem) value;

            if (item != null) {
                label.setText(item.toString());
                list.setToolTipText(item.getAccount().getDisplayName());

                boolean unselectableAccount = false;

                try {
                    unselectableAccount = (CollabManager.getDefault().getSession(item.getAccount()) != null) ||
                        !item.getAccount().isValid();
                } catch (Exception e) {
                    Debug.debugNotify(e);
                }

                Color color = UIManager.getColor("textInactiveText"); // NOI18N

                if (color == null) {
                    color = Color.lightGray;
                }

                if (isSelected) {
                    label.setBackground(unselectableAccount ? color : label.getBackground());
                } else {
                    label.setForeground(unselectableAccount ? color : label.getForeground());
                }

                label.setIcon(unselectableAccount ? DISABLED_ICON : ICON);
            } else {
                label.setIcon(ICON);
            }

            label.setOpaque(isSelected);

            ToolTipManager.sharedInstance().registerComponent(list);

            return label;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Runs in AWT thread
     *
     */
    protected class LoginSuccessTask extends Object implements Runnable {
        private Account account;
        private String password;
        private boolean rememberPassword;

        /**
         *
         *
         */
        public LoginSuccessTask(Account account, String password, boolean rememberPassword) {
            super();
            this.account = account;
            this.password = password;
            this.rememberPassword = rememberPassword;
        }

        /**
         *
         *
         */
        public void run() {
            try {
                // If we logged in successfully, remember the password if
                // instructed to
                if (rememberPassword) {
                    // Store the current password in the account
                    account.setPassword(password);
                } else {
                    // Erase the password from the account
                    account.setPassword(null);

                    // Erase the current password field
                    if (account == getSelectedAccount()) {
                        passwordField.setText(""); // NOI18N
                    }
                }

                // Show the collab explorer
                CollabExplorerPanel.getInstance().showComponent(CollabExplorerPanel.COMPONENT_EXPLORER);
            } finally {
                loginButton.setEnabled(true);
                pendingLogins.remove(account);
                updateSelectedAccountStatus(account);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Runs in AWT thread
     *
     */
    protected class LoginFailureTask extends Object implements Runnable {
        private Account account;

        /**
         *
         *
         */
        public LoginFailureTask(Account account) {
            super();
            this.account = account;
        }

        /**
         *
         *
         */
        public void run() {
            loginButton.setEnabled(true);
            pendingLogins.remove(account);
            updateSelectedAccountStatus(account);
        }
    }
}
