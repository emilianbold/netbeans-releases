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

