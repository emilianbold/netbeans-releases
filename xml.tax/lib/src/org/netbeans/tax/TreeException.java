/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.tax;

import java.io.IOException;

/**
 * All exceptions in tree are by default unchecked so they must be
 * declared in method signature if they should behave as checked exceptions.
 * <p>
 * At many places it is accurate just mention in JavaDoc that the method
 * may throw it (if passing the right value is at callee reponsibility).
 * It must be declared just at places where callee can not explicitly
 * guarantee that the exception will not occure so it must check for it.
 * <p>
 * It is a folding exception.
 *
 * @author Libor Kramolis
 * @version 0.1
 */
public class TreeException extends Exception {

    /** Serial Version UID */
    private static final long serialVersionUID =1949769568282926780L;
    
    /** Encapsulated exception. */
    private Exception exception;
    
    //
    // init
    //

    /** Create new TreeException. */
    public TreeException (String msg, Exception exception) {
        super (msg);
        
        this.exception = exception;
    }
    

    /** Creates new TreeException with specified detail message.
     * @param msg detail message
     */
    public TreeException (String msg) {
        this (msg, null);
    }

    
    /** Creates new TreeException with specified encapsulated exception.
     * @param exc encapsulated exception
     */    
    public TreeException (Exception exc) {
        this (exc.getMessage(), exc);
    }


    //
    // itself
    //
    
    /** Get the encapsulated exception.
     * @return encapsulated encapsulated
     */    
    public Exception getException () {
        return exception;
    }
    
    /**
     */
    public void printStackTrace () {
        super.printStackTrace();
        if (exception != null) {
            System.err.println (Util.getString ("PROP_Wrapped_exception") + exception.getMessage());
            exception.printStackTrace();
        }
    }

}
