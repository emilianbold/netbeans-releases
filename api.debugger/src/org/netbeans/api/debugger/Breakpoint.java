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

/**
 * Abstract definition of breakpoint.
 *
 * @author   Jan Jancura
 */
public abstract class Breakpoint {
    
    
    /** Property name for enabled status of the breakpoint. */
    public static final String          PROP_ENABLED = "enabled"; // NOI18N
    
    /**
     * Called when breakpoint is removed.
     */
    protected void dispose () {
    }

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
     * Add a property change listener.
     *
     * @param l the listener to add
     */
    public abstract void addPropertyChangeListener (PropertyChangeListener l);

    /**
     * Remove a property change listener.
     *
     * @param l the listener to remove
     */
    public abstract void removePropertyChangeListener (PropertyChangeListener l);
}
