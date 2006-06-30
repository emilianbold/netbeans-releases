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

