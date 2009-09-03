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
 * ToolsManagerPanel.java
 *
 * Created on Aug 8, 2009, 1:20:21 PM
 */
package org.netbeans.modules.dlight.toolsui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.util.List;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.dlight.api.tool.DLightConfiguration;
import org.netbeans.modules.dlight.api.tool.DLightConfigurationManager;
import org.netbeans.modules.dlight.api.tool.DLightConfigurationUIWrapper;
import org.netbeans.modules.dlight.api.tool.DLightTool;
import org.netbeans.modules.dlight.api.tool.DLightToolUIWrapper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author thp
 */
public class ToolsManagerPanel extends javax.swing.JPanel {
    private List<DLightConfigurationUIWrapper> dLightConfigurations = null;
    private List<DLightTool> allDLightTools = null;
    private static String manageConfigurations = getString("ManageConfiurations");
    private int lastSelectedIndex = 0;
    private ToolsTable toolsTable = null;

    /** Creates new form ToolsManagerPanel */
    public ToolsManagerPanel() {
        initComponents();

        allDLightTools = DLightConfigurationManager.getInstance().getDefaultConfiguration().getToolsSet();

//        ArrayList<DLightConfigurationWrapper> list = new ArrayList<DLightConfigurationWrapper>();
//        for (DLightConfiguration dLightConfiguration : DLightConfigurationManager.getInstance().getDLightConfigurations()) {
//            list.add(new DLightConfigurationWrapper(dLightConfiguration, allDLightTools));
//        }
        initDialog(DLightConfigurationManager.getInstance().getDLightConfigurationUIWrappers());
        setPreferredSize(new Dimension(700, 400));
    }

    private void initDialog(List<DLightConfigurationUIWrapper> list) {
        // profile configuration combobox
        profileConfigurationComboBox.removeAllItems();
        dLightConfigurations = list;
        for (DLightConfigurationUIWrapper dlightConfigurationWrapper : dLightConfigurations) {
            profileConfigurationComboBox.addItem(dlightConfigurationWrapper);
        }
        profileConfigurationComboBox.addItem(manageConfigurations);
        profileConfigurationComboBox.setSelectedIndex(0);
    }

    private void initConfigurationPanel(DLightConfigurationUIWrapper dlightConfigurationUIWrapper) {
        DLightConfiguration gizmoConfiguration = dlightConfigurationUIWrapper.getdLightConfiguration();
        assert gizmoConfiguration != null;
        profileOnRunCheckBox.setSelected(true);
        defaultDataProviderComboBox.removeAllItems();
        defaultDataProviderComboBox.addItem("SunStudio"); // NOI18N
        defaultDataProviderComboBox.addItem("DTrace"); // NOI18N
        toolsTable = new ToolsTable(dlightConfigurationUIWrapper.getTools(), new MySelectionListener());
        toolsList.setViewportView(toolsTable);
        toolsTable.initSelection();//getSelectionModel().setSelectionInterval(0, 0);
    }

    public boolean apply() {
        return true;
    }

    private DLightToolUIWrapper getSelectedDLightToolWrapper() {
        int row = toolsTable.getSelectedRow();
        DLightConfigurationUIWrapper dLightConfigurationWrapper = (DLightConfigurationUIWrapper)profileConfigurationComboBox.getSelectedItem();
        DLightToolUIWrapper tool = dLightConfigurationWrapper.getTools().get(row);
        return tool;
    }

