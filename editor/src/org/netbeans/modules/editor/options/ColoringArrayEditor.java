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

/*
 * Log
 *  14   Gandalf-post-FCS1.12.1.0    2/28/00  Petr Nejedly    Redesign of 
 *       ColoringEditor
 *  13   Gandalf   1.12        1/13/00  Miloslav Metelka Localization
 *  12   Gandalf   1.11        1/11/00  Petr Nejedly    ScrollPane, distribution
 *       of changes
 *  11   Gandalf   1.10        12/28/99 Miloslav Metelka 
 *  10   Gandalf   1.9         11/14/99 Miloslav Metelka 
 *  9    Gandalf   1.8         11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         8/27/99  Miloslav Metelka 
 *  6    Gandalf   1.5         8/17/99  Miloslav Metelka 
 *  5    Gandalf   1.4         7/30/99  Miloslav Metelka 
 *  4    Gandalf   1.3         7/29/99  Miloslav Metelka 
 *  3    Gandalf   1.2         7/26/99  Miloslav Metelka 
 *  2    Gandalf   1.1         7/21/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
 * $
 */