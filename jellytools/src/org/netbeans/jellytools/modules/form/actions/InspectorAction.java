/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.modules.form.actions;

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.Action;

/** Used to call "Window|GUI Editor|Inspector" main menu item,
 * "org.netbeans.modules.form.actions.InspectorAction" or Ctrl+Shift+2 shortcut.
 * @see Action
 * @author Jiri.Skrivanek@sun.com
 */
public class InspectorAction extends Action {

    // Window|GUI Editor|Inspector
    private static final String inspectorMenu = 
        Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Window")+
        "|" +
        Bundle.getStringTrimmed("org.netbeans.modules.form.resources.Bundle", 
                                "Menu/Window/Form")+
        "|" +
        Bundle.getStringTrimmed("org.netbeans.modules.form.actions.Bundle", 
                                "CTL_InspectorAction");
    
    private static final Shortcut shortcut = 
        new Shortcut(KeyEvent.VK_2, KeyEvent.CTRL_MASK|KeyEvent.SHIFT_MASK);

    /** Creates new InspectorAction instance */    
    public InspectorAction() {
        super(inspectorMenu, null, "org.netbeans.modules.form.actions.InspectorAction", shortcut);
    }
}
