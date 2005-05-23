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
package org.netbeans.modules.j2ee.websphere6;

import java.io.*;

import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.factories.*;
import javax.enterprise.deploy.spi.exceptions.*;

import org.openide.util.NbBundle;

import org.netbeans.modules.j2ee.websphere6.util.WSDebug;

/**
 * The main entry point to the plugin. Keeps the required static data for the 
 * plugin and returns the DeploymentManagers required for deployment and 
 * configuration. Does not directly perform any interaction with the server.
 * 
 * @author Kirill Sorokin
 */
public class WSDeploymentFactory implements DeploymentFactory {
    
    // additional properties that are stored in the InstancePropeties object
    public static final String SERVER_ROOT_ATTR = "serverRoot";        // NOI18N
    public static final String DOMAIN_ROOT_ATTR = "domainRoot";        // NOI18N
    public static final String IS_LOCAL_ATTR = "isLocal";              // NOI18N
    public static final String HOST_ATTR = "host";                     // NOI18N
    public static final String PORT_ATTR = "port";                     // NOI18N
    public static final String DEBUGGER_PORT_ATTR = "debuggerPort";    // NOI18N
    public static final String SERVER_NAME_ATTR = "serverName";        // NOI18N
    
    /**
     * The singleton instance of the factory
     */
    private static WSDeploymentFactory instance;
    
    /**
     * The singleton factory method
     * 
     * @return the singleton instance of the factory
     */
    public static synchronized DeploymentFactory create() {
        // if the instance is not initialized yet - create it
        if (instance == null) {
            instance = new WSDeploymentFactory();
        }
        
        // return the instance
        return instance;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // DeploymentFactory implementation
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Returns a wrapper for the connected deployment manager
     * 
     * @return a connected DeploymentManager implementation
     */
    public DeploymentManager getDeploymentManager(String uri, String username, 
            String password) throws DeploymentManagerCreationException {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("getDeploymentManager(" + uri + ", " +      // NOI18N
                    username + ", " + password + ")");                 // NOI18N
        
        // return a new deployment manager
        return new WSDeploymentManager(uri, username, password);
    }
    
    /**
     * Returns a wrapper for the disconnecter deployment manager
     * 
     * @return a disconnected DeploymentManager implementation
     */
    public DeploymentManager getDisconnectedDeploymentManager(String uri) 
            throws DeploymentManagerCreationException {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("getDisconnectedDeploymentManager(" + uri + // NOI18N
                    ")");                                              // NOI18N
        
        // return a new deployment manager
        return new WSDeploymentManager(uri);
    }
    
    /**
     * Tells whether this deployment factory is capable to handle the server
     * identified by the given URI
     * 
     * @param uri the server URI
     * @return can or cannot handle the URI
     */
    public boolean handlesURI(String uri) {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("handlesURI(" + uri + ")");                 // NOI18N
        
        return uri == null ? false : uri.startsWith(
                "deployer:WebSphere:");                                // NOI18N
    }
    
    /**
     * Returns the product version of the deployment factory
     * 
     * @return the product version
     */
    public String getProductVersion() {
        return NbBundle.getMessage(WSDeploymentFactory.class, 
                "TXT_productVersion");                                 // NOI18N
    }
    
    /**
     * Returns the deployment factory dysplay name
     * 
     * @return display name
     */
    public String getDisplayName() {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("getDisplayName()");                        // NOI18N
        
        return NbBundle.getMessage(WSDeploymentFactory.class, 
                "TXT_displayName");                                    // NOI18N
    }
}