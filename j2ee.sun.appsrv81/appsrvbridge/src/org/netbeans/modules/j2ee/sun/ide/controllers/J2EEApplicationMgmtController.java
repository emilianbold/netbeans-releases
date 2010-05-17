package org.netbeans.modules.j2ee.sun.ide.controllers;
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
import com.sun.appserv.management.j2ee.J2EEDeployedObject;
import java.util.Iterator;
import java.util.List;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.config.Enabled;
import com.sun.appserv.management.client.AppserverConnectionSource;
import com.sun.appserv.management.config.DeployedItemRefConfig;
import com.sun.appserv.management.config.J2EEApplicationConfig;
import com.sun.appserv.management.j2ee.J2EEApplication;
import com.sun.appserv.management.j2ee.EJBModule;
import com.sun.appserv.management.j2ee.WebModule;
import com.sun.appserv.management.j2ee.AppClientModule;
import com.sun.appserv.management.j2ee.ResourceAdapterModule;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.sun.bridge.apis.AppserverMgmtControllerBase;

import org.netbeans.modules.j2ee.sun.util.NodeTypes;

/**
 * Used as a conduit between the Netbeans API's and the AMX MBean API
 * data model. This API contains convenient methods for retrieving
 * components allowing the netbeans module heirarchy remain agnostic to the
 * underlying data model. 
 * 
 * This controller is used to navigate through deployed applications 
 * subcomponents such as web, ejb, connector, and app client modules.
 */
public class J2EEApplicationMgmtController extends AppserverMgmtControllerBase 
        implements DeployedItemsController, EnablerController {
    
    private J2EEApplication application;
    private J2EEApplicationConfig appConfig;
       
    /**
     * Create an instance of J2EEServerMgmtController used in the interaction
     * with AMX API for Sun Java System Application Server. 
     * 
     * @param server The AMX J2EEServer object representing a particular server.
     */
    public J2EEApplicationMgmtController(final J2EEApplication application, 
            final DeploymentManager dplmtMgr,
            final AppserverConnectionSource connection) {
        super(application, dplmtMgr, connection);
        this.application = application;
    }

     public J2EEApplicationMgmtController(final J2EEApplication application, 
            final J2EEApplicationConfig appConfig, 
            final DeploymentManager dplmtMgr,
            final AppserverConnectionSource connection) {
        super(appConfig, dplmtMgr, connection);
        this.application = application;
        this.appConfig = appConfig;
    }
    /**
     * Returns the properties of the application given the name.
     * 
     * @param appName The name of the application.
     * @return All the application properties.
     */
    public java.util.Map getProperties(List propsToIgnore) {  
        return getJ2EEAndConfigProperties(NodeTypes.ENTERPRISE_APPLICATION, 
            this.application, this.appConfig, propsToIgnore); 
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
        
        return ControllerUtil.setAttributeValue(application, appConfig, attrName, value, 
            getMBeanServerConnection());
    }
    
    /**
     *
     *
     */
    public EJBModuleController[] getEJBModules() {
        
        testIfServerInDebug();
        
        java.util.Map apps = application.getContaineeMap(EJBModule.J2EE_TYPE);
        java.util.Vector controllers = new java.util.Vector();
        for(Iterator itr = apps.values().iterator(); itr.hasNext(); ) {
            controllers.add(new EJBModuleController(
                (EJBModule)itr.next(), getDeploymentManager(),
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
    public AppClientModuleController[] getAppClientModules() {
        
        testIfServerInDebug();
        
        java.util.Map apps = application.getContaineeMap(AppClientModule.J2EE_TYPE);
        java.util.Vector controllers = new java.util.Vector();
        for(Iterator itr = apps.values().iterator(); itr.hasNext(); ) {
            controllers.add(new AppClientModuleController(
                (AppClientModule)itr.next(), getDeploymentManager(), 
                    appMgmtConnection));
        }
        AppClientModuleController[] result = 
            new AppClientModuleController[controllers.size()];
        return (AppClientModuleController[]) controllers.toArray(result);
    }

    
    /**
     *
     *
     */
    public ConnectorModuleController[] getConnectorModules() {
        
        testIfServerInDebug();
        
        String resJ2EETypeProp = 
            Util.makeJ2EETypeProp(ResourceAdapterModule.J2EE_TYPE);
        String appProperty = 
            JMXUtil.makeProp(J2EEApplication.J2EE_TYPE, getName());
        String props = Util.concatenateProps(resJ2EETypeProp, appProperty);
        java.util.Set embeddedRars = getQueryMgr().queryPropsSet(props);
        java.util.Vector resAdaptorModules = new java.util.Vector();
        for(Iterator itr = embeddedRars.iterator(); itr.hasNext(); ) {
            resAdaptorModules.add(new ConnectorModuleController(
                    (ResourceAdapterModule)itr.next(), getDeploymentManager(),
                        appMgmtConnection));
        }
        ConnectorModuleController[] result = 
            new ConnectorModuleController[resAdaptorModules.size()];
        return (ConnectorModuleController[]) resAdaptorModules.toArray(result);
    }
    
    
    /**
     *
     *
     */
    public WebModuleController[] getWebModules() {
        
        testIfServerInDebug();
        java.util.Map apps = application.getContaineeMap(WebModule.J2EE_TYPE);

        java.util.Vector controllers = new java.util.Vector();
        for(Iterator itr = apps.values().iterator(); itr.hasNext(); ) {
            controllers.add(new WebModuleController(
                (WebModule)itr.next(), getDeploymentManager(),
                    appMgmtConnection));
        }
        WebModuleController[] result = 
            new WebModuleController[controllers.size()];
        return (WebModuleController[]) controllers.toArray(result);
    }
    
    /**
     * Returns all the names of the web modules embedded in the specified
     * deployed application.
     *
     * @return An array of modules embedded in this application.
     */
    public String[] getEmbeddedModulesByType(String nodeType) {
        
        testIfServerInDebug();
        
        return ControllerUtil.getComponentNamesFromMap(
            application.getContaineeMap(
                NodeTypes.getAMXJ2EETypeByNodeType(nodeType)));
    }
    
    
    /**
     *
     */
    public String[] getEjbsByType(String nodeType, String ejbModuleName, 
            String ejbName) {
        
        testIfServerInDebug();
        
         return ControllerUtil.getComponentNamesFromMap(
            application.getContaineeMap(
                NodeTypes.getAMXJ2EETypeByNodeType(nodeType)));
    }
    
    
    /**
     * Returns the EJBModule object by ejb module name.
     *
     * @param ejbModuleName The name of the ejbmodule.
     * @return The EJBModule object corresponding to the name given.
     */
    public EJBModule getEJBModuleByName(String ejbModuleName) {
        
        testIfServerInDebug();
        
        java.util.Map modules = application.getContaineeMap(EJBModule.J2EE_TYPE);
        return (EJBModule) modules.get(ejbModuleName);
    }
    
    
    /**
     *
     *
     *
     */
    public boolean isEnabled() {
        testIfServerInDebug();
        boolean configEnabled = ((Enabled)appConfig).getEnabled();
        return ControllerUtil.calculateIsEnabled(appConfig, configEnabled);
    }
    
    
    /**
     *
     *
     *
     */
    public void setEnabled(boolean enabled) {
        testIfServerInDebug();
        DeployedItemRefConfig config = ControllerUtil.getDeployedItemRefConfig(appConfig);
        if(config != null)
            config.setEnabled(enabled);
    }

    public J2EEDeployedObject getJ2EEObject() {
        return application;
    }
    
}



