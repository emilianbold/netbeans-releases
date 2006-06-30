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

import java.beans.PropertyChangeEvent;

/**
 * Empty implementation of
 * {@link DebuggerManagerListener}.
 *
 * @author   Jan Jancura
 */
public class DebuggerManagerAdapter implements LazyDebuggerManagerListener {

    /**
     * Called when set of breakpoints is initialized.
     *
     * @return initial set of breakpoints
     */
    public Breakpoint[] initBreakpoints () {
        return new Breakpoint [0];
    }

    /**
     * Called when some breakpoint is added.
     *
     * @param breakpoint a new breakpoint
     */
    public void breakpointAdded (Breakpoint breakpoint) {
    }

    /**
     * Called when some breakpoint is removed.
     *
     * @param breakpoint removed breakpoint
     */
    public void breakpointRemoved (Breakpoint breakpoint) {
    }

    /**
     * Called when set of watches is initialized.
     *
     * @return initial set of watches
     */
    public void initWatches () {
    }

    /**
     * Called when some watch is added.
     *
     * @param watch a new watch
     */
    public void watchAdded (Watch watch) {
    }

    /**
     * Called when some watch is removed.
     *
     * @param watch removed watch
     */
    public void watchRemoved (Watch watch) {
    }

    /**
     * Called when some session is added.
     *
     * @param session a new session
     */
    public void sessionAdded (Session session) {
    }

    /**
     * Called when some session is removed.
     *
     * @param session removed session
     */
    public void sessionRemoved (Session session) {
    }

    /**
     * Called when some engine is added.
     *
     * @param engine a new engine
     */
    public void engineAdded (DebuggerEngine engine) {
    }

    /**
     * Called when some engine is removed.
     *
     * @param engine removed engine
     */
    public void engineRemoved (DebuggerEngine engine) {
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
    }
    
    public String[] getProperties () {
        return new String [0];
    }
}
