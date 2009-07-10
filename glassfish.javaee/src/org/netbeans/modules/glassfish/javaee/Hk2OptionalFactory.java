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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.javaee;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.swing.event.ChangeListener;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.glassfish.javaee.db.Hk2DatasourceManager;
import org.netbeans.modules.glassfish.javaee.ide.FastDeploy;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.AntDeploymentProvider;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.JDBCDriverDeployer;
import org.netbeans.modules.j2ee.deployment.plugins.spi.MessageDestinationDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInitializationException;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInstanceDescriptor;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.InstantiatingIterator;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;


/**
 *
 * @author Ludovic Champenois
 * @author Peter Williams
 * @author vince kraemer
 */
public class Hk2OptionalFactory extends OptionalDeploymentManagerFactory {
    private DeploymentFactory df;
    private ServerUtilities commonUtilities;

    protected Hk2OptionalFactory(DeploymentFactory df, ServerUtilities su) {
        this.df = df;
        this.commonUtilities = su;
    }

    public static Hk2OptionalFactory createPrelude() {
        ServerUtilities t = ServerUtilities.getPreludeUtilities();
        return null == t ? null : new Hk2OptionalFactory(Hk2DeploymentFactory.createPrelude(),
                t);
    }
    
    public static Hk2OptionalFactory createEe6() {
        ServerUtilities t = ServerUtilities.getEe6Utilities();
        return null == t ? null : new Hk2OptionalFactory(Hk2DeploymentFactory.createEe6(),
                t);
    }
    
    public StartServer getStartServer(DeploymentManager dm) {
        return new Hk2StartServer(dm);
    }
    
    public IncrementalDeployment getIncrementalDeployment(DeploymentManager dm) {
        return dm instanceof Hk2DeploymentManager ?
                new FastDeploy((Hk2DeploymentManager) dm) : null;
    }
    
    public FindJSPServlet getFindJSPServlet(DeploymentManager dm) {
        // if assertions are on... blame the caller
        assert dm instanceof Hk2DeploymentManager : "dm isn't an hk2dm";  // NOI18N
        // this code might actually be in production. log the bogus-ness and degrade gracefully
        FindJSPServlet retVal = null;
        try {
            Hk2DeploymentManager hk2dm = (Hk2DeploymentManager) dm;
            if (hk2dm.getCommonServerSupport().getInstanceProperties().get(GlassfishModule.DOMAINS_FOLDER_ATTR) != null) {
                retVal = new FindJSPServletImpl((Hk2DeploymentManager) dm, this);
            }
        } catch (ClassCastException cce) {
            Logger.getLogger("glassfish-javaee").log(Level.FINER, "caller passed invalid param", cce); // NOI18N
        }
        return retVal;
    }

    @Override
    public boolean isCommonUIRequired() {
        return false;
    }
    
    @Override
    public InstantiatingIterator getAddInstanceIterator() {
        return new J2eeInstantiatingIterator(commonUtilities);
    }
    
    @Override
    public DatasourceManager getDatasourceManager(DeploymentManager dm) {
        return dm instanceof Hk2DeploymentManager ?
                new Hk2DatasourceManager((Hk2DeploymentManager) dm) : null;
    }
    
    @Override
    public JDBCDriverDeployer getJDBCDriverDeployer(DeploymentManager dm) {
        // if assertions are on... blame the caller
        assert dm instanceof Hk2DeploymentManager : "dm isn't an hk2dm";  // NOI18N
        // this code might actually be in production. log the bogus-ness and degrade gracefully
        JDBCDriverDeployer retVal = null;
        try {
            retVal = new JDBCDriverDeployerImpl((Hk2DeploymentManager) dm, this);
        } catch (ClassCastException cce) {
            Logger.getLogger("glassfish-javaee").log(Level.FINER, "caller passed invalid param", cce); // NOI18N
        }
        return retVal;
    }
    
    @Override
     public MessageDestinationDeployment getMessageDestinationDeployment(DeploymentManager dm) {
        return dm instanceof Hk2DeploymentManager ?
                new Hk2MessageDestinationManager((Hk2DeploymentManager) dm) : null;
    }

    @Override
    public AntDeploymentProvider getAntDeploymentProvider(DeploymentManager dm) {
        // if assertions are on... blame the caller
        assert dm instanceof Hk2DeploymentManager : "dm isn't an hk2dm";  // NOI18N
        // this code might actually be in production. log the bogus-ness and degrade gracefully
        AntDeploymentProvider retVal = null;
        try {
            retVal = new AntDeploymentProviderImpl((Hk2DeploymentManager) dm, this);
        } catch (ClassCastException cce) {
            Logger.getLogger("glassfish-javaee").log(Level.FINER, "caller passed invalid param", cce); // NOI18N
        }
        return retVal;
    }

