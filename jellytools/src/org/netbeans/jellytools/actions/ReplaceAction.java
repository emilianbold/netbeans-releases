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

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.ProjectsTabOperator;

/** Used to call "Edit|Replace" main menu item,
 * "org.openide.actions.ReplaceAction" or Ctrl+H shortcut.
 * @see Action
 * @see ActionNoBlock
 * @author Roman Strobl 
 */
public class ReplaceAction extends ActionNoBlock {
    // "Edit|Replace..."
    private static final String replaceMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Edit")
                                              + "|"
                                              + Bundle.getStringTrimmed("org.openide.actions.Bundle", "Replace");
    private static final Shortcut replaceShortcut = new Shortcut(KeyEvent.VK_H, KeyEvent.CTRL_MASK);
    
    /** creates new ReplaceAction instance */
    public ReplaceAction() {
        super(replaceMenu, null, "org.openide.actions.ReplaceAction", replaceShortcut);
    }
}
