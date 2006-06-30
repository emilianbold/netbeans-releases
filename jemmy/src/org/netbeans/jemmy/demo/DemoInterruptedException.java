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

package org.netbeans.jemmy.demo;

import org.netbeans.jemmy.TestCompletedException;

import java.io.PrintStream;

/**
 *
 * Exception is throught if test (demo) execution has been interrupted
 * (CommentWindow.isInterrupted() returned true).
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *
 */

public class DemoInterruptedException extends TestCompletedException {

    /**
     * Constructs a DemoInterruptedException object.
     * @param description an exception descriptio.
     */
    public DemoInterruptedException(String description) {
	super(100, description);
    }

    public void printStackTrace() {
	printStackTrace(System.out);
    }

    public void printStackTrace(PrintStream ps) {
	super.printStackTrace(ps);
    }
}
