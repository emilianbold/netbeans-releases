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

/** Used to call "Build|Compile File" main menu item, "Compile File" popup menu or F9 shortcut.
 * @see Action
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> 
 */
public class CompileAction extends Action {

    // Build|Compile
    private static final String compileMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Build")+"|"
                                            +Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_CompileSingleAction_Name");
    private static final Shortcut compileShortcut = new Shortcut(KeyEvent.VK_F9);
    // Compile File
    private static final String compilePopup = Bundle.getString("org.netbeans.modules.java.project.Bundle", "LBL_CompileFile_Action");
    
    /** creates new CompileAction instance */    
    public CompileAction() {
        super(compileMenu, compilePopup, null, compileShortcut);
    }
}