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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.UIResource;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.connections.ConfigManager;
import org.netbeans.modules.php.project.connections.ConfigManager.Configuration;
import org.netbeans.modules.php.project.connections.RemoteClient;
import org.netbeans.modules.php.project.connections.RemoteConnections;
import org.netbeans.modules.php.project.connections.RemoteException;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.connections.spi.RemoteConfigurationPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 * @author Tomas Mysik
 */
public class RemoteConnectionsPanel extends JPanel implements ChangeListener {
    private static final long serialVersionUID = -2869751187565123236L;

    private static final RequestProcessor TEST_CONNECTION_RP = new RequestProcessor("Test Remote Connection", 1); // NOI18N

    private final ConfigListModel configListModel = new ConfigListModel();
    private final RemoteConnections remoteConnections;
    private final ConfigManager configManager;

    private RemoteConfigurationPanel configurationPanel = new EmptyConfigurationPanel();
    private DialogDescriptor descriptor = null;
    private NotificationLineSupport notificationLineSupport = null;
    private RequestProcessor.Task testConnectionTask = null;

    public RemoteConnectionsPanel(RemoteConnections remoteConnections, ConfigManager configManager) {
        this.remoteConnections = remoteConnections;
        this.configManager = configManager;

        initComponents();

        // init
        configList.setModel(configListModel);
        configList.setCellRenderer(new ConfigListRenderer());

        setEnabledRemoveButton();

        // listeners
        registerListeners();
    }

    public void setConfigurations(List<Configuration> configurations) {
        configListModel.setElements(configurations);
    }

