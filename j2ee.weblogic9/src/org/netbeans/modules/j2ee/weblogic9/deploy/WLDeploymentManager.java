/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.j2ee.weblogic9.deploy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.weblogic9.WLDeploymentFactory;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;


/**
 * Main class of the deployment process. This serves as a wrapper for the
 * server's DeploymentManager implementation, all calls are delegated to the
 * server's implementation, with the thread's context classloader updated
 * if necessary.
 *
 * @author Kirill Sorokin
 * @author Petr Hejl
 */
public class WLDeploymentManager implements DeploymentManager {

    private static final Logger LOGGER = Logger.getLogger(WLDeploymentManager.class.getName());

    private final WLDeploymentFactory factory;

    private final String uri;
    private final String host;
    private final String port;
    private volatile boolean disconnected;

    /* GuardedBy("this") */
    private WLClassLoader classLoader;

    /* GuardedBy("this") */
    private InstanceProperties instanceProperties;

    public WLDeploymentManager(WLDeploymentFactory factory, String uri,
            String host, String port, boolean disconnected) {
        this.factory = factory;
        this.uri = uri;
        this.host = host;
        this.port = port;
        this.disconnected = disconnected;
    }

    /**
     * Returns the stored server URI.
     */
    public String getUri() {
        return this.uri;
    }

    /**
     * Returns the server host stored in the instance properties.
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the server port stored in the instance properties.
     */
    public String getPort() {
        return port;
    }

    /**
     * Returns the InstanceProperties object for the current server instance.
     */
    public synchronized InstanceProperties getInstanceProperties() {
        if (instanceProperties == null) {
            this.instanceProperties = InstanceProperties.getInstanceProperties(uri);

        }
        return instanceProperties;
    }

