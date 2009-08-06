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
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
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
public class FtpConfigurationPanel extends JPanel implements RemoteConfigurationPanel {
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

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public JComponent getComponent() {
        return this;
    }

    public boolean isValidConfiguration() {
        // remember password is dangerous
        // just warning - do it every time
        String err = RemoteValidator.validateRememberPassword(passwordTextField.getPassword());
        setWarning(err);

        err = RemoteValidator.validateHost(hostTextField.getText());
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

    public String getError() {
        return error;
    }

    protected void setError(String error) {
        this.error = error;
    }

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









        hostLabel = new JLabel();
        hostTextField = new JTextField();
        portLabel = new JLabel();
        portTextField = new JTextField();
        userLabel = new JLabel();
        userTextField = new JTextField();
        anonymousCheckBox = new JCheckBox();
        passwordLabel = new JLabel();
        passwordTextField = new JPasswordField();
        initialDirectoryLabel = new JLabel();
        initialDirectoryTextField = new JTextField();
        timeoutLabel = new JLabel();
        timeoutTextField = new JTextField();
        passiveModeCheckBox = new JCheckBox();
        passwordLabelInfo = new JLabel();
        Mnemonics.setLocalizedText(hostLabel, NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.hostLabel.text_1")); // NOI18N
        hostTextField.setMinimumSize(new Dimension(150, 19));
        Mnemonics.setLocalizedText(portLabel, NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.portLabel.text_1"));
        portTextField.setMinimumSize(new Dimension(20, 19));
        Mnemonics.setLocalizedText(userLabel, NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.userLabel.text_1"));
        Mnemonics.setLocalizedText(anonymousCheckBox, NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.anonymousCheckBox.text_1"));
        Mnemonics.setLocalizedText(passwordLabel, NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.passwordLabel.text_1"));
        Mnemonics.setLocalizedText(initialDirectoryLabel, NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.initialDirectoryLabel.text_1"));
        Mnemonics.setLocalizedText(timeoutLabel, NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.timeoutLabel.text_1"));
        timeoutTextField.setMinimumSize(new Dimension(20, 19));
        Mnemonics.setLocalizedText(passiveModeCheckBox, NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.passiveModeCheckBox.text_1"));
        Mnemonics.setLocalizedText(passwordLabelInfo, NbBundle.getMessage(FtpConfigurationPanel.class, "FtpConfigurationPanel.passwordLabelInfo.text_1"));
        passwordLabelInfo.setEnabled(false);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(0, 0, 0)
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(hostLabel)
                            .add(userLabel)
                            .add(passwordLabel)
                            .add(initialDirectoryLabel)
                            .add(timeoutLabel))
                        .add(25, 25, 25)
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(layout.createParallelGroup(GroupLayout.LEADING)
                                    .add(userTextField, GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                                    .add(hostTextField, GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                                    .add(passwordTextField, GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                                    .add(initialDirectoryTextField, GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                                    .add(timeoutTextField, GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(GroupLayout.TRAILING)
                                    .add(GroupLayout.LEADING, layout.createSequentialGroup()
                                        .add(portLabel)
                                        .addPreferredGap(LayoutStyle.RELATED)
                                        .add(portTextField, GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE))
                                    .add(GroupLayout.LEADING, anonymousCheckBox)))
                            .add(passwordLabelInfo)))
                    .add(passiveModeCheckBox)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(hostLabel)
                    .add(portLabel)
                    .add(portTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(hostTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(userLabel)
                    .add(anonymousCheckBox)
                    .add(userTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(passwordLabel)
                    .add(passwordTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(passwordLabelInfo)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(initialDirectoryLabel)
                    .add(initialDirectoryTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(timeoutTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(timeoutLabel))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(passiveModeCheckBox)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JCheckBox anonymousCheckBox;
    private JLabel hostLabel;
    private JTextField hostTextField;
    private JLabel initialDirectoryLabel;
    private JTextField initialDirectoryTextField;
    private JCheckBox passiveModeCheckBox;
    private JLabel passwordLabel;
    private JLabel passwordLabelInfo;
    private JPasswordField passwordTextField;
    private JLabel portLabel;
    private JTextField portTextField;
    private JLabel timeoutLabel;
    private JTextField timeoutTextField;
    private JLabel userLabel;
    private JTextField userTextField;
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

    public void store(Configuration cfg) {
        cfg.putValue(FtpConnectionProvider.HOST, getHostName());
        cfg.putValue(FtpConnectionProvider.PORT, getPort());
        cfg.putValue(FtpConnectionProvider.USER, getUserName());
        cfg.putValue(FtpConnectionProvider.PASSWORD, getPassword(), true);
        cfg.putValue(FtpConnectionProvider.ANONYMOUS_LOGIN, String.valueOf(isAnonymousLogin()));
        cfg.putValue(FtpConnectionProvider.INITIAL_DIRECTORY, getInitialDirectory());
        cfg.putValue(FtpConnectionProvider.TIMEOUT, getTimeout());
        cfg.putValue(FtpConnectionProvider.PASSIVE_MODE, String.valueOf(isPassiveMode()));
    }

    private final class DefaultDocumentListener implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
            processUpdate();
        }
        public void removeUpdate(DocumentEvent e) {
            processUpdate();
        }
        public void changedUpdate(DocumentEvent e) {
            processUpdate();
        }
        private void processUpdate() {
            fireChange();
        }
    }

    private final class DefaultActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            fireChange();
        }
    }
}
