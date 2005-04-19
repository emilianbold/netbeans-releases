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

package org.netbeans.xtest.testrunner;

/**
 * @author  mb115822
 */
public class TestBoardLauncherException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>TestBoardLauncherException</code> without detail message.
     */
    public TestBoardLauncherException() {
    }
    
    
    /**
     * Constructs an instance of <code>TestBoardLauncherException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public TestBoardLauncherException(String msg) {
        super(msg);
    }
    
    public TestBoardLauncherException(String msg, Throwable cause) {
        super(msg, cause);
    }    
}
