/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.j2ee.deployment.impl;

import org.openide.nodes.Node;
import org.netbeans.modules.j2ee.deployment.plugins.spi.TargetNameResolver;
import javax.management.j2ee.Management;
import javax.enterprise.deploy.spi.Target;

// PENDING use environment providers, not Cookies
// PENDING issue  --   Target <==> J2EEDomain relationship 1 to many, many to 1, 1 to 1, or many to many
public class ServerTarget implements Node.Cookie {
    
    ServerInstance instance;
    String targetName;
    String[] names = new String[0];
    
    public ServerTarget(ServerInstance instance, Target target) {
        this.instance = instance;
        this.targetName = target.getName();
        TargetNameResolver resolver = instance.getTargetResolver();
        if(resolver != null) names = resolver.getManagedServerNames(targetName);
    }
    
    public String[] getServers() {
        return names;
    }
    
    public ServerInstance getInstance() {
        return instance;
    }
    
    public Management getManagement(String serverName) {
        return instance.getTargetResolver().getJ2eeManagement(serverName);
    }
    
    public String getName() {
        return targetName;
    }
}
