/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * MappingException.java
 *
 * Created on September 10, 2002, 9:41 PM
 */

package org.netbeans.xtest.xmlserializer;

/**
 *
 * @author  breh
 */
public class MappingException extends Exception {
    

    
    /**
     * Creates a new instance of <code>MappingException</code> without detail message.
     */
    public MappingException() {
    	super();
    }
    
    
    /**
     * Constructs an instance of <code>MappingException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public MappingException(String msg) {
    	super(msg);
    }
    
    public MappingException(String msg, Throwable cause) {
    	super(msg, cause);
    }


		
 
	    
    
}
