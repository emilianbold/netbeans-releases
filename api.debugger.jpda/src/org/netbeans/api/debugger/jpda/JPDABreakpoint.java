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

import com.sun.jdi.request.EventRequest;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;

/**
 * Abstract definition of JPDA breakpoint.
 *
 * @author   Jan Jancura
 */
public class JPDABreakpoint extends Breakpoint {

    // static ..................................................................

    /** Property name constant. */
    public static final String          PROP_SUSPEND = "suspend"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_HIDDEN = "hidden"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_PRINT_TEXT = "printText"; // NOI18N

    /** Suspend property value constant. */
    public static final int             SUSPEND_ALL = EventRequest.SUSPEND_ALL;
    /** Suspend property value constant. */
    public static final int             SUSPEND_EVENT_THREAD = EventRequest.SUSPEND_EVENT_THREAD;
    /** Suspend property value constant. */
    public static final int             SUSPEND_NONE = EventRequest.SUSPEND_NONE;

    
    // private variables .....................................................

    /** Set of actions. */
    private boolean                     enabled = true;
    private boolean                     hidden = false;
    private int                         suspend = SUSPEND_ALL;
    private String                      printText;
    private Collection<JPDABreakpointListener>  breakpointListeners = new HashSet<JPDABreakpointListener>();
    
   
    JPDABreakpoint () {
    }
    

    // main methods ............................................................
    
    /**
     * Gets value of suspend property.
     *
     * @return value of suspend property
     */
    public int getSuspend () {
        return suspend;
    }

    /**
     * Sets value of suspend property.
     *
     * @param s a new value of suspend property
     */
    public void setSuspend (int s) {
        if (s == suspend) return;
        int old = suspend;
        suspend = s;
        firePropertyChange (PROP_SUSPEND, new Integer (old), new Integer (s));
    }
    
    /**
     * Gets value of hidden property.
     *
     * @return value of hidden property
     */
    public boolean isHidden () {
        return hidden;
    }

    /**
     * Sets value of hidden property.
     *
     * @param h a new value of hidden property
     */
    public void setHidden (boolean h) {
        if (h == hidden) return;
        boolean old = hidden;
        hidden = h;
        firePropertyChange (PROP_HIDDEN, Boolean.valueOf (old), Boolean.valueOf (h));
    }
    
    /**
     * Gets value of print text property.
     *
     * @return value of print text property
     */
    public String getPrintText () {
        return printText;
    }

    /**
     * Sets value of print text property.
     *
     * @param printText a new value of print text property
     */
    public void setPrintText (String printText) {
        if (this.printText == printText) return;
        String old = this.printText;
        this.printText = printText;
        firePropertyChange (PROP_PRINT_TEXT, old, printText);
    }

    /**
     * Test whether the breakpoint is enabled.
     *
     * @return <code>true</code> if so
     */
    public boolean isEnabled () {
        return enabled;
    }
    
    /**
     * Disables the breakpoint.
     */
    public void disable () {
        if (!enabled) return;
        enabled = false;
        firePropertyChange 
            (PROP_ENABLED, Boolean.TRUE, Boolean.FALSE);
    }
    
    /**
     * Enables the breakpoint.
     */
    public void enable () {
        if (enabled) return;
        enabled = true;
        firePropertyChange 
            (PROP_ENABLED, Boolean.FALSE, Boolean.TRUE);
    }
    
    /** 
     * Adds a JPDABreakpointListener.
     *
     * @param listener the listener to add
     */
    public synchronized void addJPDABreakpointListener (
        JPDABreakpointListener listener
    ) {
        breakpointListeners.add (listener);
    }

    /** 
     * Removes a JPDABreakpointListener.
     *
     * @param listener the listener to remove
     */
    public synchronized void removeJPDABreakpointListener (
        JPDABreakpointListener listener
    ){
        breakpointListeners.remove (listener);
    }

    /**
     * Fire JPDABreakpointEvent.
     *
     * @param event a event to be fired
     */
    void fireJPDABreakpointChange (JPDABreakpointEvent event) {
        Iterator<JPDABreakpointListener> i =
                new HashSet<JPDABreakpointListener>(breakpointListeners).iterator();
        while (i.hasNext ())
            i.next().breakpointReached (event);
    }
}
