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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.deployment.devmodules.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.impl.*;
import org.netbeans.modules.j2ee.deployment.impl.projects.*;
import org.netbeans.modules.j2ee.deployment.impl.ui.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.JDBCDriverDeployer;
import org.openide.util.NbBundle;

/**
 *
 * @author  Pavel Buzek
 */
public final class Deployment {

    private static boolean alsoStartTargets = true;    //TODO - make it a property? is it really needed?
    
    private static Deployment instance = null;

    public static synchronized Deployment getDefault () {
        if (instance == null) {
            instance = new Deployment ();
        }
        return instance;
    }
    
    private Deployment () {
    }
    
    /** Deploys a web J2EE module to server.
     * @param clientModuleUrl URL of module within a J2EE Application that 
     * should be used as a client (can be null for standalone modules)
     * <div class="nonnormative">
     * <p>Note: if null for J2EE application the first web or client module will be used.</p>
     * </div>
     * @return complete URL to be displayed in browser (server part plus the client module and/or client part provided as a parameter)
     */
    public String deploy (J2eeModuleProvider jmp, boolean debugmode, String clientModuleUrl, String clientUrlPart, boolean forceRedeploy) throws DeploymentException {
        return deploy(jmp, debugmode, clientModuleUrl, clientUrlPart, forceRedeploy, null);
    }
    
    public String deploy (J2eeModuleProvider jmp, boolean debugmode, String clientModuleUrl, String clientUrlPart, boolean forceRedeploy, Logger logger) throws DeploymentException {
        
        DeploymentTargetImpl deploymentTarget = new DeploymentTargetImpl(jmp, clientModuleUrl);
        TargetModule[] modules = null;
        final J2eeModule module = deploymentTarget.getModule();

        String title = NbBundle.getMessage(Deployment.class, "LBL_Deploying", jmp.getDeploymentName());
        ProgressUI progress = new ProgressUI(title, false, logger);
        
        try {
            progress.start();
            
            ServerString server = deploymentTarget.getServer(); //will throw exception if bad server id
        
            if (module == null) {
                String msg = NbBundle.getMessage (Deployment.class, "MSG_NoJ2eeModule");
                throw new DeploymentException(msg);
            }
            ServerInstance serverInstance = server.getServerInstance();
            if (server == null || serverInstance == null) {
                String msg = NbBundle.getMessage (Deployment.class, "MSG_NoTargetServer");
                throw new DeploymentException(msg);
            }
            
            JDBCDriverDeployer jdbcDriverDeployer = server.getServerInstance().getJDBCDriverDeployer();
            
            // Currently it is not possible to select target to which modules will 
            // be deployed. Lets use the first one.
            ServerTarget targets[] = serverInstance.getTargets();
            if (targets.length > 0) {
                Target target = targets[0].getTarget();
                Set<Datasource> moduleDatasources = jmp.getModuleDatasources();
                if (moduleDatasources.size() > 0 && jdbcDriverDeployer != null
                        && jdbcDriverDeployer.supportsDeployJDBCDrivers(target)) {
                    ProgressObject po = jdbcDriverDeployer.deployJDBCDrivers(target, moduleDatasources);
                    ProgressObjectUtil.trackProgressObject(progress, po, Long.MAX_VALUE);
                }
            }
            
            boolean serverReady = false;
            TargetServer targetserver = new TargetServer(deploymentTarget);

            if (alsoStartTargets || debugmode) {
                targetserver.startTargets(debugmode, progress);
            } else { //PENDING: how do we know whether target does not need to start when deploy only
                server.getServerInstance().start(progress);
            }

            jmp.deployDatasources();
            deployMessageDestinations(jmp);

            modules = targetserver.deploy(progress, forceRedeploy);
            // inform the plugin about the deploy action, even if there was
            // really nothing needed to be deployed
            targetserver.notifyIncrementalDeployment(modules);
            
            if (modules != null && modules.length > 0) {
                deploymentTarget.setTargetModules(modules);
            } else {
                String msg = NbBundle.getMessage(Deployment.class, "MSG_ModuleNotDeployed");
                throw new DeploymentException (msg);
            }
            return deploymentTarget.getClientUrl(clientUrlPart);
        } catch (Exception ex) {            
            String msg = NbBundle.getMessage (Deployment.class, "MSG_DeployFailed", ex.getLocalizedMessage ());
            java.util.logging.Logger.getLogger("global").log(Level.INFO, null, ex);
            throw new DeploymentException(msg, ex);
        } finally {
            if (progress != null) {
                progress.finish();
            }
        }
    }
    
    private static void deployMessageDestinations(J2eeModuleProvider jmp) throws ConfigurationException {
        ServerInstance si = ServerRegistry.getInstance ().getServerInstance (jmp.getServerInstanceID ());
        if (si != null) {
            si.deployMessageDestinations(jmp.getConfigSupport().getMessageDestinations());
        }
        else {
            java.util.logging.Logger.getLogger("global").log(Level.WARNING,
                    "The message destinations cannot be deployed because the server instance cannot be found."); // NOI18N
        }
    }
    
    public static final class DeploymentException extends Exception {
        private DeploymentException (String msg) {
            super (msg);
        }
        private DeploymentException (Throwable t) {
            super (t);
        }
        private DeploymentException (String s, Throwable t) {
            super (s, t);
        }
        /**
         * Returns a short description of this DeploymentException.
         * overwrite the one from Exception to avoid showing the class name that does nto provide any real value.
         * @return a string representation of this DeploymentException.
         */
        public String toString() {
            String s = getClass().getName();
            String message = getLocalizedMessage();
            return (message != null) ? (message) : s;
        }
    }
    
    public String [] getServerInstanceIDs () {
        return InstanceProperties.getInstanceList ();
    }
    
