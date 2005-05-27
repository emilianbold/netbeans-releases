    /*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.websphere6;

import java.io.*;
import java.util.*;

import javax.enterprise.deploy.model.*;
import javax.enterprise.deploy.shared.*;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.exceptions.*;
import javax.enterprise.deploy.spi.factories.*;
import javax.enterprise.deploy.spi.status.*;

import org.openide.*;
import org.openide.util.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;

import org.netbeans.modules.j2ee.websphere6.util.WSDebug;

/**
 * Main class of the deployment process. This serves a a wrapper for the 
 * server's DeploymentManager implementation, all calls are delegated to the
 * server's implementation, with the thread's context classloader updated
 * if necessary.
 * 
 * @author Kirill Sorokin
 */
public class WSDeploymentManager implements DeploymentManager {
    
    /**
     * Current classloader used to work with WS classes
     */
    private WSClassLoader loader;
    
    /**
     * Server's DeploymentFactory implementation
     */
    private DeploymentFactory factory;
    
    /**
     * Server's DeploymentManager implementation
     */
    private DeploymentManager dm;
    
    /**
     * Current server instance's properties
     */
    private InstanceProperties instanceProperties;
    
    /**
     * Connection properties - URI
     */
    private String uri;
    
    /**
     * Connection properties - user name
     */
    private String username;
    
    /**
     * Connection properties - password
     */
    private String password;
    
    /**
     * Marker that indicated whether the server is connected
     */
    private boolean isConnected;
    
    /**
     * Creates a new instance of the deployment manager
     * 
     * @param uri the server's URI
     * @param username username for connecting to the server
     * @param password password for connecting to the server
     */
    public WSDeploymentManager(String uri, String username, String password) {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("WSDeploymentManager(" + uri + ", " +       // NOI18N
                    username + ", " + password + ")");                 // NOI18N
        
        // save the connection properties
        this.uri = uri;
        this.username = username;
        this.password = password;
    }
    
    /**
     * Creates a new instance of the deployment manager
     * 
     * @param uri the server's URI
     */
    public WSDeploymentManager(String uri) {
        this(uri, null, null);
    }
    
