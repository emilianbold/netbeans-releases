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
