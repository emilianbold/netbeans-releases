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
package org.netbeans.modules.j2ee.sun.util;

import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.appserv.management.config.AdminObjectResourceConfig;
import com.sun.appserv.management.config.AppClientModuleConfig;
import com.sun.appserv.management.config.ConnectorConnectionPoolConfig;
import com.sun.appserv.management.config.ConnectorResourceConfig;
import com.sun.appserv.management.config.CustomResourceConfig;
import com.sun.appserv.management.config.EJBModuleConfig;
import com.sun.appserv.management.config.J2EEApplicationConfig;
import com.sun.appserv.management.config.JDBCConnectionPoolConfig;
import com.sun.appserv.management.config.JDBCResourceConfig;
import com.sun.appserv.management.config.JNDIResourceConfig;
import com.sun.appserv.management.config.JavaConfig;
import com.sun.appserv.management.config.MailResourceConfig;
import com.sun.appserv.management.config.PersistenceManagerFactoryResourceConfig;
import com.sun.appserv.management.config.RARModuleConfig;
import com.sun.appserv.management.config.ResourceAdapterConfig;
import com.sun.appserv.management.config.WebModuleConfig;

import com.sun.appserv.management.j2ee.AppClientModule;
import com.sun.appserv.management.j2ee.EJBModule;
import com.sun.appserv.management.j2ee.EntityBean;
import com.sun.appserv.management.j2ee.J2EEApplication;
import com.sun.appserv.management.j2ee.MessageDrivenBean;
import com.sun.appserv.management.j2ee.ResourceAdapter;
import com.sun.appserv.management.j2ee.ResourceAdapterModule;
import com.sun.appserv.management.j2ee.Servlet;
import com.sun.appserv.management.j2ee.StatefulSessionBean;
import com.sun.appserv.management.j2ee.StatelessSessionBean;
import com.sun.appserv.management.j2ee.WebModule;
import java.util.Arrays;
import java.util.List;





/**
 *
 * 
 */
public class NodeTypes {
    
    private static Logger logger;
    private static HashMap nodeHierarchy;
    private static HashMap nodeToInterfaceMap;
    private static HashMap nodeToConfigPeerInterfaceMap;
    private static HashMap deleteResOpNameMapper;
    private static HashMap propertiesMapper;    
    public static final String ENABLED = "Enabled";
    
    //Container Nodes
    public static final String DOMAIN = "ROOT";
    public static final String APPLICATIONS = "APPLICATIONS";
    public static final String RESOURCES = "RESOURCES";
    public static final String RESOURCES90 = "RESOURCES90";
    public static final String ENTERPRISE_APPLICATIONS = "ENTERPRISE_APPS";
    public static final String ENTERPRISE_APPLICATION = "ENTERPRISE_APP";
    public static final String WEB_APPLICATIONS = "WEB_APPS";
    public static final String WEB_APPLICATION = "WEB_APP";
    public static final String EJB_MODULES = "EJB_MODULES";
    public static final String EJB_MODULE = "EJB_MODULE";
    public static final String CONNECTOR_MODULES = "CONNECTOR_MODULES";
    public static final String CONNECTOR_MODULE = "CONNECTOR_MODULE";    
    public static final String APP_CLIENT_MODULES = "APPCLIENTS";    
    public static final String APP_CLIENT_MODULE = "APPCLIENT";
    public static final String SIP_APPLICATIONS = "SIP_APPS";
    public static final String SIP_APPLICATION = "SIP_APP";
    
