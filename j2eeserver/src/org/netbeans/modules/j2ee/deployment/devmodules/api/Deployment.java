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

package org.netbeans.modules.j2ee.deployment.devmodules.api;

import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.impl.*;
import org.netbeans.modules.j2ee.deployment.impl.projects.*;
import org.netbeans.modules.j2ee.deployment.impl.ui.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;

/**
 *
 * @author  Pavel Buzek
 */
public final class Deployment {
    
    private static final int MAX_DEPLOY_PROGRESS = 5;

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
    public String deploy (J2eeModuleProvider jmp, boolean debugmode, String clientModuleUrl, String clientUrlPart) throws DeploymentException {
        
        DeploymentTargetImpl target = new DeploymentTargetImpl(jmp, clientModuleUrl);

        ServerString server = target.getServer();
        J2eeModule module = target.getModule();
        TargetModule[] modules = null;
        DeployProgressUI progress = new DeployProgressMonitor(false, true);  // modeless with stop/cancel buttons
        progress.startProgressUI(MAX_DEPLOY_PROGRESS);
        
        try {
            if (module == null) {
                progress.addError(getBundle("MSG_NoJ2eeModule"));
                throw new DeploymentException (getBundle("MSG_NoJ2eeModule"));
            }
            if (server == null || server.getServerInstance() == null) {
                progress.addError(getBundle("MSG_NoTargetServer"));
                throw new DeploymentException (getBundle("MSG_NoTargetServer"));
            }
            
            progress.recordWork(1);
            
            boolean serverReady = false;
            TargetServer targetserver = new TargetServer(target);

            if (alsoStartTargets || debugmode) {
                serverReady = targetserver.startTargets(debugmode, progress);
            } else { //PENDING: how do we know whether target does not need to start when deploy only
                serverReady = server.getServerInstance().start(progress);
            }
            if (! serverReady) {
                progress.addError(getBundle("MSG_StartServerFailed"));
                throw new DeploymentException (getBundle("MSG_StartServerFailed"));
            }
            
            progress.recordWork(2);
            modules = targetserver.deploy(progress);
            progress.recordWork(MAX_DEPLOY_PROGRESS-1);
            
        } catch (Exception ex) {
            throw new DeploymentException (getBundle("MSG_DeployFailed"));
        }
        
        if (modules != null && modules.length > 0) {
            target.setTargetModules(modules);
            progress.recordWork(MAX_DEPLOY_PROGRESS);
        } else {
            throw new DeploymentException ("Some other error.");
        }
        return target.getClientUrl(clientUrlPart);
    }
    
    private String getBundle(String key) {
        return java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ant/Bundle").getString(key); // NOI18N
    }
    
    public static final class DeploymentException extends Exception {
        private DeploymentException (String msg) {
            super (msg);
        }
    }
    
    public String [] getServerInstanceIDs () {
        return InstanceProperties.getInstanceList ();
    }
    
    public String getServerInstanceDisplayName (String id) {
        return ServerRegistry.getInstance ().getServerInstance (id).getDisplayName ();
    }
    
    public String getServerID (String instanceId) {
        return ServerRegistry.getInstance ().getServerInstance (instanceId).getServer ().getShortName ();
    }
    
    public String getDefaultServerInstanceID () {
        return ServerRegistry.getInstance ().getDefaultInstance ().getServerInstance ().getUrl ();
    }
    
    public String [] getInstancesOfServer (String id) {
        ServerInstance sis [] = ServerRegistry.getInstance ().getServer (id).getInstances ();
        String ids [] = new String [sis.length];
        for (int i = 0; i < sis.length; i++) {
            ids [i] = sis [i].getUrl ();
        }
        return ids;
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
    
    public String getServerDisplayName (String id) {
        return ServerRegistry.getInstance ().getServer (id).getShortName ();
    }
}
