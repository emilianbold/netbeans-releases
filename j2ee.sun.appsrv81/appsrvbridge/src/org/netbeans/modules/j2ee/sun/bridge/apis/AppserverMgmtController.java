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
package org.netbeans.modules.j2ee.sun.bridge.apis;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.management.Attribute;
import javax.management.ObjectName;
import org.netbeans.modules.j2ee.sun.ide.controllers.*;

import org.netbeans.modules.j2ee.sun.util.NodeTypes;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.client.AppserverConnectionSource;
import com.sun.appserv.management.config.AdminObjectResourceConfig;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.ConnectorConnectionPoolConfig;
import com.sun.appserv.management.config.ConnectorResourceConfig;
import com.sun.appserv.management.config.CustomResourceConfig;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.J2EEApplicationConfig;
import com.sun.appserv.management.config.JDBCConnectionPoolConfig;
import com.sun.appserv.management.config.JDBCResourceConfig;
import com.sun.appserv.management.config.JNDIResourceConfig;
import com.sun.appserv.management.config.JavaConfig;
import com.sun.appserv.management.config.LogServiceConfig;
import com.sun.appserv.management.config.MailResourceConfig;
import com.sun.appserv.management.config.ModuleLogLevelsConfig;
import com.sun.appserv.management.config.PersistenceManagerFactoryResourceConfig;
import com.sun.appserv.management.config.ResourceConfig;
import com.sun.appserv.management.config.StandaloneServerConfig;
import com.sun.appserv.management.config.WebModuleConfig;
import com.sun.appserv.management.j2ee.J2EEServer;
import java.util.Arrays;



/**
 * Used as a conduit between the Netbeans API's and the AMX MBean API
 * data model. This API contains convienent methods for retrieving
 * components allowing the netbeans module heirarchy remain agnostic to the
 * underlying data model. 
 */
public class AppserverMgmtController extends AppserverMgmtControllerBase {


    /**
     * Create an instance of AppserverMgmtController used in the interaction
     * with AMX API for Sun Java System Application Server. 
     *
     * @param deployMgr The DeploymentManager object responsible for creating
     *        the connection source.
     * @param connection An AppserverConnectionSource object used to connect
     *                   to the appserver. 
     */
    public AppserverMgmtController(final DeploymentManager deployMgr,
            final AppserverConnectionSource connection) {
        super(deployMgr, connection);
    }

       
    /**
     * Return the J2EEServerMgmtController responsible for handling all
     * operations on AMX apis involving server specific configuration and
     * management. Currently this returns the DAS server instance. 
     *
     * @returns The J2EEServerMgmtController controller for the DAS server 
     *          instance.
     */
    public J2EEServerMgmtController getJ2EEServerMgmtController() {
        return getJ2EEServerMgmtController(DAS_SERVER_NAME);
    }


    /**
     * Return the J2EEServerMgmtController responsible for handling all
     * operations on AMX apis involving server specific configuration and
     * management. 
     *
     * @param serverName The name of the server instance.
     * @returns The J2EEServerMgmtController controller for the server instance
     *          specified by name.
     */
    public J2EEServerMgmtController getJ2EEServerMgmtController(
            final String serverName) {
        Map serverMap = getJ2EEDomain().getServerMap();
        J2EEServer j2eeServer = 
            (J2EEServer) ControllerUtil.getAMXComponentFromMap(
                serverMap, serverName);
        return new J2EEServerMgmtController(j2eeServer, getDeploymentManager(),
                appMgmtConnection);
    }
    
   
    /**
     * Returns the names of the JDBC Resources.
     */
    public String[] getJDBCResources() {
       return getComponentNamesFromMap(getDomainConfig().
            getJDBCResourceConfigMap());
    }
    
    
    /**
     * Returns the names of the JDBC Connection Pools.
     */
    public String[] getJDBCConnectionPools() {
       return getComponentNamesFromMap(getDomainConfig().
            getJDBCConnectionPoolConfigMap());
    }
    
    
    /**
     * Returns the names of the JMS Connection Factories.
     */
    public String[] getJMSConnectionFactories() {
       return getConnectorResources();
    }
    
    
    /**
     * Returns the names of the JDBC Connection Pools.
     */
    public String[] getDestinationResources() {
       return getAdminObjectResources();
    }
    
    
    /**
     * Returns the names of the JDBC Connection Pools.
     */
    public String[] getJavaMailSessionResources() {
       return getComponentNamesFromMap(
        getDomainConfig().getMailResourceConfigMap());
    }
    
    /**
     * Returns the names of the Connector Resources.
     */
    public String[] getConnectorResources() {
       return getComponentNamesFromMap(
        getDomainConfig().getConnectorResourceConfigMap());
    }
    
