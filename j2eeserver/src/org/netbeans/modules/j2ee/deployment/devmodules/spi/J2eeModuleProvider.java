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

package org.netbeans.modules.j2ee.deployment.devmodules.spi;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping;
import org.netbeans.modules.j2ee.deployment.common.api.ValidationException;
import org.netbeans.modules.j2ee.deployment.config.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.j2ee.deployment.impl.DefaultSourceMap;
import org.netbeans.modules.j2ee.deployment.impl.Server;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ServerString;
import org.netbeans.modules.j2ee.deployment.impl.ServerTarget;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.common.api.SourceFileMap;
import org.netbeans.modules.j2ee.deployment.plugins.api.StartServer;
import org.netbeans.modules.j2ee.deployment.plugins.api.VerifierSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** This object must be implemented by J2EE module support and an instance 
 * added into project lookup.
 * 
 * @author  Pavel Buzek
 */
public abstract class J2eeModuleProvider {
    
    private InstanceListener il;
    private ConfigSupportImpl configSupportImpl;
    List listeners = new ArrayList();
    private ConfigFilesListener configFilesListener = null;
    
    /**
     * Enterprise resorce directory property
     *
     * @since 1.12
     */
    public static final String PROP_ENTERPRISE_RESOURCE_DIRECTORY = "resourceDir"; // NOI18N
    
    private PropertyChangeSupport supp = new PropertyChangeSupport(this);
    
    public J2eeModuleProvider () {
        il = new IL ();
        ServerRegistry.getInstance ().addInstanceListener (
            (InstanceListener) WeakListeners.create(
                InstanceListener.class, il, ServerRegistry.getInstance ()));
    }
    
    public abstract J2eeModule getJ2eeModule ();
    
    public abstract ModuleChangeReporter getModuleChangeReporter ();
    
    public final ConfigSupport getConfigSupport () {
        ConfigSupportImpl confSupp;
        synchronized (this) {
            confSupp = configSupportImpl;
        }
        if (confSupp == null) {
            confSupp = new ConfigSupportImpl(this);
            synchronized (this) {
                configSupportImpl = confSupp;
            }
        }
	return confSupp;
    }
    
    /**
     * Return server debug info.
     * Note: if server is not running and needs to be up for retrieving debug info, 
     * this call will return null.  This call is also used by UI so it should not 
     * try to ping or start the server.
     */
    public final ServerDebugInfo getServerDebugInfo () {
        ServerInstance si = ServerRegistry.getInstance ().getServerInstance (getServerInstanceID ());
        StartServer ss = si.getStartServer();
        if (ss == null) {
            return null;
        }
        // AS8.1 needs to have server running to get accurate debug info, and also need a non-null target 
        // But getting targets from AS8.1 require start server which would hang UI, so avoid start server
        // Note: for debug info after deploy, server should already start.
        if (! si.isRunningLastCheck() && ss.needsStartForTargetList()) {
            if (ss.isAlsoTargetServer(null)) {
                return ss.getDebugInfo(null);
            } else {
                return null;
            }
        }
        
        Target target = null;
        if (si != null) {
            ServerTarget[] sts = si.getTargets();
            for (int i=0; i<sts.length; i++) {
                if (si.getStartServer().isAlsoTargetServer(sts[i].getTarget())) {
                    target = sts[i].getTarget();
                }
            }
            if (target == null && sts.length > 0) {
                target = sts[0].getTarget();
            }
            return si.getStartServer().getDebugInfo(target);
        }
        return null;
    }
    
    /**
     * Gets the data sources deployed on the target server instance.
     *
     * @return set of data sources
     *
     * @since 1.15 
     */
    public Set<Datasource> getServerDatasources() {
        ServerInstance si = ServerRegistry.getInstance ().getServerInstance (getServerInstanceID ());
        Set<Datasource> deployedDS = Collections.<Datasource>emptySet();
        if (si != null) {
            deployedDS = si.getDatasources();
        }
        else {
            ErrorManager.getDefault().log(ErrorManager.WARNING, 
                    "The server data sources cannot be retrieved because the server instance cannot be found.");
        }
        return deployedDS;
    }
    
