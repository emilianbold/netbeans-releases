/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.deployment.impl;

import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.shared.ModuleType;
import org.openide.ErrorManager;

public class ServerString implements java.io.Serializable {
    
    private final String plugin;
    private final String instance;
    private final String[] targets;
    private final transient ServerInstance serverInstance;
    
    public ServerString(String plugin, String instance, String[] targets) {
        this.plugin = plugin; this.instance = instance; this.targets = targets; this.serverInstance = null;
    }
    
    public ServerString(Server server) {
        this.plugin = server.getShortName();
        this.instance = null;
        this.targets = new String[0];
        this.serverInstance = null;
    }
    
    public ServerString(ServerInstance instance) {
        this.plugin = instance.getServer().getShortName();
        this.instance = instance.getUrl();
        this.serverInstance = instance;
        ServerTarget[] serverTargets;
        try {
            serverTargets = instance.getTargets();
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
            this.targets = new String[0];
            return;
        }
        this.targets = new String[serverTargets.length];
        for (int i=0; i<serverTargets.length; i++) {
            targets[i] = serverTargets[i].getName();
        }
    }
    
    public ServerString(ServerTarget target) {
        this.plugin = target.getInstance().getServer().getShortName();
        this.instance = target.getInstance().getUrl();
        this.targets = new String[] { target.getName() };
        this.serverInstance = null;
    }
    
    public String getPlugin() {
        return plugin;
    }
    
    public String getUrl() {
        return instance;
    }
    
    public String[] getTargets() {
        return targets;
    }
    
    public Server getServer() {
        return ServerRegistry.getInstance().getServer(plugin);
    }
    
    public ServerInstance getServerInstance() {
        if (serverInstance != null)
            return serverInstance;
        return ServerRegistry.getInstance().getServerInstance(instance);
    }
    
    /*public TargetServer getServerTarget(ModuleType type) {
        return new TargetServer(this, type);
    }*/
    
    public String toString() {
        if (targets == null) return "Server " + plugin + " Instance " + instance + " Targets none"; // NOI18N
        return "Server " + plugin + " Instance " + instance + " Targets " + targets.length; // NOI18N
    }
    
    public static ServerString fromTarget(ServerInstance instance, Target target) {
        return new ServerString(new ServerTarget(instance, target));
    }
    
    public Target[] toTargets() {
        Target[] ret = new Target[targets.length];
        for (int i=0; i<targets.length; i++)
            ret[i] = getServerInstance().getServerTarget(targets[i]).getTarget();
        return ret;
    }
}
