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

import javax.enterprise.deploy.spi.Target;

public class ServerString implements java.io.Serializable {
    
    private final String plugin;
    private final String instance;
    private final String[] targets;
    
    public ServerString(String plugin, String instance, String[] targets) {
        this.plugin = plugin; this.instance = instance; this.targets = targets;
    }
    
    public ServerString(ServerInstance instance) {
        this.plugin = instance.getServer().getShortName();
        this.instance = instance.getUrl();
        this.targets = null;
    }
    
    public ServerString(ServerTarget target) {
        this.plugin = target.getInstance().getServer().getShortName();
        this.instance = target.getInstance().getUrl();
        this.targets = new String[] { target.getName() };
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
    
    public ServerInstance getServerInstance() {
        return ServerRegistry.getInstance().getServerInstance(instance);
    }
    
    public TargetServer getServerTarget(String[] ids) {
		ServerInstance instance = getServerInstance();
        //Target[] targets = instance.getDeploymentManager().getTargets();
        return new TargetServer(instance,null/*targets*/,ids);
    }
    
    public String toString() {
        return "Server " + plugin + " Instance " + instance + " Targets " + targets.length;
        }
}
