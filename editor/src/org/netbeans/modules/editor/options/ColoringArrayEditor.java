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

package org.netbeans.modules.editor.options;

import java.awt.*;
import java.beans.*;
import java.util.HashMap;
import javax.swing.event.*;

import org.netbeans.editor.Coloring;
//import org.netbeans.modules.editor.options.ColoringBean;
import org.openide.explorer.propertysheet.PropertyPanel;
import org.openide.util.HelpCtx;

/**
 * ColoringArrayEditor is editor for Editors colorings settings, operates over
 * java.util.HashMap as it needs null key. Null key is used for transferring kitClass,
 * default coloring is named Settings.DEFAULT, other colorings are mapped by their names.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */

public class ColoringArrayEditor extends PropertyEditorSupport {

    /** Access to our localized texts */
    static java.util.ResourceBundle bundle =
        org.openide.util.NbBundle.getBundle( ColoringArrayEditor.class );

    private ColoringArrayEditorPanel editor;

    private static final String HELP_ID = "editing.fonts"; // !!! NOI18N

    public boolean supportsCustomEditor() {
        return true;
    }

    public String getAsText() {
        return bundle.getString( "PROP_Coloring" ); // NOI18N
    }

    public java.awt.Component getCustomEditor() {
        if( editor == null ) {
            editor = new ColoringArrayEditorPanel();
            HelpCtx.setHelpIDString( editor, getHelpCtx().getHelpID() );
            refreshEditor();
            editor.addPropertyChangeListener( new PropertyChangeListener() {
                                                  public void propertyChange( PropertyChangeEvent evt ) {
                                                      if( "value".equals( evt.getPropertyName() ) ) setValue( editor.getValue() ); // NOI18N
                                                  }
                                              });
        }
        return editor;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(HELP_ID);
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
