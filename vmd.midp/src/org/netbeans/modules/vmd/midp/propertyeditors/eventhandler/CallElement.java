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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.ListCellRenderer;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.handlers.CallPointEventHandlerCD;
import org.netbeans.modules.vmd.midp.components.handlers.MethodPointEventHandlerCD;
import org.netbeans.modules.vmd.midp.propertyeditors.element.PropertyEditorEventHandlerElement;
import org.netbeans.modules.vmd.midp.propertyeditors.element.PropertyEditorElementFactory;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public class CallElement extends JPanel implements PropertyEditorEventHandlerElement {
    private DefaultComboBoxModel pointsModel;
    private JRadioButton radioButton;
    private ListCellRenderer cellRenderer;
    
    private CallElement() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(CallElement.class, "LBL_CALL")); // NOI18N
        pointsModel = new DefaultComboBoxModel();
        cellRenderer = new ListCellRenderer();
        
        initComponents();
    }
    
    public void setTextForPropertyValue (String text) {
    }
    
    public JComponent getCustomEditorComponent() {
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

    public String getTextForPropertyValue () {
        return ""; // NOI18N
    }
    
    public void updateState(PropertyValue value) {
        if (value != null) {
            DesignComponent eventHandler = value.getComponent();
            System.out.println("eventHandler.getType() = " + eventHandler.getType());
            if (eventHandler.getType().equals(CallPointEventHandlerCD.TYPEID) || eventHandler.getType().equals(MethodPointEventHandlerCD.TYPEID)) {
                System.out.println("WTF?");
                System.out.println("eventHandler.getType().equals(CallPointEventHandlerCD.TYPEID) = " + eventHandler.getType().equals(CallPointEventHandlerCD.TYPEID));
                System.out.println("eventHandler.getType().equals(MethodPointEventHandlerCD.TYPEID) = " + eventHandler.getType().equals(MethodPointEventHandlerCD.TYPEID));
                radioButton.setSelected(true);
            }
        }
    }
    
    public void createEventHandler(DesignComponent eventSource) {
        if (!radioButton.isSelected()) {
            return;
        }
        DesignComponent callElement = (DesignComponent) pointsModel.getSelectedItem();
        MidpDocumentSupport.updateEventHandlerFromTarget(eventSource, callElement);
    }
    
    public void updateModel(List<DesignComponent> components, int modelType) {
        if (modelType == MODEL_TYPE_POINTS) {
            pointsModel.removeAllElements();
            for (DesignComponent component : components) {
                pointsModel.addElement(component);
            }
        }
    }
    
    public void setElementEnabled(boolean enabled) {
    }

    public Collection<TypeID> getTypes() {
        List<TypeID> list = new ArrayList<TypeID>(2);
        list.add(CallPointEventHandlerCD.TYPEID);
        list.add(MethodPointEventHandlerCD.TYPEID);
        return list;
    }
    
    public static class CallElementFactory implements PropertyEditorElementFactory {
        public PropertyEditorEventHandlerElement createElement() {
            return new CallElement();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        comboBox = new javax.swing.JComboBox();

        comboBox.setModel(pointsModel);
        comboBox.setRenderer(cellRenderer);
        comboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(comboBox, 0, 300, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(comboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void comboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxActionPerformed
        //radioButton.setSelected(true);
    }//GEN-LAST:event_comboBoxActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox comboBox;
    // End of variables declaration//GEN-END:variables
}
