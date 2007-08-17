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
package org.netbeans.modules.j2ee.jboss4;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.MissingResourceException;
import javax.management.ObjectName;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.modules.j2ee.jboss4.config.gen.JbossWeb;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import java.util.Vector;
import java.io.File;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import org.netbeans.modules.j2ee.jboss4.nodes.Util;
import org.openide.filesystems.JarFileSystem;
import org.openide.util.RequestProcessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.j2ee.jboss4.ide.JBDeploymentStatus;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.management.QueryExp;

import org.openide.util.NbBundle;

/**
 *
 * @author Ivan Sidorkin
 */
public class JBDeployer implements ProgressObject, Runnable {
    /** timeout for waiting for URL connection */
    private static final int TIMEOUT = 60000;
    
    JBDeploymentManager dm;
    
    File file;
    String uri;
    JBTargetModuleID module_id;
    /** Creates a new instance of JBDeployer */
    public JBDeployer(String serverUri, JBDeploymentManager dm) {
        uri = serverUri;
        this.dm = dm;
    }
    
    
    public ProgressObject deploy(Target[] target, File file, File file2, String host, int port){
        //PENDING: distribute to all targets!
        module_id = new JBTargetModuleID(target[0], file.getName() );
        
        try{
            String server_url = "http://" + host+":"+port;
            
            if (file.getName().endsWith(".war")) {
                module_id.setContextURL( server_url + JbossWeb.createGraph(file2).getContextRoot());
            } else if (file.getName().endsWith(".ear")) {
                JarFileSystem jfs = new JarFileSystem();
                jfs.setJarFile(file);
                FileObject appXml = jfs.getRoot().getFileObject("META-INF/application.xml");
                if (appXml != null) {
                    Application ear = DDProvider.getDefault().getDDRoot(appXml);
                    Module modules [] = ear.getModule();
                    for (int i = 0; i < modules.length; i++) {
                        JBTargetModuleID mod_id = new JBTargetModuleID(target[0]);
                        if (modules[i].getWeb() != null) {
                            mod_id.setContextURL(server_url + modules[i].getWeb().getContextRoot());
                        }
                        module_id.addChild(mod_id);
                    }
                } else {
                    System.out.println("Cannot file META-INF/application.xml in " + file);
                }
            }
            
        }catch(Exception e){
            e.printStackTrace();
            Logger.getLogger("global").log(Level.INFO, null, e);
        }

        this.file = file;
        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, NbBundle.getMessage(JBDeployer.class, "MSG_DEPLOYING", file.getAbsolutePath())));
        RequestProcessor.getDefault().post(this, 0, Thread.NORM_PRIORITY);
        return this;
    }

    public ProgressObject redeploy (TargetModuleID module_id[], File file, File file2) {
        //PENDING: distribute all modules!
        this.file = file;
        this.module_id = (JBTargetModuleID) module_id[0];
        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, NbBundle.getMessage(JBDeployer.class, "MSG_DEPLOYING", file.getAbsolutePath())));
        RequestProcessor.getDefault().post(this, 0, Thread.NORM_PRIORITY);
        return this;
    }
    
    public void run(){

        String deployDir = InstanceProperties.getInstanceProperties(uri).getProperty(JBPluginProperties.PROPERTY_DEPLOY_DIR);
        FileObject foIn = FileUtil.toFileObject(file);
        FileObject foDestDir = FileUtil.toFileObject(new File(deployDir));
        String fileName = file.getName();
        
        File toDeploy = new File(deployDir+File.separator+fileName);
        if(toDeploy.exists())
            toDeploy.delete();
        
        fileName = fileName.substring(0,fileName.lastIndexOf('.'));
        String msg = NbBundle.getMessage(JBDeployer.class, "MSG_DEPLOYING", file.getAbsolutePath());
        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, msg));
        
        try{
            org.openide.filesystems.FileUtil.copyFile(foIn, foDestDir, fileName); // copy version
            TargetModuleID moduleID = module_id;
            String webUrl = module_id.getWebURL();
            if (webUrl == null) {
                TargetModuleID ch [] = module_id.getChildTargetModuleID();
                if (ch != null) {
                    for (int i = 0; i < ch.length; i++) {
                        webUrl = ch [i].getWebURL();
                        if (webUrl != null) {
                            moduleID = ch[i];
                            break;
                        }
                    }
                }
                
            }
            if (webUrl!= null) {
                URL url = new URL (webUrl);
                String waitingMsg = NbBundle.getMessage(JBDeployer.class, "MSG_Waiting_For_Url", url);
                fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, waitingMsg));
                //delay to prevent hitting the old content before reload
                for (int i = 0; i < 3; i++) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                //wait until the url becomes active
                checkUrlReady(moduleID);
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
            fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED, "Failed"));
        } catch (MissingResourceException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
            fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED, "Failed"));
        } catch (IOException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
            fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED, "Failed"));
        }

        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.COMPLETED, "Applicaton Deployed"));
    }
    
    private void checkUrlReady(TargetModuleID moduleID) {
        try {
            String warName = moduleID.getModuleID();
            ObjectName searchPattern = new ObjectName("jboss.web.deployment:war=" + warName + ",*"); // NOI18N
            Object server = Util.getRMIServer(dm);
            long start = System.currentTimeMillis();
            Set managedObj = Collections.emptySet();
            while (managedObj.size() == 0 && System.currentTimeMillis() - start < TIMEOUT) {
                managedObj = (Set) server.getClass().getMethod("queryMBeans", new Class[]  {ObjectName.class, QueryExp.class}).invoke(server, new Object[]  {searchPattern, null});
            }
        } catch (Exception ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        }
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



