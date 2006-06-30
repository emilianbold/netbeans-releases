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

package org.netbeans.modules.j2ee.genericserver;

import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Adamek
 */
public class GSDeploymentFactory implements DeploymentFactory {

    public static final String GENERIC_SERVER_PREFIX = "generic"; // NOI18N
    
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
            throw new DeploymentManagerCreationException("Invalid URI:" + uri); // NOI18N
        }
        return new GSDeploymentManager();
    }
    
    public DeploymentManager getDisconnectedDeploymentManager(String str) throws DeploymentManagerCreationException {
        return new GSDeploymentManager();
    }
    
    public String getProductVersion() {
        return "0.1"; // NOI18N
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(GSDeploymentFactory.class, "TXT_DisplayName"); // NOI18N
    }
    
}
