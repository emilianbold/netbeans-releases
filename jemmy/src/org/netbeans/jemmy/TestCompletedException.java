/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
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
     * @return test status
     */
    public int getStatus() {
	return(status);
    }
}
