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
 * This {@link DebuggerManagerListener} modification is designed to be 
 * registerred in "META-INF/debugger/".
 *
 * @author   Jan Jancura
 */
public interface LazyDebuggerManagerListener extends DebuggerManagerListener {

    /**
     * Returns list of properties this listener is listening on.
     *
     * @return list of properties this listener is listening on
     */
    public String[] getProperties ();
}
