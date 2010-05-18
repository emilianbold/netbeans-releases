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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.sun.manager.jbi;

/**
 *
 * @author Graj
 */
public interface GenericConstants {

    public static final String PORT = "port"; // NOI18N
    public static final String HOSTNAME = "hostName"; // NOI18N
    public static final String DOMAINNAME = "domainName"; // NOI18N
    public static final String HTTP_ADMINISTRATION_PORT = "httpAdministrationPort"; // NOI18N
    public static final String IIOP_PORT = "iiopPort"; // NOI18N
    public static final String JRMP_PORT = "jrmpPort"; // NOI18N
    public static final String HTTP_ENDPOINT_PORT = "httpEndpointPort"; // NOI18N
    public static final String USER_NAME = "userName"; // NOI18N
    public static final String PASSWORD = "password"; // NOI18N
    public static final String MULTICAST_GROUP_ADDRESS = "multicastGroupAddress"; // NOI18N
    public static final String MULTICAST_GROUP_PORT = "multicastGroupPort"; // NOI18N
    public static final String MULTICAST_TIME_TO_LIVE = "multicastTimeToLive"; // NOI18N
    public static final String MULTICAST_MESSAGE_SEND_INTERVAL = "multicastMessageSendInterval"; // NOI18N
    public static final String UPDATER_REFRESH_RATE = "updaterRefreshRate"; // seconds // NOI18N

    public static final String DEFAULT_HOST_NAME = "127.0.0.1"; // NOI18N
    public static final String DEFAULT_DOMAIN_NAME = "domain1"; // NOI18N
    public static final String DEFAULT_ADMIN_PORT = "4848"; // NOI18N
    public static final String DEFAULT_HTTP_USER_PORT = "8080"; // NOI18N
    public static final String DEFAULT_IIOP_ADMIN_PORT = "3700"; // NOI18N
    public static final String DEFAULT_JRMP_ADMIN_PORT = "8686"; // NOI18N
    public static final String DEFAULT_USER_NAME = "admin"; // NOI18N
    public static final String DEFAULT_CREDENTIALS = "adminadmin"; // NOI18N
    public static final String DEFAULT_MULTICAST_GROUP_ADDRESS = "225.1.2.3"; // NOI18N
    public static final String DEFAULT_MULTICAST_GROUP_PORT = "5000"; // NOI18N
    public static final String DEFAULT_MULTICAST_TIME_TO_LIVE = "10"; // NOI18N
    public static final String DEFAULT_MULTICAST_MESSAGE_SEND_INTERVAL = "60"; // seconds // NOI18N
    public static final String DEFAULT_UPDATER_REFRESH_RATE = "8"; // seconds // NOI18N

    // None
    public static final int INVALID_DIRECTION_KEY = 10;
    // backward
    public static final int PROVIDER_TO_CONSUMER_DIRECTION_KEY = -1;
    // forward and backward round-trip
    public static final int CONSUMER_TO_PROVIDER_AND_BACK_DIRECTION_KEY = 0;
    //  forward
    public static final int CONSUMER_TO_PROVIDER_DIRECTION_KEY = 1;

    public static final String CONSUMER_SUFFIX = "Consumer"; // NOI18N
    
    
    public static final String PORTMAPS_KEY = "portmaps"; // NOI18N
    public static final String PORTMAP_KEY = "portmap"; // NOI18N
    public static final String DIRECTION_KEY = "direction"; // NOI18N
    public static final String INBOUND_KEY = "inbound"; // NOI18N
    public static final String OUTBOUND_KEY = "outbound"; // NOI18N
    public static final String ENDPOINT_KEY = "endPoint"; // NOI18N
    public static final String SERVICE_KEY = "service"; // NOI18N

    public static final String COLON_DELIMITER = "\\:"; // NOI18N
    public static final String DOLLAR_DELIMITER = "\\$"; // NOI18N
    public static final String VERTICAL_LINE_DELIMITER = "\\|"; // NOI18N
    public static final String TILDE_DELIMITER = "\\~"; // NOI18N

    public static final String ENDPOINT_SELECTION_SESSION_KEY = "EndpointsSelected"; // NOI18N
    public static final String ENDPOINT_MANAGER_SESSION_KEY = "EndpointManager"; // NOI18N
    public static final String CONNECTION_METADATA_HELPER_SESSION_KEY = "ConnectionMetadataHelper"; // NOI18N

//    public static final String ASPECT_PACKAGER_METADATA_SESSION_KEY = "AspectPackagerMetadata";
//    public static final String ASPECT_CONFIGURATION_PROPERTIES_SESSION_KEY = "AspectConfigurationProperties";
    
