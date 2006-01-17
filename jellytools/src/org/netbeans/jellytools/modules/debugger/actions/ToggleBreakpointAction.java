/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.modules.debugger.actions;

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.Action.Shortcut;
import org.netbeans.jellytools.nodes.Node;

/** Used to call "Run|Toggle Breakpoint" main menu item,  
 * "Toggle Breakpoint" popup menu item or CTRL+F8 shortcut.
 * @see org.netbeans.jellytools.actions.Action
 * @author Jiri.Skrivanek@sun.com
 */
public class ToggleBreakpointAction extends Action {

    // "Toggle Breakpoint"
    private static String toggleBreakpointItem = Bundle.getStringTrimmed(
                                "org.netbeans.modules.debugger.ui.actions.Bundle",
                                "CTL_Toggle_breakpoint");
    // "Run"
    private static final String runItem = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Menu/RunProject");
    // "Run|Toggle Breakpoint"
    private static final String mainMenuPath = runItem+"|"+toggleBreakpointItem;
    private static final Shortcut shortcut = new Shortcut(KeyEvent.VK_F8, KeyEvent.CTRL_MASK);
    
    /** Creates new ContinueAction instance. */
    public ToggleBreakpointAction() {
        super(mainMenuPath, toggleBreakpointItem, null, shortcut);
    }
}