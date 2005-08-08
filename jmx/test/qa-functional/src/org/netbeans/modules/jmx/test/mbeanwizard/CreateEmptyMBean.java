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
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.jmx.test.helpers.JellyToolsHelper;
import org.netbeans.modules.jmx.test.helpers.MBean;



/**
 *
 * @author an156382
 */
public class CreateEmptyMBean extends JellyTestCase {
    
    public final String PROJECT_NAME = "MBeanFunctionalTest";
    
    /** Need to be defined because of JUnit */
    public CreateEmptyMBean(String name) {
        super(name);
    }
    
    
    public static NbTestSuite suite() {
        
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CreateEmptyMBean("constructTest1MBean"));
        suite.addTest(new CreateEmptyMBean("constructTest5MBean"));
        suite.addTest(new CreateEmptyMBean("constructTest9MBean"));
        
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
    
    public void constructTest1MBean() {
        
        MBean standardMBean = createEmptyStandardMBean();
        wizardExecution(standardMBean);  
        //TODO Diff between generated and master files
    }
    
    public void constructTest5MBean() {
        
        MBean extendedMBean = createEmptyExtendedStandardMBean();
        wizardExecution(extendedMBean);
        
        //TODO Diff between generated and master files
        
    }
    
    public void constructTest9MBean() {
        
        MBean dynamicMBean = createEmptyDynamicMBean();
        wizardExecution(dynamicMBean);
        
        //TODO Diff between generated and master files
        
    }
    
    /****************************************************************************/
    /************************* SPECIRFIC HELPER METHODS *************************/
    /****************************************************************************/
    
    //========================= WizardExecution =================================//
    
    private void wizardExecution(MBean mbean) {
        
        NewFileWizardOperator nfwo = JellyToolsHelper.init("JMX MBean", 
                mbean.getMBeanName(), mbean.getMBeanPackage());
        nfwo.next();
        
        optionStep(nfwo, mbean);
        nfwo.next();
        
        attributeStep(nfwo);
        nfwo.next();
       
        operationStep(nfwo);
        nfwo.next();
        
        notificationStep(nfwo);
        nfwo.next();
        
        junitStep(nfwo);
        nfwo.finish();
    }
    
    //========================= Class Name generation ===========================//
    
    /**
     * Functional test which creates an empty fake standardMBean and gives it to the MBean wizard execution
     *
     */
    private MBean createEmptyStandardMBean() {
        return new MBean("ConstructTest1MBean", "StandardMBean", "com.foo.bar", 
                "StandardMBean without attributes, operations and notifications", null, null, null); 
    }
    
    public MBean createEmptyExtendedStandardMBean() {
        return new MBean("ConstructTest5Mbean", "ExtendedStandardMBean", 
                "com.foo.bar", 
                "ExtendedStandardMBean without attributes, operations and notifications",
                 null, null, null);
    }
    
    private MBean createEmptyDynamicMBean() {
        return new MBean("ConstructTest9Mbean", "DynamicMBean", 
                "com.foo.bar", 
                "DynamicMBean without attributes, operations and notifications",
                 null, null, null);
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
        
        assertFalse(JellyToolsHelper.verifyCheckBoxSelected("ImplementMBeanItf", nfwo));
        assertFalse(JellyToolsHelper.verifyCheckBoxEnabled("PreRegisterParam", nfwo));
        
        JellyToolsHelper.setTextFieldContent("mbeanDescriptionJTextField", nfwo, 
                mbean.getMBeanComment());
        assertEquals(mbean.getMBeanComment(), JellyToolsHelper.getTextFieldContent(
                "mbeanDescriptionJTextField", nfwo));
    }
    
    private void attributeStep(NewFileWizardOperator nfwo) {
        // attributes
        assertTrue(JellyToolsHelper.verifyTableEnabled("attributeTable",nfwo));
        assertTrue(JellyToolsHelper.verifyButtonEnabled("attrAddJButton",nfwo));
        assertFalse(JellyToolsHelper.verifyButtonEnabled("attrRemoveJButton",nfwo)); 
    }
        
    private void operationStep(NewFileWizardOperator nfwo) {
        // operations
        assertTrue(JellyToolsHelper.verifyTableEnabled("methodTable",nfwo));
        assertTrue(JellyToolsHelper.verifyButtonEnabled("methAddJButton",nfwo));
        assertFalse(JellyToolsHelper.verifyButtonEnabled("methRemoveJButton",nfwo));
    }
    
    private void notificationStep(NewFileWizardOperator nfwo) {
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled("implNotifEmitCheckBox", nfwo));
        assertFalse(JellyToolsHelper.verifyCheckBoxSelected("implNotifEmitCheckBox", nfwo));
        
        assertFalse(JellyToolsHelper.verifyCheckBoxEnabled("genDelegationCheckBox", nfwo));
        assertFalse(JellyToolsHelper.verifyCheckBoxEnabled("genSeqNbCheckBox", nfwo));
        
        assertFalse(JellyToolsHelper.verifyTableEnabled("notificationTable",nfwo));
        assertFalse(JellyToolsHelper.verifyButtonEnabled("notifAddJButton",nfwo));
        assertFalse(JellyToolsHelper.verifyButtonEnabled("notifRemJButton",nfwo));
    }
    
    private void junitStep(NewFileWizardOperator nfwo) {
        
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled("junitJChckBox", nfwo));
        assertFalse(JellyToolsHelper.verifyCheckBoxSelected("junitJChckBox", nfwo));
        
        assertFalse(JellyToolsHelper.verifyTextFieldEditable("tfClassToTest", nfwo));
        assertFalse(JellyToolsHelper.verifyTextFieldEditable("tfTestClass", nfwo));
        
        assertFalse(JellyToolsHelper.verifyCheckBoxEnabled("defaultMethodBodyJCheckBox", nfwo));
        assertTrue(JellyToolsHelper.verifyCheckBoxSelected("defaultMethodBodyJCheckBox", nfwo));
        
        assertFalse(JellyToolsHelper.verifyCheckBoxEnabled("javaDocJCheckBox", nfwo));
        assertTrue(JellyToolsHelper.verifyCheckBoxSelected("javaDocJCheckBox", nfwo));
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
