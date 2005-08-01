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

package org.netbeans.modules.tomcat5.ide;

import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.netbeans.modules.tomcat5.jsps.FindJSPServletImpl;

import org.openide.WizardDescriptor;
import org.netbeans.modules.tomcat5.wizard.AddInstanceIterator;

/**
 * OptionalFactory implementation for Tomcat 5.0
 *
 * @author  Pavel Buzek
 */
public class OptionalFactory extends OptionalDeploymentManagerFactory {
    /** Creates a new instance of OptionalFactory */
    public OptionalFactory () {
    }
    
    public FindJSPServlet getFindJSPServlet (javax.enterprise.deploy.spi.DeploymentManager dm) {
        return new FindJSPServletImpl (dm);
    }
    
    public IncrementalDeployment getIncrementalDeployment (javax.enterprise.deploy.spi.DeploymentManager dm) {
        return new TomcatIncrementalDeployment (dm);
    }
    
    public StartServer getStartServer (javax.enterprise.deploy.spi.DeploymentManager dm) {
        return new StartTomcat (dm);
    }
    
    public TargetModuleIDResolver getTargetModuleIDResolver(javax.enterprise.deploy.spi.DeploymentManager dm) {
        return new TMIDResolver (dm);
    }

    public WizardDescriptor.InstantiatingIterator getAddInstanceIterator() {
        return new AddInstanceIterator(TomcatManager.TOMCAT_50);
    }
}
