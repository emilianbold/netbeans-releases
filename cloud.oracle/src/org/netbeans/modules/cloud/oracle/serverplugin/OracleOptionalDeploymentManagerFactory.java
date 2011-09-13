/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cloud.oracle.serverplugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibrary;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInitializationException;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerLibraryFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerLibraryImplementation;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerLibraryManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.openide.WizardDescriptor.InstantiatingIterator;

/**
 *
 */
public class OracleOptionalDeploymentManagerFactory extends OptionalDeploymentManagerFactory {

    @Override
    public StartServer getStartServer(DeploymentManager dm) {
        return new OracleStartServer();
    }

    @Override
    public IncrementalDeployment getIncrementalDeployment(DeploymentManager dm) {
        return null;
    }

    @Override
    public InstantiatingIterator getAddInstanceIterator() {
        return null;
    }
    
    @Override
    public FindJSPServlet getFindJSPServlet(DeploymentManager dm) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public boolean isCommonUIRequired() {
        return false;
    }

    @Override
    public void finishServerInitialization() throws ServerInitializationException {
        OracleJ2EEServerInstanceProvider.getProvider().refreshServers();
    }

    @Override
    public ServerLibraryManager getServerLibraryManager(DeploymentManager dm) {
        return new ServerLibraryManagerImpl();
    }
    
    private static final class ServerLibraryManagerImpl implements ServerLibraryManager {

        @Override
        public Set<ServerLibrary> getDeployableLibraries() {
            return Collections.<ServerLibrary>emptySet();
        }

        @Override
        public Set<ServerLibrary> getDeployedLibraries() {
            
            // list of libraries seems to be fixed for WLS in cloud; 
            // check WSL console for full list; this is jsut subset relevant for NetBeans
            
            Set<ServerLibrary> res = new HashSet<ServerLibrary>();
            res.add(ServerLibraryFactory.createServerLibrary(
                    new ServerLibraryImpl("JavaServer Faces", "JSF Reference Implementation", "1.2", "1.2.9.0", "jsf"))); // NOI18N
            res.add(ServerLibraryFactory.createServerLibrary(
                    new ServerLibraryImpl("JavaServer Pages Standard Tag Library (JSTL)", "JSTL Reference Implementation", "1.2", "1.2.0.1", "jstl"))); // NOI18N
            return res;
        }

        @Override
        public Set<ServerLibraryDependency> getMissingDependencies(Set<ServerLibraryDependency> dependencies) {
            return Collections.<ServerLibraryDependency>emptySet();
        }

        @Override
        public Set<ServerLibraryDependency> getDeployableDependencies(Set<ServerLibraryDependency> dependencies) {
            return Collections.<ServerLibraryDependency>emptySet();
        }

        @Override
        public void deployLibraries(Set<ServerLibraryDependency> libraries) throws ConfigurationException {
        }

    }
    
    private static final class ServerLibraryImpl implements ServerLibraryImplementation {

        private String spec;
        private String impl;
        private Version specVer;
        private Version implVer;
        private String name;

        public ServerLibraryImpl(String spec, String impl, String specVer, String implVer, String name) {
            this.spec = spec;
            this.impl = impl;
            this.specVer = Version.fromDottedNotationWithFallback(specVer);
            this.implVer = Version.fromDottedNotationWithFallback(implVer);
            this.name = name;
        }
        
        @Override
        public String getSpecificationTitle() {
            return spec;
        }

        @Override
        public String getImplementationTitle() {
            return  impl;
        }

        @Override
        public Version getSpecificationVersion() {
            return specVer;
        }

        @Override
        public Version getImplementationVersion() {
            return implVer;
        }

        @Override
        public String getName() {
            return name;
        }
        
    }
    
    public static final class OracleStartServer extends StartServer {

        @Override
        public boolean isAlsoTargetServer(Target target) {
            return true;
        }

        @Override
        public boolean supportsStartDeploymentManager() {
            return false;
        }

        @Override
        public ProgressObject startDeploymentManager() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public ProgressObject stopDeploymentManager() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public boolean needsStartForConfigure() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public boolean needsStartForTargetList() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public boolean needsStartForAdminConfig() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public boolean isRunning() {
            return true;
        }

        @Override
        public boolean isDebuggable(Target target) {
            return false;
        }

        @Override
        public ProgressObject startDebugging(Target target) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public ServerDebugInfo getDebugInfo(Target target) {
            return null;
        }
        
    }
}
