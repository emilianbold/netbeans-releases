/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.ui.platform.NbPlatformCustomizer;
import org.netbeans.modules.apisupport.project.ui.platform.PlatformComponentFactory;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;

/**
 * Represents <em>Libraries</em> panel in NetBeans Module customizer.
 *
 * @author mkrauskopf
 */
public class CustomizerLibraries extends NbPropertyPanel.Single {
    
    /** Creates new form CustomizerLibraries */
    public CustomizerLibraries(final SingleModuleProperties props) {
        super(props, CustomizerLibraries.class);
        initComponents();
        initAccessibility();
        if (!getProperties().isSuiteComponent()) {
            addLibrary.setVisible(false);
            Mnemonics.setLocalizedText(addDepButton, getMessage("CTL_AddButton"));
        }
        refresh();
        dependencyList.setCellRenderer(CustomizerComponentFactory.getDependencyCellRenderer(false));
        javaPlatformCombo.setRenderer(JavaPlatformComponentFactory.javaPlatformListCellRenderer());
        removeTokenButton.setEnabled(false);
        attachListeners();
    }
    
    void refresh() {
        refreshJavaPlatforms();
        refreshPlatforms();
        platformValue.setEnabled(getProperties().isStandalone());
        managePlafsButton.setEnabled(getProperties().isStandalone());
        updateEnabled();
        reqTokenList.setModel(getProperties().getRequiredTokenListModel());
        dependencyList.setModel(getProperties().getDependenciesListModel());
        dependencyList.getModel().addListDataListener(new ListDataListener() {
            public void contentsChanged(ListDataEvent e) { updateEnabled(); }
            public void intervalAdded(ListDataEvent e) { updateEnabled(); }
            public void intervalRemoved(ListDataEvent e) { updateEnabled(); }
        });
    }
    