    /**
     * Returns the names of the Custom Resources.
     */
    public String[] getCustomResources() {
       return getComponentNamesFromMap(
        getDomainConfig().getCustomResourceConfigMap());
    }
    
    /**
     * Returns the names of the External Resources.
     */
    public String[] getExternalResources() {
       return getComponentNamesFromMap(
        getDomainConfig().getJNDIResourceConfigMap());
    }
    
    /**
     * Returns the names of the Connector Connection Pools.
     */
    public String[] getConnectorConnectionPools() {
        return getComponentNamesFromMap(
            getDomainConfig().getConnectorConnectionPoolConfigMap());
    }
    
    /**
     * Returns the names of the Admin Object Resources.
     */
    public String[] getAdminObjectResources() {
       return getComponentNamesFromMap(
        getDomainConfig().getAdminObjectResourceConfigMap());
    }
    
    
    /**
     * Returns the names of the Persistence Manager Factory Resources.
     */
    public String[] getPersistenceManagerFactoryResources() {
       return getComponentNamesFromMap(
        getDomainConfig().getPersistenceManagerFactoryResourceConfigMap());
    }
    
    
    /**
     * Returns the names of the Web Modules.
     */
    public String[] getWebModules() {
       return getComponentNamesFromMap(
        getDomainConfig().getWebModuleConfigMap());
    }
    
    
    /**
     * Returns the names of the Enterprise Applications.
     */
    public String[] getEnterpriseApplications() {
       return getComponentNamesFromMap(
            getDomainConfig().getJ2EEApplicationConfigMap());
    }
    
    
    /**
     * Returns the properties jdbc resource given the jndi name.
     * 
     * @param The jndi name of the JDBC resource.
     * @return A Map of all the JDBC properties given the jndi name.
     */
    public java.util.Map getJDBCResourceProperties(
            final String jdbcResourceName,
            final List propsToIgnore) {
        JDBCResourceConfig res = getJDBCResourceConfigByName(jdbcResourceName);
        return getPropertiesFromBackend(NodeTypes.JDBC_RESOURCE, res, 
                propsToIgnore);
    }
    
    
    /**
     * Returns the properties of a jdbc connection pool given the jndi name.
     * 
     * @param jdbcConnPoolName The jndi name of the JDBC Connection Pool.
     * @return A Map of all the JDBC Connection Pool properties given the jndi name.
     */
    public java.util.Map getJDBCConnectionPoolProperties(
            final String jdbcConnPoolName,
            final List propsToIgnore) {
        JDBCConnectionPoolConfig res = 
            getJDBCConnectionPoolConfigByName(jdbcConnPoolName);
        return getPropertiesFromBackend(NodeTypes.CONNECTION_POOL, res, 
                propsToIgnore);
    }
    
    
    /**
     * Returns all of the properties of a jvm.
     *
     * @param serverName The name of the server from which JVM properties are
     *        requested for properties.
     * @return A Map of all the JVM properties for this server instance.
     */
    public java.util.Map getJVMProperties(final String serverName,
            final List propsToIgnore) {
        JavaConfig javaConfig = getReferencedConfig(serverName).getJavaConfig();
        return getPropertiesFromBackend(NodeTypes.JVM, javaConfig, 
                propsToIgnore);
    }
    
    /**
     * Returns all of the properties of a pm resource.
     *
     * @param pmResourceName The name of the pm resource.
     * @return A Map of all the Pm resource properties for this server instance.
     */
    public java.util.Map getPersistenceManagerFactoryProperties(
            final String pmResourceName,
            final List propsToIgnore) {
        PersistenceManagerFactoryResourceConfig res = 
            getPersistenceManagerResourceFactoryConfigByName(pmResourceName);
        return getPropertiesFromBackend(NodeTypes.PM_RESOURCE, res, 
                propsToIgnore);
    }
    
