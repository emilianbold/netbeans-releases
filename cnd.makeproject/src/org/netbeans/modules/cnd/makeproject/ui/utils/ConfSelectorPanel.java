/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.makeproject.ui.utils;

import java.awt.GridBagConstraints;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.openide.util.NbBundle;

public class ConfSelectorPanel extends javax.swing.JPanel {

    private static Configuration[] lastConfigurationItems;
    private static JCheckBox[] lastCheckBoxes;
    private Configuration[] configurationItems;
    private JCheckBox[] checkBoxes;

    JButton[] actionButtons;

    public ConfSelectorPanel(String labelText, char mn, Configuration[] configurationItems, JButton[] actionButtons) {
        initComponents();
        GridBagConstraints gridBagConstraints;
        
        this.configurationItems = configurationItems;
        this.actionButtons = actionButtons;
        
        // Set the label
        label.setText(labelText);
        label.setDisplayedMnemonic(mn);
        
        // Add the comboboxes
        CheckBoxActionListener checkBoxActionListener = new CheckBoxActionListener();
        checkBoxes = new JCheckBox[configurationItems.length];
        for (int i = 0; i < configurationItems.length; i++) {
            JCheckBox checkBox = new JCheckBox();
            checkBox.addActionListener(checkBoxActionListener);
            checkBox.setBackground(new java.awt.Color(255, 255, 255));
            checkBox.setText(configurationItems[i].toString());
            if (sameAsLastTime(configurationItems))
                checkBox.setSelected(lastCheckBoxes[i].isSelected());
            else
                checkBox.setSelected(true);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = i;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
            if (i == configurationItems.length-1) {
                gridBagConstraints.weightx = 1.0;
                gridBagConstraints.weighty = 1.0;
            }
            innerPanel.add(checkBox, gridBagConstraints);
            checkBoxes[i] = checkBox;
            checkBox.getAccessibleContext().setAccessibleDescription(""); // NOI18N
        }
        
        // Add the action buttons
        if (actionButtons != null) {
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
            for (int i = 0; i < actionButtons.length; i++) {
                gridBagConstraints.gridy++;
                buttonPanel.add(actionButtons[i], gridBagConstraints);
            }
        }
        
        // Set background
        innerPanel.setBackground(new java.awt.Color(255, 255, 255));
        
        // Set size
        setPreferredSize(new java.awt.Dimension(350, 250));
        
        // Accessibility
        getAccessibleContext().setAccessibleDescription(getString("SELECTED_CONF_AD"));
        label.setDisplayedMnemonic(getString("SELECTED_CONF_MN").charAt(0));
        selectAllButton.getAccessibleContext().setAccessibleDescription(getString("SELECT_ALL_BUTTON_AD"));
        deselectAllButton.getAccessibleContext().setAccessibleDescription(getString("DESELECT_ALL_BUTTON_AD"));
    }

    private boolean sameAsLastTime(Configuration[] configurationItems) {
	if (lastConfigurationItems == null || lastCheckBoxes == null)
	    return false;
	if (configurationItems.length != lastConfigurationItems.length)
	    return false;
	if (configurationItems.length != lastCheckBoxes.length)
	    return false;
	for (int i = 0; i < configurationItems.length; i++) {
	    if (configurationItems[i] != lastConfigurationItems[i])
		return false;
	}
	return true;
    }
    
    class CheckBoxActionListener implements java.awt.event.ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            checkCheckBoxes();
        }
    }
    
    public void checkCheckBoxes() {
        boolean oneSelected = false;
        for (int i = 0; i < checkBoxes.length; i++) {
            if (checkBoxes[i].isSelected()) {
                oneSelected = true;
                break;
            }
        }
        if (actionButtons != null) {
            for (int i = 0; i < actionButtons.length; i++)
                actionButtons[i].setEnabled(oneSelected);
        }
    }
    
    public Configuration[] getSelectedConfs() {
        lastConfigurationItems = configurationItems;
        lastCheckBoxes = checkBoxes;
        
        Vector vector = new Vector();
        for (int i = 0; i < configurationItems.length; i++) {
            if (checkBoxes[i].isSelected())
                vector.add(configurationItems[i]);
        }
        
        return (Configuration[])vector.toArray(new Configuration[vector.size()]);
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        label = new javax.swing.JLabel();
        confPanel = new javax.swing.JPanel();
        scrollPanel = new javax.swing.JScrollPane();
        innerPanel = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        selectAllButton = new javax.swing.JButton();
        deselectAllButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        label.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/utils/Bundle").getString("SELECTED_CONF_MN").charAt(0));
        label.setLabelFor(innerPanel);
        label.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/utils/Bundle").getString("SELECTED_CONF_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 2, 0);
        add(label, gridBagConstraints);

        confPanel.setLayout(new java.awt.GridBagLayout());

        innerPanel.setLayout(new java.awt.GridBagLayout());

        innerPanel.setBackground(new java.awt.Color(255, 255, 255));
        scrollPanel.setViewportView(innerPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        confPanel.add(scrollPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(confPanel, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        selectAllButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/utils/Bundle").getString("SELECT_ALL_BUTTON_MN").charAt(0));
        selectAllButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/utils/Bundle").getString("SELECT_ALL_BUTTON_TXT"));
        selectAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        buttonPanel.add(selectAllButton, gridBagConstraints);

        deselectAllButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/utils/Bundle").getString("DESELECT_ALL_BUTTON_MN").charAt(0));
        deselectAllButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/utils/Bundle").getString("DESELECT_ALL_BUTTON_TXT"));
        deselectAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deselectAllButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        buttonPanel.add(deselectAllButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 12);
        add(buttonPanel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    private void deselectAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deselectAllButtonActionPerformed
        for (int i = 0; i < checkBoxes.length; i++) {
            checkBoxes[i].setSelected(false);
        }
        checkCheckBoxes();
    }//GEN-LAST:event_deselectAllButtonActionPerformed
    
    private void selectAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllButtonActionPerformed
        for (int i = 0; i < checkBoxes.length; i++) {
            checkBoxes[i].setSelected(true);
        }
        checkCheckBoxes();
    }//GEN-LAST:event_selectAllButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JPanel confPanel;
    private javax.swing.JButton deselectAllButton;
    private javax.swing.JPanel innerPanel;
    private javax.swing.JLabel label;
    private javax.swing.JScrollPane scrollPanel;
    private javax.swing.JButton selectAllButton;
    // End of variables declaration//GEN-END:variables
    
    /** Look up i18n strings here */
    private static ResourceBundle bundle;
    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(ConfSelectorPanel.class);
        }
        return bundle.getString(s);
    }
}
