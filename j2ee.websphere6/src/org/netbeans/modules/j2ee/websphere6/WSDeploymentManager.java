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

import org.netbeans.modules.j2ee.websphere6.util.WSDebug;
import java.io.*;
import java.security.AccessControlException;
import java.util.*;

import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.exceptions.*;
import javax.enterprise.deploy.spi.factories.*;
import javax.enterprise.deploy.spi.status.*;

import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Kirill Sorokin
 */
public class WSDeploymentManager implements DeploymentManager {
    
    private WSClassLoader loader;
    private DeploymentFactory factory;
    private DeploymentManager dm;
    
    private InstanceProperties instanceProperties;
    
    private String uri;
    private String username;
    private String password;
    
    private boolean isConnected;
    
    public WSDeploymentManager(String uri, String username, String password) {
        if (WSDebug.isEnabled())
            WSDebug.notify("WSDeploymentManager(" + uri + ", " + username + ", " + password + ")");
        
        this.uri = uri;
        this.username = username;
        this.password = password;
    }
    
    public WSDeploymentManager(String uri) {
        this(uri, null, null);
    }
    
    private void parseUri() {
        String[] parts = uri.split(":"); // NOI18N
        
        getInstanceProperties().setProperty(WSDeploymentFactory.HOST_ATTR, parts[2]);
        getInstanceProperties().setProperty(WSDeploymentFactory.PORT_ATTR, parts[3]);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Connection data methods
    ////////////////////////////////////////////////////////////////////////////
    public String getURI() {
        return this.uri;
    }
    
    public String getHost() {
        return getInstanceProperties().getProperty(WSDeploymentFactory.HOST_ATTR);
    }
    
    public String getPort() {
        return getInstanceProperties().getProperty(WSDeploymentFactory.PORT_ATTR);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Class loading related things
    ////////////////////////////////////////////////////////////////////////////
    private void loadDeploymentFactory() {
        if (WSDebug.isEnabled())
            WSDebug.notify("loadDeploymentFactory()");
        
        if (factory == null) {
            loader = WSClassLoader.getInstance(getInstanceProperties().getProperty(WSDeploymentFactory.SERVER_ROOT_ATTR), getInstanceProperties().getProperty(WSDeploymentFactory.DOMAIN_ROOT_ATTR));
            
            loader.updateLoader();
            
            try {
                factory = (DeploymentFactory) loader.loadClass("com.ibm.ws.management.application.j2ee.deploy.spi.factories.DeploymentFactoryImpl").newInstance();
            } catch (ClassNotFoundException e) {
                ErrorManager.getDefault().notify(ErrorManager.ERROR, e);
            } catch (InstantiationException e) {
                ErrorManager.getDefault().notify(ErrorManager.ERROR, e);
            } catch (IllegalAccessException e) {
                ErrorManager.getDefault().notify(ErrorManager.ERROR, e);
            } finally {
                loader.restoreLoader();
            }
        }
    }
    
    private void updateDeploymentManager() {
        if (WSDebug.isEnabled())
            WSDebug.notify("updateDeploymentManager()");
        
        loadDeploymentFactory();
        
        loader.updateLoader();
        
//        System.setProperty("com.ibm.SOAP.configURL", "C:/Program Files/IBM/WebSphere/AppServer/profiles/default/properties/soap.client.props");
        
        try {
            if (dm != null) {
                dm.release();
            }
            dm = factory.getDeploymentManager(uri, username, password);
            isConnected = true;
        } catch (DeploymentManagerCreationException e) {
            try {
                isConnected = false;
                dm = factory.getDisconnectedDeploymentManager(uri);
            } catch (DeploymentManagerCreationException ex) {
                ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
            }
        } finally {
            loader.restoreLoader();
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // IDE data methods
    ////////////////////////////////////////////////////////////////////////////
    public InstanceProperties getInstanceProperties() {
        if (WSDebug.isEnabled())
            WSDebug.notify("getInstanceProperties()");
        
        if (instanceProperties == null) {
            if (WSDebug.isEnabled())
                WSDebug.notify("getting instance properties for uri: " + uri);
            instanceProperties = InstanceProperties.getInstanceProperties(uri);
            
            if (instanceProperties != null) {
                parseUri();
            }
        }
        
        return instanceProperties;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // DeploymentManager Implementation
    ////////////////////////////////////////////////////////////////////////////
    public ProgressObject distribute(Target[] target, File file, File file2) throws IllegalStateException {
        if (WSDebug.isEnabled())
            WSDebug.notify("distribute(" + target + ", " + file + ", " + file2 + ")");
        
        updateDeploymentManager();
        
        loader.updateLoader();
        
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(WSDeploymentManager.class, "ERR_illegalState"));
        }
        
        try {
            return dm.distribute(target, file, file2);
        } finally {
            loader.restoreLoader();
        }
    }
    
    public DeploymentConfiguration createConfiguration(DeployableObject deployableObject) throws InvalidModuleException {
        if (WSDebug.isEnabled())
            WSDebug.notify("createConfiguration(" + deployableObject + ")");
        
        updateDeploymentManager();
        
        loader.updateLoader();
        try {
            return new WSDeploymentConfiguration(dm, deployableObject, getInstanceProperties());
        } finally {
            loader.restoreLoader();
        }
    }
    
    public ProgressObject redeploy(TargetModuleID[] targetModuleID, InputStream inputStream, InputStream inputStream2) throws UnsupportedOperationException, IllegalStateException {
        if (WSDebug.isEnabled())
            WSDebug.notify("redeploy(" + targetModuleID + ", " + inputStream + ", " + inputStream2 + ")");
        
        updateDeploymentManager();
        
        loader.updateLoader();
        
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(WSDeploymentManager.class, "ERR_illegalState"));
        }
        
        try {
            return dm.redeploy(targetModuleID, inputStream, inputStream2);
        } finally {
            loader.restoreLoader();
        }
    }

    public ProgressObject distribute(Target[] target, InputStream inputStream, InputStream inputStream2) throws IllegalStateException {
        if (WSDebug.isEnabled())
            WSDebug.notify("distribute(" + target + ", " + inputStream + ", " + inputStream2 + ")");
        
        updateDeploymentManager();
        
        loader.updateLoader();
        
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(WSDeploymentManager.class, "ERR_illegalState"));
        }
        
