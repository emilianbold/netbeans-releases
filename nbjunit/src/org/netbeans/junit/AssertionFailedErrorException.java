/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1999-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.junit;

/*
 * AssertionFileFailedError.java
 *
 * Created on March 21, 2002, 3:05 PM
 */
import junit.framework.AssertionFailedError;
import java.io.PrintWriter;
import java.io.PrintStream;

/** Error containing nested Exception.
 * It describes the failure and holds and print also the original Exception.
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0
 */
public class AssertionFailedErrorException extends AssertionFailedError {

    /** contains Exception that caused AssertionFailedError
     */    
    protected Throwable nestedException;

    /** Creates new AssertionFailedErrorException
     * @param nestedException contains Exception that caused AssertionFailedError
     */
    public AssertionFailedErrorException(Throwable nestedException) {
        this(null, nestedException);
    }

    /** Creates new AssertionFailedErrorException 
     *  @param message The error description menssage.
     *  @param nestedException contains Exception that caused AssertionFailedError
     */
    public AssertionFailedErrorException(String message, Throwable nestedException) {
        super(message);
        this.nestedException = nestedException;
    }
    
    /** prints stack trace of assertion error and nested exception into System.err
     */    
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /** prints stack trace of assertion error and nested exception
     * @param err PrintWriter where to print stack trace
     */    
    public void printStackTrace(PrintWriter err) {
        synchronized (err) {
            super.printStackTrace(err);
            err.println("\nNested Exception is:");
            nestedException.printStackTrace(err);
        }
    }

    /** prints stack trace of assertion error and nested exception
     * @param err PrintStream where to print stack trace
     */    
    public void printStackTrace(PrintStream err) {
        synchronized (err) {
            super.printStackTrace(err);
            err.println("\nNested Exception is:");
            nestedException.printStackTrace(err);
        }
    }
}