    /**
     * Returns the properties of a jmx connection factory given the jndi name.
     * 
     * @param jmsConnFactoryName The jndi name of the JMS Resource.
     * @return All the JMS Connection Factory properties given the jndi 
     *         name.
     */  
    public java.util.Map getJMSConnectionFactoryProperties(
            final String jmsConnFactoryName,
            final List propsToIgnore) {
        ConnectorResourceConfig res = 
                getConnectorResourceConfigByName(jmsConnFactoryName);
        String connectorPoolName = res.getPoolName();
        Map poolProps = getConnectorConnectionPoolProperties(connectorPoolName, 
                NodeTypes.getNodeProperties(NodeTypes.CONNECTION_FACTORY_POOL));
        Map factoryProps = getPropertiesFromBackend(NodeTypes.CONNECTION_FACTORY, res,
                propsToIgnore);      
        factoryProps.putAll(poolProps);
        return factoryProps;
    }
    
    
    /**
     * Returns the properties of a destination resource given the jndi name.
     * 
     * @param destResName The jndi name of the destination resource.
     * @return All of the JMS destination resource properties given the jndi 
     *         name.
     */  
    public java.util.Map getDestinationResourceProperties(
            final String destResName,
            final List propsToIgnore) {
        AdminObjectResourceConfig res = 
                getAdminObjectResourceConfigByName(destResName);
        return getPropertiesFromBackend(NodeTypes.DESTINATION_RESOURCE, res,
                propsToIgnore);      
    }
    
    
    /**
     * Returns the properties JavaMail Session resource given the jndi name.
     * 
     * @param The jndi name of the JavaMail Session resource.
     * @return All the JavaMail properties given the jndi name.
     */
    public java.util.Map getJavaMailSessionResourceProperties(
            final String mailResourceName,
            final List propsToIgnore) {
        MailResourceConfig res = getMailResourceConfigByName(mailResourceName);
        return getPropertiesFromBackend(NodeTypes.MAIL_RESOURCE, res,
                propsToIgnore);      
    }
    
    
    /**
     * Returns the properties of a connector connection pool given the name.
     * 
     * @param The name of the Connector Connection Pool resource.
     * @return All the Connector Connection Pool properties given the name.
     */
    public java.util.Map getConnectorConnectionPoolProperties(
            final String poolName,
            final List propsToIgnore) {
        ConnectorConnectionPoolConfig res = 
                getConnectorConnectionPoolConfigByName(poolName);
        return getPropertiesFromBackend(NodeTypes.CONNECTOR_CONNECTION_POOL, 
                res, propsToIgnore);      
    }
    
    
    /**
     * Returns the properties of an admin object resource given the name.
     * 
     * @param resName The name of the Admin Object Resource.
     * @return All the admin object resource properties given the name.
     */
    public java.util.Map getAdminObjectResourceProperties(
            final String resName,
            final List propsToIgnore) {
        AdminObjectResourceConfig res = 
                getAdminObjectResourceConfigByName(resName);
        return getPropertiesFromBackend(NodeTypes.ADMIN_OBJECT_RESOURCE, res,
                propsToIgnore);      
    }
  
    
    /**
     * Returns the properties of a connector resource given the name.
     * 
     * @param The name of the Connector Resource.
     * @return All the Connector Resource properties given the name.
     */
    public java.util.Map getConnectorResourceProperties(
            final String resName,
            final List propsToIgnore) {
        ConnectorResourceConfig res = 
                getConnectorResourceConfigByName(resName);
        return getPropertiesFromBackend(NodeTypes.CONNECTOR_RESOURCE, res,
                propsToIgnore);      
    }
    
    
    /**
     * Returns the properties of a custom resource given the name.
     * 
     * @param The name of the Custom Resource.
     * @return All the Custom Resource properties given the name.
     */
    public java.util.Map getCustomResourceProperties(
            final String resName,
            final List propsToIgnore) {
        CustomResourceConfig res = 
                getCustomResourceConfigByName(resName);
        return getPropertiesFromBackend(NodeTypes.CUSTOM_RESOURCE, res,
                propsToIgnore);      
    }

    /**
     * Returns the properties of an external resource module given the name.
     * 
     * @param The name of the External Resource.
     * @return All the External Resource properties given the name.
     */
    public java.util.Map getExternalResourceProperties(
            final String moduleName,
            final List propsToIgnore) {
        JNDIResourceConfig res = 
                getJNDIResourceConfigByName(moduleName);
        return getPropertiesFromBackend(NodeTypes.EXTERNAL_RESOURCE, res,
                propsToIgnore);      
    }
    
    
    /**
     *
     *
     */
    public Attribute setExternalResourceProperty(String resName, String attributeName,
            Object value) {
        
        testIfServerInDebug();
        
        JNDIResourceConfig res = 
                getJNDIResourceConfigByName(resName);
        Attribute attr = ControllerUtil.setAttributeValue(res, attributeName, value, 
            getMBeanServerConnection());
        return attr;
    }
 
    
    /**
     *
     *
     */
    public String[] getClusters() {
       return getComponentNamesFromMap(getClustersMap());
    }
   
    
    StandaloneServerConfig getStandaloneServerConfigByName(
            String serverName) {
        java.util.Map map = getStandaloneServerInstancesMap();
        return (StandaloneServerConfig)map.get(serverName);
    }
    
    
    /**
     *
     */
    JDBCResourceConfig getJDBCResourceConfigByName(
            String jdbcResourceName) {
        java.util.Map map = getDomainConfig().getJDBCResourceConfigMap();
        return (JDBCResourceConfig)map.get(jdbcResourceName);
    }
    
    
    /**
     *
     */
    MailResourceConfig getMailResourceConfigByName(
            String mailResourceName) {
        java.util.Map map = getDomainConfig().getMailResourceConfigMap();
        return (MailResourceConfig)map.get(mailResourceName);
    }
    
    
    /**
     *
     */
    JNDIResourceConfig getJNDIResourceConfigByName(
            String resName) {
        java.util.Map map = getDomainConfig().getJNDIResourceConfigMap();
        return (JNDIResourceConfig)map.get(resName);
    }
    
