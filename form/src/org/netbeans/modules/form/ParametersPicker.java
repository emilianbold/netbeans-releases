/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.loaders.form;

import java.beans.*;
import java.util.Vector;
import java.util.Enumeration;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.netbeans.ide.util.Utilities;

/** The ParametersPicker is a panel which allows to enter a method parameter data.
*
* @author  Ian Formanek
* @version 1.00, Aug 29, 1998
*/
public class ParametersPicker extends javax.swing.JPanel {

  /** Initializes the Form */
  public ParametersPicker(FormManager manager, RADComponent sourceComponent, Class requiredType) {
    initComponents ();
    this.requiredType = requiredType;
    this.manager = manager;
    this.sourceComponent = sourceComponent;

    javax.swing.ButtonGroup bg = new javax.swing.ButtonGroup ();
    bg.add (valueButton);
    bg.add (propertyButton);
    bg.add (methodButton);
    bg.add (codeButton);

    if ((!requiredType.isPrimitive ()) &&
        (!requiredType.equals (String.class))) {
      valueButton.setEnabled (false);
      propertyButton.setSelected (true);
    }

    // localize components
    setBorder (new javax.swing.border.CompoundBorder (
      new javax.swing.border.TitledBorder (
      new javax.swing.border.EtchedBorder (), " " + FormEditor.getFormBundle ().getString ("CTL_CW_GetParametersFrom") + " "), // "Get Parameter From:"
      new javax.swing.border.EmptyBorder (new java.awt.Insets(5, 5, 5, 5))));
    valueButton.setText (FormEditor.getFormBundle ().getString ("CTL_CW_Value")); // "Value:"
    propertyButton.setText (FormEditor.getFormBundle ().getString ("CTL_CW_Property")); // "Property:"
    propertyLabel.setText (FormEditor.getFormBundle ().getString ("CTL_CW_NoProperty")); // "<No Property Selected>"
    methodButton.setText (FormEditor.getFormBundle ().getString ("CTL_CW_Method")); // "Method Call:"
    methodLabel.setText (FormEditor.getFormBundle ().getString ("CTL_CW_NoMethod")); // "<No Method Selected>"
    codeButton.setText (FormEditor.getFormBundle ().getString ("CTL_CW_UserCode")); // "User Code:"

    updateParameterTypes ();
    currentFilledState = isFilled ();
  }

  public String getPreviewText () {
    if (!isFilled ())
      return FormEditor.getFormBundle ().getString ("CTL_CW_NotSet"); // "<not set>"
    if (codeButton.isSelected ()) {
      return FormEditor.getFormBundle ().getString ("CTL_CW_Code"); // "<code>";
    }
    return getText ();
  }

  public String getText () {
    if (!isFilled ())
      return FormEditor.getFormBundle ().getString ("CTL_CW_NotSet"); // "<not set>"
    if (valueButton.isSelected ()) {
      if (requiredType.equals (String.class)) {
        String s = valueField.getText ();
        s = Utilities.replaceString (s, "\\", "\\\\"); // fixes bug 835
        s = Utilities.replaceString (s, "\"", "\\\"");
        return "\""+s+"\"";
      }
      else
        return (valueField.getText () != null) ? valueField.getText () : "";
    } else if (codeButton.isSelected ()) {
      return codeArea.getText ();
    } else if (propertyButton.isSelected ()) {
      StringBuffer sb = new StringBuffer ();
      if (!(selectedComponent instanceof FormContainer)) {
        sb.append (selectedComponent.getName ());
        sb.append (".");
      }
      sb.append (selectedProperty.getReadMethod ().getName ());
      sb.append (" ()");
      return  sb.toString ();
    } else if (methodButton.isSelected ()) {
      StringBuffer sb = new StringBuffer ();
      if (!(selectedComponent instanceof FormContainer)) { 
        sb.append (selectedComponent.getName ());
        sb.append (".");
      }
      sb.append (selectedMethod.getName ()); // [PENDING - method parameters]
      sb.append (" ()");
      return  sb.toString ();
    } else return "";
  }

  public boolean isFilled () {
    if (codeButton.isSelected ()) {
      if (requiredType.equals (String.class)) return true;
      else return !"".equals (codeArea.getText ());
    } else if (propertyButton.isSelected ()) {
      return (selectedComponent != null);
    } else if (valueButton.isSelected ()) {
      if (requiredType.equals (String.class)) return true;
      else return !"".equals (valueField.getText ());
    } else if (methodButton.isSelected ()) {
      return (selectedMethod != null);
    } else return false;
  }

