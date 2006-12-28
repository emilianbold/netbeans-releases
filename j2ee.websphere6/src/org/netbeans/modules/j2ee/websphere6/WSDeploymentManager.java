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
package org.netbeans.modules.j2ee.websphere6;

import java.io.*;
import java.util.*;
import java.util.jar.JarFile;

import javax.enterprise.deploy.model.*;
import javax.enterprise.deploy.shared.*;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.exceptions.*;
import javax.enterprise.deploy.spi.factories.*;
import javax.enterprise.deploy.spi.status.*;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.netbeans.modules.j2ee.websphere6.config.*;

import org.openide.*;
import org.openide.util.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;

import org.netbeans.modules.j2ee.websphere6.util.WSDebug;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.zip.ZipEntry;

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
     * Temporary hack for storing webmodule`s context path in EntApplication
     */
    private static String webUrlInEntApp = "";
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
     * Returns the server password stored in the instance properties
     */
    public String getPassword() {
        return getInstanceProperties().getProperty(WSDeploymentFactory.PASSWORD_ATTR);
        
    }
    
    /**
     * Returns the server username stored in the instance properties
     */
    public String getUsername() {
        return getInstanceProperties().getProperty(WSDeploymentFactory.USERNAME_ATTR);
        
    }
    
    
    /**
     * Returns the server port stored in the instance properties
     */
    public String getPort() {
        return getInstanceProperties().getProperty(
                WSDeploymentFactory.PORT_ATTR);
    }
    public String getAdminPort() {
        return getInstanceProperties().getProperty(
                WSDeploymentFactory.ADMIN_PORT_ATTR);
    }
    public String getDefaultHostPort() {
        return getInstanceProperties().getProperty(
                WSDeploymentFactory.DEFAULT_HOST_PORT_ATTR);
    }
    /**
     * Returns the server installation directory
     */
    public String getServerRoot() {
        return (getInstanceProperties()!=null)?getInstanceProperties().getProperty(
                WSDeploymentFactory.SERVER_ROOT_ATTR):"";
    }
    
    /**
     * Returns the profile root directory
     */
    public String getDomainRoot() {
        return (getInstanceProperties()!=null)?getInstanceProperties().getProperty(
                WSDeploymentFactory.DOMAIN_ROOT_ATTR):"";
    }
    
    public String getLogFilePath() {
        return getDomainRoot() + File.separator + "logs" +  // NOI18N
                File.separator +
                getInstanceProperties().getProperty(
                WSDeploymentFactory.SERVER_NAME_ATTR) + File.separator +
                "trace.log"; // NOI18N
    }
    
    /**
     * Returns true if the type of server is local. Otherwise return false
     */
    public String getIsLocal() {
        return (getInstanceProperties()!=null)?getInstanceProperties().getProperty(
                WSDeploymentFactory.IS_LOCAL_ATTR):"";
    }
    
    /**
     * Set server root property
     */
    public void setServerRoot(String serverRoot) {
        if(getInstanceProperties()!=null)
            getInstanceProperties().setProperty(WSDeploymentFactory.SERVER_ROOT_ATTR,serverRoot);
    }
    
    /**
     * Set domain root property
     */
    public void setDomainRoot(String domainRoot) {
        if(getInstanceProperties()!=null)
            getInstanceProperties().setProperty(WSDeploymentFactory.DOMAIN_ROOT_ATTR,domainRoot);
    }
    
    /**
     * Set host property
     */
    public void setHost(String host) {
        if(getInstanceProperties()!=null)   {
            getInstanceProperties().setProperty(WSDeploymentFactory.HOST_ATTR,host);
        }
    }
    
    /**
     * Set port property
     */
    public void setPort(String port) {
        if(getInstanceProperties()!=null){
            getInstanceProperties().setProperty(WSDeploymentFactory.PORT_ATTR,port);
        }
    }
    
    
    /**
     * Set admin port property
     */
    public void setAdminPort(String adminPort) {
        if(getInstanceProperties()!=null){
            getInstanceProperties().setProperty(WSDeploymentFactory.ADMIN_PORT_ATTR,adminPort);
        }
    }
    
    /**
     * Set admin port property
     */
    public void setDefaultHostPort(String defaultHostPort) {
        if(getInstanceProperties()!=null){
            getInstanceProperties().setProperty(WSDeploymentFactory.DEFAULT_HOST_PORT_ATTR,defaultHostPort);
        }
    }
    /**
     * Set password property
     */
    public void setPassword(String password) {
        if(getInstanceProperties()!=null)
            getInstanceProperties().setProperty(WSDeploymentFactory.PASSWORD_ATTR,password);
    }
    
    /**
     * Set username property
     */
    public void setUsername(String username) {
        if(getInstanceProperties()!=null)
            getInstanceProperties().setProperty(WSDeploymentFactory.USERNAME_ATTR,username);
    }
    
    /**
     * Set Server Name
     */
    public void setServerName(String name) {
        if(getInstanceProperties()!=null)
            getInstanceProperties().setProperty(WSDeploymentFactory.SERVER_NAME_ATTR,name);
    }
    
    /**
     * Set .xml configuration file path
     */
    public void setConfigXmlPath(String path) {
        if(getInstanceProperties()!=null)
            getInstanceProperties().setProperty(WSDeploymentFactory.CONFIG_XML_PATH,path);
    }
    
    /**
     * Set local/remote type of server
     */
    public void setIsLocal(String isLocal) {
        if(getInstanceProperties()!=null)
            getInstanceProperties().setProperty(WSDeploymentFactory.IS_LOCAL_ATTR,isLocal);
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
            
            String serverProp=getServerRoot();
            
            String domainProp=getDomainRoot();
            
            loader = WSClassLoader.getInstance(serverProp,domainProp);
            
            // update the context classloader
            loader.updateLoader();
            
            // load the factory class and instantiate it
            try {
                factory = (DeploymentFactory) loader.loadClass(
                        "com.ibm.ws.management.application.j2ee." +    // NOI18N
                        "deploy.spi.factories.DeploymentFactoryImpl"). // NOI18N
                        newInstance();
            } catch (ClassNotFoundException e) {
                //ErrorManager.getDefault().notify(ErrorManager.ERROR, e);
                e=null;
                // do nothing. Fix for issue with Exception on IDE restarting
                // after removing server from Runtime with opened projects
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
            
            if(factory!=null) {
                // try to get a connected deployment manager
                dm = factory.getDeploymentManager(uri, username, password);
                
                // set the connected marker
                isConnected = true;
            }
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
    
    public boolean getIsConnected() {
        return isConnected;
    }
    public String getServerTitleMessage() {
        String title = "[" + getInstanceProperties(). // NOI18N
                getProperty(WSDeploymentFactory.SERVER_NAME_ATTR) + ", "+
                getInstanceProperties().
                getProperty(WSDeploymentFactory.HOST_ATTR) + ":"+
                getInstanceProperties().
                getProperty(WSDeploymentFactory.PORT_ATTR)+ "]";
        return title;
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
        //try {
        webUrlInEntApp = null;
        
        if(file.exists() && file.getPath().endsWith(".ear")) {
            // try to get WebApplication context path from EAR archive
            JarFile jarFile = null;
            ZipEntry ze= null;
            InputStream is = null;
            ByteArrayOutputStream os = null;
            try {
                jarFile = new JarFile(file);
                ze = jarFile.getEntry("META-INF/application.xml");
                is = jarFile.getInputStream(ze);
                os = new ByteArrayOutputStream();
                int n=0;
                byte[] buf = new byte[1024];
                while ((n = is.read(buf, 0, 1024)) > -1) {
                    os.write(buf, 0, n);
                }
                String content = os.toString();
                String token = "<context-root>";
                int startIndex = content.indexOf(token);
                int endIndex = content.indexOf("</context-root>");
                if (startIndex != -1 && endIndex != -1) {
                    webUrlInEntApp = content.substring(startIndex + token.length(),
                            endIndex);
                }
            } catch (IOException ex) {
                //do nothing.. strange case
            } finally {
                try {
                    if(jarFile!=null) jarFile.close();
                } catch(IOException ex) {
                } finally {
                    try {
                        if(is != null) is.close();
                    } catch(IOException ex) {
                    } finally {
                        try {
                            if(os != null) is.close();
                        } catch(IOException ex) {
                        }
                    }
                }
            }
            
        }
        
        //}
        
        try {
            // delegate the call and return the result
            ProgressObject po = dm.distribute(target, file, file2);
            
            return po;
            
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
        ModuleType type = deployableObject.getType();
        try {
            if(dm!=null) {
                InstanceProperties ip=getInstanceProperties();
                if (type == ModuleType.WAR) {
                    return new WarDeploymentConfiguration(dm, deployableObject,ip);
                } else if (type == ModuleType.EAR) {
                    if(deployableObject instanceof J2eeApplicationObject) {
                        return new EarDeploymentConfiguration(dm, (J2eeApplicationObject)deployableObject,ip);
                    }
                    return new EarDeploymentConfiguration(dm, deployableObject,ip);
                } else if (type == ModuleType.EJB) {
                    return new EjbDeploymentConfiguration(dm, deployableObject,ip);
                } else {
                    throw new InvalidModuleException("Unsupported module type: " + type.toString()); // NOI18N
                }
            }
        } finally {
            // restore the context classloader
            loader.restoreLoader();
        }
        return null;
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
        
        String webUrl=null;
        try {
            // if WebSphere return null for getWebURL, then set it to the right value
            Method method = targetModuleID[0].getClass().
                    getMethod("getWebURL", new Class[0]);            // NOI18N
            webUrl = (String) method.
                    invoke(targetModuleID[0], new Object[0]);
            
            if (webUrlInEntApp!=null && targetModuleID[0].getModuleID().contains("type=Application")) {
                TargetModuleID [] tmids = targetModuleID;
                if (tmids.length==1 &&
                        tmids[0].getModuleID().contains("type=Application") &&
                        tmids[0].getChildTargetModuleID()==null) {
                    try {
                        TargetModuleID child = (TargetModuleID) loader.loadClass(
                                "com.ibm.ws.management.application.j2ee.deploy.spi.TargetModuleIDImpl").
                                newInstance();
                        
                        //set target server
                        method = child.getClass().
                                getMethod("setTarget", new Class[] {Target.class});  // NOI18N
                        method.invoke(child, new Object[] {tmids[0].getTarget()});
                        
                        
                        // set parent
                        method = child.getClass().
                                getMethod("setParentTargetModuleID", new Class[] {TargetModuleID.class});  // NOI18N
                        method.invoke(child, new Object[] {tmids[0]});
                        
                        
                        // set object name
                        String id = "WebSphere:name=" + webUrlInEntApp.substring(1) + ",type=WebModule";
                        String oname = id + ",*";
                        ObjectName on = new ObjectName(oname);
                        method = child.getClass().
                                getMethod("setObjectName", new Class[] {ObjectName.class} );  // NOI18N
                        method.invoke(child, new Object [] {on});
                        
                        
                        //set module id
                        method = child.getClass().
                                getMethod("setModuleID", new Class[] {String.class});  // NOI18N
                        method.invoke(child, new Object [] {id});
                        
                        //set type
                        method = child.getClass().
                                getMethod("setModuleType", new Class[] {String.class});  // NOI18N
                        String type = ModuleType.WAR.toString();
                        method.invoke(child, new Object[] {type});
                        
                        //set startable
                        
                        method = child.getClass().
                                getMethod("setStartable", new Class[] {boolean.class});  // NOI18N
                        method.invoke(child, new Object[] {true});
                        
                        //set weburl
                        String port=getDefaultHostPort();
                        String host=getHost();
                        if(uri.indexOf(WSURIManager.WSURI)!=-1) {
                            host=uri.split(":")[2];
                        }
                        webUrl="http://" + host + ":" + port; // NOI18N
                        if (webUrlInEntApp != null) {
                            webUrl = webUrl + webUrlInEntApp;
                            webUrlInEntApp = null;
                        } else {
                            System.out.println(
                                    NbBundle.getMessage(WSDeploymentManager.class,
                                    "ERR_wrongContextRoot"));
                        }
                        method = child.getClass().
                                getMethod("setWebURL", new Class[] {String.class});  // NOI18N
                        method.invoke(child, new Object[] {webUrl});
                        
                        method = tmids[0].getClass().
                                getMethod("setChildTargetModuleID", new Class[] {TargetModuleID[].class});  // NOI18N
                        method.invoke(tmids[0], new Object[] {new TargetModuleID[] {child}});
                        
                    } catch (ClassNotFoundException ex) {
                        ex=null;//do nothing
                    } catch (InstantiationException ex) {
                        ex=null;//do nothing
                    } catch (IllegalAccessException ex) {
                        ex=null;//do nothing
                    } catch (NoSuchMethodException ex) {
                        ex=null;//do nothing
                    } catch (InvocationTargetException ex) {
                        ex=null;//do nothing
                    }  catch (NullPointerException ex) {
                        ex=null;//do nothing
                    } catch (MalformedObjectNameException ex) {
                        ex=null;//do nothing
                    }
                }
            }
            
            
            if(targetModuleID[0].getModuleID().contains("type=WebModule") /*||
                    targetModuleID[0].getModuleID().contains("type=Application")*/) {
                if(webUrl==null) {
                    String port=getDefaultHostPort();
                    String host=getHost();
                    if(uri.indexOf(WSURIManager.WSURI)!=-1) {
                        host=uri.split(":")[2];
                    }
                    webUrl="http://" + host + ":" + port; // NOI18N
                    if (webUrlInEntApp != null) {
                        webUrl = webUrl + webUrlInEntApp;
                        webUrlInEntApp = null;
                    } else {
                        System.out.println(
                                NbBundle.getMessage(WSDeploymentManager.class,
                                "ERR_wrongContextRoot"));
                    }
                    method = targetModuleID[0].getClass().
                            getMethod("setWebURL", new Class[] {String.class});  // NOI18N
                    method.invoke(targetModuleID[0], new Object[] {webUrl});
                    
                    method = targetModuleID[0].getClass().
                            getMethod("getWebURL", new Class[0] );  // NOI18N
                    String webUrlAfter = (String) method.
                            invoke(targetModuleID[0], new Object[0]);
                    webUrlAfter = webUrlAfter + "";
                }
                
                /* Commented as it doesn`t work properly
                try { // stop running web modules
                 
                    TargetModuleID [] modules = getRunningModules(
                            ModuleType.WAR,
                            new Target[] {targetModuleID[0].getTarget()});
                    TargetModuleID [] amodules = getAvailableModules(
                            ModuleType.WAR,
                            new Target[] {targetModuleID[0].getTarget()});
                 
                 
                    for(int i=0;i<modules.length;i++) {
                        if(modules[i].getWebURL()==null || webUrl.equals(modules[i].getWebURL())) {
                            //stop(new TargetModuleID[] {modules[i]} );
                        }
                    }
                } catch(TargetException ex) {
                    ;//do nothing..
                } */
            }
            
        }  catch (IllegalAccessException e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        } catch (NoSuchMethodException e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        } catch (InvocationTargetException e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        } catch (ArrayIndexOutOfBoundsException e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        }
        
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
            TargetModuleID[] am = dm.getAvailableModules(moduleType, target);
            return am;
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
            TargetModuleID [] nrm = dm.getNonRunningModules(moduleType, target);
            return nrm;
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
            TargetModuleID [] rm = dm.getRunningModules(moduleType, target);
            return rm;
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