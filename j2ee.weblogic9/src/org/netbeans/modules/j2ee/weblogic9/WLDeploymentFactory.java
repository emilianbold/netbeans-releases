/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.weblogic9;

import java.io.*;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import org.netbeans.modules.j2ee.weblogic9.util.WLDebug;
import org.openide.ErrorManager;

import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.factories.*;
import javax.enterprise.deploy.spi.exceptions.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;

import org.openide.util.NbBundle;

/**
 * The main entry point to the plugin. Keeps the required static data for the 
 * plugin and returns the DeploymentManagers required for deployment and 
 * configuration. Does not directly perform any interaction with the server.
 * 
 * @author Kirill Sorokin
 */
public class WLDeploymentFactory implements DeploymentFactory {
    
    public static final String URI_PREFIX = "deployer:WebLogic:http://"; // NOI18N
    
    // additional properties that are stored in the InstancePropeties object
    public static final String SERVER_ROOT_ATTR = "serverRoot";        // NOI18N
    public static final String DOMAIN_ROOT_ATTR = "domainRoot";        // NOI18N
    public static final String IS_LOCAL_ATTR = "isLocal";              // NOI18N
    public static final String HOST_ATTR = "host";                     // NOI18N
    public static final String PORT_ATTR = "port";                     // NOI18N
    public static final String DEBUGGER_PORT_ATTR = "debuggerPort";    // NOI18N
    
    /**
     * The singleton instance of the factory
     */
    private static WLDeploymentFactory instance;

    private DeploymentFactory wlFactory = null;
    
    /**
     * The singleton factory method
     * 
     * @return the singleton instance of the factory
     */
    public static synchronized DeploymentFactory getInstance() {
        if (instance == null) {
            instance = new WLDeploymentFactory();
            DeploymentFactoryManager.getInstance().registerDeploymentFactory(instance);
        }
        return instance;
    }
    
    static class Empty {
    }
    
    public static class ExtendedClassLoader extends URLClassLoader {

        public ExtendedClassLoader( ClassLoader _loader) throws MalformedURLException, RuntimeException {
            super(new URL[0], _loader);
        }
        
        public void addURL(File f) throws MalformedURLException, RuntimeException {
                if (f.isFile()){
                    addURL(f.toURL());
                }
        }

        protected PermissionCollection getPermissions(CodeSource _cs) {
            Permissions p = new Permissions();
            p.add(new AllPermission());
            return p;
        }
        
    }
    
    private static ExtendedClassLoader loader = null;
    
    public static ClassLoader getWLClassLoader () {
        if (loader == null) {
            resetWLClassLoader(WLPluginProperties.getInstance().getInstallLocation());
        }
        return loader;
    }
    
    public static void resetWLClassLoader (String serverRoot) {
        loader = null;
        try {
            loader = new ExtendedClassLoader(new Empty().getClass().getClassLoader());
            loader.addURL(new File(serverRoot + "/server/lib/weblogic.jar"));
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        }
    }
    
    private DeploymentManager getDM(String uri, String username, String password, String host, String port) throws DeploymentManagerCreationException {
        if (WLDebug.isEnabled()) {
            WLDebug.notify(WLDeploymentFactory.class, "getDM, uri:" + uri+" username:" + username+" password:"+password+" host:"+host+" port:"+port);
        }
        DeploymentManagerCreationException dmce = null;
        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader loader = getWLClassLoader();
            Thread.currentThread().setContextClassLoader(loader);
            Class helperClazz = loader.loadClass("weblogic.deploy.api.tools.SessionHelper"); //NOI18N
            Method m = helperClazz.getDeclaredMethod("getDeploymentManager", new Class [] {String.class,String.class,String.class,String.class});
            Object o = m.invoke(null, new Object [] {host, port, username, password});
            if (DeploymentManager.class.isAssignableFrom(o.getClass())) {
                return (DeploymentManager) o;
            } else {
                dmce = new DeploymentManagerCreationException ("Instance created by weblogic is not DeploymentManager instance.");
            }
        } catch (Exception e) {
            dmce = new DeploymentManagerCreationException ("Cannot create weblogic DeploymentManager instance.");
            ErrorManager.getDefault().annotate(dmce, e);
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }
        throw dmce;
    }
    
    private DeploymentManager getDiscoDM(String uri) throws DeploymentManagerCreationException {
        DeploymentManagerCreationException dmce = null;
        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader loader = getWLClassLoader();
            Thread.currentThread().setContextClassLoader(loader);
            Class helperClazz = loader.loadClass("weblogic.deploy.api.tools.SessionHelper"); //NOI18N
            Method m = helperClazz.getDeclaredMethod("getDisconnectedDeploymentManager", new Class [] {});
            Object o = m.invoke(null, new Object [] {});
            if (DeploymentManager.class.isAssignableFrom(o.getClass())) {
                return (DeploymentManager) o;
            } else {
                dmce = new DeploymentManagerCreationException ("Instance created by weblogic is not disconnected DeploymentManager instance.");
            }
        } catch (Exception e) {
            dmce = new DeploymentManagerCreationException ("Cannot create weblogic disconnected DeploymentManager instance.");
            ErrorManager.getDefault().annotate(dmce, e);
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }
        throw dmce;
    }
    
    public boolean handlesURI(String uri) {
        if (uri != null && uri.startsWith(URI_PREFIX)) {
            return true;
        }
        
        return false;
    }
    
    public DeploymentManager getDeploymentManager(String uri, String username, 
            String password) throws DeploymentManagerCreationException {
        if (WLDebug.isEnabled()) {
            WLDebug.notify(WLDeploymentFactory.class, "getDeploymentManager, uri:" + uri+" username:" + username+" password:"+password);
        }
        String[] parts = uri.split(":");                               // NOI18N
        String host = parts[3].substring(2);
        String port = parts[4];
        return new WLDeploymentManager(getDM (uri, username, password, host, port), uri, username, password, host, port);
    }
    
    public DeploymentManager getDisconnectedDeploymentManager(String uri) 
            throws DeploymentManagerCreationException {
        if (WLDebug.isEnabled()) {
            WLDebug.notify(WLDeploymentFactory.class, "getDisconnectedDeploymentManager, uri:" + uri);
        }
        String[] parts = uri.split(":");                               // NOI18N
        String host = parts[3].substring(2);
        String port = parts[4];
        return new WLDeploymentManager(getDiscoDM(uri), uri, host, port);
    }
    
    public String getProductVersion() {
        return NbBundle.getMessage(WLDeploymentFactory.class, 
                "TXT_productVersion");                                  // NOI18N
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(WLDeploymentFactory.class, 
                "TXT_displayName");                                    // NOI18N
    }
}
