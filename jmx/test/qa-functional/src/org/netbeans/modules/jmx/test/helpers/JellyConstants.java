/*
 * JellyConstants.java
 *
 * Created on August 10, 2005, 9:09 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.jmx.test.helpers;

/**
 *
 * @author alex
 */
public class JellyConstants {
    
   //tempo modes
   public static final String NONE = "";
   public static final String DEBUG = "";
   public static final String NOMINAL = "";
    
   //Package 
   public static final String PROJECT_NAME = "JMXTESTMBeanFunctionalTest";
   
   public static final String PROJECT_CAT = "General";
   public static final String PROJECT_APP = "Java Application";
   public static final String JAVA_PACKAGE = "Java Package";
   public static final String PACKAGE_NAME = "com.foo.bar";
   public static final String CATEGORY = "JMX MBean";
   public static final String TEST_ACTION = "Test Project";
   public static final String MGMT = "Management";
   public static final String SOURCE_PACKAGE = "Source Packages";
   public static final String TEST_PACKAGE = "Test Packages";
   
   //Extensions
   public static final String JAVA_EXT = ".java";
   public static final String PT = ".";
   public static final String D_PT = ":";
   public static final String JAVA_FILE_CATEG = "Java Classes";
   public static final String JAVA_FILE_TYPE = "Java Class";
   public static final String SRC_PKG = "Source Packages|";
   public static final String LINE = "line"; 
   public static final String PIPE = "|";
   public static final String EMPTYSTRING = "";
   
   //MBean type
   public static final String STDMBEAN = "StandardMBean";
   public static final String EXTSTDMBEAN = "ExtendedStandardMBean";
   public static final String DYNMBEAN = "DynamicMBean";
   
   //MBean name
   public static final String MBEAN_ONE = "ConstructTest1MBean";
   public static final String MBEAN_TWO = "ConstructTest2MBean";
   public static final String MBEAN_THREE = "ConstructTest3MBean";
   public static final String MBEAN_FOUR = "ConstructTest4MBean";
   public static final String MBEAN_FIVE = "ConstructTest5MBean";
   public static final String MBEAN_SIX = "ConstructTest6MBean";
   public static final String MBEAN_SEVEN = "ConstructTest7MBean";
   public static final String MBEAN_EIGHT = "ConstructTest8MBean";
   public static final String MBEAN_NINE = "ConstructTest9MBean";
   public static final String MBEAN_TEN = "ConstructTest10MBean";
   public static final String MBEAN_ELEVEN = "ConstructTest11MBean";
   public static final String MBEAN_TWELVE = "ConstructTest12MBean";
   public static final String MBEAN_THIRTEEN = "ConstructTest13MBean";
   public static final String MBEAN_FOURTEEN = "ConstructTest14MBean";
   public static final String MBEAN_FIFTEEN = "ConstructTest15MBean";
   public static final String MBEAN_SIXTEEN = "ConstructTest16MBean";
   
   //MBean Comments
   public static final String MBEAN_ONE_COMMENT = "StandardMBean without attributes, operations and notifications";
   public static final String MBEAN_TWO_COMMENT = "StandardMBean with one simple attribute, one two parameter operation and one notification";
   public static final String MBEAN_FOUR_COMMENT = "StandardMBean with two attributes, three operations and three notifications";
   public static final String MBEAN_FIVE_COMMENT = "ExtendedStandardMBean without attributes, operations and notifications";
   public static final String MBEAN_SIX_COMMENT = "Extended StandardMBean with one simple attribute, one two parameter operation and one notification";
   public static final String MBEAN_EIGHT_COMMENT = "Extended StandardMBean with two attributes, three operations and three notifications";
   public static final String MBEAN_NINE_COMMENT = "DynamicMBean without attributes, operations and notifications";
   public static final String MBEAN_TEN_COMMENT = "DynamicMBean with one simple attribute, one two parameter operation and one notification";
   public static final String MBEAN_TWELVE_COMMENT = "DynamicMBean with two attributes, three operations and three notifications";
   public static final String MBEAN_THIRTEEN_COMMENT = "Wrapped ExtendedStandardMBean with all attributes";
   public static final String MBEAN_FOURTEEN_COMMENT = "Wrapped ExtendedStandardMBean with minimal attributes";
   public static final String MBEAN_FIFTEEN_COMMENT = "Wrapped ExtendedStandardMBean with all operations";
   public static final String MBEAN_SIXTEEN_COMMENT = "Wrapped ExtendedStandardMBean with no operations";
   
