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
import java.io.File;

/** 
*
* @author Petr Pisl
*/
public interface TomcatWebModuleCookie extends Node.Cookie {

    public void undeploy();
    
    public void start ();
    
    public void stop ();
    
    public boolean isRunning();
    
    public void openLog();
    
    public boolean hasLogger();

}
