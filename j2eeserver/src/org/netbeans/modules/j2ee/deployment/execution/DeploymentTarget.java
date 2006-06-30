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
