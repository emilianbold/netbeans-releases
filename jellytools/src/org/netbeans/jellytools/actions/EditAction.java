/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.actions;

import org.netbeans.jellytools.Bundle;

/** Used to call "Edit" popup menu item or
 * "org.openide.actions.EditAction".
 * @see Action
 * @see org.netbeans.jellytools.nodes.PropertiesNode
 * @see org.netbeans.jellytools.nodes.URLNode
 * @see org.netbeans.jellytools.nodes.FormNode
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class EditAction extends Action {
    
    private static final String editPopup = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Edit");

    /** creates new EditAction instance */    
    public EditAction() {
        super(null, editPopup, "org.openide.actions.EditAction");
    }
}