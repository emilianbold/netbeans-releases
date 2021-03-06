/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.sendto.config;

import org.netbeans.modules.dlight.sendto.api.ConfigurationsRegistry;
import org.netbeans.modules.dlight.sendto.api.Configuration;
import org.netbeans.modules.dlight.sendto.api.ConfigurationPanel;
import org.netbeans.modules.dlight.sendto.api.ConfigurationsModel;
import org.netbeans.modules.dlight.sendto.api.Handlers;
import org.netbeans.modules.dlight.sendto.spi.Handler;
import org.netbeans.modules.dlight.sendto.ui.ConfigurationNodes;
import org.netbeans.modules.dlight.sendto.ui.ConfigurationNodes.ConfigurationNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.ListView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author akrasny
 */
public final class OptionsPanel extends javax.swing.JPanel implements ExplorerManager.Provider {

    private final ExplorerManager manager = new ExplorerManager();
    private final SelectionChangeListener listener = new SelectionChangeListener();
    private final ConfigurationChangeListener docListener = new ConfigurationChangeListener();
    private Configuration selectedConfiguration;
    private ConfigurationsModel model;
    private boolean modified;

    /**
     * Creates new form ConfigurationPanel
     */
    public OptionsPanel() {
        initComponents();
        manager.addPropertyChangeListener(listener);

        cbScriptConfigurator.setModel(new DefaultComboBoxModel(Handlers.getHandlers()));

        final ListView h_list = new ListView();
        h_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        handlersListPanel.add(h_list);

        nameFld.getDocument().addDocumentListener(docListener);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        leftPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        handlersListPanel = new javax.swing.JPanel();
        buttonsPanel = new javax.swing.JPanel();
        btnAdd = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        btnCopy = new javax.swing.JButton();
        rightPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        nameFld = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        cbScriptConfigurator = new javax.swing.JComboBox();
        configPanel = new javax.swing.JPanel();

        jLabel4.setLabelFor(handlersListPanel);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.jLabel4.text")); // NOI18N

        handlersListPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        handlersListPanel.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(btnAdd, org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.btnAdd.text")); // NOI18N
        btnAdd.setToolTipText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.btnAdd.toolTipText")); // NOI18N
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnRemove, org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.btnRemove.text")); // NOI18N
        btnRemove.setToolTipText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.btnRemove.toolTipText")); // NOI18N
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnCopy, org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.btnCopy.text")); // NOI18N
        btnCopy.setToolTipText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.btnCopy.toolTipText")); // NOI18N
        btnCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCopyActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buttonsPanelLayout = new javax.swing.GroupLayout(buttonsPanel);
        buttonsPanel.setLayout(buttonsPanelLayout);
        buttonsPanelLayout.setHorizontalGroup(
            buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonsPanelLayout.createSequentialGroup()
                .addComponent(btnAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRemove, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCopy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        buttonsPanelLayout.setVerticalGroup(
            buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btnAdd)
                .addComponent(btnRemove)
                .addComponent(btnCopy))
        );

        buttonsPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnAdd, btnCopy, btnRemove});

