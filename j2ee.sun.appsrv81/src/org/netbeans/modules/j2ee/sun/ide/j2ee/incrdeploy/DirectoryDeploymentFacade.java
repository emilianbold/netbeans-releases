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
/*
 * DirectoryDeploymentFacade.java
 *
 * Created on November 6, 2003, 11:25 AM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.incrdeploy;
import java.io.File;

import org.netbeans.modules.j2ee.deployment.plugins.api.IncrementalDeployment;

import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.sun.ide.j2ee.Utils;
import org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.actions.ViewLogAction;
import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.deployment.plugins.api.AppChangeDescriptor;

import org.netbeans.modules.j2ee.sun.api.ServerInterface;
import org.openide.ErrorManager;
import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;

/**
 *
 * @author  vkraemer
 */
public class DirectoryDeploymentFacade  extends IncrementalDeployment {
    
    Object inner = null;
      private File[] resourceDirs = null;
    private SunDeploymentManagerInterface dm;
   

    /** Creates a new instance of DirectoryDeploymentFacade */
    public DirectoryDeploymentFacade(DeploymentManager dm) {
        //System.out.println("DirectoryDeploymentFacade called");
        try {
            setDeploymentManager(dm);
            Class[] cls= new Class[1];
            cls[0]=DeploymentManager.class;
            java.lang.reflect.Constructor ctr =null;
	    SunDeploymentManagerInterface sdm = (SunDeploymentManagerInterface)dm;
	    ClassLoader loader = ServerLocationManager.getNetBeansAndServerClassLoader(sdm.getPlatformRoot());
            ctr = loader.loadClass("org.netbeans.modules.j2ee.sun.bridge.DirectoryDeployment").getConstructor(cls);
            Object[] o= new Object[1];
            o[0]=dm;
            
            inner = ctr.newInstance(o);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    /**
     * @param manager
     */
    public void setDeploymentManager(DeploymentManager manager) {
        if (null == manager)
            throw new IllegalArgumentException("invalid null argumment");
        
        if (manager instanceof SunDeploymentManagerInterface)
            this.dm = (SunDeploymentManagerInterface) manager;
        else
            throw new IllegalArgumentException("setDeploymentManager: Invalid manager type, expecting SunDeploymentManager and got "+manager.getClass().getName());
    }    
    
        
    
    public java.io.File getDirectoryForModule(javax.enterprise.deploy.spi.TargetModuleID module) {
        if (null == inner)
            return null;
        return ((IncrementalDeployment)inner).getDirectoryForModule(module);
    }
    
    public String getModuleUrl(javax.enterprise.deploy.spi.TargetModuleID module) {
        if (null == inner)
            return null;
        return ((IncrementalDeployment)inner).getModuleUrl(module);
    }
    
    public ProgressObject incrementalDeploy(TargetModuleID module, AppChangeDescriptor changes) {
         //Register resources if any

         // XXX 
         //No way to get resourceDirs from input parameters of this method. 
         //Initializing resourceDirs in canFileDeploy() method.
         //Relying on the assumption that canFileDeploy() is & *will* always get
         //called with appropriate DeployableObject before this method.
         if((resourceDirs != null) && (dm != null))
            Utils.registerResources(resourceDirs, (ServerInterface)dm.getManagement());
            if (null!=dm)
                ViewLogAction.viewLog(dm);
                
         return ((IncrementalDeployment)inner).incrementalDeploy(module, changes);
    }
    
    
    
    
    public ProgressObject initialDeploy(Target target, DeployableObject deployableObject, DeploymentConfiguration deploymentConfiguration, File file) {
       //Register resources if any
       File[] resourceDirs = Utils.getResourceDirs(deployableObject);
       if((resourceDirs != null) && (dm != null)) 
           Utils.registerResources(resourceDirs, (ServerInterface)dm.getManagement());
       if (null != dm)
        ViewLogAction.viewLog(dm);
        return ((IncrementalDeployment)inner).initialDeploy(target,deployableObject,deploymentConfiguration, file);
    }

    /**
     * Whether the deployable object could be file deployed to the specified target
     * @param target target in question
     * @param deployable the deployable object in question
     * @return true if it is possible to do file deployment
     */
    public boolean canFileDeploy(Target target, DeployableObject deployableObject) {
        if (org.netbeans.modules.j2ee.sun.ide.j2ee.PluginProperties.getDefault().isIncrementalDeploy()==false)
            return false;
        resourceDirs = Utils.getResourceDirs(deployableObject);
        if (null == target)
            return false;
        if (null == deployableObject)
            return false;
        if (null == dm)
            return false;
        if (deployableObject.getType() == ModuleType.EAR || 
                    deployableObject.getType() == ModuleType.EJB)
            return false;
        return dm.isLocal();
    }
    
    public File getDirectoryForNewApplication(javax.enterprise.deploy.spi.Target target, DeployableObject deployableObject, DeploymentConfiguration deploymentConfiguration) {
        return ((IncrementalDeployment)inner).getDirectoryForNewApplication(target,deployableObject,deploymentConfiguration);
    }
    public File getDirectoryForNewApplication(String deploymentName, Target target, DeploymentConfiguration configuration){
        SunONEDeploymentConfiguration s1dc =(SunONEDeploymentConfiguration) configuration;       
        s1dc.setDeploymentModuleName(deploymentName);
	return null;
    }
    
    public File getDirectoryForNewModule(File file, String str, DeployableObject deployableObject, DeploymentConfiguration deploymentConfiguration) {
        return ((IncrementalDeployment)inner).getDirectoryForNewModule(file,str, deployableObject,deploymentConfiguration);
    }
    
}
