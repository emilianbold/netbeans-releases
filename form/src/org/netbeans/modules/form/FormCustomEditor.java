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

import org.openide.*;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.util.Utilities;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyEditor;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author  Ian Formanek
 */
public class FormCustomEditor extends JPanel implements EnhancedCustomPropertyEditor {

  private static final int DEFAULT_WIDTH  = 350;
  private static final int DEFAULT_HEIGHT = 350;

// -----------------------------------------------------------------------------
// Private variables

  private FormPropertyEditor editor;
  private JTabbedPane tabs;
  private PropertyEditor[] allEditors;
  private Component[] allCustomEditors;
    
  private String preCode;
  private String postCode;
  
// -----------------------------------------------------------------------------
// Constructor

  static final long serialVersionUID =-5566324092702416875L;

  public FormCustomEditor (FormPropertyEditor editor) {
    this.editor = editor;
    setBorder (new EmptyBorder (5, 5, 5, 5));
    setLayout (new BorderLayout ());

    preCode = editor.getRADProperty ().getPreCode ();
    postCode = editor.getRADProperty ().getPostCode ();
    
    allEditors = editor.getAllEditors ();
    allCustomEditors = new Component[allEditors.length];
    PropertyEditor currentlyUsedEditor = editor.getModifiedEditor ();
    if (currentlyUsedEditor == null) {
      currentlyUsedEditor = allEditors[0];
      editor.setModifiedEditor (currentlyUsedEditor);
    } else if (!currentlyUsedEditor.getClass ().equals (allEditors[0].getClass ())) {
      // if the current editor does not match any of available ones, we will use the first available instead of it
      editor.setModifiedEditor (currentlyUsedEditor);
    }

    if (allEditors.length == 1) {
      if (allEditors[0] instanceof FormAwareEditor) {
        ((FormAwareEditor)allEditors[0]).setRADComponent (editor.getRADComponent (), editor.getRADProperty ());
      }

      if (allEditors[0] instanceof org.openide.explorer.propertysheet.editors.NodePropertyEditor) {
        ((org.openide.explorer.propertysheet.editors.NodePropertyEditor)allEditors[0]).attach (new org.openide.nodes.Node[] { editor.getRADComponent ().getNodeReference () });
      }

      allEditors[0].setValue (editor.getValue ());

      if (allEditors[0].supportsCustomEditor ()) {
        add (allCustomEditors[0] = allEditors[0].getCustomEditor (), BorderLayout.CENTER);
      } else {
        // [FUTURE - add property sheet line component]
        add (allCustomEditors[0] = new JLabel ("PropertyEditor does not support custom editing"), BorderLayout.CENTER);
      }
      
    } else {
      tabs = new JTabbedPane ();
      int indexToSelect = -1;
      for (int i = 0; i < allEditors.length; i++) {
        if (allEditors[i] instanceof FormAwareEditor) {
          ((FormAwareEditor)allEditors[i]).setRADComponent (editor.getRADComponent (), editor.getRADProperty ());
        }

        if (allEditors[i] instanceof org.openide.explorer.propertysheet.editors.NodePropertyEditor) {
          ((org.openide.explorer.propertysheet.editors.NodePropertyEditor)allEditors[i]).attach (new org.openide.nodes.Node[] { editor.getRADComponent ().getNodeReference () });
        }

        if (allEditors[i].getClass ().equals (currentlyUsedEditor.getClass ()) && (indexToSelect == -1)) {
          allEditors[i].setValue (editor.getValue ());
          indexToSelect = i;
        } else {
          Object currValue = editor.getValue ();
          boolean valueSet = false;
          if (currValue != null) {
            if (editor.getPropertyType ().isAssignableFrom (currValue.getClass ())) {
              allEditors[i].setValue (currValue); // current value is of the real property type
              valueSet = true;
            } else if (currValue instanceof FormDesignValue) {
              Object desValue = ((FormDesignValue)currValue).getDesignValue (editor.getRADComponent ());
              if (desValue != FormDesignValue.IGNORED_VALUE) {
                allEditors[i].setValue (desValue); // current value is of the real property type
                valueSet = true;
              }
            }
          } 
          if (!valueSet) {
            Object defValue = editor.getRADProperty ().getDefaultValue ();
            if (defValue != null) {
              allEditors[i].setValue (defValue);
            }
          }
        }

        String tabName;
        if (allEditors[i] instanceof NamedPropertyEditor) {
          tabName = ((NamedPropertyEditor)allEditors[i]).getDisplayName ();
        } else {
          tabName = Utilities.getShortClassName (allEditors[i].getClass ());
        }
        if (allEditors[i].supportsCustomEditor ()) {
          tabs.addTab (tabName, allCustomEditors[i] = allEditors[i].getCustomEditor ());
        } else {
          // [FUTURE - add property sheet line component]
          tabs.addTab (tabName, allCustomEditors[i] = new JLabel ("PropertyEditor does not support custom editing"));
        }
      }

      add (tabs, BorderLayout.CENTER);

      if (indexToSelect == -1) { 
        // if the current editor does not match any of available ones, we will use the first available instaed of it
        tabs.setSelectedIndex (0);
        editor.setModifiedEditor (allEditors[0]);
      } else {
        tabs.setSelectedIndex (indexToSelect);
      }

      tabs.addChangeListener (new ChangeListener () {
          public void stateChanged (ChangeEvent evt) {
            FormCustomEditor.this.editor.setModifiedEditor (getCurrentPropertyEditor ());
          }
        }
      );
    }

    JButton advancedButton = new JButton (FormEditor.getFormBundle ().getString ("CTL_Advanced"));
    advancedButton.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          showAdvancedSettings ();
        }
      }
    );

    JPanel advancedPanel = new JPanel ();
    advancedPanel.setLayout (new java.awt.FlowLayout (java.awt.FlowLayout.LEFT, 0, 0));
    advancedPanel.setBorder (new EmptyBorder (8, 0, 0, 0));
    advancedPanel.add (advancedButton);
    add (advancedPanel, BorderLayout.SOUTH);
  }

  public Dimension getPreferredSize () {
    Dimension inh = super.getPreferredSize ();
    return new Dimension (Math.max (inh.width, DEFAULT_WIDTH), Math.max (inh.height, DEFAULT_HEIGHT));
  }

  private void showAdvancedSettings () {
    FormCustomEditorAdvanced fcea = new FormCustomEditorAdvanced (preCode, postCode);
    DialogDescriptor dd;
    TopManager.getDefault ().createDialog (dd = new DialogDescriptor (
        fcea,
        "Advanced Initialization Code"
      )
    ).show ();
    
    if (dd.getValue () == DialogDescriptor.OK_OPTION) {
      preCode = fcea.getPreCode ();
      postCode =fcea.getPostCode ();
    }
  }

