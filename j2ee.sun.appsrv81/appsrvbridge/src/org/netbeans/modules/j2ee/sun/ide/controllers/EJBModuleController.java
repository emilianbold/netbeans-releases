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

import java.util.Map;
import java.util.List;

import javax.management.Attribute;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.client.AppserverConnectionSource;
import com.sun.appserv.management.config.DeployedItemRefConfig;
import com.sun.appserv.management.config.EJBModuleConfig;
import com.sun.appserv.management.config.Enabled;
import com.sun.appserv.management.j2ee.EJBModule;
import com.sun.appserv.management.j2ee.J2EEDeployedObject;
import com.sun.appserv.management.j2ee.J2EETypes;
import java.util.Set;

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
public class EJBModuleController extends AppserverMgmtControllerBase 
        implements DeployedItemsController, EnablerController {
    
    private EJBModule ejbModule;
    private EJBModuleConfig ejbConfig;
    
    /**
     * Create an instance of J2EEServerMgmtController used in the interaction
     * with AMX API for Sun Java System Application Server. 
     * 
     * @param server The AMX J2EEServer object representing a particular server.
     */
    public EJBModuleController(EJBModule ejbModule, 
            AppserverConnectionSource connection) {
        super(ejbModule, connection);
        this.ejbModule = ejbModule;
    }
    
    
    /**
     * Create an instance of J2EEServerMgmtController used in the interaction
     * with AMX API for Sun Java System Application Server. 
     * 
     * @param server The AMX J2EEServer object representing a particular server.
     */
    public EJBModuleController(final EJBModule ejbModule, 
            final DeploymentManager dplmtMgr,
            final AppserverConnectionSource connection) {
        super(ejbModule, dplmtMgr, connection);
        this.ejbModule = ejbModule;
    }

    public EJBModuleController(final EJBModule ejbModule, 
            final EJBModuleConfig ejbConfig,
            final DeploymentManager dplmtMgr,
            final AppserverConnectionSource connection) {
        super(ejbConfig, dplmtMgr, connection);
        this.ejbModule = ejbModule;
        this.ejbConfig = ejbConfig;
    }
    
    /**
     * Returns the properties of the application given the name.
     * 
     * @param propsToIgnore The String Properties to ignore.
     * @return All the application properties.
     */
    public Map getProperties(List propsToIgnore) {  
        return getJ2EEAndConfigProperties(NodeTypes.EJB_MODULE, this.ejbModule,
                this.ejbConfig, propsToIgnore);
    }
    
    
    /**
     * Returns the properties of the application given the name.
     * 
     * @param appName The name of the attribute.
     * @param value The value of the attribute to set. 
     *
     * @return updated Attribute
     */
    public Attribute setProperty(final String attrName, final Object value) {
        
        testIfServerInDebug();
        
        return ControllerUtil.setAttributeValue(ejbModule, ejbConfig, attrName, value, 
            getMBeanServerConnection());
    }
    
    /**
     *
     *
     */
    public String[] getStatelessSessionBeans() {
        return getEjbsByType(NodeTypes.STATELESS_SESSION_BEAN);
    }
    
    
    /**
     *
     */
    public String[] getStatefulSessionBeans() {
        return getEjbsByType(NodeTypes.STATEFUL_SESSION_BEAN);
    }
    
    
    /**
     *
     */
    public String[] getMessageDrivenBeans() {
        return getEjbsByType(NodeTypes.MESSAGE_DRIVEN_BEAN);
    }
    
    
    /**
     *
     *
     */
    public String[] getEntityBeans() {
        return getEjbsByType(NodeTypes.ENTITY_BEAN);
    }

    
    /**
     *
     */
    public Map getStatelessEJBProperties(final String ejbName, 
            final List propsToIgnore) {
        return getPropertiesFromBackend(NodeTypes.STATELESS_SESSION_BEAN, 
            getEjb(NodeTypes.STATELESS_SESSION_BEAN, ejbName), propsToIgnore);      
    }
    
    /**
     *
     */
    public Attribute setStatelessEJBProperties(final String ejbName, 
            final String attrName, final Object value) {
        
        testIfServerInDebug();
        
        return ControllerUtil.setAttributeValue(
            getEjb(NodeTypes.STATELESS_SESSION_BEAN, ejbName), attrName, value,
                getMBeanServerConnection());    
    }
    
    
    /**
     *
     */
    public Map getStatefulEJBProperties(final String ejbName,
            final List propsToIgnore) {
        return getPropertiesFromBackend(NodeTypes.STATEFUL_SESSION_BEAN, 
            getEjb(NodeTypes.STATEFUL_SESSION_BEAN, ejbName), propsToIgnore);     
    }
    
    /**
     *
     */
    public Attribute setStatefulEJBProperties(final String ejbName, 
            final String attrName, final Object value) {
        
        testIfServerInDebug();
        
        return ControllerUtil.setAttributeValue(
            getEjb(NodeTypes.STATEFUL_SESSION_BEAN, ejbName), attrName, value,
                getMBeanServerConnection());      
    }
    
    /**
     *
     */
    public Map getEntityEJBProperties(final String ejbName,
            final List propsToIgnore) {
        return getPropertiesFromBackend(NodeTypes.ENTITY_BEAN, 
            getEjb(NodeTypes.ENTITY_BEAN, ejbName), propsToIgnore);      
    }
    
    /**
     *
     */
    public Attribute setEntityEJBProperties(final String ejbName, 
            final String attrName, final Object value) {
        
        testIfServerInDebugAndLogException();
        
        return ControllerUtil.setAttributeValue(
            getEjb(NodeTypes.ENTITY_BEAN, ejbName), attrName, value,
                getMBeanServerConnection());
    }
    
    /**
     *
     */
    public Map getMessageDrivenEJBProperties(final String ejbName,
            final List propsToIgnore) {
        return getPropertiesFromBackend(NodeTypes.MESSAGE_DRIVEN_BEAN, 
            getEjb(NodeTypes.MESSAGE_DRIVEN_BEAN, ejbName), propsToIgnore);      
    }
    
    /**
     *
     */
    public Attribute setMessageDrivenEJBProperties(final String ejbName, 
            final String attrName, final Object value) {
        
        testIfServerInDebug();
        
        return ControllerUtil.setAttributeValue(
            getEjb(NodeTypes.ENTITY_BEAN, ejbName), attrName, value,
                getMBeanServerConnection());     
    }
    
    /**
     *
     */
    public AMX getEjb(final String nodeType, final String ejbName) {
        testIfServerInDebug();
        return ejbModule.getContainee(
            NodeTypes.getAMXJ2EETypeByNodeType(nodeType), ejbName);
    }
    

    /**
     *
     */
    public String[] getEjbsByType(String nodeType) {
        testIfServerInDebug();
        Set ejbs = ejbModule.getContaineeSet(NodeTypes.getAMXJ2EETypeByNodeType(nodeType));
        String[] components = ControllerUtil.getComponentNamesFromSet(ejbs);
        return components;
    }

    
    /**
     *
     *
     *
     */
    public boolean isEnabled() {
        testIfServerInDebug();
        boolean configEnabled = ((Enabled)ejbConfig).getEnabled();
        return ControllerUtil.calculateIsEnabled(ejbConfig, configEnabled);
    }
    
    
    /**
     *
     *
     *
     */
    public void setEnabled(boolean enabled) {
        testIfServerInDebug();
        DeployedItemRefConfig config = ControllerUtil.getDeployedItemRefConfig(ejbConfig);
        if(config != null)
            config.setEnabled(enabled);
    }
    
    public J2EEDeployedObject getJ2EEObject() {
        return ejbModule;
    }
}
