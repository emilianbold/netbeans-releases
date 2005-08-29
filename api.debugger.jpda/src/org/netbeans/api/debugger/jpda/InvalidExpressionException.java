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

package org.netbeans.api.debugger.jpda;


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

