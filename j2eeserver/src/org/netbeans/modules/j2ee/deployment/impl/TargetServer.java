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
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.impl.ui.DeployProgressUI;
import org.openide.ErrorManager;
import org.netbeans.modules.j2ee.deployment.execution.DeploymentTarget;
import org.netbeans.modules.j2ee.deployment.execution.DeploymentConfigurationProvider;
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
 * scope of one deployment execution.
 *
 * Typical user are ServerExecutor and Debugger code, with the following general sequence:
 *
 *      TargetServer ts = new TargetServer(deploymentTarget);
 *      ts.startTargets(deployProgressUI);
 *      TargetModule[] tms = ts.deploy(deployProgressUI);
 *      deploymentTarget.setTargetModules(tms);
 */
public class TargetServer {
    
    private static final long DISTRIBUTE_TIMEOUT = 120000;
    private static final long INCREMENTAL_TIMEOUT = 60000;
    private static final long TIMEOUT = 60000;
    private static final TargetModule[] EMPTY_TARGETMODULE_ARRAY = new TargetModule[0];
    
    private Target[] targets;
    private final ServerInstance instance;
    private final DeploymentTarget dtarget;
    private IncrementalDeployment incremental; //null value signifies don't do incremental
    private boolean debugMode = false;
    private Set deployedRootTMIDs = new HashSet(); // type TargetModule
    private Set undeployTMIDs = new HashSet(); // TMID
    private Set distributeTargets = new HashSet(); //Target
    private TargetModule[] redeployTargetModules = null;
    private File application = null;
    private File currentContentDir = null;
    private String contextRoot = null;
    private ProgressHandler startEventHandler = null;
    
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

