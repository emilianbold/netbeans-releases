/*
 * TestBoardLauncherException.java
 *
 * Created on October 20, 2003, 6:34 PM
 */

package org.netbeans.xtest.testrunner;

/**
 *
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
