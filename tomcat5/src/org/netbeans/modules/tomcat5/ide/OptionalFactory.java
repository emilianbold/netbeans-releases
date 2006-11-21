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

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.AntDeploymentProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.DatasourceManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.api.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.api.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.api.StartServer;
import org.netbeans.modules.j2ee.deployment.plugins.api.TargetModuleIDResolver;
import org.netbeans.modules.tomcat5.AntDeploymentProviderImpl;
import org.netbeans.modules.tomcat5.TomcatManager.TomcatVersion;
import org.netbeans.modules.tomcat5.config.TomcatDatasourceManager;
import org.netbeans.modules.tomcat5.jsps.FindJSPServletImpl;
import org.openide.WizardDescriptor;
import org.netbeans.modules.tomcat5.wizard.AddInstanceIterator;

/**
 * OptionalFactory implementation
 *
 * @author  Pavel Buzek
 */
public class OptionalFactory extends OptionalDeploymentManagerFactory {
    
    private final TomcatVersion version;
    
    /** Creates a new instance of OptionalFactory */
    private OptionalFactory(TomcatVersion version) {
        this.version = version;
    }
    
    public static OptionalFactory create50() {
        return new OptionalFactory(TomcatVersion.TOMCAT_50);
    }
    
    public static OptionalFactory create55() {
        return new OptionalFactory(TomcatVersion.TOMCAT_55);
    }
    
    public static OptionalFactory create60() {
        return new OptionalFactory(TomcatVersion.TOMCAT_60);
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
        return new AddInstanceIterator(version);
    }
    
    public DatasourceManager getDatasourceManager(DeploymentManager dm) {
        return new TomcatDatasourceManager(dm);
    }
    
    public AntDeploymentProvider getAntDeploymentProvider(DeploymentManager dm) {
        return new AntDeploymentProviderImpl(dm);
    }
}
