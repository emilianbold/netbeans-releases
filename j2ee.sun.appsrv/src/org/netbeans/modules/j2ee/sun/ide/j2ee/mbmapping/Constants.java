/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * Constants.java
 *
 * Created on January 16, 2004, 4:16 PM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.mbmapping;

/**
 *
 * @author  nityad
 */
public interface Constants {
    
    static final String WAIT_NODE = "wait_node"; //NOI18N
    
    //static final String MAP_J2EEAPP_STANDALONE = "ias:type=applications,category=config"; //NOI18N
    static final String MAP_J2EEAPP_STANDALONE = "com.sun.appserv:type=applications,category=config"; //NOI18N
    static final String RUNTIME_WEB_MODULE_NAME = "com.sun.appserv:j2eeType=WebModule,J2EEApplication=null,J2EEServer=server,*";
    
    //Server MBean
    static final String OBJ_J2EEServer = "com.sun.appserv:j2eeType=J2EEServer,name=server,*"; //NOI18N
    static final String OBJ_J2EE = "com.sun.appserv:j2eeType=J2EEServer,name=server,category=runtime"; //NOI18N
    static final String[] JSR_SERVER_INFO = {"debugPort", "nodes", "serverVersion", "restartRequired", "serverVendor" }; //NOI18N
    static final String[] ADDITIONAL_SERVER_INFO = {"port", "domain" }; //NOI18N
    
    //Required Attributes from JSR77 Mbeans for Applications
    static final String[] JSR_APPLICATION_MODULE_RESOURCE = {"objectName", "deploymentDescriptor", "hasWebServices", "state" }; //NOI18N
    static final String[] JSR_WEB_MODULE_RESOURCE ={"objectName", "deploymentDescriptor", "displayName", "docBase", //NOI18N
                                                           "hasWebServices", "welcomeFiles", "workPath", "state" }; //NOI18N
    static final String[] JSR_EJB_MODULE_RESOURCE = {"objectName", "deploymentDescriptor", "state" }; //NOI18N
    static final String[] JSR_RAR_MODULE_RESOURCE = {"objectName", "deploymentDescriptor", "state" }; //NOI18N
    static final String[] JSR_APPCLIENT_MODULE_RESOURCE = {"objectName", "deploymentDescriptor", "state" }; //NOI18N
    
    static final String[] JVM_STR_TO_ARR = {"server-classpath", "classpath-suffix", "classpath-prefix" }; //NOI18N
        
    
    //FIXME: seperate out the ias portion and use only res-type. Move res-type to bundle.                              
    static final String[] RESOURCES_LIST = {"ias:type=resources,category=config,res-type=jdbc-resource",
                     "ias:type=resources,category=config,res-type=jdbc-connection-pool",
                     "ias:type=resources,category=config,res-type=pmf-resource",
                     "ias:type=resources,category=config,res-type=mail-resource",
                     "ias:type=resources,category=config,res-type=connector-resource",
                     "ias:type=resources,category=config,res-type=connector-connection-pool",
                     "ias:type=resources,category=config,res-type=admin-object-resource",
                     "ias:type=resources,category=config,res-type=jms-connection-factory",
                     "ias:type=resources,category=config,res-type=jms-destination-resource",
                     "ias:type=resources,category=config,res-type=custom-resource",
                     "ias:type=resources,category=config,res-type=external-jndi-resource"};
    
    static final String MAP_RESOURCES = "com.sun.appserv:type=resources,category=config";//NOI18N
    static final String OPER_OBJ_JDBCResource = "getJdbcResource"; //NOI18N
    static final String OPER_OBJ_ConnPoolResource = "getJdbcConnectionPool"; //NOI18N
    static final String OPER_OBJ_PMFResource = "getPersistenceManagerFactoryResource"; //NOI18N
    static final String OPER_OBJ_JavaMailResource = "getMailResource";//NOI18N
    static final String OPER_OBJ_ConnectorResource = "getConnectorResource";//NOI18N
    static final String OPER_OBJ_ConnectorConnPoolResource = "getConnectorConnectionPool";//NOI18N
    static final String OPER_OBJ_AdminObjectResource = "getAdminObjectResource";//NOI18N
    //The following 4 require a String param representing target which is null for PE
    static final String OPER_OBJ_JmsResource = "getJmsResource";//NOI18N
    static final String OPER_OBJ_JmsConnectionFactory = "getJmsConnectionFactory";//NOI18N
    static final String OPER_OBJ_JmsDestinationResource = "getJmsDestinationResource";//NOI18N
    static final String OPER_OBJ_JndiCustomResource = "getCustomResource";//NOI18N
    static final String OPER_OBJ_JndiExternalResource = "getExternalJndiResource";//NOI18N                     
    
