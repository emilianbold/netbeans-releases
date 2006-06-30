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

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.api.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.api.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.api.StartServer;
import org.netbeans.modules.j2ee.genericserver.ide.GSInstantiatingIterator;
import org.openide.WizardDescriptor.InstantiatingIterator;

/**
 *
 * @author Martin Adamek
 */
public class GSOptionalDeploymentManagerFactory extends OptionalDeploymentManagerFactory {
    
    // TODO: this is just temporary, to not show this instance in Registry
    // needs maybe option similar to is_it_bundled_tomcat to define visibility
    // current solution is only for EJB Freeform
    public StartServer getStartServer(DeploymentManager dm) {
        return null;//new GSStartServer();
    }

    public IncrementalDeployment getIncrementalDeployment(DeploymentManager dm) {
        return null;
    }

    public FindJSPServlet getFindJSPServlet(DeploymentManager dm) {
        return null;
    }

    // TODO: if returned value is null then this server in not displayed 
    // in Add server instance dialog. InstantiatingIterator should be returned
    // when whole functionality will be implemented. Current solution is only for 
    // EJB freeform project
    public InstantiatingIterator getAddInstanceIterator() {
        return null;//new GSInstantiatingIterator();
    }
    
    
}
