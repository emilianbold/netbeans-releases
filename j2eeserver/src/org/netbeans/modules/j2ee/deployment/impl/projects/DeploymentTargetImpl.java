/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2ee.deployment.impl.projects;

import org.netbeans.modules.j2ee.deployment.execution.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.j2ee.deployment.impl.*;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.shared.ModuleType;
import org.openide.filesystems.FileUtil;
import java.util.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.config.*;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
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
        
        if (moduleProvider instanceof J2eeApplicationProvider) {
            J2eeApplicationProvider ear = (J2eeApplicationProvider) moduleProvider;
            J2eeModuleProvider clientProvider = getChildModuleProvider(ear, clientName);
            if (clientProvider != null)
                clientModule = clientProvider.getJ2eeModule();
            else {
                //findWebUrl(null) will take care to find a first weburl it sees, but just to be sure...
                J2eeApplication jmc = (J2eeApplication) ear.getJ2eeModule();
                J2eeModule[] modules = jmc.getModules();
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
    
    private J2eeModuleProvider getChildModuleProvider(J2eeModuleProvider jmp, String uri) {
        if (uri == null)
            return null;
        J2eeModuleProvider child = null;
        if (jmp instanceof J2eeApplicationProvider) {
            J2eeApplicationProvider jap = (J2eeApplicationProvider) jmp;
            child = jap.getChildModuleProvider(uri);
            if (child == null) {
                String root = "/" ; // NOI18N
                if (uri.startsWith(root)) {
                    uri = uri.substring(1);
                } else {
                    uri = root + uri;
                }
                child = jap.getChildModuleProvider(uri);
            }
        }
        return child;
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
        return targetModules.clone();
    }
    
    public void setTargetModules(TargetModule[] targetModules) {
        this.targetModules = targetModules.clone();
        for (int i=0; i< targetModules.length; i++) {
            targetModules[i].save(getTargetModuleFileName());
        }
    }
    
    public ModuleConfigurationProvider getModuleConfigurationProvider() {
        return getConfigSupportImpl ();
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
            Logger.getLogger("global").log(Level.INFO, null, ioe);
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