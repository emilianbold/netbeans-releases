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
import java.util.Hashtable;
import java.util.Locale;
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
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
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

    private DeploymentManager dm;
    private String realUri;
    private MBeanServerConnection rmiServer;

    private int debuggingPort = 8787;

    private InstanceProperties instanceProperties;
    private boolean needsRestart;

    /**
     * Stores information about running instances. instance is represented by its InstanceProperties,
     *  running state by Boolean.TRUE, stopped state Boolean.FALSE.
     * WeakHashMap should guarantee erasing of an unregistered server instance bcs instance properties are also removed along with instance.
     */
    private static Map/*<InstanceProperties, Boolean>*/ propertiesToIsRunning = Collections.synchronizedMap(new WeakHashMap());

    /** Creates a new instance of JBDeploymentManager */
    public JBDeploymentManager(DeploymentManager dm, String uri, String username, String password) {
        realUri = uri;
        this.dm = dm;
        rmiServer = null;
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

    public synchronized MBeanServerConnection getRMIServer() {
        if(rmiServer == null) {
            ClassLoader oldLoader = null;

            try {
                oldLoader = Thread.currentThread().getContextClassLoader();
                InstanceProperties ip = this.getInstanceProperties();
                URLClassLoader loader = JBDeploymentFactory.getJBClassLoader(ip.getProperty(JBPluginProperties.PROPERTY_ROOT_DIR),
                        ip.getProperty(JBPluginProperties.PROPERTY_SERVER_DIR));
                Thread.currentThread().setContextClassLoader(loader);

                JBProperties props = getProperties();
                Hashtable env = new Hashtable();

                // Sets the jboss naming environment
                env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.NamingContextFactory");
                env.put(Context.PROVIDER_URL, "jnp://localhost:"+JBPluginUtils.getJnpPort(ip.getProperty(JBPluginProperties.PROPERTY_SERVER_DIR)));
                env.put(Context.OBJECT_FACTORIES, "org.jboss.naming");
                env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces" );
                env.put("jnp.disableDiscovery", Boolean.TRUE);

                final String JAVA_SEC_AUTH_LOGIN_CONF = "java.security.auth.login.config"; // NOI18N
                String oldAuthConf = System.getProperty(JAVA_SEC_AUTH_LOGIN_CONF);

                File securityConf = new File(props.getRootDir(), "/client/auth.conf");
                if (securityConf.exists()) {
                    env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.security.jndi.LoginInitialContextFactory");
                    env.put(Context.SECURITY_PRINCIPAL, props.getUsername());
                    env.put(Context.SECURITY_CREDENTIALS, props.getPassword());
                    System.setProperty(JAVA_SEC_AUTH_LOGIN_CONF, securityConf.getAbsolutePath()); // NOI18N
                }

                // Gets naming context
                InitialContext ctx = new InitialContext(env);

                //restore java.security.auth.login.config system property
                if (oldAuthConf != null) {
                    System.setProperty(JAVA_SEC_AUTH_LOGIN_CONF, oldAuthConf);
                } else {
                    System.clearProperty(JAVA_SEC_AUTH_LOGIN_CONF);
                }

                // Lookup RMI Adaptor
                rmiServer = (MBeanServerConnection)ctx.lookup("/jmx/invoker/RMIAdaptor");
            } catch (NameNotFoundException ex) {
            } catch (NamingException ex) {
                // Nothing to do
            } finally {
                if (oldLoader != null)
                    Thread.currentThread().setContextClassLoader(oldLoader);
            }
        }

        return rmiServer;
    }

    public Object invokeMBeanOperation(ObjectName name, String method, Object[] params, String[] signature)
            throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {

        MBeanServerConnection conn = null;
        synchronized (this) {
            conn = getRMIServer();
        }

        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(conn.getClass().getClassLoader());
            return conn.invoke(name, method, params, signature);
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }

    }

    public synchronized MBeanServerConnection refreshRMIServer() {
        rmiServer = null;
        return getRMIServer();
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
