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

