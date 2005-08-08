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

package org.netbeans.modules.jmx.test.mbeanwizard;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JDialog;
import javax.swing.JTable;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jemmy.drivers.tables.JTableMouseDriver;
import org.netbeans.jemmy.operators.DialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.jmx.test.helpers.Attribute;
import org.netbeans.modules.jmx.test.helpers.JellyToolsHelper;
import org.netbeans.modules.jmx.test.helpers.MBean;
import org.netbeans.modules.jmx.test.helpers.Notification;
import org.netbeans.modules.jmx.test.helpers.Operation;
import org.netbeans.modules.jmx.test.helpers.Parameter;



/**
 *
 * @author an156382
 */
public class CreateOneFeatureMBean extends JellyTestCase {
    
    public final String PROJECT_NAME = "MBeanFunctionalTest";
    
    public final String TWO   = "2";
    public final String THREE  = "3";
    public final String SIX    = "6";
    public final String SEVEN   = "7";
    public final String TEN    = "10";
    public final String ELEVEN   = "11";
    
    /** Need to be defined because of JUnit */
    public CreateOneFeatureMBean(String name) {
        super(name);
    }
    
    
    public static NbTestSuite suite() {
        
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CreateOneFeatureMBean("constructTest2MBean"));
        suite.addTest(new CreateOneFeatureMBean("constructTest6MBean"));
        suite.addTest(new CreateOneFeatureMBean("constructTest10MBean"));
        