    @Override
    public ServerInstanceDescriptor getServerInstanceDescriptor(DeploymentManager dm) {
        ServerInstanceDescriptor result = null;
        if(dm instanceof Hk2DeploymentManager) {
            result = new Hk2ServerInstanceDescriptor((Hk2DeploymentManager) dm);
        } else {
            Logger.getLogger("glassfish-javaee").log(Level.WARNING, "Invalid deployment manager: " + dm); // NOI18N
        }
        return result;
    }

    private static class J2eeInstantiatingIterator implements InstantiatingIterator {
        
        private final InstantiatingIterator delegate;
        private ServerUtilities su;

        public J2eeInstantiatingIterator(ServerUtilities su) {
            this.delegate = su.getAddInstanceIterator();
            this.su = su;
        }

        public void removeChangeListener(ChangeListener l) {
            delegate.removeChangeListener(l);
        }

        public void previousPanel() {
            delegate.previousPanel();
        }

        public void nextPanel() {
            delegate.nextPanel();
        }

        public String name() {
            return delegate.name();
        }

        public boolean hasPrevious() {
            return delegate.hasPrevious();
        }

        public boolean hasNext() {
            return delegate.hasNext();
        }

        public Panel current() {
            return delegate.current();
        }

        public void addChangeListener(ChangeListener l) {
            delegate.addChangeListener(l);
        }

        public void uninitialize(WizardDescriptor wizard) {
            delegate.uninitialize(wizard);
        }

        public Set instantiate() throws IOException {
            Set set = delegate.instantiate();
            if(!set.isEmpty()) {
                Object obj = set.iterator().next();
                if(obj instanceof ServerInstance) {
                    ServerInstance instance = (ServerInstance) obj;
                    Lookup lookup = su.getLookupFor(instance);
                    if (lookup != null) {
                        JavaEEServerModule module = lookup.lookup(JavaEEServerModule.class);
                        if(module != null) {
                            return Collections.singleton(module.getInstanceProperties());
                        } else {
                            Logger.getLogger("glassfish-javaee").log(Level.WARNING,
                                    "No JavaEE facade found for " + instance.getDisplayName());
                        }
                    } else {
                        Logger.getLogger("glassfish-javaee").log(Level.WARNING,
                                "No lookup found for " + instance.getDisplayName());
                    }
                } else {
                    Logger.getLogger("glassfish-javaee").log(Level.WARNING,
                            "AddServerWizard iterator must return a set of ServerInstance objects.");
                }
            }
            return Collections.EMPTY_SET;
        }

        public void initialize(WizardDescriptor wizard) {
            delegate.initialize(wizard);
        }
        
    }


    @Override
    public void finishServerInitialization() throws ServerInitializationException {
        try {
            // remove any invalid server definitions...
            String[] urls = InstanceProperties.getInstanceList();
            if (null != urls) {
                List<String> needToRemove = new ArrayList<String>();
                for (String url : urls) {
                    if (df.handlesURI(url)) {
                        InstanceProperties ip = InstanceProperties.getInstanceProperties(url);
                        String installDirName = ip.getProperty(GlassfishModule.GLASSFISH_FOLDER_ATTR);
                        String domainDirName = ip.getProperty(GlassfishModule.DOMAINS_FOLDER_ATTR)+
                                File.separator+ip.getProperty(GlassfishModule.DOMAIN_NAME_ATTR);
                        File instDir = new File(installDirName);
                        File domainDir = new File(domainDirName);
                        // TODO -- more complete test here...
                        // TODO -- need to account for remote domain here?
                        if (!instDir.exists() || !instDir.isDirectory() || 
                                !domainDir.exists() || !domainDir.isDirectory() ||
                                !domainDir.canWrite()) {
                            needToRemove.add(url);
                        }
                    }
                }
                for (String url : needToRemove) {
                    InstanceProperties.removeInstance(url);
                }
            }
            //
            final boolean needToRegisterDefaultServer =
                    !NbPreferences.forModule(this.getClass()).getBoolean(ServerUtilities.PROP_FIRST_RUN, false);
            if (needToRegisterDefaultServer) {
                commonUtilities.getServerProvider();
            }
        } catch (Exception ex) {
            throw new ServerInitializationException("failed to init default instance", ex);
        }
    }
}