    /**
     * Gets the data sources saved in the module.
     *
     * @return set of data sources
     *
     * @since 1.15 
     */
    public Set<Datasource> getModuleDatasources() {
        Set<Datasource> projectDS = getConfigSupport().getDatasources();
        return projectDS;
    }

    /**
     * Tests whether data source creation is supported.
     *
     * @return true if data source creation is supported, false otherwise.
     *
     * @since 1.15 
     */
    public boolean isDatasourceCreationSupported() {
        return getConfigSupport().isDatasourceCreationSupported();
    }
    
    
    /**
     * Creates and saves data source in the module if it does not exist yet on the target server or in the module.
     * Data source is considered to be existing when JNDI name of the found data source and the one
     * just created equal.
     *
     * @param jndiName name of data source
     * @param url database URL
     * @param username database user
     * @param password user's password
     * @param driver fully qualified name of database driver class
     * @return created data source
     * @exception DatasourceAlreadyExistsException if conflicting data source is found
     *
     * @since 1.15 
     */
    public final Datasource createDatasource(String jndiName, String  url, String username, String password, String driver) 
    throws DatasourceAlreadyExistsException {

        //check whether the ds is not already on the server
        Set<Datasource> deployedDS = getServerDatasources();
        if (deployedDS != null) {
            for (Iterator it = deployedDS.iterator(); it.hasNext();) {
                Datasource ds = (Datasource) it.next();
                if (jndiName.equals(ds.getJndiName())) // ds with the same JNDI name already exists on the server, do not create new one
                    throw new DatasourceAlreadyExistsException(ds);
            }
        }
        
        Datasource ds = null;
        try {
            //btw, ds existence in a project is verified directly in the deployment configuration
            ds = getConfigSupport().createDatasource(jndiName, url, username, password, driver);
        } catch (OperationUnsupportedException oue) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, oue);
        }
        
        return ds;
    }
    
    /**
     * Deploys data sources saved in the module.
     *
     * @exception ConfigurationException if there is some problem with data source configuration
     * @exception DatasourceAlreadyExistsException if module data source(s) are conflicting
     * with data source(s) already deployed on the server
     *
     * @since 1.15 
     */
    public void deployDatasources() throws ConfigurationException, DatasourceAlreadyExistsException {
        ServerInstance si = ServerRegistry.getInstance ().getServerInstance (getServerInstanceID ());
        if (si != null) {
            Set<Datasource> moduleDS = getModuleDatasources();
            si.deployDatasources(moduleDS);
        }
        else {
            ErrorManager.getDefault().log(ErrorManager.WARNING, 
                    "The data sources cannot be deployed because the server instance cannot be found.");
        }
    }
    
    
    /**
     * Register a listener which will be notified when some of the properties
     * change.
     * 
     * @param l listener which should be added.
     * @since 1.12
     */
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        supp.addPropertyChangeListener(l);
    }
    
    /**
     * Remove a listener registered previously.
     *
     * @param l listener which should be removed.
     * @since 1.12
     */
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        supp.removePropertyChangeListener(l);
    }
    

    /** 
     * Fire PropertyChange to all registered PropertyChangeListeners.
     *
     * @param propName property name.
     * @param oldValue old value.
     * @param newValue new value.
     * @since 1.12
     */
    protected final void firePropertyChange(String propName, Object oldValue, Object newValue) {
        supp.firePropertyChange(propName, oldValue, newValue);
    }
    
    /**
     * Configuration support to allow development module code to access well-known 
     * configuration propeties, such as web context root, cmp mapping info...
     * The setters and getters work with server specific data on the server returned by
     * {@link getServerID} method.
     */
    public static interface ConfigSupport {
        /**
         * Create an initial fresh configuration for the current module.  Do nothing if configuration already exists.
         * @return true if there is no existing configuration, false if there is exsisting configuration.
         */
        public boolean createInitialConfiguration();
        /**
         * Ensure configuration is ready to respond to any editing to the module.
         * @return true if the configuration is ready, else false.
         */
        public boolean ensureConfigurationReady();

        /**
         * Save configuration.  This is mainly for wizard actions that could
         * initiate changes in configuration.  These changes should be saved
         * explicitly by wizard, not implicitly by plugin, in order to avoid
         * possible side-effect.
         */
        //public void saveConfiguration() throws IOException;

        /**
         * Set/get web module context root.
         */
        public void setWebContextRoot(String contextRoot);
        public String getWebContextRoot();
        
        /**
         * Return a list of file names for current server specific deployment 
         * descriptor used in this module.
         */
        public String [] getDeploymentConfigurationFileNames();
        /**
         * Return relative path within the archive or distribution content for the
         * given server specific deployment descriptor file.
         * @param deploymentConfigurationFileName server specific descriptor file name
         * @return relative path inside distribution content.
         */
        public String getContentRelativePath(String deploymentConfigurationFileName);
        /**
         * Push the CMP and CMR mapping info to the server configuraion.
         * This call is typically used by CMP mapping wizard.
         */
        public void setCMPMappingInfo(OriginalCMPMapping[] mappings);
        /**
         * Ensure needed resources are automatically defined for the entity
         * represented by given DDBean.
         * @param ejbname the ejb name
         * @param ejbtype dtd name for type of ejb: 'message-drive', 'entity', 'session'.
         * @deprecated replaced with ensureResourceDefinedForEjb with JNDI name attribute
         */
        @Deprecated
        public void ensureResourceDefinedForEjb(String ejbname, String ejbtype);
        
        /**
         * Ensure needed resources are automatically defined for the entity
         * represented by given DDBean.
         * @param ejbName   the EJB name
         * @param ejbType   the DTD name for type of EJB: 'message-drive', 'entity', 'session'.
         * @param jndiName  the JNDI name of the resource where the EJB is stored
         */
        public void ensureResourceDefinedForEjb(String ejbName, String ejbType, String jndiName);
        
        /**
         * Tests whether data source creation is supported.
         *
         * @return true if data source creation is supported, false otherwise.
         *
         * @since 1.15 
         */
        public boolean isDatasourceCreationSupported();
                
        /**
         * Gets the data sources saved in the module.
         *
         * @return set of data sources
         *
         * @since 1.15 
         */
        public Set<Datasource> getDatasources();
        
        /**
         * Creates and saves data source in the module if it does not exist yet in the module.
         * Data source is considered to be existing when JNDI name of the found data source and the one
         * just created equal.
         *
         * @param jndiName name of data source
         * @param url database URL
         * @param username database user
         * @param password user's password
         * @param driver fully qualified name of database driver class
         * @return created data source
         * @exception OperationUnsupportedException if operation is not supported
         * @exception DatasourceAlreadyExistsException if conflicting data source is found
         *
         * @since 1.15 
         */
        public Datasource createDatasource(String jndiName, String  url, String username, String password, String driver)
        throws OperationUnsupportedException, DatasourceAlreadyExistsException;
    }
    
    /**
     * Returns source deployment configuration file path for the given deployment 
     * configuration file name. 
     *
     * @param name file name of the deployement configuration file.
     * @return non-null absolute path to the deployment configuration file.
     */
    abstract public File getDeploymentConfigurationFile(String name);
    
    /**
     * Finds source deployment configuration file object for the given deployment 
     * configuration file name.  
     *
     * @param name file name of the deployement configuration file.
     * @return FileObject of the configuration descriptor file; null if the file does not exists.
     * 
     */
    abstract public FileObject findDeploymentConfigurationFile (String name);
    
    /**
     * Returns directory containing definition for enterprise resources needed for
     * the module execution; return null if not supported
     */
    public File getEnterpriseResourceDirectory() {
        return null;
    }
    
    /**
     *  Returns list of root directories for source files including configuration files.
     *  Examples: file objects for src/java, src/conf.  
     *  Note: 
     *  If there is a standard configuration root, it should be the first one in
     *  the returned list.
     */
    public FileObject[] getSourceRoots() {
        return new FileObject[0];
    }
    
    /**
     * Return destination path-to-source file mappings.
     * Default returns config file mapping with straight mapping from the configuration
     * directory to distribution directory.
     */
    public SourceFileMap getSourceFileMap() {
        return new DefaultSourceMap(this);
    }
    
    /** If the module wants to specify a target server instance for deployment 
     * it needs to override this method to return false. 
     */
    public boolean useDefaultServer () {
        return true;
    }
    
    /**
     * Set ID of the server instance that will be used for deployment.
     * 
     * @param severInstanceID server instance ID.
     * @since 1.6
     */
    public abstract void setServerInstanceID(String severInstanceID);
    
    /** Id of server isntance for deployment. The default implementation returns
     * the default server instance selected in Server Registry. 
     * The return value may not be null.
     * If modules override this method they also need to override {@link useDefaultServer}.
     */
    public String getServerInstanceID () {
        return ServerRegistry.getInstance ().getDefaultInstance ().getUrl ();
    }
    
    /**
     * Return InstanceProperties of the server instance
     **/
    public InstanceProperties getInstanceProperties(){
        return InstanceProperties.getInstanceProperties(getServerInstanceID());
    }

    /** This method is used to determin type of target server.
     * The return value must correspond to value returned from {@link getServerInstanceID}.
     */
    public String getServerID () {
        return ServerRegistry.getInstance ().getDefaultInstance ().getServer ().getShortName ();
    }
    
    /**
     * Return name to be used in deployment of the module.
     */
    public String getDeploymentName() {
        return getConfigSupportImpl().getDeploymentName();
    }

    /**
     * Returns true if the current target platform provide verifier support for this module.
     */
    public boolean hasVerifierSupport() {
        String serverId = getServerID();
        if (serverId != null) {
            Server server = ServerRegistry.getInstance().getServer(serverId);
            if (server != null) {
                return server.canVerify(getJ2eeModule().getModuleType());
            }
        }
        return false;
    }
    
    /**
     * Invoke verifier from current platform on the provided target file.
     * @param target File to run verifier against.
     * @param logger output stream to write verification resutl to.
     * @return true
     */
    public void verify(FileObject target, OutputStream logger) throws ValidationException {
        VerifierSupport verifier = ServerRegistry.getInstance().getServer(getServerID()).getVerifierSupport();
        if (verifier == null) {
            throw new ValidationException ("Verification not supported by the selected server");
        }
        Object type = getJ2eeModule().getModuleType();
        if (!verifier.supportsModuleType(type)) {
            throw new ValidationException ("Verification not supported for module type " + type);
        }
        ServerRegistry.getInstance().getServer(getServerID()).getVerifierSupport().verify(target, logger);
    }
    
    protected final void fireServerChange (String oldServerID, String newServerID) {
        Server oldServer = ServerRegistry.getInstance ().getServer (oldServerID);
	Server newServer = ServerRegistry.getInstance ().getServer (newServerID);
        if (oldServer != null && newServer != null && !oldServer.equals (newServer)) {

            if (J2eeModule.WAR.equals(getJ2eeModule().getModuleType())) {
                String oldCtxPath = getConfigSupportImpl().getWebContextRoot();
                ConfigSupportImpl oldConSupp;
                synchronized (this) {
                    oldConSupp = configSupportImpl;
                    configSupportImpl = null;
                }
                getConfigSupportImpl().ensureConfigurationReady();
                if (oldCtxPath == null || oldCtxPath.equals("")) { //NOI18N
                    oldCtxPath = getDeploymentName().replace(' ', '_'); //NOI18N
                    char c [] = oldCtxPath.toCharArray();
                    for (int i = 0; i < c.length; i++) {
                        if (!Character.UnicodeBlock.BASIC_LATIN.equals(Character.UnicodeBlock.of(c[i])) ||
                                !Character.isLetterOrDigit(c[i])) {
                            c[i] = '_';
                        }
                    }
                    oldCtxPath = "/" + new String (c); //NOI18N
                }
                getConfigSupportImpl().setWebContextRoot(oldCtxPath);
                if (oldConSupp != null) {
                    oldConSupp.dispose();
                }
            } else {
                ConfigSupportImpl oldConSupp;
                synchronized (this) {
                    oldConSupp = configSupportImpl;
                    configSupportImpl = null;
                }
                getConfigSupportImpl().ensureConfigurationReady();
                if (oldConSupp != null) {
                    oldConSupp.dispose();
                }
            }
        }
    }
        
    /**
     * Returns all configuration files known to this J2EE Module.
     */
    public final FileObject[] getConfigurationFiles() {
        return getConfigurationFiles(false);
    }

    public final FileObject[] getConfigurationFiles(boolean refresh) {
        if (refresh) {
            configFilesListener.stopListening();
            configFilesListener = null;
        }
        addCFL();
        return ConfigSupportImpl.getConfigurationFiles(this);
    }
    
    public final void addConfigurationFilesListener(ConfigurationFilesListener l) {
        listeners.add(l);
    }
    public final void removeConfigurationFilesListener(ConfigurationFilesListener l) {
        listeners.remove(l);
    }
    
    /**
     * Register an instance listener that will listen to server instances changes.
     *
     * @l listener which should be added.
     *
     * @since 1.6
     */
    public final void addInstanceListener(InstanceListener l) {
        ServerRegistry.getInstance ().addInstanceListener(l);
    }

    /**
     * Remove an instance listener which has been registered previously.
     *
     * @l listener which should be removed.
     *
     * @since 1.6
     */
    public final void removeInstanceListener(InstanceListener l) {
        ServerRegistry.getInstance ().removeInstanceListener(l);
    }
    
    private void addCFL() {
        //already listen
        if (configFilesListener != null)
            return;
        configFilesListener = new ConfigFilesListener(this, listeners);
    }
    
    private final class IL implements InstanceListener {
        
        public void changeDefaultInstance (String oldInst, String newInst) {
            ServerInstance oldServerInstance = ServerRegistry.getInstance().getServerInstance(oldInst);
            ServerInstance newServerInstance = ServerRegistry.getInstance().getServerInstance(newInst);
            ServerString oldInstance = oldServerInstance != null 
                                            ? new ServerString(oldServerInstance) 
                                            : null;
            ServerString newInstance = newServerInstance != null 
                                            ? new ServerString(newServerInstance) 
                                            : null;
            if (useDefaultServer () && newInstance != null 
                    && (oldInstance == null || !oldInstance.getPlugin().equals(newInstance.getPlugin()))) {
                if (J2eeModule.WAR.equals(getJ2eeModule().getModuleType())) {
                    String oldCtxPath = getConfigSupportImpl().getWebContextRoot();
                    oldCtxPath = "/"+J2eeModuleProvider.this.getDeploymentName(); //NOI18N
                    ConfigSupportImpl oldConSupp;
                    synchronized (J2eeModuleProvider.this) {
                        oldConSupp = configSupportImpl;
                        configSupportImpl = null;
                    }
                    if (oldConSupp != null) {
                        oldConSupp.dispose();
                    }
                    getConfigSupportImpl().ensureConfigurationReady();
                    String ctx = getConfigSupportImpl().getWebContextRoot ();
                    if (ctx == null || ctx.equals ("")) { //NOI18N
                        getConfigSupportImpl().setWebContextRoot(oldCtxPath);
                    }
                } else {
                    ConfigSupportImpl oldConSupp;
                    synchronized (J2eeModuleProvider.this) {
                        oldConSupp = configSupportImpl;
                        configSupportImpl = null;
                    }
                    if (oldConSupp != null) {
                        oldConSupp.dispose();
                    }
                    getConfigSupportImpl().ensureConfigurationReady();
                }
            }
        }
        
        public void instanceAdded (String instance) {
        }
        
        public void instanceRemoved (String instance) {
        }
        
    }
    
    private ConfigSupportImpl getConfigSupportImpl() {
        return (ConfigSupportImpl) getConfigSupport();
    }

}
