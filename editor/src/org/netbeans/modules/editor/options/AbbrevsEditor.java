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

import java.awt.Component;
import java.beans.*;
import java.util.*;
import javax.swing.event.*;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * AbbrevsEditor is editor for Map of abbreviations.
 * Each abbreviation is pair of Strings.
 * @author  Petr Nejedly
 */

public class AbbrevsEditor extends PropertyEditorSupport {

    private AbbrevsEditorPanel editorPanel;
    
    private static final String HELP_ID = "editing.abbreviations"; // !!! NOI18N

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
            editorPanel = new AbbrevsEditorPanel( this );
            HelpCtx.setHelpIDString( editorPanel, getHelpCtx().getHelpID() );
            refreshEditorPanel();
        }
        return editorPanel;
    }

    private void refreshEditorPanel() {
        if( editorPanel != null ) {
            editorPanel.setValue( (Map)getValue() );
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
                refreshEditorPanel();
            }
        }
    }

    /**
     * The way our customEditor notifies us when user changes something.
     */
    protected void customEditorChange() {
        // forward it to parent, which will fire propertyChange
        super.setValue( new HashMap( editorPanel.getValue() ) );
    }

    /**
     * Return the label to be shown in the PropertySheet
     */
    public String getAsText() {
        return NbBundle.getBundle( KeyBindingsEditor.class ).getString( "PROP_Abbreviations" ); // NOI18N
    }

    /**
     * Don't bother if the user tried to edit our label in the PropertySheet
     */
    public void setAsText( String s ) {
    }

}
