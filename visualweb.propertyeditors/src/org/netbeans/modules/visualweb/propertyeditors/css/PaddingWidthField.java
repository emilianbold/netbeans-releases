/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.visualweb.propertyeditors.css;

import org.netbeans.modules.visualweb.propertyeditors.css.model.CssStyleData;
import org.netbeans.modules.visualweb.propertyeditors.css.model.MarginPaddingModel;
import org.netbeans.modules.visualweb.propertyeditors.css.model.PropertyWithUnitData;
import org.netbeans.modules.visualweb.propertyeditors.css.model.Utils;
import java.beans.PropertyChangeSupport;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author  Winston Prakash
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
    public void addCssPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }
    
    /**
     * Removes a PropertyChangeListener from the listener list.
     * @param l The listener to remove.
     */
    public void removeCssPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
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
    
     
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        borderPaddingCombo = new javax.swing.JComboBox();
        borderPaddingUnitCombo = new javax.swing.JComboBox();

        setLayout(new java.awt.BorderLayout(5, 0));

        borderPaddingCombo.setEditable(true);
        borderPaddingCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                borderPaddingComboItemStateChanged(evt);
            }
        });
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

        add(borderPaddingCombo, java.awt.BorderLayout.CENTER);

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

    }
    // </editor-fold>//GEN-END:initComponents
    
    private void borderPaddingUnitComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_borderPaddingUnitComboItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setborderPadding();
        }
    }//GEN-LAST:event_borderPaddingUnitComboItemStateChanged
    
    private void borderPaddingUnitComboFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_borderPaddingUnitComboFocusLost
        setborderPadding();
    }//GEN-LAST:event_borderPaddingUnitComboFocusLost
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
            setborderPadding();
        }
    }//GEN-LAST:event_borderPaddingComboItemStateChanged
    
    private void borderPaddingComboFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_borderPaddingComboFocusLost
        setborderPadding();
    }//GEN-LAST:event_borderPaddingComboFocusLost
    
    private void borderPaddingComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_borderPaddingComboActionPerformed
        setborderPadding();
    }//GEN-LAST:event_borderPaddingComboActionPerformed
    
    private void setborderPadding(){
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
