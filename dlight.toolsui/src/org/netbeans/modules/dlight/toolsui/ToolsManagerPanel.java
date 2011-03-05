/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.dlight.api.tool.DLightConfiguration;
import org.netbeans.modules.dlight.api.tool.DLightTool;
import org.netbeans.modules.dlight.api.tool.impl.DLightConfigurationManagerAccessor;
import org.netbeans.modules.dlight.toolsui.api.PanelWithApply;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author thp
 */
public class ToolsManagerPanel extends PanelWithApply {

    private List<DLightConfigurationUIWrapper> dLightConfigurations = null;
    private List<DLightTool> allDLightTools = null;
    private static String manageConfigurations = getString("ManageConfiurations");
    private int lastSelectedIndex = 0;
    private ToolsTable toolsTable = null;

    /** Creates new form ToolsManagerPanel */
    public ToolsManagerPanel() {
        this(null);
    }

    public ToolsManagerPanel(String preferredConfigurationName) {
        initComponents();
        descriptionArea.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background")); // NOI18N
        descriptionArea.setOpaque(true);

        DLightConfigurationManagerAccessor accessor = DLightConfigurationManagerAccessor.getDefault();
        allDLightTools = accessor.getDefaultConfiguration().getToolsSet();
        initDialog(DLightConfigurationUIWrapperProvider.getInstance().getDLightConfigurationUIWrappers(), preferredConfigurationName);
        setPreferredSize(new Dimension(700, 400));
    }

    private void initDialog(List<DLightConfigurationUIWrapper> list, String preferredConfigurationName) {
        // profile configuration combobox
        profileConfigurationComboBox.removeAllItems();
        dLightConfigurations = list;
        Collections.sort(dLightConfigurations, new DLightConfigurationComparator());
        DLightConfigurationUIWrapper preferredConfiguration = null;
        for (DLightConfigurationUIWrapper dlightConfigurationWrapper : dLightConfigurations) {
            profileConfigurationComboBox.addItem(dlightConfigurationWrapper);
            if (preferredConfigurationName != null && dlightConfigurationWrapper.getDisplayName().equals(preferredConfigurationName)) {
                preferredConfiguration = dlightConfigurationWrapper;
            }
        }
        profileConfigurationComboBox.addItem(manageConfigurations);
        if (preferredConfiguration != null) {
            profileConfigurationComboBox.setSelectedItem(preferredConfiguration);
        } else {
            profileConfigurationComboBox.setSelectedIndex(0);
        }
    }

    private void initConfigurationPanel(DLightConfigurationUIWrapper dlightConfigurationUIWrapper) {
        DLightConfiguration gizmoConfiguration = dlightConfigurationUIWrapper.getDLightConfiguration();
        assert gizmoConfiguration != null;
//        defaultDataProviderComboBox.removeAllItems();
//        defaultDataProviderComboBox.addItem("SunStudio"); // NOI18N
//        defaultDataProviderComboBox.addItem("DTrace"); // NOI18N
        // FIXUP: should be moved to tool
//        String dataProvider;
//        if (gizmoConfiguration.getDisplayedName().indexOf("DTrace") >= 0) {
//            dataProvider = "DTrace"; // NOI18N
//        }
//        else if (gizmoConfiguration.getDisplayedName().indexOf("Studio") >= 0) {
//            dataProvider = "Sun Studio"; // NOI18N
//        }
//        else {
//            dataProvider = "Simple (indicators only)"; // NOI18N
//        }
        //dataProviderLabel2.setText(dataProvider);
        toolsTable = new ToolsTable(dlightConfigurationUIWrapper, dlightConfigurationUIWrapper.getTools(), new MySelectionListener());
        toolsList.setViewportView(toolsTable);
        toolsTable.initSelection();//getSelectionModel().setSelectionInterval(0, 0);
        toolsLabel.setLabelFor(toolsTable);
    }

    private DLightConfigurationUIWrapper inList(String name, List<DLightConfigurationUIWrapper> list) {
        for (DLightConfigurationUIWrapper wrapper : list) {
            if (name.equals(wrapper.getName())) {
                return wrapper;
            }
        }
        return null;
    }

