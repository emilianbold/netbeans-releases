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
