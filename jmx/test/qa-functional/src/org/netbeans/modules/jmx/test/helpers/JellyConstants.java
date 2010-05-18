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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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

package org.netbeans.modules.jmx.test.helpers;

/**
 * Constants definitions
 */
public class JellyConstants {
    
    // ==================================================================
    // PROJECT DEFINITIONS
    // ==================================================================
    
    // Project categories
    public static final String PROJECT_CATEGORY_JAVA = "Java";
    public static final String PROJECT_CATEGORY_SAMPLES_JMX = "Samples|JMX";
    
    // Project types
    public static final String PROJECT_TYPE_ANAGRAM_GAME =
            "Anagram Game Managed with JMX";
    public static final String PROJECT_TYPE_JAVA_APPLICATION = "Java Application";
    
    // Project names
    public static final String PROJECT_NAME_BOOTSTRAP_MDR_SCAN =
            "JMXTESTBootstrapMDRScan";
    public static final String PROJECT_NAME_MBEAN_FUNCTIONAL =
            "JMXTESTMBeanFunctional";
    public static final String PROJECT_NAME_ACTION_FUNCTIONAL =
            "JMXTESTActionsFunctional";
    public static final String PROJECT_NAME_MANAGER_FUNCTIONAL =
            "JMXTESTManagerFunctional";
    public static final String PROJECT_NAME_CONFIGURATION_FUNCTIONAL =
            "JMXTESTConfigurationFunctional";
    public static final String PROJECT_NAME_J2SE_PROJECT_INTEGRATION =
            "JMXTESTJ2SEProjectIntegration";
    
    // Project nodes names
    public static final String SOURCE_PACKAGES = "Source Packages";
    public static final String TEST_PACKAGES = "Test Packages";
    
    // ==================================================================
    // FILE DEFINITIONS
    // ==================================================================
    
    // File categories
    public static final String FILE_CATEGORY_JAVA = "Java";
    public static final String FILE_CATEGORY_JMX = "JMX";
    public static final String FILE_CATEGORY_OTHER = "Other";
    
    // File types
    //JF: need to distinguish between java empty file and other empty file
    public static final String FILE_TYPE_EMPTY_JAVA_FILE = "Empty Java File";
    public static final String FILE_TYPE_EMPTY_FILE = "Empty File";
    public static final String FILE_TYPE_JAVA_PACKAGE = "Java Package";
    public static final String FILE_TYPE_JAVA_CLASS = "Java Class";
    public static final String FILE_TYPE_STANDARD_MBEAN = "Standard MBean";
    public static final String FILE_TYPE_MXBEAN = "MXBean";
    public static final String FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS =
            "MBean From Existing Java Class";
    public static final String FILE_TYPE_STANDARD_MBEAN_WITH_METADATA =
            "Standard MBean With Metadata";
    public static final String FILE_TYPE_MANAGEMENT_CONFIGURATION_FILE =
            "Management Configuration File";
    public static final String FILE_TYPE_JMX_MANAGER = "JMX Manager";
    
    // Package name
    public static final String PACKAGE_COM_FOO_BAR = "com.foo.bar";
    
    // ==================================================================
    // ACTIONS COMPONENTS DEFINITIONS
    // ==================================================================
    
    // Project Properties action item
    public static final String ACTION_PROPERTIES = "Properties";
    public static final String PROPERTIES_DIALOG_TITLE = "Project Properties";    
    
    // Project Properties Monitoring and Management
    public static final String MONITORING_AND_MANAGEMENT = "Monitoring and Management";
    public static final String ENABLE_RMI_REMOTE_ACCESS_CHECK_BOX = "enableRmiRemoteAccessCheckBox";
    
    // ==================================================================
    // MBEAN COMPONENTS DEFINITIONS
    // ==================================================================
    
    // Name and Location wizard
    public static final String CREATED_FILE_TEXT_FIELD = "generatedFileJTextField";
    public static final String MBEAN_DESCRIPTION_TEXT_FIELD = "mbeanDescriptionJTextField";
    public static final String CLASS_TO_WRAP_TEXT_FIELD = "ExistingClassTextField";
    public static final String CLASS_TO_WRAP_BROWSE_BUTTON = "browseButton";
    public static final String OBJECT_WRAPPED_AS_MXBEAN_CHECK_BOX = "isMXBeanCheckBox";
    
    // Specify Attributes wizard
    public static final String ATTRIBUTE_TABLE = "attributeTable";
    public static final String ATTRIBUTE_ADD_BUTTON = "attrAddJButton";
    public static final String ATTRIBUTE_REMOVE_BUTTON = "attrRemoveJButton";
    public static final String ATTRIBUTE_ACCESS_BOX = "attrAccessBox";
    public static final String WRAPPER_ATTRIBUTE_TABLE = "wrapperAttributeTable";
    public static final String WRAPPER_ATTRIBUTE_REMOVE_BUTTON = "wrapperAttributeRemoveButton";
    public static final String WRAPPER_ATTRIBUTE_ACCESS_BOX = "wrapperAttrAccessBox";
    
