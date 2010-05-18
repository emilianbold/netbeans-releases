/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.j2ee.sun.ide.controllers;

import java.util.Iterator;
import java.util.Map;
import java.util.List;
import com.sun.appserv.management.j2ee.AppClientModule;
import com.sun.appserv.management.j2ee.EJBModule;
import com.sun.appserv.management.j2ee.J2EEApplication;
import com.sun.appserv.management.j2ee.J2EEServer;
import com.sun.appserv.management.j2ee.ResourceAdapterModule;
import com.sun.appserv.management.j2ee.WebModule;
import com.sun.appserv.management.client.AppserverConnectionSource;
import com.sun.appserv.management.config.AppClientModuleConfig;
import com.sun.appserv.management.config.EJBModuleConfig;
import com.sun.appserv.management.config.J2EEApplicationConfig;
import com.sun.appserv.management.config.RARModuleConfig;
import com.sun.appserv.management.config.WebModuleConfig;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.j2ee.J2EEDeployedObject;
import javax.management.ObjectName;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.sun.bridge.apis.AppserverMgmtControllerBase;
import org.netbeans.modules.j2ee.sun.bridge.apis.Controller;

/**
 * Used as a conduit between the Netbeans API's and the AMX MBean API
 * data model. This API contains convienent methods for retrieving
 * components allowing the netbeans module heirarchy remain agnostic to the
 * underlying data model. This controller is used to navigate through deployed
 * applications and web, ejb, connector, and app client modules.
 */
