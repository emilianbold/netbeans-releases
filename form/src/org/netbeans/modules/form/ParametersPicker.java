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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.*;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.TopManager;
import org.openide.ErrorManager;

/** The ParametersPicker is a panel which allows to enter a method parameter data.
 *
 * @author  Ian Formanek
 */
public class ParametersPicker extends javax.swing.JPanel implements EnhancedCustomPropertyEditor {

    static final long serialVersionUID =1116033799965380000L;
    /** Initializes the Form */
    public ParametersPicker(FormModel formModel, /*RADComponent sourceComponent,*/ Class requiredType) {
        initComponents();
        this.requiredType = requiredType;
        this.formModel = formModel;
//        this.sourceComponent = sourceComponent;

        javax.swing.ButtonGroup bg = new javax.swing.ButtonGroup();
        bg.add(valueButton);
        bg.add(beanButton);
        bg.add(propertyButton);
        bg.add(methodButton);
        bg.add(codeButton);

        if ((!requiredType.isPrimitive()) &&
            (!requiredType.equals(String.class))) {
            valueButton.setEnabled(false);
            propertyButton.setSelected(true);
        }

        // localize components
        setBorder(new javax.swing.border.CompoundBorder(
            new javax.swing.border.TitledBorder(
                new javax.swing.border.EtchedBorder(), " " + FormEditor.getFormBundle().getString("CTL_CW_GetParametersFrom") + " "), // "Get Parameter From:"
            new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5))));
        valueButton.setText(FormEditor.getFormBundle().getString("CTL_CW_Value")); // "Value:"
        beanButton.setText(FormEditor.getFormBundle().getString("CTL_CW_Bean")); // "Bean:"
        propertyButton.setText(FormEditor.getFormBundle().getString("CTL_CW_Property")); // "Property:"
        propertyLabel.setText(FormEditor.getFormBundle().getString("CTL_CW_NoProperty")); // "<No Property Selected>"
        methodButton.setText(FormEditor.getFormBundle().getString("CTL_CW_Method")); // "Method Call:"
        methodLabel.setText(FormEditor.getFormBundle().getString("CTL_CW_NoMethod")); // "<No Method Selected>"
        codeButton.setText(FormEditor.getFormBundle().getString("CTL_CW_UserCode")); // "User Code:"

        beansList = new ArrayList();
        DefaultComboBoxModel beanComboModel = new DefaultComboBoxModel();
        beanComboModel.addElement(FormEditor.getFormBundle().getString("CTL_CW_SelectBean"));
        for (Iterator it = formModel.getMetaComponents().iterator(); it.hasNext();) {
            RADComponent radComp =(RADComponent)it.next();
            if (requiredType.isAssignableFrom(radComp.getBeanClass())) {
                beansList.add(radComp);
                if (radComp == formModel.getTopRADComponent()) {
                    beanComboModel.addElement(FormEditor.getFormBundle().getString("CTL_FormTopContainerName"));
                } else {
                    beanComboModel.addElement(radComp.getName());
                }
            }
        }
        if (beansList.size() > 0) {
            beanCombo.setModel(beanComboModel);
            beanCombo.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent evt) {
                    int index = beanCombo.getSelectedIndex();
                    if (index == 0) {
                        selectedComponent = null;
                    } else {
                        selectedComponent =(RADComponent)beansList.get(index - 1);
                    }
                    fireStateChange();
                }
            }
                                      );
        } else {
            beanButton.setEnabled(false);    // no beans on the form are of the required type
        }

        codeArea.setContentType("text/x-java");    // allow syntax coloring // NOI18N

        updateParameterTypes();
        currentFilledState = isFilled();

        HelpCtx.setHelpIDString(this, "gui.source.modifying.property"); // NOI18N
    }

    public void setPropertyValue(RADConnectionPropertyEditor.RADConnectionDesignValue value) {
        if (value == null) return; // can happen if starting without previously set value

        switch (value.type) {
            case RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_VALUE:
                valueButton.setSelected(true);
                valueField.setText(value.value);
                break;
            case RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_BEAN:
                beanButton.setSelected(true);
                selectedComponent = value.getRADComponent();
                int index = beansList.indexOf(selectedComponent);
                if (index == -1) {
                    beanCombo.setSelectedIndex(0);
                } else {
                    beanCombo.setSelectedIndex(index+1);
                }
                break;
            case RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_PROPERTY:
                propertyButton.setSelected(true);
                selectedComponent = value.getRADComponent();
                selectedProperty = value.getProperty();
                if (selectedComponent == formModel.getTopRADComponent()) {
                    propertyLabel.setText(selectedProperty.getName());
                } else {
                    propertyLabel.setText(selectedComponent.getName() + "." + selectedProperty.getName()); // NOI18N
                }
                break;
            case RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_METHOD:
                methodButton.setSelected(true);
                selectedComponent = value.getRADComponent();
                selectedMethod = value.getMethod();
                if (selectedComponent == formModel.getTopRADComponent()) {
                    methodLabel.setText(selectedMethod.getName());
                } else {
                    methodLabel.setText(selectedComponent.getName() + "." + selectedMethod.getName()); // NOI18N
                }
                break;
            case RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_CODE:
            default:
                codeButton.setSelected(true);
                codeArea.setText(value.userCode);
                break;
        }

        // update enabled state
        updateParameterTypes();
    }

    // ----------------------------------------------------------------------------------------
    // EnhancedCustomPropertyEditor implementation

    /** Get the customized property value.
     * @return the property value
     * @exception InvalidStateException when the custom property editor does not contain a valid property value
     *(and thus it should not be set)
     */
    public Object getPropertyValue() throws IllegalStateException {
        if (!isFilled()) {
            IllegalStateException exc = new IllegalStateException();
            TopManager.getDefault().getErrorManager().annotate(
                exc, ErrorManager.USER, null, 
                FormEditor.getFormBundle().getString("ERR_NothingEntered"), // NOI18N
                null, null);
            throw exc;
        }

        if (valueButton.isSelected()) {
            return new RADConnectionPropertyEditor.RADConnectionDesignValue(requiredType, valueField.getText());
        } else if (beanButton.isSelected()) {
            return new RADConnectionPropertyEditor.RADConnectionDesignValue(selectedComponent);
        } else if (codeButton.isSelected()) {
            return new RADConnectionPropertyEditor.RADConnectionDesignValue(codeArea.getText());
        } else if (propertyButton.isSelected()) {
            return new RADConnectionPropertyEditor.RADConnectionDesignValue(selectedComponent, selectedProperty);
        } else if (methodButton.isSelected()) {
            return new RADConnectionPropertyEditor.RADConnectionDesignValue(selectedComponent, selectedMethod);
        } else return null;
    }

    // ----------------------------------------------------------------------------------------
    // end of EnhancedCustomPropertyEditor implementation

    public String getPreviewText() {
        if (!isFilled())
            return FormEditor.getFormBundle().getString("CTL_CW_NotSet"); // "<not set>"
        if (codeButton.isSelected()) {
            return FormEditor.getFormBundle().getString("CTL_CW_Code"); // "<code>";
        }
        return getText();
    }

    public String getText() {
        if (!isFilled())
            return FormEditor.getFormBundle().getString("CTL_CW_NotSet"); // "<not set>"
        if (valueButton.isSelected()) {
            if (requiredType.equals(String.class)) {
                String s = valueField.getText();
                s = Utilities.replaceString(s, "\\", "\\\\"); // fixes bug 835 // NOI18N
                s = Utilities.replaceString(s, "\"", "\\\""); // NOI18N
                return "\""+s+"\""; // NOI18N
            }
            else
                return(valueField.getText() != null) ? valueField.getText() : ""; // NOI18N
        } else if (codeButton.isSelected()) {
            return codeArea.getText();
        } else if (beanButton.isSelected()) {
            if (selectedComponent == formModel.getTopRADComponent()) {
                return "this"; // NOI18N
            } else {
                return(selectedComponent.getName());
            }
        } else if (propertyButton.isSelected()) {
            StringBuffer sb = new StringBuffer();
            if (selectedComponent != formModel.getTopRADComponent()) {
                sb.append(selectedComponent.getName());
                sb.append("."); // NOI18N
            }
            if (selectedProperty != null) {
                sb.append(selectedProperty.getReadMethod().getName());
                sb.append("()"); // NOI18N
            } else {
                sb.append("???"); // NOI18N
            }
            return  sb.toString();
        } else if (methodButton.isSelected()) {
            StringBuffer sb = new StringBuffer();
            if (selectedComponent != formModel.getTopRADComponent()) {
                sb.append(selectedComponent.getName());
                sb.append("."); // NOI18N
            }
            sb.append(selectedMethod.getName()); // [FUTURE: - method parameters]
            sb.append("()"); // NOI18N
            return  sb.toString();
        } else return ""; // NOI18N
    }

    public boolean isFilled() {
        if (codeButton.isSelected()) {
            if (requiredType.equals(String.class)) return true;
            else return !"".equals(codeArea.getText()); // NOI18N
        } else if (beanButton.isSelected()) {
            return(selectedComponent != null);
        } else if (propertyButton.isSelected()) {
            return(selectedProperty != null);
        } else if (valueButton.isSelected()) {
            if (requiredType.equals(String.class)) return true;
            else return !"".equals(valueField.getText()); // NOI18N
        } else if (methodButton.isSelected()) {
            return(selectedMethod != null);
        } else return false;
    }

    public synchronized void addChangeListener(ChangeListener l) {
        if (listeners == null)
            listeners = new ArrayList();
        listeners.add(l);
    }

    public synchronized void removeListener(ChangeListener l) {
        if (listeners == null)
            return;
        listeners.remove(l);
    }

    private synchronized void fireStateChange() {
        if (listeners == null)
            return;
        ArrayList list =(ArrayList)listeners.clone();
        ChangeEvent evt = new ChangeEvent(this);
        for (Iterator it = list.iterator(); it.hasNext();)
            ((ChangeListener)it.next()).stateChanged(evt);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        valueButton = new javax.swing.JRadioButton();
        valueField = new javax.swing.JTextField();
        beanButton = new javax.swing.JRadioButton();
        beanCombo = new javax.swing.JComboBox();
        propertyButton = new javax.swing.JRadioButton();
        propertyLabel = new javax.swing.JLabel();
        propertyDetailsButton = new javax.swing.JButton();
        methodButton = new javax.swing.JRadioButton();
        methodLabel = new javax.swing.JLabel();
        methodDetailsButton = new javax.swing.JButton();
        codeButton = new javax.swing.JRadioButton();
        codeScrollPane = new javax.swing.JScrollPane();
        codeArea = new javax.swing.JEditorPane();
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        valueButton.setSelected(true);
        valueButton.setText(FormEditor.getFormBundle ().getString ("CTL_CW_Value"));
        valueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeButtonPressed(evt);
            }
        }
        );
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(valueButton, gridBagConstraints1);
        
        
        valueField.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                updateState(evt);
            }
        }
        );
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 5, 0);
        gridBagConstraints1.weightx = 1.0;
        add(valueField, gridBagConstraints1);
        
        
        beanButton.setText(FormEditor.getFormBundle ().getString ("CTL_CW_Bean"));
        beanButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeButtonPressed(evt);
            }
        }
        );
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(beanButton, gridBagConstraints1);
        
        
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 5, 0);
        add(beanCombo, gridBagConstraints1);
        
        
        propertyButton.setText(FormEditor.getFormBundle ().getString ("CTL_CW_Property"));
        propertyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeButtonPressed(evt);
            }
        }
        );
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(propertyButton, gridBagConstraints1);
        
        
        propertyLabel.setText(FormEditor.getFormBundle ().getString ("CTL_CW_NoProperty"));
        propertyLabel.setEnabled(false);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.weightx = 1.0;
        add(propertyLabel, gridBagConstraints1);
        
        
        propertyDetailsButton.setText("...");
        propertyDetailsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                propertyDetailsButtonActionPerformed(evt);
            }
        }
        );
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.insets = new java.awt.Insets(5, 0, 0, 0);
        add(propertyDetailsButton, gridBagConstraints1);
        
        
        methodButton.setText(FormEditor.getFormBundle ().getString ("CTL_CW_MethodCall"));
        methodButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeButtonPressed(evt);
            }
        }
        );
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(methodButton, gridBagConstraints1);
        
        
        methodLabel.setText(FormEditor.getFormBundle ().getString ("CTL_CW_NoMethod"));
        methodLabel.setEnabled(false);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.weightx = 1.0;
        add(methodLabel, gridBagConstraints1);
        
        
        methodDetailsButton.setText("...");
        methodDetailsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                methodDetailsButtonActionPerformed(evt);
            }
        }
        );
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.insets = new java.awt.Insets(5, 0, 0, 0);
        add(methodDetailsButton, gridBagConstraints1);
        
        
        codeButton.setText(FormEditor.getFormBundle ().getString ("CTL_CW_UserCode"));
        codeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeButtonPressed(evt);
            }
        }
        );
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(codeButton, gridBagConstraints1);
        
        
        
        codeArea.setEditable(false);
          codeArea.addCaretListener(new javax.swing.event.CaretListener() {
              public void caretUpdate(javax.swing.event.CaretEvent evt) {
                  updateState(evt);
              }
          }
          );
          codeScrollPane.setViewportView(codeArea);
          
          
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(5, 0, 0, 0);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(codeScrollPane, gridBagConstraints1);
        
    }//GEN-END:initComponents



    private void methodDetailsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_methodDetailsButtonActionPerformed
        MethodPicker picker = new MethodPicker(null, formModel, null, requiredType);
        picker.setSelectedComponent(selectedComponent);
        picker.setSelectedMethod(selectedMethod);

        picker.show();
        if (picker.getReturnStatus() == MethodPicker.OK) {
            selectedComponent = picker.getSelectedComponent();
            selectedMethod = picker.getSelectedMethod();
            methodLabel.setEnabled(true);
            if (selectedComponent == formModel.getTopRADComponent()) {
                methodLabel.setText(selectedMethod.getName());
            } else {
                methodLabel.setText(selectedComponent.getName() + "." + selectedMethod.getName()); // NOI18N
            }
            methodLabel.repaint();
            fireStateChange();
        }
    }//GEN-LAST:event_methodDetailsButtonActionPerformed

    private void updateState(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_updateState
        fireStateChange();
        codeArea.getCaret().setVisible(codeButton.isSelected() && codeArea.hasFocus());
    }//GEN-LAST:event_updateState

    private void propertyDetailsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_propertyDetailsButtonActionPerformed
        if (propertyPicker == null) {
            propertyPicker = new PropertyPicker(null, formModel, null, requiredType);
        }
        propertyPicker.setSelectedComponent(selectedComponent);
        propertyPicker.setSelectedProperty(selectedProperty);
        propertyPicker.show();
        if (propertyPicker.getReturnStatus() == PropertyPicker.OK) {
            selectedComponent = propertyPicker.getSelectedComponent();
            selectedProperty = propertyPicker.getSelectedProperty();
            propertyLabel.setEnabled(true);
            if (selectedComponent == formModel.getTopRADComponent()) {
                propertyLabel.setText(selectedProperty.getName());
            } else {
                propertyLabel.setText(selectedComponent.getName() + "." + selectedProperty.getName()); // NOI18N
            }
            propertyLabel.repaint();
            fireStateChange();
        }
    }//GEN-LAST:event_propertyDetailsButtonActionPerformed

    private void typeButtonPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeButtonPressed
        updateParameterTypes();
        if (beanButton.isSelected()) {
            beanCombo.requestFocus();
        } else if (codeButton.isSelected()) {
            codeArea.requestFocus();
        } else if (propertyButton.isSelected()) {
            propertyDetailsButton.requestFocus();
        } else if (methodButton.isSelected()) {
            methodDetailsButton.requestFocus();
        } else if (valueButton.isSelected()) {
            valueField.requestFocus();
        }
    }//GEN-LAST:event_typeButtonPressed

    private void updateParameterTypes() {
        valueField.setEnabled(valueButton.isSelected());
        beanCombo.setEnabled(beanButton.isSelected());
        if (!propertyButton.isSelected()) {
            propertyLabel.setText(FormEditor.getFormBundle().getString("CTL_CW_NoProperty")); // "<No Property Selected>"
        }
        propertyLabel.setEnabled(propertyButton.isSelected());
        propertyLabel.repaint();
        propertyDetailsButton.setEnabled(propertyButton.isSelected());

        if (!methodButton.isSelected()) {
            methodLabel.setText(FormEditor.getFormBundle().getString("CTL_CW_NoMethod")); // "<No Method Selected>"
        }
        methodLabel.setEnabled(methodButton.isSelected());
        methodLabel.repaint();
        methodDetailsButton.setEnabled(methodButton.isSelected());
        codeArea.setEditable(codeButton.isSelected());
        codeArea.getCaret().setVisible(codeButton.isSelected() && codeArea.hasFocus());
        fireStateChange();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton valueButton;
    private javax.swing.JTextField valueField;
    private javax.swing.JRadioButton beanButton;
    private javax.swing.JComboBox beanCombo;
    private javax.swing.JRadioButton propertyButton;
    private javax.swing.JLabel propertyLabel;
    private javax.swing.JButton propertyDetailsButton;
    private javax.swing.JRadioButton methodButton;
    private javax.swing.JLabel methodLabel;
    private javax.swing.JButton methodDetailsButton;
    private javax.swing.JRadioButton codeButton;
    private javax.swing.JScrollPane codeScrollPane;
    private javax.swing.JEditorPane codeArea;
    // End of variables declaration//GEN-END:variables

    private FormModel formModel;
//    private RADComponent sourceComponent;
    private Class requiredType;

    private PropertyPicker propertyPicker;
    private MethodPicker methodPicker;

    private String selectedPropertyText = null;
    private ArrayList listeners;
    private boolean currentFilledState;
    private RADComponent selectedComponent;
    private PropertyDescriptor selectedProperty;
    private MethodDescriptor selectedMethod;

    private ArrayList beansList;
    private DefaultComboBoxModel beanComboModel;
}
