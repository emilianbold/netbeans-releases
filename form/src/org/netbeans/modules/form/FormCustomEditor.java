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

import java.awt.*;
import java.beans.PropertyEditor;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.border.EmptyBorder;

/** 
 *
 * @author  Ian Formanek, Vladimir Zboril
 */
public class FormCustomEditor extends JPanel
                              implements EnhancedCustomPropertyEditor {

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

        advancedButton.setText(FormEditor.getFormBundle().getString("CTL_Advanced")); // NOI18N
        advancedButton.setMnemonic(FormEditor.getFormBundle().getString(
                                        "CTL_Advanced_mnemonic").charAt(0)); // NOI18N
        advancedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showAdvancedSettings();
            }
        });
        
        jLabel1.setText(FormEditor.getFormBundle().getString("LAB_SelectMode")); //NOI18N
        jLabel1.setDisplayedMnemonic((FormEditor.getFormBundle().getString(
                                        "LAB_SelectMode.mnemonic").charAt(0))); //NOI18N
        jLabel1.setLabelFor(editorsCombo);
        
        HelpCtx.setHelpIDString(cardPanel, getClass().getName() + ".tabbedPane"); // NOI18N

        this.editor = editor;
        preCode = editor.getProperty().getPreCode();
        postCode = editor.getProperty().getPostCode();
        allEditors = editor.getAllEditors();

        PropertyEditor currentEditor = editor.getModifiedEditor();
        if (currentEditor == null) {
            currentEditor = allEditors[0];
            editor.setModifiedEditor(currentEditor);
        }
      
        int currentIndex = -1;

        for (int i=0; i < allEditors.length; i++)
            if (allEditors[i].getClass().equals(currentEditor.getClass())) {
                currentIndex = i;
                break;
            }

        if (currentIndex == -1) {
            PropertyEditor[] editors = new PropertyEditor[allEditors.length+1];
            editors[0] = currentEditor;
            System.arraycopy(allEditors, 0, editors, 1, allEditors.length);
            allEditors = editors;
            currentIndex = 0;
        }

        allCustomEditors = new Component[allEditors.length];

        Object currentValue = editor.getValue();

        for (int i=0; i < allEditors.length; i++) {
            PropertyEditor prEd = allEditors[i];
            editor.getPropertyContext().initPropertyEditor(prEd);

            if (i == currentIndex) { // this is the currently used editor
                prEd.setValue(currentValue);
            }
            else {
                boolean valueSet = false;
                if (currentValue != null) {
                    if (editor.getPropertyType().isAssignableFrom(
                                                   currentValue.getClass())) {
                        // currentValue contains a real property value
                        prEd.setValue(currentValue);
                        valueSet = true;
                    }
                    else if (currentValue instanceof FormDesignValue) {
                        Object realValue =
                            ((FormDesignValue)currentValue).getDesignValue();
                        if (realValue != FormDesignValue.IGNORED_VALUE) {
                            // current value is FormDesignValue with known real value
                            prEd.setValue(realValue); 
                            valueSet = true;
                        }
                    }
                }
                if (!valueSet) {
                    Object defaultValue = editor.getProperty().getDefaultValue();
                    if (defaultValue != null)
                        prEd.setValue(defaultValue);
                }
            }

            String editorName = prEd instanceof NamedPropertyEditor ?
                        ((NamedPropertyEditor)prEd).getDisplayName() :
                        Utilities.getShortClassName(prEd.getClass());

            Component custEd = null;
            if (!prEd.supportsCustomEditor()
                    || (custEd = prEd.getCustomEditor()) instanceof Window) {
                JPanel p = new JPanel(new GridBagLayout());
                p.add(new JLabel(FormEditor.getFormBundle().getString(
                                            "CTL_PropertyEditorDoesNot"))); // NOI18N
                custEd = p;
            }

            allCustomEditors[i] = custEd;
            cardPanel.add(editorName, custEd);
            editorsCombo.addItem(editorName);
        }

        editorsCombo.setSelectedIndex(currentIndex);
        CardLayout cl = (CardLayout) cardPanel.getLayout();
        cl.show(cardPanel, (String) editorsCombo.getSelectedItem());

        editorsCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CardLayout cl = (CardLayout) cardPanel.getLayout();
                cl.show(cardPanel, (String) editorsCombo.getSelectedItem());
                FormCustomEditor.this.editor.setModifiedEditor(getCurrentPropertyEditor());
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        editorsCombo = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        cardPanel = new javax.swing.JPanel();
        advancedButton = new javax.swing.JButton();
        
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.insets = new java.awt.Insets(12, 5, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(editorsCombo, gridBagConstraints1);
        
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
        cardPanel.setLayout(new java.awt.CardLayout());
        
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 0;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints2.insets = new java.awt.Insets(12, 12, 11, 11);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.weighty = 1.0;
        jPanel1.add(cardPanel, gridBagConstraints2);
        
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox editorsCombo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel cardPanel;
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
        int index = editorsCombo.getSelectedIndex();
        return (index == -1) ? null : allEditors[index];
    }

    
    public Component getCurrentCustomPropertyEditor() {
        int index = editorsCombo.getSelectedIndex();
        return (index == -1) ? null : allCustomEditors[index];
    }
}
