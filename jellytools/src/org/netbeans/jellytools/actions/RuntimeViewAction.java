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

/** RuntimeViewAction class 
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class RuntimeViewAction extends Action {
    private static final String runtimeMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/View")
                                           + "|"
                                           + Bundle.getString("org.netbeans.core.actions.Bundle", "CTL_ViewRuntimeTabAction");
//    private static final Shortcut explorerShortcut = new Shortcut(KeyEvent.VK_2, KeyEvent.CTRL_MASK);

    /** creates new RuntimeViewAction instance */    
    public RuntimeViewAction() {
        super(runtimeMenu, null, "org.netbeans.core.actions.ViewRuntimeTabAction");
    }
}