    // Specify Operations wizard
    // 2 names have been defined for operation table component,
    // depending the table has been accessed from the toolbar menu or from action
    public static final String OPERATION_TABLE_FROM_MENU = "methodTable";
    public static final String OPERATION_TABLE_FROM_ACTION = "operationTable";
    public static final String OPERATION_ADD_BUTTON_FROM_MENU = "methAddJButton";
    public static final String OPERATION_ADD_BUTTON_FROM_ACTION = "opAddJButton";
    public static final String OPERATION_REMOVE_BUTTON = "methRemoveJButton";
    public static final String OPERATION_TYPE_COMBO_BOX = "methTypeBox";
    public static final String OPERATION_PARAM_TEXT_FIELD = "methParamTextField";
    public static final String OPERATION_EXCEP_TEXT_FIELD = "methExcepTextField";
    public static final String OPERATION_ADD_PARAM_BUTTON = "methAddParamButton";
    public static final String OPERATION_ADD_EXCEP_BUTTON = "methAddExcepJButton";
    public static final String WRAPPER_OPERATION_TABLE = "wrapperOperationTable";
    public static final String WRAPPER_OPERATION_REMOVE_BUTTON = "wrapperOpRemoveJButton";
    
    // Edit Operation parameters wizard
    public static final String PARAMETER_DIALOG_TITLE = "Edit Parameters";
    public static final String PARAMETER_TABLE = "ParamPopupTable";
    public static final String PARAMETER_ADD_BUTTON = "addParamJButton";
    public static final String PARAMETER_REMOVE_BUTTON = "remParamJButton";
    
    // Edit Operation exceptions wizard
    public static final String EXCEPTION_DIALOG_TITLE = "Edit Exceptions";
    public static final String EXCEPTION_TABLE = "ExcepPopupTable";
    public static final String EXCEPTION_ADD_BUTTON = "addExceptionJButton";
    public static final String EXCEPTION_REMOVE_BUTTON = "remExceptionJButton";
    
    public static final String CLOSE_JBUTTON = "closeJButton";
    
    
    // ==================================================================
    // MANAGEMENT CONFIGURATION COMPONENTS DEFINITIONS
    // ==================================================================
    
    // Name and Location wizard
    public static final String RMI_ACCESS_FILE_TEXT_FIELD = "rmiAccessFileJTextField";
    public static final String RMI_PASSWORD_FILE_TEXT_FIELD = "rmiPasswordFileJTextField";
    public static final String ENABLE_THREAD_CONTENTION_CHECK_BOX = "threadContentionJCheckBox";
    
    // Enable RMI wizard
    public static final String ENABLE_RMI_CHECK_BOX = "rMIJCheckBox";
    public static final String RMI_PORT_LABEL = "rMIPortJLabel";
    public static final String RMI_PORT_TEXT_FIELD = "rMIPortJTextField";
    public static final String RMI_REQUIRE_AUTH_CHECK_BOX = "authJCheckBox";
    public static final String RMI_CREDENTIALS_LABEL = "tableNameJLabel";
    public static final String RMI_CREDENTIALS_TABLE = "authTable";
    public static final String RMI_CREDENTIALS_ACCESS_COMBO_BOX = "access";
    public static final String RMI_CREDENTIALS_ADD_BUTTON = "addAuth";
    public static final String RMI_CREDENTIALS_REMOVE_BUTTON = "removeAuth";
    public static final String RMI_SSL_CHECK_BOX = "sslJCheckBox";
    public static final String RMI_SSL_PROTOCOL_LABEL = "sslProtocolJLabel";
    public static final String RMI_SSL_PROTOCOL_TEXT_FIELD = "sslProtocolJTextField";
    public static final String RMI_SSL_CIPHER_LABEL = "sslCipherJLabel";
    public static final String RMI_SSL_CIPHER_TEXT_FIELD = "sslCipherJTextField";
    public static final String RMI_SSL_CLIENT_CHECK_BOX = "sslClientAuthJCheckBox";
    
    // SNMP Configuration wizard
    public static final String ENABLE_SNMP_CHECK_BOX = "sNMPJCheckBox";
    public static final String SNMP_PORT_LABEL = "sNMPPortJLabel";
    public static final String SNMP_PORT_TEXT_FIELD = "sNMPPortJTextField";
    public static final String SNMP_INTERFACE_LABEL = "interfaceJLabel";
    public static final String SNMP_INTERFACE_TEXT_FIELD = "interfaceJTextField";
    public static final String SNMP_TRAP_PORT_LABEL = "sNMPTrapPortJLabel";
    public static final String SNMP_TRAP_PORT_TEXT_FIELD = "trapPortJTextField";
    public static final String SNMP_ACL_CHECK_BOX = "aclJCheckBox";
    public static final String SNMP_ACL_FILE_LABEL = "customACL";
    public static final String SNMP_ACL_FILE_TEXT_FIELD = "aclFileJTextField";
    public static final String SNMP_ACL_FILE_BROWSE_BUTTON = "aclFileJButton";
    
