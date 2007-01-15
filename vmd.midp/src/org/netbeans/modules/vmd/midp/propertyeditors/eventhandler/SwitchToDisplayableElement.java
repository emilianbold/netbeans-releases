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

package org.netbeans.modules.vmd.midp.propertyeditors.eventhandler;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.ListCellRenderer;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.displayables.AlertCD;
import org.netbeans.modules.vmd.midp.components.handlers.SwitchDisplayableEventHandlerCD;
import org.netbeans.modules.vmd.midp.propertyeditors.element.PropertyEditorElementFactory;
import org.netbeans.modules.vmd.midp.propertyeditors.element.PropertyEditorEventHandlerElement;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.util.List;

/**
 *
 * @author Anton Chechel
 */
public class SwitchToDisplayableElement extends JPanel implements PropertyEditorEventHandlerElement {
    private DefaultComboBoxModel displayablesModel;
    private DefaultComboBoxModel displayablesWithoutAlertModel;
    private JRadioButton radioButton;
    private ListCellRenderer cellRenderer;
    
    private SwitchToDisplayableElement() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(SwitchToDisplayableElement.class, "LBL_SWITCH_TO_DISPL")); // NOI18N
        displayablesModel = new DefaultComboBoxModel();
        displayablesWithoutAlertModel = new DefaultComboBoxModel();
        cellRenderer = new ListCellRenderer();
        
        initComponents();
    }
    
    public void setText(String text) {
    }
    
    public JComponent getComponent() {
        return this;
    }
    
    public JRadioButton getRadioButton() {
        return radioButton;
    }
    
    public boolean isInitiallySelected() {
        return false;
    }
    
    public boolean isVerticallyResizable() {
        return false;
    }
    
    public void createEventHandler(DesignComponent eventSource) {
        if (!radioButton.isSelected()) {
            return;
        }
        DesignComponent displayable = (DesignComponent) displayablesModel.getSelectedItem();
        DesignComponent displayable2 = (DesignComponent) displayablesWithoutAlertsComboBox.getSelectedItem();
        if (thenGoToCheckBox.isSelected()) {
            DesignComponent eventHandler = MidpDocumentSupport.updateEventHandlerFromTarget(eventSource, displayable2);
            MidpDocumentSupport.updateEventHandlerWithAlert(eventHandler, displayable);
        } else {
            DesignComponent eventHandler = MidpDocumentSupport.updateEventHandlerFromTarget(eventSource, displayable);
            MidpDocumentSupport.updateSwitchDisplayableEventHandler(eventHandler, null, displayable);
        }
    }
    
    public void updateModel(List<DesignComponent> components, int modelType) {
        if (modelType == MODEL_TYPE_DISPLAYABLES) {
            displayablesModel.removeAllElements();
            for (DesignComponent component : components) {
                displayablesModel.addElement(component);
            }
        }
        
        if (modelType == MODEL_TYPE_DISPLAYABLES_WITHOUT_ALERTS) {
            displayablesWithoutAlertModel.removeAllElements();
            for (DesignComponent component : components) {
                displayablesWithoutAlertModel.addElement(component);
            }
        }
    }
    
    public String getText() {
        return null;
    }
    
    public void setPropertyValue(PropertyValue value) {
        if (value != null) {
            DesignComponent eventHandler = value.getComponent();
            if (SwitchDisplayableEventHandlerCD.TYPEID.equals(eventHandler.getType())) {
                radioButton.setSelected(true);
                
                DesignComponent displayable = eventHandler.readProperty(SwitchDisplayableEventHandlerCD.PROP_DISPLAYABLE).getComponent();
                DesignComponent displayable2 = eventHandler.readProperty(SwitchDisplayableEventHandlerCD.PROP_ALERT).getComponent();
                if (displayable2 != null) {
                    thenGoToCheckBox.setEnabled(true);
                    displayablesWithoutAlertsComboBox.setEnabled(true);
                    thenGoToCheckBox.setSelected(true);
                    displayablesComboBox.setSelectedItem(displayable2);
                    displayablesWithoutAlertsComboBox.setSelectedItem(displayable);
                } else {
                    displayablesComboBox.setSelectedItem(displayable);
                    clearAlertCheckBox();
                }
            } else {
                clearAlertCheckBox();
            }
        } else {
            clearAlertCheckBox();
        }
    }
    
    private void clearAlertCheckBox() {
        thenGoToCheckBox.setSelected(false);
        thenGoToCheckBox.setEnabled(false);
        displayablesWithoutAlertsComboBox.setEnabled(false);
    }
    
    public void setEnabled(boolean enabled) {
    }
    
    public static class SwitchToDisplayableElementFactory implements PropertyEditorElementFactory {
        public PropertyEditorEventHandlerElement createElement() {
            return new SwitchToDisplayableElement();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        displayablesComboBox = new javax.swing.JComboBox();
        thenGoToCheckBox = new javax.swing.JCheckBox();
        displayablesWithoutAlertsComboBox = new javax.swing.JComboBox();

        displayablesComboBox.setModel(displayablesModel);
        displayablesComboBox.setRenderer(cellRenderer);
        displayablesComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayablesComboBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(thenGoToCheckBox, org.openide.util.NbBundle.getMessage(SwitchToDisplayableElement.class, "LBL_THROUGH_ALERT")); // NOI18N
        thenGoToCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        thenGoToCheckBox.setEnabled(false);
        thenGoToCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        thenGoToCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                thenGoToCheckBoxActionPerformed(evt);
            }
        });

        displayablesWithoutAlertsComboBox.setModel(displayablesWithoutAlertModel);
        displayablesWithoutAlertsComboBox.setEnabled(false);
        displayablesWithoutAlertsComboBox.setRenderer(cellRenderer);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(thenGoToCheckBox)
                .addContainerGap())
            .add(displayablesWithoutAlertsComboBox, 0, 300, Short.MAX_VALUE)
            .add(displayablesComboBox, 0, 300, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(displayablesComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(thenGoToCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(displayablesWithoutAlertsComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void displayablesComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayablesComboBoxActionPerformed
        DesignComponent component = (DesignComponent) displayablesComboBox.getSelectedItem();
        if (component == null) {
            return;
        }
        radioButton.setSelected(true);
        displayablesWithoutAlertsComboBox.setEnabled(AlertCD.TYPEID.equals(component.getType()));
        thenGoToCheckBox.setEnabled(AlertCD.TYPEID.equals(component.getType()));
    }//GEN-LAST:event_displayablesComboBoxActionPerformed
    
    private void thenGoToCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_thenGoToCheckBoxActionPerformed
        displayablesWithoutAlertsComboBox.setEnabled(thenGoToCheckBox.isSelected());
}//GEN-LAST:event_thenGoToCheckBoxActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox displayablesComboBox;
    private javax.swing.JComboBox displayablesWithoutAlertsComboBox;
    private javax.swing.JCheckBox thenGoToCheckBox;
    // End of variables declaration//GEN-END:variables
}