    public boolean open(final RemoteConfiguration remoteConfiguration) {
        testConnectionTask = TEST_CONNECTION_RP.create(new Runnable() {
            public void run() {
                testConnection();
            }
        }, true);
        descriptor = new DialogDescriptor(
                this,
                NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_ManageRemoteConnections"),
                true,
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.OK_OPTION,
                null);
        notificationLineSupport = descriptor.createNotificationLineSupport();
        testConnectionTask.addTaskListener(new TaskListener() {
            public void taskFinished(Task task) {
                enableTestConnection();
            }
        });
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        try {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (getConfigurations().isEmpty()) {
                        // no config available => show add config dialog
                        addConfig();
                    } else {
                        // this would need to implement hashCode() and equals() for RemoteConfiguration.... hmm, probably not needed
                        //assert getConfigurations().contains(remoteConfiguration) : "Unknow remote configration: " + remoteConfiguration;
                        if (remoteConfiguration != null) {
                            // select config
                            selectConfiguration(remoteConfiguration.getName());
                        } else {
                            // select the first one
                            selectConfiguration(0);
                        }
                    }
                }
            });
            dialog.setVisible(true);
        } finally {
            dialog.dispose();
        }
        return descriptor.getValue() == NotifyDescriptor.OK_OPTION;
    }

    private void registerListeners() {
        configList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                setEnabledRemoveButton();
                selectCurrentConfig();
            }
        });
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addConfig();
            }
        });
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeConfig();
            }
        });
    }

    void testConnection() {
        testConnectionButton.setEnabled(false);

        Configuration selectedConfiguration = getSelectedConfiguration();
        assert selectedConfiguration != null;
        RemoteConfiguration remoteConfiguration = remoteConnections.getRemoteConfiguration(selectedConfiguration);
        assert remoteConfiguration != null : "Cannot find remote configuration for config manager configuration " + selectedConfiguration.getName();

        String configName = selectedConfiguration.getDisplayName();
        String progressTitle = NbBundle.getMessage(RemoteConnectionsPanel.class, "MSG_TestingConnection", configName);
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle(progressTitle);
        RemoteClient client = new RemoteClient(remoteConfiguration);
        RemoteException exception = null;
        try {
            progressHandle.start();
            client.connect();
        } catch (RemoteException ex) {
            exception = ex;
        } finally {
            try {
                client.disconnect();
            } catch (RemoteException ex) {
                // ignored
            }
            progressHandle.finish();
        }

        // notify user
        String msg = null;
        int msgType = 0;
        if (exception != null) {
            if (exception.getRemoteServerAnswer() != null) {
                msg = NbBundle.getMessage(RemoteConnectionsPanel.class, "MSG_TestConnectionFailedServerAnswer", exception.getMessage(), exception.getRemoteServerAnswer());
            } else if (exception.getCause() != null) {
                msg = NbBundle.getMessage(RemoteConnectionsPanel.class, "MSG_TestConnectionFailedCause", exception.getMessage(), exception.getCause().getMessage());
            } else {
                msg = exception.getMessage();
            }
            msgType = NotifyDescriptor.ERROR_MESSAGE;
        } else {
            msg = NbBundle.getMessage(RemoteConnectionsPanel.class, "MSG_TestConnectionSucceeded");
            msgType = NotifyDescriptor.INFORMATION_MESSAGE;
        }
        DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                    msg,
                    configName,
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    msgType,
                    new Object[] {NotifyDescriptor.OK_OPTION},
                    NotifyDescriptor.OK_OPTION));
    }

    void enableTestConnection() {
        assert testConnectionTask != null;

        Configuration cfg = getSelectedConfiguration();
        testConnectionButton.setEnabled(testConnectionTask.isFinished() && cfg != null && cfg.isValid());
    }

    private void addConfiguration(ConfigManager.Configuration configuration) {
        addConfiguration(configuration, true);
    }

    private void addConfiguration(ConfigManager.Configuration configuration, boolean select) {
        assert configListModel.indexOf(configuration) == -1 : "Configuration already in the list: " + configuration;
        configListModel.addElement(configuration);
        if (select) {
            configList.setSelectedValue(configuration, true);
            switchConfigurationPanel();
            descriptor.setValid(false);
        }
    }

    private void selectConfiguration(int index) {
        configList.setSelectedIndex(index);
        switchConfigurationPanel();
    }

    private void selectConfiguration(String configName) {
        configList.setSelectedValue(configListModel.getElement(configName), true);
        switchConfigurationPanel();
    }

    private void readActiveConfig(Configuration cfg) {
        configurationPanel.read(cfg);
    }

    private void storeActiveConfig(Configuration cfg) {
        configurationPanel.store(cfg);
    }

    private void switchConfigurationPanel() {
        configurationPanel.removeChangeListener(this);

        String name = null;
        String type = null;
        Configuration configuration = (Configuration) configList.getSelectedValue();
        if (configuration != null) {
            type = remoteConnections.getConfigurationType(configuration);
            name = configuration.getDisplayName();

            configurationPanel = remoteConnections.getConfigurationPanel(configuration);
            assert configurationPanel != null : "Panel must be provided for configuration " + configuration.getName();
            readActiveConfig(configuration);
            configManager.markAsCurrentConfiguration(configuration.getName());
        } else {
            configurationPanel = new EmptyConfigurationPanel();
        }

        configurationPanel.addChangeListener(this);

        resetFields();

        if (configuration != null) {
            assert name != null : "Name must be found for config " + configuration.getDisplayName();
            assert type != null : "Type must be found for config " + configuration.getDisplayName();

            nameTextField.setText(NbBundle.getMessage(RemoteConnectionsPanel.class, "TXT_NameType", name, type));
        }
        configurationPanelHolder.add(configurationPanel.getComponent(), BorderLayout.NORTH);
        configurationPanelHolder.validate();
    }

    private ConfigManager.Configuration getSelectedConfiguration() {
        return (Configuration) configList.getSelectedValue();
    }

    private List<Configuration> getConfigurations() {
        return configListModel.getElements();
    }

    private void removeConfiguration(ConfigManager.Configuration configuration) {
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
            switchConfigurationPanel();
        }
    }

    private void resetFields() {
        nameTextField.setText(" "); // NOI18N

        configurationPanelHolder.removeAll();
        configurationPanelHolder.validate();
        configurationPanelHolder.repaint();
        setError(null);
        setWarning(null);
    }

    private boolean isValidConfiguration() {
        return configurationPanel.isValidConfiguration();
    }

    private String getError() {
        return configurationPanel.getError();
    }

    private String getWarning() {
        return configurationPanel.getWarning();
    }

    private void setError(String msg) {
        assert descriptor != null;
        assert notificationLineSupport != null;

        if (StringUtils.hasText(msg)) {
            notificationLineSupport.setErrorMessage(msg);
            descriptor.setValid(false);
        } else {
            notificationLineSupport.clearMessages();
            descriptor.setValid(true);
        }

        enableTestConnection();
    }

    private void setWarning(String msg) {
        assert descriptor != null;
        assert notificationLineSupport != null;

        if (StringUtils.hasText(msg)) {
            notificationLineSupport.setWarningMessage(msg);
        }
    }

    private void refreshConfigList() {
        configList.repaint();
    }

    void setEnabledRemoveButton() {
        setEnabledRemoveButton(configList.getSelectedIndex() != -1);
    }

    private void setEnabledRemoveButton(boolean enabled) {
        removeButton.setEnabled(enabled);
    }

    void validateActiveConfig() {
        boolean valid = isValidConfiguration();
        String error = getError();
        Configuration cfg = getSelectedConfiguration();
        cfg.setErrorMessage(error);
        setError(error);

        if (!valid) {
            return;
        }

        setWarning(getWarning());

        // check whether all the configs are errorless
        checkAllConfigs();
    }

    private void checkAllConfigs() {
        for (Configuration cfg : getConfigurations()) {
            assert cfg != null;
            if (!cfg.isValid()) {
                setError(NbBundle.getMessage(RemoteConnectionsPanel.class, "MSG_InvalidConfiguration", cfg.getDisplayName()));
                assert descriptor != null;
                descriptor.setValid(false);
                return;
            }
        }
    }

    void addConfig() {
        NewRemoteConnectionPanel panel = new NewRemoteConnectionPanel(configManager);
        if (panel.open()) {
            String config = panel.getConfigName();
            String name = panel.getConnectionName();
            String type = panel.getConnectionType();
            assert config != null;
            assert name != null;
            assert type != null;

            Configuration cfg = configManager.createNew(config, name);
            RemoteConfiguration remoteConfiguration = remoteConnections.createRemoteConfiguration(type, cfg);
            assert remoteConfiguration != null : "No remote configuration created for type: " + type;
            addConfiguration(cfg);
            configManager.markAsCurrentConfiguration(config);
        }
    }

    void removeConfig() {
        Configuration cfg = getSelectedConfiguration();
        assert cfg != null;
        configManager.configurationFor(cfg.getName()).delete();
        removeConfiguration(cfg); // this will change the current selection in the list => selectCurrentConfig() is called
    }

    void selectCurrentConfig() {
        Configuration cfg = getSelectedConfiguration();
        if (cfg != null) {
            switchConfigurationPanel();
            // validate fields only if there's valid config
            validateActiveConfig();
        } else {
            resetFields();
            checkAllConfigs();
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

        configScrollPane = new javax.swing.JScrollPane();
        configList = new javax.swing.JList();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        configurationPanelHolder = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        separator = new javax.swing.JSeparator();
        testConnectionButton = new javax.swing.JButton();

        configList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        configScrollPane.setViewportView(configList);
        configList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.configList.AccessibleContext.accessibleName")); // NOI18N
        configList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.configList.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_Add")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_Remove")); // NOI18N

        configurationPanelHolder.setLayout(new java.awt.BorderLayout());

        nameLabel.setLabelFor(nameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_Name")); // NOI18N

        nameTextField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(testConnectionButton, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_TestConnection")); // NOI18N
        testConnectionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testConnectionButtonActionPerformed(evt);
            }
        });

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
                    .add(configScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, configurationPanelHolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                    .add(separator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(nameLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE))
                    .add(testConnectionButton))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {addButton, removeButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(configScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(nameLabel)
                            .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(separator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(configurationPanelHolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addButton)
                    .add(removeButton)
                    .add(testConnectionButton))
                .addContainerGap())
        );

        configScrollPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.configScrollPane.AccessibleContext.accessibleName")); // NOI18N
        configScrollPane.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.configScrollPane.AccessibleContext.accessibleDescription")); // NOI18N
        addButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.addButton.AccessibleContext.accessibleName")); // NOI18N
        addButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.addButton.AccessibleContext.accessibleDescription")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.removeButton.AccessibleContext.accessibleName")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.removeButton.AccessibleContext.accessibleDescription")); // NOI18N
        configurationPanelHolder.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.detailsPanel.AccessibleContext.accessibleName")); // NOI18N
        configurationPanelHolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.detailsPanel.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void testConnectionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testConnectionButtonActionPerformed
        testConnectionTask.schedule(0);
    }//GEN-LAST:event_testConnectionButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JList configList;
    private javax.swing.JScrollPane configScrollPane;
    private javax.swing.JPanel configurationPanelHolder;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton removeButton;
    private javax.swing.JSeparator separator;
    private javax.swing.JButton testConnectionButton;
    // End of variables declaration//GEN-END:variables

    public void stateChanged(ChangeEvent e) {
        Configuration cfg = getSelectedConfiguration();
        if (cfg != null) {
            // no config selected
            validateActiveConfig();
            storeActiveConfig(cfg);
        }

        // because of correct coloring of list items (invalid configurations)
        refreshConfigList();
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

    private static final class EmptyConfigurationPanel implements RemoteConfigurationPanel {
        private static final JPanel PANEL = new JPanel();

        public void addChangeListener(ChangeListener listener) {
        }

        public void removeChangeListener(ChangeListener listener) {
        }

        public JComponent getComponent() {
            return PANEL;
        }

        public boolean isValidConfiguration() {
            return true;
        }

        public String getError() {
            return null;
        }

        public String getWarning() {
            return null;
        }

        public void read(Configuration configuration) {
        }

        public void store(Configuration configuration) {
        }
    }
}
