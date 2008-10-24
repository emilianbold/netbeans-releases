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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.UIResource;
import org.netbeans.modules.php.project.connections.ConfigManager;
import org.netbeans.modules.php.project.connections.ConfigManager.Configuration;
import org.netbeans.modules.php.project.connections.RemoteConnections;
import org.netbeans.modules.php.project.connections.spi.RemoteConfigurationPanel;
import org.openide.util.ChangeSupport;

/**
 * @author Tomas Mysik
 */
public class RemoteConnectionsPanel extends JPanel implements ChangeListener {
    private static final long serialVersionUID = -2863411975980644116L;

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final ConfigListModel configListModel = new ConfigListModel();
    private final RemoteConnections remoteConnections;

    private RemoteConfigurationPanel configurationPanel = new EmptyConfigurationPanel();

    public RemoteConnectionsPanel(RemoteConnections remoteConnections) {
        this.remoteConnections = remoteConnections;

        initComponents();
        errorLabel.setText(" "); // NOI18N

        // init
        configList.setModel(configListModel);
        configList.setCellRenderer(new ConfigListRenderer());

        setEnabledRemoveButton();

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
            switchConfigurationPanel();
        }
    }

    public void selectConfiguration(int index) {
        configList.setSelectedIndex(index);
        switchConfigurationPanel();
    }

    public void selectConfiguration(String configName) {
        configList.setSelectedValue(configListModel.getElement(configName), true);
        switchConfigurationPanel();
    }

    public void setActiveConfig(Configuration cfg) {
        configurationPanel.read(cfg);
    }

    public void updateActiveConfig(Configuration cfg) {
        configurationPanel.store(cfg);
    }

    private void switchConfigurationPanel() {
        configurationPanel.removeChangeListener(this);

        Configuration configuration = (Configuration) configList.getSelectedValue();
        if (configuration != null) {
            configurationPanel = remoteConnections.getConfigurationPanel(configuration);
            assert configurationPanel != null : "Panel must be provided for configuration " + configuration.getName();
            // XXX
            configurationPanel.read(configuration);
        } else {
            configurationPanel = new EmptyConfigurationPanel();
        }

        configurationPanel.addChangeListener(this);

        resetFields();
        configurationPanelHolder.add(configurationPanel.getComponent(), BorderLayout.NORTH);
        configurationPanelHolder.validate();
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
            switchConfigurationPanel();
        }
    }

    public void setEnabledFields(boolean enabled) {
        configurationPanel.setEnabledFields(enabled);
    }

    public void resetFields() {
        configurationPanelHolder.removeAll();
        configurationPanelHolder.validate();
        setError(null);
    }

    public boolean isValidConfiguration() {
        return configurationPanel.isValidConfiguration();
    }

    public String getError() {
        return configurationPanel.getError();
    }

    public void setError(String msg) {
        errorLabel.setText(" "); // NOI18N
        errorLabel.setForeground(UIManager.getColor("nb.errorForeground")); // NOI18N
        errorLabel.setText(msg);
    }

    private void registerListeners() {
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

    void fireChange() {
        changeSupport.fireChange();
        // because of correct coloring of list items (invalid configurations)
        refreshConfigList();
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
        errorLabel = new javax.swing.JLabel();

        setFocusTraversalPolicy(null);

        configList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        configScrollPane.setViewportView(configList);
        configList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.configList.AccessibleContext.accessibleName")); // NOI18N
        configList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.configList.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_Add")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_Remove")); // NOI18N

        configurationPanelHolder.setLayout(new java.awt.BorderLayout());

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
                    .add(configScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, errorLabel)
                    .add(configurationPanelHolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {addButton, removeButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(configurationPanelHolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)
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
        configurationPanelHolder.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.detailsPanel.AccessibleContext.accessibleName")); // NOI18N
        configurationPanelHolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.detailsPanel.AccessibleContext.accessibleDescription")); // NOI18N
        errorLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.errorLabel.AccessibleContext.accessibleName")); // NOI18N
        errorLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.errorLabel.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RemoteConnectionsPanel.class, "RemoteConnectionsPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JList configList;
    private javax.swing.JScrollPane configScrollPane;
    private javax.swing.JPanel configurationPanelHolder;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables

    public void stateChanged(ChangeEvent e) {
        fireChange();
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

        public JPanel getComponent() {
            return PANEL;
        }

        public void setEnabledFields(boolean enabled) {
        }

        public boolean isValidConfiguration() {
            return true;
        }

        public String getError() {
            return null;
        }

        public void read(Configuration configuration) {
        }

        public void store(Configuration configuration) {
        }
    }
}
