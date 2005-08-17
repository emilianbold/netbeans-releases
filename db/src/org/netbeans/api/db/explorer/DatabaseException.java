/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.db.explorer;

/**
 * Generic database exception.
 *
 * @author Slavek Psenicka, Andrei Badea
 */
public final class DatabaseException extends Exception
{
    
    static final long serialVersionUID = 7114326612132815401L;
    
    /**
     * Constructs a new exception with a specified message.
     *
     * @param message the text describing the exception.
     */
    public DatabaseException(String message) {
        super (message);
    }
    
    /**
     * Constructs a new exception with the specified cause.
     *
     * @param cause the cause of the exception.
     */
    public DatabaseException(Throwable cause) {
        super (cause);
    }
}
