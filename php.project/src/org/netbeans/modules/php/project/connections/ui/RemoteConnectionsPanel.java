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

package org.netbeans.modules.php.project.connections.ui;

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
import org.netbeans.modules.php.project.connections.ConfigManager;
import org.netbeans.modules.php.project.connections.ConfigManager.Configuration;
import org.netbeans.modules.php.project.connections.RemoteConnections.ConnectionType;
import org.openide.util.ChangeSupport;

/**
 * @author Tomas Mysik
 */
public class RemoteConnectionsPanel extends JPanel {
    private static final long serialVersionUID = -2863458752980644116L;

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final ConfigListModel configListModel = new ConfigListModel();

    public RemoteConnectionsPanel() {
        initComponents();
        errorLabel.setText(" "); // NOI18N
        warningLabel.setText(" "); // NOI18N

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

    public void addConfiguration(ConfigManager.Configuration configuration) {
        addConfiguration(configuration, true);
    }

    public void addConfiguration(ConfigManager.Configuration configuration, boolean select) {
        assert configListModel.indexOf(configuration) == -1 : "Configuration already in the list: " + configuration;
        configListModel.addElement(configuration);
        if (select) {
            configList.setSelectedValue(configuration, true);
        }
    }

    public void selectConfiguration(int index) {
        configList.setSelectedIndex(index);
    }

    public void selectConfiguration(String configName) {
        configList.setSelectedValue(configListModel.getElement(configName), true);
    }

    public ConfigManager.Configuration getSelectedConfiguration() {
        return (Configuration) configList.getSelectedValue();
    }

    public List<Configuration> getConfigurations() {
        return configListModel.getElements();
    }

    public void setConfigurations(List<Configuration> configurations) {
        configListModel.setElements(configurations);
    }

    public void removeConfiguration(ConfigManager.Configuration configuration) {
        assert configListModel.indexOf(configuration) != -1 : "Configuration not in the list: " + configuration;
        // select another config if possible
        int toSelect = -1;
        int idx = configListModel.indexOf(configuration);
        if (idx + 1 < configListModel.getSize()) {
            // select the next element
            toSelect = idx;
        } else if (configListModel.getSize() > 1) {
            // select the previous element
            toSelect = idx - 1;
        }
        configListModel.removeElement(configuration);
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
        anonymousCheckBox.setEnabled(enabled);
        initialDirectoryTextField.setEnabled(enabled);
        timeoutTextField.setEnabled(enabled);
        passiveModeCheckBox.setEnabled(enabled);
    }

    public void resetFields() {
        connectionTextField.setText(null);
        hostTextField.setText(null);
        portTextField.setText(null);
        userTextField.setText(null);
        passwordTextField.setText(null);
        anonymousCheckBox.setSelected(false);
        initialDirectoryTextField.setText(null);
        timeoutTextField.setText(null);
        passiveModeCheckBox.setSelected(false);
        // reset error and warning as well
        setWarning(null);
        setError(null);
    }

    public void setError(String msg) {
        errorLabel.setText(" "); // NOI18N
        errorLabel.setForeground(UIManager.getColor("nb.errorForeground")); // NOI18N
        errorLabel.setText(msg);
    }

    public void setWarning(String msg) {
        warningLabel.setText(" "); // NOI18N
        warningLabel.setForeground(UIManager.getColor("nb.warningForeground")); // NOI18N
        warningLabel.setText(msg);
    }

    private void registerListeners() {
        DocumentListener documentListener = new DefaultDocumentListener();
        ActionListener actionListener = new DefaultActionListener();
        typeComboBox.addActionListener(actionListener);
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
        initialDirectoryLabel = new javax.swing.JLabel();
        initialDirectoryTextField = new javax.swing.JTextField();
        timeoutLabel = new javax.swing.JLabel();
        timeoutTextField = new javax.swing.JTextField();
        passiveModeCheckBox = new javax.swing.JCheckBox();
        separator = new javax.swing.JSeparator();
        warningLabel = new javax.swing.JLabel();
        passwordLabelInfo = new javax.swing.JLabel();
        errorLabel = new javax.swing.JLabel();

        setFocusTraversalPolicy(null);

        configList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        configScrollPane.setViewportView(configList);
        configList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.configList.AccessibleContext.accessibleName")); // NOI18N
        configList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.configList.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_Add")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_Remove")); // NOI18N

