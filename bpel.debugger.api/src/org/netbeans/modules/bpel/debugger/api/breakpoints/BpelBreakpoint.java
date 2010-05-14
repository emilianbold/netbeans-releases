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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.debugger.api.breakpoints;

import org.netbeans.api.debugger.Breakpoint;

/**
 *
 * @author Alexander Zgursky
 */
public class BpelBreakpoint extends Breakpoint {

    private boolean  myIsEnabled = true;

    /** Creates a new instance of BpelBreakpoint. */
    public BpelBreakpoint() {
        // does nothing
    }

    /**
     * Test whether the breakpoint is enabled.
     *
     * @return <code>true</code> if so
     */
    public boolean isEnabled() {
        return myIsEnabled;
    }

    /**
     * Disables the breakpoint.
     */
    public void disable() {
        if (!myIsEnabled) {
            return;
        }
        myIsEnabled = false;
        firePropertyChange(PROP_ENABLED, Boolean.TRUE, Boolean.FALSE);
    }
    
    /**
     * Enables the breakpoint.
     */
    public void enable() {
        if (myIsEnabled) {
            return;
        }
        myIsEnabled = true;
        firePropertyChange(PROP_ENABLED, Boolean.FALSE, Boolean.TRUE);
    }
}
