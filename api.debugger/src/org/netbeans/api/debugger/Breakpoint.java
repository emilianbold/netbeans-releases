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

package org.netbeans.api.debugger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Abstract definition of breakpoint.
 *
 * @author   Jan Jancura
 */
public abstract class Breakpoint {
    
    
    /** Property name for enabled status of the breakpoint. */
    public static final String          PROP_ENABLED = "enabled"; // NOI18N
    /** Property name for disposed state of the breakpoint. */
    public static final String          PROP_DISPOSED = "disposed"; // NOI18N
    /** Property name for name of group of the breakpoint. */
    public static final String          PROP_GROUP_NAME = "groupName"; // NOI18N
    
    /** Support for property listeners. */
    private PropertyChangeSupport       pcs;
    private String                      groupName = "";
    
    { pcs = new PropertyChangeSupport (this); }

    /**
     * Called when breakpoint is removed.
     */
    protected void dispose () {}

    /**
     * Test whether the breakpoint is enabled.
     *
     * @return <code>true</code> if so
     */
    public abstract boolean isEnabled ();
    
    /**
     * Disables the breakpoint.
     */
    public abstract void disable ();
    
    /**
     * Enables the breakpoint.
     */
    public abstract void enable ();
    
    public String getGroupName () {
        return groupName;
    }
    
    public void setGroupName (String newGroupName) {
        if (groupName.equals (newGroupName)) return;
        String old = groupName;
        groupName = newGroupName;
        firePropertyChange (PROP_GROUP_NAME, old, newGroupName);
    }
    
    /** 
     * Add a listener to property changes.
     *
     * @param listener the listener to add
     */
    public synchronized void addPropertyChangeListener (
        PropertyChangeListener listener
    ) {
        pcs.addPropertyChangeListener (listener);
    }

    /** 
     * Remove a listener to property changes.
     *
     * @param listener the listener to remove
     */
    public synchronized void removePropertyChangeListener (
        PropertyChangeListener listener
    ) {
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
    
    /**
     * Called when breakpoint is removed.
     */
    void disposeOut () {
        dispose ();
        firePropertyChange (PROP_DISPOSED, Boolean.FALSE, Boolean.TRUE);
    }
}
