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

/** BuildProjectAction class 
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class BuildProjectAction extends Action {
    
    private static final String buildProjectPopup = Bundle.getStringTrimmed("org.openide.actions.Bundle", "BuildProject");
    private static final String buildProjectMenu = Bundle.getStringTrimmed("org.netbeans.modules.projects.Bundle", "Menu/Project")
                                            + "|" + buildProjectPopup;
    private static final Shortcut buildProjectShortcut = new Shortcut(KeyEvent.VK_F11, KeyEvent.SHIFT_MASK | KeyEvent.CTRL_MASK);

    /** creates new BuildProjectAction instance */    
    public BuildProjectAction() {
        super(buildProjectMenu, buildProjectPopup, "org.openide.actions.BuildProjectAction", buildProjectShortcut);
    }
}