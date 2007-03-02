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
package org.netbeans.modules.visualweb.propertyeditors.css;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.openide.util.NbBundle;

/**
 * List Style editor.  
 * @author  Winston Prakash
 *          Jeff Hoffman (HIE design)
 */
public class ListStyleEditor extends StyleEditor {
    
    /** Creates new form FontStyleEditor */
    public ListStyleEditor() {
        setName("listStyleEditor"); //NOI18N
        setDisplayName(NbBundle.getMessage(StyleBuilderDialog.class, "LIST_EDITOR_DISPNAME")); 
        initComponents();
    }
    
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        previewPanel = new javax.swing.JPanel();
        previewText = new javax.swing.JLabel();
        colorPanel = new javax.swing.JPanel();
        decorationLabel = new javax.swing.JLabel();
        listTypeCombo = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jComboBox2 = new javax.swing.JComboBox();
        colorButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        previewPanel.setBorder(new javax.swing.border.TitledBorder("Preview"));
        previewPanel.setName("");
        previewText.setText("<preview of the list with a couple items>");
        previewPanel.add(previewText);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        add(previewPanel, gridBagConstraints);

        colorPanel.setLayout(new java.awt.GridBagLayout());

        decorationLabel.setText("Bullet Icon:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        colorPanel.add(decorationLabel, gridBagConstraints);

        listTypeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<Not Set>", "disc", "circle", "square", "decimal (1, 2, 3...)", "lower-roman (i, ii, iii...)", "upper-roman (I, II, III...)", "lower-alpha (a, b, c...)", "upper-alpha (A, B, C...)", "none" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        colorPanel.add(listTypeCombo, gridBagConstraints);

        jLabel1.setText("List Type:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        colorPanel.add(jLabel1, gridBagConstraints);

        jLabel2.setText("Bullet Position");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 10);
        colorPanel.add(jLabel2, gridBagConstraints);

        jComboBox1.setEditable(true);
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<Not Set>", "<URL>", "none" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        colorPanel.add(jComboBox1, gridBagConstraints);

        jComboBox2.setEditable(true);
        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<Not Set>", "inside", "outside" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        colorPanel.add(jComboBox2, gridBagConstraints);

        colorButton.setText("...");
        colorButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        colorPanel.add(colorButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        add(colorPanel, gridBagConstraints);

    }//GEN-END:initComponents
   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton colorButton;
    private javax.swing.JPanel colorPanel;
    private javax.swing.JLabel decorationLabel;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JComboBox listTypeCombo;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JLabel previewText;
    // End of variables declaration//GEN-END:variables
    
}
