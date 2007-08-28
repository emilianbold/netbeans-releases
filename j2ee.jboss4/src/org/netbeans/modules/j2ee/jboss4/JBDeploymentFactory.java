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

import java.util.HashMap;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginUtils;
import org.openide.util.NbBundle;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;

/**
 *
 * @author Kirill Sorokin
 */
public class JBDeploymentFactory implements DeploymentFactory {
    
    public static final String URI_PREFIX = "jboss-deployer:"; // NOI18N
    
    private static final String DISCONNECTED_URI = "jboss-deployer:http://localhost:8080&"; // NOI18N
    
    private static JBDeploymentFactory instance;
    
    public static synchronized DeploymentFactory create() {
        if (instance == null) {
            instance = new JBDeploymentFactory();
            DeploymentFactoryManager.getInstance().registerDeploymentFactory(instance);

            registerDefaultServerInstance();
        }
        
        return instance;
    }
    
//    private DeploymentFactory jbossFactory = null;
    /**
     * Mapping of a server installation directory to a deployment factory
     */
    private HashMap/*<String, DeploymentFactory*/ jbossFactories = new HashMap();
    
    public static class JBClassLoader extends URLClassLoader {

        public JBClassLoader(URL[] urls, ClassLoader parent) throws MalformedURLException, RuntimeException {
            super(urls, parent);
        }
        
        protected PermissionCollection getPermissions(CodeSource codeSource) {
            Permissions p = new Permissions();
            p.add(new AllPermission());
            return p;
        }
        
       public Enumeration<URL> getResources(String name) throws IOException {
           // get rid of annoying warnings
           if (name.indexOf("jndi.properties") != -1) {// || name.indexOf("i18n_user.properties") != -1) { // NOI18N
               return Collections.enumeration(Collections.<URL>emptyList());
           } 

           return super.getResources(name);
       }         
    }

    public static URLClassLoader getJBClassLoader(String serverRoot, String domainRoot) {
        try {
            // dom4j.jar library for JBoss Application Server 4.0.4 and lower and JBoss Application Server 5.0
            File dom404 = new File(serverRoot + "/lib/dom4j.jar"); // NOI18N

            // dom4j.jar library for JBoss Application Server 4.0.5
            File dom405 = new File(domainRoot + "/lib/dom4j.jar"); // NOI18N

            // jboss-common-client.jar JBoss Application Server 4.x
            File client40 = new File(serverRoot + "/client/jboss-common-client.jar"); // NOI18N

            // jboss-client.jar JBoss Application Server 5.0
            File client50 = new File(serverRoot + "/client/jboss-client.jar"); // NOI18N

            // jboss-common-core.jar for JBoss Application Server 5.0
            File core50 = new File(serverRoot + "/client/jboss-common-core.jar"); // NOI18N

            // jboss-logging-spi.jar for JBoss Application Server 5.0
            File logging50 = new File(serverRoot + "/client/jboss-logging-spi.jar"); // NOI18N

            List<URL> urlList = new ArrayList<URL>();
            
            urlList.add(new File(serverRoot + "/client/jbossall-client.jar").toURI().toURL());      //NOI18N
            urlList.add(new File(serverRoot + "/client/jboss-deployment.jar").toURI().toURL());     //NOI18N
            urlList.add(new File(serverRoot + "/client/jnp-client.jar").toURI().toURL());           //NOI18N
            urlList.add((dom404.exists()) ? (dom404.toURI().toURL()) : (dom405.toURI().toURL()));   //NOI18N

            if(client40.exists())
                urlList.add(client40.toURI().toURL());

            if(client50.exists())
                urlList.add(client50.toURI().toURL());

            if(core50.exists())
                urlList.add(core50.toURI().toURL());

            if(logging50.exists())
                urlList.add(logging50.toURI().toURL());
            
            URLClassLoader loader = new JBClassLoader(urlList.toArray(new URL[] {}), JBDeploymentFactory.class.getClassLoader());
            return loader;
        } catch (Exception e) {
            Logger.getLogger("global").log(Level.WARNING, null, e);
        }
        return null;
    }
    
