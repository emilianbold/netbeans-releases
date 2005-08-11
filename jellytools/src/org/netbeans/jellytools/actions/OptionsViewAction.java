/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.actions;

import org.netbeans.jellytools.Bundle;

/** Used to call "Tools|Options" main menu item or
 * "org.netbeans.core.actions.OptionsAction". If called on MAC it uses IDE API to
 * open Options. Apple system menu is unreachable for Jemmy 
 * (http://www.netbeans.org/issues/show_bug.cgi?id=61300).
 * @see Action 
 */
public class OptionsViewAction extends Action {
    private static final String menu = 
        Bundle.getStringTrimmed("org.netbeans.core.Bundle", 
                                "Menu/Tools") +
        "|" +
        Bundle.getStringTrimmed("org.netbeans.core.actions.Bundle", 
                                "Options");

    /** Creates new instance. */    
    public OptionsViewAction() {
        super(menu, null, "org.netbeans.core.actions.OptionsAction");
    }
    
    /** performs action through main menu. If called on MAC it uses IDE API to
     * open Options. Apple system menu is unreachable for Jemmy 
     * (http://www.netbeans.org/issues/show_bug.cgi?id=61300).
     * @throws UnsupportedOperationException when action does not support menu mode 
     */
    public void performMenu() {
        if(System.getProperty("os.name").toLowerCase().indexOf("mac") > -1) { // NOI18N
            performAPI();
        } else {
            super.performMenu();
        }
    }
}
