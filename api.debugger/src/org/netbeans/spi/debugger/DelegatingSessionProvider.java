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

package org.netbeans.spi.debugger;

import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.Session;


/**
 * Delegates {@link org.netbeans.api.debugger.DebuggerInfo}
 * support to some some existing 
 * {@link org.netbeans.api.debugger.Session}.
 *
 * @author Jan Jancura
 */
public abstract class DelegatingSessionProvider {
    
    /**
     * Returns a {@link org.netbeans.api.debugger.Session} to delegate 
     * on.
     *
     * @return Session to delegate on
     */
    public abstract Session getSession (
        DebuggerInfo debuggerInfo
    );
}

