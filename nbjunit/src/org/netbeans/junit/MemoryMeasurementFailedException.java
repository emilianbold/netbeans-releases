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


/*
 * MemoryMeasurementFailedException.java
 *
 * Created on August 11, 2003, 3:43 PM
 */

package org.netbeans.junit;

/** Throws when MemoryMeasurement methods are having some problem
 * @author Martin Brehovsky
 */
public class MemoryMeasurementFailedException extends java.lang.RuntimeException {
    
    /**
     * Creates a new instance of <code>InitializationException</code> without detail message.
     */
    public MemoryMeasurementFailedException() {
    }
    
    
    /**
     * Constructs an instance of <code>InitializationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public MemoryMeasurementFailedException(String msg) {
        super(msg);
    }
    
    /** Constructs an instance of <code>InitializationException</code> with the specified detail message.
     * @param cause Cause of the exception
     * @param msg the detail message.
     */
    public MemoryMeasurementFailedException(String msg, Throwable cause) {
        super(msg,cause);
    }    
}
