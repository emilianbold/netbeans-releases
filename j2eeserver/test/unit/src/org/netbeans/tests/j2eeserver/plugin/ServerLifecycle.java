/*
 *                 Sun Public License Notice
 *
 * The contents of thisfile are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.tests.j2eeserver.plugin;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.shared.CommandType;

import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.tests.j2eeserver.plugin.jsr88.*;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;

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
