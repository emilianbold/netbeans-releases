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

import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.util.Utilities;

import java.awt.BorderLayout;
import java.awt.Component;
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

// -----------------------------------------------------------------------------
// Private variables

  private FormPropertyEditor editor;
  private JTabbedPane tabs;
  private PropertyEditor[] allEditors;
  private Component[] allCustomEditors;
    
// -----------------------------------------------------------------------------
// Constructor

  public FormCustomEditor (FormPropertyEditor editor) {
    this.editor = editor;
    setBorder (new EmptyBorder (5, 5, 5, 5));
    setLayout (new BorderLayout ());

    allEditors = FormPropertyEditorManager.getAllEditors (editor.getPropertyType (), false);
    allCustomEditors = new Component[allEditors.length];
    PropertyEditor currentlyUsedEditor = editor.getCurrentEditor ();
    if (currentlyUsedEditor == null) {
      currentlyUsedEditor = allEditors[0];
      editor.setModifiedEditor (currentlyUsedEditor);
    } else if (!currentlyUsedEditor.getClass ().equals (allEditors[0].getClass ())) {
      // if the current editor does not match any of available ones, we will use the first available instead of it
      editor.setModifiedEditor (currentlyUsedEditor);
    }

    if (allEditors.length == 1) {
      if (allEditors[0].supportsCustomEditor ()) {
        add (allCustomEditors[0] = allEditors[0].getCustomEditor (), BorderLayout.CENTER);
      } else {
        // [PENDING - add property sheet line component]
        add (allCustomEditors[0] = new JLabel ("PropertyEditor does not support custom editing"), BorderLayout.CENTER);
      }
      
    } else {
      tabs = new JTabbedPane ();
      int indexToSelect = -1;
      for (int i = 0; i < allEditors.length; i++) {
        if (allEditors[i].getClass ().equals (currentlyUsedEditor.getClass ()) && (indexToSelect == -1)) {
          allEditors[i].setValue (editor.getValue ());
          indexToSelect = i;
        }
        if (allEditors[i] instanceof FormAwareEditor) {
          ((FormAwareEditor)allEditors[i]).setRADComponent (editor.getRADComponent ());
        }
        if (allEditors[i].supportsCustomEditor ()) {
          tabs.addTab (Utilities.getShortClassName (allEditors[i].getClass ()), allCustomEditors[i] = allEditors[i].getCustomEditor ());
        } else {
          // [PENDING - add property sheet line component]
          tabs.addTab (Utilities.getShortClassName (allEditors[i].getClass ()), allCustomEditors[i] = new JLabel ("PropertyEditor does not support custom editing"));
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
