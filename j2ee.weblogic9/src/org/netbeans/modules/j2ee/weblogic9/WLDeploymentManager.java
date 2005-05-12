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
package org.netbeans.modules.j2ee.weblogic9;

import org.netbeans.modules.j2ee.weblogic9.util.WLDebug;
import java.io.*;
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
public class WLDeploymentManager implements DeploymentManager {
    
    private WLClassLoader loader;
    private DeploymentFactory factory;
    private DeploymentManager dm;
    
    private InstanceProperties instanceProperties;
    
    private String uri;
    private String username;
    private String password;
    
    private boolean isConnected;
    
    public WLDeploymentManager(String uri, String username, String password) {
        this.uri = uri;
        this.username = username;
        this.password = password;
    }
    
    public WLDeploymentManager(String uri) {
        this(uri, null, null);
    }
    
    private void parseUri() {
        String[] parts = uri.split(":"); // NOI18N
        
        getInstanceProperties().setProperty(WLDeploymentFactory.HOST_ATTR, parts[3].substring(2));
        getInstanceProperties().setProperty(WLDeploymentFactory.PORT_ATTR, parts[4]);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Connection data methods
    ////////////////////////////////////////////////////////////////////////////
    public String getURI() {
        return this.uri;
    }
    
    public String getHost() {
        return getInstanceProperties().getProperty(WLDeploymentFactory.HOST_ATTR);
    }
    
    public String getPort() {
        return getInstanceProperties().getProperty(WLDeploymentFactory.PORT_ATTR);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Class loading related things
    ////////////////////////////////////////////////////////////////////////////
    private void loadDeploymentFactory() {
        if (WLDebug.isEnabled())
            WLDebug.notify(getClass(), "loadDeploymentFactory()");
        
        if (factory == null) {
            loader = WLClassLoader.getInstance(getInstanceProperties().getProperty(WLDeploymentFactory.SERVER_ROOT_ATTR));
            
            loader.updateLoader();
            
            try {
                factory = (DeploymentFactory) loader.loadClass("weblogic.deploy.api.spi.factories.internal.DeploymentFactoryImpl").newInstance();
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
        if (WLDebug.isEnabled())
            WLDebug.notify(getClass(), "updateDeploymentManager()");
        
        loadDeploymentFactory();
        
        loader.updateLoader();
        
        try {
            if (dm != null) {
                dm.release();
            }
            dm = factory.getDeploymentManager(uri, username, password);
            isConnected = true;
        } catch (DeploymentManagerCreationException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
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
        if (instanceProperties == null) {
            this.instanceProperties = InstanceProperties.getInstanceProperties(uri);
            
            parseUri();
        }
        
        return instanceProperties;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // DeploymentManager Implementation
    ////////////////////////////////////////////////////////////////////////////
    public ProgressObject distribute(Target[] target, File file, File file2) throws IllegalStateException {
        if (WLDebug.isEnabled())
            WLDebug.notify(getClass(), "distribute(" + target + ", " + file + ", " + file2 + ")");
        
        updateDeploymentManager();
        
        loader.updateLoader();
        
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(WLDeploymentManager.class, "ERR_illegalState"));
        }
        
        try {
            return dm.distribute(target, file, file2);
        } finally {
            loader.restoreLoader();
        }
    }
    
    public DeploymentConfiguration createConfiguration(DeployableObject deployableObject) throws InvalidModuleException {
        if (WLDebug.isEnabled())
            WLDebug.notify(getClass(), "createConfiguration(" + deployableObject + ")");
        
        updateDeploymentManager();
        
        loader.updateLoader();
        try {
            return new WLDeploymentConfiguration(dm, deployableObject);
        } finally {
            loader.restoreLoader();
        }
    }
    
    public ProgressObject redeploy(TargetModuleID[] targetModuleID, InputStream inputStream, InputStream inputStream2) throws UnsupportedOperationException, IllegalStateException {
        if (WLDebug.isEnabled())
            WLDebug.notify(getClass(), "redeploy(" + targetModuleID + ", " + inputStream + ", " + inputStream2 + ")");
        
        updateDeploymentManager();
        
        loader.updateLoader();
        
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(WLDeploymentManager.class, "ERR_illegalState"));
        }
        
        try {
            return dm.redeploy(targetModuleID, inputStream, inputStream2);
        } finally {
            loader.restoreLoader();
        }
    }

    public ProgressObject distribute(Target[] target, InputStream inputStream, InputStream inputStream2) throws IllegalStateException {
        if (WLDebug.isEnabled())
            WLDebug.notify(getClass(), "distribute(" + target + ", " + inputStream + ", " + inputStream2 + ")");
        
        updateDeploymentManager();
        
        loader.updateLoader();
        
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(WLDeploymentManager.class, "ERR_illegalState"));
        }
        
        try {
            return dm.distribute(target, inputStream, inputStream2);
        } finally {
            loader.restoreLoader();
        }
    }