    /**
     *
     */
    JDBCConnectionPoolConfig getJDBCConnectionPoolConfigByName(
            String jdbcConnPoolName) {
        java.util.Map map = getDomainConfig().getJDBCConnectionPoolConfigMap();
        return (JDBCConnectionPoolConfig)map.get(jdbcConnPoolName);
    }
    
    /**
     *
     */
//    JMSResourceConfig getJMSResourceConfigByName(
//            String jmsResourceName) {
//        java.util.Map map = getDomainConfig().getJMSResourceConfigMap();
//        return (JMSResourceConfig)map.get(jmsResourceName);
//    }
    
    /**
     *
     */
    ConnectorResourceConfig getConnectorResourceConfigByName(
            String connResourceName) {
        java.util.Map map = 
            getDomainConfig().getConnectorResourceConfigMap();
        return (ConnectorResourceConfig)map.get(connResourceName);
    }
    
    /**
     *
     */
    ConnectorConnectionPoolConfig getConnectorConnectionPoolConfigByName(
            String connPoolName) {
        java.util.Map map = 
            getDomainConfig().getConnectorConnectionPoolConfigMap();
        return (ConnectorConnectionPoolConfig)map.get(connPoolName);
    }
    
    /**
     *
     */
    CustomResourceConfig getCustomResourceConfigByName(String resName) {
        java.util.Map map = 
            getDomainConfig().getCustomResourceConfigMap();
        return (CustomResourceConfig)map.get(resName);
    }
    
    /**
     *
     */
    AdminObjectResourceConfig getAdminObjectResourceConfigByName(
            String adminObjName) {
        java.util.Map map = getDomainConfig().getAdminObjectResourceConfigMap();
        return (AdminObjectResourceConfig)map.get(adminObjName);
    }
    
    /**
     *
     */
    PersistenceManagerFactoryResourceConfig 
            getPersistenceManagerResourceFactoryConfigByName(
                String pmResourceName) {
        java.util.Map map = 
            getDomainConfig().getPersistenceManagerFactoryResourceConfigMap();
        return (PersistenceManagerFactoryResourceConfig)map.get(pmResourceName);
    }
    
    
    /**
     *
     */
    WebModuleConfig getWebModuleConfigByName(String webModuleName) {
        java.util.Map map = getDomainConfig().getWebModuleConfigMap();
        return (WebModuleConfig)map.get(webModuleName);
    }
    
    
    /**
     *
     */
    J2EEApplicationConfig getJ2EEApplicationConfigByName(String appName) {
        java.util.Map map = 
            getDomainConfig().getJ2EEApplicationConfigMap();
        return (J2EEApplicationConfig)map.get(appName);
    }
    
    /**
     *
     *
     */
    private String[] getComponentNamesFromMap(java.util.Map map) {
       return ControllerUtil.getComponentNamesFromMap(map);
    }
    
    /**
     *
     *
     */
    java.util.Map getClustersMap() {
       return getDomainConfig().getClusterConfigMap();
    }
    
    /**
     *
     *
     */
    java.util.Map getStandaloneServerInstancesMap() {
       return getDomainConfig().getStandaloneServerConfigMap();
    }
    

    /**
     *
     */
    public ConfigConfig getReferencedConfig(String serverName) {
        StandaloneServerConfig serverConfig = 
            getStandaloneServerConfigByName(serverName);
        DomainConfig config = (DomainConfig)serverConfig.getContainer();
        AMX component =
            ControllerUtil.getAMXComponentFromMap(config.getConfigConfigMap(), 
                serverConfig.getReferencedConfigName());
        return (ConfigConfig)component;
    }

    
    /**
     *
     *
     */
    public Attribute setJDBCResourceProperty(String jdbcName, String attributeName,
            Object value) {
        
        testIfServerInDebug();
        JDBCResourceConfig res = getJDBCResourceConfigByName(jdbcName);
        Attribute attr = ControllerUtil.setAttributeValue(res, attributeName, value,
                    getMBeanServerConnection());
        return attr;
    }
    
    /**
     *
     *
     */
    public Attribute setJDBCConnectionPoolProperty(String resName, 
            String attributeName, Object value) {
        
        testIfServerInDebug();
        
        Attribute attr = ControllerUtil.setAttributeValue(
            getJDBCConnectionPoolConfigByName(resName), attributeName, value, 
                getMBeanServerConnection());
        return attr;
    }
    
    
    /**
     *
     *
     */
    public Attribute setJMSResourceProperty(String resName, 
            String attributeName, Object value) {
        
        testIfServerInDebug();
        
        Attribute attr = ControllerUtil.setAttributeValue(
            getConnectorResourceConfigByName(resName), attributeName, value, 
                getMBeanServerConnection());
        return attr;
    }
    
