/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.projectimport;

/**
 * Indicates that the parsed project is invalid.
 *
 * @author mkrauskopf
 */
public class ProjectImporterException extends java.lang.Exception {
    
	private static final long serialVersionUID = 3258688819070055737L;

	/**
     * Creates a new instance of <code>ProjectImporterException</code> without
     * detail message.
     */
    public ProjectImporterException() {/*empty constructor*/}
    
    /**
     * Constructs an instance of <code>ProjectImporterException</code> with the
     * specified detail message.
     * 
     * @param msg the detail message.
     */
    public ProjectImporterException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>ProjectImporterException</code> with the
     * specified cause.
     * 
     * @param cause the cause exception
     */
    public ProjectImporterException(Throwable cause) {
        super(cause);
    }
    
    /**
     * Constructs an instance of <code>ProjectImporterException</code> with the
     * specified detail message and cause.
     * 
     * @param msg the detail message.
     * @param cause the cause exception
     */
    public ProjectImporterException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
