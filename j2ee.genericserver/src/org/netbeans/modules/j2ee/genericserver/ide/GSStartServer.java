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

package org.netbeans.modules.j2ee.genericserver.ide;

import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.plugins.api.StartServer;

/**
 *
 * @author Martin Adamek
 */
public class GSStartServer extends StartServer {
    
    public ProgressObject startDebugging(Target target) {
        return null;
    }

    public boolean isDebuggable(Target target) {
        return false;
    }

    public boolean isAlsoTargetServer(Target target) {
        return true;
    }

    public ServerDebugInfo getDebugInfo(Target target) {
        return null;
    }

    public boolean supportsStartDeploymentManager() {
        return false;
    }

    public ProgressObject stopDeploymentManager() {
        return null;
    }

    public ProgressObject startDeploymentManager() {
        return null;
    }

    public boolean needsStartForTargetList() {
        return false;
    }

    public boolean needsStartForConfigure() {
        return false;
    }

    public boolean needsStartForAdminConfig() {
        return false;
    }

    public boolean isRunning() {
        return false;
    }
    
}
