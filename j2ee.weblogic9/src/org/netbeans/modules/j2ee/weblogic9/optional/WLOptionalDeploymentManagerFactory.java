/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.weblogic9.optional;

import javax.enterprise.deploy.spi.*;

import org.openide.WizardDescriptor.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;

import org.netbeans.modules.j2ee.weblogic9.ui.wizard.*;

/**
 * An entry point to the plugin's optional functionality, such as server 
 * start/stop, incremental deployment, custom wizard for instance addition and
 * the ability to locate the servlet for a jsp page.
 * 
 * @author Kirill Sorokin
 */
public class WLOptionalDeploymentManagerFactory extends OptionalDeploymentManagerFactory {
    
    /**
     * Returns an object responsible for starting a particular server instance.
     * The information about the instance is fetched from the supplied 
     * deployment manager.
     * 
     * @param dm the server's deployment manager
     * 
     * @return an object for starting/stopping the server
     */
    public StartServer getStartServer(DeploymentManager dm) {
        return new WLStartServer(dm);
    }

    /**
     * Returns an object responsible for performing incremental deployment on
     * a particular server instance. The instance information should be fetched 
     * from the supplied deployment manager.
     * We do not support that, thus return null
     * 
     * @param dm the server's deployment manager
     * 
     * @return an object for performing the incremental deployment, i.e. null
     */
    public IncrementalDeployment getIncrementalDeployment(DeploymentManager dm) {
        return null;
    }

    /**
     * Returns an object responsible for finding a corresponsing servlet for a 
     * given jsp deployed on a particular server instance. Instance data should 
     * be fetched from the supplied deployment manager.
     * We do not support that, thus return null
     * 
     * @param dm the server's deployment manager
     * 
     * @return an object for finding the servlet, i.e. null
     */
    public FindJSPServlet getFindJSPServlet(DeploymentManager dm) {
        return null;
    }

    /**
     * Returns an instance of the custom wizard for adding a server instance.
     * 
     * @return a custom wizard
     */
    public InstantiatingIterator getAddInstanceIterator() {
        return new WLInstantiatingIterator();
    }
    
}
