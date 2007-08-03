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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.ui.options;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platforms;
import org.netbeans.modules.cnd.settings.CppSettings;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;

/**
 * Replaces the old project system options panel.
 */
public class ProjectOptionsPanel extends JPanel {
    
    private boolean changed;
    private boolean listen = false;
    
    private ArrayList propertyChangeListeners = new ArrayList();
    
    private DocumentListener documentListener;
    
    /** Creates new form ProjectOptionsPanel */
    public ProjectOptionsPanel() {
        initComponents();
        // Accessible Description
        reuseCheckBox.getAccessibleContext().setAccessibleDescription(getString("REUSE_CHECKBOX_AD"));
        saveCheckBox.getAccessibleContext().setAccessibleDescription(getString("SAVE_CHECKBOX_AD"));
        platformComboBox.getAccessibleContext().setAccessibleDescription(getString("DEFAULT_PLATFORM_AD"));
        filePathcomboBox.getAccessibleContext().setAccessibleDescription(getString("FILE_PATH_AD"));
        makeOptionsTextField.getAccessibleContext().setAccessibleDescription(getString("MAKE_OPTIONS_AD"));
        
        
        documentListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                validateFields();
            }
            
            public void removeUpdate(DocumentEvent e) {
                validateFields();
            }
            
            public void changedUpdate(DocumentEvent e) {
                validateFields();
            }
        };
        
        makeOptionsTextField.getDocument().addDocumentListener(documentListener);
        setName("TAB_ProjectsTab"); // NOI18N (used as a pattern...)
    }
    
    public void update() {
        listen = false;
        MakeOptions makeOptions = MakeOptions.getInstance();
        
        // Platform
        platformComboBox.removeAllItems();
        for (int i = 0; i < Platforms.getPlatformDisplayNames().length; i++) {
            platformComboBox.addItem(Platforms.getPlatformDisplayNames()[i]);
        }
        platformComboBox.setSelectedIndex(makeOptions.getPlatform());
        
        // Dependency Checking
        dependencyCheckingCheckBox.setSelected(makeOptions.getDepencyChecking());
        
        // Make Command
        makeOptionsTextField.setText(makeOptions.getMakeOptions());
        
        // Path Mode
        filePathcomboBox.removeAllItems();
        for (int i = 0; i < MakeOptions.PathModeNames.length; i++) {
            filePathcomboBox.addItem(MakeOptions.PathModeNames[i]);
        }
        filePathcomboBox.setSelectedIndex(makeOptions.getPathMode());
        
        // Save
        saveCheckBox.setSelected(makeOptions.getSave());
        
        // Reuse
        reuseCheckBox.setSelected(makeOptions.getReuse());
        
        listen = true;
        changed = false;
    }
    
    /** Apply changes */
    public void applyChanges() {
        
        MakeOptions makeOptions = MakeOptions.getInstance();
        
        // Platform
        makeOptions.setPlatform(platformComboBox.getSelectedIndex());
        
        // Dependency Checking
        makeOptions.setDepencyChecking(dependencyCheckingCheckBox.isSelected());
        
        // Make Command
        makeOptions.setMakeOptions(makeOptionsTextField.getText());
        
        // Path Mode
        makeOptions.setPathMode(filePathcomboBox.getSelectedIndex());
        
        // Save
        makeOptions.setSave(saveCheckBox.isSelected());
        
        // Reuse
        makeOptions.setReuse(reuseCheckBox.isSelected());
        
        changed = false;
    }
    
    /** What to do if user cancels the dialog (nothing) */
    public void cancel() {
        changed = false;
    }
    
    /**
     * Lets NB know if the data in the panel is valid and OK should be enabled
     * 
     * @return Returns true if all data is valid
     */
    public boolean dataValid() {
        return true;
    }
    
    /**
     * Lets caller know if any data has been changed.
     * 
     * @return True if anything has been changed
     */
    public boolean isChanged() {
        return changed;
    }
    
    private void validateFields() {
        PropertyChangeEvent pce = new PropertyChangeEvent(this, OptionsPanelController.PROP_VALID, this, this);
        firePropertyChange(pce);
    }
    
    public void firePropertyChange(PropertyChangeEvent evt) {
        PropertyChangeListener[] listeners = (PropertyChangeListener[])propertyChangeListeners.toArray(new PropertyChangeListener[propertyChangeListeners.size()]);
        for (int i = 0; i < listeners.length; i++)
            listeners[i].propertyChange(evt);
    }
    
    private static String getString(String key) {
        return NbBundle.getMessage(ProjectOptionsPanel.class, key);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        platformLabel = new javax.swing.JLabel();
        platformComboBox = new javax.swing.JComboBox();
        defaultPlatformInfoLabel = new javax.swing.JLabel();
        makeOptionsLabel = new javax.swing.JLabel();
        makeOptionsTextField = new javax.swing.JTextField();
        makeOptionsTxt = new javax.swing.JLabel();
        filePathLabel = new javax.swing.JLabel();
        filePathcomboBox = new javax.swing.JComboBox();
        filePathTxt = new javax.swing.JTextArea();
        filePathTxt.setBackground(getBackground());
        saveCheckBox = new javax.swing.JCheckBox();
        reuseCheckBox = new javax.swing.JCheckBox();
        dependencyCheckingCheckBox = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();

        setLayout(new java.awt.GridBagLayout());

        platformLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle").getString("DEFAULT_PLATFORM_MN").charAt(0));
        platformLabel.setLabelFor(platformComboBox);
        platformLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle").getString("DEFAULT_PLATFORM"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(platformLabel, gridBagConstraints);

        platformComboBox.setMinimumSize(new java.awt.Dimension(60, 18));
        platformComboBox.setPreferredSize(new java.awt.Dimension(60, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(platformComboBox, gridBagConstraints);

        defaultPlatformInfoLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle").getString("USED_TXT"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 12);
        add(defaultPlatformInfoLabel, gridBagConstraints);

        makeOptionsLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle").getString("MAKE_OPTIONS_MN").charAt(0));
        makeOptionsLabel.setLabelFor(makeOptionsTextField);
        makeOptionsLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle").getString("MAKE_OPTIONS"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(makeOptionsLabel, gridBagConstraints);

        makeOptionsTextField.setColumns(45);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 12);
        add(makeOptionsTextField, gridBagConstraints);

        makeOptionsTxt.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle").getString("MAKE_OPTIONS_TXT"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 0, 12);
        add(makeOptionsTxt, gridBagConstraints);

        filePathLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle").getString("FILE_PATH_MN").charAt(0));
        filePathLabel.setLabelFor(filePathcomboBox);
        filePathLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle").getString("FILE_PATH"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(filePathLabel, gridBagConstraints);

        filePathcomboBox.setMinimumSize(new java.awt.Dimension(75, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 4, 0, 12);
        add(filePathcomboBox, gridBagConstraints);

        filePathTxt.setEditable(false);
        filePathTxt.setLineWrap(true);
        filePathTxt.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle").getString("FILE_PATH_MODE_TXT"));
        filePathTxt.setWrapStyleWord(true);
        filePathTxt.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 10.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(filePathTxt, gridBagConstraints);

        saveCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle").getString("SAVE_CHECKBOX_MN").charAt(0));
        saveCheckBox.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle").getString("SAVE_CHECKBOX_TXT"));
        saveCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        saveCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 12);
        add(saveCheckBox, gridBagConstraints);

        reuseCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle").getString("REUSE_CHECKBOX_MN").charAt(0));
        reuseCheckBox.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle").getString("REUSE_CHECKBOX_TXT"));
        reuseCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        reuseCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 12);
        add(reuseCheckBox, gridBagConstraints);

        dependencyCheckingCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle").getString("DEPENDENCY_CHECKING_MN").charAt(0));
        dependencyCheckingCheckBox.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle").getString("DEPENDENCY_CHECKING_TXT"));
        dependencyCheckingCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        dependencyCheckingCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        dependencyCheckingCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dependencyCheckingCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 11);
        add(dependencyCheckingCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jSeparator1, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void dependencyCheckingCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dependencyCheckingCheckBoxActionPerformed
// TODO add your handling code here:
        
        PropertyChangeEvent pce = new PropertyChangeEvent(this, OptionsPanelController.PROP_VALID, this, this);
        firePropertyChange(pce);
//        pce = new PropertyChangeEvent(this, "buran" + OptionsPanelController.PROP_VALID, this, this);
//        firePropertyChange(pce);
    }//GEN-LAST:event_dependencyCheckingCheckBoxActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel defaultPlatformInfoLabel;
    private javax.swing.JCheckBox dependencyCheckingCheckBox;
    private javax.swing.JLabel filePathLabel;
    private javax.swing.JTextArea filePathTxt;
    private javax.swing.JComboBox filePathcomboBox;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel makeOptionsLabel;
    private javax.swing.JTextField makeOptionsTextField;
    private javax.swing.JLabel makeOptionsTxt;
    private javax.swing.JComboBox platformComboBox;
    private javax.swing.JLabel platformLabel;
    private javax.swing.JCheckBox reuseCheckBox;
    private javax.swing.JCheckBox saveCheckBox;
    // End of variables declaration//GEN-END:variables
    
}
