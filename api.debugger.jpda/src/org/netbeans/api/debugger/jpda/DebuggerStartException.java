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

package org.netbeans.api.debugger.jpda;


/**
 * Notification about problems during debugger start.
 *
 * @author   Jan Jancura
 */
public class DebuggerStartException extends Exception {

    private Throwable throwable;


    /**
     * Constructs a DebuggerStartException with given message.
     *
     * @param message a exception message
     */
    public DebuggerStartException (String message) {
        super (message);
    }

    /**
     * Constructs a DebuggerStartException for a given target exception.
     *
     * @param t a target exception
     */
    public DebuggerStartException (Throwable t) {
        super (t.getMessage ());
        initCause(t);
        throwable = t;
    }
    
    /**
     * Get the thrown target exception.
     *
     * @return the thrown target exception
     */
    public Throwable getTargetException () {
        return throwable;
    }
}

