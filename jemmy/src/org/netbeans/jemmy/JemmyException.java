/*
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 * 
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
 * All Rights Reserved.
 * 
 * Contributor(s): Alexandre Iline.
 * 
 * $Id$ $Revision$ $Date$
 * 
 */

package org.netbeans.jemmy;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * 
 * Parent of all Jemmy exceptions.
 * Exception can be throught from inside jemmy methods,
 * if some exception occurs from code invoked from jemmy.
 *	
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class JemmyException extends RuntimeException{

    private Exception innerException = null;
    private Object object = null;
    private String description;

    /**
     * Constructor.
     * @param description
     */
    public JemmyException(String description) {
	super(description);
    }

    /**
     * Constructor.
     * @param description
     * @param innerException Exception from code invoked from jemmy.
     */
    public JemmyException(String description, Exception innerException) {
	this(description);
	this.innerException = innerException;
    }

    /**
     * Constructor.
     * @param description
     * @param object Object regarding which exception is thrown.
     */
    public JemmyException(String description, Object object) {
	this(description);
	this.object = object;
    }

    /**
     * Constructor.
     * @param description
     * @param innerException Exception from code invoked from jemmy.
     * @param object Object regarding which exception is thrown.
     */
    public JemmyException(String description, Exception innerException, Object object) {
	this(description, innerException);
	this.object = object;
    }

    /**
     * Returns "object" constructor parameter.
     */
    public Object getObject() {
	return(object);
    }

    /**
     * Returns inner exception.
     */
    public Exception getInnerException() {
	return(innerException);
    }

    /**
     * Prints stack trace into System.out.
     */
    public void printStackTrace() {
	printStackTrace(System.out);
    }

    /**
     * Prints stack trace.
     * @param ps PrintStream to print stack trace into.
     */
    public void printStackTrace(PrintStream ps) {
	ps.println(description);
	super.printStackTrace(ps);
	if(innerException != null) {
	    ps.println("Inner exception:");
	    innerException.printStackTrace(ps);
	}
	if(object != null) {
	    ps.println("Object:");
	    ps.println(object.toString());;
	}
    }

    /**
     * Prints stack trace.
     * @param ps PrintWriter to print stack trace into.
     */
    public void printStackTrace(PrintWriter pw) {
	pw.println(description);
	super.printStackTrace(pw);
	if(innerException != null) {
	    pw.println("Inner exception:");
	    innerException.printStackTrace(pw);
	}
	if(object != null) {
	    pw.println("Object:");
	    pw.println(object.toString());;
	}
    }
}
