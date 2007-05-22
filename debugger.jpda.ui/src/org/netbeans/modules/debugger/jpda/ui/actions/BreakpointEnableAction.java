/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui.actions;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.BooleanStateAction;
import org.openide.util.actions.NodeAction;

import org.netbeans.api.debugger.jpda.JPDABreakpoint;


/**
 * Enables or disables breakpoints.
 *
 * @author Martin Entlicher
 */
public class BreakpointEnableAction extends BooleanStateAction {

    public boolean isEnabled() {
        JPDABreakpoint b = BreakpointCustomizeAction.getCurrentLineBreakpoint();
        if (b == null) {
            b = ToggleMethodFieldBreakpointAction.getCurrentFieldMethodBreakpoint();
        }
        if (b != null) {
            boolean value = b.isEnabled();
            super.setBooleanState(value);
            return true;
        }
        return false;
    }

    public String getName() {
        return NbBundle.getMessage(BreakpointEnableAction.class, "CTL_enabled");
    }
    
    public void setBooleanState(boolean value) {
        JPDABreakpoint b = BreakpointCustomizeAction.getCurrentLineBreakpoint();
        if (b == null) {
            b = ToggleMethodFieldBreakpointAction.getCurrentFieldMethodBreakpoint();
        }
        if (value) {
            b.enable();
        } else {
            b.disable();
        }
        super.setBooleanState(value);
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
}
