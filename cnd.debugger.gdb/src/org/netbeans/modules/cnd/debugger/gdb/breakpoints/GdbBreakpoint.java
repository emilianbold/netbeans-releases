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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.debugger.gdb.breakpoints;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.netbeans.api.debugger.Breakpoint;

import org.netbeans.modules.cnd.debugger.gdb.event.GdbBreakpointEvent;
import org.netbeans.modules.cnd.debugger.gdb.event.GdbBreakpointListener;

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
    
    /* valid breakpoint states */
    /** breakpoint is unvalidated by gdb (which may not be running) */
    public static final int             UNVALIDATED = 0;
    
    /** Breakpoint has been sent to gdb for validation */
    public static final int             VALIDATION_PENDING = 1;
    
    /** Gdb has validated this breakpoint and is currently running */
    public static final int             VALIDATED = 2;

    private boolean                     enabled = true;
    private boolean                     hidden = false;
    private int                         suspend = 0; // Not fully implemented yet!
    private String                      printText;
    private HashSet                     breakpointListeners = new HashSet();
    private int                         state = UNVALIDATED;
    private static Set                  pending = Collections.synchronizedSet(new HashSet());
    private static Map                  validated = Collections.synchronizedMap(new HashMap());
    
    /**
     *  Get the breakpoint associated with the gdb breakpoint number.
     */
    public static GdbBreakpoint get(String breakpointNumber) {
        return (GdbBreakpoint) validated.get(breakpointNumber);
    }
    
    /**
     *  Search all VALIDATION_PENDING breakpoints for a match. At the time this is called, we
     *  have no way of knowing what type of a breakpoint we're looking for. So enough information
     *  is passed to get any type.
     *
     *  @param file The file name of the breakpoint
     *  @param line The line number of the breakpoint
     *  @param func The funciton name of the breakpoint
     */
    public static GdbBreakpoint getPending(String file, int lnum, String func) {
        GdbBreakpoint breakpoint = null;
        Iterator iter = pending.iterator();
        
        while (iter.hasNext()) {
            Object o = iter.next();
            if (o instanceof LineBreakpoint) {
                LineBreakpoint lbp = (LineBreakpoint) o;
                String path = lbp.getPath();
                if (path.equals(file) && lbp.getLineNumber() == lnum) { // NOI18N
                    breakpoint = lbp;
                    break;
                }
            }
        }
        
        return breakpoint;
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
                (state == UNVALIDATED || state == VALIDATION_PENDING || state == VALIDATED)) {
            this.state = state;
        }
    }
    
    public void setPending() {
        setState(VALIDATION_PENDING);
        pending.add(this);
    }
    
    public void setValidated(String breakpointNumber) {
        setState(VALIDATED);
        pending.remove(this);
        validated.put(breakpointNumber, this);
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
}
