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
 * BorderWidthField.java
 *
 * Created on October 22, 2004, 5:08 PM
 */

package org.netbeans.modules.css.visual.ui;

import org.netbeans.modules.css.visual.model.BorderModel;
import org.netbeans.modules.css.visual.model.PropertyWithUnitData;
import org.netbeans.modules.css.visual.model.BorderModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.netbeans.modules.css.visual.model.Utils;

/**
 * Border Width field wiith text field and unit combo box
 * @author  Winston Prakash
 * @version 1.0
 */
public class BorderWidthField extends javax.swing.JPanel {
    PropertyWithUnitData borderWidthData = new PropertyWithUnitData();

    BorderModel borderModel = new BorderModel();

    /** Creates new form BorderWidthField */
    public BorderWidthField() {
        initComponents();
        borderWidthCombo.setModel(borderModel.getWidthList());
        borderWidthUnitCombo.setModel(borderModel.getWidthUnitList());

        // Add editor listeners to the border width combobox
        final JTextField borderWidthComboEditor = (JTextField) borderWidthCombo.getEditor().getEditorComponent();
        borderWidthComboEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        borderWidthUnitCombo.setEnabled(Utils.isInteger(borderWidthComboEditor.getText()));
                    }
                });
            }
        });
    }

    public void setWidthString(String widthStr){
        if((widthStr != null) && !widthStr.equals("")){
            if(Utils.isInteger(widthStr)){
                setWidthValue(widthStr);
            }else{
                String unit = getUnit(widthStr);
                setWidthUnit(unit);
                setWidthValue(widthStr.replaceAll(unit,"").trim());
            }
        }else{
            setWidthValue(null);
            setWidthUnit(null);
        }
    }

    public String getWidthString(){
        return borderWidthData.toString();
    }

    private String getUnit(String widthStr){
        DefaultComboBoxModel unitList = borderModel.getWidthUnitList();
        for(int i=0; i< unitList.getSize(); i++){
            String unit = (String)unitList.getElementAt(i);
            if(widthStr.trim().endsWith(unit)){
                return unit;
            }
        }
        return "";
    }

    public void setWidthValue(String value){
        if((value == null) || value.equals("")){
            borderWidthCombo.setSelectedIndex(0);
        }else{
            borderWidthCombo.setSelectedItem(value);
            borderWidthData.setValue(value);
        }
    }

    public void setWidthUnit(String value){
        if((value == null) || value.equals("")){
            borderWidthUnitCombo.setSelectedIndex(borderModel.getWidthUnitList().getIndexOf("px"));
        }else{
            if(borderModel.getWidthUnitList().getIndexOf(value) != -1){
                borderWidthUnitCombo.setSelectedIndex(borderModel.getWidthUnitList().getIndexOf(value));
            }else{
                borderWidthUnitCombo.setSelectedIndex(borderModel.getWidthUnitList().getIndexOf("px"));
            }
            borderWidthData.setUnit(value);
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        borderWidthCombo = new javax.swing.JComboBox();
        borderWidthUnitCombo = new javax.swing.JComboBox();

        setLayout(new java.awt.BorderLayout(3, 0));

        borderWidthCombo.setEditable(true);
        borderWidthCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                borderWidthComboItemStateChanged(evt);
            }
        });
        borderWidthCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                borderWidthComboActionPerformed(evt);
            }
        });
        borderWidthCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                borderWidthComboFocusLost(evt);
            }
        });

        add(borderWidthCombo, java.awt.BorderLayout.CENTER);
        borderWidthCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BorderWidthField.class, "BORDER_WIDTH_FIELD_ACCESS_NAME"));
        borderWidthCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BorderWidthField.class, "BORDER_WIDTH_FIELD_ACCESS_DESC"));

        borderWidthUnitCombo.setEnabled(false);
        borderWidthUnitCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                borderWidthUnitComboItemStateChanged(evt);
            }
        });
        borderWidthUnitCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                borderWidthUnitComboFocusLost(evt);
            }
        });

        add(borderWidthUnitCombo, java.awt.BorderLayout.EAST);
        borderWidthUnitCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BorderWidthField.class, "BORDER_WIDTH_UNIT_ACCESS_NAME"));
        borderWidthUnitCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BorderWidthField.class, "BORDER_WIDTH_UNIT_ACCESS_DESC"));

    }
    // </editor-fold>//GEN-END:initComponents
    
    private void borderWidthUnitComboFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_borderWidthUnitComboFocusLost
        setBorderWidth();
    }//GEN-LAST:event_borderWidthUnitComboFocusLost
    
    private void borderWidthUnitComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_borderWidthUnitComboItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setBorderWidth();
        }
    }//GEN-LAST:event_borderWidthUnitComboItemStateChanged
    
    // For accessibility
    public void setAccessibleName(String comboName, String unitName){
        borderWidthCombo.getAccessibleContext().setAccessibleName(comboName);
        borderWidthUnitCombo.getAccessibleContext().setAccessibleName(unitName);
    }
    
    // For accessibility
    public void setAccessibleDescription(String comboDesc, String unitDesc){
        borderWidthCombo.getAccessibleContext().setAccessibleDescription(comboDesc);
        borderWidthUnitCombo.getAccessibleContext().setAccessibleDescription(unitDesc);
    }
    
    private void borderWidthComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_borderWidthComboItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setBorderWidth();
            borderWidthUnitCombo.setEnabled(Utils.isInteger(borderWidthCombo.getSelectedItem().toString()));
        }
    }//GEN-LAST:event_borderWidthComboItemStateChanged
    
    private void borderWidthComboFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_borderWidthComboFocusLost
        setBorderWidth();
    }//GEN-LAST:event_borderWidthComboFocusLost
    
    private void borderWidthComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_borderWidthComboActionPerformed
        setBorderWidth();
    }//GEN-LAST:event_borderWidthComboActionPerformed
    
    private void setBorderWidth(){
        String oldValue = borderWidthData.toString();
        borderWidthData.setUnit(borderWidthUnitCombo.getSelectedItem().toString());
        borderWidthData.setValue( borderWidthCombo.getSelectedItem().toString());
        firePropertyChange("border-width", oldValue, borderWidthData.toString());//NOI18N
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox borderWidthCombo;
    private javax.swing.JComboBox borderWidthUnitCombo;
    // End of variables declaration//GEN-END:variables
    
}
