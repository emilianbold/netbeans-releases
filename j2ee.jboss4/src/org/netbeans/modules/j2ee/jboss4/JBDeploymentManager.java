/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.j2ee.jboss4;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import javax.naming.NameNotFoundException;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.jboss4.util.JBProperties;
import org.netbeans.modules.j2ee.jboss4.ide.JBJ2eePlatformFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginUtils;

/**
 *
 * @author Kirill Sorokin
 */
public class JBDeploymentManager implements DeploymentManager {

    private static final Logger LOGGER = Logger.getLogger(JBDeploymentManager.class.getName());
    
    private final String realUri;

    private final String jbUri;

    private final DeploymentFactory df;

    /** <i>GuardedBy("this")</i> */
    private DeploymentManager manager;

    private int debuggingPort = 8787;

    /** <i>GuardedBy("this")</i> */
    private InstanceProperties instanceProperties;

    private boolean needsRestart;

    private volatile Boolean as7;

    private JBJ2eePlatformFactory.J2eePlatformImplImpl jbPlatform;
    
    /**
     * Stores information about running instances. instance is represented by its InstanceProperties,
     *  running state by Boolean.TRUE, stopped state Boolean.FALSE.
     * WeakHashMap should guarantee erasing of an unregistered server instance bcs instance properties are also removed along with instance.
     */
    private static final Map<InstanceProperties, Boolean> propertiesToIsRunning = Collections.synchronizedMap(new WeakHashMap());