    public static final String INFORMATION_DIALOG_TITLE = "Information";
    
    
    // ==================================================================
    // MANAGER COMPONENTS DEFINITIONS
    // ==================================================================
    
    // Name and Location wizard
    public static final String GENERATE_MAIN_METHOD_CHECK_BOX =
            "managerGenerateMainMethodCheckBox";
    public static final String SET_MAIN_PROJECT_CHECK_BOX =
            "managerSetAsMainClassCheckBox";
    public static final String GENERATE_DISCOVERY_CODE_CHECK_BOX =
            "generateSampleCodeCheckBox";
    
    // Specify JMX Agent URL wizard
    public static final String AGENT_URL_TEXT_FIELD = "customURLJTextField";
    public static final String AGENT_URL_EDIT_BUTTON = "RMIURLButton";
    public static final String AUTHENTICATED_CONNECTION_CHECK_BOX = "securityCbx";
    public static final String GENERATE_SAMPLE_RADIO_BUTTON = "sampleRbtn";
    public static final String GENERATE_CREDENTIALS_RADIO_BUTTON = "customCredentialRbtn";
    public static final String USERNAME_TEXT_FIELD = "userNameJTextField";
    public static final String PASSWORD_TEXT_FIELD = "userPasswordJTextField";
    
    // RMI JMX Agent URL wizard
    public static final String AGENT_URL_DIALOG_TITLE = "RMI JMX Agent URL";
    public static final String URL_PROTOCOL_COMBO_BOX = "protocolComboBox";
    public static final String URL_HOST_TEXT_FIELD = "hostJTextField";
    public static final String URL_PORT_TEXT_FIELD = "portJTextField";
    public static final String URL_PATH_TEXT_FIELD = "urlTextField";
    
    
    // ==================================================================
    // JMX ACTIONS COMPONENTS DEFINITIONS
    // ==================================================================
    
    // JMX action items
    public static final String ACTION_JMX = "JMX";
    public static final String ACTION_ADD_MBEAN_ATTRIBUTES =
            "Add MBean Attributes...";
    public static final String ACTION_ADD_MBEAN_OPERATIONS =
            "Add MBean Operations...";
    public static final String ACTION_IMPLEMENT_MBEAN_REGISTRATION =
            "Implement MBeanRegistration Interface...";
    public static final String ACTION_IMPLEMENT_NOTIFICATION_EMITTER =
            "Implement NotificationEmitter Interface...";
    public static final String ACTION_GENERATE_MBEAN_REGISTRATION =
            "Generate MBean Registration...";
    
    // Add MBean attributes action
    public static final String ADD_ATTRIBUTES_DIALOG_TITLE =
            "Add Attributes. MBean Interface <INTERFACE>.";    
    
    // Add MBean operations action
    public static final String ADD_OPERATIONS_DIALOG_TITLE =
            "Add Operations. MBean Interface <INTERFACE>.";    
    
    // Implement MBeanRegistration Interface action
    public static final String IMPLEMENT_MBEAN_REGISTRATION_DIALOG_TITLE =
            "Implement MBeanRegistration Interface";
    public static final String GENERATE_PRIVATE_FIELDS_CHECK_BOX = 
            "keepRefCheckBox";
    
    // Implement NotificationEmitter interface action    
    public static final String IMPLEMENT_NOTIFICATION_EMITTER_DIALOG_TITLE =
            "Implement NotificationEmitter Interface";
    public static final String GENERATE_DELEGATION_CHECK_BOX =
            "genDelegationCheckBox";
    public static  final String GENERATE_SEQUENCE_NUM_CHECK_BOX =
            "genSeqNbCheckBox";
    public static final String NOTIFICATION_TABLE = "notificationTable";
    public static final String NOTIFICATION_ADD_BUTTON = "notifAddJButton";
    public static final String NOTIFICATION_ADD_TYPE_BUTTON = "notifTypePopupJButton";
    public static final String TYPE_DIALOG_TITLE = "Edit Types";
    public static final String TYPE_TABLE = "notifPopupTable";
    public static final String TYPE_ADD_BUTTON = "addNotifTypeJButton";
    public static final String TYPE_CLOSE_BUTTON = "closeNotifTypeJButton";
    