    public static final String JDBC = "JDBC";
    public static final String JDBC_RESOURCES = "JDBC_RESOURCES";
    public static final String CONNECTION_POOLS = "CONNECTION_POOLS";
    public static final String PERSISTENCE_MANAGER_RESOURCES = "PM_RESOURCES";
    public static final String JMS_RESOURCES = "JMS_RESOURCES";   
    public static final String CONNECTION_FACTORIES = "CONNECTION_FACTORIES";   
    public static final String DESTINATION_RESOURCES = "DESTINATION_RESOURCES";   
    public static final String MAIL_RESOURCES = "MAIL_RESOURCES";
    public static final String JNDI = "JNDI";    
    public static final String CUSTOM_RESOURCES = "CUSTOM_RESOURCES";    
    public static final String EXTERNAL_RESOURCES = "EXTERNAL_RESOURCES";    
    public static final String CONNECTORS = "CONNECTORS";
    public static final String CONNECTOR_RESOURCES = "CONNECTOR_RESOURCES";
    public static final String CONNECTOR_CONNECTION_POOLS = "CONNECTOR_CONNECTION_POOLS";
    public static final String ADMIN_OBJECT_RESOURCES = "ADMIN_OBJECT_RESOURCES";  
  

    
    //Leaf Nodes
    public static final String CLUSTER = "CLUSTER";
    public static final String STANDALONE_INSTANCE = "STANDALONE_INSTANCE";
    public static final String JDBC_RESOURCE = "JDBC_RESOURCE";
    public static final String CONNECTION_FACTORY = "CONNECTION_FACTORY";
     public static final String CONNECTION_FACTORY_POOL = "CONNECTION_FACTORY_POOL";
    public static final String CONNECTION_POOL = "CONNECTION_POOL";    
    public static final String DESTINATION_RESOURCE = "DESTINATION_RESOURCE";
    public static final String CUSTOM_RESOURCE = "CUSTOM_RESOURCE";
    public static final String EXTERNAL_RESOURCE = "EXTERNAL_RESOURCE";
    public static final String CONNECTOR_RESOURCE = "CONNECTOR_RESOURCE";  
    public static final String CONNECTOR_CONNECTION_POOL = "CONNECTOR_CONNECTION_POOL";    
    public static final String MAIL_RESOURCE = "MAIL_RESOURCE"; 
    public static final String PM_RESOURCE = "PM_RESOURCE";    
    public static final String ADMIN_OBJECT_RESOURCE = "ADMIN_OBJECT_RESOURCE";        
    public static final String JVM = "JVM";
    public static final String SERVLET = "SERVLET";
    public static final String EJB = "EJB";
    public static final String STATELESS_SESSION_BEAN = "STATELESS_BEAN";
    public static final String STATEFUL_SESSION_BEAN = "STATEFUL_BEAN";
    public static final String MESSAGE_DRIVEN_BEAN = "MESSAGE_DRIVEN_BEAN";
    public static final String ENTITY_BEAN = "ENTITY_BEAN";
    public static final String RESOURCE_ADAPTER = "RESOURCE_ADAPTER";
    public static final String SERVER_RESOURCE = "SERVER_RESOURCE";
    
    //configs
    public static final String EJB_MODULE_CONFIG = "EJB_MODULE_CONFIG";
    public static final String CONNECTOR_MODULE_CONFIG = "CONNECTOR_MODULE_CONFIG";
    public static final String RESOURCE_ADAPTER_CONFIG = "RESOURCE_ADAPTER_CONFIG";    
    public static final String APP_CLIENT_MODULE_CONFIG = "APP_CLIENT_MODULE_CONFIG";  
    public static final String WEB_MODULE_CONFIG = "WEB_MODULE_CONFIG";  
    public static final String SERVLET_CONFIG = "SERVLET_CONFIG";  
    public static final String SIPAPP_CONVERGED_PROP = "isConverged";
    public static final String SIPAPP = "SIPAPP";
    public static final String SIPAPP_CONVERGED = "SIPAPP_CONVERGED";

    
    //Property Editor values
    public static final String[] ENTERPRISE_APPLICATION_PROPS = {
       "ObjectType", "server", "modules", "Properties","PropertyNames" };
    public static final String[] WEB_APPLICATION_PROPS = {
       "AvailabilityEnabled", "ContextRoot", "deploymentDescriptor", "Description", "DirectoryDeployed", "Enabled", "HasWebServices", "Location", "Name", "WelcomeFiles" };
    public static final String[] EJB_MODULE_PROPS = {
       "ObjectType", "server", "ejbs", "Properties","PropertyNames" };
    public static final String[] CONNECTOR_MODULE_PROPS = {
       "ObjectType", "server", "resourceAdapters", "Properties","PropertyNames" };
    public static final String[] APP_CLIENT_MODULE_PROPS = {
       "server" };
    public static final String[] JVM_PROPS = {
       "Name", "Properties", "PropertyNames" };   
    public static final String[] SERVER_RESOURCE_PROPS = {
       "ObjectType", "PropertyNames" };
    public static final String[] CONNECTION_FACTORY_PROPS = {
       "ObjectType", "PropertyNames", "Properties" };
    public static final String[] CONNECTION_FACTORY_POOL_PROPS = {
       "Description", "Name", "PropertyNames", "MatchConnections", "ResourceAdapterName", "MaxConnectionUsageCount",
       "ValidateAtMostOncePeriodInSeconds", "ConnectionLeakReclaim", "ConnectionLeakTimeoutInSeconds",
       "ConnectionCreationRetryAttempts", "ConnectionDefinitionName", "ConnectionDefinitionName", 
       "LazyConnectionEnlistment", "LazyConnectionAssociation", "AssociateWithThread", 
       "ConnectionCreationRetryIntervalInSeconds" };
     private static final String[] SIP_APPLICATION_PROPS = {
        "libraries", "object-type"};
       
