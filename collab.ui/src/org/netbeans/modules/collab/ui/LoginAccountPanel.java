/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.collab.ui;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;
import org.openide.util.*;
import com.sun.collablet.Account;
import com.sun.collablet.AccountManager;
import com.sun.collablet.CollabManager;
import org.netbeans.modules.collab.core.Debug;
import org.openide.awt.Mnemonics;


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
            backgroundImage = ImageUtilities.loadImage("org/netbeans/modules/collab/ui/resources/login_bg.jpg");

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
                Mnemonics.setLocalizedText(newAccountLink, NbBundle.getMessage(LoginAccountPanel.class, "LBL_LoginAccountForm_manageAccountLink"));

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
        newAccountLink = new javax.swing.JButton();
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

        FormListener formListener = new FormListener();

        setLayout(new java.awt.GridBagLayout());

        setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(275, 300));
        setRequestFocusEnabled(false);
        accountScrollPane.setBorder(null);
        accountScrollPane.setMinimumSize(new java.awt.Dimension(200, 22));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(newAccountLink, org.openide.util.NbBundle.getMessage(LoginAccountPanel.class, "LBL_LoginAccountForm_newAccountLink")); // NOI18N
        newAccountLink.setBorderPainted(false);
        newAccountLink.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
        newAccountLink.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        newAccountLink.setMinimumSize(new java.awt.Dimension(109, 15));
        newAccountLink.addActionListener(formListener);

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
        accountLabel.setLabelFor(accountComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(accountLabel, org.openide.util.NbBundle.getMessage(LoginAccountPanel.class, "LBL_LoginAccountPanel_Account")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        accountPanel.add(accountLabel, gridBagConstraints);

        accountComboBox.setMinimumSize(new java.awt.Dimension(24, 22));
        accountComboBox.setPreferredSize(new java.awt.Dimension(24, 22));
        accountComboBox.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        accountPanel.add(accountComboBox, gridBagConstraints);

        passwordLabel.setLabelFor(passwordField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(LoginAccountPanel.class, "LBL_LoginAccountPanel_Password")); // NOI18N
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

        org.openide.awt.Mnemonics.setLocalizedText(rememberPasswordCheckBox, org.openide.util.NbBundle.getMessage(LoginAccountPanel.class, "LBL_LoginAccountPanel_RememberPassword")); // NOI18N
        rememberPasswordCheckBox.setOpaque(false);
        rememberPasswordCheckBox.addItemListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        accountPanel.add(rememberPasswordCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(autoLoginCheckBox, org.openide.util.NbBundle.getMessage(LoginAccountPanel.class, "LBL_LoginAccountPanel_AutoLogin")); // NOI18N
        autoLoginCheckBox.addItemListener(formListener);

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

        org.openide.awt.Mnemonics.setLocalizedText(loginButton, org.openide.util.NbBundle.getMessage(LoginAccountPanel.class, "BTN_LoginAccountPanel_Login")); // NOI18N
        loginButton.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel1.add(loginButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(LoginAccountPanel.class, "LBL_LoginAccountPanel_cancelButton")); // NOI18N
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(formListener);

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

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.ItemListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == newAccountLink) {
                LoginAccountPanel.this.newAccountLinkActionPerformed(evt);
            }
            else if (evt.getSource() == accountComboBox) {
                LoginAccountPanel.this.accountComboBoxActionPerformed(evt);
            }
            else if (evt.getSource() == loginButton) {
                LoginAccountPanel.this.loginButtonActionPerformed(evt);
            }
            else if (evt.getSource() == cancelButton) {
                LoginAccountPanel.this.cancelButtonActionPerformed(evt);
            }
        }

        public void itemStateChanged(java.awt.event.ItemEvent evt) {
            if (evt.getSource() == rememberPasswordCheckBox) {
                LoginAccountPanel.this.rememberPasswordCheckBoxItemStateChanged(evt);
            }
            else if (evt.getSource() == autoLoginCheckBox) {
                LoginAccountPanel.this.autoLoginCheckBoxItemStateChanged(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void rememberPasswordCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rememberPasswordCheckBoxItemStateChanged

        if (rememberPasswordCheckBox.isSelected()) {
            autoLoginCheckBox.setEnabled(true);
        } else {
            autoLoginCheckBox.setEnabled(false);
            autoLoginCheckBox.setSelected(false);
        }
    }//GEN-LAST:event_rememberPasswordCheckBoxItemStateChanged

    private void autoLoginCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_autoLoginCheckBoxItemStateChanged

        if ((getSelectedAccount() != null) && getSelectedAccount().isValid()) {
            CollabManager.getDefault().getUserInterface().setAutoLoginAccount(
                getSelectedAccount(), evt.getStateChange() == ItemEvent.SELECTED
            );
        }
    }//GEN-LAST:event_autoLoginCheckBoxItemStateChanged

    private void accountComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_accountComboBoxActionPerformed

        Account account = getSelectedAccount();

        if (account != null) {
            updateSelectedAccountStatus(account);
        }
    }//GEN-LAST:event_accountComboBoxActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed

        // Show the collab explorer
        CollabExplorerPanel.getInstance().showComponent(CollabExplorerPanel.COMPONENT_EXPLORER);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void newAccountLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newAccountLinkActionPerformed

        if (accountPanel.isVisible()) // bring up account managment dialog
         {
            CollabManager.getDefault().getUserInterface().manageAccounts(
                (getSelectedAccount() == null) ? null : getSelectedAccount()
            );
        } else // Prompt user to create a new account
         {
            newAccount = CollabManager.getDefault().getUserInterface().createNewAccount(null, null);
        }
    }//GEN-LAST:event_newAccountLinkActionPerformed

    private void loginButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginButtonActionPerformed
        login(getSelectedAccount());
    }//GEN-LAST:event_loginButtonActionPerformed

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
        private final Image IMAGE = ImageUtilities.loadImage("org/netbeans/modules/collab/core/resources/account_png.gif"); // NOI18N
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
    private javax.swing.JButton newAccountLink;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JCheckBox rememberPasswordCheckBox;
    // End of variables declaration//GEN-END:variables
}
