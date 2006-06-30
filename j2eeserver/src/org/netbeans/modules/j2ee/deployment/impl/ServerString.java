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

package org.netbeans.modules.j2ee.deployment.impl;

import javax.enterprise.deploy.spi.Target;

public class ServerString implements java.io.Serializable {

    private final String plugin;
    private final String instance;
    private final String[] targets;
    private final transient ServerInstance serverInstance;
    private transient String[] theTargets;
    private static final long serialVersionUID = 923457209372L;

    public ServerString(String plugin, String instance, String[] targets) {
        if (targets == null)
            this.targets = new String[0];
        else 
            this.targets = targets;
        this.plugin = plugin; this.instance = instance; this.serverInstance = null;
    }
    
    public ServerString(Server server) {
        this.plugin = server.getShortName();
        this.instance = null;
        this.targets = new String[0];
        this.serverInstance = null;
    }
    
    public ServerString(ServerInstance instance) {
	assert instance != null;
        this.plugin = instance.getServer().getShortName();
        this.instance = instance.getUrl();
        this.serverInstance = instance;

        this.targets = null;
        /*if (! instance.isRunning()) {
            this.targets = new String[0];
            return;
        }
        
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
        }*/
    }
    
    public ServerString(ServerTarget target) {
        this.plugin = target.getInstance().getServer().getShortName();
        this.instance = target.getInstance().getUrl();
        this.targets = new String[] { target.getName() };
        this.serverInstance = null;
    }
    
    public ServerString(ServerInstance instance, String targetName) {
        this.plugin = instance.getServer().getShortName();
        this.instance = instance.getUrl();
        this.serverInstance = instance;
        if (targetName != null && ! "".equals(targetName.trim())) //NOI18N
            this.targets = new String[] { targetName };
        else
            this.targets = null;
    }
    
    public String getPlugin() {
        return plugin;
    }
    
    public String getUrl() {
        return instance;
    }
    
    public String[] getTargets() {
        return getTargets(false);
    }

    public String[] getTargets(boolean concrete) {
        if (! concrete) {
            if (targets == null) return new String[0];
            return targets;
         }

        if (targets != null && targets.length > 0)
            return targets;

        if (theTargets != null)
            return theTargets;
        
        ServerTarget[] serverTargets = getServerInstance().getTargets();
        theTargets = new String[serverTargets.length];
        for (int i=0; i<theTargets.length; i++)
            theTargets[i] = serverTargets[i].getName();
        return theTargets;
    }

    public Server getServer() {
        return ServerRegistry.getInstance().getServer(plugin);
    }
    
    public ServerInstance getServerInstance() {
        if (serverInstance != null)
            return serverInstance;
        return ServerRegistry.getInstance().getServerInstance(instance);
    }
    
    public String toString() {
        if (targets == null) return "Server " + plugin + " Instance " + instance + " Targets none"; // NOI18N
        return "Server " + plugin + " Instance " + instance + " Targets " + targets.length; // NOI18N
    }
    
    public static ServerString fromTarget(ServerInstance instance, Target target) {
        return new ServerString(new ServerTarget(instance, target));
    }
    
    public Target[] toTargets() {
        String[] targetNames = getTargets(true);
        Target[] ret = new Target[targetNames.length];
        for (int i=0; i<targetNames.length; i++)
            ret[i] = getServerInstance().getServerTarget(targetNames[i]).getTarget();
        return ret;
    }
}