   // Diff
   public static final int COMPLETE_GENERATED_FILENAME = 0;
   public static final int CLASSNAME = 1;
   public static final int INTERFACENAME = 2;
    
   public static final int COMPLETE_GENERATED_TEST_FILENAME = 0;
   public static final int JUNIT_FILENAME = 1;
    
   //========= Component names ==============//
   
   //Option step
   public static final String GENFILE_TXT = "generatedFileJTextField";
   public static final String EXISTINGCLASS_CBX = "ExistingClassCheckBox";
   public static final String EXISTINGCLASS_TXT = "ExistingClassTextField";
   public static final String IMPLEMMBEAN_CBX = "ImplementMBeanItf";
   public static final String PREREGPARAM_CBX = "PreRegisterParam";
   public static final String MBEANDESCR_TXT = "mbeanDescriptionJTextField";
   
   //Attribute step
   public static final String ATTR_TBL = "attributeTable";
   public static final String ATTR_ADD_BTN = "attrAddJButton";
   public static final String ATTR_REM_BTN = "attrRemoveJButton";
   public static final String ATTR_ACCESS_CB = "attrAccessBox";
   
   public static final String W_ATTR_TBL = "wrapperAttributeTable";
   public static final String W_ATTR_REM_BTN = "wrapperAttributeRemoveButton";
   public static final String W_ATTR_ACCESS_CB = "wrapperAttrAccessBox";
   
   //Operation step
   public static final String OPER_TBL = "methodTable";
   public static final String OPER_ADD_BTN = "methAddJButton";
   public static final String OPER_REM_BTN = "methRemoveJButton";
   public static final String OPER_TYPE_BX = "methTypeBox";
   public static final String OPER_PARAM_TXT = "methParamTextField";
   public static final String OPER_EXCEP_TXT = "methExcepTextField";
           
   public static final String PARAM_POP_TBL = "ParamPopupTable";
   public static final String PARAM_ADD_BTN = "methAddParamButton";
   public static final String ADD_PARAM_BTN = "addParamJButton";
   public static final String REM_PARAM_BTN = "remParamJButton";
   
   public static final String EXCEP_POP_TBL = "ExcepPopupTable";
   public static final String EXCEP_ADD_BTN = "methAddExcepJButton";
   public static final String ADD_EXCEP_BTN = "addExceptionJButton";
   public static final String REM_EXCEP_BTN = "remExceptionJButton";
   
   public static final String OP_TBL = "operationTable";
   public static final String ADD_OP_BTN = "opAddJButton";
   
   public static final String W_OPER_TBL = "wrapperOperationTable";
   public static final String W_OPER_REM_BTN = "wrapperOpRemoveJButton";
   
   public static final String CLOSE_BTN = "closeJButton";
   public static final String CANCEL_BTN = "cancelJButton";
   
   //Notification step
   public static final String IMPLNOTIFEMIT_CBX = "implNotifEmitCheckBox";
   public static final String GENDELEG_CBX = "genDelegationCheckBox";
   public static final String GENSEQNUM_CBX = "genSeqNbCheckBox";
   public static final String NOTIF_TBL = "notificationTable";
   public static final String NOTIF_ADDTYPE_BTN = "notifTypePopupJButton";
   public static final String NOTIF_ADD_BTN = "notifAddJButton";
   public static final String NOTIF_REM_BTN = "notifRemJButton";
   public static final String NOTIF_CLSS_BX = "notifClassBox";
   
