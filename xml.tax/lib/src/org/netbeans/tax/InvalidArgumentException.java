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
            detail = " " + Util.THIS.getString ("PROP_violating_argument", argument);
        }
        
        return super.getMessage () + detail;
    }
    
}
