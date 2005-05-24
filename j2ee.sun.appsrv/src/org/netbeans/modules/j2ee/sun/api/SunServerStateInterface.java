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

package org.netbeans.modules.j2ee.sun.api;

/**
 * Extensions specific to our sun deployment manager: check is the server is used in a possible IDE debug session
 * In this case, we don't want to do admin calls...
 * @author  Ludo
 */
public interface SunServerStateInterface {
    
        /**
     * Returns true if this server is started in debug mode AND debugger is attached to it.
     * Doesn't matter whether the thread are suspended or not.
     */
    public boolean isDebugged() ;
    
    /**
     * Returns true if this server is started in debug mode AND debugger is attached to it 
     * AND threads are suspended (e.g. debugger stopped on breakpoint)
     */
    public boolean isSuspended() ;    

        /* return true is this  deploymment manager is running*/
    public boolean isRunning();
    
    /* display the server log file. This works only for local servers.
     **/
    
    public void viewLogFile();
}
