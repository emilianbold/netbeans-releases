/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.launchers.actions.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.launchers.actions.ui.LaunchersConfig.LauncherConfig;
import org.netbeans.modules.cnd.makeproject.runprofiles.ui.ListTableModel;
import org.netbeans.modules.cnd.makeproject.ui.customizer.MakeContext;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.ListView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Alexander Simon
 */
public class LaunchersPanel extends JPanel implements ExplorerManager.Provider, MakeContext.Savable {

    private final ExplorerManager manager = new ExplorerManager();
    private final SelectionChangeListener listener = new SelectionChangeListener();
    private final ArrayList<LauncherConfig> launchers = new ArrayList<>();
    private final LaunchersNodes nodes;
    private LauncherConfig selectedConfiguration;
    private final LaunchersConfig instance;
    private final ListTableModel envVarModel;
    private final JTable envVarTable;
    final ListView h_list;
    private boolean modified = false;
    private volatile boolean resetFields = true;

    /**
     * Creates new form LaunchersPanel
     */
    public LaunchersPanel(Project project) {
        setPreferredSize(new Dimension(640, 450));
        setMinimumSize(new Dimension(400, 200));
        initComponents();
        
        envVarModel = new ListTableModel(NbBundle.getMessage(LaunchersPanel.class, "EnvName"),
                                 NbBundle.getMessage(LaunchersPanel.class, "EnvValue"));
	envVarTable = new JTable(envVarModel);
	envVarModel.setTable(envVarTable);
	envVarScrollPane.setViewportView(envVarTable);
        envVarTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                validateEnvButtons(e);
            }
        });

        manager.addPropertyChangeListener(listener);
        instance = new LaunchersConfig(project);
        instance.load();
        launchers.addAll(instance.getLaunchers());
        nodes = new LaunchersNodes(launchers);
        h_list = new ListView();
        h_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        LauncersListPanel.add(h_list, BorderLayout.CENTER);
        update();
        final ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!resetFields) {
                    updateListViewItem();
                }
            }
        };
        publicCheckBox.addActionListener(actionListener);
        hideCheckBox.addActionListener(actionListener);
        final DocumentListener documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!resetFields) {
                    updateListViewItem();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!resetFields) {
                    updateListViewItem();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!resetFields) {
                    updateListViewItem();
                }
            }
        };
        launcherNameTextField.getDocument().addDocumentListener(documentListener);
        runTextField.getDocument().addDocumentListener(documentListener);
    }


    @Override
    public void save() {
        updateSelectedConfiguration();
        if (modified) {
            instance.save(launchers);
        }
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    final void update() {
        LauncherConfig sc = selectedConfiguration;
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

    private void selectNode(final LauncherConfig cfg) {
        Children children = manager.getRootContext().getChildren();
        for (Node node : children.getNodes()) {
            if (node instanceof LauncherNode) {
                if (((LauncherNode) node).getConfiguration() == cfg) {
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

    private LauncherConfig getSelectedConfiguration() {
        Node[] selectedNodes = manager.getSelectedNodes();
        if (selectedNodes.length == 1 && selectedNodes[0] instanceof LauncherNode) {
            return ((LauncherNode) selectedNodes[0]).getConfiguration();
        } else {
            return null;
        }
    }

    private String getString(String s) {
        return s.trim().replace('\n', ' ').replace('\t', ' ');
    }

    private void updateSelectedConfiguration() {
        if (selectedConfiguration != null) {
            selectedConfiguration.setName(launcherNameTextField.getText().trim());
            selectedConfiguration.setCommand(getString(runTextField.getText()));
            selectedConfiguration.setBuildCommand(buildTextField.getText().trim());
            selectedConfiguration.setRunDir(runDirTextField.getText().trim());
            selectedConfiguration.setSymbolFiles(symbolsTextField.getText().trim());
            selectedConfiguration.setPublic(publicCheckBox.isSelected());
            selectedConfiguration.setHide(hideCheckBox.isSelected());
            if (envVarTable.isEditing()) {
                TableCellEditor cellEditor = envVarTable.getCellEditor();
                if (cellEditor != null) {
                    cellEditor.stopCellEditing();
                }
            }

            HashMap<String, String> newContent = new HashMap<>();
            for(int i = 0; i < envVarModel.getRowCount(); i++) {
                String key = (String) envVarModel.getValueAt(i, 0);
                String value = (String) envVarModel.getValueAt(i, 1);
                if (key == null || value == null) {
                    continue;
                }
                key = key.trim();
                if (!key.isEmpty()) {
                    newContent.put(key, value.trim());
                    
                }
            }
            if ( selectedConfiguration.getEnv().size() != newContent.size()) {
               modified = true;
            } else {
                modified |= !selectedConfiguration.getEnv().equals(newContent);
            }
            selectedConfiguration.getEnv().clear();
            selectedConfiguration.getEnv().putAll(newContent);
            modified |= selectedConfiguration.isModified();
            updateListViewItem();
        }
    }

    private void updateListViewItem() {
        if (selectedConfiguration != null) {
            Node[] selectedNodes = manager.getSelectedNodes();
            if (selectedNodes.length == 1 && selectedNodes[0] instanceof LauncherNode) {
                LauncherNode node = ((LauncherNode) selectedNodes[0]);
                if (selectedConfiguration == node.getConfiguration()) {
                    node.updateNode(launcherNameTextField.getText().trim(), getString(runTextField.getText()),
                            publicCheckBox.isSelected(), hideCheckBox.isSelected());
                }
            }
        }
    }
    private void enableControls() {
        boolean b = selectedConfiguration != null;
        boolean c = true;
        boolean top = true;
        boolean bottom = true;
        if (b) {
            c = selectedConfiguration.getID() >= 0;
            int index = launchers.indexOf(selectedConfiguration);
            if (index > 1) {
                bottom = index == launchers.size()-1;
                top = index == 2;
            }
        }
        upButton.setEnabled(b && !top);
        downButton.setEnabled(b && !bottom);
        removeButton.setEnabled(b && c);
        copyButton.setEnabled(b && c);
        launcherNameTextField.setEnabled(b && c);
        runTextField.setEnabled(b && c);
        buildTextField.setEnabled(b && c);
        publicCheckBox.setEnabled(b && c);
        hideCheckBox.setEnabled(b && c);
        runDirTextField.setEnabled(b);
        symbolsTextField.setEnabled(b);
        addEnvButton.setEnabled(b);
        removeEnvButton.setEnabled(b);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        leftPanel = new javax.swing.JPanel();
        launchersListLabel = new javax.swing.JLabel();
        addButton = new javax.swing.JButton();
        LauncersListPanel = new javax.swing.JPanel();
        removeButton = new javax.swing.JButton();
        copyButton = new javax.swing.JButton();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();
        rightPanel = new javax.swing.JPanel();
        launcherNameLabel = new javax.swing.JLabel();
        launcherNameTextField = new javax.swing.JTextField();
        runLabel = new javax.swing.JLabel();
        buildLabel = new javax.swing.JLabel();
        buildTextField = new javax.swing.JTextField();
        runDirLabel = new javax.swing.JLabel();
        runDirTextField = new javax.swing.JTextField();
        symbolLabel = new javax.swing.JLabel();
        symbolsTextField = new javax.swing.JTextField();
        envLabel = new javax.swing.JLabel();
        envVarScrollPane = new javax.swing.JScrollPane();
        addEnvButton = new javax.swing.JButton();
        removeEnvButton = new javax.swing.JButton();
        publicCheckBox = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        runTextField = new javax.swing.JTextArea();
        hideCheckBox = new javax.swing.JCheckBox();

        launchersListLabel.setLabelFor(LauncersListPanel);
        org.openide.awt.Mnemonics.setLocalizedText(launchersListLabel, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.launchersListLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.addButton.text")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        LauncersListPanel.setMaximumSize(new java.awt.Dimension(300, 2147483647));
        LauncersListPanel.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.removeButton.text")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(copyButton, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.copyButton.text")); // NOI18N
        copyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(upButton, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.upButton.text")); // NOI18N
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(downButton, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.downButton.text")); // NOI18N
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout leftPanelLayout = new javax.swing.GroupLayout(leftPanel);
        leftPanel.setLayout(leftPanelLayout);
        leftPanelLayout.setHorizontalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(leftPanelLayout.createSequentialGroup()
                        .addComponent(launchersListLabel)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(leftPanelLayout.createSequentialGroup()
                        .addGroup(leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(leftPanelLayout.createSequentialGroup()
                                .addComponent(upButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(downButton))
                            .addGroup(leftPanelLayout.createSequentialGroup()
                                .addComponent(addButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removeButton)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(copyButton)
                        .addGap(0, 39, Short.MAX_VALUE))
                    .addComponent(LauncersListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        leftPanelLayout.setVerticalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(launchersListLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(LauncersListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addButton)
                    .addComponent(removeButton)
                    .addComponent(copyButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(upButton)
                    .addComponent(downButton)))
        );

        launcherNameLabel.setLabelFor(launcherNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(launcherNameLabel, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.launcherNameLabel.text")); // NOI18N
        launcherNameLabel.setToolTipText(org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LauncherDisplayNameToolTip")); // NOI18N

        launcherNameTextField.setMaximumSize(new java.awt.Dimension(300, 2147483647));

        runLabel.setLabelFor(runTextField);
        org.openide.awt.Mnemonics.setLocalizedText(runLabel, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.runLabel.text")); // NOI18N

        buildLabel.setLabelFor(buildTextField);
        org.openide.awt.Mnemonics.setLocalizedText(buildLabel, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.buildLabel.text")); // NOI18N
        buildLabel.setToolTipText(org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "BuildCommandToolTip")); // NOI18N

        buildTextField.setMaximumSize(new java.awt.Dimension(300, 2147483647));

        runDirLabel.setLabelFor(runDirTextField);
        org.openide.awt.Mnemonics.setLocalizedText(runDirLabel, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.runDirLabel.text")); // NOI18N
        runDirLabel.setToolTipText(org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "RunDirectoryToolTip")); // NOI18N

        runDirTextField.setMaximumSize(new java.awt.Dimension(300, 2147483647));

        symbolLabel.setLabelFor(symbolsTextField);
        org.openide.awt.Mnemonics.setLocalizedText(symbolLabel, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.symbolLabel.text")); // NOI18N
        symbolLabel.setToolTipText(org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "SymbolFilesToolTip")); // NOI18N

        symbolsTextField.setMaximumSize(new java.awt.Dimension(300, 2147483647));

        envLabel.setLabelFor(envVarScrollPane);
        org.openide.awt.Mnemonics.setLocalizedText(envLabel, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.envLabel.text")); // NOI18N
        envLabel.setToolTipText(org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "EnvToolTip")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addEnvButton, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.addEnvButton.text")); // NOI18N
        addEnvButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addEnvButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeEnvButton, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.removeEnvButton.text")); // NOI18N
        removeEnvButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeEnvButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(publicCheckBox, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.publicCheckBox.text")); // NOI18N
        publicCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "PublicToolTip")); // NOI18N

        runTextField.setColumns(20);
        runTextField.setLineWrap(true);
        runTextField.setRows(5);
        runTextField.setWrapStyleWord(true);
        runTextField.setMinimumSize(new java.awt.Dimension(360, 17));
        jScrollPane1.setViewportView(runTextField);

        org.openide.awt.Mnemonics.setLocalizedText(hideCheckBox, org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "LaunchersPanel.hideCheckBox.text")); // NOI18N
        hideCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(LaunchersPanel.class, "HideTooltip")); // NOI18N

        javax.swing.GroupLayout rightPanelLayout = new javax.swing.GroupLayout(rightPanel);
        rightPanel.setLayout(rightPanelLayout);
        rightPanelLayout.setHorizontalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 603, Short.MAX_VALUE)
                    .addComponent(envVarScrollPane, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(runLabel)
                            .addComponent(buildLabel)
                            .addComponent(runDirLabel)
                            .addComponent(symbolLabel)
                            .addComponent(launcherNameLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(launcherNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(symbolsTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(runDirTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buildTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addComponent(publicCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(addEnvButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeEnvButton))
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(hideCheckBox)
                            .addComponent(envLabel))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        rightPanelLayout.setVerticalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(launcherNameLabel)
                    .addComponent(launcherNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(runLabel)
                .addGap(3, 3, 3)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buildLabel)
                    .addComponent(buildTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runDirLabel)
                    .addComponent(runDirTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(symbolLabel)
                    .addComponent(symbolsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(envLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(envVarScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addEnvButton)
                    .addComponent(removeEnvButton)
                    .addComponent(publicCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hideCheckBox)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(leftPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rightPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(rightPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(leftPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        updateSelectedConfiguration();
        int max = 1000;
        for(LauncherConfig cfg : launchers) {
            if (cfg.getID() >= max) {
                max = (cfg.getID() + 1000) / 1000;
                max = max *1000;
            }
        }
        LauncherConfig newConfiguration = new LauncherConfig(max, true);
        launchers.add(newConfiguration);
        nodes.restKeys();
        selectNode(newConfiguration);
        //cbScriptConfigurator.setSelectedIndex(0);
        modified = true;
    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
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

        launchers.remove(getSelectedConfiguration());
        nodes.restKeys();

        if (nodeToSelect != null) {
            try {
                manager.setSelectedNodes(new Node[]{nodeToSelect});
            } catch (PropertyVetoException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        modified = true;
    }//GEN-LAST:event_removeButtonActionPerformed

    private void copyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyButtonActionPerformed
        updateSelectedConfiguration();
        int max = 1000;
        for(LauncherConfig cfg : launchers) {
            if (cfg.getID() >= max) {
                max = (cfg.getID() + 1000) / 1000;
                max = max *1000;
            }
        }
        LauncherConfig newConfiguration = getSelectedConfiguration().copy(max);
        launchers.add(newConfiguration);
        nodes.restKeys();
        selectNode(newConfiguration);
        modified = true;
    }//GEN-LAST:event_copyButtonActionPerformed

    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        Node[] selectedNodes = manager.getSelectedNodes();
        if (selectedNodes.length > 0) {
            updateSelectedConfiguration();
            final LauncherConfig current = getSelectedConfiguration();
            int curIndex = launchers.indexOf(current);
            LauncherConfig prev = launchers.get(curIndex-1);
            if (curIndex-2 > 0) {
                 LauncherConfig prevPrev = launchers.get(curIndex-2);
                 int prevIndex = prev.getID();
                 int prevPrevIndex = prevPrev.getID();
                 if (prevPrevIndex < prevIndex) {
                     int candidate = (prevPrevIndex + prevIndex) / 2;
                     if (prevPrevIndex < candidate && candidate < prevIndex) {
                         // set middle index to avoid full renumeration
                         current.setID(candidate);
                     }
                 }
            }
            launchers.set(curIndex, prev);
            launchers.set(curIndex-1, current);
            nodes.restKeys();
            modified = true;
            try {
                manager.setSelectedNodes(new Node[0]);
            } catch (PropertyVetoException ex) {
                Exceptions.printStackTrace(ex);
            }
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    Node[] nodes = manager.getRootContext().getChildren().getNodes();
                    int i = 0;
                    Node nodeToSelect = null;
                    for (; i < nodes.length; i++) {
                        if (nodes[i].getLookup().lookup(LauncherConfig.class) == current) {
                            nodeToSelect = nodes[i];
                            break;
                        }
                    }
                    if (nodeToSelect != null){
                        try {
                            manager.setSelectedNodes(new Node[]{nodeToSelect});
                        } catch (PropertyVetoException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            });
        }
    }//GEN-LAST:event_upButtonActionPerformed

    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        Node[] selectedNodes = manager.getSelectedNodes();
        if (selectedNodes.length > 0) {
            updateSelectedConfiguration();
            final LauncherConfig current = getSelectedConfiguration();
            int curIndex = launchers.indexOf(current);
            LauncherConfig next = launchers.get(curIndex+1);
            if (curIndex+2 < launchers.size()) {
                LauncherConfig nextNext = launchers.get(curIndex+2);
                int nextIndex = next.getID();
                int nextNextIndex = nextNext.getID();
                if (nextIndex < nextNextIndex) {
                    int catndidate = (nextIndex + nextNextIndex) / 2;
                    if (nextIndex < catndidate && catndidate  < nextNextIndex) {
                        current.setID(catndidate);
                    }
                }
            } else {
                int candidate = (next.getID()+1000) / 1000;

                current.setID(candidate * 1000);
            }
            launchers.set(curIndex, next);
            launchers.set(curIndex+1, current);
            nodes.restKeys();
            modified = true;
            try {
                manager.setSelectedNodes(new Node[0]);
            } catch (PropertyVetoException ex) {
                Exceptions.printStackTrace(ex);
            }
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    Node[] nodes = manager.getRootContext().getChildren().getNodes();
                    int i = 0;
                    Node nodeToSelect = null;
                    for (; i < nodes.length; i++) {
                        if (nodes[i].getLookup().lookup(LauncherConfig.class) == current) {
                            nodeToSelect = nodes[i];
                            break;
                        }
                    }
                    if (nodeToSelect != null){
                        try {
                            manager.setSelectedNodes(new Node[]{nodeToSelect});
                        } catch (PropertyVetoException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            });
        }
    }//GEN-LAST:event_downButtonActionPerformed

    private void addEnvButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addEnvButtonActionPerformed
        envVarModel.addRow();
    }//GEN-LAST:event_addEnvButtonActionPerformed

    private void removeEnvButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeEnvButtonActionPerformed
        int selectedRow = envVarTable.getSelectedRow();
        if (selectedRow >= 0) {
            envVarModel.removeRows(new int[]{selectedRow});
        }
    }//GEN-LAST:event_removeEnvButtonActionPerformed

    private void validateEnvButtons(ListSelectionEvent e) {
	int[] selRows = envVarTable.getSelectedRows();
        removeButton.setEnabled(envVarModel.getRowCount() > 0 && selRows != null && selRows.length > 0);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel LauncersListPanel;
    private javax.swing.JButton addButton;
    private javax.swing.JButton addEnvButton;
    private javax.swing.JLabel buildLabel;
    private javax.swing.JTextField buildTextField;
    private javax.swing.JButton copyButton;
    private javax.swing.JButton downButton;
    private javax.swing.JLabel envLabel;
    private javax.swing.JScrollPane envVarScrollPane;
    private javax.swing.JCheckBox hideCheckBox;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel launcherNameLabel;
    private javax.swing.JTextField launcherNameTextField;
    private javax.swing.JLabel launchersListLabel;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JCheckBox publicCheckBox;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton removeEnvButton;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JLabel runDirLabel;
    private javax.swing.JTextField runDirTextField;
    private javax.swing.JLabel runLabel;
    private javax.swing.JTextArea runTextField;
    private javax.swing.JLabel symbolLabel;
    private javax.swing.JTextField symbolsTextField;
    private javax.swing.JButton upButton;
    // End of variables declaration//GEN-END:variables

    
    private final class SelectionChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                updateSelectedConfiguration();
                selectedConfiguration = getSelectedConfiguration();
                resetFields = true;
                setContent(selectedConfiguration);
                resetFields = false;
                enableControls();
            }
        }

        private void setContent(LauncherConfig cfg) {
            launcherNameTextField.setText(cfg == null ? null : cfg.getName());
            runTextField.setText(cfg == null ? null : cfg.getCommand());
            runTextField.setCaretPosition(0);
            runDirTextField.setText(cfg == null ? null : cfg.getRunDir());
            buildTextField.setText(cfg == null ? null : cfg.getBuildCommand());
            symbolsTextField.setText(cfg == null ? null : cfg.getSymbolFiles());
            publicCheckBox.setSelected(cfg == null ? false : cfg.getPublic());
            hideCheckBox.setSelected(cfg == null ? false : cfg.isHide());
	    ArrayList<String> col0 = new ArrayList<>();
	    ArrayList<String> col1 = new ArrayList<>();
            int n;
            if (cfg != null) {
                for(Map.Entry<String,String> e : cfg.getEnv().entrySet()) {
                    col0.add(e.getKey());
                    col1.add(e.getValue());
                }
                n = cfg.getEnv().size();
            } else {
                n = 0;
            }
	    envVarModel.setData(n, col0, col1);
            envVarTable.tableChanged(null);
	}
    }

    private static final class LaunchersNodes extends Children.Keys<LauncherConfig> {
        private final ArrayList<LauncherConfig> launcers;
        public LaunchersNodes(final ArrayList<LauncherConfig> launcers) {
            this.launcers = launcers;
            setKeys(launcers);
        }

        private void restKeys() {
            setKeys(launcers);
        }

        @Override
        protected Node[] createNodes(LauncherConfig key) {
            return new LauncherNode[]{new LauncherNode(key)};
        }

    }

    public static class LauncherNode extends AbstractNode {

        private Image icon;
        private static JTextField test = new JTextField();
        private String name;
        private String command;
        private boolean pub;
        private boolean hide;
        private int id;


        public LauncherNode(LauncherConfig cfg) {
            super(Children.LEAF, Lookups.fixed(cfg));
            name = cfg.getDisplayedName();
            hide = cfg.isHide();
            command = cfg.getCommand();
            pub = cfg.getPublic();
            id = cfg.getID();
            updateIcon();
        }

        private void updateIcon() {
            final String resources = "org/netbeans/modules/cnd/makeproject/launchers/resources/"; // NOI18N
            String iconFile;
            if (id >= 0) {
                iconFile = pub ? "launcher_public.png" : "launcher_private.png"; // NOI18N
            } else {
                iconFile = pub ? "common_public.png" : "common_private.png"; // NOI18N
            }
            icon = ImageUtilities.loadImage(resources + iconFile, false);
        }

        public LauncherConfig getConfiguration() {
            return getLookup().lookup(LauncherConfig.class);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return icon;
        }

        // TODO: How to make this correctly?
        public void updateNode(String name, String command, boolean pub, boolean hide) {
            this.name = name;
            this.command = command;
            this.pub = pub;
            this.hide = hide;
            fireDisplayNameChange(null, getDisplayName());
            updateIcon();
            fireIconChange();
        }

        @Override
        public Image getIcon(int type) {
            return icon;
        }

        @Override
        public String getHtmlDisplayName() {
            if (hide || id < 0) {
                return "<font color='!textInactiveText'>" + getDisplayName()+"</font>"; // NOI18N
            }
            return super.getHtmlDisplayName();
        }

        @Override
        public String getDisplayName() {
            if (id < 0) {
                return NbBundle.getMessage(LaunchersPanel.class, "COMMON_PROPERTIES");
            } else {
                String res = name;
                if (res == null || res.isEmpty()) {
                    res = command;
                }
                return res;
            }
        }

        @Override
        public String toString() {
            return getDisplayName();
        }
    }
    
}
