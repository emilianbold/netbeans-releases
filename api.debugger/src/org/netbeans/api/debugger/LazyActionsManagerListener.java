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


/**
 * This {@link ActionsManagerListener} modification is designed to be 
 * registerred in "META-INF/debugger/".
 * LazyActionsManagerListener should be registerred for some concrete 
 * {@link DebuggerEngine} (use 
 * "META-INF/debugger/<DebuggerEngine-id>/LazyActionsManagerListener"), or 
 * for global {@link ActionsManager} (use 
 * "META-INF/debugger/LazyActionsManagerListener").
 * New instance of LazyActionsManagerListener implementation is loaded
 * when the new instance of {@link ActionsManager} is created, and its registerred
 * automatically to all properties returned by {@link #getProperties}. 
 *
 * @author   Jan Jancura
 */
public abstract class LazyActionsManagerListener extends ActionsManagerAdapter {

        
    /**
     * This method is called when engine dies.
     */
    protected abstract void destroy ();

    /**
     * Returns list of properties this listener is listening on.
     *
     * @return list of properties this listener is listening on
     */
    public abstract String[] getProperties ();
}
