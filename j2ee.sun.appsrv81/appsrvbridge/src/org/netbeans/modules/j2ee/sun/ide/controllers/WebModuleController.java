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
import com.sun.appserv.management.client.AppserverConnectionSource;
import com.sun.appserv.management.config.DeployedItemRefConfig;
import com.sun.appserv.management.config.Enabled;
import com.sun.appserv.management.config.WebModuleConfig;
import com.sun.appserv.management.j2ee.J2EEDeployedObject;
import com.sun.appserv.management.j2ee.WebModule;
import com.sun.appserv.management.j2ee.Servlet;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.sun.bridge.apis.AppserverMgmtControllerBase;

import org.netbeans.modules.j2ee.sun.util.NodeTypes;


/**
 * Used as a conduit between the Netbeans API's and the AMX MBean API
 * data model. This API contains convenient methods for retrieving
 * components allowing the netbeans module heirarchy remain agnostic to the
 * underlying data model. 
 * 
 * This controller is used to navigate through deployed web modules.
 */
public class WebModuleController extends AppserverMgmtControllerBase 
        implements DeployedItemsController, EnablerController {
    
    private WebModule webModule;
    private WebModuleConfig webConfig;
    
    
    /**
     * Create an instance of J2EEServerMgmtController used in the interaction
     * with AMX API for Sun Java System Application Server. 
     * 
     * @param server The AMX J2EEServer object representing a particular server.
     */
    public WebModuleController(WebModule webModule, 
            AppserverConnectionSource connection) {
        super(webModule, connection);
        this.webModule = webModule;
    }
    
    
    /**
     * Create an instance of J2EEServerMgmtController used in the interaction
     * with AMX API for Sun Java System Application Server. 
     * 
     * @param server The AMX J2EEServer object representing a particular server.
     */
    public WebModuleController(final WebModule webModule, 
            final DeploymentManager dplmtMgr,
            final AppserverConnectionSource connection) {
        super(webModule, dplmtMgr, connection);
        this.webModule = webModule;
    }

    public WebModuleController(final WebModule webModule, 
            final WebModuleConfig webConfig,
            final DeploymentManager dplmtMgr,
            final AppserverConnectionSource connection) {
        super(webConfig, dplmtMgr, connection);
        this.webModule = webModule;
        this.webConfig = webConfig;
    }
    
    /**
     * Returns the name that the node will use to display.
     *
     * @return The name used to display in the node hierarchy.
     */
    public String getDisplayName() {
        if(webConfig != null)
            return webConfig.getName();
        else
            return webModule.getPath();
    }
    
    /**
     * Returns the properties of the application given the name.
     * 
     * @param propsToIgnore The String properties to ignore.
     * @return All the application properties.
     */
    public Map getProperties(List propsToIgnore) { 
        return getJ2EEAndConfigProperties(NodeTypes.WEB_APPLICATION, this.webModule,
            this.webConfig, propsToIgnore);
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
        
        return ControllerUtil.setAttributeValue(webModule, webConfig, attrName, value, 
            getMBeanServerConnection());
    }

    /**
     *
     *
     */
    private Servlet getServletByName(String servletName) {
        
        testIfServerInDebug();
        
        return (Servlet) (getServletsAsMap().get(servletName));
    }
    
    
    /**
     *
     *
     */
    private Map getServletsAsMap() {
        
        testIfServerInDebug();
        
        return webModule.getContaineeMap(
            NodeTypes.getAMXJ2EETypeByNodeType(NodeTypes.SERVLET));
    }
    
    
    /**
     *
     *
     */
    public Map getServletProperties(final String servletName,
            final List propsToIgnore) {
        return getPropertiesFromBackend(NodeTypes.SERVLET, 
            getServletByName(servletName), propsToIgnore);
    }
    
    
    /**
     *
     */
    public javax.management.Attribute setServletProperties(final String servletName, 
            final String attrName, final Object value) {
        
        testIfServerInDebug();
        
        return ControllerUtil.setAttributeValue(
            getServletByName(servletName), attrName, value,
                getMBeanServerConnection());     
    }
    
    
    /**
     *
     *
     */
    public String[] getServlets() {
        testIfServerInDebug();
        return ControllerUtil.getComponentNamesFromMap(getServletsAsMap());
    }
    
    
    
    /**
     *
     *
     *
     */
    public boolean isEnabled() {
        testIfServerInDebug();
        boolean configEnabled = ((Enabled)webConfig).getEnabled();
        return ControllerUtil.calculateIsEnabled(webConfig, configEnabled);
    }
    
    
    /**
     *
     *
     *
     */
    public void setEnabled(boolean enabled) {
        testIfServerInDebug();
        DeployedItemRefConfig config = ControllerUtil.getDeployedItemRefConfig(webConfig);
        if(config != null)
            config.setEnabled(enabled);
    }
 
    public J2EEDeployedObject getJ2EEObject() {
        return webModule;
    }
}
