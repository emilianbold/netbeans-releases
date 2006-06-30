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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

        /* return true is this  deploymment manager is running*/
    public boolean isRunning();
    
    /* display the server log file. This works only for local servers.
     **/
    
    public void viewLogFile();
}
