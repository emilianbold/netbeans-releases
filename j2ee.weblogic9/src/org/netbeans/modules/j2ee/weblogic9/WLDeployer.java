/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.weblogic9;

import java.net.URL;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import java.util.Vector;
import java.io.File;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import org.netbeans.modules.j2ee.jboss4.URLWait;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;

import org.openide.util.NbBundle;

/**
 *
 * @author Ivan Sidorkin
 */
public class WLDeployer implements ProgressObject, Runnable {
    private static final String AUTO_DEPLOY_DIR = "/autodeploy"; //NOI18N
    /** timeout for waiting for URL connection */
    private static final int TIMEOUT = 60000;
    
    Target[] target;
    File file;
    File file2;
    String uri;
    TargetModuleID module_id;
    /** Creates a new instance of JBDeployer */
    public WLDeployer(String serverUri) {
        uri = serverUri;
    }
    
    
    public ProgressObject deploy(Target[] target, File file, File file2, TargetModuleID module_id){
        

        this.target = target;
        this.file = file;
        this.file2 = file2;
        this.module_id = module_id;
        fireHandleProgressEvent(null, new WLDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, NbBundle.getMessage(WLDeployer.class, "MSG_DEPLOYING", file.getAbsolutePath())));
        RequestProcessor.getDefault().post(this, 0, Thread.NORM_PRIORITY);
        return this;
    }
    
    
    public void run(){

        String deployDir = InstanceProperties.getInstanceProperties(uri).getProperty(WLDeploymentFactory.DOMAIN_ROOT_ATTR) + AUTO_DEPLOY_DIR;
        FileObject foIn = FileUtil.toFileObject(file);
        FileObject foDestDir = FileUtil.toFileObject(new File(deployDir));
        String fileName = file.getName();
        
        File toDeploy = new File(deployDir+File.separator+fileName);
        if(toDeploy.exists())
            toDeploy.delete();
        
        fileName = fileName.substring(0,fileName.lastIndexOf('.'));
        String msg = NbBundle.getMessage(WLDeployer.class, "MSG_DEPLOYING", file.getAbsolutePath());
        fireHandleProgressEvent(null, new WLDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, msg));
        
        try{
            URL url = new URL (module_id.getWebURL());
            String waitingMsg = NbBundle.getMessage(WLDeployer.class, "MSG_Waiting_For_Url", url);
            org.openide.filesystems.FileUtil.copyFile(foIn, foDestDir, fileName); // copy version
            fireHandleProgressEvent(null, new WLDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, waitingMsg));
            //delay to prevent hitting the old content before reload
            for (int i = 0; i < 3; i++) {
                Thread.sleep(1000);
                fireHandleProgressEvent(null, new WLDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, waitingMsg));
            }
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < TIMEOUT) {
                if (URLWait.waitForUrlReady(url, 1000)) {
                    break;
                }
                fireHandleProgressEvent(null, new WLDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, waitingMsg));
            }
        }catch(Exception e){
            fireHandleProgressEvent(null, new WLDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED, "Failed"));
        }

        fireHandleProgressEvent(null, new WLDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.COMPLETED, "Applicaton Deployed"));
    }
    
    
    // ----------  Implementation of ProgressObject interface
    private Vector listeners = new Vector();
    private DeploymentStatus deploymentStatus;
    
    public void addProgressListener(ProgressListener pl) {
        listeners.add(pl);
    }
    
    public void removeProgressListener(ProgressListener pl) {
        listeners.remove(pl);
    }
    
    public void stop() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("");
    }
    
    public boolean isStopSupported() {
        return false;
    }
    
    public void cancel() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("");
    }
    
    public boolean isCancelSupported() {
        return false;
    }
    
    public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
        return null;
    }
    
    public TargetModuleID[] getResultTargetModuleIDs() {
        return new TargetModuleID[]{ module_id };
    }
    
    public DeploymentStatus getDeploymentStatus() {
        return deploymentStatus;
    }
    
    /** Report event to any registered listeners. */
    public void fireHandleProgressEvent(TargetModuleID targetModuleID, DeploymentStatus deploymentStatus) {
        ProgressEvent evt = new ProgressEvent(this, targetModuleID, deploymentStatus);
        
        this.deploymentStatus = deploymentStatus;
        
        java.util.Vector targets = null;
        synchronized (this) {
            if (listeners != null) {
                targets = (java.util.Vector) listeners.clone();
            }
        }
        
        if (targets != null) {
            for (int i = 0; i < targets.size(); i++) {
                ProgressListener target = (ProgressListener)targets.elementAt(i);
                target.handleProgressEvent(evt);
            }
        }
    }
    
    
}



