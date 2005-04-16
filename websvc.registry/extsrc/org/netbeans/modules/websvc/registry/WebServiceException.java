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

package org.netbeans.modules.websvc.registry;

/**
 * This exception is meant to be used as a wrapper for exceptions that occur during web service processing.  This
 * method should be used between classes where one class knows about the UI while the other SHOULD not like "Util".
 * @author David Botterill
 */
public class WebServiceException extends Exception {
    
    /** Creates a new instance of WebServiceException */
    public WebServiceException() {
    }
    public WebServiceException(String inMessage,Throwable inThrowable) {
        super(inMessage,inThrowable);
    }
    
}
