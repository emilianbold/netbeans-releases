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
import org.netbeans.modules.css.visual.model.PropertyData;
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
    private PropertyChangeSupport propertyChangeSupport;
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
    
    private void setColor(){
        PropertyData colorPropertyData = new PropertyData();
        currentColor = (String)colorComboBox.getSelectedItem();
        colorPropertyData.setValue(currentColor);
        colorModel.setColor(currentColor);
        repaint();
        firePropertyChange("color", oldColor, colorPropertyData.toString()); //NOI18N
        oldColor = colorPropertyData.toString();
        
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
