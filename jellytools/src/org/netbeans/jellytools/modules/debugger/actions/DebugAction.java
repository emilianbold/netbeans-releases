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

/** Used to call "Run|Run File|Debug "MyClass.java"" main menu item,  
 * "Debug File" popup menu item or CTRL+Shift+F5 shortcut.
 * @see org.netbeans.jellytools.actions.Action
 * @author Jiri.Skrivanek@sun.com
 */
public class DebugAction extends Action {

    // "Run"
    private static final String runItem = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Menu/RunProject");
    // "Run File"
    private static final String runFileItem = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Menu/RunProject/RunOther");
    // "Debug File"
    private static final String popupPath = 
            Bundle.getStringTrimmed("org.netbeans.modules.java.project.Bundle", "LBL_DebugFile_Action");
    private static final Shortcut shortcut = new Shortcut(KeyEvent.VK_F5, KeyEvent.CTRL_MASK+KeyEvent.SHIFT_MASK);
    
    /** Creates new DebugAction instance. */
    public DebugAction() {
        super(null, popupPath, null, shortcut);
    }
    
    /** Performs action through main menu. 
     * @param node node to be selected before action
     */
    public void performMenu(Node node) {
        this.menuPath = runItem+"|"+runFileItem+"|"+
                Bundle.getString("org.netbeans.modules.project.ui.actions.Bundle",
                                 "LBL_DebugSingleAction_Name",
                                 new Object[] {new Integer(1), node.getText()});
        super.performMenu(node);
    }
}