    public static final String GOVERNANCE_CONFIGURATION_SESSION_KEY = "GovernanceConfiguration"; // NOI18N
    public static final String GOVERNANCE_CURRENT_SERVICE_ASSEMBLY_SESSION_KEY = "GovernanceCurrentServiceAssembly"; // NOI18N
    public static final String GOVERNANCE_CURRENT_SERVICE_UNIT_LIST_SESSION_KEY = "GovernanceCurrentServiceUnitList"; // NOI18N
    
    

    public static final String CONNECTION_PROPERTIES_KEY = "connection.properties"; // NOI18N
    public static final String SERVER_INFORMATION_KEY = "SERVER_INFORMATION"; // NOI18N

    public static final String SERVICE_UNIT_NAME_PARAMETER_KEY = "serviceUnitName"; // NOI18N
    
    public static final String NAME_PARAMETER_KEY = "name"; // NOI18N
    public static final String OPERATION_PARAMETER_KEY = "operation"; // NOI18N
    public static final String INPUT_PARAMETER_KEY = "input"; // NOI18N
    public static final String TYPE_PARAMETER_KEY = "type"; // NOI18N
    public static final String COMPONENT_NAME_KEY = "componentName"; // NOI18N
    public static final String SHOW_PARAMETER_KEY = "show"; // NOI18N
    public static final String KEY_PARAMETER_KEY = "key"; // NOI18N
    public static final String CLEAN_PARAMETER_KEY = "clean";  // NOI18N
    public static final String FILE_NAME_PARAMETER_KEY = "fileName"; // NOI18N
    public static final String ACTION_NAME_PARAMETER_KEY = "actionName";     // NOI18N
    public static final String REMOVE_PARAMETER_KEY = "remove";  // NOI18N
    public static final String CONFIGURE_PARAMETER_KEY = "configure";  // NOI18N
    
    public static final String TYPE_PARAMETER_KEY_VALUE = "ServiceEngine"; // NOI18N
    public static final String SHOW_CONFIG_PROPERTIES_PARAMETER_KEY_VALUE = "ConfigProperties"; // NOI18N
    public static final String SHOW_PORTMAP_URL_PARAMETER_KEY_VALUE = "PortMapURL"; // NOI18N

    public static final String PROVISIONING_ID = "Provider"; // NOI18N
    public static final String CONSUMING_ID = "Consumer"; // NOI18N

    public static final String PROVISIONING_SERVICE_NAME = "com.sun.ProvisioningService"; // NOI18N
    public static final String PROVISIONING_SERVICE_UNIT_SUFFIX = "-ProvisioningServiceUnit"; // NOI18N

    /** Deployment Type  */
    public static final String DEPLOYMENT_TYPE = "service-assembly"; // NOI18N
    /** unknown type */
    public static final String UNKNOWN_TYPE = "unknown"; // NOI18N
    /** Binding type  */
    public static final String BINDING_TYPE = "binding-component"; // NOI18N
    /** Engine Type */
    public static final String ENGINE_TYPE = "service-engine"; // NOI18N
    /** Namespace Type  */
    public static final String NAMESPACE_TYPE = "shared-library"; // NOI18N

    /** state  Loaded status.  */
    public static final String UNKNOWN_STATE = "Unknown"; // NOI18N
    /** Installed status */
    public static final String SHUTDOWN_STATE = "Shutdown"; // NOI18N
    /** Stopped status  */
    public static final String STOPPED_STATE = "Stopped"; // NOI18N
    /** Started status */
    public static final String STARTED_STATE = "Started"; // NOI18N




    public static final String SOAP_ENCODING_SCHEMA_URL = "http://schemas.xmlsoap.org/soap/encoding/"; // NOI18N
    public static final String WSDL_SOAP_SCHEMA_URL = "http://schemas.xmlsoap.org/wsdl/soap/"; // NOI18N
    public static final String WSDL_MODEL_SESSION_ATTRIBUTE_KEY = "WSDL_MODEL_SESSION_ATTRIBUTE"; // NOI18N
    public static final String WSDL_MODEL_DOCUMENT_SESSION_ATTRIBUTE_KEY = "WSDL_MODEL_DOCUMENT_SESSION_ATTRIBUTE"; // NOI18N
    public static final String RESULT_SESSION_ATTRIBUTE_KEY = "RESULT"; // NOI18N
    public static final String SOAP_TRANSPORT_KEY = "http://schemas.xmlsoap.org/soap/http"; // NOI18N
    public static final String HTTP_URL_PREFIX_KEY = "http://"; // NOI18N
    public static final String FILE_URL_REFIX_KEY = "file:///"; // NOI18N
    public static final String WSDL_VERBOSE_KEY = "javax.wsdl.verbose"; // NOI18N
    public static final String WSDL_IMPORT_DOCUMENTS_KEY = "javax.wsdl.importDocuments"; // NOI18N
    public static final String WSDL_SCHEMA_URL = "http://schemas.xmlsoap.org/wsdl/"; // NOI18N


