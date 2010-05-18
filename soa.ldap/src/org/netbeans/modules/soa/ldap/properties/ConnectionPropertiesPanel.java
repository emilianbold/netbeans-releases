/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

/*
 * ConnectionPropertiesPanel.java
 *
 * Created on 13.07.2009, 16:34:19
 */

package org.netbeans.modules.soa.ldap.properties;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 *
 *
 * @author anjeleevich
 */

public class ConnectionPropertiesPanel extends javax.swing.JPanel {
    
// Note
// If you check this checkbox, the password will be stored on the filesystem.
// Otherwise you will be prompted each time the connection is established.

    private ConnectionProperties oldConnectionProperties;

    private DialogDescriptor dialogDescriptor;

    private NumberTextFieldHelper portHelper;
    private NumberTextFieldHelper searchCountLimitHelper;
    private NumberTextFieldHelper browseCountLimitHelper;

    private TextFieldValidationHelper connectionNameHelper;
    private TextFieldValidationHelper hostHelper;
    private TextFieldValidationHelper userHelper;

    private PasswordValidationHelper passwordHelper;

    /** Creates new form ConnectionPropertiesPanel */
    public ConnectionPropertiesPanel(ConnectionProperties 
            oldConnectionProperties)
    {
        initComponents();

        this.oldConnectionProperties = oldConnectionProperties;

        methodComboBox.setModel(new DefaultComboBoxModel(AuthenticationType
                .values()));

        portHelper = new NumberTextFieldHelper(this, portTextField,
                LDAP_PORT, ConnectionPropertiesPanel.class, "DEFAULT_PORT", // NOI18N
                1, 65535);

        searchCountLimitHelper = new NumberTextFieldHelper(this,
                searchCountLimitTextField,
                0, ConnectionPropertiesPanel.class, "DEFAULT_COUNT_LIMIT", // NOI18N
                0, Integer.MAX_VALUE);

        browseCountLimitHelper = new NumberTextFieldHelper(this,
                browseCountLimitTextField,
                0, ConnectionPropertiesPanel.class, "DEFAULT_COUNT_LIMIT", // NOI18N
                0, Integer.MAX_VALUE);

        connectionNameHelper = new TextFieldValidationHelper(this,
                connectionNameTextField);
        
        hostHelper = new TextFieldValidationHelper(this, hostTextField);
        userHelper = new TextFieldValidationHelper(this, userTextField);

        passwordHelper = new PasswordValidationHelper(this, passwordField);

        if (oldConnectionProperties != null) {
            String connectionName = oldConnectionProperties.getConnectionName();
            if (connectionName != null) {
                connectionNameTextField.setText(connectionName);
            }
            
            String host = oldConnectionProperties.getHost();
            if (host != null) {
                hostTextField.setText(host);
            }

            boolean useSSL = oldConnectionProperties.isUseSSL();
            int portNumber = oldConnectionProperties.getPort();

            useSSLCheckBox.setSelected(useSSL);

            if (useSSL) {
                portHelper.setDefaultValue(LDAPS_PORT);
            } else {
                portHelper.setDefaultValue(LDAP_PORT);
            }

            if (portNumber != 0) {
                portHelper.setValue(portNumber);
            }

            // security
            AuthenticationType authenticationType = oldConnectionProperties
                    .getAuthenticationType();
            methodComboBox.setSelectedItem(authenticationType);
            methodComboboxActionPerformed(null);

            if (authenticationType != AuthenticationType.NO_AUTHENTICATION) {
                userTextField.setText(oldConnectionProperties.getUser());

                char[] password = oldConnectionProperties.getPassword();
                String passwordString = (password == null) ? "" // NOI18N
                        : new String(password);

                passwordField.setText(passwordString);
                savePasswordCheckBox.setSelected(oldConnectionProperties
                        .isSavePassword());
            }

            // options
            String baseDN = oldConnectionProperties.getBaseDN();
            if (baseDN != null) {
                baseDNTextField.setText(baseDN);
            }

            int browseCountLimit = oldConnectionProperties
                    .getBrowseCountLimit();
            if (browseCountLimit != 0) {
                browseCountLimitHelper.setValue(browseCountLimit);
            }

            int searchCountLimit = oldConnectionProperties
                    .getSearchCountLimit();
            if (searchCountLimit != 0) {
                searchCountLimitHelper.setValue(searchCountLimit);
            }
        } else {
            methodComboBox.setSelectedItem(AuthenticationType.NO_AUTHENTICATION);
            methodComboboxActionPerformed(null);
        }
    }

