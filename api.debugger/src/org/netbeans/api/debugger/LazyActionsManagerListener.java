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