    public DeploymentFactory getFactory(String instanceURL) {
        DeploymentFactory jbossFactory = null;
        try {
            String jbossRoot = InstanceProperties.getInstanceProperties(instanceURL).
                                    getProperty(JBPluginProperties.PROPERTY_ROOT_DIR);
            
            String domainRoot = InstanceProperties.getInstanceProperties(instanceURL).
                                    getProperty(JBPluginProperties.PROPERTY_SERVER_DIR);
            
            // if jbossRoot is null, then we are in a server instance registration process, thus this call
            // is made from InstanceProperties creation -> JBPluginProperties singleton contains 
            // install location of the instance being registered
            if (jbossRoot == null)
                jbossRoot = JBPluginProperties.getInstance().getInstallLocation();
            
            // if domainRoot is null, then we are in a server instance registration process, thus this call
            // is made from InstanceProperties creation -> JBPluginProperties singleton contains 
            // install location of the instance being registered
            if (domainRoot == null)
                domainRoot = JBPluginProperties.getInstance().getDomainLocation();
            
            jbossFactory = (DeploymentFactory) jbossFactories.get(jbossRoot);
            if ( jbossFactory == null ) {
                URLClassLoader loader = getJBClassLoader(jbossRoot, domainRoot);
                jbossFactory = (DeploymentFactory) loader.loadClass("org.jboss.deployment.spi.factories.DeploymentFactoryImpl").newInstance();//NOI18N
                
                jbossFactories.put(jbossRoot, jbossFactory);
            }
        } catch (Exception e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }

        return jbossFactory;
    }
    
    public boolean handlesURI(String uri) {
        if (uri != null && uri.startsWith(URI_PREFIX)) {
            return true;
        }
        
        return false;
    }
    
    public DeploymentManager getDeploymentManager(String uri, String uname, String passwd) throws DeploymentManagerCreationException {
        if (!handlesURI(uri)) {
            throw new DeploymentManagerCreationException(NbBundle.getMessage(JBDeploymentFactory.class, "MSG_INVALID_URI", uri)); // NOI18N
        }
        
        DeploymentFactory df = getFactory(uri);
        if (df == null)
            throw new DeploymentManagerCreationException(NbBundle.getMessage(JBDeploymentFactory.class, "MSG_ERROR_CREATING_DM", uri)); // NOI18N

        String jbURI = uri;
        try {
            jbURI = uri.substring(0, uri.indexOf("&")); // NOI18N
        }
        catch (Exception e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }
        
        return new JBDeploymentManager(df.getDeploymentManager(jbURI, uname, passwd), uri, uname, passwd);
    }
     
    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        if (!handlesURI(uri)) {
            throw new DeploymentManagerCreationException(NbBundle.getMessage(JBDeploymentFactory.class, "MSG_INVALID_URI", uri)); // NOI18N
        }
        
