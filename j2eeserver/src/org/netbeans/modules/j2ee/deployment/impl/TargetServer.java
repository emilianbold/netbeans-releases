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
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.status.*;
import javax.enterprise.deploy.spi.exceptions.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.j2ee.deployment.impl.ui.DeployProgressUI;
import org.openide.ErrorManager;
import org.netbeans.modules.j2ee.deployment.execution.DeploymentTarget;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;

// import org.netbeans.api.debugger.DebuggerInfo;
import java.util.Timer;
import java.util.TimerTask;

import java.util.*;
import java.io.*;
import javax.enterprise.deploy.model.DeployableObject;

/**
 * Encapsulates a set of ServerTarget(s), provides a wrapper for deployment
 * help.  This is a throw away object, that get created and used within
 * scope of a deployment execution only.
 * The usage is for executor to call procesLastTargetModules(TargetModule[]) to
 * build a list of TargetModuleID's known by the IDE to have been deployed and
 * their oldest timestamp.
 */
public class TargetServer {
    
    private Target[] targets;
    private final ServerInstance instance;
    private final DeploymentTarget dtarget;
    private IncrementalDeployment incremental; //null value signifies don't do incremental
    private boolean debugMode = false;
    private Set deployedRootTMIDs = new HashSet(); // type TargetModule
    private Set distributeTargets = new HashSet();
    private TargetModule[] redeployTargetModules = null;
    private File application = null;
    
    public TargetServer(DeploymentTarget target) {
        this.dtarget = target;
        this.instance = dtarget.getServer().getServerInstance();
    }
    
    private void init(DeployProgressUI ui) {
        if (targets != null)
            return;
        
        if (instance.getStartServer().isAlsoTargetServer(null)) {
            if (debugMode) {
                instance.startDebugTarget(null, ui);
            } else {
                instance.start(ui);
            }
        }
        
        this.targets = dtarget.getServer().toTargets();
        
        // see if we want and can incremental
        if (dtarget.doFastDeploy()) {
            incremental = instance.getIncrementalDeployment();
            if (incremental != null && ! checkServiceImplementations())
                incremental = null;
        }
        
        processLastTargetModules();
    }
    
