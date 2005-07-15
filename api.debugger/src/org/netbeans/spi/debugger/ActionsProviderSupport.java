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

package org.netbeans.spi.debugger;

import java.util.HashSet;
import java.util.Vector;


/**
 * Support for {@link ActionsProvider} implementation. You should implement
 * {@link #doAction} and {@link #getActions} only, and call {@link #setEnabled}
 * when the action state is changed.
 * 
 * @author   Jan Jancura
 */
public abstract class ActionsProviderSupport extends ActionsProvider {
    
    private HashSet enabled = new HashSet ();
    private Vector listeners = new Vector ();

    
    /**
     * Called when the action is called (action button is pressed).
     *
     * @param action an action which has been called
     */
    public abstract void doAction (Object action);
    
    /**
     * Returns a state of given action defined by {@link #setEnabled} 
     * method call.
     *
     * @param action action
     */
    public boolean isEnabled (Object action) {
        return enabled.contains (action);
    }
    
    /**
     * Sets state of enabled property.
     *
     * @param action action whose state should be changed
     * @param enabled the new state
     */
    protected final void setEnabled (Object action, boolean enabled) {
        boolean fire = false;
        if (enabled)
            fire = this.enabled.add (action);
        else
            fire = this.enabled.remove (action);
        if (fire)
            fireActionStateChanged (action, enabled);
    }
    
    /**
     * Fires a change of action state.
     *
     * @param action action whose state has been changed
     * @param enabled the new state
     */
    protected void fireActionStateChanged (Object action, boolean enabled) {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((ActionsProviderListener) v.elementAt (i)).actionStateChange (
                action, enabled
            );
    }
    
    /**
     * Adds property change listener.
     *
     * @param l new listener.
     */
    public final void addActionsProviderListener (ActionsProviderListener l) {
        listeners.addElement (l);
    }
    
    /**
     * Removes property change listener.
     *
     * @param l removed listener.
     */
    public final void removeActionsProviderListener (ActionsProviderListener l) {
        listeners.removeElement (l);
    }
}

