/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.connections.ftp;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.project.connections.ConfigManager.Configuration;
import org.netbeans.modules.php.project.connections.common.RemoteValidator;
import org.netbeans.modules.php.project.connections.spi.RemoteConfigurationPanel;
import org.netbeans.modules.php.project.ui.customizer.RunAsValidator;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public final class FtpConfigurationPanel extends JPanel implements RemoteConfigurationPanel {
    private static final long serialVersionUID = 62342689756412730L;

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private String error = null;
    private String warning = null;

    public FtpConfigurationPanel() {
        initComponents();

        setEnabledLoginCredentials();

        // listeners
        registerListeners();
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public boolean isValidConfiguration() {
        String err = RemoteValidator.validateHost(hostTextField.getText());
        if (err != null) {
            setError(err);
            return false;
        }

        err = RemoteValidator.validatePort(portTextField.getText());
        if (err != null) {
            setError(err);
            return false;
        }

        if (!validateUser()) {
            return false;
        }

        if (!validateInitialDirectory()) {
            return false;
        }

        err = RemoteValidator.validateTimeout(timeoutTextField.getText());
        if (err != null) {
            setError(err);
            return false;
        }
        setError(null);
        return true;
    }

    @Override
    public String getError() {
        return error;
    }

    protected void setError(String error) {
        this.error = error;
    }

    @Override
    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    void setEnabledLoginCredentials() {
        setEnabledLoginCredentials(!anonymousCheckBox.isSelected());
    }

    private void setEnabledLoginCredentials(boolean enabled) {
        userTextField.setEnabled(enabled);
        passwordTextField.setEnabled(enabled);
    }

    private void registerListeners() {
        DocumentListener documentListener = new DefaultDocumentListener();
        ActionListener actionListener = new DefaultActionListener();
        hostTextField.getDocument().addDocumentListener(documentListener);
        portTextField.getDocument().addDocumentListener(documentListener);
        userTextField.getDocument().addDocumentListener(documentListener);
        passwordTextField.getDocument().addDocumentListener(documentListener);
        anonymousCheckBox.addActionListener(actionListener);
        initialDirectoryTextField.getDocument().addDocumentListener(documentListener);
        timeoutTextField.getDocument().addDocumentListener(documentListener);
        passiveModeCheckBox.addActionListener(actionListener);

        // internals
        anonymousCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setEnabledLoginCredentials();
            }
        });
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    private boolean validateUser() {
        if (isAnonymousLogin()) {
            return true;
        }
        String err = RemoteValidator.validateUser(userTextField.getText());
        if (err != null) {
            setError(err);
            return false;
        }
        return true;
    }

    private boolean validateInitialDirectory() {
        String err = RunAsValidator.validateUploadDirectory(getInitialDirectory(), false);
        if (err != null) {
            setError(err);
            return false;
        }
        return true;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        hostLabel = new javax.swing.JLabel();
        hostTextField = new javax.swing.JTextField();
        portLabel = new javax.swing.JLabel();
        portTextField = new javax.swing.JTextField();
        userLabel = new javax.swing.JLabel();
        userTextField = new javax.swing.JTextField();
        anonymousCheckBox = new javax.swing.JCheckBox();
        passwordLabel = new javax.swing.JLabel();
        passwordTextField = new javax.swing.JPasswordField();
        initialDirectoryLabel = new javax.swing.JLabel();
        initialDirectoryTextField = new javax.swing.JTextField();
        timeoutLabel = new javax.swing.JLabel();
        timeoutTextField = new javax.swing.JTextField();
        passiveModeCheckBox = new javax.swing.CheckBox();
        passwordLabelInfo = new javax.swing.JLabel();

        setFocusTraversalPolicy(null);

        hostLabel.setLabelFor(hostTextField);
        org.openide.awt.Mnemonics.setLocalizedText(hostLabel, org.openide.util.NbBundle.getMessge(FtpConfigurationPanel.class, "FtpConfigurationPanel.hostLabel.text_1")); // NOI18N

        hostTextField.setMinimumSize(new java.awt.Dimension(150, 19));

        portLabel.setLabelFor(portTextField);
        org.openide.awt.Mnemonics.setLocal        portLabel, org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.portLabel.text_1")); // NOI18N
ortTextField.setMinimumSize(new java.awt.Dimension(20, 19));

        userLabel.setLabelFor(userTextField);
        org.openide.awt.Mnemonics.setLocalizedText(userLabel, org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.userLabel.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(anonymousCheckBox, org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.anonymousCheckBox.text_1")); // NOI18N

        passwordLabel.setLabelFor(passwordTextField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(FtpConfigurationP        , "FtpConfigurationPanel.passwordLabel.text_1")); // NOI18N

        initialDirectoryLabel.setLabelFor(initialDirectoryTextField);
     
ide.awt.Mnemonics.setLocalizedText(initialDirectoryLabel, org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.initialDirectoryLabel.text_1")); // NOI18N

        timeoutLabel.setLabelFor(timeoutTextField);
        org.openide.awt.Mnmonics.setLocalizedText(timeoutLabel, org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.timeoutLabel.text_1")); // NOI18N

 utTextField.setMinimumSizwt.Dimension(20, 19));

        org.openide.awt.Mnemonics.setLocalizedText(passiveModeCheckBox, org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.passiveModeCheckBox.text_1")); // NOI18N

        passwordLabelInfo.setLabelFor(this);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabelInfo, org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.passwordLabelInfo.text_1")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .adostLabel)
                    .addComponent(userLabel)
                    .addComponent(passwordLabel)
        .addComponent(initialDirectoryLabel)
                    .addComponent(timeoutLabel))
                dGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javaxLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.cialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(userTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                            .addComponent(hostTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                            .addComponent(passwordTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                   dComponent(initialDirectorjavax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                            .addComponent(timeoutTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(portLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponenELATED)
                    .addComponent(portTextFielng.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_                        .addonymousCheckBox, javax.swiut.Alignment.LEADING))
                        .addGap(0, 0, 0))
                    .addComponent(passwordLabelInfo)))
            .addComponent(passiveModeCheckBox)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGcreateSequentialGroup()
    .addGroup(layout.createParavax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostLabel)
                    .addComponent(portLabel)
                    .addComponent(portTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javaxLayout.PREFERRED_SIZE)
        .addComponent(hostTextFswing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userLabel)
                    .addComponent(anonymousCheckBox          .addComponent(useravax.swing.GroupLayout.PRE javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGrong.GroupLayout.Alignment.BAS               .addComponeabel)
                    .addComponent(passwordTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.ComponentPlacement.RELATED)
                .addComponent(passwordLabelInfo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(initialDirectoryLabel)
                    .addComponent(initialDirectoryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(timeoutTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(timeoutLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(passiveModeCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        hostLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.hostLabel.AccessibleContext.accessibleName")); // NOI18N
        hostLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.hostLabel.AccessibleContext.accessibleDescription")); // NOI18N
        hostTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.hostTextField.AccessibleContext.accessibleName")); // NOI18N
        hostTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.hostTextField.AccessibleContext.accessibleDescription")); // NOI18N
        portLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.portLabel.AccessibleContext.accessibleName")); // NOI18N
        portLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.portLabel.AccessibleContext.accessibleDescription")); // NOI18N
        portTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.portTextField.AccessibleContext.accessibleName")); // NOI18N
        portTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.portTextField.AccessibleContext.accessibleDescription")); // NOI18N
        userLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.userLabel.AccessibleContext.accessibleName")); // NOI18N
        userLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.userLabel.AccessibleContext.accessibleDescription")); // NOI18N
        userTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.userTextField.AccessibleContext.accessibleName")); // NOI18N
        userTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.userTextField.AccessibleContext.accessibleDescription")); // NOI18N
        anonymousCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.anonymousCheckBox.AccessibleContext.accessibleName")); // NOI18N
        anonymousCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.anonymousCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        passwordLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.passwordLabel.AccessibleContext.accessibleName")); // NOI18N
        passwordLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.passwordLabel.AccessibleContext.accessibleDescription")); // NOI18N
        passwordTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.passwordTextField.AccessibleContext.accessibleName")); // NOI18N
        passwordTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.passwordTextField.AccessibleContext.accessibleDescription")); // NOI18N
        initialDirectoryLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.initialDirectoryLabel.AccessibleContext.accessibleName")); // NOI18N
        initialDirectoryLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.initialDirectoryLabel.AccessibleContext.accessibleDescription")); // NOI18N
        initialDirectoryTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.initialDirectoryTextField.AccessibleContext.accessibleName")); // NOI18N
        initialDirectoryTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.initialDirectoryTextField.AccessibleContext.accessibleDescription")); // NOI18N
        timeoutLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.timeoutLabel.AccessibleContext.accessibleName")); // NOI18N
        timeoutLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.timeoutLabel.AccessibleContext.accessibleDescription")); // NOI18N
        timeoutTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.timeoutTextField.AccessibleContext.accessibleName")); // NOI18N
        timeoutTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.timeoutTextField.AccessibleContext.accessibleDescription")); // NOI18N
        passiveModeCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.passiveModeCheckBox.AccessibleContext.accessibleName")); // NOI18N
        passiveModeCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.passiveModeCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        passwordLabelInfo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.passwordLabelInfo.AccessibleContext.accessibleName")); // NOI18N
        passwordLabelInfo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.passwordLabelInfo.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox anonymousCheckBox;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JTextField hostTextField;
    private javax.swing.JLabel initialDirectoryLabel;
    private javax.swing.JTextField initialDirectoryTextField;
    private javax.swing.JCheckBox passiveModeCheckBox;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JLabel passwordLabelInfo;
    private javax.swing.JPasswordField passwordTextField;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextField portTextField;
    private javax.swing.JLabel timeoutLabel;
    private javax.swing.JTextField timeoutTextField;
    private javax.swing.JLabel userLabel;
    private javax.swing.JTextField userTextField;
    // End of variables declaration//GEN-END:variables

    public String getHostName() {
        return hostTextField.getText();
    }

    public void setHostName(String hostName) {
        hostTextField.setText(hostName);
    }

    public String getPort() {
        return portTextField.getText();
    }

    public void setPort(String port) {
        portTextField.setText(port);
    }

    public String getUserName() {
        return userTextField.getText();
    }

    public void setUserName(String userName) {
        userTextField.setText(userName);
    }

    public String getPassword() {
        return new String(passwordTextField.getPassword());
    }

    public void setPassword(String password) {
        passwordTextField.setText(password);
    }

    public boolean isAnonymousLogin() {
        return anonymousCheckBox.isSelected();
    }

    public void setAnonymousLogin(boolean anonymousLogin) {
        anonymousCheckBox.setSelected(anonymousLogin);
        setEnabledLoginCredentials();
    }

    public String getInitialDirectory() {
        return initialDirectoryTextField.getText();
    }

    public void setInitialDirectory(String initialDirectory) {
        initialDirectoryTextField.setText(initialDirectory);
    }

    public String getTimeout() {
        return timeoutTextField.getText();
    }

    public void setTimeout(String timeout) {
        timeoutTextField.setText(timeout);
    }

    public boolean isPassiveMode() {
        return passiveModeCheckBox.isSelected();
    }

    public void setPassiveMode(boolean passiveMode) {
        passiveModeCheckBox.setSelected(passiveMode);
    }

    @Override
    public void read(Configuration cfg) {
        setHostName(cfg.getValue(FtpConnectionProvider.HOST));
        setPort(cfg.getValue(FtpConnectionProvider.PORT));
        setUserName(cfg.getValue(FtpConnectionProvider.USER));
        setPassword(cfg.getValue(FtpConnectionProvider.PASSWORD, true));
        setAnonymousLogin(Boolean.valueOf(cfg.getValue(FtpConnectionProvider.ANONYMOUS_LOGIN)));
        setInitialDirectory(cfg.getValue(FtpConnectionProvider.INITIAL_DIRECTORY));
        setTimeout(cfg.getValue(FtpConnectionProvider.TIMEOUT));
        setPassiveMode(Boolean.valueOf(cfg.getValue(FtpConnectionProvider.PASSIVE_MODE)));
    }

    @Override
    public void store(Configuration cfg) {
        cfg.putValue(FtpConnectionProvider.HOST, getHostName());
        cfg.putValue(FtpConnectionProvider.PORT, getPort());
        cfg.putValue(FtpConnectionProvider.USER, getUserName());
        cfg.putValue(FtpConnectionProvider.PASSWORD, getPassword(), true);
        cfg.putValue(FtpConnectionProvider.ANONYMOUS_LOGIN, String.valueOf(isAnonymousLogin()));
        cfg.putValue(FtpConnectionProvider.INITIAL_DIRECTORY, RunAsValidator.sanitizeUploadDirectory(getInitialDirectory(), false));
        cfg.putValue(FtpConnectionProvider.TIMEOUT, getTimeout());
        cfg.putValue(FtpConnectionProvider.PASSIVE_MODE, String.valueOf(isPassiveMode()));
    }

    private final class DefaultDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            processUpdate();
        }
        @Override
        public void removeUpdate(DocumentEvent e) {
            processUpdate();
        }
        @Override
        public void changedUpdate(DocumentEvent e) {
            processUpdate();
        }
        private void processUpdate() {
            fireChange();
        }
    }

    private final class DefaultActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            fireChange();
        }
    }
}