    private boolean canFileDeploy(Target[] targetz, DeployableObject deployable) {
        if (targetz == null || targetz.length != 1) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, NbBundle.getMessage(
            TargetServer.class, "MSG_MoreThanOneIncrementalTargets"));
            return false;
        }
        
        if (!instance.getIncrementalDeployment().canFileDeploy(targetz[0], deployable))
            return false;
        
        return true;
    }
    
    private boolean canFileDeploy(TargetModule[] targetModules, DeployableObject deployable) {
        if (targetModules == null || targetModules.length != 1) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, NbBundle.getMessage(
            TargetServer.class, "MSG_MoreThanOneIncrementalTargets"));
            return false;
        }
        
        if (!instance.getIncrementalDeployment().canFileDeploy(targetModules[0].getTarget(), deployable))
            return false;
        
        return true;
    }
    
    private AppChangeDescriptor distributeChanges(TargetModule targetModule, DeployProgressUI ui) {
        ServerFileDistributor sfd = new ServerFileDistributor(instance, dtarget);
        ui.setProgressObject(sfd);
        ModuleChangeReporter mcr = dtarget.getModuleChangeReporter();
        AppChangeDescriptor acd = sfd.distribute(targetModule, mcr);
        return acd;
    }
    
    private File initialDistribute(Target target, DeployProgressUI ui) {
        InitialServerFileDistributor sfd = new InitialServerFileDistributor(dtarget, target);
        ui.setProgressObject(sfd);
        return sfd.distribute();
    }
    
    private boolean checkServiceImplementations() {
        String missing = null;
        if (instance.getServer().getDeploymentPlanSplitter() == null)
            missing = DeploymentPlanSplitter.class.getName();
        
        if (missing != null) {
            String msg = NbBundle.getMessage(ServerFileDistributor.class, "MSG_MissingServiceImplementations", missing);
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL,  msg);
            return false;
        }
        
        return true;
    }
    
    /**
     * Process last deployment TargetModuleID's for eligibility and oldest timestamp
     */
    private void processLastTargetModules() {
        TargetModule[] targetModules = dtarget.getTargetModules();
        if (targetModules == null || targetModules.length == 0) {
            for (int i=0; i<targets.length; i++) {
                distributeTargets.add(targets[i]);
            }
            return;
        }
        
        // existing TMID's
        DeploymentManager dm = instance.getDeploymentManager();
        Map availables = new HashMap();
        try {
            ModuleType type = (ModuleType) dtarget.getModule().getModuleType();
            TargetModuleID[] ids = dm.getAvailableModules(type, targets);
            for (int i=0; i<ids.length; i++) {
                availables.put(ids[i].getModuleID(), ids[i]);
            }
        } catch (TargetException te) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, te);
        }
        
        Set targetNames = new HashSet();
        for (int i=0; i<targets.length; i++) targetNames.add(targets[i].getName());
        
        Set toRedeploy = new HashSet();
        for (int i=0; i<targetModules.length; i++) {
            // not my module
            if (! targetModules[i].getInstanceUrl().equals(instance.getUrl()) ||
            ! targetNames.contains(targetModules[i].getTargetName()))
                continue;
            
            TargetModuleID tmID = (TargetModuleID) availables.get(targetModules[i].getId());
            
            // no longer a deployed module on server
            if (tmID == null) {
                Target target = targetModules[i].findTarget();
                if (target != null)
                    distributeTargets.add(target);
            } else {
                targetModules[i].initDelegate(tmID);
                toRedeploy.add(targetModules[i]);
            }
        }
        
        // check if redeploy not suppported and not incremental then transfer to distribute list
        if (incremental == null && getApplication() == null) {
            toRedeploy = Collections.EMPTY_SET;
        } else if (incremental == null) {
            long lastModified = getApplication().lastModified();
            for (Iterator j=toRedeploy.iterator(); j.hasNext();) {
                TargetModule deployed = (TargetModule) j.next();
                if (lastModified >= deployed.getTimestamp()) {
                    //transfer to distribute
                    if (! dm.isRedeploySupported()) {
                        distributeTargets.add(deployed.findTarget());
                        j.remove();
                    }
                } else {
                    // no need to redeploy
                    j.remove();
                }
            }
        }
        
        redeployTargetModules = (TargetModule[]) toRedeploy.toArray(new TargetModule[toRedeploy.size()]);
    }
    
    private File getApplication() {
        if (application != null) return application;
        try {
            FileObject archiveFO = dtarget.getModule().getArchive();
            if (archiveFO == null) return null;
            application = FileUtil.toFile(archiveFO);
            return application;
        } catch (IOException ioe) {
            ErrorManager.getDefault().log(ErrorManager.EXCEPTION, ioe.getMessage());
            return null;
        }
    }
    
    public boolean startTargets(boolean debugMode, DeployProgressUI ui) {
        this.debugMode = debugMode;
        init(ui);
        if (debugMode) {
            for (int i=0; i<targets.length; i++)
                if (! instance.startDebugTarget(targets[i], ui))
                    return false;
        } else {
            for (int i=0; i<targets.length; i++)
                if (! instance.startTarget(targets[i], ui))
                    return false;
        }
        return true;
    }
    
    private boolean setProgressObject(DeployProgressUI progUI, ProgressObject obj) {
        return progUI.setProgressObject(obj);
    }
    
    private class DistributeEventHandler implements ProgressListener {
        DeployProgressUI ui;
        ProgressObject po;
        
        public DistributeEventHandler(DeployProgressUI ui, ProgressObject po) {
            this.ui = ui;
            this.po = po;
            ui.setProgressObject(po);
            po.addProgressListener(this);
        }
        public void handleProgressEvent(ProgressEvent progressEvent) {
            DeploymentStatus status = progressEvent.getDeploymentStatus();
            StateType state = status.getState();
            
            System.err.println("mw DistributeEventHandler.handleProgressEvent state= " + state);
            
            if (state == StateType.COMPLETED) {
                TargetModuleID[] modules = po.getResultTargetModuleIDs();
                modules = saveRootTargetModules(modules);
                po = null;
                
                System.err.println("Distributed modules: "+Arrays.asList(modules));
                
                ProgressObject startPO = instance.getDeploymentManager().start(modules);
                ui.setProgressObject(startPO);
                new StartEventHandler(startPO);
                
                final Timer timer2 = new Timer();
                final ProgressObject startObj = startPO;
                timer2.schedule(new  TimerTask() {
                    public void run() {
                        System.out.println("TimerTask on command: " + startObj.getDeploymentStatus().getCommand()
                        + " completed= " + startObj.getDeploymentStatus().isCompleted());
                        if (startObj.getDeploymentStatus().isRunning()) {
                            wakeUp();
                        }
                        timer2.cancel(); //Terminate the timer thread
                    }
                }
                , 120000);
                
            } // end of if (state == StateType.COMPLETED)
            else if (state == StateType.FAILED) {
                wakeUp();
            } // end of else if (state == StateType.FAILED)
        }
    }
    
    private class IncrementalEventHandler implements ProgressListener {
        ProgressObject po;
        
        public IncrementalEventHandler(ProgressObject po) {
            this.po = po;
        }
        public void handleProgressEvent(ProgressEvent progressEvent) {
            StateType state = progressEvent.getDeploymentStatus().getState();
            if (state == StateType.COMPLETED) {
                TargetModuleID[] modules = po.getResultTargetModuleIDs();
                saveRootTargetModules(modules);
                po = null;
                wakeUp();
            }
            else if (state == StateType.FAILED) {
                po = null;
                wakeUp();
            }
        }
    }
    
    private class StartEventHandler implements ProgressListener {
        ProgressObject startPO;
        
        public StartEventHandler(ProgressObject startPO) {
            this.startPO = startPO;
            startPO.addProgressListener(this);
        }
        public void handleProgressEvent(ProgressEvent progressEvent) {
            DeploymentStatus status = progressEvent.getDeploymentStatus();
            StateType state = status.getState();
            
            System.err.println("mw StartEventHandler.handleProgressEvent state= " + state);
            
            if (state == StateType.COMPLETED) {
                TargetModuleID[] modules = startPO.getResultTargetModuleIDs();
                
                System.err.println("Started modules: " + Arrays.asList(modules));
                for(int i = 0; i < modules.length; i++) {
                    System.err.println("  URL=" + modules[i].getWebURL());
                }
                
                wakeUp();
                
            } // end of if (state == StateType.COMPLETED)
            else if (state == StateType.FAILED) {
                wakeUp();
            } // end of else if (state == StateType.FAILED)
        }
    }
    
    public synchronized void wakeUp() {
        notifyAll();
    }
    
    public boolean sleep() {
        return sleep(0);
    }
    public synchronized boolean sleep(long timeout) {
        try {
            long t0 = System.currentTimeMillis();
            this.wait(timeout);
            return ((System.currentTimeMillis() - t0) < timeout);
        } catch (Exception e) {
            return false;
        } // end of try-catch
    }
    
    //collect root modules into TargetModule with timestamp
    private TargetModuleID[] saveRootTargetModules(TargetModuleID [] modules) {
        long timestamp = System.currentTimeMillis();
        Set originals = new HashSet();
        for (int i=0; i<modules.length; i++) {
            if (modules[i].getParentTargetModuleID() == null) {
                String id = modules[i].getModuleID();
                String targetName = modules[i].getTarget().getName();
                TargetModule tm = new TargetModule(id, instance.getUrl(), targetName, timestamp, modules[i]);
                deployedRootTMIDs.add(tm);
                originals.add(modules[i]);
            }
        }
        return (TargetModuleID[]) originals.toArray(new TargetModuleID[originals.size()]);
    }
    
    public TargetModule[] deploy(DeployProgressUI progressUI) throws IOException {
        init(progressUI);
        
        ProgressObject progressObject = null;
        
        // save configuration file if not already save
        // Note: configuration is not DO so will not be saved automatically on execute
        try {
            dtarget.getDeploymentConfigurationProvider().saveOnDemand();
        } catch (IOException ioe) {
            ErrorManager.getDefault().log(ErrorManager.EXCEPTION, ioe.getMessage());
            return null;
        }
        
        File plan = dtarget.getConfigurationFile();
        DeployableObject deployable = dtarget.getDeploymentConfigurationProvider().getDeployableObject(null);

        // handle initial file deployment or distribute
        if (distributeTargets.size() > 0) {
            Target[] targetz = (Target[]) distributeTargets.toArray(new Target[distributeTargets.size()]);
            
            if (incremental != null && canFileDeploy(targetz, deployable)) {
                InitialServerFileDistributor sfd = new InitialServerFileDistributor(dtarget, targetz[0]);
                progressUI.setProgressObject(sfd);
                File dir = sfd.distribute();
                if (dir == null)
                    return new TargetModule[0];
                progressObject = incremental.initialDeploy(targetz[0], deployable, dtarget.getDeploymentConfigurationProvider().getDeploymentConfiguration(), dir);
                new DistributeEventHandler(progressUI, progressObject);
                //System.out.println("incrementalDeploy: Status="+state);
                if (!sleep(120000) )
                    ErrorManager.getDefault().log(ErrorManager.WARNING, "incrementalDeploy: timeout or interrupted!");
                StateType state = progressObject.getDeploymentStatus().getState();
                if (state == StateType.FAILED) {
                    sfd.cleanup();
                }

/*                if (state !=  StateType.COMPLETED && state != StateType.FAILED) {
                    //System.out.println("Waiting on incrementalDeploy of ="+Arrays.asList(redeployTargetModules));
                    if (!sleep(120000) )
                        ErrorManager.getDefault().log(ErrorManager.WARNING, "incrementalDeploy: timeout or interrupted!");
                } else if (state == StateType.FAILED) {
                    sfd.cleanup();
                }
 **/
            } else {
                if (getApplication() != null) {
                    //System.err.println("  Distributing " + application + " to " + Arrays.asList(targetz));
                    progressObject = instance.getDeploymentManager().distribute(targetz, getApplication(), plan);
                    new DistributeEventHandler(progressUI, progressObject);
                    sleep();
                } else {
                    progressUI.addError(NbBundle.getMessage(TargetServer.class, "MSG_NoArchive"));
                }
            }
        }
        
        // handle increment or redeploy
        if (redeployTargetModules != null && redeployTargetModules.length > 0) {
            
            if (incremental != null && canFileDeploy(redeployTargetModules, deployable)) {
                AppChangeDescriptor acd = distributeChanges(redeployTargetModules[0], progressUI);
                if (acd == null)
                    return new TargetModule[0];
                
                if (anyChanged(acd)) {
                    //long t0 = System.currentTimeMillis();
                    
                    progressObject = incremental.incrementalDeploy(redeployTargetModules[0].delegate(), acd);
                    progressUI.setProgressObject(progressObject);
                    
                    if (progressObject == null) {
                        progressUI.addMessage(NbBundle.getMessage(TargetServer.class, "MSG_IncrementalDeployNoProgress"));
                    }
                    
                    IncrementalEventHandler h = new IncrementalEventHandler(progressObject);
                    progressObject.addProgressListener(h);
                    
                    if (!sleep(120000) )
                        ErrorManager.getDefault().log(ErrorManager.WARNING, "incrementalDeploy: timeout or interrupted!");
/*                    StateType state = progressObject.getDeploymentStatus().getState();
                    //System.out.println("incrementalDeploy: Status="+state);
                    if (state !=  StateType.COMPLETED && state != StateType.FAILED) {
                        //System.out.println("Waiting on incrementalDeploy of ="+Arrays.asList(redeployTargetModules));
                        if (!sleep(120000) )
                            ErrorManager.getDefault().log(ErrorManager.WARNING, "incrementalDeploy: timeout or interrupted!");
                    }
                    */
                    progressObject.removeProgressListener(h);
                    //System.out.println("incrementalDeploy time="+(System.currentTimeMillis()-t0)+" msecs.");
                } else { // return original target modules
                    return dtarget.getTargetModules();
                }
            } else { // redeploy
                TargetModuleID[] tmids = TargetModule.toTargetModuleID(redeployTargetModules);
                if (getApplication() != null) {
                    System.err.println("  Redeploying " + application + " with TMIDs " + Arrays.asList(redeployTargetModules));
                    System.err.println("  Plan " + plan);
                    
                    progressObject = instance.getDeploymentManager().redeploy(tmids, getApplication(), plan);
                    new DistributeEventHandler(progressUI, progressObject);
                    sleep();
                } else {
                    progressUI.addError(NbBundle.getMessage(TargetServer.class, "MSG_NoArchive"));
                }
            }
        }
        
        return (TargetModule[]) deployedRootTMIDs.toArray(new TargetModule[deployedRootTMIDs.size()]);
    }
    
    public static boolean anyChanged(AppChangeDescriptor acd) {
        System.out.println(acd);
        return (acd.manifestChanged() || acd.descriptorChanged() || acd.classesChanged()
        || acd.ejbsChanged() || acd.serverDescriptorChanged());
    }
}
