/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx;

/**
 *
 *  Wizard internal constants : no I18N necessary
 */
public class WizardConstants
{    
    // project location name in the wizard map
    public static final String PROP_PROJECT_LOCATION    = "wizdata.projectLocation"; // NOI18N
    public static final String PROP_PROJECT_NAME        = "wizdata.projectName"; // NOI18N

    // mbean default location in the wizard map
    public static final String PROP_MBEAN_NAME          = "wizdata.mbeanName"; // NOI18N
    public static final String PROP_MBEAN_PACKAGE_NAME  = "wizdata.mbeanPackageName"; // NOI18N
    public static final String PROP_MBEAN_PACKAGE_PATH  = "wizdata.mbeanPackagePath"; // NOI18N
    public static final String PROP_MBEAN_FILE_PATH  = "wizdata.mbeanFilePath"; // NOI18N
    
    // properties to see if mbean wrapps existing ressource
    public static final String PROP_MBEAN_EXISTING_CLASS = "wizdata.mbeanExistingClass"; // NOI18N
            
    // agent default location in the wizard map
    public static final String PROP_AGENT_NAME          = "wizdata.agentName"; // NOI18N
    public static final String PROP_AGENT_PACKAGE_NAME  = "wizdata.agentPackageName"; // NOI18N
    public static final String PROP_AGENT_PACKAGE_PATH  = "wizdata.agentPackagePath"; // NOI18N
    
    //Used for generation of a JMX  Agent
    public static final String AGENT_TEMPLATE_CLASS             = "Agent"; // NOI18N
    public static final String MAIN_METHOD_NAME                 = "main"; // NOI18N
    public static final String PROP_AGENT_DEFAULT_NAME          = "JMXAgent";
    public static final String PROP_AGENT_INIT_METHOD_NAME      = "init"; // NOI18N
    public static final String PROP_AGENT_MAIN_METHOD_SELECTED  = 
            "wizdata.agentMainMethodSelected"; // NOI18N
    public static final String PROP_AGENT_SAMPLE_CODE_SELECTED  = 
            "wizdata.agentSampleCodeSelected"; // NOI18N
    public static final String PROP_AGENT_MAIN_CLASS_SELECTED =
            "wizdata.agentMainClassSelected"; // NOI18N
    
    // properties to fill for the jmx manager (to be modified)
    public static final String PROP_MANAGER_MAIN_METHOD_SELECTED =
            "wizdata.managerMainMethodSelected"; // NOI18N
    public static final String PROP_MANAGER_MAIN_CLASS_SELECTED =
            "wizdata.managerMainClassSelected"; // NOI18N
    public static final String PROP_MANAGER_SAMPLE_CODE_SELECTED  = 
            "wizdata.managerSampleCodeSelected"; // NOI18N
    public static final String PROP_MANAGER_RMI_URL_SELECTED  = 
            "wizdata.managerRmiUrlSelected"; // NOI18N
    public static final String PROP_MANAGER_FREEFORM_URL_SELECTED  = 
            "wizdata.managerFreeFormUrlSelected"; // NOI18N
    public static final String PROP_MANAGER_SECURITY_SELECTED  = 
            "wizdata.managerSecuritySelected"; // NOI18N
    public static final String PROP_MANAGER_CREDENTIAL_SAMPLE_SELECTED  = 
            "wizdata.credentialSampleSelected"; // NOI18N
    public static final String PROP_MANAGER_USER_CREDENTIAL_SELECTED  = 
            "wizdata.userCredentialSelected"; // NOI18N
    
    public static final String PROP_MANAGER_HOST = 
            "wizdata.managerHost"; // NOI18N
    public static final String PROP_MANAGER_PORT = 
            "wizdata.managerPort"; // NOI18N
    public static final String PROP_MANAGER_USER_NAME = 
            "wizdata.managerUserName"; // NOI18N
    public static final String PROP_MANAGER_USER_PASSWORD = 
            "wizdata.managerUserPassword"; // NOI18N
    public static final String PROP_MANAGER_FREEFORM_URL = 
        "wizdata.managerUrl"; // NOI18N
    public static final Integer MANAGER_MAX_PORT_NUMBER = 65535; // NOI18N
    