    //Child definitions
//    private static final String[] DOMAIN_CHILD_TYPES = { 
//        APPLICATIONS, RESOURCES,   JVM };
    private static final String[] APPLICATIONS_CHILD_TYPES = { 
        ENTERPRISE_APPLICATIONS, WEB_APPLICATIONS, EJB_MODULES, 
        CONNECTOR_MODULES, APP_CLIENT_MODULES };
    private static final String[] RESOURCES_CHILD_TYPES = { 
        JDBC, PERSISTENCE_MANAGER_RESOURCES, JMS_RESOURCES, 
        MAIL_RESOURCES ,JNDI, CONNECTORS
    };
    private static final String[] RESOURCES90_CHILD_TYPES = { 
        JDBC, JMS_RESOURCES, MAIL_RESOURCES ,JNDI, CONNECTORS
    };
    private static final String[] JDBC_CHILD_TYPES = { 
        JDBC_RESOURCES, CONNECTION_POOLS };
    private static final String[] JMS_RESOURCES_CHILD_TYPES = { 
        CONNECTION_FACTORIES, DESTINATION_RESOURCES };        
    private static final String[] JNDI_CHILD_TYPES = {
        CUSTOM_RESOURCES, EXTERNAL_RESOURCES };   
    private static final String[] CONNECTORS_CHILD_TYPES = {
        CONNECTOR_RESOURCES, CONNECTOR_CONNECTION_POOLS, 
        ADMIN_OBJECT_RESOURCES };



        
    static {
        
        //initialize logging 
        logger = Logger.getLogger("org.netbeans.modules.j2ee.sun");
        
        nodeHierarchy = new HashMap();
//        nodeHierarchy.put(DOMAIN, DOMAIN_CHILD_TYPES);
        nodeHierarchy.put(APPLICATIONS, APPLICATIONS_CHILD_TYPES);
        nodeHierarchy.put(RESOURCES, RESOURCES_CHILD_TYPES);
        nodeHierarchy.put(RESOURCES90, RESOURCES90_CHILD_TYPES);
        nodeHierarchy.put(JDBC, JDBC_CHILD_TYPES);
        nodeHierarchy.put(JMS_RESOURCES, JMS_RESOURCES_CHILD_TYPES);
        nodeHierarchy.put(JNDI, JNDI_CHILD_TYPES);
        nodeHierarchy.put(CONNECTORS, CONNECTORS_CHILD_TYPES);
        
        nodeToInterfaceMap = new HashMap();
        nodeToInterfaceMap.put(JDBC_RESOURCE, JDBCResourceConfig.class);
        nodeToInterfaceMap.put(PM_RESOURCE, PersistenceManagerFactoryResourceConfig.class);
        nodeToInterfaceMap.put(MAIL_RESOURCE, MailResourceConfig.class);
        nodeToInterfaceMap.put(CONNECTOR_RESOURCE, ConnectorResourceConfig.class);
        nodeToInterfaceMap.put(ADMIN_OBJECT_RESOURCE, AdminObjectResourceConfig.class);
//        nodeToInterfaceMap.put(CONNECTION_FACTORY, JMSResourceConfig.class);
        nodeToInterfaceMap.put(CONNECTION_POOL, JDBCConnectionPoolConfig.class);
        nodeToInterfaceMap.put(CUSTOM_RESOURCE, CustomResourceConfig.class);
        nodeToInterfaceMap.put(CONNECTOR_RESOURCE, ConnectorResourceConfig.class);
        nodeToInterfaceMap.put(EXTERNAL_RESOURCE, JNDIResourceConfig.class);        
        nodeToInterfaceMap.put(CONNECTOR_CONNECTION_POOL, ConnectorConnectionPoolConfig.class);        
        nodeToInterfaceMap.put(JVM, JavaConfig.class);        
        nodeToInterfaceMap.put(SERVLET, Servlet.class);         
        nodeToInterfaceMap.put(EJB_MODULE, EJBModule.class);
        nodeToInterfaceMap.put(CONNECTOR_MODULE, ResourceAdapterModule.class);
        nodeToInterfaceMap.put(WEB_APPLICATION, WebModule.class);
        nodeToInterfaceMap.put(ENTERPRISE_APPLICATION, J2EEApplication.class);
        nodeToInterfaceMap.put(CONNECTION_FACTORY, ConnectorResourceConfig.class);
        nodeToInterfaceMap.put(DESTINATION_RESOURCE, AdminObjectResourceConfig.class);
        nodeToInterfaceMap.put(STATELESS_SESSION_BEAN, StatelessSessionBean.class);
        nodeToInterfaceMap.put(STATEFUL_SESSION_BEAN, StatefulSessionBean.class);
        nodeToInterfaceMap.put(MESSAGE_DRIVEN_BEAN, MessageDrivenBean.class);
        nodeToInterfaceMap.put(ENTITY_BEAN, EntityBean.class);
        nodeToInterfaceMap.put(APP_CLIENT_MODULE, AppClientModule.class);
        nodeToInterfaceMap.put(RESOURCE_ADAPTER, ResourceAdapter.class);
//        nodeToInterfaceMap.put(JBI, ResourceAdapter.class);
//        nodeToInterfaceMap.put(SERVICE_ENGINES, ResourceAdapter.class);
//        nodeToInterfaceMap.put(BINDING_COMPONENTS, ResourceAdapter.class);
//        nodeToInterfaceMap.put(SHARED_LIBRARIES, ResourceAdapter.class);
//        nodeToInterfaceMap.put(SERVICE_ASSEMBLIES, ResourceAdapter.class);

        
        //constructed nodeType to config peer interface map
        nodeToConfigPeerInterfaceMap = new HashMap();   
        nodeToConfigPeerInterfaceMap.put(APP_CLIENT_MODULE, AppClientModuleConfig.class);        
        nodeToConfigPeerInterfaceMap.put(EJB_MODULE, EJBModuleConfig.class);
        nodeToConfigPeerInterfaceMap.put(CONNECTOR_MODULE, RARModuleConfig.class);
        nodeToConfigPeerInterfaceMap.put(WEB_APPLICATION, WebModuleConfig.class); 
        nodeToConfigPeerInterfaceMap.put(RESOURCE_ADAPTER, ResourceAdapterConfig.class);       
        nodeToConfigPeerInterfaceMap.put(APP_CLIENT_MODULE, AppClientModuleConfig.class);        
        nodeToConfigPeerInterfaceMap.put(ENTERPRISE_APPLICATION, J2EEApplicationConfig.class);
        
        //mapping of resource to delete method on resources mbean = WORKAROUND
        deleteResOpNameMapper = new HashMap();
        deleteResOpNameMapper.put(JDBC_RESOURCE, 
                "deleteJdbcResource");
        deleteResOpNameMapper.put(PM_RESOURCE, 
                "deletePersistenceManagerFactoryResource");
        deleteResOpNameMapper.put(MAIL_RESOURCE, 
                "deleteMailResource");
        deleteResOpNameMapper.put(CONNECTOR_RESOURCE, 
                "deleteConnectorResource");
        deleteResOpNameMapper.put(ADMIN_OBJECT_RESOURCE, 
                "deleteAdminObjectResource");
        deleteResOpNameMapper.put(EXTERNAL_RESOURCE, 
                "deleteExternalJndiResource");
        deleteResOpNameMapper.put(CONNECTOR_CONNECTION_POOL, 
                "deleteConnectorConnectionPool");
        deleteResOpNameMapper.put(CONNECTION_POOL, 
                "deleteJdbcConnectionPool");
        deleteResOpNameMapper.put(CUSTOM_RESOURCE, 
                "deleteCustomResource");
        deleteResOpNameMapper.put(CONNECTOR_RESOURCE, 
                "deleteConnectorResource");
        deleteResOpNameMapper.put(CONNECTION_FACTORY, 
                "deleteJmsResource");
        deleteResOpNameMapper.put(DESTINATION_RESOURCE, 
                "deleteJmsDestinationResource");
        
         propertiesMapper = new HashMap();
         propertiesMapper.put(ENTERPRISE_APPLICATION, ENTERPRISE_APPLICATION_PROPS);
         propertiesMapper.put(WEB_APPLICATION, WEB_APPLICATION_PROPS);
         propertiesMapper.put(EJB_MODULE, EJB_MODULE_PROPS);
         propertiesMapper.put(CONNECTOR_MODULE, CONNECTOR_MODULE_PROPS);
         propertiesMapper.put(APP_CLIENT_MODULE, APP_CLIENT_MODULE_PROPS);
         propertiesMapper.put(JVM, JVM_PROPS);                
         propertiesMapper.put(SERVER_RESOURCE, SERVER_RESOURCE_PROPS);
         propertiesMapper.put(CONNECTION_FACTORY, CONNECTION_FACTORY_PROPS);
         propertiesMapper.put(CONNECTION_FACTORY_POOL, CONNECTION_FACTORY_POOL_PROPS);                
         propertiesMapper.put(SIP_APPLICATION, SIP_APPLICATION_PROPS);
    }
        
