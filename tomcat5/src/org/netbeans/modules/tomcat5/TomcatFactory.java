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

package org.netbeans.modules.tomcat5;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.tomcat5.TomcatManager.TomcatVersion;
import org.netbeans.modules.tomcat5.util.TomcatInstallUtil;
import org.netbeans.modules.tomcat5.util.TomcatProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/** 
 * Factory capable to create DeploymentManager that can deploy to Tomcat 5 and 6.
 *
 * Tomcat URI has following format:
 * <PRE><CODE>tomcat[55|60]:[home=&lt;home_path&gt;:[base=&lt;base_path&gt;:]]&lt;manager_app_url&gt;</CODE></PRE>
 * for example
 * <PRE><CODE>tomcat:http://localhost:8080/manager/</CODE></PRE>
 * where paths values will be used as CATALINA_HOME and CATALINA_BASE properties and manager_app_url
 * denotes URL of manager application configured on this server and has to start with <CODE>http:</CODE>.
 * @author Radim Kubacki
 */
public final class TomcatFactory implements DeploymentFactory {
    
    public static final String SERVER_ID_50 = "Tomcat";     // NOI18N
    public static final String SERVER_ID_55 = "Tomcat55";   // NOI18N
    public static final String SERVER_ID_60 = "Tomcat60";   // NOI18N
    
    public static final String TOMCAT_URI_PREFIX_50 = "tomcat:";    // NOI18N
    public static final String TOMCAT_URI_PREFIX_55 = "tomcat55:";  // NOI18N
    public static final String TOMCAT_URI_PREFIX_60 = "tomcat60:";  // NOI18N
    
    public static final String TOMCAT_URI_HOME_PREFIX = "home=";    // NOI18N
    public static final String TOMCAT_URI_BASE_PREFIX = ":base=";   // NOI18N

    private static final String DISCONNECTED_URI_50 = "tomcat:jakarta-tomcat-5.0.x";    // NOI18N
    private static final String DISCONNECTED_URI_55 = "tomcat55:jakarta-tomcat-5.5.x";  // NOI18N
    private static final String DISCONNECTED_URI_60 = "tomcat60:apache-tomcat-6.0.x";   // NOI18N
    
    private static TomcatFactory instance;
    private static TomcatFactory instance55;
    private static TomcatFactory instance60;
    
    private static final WeakHashMap managerCache = new WeakHashMap();
    
    private static Logger err = Logger.getLogger("org.netbeans.modules.tomcat5");  // NOI18N
    
    private final String tomcatUriPrefix;
    private final String disconnectedUri;
    private final TomcatVersion version;
    
    /**
     * System property pointing to the 'bundled' Tomcat catalina home directory.
     * This is used for example by installer to register the default Tomcat server 
     * instance.
     */
    private static final String PROP_CATALINA_HOME = "org.netbeans.modules.tomcat.autoregister.catalinaHome"; // NOI18N
    private static final String PROP_TOKEN = "org.netbeans.modules.tomcat.autoregister.token"; // NOI18N
    private static final String PROP_REMOVED_INSTANCE_TOKEN = "removed_instance_token"; // NOI18N
    
    static {
        autoregisterTomcatInstance();
    }
            
    private TomcatFactory(TomcatVersion version) {
        this.version = version;
        switch (version) {
            case TOMCAT_50 :
                tomcatUriPrefix = TOMCAT_URI_PREFIX_50;
                disconnectedUri = DISCONNECTED_URI_50;
                break;
            case TOMCAT_55 :
                tomcatUriPrefix = TOMCAT_URI_PREFIX_55;
                disconnectedUri = DISCONNECTED_URI_55;
                break;
            case TOMCAT_60 :
            default:
                tomcatUriPrefix = TOMCAT_URI_PREFIX_60;
                disconnectedUri = DISCONNECTED_URI_60;
                break;
        }
    }
    
    /** 
     * Factory method to create DeploymentFactory for Tomcat 5.0.x
     */
    public static synchronized TomcatFactory create50() {
        if (instance == null) {
            if (err.isLoggable(Level.FINE)) err.log(Level.FINE, "Creating TomcatFactory"); // NOI18N
            instance = new TomcatFactory(TomcatVersion.TOMCAT_50);
            DeploymentFactoryManager.getInstance().registerDeploymentFactory(instance);
        }
        return instance;
    }
    
