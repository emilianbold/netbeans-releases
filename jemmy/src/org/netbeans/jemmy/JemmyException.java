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

    private Throwable innerException = null;
    private Object object = null;

    /**
     * Constructor.
     * @param description An exception description.
     */
    public JemmyException(String description) {
	super(description);
    }

    /**
     * Constructor.
     * @param description An exception description.
     * @param innerException Exception from code invoked from jemmy.
     */
    public JemmyException(String description, Throwable innerException) {
	this(description);
	this.innerException = innerException;
    }

    /**
     * Constructor.
     * @param description An exception description.
     * @param object Object regarding which exception is thrown.
     */
    public JemmyException(String description, Object object) {
	this(description);
	this.object = object;
    }

    /**
     * Constructor.
     * @param description An exception description.
     * @param innerException Exception from code invoked from jemmy.
     * @param object Object regarding which exception is thrown.
     */
    public JemmyException(String description, Throwable innerException, Object object) {
	this(description, innerException);
	this.object = object;
    }

    /**
     * Returns "object" constructor parameter.
     * @return the Object value associated with the exception.
     */
    public Object getObject() {
	return(object);
    }

    /**
     * Returns inner exception.
     * @return An inner exception.
     * @deprecated Use getInnerThrowable()
     */
    public Exception getInnerException() {
        if(innerException instanceof Exception) {
            return((Exception)innerException);
        } else {
            return(null);
        }
    }

    /**
     * Returns inner throwable.
     * @return An inner throwable.
     */
    public Throwable getInnerThrowable() {
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
     * 
     * @param	pw PrintWriter to print stack trace into.
     * 	
     */
    public void printStackTrace(PrintWriter pw) {
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