    private synchronized ClassLoader getWLClassLoader(String serverRoot) {
        if (classLoader == null) {
            try {
                URL[] urls = new URL[] {new File(serverRoot + "/server/lib/weblogic.jar").toURI().toURL()}; // NOI18N
                classLoader = new WLClassLoader(urls, WLDeploymentManager.class.getClassLoader());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, null, e);
            }
        }
        return classLoader;
    }

    private <T> T executeAction(Action<T> action) throws ExecutionException {
        ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
        String serverRoot = getInstanceProperties().getProperty(WLPluginProperties.SERVER_ROOT_ATTR);
        // if serverRoot is null, then we are in a server instance registration process, thus this call
        // is made from InstanceProperties creation -> WLPluginProperties singleton contains
        // install location of the instance being registered
        if (serverRoot == null) {
            serverRoot = WLPluginProperties.getInstance().getInstallLocation();
        }

        Thread.currentThread().setContextClassLoader(getWLClassLoader(serverRoot));
        try {
            DeploymentManager manager = getDeploymentManager(
                    getInstanceProperties().getProperty(InstanceProperties.USERNAME_ATTR),
                    getInstanceProperties().getProperty(InstanceProperties.PASSWORD_ATTR),
                    host, port);
            try {
                return action.execute(manager);
            } finally {
                manager.release();
            }
        } catch(DeploymentManagerCreationException ex) {
            throw new ExecutionException(ex);
        } finally {
            Thread.currentThread().setContextClassLoader(originalLoader);
        }
    }

    private static DeploymentManager getDeploymentManager(String username,
            String password, String host, String port) throws DeploymentManagerCreationException {

        DeploymentManagerCreationException dmce = null;
        try {
            Class helperClazz = Class.forName("weblogic.deploy.api.tools.SessionHelper", // NOI18N
                    false, Thread.currentThread().getContextClassLoader());
            Method m = helperClazz.getDeclaredMethod("getDeploymentManager", // NOI18N
                    new Class[]{String.class, String.class, String.class, String.class});
            Object o = m.invoke(null, new Object[]{host, port, username, password});
            if (DeploymentManager.class.isAssignableFrom(o.getClass())) {
                return (DeploymentManager) o;
            } else {
                dmce = new DeploymentManagerCreationException(
                        "Instance created by WebLogic is not DeploymentManager instance.");
            }
        } catch (Exception e) {
            dmce = new DeploymentManagerCreationException(
                    "Cannot create weblogic DeploymentManager instance.");
            dmce.initCause(e);
        } catch (NoClassDefFoundError err) {
            dmce = new DeploymentManagerCreationException(
                    "Cannot create weblogic DeploymentManager instance.");
            dmce.initCause(err);
        }
        throw dmce;
    }

    public ProgressObject distribute(Target[] target, File file, File file2) throws IllegalStateException {
        if (disconnected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        WLCommandDeployer wlDeployer = new WLCommandDeployer(factory, getInstanceProperties());
        return wlDeployer.deploy(target, file, file2, getHost(), getPort());
    }

    public ProgressObject distribute(Target[] target, ModuleType moduleType, InputStream inputStream, InputStream inputStream0) throws IllegalStateException {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    public ProgressObject distribute(Target[] target, InputStream inputStream, InputStream inputStream2) throws IllegalStateException {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    public boolean isRedeploySupported() {
        return false;
    }

    public ProgressObject redeploy(TargetModuleID[] targetModuleID, InputStream inputStream, InputStream inputStream2) throws  UnsupportedOperationException, IllegalStateException {
        throw new UnsupportedOperationException("Redeploy not yet implemented");
    }

    public ProgressObject redeploy(TargetModuleID[] targetModuleID, File file, File file2) throws UnsupportedOperationException, IllegalStateException {
        throw new UnsupportedOperationException("Redeploy not yet implemented");
    }

    public ProgressObject undeploy(TargetModuleID[] targetModuleID) throws IllegalStateException {
        if (disconnected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        WLCommandDeployer wlDeployer = new WLCommandDeployer(factory, getInstanceProperties());
        return wlDeployer.undeploy(targetModuleID);
    }

    @Override
    public ProgressObject stop(TargetModuleID[] targetModuleID) throws IllegalStateException {
        if (disconnected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        WLCommandDeployer wlDeployer = new WLCommandDeployer(factory, getInstanceProperties());
        return wlDeployer.stop(targetModuleID);
    }

    @Override
    public ProgressObject start(TargetModuleID[] targetModuleID) throws IllegalStateException {
        if (disconnected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        WLCommandDeployer wlDeployer = new WLCommandDeployer(factory, getInstanceProperties());
        return wlDeployer.start(targetModuleID);
    }

    @Override
    public TargetModuleID[] getAvailableModules(final ModuleType moduleType, final Target[] target) throws TargetException, IllegalStateException {
        if (disconnected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        try {
            return executeAction(new Action<TargetModuleID[]>() {
                @Override
                public TargetModuleID[] execute(DeploymentManager manager) throws ExecutionException {
                    try {
                        return manager.getAvailableModules(moduleType, translateTargets(manager, target));
                    } catch (TargetException ex) {
                        throw new ExecutionException(ex);
                    }
                }
            });
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof TargetException) {
                throw (TargetException) ex.getCause();
            }
            LOGGER.log(Level.INFO, null, ex.getCause());
            return new TargetModuleID[] {};
        }
    }

    @Override
    public TargetModuleID[] getNonRunningModules(final ModuleType moduleType, final Target[] target) throws TargetException, IllegalStateException {
        if (disconnected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        try {
            return executeAction(new Action<TargetModuleID[]>() {
                @Override
                public TargetModuleID[] execute(DeploymentManager manager) throws ExecutionException {
                    try {
                        return manager.getNonRunningModules(moduleType, translateTargets(manager, target));
                    } catch (TargetException ex) {
                        throw new ExecutionException(ex);
                    }
                }
            });
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof TargetException) {
                throw (TargetException) ex.getCause();
            }
            LOGGER.log(Level.INFO, null, ex.getCause());
            return new TargetModuleID[] {};
        }
    }

    @Override
    public TargetModuleID[] getRunningModules(final ModuleType moduleType, final Target[] target) throws TargetException, IllegalStateException {
        if (disconnected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        try {
            return executeAction(new Action<TargetModuleID[]>() {
                @Override
                public TargetModuleID[] execute(DeploymentManager manager) throws ExecutionException {
                    try {
                        return manager.getRunningModules(moduleType, translateTargets(manager, target));
                    } catch (TargetException ex) {
                        throw new ExecutionException(ex);
                    }
                }
            });
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof TargetException) {
                throw (TargetException) ex.getCause();
            }
            LOGGER.log(Level.INFO, null, ex.getCause());
            return new TargetModuleID[] {};
        }
    }

    @Override
    public Target[] getTargets() throws IllegalStateException {
        if (disconnected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        try {
            return executeAction(new Action<Target[]>() {
                public Target[] execute(DeploymentManager manager) throws ExecutionException {
                    return manager.getTargets();
                }
            });
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex.getCause());
            return new Target[] {};
        }
    }

    @Override
    public void release() {
        disconnected = true;
    }

    public DeploymentConfiguration createConfiguration(
            DeployableObject deployableObject) throws InvalidModuleException {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    public void setLocale(Locale locale) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    public boolean isLocaleSupported(Locale locale) {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    public void setDConfigBeanVersion(DConfigBeanVersionType dConfigBeanVersionType)
            throws DConfigBeanVersionUnsupportedException {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType dConfigBeanVersionType) {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    public Locale getCurrentLocale() {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    public DConfigBeanVersionType getDConfigBeanVersion() {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    public Locale getDefaultLocale() {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    public Locale[] getSupportedLocales() {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    // TODO if possible (due to workflow of j2eeserver) reuse deployment manager
    // instead of this
    private static Target[] translateTargets(DeploymentManager manager, Target[] originalTargets) {
        Target[] targets = manager.getTargets();
        // WL does not implement equals however implements hashCode
        // it consider two Target instances coming from different
        // deployment managers different

        // perhaps we could share DeploymentManager somehow
        List<Target> deployTargets = new ArrayList<Target>(originalTargets.length);
        for (Target t : targets) {
            for (Target t2 : originalTargets) {
                if (t.hashCode() == t2.hashCode()
                        && t.getName().equals(t2.getName())) {
                    deployTargets.add(t);
                }
            }
        }
        return deployTargets.toArray(new Target[deployTargets.size()]);
    }

    private static interface Action<T> {

         T execute(DeploymentManager manager) throws ExecutionException;
    }

    private static class WLClassLoader extends URLClassLoader {

        public WLClassLoader(URL[] urls, ClassLoader parent) throws MalformedURLException {
            super(urls, parent);
        }

        public void addURL(File f) throws MalformedURLException {
            if (f.isFile()) {
                addURL(f.toURL());
            }
        }

        @Override
        protected PermissionCollection getPermissions(CodeSource codeSource) {
            Permissions p = new Permissions();
            p.add(new AllPermission());
            return p;
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            // get rid of annoying warnings
            if (name.indexOf("jndi.properties") != -1 || name.indexOf("i18n_user.properties") != -1) { // NOI18N
                return Collections.enumeration(Collections.<URL>emptyList());
            }

            return super.getResources(name);
        }
    }
}