   public static final String NT_POP_TBL = "notifPopupTable";
   public static final String NT_ADD_BTN = "notifTypePopupJButton";
   public static final String NT_TYPE_TXT = "typeTextField";
   public static final String ADD_NT_BTN = "addNotifTypeJButton";
   public static final String REM_NT_BTN = "remNotifTypeJButton";
   public static final String NT_CLOSE_BTN = "closeNotifTypeJButton";

   //Junit step
   public static final String JU_CBX = "junitJChckBox";
   public static final String CLASSTOTEST_TXT = "tfClassToTest";
   public static final String TESTCLASS_TXT = "tfTestClass";
   public static final String DEFMETHBODY_CBX = "defaultMethodBodyJCheckBox";
   public static final String JAVADOC_CBX = "javaDocJCheckBox";
   public static final String GENUNITFILE_TXT = "generatedTestFileJTextField";
   
   //============== Attribute ========================//
   public static final String ATTR1_NAME = "firstAttribute";
   public static final String ATTR1_DESCR = "First Attribute description";
   public static final String ATTR2_NAME = "secondAttribute";
   public static final String ATTR2_DESCR = "Second Attribute description";
   
   //============== Operations ========================//
   
   public static final String OP1_NAME = "FirstOperation";
   public static final String OP1_DESCR = "First Operation Description";
   public static final String OP2_NAME = "SecondOperation";
   public static final String OP2_DESCR = "Second Operation Description";
   public static final String OP3_NAME = "ThirdOperation";
   public static final String OP3_DESCR = "Third Operation Description";
           
           
   //Parameter
   public static final String PARAM1_NAME = "firstParameter";
   public static final String PARAM1_DESCR = "First Parameter Description";
   public static final String PARAM2_NAME = "secondParameter";
   public static final String PARAM2_DESCR = "Second Parameter Description";
   public static final String PARAM3_NAME = "thirdParameter";
   public static final String PARAM3_DESCR = "Third Parameter Description";
   public static final String PARAM4_NAME = "fourthParameter";
   public static final String PARAM4_DESCR = "Fourth Parameter Description";
   public static final String PARAM5_NAME = "fifthParameter";
   public static final String PARAM5_DESCR = "Fifth Parameter Description";
   public static final String PARAM6_NAME = "sixthParameter";
   public static final String PARAM6_DESCR = "Sixth Parameter Description";
   public static final String PARAM7_NAME = "seventhParameter";
   public static final String PARAM7_DESCR = "Seventh Parameter Description";
   public static final String PARAM8_NAME = "eighthParameter";
   public static final String PARAM8_DESCR = "Eighth Parameter Description";
   public static final String PARAM9_NAME = "ninethParameter";
   public static final String PARAM9_DESCR = "Nineth Parameter Description";
   public static final String PARAM10_NAME = "tenthParameter";
   public static final String PARAM10_DESCR = "Tenth Parameter Description";
   
   //Exception
   public static final String EXCEP_NULL_TYPE = "java.lang.NullPointerException";
   public static final String EXCEP1_DESCR = "First Exception description";
   
   //Notification
   public static final String NOTIF1_DESCR = "First Notification Description";
   public static final String NOTIF2_DESCR = "Second Notification Description";
   public static final String NOTIF3_DESCR = "Third Notification Description";
   public static final String NOTIF_USER_DESCR = "User defined notification";
   
   //Notification types
   public static final String NOTIF_CHANGE = "javax.management.AttributeChangeNotification";
   public static final String NOTIF_ = "javax.management.Notification";
   public static final String USER_NOTIF = "UserNotification";
   
   public static final String NOTIF_TYPE_STD = "standard";
   public static final String NOTIF_TYPE_1 = "com.foo.bar.mbean.1";
   public static final String NOTIF_TYPE_2 = "com.foo.bar.mbean.2";
            
