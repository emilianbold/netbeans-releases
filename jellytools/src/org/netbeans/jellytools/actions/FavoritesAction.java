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

/** Used to call "Window|Favorites" main menu item or CTRL+3 shortcut.
 * @see Action
 * @author Jiri.Skrivanek@sun.com
 */
public class FavoritesAction extends Action {
    private static final String allFilesMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Window")
                                           + "|"
                                           + Bundle.getStringTrimmed("org.netbeans.modules.favorites.Bundle", "ACT_View");
    private static final Shortcut shortcut = new Shortcut(KeyEvent.VK_3, KeyEvent.CTRL_MASK);

    /** creates new FavoritesAction instance */    
    public FavoritesAction() {
        super(allFilesMenu, null, shortcut);
    }
}