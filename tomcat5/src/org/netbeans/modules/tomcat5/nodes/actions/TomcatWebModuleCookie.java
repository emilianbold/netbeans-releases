/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5.nodes.actions;

import org.openide.nodes.Node;

/** 
 * Tomcat web module cookie
 * @author Petr Pisl
 */
public interface TomcatWebModuleCookie extends Node.Cookie {

    /**
     * Undeploys the web module from the server.
     */    
    public void undeploy();

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
