/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.deployment.execution;

import org.netbeans.modules.j2ee.deployment.impl.ServerString;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.impl.TargetModule;
import java.io.File;

/**
 *
 * @author  gfink
 */
public interface DeploymentTarget {
    
    public J2eeModule getModule ();
    
    public ModuleChangeReporter getModuleChangeReporter ();

    public ServerString getServer();
    
    public File getConfigurationFile();

    public String getClientUrl(String partUrl);
    
    public TargetModule[] getTargetModules();
    
    public void setTargetModules(TargetModule[] targetModules);
    
    public DeploymentConfigurationProvider getDeploymentConfigurationProvider();
    
    public J2eeModuleProvider.ConfigSupport getConfigSupport();
    
    public String getDeploymentName();
}