        try {
            return dm.distribute(target, inputStream, inputStream2);
        } finally {
            loader.restoreLoader();
        }
    }

    public ProgressObject undeploy(TargetModuleID[] targetModuleID) throws IllegalStateException {
        if (WSDebug.isEnabled())
            WSDebug.notify("undeploy(" + targetModuleID + ")");
        
        updateDeploymentManager();
        
        loader.updateLoader();
        
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(WSDeploymentManager.class, "ERR_illegalState"));
        }
        
        try {
            return dm.undeploy(targetModuleID);
        } finally {
            loader.restoreLoader();
        }
    }

    public ProgressObject stop(TargetModuleID[] targetModuleID) throws IllegalStateException {
        if (WSDebug.isEnabled())
            WSDebug.notify("stop(" + targetModuleID + ")");
                
        updateDeploymentManager();
        
        loader.updateLoader();
        
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(WSDeploymentManager.class, "ERR_illegalState"));
        }
        
        try {
            return dm.stop(targetModuleID);
        } finally {
            loader.restoreLoader();
        }
    }

    public ProgressObject start(TargetModuleID[] targetModuleID) throws IllegalStateException {
        if (WSDebug.isEnabled())
            WSDebug.notify("start(" + targetModuleID + ")");
        
        updateDeploymentManager();
        
        loader.updateLoader();
        
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(WSDeploymentManager.class, "ERR_illegalState"));
        }
        
        try {
            return dm.start(targetModuleID);
        } finally {
            loader.restoreLoader();
        }
    }

    public TargetModuleID[] getAvailableModules(ModuleType moduleType, Target[] target) throws TargetException, IllegalStateException {
        if (WSDebug.isEnabled())
            WSDebug.notify("getAvailableModules(" + moduleType + ", " + target + ")");
        
        updateDeploymentManager();
        
        loader.updateLoader();
        
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(WSDeploymentManager.class, "ERR_illegalState"));
        }
        
        try {
            return dm.getAvailableModules(moduleType, target);
        } finally {
            loader.restoreLoader();
        }
    }

    public TargetModuleID[] getNonRunningModules(ModuleType moduleType, Target[] target) throws TargetException, IllegalStateException {
        if (WSDebug.isEnabled())
            WSDebug.notify("getNonRunningModules(" + moduleType + ", " + target + ")");
        
        updateDeploymentManager();
        
        loader.updateLoader();
        
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(WSDeploymentManager.class, "ERR_illegalState"));
        }
        
        try {
            return dm.getNonRunningModules(moduleType, target);
        } finally {
            loader.restoreLoader();
        }
    }

    public TargetModuleID[] getRunningModules(ModuleType moduleType, Target[] target) throws TargetException, IllegalStateException {
        if (WSDebug.isEnabled())
            WSDebug.notify("getRunningModules(" + moduleType + ", " + target + ")");
        
        updateDeploymentManager();
        
        loader.updateLoader();
        
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(WSDeploymentManager.class, "ERR_illegalState"));
        }
        
        try {
            return dm.getRunningModules(moduleType, target);
        } finally {
            loader.restoreLoader();
        }
    }

    public ProgressObject redeploy(TargetModuleID[] targetModuleID, File file, File file2) throws UnsupportedOperationException, IllegalStateException {
        if (WSDebug.isEnabled())
            WSDebug.notify("redeploy(" + targetModuleID + ", " + file + ", " + file2 + ")");
        
        updateDeploymentManager();
        
        loader.updateLoader();
        
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(WSDeploymentManager.class, "ERR_illegalState"));
        }
        
        try {
            return dm.redeploy(targetModuleID, file, file2);
        } finally {
            loader.restoreLoader();
        }
    }
    
    public void setLocale(Locale locale) throws UnsupportedOperationException {
        if (WSDebug.isEnabled())
            WSDebug.notify("setLocale(" + locale + ")");
        
        updateDeploymentManager();
        
        dm.setLocale(locale);
    }

    public boolean isLocaleSupported(Locale locale) {
        if (WSDebug.isEnabled())
            WSDebug.notify("isLocaleSupported(" + locale + ")");
        
        updateDeploymentManager();
        
        return dm.isLocaleSupported(locale);
    }

    public void setDConfigBeanVersion(DConfigBeanVersionType dConfigBeanVersionType) throws DConfigBeanVersionUnsupportedException {
        if (WSDebug.isEnabled())
            WSDebug.notify("setDConfigBeanVersion(" + dConfigBeanVersionType + ")");
        
        updateDeploymentManager();
        
        dm.setDConfigBeanVersion(dConfigBeanVersionType);
    }

    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType dConfigBeanVersionType) {
        if (WSDebug.isEnabled())
            WSDebug.notify("isDConfigBeanVersionSupported(" + dConfigBeanVersionType + ")");
        
        updateDeploymentManager();
        
        return dm.isDConfigBeanVersionSupported(dConfigBeanVersionType);
    }

    public void release() {
        if (WSDebug.isEnabled())
            WSDebug.notify("release()");
        
        if (dm != null) {
            dm.release();
            dm = null;
        }
    }

    public boolean isRedeploySupported() {
        if (WSDebug.isEnabled())
            WSDebug.notify("isRedeploySupported()");
        
        updateDeploymentManager();
        
        return dm.isRedeploySupported();
    }

    public Locale getCurrentLocale() {
        if (WSDebug.isEnabled())
            WSDebug.notify("getCurrentLocale()");
        
        updateDeploymentManager();
        
        return dm.getCurrentLocale();
    }

    public DConfigBeanVersionType getDConfigBeanVersion() {
        if (WSDebug.isEnabled())
            WSDebug.notify("getDConfigBeanVersion()");
        
        updateDeploymentManager();
        
        return dm.getDConfigBeanVersion();
    }

    public Locale getDefaultLocale() {
        if (WSDebug.isEnabled())
            WSDebug.notify("getDefaultLocale()");
        
        updateDeploymentManager();
        
        return dm.getDefaultLocale();
    }

    public Locale[] getSupportedLocales() {
        if (WSDebug.isEnabled())
            WSDebug.notify("getSupportedLocales()");
        
        updateDeploymentManager();
        
        return dm.getSupportedLocales();
    }

    public Target[] getTargets() throws IllegalStateException {
        if (WSDebug.isEnabled())
            WSDebug.notify("getTargets()");
        
        updateDeploymentManager();
        
        loader.updateLoader();
        
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(WSDeploymentManager.class, "ERR_illegalState"));
        }
        
        try {
            return dm.getTargets();
        } finally {
            loader.restoreLoader();
        }
    }
    
}