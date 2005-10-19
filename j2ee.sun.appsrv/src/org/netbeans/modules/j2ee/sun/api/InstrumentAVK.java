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

package org.netbeans.modules.j2ee.sun.api;


import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
/**
 *
 * @author Nitya Doraisamy
 */
public interface InstrumentAVK {
    
    /*
     *@param : SunDeploymentManager associated with server
     */
    public void setDeploymentManager(SunDeploymentManagerInterface sdm);
    
    /*
     *@param : onOff - indicates whether to add or remove istrumentation
     *for AVK
     *Server could be running or stopped
     *Turning On Instrumentation : edit server classpath and jvm option
     *Turning Off Instrumentation : remove classpath and jvm option
     */
    public void setAVK(boolean onOff);
    
    /*
     *Server should be stopped before this is called. Handled by GenerateReportAction
     *in AVK plugin
     *Runs report tool and then launches browser with generated report
     */
    public void generateReport();
    
     /* Show Dialog Descriptor to choose between Static / Dynamic Verification
      * @return : static / dynamic / none
      */
    public boolean createAVKSupport(DeploymentManager dm, J2eeModuleProvider target);
}
