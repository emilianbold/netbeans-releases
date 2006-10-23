/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.modules.debugger.actions;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.Action;

/**
 * Used to call "Window | Debugging | Breakpoints" main menu item or Alt-Shift-F5
 * shortcut.
 * @see org.netbeans.jellytools.actions.Action
 * @see org.netbeans.jellytools.actions.ActionNoBlock
 * @author <a href="mailto:martin.schovanek@sun.com">Martin Schovanek</a>
 */
public class BreakpointsWindowAction extends Action {
    private static final String menuPath =
            Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Window") +
            "|" + Bundle.getStringTrimmed(
            "org.netbeans.modules.debugger.resources.Bundle",
            "CTL_Debugging_workspace") +
            "|" + Bundle.getStringTrimmed(
            "org.netbeans.modules.debugger.ui.actions.Bundle",
            "CTL_BreakpointsAction");
    private static final KeyStroke keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_5, KeyEvent.SHIFT_MASK|KeyEvent.ALT_MASK);

    /**
     * creates new BreakpointsWindowAction instance 
     */    
    public BreakpointsWindowAction() {
        super(menuPath, null, keystroke);
    }
}