    /**
     * Parses the URI and stores the parsed URI in the instance properties 
     * object
     */
    private void parseUri() {
        // split the uri
        String[] parts = uri.split(":"); // NOI18N
        
        // set the host and port properties
        getInstanceProperties().setProperty(
                WSDeploymentFactory.HOST_ATTR, parts[2]);
        getInstanceProperties().setProperty(
                WSDeploymentFactory.PORT_ATTR, parts[3]);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Connection data methods
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Returns the stored server URI
     */
    public String getURI() {
        return this.uri;
    }
    
    /**
     * Returns the server host stored in the instance properties
     */
    public String getHost() {
        return getInstanceProperties().getProperty(
                WSDeploymentFactory.HOST_ATTR);
    }
    
    /**
     * Returns the server port stored in the instance properties
     */
    public String getPort() {
        return getInstanceProperties().getProperty(
                WSDeploymentFactory.PORT_ATTR);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Class loading related things
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Loads the server's deployment factory if it's not already loaded. During 
     * this process the classloader for WS classes is initialized.
     */
    private void loadDeploymentFactory() {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("loadDeploymentFactory()");                 // NOI18N
        
        // if the factory is not loaded - load it
        if (factory == null) {
            // init the classloader
            loader = WSClassLoader.getInstance(getInstanceProperties().
                    getProperty(WSDeploymentFactory.SERVER_ROOT_ATTR), 
                    getInstanceProperties().getProperty(
                    WSDeploymentFactory.DOMAIN_ROOT_ATTR));
            
            // update the context classloader
            loader.updateLoader();
            
            // load the factory class and instantiate it
            try {
                factory = (DeploymentFactory) loader.loadClass(
                        "com.ibm.ws.management.application.j2ee." +    // NOI18N
                        "deploy.spi.factories.DeploymentFactoryImpl"). // NOI18N
                        newInstance();
            } catch (ClassNotFoundException e) {
                ErrorManager.getDefault().notify(ErrorManager.ERROR, e);
            } catch (InstantiationException e) {
                ErrorManager.getDefault().notify(ErrorManager.ERROR, e);
            } catch (IllegalAccessException e) {
                ErrorManager.getDefault().notify(ErrorManager.ERROR, e);
            } finally {
                // restore the loader
                loader.restoreLoader();
            }
        }
    }
    
    /**
     * Updates the stored deployment manager. This is used when the current 
     * deployment manager cannot be used due to any reason, for example
     * it is disconnected, its deployment application is already defined, etc
     */
    private void updateDeploymentManager() {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("updateDeploymentManager()");               // NOI18N
        
        // load the deployment factory
        loadDeploymentFactory();
        
        // update the context classloader
        loader.updateLoader();
        
        try {
            // if the current deployment manager is not null - flush the 
            // resources it has registered
            if (dm != null) {
                dm.release();
            }
            
            // try to get a connected deployment manager
            dm = factory.getDeploymentManager(uri, username, password);
            
            // set the connected marker
            isConnected = true;
        } catch (DeploymentManagerCreationException e) {
            try {
                // if the connected deployment manager cannot be obtained - get
                // a disconnected one and set the connected marker to false
                isConnected = false;
                dm = factory.getDisconnectedDeploymentManager(uri);
            } catch (DeploymentManagerCreationException ex) {
                ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
            }
        } finally {
            // restore the context classloader
            loader.restoreLoader();
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // IDE data methods
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Returns the InstanceProperties object for the current server instance
     */
    public InstanceProperties getInstanceProperties() {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("getInstanceProperties()");                 // NOI18N
        
        // if the stored instance properties are null - get them via the 
        // InstanceProperties' static method
        if (instanceProperties == null) {
            instanceProperties = InstanceProperties.getInstanceProperties(uri);
            
            // if the instance properties were obtained successfully - parse 
            // the URI and store the host and port in the instance properties
            if (instanceProperties != null) {
                parseUri();
            }
        }
        
        // return either the stored or the newly obtained instance properties
        return instanceProperties;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // DeploymentManager Implementation
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public ProgressObject distribute(Target[] target, File file, File file2) 
            throws IllegalStateException {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("distribute(" + target + ", " + file +      // NOI18N
                    ", " + file2 + ")");                               // NOI18N
        
        // update the deployment manager
        updateDeploymentManager();
        
        // if the manager is not connected - throw an IllegalStateException
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(
                    WSDeploymentManager.class, "ERR_illegalState"));   // NOI18N
        }
        
        // update the context classloader
        loader.updateLoader();
        
        try {
            // delegate the call and return the result
            return dm.distribute(target, file, file2);
        } finally {
            // restore the context classloader
            loader.restoreLoader();
        }
    }
    
    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     * 
     * @return a wrapper for the server's DeploymentConfiguration implementation
     */
    public DeploymentConfiguration createConfiguration(
            DeployableObject deployableObject) throws InvalidModuleException {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("createConfiguration(" +                    // NOI18N
                    deployableObject + ")");                           // NOI18N
        
        // update the deployment manager
        updateDeploymentManager();
        
        // update the context classloader
        loader.updateLoader();
        try {
            // return the wrapper deployment configuration
            return new WSDeploymentConfiguration(dm, deployableObject, 
                    getInstanceProperties());
        } finally {
            // restore the context classloader
            loader.restoreLoader();
        }
    }
    
    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public ProgressObject redeploy(TargetModuleID[] targetModuleID, 
            InputStream inputStream, InputStream inputStream2) 
            throws UnsupportedOperationException, IllegalStateException {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("redeploy(" + targetModuleID + ", " +       // NOI18N
                    inputStream + ", " + inputStream2 + ")");          // NOI18N
        
        // update the deployment manager
        updateDeploymentManager();
        
        // if the manager is not connected - throw an IllegalStateException
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(
                    WSDeploymentManager.class, "ERR_illegalState"));   // NOI18N
        }
        
        // update the context classloader
        loader.updateLoader();
        
        try {
            // delegate the call and return the result
            return dm.redeploy(targetModuleID, inputStream, inputStream2);
        } finally {
            // restore the context classloader
            loader.restoreLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public ProgressObject distribute(Target[] target, InputStream inputStream, 
            InputStream inputStream2) throws IllegalStateException {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("distribute(" + target + ", " +             // NOI18N
                    inputStream + ", " + inputStream2 + ")");          // NOI18N
        
        // update the deployment manager
        updateDeploymentManager();
        
        // if the manager is not connected - throw an IllegalStateException
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(
                    WSDeploymentManager.class, "ERR_illegalState"));   // NOI18N
        }
        
        // update the context classloader
        loader.updateLoader();
        
        try {
            // delegate the call and return the result
            return dm.distribute(target, inputStream, inputStream2);
        } finally {
            // restore the context classloader
            loader.restoreLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public ProgressObject undeploy(TargetModuleID[] targetModuleID) 
            throws IllegalStateException {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("undeploy(" + targetModuleID + ")");        // NOI18N
        
        // update the deployment manager
        updateDeploymentManager();
        
        // if the manager is not connected - throw an IllegalStateException
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(
                    WSDeploymentManager.class, "ERR_illegalState"));   // NOI18N
        }
        
        // update the context classloader
        loader.updateLoader();
        
        try {
            // delegate the call and return the result
            return dm.undeploy(targetModuleID);
        } finally {
            // restore the context classloader
            loader.restoreLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public ProgressObject stop(TargetModuleID[] targetModuleID) 
            throws IllegalStateException {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("stop(" + targetModuleID + ")");            // NOI18N
                
        // update the deployment manager
        updateDeploymentManager();
        
        // if the manager is not connected - throw an IllegalStateException
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(
                    WSDeploymentManager.class, "ERR_illegalState"));   // NOI18N
        }
        
        // update the context classloader
        loader.updateLoader();
        
        try {
            // delegate the call and return the result
            return dm.stop(targetModuleID);
        } finally {
            // restore the context classloader
            loader.restoreLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public ProgressObject start(TargetModuleID[] targetModuleID) 
            throws IllegalStateException {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("start(" + targetModuleID + ")");           // NOI18N
        
        // update the deployment manager
        updateDeploymentManager();
        
        // if the manager is not connected - throw an IllegalStateException
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(
                    WSDeploymentManager.class, "ERR_illegalState"));   // NOI18N
        }
        
        // update the context classloader
        loader.updateLoader();
        
        try {
            // delegate the call and return the result
            return dm.start(targetModuleID);
        } finally {
            // restore the context classloader
            loader.restoreLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public TargetModuleID[] getAvailableModules(ModuleType moduleType, 
            Target[] target) throws TargetException, IllegalStateException {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("getAvailableModules(" + moduleType +       // NOI18N
                    ", " + target + ")");                              // NOI18N
        
        // update the deployment manager
        updateDeploymentManager();
        
        // if the manager is not connected - throw an IllegalStateException
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(
                    WSDeploymentManager.class, "ERR_illegalState"));   // NOI18N
        }
        
        // update the context classloader
        loader.updateLoader();
        
        try {
            // delegate the call and return the result
            return dm.getAvailableModules(moduleType, target);
        } finally {
            // restore the context classloader
            loader.restoreLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public TargetModuleID[] getNonRunningModules(ModuleType moduleType, 
            Target[] target) throws TargetException, IllegalStateException {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("getNonRunningModules(" + moduleType +      // NOI18N
                    ", " + target + ")");                              // NOI18N
        
        // update the deployment manager
        updateDeploymentManager();
        
        // if the manager is not connected - throw an IllegalStateException
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(
                    WSDeploymentManager.class, "ERR_illegalState"));   // NOI18N
        }
        
        // update the context classloader
        loader.updateLoader();
        
        try {
            // delegate the call and return the result
            return dm.getNonRunningModules(moduleType, target);
        } finally {
            // restore the context classloader
            loader.restoreLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public TargetModuleID[] getRunningModules(ModuleType moduleType, 
            Target[] target) throws TargetException, IllegalStateException {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("getRunningModules(" + moduleType +         // NOI18N
                    ", " + target + ")");                              // NOI18N
        
        // update the deployment manager
        updateDeploymentManager();
        
        // if the manager is not connected - throw an IllegalStateException
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(
                    WSDeploymentManager.class, "ERR_illegalState"));   // NOI18N
        }
        
        // update the context classloader
        loader.updateLoader();
        
        try {
            // delegate the call and return the result
            return dm.getRunningModules(moduleType, target);
        } finally {
            // restore the context classloader
            loader.restoreLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public ProgressObject redeploy(TargetModuleID[] targetModuleID, File file, 
            File file2) throws UnsupportedOperationException, 
            IllegalStateException {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("redeploy(" + targetModuleID + ", " +       // NOI18N
                    file + ", " + file2 + ")");                        // NOI18N
        
        // update the deployment manager
        updateDeploymentManager();
        
        // if the manager is not connected - throw an IllegalStateException
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(
                    WSDeploymentManager.class, "ERR_illegalState"));   // NOI18N
        }
        
        // update the context classloader
        loader.updateLoader();
        
        try {
            // delegate the call and return the result
            return dm.redeploy(targetModuleID, file, file2);
        } finally {
            // restore the context classloader
            loader.restoreLoader();
        }
    }
    
    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public void setLocale(Locale locale) throws UnsupportedOperationException {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("setLocale(" + locale + ")");               // NOI18N
        
        // update the deployment manager
        updateDeploymentManager();
        
        // delegate the call
        dm.setLocale(locale);
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public boolean isLocaleSupported(Locale locale) {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("isLocaleSupported(" + locale + ")");       // NOI18N
        
        // update the deployment manager
        updateDeploymentManager();
        
        // delegate the call
        return dm.isLocaleSupported(locale);
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public void setDConfigBeanVersion(
            DConfigBeanVersionType dConfigBeanVersionType) 
            throws DConfigBeanVersionUnsupportedException {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("setDConfigBeanVersion(" +                  // NOI18N
                    dConfigBeanVersionType + ")");                     // NOI18N
        
        // update the deployment manager
        updateDeploymentManager();
        
        // delegate the call
        dm.setDConfigBeanVersion(dConfigBeanVersionType);
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public boolean isDConfigBeanVersionSupported(
            DConfigBeanVersionType dConfigBeanVersionType) {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("isDConfigBeanVersionSupported(" +          // NOI18N
                    dConfigBeanVersionType + ")");                     // NOI18N
        
        // update the deployment manager
        updateDeploymentManager();
        
        // delegate the call and return the result
        return dm.isDConfigBeanVersionSupported(dConfigBeanVersionType);
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public void release() {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("release()");                               // NOI18N
        
        if (dm != null) {
            // delegate the call and clear the stored deployment manager
            dm.release();
            dm = null;
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public boolean isRedeploySupported() {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("isRedeploySupported()");                   // NOI18N
        
        // update the deployment manager
        updateDeploymentManager();
        
        // delegate the call and return the result
        return dm.isRedeploySupported();
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public Locale getCurrentLocale() {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("getCurrentLocale()");                      // NOI18N
        
        // update the deployment manager
        updateDeploymentManager();
        
        // delegate the call and return the result
        return dm.getCurrentLocale();
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public DConfigBeanVersionType getDConfigBeanVersion() {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("getDConfigBeanVersion()");                 // NOI18N
        
        // update the deployment manager
        updateDeploymentManager();
        
        // delegate the call and return the result
        return dm.getDConfigBeanVersion();
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public Locale getDefaultLocale() {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("getDefaultLocale()");                      // NOI18N
        
        // update the deployment manager
        updateDeploymentManager();
        
        // delegate the call and return the result
        return dm.getDefaultLocale();
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public Locale[] getSupportedLocales() {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("getSupportedLocales()");                   // NOI18N
        
        // update the deployment manager
        updateDeploymentManager();
        
        // delegate the call and return the result
        return dm.getSupportedLocales();
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public Target[] getTargets() throws IllegalStateException {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("getTargets()");                            // NOI18N
        
        // update the deployment manager
        updateDeploymentManager();
        
        // update the context classloader
        loader.updateLoader();
        
        // if the manager is not connected - throw an IllegalStateException
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(
                    WSDeploymentManager.class, "ERR_illegalState"));   // NOI18N
        }
        
        try {
            // delegate the call and return the result
            return dm.getTargets();
        } finally {
            // restore the context classloader
            loader.restoreLoader();
        }
    }
    
}