        javax.swing.GroupLayout leftPanelLayout = new javax.swing.GroupLayout(leftPanel);
        leftPanel.setLayout(leftPanelLayout);
        leftPanelLayout.setHorizontalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addComponent(jLabel4)
                .addContainerGap(160, Short.MAX_VALUE))
            .addComponent(handlersListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(buttonsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        leftPanelLayout.setVerticalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(handlersListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabel1.setLabelFor(nameFld);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.jLabel1.text")); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(100, 18));

        nameFld.setText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.nameFld.text")); // NOI18N
        nameFld.setToolTipText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.nameFld.toolTipText")); // NOI18N

        jLabel5.setText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.jLabel5.text")); // NOI18N
        jLabel5.setPreferredSize(new java.awt.Dimension(100, 18));

        cbScriptConfigurator.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbScriptConfiguratorActionPerformed(evt);
            }
        });

        configPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout rightPanelLayout = new javax.swing.GroupLayout(rightPanel);
        rightPanel.setLayout(rightPanelLayout);
        rightPanelLayout.setHorizontalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbScriptConfigurator, 0, 162, Short.MAX_VALUE))
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nameFld))
            .addComponent(configPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        rightPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel1, jLabel5});

        rightPanelLayout.setVerticalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameFld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbScriptConfigurator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(configPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(leftPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rightPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(leftPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(rightPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        // TODO: how to make it correctly??

        Node[] selectedNodes = manager.getSelectedNodes();
        Node nodeToSelect = null;
        int i = 0;

        if (selectedNodes.length > 0) {
            Node n = selectedNodes[0];
            Node[] nodes = manager.getRootContext().getChildren().getNodes();

            for (; i < nodes.length; i++) {
                if (nodes[i] == n) {
                    break;
                }
            }

            int idx = i + 1;

            if (idx >= nodes.length) {
                idx = i - 1;
            }

            nodeToSelect = idx < 0 ? null : nodes[idx];
        }

        model.remove(getSelectedConfiguration());

        if (nodeToSelect != null) {
            try {
                manager.setSelectedNodes(new Node[]{nodeToSelect});
            } catch (PropertyVetoException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        modified = true;
    }//GEN-LAST:event_btnRemoveActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        Configuration newConfiguration = new Configuration();
        model.add(newConfiguration);
        selectNode(newConfiguration);
        cbScriptConfigurator.setSelectedIndex(0);
        modified = true;
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCopyActionPerformed
        updateSelectedConfiguration();
        Configuration newConfiguration = getSelectedConfiguration().copy();
        model.add(newConfiguration);
        selectNode(newConfiguration);
        modified = true;
    }//GEN-LAST:event_btnCopyActionPerformed

    private void cbScriptConfiguratorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbScriptConfiguratorActionPerformed
        configPanel.removeAll();
        Handler handler = (Handler) cbScriptConfigurator.getSelectedItem();
        if (handler != null) {
            ConfigurationPanel cfgPanel = handler.getConfigurationPanel();
            cfgPanel.updatePanel(selectedConfiguration);
            configPanel.add(cfgPanel);
        }
        configPanel.revalidate();
        configPanel.repaint();
    }//GEN-LAST:event_cbScriptConfiguratorActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnCopy;
    private javax.swing.JButton btnRemove;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JComboBox cbScriptConfigurator;
    private javax.swing.JPanel configPanel;
    private javax.swing.JPanel handlersListPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JTextField nameFld;
    private javax.swing.JPanel rightPanel;
    // End of variables declaration//GEN-END:variables

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    private Configuration getSelectedConfiguration() {
        Node[] selectedNodes = manager.getSelectedNodes();
        if (selectedNodes.length == 1 && selectedNodes[0] instanceof ConfigurationNode) {
            return ((ConfigurationNode) selectedNodes[0]).getConfiguration();
        } else {
            return null;
        }
    }

    boolean isModified() {
        return modified;
    }

    boolean isDataValid() {
        return true;
    }

    void cancel() {
    }

    void applyChanges() {
        updateSelectedConfiguration();
        for (Configuration configuration : model.getConfigurations()) {
            configuration.applyChanges();
        }
        ConfigurationsRegistry.update(model);
    }

    void update() {
        model = ConfigurationsRegistry.getModelCopy();
        ConfigurationNodes nodes = new ConfigurationNodes(model);
        Configuration sc = selectedConfiguration;
        manager.setRootContext(new AbstractNode(nodes));
        modified = false;

        if (sc == null) {
            if (nodes.getNodesCount() > 0) {
                try {
                    manager.setSelectedNodes(new Node[]{nodes.getNodeAt(0)});
                } catch (PropertyVetoException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                // Send this event to activate/deactivate buttons...
                listener.propertyChange(new PropertyChangeEvent(this, ExplorerManager.PROP_SELECTED_NODES, null, null));
            }
        } else {
            selectNode(sc);
        }
    }

    private void updateSelectedConfiguration() {
        if (selectedConfiguration != null) {
            selectedConfiguration.setName(nameFld.getText());
            Handler handler = (Handler) cbScriptConfigurator.getSelectedItem();
            selectedConfiguration.setHandler(handler);
            handler.getConfigurationPanel().updateConfig(selectedConfiguration);
            modified |= selectedConfiguration.isModified();
        }
    }

    private void enableControls() {
        boolean b = selectedConfiguration != null;
        btnRemove.setEnabled(b);
        btnCopy.setEnabled(b);
        nameFld.setEnabled(b);
        cbScriptConfigurator.setEnabled(b);
    }

    private void selectNode(final Configuration cfg) {
        Children children = manager.getRootContext().getChildren();
        for (Node node : children.getNodes()) {
            if (node instanceof ConfigurationNode) {
                if (((ConfigurationNode) node).getConfiguration().getID().intValue() == cfg.getID().intValue()) {
                    try {
                        manager.setSelectedNodes(new Node[]{node});
                    } catch (PropertyVetoException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    break;
                }
            }
        }
    }

    private final class SelectionChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                updateSelectedConfiguration();
                selectedConfiguration = getSelectedConfiguration();
                setContent(selectedConfiguration);
            }
        }

        private void setContent(Configuration cfg) {
            nameFld.setText(cfg == null ? "" : cfg.getName());

            cbScriptConfigurator.setSelectedItem(cfg == null ? null : cfg.getHandler());

            enableControls();
            modified = false;
        }
    }

    private final class ConfigurationChangeListener implements DocumentListener, ActionListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            update(e);
        }

        private void update(DocumentEvent e) {
            modified = true;
            if (e.getDocument() == nameFld.getDocument() && selectedConfiguration != null) {
                selectedConfiguration.setName(nameFld.getText());
                ((ConfigurationNode) (manager.getSelectedNodes()[0])).updateName();
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            modified = true;
        }
    }
}