    /** Creates a new instance of JBDeploymentManager */
    public JBDeploymentManager(DeploymentFactory df, String realUri,
            String jbUri, String username, String password) {
        this.realUri = realUri;
        this.jbUri = jbUri;
        this.df = df;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Connection data methods
    ////////////////////////////////////////////////////////////////////////////
    public String getHost() {
        String host = InstanceProperties.getInstanceProperties(realUri).
                getProperty(JBPluginProperties.PROPERTY_HOST);
        return host;
    }

    public int getPort() {
        String port = InstanceProperties.getInstanceProperties(realUri).
                getProperty(JBPluginProperties.PROPERTY_PORT);
        return new Integer(port).intValue();
    }

    public int getDebuggingPort() {
        return debuggingPort;
    }

    public String getUrl() {
        return realUri;
    }


    public synchronized InstanceProperties getInstanceProperties() {
        if (instanceProperties == null) {
            instanceProperties = InstanceProperties.getInstanceProperties(realUri);
        }
        return instanceProperties;
    }

    private synchronized <T> T executeAction(final Action<T> action) throws Exception {
        return invokeLocalAction(new Callable<T>() {

            @Override
            public T call() throws Exception {
                DeploymentManager manager = null;
                try {
                    manager = getDeploymentManager(jbUri,
                            getInstanceProperties().getProperty(InstanceProperties.USERNAME_ATTR),
                            getInstanceProperties().getProperty(InstanceProperties.PASSWORD_ATTR));

                    return action.execute(manager);
                } catch (DeploymentManagerCreationException ex) {
                    throw new ExecutionException(ex);
                }
            }
        });
    }

    private synchronized DeploymentManager getDeploymentManager(String uri,
            String username, String password) throws DeploymentManagerCreationException {

        if (manager != null) {
            // this should work even if some older WL release does not have this
            // method in such case DM is created always from scratch
            try {
                Field f = manager.getClass().getDeclaredField("isConnected"); // NOI18N
                f.setAccessible(true);
                Object o = f.get(manager);
                if ((o instanceof Boolean) && ((Boolean) o).booleanValue()) {
                    //manager.getTargets();
                    return manager;
                }
            } catch (Exception ex) {
                // go through to release
            }
            manager.release();
        }

        try {
            manager = df.getDeploymentManager(uri, username, password);
            return manager;
        } catch (NoClassDefFoundError e) {
            DeploymentManagerCreationException dmce = new DeploymentManagerCreationException("Classpath is incomplete"); // NOI18N
            dmce.initCause(e);
            throw dmce;
        }
    }

    /**
     * This is a handy method to execute the any {@code action} within JBoss's class loader.
     *
     * @param action the action to be executed
     * @return T
     * @throws ExecutionException
     */
    public <T> T invokeLocalAction(final Callable<T> action) throws Exception {
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        try {
            InstanceProperties ip = getInstanceProperties();
            URLClassLoader loader = JBDeploymentFactory.getInstance().getJBClassLoader(ip);
            Thread.currentThread().setContextClassLoader(loader);
            return action.call();
        } finally {
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
    }

    public synchronized <T> T invokeRemoteAction(JBRemoteAction<T> action) throws ExecutionException {

        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        InitialContext ctx = null;
        JMXConnector conn = null;

        try {
            InstanceProperties ip = getInstanceProperties();
            URLClassLoader loader = JBDeploymentFactory.getInstance().getJBClassLoader(ip);
            Thread.currentThread().setContextClassLoader(loader);

            JBProperties props = getProperties();
            Properties env = new Properties();

            // Sets the jboss naming environment
            String jnpPort = Integer.toString(
                    JBPluginUtils.getJnpPortNumber(ip.getProperty(JBPluginProperties.PROPERTY_SERVER_DIR)));

            env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.NamingContextFactory");
            env.put(Context.PROVIDER_URL, "jnp://localhost"+ ":"  + jnpPort);
            env.put(Context.OBJECT_FACTORIES, "org.jboss.naming");
            env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces" );
            env.put("jnp.disableDiscovery", Boolean.TRUE);

            final String JAVA_SEC_AUTH_LOGIN_CONF = "java.security.auth.login.config"; // NOI18N
            String oldAuthConf = System.getProperty(JAVA_SEC_AUTH_LOGIN_CONF);

            env.put(Context.SECURITY_PRINCIPAL, props.getUsername());
            env.put(Context.SECURITY_CREDENTIALS, props.getPassword());
            env.put("jmx.remote.credentials", // NOI18N
                    new String[] {props.getUsername(), props.getPassword()});

            File securityConf = new File(props.getRootDir(), "/client/auth.conf");
            if (securityConf.exists()) {
                env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.security.jndi.LoginInitialContextFactory");
                System.setProperty(JAVA_SEC_AUTH_LOGIN_CONF, securityConf.getAbsolutePath()); // NOI18N
            }

            if (!props.isVersion(JBPluginUtils.JBOSS_7_0_0)) {
                // Gets naming context
                ctx = new InitialContext(env);
            }

            //restore java.security.auth.login.config system property
            if (oldAuthConf != null) {
                System.setProperty(JAVA_SEC_AUTH_LOGIN_CONF, oldAuthConf);
            } else {
                System.clearProperty(JAVA_SEC_AUTH_LOGIN_CONF);
            }

            MBeanServerConnection rmiServer = null;
            try {
                JMXServiceURL url;
                if (props.isVersion(JBPluginUtils.JBOSS_7_0_0)) {
                    // using management-native port
                    url = new JMXServiceURL(
                            System.getProperty("jmx.service.url", "service:jmx:remoting-jmx://localhost:9999")); // NOI18N
                } else {
                    url = new JMXServiceURL(
                            "service:jmx:rmi:///jndi/rmi://localhost:1090/jmxrmi"); // NOI18N
                }
                conn = JMXConnectorFactory.connect(url);

                rmiServer = conn.getMBeanServerConnection();
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }

            if (rmiServer == null && ctx != null) {
                // Lookup RMI Adaptor
                rmiServer = (MBeanServerConnection) ctx.lookup("/jmx/invoker/RMIAdaptor"); // NOI18N
            }

            JBoss5ProfileServiceProxy profileService = null;
            try {
                if (ctx != null) {
                    Object service = ctx.lookup("ProfileService"); // NOI18N
                    if (service != null) {
                        profileService = new JBoss5ProfileServiceProxy(service);
                    }
                }
            } catch (NameNotFoundException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }

            if (rmiServer != null) {
                return action.action(rmiServer, profileService);
            } else {
                throw new IllegalStateException("No rmi server acquired for " + realUri);
            }
        } catch (NameNotFoundException ex) {
            LOGGER.log(Level.FINE, null, ex);
            throw new ExecutionException(ex);
        } catch (NamingException ex) {
            LOGGER.log(Level.FINE, null, ex);
            throw new ExecutionException(ex);
        } catch (Exception ex) {
            LOGGER.log(Level.FINE, null, ex);
            throw new ExecutionException(ex);
        } finally {
            try {
                if (ctx != null) {
                    ctx.close();
                }
            } catch (NamingException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods for retrieving server instance state
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Returns true if the given instance properties are present in the map and value equals true.
     * Otherwise return false.
     */
    public static boolean isRunningLastCheck(InstanceProperties ip) {
        boolean isRunning = propertiesToIsRunning.containsKey(ip) && propertiesToIsRunning.get(ip).equals(Boolean.TRUE);
        return isRunning;
    }

    /**
     * Stores state of an instance represented by InstanceProperties.
     */
    public static void setRunningLastCheck(InstanceProperties ip, Boolean isRunning) {
        assert(ip != null);
        propertiesToIsRunning.put(ip, isRunning);
    }

    boolean isAs7() {
        if (as7 == null) {
            as7 = getProperties().isVersion(JBPluginUtils.JBOSS_7_0_0);
        }
        return as7;
    }
    ////////////////////////////////////////////////////////////////////////////
    // DeploymentManager Implementation
    ////////////////////////////////////////////////////////////////////////////
    public ProgressObject distribute(Target[] target, File file, File file2) throws IllegalStateException {
        if (isAs7()) {
            return new JB7Deployer(realUri, this).deploy(target, file, file2, getHost(), getPort());
        }
        return new JBDeployer(realUri, this).deploy(target, file, file2, getHost(), getPort());
    }

    public DeploymentConfiguration createConfiguration(DeployableObject deployableObject) throws InvalidModuleException {
        throw new RuntimeException("This method should never be called."); // NOI18N
    }

    public ProgressObject redeploy(final TargetModuleID[] targetModuleID,
            final InputStream inputStream, final InputStream inputStream2) throws UnsupportedOperationException, IllegalStateException {
        if (df == null) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        try {
            return executeAction(new Action<ProgressObject>() {
                @Override
                public ProgressObject execute(DeploymentManager manager) throws ExecutionException {
                    if (isAs7()) {
                        return manager.redeploy(translateForUndeploy(targetModuleID),
                                inputStream, inputStream2);
                    }
                    return manager.redeploy(targetModuleID, inputStream, inputStream2);
                }
            });
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalStateException) {
                throw (IllegalStateException) ex.getCause();
            } else {
                throw new IllegalStateException(ex.getCause());
            }
        }
    }

    public ProgressObject distribute(Target[] target, InputStream inputStream, InputStream inputStream2) throws IllegalStateException {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    public ProgressObject distribute(Target[] target, ModuleType moduleType, InputStream inputStream, InputStream inputStream0) throws IllegalStateException {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    public ProgressObject undeploy(final TargetModuleID[] targetModuleID) throws IllegalStateException {
        if (df == null) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        try {
            return executeAction(new Action<ProgressObject>() {
                @Override
                public ProgressObject execute(DeploymentManager manager) throws ExecutionException {
                    if (isAs7()) {
                        return manager.undeploy(translateForUndeploy(targetModuleID));
                    }
                    return manager.undeploy(targetModuleID);
                }
            });
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalStateException) {
                throw (IllegalStateException) ex.getCause();
            } else {
                throw new IllegalStateException(ex.getCause());
            }
        }
    }

    public ProgressObject stop(final TargetModuleID[] targetModuleID) throws IllegalStateException {
        if (df == null) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        try {
            return executeAction(new Action<ProgressObject>() {
                @Override
                public ProgressObject execute(DeploymentManager manager) throws ExecutionException {
                    return manager.stop(targetModuleID);
                }
            });
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalStateException) {
                throw (IllegalStateException) ex.getCause();
            } else {
                throw new IllegalStateException(ex.getCause());
            }
        }
    }

    public ProgressObject start(final TargetModuleID[] targetModuleID) throws IllegalStateException {
        if (df == null) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        try {
            return executeAction(new Action<ProgressObject>() {
                @Override
                public ProgressObject execute(DeploymentManager manager) throws ExecutionException {
                    if (isAs7()) {
                        return manager.start(unwrap(targetModuleID));
                    }
                    return manager.start(targetModuleID);
                }
            });
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalStateException) {
                throw (IllegalStateException) ex.getCause();
            } else {
                throw new IllegalStateException(ex.getCause());
            }
        }
    }

    public TargetModuleID[] getAvailableModules(final ModuleType moduleType, final Target[] target) throws TargetException, IllegalStateException {
        if (isAs7()) {
            if (df == null) {
                throw new IllegalStateException("Deployment manager is disconnected");
            }
            try {
                return executeAction(new Action<TargetModuleID[]>() {
                    @Override
                    public TargetModuleID[] execute(DeploymentManager manager) throws ExecutionException {
                        try {
                            TargetModuleID[] ids = manager.getAvailableModules(moduleType, target);
                            if (ids != null) {
                                return ids;
                            } else {
                                return new TargetModuleID[]{};
                            }
                        } catch (TargetException ex) {
                            throw new ExecutionException(ex);
                        } catch (IllegalStateException ex) {
                            throw new ExecutionException(ex);
                        }

                    }
                });
            } catch (Exception ex) {
                if (ex.getCause() instanceof TargetException) {
                    throw (TargetException) ex.getCause();
                } else if (ex.getCause() instanceof IllegalStateException) {
                    throw (IllegalStateException) ex.getCause();
                } else {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
        }
        return new TargetModuleID[]{};
    }

    public TargetModuleID[] getNonRunningModules(final ModuleType moduleType, final Target[] target) throws TargetException, IllegalStateException {
        if (isAs7()) {
            if (df == null) {
                throw new IllegalStateException("Deployment manager is disconnected");
            }
            try {
                return executeAction(new Action<TargetModuleID[]>() {
                    @Override
                    public TargetModuleID[] execute(DeploymentManager manager) throws ExecutionException {
                        try {
                            TargetModuleID[] ids = manager.getNonRunningModules(moduleType, target);
                            if (ids != null) {
                                return ids;
                            } else {
                                return new TargetModuleID[]{};
                            }
                        } catch (TargetException ex) {
                            throw new ExecutionException(ex);
                        } catch (IllegalStateException ex) {
                            throw new ExecutionException(ex);
                        }

                    }
                });
            } catch (Exception ex) {
                if (ex.getCause() instanceof TargetException) {
                    throw (TargetException) ex.getCause();
                } else if (ex.getCause() instanceof IllegalStateException) {
                    throw (IllegalStateException) ex.getCause();
                } else {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
        }
        return new TargetModuleID[]{};
    }

    public TargetModuleID[] getRunningModules(final ModuleType moduleType, final Target[] target) throws TargetException, IllegalStateException {
        if (df == null) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        try {
            return executeAction(new Action<TargetModuleID[]>() {
                @Override
                public TargetModuleID[] execute(DeploymentManager manager) throws ExecutionException {
                    try {
                        TargetModuleID[] ids = manager.getRunningModules(moduleType, target);
                        if (ids != null) {
                            return ids;
                        } else {
                            return new TargetModuleID[]{};
                        }
                    } catch (TargetException ex) {
                        throw new ExecutionException(ex);
                    } catch (IllegalStateException ex) {
                        throw new ExecutionException(ex);
                    }

                }
            });
        } catch (Exception ex) {
            if (ex.getCause() instanceof TargetException) {
                throw (TargetException) ex.getCause();
            } else if (ex.getCause() instanceof IllegalStateException) {
                throw (IllegalStateException) ex.getCause();
            } else {
                LOGGER.log(Level.INFO, null, ex);
            }
        }
        return new TargetModuleID[]{};
    }

    public ProgressObject redeploy(TargetModuleID[] targetModuleID, File file, File file2) throws UnsupportedOperationException, IllegalStateException {
        if (isAs7()) {
            return new JB7Deployer(realUri, this).redeploy(targetModuleID, file, file2);
        }
        return new JBDeployer(realUri, this).redeploy(targetModuleID, file, file2);
    }

    public void release() {
        // noop as manager is cached and reused
    }

    public boolean isRedeploySupported() {
        try {
            return executeAction(new Action<Boolean>() {
                @Override
                public Boolean execute(DeploymentManager manager) throws ExecutionException {
                    try {
                        return manager.isRedeploySupported();
                    } catch (IllegalStateException ex) {
                        throw new ExecutionException(ex);
                    }

                }
            });
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, null, ex);
            return false;
        }
    }

    public void setDConfigBeanVersion(DConfigBeanVersionType dConfigBeanVersionType) throws DConfigBeanVersionUnsupportedException {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType dConfigBeanVersionType) {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    public void setLocale(Locale locale) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("This method should never be called!"); // NOI18N
    }

    public boolean isLocaleSupported(Locale locale) {
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

    public Target[] getTargets() throws IllegalStateException {
        if (df == null) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }
        try {
            return executeAction(new Action<Target[]>() {
                @Override
                public Target[] execute(DeploymentManager manager) throws ExecutionException {
                    try {
                        return manager.getTargets();
//                        Target[] server = manager.getTargets();
//                        if (server == null) {
//                            server = new Target[]{};
//                        }
//                        synchronized (JBDeploymentManager.this) {
//                            boolean changed = false;
//                            if (server.length != targets.size()) {
//                                changed = true;
//                            } else {
//                                for (Target t : server) {
//                                    if (!targets.containsKey(t.getName())) {
//                                        changed = true;
//                                        break;
//                                    }
//                                }
//                            }
//                            if (changed) {
//                                closeTargets(targets.values());
//                                targets.clear();
//                                for (Target t : server) {
//                                    targets.put(t.getName(), t);
//                                }
//                            }
//                            return targets.values().toArray(new Target[targets.size()]);
//                        }
                    } catch (IllegalStateException ex) {
                        throw new ExecutionException(ex);
                    }

                }
            });
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalStateException) {
                throw (IllegalStateException) ex.getCause();
            } else {
                LOGGER.log(Level.INFO, null, ex);
            }
        }
        return new Target[]{};
    }

    public JBJ2eePlatformFactory.J2eePlatformImplImpl getJBPlatform() {
        if (jbPlatform == null) {
            jbPlatform = (JBJ2eePlatformFactory.J2eePlatformImplImpl) new JBJ2eePlatformFactory().getJ2eePlatformImpl(this);
        }
        return jbPlatform;
    }

    public JBProperties getProperties() {
        return new JBProperties(this);
    }

    /**
     * Mark the server with a needs restart flag. This may be needed
     * for instance when JDBC driver is deployed to a running server.
     */
    public synchronized void setNeedsRestart(boolean needsRestart) {
        this.needsRestart = needsRestart;
    }

    /**
     * Returns true if the server needs to be restarted. This may occur
     * for instance when JDBC driver was deployed to a running server
     */
    public synchronized boolean getNeedsRestart() {
        return needsRestart;
    }

    // we are doing this because undeployment/redeployment as implemented
    // in current AS7 expect URL as module ID, though the server seturned IDs
    // are just filenames
    private TargetModuleID[] translateForUndeploy(TargetModuleID[] ids) {
        final String deployDir = getInstanceProperties().getProperty(JBPluginProperties.PROPERTY_DEPLOY_DIR);
        if (deployDir != null) {
            TargetModuleID[] ret = new TargetModuleID[ids.length];
            for (int i = 0; i < ids.length; i++) {
                File testFile = new File(deployDir, ids[i].getModuleID());
                if (testFile.exists()) {
                    // XXX is this needed ?
                    File markFile = new File(deployDir, ids[i].getModuleID() + ".deployed"); // NOI18N
                    if (markFile.isFile()) {
                        try {
                            ret[i] = new WrappedTargetModuleID(ids[i], null, testFile.toURI().toURL().toString(), null);
                            continue;
                        } catch (MalformedURLException ex) {
                            LOGGER.log(Level.FINE, null, ex);
                        }
                    }
                }
                ret[i] = ids[i];
            }
            return ret;
        }
        return ids;
    }
    
    private static TargetModuleID[] unwrap(TargetModuleID[] ids) {
        if (ids == null || ids.length == 0) {
            return ids;
        }
        TargetModuleID[] ret = new TargetModuleID[ids.length];
        for (int i = 0; i < ids.length; i++) {
            if (ids[i] instanceof WrappedTargetModuleID) {
                ret[i] = ((WrappedTargetModuleID) ids[i]).getOriginal();
            } else {
                ret[i] = ids[i];
            }
        }
        return ret;
    }

    private static void closeTargets(Iterable<Target> targets) {
        if (targets == null) {
            return;
        }
        for (Target target : targets) {
            try {
                Field f = target.getClass().getDeclaredField("modelControllerClient"); // NOI18N
                f.setAccessible(true);
                Object value = f.get(target);
                if (value != null) {
                    Method m = value.getClass().getDeclaredMethod("close", null); // NOI18N
                    m.invoke(value, null);
                }
            } catch (Exception e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }
    }

    private static interface Action<T> {

         T execute(DeploymentManager manager) throws ExecutionException;
    }
}
