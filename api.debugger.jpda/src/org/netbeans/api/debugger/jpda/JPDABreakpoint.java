/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger.jpda;

import com.sun.jdi.request.EventRequest;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.netbeans.api.debugger.Breakpoint;


/**
 * Abstract definition of JPDA breakpoint.
 *
 * @author   Jan Jancura
 */
public abstract class JPDABreakpoint extends Breakpoint {
    
    // static ..................................................................
    
    /** Property name constant. */
    public static final String          PROP_SUSPEND = "suspend"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_PRINT_TEXT = "printText"; // NOI18N

    /** Suspend property value constant. */
    public static final int             SUSPEND_ALL = EventRequest.SUSPEND_ALL;
    /** Suspend property value constant. */
    public static final int             SUSPEND_EVENT_THREAD = EventRequest.SUSPEND_EVENT_THREAD;
    /** Suspend property value constant. */
    public static final int             SUSPEND_NONE = EventRequest.SUSPEND_NONE;

    
    // private variables .....................................................

    /** Support for property listeners. */
    private transient PropertyChangeSupport pcs;
    /** Set of actions. */
    private boolean                     enabled = true;
    private int                         suspend = SUSPEND_ALL;
    private String                      printText = null;
    
    {
        pcs = new PropertyChangeSupport (this);
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
     * @param s a new value of print text property
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
     * Add a listener to property changes.
     *
     * @param listener the listener to add
     */
    public synchronized void addPropertyChangeListener (PropertyChangeListener listener) {
        pcs.addPropertyChangeListener (listener);
    }

    /** 
     * Remove a listener to property changes.
     *
     * @param listener the listener to remove
     */
    public synchronized void removePropertyChangeListener (PropertyChangeListener listener){
        pcs.removePropertyChangeListener (listener);
    }

    /**
     * Adds a property change listener.
     *
     * @param propertyName a name of property to listen on
     * @param l the listener to add
     */
    public void addPropertyChangeListener (
        String propertyName, PropertyChangeListener l
    ) {
        pcs.addPropertyChangeListener (propertyName, l);
    }

    /**
     * Removes a property change listener.
     *
     * @param propertyName a name of property to stop listening on
     * @param l the listener to remove
     */
    public void removePropertyChangeListener (
        String propertyName, PropertyChangeListener l
    ) {
        pcs.removePropertyChangeListener (propertyName, l);
    }

    /**
     * Fire property change.
     *
     * @param name name of property
     * @param o old value of property
     * @param n new value of property
     */
    protected void firePropertyChange (String name, Object o, Object n) {
        pcs.firePropertyChange (name, o, n);
    }
}
