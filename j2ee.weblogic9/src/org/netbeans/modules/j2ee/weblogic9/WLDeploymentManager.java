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
package org.netbeans.modules.j2ee.weblogic9;


import java.io.*;
import java.util.*;

import javax.enterprise.deploy.model.*;
import javax.enterprise.deploy.shared.*;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.exceptions.*;
import javax.enterprise.deploy.spi.status.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.j2ee.weblogic9.config.EarDeploymentConfiguration;
import org.netbeans.modules.j2ee.weblogic9.config.EjbDeploymentConfiguration;
import org.netbeans.modules.j2ee.weblogic9.config.WarDeploymentConfiguration;

import org.netbeans.modules.j2ee.weblogic9.util.WLDebug;
import org.openide.ErrorManager;


/**
 * Main class of the deployment process. This serves as a wrapper for the 
 * server's DeploymentManager implementation, all calls are delegated to the
 * server's implementation, with the thread's context classloader updated
 * if necessary.
 * 
 * @author Kirill Sorokin
 */
public class WLDeploymentManager implements DeploymentManager {
    
    private DeploymentManager dm;
    private InstanceProperties instanceProperties;
    private String uri;
    private String username;
    private String password;
    private boolean isConnected;
    private String host;
    private String port;
    
    /** System process of the started WL server */
    private Process process;
    
    /** Create connected DM */
    public WLDeploymentManager(DeploymentManager dm, String uri, String username, String password, String host, String port) {
        this.dm = dm;
        this.uri = uri;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.isConnected = true;
    }
    
    /** Create disconnected DM */
    public WLDeploymentManager(DeploymentManager dm, String uri, String host, String port) {
        this.dm = dm;
        this.uri = uri;
        this.host = host;
        this.port = port;
        this.isConnected = false;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Connection data methods
    ////////////////////////////////////////////////////////////////////////////
    
    public boolean isConnected() {
        return isConnected;
    }
    
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
        return host;
    }
    
    public String getUsername () {
        return getInstanceProperties().getProperty(InstanceProperties.USERNAME_ATTR);
    }
    
    public String getPassword () {
        return getInstanceProperties().getProperty(InstanceProperties.PASSWORD_ATTR);
    }
    
    /**
     * Returns the server port stored in the instance properties
     */
    public String getPort() {
        return port;
    }
    
    public boolean isLocal () {
        return new Boolean(getInstanceProperties().getProperty(WLPluginProperties.IS_LOCAL_ATTR)).booleanValue();
    }
    /**
     * Returns the InstanceProperties object for the current server instance
     */
    public InstanceProperties getInstanceProperties() {
        if (instanceProperties == null) {
            this.instanceProperties = InstanceProperties.getInstanceProperties(uri);
            
        }
        return instanceProperties;
    }
    
    /**
     * Set the <code>Process</code> of the started WL server.
     *
     * @param <code>Process</code> of the started WL server.
     */
    public synchronized void setServerProcess(Process process) {
        this.process = process;
    }

    /**
     * Return <code>Process</code> of the started WL server.
     *
     * @return <code>Process</code> of the started WL server, <code>null</code> if
     *         WL wasn't started by IDE.
     */
    public synchronized Process getServerProcess() {
        return process;
    }
    
    public ProgressObject distribute(Target[] target, File file, File file2) 
            throws IllegalStateException {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify(getClass(), "distribute(" + target + ", " + // NOI18N
                    file + ", " + file2 + ")");                        // NOI18N
        
        if (isLocal()) {
            //autodeployment version
            return new WLDeployer(uri).deploy(target, file, file2, getHost(), getPort());
        } else {
            //weblogic jsr88 version
            modifiedLoader();
            try {
                return new DelegatingProgressObject(dm.distribute(target, file, file2));
            } finally {
                originalLoader();
            }
        }
    }
    
    public ProgressObject distribute(Target[] target, ModuleType moduleType, InputStream inputStream, InputStream inputStream0) throws IllegalStateException {
        return distribute(target, inputStream, inputStream0);
    }
    
    private ClassLoader swapLoader;
    
