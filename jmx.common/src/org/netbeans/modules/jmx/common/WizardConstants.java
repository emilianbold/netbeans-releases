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
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
package org.netbeans.modules.jmx.common;

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
    public static final String PROP_MBEAN_FILE_PATH  = "wizdata.mbeanFilePath"; // NOI18N
    public static final String PROP_MBEAN_IMPL_REG_ITF = "wizdata.mbeanImplemRegItf"; // NOI18N
    public static final String PROP_MBEAN_PRE_REG_PARAM = "wizdata.mbeanPreRegParam"; // NOI18N
    
    // properties to see if mbean wrapps existing ressource
    public static final String PROP_MBEAN_EXISTING_CLASS = "wizdata.mbeanExistingClass"; // NOI18N
    public static final String PROP_MBEAN_EXISTING_CLASS_IS_MXBEAN = "wizdata.mbeanExistingClass.ismxbean"; // NOI18N
            
    // agent default location in the wizard map
    public static final String PROP_AGENT_NAME          = "wizdata.agentName"; // NOI18N
    public static final String PROP_AGENT_PACKAGE_NAME  = "wizdata.agentPackageName"; // NOI18N
    public static final String PROP_AGENT_PACKAGE_PATH  = "wizdata.agentPackagePath"; // NOI18N
    
    //Used for generation of a JMX  Agent
    public static final String AGENT_TEMPLATE_CLASS             = "Agent"; // NOI18N
    public static final String MAIN_METHOD_NAME                 = "main"; // NOI18N
    public static final String PROP_AGENT_DEFAULT_NAME          = "JMXAgent";// NOI18N
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
    //public static final String PROP_MANAGER_RMI_URL_SELECTED  = 
    //        "wizdata.managerRmiUrlSelected"; // NOI18N
    //public static final String PROP_MANAGER_FREEFORM_URL_SELECTED  = 
    //        "wizdata.managerFreeFormUrlSelected"; // NOI18N
    public static final String PROP_MANAGER_SECURITY_SELECTED  = 
            "wizdata.managerSecuritySelected"; // NOI18N
    public static final String PROP_MANAGER_CREDENTIAL_SAMPLE_SELECTED  = 
            "wizdata.credentialSampleSelected"; // NOI18N
    public static final String PROP_MANAGER_USER_CREDENTIAL_SELECTED  = 
            "wizdata.userCredentialSelected"; // NOI18N
    
    //public static final String PROP_MANAGER_HOST = 
    //        "wizdata.managerHost"; // NOI18N
    //public static final String PROP_MANAGER_PORT = 
    //        "wizdata.managerPort"; // NOI18N
    public static final String PROP_MANAGER_USER_NAME = 
            "wizdata.managerUserName"; // NOI18N
    public static final String PROP_MANAGER_USER_PASSWORD = 
            "wizdata.managerUserPassword"; // NOI18N
    public static final String PROP_MANAGER_AGENT_URL = 
        "wizdata.managerUrl"; // NOI18N
    public static final Integer MANAGER_MAX_PORT_NUMBER = 65535;
    
    // project hierarchy layout for code generation
    static public final String SRC_DIR  = "src";// NOI18N
    static public final String ETC_DIR  = "etc";// NOI18N
    static public final String TEST_DIR = "test";// NOI18N

    static public final String JAVA_EXT             = "java";// NOI18N
    static public final String PROPERTIES_EXT       = "properties";// NOI18N
    static public final String ACCESS_EXT           = "access";// NOI18N
    static public final String PASSWORD_EXT         = "password";// NOI18N
    static public final String MBEAN_ITF_SUFFIX     = "MBean";// NOI18N
    static public final String MXBEAN_ITF_SUFFIX     = "MXBean";// NOI18N
    static public final String AGENT_ITF_SUFFIX     = "Agent";// NOI18N
    public static final String MBEAN_SUPPORT_SUFFIX = "DynamicSupport";// NOI18N
    
    static public final String GETTER_PREFIX     = "get";// NOI18N
    static public final String SETTER_PREFIX     = "set";// NOI18N
    static public final String PARAM_NAME_PREFIX = "p";// NOI18N
    static public final String ATTR_RET_NAME     = "value";// NOI18N

    static public final String ATTR_ACCESS_READ_WRITE = "Read / Write";// NOI18N
    static public final String ATTR_ACCESS_READ_ONLY  = "ReadOnly";// NOI18N
    static public final String ATTR_ACCESS_WRITE_ONLY  = "WriteOnly";// NOI18N
    
    static public final String ATTR_DESCR_DEFVALUE_PREFIX = "NewAttribute"; // NOI18N
    static public final String ATTR_DESCR_DEFVALUE_SUFFIX = " Description";// NOI18N
    static public final String ATTR_NAME_DEFVALUE         = "NewAttribute";// NOI18N
    
    static public final String METH_NAME_DEFVALUE         = "newOperation";// NOI18N
    static public final String METH_DESCR_DEFVALUE_PREFIX = "newOperation";// NOI18N
    static public final String METH_DESCR_DEFVALUE_SUFFIX = " Description";// NOI18N
    
    static public final String METH_PARAM_NAME_DEFVALUE         = "parameter";// NOI18N
    static public final String METH_PARAM_DESCR_DEFVALUE_PREFIX = "parameter";// NOI18N
    static public final String METH_PARAM_DESCR_DEFVALUE_SUFFIX = " Description";// NOI18N
    
    static public final String METH_EXCEP_CLASS_DEFVALUE = "java.lang.Exception"; // NOI18N
    static public final String METH_EXCEP_DESCR_DEFVALUE = "Exception Description"; // NOI18N
    
    static public final String NOTIF_DESCR_DEFVALUE = "NewNotification Description";// NOI18N
    
    static public final String NOTIF_TYPE_DEFVALUE         = ""; // NOI18N
    static public final String NOTIF_TYPE_ATTRIBUTE_CHANGE = "ATTRIBUTE_CHANGE";// NOI18N
    
    public static final String ATTRIBUTECHANGE_TYPE = 
            "AttributeChangeNotification.ATTRIBUTE_CHANGE";// NOI18N
    public static final String ATTRIBUTECHANGE_NOTIFICATION = 
            "javax.management.AttributeChangeNotification";// NOI18N
    public static final String NOTIFICATION = "javax.management.Notification";// NOI18N
    
    public static final String STANDARD_MBEAN_CLASS = 
            "javax.management.StandardMBean";// NOI18N
    
    static public final String MBEAN_POPUP_EDIT_BUTTON = "Edit";// NOI18N
    static public final String MBEAN_DESCR_DEFVALUE    = " Description";// NOI18N
    static public final String MBEAN_NAME_DEFVALUE = "NewJMXResource";// NOI18N
    
    // MBean types
    static public final String MBEAN_STANDARDMBEAN = "Standard";// NOI18N
    static public final String MXBEAN = "MXBean";// NOI18N
    static public final String MXBEAN_SUFFIX = "MXBean";// NOI18N
    static public final String MXBEAN_ANNOTATION = "MXBean";// NOI18N
    static public final String MBEAN_DYNAMICMBEAN  = "Dynamic";// NOI18N
    static public final String MBEAN_EXTENDED      = "Extended";// NOI18N
    static public final String MBEAN_FROM_EXISTING_CLASS      = "FromExistingClass";// NOI18N
    // Attributes stored on the template wizard:
    /** type String. */

    // information of the MBean panel
    public static final String PROP_MBEAN_DESCRIPTION            = "wizdata.mbeanDescription"; // NOI18N
    public static final String PROP_MBEAN_TYPE                   = "wizdata.mbeanType";       // NOI18N
    public static final String PROP_ATTR_NB                      = "wizdata.attributeNb"; // NOI18N
    public static final String PROP_ATTR_NAME                    = "wizdata.attributeName"; // NOI18N
    public static final String PROP_ATTR_TYPE                    = "wizdata.attributeType"; // NOI18N
    public static final String PROP_ATTR_TYPE_MIRROR                    = "wizdata.attributeTypeMirror"; // NOI18N
    public static final String PROP_ATTR_DESCR                   = "wizdata.attributeDescription"; // NOI18N
    public static final String PROP_METHOD_NB                    = "wizdata.methodNb"; // NOI18N
    public static final String PROP_METHOD_NAME                  = "wizdata.methodName"; // NOI18N
    public static final String PROP_METHOD_TYPE                  = "wizdata.methodType"; // NOI18N
    public static final String PROP_METHOD_PARAM                 = "wizdata.methodParamType"; // NOI18N
    public static final String PROP_METHOD_EXCEP                 = "wizdata.methodException"; // NOI18N
    public static final String PROP_METHOD_DESCR                 = "wizdata.methodDescription"; // NOI18N
    public static final String PROP_IMPL_NOTIF_EMITTER           = "wizdata.notifEmitter"; // NOI18N
    public static final String PROP_GEN_BROADCAST_DELEGATION     = "wizdata.genBroadcastEmitter"; // NOI18N
    public static final String PROP_GEN_SEQ_NUMBER               = "wizdata.genSeqNumber"; // NOI18N
    public static final String PROP_NOTIF_NB                     = "wizdata.notifNb"; // NOI18N
    public static final String PROP_NOTIF_CLASS                  = "wizdata.notifClass"; // NOI18N
    public static final String PROP_NOTIF_DESCR                  = "wizdata.notifDescription"; // NOI18N
    public static final String PROP_NOTIF_TYPE                   = "wizdata.notifType"; // NOI18N
            
    // number of times the user switched panels
    public static final String PROP_USER_ORDER_NUMBER            = "wizdata.intro_attributeName";// NOI18N
    
    // number of user added attributes
    public static final String PROP_USER_ADDED_ATTR              = "wizdata.userAdded_attributeNb";// NOI18N
    
    // number of introspected attributes
    public static final String PROP_INTRO_ATTR_NB                = "wizdata.intro_attributeNb";// NOI18N
    public static final String PROP_INTRO_ATTR_NAME              = "wizdata.intro_attributeName";// NOI18N
    public static final String PROP_INTRO_ATTR_TYPE              = "wizdata.intro_attributeType";// NOI18N
        public static final String PROP_INTRO_ATTR_TYPE_MIRROR              = "wizdata.intro_attributeTypeMirror";// NOI18N
    public static final String PROP_INTRO_ATTR_RW                = "wizdata.intro_attributeAccess";// NOI18N
    public static final String PROP_INTRO_ATTR_DESCR             = "wizdata.intro_attributeDescr";// NOI18N
    public static final String PROP_INTRO_ATTR_SELECT            = "wizdata.intro_attributeSelect";// NOI18N
    
    public static final String PROP_INTRO_METHOD_NB              = "wizdata.intro_methodNb"; // NOI18N
    public static final String PROP_INTRO_METHOD_NAME            = "wizdata.intro_methodName"; // NOI18N
    public static final String PROP_INTRO_METHOD_TYPE            = "wizdata.intro_methodType"; // NOI18N
    public static final String PROP_INTRO_METHOD_PARAM           = "wizdata.intro_methodParamType"; // NOI18N
    public static final String PROP_INTRO_METHOD_EXCEP           = "wizdata.intro_methodException"; // NOI18N
    public static final String PROP_INTRO_METHOD_DESCR           = "wizdata.intro_methodDescription"; // NOI18N
    public static final String PROP_INTRO_METHOD_SELECT            = "wizdata.intro_methodSelect";// NOI18N
    
    /** type boolean. */
    public static final String PROP_ATTR_RW = "wizdata.attributeRW"; // NOI18N
    
    public static final String PARAMETER_SEPARATOR  = ","; // NOI18N
    public static final String EXCEPTIONS_SEPARATOR = ",";// NOI18N
    
    public static final String EMPTY_STRING = " ";// NOI18N
    public static final String EMPTYSTRING = "";// NOI18N

    /** Supported type names */
    public static final String BOOLEAN_OBJ_NAME     = "Boolean";// NOI18N
    public static final String BOOLEAN_OBJ_FULLNAME = "java.lang.Boolean";// NOI18N
    public static final String BOOLEAN_NAME         = "boolean";// NOI18N
    public static final String BYTE_OBJ_NAME        = "Byte";// NOI18N
    public static final String BYTE_OBJ_FULLNAME    = "java.lang.Byte";// NOI18N
    public static final String BYTE_NAME            = "byte";// NOI18N
    public static final String CHAR_OBJ_NAME        = "Character";// NOI18N
    public static final String CHAR_OBJ_FULLNAME    = "java.lang.Character";// NOI18N
    public static final String CHAR_NAME            = "char";// NOI18N
    public static final String DATE_OBJ_NAME        = "Date";// NOI18N
    public static final String DATE_OBJ_FULLNAME    = "java.util.Date";// NOI18N
    public static final String INTEGER_OBJ_NAME     = "Integer";// NOI18N
    public static final String INTEGER_OBJ_FULLNAME = "java.lang.Integer";// NOI18N
    public static final String INT_NAME             = "int";// NOI18N
    public static final String LONG_OBJ_NAME        = "Long";// NOI18N
    public static final String LONG_OBJ_FULLNAME    = "java.lang.Long";// NOI18N
    public static final String LONG_NAME            = "long";// NOI18N
    public static final String FLOAT_OBJ_NAME       = "Float";// NOI18N
    public static final String FLOAT_OBJ_FULLNAME   = "java.lang.Float";// NOI18N
    public static final String FLOAT_NAME           = "float";// NOI18N
    public static final String DOUBLE_OBJ_NAME      = "Double";// NOI18N
    public static final String DOUBLE_OBJ_FULLNAME  = "java.lang.Double";// NOI18N
    public static final String DOUBLE_NAME          = "double";// NOI18N
    public static final String OBJECT_NAME          = "Object";// NOI18N
    public static final String OBJECT_FULLNAME      = "java.lang.Object";// NOI18N
    public static final String OBJECTNAME_NAME      = "ObjectName";// NOI18N
    public static final String OBJECTNAME_FULLNAME  = 
            "javax.management.ObjectName";// NOI18N
    public static final String STRING_OBJ_NAME      = "String";// NOI18N
    public static final String STRING_OBJ_FULLNAME  = "java.lang.String";// NOI18N
    public static final String VOID_NAME            = "void";// NOI18N
    public static final String VOID_OBJ_FULLNAME    = "java.lang.Void";// NOI18N
    public static final String VOID_RET_TYPE        = "void";// NOI18N
    public static final String ARRAYS_FULLNAME      = "java.util.Arrays";// NOI18N
    
    public static final String NULL = "null";// NOI18N
    public static final String CLASS_EXT = ".class";// NOI18N
    
    //key words of java
    public static final String PACKAGE_NAME  = "package";// NOI18N
    
    //Used for generation of a JMX management configuration
    public static final String PROP_CONFIG_FILE_PATH = "wizdata.configFilePath";// NOI18N
    public static final String RMI_ACCESS_FILE       = "wizdata.rmiAccessFile"; // NOI18N
    public static final String RMI_PASSWORD_FILE     = "wizdata.rmiPassFile"; // NOI18N
    public static final String RMI_SELECTED          = "wizdata.rmiSelected";// NOI18N
    public static final String RMI_PORT              = "wizdata.rmiPort";// NOI18N
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
    
    public static final String DESC                 = "_DESC_"; // NOI18N
    public static final String IMMUTABLE_CODE_BEGIN = "GEN-BEGIN:"; // NOI18N
    public static final String IMMUTABLE_CODE_END   = "GEN-END:"; // NOI18N
    
    public static final String TYPE = ".TYPE"; // NOI18N
    public static final String CLASS = ".class"; // NOI18N
    public static final String GETNAME = ".getName()"; // NOI18N

}