// -----------------------------------------------------------------------------
// EnhancedCustomPropertyEditor implementation

  /** Get the customized property value.
  * @return the property value
  * @exception InvalidStateException when the custom property editor does not contain a valid property value
  *            (and thus it should not be set)
  */
  public Object getPropertyValue () throws IllegalStateException {
    Component currentCustomEditor = getCurrentCustomPropertyEditor ();
    PropertyEditor currentEditor = getCurrentPropertyEditor ();

    if (currentEditor != null) {
      editor.commitModifiedEditor ();
    }
    
    editor.getRADProperty ().setPreCode (preCode); // [PENDING - change only if modified]
    editor.getRADProperty ().setPostCode (postCode);
    
    if (currentCustomEditor instanceof EnhancedCustomPropertyEditor) {
      return ((EnhancedCustomPropertyEditor)currentCustomEditor).getPropertyValue ();
    }
    if (currentEditor != null) {
      return currentEditor.getValue ();
    }
    
    return editor.getValue ();
  }

  public PropertyEditor getCurrentPropertyEditor () {
    int index = 0;
    if (tabs != null) {
      index = tabs.getSelectedIndex ();
      if (index == -1) {
        return null;
      }
    }
    return allEditors[index];
  }

  public Component getCurrentCustomPropertyEditor () {
    int index = 0;
    if (tabs != null) index = tabs.getSelectedIndex ();
    if (index == -1) {
      return null;
    }
    return allCustomEditors[index];
  }
}

/*
 * Log
 *  16   Gandalf   1.15        9/12/99  Ian Formanek    FormAwareEditor.setRADComponent
 *        changed, advanced dialog for Pre/Post code invocation
 *  15   Gandalf   1.14        9/6/99   Ian Formanek    Fixed bug 3187 - 
 *       Property editor of layout, model (and maybe more) could be bigger.
 *  14   Gandalf   1.13        8/17/99  Ian Formanek    Furhet improved value 
 *       used for multiple editors, employed NamedPropertyEditor
 *  13   Gandalf   1.12        8/17/99  Ian Formanek    Fixed work with multiple
 *       property editors
 *  12   Gandalf   1.11        8/10/99  Ian Formanek    Generated Serial Version
 *       UID
 *  11   Gandalf   1.10        8/1/99   Ian Formanek    NodePropertyEditor 
 *       employed
 *  10   Gandalf   1.9         8/1/99   Ian Formanek    
 *  9    Gandalf   1.8         7/23/99  Ian Formanek    Fixes problem with 
 *       properties, where the RADConnectionPropertryEditor is the only one 
 *       available
 *  8    Gandalf   1.7         6/30/99  Ian Formanek    reflecting change in 
 *       enhanced property editors interfaces
 *  7    Gandalf   1.6         6/24/99  Ian Formanek    Improved 
 *       FormPropertyEditor towards accepting multiple editors
 *  6    Gandalf   1.5         6/23/99  Ian Formanek    
 *  5    Gandalf   1.4         6/22/99  Ian Formanek    Further tweaked for 
 *       multiple (custom) editors
 *  4    Gandalf   1.3         6/22/99  Ian Formanek    Fixed working with 
 *       FormAwareEditors
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/30/99  Ian Formanek    
 *  1    Gandalf   1.0         5/24/99  Ian Formanek    
 * $
 */
