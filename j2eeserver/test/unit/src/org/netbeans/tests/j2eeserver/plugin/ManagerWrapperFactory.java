/*
 *                 Sun Public License Notice
 *
 * The contents of thisfile are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.tests.j2eeserver.plugin;

import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;

/**
 *
 * @author  nn136682
 */
public class ManagerWrapperFactory extends OptionalDeploymentManagerFactory {
    
    /** Creates a new instance of ManagerWrapperFactory */
    public ManagerWrapperFactory() {
    }
    
    public FindJSPServlet getFindJSPServlet(javax.enterprise.deploy.spi.DeploymentManager dm) {
        return null;
    }
    
    public IncrementalDeployment getIncrementalDeployment(javax.enterprise.deploy.spi.DeploymentManager dm) {
        return new IncrementalDeploySupport(dm);
    }
    
    public StartServer getStartServer(javax.enterprise.deploy.spi.DeploymentManager dm) {
        return new ServerLifecycle(dm);
    }
    
}
