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

/** Used to call "View" popup menu item or
 * "org.openide.actions.ViewAction".
 * @see Action
 * @see org.netbeans.jellytools.nodes.HTMLNode
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class ViewAction extends Action {
    
    private static final String viewPopup = Bundle.getStringTrimmed("org.openide.actions.Bundle", "View");

    /** creates new ViewAction instance */    
    public ViewAction() {
        super(null, viewPopup, "org.openide.actions.ViewAction");
    }
}