    // project hierarchy layout for code generation
    static public final String SRC_DIR  = "src";
    static public final String ETC_DIR  = "etc";
    static public final String TEST_DIR = "test";

    static public final String JAVA_EXT             = "java";
    static public final String PROPERTIES_EXT       = "properties";
    static public final String ACCESS_EXT           = "access";
    static public final String PASSWORD_EXT         = "password";
    static public final String MBEAN_ITF_SUFFIX     = "MBean";
    static public final String JUNIT_TEST_SUFFIX    = "Test";
    static public final String AGENT_ITF_SUFFIX     = "Agent";
    public static final String MBEAN_SUPPORT_SUFFIX = "DynamicSupport";
    
    static public final String GETTER_PREFIX     = "get";
    static public final String SETTER_PREFIX     = "set";
    static public final String PARAM_NAME_PREFIX = "p";
    static public final String ATTR_RET_NAME     = "value";

    static public final String ATTR_ACCESS_READ_WRITE = "Read / Write";
    static public final String ATTR_ACCESS_READ_ONLY  = "ReadOnly";
    static public final String ATTR_ACCESS_WRITE_ONLY  = "WriteOnly";
    
    static public final String ATTR_DESCR_DEFVALUE_PREFIX = "NewAttribute"; 
    static public final String ATTR_DESCR_DEFVALUE_SUFFIX = " Description";
    static public final String ATTR_NAME_DEFVALUE         = "NewAttribute";
    
    static public final String METH_NAME_DEFVALUE         = "newOperation";
    static public final String METH_DESCR_DEFVALUE_PREFIX = "newOperation";
    static public final String METH_DESCR_DEFVALUE_SUFFIX = " Description";
    
    static public final String METH_PARAM_NAME_DEFVALUE         = "parameter";
    static public final String METH_PARAM_DESCR_DEFVALUE_PREFIX = "parameter";
    static public final String METH_PARAM_DESCR_DEFVALUE_SUFFIX = " Description";
    
    static public final String METH_EXCEP_CLASS_DEFVALUE = "java.lang.Exception"; 
    static public final String METH_EXCEP_DESCR_DEFVALUE = "Exception description"; 
    
    static public final String NOTIF_DESCR_DEFVALUE = "NewNotification Description";
    
    static public final String NOTIF_TYPE_DEFVALUE         = ""; 
    static public final String NOTIF_TYPE_ATTRIBUTE_CHANGE = "ATTRIBUTE_CHANGE";
    
    public static final String ATTRIBUTECHANGE_TYPE = 
            "AttributeChangeNotification.ATTRIBUTE_CHANGE";
    public static final String ATTRIBUTECHANGE_NOTIFICATION = 
            "javax.management.AttributeChangeNotification";
    public static final String NOTIFICATION = "javax.management.Notification";
    
    public static final String STANDARD_MBEAN_CLASS = 
            "javax.management.StandardMBean";
    
    static public final String MBEAN_POPUP_EDIT_BUTTON = "Edit";
    static public final String MBEAN_DESCR_DEFVALUE    = " Description";
    static public final String JUNIT_TEST_CLASS_NAME_DEFVALUE = 
            "NewJMXResourceTest";        
    static public final String MBEAN_NAME_DEFVALUE = "NewJMXResource";
    
    // MBean types
    static public final String MBEAN_STANDARDMBEAN = "Standard";
    static public final String MBEAN_DYNAMICMBEAN  = "Dynamic";
    static public final String MBEAN_EXTENDED      = "Extended";
    
    // Attributes stored on the template wizard:
    /** type String. */