    //Delete resources
    //The following 4 require a second String param representing target which is null for PE
    static final String DELETE_JDBCResource = "deleteJdbcResource"; //NOI18N
    static final String DELETE_ConnPoolResource = "deleteJdbcConnectionPool"; //NOI18N
    static final String DELETE_PMFResource = "deletePersistenceManagerFactoryResource"; //NOI18N
    static final String DELETE_JavaMailResource = "deleteMailResource";//NOI18N
    static final String DELETE_ConnectorResource = "deleteConnectorResource";//NOI18N
    static final String DELETE_ConnectorConnPoolResource = "deleteConnectorConnectionPool";//NOI18N
    static final String DELETE_AdminObjectResource = "deleteAdminObjectResource";//NOI18N
    static final String DELETE_JndiCustomResource = "deleteCustomResource";//NOI18N
    static final String DELETE_JndiExternalResource = "deleteExternalJndiResource";//NOI18N                     
    static final String DELETE_JmsConnectionFactory = "deleteJmsConnectionFactory";//NOI18N
    static final String DELETE_JmsDestinationResource = "deleteJmsDestinationResource";//NOI18N
    
    
    //J2EE TYPE Names
    static final String SERVER = "J2EEServer";
    static final String JVM = "JVM";
    static final String APP_MOD = "J2EEApplication";
    static final String EJB_MOD = "EJBModule";
    static final String WEB_MOD = "WebModule";
    static final String APPCLIENT_MOD = "AppClientModule";
    static final String RAR_MOD = "ResourceAdapterModule";
    
    static final String APP_MOD_CONFIG = "j2ee-application";
    static final String EJB_MOD_CONFIG = "ejb-module";
    static final String WEB_MOD_CONFIG = "web-module";
    static final String APPCLIENT_MOD_CONFIG = "appclient-module";
    static final String RAR_MOD_CONFIG = "connector-module";
    static final String[] CONFIG_TYPES = {"j2ee-application", "ejb-module", "web-module", "appclient-module", "connector-module"};
    
    static final String ENTITY_BN = "EntityBean";
    static final String STATEFUL_BN = "StatefulSessionBean";
    static final String STATELESS_BN = "StatelessSessionBean";
    static final String MDB = "MessageDrivenBean";
    static final String SERVLET = "Servlet";
    static final String JSP_MONITOR = "JspMonitor";
    static final String RAR = "ResourceAdapter";
    
    //CONFIG - Resource Names
    static final String JDBC_RESOURCE = "jdbc-resource";
    static final String CP_RESOURCE = "jdbc-connection-pool";
    static final String PMF_RESOURCE = "persistence-manager-factory-resource";
    static final String MAIL_RESOURCE = "mail-resource";
    static final String JMS_CONNECTION_FACTORY = "jms-connection-factory";
    static final String JMS_ADMIN_OBJECT = "jms-destination-resource";
    static final String CUSTOM_RESOURCE = "custom-resource";
    static final String EXT_JNDI_RESOURCE = "external-jndi-resource";
    static final String CONN_RESOURCE = "connector-resource";
    static final String ADMIN_OBJ_RESOURCE = "admin-object-resource";
    static final String CONN_CP_RESOURCE = "connector-connection-pool";
    
    static final String JMS_RESOURCE = "jms-resource";
    static final String JMS_CONN_RESOURCE = "jms-connector-resource";
    static final String JMS_ADMIN_OBJ_RESOURCE = "jms-admin-object-resource";
    static final String PMFACTORY_RESOURCE = "pmf-resource"; 
    
