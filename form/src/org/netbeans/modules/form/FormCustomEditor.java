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

/*
 * FormCustomEditor.java
 *
 * Created on March 1, 2001, 11:52 AM
 */

package org.netbeans.modules.form;

import org.openide.*;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyEditor;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.border.EmptyBorder;

/** 
 *
 * @author  Ian Formanek, Vladimir Zboril
 */
public class FormCustomEditor extends javax.swing.JPanel implements EnhancedCustomPropertyEditor {

    private static final int DEFAULT_WIDTH  = 350;
    private static final int DEFAULT_HEIGHT = 350;

    // -----------------------------------------------------------------------------
    // Private variables

    private FormPropertyEditor editor;
    private PropertyEditor[] allEditors;
    private Component[] allCustomEditors;

    private String preCode;
    private String postCode;

    static final long serialVersionUID =-5566324092702416875L;
    
    /** Creates new form FormCustomEditor */
    public FormCustomEditor(FormPropertyEditor editor) {
        initComponents();
        
        advancedButton.setText(FormEditor.getFormBundle().getString("CTL_Advanced"));   // NOI18N
        advancedButton.setMnemonic(FormEditor.getFormBundle().getString("CTL_Advanced_mnemonic").charAt(0));    // NOI18N
        advancedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showAdvancedSettings();
            }
        });
        
        jLabel1.setText(FormEditor.getFormBundle().getString("LAB_SelectMode"));   //NOI18N
        jLabel1.setDisplayedMnemonic((FormEditor.getFormBundle().getString("LAB_SelectMode.mnemonic").charAt(0)));   //NOI18N
        jLabel1.setLabelFor(jComboBox1);
        
        this.editor = editor;
        preCode = editor.getProperty().getPreCode();
        postCode = editor.getProperty().getPostCode();
        allEditors = editor.getAllEditors();
        allCustomEditors = new Component[allEditors.length];
        PropertyEditor currentlyUsedEditor = editor.getModifiedEditor();
        if (currentlyUsedEditor == null) {
            currentlyUsedEditor = allEditors[0];
            editor.setModifiedEditor(currentlyUsedEditor);
        } else if (!currentlyUsedEditor.getClass().equals(allEditors[0].getClass())) {
            // if the current editor does not match any of available ones, we will use the first available instead of it
            editor.setModifiedEditor(currentlyUsedEditor);
        }
      
        //**********************************    
        HelpCtx.setHelpIDString(CardPanel, FormCustomEditor.class.getName() + ".tabbedPane"); // NOI18N
            
        int indexToSelect = -1;
        for (int i = 0; i < allEditors.length; i++) {
            editor.getPropertyContext().initPropertyEditor(allEditors[i]);

                if (allEditors[i].getClass().equals(currentlyUsedEditor.getClass()) && indexToSelect == -1) {
                    allEditors[i].setValue(editor.getValue());
                    indexToSelect = i;
                } else {
                    Object currValue = editor.getValue();
                    boolean valueSet = false;
                    if (currValue != null) {
                        if (editor.getPropertyType().isAssignableFrom(currValue.getClass())) {
                            allEditors[i].setValue(currValue); // current value is of the real property type
                            valueSet = true;
                        } else if (currValue instanceof FormDesignValue) {
                            Object desValue = ((FormDesignValue)currValue).getDesignValue();
                            if (desValue != FormDesignValue.IGNORED_VALUE) {
                                allEditors[i].setValue(desValue); // current value is of the real property type
                                valueSet = true;
                            }
                        }
                    }
                    if (!valueSet) {
                        Object defValue = editor.getProperty().getDefaultValue();
                        if (defValue != null) {
                            allEditors[i].setValue(defValue);
                        }
                    }
                }

                String CardName;
                if (allEditors[i] instanceof NamedPropertyEditor) {
                    CardName =((NamedPropertyEditor)allEditors[i]).getDisplayName();
                } else {
                    CardName = Utilities.getShortClassName(allEditors[i].getClass());
                }

                Component custEd = null;
                if (!allEditors[i].supportsCustomEditor() || (custEd = allEditors[i].getCustomEditor()) instanceof java.awt.Window) {
                    javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.GridBagLayout());
                    p.add(new JLabel(FormEditor.getFormBundle().getString("CTL_PropertyEditorDoesNot")));   // NOI18N
                    custEd = p;
                }

                allCustomEditors[i] = custEd;
                CardPanel.add(CardName, custEd);
                jComboBox1.addItem(CardName);
            }

            
        if (indexToSelect == -1) {
            // if the current editor does not match any of available ones, we will use the first available instaed of it
            jComboBox1.setSelectedIndex(0);
        } else {
           jComboBox1.setSelectedIndex( indexToSelect); 
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jComboBox1 = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        CardPanel = new javax.swing.JPanel();
        advancedButton = new javax.swing.JButton();
        
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.insets = new java.awt.Insets(12, 5, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(jComboBox1, gridBagConstraints1);
        
        jLabel1.setText("jLabel1");
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel1, gridBagConstraints1);
        
        jPanel1.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints2;
        
        jPanel1.setBorder(new javax.swing.border.EtchedBorder());
        CardPanel.setLayout(new java.awt.CardLayout());
        
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 0;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints2.insets = new java.awt.Insets(12, 12, 11, 11);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.weighty = 1.0;
        jPanel1.add(CardPanel, gridBagConstraints2);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 11);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(jPanel1, gridBagConstraints1);
        
        advancedButton.setText("jButton1");
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(advancedButton, gridBagConstraints1);
        
    }//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // Add your handling code here:
        CardLayout cl = (CardLayout) CardPanel.getLayout();
        cl.show(CardPanel, (String) jComboBox1.getSelectedItem());
        FormCustomEditor.this.editor.setModifiedEditor(getCurrentPropertyEditor());
    }//GEN-LAST:event_jComboBox1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel CardPanel;
    private javax.swing.JButton advancedButton;
    // End of variables declaration//GEN-END:variables
    
    public Dimension getPreferredSize() {
        Dimension inh = super.getPreferredSize();
        return new Dimension(Math.max(inh.width, DEFAULT_WIDTH), Math.max(inh.height, DEFAULT_HEIGHT));
    }

    private void showAdvancedSettings() {
        FormCustomEditorAdvanced fcea = new FormCustomEditorAdvanced(preCode, postCode);
        DialogDescriptor dd = new DialogDescriptor(
            fcea,
            FormEditor.getFormBundle().getString("CTL_AdvancedInitializationCode")      // NOI18N
            );
        dd.setHelpCtx(new HelpCtx(FormCustomEditorAdvanced.class));
        TopManager.getDefault().createDialog(dd).show();

        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            preCode = fcea.getPreCode();
            postCode = fcea.getPostCode();
        }
    }
    
    // -----------------------------------------------------------------------------
    // EnhancedCustomPropertyEditor implementation

    /** Get the customized property value.
     * @return the property value
     * @exception InvalidStateException when the custom property editor does not contain a valid property value
     *(and thus it should not be set)
     */
    public Object getPropertyValue() throws IllegalStateException {
        Component currentCustomEditor = getCurrentCustomPropertyEditor();
        PropertyEditor currentEditor = getCurrentPropertyEditor();

        if (currentEditor != null) {
            editor.commitModifiedEditor();
        }

        editor.getProperty().setPreCode(preCode); // [PENDING - change only if modified]
        editor.getProperty().setPostCode(postCode);

        if (currentCustomEditor instanceof EnhancedCustomPropertyEditor) {
            return((EnhancedCustomPropertyEditor)currentCustomEditor).getPropertyValue();
        }
        if (currentEditor != null) {
            return currentEditor.getValue();
        }

        return editor.getValue(); 
    }

    public PropertyEditor getCurrentPropertyEditor() {
        int index = jComboBox1.getSelectedIndex();
        return (index == -1) ? null : allEditors[index];
    }

    
    public Component getCurrentCustomPropertyEditor() {
        int index = jComboBox1.getSelectedIndex();
        return (index == -1) ? null : allCustomEditors[index];
    }
}