        suite.addTest(new CreateOneFeatureMBean("constructTest3MBean"));
        suite.addTest(new CreateOneFeatureMBean("constructTest7MBean"));
        suite.addTest(new CreateOneFeatureMBean("constructTest11MBean"));
    
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
    }
    
    public void setUp() {
        JellyToolsHelper.grabProjectNode(PROJECT_NAME);
    }
    
    public void tearDown() {
        
    }
    
    public void constructTest2MBean() {
        
        MBean standardMBean = createStandardMBean(TWO);
        wizardExecution(standardMBean);  
        //TODO Diff between generated and master files
    }
    
    public void constructTest6MBean() {
        
        MBean extendedMBean = createExtendedStandardMBean(SIX);
        wizardExecution(extendedMBean);
        
        //TODO Diff between generated and master files
        
    }
    
    public void constructTest10MBean() {
        
        MBean dynamicMBean = createDynamicMBean(TEN);
        wizardExecution(dynamicMBean);
        
        //TODO Diff between generated and master files
        
    }
    
    public void constructTest3MBean() {
        
        MBean standardMBean = createStandardMBean(THREE);
        wizardExecution(standardMBean);  
        //TODO Diff between generated and master files
    }
    
    public void constructTest7MBean() {
        
        MBean extendedMBean = createExtendedStandardMBean(SEVEN);
        wizardExecution(extendedMBean);
        
        //TODO Diff between generated and master files
        
    }
    
    public void constructTest11MBean() {
        
        MBean dynamicMBean = createDynamicMBean(ELEVEN);
        wizardExecution(dynamicMBean);
        
        //TODO Diff between generated and master files
        
    }
    
  //========================= WizardExecution =================================//
    
    private void wizardExecution(MBean mbean) {
        
        NewFileWizardOperator nfwo = JellyToolsHelper.init("JMX MBean", 
                mbean.getMBeanName(), mbean.getMBeanPackage());
        nfwo.next();
        
        optionStep(nfwo, mbean);
        nfwo.next();
        
        attributeStep(nfwo, mbean);
        nfwo.next();
       
        operationStep(nfwo, mbean);
        nfwo.next();
        
        notificationStep(nfwo, mbean);
        nfwo.next();
      
        junitStep(nfwo, mbean);
        nfwo.finish();
    }
    
    //========================= Class Name generation ===========================//
    
    private MBean createStandardMBean(String number) {
        
        return new MBean(
                "ConstructTest"+number+"MBean", 
                "StandardMBean", 
                "com.foo.bar", 
                "StandardMBean with one simple attribute, one two parameter operation and one notification", 
                constructMBeanAttributes(), constructMBeanOperations(), 
                constructMBeanNotifications(number));
    }
    
    private MBean createExtendedStandardMBean(String number) {
        
        return new MBean(
                "ConstructTest"+number+"MBean", 
                "ExtendedStandardMBean", 
                "com.foo.bar", 
                "Extended StandardMBean with one simple attribute, one two parameter operation and " +
                "one notification", 
                constructMBeanAttributes(), constructMBeanOperations(), 
                constructMBeanNotifications(number));
    }
    
    private MBean createDynamicMBean(String number) {
        
        return new MBean(
                "ConstructTest"+number+"MBean", 
                "DynamicMBean", 
                "com.foo.bar", 
                "DynamicMBean with one simple attribute, one two parameter operation and one notification", 
                constructMBeanAttributes(), 
                constructMBeanOperations(), 
                constructMBeanNotifications(number));
    }
    
    private ArrayList<Attribute> constructMBeanAttributes() {
        
        Attribute mBeanAttribute = new Attribute("firstAttribute", "int", "ReadOnly", 
                "First Attribute description");
        ArrayList<Attribute> attrs = new ArrayList<Attribute>();
        attrs.add(mBeanAttribute);
        
       return attrs;
    }
    
    private ArrayList<Operation> constructMBeanOperations() {
        
        Parameter mBeanOperationParameter1 = new Parameter("firstParameter", "String",
                "First Parameter Description");
        Parameter mBeanOperationParameter2 = new Parameter("secondParameter", "ObjectName",
                "Second Parameter Description");
        ArrayList<Parameter> params = new ArrayList<Parameter>();
        params.add(mBeanOperationParameter1);
        params.add(mBeanOperationParameter2);
        
        //operation construction
        Operation mBeanOperation = new Operation("FirstOperation", "void", params, null, 
                "First Operation Description");
        ArrayList<Operation> ops = new ArrayList<Operation>();
        ops.add(mBeanOperation);
        
        return ops;
    }
    
    private ArrayList<Notification> constructMBeanNotifications(String number) {
        Notification mBeanNotification;
        //Notification construction
        if (number.equals(THREE) || number.equals(SEVEN) || number.equals(ELEVEN)) 
            mBeanNotification = new Notification("javax.management.AttributeChangeNotification",
                "First Notification Description", null);
        else
            mBeanNotification = new Notification("javax.management.Notification",
                "First Notification Description", null);
        ArrayList<Notification> notifs = new ArrayList<Notification>();
        notifs.add(mBeanNotification);
        
        return notifs;
    }
    
    //========================= Class Name generation ===========================//
    
    private String getCompleteGeneratedFileName(NewFileWizardOperator nfwo) {
        return JellyToolsHelper.getTextFieldContent("generatedFileJTextField", nfwo);
    }
    
    private String getClassName(String completeFileName, String mbeanName) {
        return JellyToolsHelper.replaceMBeanClassName(completeFileName, mbeanName+".java");
    }
    
    private String getInterfaceName(String completeFileName) {
        String itfWithExtension = completeFileName.substring(
                completeFileName.lastIndexOf(File.separatorChar)+1);
        return itfWithExtension.substring(0, itfWithExtension.lastIndexOf('.'));
    }
    
    //========================= Panel discovery ==================================//
    
    private void optionStep(NewFileWizardOperator nfwo, MBean mbean) {
        // get the generated file name for campare with master files
        String completeGeneratedFileName = getCompleteGeneratedFileName(nfwo);
        String className = getClassName(completeGeneratedFileName, mbean.getMBeanName());
        String itfName = getInterfaceName(completeGeneratedFileName);
        
        assertFalse(JellyToolsHelper.verifyCheckBoxSelected("ExistingClassCheckBox", nfwo));
        assertFalse(JellyToolsHelper.verifyTextFieldEnabled("ExistingClassTextField", nfwo));
        
        JellyToolsHelper.changeRadioButtonSelection(mbean.getMBeanType(), nfwo, true);
        assertTrue(JellyToolsHelper.verifyRadioButtonSelected(mbean.getMBeanType(), nfwo));
        assertTrue(checkMBeanTypeButtons(nfwo, mbean));
        
        String name = mbean.getMBeanName();
        if (name.equals("ConstructTest11MBean") ||
                name.equals("ConstructTest7MBean") ||
                name.equals("ConstructTest3MBean")) {
            assertTrue(JellyToolsHelper.verifyCheckBoxEnabled("ImplementMBeanItf", nfwo));
            assertFalse(JellyToolsHelper.verifyCheckBoxSelected("ImplementMBeanItf", nfwo));
            assertFalse(JellyToolsHelper.verifyCheckBoxEnabled("PreRegisterParam", nfwo));
        } else {
            JellyToolsHelper.changeCheckBoxSelection("ImplementMBeanItf", nfwo, true);
            assertTrue(JellyToolsHelper.verifyCheckBoxSelected("ImplementMBeanItf", nfwo));
            assertTrue(JellyToolsHelper.verifyCheckBoxEnabled("PreRegisterParam", nfwo));
            assertFalse(JellyToolsHelper.verifyCheckBoxSelected("PreRegisterParam", nfwo));
        }
        
        JellyToolsHelper.setTextFieldContent("mbeanDescriptionJTextField", nfwo, 
                mbean.getMBeanComment());
        assertEquals(mbean.getMBeanComment(), JellyToolsHelper.getTextFieldContent(
                "mbeanDescriptionJTextField", nfwo));
    }
    
    private void attributeStep(NewFileWizardOperator nfwo, MBean mbean) {
        // attributes
        assertTrue(JellyToolsHelper.verifyTableEnabled("attributeTable",nfwo));
        assertTrue(JellyToolsHelper.verifyButtonEnabled("attrAddJButton",nfwo));
        assertFalse(JellyToolsHelper.verifyButtonEnabled("attrRemoveJButton",nfwo)); 
        
        JellyToolsHelper.fillMBeanAttributes(nfwo, mbean);
    }
 
    private void operationStep(NewFileWizardOperator nfwo, MBean mbean) {
      
        assertTrue(JellyToolsHelper.verifyTableEnabled("methodTable",nfwo));
        assertTrue(JellyToolsHelper.verifyButtonEnabled("methAddJButton",nfwo));
        assertFalse(JellyToolsHelper.verifyButtonEnabled("methRemoveJButton",nfwo));
        
        JellyToolsHelper.fillMBeanOperation(nfwo, mbean);
    }
   
    private void notificationStep(NewFileWizardOperator nfwo, MBean mbean) {
        
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled("implNotifEmitCheckBox", nfwo));
        JellyToolsHelper.changeCheckBoxSelection("implNotifEmitCheckBox", nfwo, true);
        assertTrue(JellyToolsHelper.verifyCheckBoxSelected("implNotifEmitCheckBox", nfwo));
        
        String name = mbean.getMBeanName();
        if (name.equals("ConstructTest11MBean") ||
                name.equals("ConstructTest7MBean") ||
                name.equals("ConstructTest3MBean")) {
                
            JellyToolsHelper.changeCheckBoxSelection("genDelegationCheckBox", nfwo, true);
            assertTrue(JellyToolsHelper.verifyCheckBoxSelected("genDelegationCheckBox", nfwo));
            JellyToolsHelper.changeCheckBoxSelection("genSeqNbCheckBox", nfwo, true);
            assertTrue(JellyToolsHelper.verifyCheckBoxSelected("genSeqNbCheckBox", nfwo));
        } else {
            assertFalse(JellyToolsHelper.verifyCheckBoxSelected("genDelegationCheckBox", nfwo));
            assertFalse(JellyToolsHelper.verifyCheckBoxSelected("genSeqNbCheckBox", nfwo));
        }
        
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled("genDelegationCheckBox", nfwo));
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled("genSeqNbCheckBox", nfwo));
        
        assertTrue(JellyToolsHelper.verifyTableEnabled("notificationTable",nfwo));
        assertTrue(JellyToolsHelper.verifyButtonEnabled("notifAddJButton",nfwo));
        assertFalse(JellyToolsHelper.verifyButtonEnabled("notifRemJButton",nfwo));
        
        JellyToolsHelper.fillMBeanNotification(nfwo, mbean);
    }
    
    private void junitStep(NewFileWizardOperator nfwo, MBean mbean) {
        JellyToolsHelper.changeCheckBoxSelection("junitJChckBox", nfwo, true);
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled("junitJChckBox", nfwo));
        assertTrue(JellyToolsHelper.verifyCheckBoxSelected("junitJChckBox", nfwo));
        
        assertTrue(JellyToolsHelper.verifyTextFieldEnabled("tfClassToTest", nfwo));
        assertFalse(JellyToolsHelper.verifyTextFieldEditable("tfClassToTest", nfwo));
        assertTrue(JellyToolsHelper.verifyTextFieldEnabled("tfTestClass", nfwo));
        assertFalse(JellyToolsHelper.verifyTextFieldEditable("tfTestClass", nfwo));
        
        String name = mbean.getMBeanName();
        if (name.equals("ConstructTest11MBean") ||
                name.equals("ConstructTest7MBean") ||
                name.equals("ConstructTest3MBean")) {
            assertTrue(JellyToolsHelper.verifyCheckBoxEnabled("defaultMethodBodyJCheckBox", nfwo));
            assertTrue(JellyToolsHelper.verifyCheckBoxSelected("defaultMethodBodyJCheckBox", nfwo));
            
            assertTrue(JellyToolsHelper.verifyCheckBoxEnabled("javaDocJCheckBox", nfwo));
            assertTrue(JellyToolsHelper.verifyCheckBoxSelected("javaDocJCheckBox", nfwo));
        } else {
            JellyToolsHelper.changeCheckBoxSelection("defaultMethodBodyJCheckBox", nfwo, false);
            assertTrue(JellyToolsHelper.verifyCheckBoxEnabled("defaultMethodBodyJCheckBox", nfwo));
            assertFalse(JellyToolsHelper.verifyCheckBoxSelected("defaultMethodBodyJCheckBox", nfwo));
            
            JellyToolsHelper.changeCheckBoxSelection("javaDocJCheckBox", nfwo, false);
            assertTrue(JellyToolsHelper.verifyCheckBoxEnabled("javaDocJCheckBox", nfwo));
            assertFalse(JellyToolsHelper.verifyCheckBoxSelected("javaDocJCheckBox", nfwo));
        }
    }
    
    private boolean checkMBeanTypeButtons(NewFileWizardOperator nfwo, MBean mbean) {
        String type = mbean.getMBeanType();
           if (type.equals("StandardMBean")) {
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected("ExtendedStandardMBean", nfwo));
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected("DynamicMBean", nfwo));
                return true;
           } else if (type.equals("ExtendedStandardMBean")) {
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected("StandardMBean", nfwo));
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected("DynamicMBean", nfwo));
                return true;
        } else if (type.equals("DynamicMBean")) {
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected("StandardMBean", nfwo));
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected("ExtendedStandardMBean", nfwo));
                return true;
        }
        return false;
    }
     
}
