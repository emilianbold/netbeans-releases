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
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import java.util.*;
import java.io.*;
import org.netbeans.modules.j2ee.deployment.config.*;
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
    
    /**
     * This will return url to invoke webbrowser for web client.
     * If there is no webclient, null will be returned. 
     *
     * Caution: this call does not attempt to detect whehter the client specified 
     * by clientName is app client.
     */
    public String getClientUrl(String partUrl) {
        // determine client module
        J2eeModule clientModule = null;
        String url = null;
        
        if (moduleProvider instanceof J2eeAppProvider) {
            J2eeAppProvider ear = (J2eeAppProvider) moduleProvider;
            J2eeModuleProvider clientProvider = ConfigurationStorage.getChildModuleProvider(ear, clientName);
            if (clientProvider != null)
                clientModule = clientProvider.getJ2eeModule();
            else {
                //findWebUrl(null) will take care to find a first weburl it sees, but just to be sure...
                J2eeModuleContainer jmc = (J2eeModuleContainer) ear.getJ2eeModule();
                J2eeModule[] modules = jmc.getModules(null);
                for (int i=0; i<modules.length; i++) {
                    if (J2eeModule.WAR.equals(modules[i].getModuleType())) {
                        clientModule = modules[i];
                        break;
                    }
                }
            }
        } else {
            clientModule = moduleProvider.getJ2eeModule();
        }
        
        url = findWebUrl(clientModule);
        if (url != null) {
            if (partUrl.startsWith("/"))
                return (url + partUrl);
            else
                return (url + "/" + partUrl); //NOI18N
        } else {
            return null;
        }
    }
    
    private TargetModule getTargetModule() {
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
        execMod.initDelegate((ModuleType)getModule().getModuleType());
        return execMod;
    }
    
    /**
     * Find the web URL for the given client module.
     * If null is passed, or when plugin failed to resolve the child module url,
     * this will attempt to return the first web url it sees.
     */
    private String findWebUrl(J2eeModule client) {
        TargetModule module = getTargetModule();
        if (module == null) {
            return null;
        }
        if (getModule() == client) { // stand-alone web
            return module.getWebURL();
        }
        
        ServerInstance instance = ServerRegistry.getInstance().getServerInstance(module.getInstanceUrl());
        IncrementalDeployment mur = instance.getIncrementalDeployment ();
        String clientModuleUri = client == null ? "" : client.getUrl();
        if (clientModuleUri.startsWith("/")) { //NOI18N
                clientModuleUri = clientModuleUri.substring(1);
        }
        TargetModuleID[] children = module.getChildTargetModuleID();
        String urlString = null;
        TargetModuleID tmid = null;
        for (int i=0; children != null && i<children.length; i++) {
            // remember when see one, just for a rainy day
            if (urlString == null || urlString.trim().equals("")) {
                urlString = children[i].getWebURL();
            }
            
            String uri = children[i].getModuleID(); //NOI18N
            if (mur != null) {
                uri = mur.getModuleUrl(children[i]);
            } else {
                int j = uri.indexOf('#');
                if (j > -1) {
                    uri = uri.substring(j+1);
                }
            }
            
            if (mur != null && clientModuleUri.equalsIgnoreCase(uri)) {
                tmid = children[i];
                break;
            }
        }
        // prefer the matched
        if (tmid != null) {
            urlString = tmid.getWebURL();
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