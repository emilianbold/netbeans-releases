/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.editor.options;

import java.beans.PropertyEditorSupport;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Martin Roskanin
 */
public class CodeFoldingEditor extends PropertyEditorSupport{

    protected HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
    }
    
    /** Creates a new instance of CodeFoldingEditor */
    public CodeFoldingEditor() {
    }

    public boolean supportsCustomEditor() {
        return true;
    }

    
    private CodeFoldingEditorPanel editorPanel;
    
    /**
     * Create custom editor tightly coupled with this editor
     */
    public java.awt.Component getCustomEditor() {
        if( editorPanel == null ) {
            editorPanel = new CodeFoldingEditorPanel( this );
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
        return NbBundle.getBundle( CodeFoldingEditor.class ).getString( "PROP_CodeFolding" ); // NOI18N
    }

    /**
     * Don't bother if the user tried to edit our label in the PropertySheet
     */
    public void setAsText( String s ) {
    }
    
    
}

