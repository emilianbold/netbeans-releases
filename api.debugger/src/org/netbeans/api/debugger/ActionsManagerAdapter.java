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

import java.beans.PropertyChangeEvent;

/**
 * Empty implementation of
 * {@link DebuggerEngineListener}.
 *
 * @author   Jan Jancura
 */
public class ActionsManagerAdapter implements ActionsManagerListener {

    
    /**
     * Called when some action is performed.
     *
     * @param action action constant
     * @param success returns true if action has been successfuly performed
     */
    public void actionPerformed (Object action, boolean success) {
    }
    
    /**
     * Called when a state of some action has been changed.
     *
     * @param action action constant
     * @param enabled a new state of action
     */
    public void actionStateChanged (Object action, boolean enabled) {
    }
}

