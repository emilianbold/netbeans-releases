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
 * PaddingWidthField.java
 *
 * Created on October 22, 2004, 5:08 PM
 */

package org.netbeans.modules.css.visual.ui;

import org.netbeans.modules.css.visual.model.MarginPaddingModel;
import org.netbeans.modules.css.visual.model.PropertyWithUnitData;
import org.netbeans.modules.css.visual.model.Utils;
import java.beans.PropertyChangeSupport;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Padding Width field wiith text field and unit combo box
 * @author  Winston Prakash
 * @version 1.0
 */
public class PaddingWidthField extends javax.swing.JPanel {
    PropertyWithUnitData borderPaddingData = new PropertyWithUnitData();
    MarginPaddingModel marginPaddingModel = new MarginPaddingModel();

    /** Creates new form borderMarginField */
    public PaddingWidthField() {
        initComponents();
        borderPaddingCombo.setModel(marginPaddingModel.getPaddingList());
        borderPaddingUnitCombo.setModel(marginPaddingModel.getPaddingUnitList());

        // Add editor listeners to the padding width combobox
        final JTextField borderPaddingComboEditor = (JTextField) borderPaddingCombo.getEditor().getEditorComponent();
        borderPaddingComboEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                       borderPaddingUnitCombo.setEnabled(Utils.isInteger(borderPaddingComboEditor.getText()));
                    }
                });
            }
        });
    }

    private PropertyChangeSupport propertyChangeSupport =  new PropertyChangeSupport(this);

    /**
     * Adds a PropertyChangeListener to the listener list.
     * @param l The listener to add.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void setPaddingString(String paddingStr){
        if((paddingStr != null) && !paddingStr.equals("")){
            if(Utils.isInteger(paddingStr)){
                setWidthValue(paddingStr);
            }else{
                String unit = getUnit(paddingStr);
                setWidthUnit(unit);
                setWidthValue(paddingStr.replaceAll(unit,"").trim());
            }
        }else{
            setWidthValue(null);
            setWidthUnit(null);
        }
    }

    public String getPaddingString(){
        return borderPaddingData.toString();
    }
    private String getUnit(String paddingStr){
        DefaultComboBoxModel unitList = marginPaddingModel.getPaddingUnitList();
        for(int i=0; i< unitList.getSize(); i++){
            String unit = (String)unitList.getElementAt(i);
            if(paddingStr.trim().endsWith(unit)){
                return unit;
            }
        }
        return "";
    }

    public void setWidthValue(String value){
        if((value == null) || value.equals("")){
            borderPaddingCombo.setSelectedIndex(0);
        }else{
            borderPaddingCombo.setSelectedItem(value);
            borderPaddingData.setValue(value);
        }
    }

    public void setWidthUnit(String value){
        if((value == null) || value.equals("")){
            borderPaddingUnitCombo.setSelectedIndex(marginPaddingModel.getPaddingUnitList().getIndexOf("px")); //NOI18N
        }else{
            if(marginPaddingModel.getMarginUnitList().getIndexOf(value) != -1){
                borderPaddingUnitCombo.setSelectedIndex(marginPaddingModel.getPaddingUnitList().getIndexOf(value));
            }else{
                borderPaddingUnitCombo.setSelectedIndex(marginPaddingModel.getPaddingUnitList().getIndexOf("px")); //NOI18N
            }
            borderPaddingData.setUnit(value);
        }
    }
    
    /**
     * Removes a PropertyChangeListener from the listener list.
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        borderPaddingCombo = new javax.swing.JComboBox();
        borderPaddingUnitCombo = new javax.swing.JComboBox();

        setLayout(new java.awt.BorderLayout(5, 0));

        borderPaddingCombo.setEditable(true);
        borderPaddingCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                borderPaddingComboActionPerformed(evt);
            }
        });
        borderPaddingCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                borderPaddingComboFocusLost(evt);
            }
        });
        borderPaddingCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                borderPaddingComboItemStateChanged(evt);
            }
        });

        add(borderPaddingCombo, java.awt.BorderLayout.CENTER);
        borderPaddingCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PaddingWidthField.class, "PADDIN_WIDTH_ACCESS_NAME"));
        borderPaddingCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PaddingWidthField.class, "PADDING_WIDTH_FIELD_ACCESS_DESC"));

        borderPaddingUnitCombo.setEnabled(false);
        borderPaddingUnitCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                borderPaddingUnitComboItemStateChanged(evt);
            }
        });
        borderPaddingUnitCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                borderPaddingUnitComboFocusLost(evt);
            }
        });

        add(borderPaddingUnitCombo, java.awt.BorderLayout.EAST);
        borderPaddingUnitCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PaddingWidthField.class, "PADDING_WIDTH_UNIT_ACCESS_NAME"));
        borderPaddingUnitCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PaddingWidthField.class, "PADDING_WIDTH_UNIT_ACCESS_DESC"));

    }
    // </editor-fold>//GEN-END:initComponents
    
    private void borderPaddingUnitComboFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_borderPaddingUnitComboFocusLost
        setBorderPadding();
    }//GEN-LAST:event_borderPaddingUnitComboFocusLost
    
    private void borderPaddingUnitComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_borderPaddingUnitComboItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setBorderPadding();
        }
    }//GEN-LAST:event_borderPaddingUnitComboItemStateChanged
    // For accessibility
    public void setAccessibleName(String comboName, String unitName){
        borderPaddingCombo.getAccessibleContext().setAccessibleName(comboName);
        borderPaddingUnitCombo.getAccessibleContext().setAccessibleName(unitName);
    }
    
    // For accessibility
    public void setAccessibleDescription(String comboDesc, String unitDesc){
        borderPaddingCombo.getAccessibleContext().setAccessibleDescription(comboDesc);
        borderPaddingUnitCombo.getAccessibleContext().setAccessibleDescription(unitDesc);
    }
    
    private void borderPaddingComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_borderPaddingComboItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            borderPaddingUnitCombo.setEnabled(Utils.isInteger(borderPaddingCombo.getSelectedItem().toString()));
            setBorderPadding();
        }
    }//GEN-LAST:event_borderPaddingComboItemStateChanged
    
    private void borderPaddingComboFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_borderPaddingComboFocusLost
        setBorderPadding();
    }//GEN-LAST:event_borderPaddingComboFocusLost
    
    private void borderPaddingComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_borderPaddingComboActionPerformed
        setBorderPadding();
    }//GEN-LAST:event_borderPaddingComboActionPerformed
    
    private void setBorderPadding(){
        String oldValue = borderPaddingData.toString();
        borderPaddingData.setUnit(borderPaddingUnitCombo.getSelectedItem().toString());
        borderPaddingData.setValue( borderPaddingCombo.getSelectedItem().toString());
        propertyChangeSupport.firePropertyChange("padding-width", oldValue, borderPaddingData.toString()); //NOI18N
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox borderPaddingCombo;
    private javax.swing.JComboBox borderPaddingUnitCombo;
    // End of variables declaration//GEN-END:variables
    
}