    // information of the MBean panel
    public static final String PROP_MBEAN_DESCRIPTION            = "wizdata.mbeanDescription"; 
    public static final String PROP_MBEAN_TYPE                   = "wizdata.mbeanType";        
    public static final String PROP_JUNIT_SELECTED               = "wizdata.junitSelected";
    public static final String PROP_ATTR_NB                      = "wizdata.attributeNb"; // NOI18N
    public static final String PROP_ATTR_NAME                    = "wizdata.attributeName"; // NOI18N
    public static final String PROP_ATTR_TYPE                    = "wizdata.attributeType"; // NOI18N
    public static final String PROP_ATTR_DESCR                   = "wizdata.attributeDescription"; 
    public static final String PROP_METHOD_NB                    = "wizdata.methodNb"; // NOI18N
    public static final String PROP_METHOD_NAME                  = "wizdata.methodName"; // NOI18N
    public static final String PROP_METHOD_TYPE                  = "wizdata.methodType"; // NOI18N
    public static final String PROP_METHOD_PARAM                 = "wizdata.methodParamType"; // NOI18N
    public static final String PROP_METHOD_EXCEP                 = "wizdata.methodException"; 
    public static final String PROP_METHOD_DESCR                 = "wizdata.methodDescription"; 
    public static final String PROP_NOTIF_NB                     = "wizdata.notifNb"; 
    public static final String PROP_NOTIF_CLASS                  = "wizdata.notifClass"; 
    public static final String PROP_NOTIF_DESCR                  = "wizdata.notifDescription"; 
    public static final String PROP_NOTIF_TYPE                   = "wizdata.notifType"; 
    public static final String PROP_JUNIT_CLASSNAME              = "wizdata.junitClassName"; 
    public static final String PROP_JUNIT_LOCATION               = "wizdata.junitLocation"; 
    public static final String PROP_JUNIT_PACKAGE                = "wizdata.junitPackage"; 
    public static final String PROP_JUNIT_SETUP_SELECTED         = "wizdata.junitSetupSelected";
    public static final String PROP_JUNIT_TEARDOWN_SELECTED      = "wizdata.junitTeardownSelected";
    public static final String PROP_JUNIT_DEFMETHBODIES_SELECTED = "wizdata.junitDefMethBodiesSelected";
    public static final String PROP_JUNIT_JAVADOC_SELECTED       = "wizdata.junitJavadocSelected";
    public static final String PROP_JUNIT_HINT_SELECTED          = "wizdata.junitHintSelected";
    

    /** type boolean. */
    public static final String PROP_ATTR_RW = "wizdata.attributeRW"; // NOI18N
    
    public static final String PARAMETER_SEPARATOR  = ","; 
    public static final String EXCEPTIONS_SEPARATOR = ",";
    
    public static final String EMPTY_STRING = " ";
    public static final String EMPTYSTRING = "";

    /** Supported type names */
    public static final String BOOLEAN_OBJ_NAME     = "Boolean";
    public static final String BOOLEAN_OBJ_FULLNAME = "java.lang.Boolean";
    public static final String BOOLEAN_NAME         = "boolean";
    public static final String BYTE_OBJ_NAME        = "Byte";
    public static final String BYTE_OBJ_FULLNAME    = "java.lang.Byte";
    public static final String BYTE_NAME            = "byte";
    public static final String CHAR_OBJ_NAME        = "Character";
    public static final String CHAR_OBJ_FULLNAME    = "java.lang.Character";
    public static final String CHAR_NAME            = "char";
    public static final String DATE_OBJ_NAME        = "Date";
    public static final String DATE_OBJ_FULLNAME    = "java.util.Date";
    public static final String INTEGER_OBJ_NAME     = "Integer";
    public static final String INTEGER_OBJ_FULLNAME = "java.lang.Integer";
    public static final String INT_NAME             = "int";
    public static final String LONG_OBJ_NAME        = "Long";
    public static final String LONG_OBJ_FULLNAME    = "java.lang.Long";
    public static final String LONG_NAME            = "long";
    public static final String FLOAT_OBJ_NAME       = "Float";
    public static final String FLOAT_OBJ_FULLNAME   = "java.lang.Float";
    public static final String FLOAT_NAME           = "float";
    public static final String DOUBLE_OBJ_NAME      = "Double";
    public static final String DOUBLE_OBJ_FULLNAME  = "java.lang.Double";
    public static final String DOUBLE_NAME          = "double";
    public static final String OBJECT_NAME          = "Object";
    public static final String OBJECT_FULLNAME      = "java.lang.Object";
    public static final String OBJECTNAME_NAME      = "ObjectName";
    public static final String OBJECTNAME_FULLNAME  = 
            "javax.management.ObjectName";
    public static final String STRING_OBJ_NAME      = "String";
    public static final String STRING_OBJ_FULLNAME  = "java.lang.String";
    public static final String VOID_NAME            = "void";
    public static final String VOID_OBJ_FULLNAME    = "java.lang.Void";
    public static final String VOID_RET_TYPE        = "void";
    
