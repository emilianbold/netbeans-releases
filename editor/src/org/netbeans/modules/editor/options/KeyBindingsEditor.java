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

    private static final String HELP_ID = "editing.keybindings"; // !!! NOI18N

    protected HelpCtx getHelpCtx () {
        return new HelpCtx (HELP_ID);
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
        }
        refreshEditor();
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
