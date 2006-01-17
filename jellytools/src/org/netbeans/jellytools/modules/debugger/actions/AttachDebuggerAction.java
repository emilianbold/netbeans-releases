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

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.ActionNoBlock;

/** Used to call "Run|Attach Debugger..." main menu item.
 * @see org.netbeans.jellytools.actions.ActionNoBlock
 * @author Jiri.Skrivanek@sun.com
 */
public class AttachDebuggerAction extends ActionNoBlock {
    
    // "Run|Attach Debugger..."
    private static final String mainMenuPath =
            Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Menu/RunProject")+
            "|"+
            Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Connect");
    
    /** Creates new AttachDebuggerAction instance. */
    public AttachDebuggerAction() {
        super(mainMenuPath, null);
    }
}