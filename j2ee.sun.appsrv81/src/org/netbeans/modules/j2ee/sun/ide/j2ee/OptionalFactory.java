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
/*
 * OptionalFactory.java
 *
 * Created on January 12, 2004, 4:15 PM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import org.openide.WizardDescriptor;

import javax.enterprise.deploy.spi.DeploymentManager;

import org.netbeans.modules.j2ee.deployment.plugins.api.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.api.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.api.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.api.StartServer;
import org.netbeans.modules.j2ee.deployment.plugins.api.TargetModuleIDResolver;
import org.netbeans.modules.j2ee.sun.ide.j2ee.jsps.FindJSPServletImpl;
import org.netbeans.modules.j2ee.sun.ide.j2ee.incrdeploy.DirectoryDeploymentFacade;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.FinderUIWizardIterator;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.TargetServerData;


/**
 *
 * @author  ludo
 */


public  class OptionalFactory extends OptionalDeploymentManagerFactory {
    
    /** Creates a new instance of OptionalFactory */
    public OptionalFactory () {
    }
    
    public FindJSPServlet getFindJSPServlet (DeploymentManager dm) {
        return new FindJSPServletImpl (dm);
    }
    
    public IncrementalDeployment getIncrementalDeployment (DeploymentManager dm) {
        return new DirectoryDeploymentFacade (dm);
    }
    
    public StartServer getStartServer (DeploymentManager dm) {
        return new StartSunServer (dm);
    }
    
    /** Create AutoUndeploySupport for the given DeploymentManager.
     * The instance returned by this method will be cached by the j2eeserver.
     */
    public TargetModuleIDResolver getTargetModuleIDResolver(DeploymentManager dm) {
        return null;
    }
    
    public WizardDescriptor.InstantiatingIterator getAddInstanceIterator() {
        FinderUIWizardIterator iterator = new FinderUIWizardIterator();
        iterator.setTargetServerData(new TargetServerData());
        return iterator;
    }
}
