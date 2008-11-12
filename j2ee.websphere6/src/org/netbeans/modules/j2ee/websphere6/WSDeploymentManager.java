/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.j2ee.websphere6;

import java.beans.PropertyVetoException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressListener;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.Arrays;
import java.util.Locale;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.status.ProgressObject;

import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
import org.openide.util.NbBundle;

/**
 * Main class of the deployment process. This serves a a wrapper for the
 * server's DeploymentManager implementation, all calls are delegated to the
 * server's implementation, with the thread's context classloader updated
 * if necessary.
 *
 * @author Kirill Sorokin
 * @author Petr Hejl
 * @author Arathi
 */
@SuppressWarnings("deprecation")
public class WSDeploymentManager implements DeploymentManager {

    private static final Logger LOGGER = Logger.getLogger(WSDeploymentManager.class.getName());

    private final WSVersion wsVersion;

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
    public WSDeploymentManager(String uri, String username, String password, WSVersion wsVersion) {
        assert wsVersion != null : "Version must not be null"; // NOI18N

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "WSDeploymentManager(" + uri + ", " + username + ", " + password + ")"); // NOI18N
        }

        // save the connection properties
        this.uri = uri;
        this.username = username;
        this.password = password;
        this.wsVersion = wsVersion;
    }

    /**
     * Creates a new instance of the deployment manager
     *
     * @param uri the server's URI
     */
    public WSDeploymentManager(String uri, WSVersion version) {
        this(uri, null, null, version);
    }

    public WSVersion getVersion() {
        return wsVersion;
    }


    /**
     * Parses the URI and stores the parsed URI in the instance properties
     * object
     */
    private void parseUri() {
        // split the uri
        String[] parts = WSURIManager.getUrlWithoutPrefix(uri).split(":"); // NOI18N

        // set the host and port properties
        getInstanceProperties().setProperty(
                WSDeploymentFactory.HOST_ATTR, parts[0]);
        getInstanceProperties().setProperty(
                WSDeploymentFactory.PORT_ATTR, parts[1]);
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
                "SystemOut.log"; // NOI18N
    }

    /**
     * Returns true if the type of server is local. Otherwise return false
     */
    public boolean isLocal() {
        return (getInstanceProperties() != null) ? Boolean.parseBoolean(getInstanceProperties().getProperty(
                WSDeploymentFactory.IS_LOCAL_ATTR)) : false;
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
        LOGGER.log(Level.FINEST, "loadDeploymentFactory()"); // NOI18N

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
                        "com.ibm.ws.management.application.j2ee.deploy.spi.factories.DeploymentFactoryImpl"). // NOI18N
                        newInstance();
            } catch (ClassNotFoundException e) {
                // do nothing. Fix for issue with Exception on IDE restarting
                // after removing server from Runtime with opened projects
                LOGGER.log(Level.FINE, null, e);
            } catch (InstantiationException e) {
                LOGGER.log(Level.SEVERE, null, e);
            } catch (IllegalAccessException e) {
                LOGGER.log(Level.SEVERE, null, e);
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
        LOGGER.log(Level.FINEST, "updateDeploymentManager()"); // NOI18N

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

            if (factory != null) {
                // try to get a connected deployment manager
                dm = new SafeDeploymentManager(factory.getDeploymentManager(
                        WSURIManager.getRealDeploymentUrl(uri), username, password));

                // set the connected marker
                isConnected = true;
            }
        } catch (DeploymentManagerCreationException e) {
            LOGGER.log(Level.FINE, null, e);
            try {
                // if the connected deployment manager cannot be obtained - get
                // a disconnected one and set the connected marker to false
                isConnected = false;
                dm = new SafeDeploymentManager(factory.getDisconnectedDeploymentManager(
                        WSURIManager.getRealDeploymentUrl(uri)));
            } catch (DeploymentManagerCreationException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
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
        LOGGER.log(Level.FINEST, "getInstanceProperties()"); // NOI18N

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
    public ProgressObject distribute(Target[] target, File file, File file2) throws IllegalStateException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "distribute(" + Arrays.toString(target) + ", " + file + ", " + file2 + ")"); // NOI18N
        }

        // update the deployment manager
        updateDeploymentManager();

        // if the manager is not connected - throw an IllegalStateException
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(
                    WSDeploymentManager.class, "ERR_illegalState"));   // NOI18N
        }

        // update the context classloader
        loader.updateLoader();

        String webUrl = null;
        File realFile = file;
        boolean wrapped = false;
        if (file.exists() && file.getName().endsWith(".war") && file2 != null && file2.exists()) {
            // wrap it
            JarOutputStream jos = null;
            try {
                String path = file.getCanonicalPath();
                String earPath = path.substring(0, path.length() - 4) + ".ear";
                jos = new JarOutputStream(new FileOutputStream(earPath), new Manifest());

                ZipEntry e = new ZipEntry(J2eeModule.APP_XML);
                jos.putNextEntry(e);
                InputStream appXmlIs = new FileInputStream(file2);
                try {
                    FileUtil.copy(appXmlIs, jos);
                } finally {
                    appXmlIs.close();
                }
                jos.closeEntry();

                e = new ZipEntry("META-INF/was.webmodule"); // NOI18N
                jos.putNextEntry(e);
                jos.write("META-INF/was.webmodule".getBytes("UTF-8")); // NOI18N
                jos.closeEntry();

                e = new ZipEntry(file.getName());
                jos.putNextEntry(e);
                InputStream warIs = new FileInputStream(file);
                try {
                    FileUtil.copy(warIs, jos);
                } finally {
                    warIs.close();
                }
                jos.closeEntry();

                realFile = new File(earPath);
                wrapped = true;
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } finally {
                try {
                    if (jos != null) {
                        jos.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        }
        if (realFile.exists() && realFile.getPath().endsWith(".ear")) { // NOI18N
            try {
                JarFileSystem jfs = new JarFileSystem();
                jfs.setJarFile(realFile);
                FileObject appXml = jfs.getRoot().getFileObject(J2eeModule.APP_XML);
                if (appXml != null) {
                    Application ear = DDProvider.getDefault().getDDRoot(appXml);
                    Module[] modules = ear.getModule();
                    for (int i = 0; i < modules.length; i++) {
                        if (modules[i].getWeb() != null) {
                            webUrl = modules[i].getWeb().getContextRoot();
                            break;
                        }
                    }
                }
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } catch (PropertyVetoException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }

        try {
            return new WSProgressObject(dm.distribute(target, realFile, null),
                    loader, webUrl, getFullUrl(webUrl), wrapped);
        } finally {
            loader.restoreLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public ProgressObject redeploy(TargetModuleID[] targetModuleID,
            InputStream inputStream, InputStream inputStream2) throws IllegalStateException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "redeploy(" + Arrays.toString(targetModuleID) + ", " + inputStream + ", " + inputStream2 + ")"); // NOI18N
        }

        // update the deployment manager
        updateDeploymentManager();

        // if the manager is not connected - throw an IllegalStateException
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(
                    WSDeploymentManager.class, "ERR_illegalState"));   // NOI18N
        }

        loader.updateLoader();
        try {
            return dm.redeploy(targetModuleID, inputStream, inputStream2);
        } finally {
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
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "distribute(" + Arrays.toString(target) + ", " + inputStream + ", " + inputStream2 + ")"); // NOI18N
        }
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

