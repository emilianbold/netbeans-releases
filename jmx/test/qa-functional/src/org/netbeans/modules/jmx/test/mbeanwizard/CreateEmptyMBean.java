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
import org.netbeans.modules.jmx.test.helpers.MBeanExecutionHelper;
import org.netbeans.modules.jmx.test.helpers.MBean;
import org.netbeans.modules.jmx.test.helpers.JellyConstants;
import java.util.ArrayList;
import java.util.Properties;


/**
 *
 * @author an156382
 */
public class CreateEmptyMBean extends JellyTestCase {
    
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
        JellyToolsHelper.grabProjectNode(JellyConstants.PROJECT_NAME);
    }
    
    public void tearDown() {
        
    }
    
    public void constructTest1MBean() {
        
        MBean standardMBean = createEmptyStandardMBean();
        wizardExecution(standardMBean);  
    }
    
    public void constructTest5MBean() {
        
        MBean extendedMBean = createEmptyExtendedStandardMBean();
        wizardExecution(extendedMBean);
    }
    
    public void constructTest9MBean() {
        
        MBean dynamicMBean = createEmptyDynamicMBean();
        wizardExecution(dynamicMBean);
    }
    
    /****************************************************************************/
    /************************* SPECIRFIC HELPER METHODS *************************/
    /****************************************************************************/
    
    //========================= WizardExecution =================================//
    
    private void wizardExecution(MBean mbean) {
        
        NewFileWizardOperator nfwo = JellyToolsHelper.init(JellyConstants.CATEGORY, 
                mbean.getMBeanName(), mbean.getMBeanPackage());
        nfwo.next();
        
        ArrayList<String> fileNames = optionStep(nfwo, mbean);
        nfwo.next();
        
        attributeStep(nfwo);
        nfwo.next();
       
        operationStep(nfwo);
        nfwo.next();
        
        notificationStep(nfwo);
        nfwo.next();
        
        junitStep(nfwo);
        
        nfwo.finish();
        
        assertTrue(JellyToolsHelper.diffOK(fileNames, null, mbean)); 
    }
    
    //========================= Class Name generation ===========================//
    
    /**
     * Functional test which creates an empty fake standardMBean and gives it to the MBean wizard execution
     *
     */
    private MBean createEmptyStandardMBean() {
        return new MBean(JellyConstants.MBEAN_ONE, JellyConstants.STDMBEAN,  
                JellyConstants.PACKAGE_NAME,  
                JellyConstants.MBEAN_ONE_COMMENT,
                null, null, null); 
    }
    
    public MBean createEmptyExtendedStandardMBean() {
        return new MBean(JellyConstants.MBEAN_FIVE, JellyConstants.EXTSTDMBEAN, 
                JellyConstants.PACKAGE_NAME,  
                JellyConstants.MBEAN_FIVE_COMMENT,
                null, null, null);
    }
    
    private MBean createEmptyDynamicMBean() {
        return new MBean(JellyConstants.MBEAN_NINE, JellyConstants.DYNMBEAN, 
                JellyConstants.PACKAGE_NAME,  
                JellyConstants.MBEAN_NINE_COMMENT,
                null, null, null);
    }
    
    //========================= Class Name generation ===========================//
    
    private String getCompleteGeneratedFileName(NewFileWizardOperator nfwo) {
        return JellyToolsHelper.getTextFieldContent(JellyConstants.GENFILE_TXT, nfwo);
    }
    
    private String getClassName(String completeFileName, String mbeanName) {
        return JellyToolsHelper.replaceMBeanClassName(completeFileName, mbeanName+JellyConstants.JAVA_EXT); 
    }
    
    private String getInterfaceName(String completeFileName) {
        String itfWithExtension = completeFileName.substring(
                completeFileName.lastIndexOf(File.separatorChar)+1);
        return itfWithExtension.substring(0, itfWithExtension.lastIndexOf('.'));
    }
    
    //========================= Panel discovery ==================================//
    
    private ArrayList<String> optionStep(NewFileWizardOperator nfwo, MBean mbean) {
        
        ArrayList<String> fileNames = new ArrayList<String>();
        
        assertFalse(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.EXISTINGCLASS_CBX, nfwo));
        assertFalse(JellyToolsHelper.verifyTextFieldEnabled(JellyConstants.EXISTINGCLASS_TXT, nfwo));
        
        JellyToolsHelper.changeRadioButtonSelection(mbean.getMBeanType(), nfwo, true);
        assertTrue(JellyToolsHelper.verifyRadioButtonSelected(mbean.getMBeanType(), nfwo));
        assertTrue(checkMBeanTypeButtons(nfwo, mbean));
        
        assertFalse(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.IMPLEMMBEAN_CBX, nfwo));
        assertFalse(JellyToolsHelper.verifyCheckBoxEnabled(JellyConstants.PREREGPARAM_CBX, nfwo));
        
        JellyToolsHelper.setTextFieldContent(JellyConstants.MBEANDESCR_TXT, nfwo, 
                mbean.getMBeanComment());
        assertEquals(mbean.getMBeanComment(), JellyToolsHelper.getTextFieldContent(
                JellyConstants.MBEANDESCR_TXT, nfwo));
        
        // get the generated file name for campare with master files
        String completeGeneratedFileName = getCompleteGeneratedFileName(nfwo);
        String className = getClassName(completeGeneratedFileName, mbean.getMBeanName());
        String itfName = getInterfaceName(completeGeneratedFileName);
        
        fileNames.add(completeGeneratedFileName);
        fileNames.add(className);
        fileNames.add(itfName);
        
        
        return fileNames;
    }
    
    private void attributeStep(NewFileWizardOperator nfwo) {
        // attributes
        assertTrue(JellyToolsHelper.verifyTableEnabled(JellyConstants.ATTR_TBL,nfwo));
        assertTrue(JellyToolsHelper.verifyButtonEnabled(JellyConstants.ATTR_ADD_BTN,nfwo));
        assertFalse(JellyToolsHelper.verifyButtonEnabled(JellyConstants.ATTR_REM_BTN,nfwo)); 
    }
        
    private void operationStep(NewFileWizardOperator nfwo) {
        // operations
        assertTrue(JellyToolsHelper.verifyTableEnabled(JellyConstants.OPER_TBL,nfwo));
        assertTrue(JellyToolsHelper.verifyButtonEnabled(JellyConstants.OPER_ADD_BTN,nfwo));
        assertFalse(JellyToolsHelper.verifyButtonEnabled(JellyConstants.OPER_REM_BTN,nfwo));
    }
    
    private void notificationStep(NewFileWizardOperator nfwo) {
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled(JellyConstants.IMPLNOTIFEMIT_CBX, nfwo));
        assertFalse(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.IMPLNOTIFEMIT_CBX, nfwo));
        
        assertFalse(JellyToolsHelper.verifyCheckBoxEnabled(JellyConstants.GENDELEG_CBX, nfwo));
        assertFalse(JellyToolsHelper.verifyCheckBoxEnabled(JellyConstants.GENSEQNUM_CBX, nfwo));
        
        assertFalse(JellyToolsHelper.verifyTableEnabled(JellyConstants.NOTIF_TBL,nfwo));
        assertFalse(JellyToolsHelper.verifyButtonEnabled(JellyConstants.NOTIF_ADD_BTN,nfwo));
        assertFalse(JellyToolsHelper.verifyButtonEnabled(JellyConstants.NOTIF_REM_BTN,nfwo));
    }
    
    private void junitStep(NewFileWizardOperator nfwo) {
        
        ArrayList<String> unitFileNames = new ArrayList<String>();
        
        assertTrue(JellyToolsHelper.verifyCheckBoxEnabled(JellyConstants.JU_CBX, nfwo));
        assertFalse(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.JU_CBX, nfwo));
        
        assertFalse(JellyToolsHelper.verifyTextFieldEditable(JellyConstants.CLASSTOTEST_TXT, nfwo));
        assertFalse(JellyToolsHelper.verifyTextFieldEditable(JellyConstants.TESTCLASS_TXT, nfwo));
        
        assertFalse(JellyToolsHelper.verifyCheckBoxEnabled(JellyConstants.DEFMETHBODY_CBX, nfwo));
        assertTrue(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.DEFMETHBODY_CBX, nfwo));
        
        assertFalse(JellyToolsHelper.verifyCheckBoxEnabled(JellyConstants.JAVADOC_CBX, nfwo));
        assertTrue(JellyToolsHelper.verifyCheckBoxSelected(JellyConstants.JAVADOC_CBX, nfwo));
    }
    
    private boolean checkMBeanTypeButtons(NewFileWizardOperator nfwo, MBean mbean) {
        String type = mbean.getMBeanType();
           if (type.equals(JellyConstants.STDMBEAN)) {
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected(JellyConstants.EXTSTDMBEAN, nfwo));
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected(JellyConstants.DYNMBEAN, nfwo));
                return true;
           } else if (type.equals(JellyConstants.EXTSTDMBEAN)) {
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected(JellyConstants.STDMBEAN, nfwo));
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected(JellyConstants.DYNMBEAN, nfwo));
                return true;
        } else if (type.equals(JellyConstants.DYNMBEAN)) {
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected(JellyConstants.STDMBEAN, nfwo));
                assertFalse(JellyToolsHelper.verifyRadioButtonSelected(JellyConstants.EXTSTDMBEAN, nfwo));
                return true;
        }
        return false;
    }
            
}
