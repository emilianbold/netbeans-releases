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
