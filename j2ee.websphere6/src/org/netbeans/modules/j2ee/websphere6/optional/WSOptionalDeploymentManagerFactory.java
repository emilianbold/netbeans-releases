/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.websphere6.optional;

import org.netbeans.modules.j2ee.websphere6.ui.wizard.WSInstantiatingIterator;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.api.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.api.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.api.StartServer;
import org.openide.WizardDescriptor.InstantiatingIterator;

/**
 *
 * @author Kirill Sorokin
 */
public class WSOptionalDeploymentManagerFactory extends OptionalDeploymentManagerFactory {
    
    public StartServer getStartServer(DeploymentManager dm) {
        return new WSStartServer(dm);
    }

    public IncrementalDeployment getIncrementalDeployment(DeploymentManager dm) {
        return null;
    }

    public FindJSPServlet getFindJSPServlet(DeploymentManager dm) {
        return null;
    }

    public InstantiatingIterator getAddInstanceIterator() {
        return new WSInstantiatingIterator();
    }
    
}