    /**
     *
     *
     */
    public Attribute setDestinationResourceProperty(String resName, 
            String attributeName, Object value) {
        
        testIfServerInDebug();
        
        Attribute attr = ControllerUtil.setAttributeValue(
            getAdminObjectResourceConfigByName(resName), attributeName, value, 
                getMBeanServerConnection());
        return attr;
    }
    
    
    /**
     *
     *
     */
    public Attribute setJavaMailSessionResourceProperty(String resName, 
            String attributeName, Object value) {
        
        testIfServerInDebug();
        
        Attribute attr = ControllerUtil.setAttributeValue(
            this.getMailResourceConfigByName(resName), attributeName, value, 
                getMBeanServerConnection());
        return attr;
    }
    
    
    /**
     *
     *
     */
    public Attribute setPersistenceManagerResourceFactoryProperty(String resName, 
            String attributeName, Object value) {
        
        testIfServerInDebug();
        
        Attribute attr = ControllerUtil.setAttributeValue(
            getPersistenceManagerResourceFactoryConfigByName(resName), 
                attributeName, value, getMBeanServerConnection());
        return attr;
    }
    
    /**
     *
     *
     */
    public Attribute setConnectorResourceProperty(String resName, 
            String attributeName, Object value) {
        Attribute attr = ControllerUtil.setAttributeValue(
            getConnectorResourceConfigByName(resName), attributeName, 
                value, getMBeanServerConnection());
        return attr;
    }
    
    
    /**
     *
     *
     */
    public Attribute setConnectorConnectionPoolProperty(String resName, 
            String attributeName, Object value) {
        
        testIfServerInDebug();
        
        Attribute attr = ControllerUtil.setAttributeValue(
            getConnectorConnectionPoolConfigByName(resName), attributeName, 
                value, getMBeanServerConnection());
        return attr;
    }
    
    
    /**
     *
     *
     */
    public Attribute setAdminObjectResourceProperty(String resName, 
            String attributeName, Object value) {
        
        testIfServerInDebug();
        
        Attribute attr = ControllerUtil.setAttributeValue(
            getAdminObjectResourceConfigByName(resName), attributeName, 
                value, getMBeanServerConnection());
        return attr;
    }
    
   /**
     *
     *
     */
    public Attribute setJVMProperty(String serverName, String attributeName, 
            Object value) {
        
        testIfServerInDebug();
        
        Attribute attr = ControllerUtil.setAttributeValue(
            getReferencedConfig(serverName).getJavaConfig(), attributeName, 
                value, getMBeanServerConnection());
        return attr;
    }
    
    
    /**
     *
     *
     */
    public Attribute setJMSConnectionFactoryProperty(String resName, 
            String attributeName, Object value) {
        
        testIfServerInDebug();
        
        ConnectorResourceConfig connFactory = getConnectorResourceConfigByName(resName);
        AMX configObject = connFactory;
        if(! connFactory.getProperties().containsKey(attributeName)){
            String poolName = connFactory.getPoolName();
            configObject = getConnectorConnectionPoolConfigByName(poolName);
        }
        Attribute attr = ControllerUtil.setAttributeValue(
            configObject, attributeName, value, getMBeanServerConnection());
        
        return attr;
    }
    
