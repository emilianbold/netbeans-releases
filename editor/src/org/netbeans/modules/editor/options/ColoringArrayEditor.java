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

package com.netbeans.developer.modules.text.options;

import java.awt.*;
import java.beans.*;
import java.util.HashMap;
import javax.swing.event.*;

import com.netbeans.editor.Coloring;
//import com.netbeans.developer.modules.text.options.ColoringBean;
import org.openide.explorer.propertysheet.PropertyPanel;

/** 
 * ColoringArrayEditor is editor for Editors colorings settings, operates over
 * java.util.HashMap as it needs null key. Null key is used for transferring kitClass,
 * default coloring is named Settings.DEFAULT, other colorings are mapped by their names.
 *
 * @author  Petr Nejedly
 * @version 
 */

public class ColoringArrayEditor extends PropertyEditorSupport {
  
  /** Access to our localized texts */
  static java.util.ResourceBundle bundle = 
    org.openide.util.NbBundle.getBundle( ColoringArrayEditor.class );

  private ColoringArrayEditorPanel editor;
  
  public boolean supportsCustomEditor() {
    return true;
  }

  public String getAsText() {
    return bundle.getString( "PROP_Coloring" ); // NOI18N
  }
  
  public java.awt.Component getCustomEditor() {    
    if( editor == null ) {
      editor = new ColoringArrayEditorPanel();
      refreshEditor();
      editor.addPropertyChangeListener( new PropertyChangeListener() {
        public void propertyChange( PropertyChangeEvent evt ) {
          if( "value".equals( evt.getPropertyName() ) ) setValue( editor.getValue() ); // NOI18N
        }
      });
    }
    return editor;
  }
  
  public void setAsText( String s ) {
    return;
  }
  
  public void setValue( Object obj ) {
    Object oldValue = getValue();
    if( (obj != null) && (! obj.equals( oldValue ) ) ) {
      super.setValue( obj );
      if( ( editor != null ) && (! editor.getValue().equals( getValue() ) ) ) {
        refreshEditor();
      }
    }
  }
  
  private void refreshEditor() {
    if( editor != null ) {
      editor.setValue( (HashMap)getValue() );
    }
  }
}
