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

/** Used to call "Properties" popup menu item, "Window|Properties" main menu item,
 * "org.openide.actions.PropertiesAction" or Ctrl+Shift+7 shortcut.
 * @see Action
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class PropertiesAction extends Action {

    private static final String propertiesPopup = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Properties");
    private static final String propertiesMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Window")
                                                + "|" + propertiesPopup;
    private static final Shortcut propertiesShortcut = new Shortcut(KeyEvent.VK_7, KeyEvent.CTRL_MASK|KeyEvent.SHIFT_MASK);

    /** creates new PropertiesAction instance */    
    public PropertiesAction() {
        super(propertiesMenu, propertiesPopup, "org.openide.actions.PropertiesAction", propertiesShortcut);
    }
}