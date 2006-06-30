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

package org.netbeans.tests.j2eeserver.plugin.jsr88;

import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.exceptions.*;

/**
 *
 * @author  gfink
 */
public class DepFactory implements DeploymentFactory {
    java.util.Map managers = new java.util.HashMap();

    /** Creates a new instance of DepFactory */
    public DepFactory() {
    }

    public javax.enterprise.deploy.spi.DeploymentManager getDeploymentManager(String str, String str1, String str2) throws javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException {
        DepManager manager = (DepManager) managers.get(str);
        if (manager == null){
            manager = new DepManager(str, str1, str2);
            managers.put(str, manager);
        }
        return manager;
    }
    
    public javax.enterprise.deploy.spi.DeploymentManager getDisconnectedDeploymentManager(String str) throws javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException {
        DepManager manager = (DepManager) managers.get(str);
        if (manager == null) {
            manager = new DepManager(str,"","");
            managers.put(str, manager);
        }
        return manager;
    }
    
    public String getDisplayName() {
        return "Sample JSR88 plugin";// PENDING parameterize this.
    }
    
    public String getProductVersion() {
        return "0.9";// PENDING version this plugin somehow?
    }
    
    public boolean handlesURI(String str) {
        return (str != null && str.startsWith("fooservice"));
    }
    
}
