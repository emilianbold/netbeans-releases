/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project.ui;

/**
 *
 * @author  mkuchtiak
 */
public class ServletUriPanel extends javax.swing.JPanel {

    /** Creates new form ServletUriPanel */
    public ServletUriPanel(String[] urlPatterns, String selectedItem, boolean fromRunMenu ) {
        initComponents();
        if (fromRunMenu) {
            org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ServletUriPanel.class, "DESC_setServletURI"));
        }
        if (selectedItem!=null) {
            jComboBox1.addItem(selectedItem);
        }
        for (int i=0;i<urlPatterns.length;i++) {
            if (!urlPatterns[i].equals(selectedItem))
                jComboBox1.addItem(urlPatterns[i]);
        }
        if (selectedItem!=null) {
            ((javax.swing.JTextField)jComboBox1.getEditor().getEditorComponent()).setText(selectedItem);
        }
    }
    
    public String getServletUri() {
        return (String)jComboBox1.getSelectedItem();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jComboBox1 = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jComboBox1.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 12);
        gridBagConstraints.weightx = 1.0;
        add(jComboBox1, gridBagConstraints);

        jLabel1.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ServletUriPanel.class, "LBL_setServletURI_mnem").charAt(0));
        jLabel1.setLabelFor(jComboBox1);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ServletUriPanel.class, "LBL_setServletURI"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel2, gridBagConstraints);

    }//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    // End of variables declaration//GEN-END:variables
    
}