/* Start add - Dileep for compilation */
    public ProgressObject distribute(Target[] target, ModuleType moduleType, InputStream inputStream, InputStream inputStream0) throws IllegalStateException {
        return distribute(target, inputStream, inputStream0);
    }
/* End add - Dileep for compilation */


    /**
     * Delegates the call to the server's deployment manager, checking whether
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public ProgressObject undeploy(TargetModuleID[] targetModuleID) throws IllegalStateException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "undeploy(" + Arrays.toString(targetModuleID) + ")"); // NOI18N
        }

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
    public ProgressObject stop(TargetModuleID[] targetModuleID) throws IllegalStateException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "stop(" + Arrays.toString(targetModuleID) + ")"); // NOI18N
        }

        // update the deployment manager
        updateDeploymentManager();

        // if the manager is not connected - throw an IllegalStateException
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(
                    WSDeploymentManager.class, "ERR_illegalState"));   // NOI18N
        }

        loader.updateLoader();
        try {
            return dm.stop(targetModuleID);
        } finally {
            loader.restoreLoader();
        }
    }

    /**
     * Delegates the call to the server's deployment manager, checking whether
     * the server is connected, updating the manager if neccessary and throwing
     * the IllegalStateException is appropriate
     */
    public ProgressObject start(TargetModuleID[] targetModuleID) throws IllegalStateException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "start(" + Arrays.toString(targetModuleID) + ")"); // NOI18N
        }

        // update the deployment manager
        updateDeploymentManager();

        // if the manager is not connected - throw an IllegalStateException
        if (!isConnected) {
            throw new IllegalStateException(NbBundle.getMessage(
                    WSDeploymentManager.class, "ERR_illegalState"));   // NOI18N
        }

        loader.updateLoader();
        try {
            return dm.start(targetModuleID);
        } finally {
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
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "getAvailableModules(" + moduleType + ", " + Arrays.toString(target) + ")"); // NOI18N
        }

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
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "getNonRunningModules(" + moduleType + ", " + Arrays.toString(target) + ")"); // NOI18N
        }

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
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "getRunningModules(" + moduleType + ", " + Arrays.toString(target) + ")"); // NOI18N
        }

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
            File file2) throws UnsupportedOperationException, IllegalStateException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "redeploy(" + Arrays.toString(targetModuleID) + ", " + file + ", " + file2 + ")"); // NOI18N
        }

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
    public void release() {
        LOGGER.log(Level.FINEST, "release()"); // NOI18N

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
        LOGGER.log(Level.FINEST, "isRedeploySupported()"); // NOI18N

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
    public Target[] getTargets() throws IllegalStateException {
        LOGGER.log(Level.FINEST, "getTargets()"); // NOI18N

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

    public DeploymentConfiguration createConfiguration(DeployableObject deployableObject) throws InvalidModuleException {
            throw new UnsupportedOperationException("This method should never be called"); // NOI18N
    }

    public void setLocale(Locale locale) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("This method should never be called"); // NOI18N
    }

    public boolean isLocaleSupported(Locale locale) {
        throw new UnsupportedOperationException("This method should never be called"); // NOI18N
    }

    public void setDConfigBeanVersion(DConfigBeanVersionType dConfigBeanVersionType)
            throws DConfigBeanVersionUnsupportedException {
        throw new UnsupportedOperationException("This method should never be called"); // NOI18N
    }

    public boolean isDConfigBeanVersionSupported(
            DConfigBeanVersionType dConfigBeanVersionType) {
        throw new UnsupportedOperationException("This method should never be called"); // NOI18N
    }

    public Locale getCurrentLocale() {
        throw new UnsupportedOperationException("This method should never be called"); // NOI18N
    }

    public DConfigBeanVersionType getDConfigBeanVersion() {
        throw new UnsupportedOperationException("This method should never be called"); // NOI18N
    }

    public Locale getDefaultLocale() {
        throw new UnsupportedOperationException("This method should never be called"); // NOI18N
    }

    public Locale[] getSupportedLocales() {
        throw new UnsupportedOperationException("This method should never be called"); // NOI18N
    }

    private String getFullUrl(String webUrl) {
        String port = getDefaultHostPort();
        String host = getHost();

        if(uri.indexOf(WSURIManager.WSURI60) != -1 || uri.indexOf(WSURIManager.WSURI61) != -1) {
            host = WSURIManager.getUrlWithoutPrefix(uri).split(":")[0];
        }
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("http://").append(host).append(":").append(port);
        urlBuilder.append(webUrl);
        return urlBuilder.toString();
    }

    private static class WSProgressObject implements ProgressObject {

        private final ProgressObject delegate;

        private final WSClassLoader loader;

        private final String webUrl;

        private final String fullUrl;

        private final boolean web;

        public WSProgressObject(ProgressObject delegate, WSClassLoader loader,
                String webUrl, String fullUrl, boolean web) {

            this.delegate = delegate;
            this.loader = loader;
            this.webUrl = webUrl;
            this.fullUrl = fullUrl;
            this.web = web;
        }

        public void stop() throws OperationUnsupportedException {
            delegate.stop();
        }

        public void removeProgressListener(ProgressListener arg0) {
            delegate.removeProgressListener(arg0);
        }

        public boolean isStopSupported() {
            return delegate.isStopSupported();
        }

        public boolean isCancelSupported() {
            return delegate.isCancelSupported();
        }

        public TargetModuleID[] getResultTargetModuleIDs() {
            TargetModuleID[] result = delegate.getResultTargetModuleIDs();


            if (webUrl != null && result != null && result.length == 1
                    && result[0].getModuleID().contains("type=Application")) { // NOI18N

                if (result[0].getChildTargetModuleID() == null) {
                    try {
                        loader.updateLoader();
                        WSTargetModuleID child = new WSTargetModuleID((TargetModuleID) loader.loadClass(
                            "com.ibm.ws.management.application.j2ee.deploy.spi.TargetModuleIDImpl").newInstance()); // NOI18N

                        child.setTarget(result[0].getTarget());
                        child.setParentTargetModuleID(result[0]);

                        String id = "WebSphere:name=" + webUrl.substring(1) + ",type=WebModule"; // NOI18N
                        child.setObjectName(new ObjectName(id + ",*")); // NOI18N
                        child.setModuleID(id);

                        child.setModuleType("WebModule"); // NOI18N
                        child.setStartable(true);
                        child.setWebURL(fullUrl);

                        new WSTargetModuleID(result[0]).setChildTargetModuleID(
                                new TargetModuleID[] {child.getDelegate()});

                    } catch (ClassNotFoundException ex) {
                        LOGGER.log(Level.INFO, null, ex);
                    } catch (InstantiationException ex) {
                        LOGGER.log(Level.INFO, null, ex);
                    } catch (IllegalAccessException ex) {
                        LOGGER.log(Level.INFO, null, ex);
                    } catch (MalformedObjectNameException ex) {
                        LOGGER.log(Level.INFO, null, ex);
                    } finally {
                        loader.restoreLoader();
                    }
                } else {
                    try {
                        loader.updateLoader();
                        for (TargetModuleID childModuleId : result[0].getChildTargetModuleID()) {
                            if (childModuleId.getModuleID().contains("type=WebModule")) { // NOI18N
                                // lets set all web modules
                                new WSTargetModuleID(childModuleId).setWebURL(fullUrl);
                            }
                        }
                    } finally {
                        loader.restoreLoader();
                    }
                }
            }

            if(result[0].getModuleID().contains("type=WebModule") // NOI18N
                    || (result[0].getModuleID().contains("type=Application") && web)) { // NOI18N

                try {
                    WSTargetModuleID wrapped = new WSTargetModuleID(result[0]);
                    wrapped.setModuleID(wrapped.getModuleID().replaceAll("type=Application", "type=WebModule")); // NOI18N
                    wrapped.setWebURL(fullUrl);
                    wrapped.setModuleType("WebModule"); // NOI18N
                } finally {
                    loader.updateLoader();
                }
            }
            return result;
        }

        public DeploymentStatus getDeploymentStatus() {
            return delegate.getDeploymentStatus();
        }

        public ClientConfiguration getClientConfiguration(TargetModuleID arg0) {
            return delegate.getClientConfiguration(arg0);
        }

        public void cancel() throws OperationUnsupportedException {
            delegate.cancel();
        }

        public void addProgressListener(ProgressListener arg0) {
            delegate.addProgressListener(arg0);
        }

    }

    private static class WSTargetModuleID implements TargetModuleID {

        private final TargetModuleID delegate;

        public WSTargetModuleID(TargetModuleID moduleID) {
            this.delegate = moduleID;
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

        public String getWebURL() {
            return delegate.getWebURL();
        }

        public void setWebURL(String webURL) {
            try {
                Method method = delegate.getClass().getMethod("setWebURL", new Class[] {String.class}); // NOI18N
                if (method != null) {
                    method.invoke(delegate, new Object[] {webURL});
                }
            } catch (NoSuchMethodException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } catch (IllegalAccessException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } catch (InvocationTargetException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }

        public Target getTarget() {
            return delegate.getTarget();
        }

        public void setTarget(Target target) {
            try {
                Method method = delegate.getClass().getMethod("setTarget", new Class[] {Target.class}); // NOI18N
                if (method != null) {
                    method.invoke(delegate, new Object[] {target});
                }
            } catch (NoSuchMethodException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } catch (IllegalAccessException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } catch (InvocationTargetException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }

        public TargetModuleID getParentTargetModuleID() {
            return delegate.getParentTargetModuleID();
        }

        public void setParentTargetModuleID(TargetModuleID targetModuleID) {
            try {
                Method method = delegate.getClass().getMethod("setParentTargetModuleID", new Class[] {TargetModuleID.class}); // NOI18N
                if (method != null) {
                    method.invoke(delegate, new Object[] {targetModuleID});
                }
            } catch (NoSuchMethodException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } catch (IllegalAccessException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } catch (InvocationTargetException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }

        public String getModuleID() {
            return delegate.getModuleID();
        }

        public TargetModuleID[] getChildTargetModuleID() {
            return delegate.getChildTargetModuleID();
        }

        public void setChildTargetModuleID(TargetModuleID[] targetModuleIDs) {
            try {
                Method method = delegate.getClass().getMethod("setChildTargetModuleID", new Class[] {TargetModuleID[].class}); // NOI18N
                if (method != null) {
                    method.invoke(delegate, new Object[] {targetModuleIDs});
                }
            } catch (NoSuchMethodException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } catch (IllegalAccessException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } catch (InvocationTargetException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }

        public void setObjectName(ObjectName name) {
            try {
                Method method = delegate.getClass().getMethod("setObjectName", new Class[] {ObjectName.class}); // NOI18N
                if (method != null) {
                    method.invoke(delegate, new Object [] {name});
                }
            } catch (NoSuchMethodException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } catch (IllegalAccessException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } catch (InvocationTargetException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }

        public void setModuleID(String id) {
            try {
                Method method = delegate.getClass().getMethod("setModuleID", new Class[] {String.class}); // NOI18N
                if (method != null) {
                    method.invoke(delegate, new Object [] {id});
                }
            } catch (NoSuchMethodException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } catch (IllegalAccessException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } catch (InvocationTargetException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }

        public void setModuleType(String type) {
            try {
                Method method = delegate.getClass().getMethod("setModuleType", new Class[] {String.class}); // NOI18N
                if (method != null) {
                    method.invoke(delegate, new Object[] {type});
                }
            } catch (NoSuchMethodException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } catch (IllegalAccessException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } catch (InvocationTargetException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }

        public void setStartable(boolean startable) {
            try {
                Method method = delegate.getClass().getMethod("setStartable", new Class[] {boolean.class}); // NOI18N
                if (method != null) {
                    method.invoke(delegate, new Object[] {true});
                }
            } catch (NoSuchMethodException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } catch (IllegalAccessException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } catch (InvocationTargetException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }

        public TargetModuleID getDelegate() {
            return delegate;
        }

    }
}
