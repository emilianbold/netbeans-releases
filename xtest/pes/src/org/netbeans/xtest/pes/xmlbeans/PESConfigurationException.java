/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * PESConfigurationException.java
 *
 * Created on June 12, 2002, 5:10 PM
 */

package org.netbeans.xtest.pes.xmlbeans;

/**
 *
 * @author  mb115822
 */
public class PESConfigurationException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>PESConfigurationException</code> without detail message.
     */
    public PESConfigurationException() {
    }
    
    
    /**
     * Constructs an instance of <code>PESConfigurationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public PESConfigurationException(String msg) {
        super(msg);
    }
}