        try {
            FileObject contentFO = dtarget.getModule().getContentDirectory();
            if (contentFO != null) {
                currentContentDir = FileUtil.toFile(contentFO);
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
        }
        
        J2eeModuleProvider.ConfigSupport configSupport = dtarget.getConfigSupport();
        if (configSupport != null) {
            contextRoot = configSupport.getWebContextRoot();
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
    
    private AppChangeDescriptor distributeChanges(TargetModule targetModule, DeployProgressUI ui) throws IOException {
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
    
    // return list of TargetModule to redeploy
    private TargetModule[] checkUndeployForChangedReferences(Set toRedeploy) {
        // PENDING: what are changed references for ejbmod, j2eeapp???
        if (dtarget.getModule().getModuleType() == J2eeModule.WAR) {
            for (Iterator j=toRedeploy.iterator(); j.hasNext();) {
                TargetModule deployed = (TargetModule) j.next();
                File lastContentDir = (deployed.getContentDirectory() == null) ? null : new File(deployed.getContentDirectory());

                // content dir or context root changes since last deploy
                if ((currentContentDir != null && ! currentContentDir.equals(lastContentDir)) ||
                      (contextRoot != null && ! contextRoot.equals(deployed.getContextRoot()))) {
                    
                    undeployTMIDs.add(deployed.delegate());
                    distributeTargets.add(deployed.findTarget());
                    j.remove();
                }
            }
        }
        
        return (TargetModule[]) toRedeploy.toArray(new TargetModule[toRedeploy.size()]);
    }
    
    // return list of target modules to redeploy
    private TargetModule[] checkUndeployForSameReferences(Target[] targs) {
        return checkUndeployForSharedReferences(Collections.EMPTY_SET, targs, null);
    }
    private TargetModule[] checkUndeployForSharedReferences(Set toRedeploy, Target[] targs) {
        return checkUndeployForSharedReferences(toRedeploy, targs, null);
    }
    private TargetModule[] checkUndeployForSharedReferences(Set toRedeploy, Target[] targs, Map queryInfo) {
        TargetModuleIDResolver tmidResolver = instance.getTargetModuleIDResolver();

        // PENDING: what are changed references for ejbmod, j2eeapp???
        if (tmidResolver != null && contextRoot != null && targs.length > 0) {
            if (queryInfo == null) {
                queryInfo = new HashMap();
                queryInfo.put(TargetModuleIDResolver.KEY_CONTEXT_ROOT, contextRoot);
            }
            
            TargetModuleID[] haveSameReferences = tmidResolver.lookupTargetModuleID(queryInfo, targs);
            boolean shared = false;
            List maybeRedistributeWhenSharedDetected = new ArrayList();
            List maybeRemoveFromRedeployWhenSharedDetected = new ArrayList();
            for (int i=0; i<haveSameReferences.length; i++) {
                TargetModule hasSameReferences = new TargetModule(haveSameReferences[i]); 
                if (! toRedeploy.contains(hasSameReferences)) {
                    shared = true;
                } else {
                    // to transfer from redeploy to redistribute when sharing detected
                    maybeRemoveFromRedeployWhenSharedDetected.add(haveSameReferences[i]);
                    maybeRedistributeWhenSharedDetected.add(haveSameReferences[i].getTarget());
                }
            }
        
            if (shared) {
                //Note: undeploy all with same references, including the ones being considered to redeploy
                undeployTMIDs.addAll(Arrays.asList(haveSameReferences)); 

                //Note: for brand new module not known on any targets (toRedeploy empty) these transfer lists will be empty
                distributeTargets.addAll(maybeRedistributeWhenSharedDetected);
                toRedeploy.removeAll(maybeRemoveFromRedeployWhenSharedDetected);
            }
        }

        return (TargetModule[]) toRedeploy.toArray(new TargetModule[toRedeploy.size()]);
    }
    
    /**
     * Process last deployment TargetModuleID's for undeploy, redistribute, redeploy and oldest timestamp
     */
    private void processLastTargetModules() {
        TargetModule[] targetModules = dtarget.getTargetModules();

        // new module
        if (targetModules == null || targetModules.length == 0) {
            distributeTargets.addAll(Arrays.asList(targets));
            checkUndeployForSameReferences(targets);
            return;
        }
        
        // existing TMID's
        DeploymentManager dm = instance.getDeploymentManager();
        Map availables = new HashMap();
        try {
            ModuleType type = (ModuleType) dtarget.getModule().getModuleType();
            TargetModuleID[] ids = dm.getAvailableModules(type, targets);
            for (int i=0; i<ids.length; i++) {
                availables.put(keyOf(ids[i]), ids[i]);
            }
        } catch (TargetException te) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, te);
        }
        
        Set targetNames = new HashSet();
        for (int i=0; i<targets.length; i++) targetNames.add(targets[i].getName());
        
        Set toRedeploy = new HashSet(); //type TargetModule
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
        
        Target[] targs = TargetModule.toTarget(redeployTargetModules);
        redeployTargetModules = checkUndeployForChangedReferences(toRedeploy);

        targs = TargetModule.toTarget(redeployTargetModules);
        redeployTargetModules = checkUndeployForSharedReferences(toRedeploy, targs);
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
    
    private class DistributeEventHandler implements ProgressListener {
        DeployProgressUI ui;
        ProgressObject po;
        
        public DistributeEventHandler(DeployProgressUI ui, ProgressObject po) {
            this.ui = ui;
            this.po = po;
        }
        public void handleProgressEvent(ProgressEvent progressEvent) {
            DeploymentStatus status = progressEvent.getDeploymentStatus();
            StateType state = status.getState();

            if (state == StateType.COMPLETED) {
                // this check is to avoid NPE is case the handler is misused, e.g.,
                // firing event multiple times and asynchronously
                if (po != null) {
                    handleCompletedDistribute(ui, po, false);
                    po = null;
                }
            } // end of if (state == StateType.COMPLETED)
            else if (state == StateType.FAILED) {
                wakeUp();
            } // end of else if (state == StateType.FAILED)
        }
    }
    
    private class IncrementalEventHandler implements ProgressListener {
        ProgressObject po;
        DeployProgressUI ui;
        
        public IncrementalEventHandler(DeployProgressUI ui, ProgressObject po) {
            this.po = po;
            this.ui = ui;
            
        }
        public void handleProgressEvent(ProgressEvent progressEvent) {
            StateType state = progressEvent.getDeploymentStatus().getState();
            if (state == StateType.COMPLETED) {
                if (po != null) {
                    TargetServer.this.handleCompletedDistribute(ui, po, true);
                    po = null;
                }
            }
            else if (state == StateType.FAILED) {
                po = null;
                wakeUp();
            }
        }
    }
    
    private class ProgressHandler implements ProgressListener {
        ProgressObject po;
        
        public ProgressHandler(ProgressObject po) {
            this.po = po;
            po.addProgressListener(this);
        }
        
        public void handleProgressEvent(ProgressEvent progressEvent) {
            DeploymentStatus status = progressEvent.getDeploymentStatus();
            StateType state = status.getState();
            if (state == StateType.COMPLETED || state == StateType.FAILED) {
                wakeUp();
            }
        }
        
        public void unregister() {
            if (po != null) {
                po.removeProgressListener(this);
                po = null;
            }
        }
    }
    
    private synchronized void wakeUp() {
        notifyAll();
    }
    
    private synchronized void sleep(long timeout) {
        try {
            long t0 = System.currentTimeMillis();
            this.wait(timeout);
            if ((System.currentTimeMillis() - t0) > timeout) {
                throw new RuntimeException(NbBundle.getMessage(TargetServer.class, "MSG_DeployTimeout", new Long(timeout/1000)));
            }
        } catch (InterruptedException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    private static String keyOf(TargetModuleID tmid) {
        StringBuffer sb =  new StringBuffer(256);
        sb.append(tmid.getModuleID());
        sb.append("@"); //NOI18N
        sb.append(tmid.getTarget().getName());
        return sb.toString();
    }
    
    //collect root modules into TargetModule with timestamp
    private TargetModuleID[] saveRootTargetModules(TargetModuleID [] modules) {
        long timestamp = System.currentTimeMillis();
        
        Set originals = new HashSet();
        for (int i=0; i<modules.length; i++) {
            if (modules[i].getParentTargetModuleID() == null) {
                //String id = modules[i].toString();
                String id = keyOf(modules[i]);
                String targetName = modules[i].getTarget().getName();
                TargetModule tm = new TargetModule(id, instance.getUrl(), timestamp, currentContentDir.getAbsolutePath(), contextRoot, modules[i]);
                deployedRootTMIDs.add(tm);
                originals.add(modules[i]);
            }
        }
        return (TargetModuleID[]) originals.toArray(new TargetModuleID[originals.size()]);
    }
    
    public TargetModule[] deploy(DeployProgressUI ui) throws IOException {
        ProgressObject po = null;
        init(ui);
        
        // Note: configuration is not DO so will not be saved automatically on execute
        DeploymentConfigurationProvider dcp = dtarget.getDeploymentConfigurationProvider();
        if (dcp != null)
            dcp.saveOnDemand();
        
        File plan = dtarget.getConfigurationFile();
        DeployableObject deployable = dtarget.getDeploymentConfigurationProvider().getDeployableObject(null);

        // undeploy if necessary
        handleAutoUndeploy(ui);

        // handle initial file deployment or distribute
        if (distributeTargets.size() > 0) {
            Target[] targetz = (Target[]) distributeTargets.toArray(new Target[distributeTargets.size()]);
            
            if (incremental != null && canFileDeploy(targetz, deployable)) {
                DeploymentConfiguration cfg = dtarget.getDeploymentConfigurationProvider().getDeploymentConfiguration();
                File dir = initialDistribute(targetz[0], ui);
                po = incremental.initialDeploy(targetz[0], deployable, cfg, dir);
                handleDeployProgress(ui, po);
                
            } else {  // standard DM.distribute
                if (getApplication() == null) {
                    throw new RuntimeException(NbBundle.getMessage(TargetServer.class, "MSG_NoArchive"));
                }
                
                ui.addMessage(NbBundle.getMessage(TargetServer.class, "MSG_Distributing", application, Arrays.asList(targetz)));
                po = instance.getDeploymentManager().distribute(targetz, getApplication(), plan);
                handleDeployProgress(ui, po);
            }
        }
        
        // handle increment or redeploy
        if (redeployTargetModules != null && redeployTargetModules.length > 0) {
            if (incremental != null && canFileDeploy(redeployTargetModules, deployable)) {
                AppChangeDescriptor acd = distributeChanges(redeployTargetModules[0], ui);
                if (anyChanged(acd)) {
                    ui.addMessage(NbBundle.getMessage(TargetServer.class, "MSG_IncrementalDeploying", redeployTargetModules[0]));
                    po = incremental.incrementalDeploy(redeployTargetModules[0].delegate(), acd);
                    handleIncrementalProgress(ui, po);
                    
                } else { // return original target modules
                    return dtarget.getTargetModules();
                }
            } else { // standard redeploy
                if (getApplication() == null)
                    throw new IllegalArgumentException(NbBundle.getMessage(TargetServer.class, "MSG_NoArchive"));
                
                ui.addMessage(NbBundle.getMessage(TargetServer.class, "MSG_Redeploying", application));
                TargetModuleID[] tmids = TargetModule.toTargetModuleID(redeployTargetModules);
                po = instance.getDeploymentManager().redeploy(tmids, getApplication(), plan);
                handleDeployProgress(ui, po);
            }
        }
        
        return (TargetModule[]) deployedRootTMIDs.toArray(new TargetModule[deployedRootTMIDs.size()]);
    }
    
    public static boolean anyChanged(AppChangeDescriptor acd) {
        return (acd.manifestChanged() || acd.descriptorChanged() || acd.classesChanged()
        || acd.ejbsChanged() || acd.serverDescriptorChanged());
    }
    
    private void handleIncrementalProgress(DeployProgressUI ui, ProgressObject po) {
        handleDeployProgress(ui, po, true);
    }
    private void handleDeployProgress(DeployProgressUI ui, ProgressObject po) {
        handleDeployProgress(ui, po, false);
    }
    
    private void handleDeployProgress(DeployProgressUI ui, ProgressObject po, boolean isIncremental ){
        ProgressListener handler = null;
        try {
            ui.setProgressObject(po);
            
            StateType state = po.getDeploymentStatus().getState();
            if (state == StateType.COMPLETED) {
                ui.addMessage(po.getDeploymentStatus().getMessage());
                handleCompletedDistribute(ui, po, isIncremental);
            } else if (state == StateType.FAILED) {
                ui.addError(NbBundle.getMessage(TargetServer.class, "MSG_DeployFailedWithNoEvent", po.getDeploymentStatus().getMessage()));
            } else {
                if (isIncremental)
                    handler = new TargetServer.IncrementalEventHandler(ui, po);
                else
                    handler = new DistributeEventHandler(ui, po);
                po.addProgressListener(handler);
                sleep(isIncremental ? INCREMENTAL_TIMEOUT : DISTRIBUTE_TIMEOUT);
            }
        } finally {
            if (handler != null)
                po.removeProgressListener(handler);
            if (startEventHandler != null) {
                startEventHandler.unregister();
                startEventHandler = null;
            }
            ui.setProgressObject(null);
        }
    }
    
    private void handleCompletedDistribute(DeployProgressUI ui, final ProgressObject po, boolean isIncremental) {
        TargetModuleID[] modules = po.getResultTargetModuleIDs();
        modules = saveRootTargetModules(modules);
        if (isIncremental) {
            //Note: plugin is responsible for starting module, depending on nature of changes
            wakeUp();
            return;
        }
        
        ProgressObject startPO = instance.getDeploymentManager().start(modules);
        startEventHandler = new ProgressHandler(startPO);
        ui.setProgressObject(startPO);
        
        StateType startState = startPO.getDeploymentStatus().getState();
        if (startState == StateType.COMPLETED || startState == StateType.FAILED) {
            wakeUp();
        }
    }
    
    private void handleAutoUndeploy(DeployProgressUI ui) throws IOException {
        // auto-undeploy if any
        if (undeployTMIDs.size() < 1)
            return;

        TargetModuleID[] tmIDs = (TargetModuleID[]) undeployTMIDs.toArray(new TargetModuleID[undeployTMIDs.size()]);
        ui.addMessage(NbBundle.getMessage(TargetServer.class, "MSG_Undeploying"));
    
        ProgressObject po = instance.getDeploymentManager().stop(tmIDs);
        handleProgressIgnoreFail(ui, po);
        
        po = instance.getDeploymentManager().undeploy(tmIDs);
        handleProgressIgnoreFail(ui, po);
    }

    private void handleProgressIgnoreFail(DeployProgressUI ui, ProgressObject po) {
        ProgressListener handler = null;
        try {
            ui.setProgressObject(po);
            
            StateType state = po.getDeploymentStatus().getState();
            if (state != StateType.COMPLETED && state != StateType.FAILED) {
                handler = new ProgressHandler(po);
                po.addProgressListener(handler);
                sleep(TIMEOUT);
            }
        } finally {
            if (handler != null)
                po.removeProgressListener(handler);
        }
    }
}