        DeploymentFactory df = null;
        InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);
        if (ip == null) {
            // null ip either means that the instance is not registered, or that this is the disconnected URL
            if (!DISCONNECTED_URI.equals(uri)) {
                throw new DeploymentManagerCreationException("JBoss instance " + uri + " is not registered in the IDE."); // NOI18N
            }
        }
        else {
            df = getFactory(uri);
            if (df == null) {
                throw new DeploymentManagerCreationException(NbBundle.getMessage(JBDeploymentFactory.class, "MSG_ERROR_CREATING_DM", uri)); // NOI18N
            }
        }

        String jbURI = uri;
        try {
            jbURI = uri.substring(0, uri.indexOf("&")); // NOI18N
        }
        catch (Exception e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }

        return new JBDeploymentManager((df != null ? df.getDisconnectedDeploymentManager(jbURI) : null), uri, null, null);
    }
    
    public String getProductVersion() {
       
        return NbBundle.getMessage (JBDeploymentFactory.class, "LBL_JBossFactoryVersion");
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(JBDeploymentFactory.class, "SERVER_NAME"); // NOI18N
    }
    
    private static final String INSTALL_ROOT_PROP_NAME = "org.netbeans.modules.j2ee.jboss4.installRoot"; // NOI18N

    private static void registerDefaultServerInstance() {
        try {
            FileObject serverInstanceDir = getServerInstanceDir();
            String serverLocation = getDefaultInstallLocation();
            String domainLocation = serverLocation + File.separator + "server" + File.separator + "default"; // NOI18N
            setRemovability(serverInstanceDir, domainLocation);
            if (JBPluginUtils.isGoodJBServerLocation4x(new File(serverLocation))    &&
                JBPluginUtils.isGoodJBInstanceLocation4x(new File(domainLocation))  ||
                JBPluginUtils.isGoodJBServerLocation5x(new File(serverLocation))    &&
                JBPluginUtils.isGoodJBInstanceLocation5x(new File(domainLocation)))
            {
                if (!isAlreadyRegistered(serverInstanceDir, domainLocation)) {
                    String host = "localhost"; // NOI18N
                    String port = JBPluginUtils.getHTTPConnectorPort(domainLocation); // NOI18N
                    register(serverInstanceDir, serverLocation, domainLocation, host, port);
                }
            }
        }
        catch (IOException ioe) {
            Logger.getLogger("global").log(Level.SEVERE, ioe.getMessage());
        }
    }
    
    private static String getDefaultInstallLocation() {
        String installRoot = System.getProperty(INSTALL_ROOT_PROP_NAME);
        if (installRoot != null && new File(installRoot).exists()) {
            return installRoot;
        }
        
        return "";
    }
    
    private static boolean isAlreadyRegistered(FileObject serverInstanceDir, String domainLocation) throws IOException {
        String domainLocationCan = new File(domainLocation).getCanonicalPath();
        for (FileObject instanceFO : serverInstanceDir.getChildren()) {
            String installedLocation = (String)instanceFO.getAttribute(JBPluginProperties.PROPERTY_SERVER_DIR);
            if (installedLocation != null) {
                String installedLocationCan = new File(installedLocation).getCanonicalPath();
                if (domainLocationCan.equals(installedLocationCan)) {
                    return true; // do not overwrite registered instance
                }
            }
        }
        
        return false;
    }

    private static void setRemovability(FileObject serverInstanceDir, String domainLocation) throws IOException {
        String domainLocationCan = new File(domainLocation).getCanonicalPath();
        for (FileObject instanceFO : serverInstanceDir.getChildren()) {
            String url = (String)instanceFO.getAttribute(InstanceProperties.URL_ATTR);
            if (url == null) { // can occur if some unxpected file is in the directory
                Logger.getLogger("global").log(Level.INFO, "No server URL in " + FileUtil.getFileDisplayName(instanceFO));
            } else if (url.startsWith(URI_PREFIX)) { // it's JBoss instance
                String installedLocation = (String)instanceFO.getAttribute(JBPluginProperties.PROPERTY_SERVER_DIR);
                String installedLocationCan = new File(installedLocation).getCanonicalPath();
                if (domainLocationCan.equals(installedLocationCan)) {
                    instanceFO.setAttribute(InstanceProperties.REMOVE_FORBIDDEN, Boolean.TRUE);
                }
                else {
                    if (instanceFO.getAttribute(InstanceProperties.REMOVE_FORBIDDEN) != null) {
                        instanceFO.setAttribute(InstanceProperties.REMOVE_FORBIDDEN, Boolean.FALSE);
                    }
                }
            }
        }
    }

    private static void register(FileObject serverInstanceDir, String serverLocation, String domainLocation, String host, String port) throws IOException {
        String displayName = generateDisplayName(serverInstanceDir);
        
        String url = URI_PREFIX + host + ":" + port + "#default&" + serverLocation;    // NOI18N
      
        String name = FileUtil.findFreeFileName(serverInstanceDir, "instance", null); // NOI18N
        FileObject instanceFO = serverInstanceDir.createData(name);

        instanceFO.setAttribute(InstanceProperties.URL_ATTR, url);
        instanceFO.setAttribute(InstanceProperties.USERNAME_ATTR, "");
        instanceFO.setAttribute(InstanceProperties.PASSWORD_ATTR, "");
        instanceFO.setAttribute(InstanceProperties.DISPLAY_NAME_ATTR, displayName);
        instanceFO.setAttribute(InstanceProperties.REMOVE_FORBIDDEN, "true");

        instanceFO.setAttribute(JBPluginProperties.PROPERTY_SERVER, "default"); // NOI18N
        String deployDir = JBPluginUtils.getDeployDir(domainLocation);
        instanceFO.setAttribute(JBPluginProperties.PROPERTY_DEPLOY_DIR, deployDir);
        instanceFO.setAttribute(JBPluginProperties.PROPERTY_SERVER_DIR, domainLocation);
        instanceFO.setAttribute(JBPluginProperties.PROPERTY_ROOT_DIR, serverLocation);
        instanceFO.setAttribute(JBPluginProperties.PROPERTY_HOST, host);
        instanceFO.setAttribute(JBPluginProperties.PROPERTY_PORT, port);
    }

    private static FileObject getServerInstanceDir() {
        Repository rep = (Repository)Lookup.getDefault().lookup(Repository.class);
        FileObject dir = rep.getDefaultFileSystem().findResource("/J2EE/InstalledServers"); // NOI18N
        return dir;
    }
    
    private static String generateDisplayName(FileObject serverInstanceDir) {
        final String serverName = NbBundle.getMessage(JBDeploymentFactory.class, "SERVER_NAME"); // NOI18N
        
        String instanceName = serverName;
        int counter = 1;
        Set<String> registeredInstances = getServerInstancesNames(serverInstanceDir);
        
        while (registeredInstances.contains(instanceName.toUpperCase())) {
            instanceName = serverName  + " (" + String.valueOf(counter++) + ")";
        }
        
        return instanceName;
    }

    private static Set<String> getServerInstancesNames(FileObject serverInstanceDir) {
        Set<String> names = new HashSet<String>();
        for (FileObject instanceFO : serverInstanceDir.getChildren()) {
            String instanceName = (String)instanceFO.getAttribute(InstanceProperties.DISPLAY_NAME_ATTR);
            names.add(instanceName.toUpperCase());
        }
        
        return names;
    }
    
}