    /**
     *
     *
     */
    public Attribute setCustomResourceProperty(String resName, 
            String attributeName, Object value) {
        
        testIfServerInDebug();
        
        Attribute attr = ControllerUtil.setAttributeValue(
            getCustomResourceConfigByName(resName), attributeName, value, 
                getMBeanServerConnection());
        return attr;
    }
    
    
    /**
     * Deletes an admin object resource.
     *
     * @param resName The name of the resource.
     */
    public void deleteAdminObjectResource(final String resName) {
        AdminObjectResourceConfig res = 
                getAdminObjectResourceConfigByName(resName);
        if(isEightPlatform()){
            deleteResourceFromBackend_WorkaroundForPE(
                    NodeTypes.ADMIN_OBJECT_RESOURCE, resName);
        }else{
            removeResourceRef(res, resName);
            getDomainConfig().removeAdminObjectResourceConfig(resName);
        }    
        
    }
    
    
    /**
     * Deletes an external jndi resource.
     *
     * @param resName The name of the resource.
     */
    public void deleteExternalResource(final String resName) {
        JNDIResourceConfig res = 
                getJNDIResourceConfigByName(resName);
        if(isEightPlatform()){
            deleteResourceFromBackend_WorkaroundForPE(
                    NodeTypes.EXTERNAL_RESOURCE, resName);
        }else{
            removeResourceRef(res, resName);
            getDomainConfig().removeJNDIResourceConfig(resName);
        }    
    }
    
    
    /**
     * Delete a custom resource.
     *
     * @param resName The name of the resource.
     */
    public void deleteCustomResource(final String resName) {
        CustomResourceConfig res = 
                getCustomResourceConfigByName(resName);
        if(isEightPlatform()){
            deleteResourceFromBackend_WorkaroundForPE(
                    NodeTypes.CUSTOM_RESOURCE, resName);
        }else{
            removeResourceRef(res, resName);
            getDomainConfig().removeCustomResourceConfig(resName);
        }    
    }
    
    
    /**
     * Delete a jms connection factory resource.
     *
     * @param resName The name of the resource.
     */
    public void deleteJMSConnectionFactory(final String resName) {
        ConnectorResourceConfig res = 
                getConnectorResourceConfigByName(resName);
        if(isEightPlatform()){
            deleteResourceFromBackend_WorkaroundForPE(
                    NodeTypes.CONNECTION_FACTORY, resName);
        }else{
            removeResourceRef(res, resName);
            getDomainConfig().removeConnectorResourceConfig(resName);
        }    
    }
    
    
    /**
     * Delete a jdbc connection factory resource.
     *
     * @param resName The name of the resource.
     */
    public void deleteJDBCConnectionPool(final String resName) {
        JDBCConnectionPoolConfig res = 
            getJDBCConnectionPoolConfigByName(resName);
        if(isEightPlatform()){
            deleteResourceFromBackend_WorkaroundForPE(
                    NodeTypes.CONNECTION_POOL, resName);
        }else
            getDomainConfig().removeJDBCConnectionPoolConfig(resName);
    }
    
    
    /**
     * Delete a connector connection pool resource.
     *
     * @param resName The name of the resource.
     */
    public void deleteConnectorConnectionPool(final String resName) {
        ConnectorConnectionPoolConfig res = 
                getConnectorConnectionPoolConfigByName(resName);
        if(isEightPlatform()){
            deleteResourceFromBackend_WorkaroundForPE(
                    NodeTypes.CONNECTOR_CONNECTION_POOL, resName);
        }else
            getDomainConfig().removeConnectorConnectionPoolConfig(resName);
     }
    
    
    /**
     * Delete a connector resource.
     *
     * @param resName The name of the resource.
     */
    public void deleteConnectorResource(final String resName) {
        ConnectorResourceConfig res = 
                getConnectorResourceConfigByName(resName);
        if(isEightPlatform()){
            deleteResourceFromBackend_WorkaroundForPE(
                    NodeTypes.CONNECTOR_RESOURCE, resName);
        }else{
            removeResourceRef(res, resName);
            getDomainConfig().removeConnectorResourceConfig(resName);
        }    
    }
    
    
    /**
     * Delete a destination resource.
     *
     * @param resName The name of the resource.
     */
    public void deleteDestinationResource(final String resName) {
        AdminObjectResourceConfig res = 
                getAdminObjectResourceConfigByName(resName);
        if(isEightPlatform()){
            deleteResourceFromBackend_WorkaroundForPE(
                    NodeTypes.DESTINATION_RESOURCE, resName);
        }else{
            removeResourceRef(res, resName);
            getDomainConfig().removeAdminObjectResourceConfig(resName);
        }    
    }
    
    
    /**
     * Delete a jdbc resource.
     *
     * @param resName The name of the resource.
     */
    public void deleteJDBCResource(final String resName) {
        JDBCResourceConfig res = getJDBCResourceConfigByName(resName);
        if(isEightPlatform()){
            deleteResourceFromBackend_WorkaroundForPE(
                    NodeTypes.JDBC_RESOURCE, resName);
        }else{
            removeResourceRef(res, resName);
            getDomainConfig().removeJDBCResourceConfig(resName);       
        }    
    }
    
    
    /**
     * Delete a Java mail resource.
     *
     * @param resName The name of the resource.
     */
    public void deleteJavaMailSessionResource(final String resName) {
        MailResourceConfig res = getMailResourceConfigByName(resName);
        if(isEightPlatform()){
            deleteResourceFromBackend_WorkaroundForPE(
                    NodeTypes.MAIL_RESOURCE, resName);
        }else{
            removeResourceRef(res, resName);
            getDomainConfig().removeMailResourceConfig(resName);
        }    
    }
    
    
    /**
     * Delete a PM resource.
     *
     * @param resName The name of the resource.
     */
    public void deletePersistenceManagerResourceFactory(final String resName) {
        PersistenceManagerFactoryResourceConfig res = 
            getPersistenceManagerResourceFactoryConfigByName(resName);
        if(isEightPlatform()){
            deleteResourceFromBackend_WorkaroundForPE(
                    NodeTypes.PM_RESOURCE, resName);
        }else{
            removeResourceRef(res, resName);
            getDomainConfig().removePersistenceManagerFactoryResourceConfig(resName);
        }    
    }
    
    
    /**
     * Delete dependent resources.
     *
     * @param jdbcConnectionPoolName The name of the JDBC connection pool
     *        referenced by 1+ jdbc resources.
     */
    public void deleteDependentJDBCResources(
            final String jdbcConnectionPoolName) {
        String [] jdbcResourceNames = 
            getDependentJDBCResourceNames(jdbcConnectionPoolName);
        for(int i = 0; i < jdbcResourceNames.length; i++) {
            deleteJDBCResource(jdbcResourceNames[i]);
        }
    }
    
    
    /**
     * Retrieves all the jdbc resources that reference the specified 
     * jdbcConnectionPoolName.
     *
     * @param jdbcConnectionPoolName The name of the JDBC connection pool
     *        referenced by 1+ jdbc resources.
     * @return A String array of all the names of the dependent jdbc resources.
     */
    public String[] getDependentJDBCResourceNames(
            final String jdbcConnectionPoolName) {
        java.util.Map jdbcResMap = 
                getDomainConfig().getJDBCResourceConfigMap();
        java.util.Vector namesToReturn = new java.util.Vector();
        if(jdbcConnectionPoolName != null) {
            for(Iterator itr = jdbcResMap.values().iterator(); itr.hasNext(); ) {
                JDBCResourceConfig config = (JDBCResourceConfig) itr.next();
                if(jdbcConnectionPoolName.equals(config.getPoolName())) {
                    namesToReturn.add(config.getName());
                }
            }
        }
        final String[] names = new String[namesToReturn.size()];
        return (String[]) namesToReturn.toArray(names);
    }
    
    
    /**
     * NOTE: WORKAROUND!
     *
     * 
     */
    private void deleteResourceFromBackend_WorkaroundForPE( 
            final String nodeType, final String resName) {
        String domainName = BACKEND_COM_SUN_APPSERV_MBEAN_DOMAIN_NAME;
        String props = Util.makeProp("type", "resources");
        String props2 = Util.makeProp("category", "config");
        props = Util.concatenateProps(props, props2);
        String operationName = null;
        ObjectName resourcesMBeanObjName = 
                Util.newObjectName(domainName, props);
        operationName = NodeTypes.getDeleteResourceMethodName(nodeType);
        if(operationName != null) {
            try {
                getMBeanServerConnection().invoke(resourcesMBeanObjName, 
                    operationName, new Object[]{resName, DAS_SERVER_NAME}, 
                    new String[]{"java.lang.String", "java.lang.String"} );
            } catch(Exception e) {
                e.printStackTrace();
            }
        }                
    }

    
    /**
     *
     */
    public String getName() {
        return "";
    }
    
