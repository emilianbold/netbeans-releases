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

/**
 * @author  Libor Kramolis
 * @version 0.1
 */
public class InvalidArgumentException extends TreeException {
    
    /** Serial Version UID */
    private static final long serialVersionUID =3768694309653946597L;
    
    /** */
    private Object argument;
    
    
    //
    // init
    //
    
    /**
     * @param arg violating value
     * @param msg exception context message (or null)
     */
    public InvalidArgumentException (Object arg, String msg) {
        super (msg == null ? "" : msg); // NOI18N
        
        argument = arg;
    }
    
    /**
     */
    public InvalidArgumentException (String msg, Exception exc) {
        super (msg, exc);
        
        argument = null;
    }
    
    /**
     */
    public InvalidArgumentException (Exception exc) {
        super (exc);
        
        argument = null;
    }
    
    
    //
    // itself
    //
    
    /**
     */
    public final Object getArgument () {
        return argument;
    }
    
    /**
     * Gives additioanl message about violating argument.
     */
    public final String getMessage () {
        String detail = ""; // NOI18N
        if (argument != null) {
            detail = " " + Util.getString ("PROP_violating_argument", argument);
        }
        
        return super.getMessage () + detail;
    }
    
}
