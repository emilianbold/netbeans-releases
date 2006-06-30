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
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.jboss4.config.WarDeploymentConfiguration;
import org.netbeans.modules.j2ee.jboss4.ide.JBJ2eePlatformFactory;
import org.netbeans.modules.j2ee.jboss4.ide.JBLogWriter;
import java.io.File;
import java.io.InputStream;
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
import org.netbeans.modules.j2ee.jboss4.config.EarDeploymentConfiguration;
import org.netbeans.modules.j2ee.jboss4.config.EjbDeploymentConfiguration;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;

/**
 *
 * @author Kirill Sorokin
 */
public class JBDeploymentManager implements DeploymentManager {
    
    private DeploymentManager dm;
    private String realUri;

    private int debuggingPort = 8787;
    
    // ide specific data
    private JBLogWriter logWriter;
    
    private InstanceProperties instanceProperties;
    
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
    
    ////////////////////////////////////////////////////////////////////////////
    // IDE data methods
    ////////////////////////////////////////////////////////////////////////////
    public JBLogWriter getLogWriter() {
        return this.logWriter;
    }
    
    public void setLogWriter(JBLogWriter logWriter) {
        this.logWriter = logWriter;
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
        return new JBDeployer(realUri).deploy(target, file, file2, getHost(), getPort());
    }
    
    public DeploymentConfiguration createConfiguration(DeployableObject deployableObject) throws InvalidModuleException {
        ModuleType type = deployableObject.getType();
        if (type == ModuleType.WAR) {
            return new WarDeploymentConfiguration(deployableObject);
        } else if (type == ModuleType.EAR) {
            return new EarDeploymentConfiguration(deployableObject);
        } else if (type == ModuleType.EJB) {
            return new EjbDeploymentConfiguration(deployableObject);
        } else {
            throw new InvalidModuleException("Unsupported module type: " + type.toString()); // NOI18N
        }
    }
    
    public ProgressObject redeploy(TargetModuleID[] targetModuleID, InputStream inputStream, InputStream inputStream2) throws UnsupportedOperationException, IllegalStateException {
        return dm.redeploy(targetModuleID, inputStream, inputStream2);
    }
    
    public ProgressObject distribute(Target[] target, InputStream inputStream, InputStream inputStream2) throws IllegalStateException {
        return dm.distribute(target, inputStream, inputStream2);
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
        return new JBDeployer(realUri).redeploy(targetModuleID, file, file2);
    }
    
    public void setDConfigBeanVersion(DConfigBeanVersionType dConfigBeanVersionType) throws DConfigBeanVersionUnsupportedException {
        dm.setDConfigBeanVersion(dConfigBeanVersionType);
    }
    
    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType dConfigBeanVersionType) {
        return dm.isDConfigBeanVersionSupported(dConfigBeanVersionType);
    }
    
    public void release() {
        dm.release();
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
    
    public JBJ2eePlatformFactory.J2eePlatformImplImpl getJBPlatform () {
        if (jbPlatform == null) {
            jbPlatform = new JBJ2eePlatformFactory.J2eePlatformImplImpl(this);
        }
        return jbPlatform;
    }
}
