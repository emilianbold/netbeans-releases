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

import org.netbeans.api.debugger.DebuggerEngine;

/**
 * Creates a new instance of {@link org.netbeans.api.debugger.DebuggerEngine}
 * for session. DebuggerEngine implements support for one debugger language 
 * for session.
 *
 * @author Jan Jancura
 */
public abstract class DebuggerEngineProvider {
    
    /** 
     * Returns set of language names supported by 
     * {@link org.netbeans.api.debugger.DebuggerEngine} provided by this 
     * DebuggerEngineProvider.
     *
     * @return language name
     */
    public abstract String[] getLanguages ();

    /**
     * Returns identifier of {@link org.netbeans.api.debugger.DebuggerEngine}.
     *
     * @return identifier of DebuggerEngine
     */
    public abstract String getEngineTypeID ();
       
    /**
     * Returns array of services for 
     * {@link org.netbeans.api.debugger.DebuggerEngine} provided by this 
     * DebuggerEngineProvider.
     *
     * @return array of services
     */
    public abstract Object[] getServices ();
    
    /**
     * Sets destructor for new {@link org.netbeans.api.debugger.DebuggerEngine} 
     * provided by this instance of DebuggerEngineProvider.
     *
     * @param desctuctor a desctuctor to be used for DebuggerEngine created
     *        by this instance
     */
    public abstract void setDestructor (DebuggerEngine.Destructor desctuctor);
}

