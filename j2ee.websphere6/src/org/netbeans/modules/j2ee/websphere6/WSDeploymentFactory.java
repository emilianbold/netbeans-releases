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

import org.netbeans.modules.j2ee.websphere6.util.WSDebug;
import java.io.File;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.openide.util.NbBundle;


/**
 *
 * @author Kirill Sorokin
 */
public class WSDeploymentFactory implements DeploymentFactory {
    
    // additional properties' names
    public static final String SERVER_ROOT_ATTR = "serverRoot"; // NOI18N
    public static final String DOMAIN_ROOT_ATTR = "domainRoot"; // NOI18N
    public static final String IS_LOCAL_ATTR = "isLocal"; // NOI18N
    public static final String HOST_ATTR = "host"; // NOI18N
    public static final String PORT_ATTR = "port"; // NOI18N
    public static final String DEBUGGER_PORT_ATTR = "debuggerPort"; // NOI18N
    public static final String SERVER_NAME_ATTR = "serverName"; // NOI18N
    
    private static WSDeploymentFactory instance;
    
    public static synchronized DeploymentFactory create() {
        if (instance == null) {
            instance = new WSDeploymentFactory();
        }
        return instance;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // DeploymentFactory implementation
    ////////////////////////////////////////////////////////////////////////////
    public DeploymentManager getDeploymentManager(String uri, String username, String password) throws DeploymentManagerCreationException {
        if (WSDebug.isEnabled())
            WSDebug.notify("getDeploymentManager(" + uri + ", " + username + ", " + password + ")");
        
        return new WSDeploymentManager(uri, username, password);
    }
    
    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        if (WSDebug.isEnabled())
            WSDebug.notify("getDisconnectedDeploymentManager(" + uri + ")");
        
        return new WSDeploymentManager(uri);
    }
    
    public boolean handlesURI(String uri) {
        if (WSDebug.isEnabled())
            WSDebug.notify("handlesURI(" + uri + ")");
        
        return uri == null ? false : uri.startsWith("deployer:WebSphere:"); // NOI18N
    }
    
    public String getProductVersion() {
        return NbBundle.getMessage(WSDeploymentFactory.class, "TXT_productVersion");  // NOI18N
    }
    
    public String getDisplayName() {
        if (WSDebug.isEnabled())
            WSDebug.notify("getDisplayName()");
        
        return NbBundle.getMessage(WSDeploymentFactory.class, "TXT_displayName");  // NOI18N
    }
}