    /**
     * Returns the properties of the Domain Node given the name.
     * 
     * @param appName The name of the application.
     * @return All the application properties.
     */
    public java.util.Map getLogProperties(String serverName) {  
        LogServiceConfig logService = getReferencedConfig(serverName).getLogServiceConfig();
        ModuleLogLevelsConfig moduleLevels =
                logService.getModuleLogLevelsConfig();
        return getLogPropertiesFromBackend(moduleLevels, moduleLevels.getAllLevels());      
    }
    
    /**
     *
     *
     */
    public Attribute setLogProperties(String serverName, String attributeName, Object value) {
        testIfServerInDebug();
        LogServiceConfig logService = getReferencedConfig(serverName).getLogServiceConfig();
        ModuleLogLevelsConfig moduleLevels =
                logService.getModuleLogLevelsConfig();
        Attribute attr = ControllerUtil.setAttributeValue(
            moduleLevels, attributeName, value, getMBeanServerConnection());
        return attr;
    }
        
    /**
     *
     *
     */
    public void updateResourceExtraProperty(String resourceName, String resourceType, Object[] props, java.util.Map oldProps) {
        testIfServerInDebug();
        if(resourceType.equals(NodeTypes.CONNECTION_POOL)){
            JDBCConnectionPoolConfig cpConfig = getJDBCConnectionPoolConfigByName(resourceName);
            if(cpConfig != null){
                removeResourceExtraProperty(cpConfig, oldProps);
                ControllerUtil.setPropertyValue(cpConfig, props);
            }
        }else if(resourceType.equals(NodeTypes.CONNECTOR_CONNECTION_POOL)){
            ConnectorConnectionPoolConfig connConfig = getConnectorConnectionPoolConfigByName(resourceName);
            if(connConfig != null){
                removeResourceExtraProperty(connConfig, oldProps);
                ControllerUtil.setPropertyValue(connConfig, props);
            }
        }else if(resourceType.equals(NodeTypes.CONNECTION_FACTORY)){
            ConnectorResourceConfig factoryConfig = getConnectorResourceConfigByName(resourceName);
            ConnectorConnectionPoolConfig connConfig = getConnectorConnectionPoolConfigByName(factoryConfig.getPoolName());
            if(connConfig != null){
                removeResourceExtraProperty(connConfig, oldProps);
                ControllerUtil.setPropertyValue(connConfig, props);
            }
        }else{
            ResourceConfig res = getRelevantResourceConfig(resourceName, resourceType);
            if(res != null){
                removeResourceExtraProperty(res, oldProps);
                ControllerUtil.setPropertyValue(res, props);
            }
        }
    }
    
