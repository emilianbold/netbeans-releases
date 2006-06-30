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

package org.netbeans.api.debugger.jpda;


/**
 * Notifies about thread started and dead events.
 *
 * <br><br>
 * <b>How to use it:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    DebuggerManager.addBreakpoint (ThreadBreakpoint.create (
 *    ));</pre>
 * This breakpoint stops when some thread is created or killed.
 *
 * @author Jan Jancura
 */
public final class ThreadBreakpoint extends JPDABreakpoint {

    /** Property name constant. */
    public static final String          PROP_BREAKPOINT_TYPE = "breakpointtType"; // NOI18N

    /** Catch type property value constant. */
    public static final int             TYPE_THREAD_STARTED = 1;
    /** Catch type property value constant. */
    public static final int             TYPE_THREAD_DEATH = 2;
    /** Catch type property value constant. */
    public static final int             TYPE_THREAD_STARTED_OR_DEATH = 3;
    
    /** Property variable. */
    private int                         breakpointType = TYPE_THREAD_STARTED;

    
    private ThreadBreakpoint () {
    }
    
    /**
     * Creates a new breakpoint for given parameters.
     *
     * @return a new breakpoint for given parameters
     */
    public static ThreadBreakpoint create () {
        return new ThreadBreakpoint ();
    }

    /**
     * Returns type of this breakpoint.
     *
     * @return type of this breakpoint
     */
    public int getBreakpointType () {
        return breakpointType;
    }

    /**
     * Sets type of this breakpoint (TYPE_THREAD_STARTED or TYPE_THREAD_DEATH).
     *
     * @param breakpointType a new value of breakpoint type property
     */
    public void setBreakpointType (int breakpointType) {
        if (breakpointType == this.breakpointType) return;
        if ((breakpointType & (TYPE_THREAD_STARTED | TYPE_THREAD_DEATH)) == 0)
            throw new IllegalArgumentException  ();
        int old = this.breakpointType;
        this.breakpointType = breakpointType;
        firePropertyChange (PROP_BREAKPOINT_TYPE, new Integer (old), new Integer (breakpointType));
    }

    /**
     * Returns a string representation of this object.
     *
     * @return  a string representation of the object
     */
    public String toString () {
        return "ThreadBreakpoint " + breakpointType;
    }
}
