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
package org.netbeans.jellytools.actions;

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.Bundle;

/** Used to call "Window|Output" main menu item, 
 * "org.netbeans.core.output.OutputWindowAction" or Ctrl+4 shortcut.
 * @see Action 
 */
public class OutputWindowViewAction extends Action {
    private static final String menu = 
        Bundle.getStringTrimmed("org.netbeans.core.Bundle", 
                                "Menu/Window") +
        "|" +
        Bundle.getStringTrimmed("org.netbeans.core.output.Bundle", 
                                "OutputWindow");
    private static final Shortcut shortcut = 
        new Shortcut(KeyEvent.VK_4, KeyEvent.CTRL_MASK);

    /** Creates new instance. */    
    public OutputWindowViewAction() {
        super(menu, null, "org.netbeans.core.output.OutputWindowAction", shortcut);
    }
}