    @Override
    public boolean apply() {
        // Delete deleted configurations
        List<DLightConfigurationUIWrapper> oldDLightConfiguration = DLightConfigurationUIWrapperProvider.getInstance().getDLightConfigurationUIWrappers();
        List<DLightConfigurationUIWrapper> toBeDeleted = new ArrayList<DLightConfigurationUIWrapper>();

        for (DLightConfigurationUIWrapper wrapper : oldDLightConfiguration) {
            DLightConfigurationUIWrapper c = inList(wrapper.getName(), dLightConfigurations);
            if (c == null || c.getCopyOf() != null) {
                toBeDeleted.add(wrapper);
            }
        }

        DLightConfigurationManagerAccessor mgrAccessor = DLightConfigurationManagerAccessor.getDefault();

        for (DLightConfigurationUIWrapper conf : toBeDeleted) {
            mgrAccessor.removeConfiguration(conf.getName());
        }

        // Rename renamed configurations
        for (DLightConfigurationUIWrapper configuration : dLightConfigurations) {
            if (configuration.isCustom() && configuration.getCopyOf() == null) {
                if (!configuration.getName().equals(configuration.getDLightConfiguration().getConfigurationName())) {
                    DLightConfiguration origDLightConfiguration = configuration.getDLightConfiguration();
                    String category = origDLightConfiguration.getCategoryName();
                    List<String> platforms = origDLightConfiguration.getPlatforms();
                    String collector = origDLightConfiguration.getCollectorProviders();
                    List<String> indicators = origDLightConfiguration.getIndicatorProviders();
                    mgrAccessor.removeConfiguration(origDLightConfiguration.getConfigurationName());
                    DLightConfiguration dlightConfiguration = mgrAccessor.registerConfiguration(configuration.getName(), configuration.getDisplayName(), category, platforms, collector, indicators);
                    configuration.setDLightConfiguration(dlightConfiguration);
                    for (DLightToolUIWrapper toolUI : configuration.getTools()) {
                        toolUI.setModified(true);
                    }
                }
            }
        }

        // create duplicates
        for (DLightConfigurationUIWrapper configuration : dLightConfigurations) {
            if (configuration.isCustom() && configuration.getCopyOf() != null) {
                DLightConfiguration origDLightConfiguration = configuration.getCopyOf();
                String category = origDLightConfiguration.getCategoryName();
                List<String> platforms = origDLightConfiguration.getPlatforms();
                String collector = origDLightConfiguration.getCollectorProviders();
                List<String> indicators = origDLightConfiguration.getIndicatorProviders();
                DLightConfiguration dlightConfiguration =
                        mgrAccessor.registerConfigurationAsACopy(configuration.getCopyOf(),
                        configuration.getName(), configuration.getDisplayName(), category, platforms, collector, indicators);
                configuration.setDLightConfiguration(dlightConfiguration);
                for (DLightToolUIWrapper toolUI : configuration.getTools()) {
                    toolUI.setModified(true);
                }
                configuration.setCopyOf(null);
            }
        }

        // save all changes to tools
        for (DLightConfigurationUIWrapper configuration : dLightConfigurations) {
            for (DLightToolUIWrapper toolUI : configuration.getTools()) {
                if (toolUI.isModified()) {
                    //if it was disabled and now enabled: should register
                    if (!toolUI.canEnable()) {
                        mgrAccessor.deleteTool(configuration.getName(), toolUI.getDLightTool());
                    } else {
                        mgrAccessor.registerTool(configuration.getName(), toolUI.getDLightTool().getID(), toolUI.isEnabled());
                    }
                }
            }
        }

        // save wrappers
        DLightConfigurationUIWrapperProvider.getInstance().setDLightConfigurationUIWrappers(dLightConfigurations);
        return true;
    }

    private DLightToolUIWrapper getSelectedDLightToolWrapper() {
        int row = toolsTable.getSelectedRow();
        DLightConfigurationUIWrapper dLightConfigurationWrapper = (DLightConfigurationUIWrapper) profileConfigurationComboBox.getSelectedItem();
        DLightToolUIWrapper tool = dLightConfigurationWrapper.getTools().get(row);
        return tool;
    }

    public DLightTool getSelectedTool() {
        int row = toolsTable.getSelectedRow();
        if (row < 0) {
            return null;
        }
        DLightToolUIWrapper wrapper = getSelectedDLightToolWrapper();
        return wrapper.getDLightTool();
    }