    private void modifiedLoader() {
        swapLoader = Thread.currentThread().getContextClassLoader();
        String serverRoot = getInstanceProperties().getProperty(WLPluginProperties.SERVER_ROOT_ATTR);
        // if serverRoot is null, then we are in a server instance registration process, thus this call
        // is made from InstanceProperties creation -> WLPluginProperties singleton contains 
        // install location of the instance being registered
        if (serverRoot == null)
            serverRoot = WLPluginProperties.getInstance().getInstallLocation();

        Thread.currentThread().setContextClassLoader(WLDeploymentFactory.getWLClassLoader(serverRoot));
    }
    private void originalLoader() {
        Thread.currentThread().setContextClassLoader(swapLoader);
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
    
    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public ProgressObject redeploy(TargetModuleID[] targetModuleID, 
            InputStream inputStream, InputStream inputStream2) 
            throws UnsupportedOperationException, IllegalStateException {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("redeploy(" + targetModuleID + ", " +       // NOI18N
                    inputStream + ", " + inputStream2 + ")");          // NOI18N
        modifiedLoader();
        try {
            return new DelegatingProgressObject(dm.redeploy(targetModuleID, inputStream, inputStream2));
        } finally {
            originalLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public ProgressObject distribute(Target[] target, InputStream inputStream, 
            InputStream inputStream2) throws IllegalStateException {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("distribute(" + target + ", " +             // NOI18N
                    inputStream + ", " + inputStream2 + ")");          // NOI18N
        modifiedLoader();
        try {
            return new DelegatingProgressObject(dm.distribute(target, inputStream, inputStream2));
        } finally {
            originalLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public ProgressObject undeploy(TargetModuleID[] targetModuleID) 
            throws IllegalStateException {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("undeploy(" + targetModuleID + ")");        // NOI18N
        modifiedLoader();
        try {
            return new DelegatingProgressObject(dm.undeploy(targetModuleID));
        } finally {
            originalLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public ProgressObject stop(TargetModuleID[] targetModuleID) 
            throws IllegalStateException {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("stop(" + targetModuleID + ")");            // NOI18N
                
        modifiedLoader();
        try {
            return new DelegatingProgressObject(dm.stop(targetModuleID));
        } finally {
            originalLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public ProgressObject start(TargetModuleID[] targetModuleID) 
            throws IllegalStateException {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("start(" + targetModuleID + ")");           // NOI18N
        
        modifiedLoader();
        try {
            return new DelegatingProgressObject(dm.start(targetModuleID));
        } finally {
            originalLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public TargetModuleID[] getAvailableModules(ModuleType moduleType, 
            Target[] target) throws TargetException, IllegalStateException {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("getAvailableModules(" + moduleType +       // NOI18N
                    ", " + target + ")");                              // NOI18N
        
        modifiedLoader();
        try {
            TargetModuleID t[] = dm.getAvailableModules(moduleType, target);
            return t;
        } finally {
            originalLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public TargetModuleID[] getNonRunningModules(ModuleType moduleType, 
            Target[] target) throws TargetException, IllegalStateException {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("getNonRunningModules(" + moduleType +      // NOI18N
                    ", " + target + ")");                              // NOI18N
        
        modifiedLoader();
        try {
            TargetModuleID t[] = dm.getNonRunningModules(moduleType, target);
            for (int i=0; i < t.length; i++) {
                System.out.println("non running module:" + t[i]);
            }
            return t;
        } finally {
            originalLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public TargetModuleID[] getRunningModules(ModuleType moduleType, 
            Target[] target) throws TargetException, IllegalStateException {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("getRunningModules(" + moduleType +         // NOI18N
                    ", " + target + ")");                              // NOI18N
        
        modifiedLoader();
        try {
            TargetModuleID t[] = dm.getRunningModules(moduleType, target);
            for (int i=0; i < t.length; i++) {
                System.out.println("running module:" + t[i]);
            }
            return t;
        } finally {
            originalLoader();
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
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("redeploy(" + targetModuleID + ", " +       // NOI18N
                    file + ", " + file2 + ")");                        // NOI18N
        
        modifiedLoader();
        try {
            return new DelegatingProgressObject(dm.redeploy(targetModuleID, file, file2));
        } finally {
            originalLoader();
        }
    }
    
    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public void setLocale(Locale locale) throws UnsupportedOperationException {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("setLocale(" + locale + ")");               // NOI18N
        
        modifiedLoader();
        try {
            dm.setLocale(locale);
        } finally {
            originalLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public boolean isLocaleSupported(Locale locale) {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("isLocaleSupported(" + locale + ")");       // NOI18N
        
        modifiedLoader();
        try {
            return dm.isLocaleSupported(locale);
        } finally {
            originalLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public void setDConfigBeanVersion(
            DConfigBeanVersionType dConfigBeanVersionType) 
            throws DConfigBeanVersionUnsupportedException {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("setDConfigBeanVersion(" +                  // NOI18N
                    dConfigBeanVersionType + ")");                     // NOI18N
        
        modifiedLoader();
        try {
            dm.setDConfigBeanVersion(dConfigBeanVersionType);
        } finally {
            originalLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public boolean isDConfigBeanVersionSupported(
            DConfigBeanVersionType dConfigBeanVersionType) {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("isDConfigBeanVersionSupported(" +          // NOI18N
                    dConfigBeanVersionType + ")");                     // NOI18N
        
        modifiedLoader();
        try {
            return dm.isDConfigBeanVersionSupported(dConfigBeanVersionType);
        } finally {
            originalLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public void release() {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("release()");                               // NOI18N
        
        modifiedLoader();
        try {
            if (dm != null) {
                // delegate the call and clear the stored deployment manager
                try {
                    dm.release();
                }
                catch (Exception e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
                finally {
                    dm = null;
                }
            }
        } finally {
            originalLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public boolean isRedeploySupported() {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("isRedeploySupported()");                   // NOI18N
        
        modifiedLoader();
        try {
            return dm.isRedeploySupported();
        } finally {
            originalLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public Locale getCurrentLocale() {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("getCurrentLocale()");                      // NOI18N
        
        modifiedLoader();
        try {
            return dm.getCurrentLocale();
        } finally {
            originalLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public DConfigBeanVersionType getDConfigBeanVersion() {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("getDConfigBeanVersion()");                 // NOI18N

        modifiedLoader();
        try {
            return dm.getDConfigBeanVersion();
        } finally {
            originalLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public Locale getDefaultLocale() {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("getDefaultLocale()");                      // NOI18N
        
        modifiedLoader();
        try {
            return dm.getDefaultLocale();
        } finally {
            originalLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public Locale[] getSupportedLocales() {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("getSupportedLocales()");                   // NOI18N
        
        modifiedLoader();
        try {
            return dm.getSupportedLocales();
        } finally {
            originalLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether 
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public Target[] getTargets() throws IllegalStateException {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify("getTargets()");                            // NOI18N
        
        modifiedLoader();
        try {
            return dm.getTargets();
        } finally {
            originalLoader();
        }
    }
    
    static class DelegatingProgressObject implements ProgressObject, ProgressListener {
        ProgressObject original;
        private Vector listeners = new Vector();
        DelegatingProgressObject (ProgressObject original) {
            this.original = original;
            original.addProgressListener(this);
        }
        
        public DeploymentStatus getDeploymentStatus() {
            return original.getDeploymentStatus();
        }

        public TargetModuleID[] getResultTargetModuleIDs() {
            return original.getResultTargetModuleIDs();
        }

        public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
            return getClientConfiguration(targetModuleID);
        }

        public boolean isCancelSupported() {
            return original.isCancelSupported();
        }

        public void cancel() throws OperationUnsupportedException {
            original.cancel();
        }

        public boolean isStopSupported() {
            return original.isStopSupported();
        }

        public void stop() throws OperationUnsupportedException {
            original.stop();
        }

        public void addProgressListener(ProgressListener progressListener) {
            listeners.add(progressListener);
        }

        public void removeProgressListener(ProgressListener progressListener) {
            listeners.remove(progressListener);
        }

        public void handleProgressEvent(ProgressEvent progressEvent) {
            java.util.Vector targets = null;
            synchronized (this) {
                if (listeners != null) {
                    targets = (java.util.Vector) listeners.clone();
                }
            }

            if (targets != null) {
                for (int i = 0; i < targets.size(); i++) {
                    ProgressListener target = (ProgressListener)targets.elementAt(i);
                    target.handleProgressEvent(progressEvent);
                }
        }
        }
        
    }
}