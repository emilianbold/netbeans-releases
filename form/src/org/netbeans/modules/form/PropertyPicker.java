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

/** The PropertyPicker is a form which allows user to choose from property set
 * of specified required class.
 *
 * @author  Ian Formanek
 * @version 1.00, Aug 29, 1998
 */
public class PropertyPicker extends javax.swing.JPanel {

    public static final int CANCEL = 0;
    public static final int OK = 1;

    static final long serialVersionUID =5689122601606238081L;
    /** Initializes the Form */
    public PropertyPicker(FormModel formModel, RADComponent componentToSelect, Class requiredType) {
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

        propertyList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        updatePropertyList();

        // localize components
        componentLabel.setDisplayedMnemonic(FormEditor.getFormBundle().getString("CTL_CW_Component_Mnemonic").charAt(0));
        listLabel.setDisplayedMnemonic(FormEditor.getFormBundle().getString("CTL_CW_PropertyList_Mnemonic").charAt(0));

        componentsCombo.getAccessibleContext().setAccessibleDescription(FormEditor.getFormBundle().getString("ACSD_CTL_CW_Component"));
        propertyList.getAccessibleContext().setAccessibleDescription(FormEditor.getFormBundle().getString("ACSD_CTL_CW_PropertyList"));
        getAccessibleContext().setAccessibleDescription(FormEditor.getFormBundle().getString("ACSD_PropertyPicker"));

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

    PropertyDescriptor getSelectedProperty() {
        if ((selectedComponent == null) ||(propertyList.getSelectedIndex() == -1))
            return null;
        return descriptors [propertyList.getSelectedIndex()];
    }

    void setSelectedProperty(PropertyDescriptor selectedProperty) {
        if (selectedProperty == null) {
            propertyList.setSelectedIndex(-1);
        } else {
            propertyList.setSelectedValue(selectedProperty.getName(), true);
        }
    }
    // ----------------------------------------------------------------------------
    // private methods

    private void updatePropertyList() {
        RADComponent sel = getSelectedComponent();
        if (sel == null) {
            propertyList.setListData(new Object [0]);
            propertyList.revalidate();
            propertyList.repaint();
        } else {
            PropertyDescriptor[] descs = sel.getBeanInfo().getPropertyDescriptors();
            ArrayList filtered = new ArrayList();
            for (int i = 0; i < descs.length; i ++) {
                if ((descs[i].getReadMethod() != null) &&       // filter out non-readable properties
                    (descs[i].getPropertyType() != null) &&  // indexed properties return null from getPropertyType
                    requiredType.isAssignableFrom(descs[i].getPropertyType())) {
                    filtered.add(descs[i]);
                }
            }

            // sort the properties by name
            Collections.sort(filtered, new Comparator() {
                public int compare(Object o1, Object o2) {
                    return((PropertyDescriptor)o1).getName().compareTo(((PropertyDescriptor)o2).getName());
                }
            }
                             );

            descriptors = new PropertyDescriptor[filtered.size()];
            filtered.toArray(descriptors);

            String[] items = new String [descriptors.length];
            for (int i = 0; i < descriptors.length; i++)
                items[i] = descriptors[i].getName();
            propertyList.setListData(items);
            propertyList.revalidate();
            propertyList.repaint();
        }
    }

    private void updateState() {
        setPickerValid((getSelectedComponent() != null) &&(getSelectedProperty() != null));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        componentsCombo = new javax.swing.JComboBox();
        propertiesScrollPane = new javax.swing.JScrollPane();
        propertyList = new javax.swing.JList();
        componentLabel = new javax.swing.JLabel();
        listLabel = new javax.swing.JLabel();
        
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 0, 11)));
        componentsCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                componentsComboItemStateChanged(evt);
            }
        });
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 5, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints1.weightx = 1.0;
        add(componentsCombo, gridBagConstraints1);
        
        propertyList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                propertyListValueChanged(evt);
            }
        });
        
        propertiesScrollPane.setViewportView(propertyList);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.gridwidth = 2;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(propertiesScrollPane, gridBagConstraints1);
        
        componentLabel.setText(FormEditor.getFormBundle().getString("CTL_Component"));
        componentLabel.setLabelFor(componentsCombo);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 5, 6);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(componentLabel, gridBagConstraints1);
        
        listLabel.setText(FormEditor.getFormBundle().getString("CTL_CW_PropertyList"));
        listLabel.setLabelFor(propertyList);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 2, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(listLabel, gridBagConstraints1);
        
    }//GEN-END:initComponents


    private void propertyListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_propertyListValueChanged
        if (propertyList.getSelectedIndex() == -1)
            selectedProperty = null;
        else
            selectedProperty = descriptors[propertyList.getSelectedIndex()];
        updateState();
    }//GEN-LAST:event_propertyListValueChanged

    private void componentsComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_componentsComboItemStateChanged
        if (componentsCombo.getSelectedIndex() == -1)
            selectedComponent = null;
        else
            selectedComponent = components[componentsCombo.getSelectedIndex()];
        updatePropertyList();
    }//GEN-LAST:event_componentsComboItemStateChanged

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:closeDialog
    }//GEN-LAST:closeDialog

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox componentsCombo;
    private javax.swing.JScrollPane propertiesScrollPane;
    private javax.swing.JList propertyList;
    private javax.swing.JLabel componentLabel;
    private javax.swing.JLabel listLabel;
    // End of variables declaration//GEN-END:variables


    private FormModel formModel;
    private boolean pickerValid = false;

    private RADComponent[] components;
    private Class requiredType;
    private PropertyDescriptor[] descriptors;
    private RADComponent selectedComponent;
    private PropertyDescriptor selectedProperty;

}
