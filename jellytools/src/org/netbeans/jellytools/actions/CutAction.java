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

/** CutAction class 
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class CutAction extends Action {

    private static final String cutPopup = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Cut");
    private static final String cutMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Edit")
                                            + "|" + cutPopup;
    private static final Shortcut cutShortcut = new Shortcut(KeyEvent.VK_X, KeyEvent.CTRL_MASK);

    /** creates new CutAction instance */    
    public CutAction() {
        super(cutMenu, cutPopup, "org.openide.actions.CutAction", cutShortcut);
    }
}