    public static final String LIST_BINDING_COMPONENTS_OPERATION_NAME = "listBindingComponents"; // NOI18N
    public static final String LIST_SERVICE_ENGINES_OPERATION_NAME = "listServiceEngines"; // NOI18N
    public static final String LIST_SHARED_LIBRARIES_OPERATION_NAME = "listSharedLibraries"; // NOI18N
    public static final String LIST_SERVICE_ASSEMBLIES_OPERATION_NAME = "listServiceAssemblies"; // NOI18N
    public static final String LIST_SHARED_LIBRARY_DEPENDENTS_OPERATION_NAME = "listSharedLibraryDependents"; // NOI18N
    
    public static final String GET_COMPONENT_INSTALLATION_DESCRIPTOR_NAME = "getComponentInstallationDescriptor"; // NOI18N
    public static final String GET_SHARED_LIBRARY_INSTALLATION_DESCRIPTOR_NAME = "getSharedLibraryInstallationDescriptor"; // NOI18N
    public static final String GET_SERVICE_UNIT_DEPLOYMENT_DESCRIPTOR_NAME = "getServiceUnitDeploymentDescriptor"; // NOI18N
    public static final String GET_SERVICE_ASSEMBLY_DEPLOYMENT_DESCRIPTOR_NAME = "getServiceAssemblyDeploymentDescriptor"; // NOI18N
    public static final String GET_COMPONENT_LOGGER_LEVELS_NAME = "getComponentLoggerLevels"; // NOI18N
    public static final String SET_COMPONENT_LOGGER_LEVEL_NAME = "setComponentLoggerLevel"; // NOI18N

    public static final String DEPLOY_SERVICE_ASSEMBLY_OPERATION_NAME = "deployServiceAssembly"; // NOI18N
    public static final String INSTALL_COMPONENT_OPERATION_NAME = "installComponent"; // NOI18N
    public static final String INSTALL_SHARED_LIBRARY_OPERATION_NAME = "installSharedLibrary"; // NOI18N
    public static final String SHUTDOWN_COMPONENT_OPERATION_NAME = "shutdownComponent"; // NOI18N
    public static final String START_COMPONENT_OPERATION_NAME = "startComponent"; // NOI18N
    public static final String STOP_COMPONENT_OPERATION_NAME = "stopComponent"; // NOI18N
    public static final String UPGRADE_COMPONENT_OPERATION_NAME = "updateComponent"; // NOI18N

    public static final String START_SERVICE_ASSEMBLY_OPERATION_NAME = "startServiceAssembly"; // NOI18N
    public static final String STOP_SERVICE_ASSEMBLY_OPERATION_NAME = "stopServiceAssembly"; // NOI18N
    public static final String SHUTDOWN_SERVICE_ASSEMBLY_OPERATION_NAME = "shutdownServiceAssembly"; // NOI18N

    public static final String UNDEPLOY_SERVICE_ASSEMBLY_OPERATION_NAME = "undeployServiceAssembly"; // NOI18N
    public static final String UNINSTALL_COMPONENT_OPERATION_NAME = "uninstallComponent"; // NOI18N
    public static final String UNINSTALL_SHARED_LIBRARY_OPERATION_NAME = "uninstallSharedLibrary"; // NOI18N

    public static final String DEPLOY_FOLDER_NAME = "deploy"; // NOI18N
    public static final String BINDING_COMPONENTS_FOLDER_NAME = "bindingComponents"; // NOI18N
    public static final String SERVICE_ENGINES_FOLDER_NAME = "serviceEngines"; // NOI18N
    public static final String SHARED_LIBRARIES_FOLDER_NAME = "sharedLibraries"; // NOI18N
    public static final String SERVICE_ASSEMBLIES_FOLDER_NAME = "serviceAssemblies"; // NOI18N
    
