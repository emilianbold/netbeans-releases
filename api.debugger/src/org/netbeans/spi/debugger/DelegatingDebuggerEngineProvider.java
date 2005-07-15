/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.debugger;

import org.netbeans.api.debugger.DebuggerEngine;

/**
 * Delegates support for some language to some existing 
 * {@link org.netbeans.api.debugger.DebuggerEngine}.
 *
 * @author Jan Jancura
 */
public abstract class DelegatingDebuggerEngineProvider {
    
    /** 
     * Returns set of language names supported by 
     * {@link org.netbeans.api.debugger.DebuggerEngine} provided by this 
     * DelegatingDebuggerEngineProvider.
     *
     * @return language name
     */
    public abstract String[] getLanguages ();

    /**
     * Returns a {@link org.netbeans.api.debugger.DebuggerEngine} to delegate 
     * on.
     *
     * @return DebuggerEngine todelegate on
     */
    public abstract DebuggerEngine getEngine ();
    
    /**
     * Sets destructor for new {@link org.netbeans.api.debugger.DebuggerEngine} 
     * returned by this instance of DebuggerEngineProvider.
     *
     * @param desctuctor a desctuctor to be used for DebuggerEngine returned
     *        by this instance
     */
    public abstract void setDestructor (DebuggerEngine.Destructor desctuctor);
}

