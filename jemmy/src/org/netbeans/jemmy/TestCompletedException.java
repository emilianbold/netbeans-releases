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
 * Exception is throught as a result of test.
 * either test failed or passed.
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class TestCompletedException extends JemmyException {

    private int status;

    /**
     * Constructor.
     * @param st Exit status.
     * @param ex Exception provoked test failure.
     */
    public TestCompletedException(int st, Exception ex) {
	super("Test " + 
	      ((st == 0) ? 
	       "passed" : 
	       "failed with status " + Integer.toString(st)),
	      ex);
	status = st;
    }

    /**
     * Constructor.
     * @param st Exit status.
     * @param description Failure reason
     */
    public TestCompletedException(int st, String description) {
	super("Test " + 
	      ((st == 0) ? 
	       "passed" : 
	       "failed with status " + Integer.toString(st) +
	       "\n" + description));
	status = st;
    }

    /**
     * Returns status.
     */
    public int getStatus() {
	return(status);
    }
}