    public static final String NULL = "null";
    public static final String CLASS_EXT = ".class";
    
    //key words of java
    public static final String PACKAGE_NAME  = "package";
    
    //Used for generation of a JMX management configuration
    public static final String PROP_CONFIG_FILE_PATH = "wizdata.configFilePath";
    public static final String RMI_ACCESS_FILE       = "wizdata.rmiAccessFile"; // NOI18N
    public static final String RMI_PASSWORD_FILE     = "wizdata.rmiPassFile"; // NOI18N
    public static final String RMI_SELECTED          = "wizdata.rmiSelected";
    public static final String RMI_PORT              = "wizdata.rmiPort";
    public static final String RMI_AUTHENTICATE  = 
            "wizdata.rmiAuthenticate"; // NOI18N
    public static final String RMI_AUTHENTICATED_USERS =
            "wizdata.rmiUsers"; // NOI18N
    public static final String RMI_SSL_CLIENT_AUTHENTICATE  = 
            "wizdata.rmiSslClientAuthenticate"; // NOI18N
    public static final String RMI_SSL_TLS_CIPHER  = 
            "wizdata.rmiSslTlsCipher"; // NOI18N
    public static final String SSL_SELECTED  = 
            "wizdata.sslSelected"; // NOI18N
    public static final String RMI_SSL_PROTOCOLS  = 
            "wizdata.rmiSslProtocols"; // NOI18N
    public static final String SNMP_SELECTED  = 
            "wizdata.snmpSelected"; // NOI18N
    public static final String SNMP_PORT  = 
            "wizdata.snmpPort"; // NOI18N
    public static final String SNMP_TRAP_PORT  = 
            "wizdata.snmpTrapPort"; // NOI18N
    public static final String SNMP_INTERFACES  = 
            "wizdata.snmpInterfaces"; // NOI18N
    public static final String SNMP_ACL = 
            "wizdata.snmpAcl"; // NOI18N
    public static final String SNMP_ACL_FILE = 
            "wizdata.snmpAclFile"; // NOI18N
    public static final String THREAD_CONTENTION_MONITOR = 
            "wizdata.threadContentionMonitor"; // NOI18N
    public static final String OTHER_PROP_NUMBER = 
            "wizdata.otherPropNb"; // NOI18N
    public static final String OTHER_PROP_NAME = 
            "wizdata.otherPropName"; // NOI18N
    public static final String OTHER_PROP_VALUE = 
            "wizdata.otherPropValue"; // NOI18N
    
    public static final String CONFIG_TABLE_CREDENTIALS = "Allowed Credentials:";
    
    public static final String DESC                 = "_DESC_"; // NOI18N
    public static final String IMMUTABLE_CODE_BEGIN = "GEN-BEGIN:"; // NOI18N
    public static final String IMMUTABLE_CODE_END   = "GEN-END:"; // NOI18N
    
    public static final String WIZARD_ERROR_MESSAGE = 
            "WizardPanel_errorMessage";
}