    //Extra Properties
    static final String GET_OPT_PROPERTIES = "getProperties";
    static final String SET_OPT_PROPERTIES = "setProperty";
    
    //Operations
    static final String OP_START = "start";
    static final String OP_STOP = "stop";
    static final String OP_ENABLE = "enable";
    static final String OP_DISABLE = "disable";
    static final String OP_RESTART = "restart";
    static final String OP_UNDEPLOY = "undeploy";
    static final String OP_DELETE_RESOURCES = "delete";
    
    //Log Modules
    static final String LOG_OBJNAME = "com.sun.appserv:type=module-log-levels,config=server-config,category=config";
    static final String[] LOGGERMODULES = {"root", "server", "web-container", "deployment",
                              "connector", "jta", "security", "jms", "jdo",
                              "javamail", "mdb-container", "verifier", "admin",
                              "cmp", "configuration", "saaj", "jts", "cmp-container", 
                              "resource-adapter", "jaxrpc", "jaxr", "classloader",
                              "naming", "ejb-container", "corba"};
                              
    static final String MAP_JVMOptions = "ias:type=java-config,config=server-config,category=config"; //NOI18N
    static final String DEBUG_OPTIONS = "debug-options"; //NOI18N
    static final String JAVA_HOME = "java-home"; //NOI18N 
    static final String DEBUG_OPTIONS_ADDRESS = "address="; //NOI18N 
    static final String JPDA_PORT = "jpda_port_number"; //NOI18N 
    static final String SHARED_MEM = "shared_memory"; //NOI18N 
    static final String ISMEM = "transport=dt_shmem"; //NOI18N
    static final String ISSOCKET = "transport=dt_socket"; //NOI18N
    static final String DEF_DEUG_OPTIONS = "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=1044"; //NOI18N                         
    static final String DEF_DEUG_OPTIONS_81 = "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=9009"; //NOI18N                         
    static final String DEF_DEUG_OPTIONS_SOCKET = "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=11000"; //NOI18N                         
    static final String DEF_DEUG_OPTIONS_SHMEM = "-Xdebug -Xrunjdwp:transport=dt_shmem,server=y,suspend=n,address="; //NOI18N                         
    
    //Config Mbean Queries
    static final String WEBMOD_TOPLEVEL = "com.sun.appserv:type=web-module,category=config,*"; //NOI18N
    static final String APPMOD_TOPLEVEL = "com.sun.appserv:type=j2ee-application,category=config,*"; //NOI18N
    static final String EJBMOD_TOPLEVEL = "com.sun.appserv:type=ejb-module,category=config,*"; //NOI18N
    static final String RARMOD_TOPLEVEL = "com.sun.appserv:type=connector-module,category=config,*"; //NOI18N
    static final String APPCLIENTMOD_TOPLEVEL = "com.sun.appserv:type=appclient-module,category=config,*"; //NOI18N
    
    static final String[] CONFIG_MODULE = {"web-module", "j2ee-application", "ejb-module", "connector-module", "appclient-module"}; //NOI18N
    
    static final String LOAD_QUERY = "getAllUserDeployedComponents"; //NOI18N
    static final String WEBMOD_QUERY = "getAllDeployedWebModules"; //NOI18N
    static final String APPMOD_QUERY = "getAllDeployedJ2EEApplications"; //NOI18N
    static final String EJBMOD_QUERY = "getAllDeployedEJBModules"; //NOI18N
    static final String RARMOD_QUERY = "getAllDeployedConnectors"; //NOI18N
    static final String APPCLIENTMOD_QUERY = "getAllDeployedAppclientModules"; //NOI18N
    
    static final String[] READ_ONLY_PROPS_RESOURCES = {"name", "jndi-name", "object-type"}; //NOI18N
    static final String[] READ_ONLY_PROPS_APPS = {"state", "object-type"}; //NOI18N
    
    static final String SERVER_VERSION = "serverVersion"; //NOI18N
    static final String RESOLVE_TOKEN_OBJNAME = "com.sun.appserv:type=domain,category=config"; //NOI18N
    static final String RESOLVE_TOKEN_OPERATION = "resolveTokens"; //NOI18N
}