    private class MySelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            DLightToolUIWrapper tool = getSelectedDLightToolWrapper();
//            toolNameLabelField.setText(tool.getDLightTool().getName());
//            onByDefaultCheckBox.setSelected(tool.isOnByDefault());
            detailsLabel.setText(tool.getDLightTool().getDetailedName());
            String text = tool.getDLightTool().getDescription();
            descriptionArea.setText(text == null ? "" : text);
        }
    }

    private static String getString(String key, String... params) {
        return NbBundle.getMessage(ToolsManagerPanel.class, key, params);
    }
    private static final String datailsLabelDefaultText = "Details:"; // NOI18N - to avoid late UI changes

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        profileConfigurationLabel = new javax.swing.JLabel();
        profileConfigurationComboBox = new javax.swing.JComboBox();
        jSplitPane1 = new javax.swing.JSplitPane();
        leftPanel = new javax.swing.JPanel();
        toolsLabel = new javax.swing.JLabel();
        toolsList = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        rightPanel = new javax.swing.JPanel();
        detailsLabel = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        descriptionArea = new javax.swing.JTextArea();

        profileConfigurationLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/dlight/toolsui/Bundle").getString("ProfilerConfiguration_MN").charAt(0));
        profileConfigurationLabel.setLabelFor(profileConfigurationComboBox);
        profileConfigurationLabel.setText(org.openide.util.NbBundle.getMessage(ToolsManagerPanel.class, "ToolsManagerPanel.profileConfigurationLabel.text")); // NOI18N
        profileConfigurationLabel.setFocusable(false);

        profileConfigurationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profileConfigurationComboBoxActionPerformed(evt);
            }
        });

        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerSize(5);
        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setDividerLocation(0.5);

        leftPanel.setPreferredSize(new java.awt.Dimension(100, 100));
        leftPanel.setLayout(new java.awt.BorderLayout());

        toolsLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/dlight/toolsui/Bundle").getString("TOOLS_MN").charAt(0));
        toolsLabel.setText(org.openide.util.NbBundle.getMessage(ToolsManagerPanel.class, "ToolsManagerPanel.toolsLabel.text")); // NOI18N
        toolsLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 5, 1, 1));
        toolsLabel.setFocusable(false);
        leftPanel.add(toolsLabel, java.awt.BorderLayout.NORTH);

        toolsList.setOpaque(false);
        toolsList.setPreferredSize(new java.awt.Dimension(100, 100));

        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList1.setOpaque(false);
        jList1.setPreferredSize(new java.awt.Dimension(100, 100));
        toolsList.setViewportView(jList1);

        leftPanel.add(toolsList, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(leftPanel);

        rightPanel.setPreferredSize(new java.awt.Dimension(100, 100));
        rightPanel.setLayout(new java.awt.BorderLayout());

        detailsLabel.setLabelFor(descriptionArea);
        detailsLabel.setText(datailsLabelDefaultText);
        detailsLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 5, 1, 1));
        detailsLabel.setFocusable(false);
        rightPanel.add(detailsLabel, java.awt.BorderLayout.NORTH);

        scrollPane.setPreferredSize(new java.awt.Dimension(100, 100));

        descriptionArea.setColumns(20);
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setRows(5);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFocusable(false);
        descriptionArea.setMargin(new java.awt.Insets(5, 5, 5, 5));
        descriptionArea.setPreferredSize(new java.awt.Dimension(100, 100));
        scrollPane.setViewportView(descriptionArea);

        rightPanel.add(scrollPane, java.awt.BorderLayout.CENTER);

        jSplitPane1.setRightComponent(rightPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(profileConfigurationLabel)
                        .addGap(12, 12, 12)
                        .addComponent(profileConfigurationComboBox, 0, 388, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(profileConfigurationLabel)
                    .addComponent(profileConfigurationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void profileConfigurationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
        Object item = profileConfigurationComboBox.getSelectedItem();
        if (item instanceof String && ((String) item).equals(manageConfigurations)) {
            profileConfigurationComboBox.hidePopup();
            MyListEditorPanel listEditorPanel = new MyListEditorPanel(dLightConfigurations);

            DialogDescriptor descriptor = new DialogDescriptor(listEditorPanel, getString("TXT_ConfigurationsCustomizer"));
            Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
            try {
                dlg.setVisible(true);
                if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
                    List<DLightConfigurationUIWrapper> newList = listEditorPanel.getListData();
                    List<DLightConfigurationUIWrapper> oldList = DLightConfigurationUIWrapperProvider.getInstance().getDLightConfigurationUIWrappers();
                    oldList.clear();
                    oldList.addAll(newList);
                    initDialog(newList, null);
                    DLightConfigurationUIWrapper dlightConfigurationUIWrapper = (DLightConfigurationUIWrapper) profileConfigurationComboBox.getSelectedItem();
                    initConfigurationPanel(dlightConfigurationUIWrapper);
                } else {
                    profileConfigurationComboBox.setSelectedIndex(lastSelectedIndex);
                }
            } finally {
                dlg.dispose();
            }

        } else if (item instanceof DLightConfigurationUIWrapper) {
            DLightConfigurationUIWrapper dlightConfigurationUIWrapper = (DLightConfigurationUIWrapper) item;
            initConfigurationPanel(dlightConfigurationUIWrapper);
        } else {
            assert false;
        }
        lastSelectedIndex = profileConfigurationComboBox.getSelectedIndex();
    }

    private class MyListEditorPanel extends ListEditorPanel<DLightConfigurationUIWrapper> {

        public MyListEditorPanel(List<DLightConfigurationUIWrapper> list) {
            super(list);
            setPreferredSize(new Dimension(400, 300));
            getAddButton().setVisible(false);
        }

//        @Override
//        public DLightConfigurationUIWrapper addAction() {
//            NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine(getString("EDIT_DIALOG_LABEL_TXT"), getString("EDIT_DIALOG_TITLE_TXT"));
//            notifyDescriptor.setInputText(getString("NewConfigurationName"));
//            DialogDisplayer.getDefault().notify(notifyDescriptor);
//            if (notifyDescriptor.getValue() != NotifyDescriptor.OK_OPTION) {
//                return null;
//            }
//            String newS = notifyDescriptor.getInputText();
//            return new DLightConfigurationUIWrapper(newS, newS, allDLightTools);
//        }
        private String makeNameUnique(String suggestedName) {
            if (findDLightConfigurationUIWrapper(suggestedName) == null) {
                return suggestedName;
            } else {
                for (int i = 1;; i++) {
                    String newName = suggestedName + "_" + i; // NOI18N
                    if (findDLightConfigurationUIWrapper(newName) == null) {
                        return newName;
                    }
                }
            }
        }

        private String makeNameLegal(String suggestedName) {
            String newName = suggestedName;
            newName = newName.replace("/", "_FSLASH_"); // NOI18N
            newName = newName.replace("\\", "_BSLASH_"); // NOI18N
            newName = newName.replace(".", "_DOT_"); // NOI18N
            return newName.trim();
        }

        private DLightConfigurationUIWrapper findDLightConfigurationUIWrapper(String name) {
            for (DLightConfigurationUIWrapper dlightConfigurationUIWrapper : getListData()) {
                if (dlightConfigurationUIWrapper.getDisplayName().equals(name)) {
                    return dlightConfigurationUIWrapper;
                }
            }
            return null;
        }

//        @Override
//        public void removeAction(DLightConfigurationUIWrapper o) {
//            super.removeAction(o);
//            profileConfigurationComboBox.setSelectedIndex(profileConfigurationComboBox.getSelectedIndex());
//        }
        @Override
        public DLightConfigurationUIWrapper copyAction(DLightConfigurationUIWrapper o) {
            String newDisplayName = makeNameUnique(getString("CopyOf", o.getDisplayName()));
            String newName = makeNameLegal(newDisplayName);
            DLightConfigurationUIWrapper copy = new DLightConfigurationUIWrapper(newName, newDisplayName, allDLightTools); // NOI18N
            List<DLightToolUIWrapper> tools = o.getTools();
            List<DLightToolUIWrapper> copyTools = copy.getTools();
            int i = 0;
            for (DLightToolUIWrapper tool : tools) {
                DLightToolUIWrapper copyTool = copyTools.get(i++);
                copyTool.setEnabled(tool.isEnabled());
                copyTool.setCanEnable(tool.canEnable());
            }
            //always link to the original
            DLightConfiguration copyOf = o.getDLightConfiguration();
            if (o.getCopyOf() != null) {
                copyOf = o.getCopyOf();
            }
            copy.setCopyOf(copyOf);
            return copy;
        }

        @Override
        public void editAction(DLightConfigurationUIWrapper o) {
            NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine(getString("EDIT_DIALOG_LABEL_TXT"), getString("EDIT_DIALOG_TITLE_TXT"));
            notifyDescriptor.setInputText(o.getDisplayName());
            DialogDisplayer.getDefault().notify(notifyDescriptor);
            if (notifyDescriptor.getValue() != NotifyDescriptor.OK_OPTION) {
                return;
            }
            String newDisplayName = notifyDescriptor.getInputText();
            newDisplayName = newDisplayName.trim();
            if (newDisplayName.trim().length() == 0) {
                newDisplayName = o.getDisplayName();
            } else {
                newDisplayName = makeNameUnique(newDisplayName);
            }
            o.setDisplayName(newDisplayName);
            String newName = makeNameLegal(newDisplayName);
            o.setName(newName);
        }

        @Override
        protected void checkSelection(int i) {
            super.checkSelection(i);
            DLightConfigurationUIWrapper dLightConfigurationWrapper = getListData().elementAt(i);
            getEditButton().setEnabled(dLightConfigurationWrapper.isCustom());
            getRemoveButton().setEnabled(dLightConfigurationWrapper.isCustom());
        }
    }

    private static class DLightConfigurationComparator implements Comparator<DLightConfigurationUIWrapper> {

        @Override
        public int compare(DLightConfigurationUIWrapper o1, DLightConfigurationUIWrapper o2) {
            return o1.getDisplayName().compareTo(o2.getDisplayName());
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea descriptionArea;
    private javax.swing.JLabel detailsLabel;
    private javax.swing.JList jList1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JComboBox profileConfigurationComboBox;
    private javax.swing.JLabel profileConfigurationLabel;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JLabel toolsLabel;
    private javax.swing.JScrollPane toolsList;
    // End of variables declaration//GEN-END:variables
}