        connectionLabel.setLabelFor(connectionTextField);
        org.openide.awt.Mnemonics.setLocalizedText(connectionLabel, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_ConnectionName")); // NOI18N

        connectionTextField.setEnabled(false);

        typeLabel.setLabelFor(typeComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(typeLabel, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_Type")); // NOI18N

        hostLabel.setLabelFor(hostTextField);
        org.openide.awt.Mnemonics.setLocalizedText(hostLabel, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_HostName")); // NOI18N

        portLabel.setLabelFor(portTextField);
        org.openide.awt.Mnemonics.setLocalizedText(portLabel, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_Port")); // NOI18N

        portTextField.setMinimumSize(new java.awt.Dimension(20, 19));

        userLabel.setLabelFor(userTextField);
        org.openide.awt.Mnemonics.setLocalizedText(userLabel, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_UserName")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(anonymousCheckBox, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_AnonymousLogin")); // NOI18N

        passwordLabel.setLabelFor(passwordTextField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_Password")); // NOI18N

        initialDirectoryLabel.setLabelFor(initialDirectoryTextField);
        org.openide.awt.Mnemonics.setLocalizedText(initialDirectoryLabel, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_InitialDirectory")); // NOI18N

        timeoutLabel.setLabelFor(timeoutTextField);
        org.openide.awt.Mnemonics.setLocalizedText(timeoutLabel, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_Timeout")); // NOI18N

        timeoutTextField.setMinimumSize(new java.awt.Dimension(20, 19));

        org.openide.awt.Mnemonics.setLocalizedText(passiveModeCheckBox, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_PassiveMode")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(warningLabel, "warning"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(passwordLabelInfo, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "TXT_PasswordInfo")); // NOI18N
        passwordLabelInfo.setEnabled(false);

        org.jdesktop.layout.GroupLayout detailsPanelLayout = new org.jdesktop.layout.GroupLayout(detailsPanel);
        detailsPanel.setLayout(detailsPanelLayout);
        detailsPanelLayout.setHorizontalGroup(
            detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(detailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(separator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE)
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
                            .add(typeComboBox, 0, 309, Short.MAX_VALUE)
                            .add(connectionTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, detailsPanelLayout.createSequentialGroup()
                                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(userTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
                                    .add(hostTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
                                    .add(passwordTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
                                    .add(initialDirectoryTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, detailsPanelLayout.createSequentialGroup()
                                        .add(portLabel)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(portTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE))
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, anonymousCheckBox)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, detailsPanelLayout.createSequentialGroup()
                                        .add(timeoutLabel)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                        .add(timeoutTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                            .add(detailsPanelLayout.createSequentialGroup()
                                .add(passwordLabelInfo)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))))
                    .add(warningLabel)
                    .add(passiveModeCheckBox))
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
                    .add(passwordTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(passwordLabelInfo)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(initialDirectoryLabel)
                    .add(timeoutLabel)
                    .add(initialDirectoryTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(timeoutTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(passiveModeCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(warningLabel)
                .addContainerGap())
        );

        connectionLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.connectionLabel.AccessibleContext.accessibleName")); // NOI18N
        connectionLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.connectionLabel.AccessibleContext.accessibleDescription")); // NOI18N
        connectionTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.connectionTextField.AccessibleContext.accessibleName")); // NOI18N
        connectionTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.connectionTextField.AccessibleContext.accessibleDescription")); // NOI18N
        typeLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.typeLabel.AccessibleContext.accessibleName")); // NOI18N
        typeLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.typeLabel.AccessibleContext.accessibleDescription")); // NOI18N
        typeComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.typeComboBox.AccessibleContext.accessibleName")); // NOI18N
        typeComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.typeComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        hostLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.hostLabel.AccessibleContext.accessibleName")); // NOI18N
        hostLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.hostLabel.AccessibleContext.accessibleDescription")); // NOI18N
        hostTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.hostTextField.AccessibleContext.accessibleName")); // NOI18N
        hostTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.hostTextField.AccessibleContext.accessibleDescription")); // NOI18N
        portLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.portLabel.AccessibleContext.accessibleName")); // NOI18N
        portLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.portLabel.AccessibleContext.accessibleDescription")); // NOI18N
        portTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.portTextField.AccessibleContext.accessibleName")); // NOI18N
        portTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.portTextField.AccessibleContext.accessibleDescription")); // NOI18N
        userLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.userLabel.AccessibleContext.accessibleName")); // NOI18N
        userLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.userLabel.AccessibleContext.accessibleDescription")); // NOI18N
        userTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.userTextField.AccessibleContext.accessibleName")); // NOI18N
        userTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.userTextField.AccessibleContext.accessibleDescription")); // NOI18N
        anonymousCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.anonymousCheckBox.AccessibleContext.accessibleName")); // NOI18N
        anonymousCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.anonymousCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        passwordLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.passwordLabel.AccessibleContext.accessibleName")); // NOI18N
        passwordLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.passwordLabel.AccessibleContext.accessibleDescription")); // NOI18N
        passwordTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.passwordTextField.AccessibleContext.accessibleName")); // NOI18N
        passwordTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.passwordTextField.AccessibleContext.accessibleDescription")); // NOI18N
        initialDirectoryLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.initialDirectoryLabel.AccessibleContext.accessibleName")); // NOI18N
        initialDirectoryLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.initialDirectoryLabel.AccessibleContext.accessibleDescription")); // NOI18N
        initialDirectoryTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.initialDirectoryTextField.AccessibleContext.accessibleName")); // NOI18N
        initialDirectoryTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.initialDirectoryTextField.AccessibleContext.accessibleDescription")); // NOI18N
        timeoutLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.timeoutLabel.AccessibleContext.accessibleName")); // NOI18N
        timeoutLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.timeoutLabel.AccessibleContext.accessibleDescription")); // NOI18N
        timeoutTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.timeoutTextField.AccessibleContext.accessibleName")); // NOI18N
        timeoutTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.timeoutTextField.AccessibleContext.accessibleDescription")); // NOI18N
        passiveModeCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.passiveModeCheckBox.AccessibleContext.accessibleName")); // NOI18N
        passiveModeCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.passiveModeCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        separator.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.separator.AccessibleContext.accessibleName")); // NOI18N
        separator.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.separator.AccessibleContext.accessibleDescription")); // NOI18N
        warningLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.warningLabel.AccessibleContext.accessibleName")); // NOI18N
        warningLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.warningLabel.AccessibleContext.accessibleDescription")); // NOI18N
        passwordLabelInfo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.passwordLabelInfo.AccessibleContext.accessibleName")); // NOI18N
        passwordLabelInfo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.passwordLabelInfo.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(errorLabel, "error"); // NOI18N

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
                    .add(configScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addButton)
                    .add(removeButton)
                    .add(errorLabel))
                .addContainerGap())
        );

        configScrollPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.configScrollPane.AccessibleContext.accessibleName")); // NOI18N
        configScrollPane.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.configScrollPane.AccessibleContext.accessibleDescription")); // NOI18N
        addButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.addButton.AccessibleContext.accessibleName")); // NOI18N
        addButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.addButton.AccessibleContext.accessibleDescription")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.removeButton.AccessibleContext.accessibleName")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.removeButton.AccessibleContext.accessibleDescription")); // NOI18N
        detailsPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.detailsPanel.AccessibleContext.accessibleName")); // NOI18N
        detailsPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.detailsPanel.AccessibleContext.accessibleDescription")); // NOI18N
        errorLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.errorLabel.AccessibleContext.accessibleName")); // NOI18N
        errorLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.errorLabel.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.AccessibleContext.accessibleDescription")); // NOI18N
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
    private javax.swing.JCheckBox passiveModeCheckBox;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JLabel passwordLabelInfo;
    private javax.swing.JPasswordField passwordTextField;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextField portTextField;
    private javax.swing.JButton removeButton;
    private javax.swing.JSeparator separator;
    private javax.swing.JLabel timeoutLabel;
    private javax.swing.JTextField timeoutTextField;
    private javax.swing.JComboBox typeComboBox;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JLabel userLabel;
    private javax.swing.JTextField userTextField;
    private javax.swing.JLabel warningLabel;
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
        private static final long serialVersionUID = -1945188556310432557L;

        private final List<Configuration> data = new ArrayList<Configuration>();

        public int getSize() {
            return data.size();
        }

        public Configuration getElementAt(int index) {
            return data.get(index);
        }

        public boolean addElement(Configuration configuration) {
            assert configuration != null;
            if (!data.add(configuration)) {
                return false;
            }
            Collections.sort(data, ConfigManager.getConfigurationComparator());
            int idx = indexOf(configuration);
            fireIntervalAdded(this, idx, idx);
            return true;
        }

        public int indexOf(Configuration configuration) {
            return data.indexOf(configuration);
        }

        public boolean removeElement(Configuration configuration) {
            int idx = indexOf(configuration);
            if (idx == -1) {
                return false;
            }
            boolean result = data.remove(configuration);
            assert result;
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

        public Configuration getElement(String configName) {
            assert configName != null;
            for (Configuration configuration : data) {
                if (configName.equals(configuration.getName())) {
                    return configuration;
                }
            }
            return null;
        }
    }
}
