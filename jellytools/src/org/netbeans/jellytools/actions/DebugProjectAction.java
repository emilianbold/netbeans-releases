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

/** Used to call "Debug Project" popup menu item on project's root node,
 * "Run|Debug Main Project" main menu item or F5 shortcut.
 * @see Action
 * @see org.netbeans.jellytools.nodes.ProjectRootNode
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> 
 * @author Jiri.Skrivanek@sun.com
 */
public class DebugProjectAction extends Action {
    
    // "Debug Project"
    private static final String debugProjectPopup = Bundle.getString("org.netbeans.modules.java.j2seproject.ui.Bundle", "LBL_DebugAction_Name");
    // "Run|Debug Main Project"
    private static final String debugProjectMenu = 
            Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Menu/RunProject")+
            "|"+
            Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_DebugMainProjectAction_Name");
    private static final Shortcut debugProjectShortcut = new Shortcut(KeyEvent.VK_F5);

    /** creates new DebugProjectAction instance */    
    public DebugProjectAction() {
        super(debugProjectMenu, debugProjectPopup, null, debugProjectShortcut);
    }
}