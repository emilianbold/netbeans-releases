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
import org.netbeans.modules.j2ee.sun.ide.Installer;


import java.io.File;



import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.sun.ide.j2ee.Utils;
import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.deployment.plugins.api.AppChangeDescriptor;

import org.netbeans.modules.j2ee.sun.api.ServerInterface;

/**
 *
 * @author  vkraemer
 */
public class DirectoryDeploymentFacade
        extends IncrementalDeployment  implements DeploymentPlanSplitter {
    
    Object inner = null;
      private File[] resourceDirs = null;
    private SunDeploymentManagerInterface dm;
   
    /** Creates a new instance of DirectoryDeploymentFacade */
    public DirectoryDeploymentFacade() {
        
        try{

               inner = Installer.getPluginLoader().loadClass("org.netbeans.modules.j2ee.sun.bridge.DirectoryDeployment").newInstance();//NOI18N
            
        } catch (Throwable t) {
          //  t.printStackTrace();
            System.out.println("WARNING: cannot create a good DirectoryDeploymentFacade: to correct, set com.sun.aas.installRoot to the correct App Server 8 PE Location and restart. -> " +t.getMessage());
        }
    }
    /** Creates a new instance of DirectoryDeploymentFacade */
    public DirectoryDeploymentFacade(DeploymentManager dm) {
        //System.out.println("DirectoryDeploymentFacade called");
        try {
            setDeploymentManager(dm);
            Class[] cls= new Class[1];
            cls[0]=DeploymentManager.class;
            java.lang.reflect.Constructor ctr =null;
                ctr = Installer.getPluginLoader().loadClass("org.netbeans.modules.j2ee.sun.bridge.DirectoryDeployment").getConstructor(cls);
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
    
//    public String[] getDeploymentPlanFileNames( javax.enterprise.deploy.shared.ModuleType module) {
//        if (null == inner)
//            return null;
//        return ((DeploymentPlanSplitter)inner).getDeploymentPlanFileNames(module);
//        
//    }
    /** Return a bogus name to satisfy the API. A file may be created by the
     * tool side. That file will be returned to this object in
     * writeDeploymentPlanFiles.  I will use it to find the directory for
     * writing the deployment descriptors and then delete it from that
     * directory.
     *
     * @param targetModuleID The module id
     * @return a single, unique file name.
     */
    public String[] getDeploymentPlanFileNames(ModuleType type) {
        String[] s;
        if (type==null){
            throw new IllegalArgumentException("invalid null argumment");
        }
        else if(type.equals(ModuleType.WAR)){
            s = new String[] { "WEB-INF/sun-web.xml" };
        }
        else if(type.equals(ModuleType.EJB)){
            s = new String[] { "META-INF/sun-ejb-jar.xml", "META-INF/sun-cmp-mappings.xml" };
        }
        else if(type.equals(ModuleType.EAR)){
            s = new String[] { "META-INF/sun-application.xml" };
        }
        else if(type.equals(ModuleType.RAR)){
            s = new String[] { "META-INF/sun-connector.xml" };
        }
        else if(type.equals(ModuleType.CAR)){
            s = new String[] { "META-INF/sun-client-application.xml" };
        }
      
        else{
            s = new String[] { ".timestamp" };
        }

        return s;
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

         return ((IncrementalDeployment)inner).incrementalDeploy(module, changes);
    }
    
    
    
    
    public ProgressObject initialDeploy(Target target, DeployableObject deployableObject, DeploymentConfiguration deploymentConfiguration, File file) {
       //Register resources if any
       File[] resourceDirs = Utils.getResourceDirs(deployableObject);
       if((resourceDirs != null) && (dm != null)) 
           Utils.registerResources(resourceDirs, (ServerInterface)dm.getManagement());
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
        //System.out.println("Ludo fix 2 getDirectoryForNewApplication"+deploymentName);
	return null;
    }
    
    public File getDirectoryForNewModule(File file, String str, DeployableObject deployableObject, DeploymentConfiguration deploymentConfiguration) {
        return ((IncrementalDeployment)inner).getDirectoryForNewModule(file,str, deployableObject,deploymentConfiguration);
    }
    
    //for DeploymentPlanSplitter

    public void readDeploymentPlanFiles(DeploymentConfiguration config, DeployableObject mod, File[] files) throws ConfigurationException {
        //Thread.dumpStack();
        SunONEDeploymentConfiguration s1dc =
            (SunONEDeploymentConfiguration) config;
        
        int len = getValidatedNumberOfFiles(mod, files);
        for (int i = 0; i < len; i++)
            s1dc.addFileToPlanForModule(files[i], mod);

    }
    
    public void writeDeploymentPlanFiles(DeploymentConfiguration config, DeployableObject mod, File[] files) throws ConfigurationException {
        SunONEDeploymentConfiguration s1dc =
            (SunONEDeploymentConfiguration) config;
        
        int len = getValidatedNumberOfFiles(mod, files);
        //
        for (int i = 0; i < len; i++) {
                s1dc.extractFileFromPlanForModule(files[i], mod);
        }
    }
        
    
    int getValidatedNumberOfFiles(DeployableObject mod, File[] files) throws ConfigurationException {
        int len = 0;
        if (null != files)
            len = files.length;
        // POST MS5 -- this should check by module type
        if (len < 1)
            throw new ConfigurationException("file list is too short");
        return len;
    }    
}
