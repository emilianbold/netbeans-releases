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

package org.netbeans.spi.debugger.jpda;

import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.spi.debugger.ContextProvider;

/**
 * Listens on stepping engine and defines classes / places the debugger can 
 * stop in.
 *
 * @author   Jan Jancura
 */
public abstract class SmartSteppingCallback {
    
    
    /**
     * Defines default set of smart stepping filters. Method is called when 
     * a new JPDA debugger session is created.
     *
     * @param f a filter to be initialized
     */
    public abstract void initFilter (SmartSteppingFilter f);
    
    /**
     * This method is called during stepping through debugged application.
     * The execution is stopped when all registerred <code>SmartSteppingCallback</code>s
     * returns true.
     *
     * @param thread contains all available information about current position
     *        in debugged application
     * @param f a filter
     * @return true if execution should be stopped on the current position
     */
    public abstract boolean stopHere (ContextProvider lookupProvider, JPDAThread thread, SmartSteppingFilter f);
}

