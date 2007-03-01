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
    /** Property name for breakpoint validity */
    public static final String          PROP_VALIDITY = "validity"; // NOI18N
    
    /** Validity values */
    public static enum                  VALIDITY { UNKNOWN, VALID, INVALID }
    
    /** Support for property listeners. */
    private PropertyChangeSupport       pcs;
    private String                      groupName = "";
    private VALIDITY                    validity = VALIDITY.UNKNOWN;
    private String                      validityMessage;
    
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
    
    /**
     * Get the validity of this breakpoint.
     * @return The breakpoint validity.
     */
    public final synchronized VALIDITY getValidity() {
        return validity;
    }
    
    /**
     * Get the message describing the current validity. For invalid breakpoints
     * this should describe the reason why it is invalid.<p>
     * Intended for use by ui implementation code, NodeModel.getShortDescription(), for example.
     * @return The validity message.
     */
    public final synchronized String getValidityMessage() {
        return validityMessage;
    }
    
    /**
     * Set the validity of this breakpoint.
     * @param validity The new breakpoint validity.
     * @param reason The message describing why is this validity being set, or <code>null</code>.
     */
    protected final void setValidity(VALIDITY validity, String reason) {
        VALIDITY old;
        synchronized (this) {
            this.validityMessage = reason;
            if (this.validity == validity) return ;
            old = this.validity;
            this.validity = validity;
        }
        firePropertyChange(PROP_VALIDITY, old, validity);
    }
    
    public String getGroupName () {
        return groupName;
    }
    
    public void setGroupName (String newGroupName) {
        if (groupName.equals (newGroupName)) return;
        String old = groupName;
        groupName = newGroupName.intern();
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