    private void attachListeners() {
        platformValue.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    // set new platform
                    getProperties().setActivePlatform((NbPlatform) platformValue.getSelectedItem());
                    // refresh dependencies list
                    dependencyList.setModel(getProperties().getDependenciesListModel());
                    updateEnabled();
                }
            }
        });
        dependencyList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updateEnabled();
                }
            }
        });
        javaPlatformCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    // set new platform
                    getProperties().setActiveJavaPlatform((JavaPlatform) javaPlatformCombo.getSelectedItem());
                }
            }
        });
        reqTokenList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    removeTokenButton.setEnabled(reqTokenList.getSelectedIndex() != -1);
                }
            }
        });
    }
    
    private void refreshJavaPlatforms() {
        javaPlatformCombo.setModel(JavaPlatformComponentFactory.javaPlatformListModel());
        javaPlatformCombo.setSelectedItem(getProperties().getActiveJavaPlatform());
    }
    
    private void refreshPlatforms() {
        platformValue.setModel(new PlatformComponentFactory.NbPlatformListModel()); // refresh
        platformValue.setSelectedItem(getProperties().getActivePlatform());
        platformValue.requestFocusInWindow();
    }
    
    private void updateEnabled() {
        // if there is no selection disable edit/remove buttons
        boolean enabled = dependencyList.getModel().getSize() > 0 &&
                getProperties().isActivePlatformValid() && dependencyList.getSelectedIndex() != -1;
        editDepButton.setEnabled(enabled);
        removeDepButton.setEnabled(enabled);
        addDepButton.setEnabled(getProperties().isActivePlatformValid());
        boolean javaEnabled = getProperties().isNetBeansOrg() ||
                (getProperties().isStandalone() &&
                /* #71631 */ ((NbPlatform) platformValue.getSelectedItem()).getHarnessVersion() >= NbPlatform.HARNESS_VERSION_50u1);
        javaPlatformCombo.setEnabled(javaEnabled);
        javaPlatformButton.setEnabled(javaEnabled);
    }
    
    private CustomizerComponentFactory.DependencyListModel getDepListModel() {
        return (CustomizerComponentFactory.DependencyListModel) dependencyList.getModel();
    }
    
    private String getMessage(String key) {
        return NbBundle.getMessage(CustomizerLibraries.class, key);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        modDepLabel = new javax.swing.JLabel();
        depButtonPanel = new javax.swing.JPanel();
        addDepButton = new javax.swing.JButton();
        addLibrary = new javax.swing.JButton();
        removeDepButton = new javax.swing.JButton();
        space1 = new javax.swing.JLabel();
        editDepButton = new javax.swing.JButton();
        dependencySP = new javax.swing.JScrollPane();
        dependencyList = new javax.swing.JList();
        platformsPanel = new javax.swing.JPanel();
        platformValue = org.netbeans.modules.apisupport.project.ui.platform.PlatformComponentFactory.getNbPlatformsComboxBox();
        platform = new javax.swing.JLabel();
        managePlafsButton = new javax.swing.JButton();
        javaPlatformLabel = new javax.swing.JLabel();
        javaPlatformCombo = new javax.swing.JComboBox();
        javaPlatformButton = new javax.swing.JButton();
        reqTokens = new javax.swing.JLabel();
        reqTokenSP = new javax.swing.JScrollPane();
        reqTokenList = new javax.swing.JList();
        tokenButtonPanel = new javax.swing.JPanel();
        addTokenButton = new javax.swing.JButton();
        removeTokenButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        modDepLabel.setLabelFor(dependencyList);
        org.openide.awt.Mnemonics.setLocalizedText(modDepLabel, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_ModuleDependencies"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 2, 0);
        add(modDepLabel, gridBagConstraints);

        depButtonPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(addDepButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_AddDependency"));
        addDepButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addModuleDependency(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        depButtonPanel.add(addDepButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addLibrary, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_AddNewLibrary"));
        addLibrary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addLibraryActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        depButtonPanel.add(addLibrary, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeDepButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_RemoveButton"));
        removeDepButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeModuleDependency(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        depButtonPanel.add(removeDepButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.weighty = 1.0;
        depButtonPanel.add(space1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(editDepButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_EditButton"));
        editDepButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editModuleDependency(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 0);
        depButtonPanel.add(editDepButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(depButtonPanel, gridBagConstraints);

        dependencySP.setViewportView(dependencyList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(dependencySP, gridBagConstraints);

        platformsPanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 12);
        platformsPanel.add(platformValue, gridBagConstraints);

        platform.setLabelFor(platformValue);
        org.openide.awt.Mnemonics.setLocalizedText(platform, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_NetBeansPlatform"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 12);
        platformsPanel.add(platform, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(managePlafsButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_ManagePlatform"));
        managePlafsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                managePlatforms(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        platformsPanel.add(managePlafsButton, gridBagConstraints);

        javaPlatformLabel.setLabelFor(javaPlatformCombo);
        org.openide.awt.Mnemonics.setLocalizedText(javaPlatformLabel, NbBundle.getMessage(CustomizerLibraries.class, "LBL_Java_Platform"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        platformsPanel.add(javaPlatformLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        platformsPanel.add(javaPlatformCombo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(javaPlatformButton, NbBundle.getMessage(CustomizerLibraries.class, "LBL_Manage_Java_Platforms"));
        javaPlatformButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaPlatformButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        platformsPanel.add(javaPlatformButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(platformsPanel, gridBagConstraints);

        reqTokens.setLabelFor(reqTokenList);
        org.openide.awt.Mnemonics.setLocalizedText(reqTokens, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_RequiredTokens"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 12);
        add(reqTokens, gridBagConstraints);

        reqTokenSP.setViewportView(reqTokenList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(reqTokenSP, gridBagConstraints);

        tokenButtonPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(addTokenButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_AddButton_d"));
        addTokenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addToken(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        tokenButtonPanel.add(addTokenButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeTokenButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_RemoveButton_v"));
        removeTokenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeToken(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        tokenButtonPanel.add(removeTokenButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(tokenButtonPanel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    private void addLibraryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addLibraryActionPerformed
        NbModuleProject project = UIUtil.runLibraryWrapperWizard(getProperties().getProject());
        if (project != null) {
            try {
                getProperties().libraryWrapperAdded();
                ModuleDependency dep = new ModuleDependency(
                        getProperties().getModuleList().getEntry(project.getCodeNameBase()));
                getDepListModel().addDependency(dep);
            } catch (IOException e) {
                assert false : e;
            }
        }
    }//GEN-LAST:event_addLibraryActionPerformed
    
    private void javaPlatformButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaPlatformButtonActionPerformed
        PlatformsCustomizer.showCustomizer((JavaPlatform) javaPlatformCombo.getSelectedItem());
        refreshJavaPlatforms();
    }//GEN-LAST:event_javaPlatformButtonActionPerformed
    
    private void removeToken(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeToken
        CustomizerComponentFactory.RequiredTokenListModel model = (CustomizerComponentFactory.RequiredTokenListModel) reqTokenList.getModel();
        Object[] selected = reqTokenList.getSelectedValues();
        for (int i = 0; i < selected.length; i++) {
            model.removeToken((String) selected[i]);
        }
        if (model.getSize() > 0) {
            reqTokenList.setSelectedIndex(0);
        }
        reqTokenList.requestFocusInWindow();
    }//GEN-LAST:event_removeToken
    
    private void addToken(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addToken
        // create add panel
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        panel.setLayout(new BorderLayout(0, 2));
        JList tokenList = new JList(getProperties().getAllTokens());
        JScrollPane tokenListSP = new JScrollPane(tokenList);
        JLabel provTokensTxt = new JLabel();
        provTokensTxt.setLabelFor(tokenList);
        Mnemonics.setLocalizedText(provTokensTxt, getMessage("LBL_ProvidedTokens_T"));
        panel.getAccessibleContext().setAccessibleDescription(getMessage("ACS_ProvidedTokensTitle"));
        tokenList.getAccessibleContext().setAccessibleDescription(getMessage("ACS_LBL_ProvidedTokens"));
        tokenListSP.getVerticalScrollBar().getAccessibleContext().setAccessibleName(getMessage("ACS_CTL_ProvidedTokensVerticalScroll"));
        tokenListSP.getVerticalScrollBar().getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CTL_ProvidedTokensVerticalScroll"));
        tokenListSP.getHorizontalScrollBar().getAccessibleContext().setAccessibleName(getMessage("ACS_CTL_ProvidedTokensHorizontalScroll"));
        tokenListSP.getHorizontalScrollBar().getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CTL_ProvidedTokensHorizontalScroll"));
        
        panel.add(provTokensTxt, BorderLayout.NORTH);
        panel.add(tokenListSP, BorderLayout.CENTER);
        
        DialogDescriptor descriptor = new DialogDescriptor(panel,
                getMessage("LBL_ProvidedTokens_NoMnem"));
        Dialog d = DialogDisplayer.getDefault().createDialog(descriptor);
        d.setVisible(true);
        d.dispose();
        if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
            Object[] selected = tokenList.getSelectedValues();
            CustomizerComponentFactory.RequiredTokenListModel model = (CustomizerComponentFactory.RequiredTokenListModel) reqTokenList.getModel();
            for (int i = 0; i < selected.length; i++) {
                model.addToken((String) selected[i]);
            }
            if (selected.length > 0) {
                reqTokenList.clearSelection();
                reqTokenList.setSelectedValue(selected[0], true);
            }
        }
        reqTokenList.requestFocusInWindow();
    }//GEN-LAST:event_addToken
    
    private void managePlatforms(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_managePlatforms
        NbPlatformCustomizer.showCustomizer();
        refreshPlatforms();
    }//GEN-LAST:event_managePlatforms
    
    private void editModuleDependency(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editModuleDependency
        ModuleDependency origDep = getDepListModel().getDependency(
                dependencyList.getSelectedIndex());
        ModuleDependency editedDep = getDepListModel().findEdited(origDep);
        EditDependencyPanel editPanel = new EditDependencyPanel(
                editedDep == null ? origDep : editedDep, getProperties().getActivePlatform());
        DialogDescriptor descriptor = new DialogDescriptor(editPanel,
                getMessage("CTL_EditModuleDependencyTitle"));
        descriptor.setHelpCtx(new HelpCtx(EditDependencyPanel.class));
        Dialog d = DialogDisplayer.getDefault().createDialog(descriptor);
        d.setVisible(true);
        if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
            getDepListModel().editDependency(origDep, editPanel.getEditedDependency());
        }
        d.dispose();
        dependencyList.requestFocusInWindow();
    }//GEN-LAST:event_editModuleDependency
    
    private void removeModuleDependency(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeModuleDependency
        getDepListModel().removeDependencies(NbCollections.checkedListByCopy(Arrays.asList(dependencyList.getSelectedValues()), ModuleDependency.class, true));
        if (dependencyList.getModel().getSize() > 0) {
            dependencyList.setSelectedIndex(0);
        }
        dependencyList.requestFocusInWindow();
    }//GEN-LAST:event_removeModuleDependency
    
    private void addModuleDependency(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addModuleDependency
        ModuleDependency[] newDeps = AddModulePanel.selectDependencies(getProperties());
        for (int i = 0; i < newDeps.length; i++) {
            ModuleDependency dep = newDeps[i];
            if ("0".equals(dep.getReleaseVersion())) { // #72216 NOI18N
                getDepListModel().addDependency(new ModuleDependency(
                            dep.getModuleEntry(), "0-1", dep.getSpecificationVersion(), // NOI18N
                            dep.hasCompileDependency(), dep.hasImplementationDepedendency()));
            } else {
                getDepListModel().addDependency(dep);
            }
            dependencyList.setSelectedValue(dep, true);
        }
        dependencyList.requestFocusInWindow();
    }//GEN-LAST:event_addModuleDependency
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addDepButton;
    private javax.swing.JButton addLibrary;
    private javax.swing.JButton addTokenButton;
    private javax.swing.JPanel depButtonPanel;
    private javax.swing.JList dependencyList;
    private javax.swing.JScrollPane dependencySP;
    private javax.swing.JButton editDepButton;
    private javax.swing.JButton javaPlatformButton;
    private javax.swing.JComboBox javaPlatformCombo;
    private javax.swing.JLabel javaPlatformLabel;
    private javax.swing.JButton managePlafsButton;
    private javax.swing.JLabel modDepLabel;
    private javax.swing.JLabel platform;
    private javax.swing.JComboBox platformValue;
    private javax.swing.JPanel platformsPanel;
    private javax.swing.JButton removeDepButton;
    private javax.swing.JButton removeTokenButton;
    private javax.swing.JList reqTokenList;
    private javax.swing.JScrollPane reqTokenSP;
    private javax.swing.JLabel reqTokens;
    private javax.swing.JLabel space1;
    private javax.swing.JPanel tokenButtonPanel;
    // End of variables declaration//GEN-END:variables
    
    private void initAccessibility() {
        addTokenButton.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_SrcLevelValue"));
        dependencyList.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_DependencyList"));
        editDepButton.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_EditDepButton"));
        removeDepButton.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_RemoveDepButton"));
        removeTokenButton.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_RemoveTokenButton"));
        addDepButton.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_AddDepButton"));
        reqTokenList.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_ReqTokenList"));
    }
    
}
