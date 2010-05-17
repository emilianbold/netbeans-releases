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

import java.util.List;
import com.sun.appserv.management.client.AppserverConnectionSource;
import com.sun.appserv.management.config.Enabled;
import com.sun.appserv.management.j2ee.J2EEDeployedObject;
import com.sun.appserv.management.j2ee.ResourceAdapterModule;
import com.sun.appserv.management.j2ee.ResourceAdapter;
import com.sun.appserv.management.config.RARModuleConfig;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.sun.bridge.apis.AppserverMgmtControllerBase;

import org.netbeans.modules.j2ee.sun.util.NodeTypes;


/**
 * Used as a conduit between the Netbeans API's and the AMX MBean API
 * data model. This API contains convenient methods for retrieving
 * components allowing the netbeans module heirarchy remain agnostic to the
 * underlying data model. 
 * 
 * This controller is used to navigate through deployed EJB modules.
 */
public class ConnectorModuleController extends AppserverMgmtControllerBase 
        implements DeployedItemsController, EnablerController {
    
    private ResourceAdapterModule connectorModule;
    private RARModuleConfig connectorConfig;
    
    /**
     * Create an instance of J2EEServerMgmtController used in the interaction
     * with AMX API for Sun Java System Application Server. 
     * 
     * @param server The AMX J2EEServer object representing a particular server.
     */
    public ConnectorModuleController(ResourceAdapterModule connectorModule, 
            AppserverConnectionSource connection) {
        super(connectorModule, connection);
        this.connectorModule = connectorModule;
    }
    
    
    /**
     * Create an instance of J2EEServerMgmtController used in the interaction
     * with AMX API for Sun Java System Application Server. 
     * 
     * @param server The AMX J2EEServer object representing a particular server.
     */
    public ConnectorModuleController(
            final ResourceAdapterModule connectorModule, 
            final DeploymentManager dplmtMgr,
            final AppserverConnectionSource connection) {
        super(connectorModule, dplmtMgr, connection);
        this.connectorModule = connectorModule;
    }

    public ConnectorModuleController(
            final ResourceAdapterModule connectorModule,
            final RARModuleConfig connectorConfig,
            final DeploymentManager dplmtMgr,
            final AppserverConnectionSource connection) {
        super(connectorConfig, dplmtMgr, connection);
        this.connectorModule = connectorModule;
        this.connectorConfig = connectorConfig;
    }
    
    /**
     * Returns the properties of the application given the name.
     * 
     * @param propsToIgnore String properties to ignore.
     * @return All the application properties.
     */
    public java.util.Map getProperties(List propsToIgnore) {
        return getJ2EEAndConfigProperties(NodeTypes.CONNECTOR_MODULE, 
            connectorModule, connectorConfig, propsToIgnore);
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
        
        return ControllerUtil.setAttributeValue(connectorModule, connectorConfig, attrName, value, 
            getMBeanServerConnection());
    }
    
    /**
     *
     *
     */
    public String[] getResourceAdaptors() {
        
        testIfServerInDebug();
        
        return ControllerUtil.getComponentNamesFromMap(
            connectorModule.getContaineeMap(
                NodeTypes.getAMXJ2EETypeByNodeType(NodeTypes.RESOURCE_ADAPTER)));
    }
    
    
    /**
     *
     */
    public java.util.Map getResourceAdapterProperties(
            final String resAdapterName, final List propsToIgnore) {
        ResourceAdapter resAdapter = getResourceAdapterByName(resAdapterName);
        return getJ2EEAndConfigProperties(NodeTypes.RESOURCE_ADAPTER, 
            resAdapter, resAdapter.getConfigPeer(), propsToIgnore);
    }
    
    
    /**
     *
     */
    public javax.management.Attribute setResourceAdapterProperty(
            final String resAdapterName, final String attrName, 
            final Object value) {
        
        testIfServerInDebug();
        
        return ControllerUtil.setAttributeValue(
            getResourceAdapterByName(resAdapterName), attrName, value,
                getMBeanServerConnection());     
    }
    
    
    /**
     *
     *
     */
    public ResourceAdapter getResourceAdapterByName(final String name) {
        
        testIfServerInDebug();
        
        return (ResourceAdapter) connectorModule.getContainee(
            NodeTypes.getAMXJ2EETypeByNodeType(
                NodeTypes.RESOURCE_ADAPTER), name);
    }
    
    
    /**
     *
     *
     *
     */
    public boolean isEnabled() {
        testIfServerInDebug();
        boolean configEnabled = ((Enabled)connectorConfig).getEnabled();
        return ControllerUtil.calculateIsEnabled(connectorConfig, configEnabled);
    }
    
    
    /**
     *
     *
     *
     */
    public void setEnabled(boolean enabled) {
        testIfServerInDebug();
        ControllerUtil.getDeployedItemRefConfig(connectorConfig).setEnabled(enabled);
    }
    
    public J2EEDeployedObject getJ2EEObject() {
        return connectorModule;
    }
    
}
