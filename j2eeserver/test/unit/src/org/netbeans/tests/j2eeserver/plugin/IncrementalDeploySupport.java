/*
 *                 Sun Public License Notice
 *
 * The contents of thisfile are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.tests.j2eeserver.plugin;

import javax.enterprise.deploy.model.*;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.shared.*;
import javax.enterprise.deploy.spi.status.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.tests.j2eeserver.plugin.jsr88.*;

import java.io.*;
import java.util.*;

/**
 *
 * @author  nn136682
 */
public class IncrementalDeploySupport extends IncrementalDeployment {
    DepManager dm;
    File applicationsDir;
    HashMap moduleDirectories = new HashMap();
    
    /** Creates a new instance of IncrementalDeploySupport */
    public IncrementalDeploySupport(DeploymentManager manager) {
        this.dm = (DepManager) dm;
    }
    
    public void setDeploymentManager(DeploymentManager manager) {
        if (manager instanceof DepManager)
            dm = (DepManager) manager;
        else
            throw new IllegalArgumentException("setDeploymentManager: Invalid manager type");
    }
    
    File getApplicationsDir() {
        if (applicationsDir != null)
            return applicationsDir;
        
        File userdir = new File(System.getProperty("netbeans.user"));
        applicationsDir = new File(userdir, "testplugin/applications");
        if (! applicationsDir.exists())
            applicationsDir.mkdirs();
        return applicationsDir;
    }
        
    static Map planFileNames = new HashMap();
    static {
        planFileNames.put(ModuleType.WAR, new String[] {"tpi-web.xml"});
        planFileNames.put(ModuleType.EJB, new String[] {"tpi-ejb-jar.xml"});
        planFileNames.put(ModuleType.EAR, new String[] {"tpi-application.xml"});
    }
    
    public File getDirectoryForModule (TargetModuleID module) {
        File appDir = new File(getApplicationsDir(), getModuleRelativePath((TestTargetMoid)module));
        if (! appDir.exists())
            appDir.mkdirs();
        System.out.println("getDirectoryForModule("+module+") returned: "+appDir);
        return appDir;
    }
    
    String getModuleRelativePath(TestTargetMoid module) {
        File path;
        if (module.getParent() != null)
            path = new File(module.getParent().getModuleID(), module.getModuleID());
        else
            path = new File(module.getModuleID());
        return path.getPath();
    }
    
    public String getModuleUrl(TargetModuleID module) {
        return ((TestTargetMoid)module).getModuleUrl();
    }
    
    
    public ProgressObject incrementalDeploy (TargetModuleID module, AppChangeDescriptor changes) {
        return null;//dm.incrementalDeploy(module, changes);
    }
    
    public boolean canFileDeploy (Target target, DeployableObject deployable) {
        return true;
    }    
    
    public File getDirectoryForNewApplication (Target target, DeployableObject app, DeploymentConfiguration configuration) {
        return null;
    }
    
    public File getDirectoryForNewModule (File appDir, String uri, DeployableObject module, DeploymentConfiguration configuration) {
        return null;
    }
    
    public ProgressObject initialDeploy (Target target, DeployableObject app, DeploymentConfiguration configuration, File dir) {
        return null;
    }
    
}
