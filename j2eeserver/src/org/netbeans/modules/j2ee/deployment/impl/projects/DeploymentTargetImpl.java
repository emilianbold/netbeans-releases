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

package org.netbeans.modules.j2ee.deployment.impl.projects;

import org.netbeans.modules.j2ee.deployment.execution.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.j2ee.deployment.impl.*;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.shared.ModuleType;
import org.openide.util.Lookup;
import org.openide.ErrorManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileUtil;
import java.util.*;
import java.io.*;
import java.net.URL;
import org.netbeans.modules.j2ee.deployment.config.*;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/** 
 *
 * @author  George FinKlang
 */
public final class DeploymentTargetImpl implements DeploymentTarget {
    
    J2eeModuleProvider moduleProvider;
    String clientName;
    ServerString server;
    TargetModule[] targetModules;
    
    public DeploymentTargetImpl(J2eeModuleProvider moduleProvider, String clientName) {
        this.moduleProvider = moduleProvider;
        this.clientName = clientName;
    }
    
    public J2eeModule getModule() {
        return moduleProvider.getJ2eeModule ();
    }
    
    public ModuleChangeReporter getModuleChangeReporter() {
        return moduleProvider.getModuleChangeReporter ();
    }
    
    public String getClientUrl(String partUrl) {
        // determine client module
        J2eeModule clientModule = getModule();
        String url = null;
        if (clientModule instanceof J2eeModuleContainer) {
            J2eeModuleContainer ear = (J2eeModuleContainer)clientModule;
            J2eeModule[] children = ear.getModules(null);
            clientModule = null;
            J2eeModule secondaryNonWeb = null;
            for (int i=0; i<children.length; i++) {
                if (children[i].getUrl().equals(clientName)) {
                    clientModule = children[i];
                    break;
                } else if (clientModule == null && children[i].getModuleType().equals(J2eeModule.WAR)) {
                    clientModule = children[i];
                } else if (secondaryNonWeb == null && children[i].getModuleType().equals(J2eeModule.CLIENT))
                    secondaryNonWeb = children[i];
            }
            if (clientModule == null)
                clientModule = secondaryNonWeb;
            
            // Has no webs or nor client modules
            if (clientModule == null)
                return null;
            
            TargetModule tmid = getTargetModule(getModule());
            url = getChildWebUrl(tmid, clientModule);
            
        } else if (clientModule.getModuleType().equals(J2eeModule.WAR)) {
            TargetModule tmid = getTargetModule(clientModule);
            if (tmid != null)
                url = tmid.getWebURL();
        }

        if (url != null) {
            return (url + partUrl);
        } else {
            return null;
        }
    }
    
    private TargetModule getTargetModule(J2eeModule module) {
        TargetModule[] mods = getTargetModules();
        if (mods == null || mods.length == 0)
            return null;
        
        if (mods[0].delegate() != null)
            return mods[0];
        
        // determine target server instance
        ServerString defaultTarget = ServerRegistry.getInstance().getDefaultInstance();
        TargetModule execMod = null;
        if ( defaultTarget != null ) {
            for (int i=0; i<mods.length; i++) {
                if (mods[i].getInstanceUrl().equals(defaultTarget.getUrl()) &&
                    mods[i].getTargetName().equals(defaultTarget.getTargets(true)[0])) {
                    execMod = mods[i];
                    break;
                }
            }
        }

        if (execMod == null) execMod = mods[0];
        execMod.initDelegate((ModuleType)module.getModuleType());
        return execMod;
    }
    
    private String getChildWebUrl(TargetModule module, J2eeModule childWeb) {
        ServerInstance instance = ServerRegistry.getInstance().getServerInstance(module.getInstanceUrl());
        IncrementalDeployment mur = instance.getIncrementalDeployment ();
        TargetModuleID tmid = null;
        String clientModuleUri = childWeb.getUrl();
            TargetModuleID[] children = module.getChildTargetModuleID();
            for (int i=0; children != null && i<children.length; i++) {
                if (clientModuleUri.equals(mur.getModuleUrl(children[i]))) {
                    tmid = children[i];
                    break;
                }
            }
        if (tmid == null) {
//            System.out.println("Failed to find TargetModuleID for webclient :"+childWeb.getUrl()+" from: "+module);
            return null;
        }
        String urlString = tmid.getWebURL();
        if (urlString == null) {
//            System.out.println("Failed to get webURL for webclient :"+childWeb.getUrl()+" from: "+tmid);
            return null;
        }
        return urlString;
    }
    
    private ConfigSupportImpl getConfigSupportImpl () {
        return (ConfigSupportImpl) moduleProvider.getConfigSupport ();
    }
    
    public File getConfigurationFile() {
        return getConfigSupportImpl ().getConfigurationFile ();
    }
    
    public ServerString getServer() {
        if (server == null) {
            String instanceID = moduleProvider.getServerInstanceID ();
            ServerInstance inst = ServerRegistry.getInstance ().getServerInstance (instanceID);
            if (inst == null) {
                throw new RuntimeException(NbBundle.getMessage(DeploymentTargetImpl.class, "MSG_TargetServerNotFound",instanceID));
            }
            server = new ServerString(inst);
        }
        return server;
    }
    
    public TargetModule[] getTargetModules() {
        if (targetModules == null || targetModules.length == 0) {
            String fname = getTargetModuleFileName();
            if (fname == null) {
                return null;
            }
            targetModules = TargetModule.load(getServer(), fname);
        }
        return targetModules;
    }
    
    public void setTargetModules(TargetModule[] targetModules) {
        this.targetModules = targetModules;
        for (int i=0; i< targetModules.length; i++) {
            targetModules[i].save(getTargetModuleFileName());
        }
    }
    
    public DeploymentConfigurationProvider getDeploymentConfigurationProvider() {
        return getConfigSupportImpl ().getStorage ();
    }
    
    public J2eeModuleProvider.ConfigSupport getConfigSupport () {
        return moduleProvider.getConfigSupport();
    }

    private String getTargetModuleFileName() {
        String fileName = getDeploymentName();
        if (fileName != null)
            return fileName;
        
        File f = null;
        try {
            if (getModule().getContentDirectory() != null) {
                f = FileUtil.toFile(getModule().getContentDirectory());
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
        }
        if (f == null) {
            fileName = getConfigSupportImpl().getDeploymentName();
        } else {
            String pathName = f.getAbsolutePath();
            fileName = TargetModule.shortNameFromPath(pathName);
        }
        return fileName;
    }
    
    public String getDeploymentName() {
        return moduleProvider.getDeploymentName();
    }
}