    /** 
     * Factory method to create DeploymentFactory for Tomcat 5.5.x
     */
    public static synchronized TomcatFactory create55() {
        if (instance55 == null) {
            if (err.isLoggable(Level.FINE)) err.log(Level.FINE, "Creating TomcatFactory"); // NOI18N
            instance55 = new TomcatFactory(TomcatVersion.TOMCAT_55);
            DeploymentFactoryManager.getInstance().registerDeploymentFactory(instance55);
        }
        return instance55;
    }
    
    /** 
     * Factory method to create DeploymentFactory for Tomcat 6.0.x
     */
    public static synchronized TomcatFactory create60() {
        if (instance60 == null) {
            if (err.isLoggable(Level.FINE)) err.log(Level.FINE, "Creating TomcatFactory"); // NOI18N
            instance60 = new TomcatFactory(TomcatVersion.TOMCAT_60);
            DeploymentFactoryManager.getInstance().registerDeploymentFactory(instance60);
        }
        return instance60;
    }
    
    /** Factory method to create DeploymentManager.
     * @param uri URL of configured manager application.
     * @param uname user with granted manager role
     * @param passwd user's password
     * @throws DeploymentManagerCreationException
     * @return {@link TomcatManager}
     */
    public synchronized DeploymentManager getDeploymentManager(String uri, String uname, String passwd) 
    throws DeploymentManagerCreationException {
        if (!handlesURI (uri)) {
            throw new DeploymentManagerCreationException ("Invalid URI:" + uri); // NOI18N
        }
        // Lets reuse the same instance of TomcatManager for each server instance
        // during the IDE session, j2eeserver does not ensure this. Without it,
        // however, we could not rely on keeping data in the member variables.
        InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);
        if (ip == null) {
            // null ip either means that the instance is not registered, or that this is the disconnected URL
            if (!disconnectedUri.equals(uri)) {
                throw new DeploymentManagerCreationException("Tomcat instance: " + uri + " is not registered in the IDE."); // NOI18N
            }
        }
        TomcatManager tm = (TomcatManager)managerCache.get(ip);
        if (tm == null) {
            try {
                tm = new TomcatManager(true, uri.substring(tomcatUriPrefix.length()), version);
                managerCache.put(ip, tm);
            } catch (IllegalArgumentException iae) {
                Throwable t = new DeploymentManagerCreationException("Cannot create deployment manager for Tomcat instance: " + uri + "."); // NOI18N
                throw (DeploymentManagerCreationException)(t.initCause(iae));
            }
        }
        return tm;
    }
    
    public DeploymentManager getDisconnectedDeploymentManager(String uri) 
    throws DeploymentManagerCreationException {
        // no need to distinguish beetween the connected and disconnected DM for Tomcat
        return getDeploymentManager(uri, null, null);
    }
    
    public String getDisplayName() {
        switch (version) {
            case TOMCAT_50 :
                return NbBundle.getMessage(TomcatFactory.class, "LBL_TomcatFactory");
            case TOMCAT_55 :
                return NbBundle.getMessage(TomcatFactory.class, "LBL_TomcatFactory55");
            case TOMCAT_60 :
            default:
                return NbBundle.getMessage(TomcatFactory.class, "LBL_TomcatFactory60");
        }
    }
    
    public String getProductVersion() {
        return NbBundle.getMessage(TomcatFactory.class, "LBL_TomcatFactoryVersion");
    }
    
    /**
     * @param str
     * @return <CODE>true</CODE> for URIs beggining with <CODE>tomcat[55|60]:</CODE> prefix
     */    
    public boolean handlesURI(String str) {
        return str != null && str.startsWith (tomcatUriPrefix);
    }
    
    /** 
     * Retrieve the tomcat version e.g. '6.0.10'
     * 
     * @throws IllegalStateException if the version information cannot be retrieved 
     */
    private static String getTomcatVersion(File catalinaHome) throws IllegalStateException {
        File catalinaJar = new File(catalinaHome, "lib/catalina.jar"); // NOI18N
        if (!catalinaJar.exists()) {
            catalinaJar = new File(catalinaHome, "server/lib/catalina.jar"); // NOI18N
        }
        try {
            URLClassLoader loader = new URLClassLoader(new URL[] { catalinaJar.toURL() });
            Class serverInfo = loader.loadClass("org.apache.catalina.util.ServerInfo"); // NOI18N
            Method method = serverInfo.getMethod("getServerInfo", new Class[] {}); // NOI18N
            String version = (String) method.invoke(serverInfo, new Object[] {});
            int idx = version.indexOf('/');
            if (idx > 0) {
                return version.substring(idx + 1);
            }
            throw new IllegalStateException("Cannot identify the version of the server."); // NOI18N
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
    
    /** 
     * Auto-registers Tomcat server instance defined by the {@link #PROP_CATALINA_HOME}
     * property.
     */
    private static void autoregisterTomcatInstance() {
        
        Repository repository = (Repository) Lookup.getDefault().lookup(Repository.class);
        FileObject serverInstanceDir = repository.getDefaultFileSystem().findResource("/J2EE/InstalledServers"); // NOI18N
        
        if (serverInstanceDir == null) {
            err.log(Level.INFO, "Cannot register the default Tomcat server.  The //J2EE//InstalledServers folder cannot be found."); // NOI18N
            return;
        }
        
        String catalinaHomeValue = System.getProperty(PROP_CATALINA_HOME);
        final String token = System.getProperty(PROP_TOKEN);
        
        // look up the auto-registgered server instance FO
        FileObject autoregInstanceFO = null;
        for (FileObject fo : serverInstanceDir.getChildren()) {
            if (Boolean.parseBoolean((String) fo.getAttribute(TomcatProperties.PROP_AUTOREGISTERED))) {
                autoregInstanceFO = fo;
                break;
            }
        }
        
        // if the system properties are no longer set, unregister if needed
        if (catalinaHomeValue == null || token == null) {
            if (autoregInstanceFO != null) {
                try {
                    autoregInstanceFO.delete();
                } catch (IOException e) {
                    err.log(Level.INFO, "The server " + autoregInstanceFO.getAttribute(InstanceProperties.URL_ATTR) + " cannot be uregistered."); // NOI18N
                    Exceptions.printStackTrace(e);
                }
            }
            return;
        }
        
        String removedToken = NbPreferences.forModule(TomcatFactory.class).get(PROP_REMOVED_INSTANCE_TOKEN, null);
        if (token.equals(removedToken)) {
            // this server instance has been already removed, do not proceed
            return;
        }
        
        File catalinaHome = new File(catalinaHomeValue);
        if (!catalinaHome.exists()) {
            err.log(Level.INFO, "Cannot register the default Tomcat server. " + "The Catalina Home directory " + catalinaHomeValue + // NOI18N
                    " passed through the " + PROP_CATALINA_HOME + " property does not exist."); // NOI18N
            return;
        }
        
        String version;
        try {
            version = getTomcatVersion(catalinaHome);
        } catch (IllegalStateException e) {
            err.log(Level.INFO, "Cannot register the default Tomcat server.  Cannot recognize the Tomcat version."); // NOI18N
            err.log(Level.INFO, null, e);
            return;
        }
        
        // build URL
        StringBuilder urlTmp;
        if (version.startsWith("5.0.")) { // NOI18N
            urlTmp = new StringBuilder(TOMCAT_URI_PREFIX_50);
        } else if (version.startsWith("5.5.")) { // NOI18N
            urlTmp = new StringBuilder(TOMCAT_URI_PREFIX_55);
        } else if (version.startsWith("6.")) { // NOI18N
            urlTmp = new StringBuilder(TOMCAT_URI_PREFIX_60);
        } else {
            err.log(Level.INFO, "Cannot register the default Tomcat server. " + " The version " + version + " is not supported."); // NOI18N
            return;
        }
        urlTmp.append(TOMCAT_URI_HOME_PREFIX);
        urlTmp.append(catalinaHomeValue);
        urlTmp.append(TOMCAT_URI_BASE_PREFIX);
        urlTmp.append("apache-tomcat-"); // NOI18N
        urlTmp.append(version);
        urlTmp.append("_base"); // NOI18N
        
        final String url = urlTmp.toString();
        
        // listen to server instance removals
        Deployment.getDefault().addInstanceListener(new InstanceListener() {
            public void changeDefaultInstance(String oldServerInstanceID, String newServerInstanceID) {
            }
            public void instanceAdded(String serverInstanceID) {
            }
            public void instanceRemoved(String serverInstanceID) {
                if (url.equals(serverInstanceID)) {
                    // the auto-registered instance was removed, remember it
                    NbPreferences.forModule(TomcatFactory.class).put(PROP_REMOVED_INSTANCE_TOKEN, token);
                }
            }
        });
        
        // make sure the server is not registered yet
        for (FileObject fo : serverInstanceDir.getChildren()) {
            if (url.equals(fo.getAttribute(InstanceProperties.URL_ATTR))) {
                // the server is already registered, do nothing
                return;
            }
        }
        
        // if the auto-registered instance has changed, removed the old one
        if (autoregInstanceFO != null && !url.equals(autoregInstanceFO.getAttribute(InstanceProperties.URL_ATTR))) {
            try {
                autoregInstanceFO.delete();
            } catch (IOException e) {
                err.log(Level.INFO, "The server " + autoregInstanceFO.getAttribute(InstanceProperties.URL_ATTR) + " cannot be uregistered."); // NOI18N
                Exceptions.printStackTrace(e);
            }
        }
        
        String displayName = generateUniqueDisplayName(serverInstanceDir, version);
        registerServerInstanceFO(serverInstanceDir, url, displayName);
    }
    
    /**
     * Generates a unique display name for the specified version of Tomcat
     * 
     * @param serverInstanceDir /J2EE/InstalledServers folder
     * @param version Tomcat version
     * 
     * @return a unique display name for the specified version of Tomcat
     */
    private static String generateUniqueDisplayName(FileObject serverInstanceDir, String version) {
        // find a unique display name
        String displayName = NbBundle.getMessage(TomcatFactory.class, "LBL_ApacheTomcat", version);
        boolean unique = true;
        int i = 1;
        while (true) {
            for (FileObject fo : serverInstanceDir.getChildren()) {
                if (displayName.equals(fo.getAttribute(InstanceProperties.DISPLAY_NAME_ATTR))) {
                    // there is already some server of the same name
                    unique = false;
                    break;
                }
            }
            if (unique) {
                break;
            }
            displayName = NbBundle.getMessage(TomcatFactory.class, "LBL_ApacheTomcatAlt", version, i++);
            unique = true;
        };
        return displayName;
    }
    
    /** 
     * Registers the server instance file object and set the default properties.
     * 
     * @param serverInstanceDir /J2EE/InstalledServers folder
     * @param url server instance url/ID
     * @param displayName display name
     */
    private static void registerServerInstanceFO(FileObject serverInstanceDir, String url, String displayName) {
        String name = FileUtil.findFreeFileName(serverInstanceDir, "tomcat_autoregistered_instance", null); // NOI18N
        FileObject instanceFO;
        try {
            instanceFO = serverInstanceDir.createData(name);
            instanceFO.setAttribute(InstanceProperties.URL_ATTR, url);
            instanceFO.setAttribute(InstanceProperties.USERNAME_ATTR, "ide"); // NOI18N
            String password = TomcatInstallUtil.generatePassword(8);
            instanceFO.setAttribute(InstanceProperties.PASSWORD_ATTR, password);
            instanceFO.setAttribute(InstanceProperties.DISPLAY_NAME_ATTR, displayName);
            instanceFO.setAttribute(InstanceProperties.HTTP_PORT_NUMBER, "8084"); // NOI18N
            instanceFO.setAttribute(TomcatProperties.PROP_SHUTDOWN, "8025"); // NOI18N
            instanceFO.setAttribute(TomcatProperties.PROP_MONITOR, "true"); // NOI18N
            instanceFO.setAttribute(TomcatManager.PROP_BUNDLED_TOMCAT, "true"); // NOI18N
            instanceFO.setAttribute(TomcatProperties.PROP_AUTOREGISTERED, "true"); // NOI18N
        } catch (IOException e) {
            err.log(Level.INFO, "Cannot register the default Tomcat server."); // NOI18N
            err.log(Level.INFO, null, e);
        }        
    }
}
