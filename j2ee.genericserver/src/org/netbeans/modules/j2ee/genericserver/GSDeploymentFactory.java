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

package org.netbeans.modules.j2ee.genericserver;

import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.openide.ErrorManager;

/**
 *
 * @author Martin Adamek
 */
public class GSDeploymentFactory implements DeploymentFactory {
    
    public static final String GENERIC_SERVER_PREFIX = "generic";
    
    private static DeploymentFactory instance;
    
    private static ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.j2ee.genericserver");  // NOI18N
    
    public static synchronized DeploymentFactory create() {
        if (instance == null) {
            if (err.isLoggable(ErrorManager.INFORMATIONAL)) err.log("Creating Generic Server Factory"); // NOI18N
            instance = new GSDeploymentFactory();
            DeploymentFactoryManager.getInstance().registerDeploymentFactory(instance);
        }
        return instance;
    }
    
    public boolean handlesURI(String str) {
        return str != null && str.startsWith(GENERIC_SERVER_PREFIX);
    }
    
    public DeploymentManager getDeploymentManager(String uri, String uname, String passwd) throws DeploymentManagerCreationException {
        if (!handlesURI(uri)) {
            throw new DeploymentManagerCreationException("Invalid URI:" + uri);
        }
        return new GSDeploymentManager();
    }
    
    public DeploymentManager getDisconnectedDeploymentManager(String str) throws DeploymentManagerCreationException {
        return new GSDeploymentManager();
    }
    
    public String getProductVersion() {
        return "0.1";
    }
    
    public String getDisplayName() {
        return "Generic Server";
    }
    
}