    class MySelectionListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
            DLightToolUIWrapper tool = getSelectedDLightToolWrapper();
            toolNameTextField.setText(tool.getdLightTool().getName());
            onByDefaultCheckBox.setSelected(tool.isOnByDefault());
            visibleCheckBox.setSelected(tool.isVisible());
            detailsLabel.setText(tool.getdLightTool().getDetailedName());
        }
    }

    private static String getString(String key, String... params) {
        return NbBundle.getMessage(ToolsManagerPanel.class, key, params);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        profileConfigurationLabel = new javax.swing.JLabel();
        profileConfigurationComboBox = new javax.swing.JComboBox();
        profileOnRunCheckBox = new javax.swing.JCheckBox();
        defaultDataProviderLabel = new javax.swing.JLabel();
        defaultDataProviderComboBox = new javax.swing.JComboBox();
        toolsPanel = new javax.swing.JPanel();
        toolsList = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        toolsLabel = new javax.swing.JLabel();
        toolPropertyPanel = new javax.swing.JPanel();
        toolNameLabel = new javax.swing.JLabel();
        toolNameTextField = new javax.swing.JTextField();
        onByDefaultCheckBox = new javax.swing.JCheckBox();
        detailsLabel = new javax.swing.JLabel();
        visibleCheckBox = new javax.swing.JCheckBox();
        updateButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        profileConfigurationLabel.setText(org.openide.util.NbBundle.getMessage(ToolsManagerPanel.class, "ToolsManagerPanel.profileConfigurationLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(profileConfigurationLabel, gridBagConstraints);

        profileConfigurationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profileConfigurationComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 4, 0, 12);
        add(profileConfigurationComboBox, gridBagConstraints);

        profileOnRunCheckBox.setText(org.openide.util.NbBundle.getMessage(ToolsManagerPanel.class, "ToolsManagerPanel.profileOnRunCheckBox.text")); // NOI18N
        profileOnRunCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profileOnRunCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 12, 0, 0);
        add(profileOnRunCheckBox, gridBagConstraints);

        defaultDataProviderLabel.setText(org.openide.util.NbBundle.getMessage(ToolsManagerPanel.class, "ToolsManagerPanel.defaultDataProviderLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(defaultDataProviderLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 12);
        add(defaultDataProviderComboBox, gridBagConstraints);

        toolsPanel.setLayout(new java.awt.GridBagLayout());

        toolsList.setViewportView(jList1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        toolsPanel.add(toolsList, gridBagConstraints);

        toolsLabel.setText(org.openide.util.NbBundle.getMessage(ToolsManagerPanel.class, "ToolsManagerPanel.toolsLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        toolsPanel.add(toolsLabel, gridBagConstraints);

        toolPropertyPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        toolPropertyPanel.setLayout(new java.awt.GridBagLayout());

        toolNameLabel.setText(org.openide.util.NbBundle.getMessage(ToolsManagerPanel.class, "ToolsManagerPanel.toolNameLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        toolPropertyPanel.add(toolNameLabel, gridBagConstraints);

        toolNameTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 6);
        toolPropertyPanel.add(toolNameTextField, gridBagConstraints);

        onByDefaultCheckBox.setText(org.openide.util.NbBundle.getMessage(ToolsManagerPanel.class, "ToolsManagerPanel.onByDefaultCheckBox.text")); // NOI18N
        onByDefaultCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onByDefaultCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 6);
        toolPropertyPanel.add(onByDefaultCheckBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        toolPropertyPanel.add(detailsLabel, gridBagConstraints);

        visibleCheckBox.setText(org.openide.util.NbBundle.getMessage(ToolsManagerPanel.class, "ToolsManagerPanel.visibleCheckBox.text")); // NOI18N
        visibleCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                visibleCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        toolPropertyPanel.add(visibleCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 12);
        toolsPanel.add(toolPropertyPanel, gridBagConstraints);

        updateButton.setText(org.openide.util.NbBundle.getMessage(ToolsManagerPanel.class, "ToolsManagerPanel.updateButton.text")); // NOI18N
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 110);
        toolsPanel.add(updateButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(toolsPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void profileOnRunCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profileOnRunCheckBoxActionPerformed

    }//GEN-LAST:event_profileOnRunCheckBoxActionPerformed

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Coming soon....\nFrom this dialog the user will be able to (semi) automatically add new tools to list and update existing tools. Details TBD but could be via update center...", NotifyDescriptor.ERROR_MESSAGE)); // NOI18N
    }//GEN-LAST:event_updateButtonActionPerformed

    private void onByDefaultCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onByDefaultCheckBoxActionPerformed
        DLightToolUIWrapper tool = getSelectedDLightToolWrapper();
        tool.setOnByDefault(!tool.isOnByDefault());
    }//GEN-LAST:event_onByDefaultCheckBoxActionPerformed

    private void profileConfigurationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profileConfigurationComboBoxActionPerformed
        Object item = profileConfigurationComboBox.getSelectedItem();
        if (item instanceof String && ((String)item).equals(manageConfigurations)) {
            MyListEditorPanel listEditorPanel = new MyListEditorPanel(dLightConfigurations);

            DialogDescriptor descriptor = new DialogDescriptor (listEditorPanel, getString("TXT_ToolsCustomizer"));
            Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
            try {
                dlg.setVisible(true);
                if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
                    List<DLightConfigurationUIWrapper> newList = listEditorPanel.getListData();
                    List<DLightConfigurationUIWrapper> oldList = DLightConfigurationManager.getInstance().getDLightConfigurationUIWrappers();
                    oldList.clear();
                    oldList.addAll(newList);
                    initDialog(newList);
                }
                else {
                    profileConfigurationComboBox.setSelectedIndex(lastSelectedIndex);
                }
            } finally {
                dlg.dispose();
            }

        } else if (item instanceof DLightConfigurationUIWrapper) {
            DLightConfigurationUIWrapper dlightConfigurationUIWrapper = (DLightConfigurationUIWrapper)item;
            initConfigurationPanel(dlightConfigurationUIWrapper);
        }
        else {
            assert false;
        }
        lastSelectedIndex = profileConfigurationComboBox.getSelectedIndex();
    }//GEN-LAST:event_profileConfigurationComboBoxActionPerformed

    private void visibleCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_visibleCheckBoxActionPerformed
        DLightToolUIWrapper tool = getSelectedDLightToolWrapper();
        tool.setVisible(!tool.isVisible());
    }//GEN-LAST:event_visibleCheckBoxActionPerformed

    class MyListEditorPanel extends ListEditorPanel<DLightConfigurationUIWrapper> {
        public MyListEditorPanel(List<DLightConfigurationUIWrapper> list) {
            super(list, null);
            setPreferredSize(new Dimension(400, 300));
        }

        @Override
        public DLightConfigurationUIWrapper addAction() {
            NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine(getString("EDIT_DIALOG_LABEL_TXT"), getString("EDIT_DIALOG_TITLE_TXT"));
            notifyDescriptor.setInputText(getString("NewConfigurationName"));
            DialogDisplayer.getDefault().notify(notifyDescriptor);
            if (notifyDescriptor.getValue() != NotifyDescriptor.OK_OPTION) {
                return null;
            }
            String newS = notifyDescriptor.getInputText();
            return new DLightConfigurationUIWrapper(newS, allDLightTools);
        }

        @Override
        public DLightConfigurationUIWrapper copyAction(DLightConfigurationUIWrapper o) {
            DLightConfigurationUIWrapper copy = new DLightConfigurationUIWrapper(getString("CopyOf", o.getName()), allDLightTools);
            List<DLightToolUIWrapper> tools = o.getTools();
            List<DLightToolUIWrapper> copyTools = copy.getTools();
            int i = 0;
            for (DLightToolUIWrapper tool : tools) {
                DLightToolUIWrapper copyTool = copyTools.get(i++);
                copyTool.setEnabled(tool.isEnabled());
                copyTool.setOnByDefault(tool.isOnByDefault());
                copyTool.setVisible(tool.isVisible());
            }
            return copy;
        }

        @Override
        public void editAction(DLightConfigurationUIWrapper o) {
            String s = o.getName();

            NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine(getString("EDIT_DIALOG_LABEL_TXT"), getString("EDIT_DIALOG_TITLE_TXT"));
            notifyDescriptor.setInputText(s);
            DialogDisplayer.getDefault().notify(notifyDescriptor);
            if (notifyDescriptor.getValue() != NotifyDescriptor.OK_OPTION) {
                return;
            }
            String newS = notifyDescriptor.getInputText();
            o.setName(newS);
        }

        @Override
        protected void checkSelection(int i) {
            super.checkSelection(i);
            DLightConfigurationUIWrapper dLightConfigurationWrapper = getListData().elementAt(i);
            getEditButton().setEnabled(dLightConfigurationWrapper.isCustom());
            getRemoveButton().setEnabled(dLightConfigurationWrapper.isCustom());
        }

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox defaultDataProviderComboBox;
    private javax.swing.JLabel defaultDataProviderLabel;
    private javax.swing.JLabel detailsLabel;
    private javax.swing.JList jList1;
    private javax.swing.JCheckBox onByDefaultCheckBox;
    private javax.swing.JComboBox profileConfigurationComboBox;
    private javax.swing.JLabel profileConfigurationLabel;
    private javax.swing.JCheckBox profileOnRunCheckBox;
    private javax.swing.JLabel toolNameLabel;
    private javax.swing.JTextField toolNameTextField;
    private javax.swing.JPanel toolPropertyPanel;
    private javax.swing.JLabel toolsLabel;
    private javax.swing.JScrollPane toolsList;
    private javax.swing.JPanel toolsPanel;
    private javax.swing.JButton updateButton;
    private javax.swing.JCheckBox visibleCheckBox;
    // End of variables declaration//GEN-END:variables
    }
