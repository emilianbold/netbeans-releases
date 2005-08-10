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
    
   //Package
   public static final String PROJECT_NAME = "MBeanFunctionalTest";
   public static final String PROJECT_CAT = "General";
   public static final String PROJECT_APP = "Java Application";
   public static final String PACKAGE_NAME = "com.foo.bar";
   public static final String CATEGORY = "JMX MBean";
   
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
   
   public static final String W_ATTR_TBL = "wrapperAttributeTable";
   public static final String W_ATTR_REM_BTN = "wrapperAttributeRemoveButton";
   public static final String W_ATTR_ACCESS_CB = "wrapperAttrAccessBox";
   
   //Operation step
   public static final String OPER_TBL = "methodTable";
   public static final String OPER_ADD_BTN = "methAddJButton";
   public static final String OPER_REM_BTN = "methRemoveJButton";
   
   public static final String W_OPER_TBL = "wrapperOperationTable";
   public static final String W_OPER_REM_BTN = "wrapperOpRemoveJButton";
   
   //Notification step
   public static final String IMPLNOTIFEMIT_CBX = "implNotifEmitCheckBox";
   public static final String GENDELEG_CBX = "genDelegationCheckBox";
   public static final String GENSEQNUM_CBX = "genSeqNbCheckBox";
   public static final String NOTIF_TBL = "notificationTable";
   public static final String NOTIF_ADD_BTN = "notifAddJButton";
   public static final String NOTIF_REM_BTN = "notifRemJButton";

   //Junit step
   public static final String JU_CBX = "junitJChckBox";
   public static final String CLASSTOTEST_TXT = "tfClassToTest";
   public static final String TESTCLASS_TXT = "tfTestClass";
   public static final String DEFMETHBODY_CBX = "defaultMethodBodyJCheckBox";
   public static final String JAVADOC_CBX = "javaDocJCheckBox";
   
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
   public static final String DATE_TYPE = "Date";
   public static final String STR_TYPE = "String";
   public static final String BOOL_TYPE = "boolean";
   public static final String BYTE_TYPE = "byte";
   public static final String CHAR_TYPE = "char";
   public static final String LONG_TYPE = "long";
   public static final String OBJNAME_TYPE = "ObjectName";
   public static final String FLOAT_TYPE = "float";
   public static final String DOUBLE_TYPE = "double";
   public static final String VOID_TYPE = "void";
   
   //============== Access ============================//
   public static final String RO = "ReadOnly";
   public static final String RW = "Read / Write";
   
   //================= Wrapper =======================//
   public static final String ATTRCLASS_TO_WRAP = "WrappedAttribute";
   public static final String OPCLASS_TO_WRAP = "WrappedOperation";
   
   //================= Extensions =======================//
   public static final String JAVA_EXT = ".java";
   public static final String PT = ".";
   public static final String JAVA_FILE_CATEG = "Java Classes";
   public static final String JAVA_FILE_TYPE = "Java Class";
   public static final String SRC_PKG = "Source Packages|";
   public static final String PIPE = "|";
   
   //================ Index for tables ==================//
   
   //Lines
   public static final int LINE_ZERO = 0;
   public static final int LINE_ONE = 1;
   public static final int LINE_TWO = 2;
   public static final int LINE_FOUR = 4;
   public static final int LINE_FIVE = 5;
   public static final int LINE_SIX = 6;
   
   //Wrapper Attribute table columns
   public static final int INCLUDE_COL = 0;
   public static final int ACCESS_COL = 3;
}
