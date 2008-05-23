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

package org.netbeans.modules.php.project.connections;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.UIResource;
import org.netbeans.modules.php.project.connections.ConfigManager.Configuration;
import org.netbeans.modules.php.project.connections.RemoteConnections.ConnectionType;
import org.openide.util.ChangeSupport;

/**
 * @author Tomas Mysik
 */
class RemoteConnectionsPanel extends JPanel {
    private static final long serialVersionUID = -286345875298064616L;

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final ConfigListModel configListModel = new ConfigListModel();

    RemoteConnectionsPanel() {
        initComponents();
        errorLabel.setText(" "); // NOI18N

        // init
        configList.setModel(configListModel);
        configList.setCellRenderer(new ConfigListRenderer());

        setEnabledLoginCredentials();
        setEnabledRemoveButton();
        for (ConnectionType connectionType : ConnectionType.values()) {
            typeComboBox.addItem(connectionType);
        }

        // initial disabled status
        setEnabledFields(false);

        // listeners
        registerListeners();
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public void addAddButtonActionListener(ActionListener listener) {
        addButton.addActionListener(listener);
    }

    public void removeAddButtonActionListener(ActionListener listener) {
        addButton.removeActionListener(listener);
    }

    public void addRemoveButtonActionListener(ActionListener listener) {
        removeButton.addActionListener(listener);
    }

    public void removeRemoveButtonActionListener(ActionListener listener) {
        removeButton.removeActionListener(listener);
    }

    public void addConfigListListener(ListSelectionListener listener) {
        configList.addListSelectionListener(listener);
    }

    public void removeConfigListListener(ListSelectionListener listener) {
        configList.removeListSelectionListener(listener);
    }

    public void addConnection(ConfigManager.Configuration connection) {
        addConnection(connection, true);
    }

    public void addConnection(ConfigManager.Configuration connection, boolean select) {
        assert configListModel.indexOf(connection) == -1 : "Connection already in the list: " + connection;
        configListModel.addElement(connection);
        if (select) {
            configList.setSelectedValue(connection, true);
        }
    }

    public ConfigManager.Configuration getSelectedConnection() {
        return (Configuration) configList.getSelectedValue();
    }

    public List<Configuration> getConfigurations() {
        return configListModel.getElements();
    }

    public void setConfigurations(List<Configuration> configurations) {
        configListModel.setElements(configurations);
    }

    public void removeConnection(ConfigManager.Configuration connection) {
        assert configListModel.indexOf(connection) != -1 : "Connection not in the list: " + connection;
        // select another config if possible
        int toSelect = -1;
        int idx = configListModel.indexOf(connection);
        if (idx + 1 < configListModel.getSize()) {
            // select the next element
            toSelect = idx;
        } else if (configListModel.getSize() > 1) {
            // select the previous element
            toSelect = idx - 1;
        }
        configListModel.removeElement(connection);
        if (toSelect != -1) {
            configList.setSelectedIndex(toSelect);
        }
    }

    public void setEnabledFields(boolean enabled) {
        typeComboBox.setEnabled(enabled);
        hostTextField.setEnabled(enabled);
        portTextField.setEnabled(enabled);
        userTextField.setEnabled(enabled);
        passwordTextField.setEnabled(enabled);
        rememberPasswordCheckBox.setEnabled(enabled);
        anonymousCheckBox.setEnabled(enabled);
        initialDirectoryTextField.setEnabled(enabled);
        timeoutTextField.setEnabled(enabled);
    }

    public void resetFields() {
        connectionTextField.setText(null);
        hostTextField.setText(null);
        portTextField.setText(null);
        userTextField.setText(null);
        passwordTextField.setText(null);
        rememberPasswordCheckBox.setSelected(false);
        anonymousCheckBox.setSelected(false);
        initialDirectoryTextField.setText(null);
        timeoutTextField.setText(null);
        // reset error as well
        setError(null);
    }

    public void setError(String msg) {
        errorLabel.setText(" "); // NOI18N
        errorLabel.setForeground(UIManager.getColor("nb.errorForeground")); // NOI18N
        errorLabel.setText(msg);
    }

    public void setWarning(String msg) {
        errorLabel.setText(" "); // NOI18N
        errorLabel.setForeground(UIManager.getColor("nb.warningForeground")); // NOI18N
        errorLabel.setText(msg);
    }

    private void registerListeners() {
        DocumentListener documentListener = new DefaultDocumentListener();
        ActionListener actionListener = new DefaultActionListener();
        typeComboBox.addActionListener(actionListener);
        hostTextField.getDocument().addDocumentListener(documentListener);
        portTextField.getDocument().addDocumentListener(documentListener);
        userTextField.getDocument().addDocumentListener(documentListener);
        passwordTextField.getDocument().addDocumentListener(documentListener);
        rememberPasswordCheckBox.addActionListener(actionListener);
        anonymousCheckBox.addActionListener(actionListener);
        initialDirectoryTextField.getDocument().addDocumentListener(documentListener);
        timeoutTextField.getDocument().addDocumentListener(documentListener);

        // internals
        anonymousCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setEnabledLoginCredentials();
            }
        });
        configList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                setEnabledRemoveButton();
            }
        });
    }

    void refreshConfigList() {
        configList.repaint();
    }

    void setEnabledRemoveButton() {
        setEnabledRemoveButton(configList.getSelectedIndex() != -1);
    }

    private void setEnabledRemoveButton(boolean enabled) {
        removeButton.setEnabled(enabled);
    }

    void setEnabledLoginCredentials() {
        setEnabledLoginCredentials(!anonymousCheckBox.isSelected());
    }

    void fireChange() {
        changeSupport.fireChange();
        // because of correct coloring of list items (invalid configurations)
        refreshConfigList();
    }

    private void setEnabledLoginCredentials(boolean enabled) {
        userTextField.setEnabled(enabled);
        passwordTextField.setEnabled(enabled);
        rememberPasswordCheckBox.setEnabled(enabled);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        configScrollPane = new javax.swing.JScrollPane();
        configList = new javax.swing.JList();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        detailsPanel = new javax.swing.JPanel();
        connectionLabel = new javax.swing.JLabel();
        connectionTextField = new javax.swing.JTextField();
        typeLabel = new javax.swing.JLabel();
        typeComboBox = new javax.swing.JComboBox();
        hostLabel = new javax.swing.JLabel();
        hostTextField = new javax.swing.JTextField();
        portLabel = new javax.swing.JLabel();
        portTextField = new javax.swing.JTextField();
        userLabel = new javax.swing.JLabel();
        userTextField = new javax.swing.JTextField();
        anonymousCheckBox = new javax.swing.JCheckBox();
        passwordLabel = new javax.swing.JLabel();
        passwordTextField = new javax.swing.JPasswordField();
        rememberPasswordCheckBox = new javax.swing.JCheckBox();
        initialDirectoryLabel = new javax.swing.JLabel();
        initialDirectoryTextField = new javax.swing.JTextField();
        timeoutLabel = new javax.swing.JLabel();
        timeoutTextField = new javax.swing.JTextField();
        separator = new javax.swing.JSeparator();
        errorLabel = new javax.swing.JLabel();

        configList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        configScrollPane.setViewportView(configList);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_Add")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_Remove")); // NOI18N

        connectionLabel.setLabelFor(connectionTextField);
        org.openide.awt.Mnemonics.setLocalizedText(connectionLabel, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_ConnectionName")); // NOI18N

        connectionTextField.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(typeLabel, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_Type")); // NOI18N

        hostLabel.setLabelFor(hostTextField);
        org.openide.awt.Mnemonics.setLocalizedText(hostLabel, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_HostName")); // NOI18N

        portLabel.setLabelFor(portTextField);
        org.openide.awt.Mnemonics.setLocalizedText(portLabel, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_Port")); // NOI18N

        userLabel.setLabelFor(userTextField);
        org.openide.awt.Mnemonics.setLocalizedText(userLabel, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_UserName")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(anonymousCheckBox, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_AnonymousLogin")); // NOI18N

        passwordLabel.setLabelFor(passwordTextField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_Password")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(rememberPasswordCheckBox, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_RememberPassword")); // NOI18N

        initialDirectoryLabel.setLabelFor(initialDirectoryTextField);
        org.openide.awt.Mnemonics.setLocalizedText(initialDirectoryLabel, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_InitialDirectory")); // NOI18N

        timeoutLabel.setLabelFor(timeoutTextField);
        org.openide.awt.Mnemonics.setLocalizedText(timeoutLabel, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_Timeout")); // NOI18N

        org.jdesktop.layout.GroupLayout detailsPanelLayout = new org.jdesktop.layout.GroupLayout(detailsPanel);
        detailsPanel.setLayout(detailsPanelLayout);
        detailsPanelLayout.setHorizontalGroup(
            detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(detailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(separator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
                    .add(detailsPanelLayout.createSequentialGroup()
                        .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(connectionLabel)
                            .add(typeLabel)
                            .add(hostLabel)
                            .add(userLabel)
                            .add(passwordLabel)
                            .add(initialDirectoryLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(typeComboBox, 0, 302, Short.MAX_VALUE)
                            .add(connectionTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, detailsPanelLayout.createSequentialGroup()
                                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(initialDirectoryTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                                    .add(userTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                                    .add(hostTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                                    .add(passwordTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                        .add(detailsPanelLayout.createSequentialGroup()
                                            .add(portLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                            .add(portTextField))
                                        .add(org.jdesktop.layout.GroupLayout.TRAILING, detailsPanelLayout.createSequentialGroup()
                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                            .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                                .add(rememberPasswordCheckBox)
                                                .add(detailsPanelLayout.createSequentialGroup()
                                                    .add(timeoutLabel)
                                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                    .add(timeoutTextField)))))
                                    .add(detailsPanelLayout.createSequentialGroup()
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(anonymousCheckBox)))))))
                .addContainerGap())
        );
        detailsPanelLayout.setVerticalGroup(
            detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(detailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(connectionLabel)
                    .add(connectionTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(typeLabel)
                    .add(typeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(separator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(hostLabel)
                    .add(portLabel)
                    .add(portTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(hostTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(userLabel)
                    .add(anonymousCheckBox)
                    .add(userTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(passwordLabel)
                    .add(rememberPasswordCheckBox)
                    .add(passwordTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(initialDirectoryLabel)
                    .add(timeoutLabel)
                    .add(initialDirectoryTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(timeoutTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(36, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(errorLabel, "dummy"); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(addButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeButton))
                    .add(configScrollPane, 0, 0, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(detailsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(errorLabel)))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {addButton, removeButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(detailsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(configScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addButton)
                    .add(removeButton)
                    .add(errorLabel))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JCheckBox anonymousCheckBox;
    private javax.swing.JList configList;
    private javax.swing.JScrollPane configScrollPane;
    private javax.swing.JLabel connectionLabel;
    private javax.swing.JTextField connectionTextField;
    private javax.swing.JPanel detailsPanel;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JTextField hostTextField;
    private javax.swing.JLabel initialDirectoryLabel;
    private javax.swing.JTextField initialDirectoryTextField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JPasswordField passwordTextField;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextField portTextField;
    private javax.swing.JCheckBox rememberPasswordCheckBox;
    private javax.swing.JButton removeButton;
    private javax.swing.JSeparator separator;
    private javax.swing.JLabel timeoutLabel;
    private javax.swing.JTextField timeoutTextField;
    private javax.swing.JComboBox typeComboBox;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JLabel userLabel;
    private javax.swing.JTextField userTextField;
    // End of variables declaration//GEN-END:variables

    public String getConnectionName() {
        return connectionTextField.getText();
    }

    public void setConnectionName(String connectionName) {
        connectionTextField.setText(connectionName);
    }

    public ConnectionType getType() {
        return (ConnectionType) typeComboBox.getSelectedItem();
    }

    public void setType(ConnectionType connectionType) {
        typeComboBox.setSelectedItem(connectionType);
    }

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

    public boolean isRememberPassword() {
        return rememberPasswordCheckBox.isSelected();
    }

    public void setRememberPassword(boolean rememberPassword) {
        rememberPasswordCheckBox.setSelected(rememberPassword);
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

    private class DefaultDocumentListener implements DocumentListener {
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

    private class DefaultActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            fireChange();
        }
    }

    public static class ConfigListRenderer extends JLabel implements ListCellRenderer, UIResource {
        private static final long serialVersionUID = 3196531352192214602L;

        public ConfigListRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            assert value instanceof ConfigManager.Configuration;
            setName("ComboBox.listRenderer"); // NOI18N
            Color errorColor = UIManager.getColor("nb.errorForeground"); // NOI18N
            ConfigManager.Configuration cfg = (ConfigManager.Configuration) value;
            setText(cfg.getDisplayName());
            setIcon(null);
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(cfg.isValid() ? list.getSelectionForeground() : errorColor);
            } else {
                setBackground(list.getBackground());
                setForeground(cfg.isValid() ? list.getForeground() : errorColor);
            }
            return this;
        }

        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name; // NOI18N
        }
    }

    public class ConfigListModel extends AbstractListModel {
        private static final long serialVersionUID = -194518992310432557L;

        private final List<Configuration> data = new ArrayList<Configuration>();

        public int getSize() {
            return data.size();
        }

        public Configuration getElementAt(int index) {
            return data.get(index);
        }

        public boolean addElement(Configuration connection) {
            assert connection != null;
            if (!data.add(connection)) {
                return false;
            }
            Collections.sort(data, ConfigManager.getConfigurationComparator());
            int idx = indexOf(connection);
            fireIntervalAdded(this, idx, idx);
            return true;
        }

        public int indexOf(Configuration connection) {
            return data.indexOf(connection);
        }

        public boolean removeElement(Configuration connection) {
            int idx = indexOf(connection);
            if (idx == -1) {
                return false;
            }
            boolean result = data.remove(connection);
            assert result == true;
            fireIntervalRemoved(this, idx, idx);
            return true;
        }

        public List<Configuration> getElements() {
            return Collections.unmodifiableList(data);
        }

        public void setElements(List<Configuration> configurations) {
            int size = data.size();
            data.clear();
            if (size > 0) {
                fireIntervalRemoved(this, 0, size - 1);
            }
            if (configurations.size() > 0) {
                data.addAll(configurations);
                Collections.sort(data, ConfigManager.getConfigurationComparator());
                fireIntervalAdded(this, 0, data.size() - 1);
            }
        }
    }
}
