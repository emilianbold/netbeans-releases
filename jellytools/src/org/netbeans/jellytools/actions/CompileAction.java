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

/** Used to call "Compile" popup menu item, "Build|Compile" main menu item,
 * "org.openide.actions.CompileAction" or F9 shortcut.
 * @see Action
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class CompileAction extends Action {
    
    private static final String compilePopup = Bundle.getStringTrimmed("org.openide.compiler.Bundle", "Compile");
    private static final String compileMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Build")
                                            + "|" + compilePopup;
    private static final Shortcut compileShortcut = new Shortcut(KeyEvent.VK_F9);

    /** creates new CompileAction instance */    
    public CompileAction() {
        super(compileMenu, compilePopup, "org.openide.actions.CompileAction", compileShortcut);
    }
}