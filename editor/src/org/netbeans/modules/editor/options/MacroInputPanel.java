/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.options;

import java.awt.Dimension;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;

/**
 * Input panel for pair of strings, one inline and one in editor
 *
 * @author  Petr Nejedly
 */

public class MacroInputPanel extends javax.swing.JPanel {

    private static ResourceBundle bundle = NbBundle.getBundle( MacroInputPanel.class );

    /** Creates new form MacrosInputPanel */
    public MacroInputPanel() {
        initComponents ();
        Dimension dim = getPreferredSize();
        dim.width = 4*dim.width;
        dim.height = 4*dim.height;
        setPreferredSize( dim );
    }

    public void setMacro( String[] macro ) {
        macroField.setText( macro[0] );
        expandTextArea.setText( macro[1] );
    }

    public String[] getMacro() {
        String[] retVal = { macroField.getText(), expandTextArea.getText() };
        return retVal;
    }


    private void initComponents() {//GEN-BEGIN:initComponents
        macroLabel = new javax.swing.JLabel();
        macroField = new javax.swing.JTextField();
        expandLabel = new javax.swing.JLabel();
        expandScrollPane = new javax.swing.JScrollPane();
        expandTextArea = new javax.swing.JTextArea();
        
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)));
        macroLabel.setText(bundle.getString( "MIP_Macro" ));
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 8, 8);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHEAST;
        add(macroLabel, gridBagConstraints1);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 8, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(macroField, gridBagConstraints1);
        
        expandLabel.setText(bundle.getString( "MIP_Expand" ));
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 0, 8);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHEAST;
        add(expandLabel, gridBagConstraints1);
        
        expandScrollPane.setViewportView(expandTextArea);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.gridheight = 2;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(expandScrollPane, gridBagConstraints1);
        
    }//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel macroLabel;
    private javax.swing.JTextField macroField;
    private javax.swing.JLabel expandLabel;
    private javax.swing.JScrollPane expandScrollPane;
    private javax.swing.JTextArea expandTextArea;
    // End of variables declaration//GEN-END:variables

}
