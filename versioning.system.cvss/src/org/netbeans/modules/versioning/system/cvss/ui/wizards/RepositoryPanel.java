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

package org.netbeans.modules.versioning.system.cvss.ui.wizards;

/**
 * UI for remote CVS repository selection. Components
 * are dynamically hidden. 
 *
 * @author  Petr Kuzel
 */
final class RepositoryPanel extends javax.swing.JPanel {
    
    /** Creates new form ProxyPanel */
    public RepositoryPanel() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        setLayout(new java.awt.GridBagLayout());

        setName(org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "BK0006"));
        org.openide.awt.Mnemonics.setLocalizedText(headerLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/wizards/Bundle").getString("BK0001"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        add(headerLabel, gridBagConstraints);

        rootsLabel.setLabelFor(rootComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(rootsLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/wizards/Bundle").getString("BK0002"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 3);
        add(rootsLabel, gridBagConstraints);

        rootComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(rootComboBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(editButton, org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "BK1105"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 0);
        add(editButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(descLabel, "(:pserver:username@host:/repositoryPath)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 9, 0);
        add(descLabel, gridBagConstraints);

        pPaswordLabel.setLabelFor(passwordTextField);
        org.openide.awt.Mnemonics.setLocalizedText(pPaswordLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/wizards/Bundle").getString("BK0003"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 3);
        add(pPaswordLabel, gridBagConstraints);

        passwordTextField.setColumns(12);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(passwordTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        add(jPanel2, gridBagConstraints);

        sshButtonGroup.add(internalSshRadioButton);
        internalSshRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(internalSshRadioButton, org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "BK1100"));
        internalSshRadioButton.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        internalSshRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        add(internalSshRadioButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(extPasswordLabel5, org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "BK1011"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 3, 3);
        add(extPasswordLabel5, gridBagConstraints);

        extPasswordField.setColumns(12);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(extPasswordField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(extREmemberPasswordCheckBox, org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "BK1012"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 0);
        add(extREmemberPasswordCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(proxyConfigurationButton, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/wizards/Bundle").getString("BK0005"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 0, 0);
        add(proxyConfigurationButton, gridBagConstraints);

        sshButtonGroup.add(extSshRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(extSshRadioButton, org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "BK1101"));
        extSshRadioButton.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        extSshRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        add(extSshRadioButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(extCommandLabel, org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "BK1013"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 3, 3);
        add(extCommandLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 0);
        add(extCommandTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 10000.0;
        add(jPanel1, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JLabel descLabel = new javax.swing.JLabel();
    final javax.swing.JButton editButton = new javax.swing.JButton();
    final javax.swing.JLabel extCommandLabel = new javax.swing.JLabel();
    final javax.swing.JTextField extCommandTextField = new javax.swing.JTextField();
    final javax.swing.JPasswordField extPasswordField = new javax.swing.JPasswordField();
    final javax.swing.JLabel extPasswordLabel5 = new javax.swing.JLabel();
    final javax.swing.JCheckBox extREmemberPasswordCheckBox = new javax.swing.JCheckBox();
    final javax.swing.JRadioButton extSshRadioButton = new javax.swing.JRadioButton();
    final javax.swing.JLabel headerLabel = new javax.swing.JLabel();
    final javax.swing.JRadioButton internalSshRadioButton = new javax.swing.JRadioButton();
    final javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
    final javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
    final javax.swing.JLabel pPaswordLabel = new javax.swing.JLabel();
    final javax.swing.JPasswordField passwordTextField = new javax.swing.JPasswordField();
    final javax.swing.JButton proxyConfigurationButton = new javax.swing.JButton();
    final javax.swing.JComboBox rootComboBox = new javax.swing.JComboBox();
    final javax.swing.JLabel rootsLabel = new javax.swing.JLabel();
    final javax.swing.ButtonGroup sshButtonGroup = new javax.swing.ButtonGroup();
    // End of variables declaration//GEN-END:variables
    
}
