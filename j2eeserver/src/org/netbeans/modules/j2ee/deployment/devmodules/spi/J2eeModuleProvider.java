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
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.openide.filesystems.FileObject;

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
        ServerRegistry.getInstance ().addInstanceListener (il);
    }
    
    public abstract J2eeModule getJ2eeModule ();
    
    public abstract ModuleChangeReporter getModuleChangeReporter ();
    
    /** A folder that contains the module sources.
     * <div class="nonnormative">
     * <p>This folder can be user for example as a location of server specific 
     * configuration files.</p>
     * </div>
     */
    public abstract FileObject getModuleFolder ();
    
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
        public void setWebContextRoot(String contextRoot);
        public String getWebContextRoot();
        /**
         * Reset configuration storage references to make sure memory reclamation on project close.
         */
        public void resetStorage();
    }
    
    public boolean useDirectoryPath() {
        return true;
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
    
    protected final void fireServerChange (String oldServerID, String newServerID) {
        Server oldServer = ServerRegistry.getInstance ().getServer (oldServerID);
	Server newServer = ServerRegistry.getInstance ().getServer (newServerID);
        if (oldServer != null && !oldServer.equals (newServer)) {
            ConfigSupportImpl cs = (ConfigSupportImpl) getConfigSupport ();
            String oldCtxPath = cs.getWebContextRoot(oldServer);
            cs.resetStorage ();
            String ctx = cs.getWebContextRoot ();
            if (ctx == null || ctx.equals ("")) {
                cs.setWebContextRoot(oldCtxPath);
            }
        }
    }
    
    private final class IL implements ServerRegistry.InstanceListener {
        
        public void changeDefaultInstance (org.netbeans.modules.j2ee.deployment.impl.ServerString oldInstance, org.netbeans.modules.j2ee.deployment.impl.ServerString newInstance) {
            if (useDefaultServer () && oldInstance == null || ((newInstance != null) && (oldInstance.getPlugin() != newInstance.getPlugin()))) {
                ConfigSupportImpl cs = (ConfigSupportImpl) getConfigSupport ();
                String oldCtxPath = cs.getWebContextRoot(oldInstance.getServer ());
                cs.resetStorage ();
                cs.setWebContextRoot(oldCtxPath);
            }
        }
        
        public void instanceAdded (org.netbeans.modules.j2ee.deployment.impl.ServerString instance) {
        }
        
        public void instanceRemoved (org.netbeans.modules.j2ee.deployment.impl.ServerString instance) {
        }
        
    }
}
