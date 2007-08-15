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

package org.netbeans.modules.tomcat5.nodes.actions;

import org.openide.nodes.Node;
import org.openide.util.RequestProcessor.Task;

/**
 * Tomcat web module cookie
 * @author Petr Pisl
 */
public interface TomcatWebModuleCookie extends Node.Cookie {

    /**
     * Undeploys the web module from the server.
     * 
     * @return scheduled task that performs undeploy
     */
    public Task undeploy();

    /**
     * Starts the web module.
     */
    public void start ();

    /**
     * Stops the web module.
     */
    public void stop ();

    /**
     * Returns <code>true</code> if the web module is started, <code>false</code>
     * otherwise.
     * @return <code>true</code> if the web module is started, <code>false</code>
     * otherwise
     */
    public boolean isRunning();
    
    /**
     * Opens web module's log file in the output window.
     */
    public void openLog();
    
    /**
     * Returns <code>true</code> if the web module has a logger defined, 
     * <code>false</code> otherwise.
     * @return <code>true</code> if the web module has a logger defined, 
     * <code>false</code> otherwise
     */
    public boolean hasLogger();

}
