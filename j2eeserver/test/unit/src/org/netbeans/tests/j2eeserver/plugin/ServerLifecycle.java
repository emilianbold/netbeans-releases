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

package org.netbeans.tests.j2eeserver.plugin;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.shared.CommandType;

import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.tests.j2eeserver.plugin.jsr88.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.StartServer;

/**
 *
 * @author  nn136682
 */
public class ServerLifecycle extends StartServer {

    private DepManager dm;

    /** Creates a new instance of StartServer */
    public ServerLifecycle(DeploymentManager dm) {
        this.dm = (DepManager)dm;
    }
    
    public ServerDebugInfo getDebugInfo(Target target) {
        return null;
    }
    
    public boolean isAlsoTargetServer(Target target) {
        return true;
    }
    
    public boolean isDebuggable(Target target) {
        return false; //target.getName().equals("Target 1");
    }
    
    public boolean isRunning() {
        return dm.getState() == DepManager.RUNNING;
    }
    
    public boolean needsStartForConfigure() {
        return false;
    }
    
    public void setDeploymentManager(DeploymentManager manager) {
        this.dm = (DepManager) manager;
    }
    
    public ProgressObject startDebugging(Target target) {
        return dm.createServerProgress();
    }
    
    public ProgressObject startDeploymentManager() {
        final ServerProgress sp = dm.createServerProgress();
        Runnable r = new Runnable() {
            public void run() {
                try { Thread.sleep(500); //latency
                } catch (Exception e) {}
                dm.setState(DepManager.STARTING);
                sp.setStatusStartRunning("TestPluginDM: "+dm.getName()+" is starting.");
                try { Thread.sleep(2000); //super server starting time
                } catch (Exception e) {}
                if (dm.getTestBehavior() == DepManager.START_FAILED) {
                    dm.setState(DepManager.FAILED);
                    sp.setStatusStartFailed("TestPluginDM: "+dm.getName()+" startup failed");
                } else {
                    dm.setState(DepManager.RUNNING);
                    sp.setStatusStartCompleted("TestPluginDM "+dm.getName()+" startup finished");
                }
            }
        };
        
        (new Thread(r)).start();
        return sp;
    }
    
    public ProgressObject stopDeploymentManager() {
        final ServerProgress sp = dm.createServerProgress();
        Runnable r = new Runnable() {
            public void run() {
                try { Thread.sleep(500); //latency
                } catch (Exception e) {}
                dm.setState(DepManager.STOPPING);
                sp.setStatusStopRunning("TestPluginDM is preparing to stop "+dm.getName()+"...");
                try { Thread.sleep(2000); //super server stop time
                } catch (Exception e) {}
                if (dm.getTestBehavior() == DepManager.STOP_FAILED) {
                    dm.setState(DepManager.FAILED);
                    sp.setStatusStopFailed("TestPluginDM stop "+dm.getName()+" failed");
                } else {
                    dm.setState(DepManager.STOPPED);
                    sp.setStatusStopCompleted("TestPluginDM startup "+dm.getName()+" finished");
                }
            }
        };

        (new Thread(r)).start();
        return sp;
    }
    
    public boolean supportsStartDeploymentManager() {
        return true;
    }
    
    public boolean needsStartForAdminConfig() {
        return true;
    }
    
    public boolean needsStartForTargetList() {
        return true;
    }
    
}
