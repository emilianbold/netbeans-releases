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

package org.netbeans.modules.debugger.jpda.ui.actions;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.BooleanStateAction;
import org.openide.util.actions.NodeAction;

import org.netbeans.api.debugger.jpda.LineBreakpoint;


/**
 * Enables or disables breakpoints.
 *
 * @author Martin Entlicher
 */
public class BreakpointEnableAction extends BooleanStateAction {
    
    public boolean isEnabled() {
        LineBreakpoint lb = LineBreakpointCustomizeAction.getCurrentBreakpoint();
        if (lb != null) {
            boolean value = lb.isEnabled();
            super.setBooleanState(value);
            return true;
        }
        return false;
    }

    public String getName() {
        return NbBundle.getMessage(BreakpointEnableAction.class, "CTL_enabled");
    }
    
    public void setBooleanState(boolean value) {
        LineBreakpoint lb = LineBreakpointCustomizeAction.getCurrentBreakpoint();
        if (value) {
            lb.enable();
        } else {
            lb.disable();
        }
        super.setBooleanState(value);
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
}
