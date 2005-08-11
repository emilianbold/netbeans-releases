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

package org.netbeans.jellytools.modules.debugger;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.modules.debugger.actions.BreakpointsWindowAction;
import org.netbeans.jellytools.modules.debugger.actions.DeleteAllBreakpointsAction;

/**
 * Provides access to the Breakpoints window.
 * <p>
 * Usage:<br>
 * <pre>
 *      BreakpointsWindowOperator bwo = new BreakpointsWindowOperator().invoke();
 *      bwo.deleteAll();
 *      bwo.close();
 * </pre>
 *
 * @author Martin.Schovanek@sun.com
 * @see org.netbeans.jellytools.OutputTabOperator
 */
public class BreakpointsWindowOperator extends TopComponentOperator {
    
    private static final Action invokeAction = new BreakpointsWindowAction();
    
    /** Waits for Breakpoints window top component and creates a new operator
     * for it. */
    public BreakpointsWindowOperator() {
        super(waitTopComponent(Bundle.getStringTrimmed (
                "org.netbeans.modules.debugger.ui.views.Bundle",
                "CTL_Breakpoints_view"),  0));
    }
    
    /**
     * Opens Breakpoints window from main menu Window|Debugging|Breakpoints and
     * returns BreakpointsWindowOperator.
     * @return instance of BreakpointsWindowOperator
     */
    public static BreakpointsWindowOperator invoke() {
        invokeAction.perform();
        return new BreakpointsWindowOperator();
    }
    
    /********************************** Actions ****************************/
    
    /** Performs Delete All action on active tab. */
    public void deleteAll() {
        new DeleteAllBreakpointsAction().perform(this);
    }
    
    /**
     * Performs verification of BreakpointsWindowOperator by accessing its
     * components.
     */
    public void verify() {    
        // TBD
    }
}
