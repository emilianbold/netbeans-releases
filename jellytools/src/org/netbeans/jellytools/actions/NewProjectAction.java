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

/** Used to call "File|New Project..."  main menu item,
 *  or Ctrl+Shift+N shortcut.
 * @see Action
 * @see ActionNoBlock
 */
public class NewProjectAction extends ActionNoBlock {
    
    /** File|New Project..." main menu path. */
    private  static final String menuPath = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/File")
                                            + "|"
                                            + Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_NewProjectAction_Name");
    /** Ctrl+Shift+N*/
    private static final Shortcut shortcut = new Shortcut(KeyEvent.VK_N, KeyEvent.SHIFT_MASK | KeyEvent.CTRL_MASK);
    
    /** Creates new NewProjectAction instance. */
    public NewProjectAction() {
        super(menuPath, null, null, shortcut);
    }
    
}