    /**
     *
     */
    private NodeTypes() { }
    
    /**
     * Returns an array of NodeType children as strings given a particular 
     * NodeType name.
     *
     * @param nodeType The node from which children types are derived.
     *
     * @return All the node types for the node name passed.
     */
    static String[] getChildTypes(String nodeType){
        return (String[]) nodeHierarchy.get(nodeType);
    }
    
    /**
     * Returns the corresponding AMX interface for a particular nodeType.
     *
     * @param nodeType The node from which the equivalent AMX interface is 
     *                 derived.
     *
     * @return The corresponding AMX interface.
     */
    public static Class getAMXInterface(String nodeType){
        return (Class) nodeToInterfaceMap.get(nodeType);
    }
    
    
    /**
     * Returns the corresponding AMX config peer interface for a 
     * particular nodeType.
     *
     * @param nodeType The node from which the equivalent AMX config peer 
     *        interface is derived.
     *
     * @return The corresponding AMX config peer interface.
     */
    public static Class getAMXConfigPeerInterface(String nodeType){
        return (Class) nodeToConfigPeerInterfaceMap.get(nodeType);
    }
    
    
    /**
     * Returns the corresponding AMX config peer interface for a 
     * particular nodeType.
     *
     * @param nodeType The node from which the equivalent AMX config peer 
     *        interface is derived.
     *
     * @return The corresponding AMX config peer interface.
     */
    public static String getDeleteResourceMethodName(String nodeType){
        return (String) deleteResOpNameMapper.get(nodeType);
    }
    
    
    /**
     * Returns the corresponding AMX J2EE_TYPE for a particular nodeType.
     *
     * @param nodeType The node from which the equivalent AMX interface is 
     *                 derived.
     *
     * @return The corresponding AMX J2EE_TYPE given a nodeType.
     */
    public static String getAMXJ2EETypeByNodeType(String nodeType){
        return getJ2EETypeValueFromInterface(
            NodeTypes.getAMXInterface(nodeType));
    }
    
    
    /**
     * Returns the corresponding AMX J2EE_TYPE for a particular nodeType.
     *
     * @param nodeType The node from which the equivalent AMX interface is 
     *                 derived.
     *
     * @return The corresponding AMX J2EE_TYPE given a nodeType.
     */
    public static String getAMXConfigPeerJ2EETypeByNodeType(String nodeType){
        return getJ2EETypeValueFromInterface(
            NodeTypes.getAMXConfigPeerInterface(nodeType));
    }

    
    /**
     *
     */
    private static String getJ2EETypeValueFromInterface(Class clazz) {
        String type = null;
        try {
            java.lang.reflect.Field j2eeType = clazz.getField("J2EE_TYPE");
            if(j2eeType == null) {
                return "";
            }
            type = (String)j2eeType.get(String.class);
        } catch (Exception e) {
            logger.log(Level.FINE, e.getMessage(), e);
        }
        return (type != null) ? type : "";
    }
    
    /**
     * Returns an array of property names as strings given a particular 
     * NodeType name.
     *
     * @param nodeType The node from which children types are derived.
     *
     * @return All the property names for the node type passed.
     */
    public static List getNodeProperties(String nodeType){
        List props = null;
        String[] strArr = (String[]) propertiesMapper.get(nodeType);
        if(strArr != null){
            props = Arrays.asList(strArr);
        }
        return props;
    }
}
