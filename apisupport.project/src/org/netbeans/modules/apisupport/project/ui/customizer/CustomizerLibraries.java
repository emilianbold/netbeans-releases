/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.modules.apisupport.project.ui.platform.NbPlatformCustomizer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 * Represents <em>Libraries</em> panel in Netbeans Module customizer.
 *
 * @author mkrauskopf
 */
public class CustomizerLibraries extends JPanel implements ComponentFactory.StoragePanel {
    
    private ComponentFactory.DependencyListModel moduleDeps;
    private ComponentFactory.DependencyListModel universeModulesModel;
    private NbModuleProperties modProps;
    
    /** Creates new form CustomizerLibraries */
    public CustomizerLibraries(final NbModuleProperties modProps,
            final ComponentFactory.DependencyListModel subModules,
            final ComponentFactory.DependencyListModel universeModules) {
        initComponents();
        platformValue.setSelectedItem(modProps.getActivePlatform());
        if (!modProps.isStandalone()) {
            platformValue.setEnabled(false);
        }
        this.modProps = modProps;
        this.moduleDeps = subModules;
        this.universeModulesModel = universeModules;
        updateEnabled();
        dependencyList.setModel(subModules);
        dependencyList.setCellRenderer(ComponentFactory.getDependencyCellRenderer(false));
        dependencyList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updateEnabled();
                }
            }
        });
    }
    
    public void store() {
        modProps.setActivePlatform((NbPlatform) platformValue.getSelectedItem());
    }
    
    private void updateEnabled() {
        // if there is no selection disable edit/remove buttons
        boolean enabled = dependencyList.getSelectedIndex() != -1;
        editDepButton.setEnabled(enabled);
        removeDepButton.setEnabled(enabled);
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
        removeDepButton = new javax.swing.JButton();
        space1 = new javax.swing.JLabel();
        editDepButton = new javax.swing.JButton();
        dependencySP = new javax.swing.JScrollPane();
        dependencyList = new javax.swing.JList();
        platformPanel = new javax.swing.JPanel();
        platformValue = org.netbeans.modules.apisupport.project.ui.platform.ComponentFactory.getNbPlatformsComboxBox();
        platform = new javax.swing.JLabel();
        managePlafsButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        modDepLabel.setLabelFor(dependencyList);
        org.openide.awt.Mnemonics.setLocalizedText(modDepLabel, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_ModuleDependencies"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 0);
        add(modDepLabel, gridBagConstraints);

        depButtonPanel.setLayout(new java.awt.GridLayout(4, 1));

        org.openide.awt.Mnemonics.setLocalizedText(addDepButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_AddButton"));
        addDepButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addModuleDependency(evt);
            }
        });

        depButtonPanel.add(addDepButton);

        org.openide.awt.Mnemonics.setLocalizedText(removeDepButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_RemoveButton"));
        removeDepButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeModuleDependency(evt);
            }
        });

        depButtonPanel.add(removeDepButton);

        depButtonPanel.add(space1);

        org.openide.awt.Mnemonics.setLocalizedText(editDepButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_EditButton"));
        editDepButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editModuleDependency(evt);
            }
        });

        depButtonPanel.add(editDepButton);

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

        platformPanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        platformPanel.add(platformValue, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(platform, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_NetBeansPlatform"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        platformPanel.add(platform, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(managePlafsButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_ManagePlatform"));
        managePlafsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                managePlatforms(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        platformPanel.add(managePlafsButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(platformPanel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    private void managePlatforms(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_managePlatforms
        NbPlatformCustomizer.showCustomizer();
        platformValue.setModel(new org.netbeans.modules.apisupport.project.ui.platform.ComponentFactory.NbPlatformListModel()); // refresh
    }//GEN-LAST:event_managePlatforms
    
    private void editModuleDependency(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editModuleDependency
        ModuleDependency origDep = moduleDeps.getDependency(
                dependencyList.getSelectedIndex());
        ModuleDependency editedDep = moduleDeps.findEdited(origDep);
        EditDependencyPanel editPanel = new EditDependencyPanel(
                editedDep == null ? origDep : editedDep);
        DialogDescriptor descriptor = new DialogDescriptor(editPanel,
                NbBundle.getMessage(CustomizerLibraries.class,
                "CTL_EditModuleDependencyTitle")); // NOI18N
        Dialog d = DialogDisplayer.getDefault().createDialog(descriptor);
        d.setVisible(true);
        if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
            moduleDeps.editDependency(origDep, editPanel.getEditedDependency());
        }
        d.dispose();
    }//GEN-LAST:event_editModuleDependency
    
    private void removeModuleDependency(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeModuleDependency
        moduleDeps.removeDependencies(Arrays.asList(dependencyList.getSelectedValues()));
        dependencyList.clearSelection();
    }//GEN-LAST:event_removeModuleDependency
    
    private void addModuleDependency(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addModuleDependency
        Set depsToAdd = new TreeSet(universeModulesModel.getDependencies());
        depsToAdd.removeAll(moduleDeps.getDependencies());
        ComponentFactory.DependencyListModel model =
                ComponentFactory.createDependencyListModel(depsToAdd);
        final AddModulePanel addPanel = new AddModulePanel(model);
        final DialogDescriptor descriptor = new DialogDescriptor(addPanel,
                NbBundle.getMessage(CustomizerLibraries.class,
                "CTL_AddModuleDependencyTitle"));// NOI18N
        descriptor.setClosingOptions(new Object[0]);
        final Dialog d = DialogDisplayer.getDefault().createDialog(descriptor);
        descriptor.setButtonListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (DialogDescriptor.OK_OPTION.equals(e.getSource()) &&
                        addPanel.getSelectedDependency() == null) {
                    return;
                }
                d.setVisible(false);
                d.dispose();
            }
        });
        d.setVisible(true);
        if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
            ModuleDependency newDep = addPanel.getSelectedDependency();
            moduleDeps.addDependency(newDep);
            dependencyList.requestFocus();
            dependencyList.setSelectedValue(newDep, true);
        }
        d.dispose();
    }//GEN-LAST:event_addModuleDependency
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addDepButton;
    private javax.swing.JPanel depButtonPanel;
    private javax.swing.JList dependencyList;
    private javax.swing.JScrollPane dependencySP;
    private javax.swing.JButton editDepButton;
    private javax.swing.JButton managePlafsButton;
    private javax.swing.JLabel modDepLabel;
    private javax.swing.JLabel platform;
    private javax.swing.JPanel platformPanel;
    private javax.swing.JComboBox platformValue;
    private javax.swing.JButton removeDepButton;
    private javax.swing.JLabel space1;
    // End of variables declaration//GEN-END:variables
    
}
