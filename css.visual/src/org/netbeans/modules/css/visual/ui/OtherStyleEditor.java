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

/*
 * OtherStyleEditor.java
 *
 * Created on October 13, 2004, 12:23 PM
 */

package org.netbeans.modules.css.visual.ui;

import org.netbeans.modules.css.visual.model.CssStyleData;
import org.openide.util.NbBundle;

/**
 * Position Style editor.
 * @author  Winston Prakash
 * @version 1.0
 */
public class OtherStyleEditor extends StyleEditor {

    /** Creates new form FontStyleEditor */
    public OtherStyleEditor() {
        setName("otherStyleEditor"); //NOI18N
        setDisplayName(NbBundle.getMessage(OtherStyleEditor.class, "OTHER_EDITOR_DISPNAME"));
        initComponents();
    }

    /**
     * Set the CSS Properties Values from the CssStyleData data structure
     * to the GUI components.
     */
    protected void setCssPropertyValues(CssStyleData cssStyleData){
        removeCssPropertyChangeListener();
        // Set the values here
        setCssPropertyChangeListener(cssStyleData);
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        colorPanel = new javax.swing.JPanel();
        filterLabel = new javax.swing.JLabel();
        cursorComboBox = new javax.swing.JComboBox();
        filterComboBox = new javax.swing.JComboBox();
        cursorLabel = new javax.swing.JLabel();
        previewPanel = new javax.swing.JPanel();
        previewText = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        colorPanel.setLayout(new java.awt.GridBagLayout());

        filterLabel.setText("Visual Filter:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 10);
        colorPanel.add(filterLabel, gridBagConstraints);

        cursorComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<Not Set>", "hand", "crosshair", "text", "wait", "default", "help", "n-resize", "s-resize", "e-resize", "w-resize", "ne-resize", "nw-resize", "se-resize", "sw-resize" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        colorPanel.add(cursorComboBox, gridBagConstraints);

        filterComboBox.setEditable(true);
        filterComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<Not Set>", "<value>" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        colorPanel.add(filterComboBox, gridBagConstraints);

        cursorLabel.setText("Cursor:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        colorPanel.add(cursorLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        add(colorPanel, gridBagConstraints);

        previewPanel.setBorder(new javax.swing.border.TitledBorder("Preview"));
        previewPanel.setName("");
        previewText.setText("<preview of cursor and/or filter>");
        previewPanel.add(previewText);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        add(previewPanel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel colorPanel;
    private javax.swing.JComboBox cursorComboBox;
    private javax.swing.JLabel cursorLabel;
    private javax.swing.JComboBox filterComboBox;
    private javax.swing.JLabel filterLabel;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JLabel previewText;
    // End of variables declaration//GEN-END:variables
    
}
