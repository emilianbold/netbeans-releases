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

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.Bundle;

/** Used to call "Delete" popup menu item, "Edit|Delete" main menu item,
 * "org.openide.actions.DeleteAction" or Delete shortcut.
 * @see Action
 * @see ActionNoBlock
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class DeleteAction extends ActionNoBlock {

    private static final String deletePopup = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Delete");
    private static final String deleteMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Edit")
                                            + "|" + deletePopup;
    private static final Shortcut deleteShortcut = new Shortcut(KeyEvent.VK_DELETE);

    /** creates new DeleteAction instance */    
    public DeleteAction() {
        super(deleteMenu, deletePopup, "org.openide.actions.DeleteAction", deleteShortcut);
    }
}