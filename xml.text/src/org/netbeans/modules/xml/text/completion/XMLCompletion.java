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

package org.netbeans.modules.xml.text.completion;

import java.lang.reflect.Modifier;
import java.util.*;
import java.awt.Component;

import javax.swing.*;

import org.netbeans.editor.ext.*;

import org.netbeans.modules.xml.text.syntax.*;

/**
 * XML Completion query specifications
 *
 * @author Petr Nejedly
 * @author Sandeep Randhawa
 * @version 1.0
 */

public class XMLCompletion extends Completion {
    
    public static final String FULLY_VALID = "Fully valid";  //???
    public static final String INSERT_END_TAG = "Insert End Tag";  //???
    
    public XMLCompletion(ExtEditorUI extEditorUI) {
        super(extEditorUI);
    }
    
    protected CompletionView createView() {
        return new ListCompletionView(new DelegatingCellRenderer());
    }
    
    protected CompletionQuery createQuery() {
        return new XMLCompletionQuery();
    }
    
    /** Substitute the document's text with the text
     * that is appopriate for the selection
     * in the view. This function is usually triggered
     * upon pressing the Enter key.
     * @return true if the substitution was performed
     *  false if not.
     */
    public synchronized boolean substituteText( boolean flag ) {
        if( getLastResult() != null ) {
            int index = getView().getSelectedIndex();
            if (index >= 0) {
                getLastResult().substituteText( index, flag );
            }
            return true;
        } else {
            return false;
        }
    }
    
    
    /* -------------------------------------------------------------------------- */
    // This would go out as the interfaces of all completions will meet
    public class DelegatingCellRenderer implements ListCellRenderer {
        ListCellRenderer defaultRenderer = new DefaultListCellRenderer();
        
        
        public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
            if( value instanceof CompletionQuery.ResultItem ) {
                return ((CompletionQuery.ResultItem)value).getPaintComponent( list, isSelected, cellHasFocus );
            } else {
                return defaultRenderer.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus);
            }
        }
    }
    
}

