/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * ConfigBeanCustomizer.java
 *
 */

package org.netbeans.modules.j2ee.deployment.plugins.api;

import javax.enterprise.deploy.spi.DeploymentManager;

/**
 *  A way for the IDE to store customized information about a server instance
 *  and make it available to a plugin.
 *
 * @author George FinKlang
 * @version 0.1
 */

public abstract class InstanceProperties {
    
    public static InstanceProperties getInstanceProperties(DeploymentManager manager) {
        return null; // PENDING 
    }

    public abstract void setProperty(String propname, String value);
    
    public abstract String getProperty(String propname);
    
    }
