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
 * ColorSelectionField.java
 *
 * Created on October 18, 2004, 12:34 PM
 */

package org.netbeans.modules.css.visual.ui;

import org.netbeans.modules.css.visual.model.ColorModel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.beans.PropertyChangeSupport;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import org.openide.util.NbBundle;


/**
 * Color Selection text field. Combination of
 * TextField and a Color button. This button
 * brings up the Color Chooser Dialog and paints
 * the selected color in its surface.
 * @author  Winston Prakash
 * @version 1.0
 */
public class ColorSelectionField extends javax.swing.JPanel {
    private PropertyChangeSupport propertyChangeSupport =  new PropertyChangeSupport(this);
    ColorModel colorModel = new ColorModel();

    String currentColor = null;
    String oldColor = null;

    /** Creates new form ColorSelectionField */
    public ColorSelectionField() {
        initComponents();
        initialize();
    }

    private void initialize(){
        // Set the font Variant to the GUI
        DefaultComboBoxModel colorList = colorModel.getColorList();
        colorComboBox.setModel(colorList);
        colorComboBox.setSelectedIndex(0);
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        colorButton = new ColorSelectionButton();
        colorComboBox = new javax.swing.JComboBox();

        setLayout(new java.awt.BorderLayout(3, 0));

        colorButton.setToolTipText(org.openide.util.NbBundle.getMessage(ColorSelectionField.class, "COLOR_CHOOSER_BTN_LABEL"));
        colorButton.setPreferredSize(new java.awt.Dimension(20, 20));
        colorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorButtonActionPerformed(evt);
            }
        });

        add(colorButton, java.awt.BorderLayout.EAST);
        colorButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ColorSelectionField.class, "COLOR_EDITOR_BTN_ACCESS_NAME"));
        colorButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ColorSelectionField.class, "COLOR_EDITOR_BTN_ACCESS_DESC"));

        colorComboBox.setEditable(true);
        colorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorComboBoxActionPerformed(evt);
            }
        });
        colorComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                colorComboBoxFocusLost(evt);
            }
        });
        colorComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                colorComboBoxItemStateChanged(evt);
            }
        });

        add(colorComboBox, java.awt.BorderLayout.CENTER);
        colorComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ColorSelectionField.class, "COLOR_SELECTION_COMBO_ACCESS_NAME"));
        colorComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ColorSelectionField.class, "COLOR_SELECTION_COMBO_ACCESS_Desc"));

    }// </editor-fold>//GEN-END:initComponents

    private void colorComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_colorComboBoxItemStateChanged
        setColor();
    }//GEN-LAST:event_colorComboBoxItemStateChanged

    private void colorComboBoxFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_colorComboBoxFocusLost
        setColor();
    }//GEN-LAST:event_colorComboBoxFocusLost
    
    private void colorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorComboBoxActionPerformed
        setColor();
    }//GEN-LAST:event_colorComboBoxActionPerformed
    
    private void colorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorButtonActionPerformed
        Color newColor = JColorChooser.showDialog(
        this,
        NbBundle.getMessage(ColorSelectionField.class, "COLOR_CHOOSER_TITLE"),
        colorModel.getColor());
        if(newColor != null){
            colorModel.setColor(newColor);
            colorComboBox.setSelectedItem(colorModel.getHexColor());
        }
    }//GEN-LAST:event_colorButtonActionPerformed
    
    /**
     * Adds a PropertyChangeListener to the listener list.
     * @param l The listener to add.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }
    
    /**
     * Removes a PropertyChangeListener from the listener list.
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }
    
    private void setColor(){
        currentColor = (String)colorComboBox.getSelectedItem();
        colorModel.setColor(currentColor);
        repaint();
        propertyChangeSupport.firePropertyChange("color", oldColor, currentColor); //NOI18N
        oldColor = currentColor;
        
    }
    
    public void setColorString(String color){
        currentColor = color;
        if((color == null) || color.equals("")){ 
            colorComboBox.setSelectedIndex(0);
        }else{
            colorComboBox.setSelectedItem(currentColor);
        }
        repaint();
    }
    
    public String getColorString(){
        return currentColor;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton colorButton;
    private javax.swing.JComboBox colorComboBox;
    // End of variables declaration//GEN-END:variables
    
    class ColorSelectionButton extends JButton{
        
        public ColorSelectionButton(){
            //setPreferredSize(new Dimension(20,20));
        }
        
        public void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2d = (Graphics2D) graphics;
            Color color = colorModel.getColor();
            if(color == null) color = Color.BLACK;
            g2d.setColor(color);
            int w = getWidth();
            int h = getHeight();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.fillRoundRect(4,4,w-9,h-9,5,5);
            g2d.setColor(color.darker());
            g2d.drawRoundRect(4,4,w-9,h-9,5,5);
        }
        
    }
}
