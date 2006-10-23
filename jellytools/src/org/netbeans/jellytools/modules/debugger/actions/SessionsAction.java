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
 * Used to call "Window|Debugging|Sessions" main menu item or Alt+Shift+6
 * shortcut.
 * @see org.netbeans.jellytools.actions.Action
 * @author Jiri.Skrivanek@sun.com
 */
public class SessionsAction extends Action {
    private static final String menuPath =
            Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Window") +
            "|"+Bundle.getStringTrimmed(
                                "org.netbeans.modules.debugger.resources.Bundle",
                                "CTL_Debugging_workspace") +
            "|"+Bundle.getStringTrimmed(
                                "org.netbeans.modules.debugger.ui.actions.Bundle",
                                "CTL_SessionsAction");

     private static final KeyStroke keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_6,
                                                          KeyEvent.VK_ALT + KeyEvent.VK_SHIFT); 
    
    /** Creates new SessionsAction instance.  */    
    public SessionsAction() {
        super(menuPath, null, keystroke);
    }
}