   //============== Types ============================//
   public static final String INT_TYPE = "int";
   public static final String INT_TYPE_FULL = "java.lang.Integer";
   public static final String DATE_TYPE = "Date";
   public static final String DATE_TYPE_FULL = "java.util.Date";
   public static final String STR_TYPE = "String";
   public static final String STR_TYPE_FULL = "java.lang.String";
   public static final String STR_ARRAY_TYPE_FULL = "java.lang.String[]";
   public static final String BOOL_TYPE = "boolean";
   public static final String BYTE_TYPE = "byte";
   public static final String CHAR_TYPE = "char";
   public static final String LONG_TYPE = "long";
   public static final String OBJNAME_TYPE = "ObjectName";
   public static final String FLOAT_TYPE = "float";
   public static final String DOUBLE_TYPE = "double";
   public static final String DOUBLE_TYPE_FULL = "java.lang.Double";
   public static final String VOID_TYPE = "void";
   public static final String LIST_TYPE_FULL = "java.util.List";
   public static final String SET_TYPE_FULL = "java.util.Set";
   
   //============== Access ============================//
   public static final String RO = "ReadOnly";
   public static final String RW = "Read / Write";
   
   //================= Wrapper =======================//
   public static final String ATTRCLASS_TO_WRAP = "WrappedAttribute";
   public static final String OPCLASS_TO_WRAP = "WrappedOperation";
   
   //================ Index for tables ==================//
   
   //Wrapper Attribute and Operations table columns
   public static final int EXPOSE_COL = 0;
   public static final int ACCESS_COL = 3;
   public static final int PARAM_COL = 3;
   public static final int EXCEP_COL = 4;
   
   //Wrapper Parameter and Exception columns
   public static final int W_EXCEP_DESCR_COL = 1;
   public static final int W_PARAM_DESCR_COL = 2;
   
   //Wrapper Operation Parameter and Exception comments
   public static final String W_PA_COMMENT1 = "First wrapped parameter comment";
   public static final String W_PA_COMMENT2 = "Second wrapped parameter comment";
   public static final String W_PA_COMMENT3 = "Third wrapped parameter comment";
   
   public static final String W_EX_COMMENT1 = "First wrapped exception comment";
   
   //======================== Diff files =====================================//
   public static final String DATE_TIME = "<current Date and Time>";
   public static final String AUTHOR = "<author>";
   public static final String EXP_LINE = "expected line :";
   
   //*********************** Manager wizard ***********************************/
   
   //Protocol
   public static final String RMI = "rmi";
   public static final String JMXMP = "jmxmp";
   public static final String SNMP = "snmp";
   
   // URL
   public static final String JMXMP_URL = "service:jmx:jmxmp://localhost:2004";
   public static final String SNMP_URL = "service:jmx:snmp://kernighan.imag.com:2004";
   public static final String SNMP_URL_WITH_SUFFIX = "service:jmx:snmp://kernighan.imag.com:2004/mynameserver";
   public static final String SAMPLE_URL = "service:jmx:<your protocol>:<agent address>";
   public static final String RMI_URL = "service:jmx:rmi://localhost:1099/jndi/rmi://localhost:1099/jmxrmi";
   public static final String RMI_URL_WITHOUT_SUFFIX = "service:jmx:rmi://localhost:1099";
   public static final String RMI_URL_K = "service:jmx:rmi://kernighan.imag.com:8080/jndi/rmi://kernighan.imag.com:8080/jmxrmi";
   public static final String JNDI_SUFFIX = "/jndi/rmi://localhost:1099/jmxrmi";
   public static final String JNDI_SUFFIX_BIS = "jndi/rmi://kernighan.imag.com:8080/jmxrmi";
   public static final String JNDI = "jndi";
   public static final String NAMESERVER_SUFFIX = "mynameserver";
   
   public static final String PORT_8080 = "8080";
   public static final String PORT_1099 = "1099";
   public static final String PORT_2004 = "2004";
   
   public static final String LOCALHOST = "localhost";
   public static final String K_HOST = "kernighan.imag.com";
   