    /**
     *
     *
     */
    private ResourceConfig getRelevantResourceConfig(String resourceName, String resourceType) {
        ResourceConfig resConfig = null;
        if(resourceType.equals(NodeTypes.ADMIN_OBJECT_RESOURCE) || resourceType.equals(NodeTypes.DESTINATION_RESOURCE)){
            resConfig = getAdminObjectResourceConfigByName(resourceName);
        }else if(resourceType.equals(NodeTypes.CONNECTOR_RESOURCE)){
            resConfig = getConnectorResourceConfigByName(resourceName);
        }else if(resourceType.equals(NodeTypes.JDBC_RESOURCE)){
            resConfig = getJDBCResourceConfigByName(resourceName);
        }else if(resourceType.equals(NodeTypes.CUSTOM_RESOURCE)){
            resConfig = getCustomResourceConfigByName(resourceName);
        }else if(resourceType.equals(NodeTypes.EXTERNAL_RESOURCE)){
            resConfig = getJNDIResourceConfigByName(resourceName);
        }else if(resourceType.equals(NodeTypes.MAIL_RESOURCE)){
            resConfig = getMailResourceConfigByName(resourceName);
        }else if(resourceType.equals(NodeTypes.PM_RESOURCE)){
            resConfig = getPersistenceManagerResourceFactoryConfigByName(resourceName);
        }
        
        return resConfig;
    }
    
    /**
     *
     *
     */
    private static void removeResourceExtraProperty(ResourceConfig res, Map oldProps) {
        try{
            Iterator it = oldProps.keySet().iterator();
            while(it.hasNext()){
                res.removeProperty(it.next().toString());
            }
        }catch(Exception ex){
            return;
            //Suppress any exception
            //When properties have been updated outside the IDE and IDE 
            //is working with a stale copy.
        }
    }
    
    /**
     *
     *
     */
    private static void removeResourceExtraProperty(JDBCConnectionPoolConfig res, Map oldProps) {
        try{
            Iterator it = oldProps.keySet().iterator();
            while(it.hasNext()){
                res.removeProperty(it.next().toString());
            }
        }catch(Exception ex){
            return;
            //Suppress any exception
            //When properties have been updated outside the IDE and IDE
            //is working with a stale copy.
        }
    }
    
    /**
     *
     *
     */
    private static void removeResourceExtraProperty(ConnectorConnectionPoolConfig res, Map oldProps) {
        try{
            Iterator it = oldProps.keySet().iterator();
            while(it.hasNext()){
                res.removeProperty(it.next().toString());
            }
        }catch(Exception ex){
            //Suppress any exception
            //When properties have been updated outside the IDE and IDE
            //is working with a stale copy.
        }
    }
        
    /**
     * Obtains the location of a web module given its context root.
     * @param contextRoot Context Root of web module
     * @return Return the location of the web module with the specified context root
     */
    public String getWebModuleName(String contextRoot){
        J2EEServerMgmtController controller = getJ2EEServerMgmtController();
        String modname = null;
        if(controller != null)
            modname = controller.getWebModuleName(contextRoot);
        
        return modname;
    }
    
    /*
     * Get Server Type : PR or EE
     *
     * @return true if 8.x EE
     */
    public boolean isEightPlatform(){
        boolean isPlatform = true;
        J2EEServerMgmtController controller = getJ2EEServerMgmtController();
        String serverName = controller.getServerName();
        if((serverName != null) && (serverName.indexOf("Enterprise Edition 8.") != -1)) //NOI18N
            isPlatform = false;
        return isPlatform;
    }
    
    /*
     * Get Server Version
     *
     * @return true if 9.0
     */
    public boolean isGlassfish(){
        boolean isGlassfish = true;
        J2EEServerMgmtController controller = getJ2EEServerMgmtController();
        String serverName = controller.getServerName();
        if((serverName != null) && (serverName.indexOf("8.") != -1)) //NOI18N
            isGlassfish = false;
        return isGlassfish;
    }
}
