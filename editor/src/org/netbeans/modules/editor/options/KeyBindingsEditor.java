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

package org.netbeans.modules.editor.options;

import java.awt.Component;
import java.beans.*;
import java.util.*;
import javax.swing.event.*;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * KeyBindingsEditor is editor for MultiKeyBindings settings of Editor,
 * operates over java.util.List, where single MultiKeyBindings are stored.
 * First item in List (with index 0) is used for transferring kitClass.
 *
 * @author  Petr Nejedly
 */

public class KeyBindingsEditor extends PropertyEditorSupport {

    private KeyBindingsEditorPanel editorPanel;

    protected HelpCtx getHelpCtx () {
        return new HelpCtx (KeyBindingsEditor.class);
    }

    /**
     * Tell the world that we have nice editor Component
     */
    public boolean supportsCustomEditor() {
        return true;
    }

    /**
     * Create custom editor tightly coupled with this editor
     */
    public java.awt.Component getCustomEditor() {
        if( editorPanel == null ) {
            editorPanel = new KeyBindingsEditorPanel( this );
            HelpCtx.setHelpIDString( editorPanel, getHelpCtx().getHelpID() );
            refreshEditor();
        }
        return editorPanel;
    }

    private void refreshEditor() {
        if( editorPanel != null ) {
            editorPanel.setValue( (List)getValue() );
        }
    }

    /**
     *  Sets the value for editor / customEditor
     */
    public void setValue( Object obj ) {
        Object oldValue = getValue();
        if( (obj != null) && (! obj.equals( oldValue ) ) ) {
            super.setValue( obj );
            if( ( editorPanel != null ) && (! editorPanel.getValue().equals( getValue() ) ) ) {
                refreshEditor();
            }
        }
    }

    /**
     * The way our customEditor notifies us when user changes something.
     */
    protected void customEditorChange() {
        // forward it to parent, which will fire propertyChange
        super.setValue( editorPanel.getValue() );
    }

    /**
     * Return the label to be shown in the PropertySheet
     */
    public String getAsText() {
        //    return "KeyBindings";
        return NbBundle.getBundle( KeyBindingsEditor.class ).getString( "PROP_KeyBindings" ); // NOI18N
    }

    /**
     * Don't bother if the user tried to edit our label in the PropertySheet
     */
    public void setAsText( String s ) {
    }

}

/*
 * Log
 *  1    Gandalf-post-FCS1.0         2/28/00  Petr Nejedly    initial revision
 * $
 */