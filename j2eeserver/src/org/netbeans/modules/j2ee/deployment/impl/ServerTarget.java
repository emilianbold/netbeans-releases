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
import javax.enterprise.deploy.spi.Target;
//import javax.management.ObjectName;
//import javax.management.Notification;
//import javax.management.NotificationListener;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.deployment.impl.ui.DeployProgressUI;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;

// PENDING use environment providers, not Cookies
// PENDING issue  --   Target <==> J2EEDomain relationship 1 to many, many to 1, 1 to 1, or many to many
public class ServerTarget implements Node.Cookie {
    
    public static final String EVT_STARTING = "j2ee.state.starting";
    public static final String EVT_RUNNING = "j2ee.state.running";
    public static final String EVT_FAILED = "j2ee.state.failed";
    public static final String EVT_STOPPED = "j2ee.state.stopped";
    public static final String EVT_STOPPING = "j2ee.state.stopping";
    private static final Integer STATE_RUNNING = new Integer(1);

    ServerInstance instance;
    Target target;
    //PENDING: caching state, sync, display through icon and action list.
    
    public ServerTarget(ServerInstance instance, Target target) {
        this.instance = instance;
        this.target = target;
    }
    
    public ServerInstance getInstance() {
        return instance;
    }
    
    public String getName() {
        return target.getName();
    }
    
    public boolean hasWebContainerOnly() {
        Server server = instance.getServer();
        return (server.canDeployWars() && ! server.canDeployEars() && ! server.canDeployEjbJars());
    }
    
    public Target getTarget() {
        return target;
    }
    
    public boolean isAlsoServerInstance() {
        return instance.getStartServer().isAlsoTargetServer(target);
    }
    
    public boolean isRunning() {
        if (isAlsoServerInstance())
            return instance.isRunning();
        return false;
    }
}
