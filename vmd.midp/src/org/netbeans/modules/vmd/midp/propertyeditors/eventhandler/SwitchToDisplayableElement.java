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
import org.netbeans.modules.vmd.midp.components.ListCellRenderer;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.propertyeditors.element.PropertyEditorEventHandlerElement;
import org.netbeans.modules.vmd.midp.propertyeditors.element.PropertyEditorElementFactory;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import javax.swing.*;
import java.util.List;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.handlers.SwitchDisplayableEventHandlerCD;

/**
 *
 * @author Anton Chechel
 */
public class SwitchToDisplayableElement extends JPanel implements PropertyEditorEventHandlerElement {
    private DefaultComboBoxModel displayablesModel;
    private DefaultComboBoxModel alertsModel;
    private JRadioButton radioButton;
    private ListCellRenderer cellRenderer;
    
    private SwitchToDisplayableElement() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(SwitchToDisplayableElement.class, "LBL_SWITCH_TO_DISPL")); // NOI18N
        displayablesModel = new DefaultComboBoxModel();
        alertsModel = new DefaultComboBoxModel();
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
    
    public void createEventHandler(DesignComponent eventSource) {
        if (!radioButton.isSelected()) {
            return;
        }
        boolean needAlert = throwAlertCheckBox.isSelected();
        DesignComponent displayable = (DesignComponent) displayablesModel.getSelectedItem();
        DesignComponent eventHandler = MidpDocumentSupport.updateEventHandlerFromTarget(eventSource, displayable);
        if (needAlert) {
            DesignComponent alert = (DesignComponent) alertsComboBox.getSelectedItem();
            if (alert != null) {
                MidpDocumentSupport.updateEventHandlerWithAlert(eventHandler, alert);
            }
        }
    }
    
    public void updateModel(List<DesignComponent> components, int modelType) {
        if (modelType == MODEL_TYPE_DISPLAYABLES) {
            displayablesModel.removeAllElements();
            for (DesignComponent component : components) {
                displayablesModel.addElement(component);
            }
        }
        
        if (modelType == MODEL_TYPE_ALERTS) {
            alertsModel.removeAllElements();
            for (DesignComponent component : components) {
                alertsModel.addElement(component);
            }
        }
    }
    
    public String getText() {
        return null;
    }
    
    public void setPropertyValue(PropertyValue value) {
        if (value != null) {
            DesignComponent eventHandler = value.getComponent();
            if (eventHandler.getType().equals(SwitchDisplayableEventHandlerCD.TYPEID)) {
                radioButton.setSelected(true);
                
                DesignComponent displayable = eventHandler.readProperty(SwitchDisplayableEventHandlerCD.PROP_DISPLAYABLE).getComponent();
                DesignComponent alert = eventHandler.readProperty(SwitchDisplayableEventHandlerCD.PROP_ALERT).getComponent();
                displayablesModel.setSelectedItem(displayable);
                if (alert != null) {
                    throwAlertCheckBox.setSelected(true);
                    alertsModel.setSelectedItem(alert);
                }
            }
        }
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
        throwAlertCheckBox = new javax.swing.JCheckBox();
        alertsComboBox = new javax.swing.JComboBox();

        displayablesComboBox.setModel(displayablesModel);
        displayablesComboBox.setRenderer(cellRenderer);
        displayablesComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayablesComboBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(throwAlertCheckBox, org.openide.util.NbBundle.getMessage(SwitchToDisplayableElement.class, "LBL_THROUGH_ALERT")); // NOI18N
        throwAlertCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        throwAlertCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        throwAlertCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                throwAlertCheckBoxActionPerformed(evt);
            }
        });

        alertsComboBox.setModel(alertsModel);
        alertsComboBox.setEnabled(false);
        alertsComboBox.setRenderer(cellRenderer);
        alertsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                alertsComboBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(displayablesComboBox, 0, 280, Short.MAX_VALUE)
                    .add(throwAlertCheckBox)
                    .add(alertsComboBox, 0, 280, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(displayablesComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(throwAlertCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(alertsComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void alertsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alertsComboBoxActionPerformed
        radioButton.setSelected(true);
    }//GEN-LAST:event_alertsComboBoxActionPerformed

    private void displayablesComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayablesComboBoxActionPerformed
        radioButton.setSelected(true);
    }//GEN-LAST:event_displayablesComboBoxActionPerformed
    
    private void throwAlertCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_throwAlertCheckBoxActionPerformed
        alertsComboBox.setEnabled(throwAlertCheckBox.isSelected());
        radioButton.setSelected(true);
    }//GEN-LAST:event_throwAlertCheckBoxActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox alertsComboBox;
    private javax.swing.JComboBox displayablesComboBox;
    private javax.swing.JCheckBox throwAlertCheckBox;
    // End of variables declaration//GEN-END:variables
}
