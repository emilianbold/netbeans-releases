/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import org.openide.util.NbBundle;

/**
 * ColoringArrayEditor is editor for Editors colorings settings, operates over
 * java.util.HashMap as it needs null key. Null key is used for transferring kitClass,
 * default coloring is named Settings.DEFAULT, other colorings are mapped by their names.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */

public class ColoringArrayEditor extends PropertyEditorSupport {

    private ColoringArrayEditorPanel editor;
    
    private static final String HELP_ID = "editing.fontsandcolors"; // !!! NOI18N

    public boolean supportsCustomEditor() {
        return true;
    }

    public String getAsText() {
        return NbBundle.getMessage( ColoringArrayEditor.class, "PROP_Coloring" ); // NOI18N
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

    protected HelpCtx getHelpCtx () {
        return new HelpCtx (HELP_ID);
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
