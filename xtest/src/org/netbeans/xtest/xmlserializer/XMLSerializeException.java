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
 * XMLBeanException.java
 *
 * Created on September 9, 2002, 11:21 PM
 */

package org.netbeans.xtest.xmlserializer;


/**
 * XMLBean exception - when anything goes wrong with XMLBean
 * @author  breh
 */
public class XMLSerializeException extends Exception {
    
    /**
     * Creates a new instance of <code>XMLBeanException</code> without detail message.
     */
    public XMLSerializeException() {
    	super();
    }
    
    
    /**
     * Constructs an instance of <code>XMLBeanException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public XMLSerializeException(String msg) {
        super(msg);
    }
    
    public XMLSerializeException(String msg, Throwable cause) {
    	super(msg, cause);
    }
}
