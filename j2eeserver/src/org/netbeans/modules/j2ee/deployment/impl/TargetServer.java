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

import org.openide.filesystems.FileUtil;

import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.status.*;
import javax.enterprise.deploy.spi.exceptions.*;
import org.netbeans.modules.j2ee.deployment.plugins.spi.*;
import org.netbeans.modules.j2ee.deployment.impl.ui.DeployProgressUI;

// import org.netbeans.api.debugger.DebuggerInfo;
import java.util.Timer;
import java.util.TimerTask;

import java.util.*;
import java.io.*;

/* Encapsulates a set of ServerTarget(s), provides a wrapper for deployment
 * help */
public class TargetServer {
    
    Target[] targets;
    ServerInstance instance;
    String[] ids;
    TargetModuleID[] modules = null;
    ProgressObject progressObject;
    
    public TargetServer(ServerInstance instance, Target[] targets, String[] ids) {
        this.targets = targets; this.instance = instance; this.ids = ids;
    }
    private Target[] getTargets() 
	{
        if (targets == null)
			targets = instance.getDeploymentManager().getTargets();
		return targets;
	}
    private TargetModuleID[] getModules() {
        if(modules == null && ids != null) {
            try {
                modules = instance.getDeploymentManager().getAvailableModules(ModuleType.EAR /* PENDING get module type here */,
                getTargets());
                Collection idSet = Arrays.asList(ids);
                Collection idList = new LinkedList();
                for(int i = 0; i < modules.length; i++) {
                    if(idSet.contains(modules[i].getModuleID())) idList.add(ids[i]);
                }
                if(idSet.size() == targets.length) {
                    modules = new TargetModuleID[targets.length];
                    idList.toArray(modules);
                }
            } catch (TargetException te) {
                // PENDING log this exception.
				te.printStackTrace();
            }
        }
        return modules;
    }
    
    public boolean canIncrementallyRedeploy(IncrementalDeployment incremental) {
        getModules();
        if(modules != null)
            return incremental.canIncrementallyRedeploy(modules);
        else return false;
    }
    
    public File getDeploymentDirectory(InplaceDeployment inplace) {
        return inplace.getDirectory(getModules());
    }
    
    public boolean canReceiveDirectory(InplaceDeployment inplace) {
        return inplace.canReceiveDirectory(getTargets());
    }
		
    public void start(DeployProgressUI ui) {
		start(ui, false);
	}
    public void /*DebuggerInfo[] */ startDebugging(DeployProgressUI ui)
    {
      return ;//start_private(ui, true);
    }

    public void start(DeployProgressUI ui, boolean debugMode) {
      start_private(ui, debugMode);
    }
    private void /* DebuggerInfo[] */ start_private(DeployProgressUI ui, boolean debugMode) {
        StartServer starter = instance.getStartServer();
        if(starter == null || ! starter.supportsStartDeploymentManager())
			return ;//null;

		int work = 0;
                // PENDING  Get a non-null ProgressObject
		progressObject = starter.startDeploymentManager();
                if ( progressObject == null) {
                    ui.addMessage("Starting deployment manager");
                    ui.addError("");    // NOI18N
                    ui.recordWork(++work);
                }
                else {
                    setProgressObject(ui, progressObject);
                }

		for (int i = 0; i < getTargets().length; i++) {
			if (debugMode)
				return ;// starter.startDebugging(getTargets()[i]);
			else {
                                // PENDING get a non-null progress object
				progressObject = starter.startServer(getTargets()[i]);
                                if ( progressObject == null) {
                                    ui.addMessage("Starting target: "+ getTargets()[i]);
                                    ui.addError("");    // NOI18N
                                    ui.recordWork(++work>2 ? 2 : work);
                                }
                                else {
                                    setProgressObject(ui, progressObject);
                                }
                        }
		}
       return ;//null;
    }
    
    private boolean setProgressObject(DeployProgressUI progUI, ProgressObject obj) {
        return progUI.setProgressObject(obj);
    }
    
    public Map deploy(DeployProgressUI progressUI, File application, InputStream plan, InplaceDeployment inplace, IncrementalDeployment incremental, Map changeList) throws Exception {
        progressObject = null;
		
        getModules();
        if(incremental != null) {
            progressObject = incremental.incrementalRedeploy(modules, application, plan, changeList);
            setProgressObject(progressUI, progressObject);
        }
        else if (inplace != null) {
            progressObject = inplace.inPlaceDistribute(getTargets(), application, plan);
            setProgressObject(progressUI, progressObject);
        }
        else {
			System.out.println("Distributing "+application+" to "+java.util.Arrays.asList(getTargets()));

                        progressObject = instance.getDeploymentManager().distribute(getTargets(),new FileInputStream(application),plan);
                        setProgressObject(progressUI, progressObject);

			//<short-circuit>
			int count = 0, guard = 7, work = 3;
                        
                        final ProgressObject distObj = progressObject;
                        final Timer timer = new Timer();
                        timer.schedule(new  TimerTask() {
                            public void run() {
                                System.out.println("TimerTask dist command: " + distObj.getDeploymentStatus().getCommand()
                                    + " is completed: " + distObj.getDeploymentStatus().isCompleted());
                                if (!distObj.getDeploymentStatus().isCompleted()) {
                                    throw new RuntimeException("Call to distribute() takes too long...");
                                }
                                timer.cancel(); //Terminate the timer thread
                            }
                        }
                        , 15000);   

			
			TargetModuleID[] modules = progressObject.getResultTargetModuleIDs();
			System.out.println("Distributed modules: "+Arrays.asList(modules));

			progressObject = instance.getDeploymentManager().start(modules);
                        setProgressObject(progressUI, progressObject);
                        
                        final Timer timer2 = new Timer();
                        final ProgressObject startObj = progressObject;
                        timer2.schedule(new  TimerTask() {
                            public void run() {
                                System.out.println("TimerTask start command: " + startObj.getDeploymentStatus().getCommand()
                                    + " is completed: " + startObj.getDeploymentStatus().isCompleted());
                                if (!startObj.getDeploymentStatus().isCompleted()) {
                                    System.out.println("Call to start() takes too long...");
                                }
                                timer2.cancel(); //Terminate the timer thread
                            }
                        }
                        , 120000);
                        
			modules = progressObject.getResultTargetModuleIDs();
			System.out.println("Started modules: "+Arrays.asList(modules));

                        Map ret = new HashMap();
                        for(int i = 0; i < modules.length; i++)
                            ret.put(modules[i].getTarget(), modules[i].getModuleID());
                                    return ret;
                                    //</short-circuit>
		}
            Map ret = new HashMap();
            TargetModuleID[] ids = progressObject.getResultTargetModuleIDs();
            for(int i = 0; i < ids.length; i++)
                ret.put(ids[i].getTarget(),ids[i].getModuleID());
            ProgressObject st = instance.getDeploymentManager().start(progressObject.getResultTargetModuleIDs());
            setProgressObject(progressUI, st);
            return ret;
    }
}
