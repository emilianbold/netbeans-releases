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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.j2ee.deployment.impl;

import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfigurationFactory;
import org.openide.filesystems.FileUtil;

import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.status.*;
import javax.enterprise.deploy.spi.exceptions.*;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.execution.DeploymentTarget;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;

import java.util.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.execution.ModuleConfigurationProvider;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.TargetModuleIDResolver;

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
    
    private Target[] targets;
    private final ServerInstance instance;
    private final DeploymentTarget dtarget;
    private IncrementalDeployment incremental; //null value signifies don't do incremental
    private boolean debugMode = false;
    private Map availablesMap = null;
    private Set deployedRootTMIDs = new HashSet(); // type TargetModule
    private Set undeployTMIDs = new HashSet(); // TMID
    private Set distributeTargets = new HashSet(); //Target
    private TargetModule[] redeployTargetModules = null;
    private File application = null;
    private File currentContentDir = null;
    private String contextRoot = null;
    
    public TargetServer(DeploymentTarget target) {
        this.dtarget = target;
        this.instance = dtarget.getServer().getServerInstance();
    }
    
    private void init(ProgressUI ui) throws ServerException {
        if (targets == null) {
            instance.start(ui);
            targets = dtarget.getServer().toTargets();
        }        
        incremental = instance.getIncrementalDeployment();
        if (incremental != null && ! checkServiceImplementations())
            incremental = null;

        try {
            FileObject contentFO = dtarget.getModule().getContentDirectory();
            if (contentFO != null) {
                currentContentDir = FileUtil.toFile(contentFO);
            }
        
        } catch (IOException ioe) {
            Logger.getLogger("global").log(Level.INFO, null, ioe);
        }
        
        J2eeModuleProvider.ConfigSupport configSupport = dtarget.getConfigSupport();
        if (J2eeModule.WAR.equals(dtarget.getModule().getModuleType())) {
            try {
                contextRoot = configSupport.getWebContextRoot();
            } catch (ConfigurationException e) {
                contextRoot = null;
            }
        }
        
        processLastTargetModules();
    }
    
    private boolean canFileDeploy(Target[] targetz, J2eeModule deployable) {
        if (targetz == null || targetz.length != 1) {
            Logger.getLogger("global").log(Level.INFO, NbBundle.getMessage(TargetServer.class, "MSG_MoreThanOneIncrementalTargets"));
            return false;
        }
        
        if (deployable == null || !instance.getIncrementalDeployment().canFileDeploy(targetz[0], deployable))
            return false;
        
        return true;
    }
    
    private boolean canFileDeploy(TargetModule[] targetModules, J2eeModule deployable) {
        if (targetModules == null || targetModules.length != 1) {
            Logger.getLogger("global").log(Level.INFO, NbBundle.getMessage(TargetServer.class, "MSG_MoreThanOneIncrementalTargets"));
            return false;
        }
        
        if (deployable == null || !instance.getIncrementalDeployment().canFileDeploy(targetModules[0].getTarget(), deployable))
            return false;
        
        return true;
    }
    
    private AppChangeDescriptor distributeChanges(TargetModule targetModule, ProgressUI ui) throws IOException {
        ServerFileDistributor sfd = new ServerFileDistributor(instance, dtarget);
        try {
            ui.setProgressObject(sfd);
            ModuleChangeReporter mcr = dtarget.getModuleChangeReporter();
            AppChangeDescriptor acd = sfd.distribute(targetModule, mcr);
            return acd;
        } finally {
            ui.setProgressObject(null);
        }
    }
    
    private File initialDistribute(Target target, ProgressUI ui) {
        InitialServerFileDistributor sfd = new InitialServerFileDistributor(dtarget, target);
        try {
            ui.setProgressObject(sfd);
            return sfd.distribute();
        } finally {
            ui.setProgressObject(null);
        }
    }
    
    private boolean checkServiceImplementations() {
        String missing = null;
        if (instance.getServer().getModuleConfigurationFactory() == null) {
            missing = ModuleConfigurationFactory.class.getName();
        }
        
        if (missing != null) {
            String msg = NbBundle.getMessage(ServerFileDistributor.class, "MSG_MissingServiceImplementations", missing);
            Logger.getLogger("global").log(Level.INFO, msg);
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
                    
                    distributeTargets.add(deployed.findTarget());
                    undeployTMIDs.add(deployed.delegate());
                    deployed.remove();
                    j.remove();
                }
            }
        }
        
        return (TargetModule[]) toRedeploy.toArray(new TargetModule[toRedeploy.size()]);
    }
    
    // return list of target modules to redeploy
    private TargetModule[] checkUndeployForSharedReferences(Target[] toDistribute) {
        Set distSet = new HashSet(Arrays.asList(toDistribute));
        return checkUndeployForSharedReferences(Collections.EMPTY_SET, distSet);
    }
    private TargetModule[] checkUndeployForSharedReferences(Set toRedeploy, Set toDistribute) {
        return checkUndeployForSharedReferences(toRedeploy, toDistribute, null);
    }
    private TargetModule[] checkUndeployForSharedReferences(Set toRedeploy, Set toDistribute, Map queryInfo) {
        // we don't want to undeploy anything when both distribute list and redeploy list are empty
        if (contextRoot == null || (toRedeploy.isEmpty() && toDistribute.isEmpty())) {
            return (TargetModule[]) toRedeploy.toArray(new TargetModule[toRedeploy.size()]);
        }
        
        Set allTargets = new HashSet(Arrays.asList(TargetModule.toTarget((TargetModule[]) toRedeploy.toArray(new TargetModule[toRedeploy.size()]))));
        allTargets.addAll(toDistribute);
        Target[] targs = (Target[]) allTargets.toArray(new Target[allTargets.size()]);

        boolean shared = false;
        List addToDistributeWhenSharedDetected = new ArrayList();
        List removeFromRedeployWhenSharedDetected = new ArrayList();
        List addToUndeployWhenSharedDetected = new ArrayList();
        List sharerTMIDs;
  
        TargetModuleIDResolver tmidResolver = instance.getTargetModuleIDResolver();
        if (tmidResolver != null) {
            if (queryInfo == null) {
                queryInfo = new HashMap();
                queryInfo.put(TargetModuleIDResolver.KEY_CONTEXT_ROOT, contextRoot);
            }
            
            TargetModuleID[] haveSameReferences = TargetModule.EMPTY_TMID_ARRAY;
            if (targs.length > 0) {
                haveSameReferences = tmidResolver.lookupTargetModuleID(queryInfo, targs);
            }
            for (int i=0; i<haveSameReferences.length; i++) {
                haveSameReferences[i] = new TargetModule(keyOf(haveSameReferences[i]), haveSameReferences[i]); 
            }
            sharerTMIDs = Arrays.asList(haveSameReferences);

            for (Iterator i=sharerTMIDs.iterator(); i.hasNext();) {
                TargetModule sharer = (TargetModule) i.next();
                if ((toRedeploy.size() > 0 && ! toRedeploy.contains(sharer)) ||
                    toDistribute.contains(sharer.getTarget())) {
                    shared = true;
                    addToUndeployWhenSharedDetected.add(sharer.delegate());
                } else {
                    removeFromRedeployWhenSharedDetected.add(sharer);
                    addToDistributeWhenSharedDetected.add(sharer.getTarget());
                }
            }
        }
        
        // this is in addition to the above check: TMID provided from tomcat 
        // plugin does not have module deployment name element
        if (!shared) {  
            sharerTMIDs = TargetModule.findByContextRoot(dtarget.getServer(), contextRoot);
            sharerTMIDs = TargetModule.initDelegate(sharerTMIDs, getAvailableTMIDsMap());

            for (Iterator i=sharerTMIDs.iterator(); i.hasNext();) {
                TargetModule sharer = (TargetModule) i.next();
                boolean redeployHasSharer = false;
                for (Iterator j=toRedeploy.iterator(); j.hasNext();) {
                    TargetModule redeploying = (TargetModule) j.next();
                    if (redeploying.equals(sharer) && redeploying.getContentDirectory().equals(sharer.getContentDirectory())) {
                        redeployHasSharer = true;
                        break;
                    }
                }
                if (! redeployHasSharer ||
                    toDistribute.contains(sharer.getTarget())) {
                        shared = true;
                        addToUndeployWhenSharedDetected.add(sharer.delegate());
                } else {
                    removeFromRedeployWhenSharedDetected.add(sharer);
                    addToDistributeWhenSharedDetected.add(sharer.getTarget());
                }
            }
        }

        if (shared) {
            undeployTMIDs.addAll(addToUndeployWhenSharedDetected); 
            //erase memory of them if any
            TargetModule.removeByContextRoot(dtarget.getServer(), contextRoot);
            // transfer from redeploy to distribute
            toRedeploy.removeAll(removeFromRedeployWhenSharedDetected);
            distributeTargets.addAll(addToDistributeWhenSharedDetected);
        } 
    
        return (TargetModule[]) toRedeploy.toArray(new TargetModule[toRedeploy.size()]);
    }

    private Map getAvailableTMIDsMap() {
        if (availablesMap != null)
            return availablesMap;
        
        // existing TMID's
        DeploymentManager dm = instance.getDeploymentManager();
        availablesMap = new HashMap();
        try {
            ModuleType type = (ModuleType) dtarget.getModule().getModuleType();
            TargetModuleID[] ids = dm.getAvailableModules(type, targets);
            if (ids == null) {
                return availablesMap;
            }
            for (int i=0; i<ids.length; i++) {
                availablesMap.put(keyOf(ids[i]), ids[i]);
            }
        } catch (TargetException te) {
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException();
            illegalArgumentException.initCause(te);
            throw illegalArgumentException;
        }
        return availablesMap;
    }
    
    /**
     * Process last deployment TargetModuleID's for undeploy, redistribute, redeploy and oldest timestamp
     */
    private void processLastTargetModules() {
        TargetModule[] targetModules = dtarget.getTargetModules();

        // new module
        if (targetModules == null || targetModules.length == 0) {
            distributeTargets.addAll(Arrays.asList(targets));
            checkUndeployForSharedReferences(targets);
            return;
        }
        
        Set targetNames = new HashSet();
        for (int i=0; i<targets.length; i++) targetNames.add(targets[i].getName());
        
        Set toRedeploy = new HashSet(); //type TargetModule
        for (int i=0; i<targetModules.length; i++) {
            // not my module
            if (! targetModules[i].getInstanceUrl().equals(instance.getUrl()) ||
            ! targetNames.contains(targetModules[i].getTargetName()))
                continue;
            
            TargetModuleID tmID = (TargetModuleID) getAvailableTMIDsMap().get(targetModules[i].getId());
            
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
        
        DeploymentManager dm = instance.getDeploymentManager();

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
                        undeployTMIDs.add(deployed.delegate());
                        j.remove();
                    }
                } else {
                    // no need to redeploy
                    j.remove();
                }
            }
        }

        redeployTargetModules = checkUndeployForChangedReferences(toRedeploy);
        Set targetSet = new HashSet(distributeTargets);
        redeployTargetModules = checkUndeployForSharedReferences(toRedeploy, targetSet);
    }
    
    private File getApplication() {
        if (application != null) return application;
        try {
            FileObject archiveFO = dtarget.getModule().getArchive();
            if (archiveFO == null) return null;
            application = FileUtil.toFile(archiveFO);
            return application;
        } catch (IOException ioe) {
            Logger.getLogger("global").log(Level.SEVERE, ioe.getMessage());
            return null;
        }
    }
    
    public void startTargets(boolean debugMode, ProgressUI ui) throws ServerException {
        this.debugMode = debugMode;
        if (instance.getStartServer().isAlsoTargetServer(null)) {
            if (debugMode) {
                instance.startDebug(ui);
            } else {
                instance.start(ui);
            }
            this.targets = dtarget.getServer().toTargets();
            return;
        }
        instance.start(ui);
        this.targets = dtarget.getServer().toTargets();
        if (debugMode) {
            for (int i=0; i<targets.length; i++) {
                instance.startDebugTarget(targets[i], ui);
            }
        } else {
            for (int i=0; i<targets.length; i++) {
                instance.startTarget(targets[i], ui);
            }
        }
    }
    
    private static String keyOf(TargetModuleID tmid) {
        /*StringBuffer sb =  new StringBuffer(256);
        sb.append(tmid.getModuleID());
        sb.append("@"); //NOI18N
        sb.append(tmid.getTarget().getName());
        return sb.toString();*/
        return tmid.toString();
    }
    
    //collect root modules into TargetModule with timestamp
    private TargetModuleID[] saveRootTargetModules(TargetModuleID [] modules) {
        long timestamp = System.currentTimeMillis();
        
        Set originals = new HashSet();
        for (int i=0; i<modules.length; i++) {
            if (modules[i].getParentTargetModuleID() == null) {
                String id = keyOf(modules[i]);
                String targetName = modules[i].getTarget().getName();
                String fromDir = "";
                if (null != currentContentDir)
                    fromDir = currentContentDir.getAbsolutePath();
                TargetModule tm = new TargetModule(id, instance.getUrl(), timestamp, fromDir, contextRoot, modules[i]);
                deployedRootTMIDs.add(tm);
                originals.add(modules[i]);
            }
        }
        return (TargetModuleID[]) originals.toArray(new TargetModuleID[originals.size()]);
    }
    
    public TargetModule[] deploy(ProgressUI ui, boolean forceRedeploy) throws IOException, ServerException {
        ProgressObject po = null;
        boolean hasActivities = false;
        
        init(ui);
        if (forceRedeploy) {
            if (redeployTargetModules == null) {
            } else {
                for (int i = 0; i < redeployTargetModules.length; i++) {
                    distributeTargets.add(redeployTargetModules [i].findTarget ());
                    undeployTMIDs.add(redeployTargetModules [i].delegate());
                    redeployTargetModules [i].remove();
                }
                redeployTargetModules = null;
            }
        }
        
        File plan = null;
        J2eeModule deployable = null;
        ModuleConfigurationProvider mcp = dtarget.getModuleConfigurationProvider();
        if (mcp != null)
            deployable = mcp.getJ2eeModule(null);
        boolean hasDirectory = (dtarget.getModule().getContentDirectory() != null);

        // undeploy if necessary
        if (undeployTMIDs.size() > 0) {
            TargetModuleID[] tmIDs = (TargetModuleID[]) undeployTMIDs.toArray(new TargetModuleID[undeployTMIDs.size()]);
            ui.progress(NbBundle.getMessage(TargetServer.class, "MSG_Undeploying"));
            ProgressObject undeployPO = instance.getDeploymentManager().undeploy(tmIDs);
            try {
                ProgressObjectUtil.trackProgressObject(ui, undeployPO, instance.getDeploymentTimeout()); // lets use the same timeout as for deployment
            } catch (TimedOutException e) {
                // undeployment failed, try to continue anyway
            }
        }

        // handle initial file deployment or distribute
        if (distributeTargets.size() > 0) {
            hasActivities = true;
            Target[] targetz = (Target[]) distributeTargets.toArray(new Target[distributeTargets.size()]);

            if (incremental != null && hasDirectory && canFileDeploy(targetz, deployable)) {
                ModuleConfiguration cfg = dtarget.getModuleConfigurationProvider().getModuleConfiguration();
                File dir = initialDistribute(targetz[0], ui);
                po = incremental.initialDeploy(targetz[0], deployable, cfg, dir);
                trackDeployProgressObject(ui, po, false);
            } else {  // standard DM.distribute
                if (getApplication() == null) {
                    throw new RuntimeException(NbBundle.getMessage(TargetServer.class, "MSG_NoArchive"));
                }
                
                ui.progress(NbBundle.getMessage(TargetServer.class, "MSG_Distributing", application, Arrays.asList(targetz)));
                plan = dtarget.getConfigurationFile();
                po = instance.getDeploymentManager().distribute(targetz, getApplication(), plan);
                trackDeployProgressObject(ui, po, false);
            }
        }
        
        // handle increment or standard redeploy
        if (redeployTargetModules != null && redeployTargetModules.length > 0) {
            hasActivities = true;
            if (incremental != null && hasDirectory && canFileDeploy(redeployTargetModules, deployable)) {
                AppChangeDescriptor acd = distributeChanges(redeployTargetModules[0], ui);
                if (anyChanged(acd)) {
                    ui.progress(NbBundle.getMessage(TargetServer.class, "MSG_IncrementalDeploying", redeployTargetModules[0]));
                    po = incremental.incrementalDeploy(redeployTargetModules[0].delegate(), acd);
                    trackDeployProgressObject(ui, po, true);
                    
                } else { // return original target modules
                    return dtarget.getTargetModules();
                }
            } else { // standard redeploy
                if (getApplication() == null)
                    throw new IllegalArgumentException(NbBundle.getMessage(TargetServer.class, "MSG_NoArchive"));
                
                ui.progress(NbBundle.getMessage(TargetServer.class, "MSG_Redeploying", application));
                TargetModuleID[] tmids = TargetModule.toTargetModuleID(redeployTargetModules);
                if (plan == null) plan = dtarget.getConfigurationFile();
                po = instance.getDeploymentManager().redeploy(tmids, getApplication(), plan);
                trackDeployProgressObject(ui, po, false);
            }
        }
        
        if (hasActivities) {
            return (TargetModule[]) deployedRootTMIDs.toArray(new TargetModule[deployedRootTMIDs.size()]);
        } else {
            return dtarget.getTargetModules();
        }
    }
    
    /**
     * Inform the plugin about the deploy action, even if there was
     * really nothing needed to be deployed.
     *
     * @param modules list of modules which are being deployed
     */
    public void notifyIncrementalDeployment(TargetModuleID[] modules) {
        if (modules !=  null && incremental != null) {
            for (int i = 0; i < modules.length; i++) {
                incremental.notifyDeployment(modules[i]);
            }
        }
    }
    
    public static boolean anyChanged(AppChangeDescriptor acd) {
        return (acd.manifestChanged() || acd.descriptorChanged() || acd.classesChanged()
        || acd.ejbsChanged() || acd.serverDescriptorChanged());
    }
    
    /**
     * Waits till the deploy progress object is in final state or till the timeout 
     * runs out. If the deploy completes successfully the module will be started
     * if needed.
     *
     * @param ui progress ui which will be notified about progress object changes .
     * @param po progress object which will be tracked.
     * @param incremental is it incremental deploy?
     */
    private void trackDeployProgressObject(ProgressUI ui, ProgressObject po, boolean incremental) throws ServerException {
        long deploymentTimeout = instance.getDeploymentTimeout();
        long startTime = System.currentTimeMillis();
        try {
            boolean completed = ProgressObjectUtil.trackProgressObject(ui, po, deploymentTimeout);
            if (completed) {
                TargetModuleID[] modules = po.getResultTargetModuleIDs();
                modules = saveRootTargetModules(modules);
                if (!incremental) {
                    // if incremental, plugin is responsible for starting module, depending on nature of changes
                    ProgressObject startPO = instance.getDeploymentManager().start(modules);
                    long deployTime = System.currentTimeMillis() - startTime;
                    ProgressObjectUtil.trackProgressObject(ui, startPO, deploymentTimeout - deployTime);
                }
            }
        } catch (TimedOutException e) {
            throw new ServerException(NbBundle.getMessage(TargetServer.class, "MSG_DeploymentTimeoutExceeded"));
        }
    }
}
