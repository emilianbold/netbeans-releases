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

import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.status.*;
import org.netbeans.modules.j2ee.deployment.plugins.spi.*;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.cookies.InstanceCookie;
import java.util.*;
import java.io.*;
import org.openide.nodes.Node;

public class ServerInstance implements Node.Cookie {
    
    final String url;
    final Server server;
    DeploymentManager manager;
    final Collection targets = new HashSet();
    
    // PENDING how to manage connected/disconnected servers with the same manager?
    // maybe concept of 'default unconnected instance' is broken?
    public ServerInstance(Server server, String url, DeploymentManager manager) {
        this.server = server; this.url = url; this.manager = manager;
    }
    
    public String getDisplayName() {
        return server.getDisplayName() + "(" + url + ")";
    }
    
    public Server getServer() {
        return server;
    }
    
    public String getUrl() {
        return url;
    }
    
    public DeploymentManager getDeploymentManager() {
        return manager;
    }

    public void refresh() { 
		FileObject fo = ServerRegistry.getInstanceFileObject(url);
		String username = (String) fo.getAttribute(ServerRegistry.USERNAME_ATTR);
		String password = (String) fo.getAttribute(ServerRegistry.PASSWORD_ATTR);
		try 
		{
			manager = server.getDeploymentManager(url, username, password);
		}
		catch(javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException e) {
			throw new RuntimeException(e.toString());
		}
	}
	
    public StartServer getStartServer() {
        StartServer ret = server.getStartServer();
        if (ret == null) return null;
        ret.setDeploymentManager(manager);
        return ret;
    }
   
    // PENDING use targets final variable?
    public ServerTarget[] getTargets() {
		System.out.println(getDisplayName()+".getTargets()");
        Target[] targs = manager.getTargets();
        if(targs == null) return new ServerTarget[0];
        ServerTarget[] ret = new ServerTarget[targs.length];
        for(int i = 0; i < ret.length; i++) {
            ret[i] = new ServerTarget(this, targs[i]);
			System.out.print("targets["+i+"]="+targs[i].getName());
		}
		
        return ret;
    }
    
    public IncrementalDeployment getIncrementalDeployment() {
        IncrementalDeployment ret = server.getIncrementalDeployment();
		if (ret != null)
			ret.setDeploymentManager(manager);
        return ret;
    }
    
    public InplaceDeployment getInplaceDeployment() {
        InplaceDeployment ret = server.getInplaceDeployment();
		if (ret != null)
			ret.setDeploymentManager(manager);
        return ret;
    }
    
    public TargetNameResolver getTargetResolver() {
        TargetNameResolver ret = server.getTargetResolver();
		if (ret != null)
			ret.setDeploymentManager(manager);
        return ret;
    }
}