   // Project and package
   public static final String MANAGER_PROJ = "JMXTESTManagerFunctionalTest";
   public static final String MANAGER_CATEGORY = "JMX Manager";
   public static final String MANAGER_PACKAGE = "managertestpackage";
   
   // Created file names
   public static final String MANAGER_T1 = "Test1Manager";
   public static final String MANAGER_T2 = "Test2Manager";
   public static final String MANAGER_T3 = "Test3Manager";
   public static final String MANAGER_T4 = "Test4Manager";
   public static final String MANAGER_T5 = "Test5Manager";
   public static final String MANAGER_T6 = "Test6Manager";
   
   /**** Component names ****/
   // manager panel
   public static final String GEN_MAIN_CBX = "managerGenerateMainMethodCheckBox";
   public static final String MAIN_CLASS_CBX = "managerSetAsMainClassCheckBox";
   public static final String SAMPLE_CBX = "generateSampleCodeCheckBox";
           
   // URL panel
   public static final String CUSTOMURL_TXT = "customURLJTextField";
   public static final String EDIT_BTN = "RMIURLButton";
   public static final String SECURITY_CBX = "securityCbx";
   public static final String SAMPLE_RBTN = "sampleRbtn";
   public static final String CUSTOMCREDENTIAL_RBTN = "customCredentialRbtn";
   public static final String USERNAME_TXT = "userNameJTextField";
   public static final String USERPASS_TXT = "userPasswordJTextField";
   public static final String NAME = "name";
   public static final String PASS = "password";
           
   // Popup
   public static final String POPUP_TITLE = "RMI JMX Agent URL";
   public static final String PROTOCOL_DEF = "RMI JVM Agent";
   public static final String PROTOCOL_CBX = "protocolComboBox";
   public static final String HOST_TXT = "hostJTextField";
   public static final String PORT_TXT = "portJTextField";
   public static final String URLSUFFIX_TXT = "urlTextField";
   
   /********************** UI tests *******************************************/
   
   //numbers
   public static final int MINUS_ONE = -1;
   public static final int ZERO = 0;
   public static final int ONE = 1;
   public static final int TWO = 2;
   public static final int THREE = 3;
   public static final int FOUR = 4;
   
   //project
   public static final String MBEAN_DEFAULT_NAME = "NewJMXClass";
   public static final String MBEAN_DEFAULT_NAME_WITH_SUFFIX = "NewJMXClassMBean.java";
   public static final String DYN_MBEAN_SUFFIX = "NewJMXClassDynamicSupport.java";
   
   //warning messages and/or error messages
   public static final String WARN_STEP1 = "Warning: As of JDK 1.4," +
                " it is highly recommended that you do NOT place Java classes" +
                " in the default package.";
   public static final String WARN_STEP2 = "Specify a class to wrap.";
   public static final String WARN_STEP2_BIS = "The specified class does not exist.";
   public static final String WARN_STEP3 = "Two or more attributes have the same name.";
   public static final String WARN_STEP4 = "Two or more operations have the same name and parameter types.";
   
   //step 1
   public static final String PKG_NAME = "uitestPackage";
   
   //step 2
   public static final String BROWSE_BTN = "browseButton";
   public static final String DEF_DESCR = "NewJMXClass Description";
   public static final String TEST_CLASS = "test.class";
   public static final String REAL_TEST_CLASS = "java.lang.String";
   public static final String A_STRING = "./..\"\"ffgP";
   
   //step 3
   public static final String A1_NAME = "count/.er";
   public static final String A2_NAME = "counter2";
   public static final String A3_NAME = "counter2";
   public static final String A4_NAME = "counter3";
   public static final String A4_NAME_BIS = "counter4";
   public static final String A5_PREFIX = "c";
   public static final String A5_PREFIX_MAJ = "C";
   public static final String A5_NAME = "count./er5";
   
   
   public static final String A1_DESC = "desc0";
   public static final String A2_DESC = "desc1";
   public static final String A3_DESC = "desc2";
   public static final String A4_DESC = "desc3";
   public static final String A5_DESC = "desc4";
   
