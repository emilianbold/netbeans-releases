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

/** Used to call "Copy" popup menu item, "Edit|Copy" main menu item,
 * "org.openide.actions.CopyAction" or Ctrl+C shortcut.
 * @see Action
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class CopyAction extends Action {

    private static final String copyPopup = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Copy");
    private static final String copyMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Edit")
                                            + "|" + copyPopup;
    private static final Shortcut copyShortcut = new Shortcut(KeyEvent.VK_C, KeyEvent.CTRL_MASK);

    /** creates new CopyAction instance */    
    public CopyAction() {
        super(copyMenu, copyPopup, "org.openide.actions.CopyAction", copyShortcut);
    }
}