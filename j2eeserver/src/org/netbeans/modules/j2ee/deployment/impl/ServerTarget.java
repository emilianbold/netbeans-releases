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

import javax.enterprise.deploy.spi.status.ProgressObject;
import org.openide.nodes.Node;
import javax.enterprise.deploy.spi.Target;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;

// PENDING use environment providers, not Cookies
// PENDING issue  --   Target <==> J2EEDomain relationship 1 to many, many to 1, 1 to 1, or many to many
public class ServerTarget implements Node.Cookie {

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
        
        StartServer ss = instance.getStartServer();
        if (ss != null) {
            return ss.isRunning(target);
        }
        return false;
    }
    
    public ProgressObject start() {
        StartServer ss = instance.getStartServer();
        if (ss != null && ss.supportsStartTarget(target)) {
            ProgressObject po = ss.startTarget(target);
            if (po != null) {
                return po;
            }
        }
        String name = target == null ? "null" : target.getName(); //NOI18N
        String msg = NbBundle.getMessage(ServerTarget.class, "MSG_StartStopTargetNotSupported", name);
        throw new UnsupportedOperationException(msg);
    }
    
    public ProgressObject stop() {
        StartServer ss = instance.getStartServer();
        if (ss != null && ss.supportsStartTarget(target)) {
            ProgressObject po = ss.stopTarget(target);
            if (po != null) {
                return po;
            }
        }
        String name = target == null ? "null" : target.getName(); //NOI18N
        String msg = NbBundle.getMessage(ServerTarget.class, "MSG_StartStopTargetNotSupported", name);
        throw new UnsupportedOperationException(msg);
    }
}
