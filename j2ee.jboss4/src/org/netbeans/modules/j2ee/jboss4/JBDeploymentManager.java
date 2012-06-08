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
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.Properties;
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
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
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
    
    private final DeploymentManager dm;
    private final String realUri;

    private int debuggingPort = 8787;

    private InstanceProperties instanceProperties;
    private boolean needsRestart;

    /**
     * Stores information about running instances. instance is represented by its InstanceProperties,
     *  running state by Boolean.TRUE, stopped state Boolean.FALSE.
     * WeakHashMap should guarantee erasing of an unregistered server instance bcs instance properties are also removed along with instance.
     */
    private static final Map<InstanceProperties, Boolean> propertiesToIsRunning = Collections.synchronizedMap(new WeakHashMap());
    
    /** Creates a new instance of JBDeploymentManager */
    public JBDeploymentManager(DeploymentManager dm, String uri, String username, String password) {
        realUri = uri;
        this.dm = dm;
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


    public InstanceProperties getInstanceProperties() {
        if (instanceProperties == null)
            instanceProperties = InstanceProperties.getInstanceProperties(realUri);

        return instanceProperties;
    }

    public <T> T invokeRemoteAction(JBRemoteAction<T> action) throws ExecutionException {

        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        InitialContext ctx = null;
        JMXConnector conn = null;

        try {
            InstanceProperties ip = getInstanceProperties();
            URLClassLoader loader = JBDeploymentFactory.getJBClassLoader(ip);
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

            // Gets naming context
            ctx = new InitialContext(env);

            //restore java.security.auth.login.config system property
            if (oldAuthConf != null) {
                System.setProperty(JAVA_SEC_AUTH_LOGIN_CONF, oldAuthConf);
            } else {
                System.clearProperty(JAVA_SEC_AUTH_LOGIN_CONF);
            }

            MBeanServerConnection rmiServer = null;
            try {
                conn = JMXConnectorFactory.connect(new JMXServiceURL(
                        "service:jmx:rmi:///jndi/rmi://localhost:1090/jmxrmi"));

                rmiServer = conn.getMBeanServerConnection();
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }

            if (rmiServer == null) {
                // Lookup RMI Adaptor
                rmiServer = (MBeanServerConnection) ctx.lookup("/jmx/invoker/RMIAdaptor"); // NOI18N
            }

            JBoss5ProfileServiceProxy profileService = null;
            try {
                Object service = ctx.lookup("ProfileService"); // NOI18N
                if (service != null) {
                    profileService = new JBoss5ProfileServiceProxy(service);
                }
            } catch (NameNotFoundException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }

            return action.action(rmiServer, profileService);
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

    ////////////////////////////////////////////////////////////////////////////
    // DeploymentManager Implementation
    ////////////////////////////////////////////////////////////////////////////
    public ProgressObject distribute(Target[] target, File file, File file2) throws IllegalStateException {
        return new JBDeployer(realUri, this).deploy(target, file, file2, getHost(), getPort());
    }

    public DeploymentConfiguration createConfiguration(DeployableObject deployableObject) throws InvalidModuleException {
        throw new RuntimeException("This method should never be called."); // NOI18N
    }

    public ProgressObject redeploy(TargetModuleID[] targetModuleID, InputStream inputStream, InputStream inputStream2) throws UnsupportedOperationException, IllegalStateException {
        return dm.redeploy(targetModuleID, inputStream, inputStream2);
    }

    public ProgressObject distribute(Target[] target, InputStream inputStream, InputStream inputStream2) throws IllegalStateException {
        return dm.distribute(target, inputStream, inputStream2);
    }

    public ProgressObject distribute(Target[] target, ModuleType moduleType, InputStream inputStream, InputStream inputStream0) throws IllegalStateException {
        return distribute(target, inputStream, inputStream0);
    }

    public ProgressObject undeploy(TargetModuleID[] targetModuleID) throws IllegalStateException {
        return dm.undeploy(targetModuleID);
    }

    public ProgressObject stop(TargetModuleID[] targetModuleID) throws IllegalStateException {
        return dm.stop(targetModuleID);
    }

    public ProgressObject start(TargetModuleID[] targetModuleID) throws IllegalStateException {
        return dm.start(targetModuleID);
    }

    public void setLocale(Locale locale) throws UnsupportedOperationException {
        dm.setLocale(locale);
    }

    public boolean isLocaleSupported(Locale locale) {
        return dm.isLocaleSupported(locale);
    }

    public TargetModuleID[] getAvailableModules(ModuleType moduleType, Target[] target) throws TargetException, IllegalStateException {
        //return dm.getAvailableModules(moduleType, target);
        return new TargetModuleID[]{};
    }

    public TargetModuleID[] getNonRunningModules(ModuleType moduleType, Target[] target) throws TargetException, IllegalStateException {
        //return dm.getNonRunningModules(moduleType, target);
        return new TargetModuleID[]{};
    }

    public TargetModuleID[] getRunningModules(ModuleType moduleType, Target[] target) throws TargetException, IllegalStateException {
        return dm.getRunningModules(moduleType, target);
    }

    public ProgressObject redeploy(TargetModuleID[] targetModuleID, File file, File file2) throws UnsupportedOperationException, IllegalStateException {
        return new JBDeployer(realUri, this).redeploy(targetModuleID, file, file2);
    }

    public void setDConfigBeanVersion(DConfigBeanVersionType dConfigBeanVersionType) throws DConfigBeanVersionUnsupportedException {
        dm.setDConfigBeanVersion(dConfigBeanVersionType);
    }

    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType dConfigBeanVersionType) {
        return dm.isDConfigBeanVersionSupported(dConfigBeanVersionType);
    }

    public void release() {
        if (dm != null) {
            dm.release();
        }
    }

    public boolean isRedeploySupported() {
        return dm.isRedeploySupported();
    }

    public Locale getCurrentLocale() {
        return dm.getCurrentLocale();
    }

    public DConfigBeanVersionType getDConfigBeanVersion() {
        return dm.getDConfigBeanVersion();
    }

    public Locale getDefaultLocale() {
        return dm.getDefaultLocale();
    }

    public Locale[] getSupportedLocales() {
        return dm.getSupportedLocales();
    }

    public Target[] getTargets() throws IllegalStateException {
        return dm.getTargets();
    }

    private JBJ2eePlatformFactory.J2eePlatformImplImpl jbPlatform;

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
}
