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

import com.netbeans.ide.util.Utilities;

import java.awt.BorderLayout;
import java.beans.PropertyEditor;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author  Ian Formanek
 */
public class FormCustomEditor extends JPanel {

// -----------------------------------------------------------------------------
// Private variables

  private FormPropertyEditor editor;
  private JTabbedPane tabs;
    
// -----------------------------------------------------------------------------
// Constructor

  public FormCustomEditor (FormPropertyEditor editor) {
    this.editor = editor;
    setBorder (new EmptyBorder (5, 5, 5, 5));
    setLayout (new BorderLayout ());
    PropertyEditor[] allEditors = FormPropertyEditorManager.getAllEditors (editor.getPropertyType (), false);
    if (allEditors.length == 1) {
      if (editor.getCurrentEditor ().supportsCustomEditor ()) {
        add (editor.getCurrentEditor ().getCustomEditor (), BorderLayout.CENTER);
      } else {
        // [PENDING - add property sheet line component]
        add (new JLabel ("PropertyEditor does not support custom editing"), BorderLayout.CENTER);
      }
      
    } else {
      tabs = new JTabbedPane ();
      for (int i = 0; i < allEditors.length; i++) {
        if (allEditors[i].supportsCustomEditor ()) {
          tabs.addTab (Utilities.getShortClassName (allEditors[i].getClass ()), allEditors[i].getCustomEditor ());
        } else {
          // [PENDING - add property sheet line component]
          tabs.addTab (Utilities.getShortClassName (allEditors[i].getClass ()), new JLabel ("PropertyEditor does not support custom editing"));
        }
      }
      add (tabs, BorderLayout.CENTER);
    }
  }

}

/*
 * Log
 *  2    Gandalf   1.1         5/30/99  Ian Formanek    
 *  1    Gandalf   1.0         5/24/99  Ian Formanek    
 * $
 */
