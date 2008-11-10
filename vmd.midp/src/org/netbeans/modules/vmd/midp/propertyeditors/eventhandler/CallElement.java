/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
        
        radioButton.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(CallElement.class, "ACSN_CALL")); // NOI18N
        radioButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(CallElement.class, "ACSD_CALL")); // NOI18N
        
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
            if (eventHandler.getType().equals(CallPointEventHandlerCD.TYPEID) || eventHandler.getType().equals(MethodPointEventHandlerCD.TYPEID)) {
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
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.vmd.midp.propertyeditors.element.PropertyEditorElementFactory.class)
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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

        comboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CallElement.class, "ACSN_Call")); // NOI18N
        comboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CallElement.class, "ACSD_Call")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void comboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxActionPerformed
        //radioButton.setSelected(true);//GEN-LAST:event_comboBoxActionPerformed
    }                                        
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox comboBox;
    // End of variables declaration//GEN-END:variables
}
