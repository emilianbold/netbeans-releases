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
 * This listener notifies about changes in the 
 * {@link org.netbeans.api.debugger.DebuggerManager} - breakpoints, watches
 * and sessions.
 *
 * @author   Jan Jancura
 */
public interface DebuggerManagerListener extends PropertyChangeListener {

    /**
     * Called when current thread is changed.
     *
     * @param oldThread the old current thread
     * @param newThread the new current thread
     */
//    public void currentThreadChanged (
//        AbstractThread oldThread,
//        AbstractThread newThread
//    );

    /**
     * Called when some breakpoint is added.
     *
     * @param breakpoint a new breakpoint
     */
    public void breakpointAdded (Breakpoint breakpoint);

    /**
     * Called when some breakpoint is removed.
     *
     * @param breakpoint removed breakpoint
     */
    public void breakpointRemoved (Breakpoint breakpoint);

    /**
     * Called when some watch is added.
     *
     * @param watch a new watch
     */
    public void watchAdded (Watch watch);

    /**
     * Called when some watch is removed.
     *
     * @param watch removed watch
     */
    public void watchRemoved (Watch watch);

    /**
     * Called when some session is added.
     *
     * @param session a new session
     */
    public void sessionAdded (Session session);

    /**
     * Called when some session is removed.
     *
     * @param session removed session
     */
    public void sessionRemoved (Session session);

    /**
     * Called when some debugger engine is added.
     *
     * @param engine a new debugger engine
     */
//    public void engineAdded (DebuggerEngine engine);

    /**
     * Called when some debugger engine is removed.
     *
     * @param engine removed debugger engine
     */
//    public void engineRemoved (DebuggerEngine engine);
}
