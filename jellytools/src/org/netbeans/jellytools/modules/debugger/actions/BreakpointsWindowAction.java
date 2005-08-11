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
package org.netbeans.jellytools.modules.debugger.actions;

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.Action.Shortcut;
import org.netbeans.jellytools.actions.ActionNoBlock;

/**
 * Used to call "Window | Debugging | Breakpoints" main menu item or Alt-Shift-F5
 * shortcut.
 * @see org.netbeans.jellytools.actions.Action
 * @see org.netbeans.jellytools.actions.ActionNoBlock
 * @author <a href="mailto:martin.schovanek@sun.com">Martin Schovanek</a> 
 */
public class BreakpointsWindowAction extends ActionNoBlock {
    private static final String menuPath =
            Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Window") +
            "|" + Bundle.getStringTrimmed(
            "org.netbeans.modules.debugger.resources.Bundle",
            "CTL_Debugging_workspace") +
            "|" + Bundle.getStringTrimmed(
            "org.netbeans.modules.debugger.ui.actions.Bundle",
            "CTL_BreakpointsAction");

    private static final Shortcut shortcut = new Shortcut(KeyEvent.VK_F5,
            KeyEvent.VK_ALT + KeyEvent.VK_SHIFT); 
    /**
     * creates new BreakpointsWindowAction instance 
     */    
    public BreakpointsWindowAction() {
        super(menuPath, null, shortcut);
    }
}