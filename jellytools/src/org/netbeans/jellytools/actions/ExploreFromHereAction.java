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

import org.netbeans.jellytools.Bundle;

/** ExploreFromHereAction class 
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class ExploreFromHereAction extends Action {
    private static final String explorerPopup = Bundle.getStringTrimmed("org.openide.actions.Bundle", "OpenLocalExplorer");
    private static final String explorerMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/View")
                                           + "|"
                                           + explorerPopup;
    //shortcut collision
    //private static final Shortcut explorerShortcut = new Shortcut(KeyEvent.VK_O, KeyEvent.CTRL_MASK);

    /** creates new ExploreFromHereAction instance */    
    public ExploreFromHereAction() {
        super(explorerMenu, explorerPopup, "org.openide.actions.OpenLocalExplorerAction", null);//explorerShortcut);
    }
}