    // Instantiate and Register MBean
    public static final String GENERATE_MBEAN_REGISTRATION_DIALOG_TITLE =
            "Instantiate and Register MBean";
    public static final String MBEAN_CLASS_TEXT_FIELD = "mbeanClassTextField";
    public static final String OBJECT_NAME_TEXT_FIELD = "objectNameTextField";
    public static final String CONSTRUCTOR_COMBO_BOX = "constructorComboBox";
    public static final String STANDARD_MBEAN_RADIO_BUTTON = "standardMBeanRadioButton";
    public static final String JAVA_CLASS_TEXT_FIELD = "classNameTextField";
    public static final String MANAGEMENT_INTERFACE_COMBO_BOX = "interfaceComboBox";
    
    public static final String ACTION_INFO_TEXT_AREA = "infoTextArea";
    

    // ==================================================================
    // TABLE COLUMN NAMES (used to retrieve column index)
    // ==================================================================
    
    public static final String ATTRIBUTE_EXPOSE_COLUMN_NAME = "Expose";
    public static final String ATTRIBUTE_NAME_COLUMN_NAME = "Attribute Name";
    public static final String ATTRIBUTE_TYPE_COLUMN_NAME = "Type";
    public static final String ATTRIBUTE_ACCESS_COLUMN_NAME = "Access";
    public static final String ATTRIBUTE_DESCRIPTION_COLUMN_NAME = "Description";
    
    public static final String OPERATION_INCLUDE_COLUMN_NAME = "Include";
    public static final String OPERATION_NAME_COLUMN_NAME = "Operation Name";
    public static final String OPERATION_RETURN_TYPE_COLUMN_NAME = "Return Type";
    public static final String OPERATION_PARAMETERS_COLUMN_NAME = "Parameters";
    public static final String OPERATION_EXCEPTIONS_COLUMN_NAME = "Exceptions";
    public static final String OPERATION_DESCRIPTION_COLUMN_NAME = "Description";
    
    public static final String PARAMETER_NAME_COLUMN_NAME = "Parameter Name";
    public static final String PARAMETER_TYPE_COLUMN_NAME = "Parameter Type";
    public static final String PARAMETER_DESCRIPTION_COLUMN_NAME = "Parameter Description";
    
    public static final String EXCEPTION_CLASS_COLUMN_NAME = "Exception Class";
    public static final String EXCEPTION_DESCRIPTION_COLUMN_NAME = "Exception Description";
    
    public static final String NOTIFICATION_CLASS_COLUMN_NAME = "Notification Class";
    public static final String NOTIFICATION_DESCRIPTION_COLUMN_NAME = "Description";
    public static final String NOTIFICATION_TYPE_COLUMN_NAME = "Notification Type";
    
    public static final String CREDENTIAL_ROLE_COLUMN_NAME = "Role";
    public static final String CREDENTIAL_PASSWORD_COLUMN_NAME = "Password";
    public static final String CREDENTIAL_ACCESS_COLUMN_NAME = "Access";
    
    
    // ==================================================================
    // WARNING/ERROR MESSAGES
    // ==================================================================

    public static final String DEFAULT_PACKAGE_WARNING =
            "Warning: It is highly recommended that you do NOT place " +
            "Java classes in the default package.";
    public static final String SPECIFY_CLASS_TO_WRAP_WARNING =
            "Specify a class to wrap.";
    public static final String CLASS_TO_WRAP_DOES_NOT_EXIST_WARNING =
            "The specified class does not exist.";
    public static final String SAME_ATTRIBUTE_WARNING =
            "Two or more attributes have the same name.";
    public static final String SAME_OPERATION_WARNING =
            "Two or more operations have the same name and parameter types.";
    public static final String INVALID_CREDENTIAL_WARNING =
            "Role must be a valid Properties file Key. Password can't be null.";
    public static final String EMPTY_USER_PASSWORD_WARNING =
            "User password is mandatory and must not be empty.";
    public static final String SPECIFY_AT_LEAST_ONE_ATTRIBUTE_WARNING =
            "You must specify at least one attribute.";
    public static final String SPECIFY_AT_LEAST_ONE_OPERATION_WARNING =
            "You must specify at least one operation.";
    public static final String NOT_A_MBEAN_CLASS_WARNING =
            "This class is not a MBean Class.";
    public static final String SPECIFIED_JAVA_CLASS_DOES_NOT_EXIST_WARNING =
            "Specified Java Class does not exist.";
    public static final String SPECIFIED_JAVA_CLASS_CANT_BE_WRAPPED_WARNING =
            "Specified Class can't be wrapped, no Management Interface to expose.";
    
    
    // ==================================================================
    // MISCELLANEOUS
    // ==================================================================
    
    public static final String READ_ONLY = "ReadOnly";
    public static final String READ_WRITE = "Read / Write";
    public static final String WRITE_ONLY = "WriteOnly";
    
}
