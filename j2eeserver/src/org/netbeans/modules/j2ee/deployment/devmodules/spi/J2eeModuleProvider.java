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

import org.netbeans.modules.j2ee.deployment.config.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.j2ee.deployment.impl.Server;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ServerString;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.common.api.SourceFileMap;
import org.openide.filesystems.FileObject;
import org.openide.util.WeakListeners;
import java.io.File;

/** This object must be implemented by J2EE module support and an instance 
 * added into project lookup.
 * 
 * @author  Pavel Buzek
 */
public abstract class J2eeModuleProvider {
    
    private ServerRegistry.InstanceListener il;
    private ConfigSupportImpl confSupp;
    
    public J2eeModuleProvider () {
        il = new IL ();
        ServerRegistry.getInstance ().addInstanceListener (
            (ServerRegistry.InstanceListener) WeakListeners.create(
            ServerRegistry.InstanceListener.class, il, ServerRegistry.getInstance ()));
    }
    
    public abstract J2eeModule getJ2eeModule ();
    
    public abstract ModuleChangeReporter getModuleChangeReporter ();
    
    public final ConfigSupport getConfigSupport () {
        if (confSupp == null) {
            confSupp = new ConfigSupportImpl (this);
        }
	return confSupp;
    }
    
    public final ServerDebugInfo getServerDebugInfo () {
        ServerInstance si = ServerRegistry.getInstance ().getServerInstance (getServerInstanceID ());
        if (si != null) {
            return si.getStartServer().getDebugInfo(null);
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
     * Return destination path-to-source file mappings.
     * Default returns config file mapping with straight mapping from the configuration
     * directory to distribution directory.
     */
    public SourceFileMap getSourceFileMap() {
        return getConfigSupportImpl().getDefaultConfigFileMap();
    }
    
    /** If the module wants to specify a target server instance for deployment 
     * it needs to override this method to return false. 
     */
    public boolean useDefaultServer () {
        return true;
    }
    
    /** Id of server isntance for deployment. The default implementation returns
     * the default server instance selected in Server Registry. 
     * The return value may not be null.
     * If modules override this method they also need to override {@link useDefaultServer}.
     */
    public String getServerInstanceID () {
        return ServerRegistry.getInstance ().getDefaultInstance ().getUrl ();
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

    protected final void fireServerChange (String oldServerID, String newServerID) {
        Server oldServer = ServerRegistry.getInstance ().getServer (oldServerID);
	Server newServer = ServerRegistry.getInstance ().getServer (newServerID);
        if (oldServer != null && !oldServer.equals (newServer)) {

            if (J2eeModule.WAR.equals(getJ2eeModule().getModuleType())) {
                String oldCtxPath = getConfigSupportImpl().getWebContextRoot();
                confSupp = null;
                String ctx = getConfigSupportImpl().getWebContextRoot ();
                if (ctx == null || ctx.equals ("")) {
                    getConfigSupportImpl().setWebContextRoot(oldCtxPath);
                }
            } else {
                J2eeModuleProvider.this.confSupp = null;
                ServerString newServerString = new ServerString(newServer);
                ConfigSupportImpl.createInitialConfiguration(this, newServerString);
            }
        }
    }
    
    /**
     * Returns all configuration files known to this J2EE Module.
     */
    protected final FileObject[] getConfigurationFiles() {
        return ConfigSupportImpl.getConfigurationFiles(this);
    }
    
    private final class IL implements ServerRegistry.InstanceListener {
        
        public void changeDefaultInstance (ServerString oldInstance, ServerString newInstance) {
            if (useDefaultServer () && oldInstance == null || ((newInstance != null) && (oldInstance.getPlugin() != newInstance.getPlugin()))) {
                if (J2eeModule.WAR.equals(getJ2eeModule().getModuleType())) {
                    String oldCtxPath = getConfigSupportImpl().getWebContextRoot();
                    J2eeModuleProvider.this.confSupp = null;
                    String ctx = getConfigSupportImpl().getWebContextRoot ();
                    if (ctx == null || ctx.equals ("")) {
                        getConfigSupportImpl().setWebContextRoot(oldCtxPath);
                    }
                } else {
                    J2eeModuleProvider.this.confSupp = null;
                    ConfigSupportImpl.createInitialConfiguration(J2eeModuleProvider.this, newInstance);
                }
            }
        }
        
        public void instanceAdded (org.netbeans.modules.j2ee.deployment.impl.ServerString instance) {
        }
        
        public void instanceRemoved (org.netbeans.modules.j2ee.deployment.impl.ServerString instance) {
        }
        
    }
    
    private ConfigSupportImpl getConfigSupportImpl() {
        return (ConfigSupportImpl) getConfigSupport();
    }
}
