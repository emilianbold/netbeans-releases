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

/*
 * IncrementalDeployment.java
 *
 */

package org.netbeans.modules.j2ee.deployment.plugins.spi;

//import org.netbeans.modules.debugger.java.JavaDebuggerType;
//import org.netbeans.api.debugger.DebuggerInfo;

import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressObject;

/**
 * Start Server functionality.  typically Start/Stop of Target server is through
 * jsr77 calls directly, as long as the j2eeserver correspondences of Target
 * are StateManageable - if not we have to disable this action anyway.
 * Debugging should b
 *
 * @author George FinKlang
 * @version 0.1
 */

public interface StartServer extends DeploymentManagerWrapper {
    
    public boolean supportsStartDeploymentManager();
    
    // use ProgressObject to follow event stream only
    public ProgressObject startDeploymentManager();
    
    public boolean supportsDebugging(Target target);
    
    // pending returns null if debugging is not possible for this server.
    // does whatever necessary to turn debugging on.
    // Target could represent multiple vms
    // @returns DebugInfo to specify the jpda info
    // PENDING use JpdaDebugInfo from debuggercore
    // PENDING should be readded once debugger module is integrated
    // public DebuggerInfo[] startDebugging(Target target);
    
    // PENDING is this necessary?  Will this correspond with a user
    // interaction?
    public void stopDebugging(Target target);
    
    // PENDING should be removed and replaced with direct jsr77 calls.
    public ProgressObject startServer(Target target);
    
}