    public ProgressObject undeploy(TargetModuleID[] targetModuleID) throws IllegalStateException {
        if (WLDebug.isEnabled())
            WLDebug.notify(getClass(), "undeploy(" + targetModuleID + ")");
        
        updateDeploymentManager();
        
        loader.updateLoader();
        
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(WLDeploymentManager.class, "ERR_illegalState"));
        }
        
        try {
            return dm.undeploy(targetModuleID);
        } finally {
            loader.restoreLoader();
        }
    }

    public ProgressObject stop(TargetModuleID[] targetModuleID) throws IllegalStateException {
        if (WLDebug.isEnabled())
            WLDebug.notify(getClass(), "stop(" + targetModuleID + ")");
                
        updateDeploymentManager();
        
        loader.updateLoader();
        
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(WLDeploymentManager.class, "ERR_illegalState"));
        }
        
        try {
            return dm.stop(targetModuleID);
        } finally {
            loader.restoreLoader();
        }
    }

    public ProgressObject start(TargetModuleID[] targetModuleID) throws IllegalStateException {
        if (WLDebug.isEnabled())
            WLDebug.notify(getClass(), "start(" + targetModuleID + ")");
        
        updateDeploymentManager();
        
        loader.updateLoader();
        
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(WLDeploymentManager.class, "ERR_illegalState"));
        }
        
        try {
            return dm.start(targetModuleID);
        } finally {
            loader.restoreLoader();
        }
    }

    public TargetModuleID[] getAvailableModules(ModuleType moduleType, Target[] target) throws TargetException, IllegalStateException {
        if (WLDebug.isEnabled())
            WLDebug.notify(getClass(), "getAvailableModules(" + moduleType + ", " + target + ")");
        
        updateDeploymentManager();
        
        loader.updateLoader();
        
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(WLDeploymentManager.class, "ERR_illegalState"));
        }
        
        try {
            return dm.getAvailableModules(moduleType, target);
        } finally {
            loader.restoreLoader();
        }
    }

    public TargetModuleID[] getNonRunningModules(ModuleType moduleType, Target[] target) throws TargetException, IllegalStateException {
        if (WLDebug.isEnabled())
            WLDebug.notify(getClass(), "getNonRunningModules(" + moduleType + ", " + target + ")");
        
        updateDeploymentManager();
        
        loader.updateLoader();
        
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(WLDeploymentManager.class, "ERR_illegalState"));
        }
        
        try {
            return dm.getNonRunningModules(moduleType, target);
        } finally {
            loader.restoreLoader();
        }
    }

    public TargetModuleID[] getRunningModules(ModuleType moduleType, Target[] target) throws TargetException, IllegalStateException {
        if (WLDebug.isEnabled())
            WLDebug.notify(getClass(), "getRunningModules(" + moduleType + ", " + target + ")");
        
        updateDeploymentManager();
        
        loader.updateLoader();
        
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(WLDeploymentManager.class, "ERR_illegalState"));
        }
        
        try {
            return dm.getRunningModules(moduleType, target);
        } finally {
            loader.restoreLoader();
        }
    }

    public ProgressObject redeploy(TargetModuleID[] targetModuleID, File file, File file2) throws UnsupportedOperationException, IllegalStateException {
        if (WLDebug.isEnabled())
            WLDebug.notify(getClass(), "redeploy(" + targetModuleID + ", " + file + ", " + file2 + ")");
        
        updateDeploymentManager();
        
        loader.updateLoader();
        
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(WLDeploymentManager.class, "ERR_illegalState"));
        }
        
        try {
            return dm.redeploy(targetModuleID, file, file2);
        } finally {
            loader.restoreLoader();
        }
    }
    
    public void setLocale(Locale locale) throws UnsupportedOperationException {
        if (WLDebug.isEnabled())
            WLDebug.notify(getClass(), "setLocale(" + locale + ")");
        
        updateDeploymentManager();
        
        dm.setLocale(locale);
    }

    public boolean isLocaleSupported(Locale locale) {
        if (WLDebug.isEnabled())
            WLDebug.notify(getClass(), "isLocaleSupported(" + locale + ")");
        
        updateDeploymentManager();
        
        return dm.isLocaleSupported(locale);
    }

    public void setDConfigBeanVersion(DConfigBeanVersionType dConfigBeanVersionType) throws DConfigBeanVersionUnsupportedException {
        if (WLDebug.isEnabled())
            WLDebug.notify(getClass(), "setDConfigBeanVersion(" + dConfigBeanVersionType + ")");
        
        updateDeploymentManager();
        
        dm.setDConfigBeanVersion(dConfigBeanVersionType);
    }

    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType dConfigBeanVersionType) {
        if (WLDebug.isEnabled())
            WLDebug.notify(getClass(), "isDConfigBeanVersionSupported(" + dConfigBeanVersionType + ")");
        
        updateDeploymentManager();
        
        return dm.isDConfigBeanVersionSupported(dConfigBeanVersionType);
    }

    public void release() {
        if (WLDebug.isEnabled())
            WLDebug.notify(getClass(), "release()");
        
        if (dm != null) {
            dm.release();
            dm = null;
        }
    }

    public boolean isRedeploySupported() {
        if (WLDebug.isEnabled())
            WLDebug.notify(getClass(), "isRedeploySupported()");
        
        updateDeploymentManager();
        
        return dm.isRedeploySupported();
    }

    public Locale getCurrentLocale() {
        if (WLDebug.isEnabled())
            WLDebug.notify(getClass(), "getCurrentLocale()");
        
        updateDeploymentManager();
        
        return dm.getCurrentLocale();
    }

    public DConfigBeanVersionType getDConfigBeanVersion() {
        if (WLDebug.isEnabled())
            WLDebug.notify(getClass(), "getDConfigBeanVersion()");
        
        updateDeploymentManager();
        
        return dm.getDConfigBeanVersion();
    }

    public Locale getDefaultLocale() {
        if (WLDebug.isEnabled())
            WLDebug.notify(getClass(), "getDefaultLocale()");
        
        updateDeploymentManager();
        
        return dm.getDefaultLocale();
    }

    public Locale[] getSupportedLocales() {
        if (WLDebug.isEnabled())
            WLDebug.notify(getClass(), "getSupportedLocales()");
        
        updateDeploymentManager();
        
        return dm.getSupportedLocales();
    }

    public Target[] getTargets() throws IllegalStateException {
        if (WLDebug.isEnabled())
            WLDebug.notify(getClass(), "getTargets()");
        
        updateDeploymentManager();
        
        loader.updateLoader();
        
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(WLDeploymentManager.class, "ERR_illegalState"));
        }
        
        try {
            return dm.getTargets();
        } finally {
            loader.restoreLoader();
        }
    }
    
}