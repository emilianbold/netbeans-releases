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

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.util.Iterator;
import java.util.SortedSet;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.NbBundle;

/**
 * Represents <em>Display</em> panel in Netbeans Module customizer.
 *
 * @author mkrauskopf
 */
final class CustomizerDisplay extends NbPropertyPanel.Single {
    
    private boolean noBundle;
    private ProjectCustomizer.Category cat;
    private boolean showInPluginManagerCheckboxChanged;
    
    /** Creates new form CustomizerDisplay */
    CustomizerDisplay(final SingleModuleProperties props, ProjectCustomizer.Category cat) {
        super(props, CustomizerDisplay.class);
        initComponents();
        initAccesibility();
        refresh();
        this.cat = cat;
        checkValidity();
    }
    
    void refresh() {
        this.noBundle = getBundle() == null;
        if (noBundle) {
            nameValue.setEnabled(false);
            categoryValue.setEnabled(false);
            shortDescValue.setEnabled(false);
            longDescValue.setEnabled(false);
        } else {
            readFromProperties();
        }
        Boolean autoUpdateShowInClient = ((SingleModuleProperties) props).getAutoUpdateShowInClient();
        if (autoUpdateShowInClient == null) {
            autoUpdateShowInClient = !getBooleanProperty(SingleModuleProperties.IS_AUTOLOAD) && !getBooleanProperty(SingleModuleProperties.IS_EAGER);
        }
        showInPluginManagerCheckbox.setSelected(autoUpdateShowInClient);
        showInPluginManagerCheckboxChanged = false;
    }
    
    private void checkValidity() {
        if (this.noBundle) {
            cat.setErrorMessage(getMessage("MSG_NoBundleForModule"));
        } else {
            cat.setErrorMessage(null);
        }
    }
    
    public void store() {
        if (!noBundle) {
            getBundle().setDisplayName(nameValue.getText());
            getBundle().setCategory(getSelectedCategory());
            getBundle().setShortDescription(shortDescValue.getText());
            getBundle().setLongDescription(longDescValue.getText());
        }
        if (showInPluginManagerCheckboxChanged) {
            ((SingleModuleProperties) props).setAutoUpdateShowInClient(showInPluginManagerCheckbox.isSelected());
        }
    }
    
    private LocalizedBundleInfo getBundle() {
        return getProperties().getBundleInfo();
    }
    
    private void readFromProperties() {
        UIUtil.setText(nameValue, getBundle().getDisplayName());
        UIUtil.setText(shortDescValue, getBundle().getShortDescription());
        longDescValue.setText(getBundle().getLongDescription());
        fillUpCategoryValue();
    }
    
    private void fillUpCategoryValue() {
        categoryValue.setEnabled(false);
        categoryValue.setModel(CustomizerComponentFactory.createComboWaitModel());
        categoryValue.setSelectedItem(CustomizerComponentFactory.WAIT_VALUE);
        ModuleProperties.RP.post(new Runnable() {
            public void run() {
                final SortedSet moduleCategories = getProperties().getModuleCategories();
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        DefaultComboBoxModel model = new DefaultComboBoxModel();
                        categoryValue.removeAllItems();
                        for (Iterator it = moduleCategories.iterator(); it.hasNext(); ) {
                            model.addElement(it.next());
                        }
                        if (!moduleCategories.contains(getCategory())) {
                            // put module's own category at the beginning
                            model.insertElementAt(getCategory(), 0);
                        }
                        categoryValue.setModel(model);
                        categoryValue.setSelectedItem(getCategory());
                        categoryValue.setEnabled(true);
                    }
                });
            }
        });
    }
    
    private String getCategory() {
        LocalizedBundleInfo bundle = getBundle();
        String category = bundle != null ? bundle.getCategory() : null;
        return category != null ? category : ""; // NOI18N
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        if (SingleModuleProperties.NB_PLATFORM_PROPERTY == evt.getPropertyName()) {
            fillUpCategoryValue();
        }
    }
    
    private String getSelectedCategory() {
        String cat = (String) categoryValue.getSelectedItem();
        return CustomizerComponentFactory.WAIT_VALUE == cat ? getCategory() : cat;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        name = new javax.swing.JLabel();
        nameValue = new javax.swing.JTextField();
        category = new javax.swing.JLabel();
        categoryValue = new javax.swing.JComboBox();
        shortDesc = new javax.swing.JLabel();
        shortDescValue = new javax.swing.JTextField();
        longDesc = new javax.swing.JLabel();
        hackPanel = new javax.swing.JPanel();
        longDescValueSP = new javax.swing.JScrollPane();
        longDescValue = new javax.swing.JTextArea();
        showInPluginManagerCheckbox = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        name.setLabelFor(nameValue);
        org.openide.awt.Mnemonics.setLocalizedText(name, org.openide.util.NbBundle.getMessage(CustomizerDisplay.class, "LBL_DisplayName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(name, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(nameValue, gridBagConstraints);

        category.setLabelFor(categoryValue);
        org.openide.awt.Mnemonics.setLocalizedText(category, org.openide.util.NbBundle.getMessage(CustomizerDisplay.class, "LBL_DisplayCategory")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(category, gridBagConstraints);

        categoryValue.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(categoryValue, gridBagConstraints);

        shortDesc.setLabelFor(shortDescValue);
        org.openide.awt.Mnemonics.setLocalizedText(shortDesc, org.openide.util.NbBundle.getMessage(CustomizerDisplay.class, "LBL_ShortDescription")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(shortDesc, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(shortDescValue, gridBagConstraints);

        longDesc.setLabelFor(longDescValue);
        org.openide.awt.Mnemonics.setLocalizedText(longDesc, org.openide.util.NbBundle.getMessage(CustomizerDisplay.class, "LBL_LongDescription")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 6);
        add(longDesc, gridBagConstraints);

        hackPanel.setLayout(new java.awt.BorderLayout());

        longDescValue.setLineWrap(true);
        longDescValue.setRows(10);
        longDescValue.setWrapStyleWord(true);
        longDescValueSP.setViewportView(longDescValue);

        hackPanel.add(longDescValueSP, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(hackPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(showInPluginManagerCheckbox, org.openide.util.NbBundle.getMessage(CustomizerDisplay.class, "CustomizerDisplay.showInPluginManagerCheckbox.text")); // NOI18N
        showInPluginManagerCheckbox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        showInPluginManagerCheckbox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        showInPluginManagerCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showInPluginManagerCheckboxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(showInPluginManagerCheckbox, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void showInPluginManagerCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showInPluginManagerCheckboxActionPerformed
        showInPluginManagerCheckboxChanged = true;
    }//GEN-LAST:event_showInPluginManagerCheckboxActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel category;
    private javax.swing.JComboBox categoryValue;
    private javax.swing.JPanel hackPanel;
    private javax.swing.JLabel longDesc;
    private javax.swing.JTextArea longDescValue;
    private javax.swing.JScrollPane longDescValueSP;
    private javax.swing.JLabel name;
    private javax.swing.JTextField nameValue;
    private javax.swing.JLabel shortDesc;
    private javax.swing.JTextField shortDescValue;
    private javax.swing.JCheckBox showInPluginManagerCheckbox;
    // End of variables declaration//GEN-END:variables
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(CustomizerDisplay.class, key);
    }
    
    private void initAccesibility() {
        longDescValue.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_LongDescValue"));
        nameValue.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_NameValue"));
        shortDescValue.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_ShortDescValue"));
    }
    
}
