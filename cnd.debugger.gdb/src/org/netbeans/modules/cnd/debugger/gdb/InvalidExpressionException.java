/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.debugger.gdb;


/**
 * Notification about bad expression.
 *
 * @author   Jan Jancura
 */
public class InvalidExpressionException extends Exception {

    private Throwable throwable;


    /**
     * Constructs a InvalidExpressionException with given message.
     *
     * @param message a exception message
     */
    public InvalidExpressionException (String message) {
        super (message);
    }

    /**
     * Constructs a InvalidExpressionException for a given target exception.
     *
     * @param t a target exception
     */
    public InvalidExpressionException (Throwable t) {
        super (t.getMessage ());// == null ? t.getClass ().getName () : t.getMessage ());
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

