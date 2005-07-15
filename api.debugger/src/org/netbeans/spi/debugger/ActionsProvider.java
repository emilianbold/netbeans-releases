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

import java.util.Set;

/**
 * Represents implementation of one or more actions. 
 *
 * @author   Jan Jancura
 */
public abstract class ActionsProvider {
    
    /**
     * Returns set of actions supported by this ActionsProvider.
     *
     * @return set of actions supported by this ActionsProvider
     */
    public abstract Set getActions ();
    
    /**
     * Called when the action is called (action button is pressed).
     *
     * @param action an action which has been called
     */
    public abstract void doAction (Object action);
    
    /**
     * Should return a state of given action.
     *
     * @param action action
     */
    public abstract boolean isEnabled (Object action);
    
    /**
     * Adds property change listener.
     *
     * @param l new listener.
     */
    public abstract void addActionsProviderListener (ActionsProviderListener l);
    

    /**
     * Removes property change listener.
     *
     * @param l removed listener.
     */
    public abstract void removeActionsProviderListener (ActionsProviderListener l);
}

