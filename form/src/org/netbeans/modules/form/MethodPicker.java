/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.form;

import java.beans.*;
import java.lang.reflect.Method;
import java.util.*;
import java.awt.*;

import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;

/** The MethodPicker is a form which allows user to pick one of methods
 * with specified required return type.
 *
 * @author  Ian Formanek
 * @version 1.00, Aug 29, 1998
 */
public class MethodPicker extends javax.swing.JPanel {

    static final long serialVersionUID =7355140527892160804L;
    /** Initializes the Form */
    public MethodPicker(FormModel formModel, RADComponent componentToSelect, Class requiredType) {
        this.formModel = formModel;
        this.requiredType = requiredType;
        initComponents();

        Collection allComponents = formModel.getMetaComponents();
        components =(RADComponent[])allComponents.toArray(new RADComponent [allComponents.size()]);
        int selIndex = -1;
        for (int i = 0; i < components.length; i++) {
            componentsCombo.addItem(components[i].getName());
            if ((componentToSelect != null) &&(componentToSelect.equals(components[i])))
                selIndex = i;
        }

        if (selIndex != -1) {
            selectedComponent = components[selIndex];
            componentsCombo.setSelectedIndex(selIndex);
        }

        updateMethodList();

        componentLabel.setText(FormEditor.getFormBundle().getString("CTL_CW_Component")); // "Component:"
        listLabel.setText(FormEditor.getFormBundle().getString("CTL_CW_MethodList"));

        componentLabel.setDisplayedMnemonic(FormEditor.getFormBundle().getString("CTL_CW_Component_Mnemonic").charAt(0));
        listLabel.setDisplayedMnemonic(FormEditor.getFormBundle().getString("CTL_CW_MethodList_Mnemonic").charAt(0));

        componentsCombo.getAccessibleContext().setAccessibleDescription(FormEditor.getFormBundle().getString("ACSD_CTL_CW_Component"));
        methodList.getAccessibleContext().setAccessibleDescription(FormEditor.getFormBundle().getString("ACSD_CTL_CW_MethodList"));
        getAccessibleContext().setAccessibleDescription(FormEditor.getFormBundle().getString("ACSD_MethodPicker"));
        HelpCtx.setHelpIDString(this, "gui.connecting.code"); // NOI18N
    }

    public boolean isPickerValid() {
        return pickerValid;
    }
    
    private void setPickerValid(boolean v) {
        boolean old = pickerValid;
        pickerValid = v;
        firePropertyChange("pickerValid", old, pickerValid); // NOI18N
    }

    RADComponent getSelectedComponent() {
        return selectedComponent;
    }

    void setSelectedComponent(RADComponent selectedComponent) {
        if (selectedComponent != null)
            componentsCombo.setSelectedItem(selectedComponent.getName());
    }

    MethodDescriptor getSelectedMethod() {
        if ((selectedComponent == null) ||(methodList.getSelectedIndex() == -1))
            return null;
        return descriptors [methodList.getSelectedIndex()];
    }

    void setSelectedMethod(MethodDescriptor selectedMethod) {
        if (selectedMethod == null) {
            methodList.setSelectedIndex(-1);
        } else {
            methodList.setSelectedValue(FormUtils.getMethodName(selectedMethod), true);
        }
    }

    // ----------------------------------------------------------------------------
    // private methods

    private void addComponentsRecursively(ComponentContainer cont, Vector vect) {
        RADComponent[] children = cont.getSubBeans();
        for (int i = 0; i < children.length; i++) {
            vect.addElement(children[i]);
            if (children[i] instanceof ComponentContainer)
                addComponentsRecursively((ComponentContainer)children[i], vect);
        }
    }

    private void updateMethodList() {
        RADComponent sel = getSelectedComponent();
        if (sel == null) {
            methodList.setListData(new Object [0]);
            methodList.revalidate();
            methodList.repaint();
        } else {
            MethodDescriptor[] descs = sel.getBeanInfo().getMethodDescriptors();
            ArrayList filtered = new ArrayList();
            for (int i = 0; i < descs.length; i ++) {
                if (requiredType.isAssignableFrom(descs[i].getMethod().getReturnType()) &&
                    (descs[i].getMethod().getParameterTypes().length == 0)) // [FUTURE: - currently we allow only methods without params]
                {
                    filtered.add(descs[i]);
                }
            }
            // sort the methods by name
            Collections.sort(filtered, new Comparator() {
                public int compare(Object o1, Object o2) {
                    return((MethodDescriptor)o1).getName().compareTo(((MethodDescriptor)o2).getName());
                }
            }
                             );

            descriptors = new MethodDescriptor[filtered.size()];
            filtered.toArray(descriptors);

            String[] items = new String [descriptors.length];
            for (int i = 0; i < descriptors.length; i++)
                items[i] = FormUtils.getMethodName(descriptors[i]);
            methodList.setListData(items);
            methodList.revalidate();
            methodList.repaint();
        }
    }

    private void updateState() {
        if ((getSelectedComponent() == null) || (getSelectedMethod() == null)) {
            setPickerValid(false);
        } else {
            setPickerValid(getSelectedMethod().getMethod().getParameterTypes().length == 0);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        componentLabel = new javax.swing.JLabel();
        componentsCombo = new javax.swing.JComboBox();
        listLabel = new javax.swing.JLabel();
        propertiesScrollPane = new javax.swing.JScrollPane();
        methodList = new javax.swing.JList();
        
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 0, 11)));
        componentLabel.setText(FormEditor.getFormBundle().getString("CTL_Component"));
        componentLabel.setLabelFor(componentsCombo);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 5, 6);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(componentLabel, gridBagConstraints1);
        
        componentsCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                componentsComboItemStateChanged(evt);
            }
        });
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 5, 0);
        add(componentsCombo, gridBagConstraints1);
        
        listLabel.setText(FormEditor.getFormBundle().getString("CTL_Component"));
        listLabel.setLabelFor(methodList);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 2, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(listLabel, gridBagConstraints1);
        
        methodList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        methodList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                methodListValueChanged(evt);
            }
        });
        
        propertiesScrollPane.setViewportView(methodList);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.gridwidth = 2;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(propertiesScrollPane, gridBagConstraints1);
        
    }//GEN-END:initComponents

    private void methodListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_methodListValueChanged
        if (methodList.getSelectedIndex() == -1)
            selectedMethod = null;
        else
            selectedMethod = descriptors[methodList.getSelectedIndex()];
        updateState();
    }//GEN-LAST:event_methodListValueChanged

    private void componentsComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_componentsComboItemStateChanged
        if (componentsCombo.getSelectedIndex() == -1)
            selectedComponent = null;
        else
            selectedComponent = components[componentsCombo.getSelectedIndex()];
        updateMethodList();
    }//GEN-LAST:event_componentsComboItemStateChanged

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:closeDialog
    }//GEN-LAST:closeDialog

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel componentLabel;
    private javax.swing.JComboBox componentsCombo;
    private javax.swing.JLabel listLabel;
    private javax.swing.JScrollPane propertiesScrollPane;
    private javax.swing.JList methodList;
    // End of variables declaration//GEN-END:variables


    private FormModel formModel;
    private boolean pickerValid = false;

    private RADComponent[] components;
    private Class requiredType;
    private MethodDescriptor[] descriptors;
    private RADComponent selectedComponent;
    private MethodDescriptor selectedMethod;

}
