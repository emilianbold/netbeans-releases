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
import org.netbeans.modules.j2ee.deployment.plugins.spi.*;
import org.netbeans.modules.j2ee.deployment.impl.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.shared.ModuleType;
import org.openide.util.Lookup;
import org.openide.awt.StatusDisplayer;
import java.util.*;
import java.io.File;
import java.net.URL;

/** 
 *
 * @author  George FinKlang
 */
public final class DeploymentTargetImpl implements DeploymentTarget {
    
    J2eeProfileSettings settings;
    J2eeDeploymentLookup deployment;
    TargetModule[] targetModules;
    
    /**
     * @param target build target that provides J2eeModule
     */
    public DeploymentTargetImpl(J2eeProfileSettings settings, J2eeDeploymentLookup deployment) {
        this.settings = settings;
        this.deployment = deployment;
    }
    
    public boolean dontDeploy() {
        return settings.DEPLOY_NONE.equals (settings.getDeployment());
    }
    
    //PENDING: UI
    static Boolean fastDeploy;
    public boolean doFastDeploy() {
        if (fastDeploy != null)
            return fastDeploy.booleanValue();
        
        if ("false".equalsIgnoreCase(System.getProperty("j2eeserver.fastDeploy"))) {
            System.out.println("FastDeploy is off!");
            fastDeploy = Boolean.FALSE;
        } else
            fastDeploy = Boolean.TRUE;
        
        return fastDeploy.booleanValue();
    }
    
    public J2eeModule getModule() {
        return deployment.getProvider ().getJ2eeModule ();
    }
    
    public ModuleChangeReporter getModuleChangeReporter() {
        return deployment.getProvider ().getModuleChangeReporter ();
    }
    
    public void startClient() {
        if (! settings.getShowClient().booleanValue())
            return;
        
        // determine client module
        J2eeModule clientModule = getModule();
        String url = null;
        if (clientModule instanceof J2eeModuleContainer) {
            String clientName = settings.getClientModule();
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
                return;
            
            TargetModule tmid = getTargetModule(getModule());
            url = getChildWebUrl(tmid, clientModule);
            
        } else if (clientModule.getModuleType().equals(J2eeModule.WAR)) {
            TargetModule tmid = getTargetModule(clientModule);
            if (tmid != null)
                url = tmid.getWebURL();
        }
        
        if (url != null)
            startWebClient(url);
        else
            return; //PENDING implement start non-web client
    }
    
    private TargetModule getTargetModule(J2eeModule module) {
        TargetModule[] mods = getTargetModules();
        if (mods == null || mods.length == 0)
            return null;
        
        // determine target server instance
        ServerString defaultTarget = ServerRegistry.getInstance().getDefaultInstance();
        TargetModule execMod = null;
        if ( defaultTarget != null ) {
            for (int i=0; i<mods.length; i++) {
                if (mods[i].getInstanceUrl().equals(defaultTarget.getUrl()) &&
                    mods[i].getTargetName().equals(defaultTarget.getTargets()[0])) {
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
        ModuleUrlResolver mur = instance.getModuleUrlResolver();
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
            System.out.println("Failed to find TargetModuleID for webclient :"+childWeb.getUrl()+" from: "+module);
            return null;
        }
        String urlString = tmid.getWebURL();
        if (urlString == null) {
            System.out.println("Failed to get webURL for webclient :"+childWeb.getUrl()+" from: "+tmid);
            return null;
        }
        return urlString;
            }
    private void startWebClient(String urlString) {
        String defaultURL = settings.getDefaultUrl();
        if (defaultURL != null && ! defaultURL.trim().equals("") ){
            urlString += "/" + defaultURL;
        }
        
        StatusDisplayer.getDefault().setStatusText("Starting browser to "+urlString);
        
        try {
            URL url = new URL(urlString);
            org.openide.awt.HtmlBrowser.URLDisplayer.getDefault().showURL (url);
        } catch (Exception io) {
            //PENDING better feedback
            io.printStackTrace();
        }
    }
    
    public File getConfigurationFile() {
        return settings.getConfigurationFile();
    }
    
    public ServerString getServer() {
        return settings.getServerString();
    }
    
    public TargetModule[] getTargetModules() {
        if (targetModules == null)
            targetModules = settings.getTargetModules();
        return targetModules;
    }
    
    public void setTargetModules(TargetModule[] targetModules) {
        this.targetModules = targetModules;
        settings.setTargetModules(targetModules);
    }
    
    public DeploymentConfigurationProvider getDeploymentConfigurationProvider() {
        return deployment.getStorage ();
    }    
    
}
