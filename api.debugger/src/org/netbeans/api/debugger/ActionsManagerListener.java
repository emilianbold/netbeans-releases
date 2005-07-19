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

package org.netbeans.api.debugger;

import java.util.EventListener;


/**
 * This listener notifies about changes in the 
 * {@link org.netbeans.api.debugger.DebuggerEngine}.
 *
 * @author   Jan Jancura
 */
public interface ActionsManagerListener extends EventListener {

    /** 
     * Property name constant for 
     * {@link #actionPerformed(Object)} event.
     * It should be use as a propertyName argument in 
     * {@link ActionsManager#addActionsManagerListener(String,ActionsManagerListener)}
     * call, if you would like to receive this event notification.
     */
    public static final String              PROP_ACTION_PERFORMED = "actionPerformed"; // NOI18N
    /** 
     * Property name constant for 
     * {@link #actionPerformed(Object)} event.
     * It should be use as a propertyName argument in 
     * {@link ActionsManager#addActionsManagerListener(String,ActionsManagerListener)}
     * call, if you would like to receive this event notification.
     */
    public static final String              PROP_ACTION_STATE_CHANGED = "actionStateChanged"; // NOI18N
    
    /**
     * Called when some action is performed.
     *
     * @param action action constant
     */
    public void actionPerformed (
        Object action
    );
    
    /**
     * Called when a state of some action has been changed.
     *
     * @param action action constant
     * @param enabled a new state of action
     */
    public void actionStateChanged (
        Object action, boolean enabled
    );
}