    public static final String ADD_APPLICATION_VARIABLE_NAME = "addApplicationVariable"; // NOI18N
    public static final String DELETE_APPLICATION_VARIABLE_NAME = "deleteApplicationVariable"; // NOI18N
    public static final String SET_APPLICATION_VARIABLE_NAME = "setApplicationVariable"; // NOI18N
    public static final String ADD_APPLICATION_CONFIGURATION_NAME = "addApplicationConfiguration"; // NOI18N
    public static final String DELETE_APPLICATION_CONFIGURATION_NAME = "deleteApplicationConfiguration"; // NOI18N
    public static final String SET_APPLICATION_CONFIGURATION_NAME = "setApplicationConfiguration"; // NOI18N

    public static final String SUN_JBI_DOMAIN_NAME = "com.sun.jbi"; // NOI18N
    public static final String STC_EBI_DOMAIN_NAME = "com.sun.ebi"; // NOI18N

    public static final String EM_DOMAIN_NOTIFICATION_HANDLER = "com.sun.eManager:name=DomainNotificationHandler,ServiceType=eManagerAdministration"; // NOI18N

    // MBean Open Type class names
    public static final String OPEN_TYPE_CLASS_VOID = "java.lang.Void"; // NOI18N
    public static final String OPEN_TYPE_CLASS_BOOLEAN = "java.lang.Boolean"; // NOI18N
    public static final String OPEN_TYPE_CLASS_CHARACTER = "java.lang.Character"; // NOI18N
    public static final String OPEN_TYPE_CLASS_BYTE = "java.lang.Byte"; // NOI18N
    public static final String OPEN_TYPE_CLASS_SHORT = "java.lang.Short"; // NOI18N
    public static final String OPEN_TYPE_CLASS_INTEGER = "java.lang.Integer"; // NOI18N
    public static final String OPEN_TYPE_CLASS_LONG = "java.lang.Long"; // NOI18N
    public static final String OPEN_TYPE_CLASS_FLOAT = "java.lang.Float"; // NOI18N
    public static final String OPEN_TYPE_CLASS_DOUBLE = "java.lang.Double"; // NOI18N
    public static final String OPEN_TYPE_CLASS_STRING = "java.lang.String"; // NOI18N
    public static final String OPEN_TYPE_CLASS_BIGDECIMAL = "java.math.BigDecimal"; // NOI18N
    public static final String OPEN_TYPE_CLASS_BIGINTEGER = "java.math.BigInteger"; // NOI18N
    public static final String OPEN_TYPE_CLASS_DATE = "java.util.Date"; // NOI18N
    public static final String OPEN_TYPE_CLASS_OBJECTNAME = "javax.management.ObjectName"; // NOI18N

    // MBean Server Object Name
    public static final String MBEAN_SERVER_OBJECT_NAME = "JMImplementation:type=MBeanServerDelegate"; // NOI18N

    /////////////////////////////////////
    // Local JVM Management Object Names
    /////////////////////////////////////
    // Local JVM Management java.lang.management.ManagementFactory MXBeans Object Names
    public static final String CLASS_LOADING_MXBEAN_NAME = "java.lang:type=ClassLoading"; // NOI18N
    public static final String COMPILATION_MXBEAN_NAME = "java.lang:type=Compilation"; // NOI18N
    public static final String GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE = "java.lang:type=GarbageCollector"; // NOI18N
    public static final String MEMORY_MANAGER_MXBEAN_DOMAIN_TYPE = "java.lang:type=MemoryManager"; // NOI18N
    public static final String MEMORY_MXBEAN_NAME = "java.lang:type=Memory"; // NOI18N
    public static final String MEMORY_POOL_MXBEAN_DOMAIN_TYPE = "java.lang:type=MemoryPool"; // NOI18N
    public static final String OPERATING_SYSTEM_MXBEAN_NAME = "java.lang:type=OperatingSystem"; // NOI18N
    public static final String RUNTIME_MXBEAN_NAME = "java.lang:type=Runtime"; // NOI18N
    public static final String THREAD_MXBEAN_NAME = "java.lang:type=Threading"; // NOI18N

    // Local JVM Management java.lang.management.MemoryNotificationInfo MXBeans Object Names
    public static final String MEMORY_COLLECTION_THRESHOLD_EXCEEDED = "java.management.memory.collection.threshold.exceeded"; // NOI18N
    public static final String MEMORY_THRESHOLD_EXCEEDED = "java.management.memory.threshold.exceeded"; // NOI18N