    public ConnectionProperties getOldConnectionProperties() {
        return oldConnectionProperties;
    }

    public ConnectionProperties getNewConnectionProperties() {
        ConnectionProperties result = new ConnectionProperties();

        result.setConnectionName(getConnectionName());
        //
        result.setHost(getHost());
        result.setUseSSL(useSSLCheckBox.isSelected());

        result.setPort(portHelper.isDefaultValueActivated() ? 0
                : portHelper.getValue());

        //
        AuthenticationType authenticationType
                = (AuthenticationType) methodComboBox.getSelectedItem();
                
        result.setAuthenticationType(authenticationType);

        if (authenticationType == AuthenticationType.NO_AUTHENTICATION) {
            result.setUser(null);
            result.setPassword(null);
            result.setSavePassword(false);
        } else if (authenticationType == AuthenticationType
                .SIMPLE_AUTHENTICATION)
        {
            result.setUser(getUser());
            result.setPassword(passwordField.getPassword());
            result.setSavePassword(savePasswordCheckBox.isSelected());
        } else {
            throw new IllegalStateException();
        }

        // options
        result.setBaseDN(getBaseDN());
        result.setSearchCountLimit(searchCountLimitHelper.getValue());
        result.setBrowseCountLimit(browseCountLimitHelper.getValue());

        return result;
    }

    public void setDialogDescriptor(DialogDescriptor dialogDescriptor) {
        this.dialogDescriptor = dialogDescriptor;
        updateValidState();
    }

    private String getHost() {
        return hostHelper.getValue();
    }

    private String getBaseDN() {
        String text = baseDNTextField.getText();
        return (text == null) ? "" : text.trim(); // NOI18N
    }

    private String getConnectionName() {
        return connectionNameHelper.getValue();
    }

    private String getUser() {
        return userHelper.getValue();
    }

