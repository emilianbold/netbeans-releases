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
/*
 * DirectoryDeploymentFacade.java
 *
 * Created on November 6, 2003, 11:25 AM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.incrdeploy;
import org.netbeans.modules.j2ee.deployment.plugins.api.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.api.DeploymentPlanSplitter;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.sun.ide.Installer;

/**
 *
 * @author  vkraemer
 */
public class DirectoryDeploymentFacade
        extends IncrementalDeployment  implements DeploymentPlanSplitter {
    
    Object inner = null;
    
    /** Creates a new instance of DirectoryDeploymentFacade */
    public DirectoryDeploymentFacade() {
        
        try{

               inner = Installer.getPluginLoader().loadClass("org.netbeans.modules.j2ee.sun.ide.j2ee.incrdeploy.DirectoryDeployment").newInstance();//NOI18N
            
        } catch (Throwable t) {
          //  t.printStackTrace();
            System.out.println("WARNING: cannot create a good DirectoryDeploymentFacade: to correct, set com.sun.aas.installRoot to the correct App Server 8 PE Location and restart. -> " +t.getMessage());
        }
    }
    /** Creates a new instance of DirectoryDeploymentFacade */
    public DirectoryDeploymentFacade(DeploymentManager dm) {
        //System.out.println("DirectoryDeploymentFacade called");
        try {
            Class[] cls= new Class[1];
            cls[0]=DeploymentManager.class;
            java.lang.reflect.Constructor ctr =null;
                ctr = Installer.getPluginLoader().loadClass("org.netbeans.modules.j2ee.sun.ide.j2ee.incrdeploy.DirectoryDeployment").getConstructor(cls);
            Object[] o= new Object[1];
            o[0]=dm;
            
            inner = ctr.newInstance(o);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    
    public String[] getDeploymentPlanFileNames( javax.enterprise.deploy.shared.ModuleType module) {
        if (null == inner)
            return null;
        return ((DeploymentPlanSplitter)inner).getDeploymentPlanFileNames(module);
        
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
    
    public javax.enterprise.deploy.spi.status.ProgressObject incrementalDeploy(javax.enterprise.deploy.spi.TargetModuleID module, org.netbeans.modules.j2ee.deployment.plugins.api.AppChangeDescriptor changes) {
        return ((IncrementalDeployment)inner).incrementalDeploy(module, changes);
    }
    
    
    
    
    public javax.enterprise.deploy.spi.status.ProgressObject initialDeploy(javax.enterprise.deploy.spi.Target target, javax.enterprise.deploy.model.DeployableObject deployableObject, javax.enterprise.deploy.spi.DeploymentConfiguration deploymentConfiguration, java.io.File file) {
        return ((IncrementalDeployment)inner).initialDeploy(target,deployableObject,deploymentConfiguration, file);
    }
    public boolean canFileDeploy(javax.enterprise.deploy.spi.Target target, javax.enterprise.deploy.model.DeployableObject deployableObject) {
        if (org.netbeans.modules.j2ee.sun.ide.j2ee.PluginProperties.getDefault().isIncrementalDeploy()==false)
            return false;
        return ((IncrementalDeployment)inner).canFileDeploy(target,deployableObject);
    }
    
    public java.io.File getDirectoryForNewApplication(javax.enterprise.deploy.spi.Target target, javax.enterprise.deploy.model.DeployableObject deployableObject, javax.enterprise.deploy.spi.DeploymentConfiguration deploymentConfiguration) {
        return ((IncrementalDeployment)inner).getDirectoryForNewApplication(target,deployableObject,deploymentConfiguration);
    }
    public java.io.File getDirectoryForNewApplication(String deploymentName, javax.enterprise.deploy.spi.Target target, javax.enterprise.deploy.spi.DeploymentConfiguration configuration){
        return ((IncrementalDeployment)inner).getDirectoryForNewApplication(deploymentName, target,configuration);
    }
    
    public java.io.File getDirectoryForNewModule(java.io.File file, String str, javax.enterprise.deploy.model.DeployableObject deployableObject, javax.enterprise.deploy.spi.DeploymentConfiguration deploymentConfiguration) {
        return ((IncrementalDeployment)inner).getDirectoryForNewModule(file,str, deployableObject,deploymentConfiguration);
    }
    
    //for DeploymentPlanSplitter
    public void writeDeploymentPlanFiles(javax.enterprise.deploy.spi.DeploymentConfiguration config, javax.enterprise.deploy.model.DeployableObject module, java.io.File[] files) throws ConfigurationException{
        ((DeploymentPlanSplitter)inner).writeDeploymentPlanFiles(config, module, files);
    }
    public void readDeploymentPlanFiles(javax.enterprise.deploy.spi.DeploymentConfiguration config, javax.enterprise.deploy.model.DeployableObject mod, java.io.File[] files) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException {
        ((DeploymentPlanSplitter)inner).readDeploymentPlanFiles(config, mod, files);
    }
    
}