    /**
     * Return ServerInstanceIDs of all registered server instances that support
     * specified module types.
     *
     * @param moduleTypes list of module types that the server instance must support.
     *
     * @return ServerInstanceIDs of all registered server instances that meet 
     *         the specified requirements.
     * @since 1.6
     */
    public String[] getServerInstanceIDs(Object[] moduleTypes) {
        return getServerInstanceIDs(moduleTypes, null, null);
    }

    /**
     * Return ServerInstanceIDs of all registered server instances that support
     * specified module types and J2EE specification versions.
     *
     * @param moduleTypes  list of module types that the server instance must support.
     * @param specVersion  lowest J2EE specification version that the server instance must support.
     *
     * @return ServerInstanceIDs of all registered server instances that meet 
     *         the specified requirements.
     * @since 1.6
     */
    public String[] getServerInstanceIDs(Object[] moduleTypes, String specVersion) {
        return getServerInstanceIDs(moduleTypes, specVersion, null);
    }
    
    /**
     * Return ServerInstanceIDs of all registered server instances that support
     * specified module types, J2EE specification version and tools.
     *
     * @param moduleTypes  list of module types that the server instance must support.
     * @param specVersion  lowest J2EE specification version that the server instance must support.
     * @param tools        list of tools that the server instance must support.
     *
     * @return ServerInstanceIDs of all registered server instances that meet 
     *         the specified requirements.
     * @since 1.6
     */
    public String[] getServerInstanceIDs(Object[] moduleTypes, String specVersion, String[] tools) {
        List result = new ArrayList();
        String[] serverInstanceIDs = getServerInstanceIDs();
        for (int i = 0; i < serverInstanceIDs.length; i++) {
            J2eePlatform platform = getJ2eePlatform(serverInstanceIDs[i]);
            if (platform != null) {
                boolean isOk = true;
		if (moduleTypes != null) {
                    Set platModuleTypes = platform.getSupportedModuleTypes();
                    for (int j = 0; j < moduleTypes.length; j++) {
                        if (!platModuleTypes.contains(moduleTypes[j])) {
                            isOk = false;
                        }
                    }
		}
                if (isOk && specVersion != null) {
                    Set platSpecVers = platform.getSupportedSpecVersions();
                    if (specVersion.equals(J2eeModule.J2EE_13)) { 
                        isOk = platSpecVers.contains(J2eeModule.J2EE_13) 
                                || platSpecVers.contains(J2eeModule.J2EE_14);
                    } else {
                        isOk = platSpecVers.contains(specVersion);
                    }
                }
                if (isOk && tools != null) {
                    for (int j = 0; j < tools.length; j++) {
                        if (!platform.isToolSupported(tools[j])) {
                            isOk = false;
                        }
                    }
                }
                if (isOk) {
                    result.add(serverInstanceIDs[i]);
                }
            }
        }
        return (String[])result.toArray(new String[result.size()]);
    }
    
    public String getServerInstanceDisplayName (String id) {
        return ServerRegistry.getInstance ().getServerInstance (id).getDisplayName ();
    }
    
    public String getServerID (String instanceId) {
        ServerInstance si = ServerRegistry.getInstance().getServerInstance(instanceId);
        if (si != null) {
            return si.getServer().getShortName();
        }
        return null;
    }
    
    /**
     * Determine if a server instance will attempt to use file deployment for a
     * J2eeModule.
     * 
     * @param instanceId The target instance's server id
     * @param mod The module to be deployed
     * @return Whether file deployment will be used
     * @since 1.27
     */
    public boolean canFileDeploy(String instanceId, J2eeModule mod) {
        boolean retVal = false;
        ServerInstance instance = ServerRegistry.getInstance().getServerInstance(instanceId);
        if (null != instance) {
            IncrementalDeployment incr = instance.getIncrementalDeployment();
            if (null != incr) {
                retVal = incr.canFileDeploy(null, mod);
            }
        }
        return retVal;
    }
    
    public String getDefaultServerInstanceID () {
        ServerString defInst = ServerRegistry.getInstance ().getDefaultInstance ();
        if (defInst != null) {
            ServerInstance si = defInst.getServerInstance();
            if (si != null) {
                return si.getUrl ();
            }
        }
        return null;
    }
    
    public String [] getInstancesOfServer (String id) {
        if (id != null) {
            Server server = ServerRegistry.getInstance().getServer(id);
            if (server != null) {
                ServerInstance sis [] = ServerRegistry.getInstance ().getServer (id).getInstances ();
                String ids [] = new String [sis.length];
                for (int i = 0; i < sis.length; i++) {
                    ids [i] = sis [i].getUrl ();
                }
                return ids;
            }
        }
        return new String[0];
    }
    
    public String [] getServerIDs () {
        Collection c = ServerRegistry.getInstance ().getServers ();
        String ids [] = new String [c.size ()];
        Iterator iter = c.iterator ();
        for (int i = 0; i < c.size (); i++) {
            Server s = (Server) iter.next ();
            ids [i] = s.getShortName ();
        }
        return ids;
    }
    
    /**
     * Return server instance's <code>J2eePlatform</code>.
     *
     * @param  serverInstanceID server instance ID.
     * @return <code>J2eePlatform</code> for the given server instance, <code>null</code> if
     *         server instance of the specified ID does not exist.
     * @since 1.5
     */
    public J2eePlatform getJ2eePlatform(String serverInstanceID) {
        ServerInstance serInst = ServerRegistry.getInstance().getServerInstance(serverInstanceID);
        if (serInst == null) return null;
        return J2eePlatform.create(serInst);
    }
    
    public String getServerDisplayName (String id) {
        return ServerRegistry.getInstance ().getServer (id).getDisplayName();
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
    
    public static interface Logger {
        public void log(String message);
    }
}
