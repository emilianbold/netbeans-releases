/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.actions;

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.Bundle;

/** Used to call "Help|Help Contents" main menu item,
 * "org.openide.actions.HelpAction" or F1 shortcut. It can also be used
 * to invoke help on a property sheet from popup menu.
 * @see Action
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class HelpAction extends Action {

    // String used in property sheets
    private static final String popupPath = Bundle.getString("org.openide.explorer.propertysheet.Bundle", "CTL_Help");
    private static final String helpMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Help")
                                         + "|" 
                                         + Bundle.getStringTrimmed("org.netbeans.modules.usersguide.Bundle", "Menu/Help/org-netbeans-modules-usersguide-master.xml");
    /* --- Helpsets subdirectory not used in NetBeans 3.6 ---
    private static final String helpSetsMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Help")
                                             + "|" 
                                             + Bundle.getStringTrimmed("org.netbeans.modules.javahelp.resources.Bundle", "Menu/Help/HelpShortcuts")
                                             + "|";
     */
    private static final Shortcut helpShortcut = new Shortcut(KeyEvent.VK_F1);

    /** Creates new HelpAction instance for specific help set.
     * @param helpSet menu item of help set to be showed (e.g. "Core IDE Help")
     */
    /* --- Helpsets subdirectory not used in NetBeans 3.6 ---
    public HelpAction(String helpSet) {
        super(helpSetsMenu+helpSet, null);
    }
     */

    /** Creates new HelpAction instance for master help set (Help|Contents)
     * or for generic use e.g. in property sheets.
     */
    public HelpAction() {
        super(helpMenu, popupPath, "org.openide.actions.HelpAction", helpShortcut);
    }
}