public class J2EEServerMgmtController 
        extends AppserverMgmtControllerBase implements Controller {
    
    private J2EEServer j2eeServer;
    private DomainConfig domainConfig;
    
    /**
     * Create an instance of J2EEServerMgmtController used in the interaction
     * with AMX API for Sun Java System Application Server. 
     * 
     * @param server The AMX J2EEServer object representing a particular server.
     */
    public J2EEServerMgmtController(final J2EEServer server, 
            final DeploymentManager dplmtMgr, 
            final AppserverConnectionSource connection) {
        super(server, dplmtMgr, connection);
        this.j2eeServer = server;
        this.domainConfig = j2eeServer.getDomainRoot().getDomainConfig();
    }

    
    /**
     * Returns all the properties of this server instance.
     *
     * @return All the properties of this server instance as a java.util.Map.
     */
    public Map getProperties(List propsToIgnore) { 
        return new java.util.HashMap() {};
    }
    
    
    /**
     * Sets the properties.
     *
     * @param attrname The name of the attribute.
     * @param value The value of the attribute to set. 
     *
     * @return updated Attribute
     */
    public javax.management.Attribute setProperty(final String attrName, final Object value) { 
        
        testIfServerInDebug();
        
        return ControllerUtil.setAttributeValue(j2eeServer, attrName, value, 
            getMBeanServerConnection());
    }
    
    
    /**
     *
     *
     */
    public J2EEApplicationMgmtController[] getApplications() {
        
        testIfServerInDebug();
        Map appsMap = j2eeServer.getContaineeMap(J2EEApplication.J2EE_TYPE);
        Map configPeerApps = ControllerUtil.getDeployedObjects(domainConfig.getJ2EEApplicationConfigMap());
        
        java.util.Vector controllers = new java.util.Vector();
        for(Iterator configPeerItr = configPeerApps.values().iterator(); configPeerItr.hasNext(); ) {
            J2EEApplicationConfig appConfig = (J2EEApplicationConfig)configPeerItr.next(); 
            J2EEApplication appMod = null;
            if(appsMap.containsKey(appConfig.getName())){
                appMod = (J2EEApplication)appsMap.get(appConfig.getName());
            }
            controllers.add(new J2EEApplicationMgmtController(
                appMod, appConfig, getDeploymentManager(), 
                    appMgmtConnection)); 
        }
        
        J2EEApplicationMgmtController[] result = 
            new J2EEApplicationMgmtController[controllers.size()];
        return (J2EEApplicationMgmtController[]) controllers.toArray(result);
    }
    
    
    /**
     *
     *
     */
    public EJBModuleController[] getEJBModules() {
        
        testIfServerInDebug();
        
        Map appsMap = j2eeServer.getContaineeMap(EJBModule.J2EE_TYPE);
        Map configPeerApps = ControllerUtil.getDeployedObjects(domainConfig.getEJBModuleConfigMap()); 
        
        java.util.Vector controllers = new java.util.Vector();
        for(Iterator configPeerItr = configPeerApps.values().iterator(); configPeerItr.hasNext(); ) {
            EJBModuleConfig ejbConfig = (EJBModuleConfig)configPeerItr.next(); 
            EJBModule ejbMod = null;
            if(appsMap.containsKey(ejbConfig.getName())){
                ejbMod = (EJBModule)appsMap.get(ejbConfig.getName());
            }
            controllers.add(new EJBModuleController(
                ejbMod, ejbConfig, getDeploymentManager(), 
                    appMgmtConnection));            
        }
        
        EJBModuleController[] result = 
            new EJBModuleController[controllers.size()];
        return (EJBModuleController[]) controllers.toArray(result);
    }
    
    
    /**
     *
     *
     */
    public ConnectorModuleController[] getConnectorModules() {
        
        testIfServerInDebug();
        Map appsMap = j2eeServer.getContaineeMap(ResourceAdapterModule.J2EE_TYPE);
        Map configPeerApps = ControllerUtil.getDeployedObjects(domainConfig.getRARModuleConfigMap()); 
        
        java.util.Vector controllers = new java.util.Vector();
        for(Iterator configPeerItr = configPeerApps.values().iterator(); configPeerItr.hasNext(); ) {
            RARModuleConfig rarConfig = (RARModuleConfig)configPeerItr.next(); 
            ResourceAdapterModule rarMod = null;
            if(appsMap.containsKey(rarConfig.getName())){
                rarMod = (ResourceAdapterModule)appsMap.get(rarConfig.getName());
            }
        
            controllers.add(new ConnectorModuleController(
                rarMod, rarConfig, getDeploymentManager(), appMgmtConnection));
        }
        ConnectorModuleController[] result = 
            new ConnectorModuleController[controllers.size()];
        return (ConnectorModuleController[]) controllers.toArray(result);
    }

    
    /**
     *
     *
     */
    public WebModuleController[] getWebModules() {
        
        testIfServerInDebug();
        
        Map appsMap = j2eeServer.getContaineeMap(WebModule.J2EE_TYPE);
        Map configPeerApps = ControllerUtil.getDeployedObjects(domainConfig.getWebModuleConfigMap()); 
        
        java.util.Vector controllers = new java.util.Vector();
        for(Iterator configPeerItr = configPeerApps.values().iterator(); configPeerItr.hasNext(); ) {
            WebModuleConfig webConfig = (WebModuleConfig)configPeerItr.next(); 
            WebModule webMod = null;
            if(appsMap.containsKey(webConfig.getName())){
                webMod = (WebModule)appsMap.get(webConfig.getName());
            }
            controllers.add(new WebModuleController(
                webMod, webConfig, getDeploymentManager(), 
                    appMgmtConnection));            
        }
       
        WebModuleController[] result = 
            new WebModuleController[controllers.size()];
        return (WebModuleController[]) controllers.toArray(result);
    }
    
    /**
     *
     *
     */
    public SIPController[] getSIPModules() {
        testIfServerInDebug();
        ObjectName[] sipApps = ControllerUtil.getSIPComponents(getMBeanServerConnection());
        java.util.Vector controllers = new java.util.Vector();
        for(int i=0; i<sipApps.length; i++){
            ObjectName sipAppObjName = sipApps[i];
            controllers.add(new SIPController(sipAppObjName, getDeploymentManager(), appMgmtConnection)); 
        }
        SIPController[] result = new SIPController[controllers.size()];
        return (SIPController[]) controllers.toArray(result);
    }
    

    /**
     * Return the web module name of a standlaone or embeded webmodule
     * from the context root
     * @param String reprsenting the Context Root of the web module
     * @return String giving the web module name. Can be null
     */
    public String getWebModuleName(String contextRoot) {
        
        testIfServerInDebug();
        String modName = null;
        Map webApps = ControllerUtil.getDeployedObjects(domainConfig.getWebModuleConfigMap()); 
        
        for(Iterator webAppsItr = webApps.values().iterator(); webAppsItr.hasNext(); ) {
            WebModuleConfig webConfig = (WebModuleConfig)webAppsItr.next(); 
            String cRoot = webConfig.getContextRoot();
            if(cRoot.equals(contextRoot)){
                modName = webConfig.getName();
                break;
            }    
        }  
        
        if(modName == null){
            Map j2eeApps = ControllerUtil.stripOutSystemApps(
                j2eeServer.getContaineeMap(J2EEApplication.J2EE_TYPE));
            for(Iterator itr = j2eeApps.values().iterator(); itr.hasNext(); ) {
                J2EEApplication j2eeApp = (J2EEApplication)itr.next();
                
                Map compWebs = j2eeApp.getContaineeMap(WebModule.J2EE_TYPE);
                for(Iterator webItr = compWebs.values().iterator(); webItr.hasNext(); ) {
                    WebModule webApp = (WebModule)webItr.next();
                    String cRoot = webApp.getPath();                   
                    if(cRoot.equals(contextRoot)){
                        String appName = j2eeApp.getName();
                        String webName = webApp.getName();
                        if(webName.indexOf("//server") != -1)  //NOI18N
                            webName = webName.substring(8, webName.length());
                        webName = webName + "_war"; //NOI18N
                        modName = appName + webName;
                        break;
                    }
                } //for
                
            }
        } //modName null
        return modName;
    }
    
    /**
     *
     */
    public AppClientModuleController[] getAppClientModules() {
        
        testIfServerInDebug();
        
        Map configPeerApps = ControllerUtil.getDeployedObjects(domainConfig.getAppClientModuleConfigMap()); 
        Map appsMap = j2eeServer.getContaineeMap(AppClientModule.J2EE_TYPE);
        
        java.util.Vector controllers = new java.util.Vector();
        for(Iterator configPeerItr = configPeerApps.values().iterator(); configPeerItr.hasNext(); ) {
            AppClientModuleConfig appClientConfig = (AppClientModuleConfig)configPeerItr.next(); 
            AppClientModule appClientMod = null;
            if(appsMap.containsKey(appClientConfig.getName())){
                appClientMod = (AppClientModule)appsMap.get(appClientConfig.getName());
            }
            controllers.add(new AppClientModuleController(
                appClientMod, appClientConfig, getDeploymentManager(), 
                    appMgmtConnection));            
        }
        
        AppClientModuleController[] result = 
            new AppClientModuleController[controllers.size()];
        return (AppClientModuleController[]) controllers.toArray(result);
    }
    
    private Map getJ2EEAppsAsMap(String type){
        Map apps = ControllerUtil.stripOutSystemApps(j2eeServer.getContaineeMap(type));
        Map j2eePeerMap = new java.util.HashMap();
        for(Iterator itr = apps.values().iterator(); itr.hasNext(); ) {
            J2EEDeployedObject j2eeMod = (J2EEDeployedObject)itr.next();
            if(j2eeMod.getConfigPeer() != null)
                j2eePeerMap.put(j2eeMod.getConfigPeer().getName(), j2eeMod);
        }
        return j2eePeerMap;
    }
    
    public String getServerName(){
        return j2eeServer.getserverVersion();
    }
}
