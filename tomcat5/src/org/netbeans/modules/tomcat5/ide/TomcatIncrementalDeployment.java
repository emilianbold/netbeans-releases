/*
 *                 Sun Public License Notice
 *
 * The contents of this file aresubject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5.ide;

import java.io.File;

import org.netbeans.modules.j2ee.deployment.plugins.api.IncrementalDeployment;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.model.*;
import javax.enterprise.deploy.shared.*;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.netbeans.modules.tomcat5.progress.ProgressEventSupport;
import org.netbeans.modules.tomcat5.progress.Status;

import org.netbeans.modules.tomcat5.*;

/**
 *
 * @author  Pavel Buzek
 */
public class TomcatIncrementalDeployment extends IncrementalDeployment {
    
    private TomcatManager tm;
    
    /** Creates a new instance of TomcatIncrementaDeployment */
    public TomcatIncrementalDeployment (DeploymentManager dm) {
        this.tm = (TomcatManager) dm;
    }
    
    public boolean canFileDeploy (Target target, DeployableObject deployable) {
        return deployable.getType ().equals (javax.enterprise.deploy.shared.ModuleType.WAR);
        
    }
    
    public File getDirectoryForModule (TargetModuleID module) {
        return null;
        /*TomcatModule tModule = (TomcatModule) module;
        String moduleFolder = tm.getCatalinaBaseDir ().getAbsolutePath ()
        + System.getProperty("file.separator") + "webapps"   //NOI18N
        + System.getProperty("file.separator") + tModule.getPath ().substring (1); //NOI18N
        File f = new File (moduleFolder);
        return f;*/
    }
    
    public File getDirectoryForNewApplication (Target target, DeployableObject module, DeploymentConfiguration configuration) {
        if (module.getType ().equals (ModuleType.WAR)) {
            return null;
            /*if (configuration instanceof WebappConfiguration) {
                String moduleFolder = tm.getCatalinaBaseDir ().getAbsolutePath ()
                + System.getProperty("file.separator") + "webapps"   //NOI18N
                + System.getProperty("file.separator") + ((WebappConfiguration)configuration).getPath ().substring (1);  //NOI18N
                File f = new File (moduleFolder);
                return f;
            }*/
        }
        throw new IllegalArgumentException ("ModuleType:" + module == null ? null : module.getType () + " Configuration:"+configuration); //NOI18N
    }
    
    public java.io.File getDirectoryForNewModule (java.io.File appDir, String uri, javax.enterprise.deploy.model.DeployableObject module, javax.enterprise.deploy.spi.DeploymentConfiguration configuration) {
        throw new UnsupportedOperationException ();
    }
    
    public ProgressObject incrementalDeploy (final TargetModuleID module, org.netbeans.modules.j2ee.deployment.plugins.api.AppChangeDescriptor changes) {
        if (changes.descriptorChanged () || changes.serverDescriptorChanged () || changes.classesChanged ()) {
            TomcatManagerImpl tmi = new TomcatManagerImpl (tm);
            if (changes.serverDescriptorChanged ()) {
                new TomcatManagerImpl (tm).remove ((TomcatModule) module);
                tmi.incrementalRedeploy ((TomcatModule) module);
            } else if (changes.descriptorChanged()) {
                new TomcatManagerImpl (tm).stop((TomcatModule) module);
                tmi.start ((TomcatModule) module);
            } else {
                tmi.reload ((TomcatModule)module);
            }
            return tmi;
        } else {
            final P p = new P (module);
            p.supp.fireHandleProgressEvent (module, new Status (ActionType.EXECUTE, CommandType.DISTRIBUTE, "", StateType.COMPLETED));
            Task t = RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        p.supp.fireHandleProgressEvent (module, new Status (ActionType.EXECUTE, CommandType.DISTRIBUTE, "", StateType.COMPLETED));
                        
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
            return p;
        }
    }
    
    public ProgressObject initialDeploy (Target target, javax.enterprise.deploy.model.DeployableObject app, DeploymentConfiguration configuration, File dir) {
        TomcatManagerImpl tmi = new TomcatManagerImpl (tm);
        File contextXml = new File (dir.getAbsolutePath () + "/META-INF/context.xml"); //NOI18N
        tmi.initialDeploy (target, contextXml, dir);
        return tmi;
    }
    
    public void notifyDeployment(TargetModuleID module) {
        if (tm.isTomcat50() && tm.getTomcatProperties().getOpenContextLogOnRun()) {
            tm.openLog(module);
        }
    }
    
    private static class P implements ProgressObject {
        
        ProgressEventSupport supp = new ProgressEventSupport (this);
        TargetModuleID tmid;
        
        P (TargetModuleID tmid) {
            this.tmid = tmid;
        }
        
        public void addProgressListener (javax.enterprise.deploy.spi.status.ProgressListener progressListener) {
            supp.addProgressListener (progressListener);
        }
        
        public void removeProgressListener (javax.enterprise.deploy.spi.status.ProgressListener progressListener) {
            supp.removeProgressListener (progressListener);
        }
        
        public javax.enterprise.deploy.spi.status.ClientConfiguration getClientConfiguration (javax.enterprise.deploy.spi.TargetModuleID targetModuleID) {
            return null;
        }
        
        public javax.enterprise.deploy.spi.status.DeploymentStatus getDeploymentStatus () {
            return supp.getDeploymentStatus ();
        }
        
        public javax.enterprise.deploy.spi.TargetModuleID[] getResultTargetModuleIDs () {
            return new TargetModuleID [] {tmid};
        }
        
        public boolean isCancelSupported () {
            return false;
        }
        
        public boolean isStopSupported () {
            return false;
        }
        
        public void cancel () throws javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException {
            throw new OperationUnsupportedException ("");
        }
        
        public void stop () throws javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException {
            throw new OperationUnsupportedException ("");
        }
        
    }
}
