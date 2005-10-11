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
        getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/wizards/Bundle").getString("ACSD_RepositoryStep"));
        org.openide.awt.Mnemonics.setLocalizedText(headerLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/wizards/Bundle").getString("BK0001"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        add(headerLabel, gridBagConstraints);

        rootsLabel.setLabelFor(rootComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(rootsLabel, org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "BK0002"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 3);
        add(rootsLabel, gridBagConstraints);

        jPanel3.setLayout(new java.awt.BorderLayout(6, 0));

        rootComboBox.setEditable(true);
        jPanel3.add(rootComboBox, java.awt.BorderLayout.CENTER);

        org.openide.awt.Mnemonics.setLocalizedText(editButton, org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "BK1105"));
        editButton.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/wizards/Bundle").getString("TT_EditFields"));
        jPanel3.add(editButton, java.awt.BorderLayout.EAST);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 0);
        add(jPanel3, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(descLabel, org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "BK2018"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 9, 0);
        add(descLabel, gridBagConstraints);

        pPaswordLabel.setLabelFor(passwordTextField);
        org.openide.awt.Mnemonics.setLocalizedText(pPaswordLabel, org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "BK0003"));
        pPaswordLabel.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/wizards/Bundle").getString("TT_PserverPassword"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 3);
        add(pPaswordLabel, gridBagConstraints);

        passwordTextField.setColumns(12);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(passwordTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        add(jPanel2, gridBagConstraints);

        sshButtonGroup.add(internalSshRadioButton);
        internalSshRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(internalSshRadioButton, org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "BK1100"));
        internalSshRadioButton.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/wizards/Bundle").getString("TT_UseInternalSSH"));
        internalSshRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        internalSshRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        add(internalSshRadioButton, gridBagConstraints);

        extPasswordLabel5.setLabelFor(extPasswordField);
        org.openide.awt.Mnemonics.setLocalizedText(extPasswordLabel5, org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "BK1011"));
        extPasswordLabel5.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/wizards/Bundle").getString("TT_SSHPassword"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 3, 3);
        add(extPasswordLabel5, gridBagConstraints);

        extPasswordField.setColumns(12);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(extPasswordField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(extREmemberPasswordCheckBox, org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "BK1012"));
        extREmemberPasswordCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/wizards/Bundle").getString("TT_RememberPassword"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 0);
        add(extREmemberPasswordCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(proxyConfigurationButton, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/wizards/Bundle").getString("BK0005"));
        proxyConfigurationButton.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/wizards/Bundle").getString("TT_ProxyConfig"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 0, 0);
        add(proxyConfigurationButton, gridBagConstraints);

        sshButtonGroup.add(extSshRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(extSshRadioButton, org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "BK1101"));
        extSshRadioButton.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/wizards/Bundle").getString("TT_UseExternal"));
        extSshRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        extSshRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        add(extSshRadioButton, gridBagConstraints);

        extCommandLabel.setLabelFor(extCommandTextField);
        org.openide.awt.Mnemonics.setLocalizedText(extCommandLabel, org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "BK1013"));
        extCommandLabel.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/wizards/Bundle").getString("TT_ExternalCommand"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 3, 3);
        add(extCommandLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
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
    final javax.swing.JPanel jPanel3 = new javax.swing.JPanel();
    final javax.swing.JLabel pPaswordLabel = new javax.swing.JLabel();
    final javax.swing.JPasswordField passwordTextField = new javax.swing.JPasswordField();
    final javax.swing.JButton proxyConfigurationButton = new javax.swing.JButton();
    final javax.swing.JComboBox rootComboBox = new javax.swing.JComboBox();
    final javax.swing.JLabel rootsLabel = new javax.swing.JLabel();
    final javax.swing.ButtonGroup sshButtonGroup = new javax.swing.ButtonGroup();
    // End of variables declaration//GEN-END:variables
    
}
