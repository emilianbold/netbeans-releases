/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.modules.debugger.actions;

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.ActionNoBlock;

/**
 * Used to call "Delete All" popup menu item in Breakpoints window.
 * @see org.netbeans.jellytools.actions.Action
 * @see org.netbeans.jellytools.actions.ActionNoBlock
 * @author <a href="mailto:martin.schovanek@sun.com">Martin Schovanek</a> 
 */
public class DeleteAllBreakpointsAction extends ActionNoBlock {
    private static final String popup = Bundle.getStringTrimmed(
            "org.netbeans.modules.debugger.ui.models.Bundle",
            "CTL_BreakpointAction_DeleteAll_Label");

    /**
     * creates new DeleteAllBreakpointsAction instance 
     */    
    public DeleteAllBreakpointsAction() {
        super(null, popup);
    }
}