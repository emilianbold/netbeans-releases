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
package org.netbeans.modules.j2ee.weblogic9;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;


/**
 *
 * @author Kirill Sorokin
 */
public class WLDeploymentFactory implements DeploymentFactory {
    
    // additional properties' names
    public static final String SERVER_ROOT_ATTR = "serverRoot"; // NOI18N
    public static final String DOMAIN_ROOT_ATTR = "domainRoot"; // NOI18N
    public static final String IS_LOCAL_ATTR = "isLocal"; // NOI18N
    public static final String HOST_ATTR = "host"; // NOI18N
    public static final String PORT_ATTR = "port"; // NOI18N
    public static final String DEBUGGER_PORT_ATTR = "debuggerPort"; // NOI18N
    
    private static WLDeploymentFactory instance;
    
    public static synchronized DeploymentFactory create() {
        if (instance == null) {
            instance = new WLDeploymentFactory();
        }
        return instance;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // DeploymentFactory implementation
    ////////////////////////////////////////////////////////////////////////////
    public DeploymentManager getDeploymentManager(String uri, String username, String password) throws DeploymentManagerCreationException {
        return new WLDeploymentManager(uri, username, password);
    }
    
    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        return new WLDeploymentManager(uri);
    }
    
    public boolean handlesURI(String uri) {
        return uri == null ? false : uri.startsWith("deployer:WebLogic:http://"); // NOI18N
    }
    
    public String getProductVersion() {
        return NbBundle.getMessage(WLDeploymentFactory.class, "TXT_productVersion");  // NOI18N
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(WLDeploymentFactory.class, "TXT_displayName");  // NOI18N
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Utility methods
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Checks whether the <code>possibleCause</code> class is the cause of the
     * <code>resultingException</code> Throwable
     */
    private static boolean isCause(Throwable resultingException, Class possibleCause) {
        if (possibleCause.isAssignableFrom(resultingException.getClass())) {
            return true;
        }
        
        if (resultingException.getCause() == null) {
            return false;
        } else {
            return isCause(resultingException.getCause(), possibleCause);
        }
    }
}
