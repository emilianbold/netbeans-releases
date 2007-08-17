/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.debugger.gdb.breakpoints;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;

import org.netbeans.modules.cnd.debugger.gdb.event.GdbBreakpointEvent;
import org.netbeans.modules.cnd.debugger.gdb.event.GdbBreakpointListener;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;

/*
 * Note: This class may need to become abstracto with a GdbBreakpoint and
 * a DbxBreakpoint derived from it...
 */

/**
 * Abstract definition of Cnd breakpoint.
 *
 * @author   Gordon Prieur (copied from Jan Jancura's JPDABreakpoint)
 */
public abstract class GdbBreakpoint extends Breakpoint {
    
    // static ..................................................................
    
    public static final String          PROP_SUSPEND = "suspend"; // NOI18N
    public static final String          PROP_HIDDEN = "hidden"; // NOI18N
    public static final String          PROP_PRINT_TEXT = "printText"; // NOI18N
    public static final String          PROP_BREAKPOINT_STATE = "breakpointState"; // NOI18N
    
    public static final int            MIN_GDB_ID = 2; // 1 is the temp bp at main...
    
    /* valid breakpoint states */
    /** breakpoint is unvalidated by gdb (which may not be running) */
    public static final int             UNVALIDATED = 0;
    
    /** Breakpoint has been sent to gdb for validation */
    public static final int             VALIDATION_PENDING = 1;
    
    /** Gdb has validated this breakpoint and is currently running */
    public static final int             VALIDATED = 2;
    
    /** Breakpoint is being deleted */
    public static final int             DELETION_PENDING = 3;
    
    public  Object                      annotation = null;
    private boolean                     enabled = true;
    private boolean                     hidden = false;
    private int                         suspend = 0; // Not fully implemented yet!
    private int				breakpointNumber;
    private String                      printText;
    private HashSet                     breakpointListeners = new HashSet();
    private int                         state = UNVALIDATED;
    private static Map                  pending = Collections.synchronizedMap(new HashMap());
    private static Map                  bplist = Collections.synchronizedMap(new HashMap());
    private GdbDebugger		debugger;
    private Object			LOCK = new Object();
    private int				id = 0;
    
    /**
     *  Provide a unique ID for each requested breakpoint
     */
    protected void setID(int id) {
	this.id = id;
    }
    
    public int getID() {
	return id;
    }
    
    /**
     *  Get the breakpoint associated with the gdb breakpoint number.
     */
    public static GdbBreakpoint get(String breakpointNumber) {
        return (GdbBreakpoint) bplist.get(breakpointNumber);
    }
    
    /**
     *  Search all VALIDATION_PENDING breakpoints for a match. At the time this is called, we
     *  have no way of knowing what type of a breakpoint we're looking for. So enough information
     *  is passed to get any type.
     *
     *  @param id The unique ID of the breakpoint
     *  @return The GdbBreakpoint represented by this ID
     */
    public static GdbBreakpoint getPending(int id) {
	return (GdbBreakpoint) pending.get(Integer.valueOf(id));
    }
    
    public void setPending() {
        setState(VALIDATION_PENDING);
        pending.put(Integer.valueOf(id), this);
    }
    
    /**
     * Get the state of this breakpoint
     */
    public int getState() {
        return state;
    }
    
    /** Set the state of this breakpoint */
    public void setState(int state) {
        if (state != this.state &&
                (state == UNVALIDATED || state == VALIDATION_PENDING ||
		 state == VALIDATED || state == DELETION_PENDING)) {
            this.state = state;
	    if (state == UNVALIDATED) {
		breakpointNumber = -1;
	    }
        }
    }
    
    public void setValidationResult(int id, String breakpointNumber) {
	assert(id == getID());
	
	if (breakpointNumber != null) {
	    this.breakpointNumber = Integer.parseInt(breakpointNumber);
	    setState(VALIDATED);
	    if (!isEnabled()) {
		debugger.break_disable(this.breakpointNumber);
	    }
	} else {
	    setState(UNVALIDATED);
	}
        bplist.put(breakpointNumber, this);
        pending.remove(Integer.valueOf(id));
	if (pending.size() == 0 && debugger.getState() == GdbDebugger.STATE_LOADING) {
	    debugger.setRunning();
	}
    }
    
    /**
     * Gets value of suspend property.
     *
     * @return value of suspend property
     */
    public int getSuspend() {
        return suspend;
    }
    
    /**
     * Sets value of suspend property.
     *
     * @param s a new value of suspend property
     */
    public void setSuspend(int s) {
        if (s == suspend) {
            return;
        }
        int old = suspend;
        suspend = s;
        firePropertyChange(PROP_SUSPEND, new Integer(old), new Integer(s));
    }
    
    /**
     * Gets value of hidden property.
     *
     * @return value of hidden property
     */
    public boolean isHidden() {
        return hidden;
    }
    
    /**
     * Sets value of hidden property.
     *
     * @param h a new value of hidden property
     */
    public void setHidden(boolean h) {
        if (h == hidden) {
            return;
        }
        boolean old = hidden;
        hidden = h;
        firePropertyChange(PROP_HIDDEN, Boolean.valueOf(old), Boolean.valueOf(h));
    }
    
    public int getBreakpointNumber() {
	return breakpointNumber;
    }
    
    /**
     * Gets value of print text property.
     *
     * @return value of print text property
     */
    public String getPrintText() {
        return printText;
    }
    
    /**
     * Sets value of print text property.
     *
     * @param printText a new value of print text property
     */
    public void setPrintText(String printText) {
        if (this.printText == printText) {
            return;
        }
        String old = this.printText;
        this.printText = printText;
        firePropertyChange(PROP_PRINT_TEXT, old, printText);
    }
    
    /**
     * Test whether the breakpoint is enabled.
     *
     * @return <code>true</code> if so
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Disables the breakpoint.
     */
    public void disable() {
        if (!enabled) {
            return;
        }
        enabled = false;
        firePropertyChange(PROP_ENABLED, Boolean.TRUE, Boolean.FALSE);
    }
    
    /**
     * Enables the breakpoint.
     */
    public void enable() {
        if (enabled) {
            return;
        }
        enabled = true;
        firePropertyChange(PROP_ENABLED, Boolean.FALSE, Boolean.TRUE);
    }
    
    /**
     *
     * Adds a GdbBreakpointListener.
     *
     * @param listener the listener to add
     */
    public synchronized void addGdbBreakpointListener(GdbBreakpointListener listener) {
        breakpointListeners.add(listener);
    }
    
    /**
     *
     * Removes a GdbBreakpointListener.
     *
     * @param listener the listener to remove
     */
    public synchronized void removeGdbBreakpointListener(GdbBreakpointListener listener){
        breakpointListeners.remove(listener);
    }
    
    /**
     * Fire GdbBreakpointEvent.
     *
     * @param event a event to be fired
     */
    public void fireGdbBreakpointChange(GdbBreakpointEvent event) {
        Iterator i = ((HashSet) breakpointListeners.clone()).iterator();
        while (i.hasNext()) {
            ((GdbBreakpointListener) i.next()).breakpointReached(event);
        }
    }
    
    protected void setDebugger(GdbDebugger debugger) {
	this.debugger = debugger;
    }
    
    public GdbDebugger getDebugger() {
	return debugger;
    }
}
