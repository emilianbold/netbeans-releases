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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.NbBundle;

/**
 *
 *
 */
public class AccountServerPanel extends JPanel {

    private WizardPanelBase wizardPanel;

    /**
     *
     *
     */
    public AccountServerPanel (WizardPanelBase wizardPanel) {
        this.wizardPanel = wizardPanel;
        setName(NbBundle.getMessage(AccountServerPanel.class, "LBL_AccountServerPanel_Name"));

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
    void readSettings(Object object) {
        AccountWizardSettings settings = AccountWizardSettings.narrow(object);
        Account account = settings.getAccount();
        serverField.setEditable(true);

        String serverURL = account.getServer();

        if ((serverURL != null) && !serverURL.trim().equals("")) {
            serverField.setText(serverURL);
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
    void storeSettings(Object object) {
        storeSettings(AccountWizardSettings.narrow(object).getAccount());
    }

    /**
     *
     *
     */
    void storeSettings(Account account) {
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
            wizardPanel.setValid(false);

            return;
        }

        if (!isProxySelected()) {
            wizardPanel.setValid(true);
        } else {
            boolean hasServerText = proxyServer.getText().trim().length() > 0;

            // If either auth field has text, then the other must also
            boolean userNameHasText = proxyUserName.getText().trim().length() > 0;
            boolean neitherHaveText = (proxyUserName.getText().trim().length() == 0) &&
                (proxyPassword.getText().trim().length() == 0);

            wizardPanel.setValid(hasServerText && (neitherHaveText || userNameHasText));
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

    private void initAccessibility() {

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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
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

        setBorder(javax.swing.BorderFactory.createCompoundBorder(null, javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        setLayout(new java.awt.GridBagLayout());

        jPanel2.setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(displayNameLbl, bundle.getString("LBL_AccountServerPanel_Message")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        jPanel2.add(displayNameLbl, gridBagConstraints);

        serverLbl.setLabelFor(serverField);
        org.openide.awt.Mnemonics.setLocalizedText(serverLbl, bundle.getString("LBL_AccountServerPanel_ServerURL")); // NOI18N
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

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, bundle.getString("MSG_AccountServerPanel_Example")); // NOI18N
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

        proxyTypePanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("LBL_AccountProxyPanel_ProxyTypePanel")), javax.swing.BorderFactory.createEmptyBorder(0, 5, 5, 5))); // NOI18N
        proxyTypePanel.setLayout(new java.awt.GridBagLayout());

        buttonGroup.add(noProxyButton);
        noProxyButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(noProxyButton, bundle.getString("LBL_AccountProxyPanel_NoProxy")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        proxyTypePanel.add(noProxyButton, gridBagConstraints);

        buttonGroup.add(httpProxyButton);
        org.openide.awt.Mnemonics.setLocalizedText(httpProxyButton, bundle.getString("LBL_AccountProxyPanel_HttpProxy")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        proxyTypePanel.add(httpProxyButton, gridBagConstraints);

        buttonGroup.add(socksProxyButton);
        org.openide.awt.Mnemonics.setLocalizedText(socksProxyButton, bundle.getString("LBL_AccountProxyPanel_SocksProxy")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        proxyTypePanel.add(socksProxyButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        add(proxyTypePanel, gridBagConstraints);

        proxyServerPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("LBL_AccountProxyPanel_ProxyServerPanel")), javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5))); // NOI18N
        proxyServerPanel.setLayout(new java.awt.GridBagLayout());

        proxyServerLabel.setLabelFor(proxyServer);
        org.openide.awt.Mnemonics.setLocalizedText(proxyServerLabel, bundle.getString("LBL_AccountProxyPanel_ProxyServer")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        proxyServerPanel.add(proxyServerLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 0);
        proxyServerPanel.add(proxyServer, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(proxyServerExample, bundle.getString("LBL_AccountProxyPanel_ProxyServer_HttpsExample")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 0);
        proxyServerPanel.add(proxyServerExample, gridBagConstraints);

        proxyUserNameLabel.setLabelFor(proxyUserName);
        org.openide.awt.Mnemonics.setLocalizedText(proxyUserNameLabel, bundle.getString("LBL_AccountProxyPanel_ProxyAuthPanel_ProxyUserName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        proxyServerPanel.add(proxyUserNameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 0);
        proxyServerPanel.add(proxyUserName, gridBagConstraints);

        proxyPasswordLabel.setLabelFor(proxyPassword);
        org.openide.awt.Mnemonics.setLocalizedText(proxyPasswordLabel, bundle.getString("LBL_AccountProxyPanel_ProxyAuthPanel_ProxyPassword")); // NOI18N
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
    }// </editor-fold>//GEN-END:initComponents

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
    // End of variables declaration//GEN-END:variables
}