   // observed attribute names
   public static final String COUNTER = "Counter";
   public static final String COUNTER2 = "Counter2";
   public static final String COUNTER3 = "Counter3";
   public static final String COUNTER4 = "Counter4";
   public static final String COUNTER5 = "Counter5";
   
   
   public static final String ATTR_DEF_NAME = "NewAttribute";
   public static final int ATTR_NAME_COL = 0;
   public static final int ATTR_TYPE_COL = 1;
   public static final int ATTR_ACCESS_COL = 2;
   public static final int ATTR_DESCR_COL = 3;
   
   //step 4
   public static final String OPER_DEF_NAME = "newOperation";
   public static final int OPER_NAME_COL = 0;
   public static final int OPER_TYPE_COL = 1;
   public static final int OPER_PARAM_COL = 1;
   public static final int OPER_DESCR_COL = 4;
   
   public static final String O1_NAME = "initCount./er";
   public static final String O1_NAME_FULL = "initCounter";
   public static final String O2_NAME = "initCounter2";
   public static final String O3_NAME = "initCounter3";
   public static final String O4_NAME = "initCounter4";
   
   //param popup
   public static final String EDIT_PARAMETERS = "Edit Parameters";
   public static final String PARAM_DEF_NAME = "parameter";
   public static final String P_SUFFIX = "param";
   
   public static final int PARAM_NAME_COL = 0;
   public static final int PARAM_TYPE_COL = 1;
   public static final int PARAM_DESC_COL = 2;
   
   public static final String P1_NAME = "param/.0";
   public static final String P1_NAME_FULL = "param0";
   public static final String P2_NAME = "param1";
   public static final String P3_NAME = "param2";
   public static final String P4_NAME = "param3";
   
   public static final String OBS_PARAM3 = "char param2";
   
   //exception popup
   public static final String EDIT_EXCEPTIONS = "Edit Exceptions";
   public static final String EXCEP_DEF_NAME = "java.lang.Exception";
   
   public static final int EXCEP_CLASS_COL = 0;
   public static final int EXCEP_DESCR_COL = 1;
   
   public static final String E1_NAME = "java.lang.IllegalArgumen/tException";
   public static final String E1_NAME_FULL = "java.lang.IllegalArgumentException";
   public static final String E3_NAME = "java.lang.IllegalStateException";
   
   public static final String OBS_EXCEP3 = "java.lang.IllegalStateException";
   
   //step 5
   public static final String NOTIF_DEF_CLASS = "javax.management.Notification";
   public static final String NOTIF_OTHER_CLASS = "javax.management.AttributeChangeNotification";
   public static final String NOTIF_USER_CLASS = "com.foo.bar.UserNotification";
   public static final String NOTIF_DEF_DESCR = "NewNotification Description";
   public static final String NOTIF_TYPE = "ATTRIBUTE_CHANGE";
   public static final int NOTIF_CLASS_COL = 0;
   public static final int NOTIF_DESCR_COL = 1;
   public static final int NOTIF_TYPE_COL = 2;
   
   //notif type popup
   public static final String EDIT_NT_TYPES = "Edit Types";
   public static final String NT_DEF_NAME = "newjmxclass.type";
   public static final int NT_TYPE_COL = 0;
   public static final String NT1_TYPE = "newjmx.newtype0";
   public static final String CLOSE_NT_BTN = "closeNotifTypeJButton";
   public static final String N1_DESC = "mycomment";
   
   //wrapper tests
   public static final String W_OP_CLASS = "WrapperOp";
   public static final String W_OP_PACKAGE = "jmxtestmbeanfunctionaltest";
   
   //creation tests
   public static final String MBEAN_CREATE_PACKAGE = "mbeancreationpackage";
   public static final String NEW_JMX_CLASS20 = "NewJMXClass20";
   public static final String WARN_SAME_CLASS = "The file NewJMXClass20.java already exists.";
}