  public synchronized void addChangeListener (ChangeListener l) {
    if (listeners == null)
      listeners = new Vector ();
    listeners.addElement (l);
  }

  public synchronized void removeListener (ChangeListener l) {
    if (listeners == null)
      return;
    listeners.removeElement (l);
  }

  private synchronized void fireStateChange () {
    if (listeners == null)
      return;
    Vector v = (Vector)listeners.clone ();
    ChangeEvent evt = new ChangeEvent (this);
    for (Enumeration e = v.elements (); e.hasMoreElements ();)
      ((ChangeListener)e.nextElement ()).stateChanged (evt);
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the FormEditor.
   */
  private void initComponents () {//GEN-BEGIN:initComponents
    setBorder (new javax.swing.border.CompoundBorder (
      new javax.swing.border.TitledBorder (
      new javax.swing.border.EtchedBorder (), " Get Parameter From: "),
      new javax.swing.border.EmptyBorder (new java.awt.Insets(5, 5, 5, 5))));
    setLayout (new java.awt.GridBagLayout ());
    java.awt.GridBagConstraints gridBagConstraints1;

    valueButton = new javax.swing.JRadioButton ();
    valueButton.setText ("Value:");
    valueButton.setSelected (true);
    valueButton.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          typeButtonPressed (evt);
        }
      }
    );
    gridBagConstraints1 = new java.awt.GridBagConstraints ();
    gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
    add (valueButton, gridBagConstraints1);

    valueField = new javax.swing.JTextField ();
    valueField.addCaretListener (new javax.swing.event.CaretListener () {
        public void caretUpdate (javax.swing.event.CaretEvent evt) {
          updateState (evt);
        }
      }
    );
    gridBagConstraints1 = new java.awt.GridBagConstraints ();
    gridBagConstraints1.gridwidth = 0;
    gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints1.weightx = 1.0;
    add (valueField, gridBagConstraints1);

    propertyButton = new javax.swing.JRadioButton ();
    propertyButton.setText ("Property:");
    propertyButton.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          typeButtonPressed (evt);
        }
      }
    );
    gridBagConstraints1 = new java.awt.GridBagConstraints ();
    gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
    add (propertyButton, gridBagConstraints1);

    propertyLabel = new javax.swing.JLabel ();
    propertyLabel.setEnabled (false);
    propertyLabel.setText ("<No Property Selected>");
    gridBagConstraints1 = new java.awt.GridBagConstraints ();
    gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints1.weightx = 1.0;
    add (propertyLabel, gridBagConstraints1);

    propertyDetailsButton = new javax.swing.JButton ();
    propertyDetailsButton.setText ("...");
    propertyDetailsButton.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          propertyDetailsButtonActionPerformed (evt);
        }
      }
    );
    gridBagConstraints1 = new java.awt.GridBagConstraints ();
    gridBagConstraints1.gridwidth = 0;
    gridBagConstraints1.insets = new java.awt.Insets (5, 0, 0, 0);
    add (propertyDetailsButton, gridBagConstraints1);

    methodButton = new javax.swing.JRadioButton ();
    methodButton.setText ("Method Call:");
    methodButton.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          typeButtonPressed (evt);
        }
      }
    );
    gridBagConstraints1 = new java.awt.GridBagConstraints ();
    gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
    add (methodButton, gridBagConstraints1);

    methodLabel = new javax.swing.JLabel ();
    methodLabel.setEnabled (false);
    methodLabel.setText ("<No Method Selected>");
    gridBagConstraints1 = new java.awt.GridBagConstraints ();
    gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints1.weightx = 1.0;
    add (methodLabel, gridBagConstraints1);

    methodDetailsButton = new javax.swing.JButton ();
    methodDetailsButton.setText ("...");
    methodDetailsButton.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          methodDetailsButtonActionPerformed (evt);
        }
      }
    );
    gridBagConstraints1 = new java.awt.GridBagConstraints ();
    gridBagConstraints1.gridwidth = 0;
    gridBagConstraints1.insets = new java.awt.Insets (5, 0, 0, 0);
    add (methodDetailsButton, gridBagConstraints1);

    codeButton = new javax.swing.JRadioButton ();
    codeButton.setText ("User Code:");
    codeButton.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          typeButtonPressed (evt);
        }
      }
    );
    gridBagConstraints1 = new java.awt.GridBagConstraints ();
    gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
    add (codeButton, gridBagConstraints1);

    codeScrollPane = new javax.swing.JScrollPane ();

      codeArea = new javax.swing.JTextArea ();
      codeArea.setEnabled (false);
      codeArea.addCaretListener (new javax.swing.event.CaretListener () {
          public void caretUpdate (javax.swing.event.CaretEvent evt) {
            updateState (evt);
          }
        }
      );

    codeScrollPane.setViewportView (codeArea);
    gridBagConstraints1 = new java.awt.GridBagConstraints ();
    gridBagConstraints1.gridwidth = 0;
    gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints1.insets = new java.awt.Insets (5, 0, 0, 0);
    gridBagConstraints1.weightx = 1.0;
    gridBagConstraints1.weighty = 1.0;
    add (codeScrollPane, gridBagConstraints1);

  }//GEN-END:initComponents

  private void methodDetailsButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_methodDetailsButtonActionPerformed
    MethodPicker picker = new MethodPicker (null, manager, sourceComponent, requiredType);
    picker.show ();
    if (picker.getReturnStatus () == MethodPicker.OK) {
      selectedComponent = picker.getSelectedComponent ();
      selectedMethod = picker.getSelectedMethod ();
      methodLabel.setEnabled (true);
      if (selectedComponent instanceof FormContainer) {
        methodLabel.setText (selectedMethod.getName ());
      } else {
        methodLabel.setText (selectedComponent.getName () + "." + selectedMethod.getName ());
      }
      methodLabel.repaint ();
      fireStateChange ();
    }
  }//GEN-LAST:event_methodDetailsButtonActionPerformed

  private void updateState (javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_updateState
    fireStateChange ();
  }//GEN-LAST:event_updateState

  private void propertyDetailsButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_propertyDetailsButtonActionPerformed
    if (propertyPicker == null) {
      propertyPicker = new PropertyPicker (null, manager, sourceComponent, requiredType);
    }
    propertyPicker.show ();
    if (propertyPicker.getReturnStatus () == PropertyPicker.OK) {
      selectedComponent = propertyPicker.getSelectedComponent ();
      selectedProperty = propertyPicker.getSelectedProperty ();
      propertyLabel.setEnabled (true);
      if (selectedComponent instanceof FormContainer) {
        propertyLabel.setText (selectedProperty.getName ());
      } else {
        propertyLabel.setText (selectedComponent.getName () + "." + selectedProperty.getName ());
      }
      propertyLabel.repaint ();
      fireStateChange ();
    }
  }//GEN-LAST:event_propertyDetailsButtonActionPerformed

  private void typeButtonPressed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeButtonPressed
    updateParameterTypes ();
  }//GEN-LAST:event_typeButtonPressed

  private void updateParameterTypes () {
    valueField.setEnabled (valueButton.isSelected ());
    propertyLabel.setEnabled (propertyButton.isSelected ());
    propertyLabel.repaint ();
    propertyDetailsButton.setEnabled (propertyButton.isSelected ());
    methodLabel.setEnabled (methodButton.isSelected ());
    methodLabel.repaint ();
    methodDetailsButton.setEnabled (methodButton.isSelected ());
    codeArea.setEnabled (codeButton.isSelected ());
    fireStateChange ();
  }

// Variables declaration - do not modify//GEN-BEGIN:variables
  javax.swing.JRadioButton valueButton;
  javax.swing.JTextField valueField;
  javax.swing.JRadioButton propertyButton;
  javax.swing.JLabel propertyLabel;
  javax.swing.JButton propertyDetailsButton;
  javax.swing.JRadioButton methodButton;
  javax.swing.JLabel methodLabel;
  javax.swing.JButton methodDetailsButton;
  javax.swing.JRadioButton codeButton;
  javax.swing.JScrollPane codeScrollPane;
  javax.swing.JTextArea codeArea;
// End of variables declaration//GEN-END:variables

  private FormManager manager;
  private RADComponent sourceComponent;
  private Class requiredType;

  private PropertyPicker propertyPicker;
  private MethodPicker methodPicker;

  private String selectedPropertyText = null;
  private Vector listeners;
  private boolean currentFilledState;
  private RADComponent selectedComponent;
  private PropertyDescriptor selectedProperty;
  private MethodDescriptor selectedMethod;
}

/*
 * Log
 *  2    Gandalf   1.1         5/15/99  Ian Formanek    
 *  1    Gandalf   1.0         5/13/99  Ian Formanek    
 * $
 */
