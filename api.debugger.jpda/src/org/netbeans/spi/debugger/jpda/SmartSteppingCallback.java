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

