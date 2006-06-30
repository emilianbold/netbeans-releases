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
     * Do not override. Should be final - the enabled state is cached,
     * therefore this method is not consulted unless the state change
     * is fired.
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

