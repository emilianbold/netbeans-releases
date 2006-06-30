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

package org.netbeans.modules.editor.options;

import java.awt.Dimension;
import org.openide.util.NbBundle;

/**
 * Input panel for pair of strings, one inline and one in editor
 *
 * @author  Petr Nejedly
 */

public class MacroInputPanel extends javax.swing.JPanel {

    /** Creates new form MacrosInputPanel */
    public MacroInputPanel() {
        initComponents ();
        getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_MIP")); // NOI18N
        macroLabel.setDisplayedMnemonic(getBundleString("MIP_Macro_Mnemonic").charAt(0)); // NOI18N
        expandLabel.setDisplayedMnemonic(getBundleString("MIP_Expand_Mnemonic").charAt(0)); // NOI18N
        macroField.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_MIP_Macro")); // NOI18N
        expandTextArea.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_MIP_Expand")); // NOI18N
        Dimension dim = getPreferredSize();
        dim.width = 4*dim.width;
        dim.height = 4*dim.height;
        setPreferredSize( dim );
    }
    
    private String getBundleString(String s) {
        return NbBundle.getMessage(MacroInputPanel.class, s);
    }    

    public void setMacro( String[] macro ) {
        macroField.setText( macro[0] );
        expandTextArea.setText( macro[1] );
    }

    public String[] getMacro() {
        String[] retVal = { macroField.getText(), expandTextArea.getText() };
        return retVal;
    }
    
    public void requestFocus(){
        macroField.requestFocus();
    }


    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        macroLabel = new javax.swing.JLabel();
        macroField = new javax.swing.JTextField();
        expandLabel = new javax.swing.JLabel();
        expandScrollPane = new javax.swing.JScrollPane();
        expandTextArea = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 11, 11)));
        macroLabel.setLabelFor(macroField);
        macroLabel.setText(getBundleString( "MIP_Macro" )); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 12);
        add(macroLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(macroField, gridBagConstraints);

        expandLabel.setLabelFor(expandTextArea);
        expandLabel.setText(getBundleString( "MIP_Expand" )); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 12);
        add(expandLabel, gridBagConstraints);

        expandScrollPane.setViewportView(expandTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(expandScrollPane, gridBagConstraints);

    }//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea expandTextArea;
    private javax.swing.JLabel macroLabel;
    private javax.swing.JTextField macroField;
    private javax.swing.JScrollPane expandScrollPane;
    private javax.swing.JLabel expandLabel;
    // End of variables declaration//GEN-END:variables

}