    /////////////////////////////////////
    // JBI Framework MBeans Object Names
    /////////////////////////////////////
    // Services
    public static final String JBI_ADMINISTRATION_SERVICE_OBJECT_NAME = "com.sun.jbi:ComponentType=System,ControlType=AdministrationService,ServiceName=AdminService"; // NOI18N
    public static final String JBI_CONFIGURATION_SERVICE_OBJECT_NAME = "com.sun.jbi:ComponentType=System,ControlType=ConfigurationService,ServiceName=ConfigurationService"; // NOI18N
    public static final String JBI_DEPLOYMENT_SERVICE_OBJECT_NAME = "com.sun.jbi:ComponentType=System,ControlType=DeploymentService,ServiceName=DeploymentService"; // NOI18N
    public static final String JBI_INSTALLATION_SERVICE_OBJECT_NAME = "com.sun.jbi:ComponentType=System,ControlType=InstallationService,ServiceName=InstallationService"; // NOI18N
    public static final String JBI_MESSAGE_SERVICE_OBJECT_NAME = "com.sun.jbi:ComponentType=System,ControlType=MessageService,ServiceName=MessageService"; // NOI18N
    public static final String JBI_LOGGING_SERVICE_OBJECT_NAME = "com.sun.jbi:ComponentType=System,ControlType=LoggingService,ServiceName=LoggingService"; // NOI18N

    public static final String JBI_FRAMEWORK_STATISTICS_OBJECT_NAME = "com.sun.jbi:ComponentType=System,ControlType=Statistics,ServiceName=Framework"; // NOI18N

    // Heartbeat
    public static final String JBI_HEART_BEAT_ADMIN_SERVICE_OBJECT_NAME = "com.sun.jbi:ComponentType=System,ControlType=HeartBeat,ServiceName=AdminService"; // NOI18N

    // Configuration
    public static final String JBI_ADMINISTRATION_SERVICE_CONFIGURATION_OBJECT_NAME = "com.sun.jbi:ComponentType=System,ControlType=Configuration,ServiceName=AdminService"; // NOI18N
    public static final String JBI_CONFIGURATION_SERVICE_SYSTEM_CONFIG_OBJECT_NAME = "com.sun.jbi:ComponentType=System,ControlType=Configuration,ServiceName=ConfigurationService"; // NOI18N
    public static final String JBI_DEPLOYMENT_SERVICE_SYSTEM_CONFIG_OBJECT_NAME = "com.sun.jbi:ComponentType=System,ControlType=Configuration,ServiceName=DeploymentService"; // NOI18N
    public static final String JBI_INSTALLATION_SERVICE_SYSTEM_CONFIG_OBJECT_NAME = "com.sun.jbi:ComponentType=System,ControlType=Configuration,ServiceName=InstallationService"; // NOI18N
    public static final String JBI_LOGGING_SERVICE_SYSTEM_CONFIG_OBJECT_NAME = "com.sun.jbi:ComponentType=System,ControlType=Configuration,ServiceName=LoggingService"; // NOI18N
    public static final String JBI_MESSAGE_SERVICE_SYSTEM_CONFIG_OBJECT_NAME = "com.sun.jbi:ComponentType=System,ControlType=Configuration,ServiceName=MessageService"; // NOI18N

    // Lifecycle
    public static final String JBI_ADMINISTRATION_SERVICE_LIFECYCLE_OBJECT_NAME = "com.sun.jbi:ComponentType=System,ControlType=Lifecycle,ServiceName=AdminService"; // NOI18N
    public static final String JBI_CONFIGURATION_SERVICE_LIFECYCLE_OBJECT_NAME = "com.sun.jbi:ComponentType=System,ControlType=Lifecycle,ServiceName=ConfigurationService"; // NOI18N
    public static final String JBI_DEPLOYMENT_SERVICE_LIFECYCLE_OBJECT_NAME = "com.sun.jbi:ComponentType=System,ControlType=Lifecycle,ServiceName=DeploymentService"; // NOI18N
    public static final String JBI_INSTALLATION_SERVICE_LIFECYCLE_OBJECT_NAME = "com.sun.jbi:ComponentType=System,ControlType=Lifecycle,ServiceName=InstallationService"; // NOI18N
    public static final String JBI_LOGGING_SERVICE_LIFECYCLE_OBJECT_NAME = "com.sun.jbi:ComponentType=System,ControlType=Lifecycle,ServiceName=LoggingService"; // NOI18N
    public static final String JBI_MESSAGE_SERVICE_LIFECYCLE_OBJECT_NAME = "com.sun.jbi:ComponentType=System,ControlType=Lifecycle,ServiceName=MessageService"; // NOI18N
}
