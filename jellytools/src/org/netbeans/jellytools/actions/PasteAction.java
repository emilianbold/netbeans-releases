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

/** Used to call "Paste" popup menu item, "Edit|Paste" main menu item,
 * "org.openide.actions.PasteAction" or Ctrl+V shortcut.
 * @see Action
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class PasteAction extends Action {

    private static final String pastePopup = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Paste");
    private static final String pasteMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Edit")
                                            + "|" + pastePopup;
    private static final Shortcut pasteShortcut = new Shortcut(KeyEvent.VK_V, KeyEvent.CTRL_MASK);

    /** creates new PasteAction instance */    
    public PasteAction() {
        super(pasteMenu, pastePopup, "org.openide.actions.PasteAction", pasteShortcut);
    }
}