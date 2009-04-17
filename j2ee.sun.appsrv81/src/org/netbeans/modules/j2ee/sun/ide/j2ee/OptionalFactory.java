/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInitializationException;
import org.netbeans.modules.j2ee.sun.ide.dm.ServerInstanceDescriptorImpl;
import org.netbeans.modules.j2ee.deployment.plugins.spi.AntDeploymentProvider;
import org.openide.WizardDescriptor;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.MessageDestinationDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.JDBCDriverDeployer;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInstanceDescriptor;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.j2ee.deployment.plugins.spi.TargetModuleIDResolver;
import org.netbeans.modules.j2ee.sun.ide.dm.SunDatasourceManager;
import org.netbeans.modules.j2ee.sun.ide.dm.SunDeploymentManager;
import org.netbeans.modules.j2ee.sun.ide.dm.SunJDBCDriverDeployer;
import org.netbeans.modules.j2ee.sun.ide.dm.SunMessageDestinationDeployment;
import org.netbeans.modules.j2ee.sun.ide.j2ee.jsps.FindJSPServletImpl;
import org.netbeans.modules.j2ee.sun.ide.j2ee.incrdeploy.DirectoryDeploymentFacade;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.AddDomainWizardIterator;

/**
 *
 * @author  ludo
 */
public  class OptionalFactory extends OptionalDeploymentManagerFactory {
    
    private String serverVersion = PlatformValidator.APPSERVERSJS;
        
    /** Creates a new instance of OptionalFactory */
    private OptionalFactory (String version) {
        this.serverVersion = version;
    }
    
    public OptionalFactory () {
    }
    
    public static OptionalFactory createFactoryForGF_V1() {
        return new OptionalFactory(PlatformValidator.GLASSFISH_V1);
    }
    
    public static OptionalFactory createFactoryForGF_V2() {
        return new OptionalFactory(PlatformValidator.GLASSFISH_V2);
    }
    
    public static OptionalFactory createFactoryForSF_V1() {
        return new OptionalFactory(PlatformValidator.SAILFIN_V1);
    }
    
    public FindJSPServlet getFindJSPServlet (DeploymentManager dm) {
        return new FindJSPServletImpl (dm);
    }
    
    public IncrementalDeployment getIncrementalDeployment (DeploymentManager dm) {
        return DirectoryDeploymentFacade.get(dm);
    }
    
    public StartServer getStartServer (DeploymentManager dm) {
        return StartSunServer.get(dm);
    }
    
    /** Create AutoUndeploySupport for the given DeploymentManager.
     * The instance returned by this method will be cached by the j2eeserver.
     */
    public TargetModuleIDResolver getTargetModuleIDResolver(DeploymentManager dm) {
        return null;
    }
    
    public WizardDescriptor.InstantiatingIterator getAddInstanceIterator() {
        WizardDescriptor.InstantiatingIterator retVal = 
                new AddDomainWizardIterator(new PlatformValidator(), serverVersion);
        return retVal;
    }
    
    public AntDeploymentProvider getAntDeploymentProvider(DeploymentManager dm) {
        return new AntDeploymentProviderImpl(dm);
    }

    public DatasourceManager getDatasourceManager(DeploymentManager dm) {
        if (!(dm instanceof SunDeploymentManager))
            throw new IllegalArgumentException("");

        SunDatasourceManager dsMgr = new SunDatasourceManager(dm);
        return dsMgr;
    }
    
    public MessageDestinationDeployment getMessageDestinationDeployment(DeploymentManager dm) {
        if (!(dm instanceof SunDeploymentManager))
            throw new IllegalArgumentException("");
        SunMessageDestinationDeployment destMgr = new SunMessageDestinationDeployment(dm);
        return destMgr;
    }
    
    public JDBCDriverDeployer getJDBCDriverDeployer(DeploymentManager dm) {
        return new SunJDBCDriverDeployer(dm);
    }

    public ServerInstanceDescriptor getServerInstanceDescriptor(DeploymentManager dm) {
        if (!(dm instanceof SunDeploymentManager))
            throw new IllegalArgumentException("");
        
        return new ServerInstanceDescriptorImpl((SunDeploymentManager) dm);
    }

    @Override
    public void finishServerInitialization() throws ServerInitializationException {
        RunTimeDDCatalog.getRunTimeDDCatalog().refresh();
        super.finishServerInitialization();
    }
    
}
