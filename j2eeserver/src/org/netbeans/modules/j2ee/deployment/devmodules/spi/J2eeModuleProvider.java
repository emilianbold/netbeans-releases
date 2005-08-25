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

package org.netbeans.modules.j2ee.deployment.devmodules.spi;

import java.io.OutputStream;
import javax.enterprise.deploy.spi.Target;
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
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.common.api.SourceFileMap;
import org.netbeans.modules.j2ee.deployment.plugins.api.StartServer;
import org.netbeans.modules.j2ee.deployment.plugins.api.VerifierSupport;
import org.openide.filesystems.FileObject;
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
    private ConfigSupportImpl confSupp;
    List listeners = new ArrayList();
    private ConfigFilesListener configFilesListener = null;
    
    public J2eeModuleProvider () {
        il = new IL ();
        ServerRegistry.getInstance ().addInstanceListener (
            (InstanceListener) WeakListeners.create(
                InstanceListener.class, il, ServerRegistry.getInstance ()));
    }
    
    public abstract J2eeModule getJ2eeModule ();
    
    public abstract ModuleChangeReporter getModuleChangeReporter ();
    
    public final ConfigSupport getConfigSupport () {
        if (confSupp == null) {
            confSupp = new ConfigSupportImpl (this);
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
         */
        public void ensureResourceDefinedForEjb(String ejbname, String ejbtype);
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
                if (oldCtxPath == null || oldCtxPath.equals("")) { //NOI18N
                    oldCtxPath = "/" + getDeploymentName(); //NOI18N
                }
                ConfigSupportImpl oldConSupp = confSupp;
                confSupp = null;
                getConfigSupportImpl().ensureConfigurationReady();
                getConfigSupportImpl().setWebContextRoot(oldCtxPath);
                oldConSupp.dispose();
            } else {
                ConfigSupportImpl oldConSupp = confSupp;
                confSupp = null;
                getConfigSupportImpl().ensureConfigurationReady();
                oldConSupp.dispose();
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
                    confSupp.dispose();
                    J2eeModuleProvider.this.confSupp = null;
                    getConfigSupportImpl().ensureConfigurationReady();
                    String ctx = getConfigSupportImpl().getWebContextRoot ();
                    if (ctx == null || ctx.equals ("")) { //NOI18N
                        getConfigSupportImpl().setWebContextRoot(oldCtxPath);
                    }
                } else {
                    confSupp.dispose();
                    J2eeModuleProvider.this.confSupp = null;
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