    public void updateValidState() {
        if (dialogDescriptor != null) {
            boolean valid = portHelper.isValid()
                    && searchCountLimitHelper.isValid()
                    && browseCountLimitHelper.isValid()
                    && connectionNameHelper.isValid()
                    && hostHelper.isValid();

            if (valid && methodComboBox.getSelectedItem() == AuthenticationType
                    .SIMPLE_AUTHENTICATION)
            {
                valid = (getUser().length() > 0);
                if (valid && savePasswordCheckBox.isSelected()) {
                    char[] password = passwordField.getPassword();
                    valid = (password != null) && (password.length > 0);
                }
            }

            boolean canCheckParameters = (methodComboBox.getSelectedItem()
                    == AuthenticationType.NO_AUTHENTICATION)
                    || (passwordHelper.isValid());

            dialogDescriptor.setValid(valid);

            checkParametersButton.setEnabled(valid && canCheckParameters);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        connectionNameLabel = new javax.swing.JLabel();
        connectionNameTextField = new javax.swing.JTextField();
        separator = new javax.swing.JSeparator();
        hostLabel = new javax.swing.JLabel();
        hostTextField = new javax.swing.JTextField();
        portLabel = new javax.swing.JLabel();
        portTextField = new javax.swing.JTextField();
        useSSLCheckBox = new javax.swing.JCheckBox();
        securityPanel = new javax.swing.JPanel();
        methodLabel = new javax.swing.JLabel();
        methodComboBox = new javax.swing.JComboBox();
        userLabel = new javax.swing.JLabel();
        userTextField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        savePasswordCheckBox = new javax.swing.JCheckBox();
        optionsPanel = new javax.swing.JPanel();
        baseDNLabel = new javax.swing.JLabel();
        baseDNTextField = new javax.swing.JTextField();
        searchCountLimitLabel = new javax.swing.JLabel();
        searchCountLimitTextField = new javax.swing.JTextField();
        browseCountLimitLabel = new javax.swing.JLabel();
        browseCountLimitTextField = new javax.swing.JTextField();
        checkParametersButton = new javax.swing.JButton();

        connectionNameLabel.setLabelFor(connectionNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(connectionNameLabel, org.openide.util.NbBundle.getMessage(ConnectionPropertiesPanel.class, "ConnectionPropertiesPanel.connectionNameLabel.text")); // NOI18N

        connectionNameTextField.setText(org.openide.util.NbBundle.getMessage(ConnectionPropertiesPanel.class, "ConnectionPropertiesPanel.connectionNameTextField.text")); // NOI18N

        hostLabel.setLabelFor(hostTextField);
        org.openide.awt.Mnemonics.setLocalizedText(hostLabel, org.openide.util.NbBundle.getMessage(ConnectionPropertiesPanel.class, "ConnectionPropertiesPanel.hostLabel.text")); // NOI18N

        hostTextField.setText(org.openide.util.NbBundle.getMessage(ConnectionPropertiesPanel.class, "ConnectionPropertiesPanel.hostTextField.text")); // NOI18N

        portLabel.setLabelFor(portTextField);
        org.openide.awt.Mnemonics.setLocalizedText(portLabel, org.openide.util.NbBundle.getMessage(ConnectionPropertiesPanel.class, "ConnectionPropertiesPanel.portLabel.text")); // NOI18N

        portTextField.setColumns(10);
        portTextField.setText(org.openide.util.NbBundle.getMessage(ConnectionPropertiesPanel.class, "ConnectionPropertiesPanel.portTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(useSSLCheckBox, org.openide.util.NbBundle.getMessage(ConnectionPropertiesPanel.class, "ConnectionPropertiesPanel.useSSLCheckBox.text")); // NOI18N
        useSSLCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useSSLCheckBoxActionPerformed(evt);
            }
        });

        securityPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ConnectionPropertiesPanel.class, "ConnectionPropertiesPanel.securityPanel.border.title"))); // NOI18N

        methodLabel.setLabelFor(methodComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(methodLabel, org.openide.util.NbBundle.getMessage(ConnectionPropertiesPanel.class, "ConnectionPropertiesPanel.methodLabel.text")); // NOI18N

        methodComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Authentication", "Simple Authentication" }));
        methodComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                methodComboboxActionPerformed(evt);
            }
        });

        userLabel.setLabelFor(userTextField);
        org.openide.awt.Mnemonics.setLocalizedText(userLabel, org.openide.util.NbBundle.getMessage(ConnectionPropertiesPanel.class, "ConnectionPropertiesPanel.userLabel.text")); // NOI18N

        userTextField.setText(org.openide.util.NbBundle.getMessage(ConnectionPropertiesPanel.class, "ConnectionPropertiesPanel.userTextField.text")); // NOI18N

        passwordLabel.setLabelFor(passwordField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(ConnectionPropertiesPanel.class, "ConnectionPropertiesPanel.passwordLabel.text")); // NOI18N

        passwordField.setText(org.openide.util.NbBundle.getMessage(ConnectionPropertiesPanel.class, "ConnectionPropertiesPanel.passwordField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(savePasswordCheckBox, org.openide.util.NbBundle.getMessage(ConnectionPropertiesPanel.class, "ConnectionPropertiesPanel.savePasswordCheckBox.text")); // NOI18N
        savePasswordCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                savePasswordCheckBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout securityPanelLayout = new org.jdesktop.layout.GroupLayout(securityPanel);
        securityPanel.setLayout(securityPanelLayout);
        securityPanelLayout.setHorizontalGroup(
            securityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(securityPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(securityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(userLabel)
                    .add(methodLabel)
                    .add(passwordLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(securityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(savePasswordCheckBox)
                    .add(methodComboBox, 0, 322, Short.MAX_VALUE)
                    .add(userTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                    .add(passwordField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE))
                .addContainerGap())
        );
        securityPanelLayout.setVerticalGroup(
            securityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(securityPanelLayout.createSequentialGroup()
                .add(securityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(methodLabel)
                    .add(methodComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(securityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(userLabel)
                    .add(userTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(securityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(passwordLabel)
                    .add(passwordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(savePasswordCheckBox))
        );

        optionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ConnectionPropertiesPanel.class, "ConnectionPropertiesPanel.optionsPanel.border.title"))); // NOI18N

        baseDNLabel.setLabelFor(baseDNTextField);
        org.openide.awt.Mnemonics.setLocalizedText(baseDNLabel, org.openide.util.NbBundle.getMessage(ConnectionPropertiesPanel.class, "ConnectionPropertiesPanel.baseDNLabel.text")); // NOI18N

        baseDNTextField.setText(org.openide.util.NbBundle.getMessage(ConnectionPropertiesPanel.class, "ConnectionPropertiesPanel.baseDNTextField.text")); // NOI18N

        searchCountLimitLabel.setLabelFor(searchCountLimitTextField);
        org.openide.awt.Mnemonics.setLocalizedText(searchCountLimitLabel, org.openide.util.NbBundle.getMessage(ConnectionPropertiesPanel.class, "ConnectionPropertiesPanel.searchCountLimitLabel.text")); // NOI18N

        searchCountLimitTextField.setColumns(15);
        searchCountLimitTextField.setText(org.openide.util.NbBundle.getMessage(ConnectionPropertiesPanel.class, "ConnectionPropertiesPanel.searchCountLimitTextField.text")); // NOI18N

        browseCountLimitLabel.setLabelFor(browseCountLimitTextField);
        org.openide.awt.Mnemonics.setLocalizedText(browseCountLimitLabel, org.openide.util.NbBundle.getMessage(ConnectionPropertiesPanel.class, "ConnectionPropertiesPanel.browseCountLimitLabel.text")); // NOI18N

        browseCountLimitTextField.setColumns(15);
        browseCountLimitTextField.setText(org.openide.util.NbBundle.getMessage(ConnectionPropertiesPanel.class, "ConnectionPropertiesPanel.browseCountLimitTextField.text")); // NOI18N

        org.jdesktop.layout.GroupLayout optionsPanelLayout = new org.jdesktop.layout.GroupLayout(optionsPanel);
        optionsPanel.setLayout(optionsPanelLayout);
        optionsPanelLayout.setHorizontalGroup(
            optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(optionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(optionsPanelLayout.createSequentialGroup()
                        .add(baseDNLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(baseDNTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE))
                    .add(optionsPanelLayout.createSequentialGroup()
                        .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(searchCountLimitLabel)
                            .add(browseCountLimitLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(browseCountLimitTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(searchCountLimitTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        optionsPanelLayout.setVerticalGroup(
            optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(optionsPanelLayout.createSequentialGroup()
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(baseDNLabel)
                    .add(baseDNTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(searchCountLimitLabel)
                    .add(searchCountLimitTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(browseCountLimitLabel)
                    .add(browseCountLimitTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(checkParametersButton, org.openide.util.NbBundle.getMessage(ConnectionPropertiesPanel.class, "ConnectionPropertiesPanel.checkParametersButton.text")); // NOI18N
        checkParametersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkParametersButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(optionsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(securityPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(separator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(connectionNameLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(connectionNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(hostLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(hostTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(portLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(portTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(useSSLCheckBox))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, checkParametersButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(connectionNameLabel)
                    .add(connectionNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(separator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(hostLabel)
                    .add(hostTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(useSSLCheckBox)
                    .add(portTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(portLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(securityPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(optionsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(checkParametersButton)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void useSSLCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useSSLCheckBoxActionPerformed
        if (useSSLCheckBox.isSelected()) {
            portHelper.setDefaultValue(LDAPS_PORT);
        } else {
            portHelper.setDefaultValue(LDAP_PORT);
        }
}//GEN-LAST:event_useSSLCheckBoxActionPerformed

    private void savePasswordCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_savePasswordCheckBoxActionPerformed
        updateValidState();
}//GEN-LAST:event_savePasswordCheckBoxActionPerformed

    private void checkParametersButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkParametersButtonActionPerformed
        new CheckParametersPanel().check(getNewConnectionProperties());
}//GEN-LAST:event_checkParametersButtonActionPerformed

    private void methodComboboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_methodComboboxActionPerformed
        AuthenticationType authenticationType = (AuthenticationType)
                methodComboBox.getSelectedItem();
        boolean enabled = authenticationType != AuthenticationType
                .NO_AUTHENTICATION;

        userTextField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        savePasswordCheckBox.setEnabled(enabled);

        updateValidState();
    }//GEN-LAST:event_methodComboboxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel baseDNLabel;
    private javax.swing.JTextField baseDNTextField;
    private javax.swing.JLabel browseCountLimitLabel;
    private javax.swing.JTextField browseCountLimitTextField;
    private javax.swing.JButton checkParametersButton;
    private javax.swing.JLabel connectionNameLabel;
    private javax.swing.JTextField connectionNameTextField;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JTextField hostTextField;
    private javax.swing.JComboBox methodComboBox;
    private javax.swing.JLabel methodLabel;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextField portTextField;
    private javax.swing.JCheckBox savePasswordCheckBox;
    private javax.swing.JLabel searchCountLimitLabel;
    private javax.swing.JTextField searchCountLimitTextField;
    private javax.swing.JPanel securityPanel;
    private javax.swing.JSeparator separator;
    private javax.swing.JCheckBox useSSLCheckBox;
    private javax.swing.JLabel userLabel;
    private javax.swing.JTextField userTextField;
    // End of variables declaration//GEN-END:variables


    private static final int LDAP_PORT = 389;
    private static final int LDAPS_PORT = 636;
}
