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

package org.netbeans.modules.j2ee.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import org.netbeans.modules.j2ee.deployment.impl.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.j2ee.deployment.impl.ui.DeployProgressUI;
import org.netbeans.modules.j2ee.deployment.impl.ui.DeployProgressMonitor;
import org.netbeans.modules.j2ee.deployment.impl.projects.*;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.filesystems.*;
import org.apache.tools.ant.Project;

/**
 * Ant task that starts the server if needed and deploys module to the server
 * @author Martin Grebac
 */
public class Deploy extends Task {
    
    static final int MAX_DEPLOY_PROGRESS = 5;

    /**
     * Holds value of property debugMode.
     */
    private boolean debugMode = false;
    
    private boolean alsoStartTargets = true; //TODO - make it a property? is it really needed?
    
    public void execute() throws BuildException { 

        log("basedir: " + getProject().getBaseDir());
        J2eeDeploymentLookup jdl = null;
        try {
            FileObject[] fobs = FileUtil.fromFile(getProject().getBaseDir());
            fobs[0].refresh(); // without this the "build" direcetory is not found in filesystems
            jdl = (J2eeDeploymentLookup) FileOwnerQuery.getOwner(fobs[0]).getLookup().lookup(J2eeDeploymentLookup.class);
        } catch (Exception e) {
            log("exception: " + e);
        }

        J2eeProfileSettings settings = jdl.getJ2eeProfileSettings();
        DeploymentTargetImpl target = new DeploymentTargetImpl(settings, jdl);

        ServerString server = target.getServer();
        J2eeModule module = target.getModule();
        TargetModule[] modules = null;
        DeployProgressUI progress = new DeployProgressMonitor(false, true);  // modeless with stop/cancel buttons
        progress.startProgressUI(MAX_DEPLOY_PROGRESS);
        
        try {
            if (module == null) {
                progress.addError(/*NbBundle.getMessage(ServerExecutor.class, "MSG_NoJ2eeModule")*/"No J2eeModule");//TODO
                throw new BuildException("No J2ee module");
            }
            if (server == null || server.getServerInstance() == null) {
                progress.addError(/*NbBundle.getMessage(ServerExecutor.class, "MSG_NoTargetServer")*/"No target server.");//TODO
                throw new BuildException("No target server.");
            }
            
            progress.recordWork(1);
            
            boolean serverReady = false;
            TargetServer targetserver = new TargetServer(target);

            if (alsoStartTargets || debugMode) {
                serverReady = targetserver.startTargets(debugMode, progress);
            } else { //PENDING: how do we know whether target does not need to start when deploy only
                serverReady = server.getServerInstance().start(progress);
            }
            log("NOT FAILED YET");
            if (! serverReady) {
                progress.addError(/*NbBundle.getMessage(ServerExecutor.class, "MSG_StartServerFailed", server)*/"Start server failed");//TODO
                throw new BuildException("Start server failed.");
            }
            
            progress.recordWork(2);
            log("GONNA DEPLOY");
            modules = targetserver.deploy(progress);
            progress.recordWork(MAX_DEPLOY_PROGRESS-1);
            
        } catch (Exception ex) {
            throw new BuildException("Deploy failed.");
        }
        
        if (modules != null && modules.length > 0) {
            target.setTargetModules(modules);
            progress.recordWork(MAX_DEPLOY_PROGRESS);
        } else {
            throw new BuildException("Some other error.");
        }
        
//        URLCookie urlCookie = (URLCookie) obj.getCookie(URLCookie.class);
//        String url = "";
//        if (urlCookie != null){
//            url = urlCookie.getURL();
//        }
        
    }

    /**
     * Getter for property debugMode.
     * @return Value of property debugMode.
     */
    public boolean getDebugMode() {
        return this.debugMode;
    }
    
    /**
     * Setter for property debugMode.
     * @param debugMode New value of property debugMode.
     */
    public void setDebugMode(boolean debug) {
        this.debugMode = debugMode;
    }
        
//    public ExecutorTask execute(final DeploymentTarget target, final String uri) {
//        Task t = RequestProcessor.getDefault().post(new Runnable() {
//            public void run() {
//                boolean success = doDeploy(target, false/*debug*/, true/*alsoStartTargets*/);
//                if (success)
//                    target.startClient(uri);
//            }
//        });
//        return null; // PENDING produce executortask
//    }
    
}
