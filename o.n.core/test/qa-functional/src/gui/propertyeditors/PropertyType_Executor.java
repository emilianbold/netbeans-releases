/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.propertyeditors;

import org.netbeans.jellytools.properties.editors.ServiceTypeCustomEditorOperator;

import org.netbeans.junit.NbTestSuite;

/**
 * Tests of Executor Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_Executor extends PropertyEditorsTest {
    
    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;
    
    public boolean waitDialog = false;
    
    /** Creates a new instance of PropertyType_Executor */
    public PropertyType_Executor(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "Executor";
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_Executor("testByCombo"));
        suite.addTest(new PropertyType_Executor("verifyCustomizer"));
        suite.addTest(new PropertyType_Executor("testCustomizerOk"));
        suite.addTest(new PropertyType_Executor("testCustomizerCancel"));
        return suite;
    }
    
    public void testByCombo(){
        propertyValue_L = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.execution.beaninfo.editors.Bundle", "LAB_NoExecutor"); // (do not execute)
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;                                     
        setByCombo(propertyName_L, propertyValue_L, true);
    }
    
    public void testCustomizerOk(){
        propertyValue_L = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.execution.beaninfo.Bundle", "CTL_ProcessExecutor"); // External Execution
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;                                     
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        propertyValue_L = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.execution.beaninfo.Bundle", "CTL_ThreadExecutor"); // Internal Execution
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;                                     
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void testCustomizerOk_platform(){
        propertyValue_L = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.execution.beaninfo.editors.Bundle", "LAB_NoExecutor"); // (do not execute)
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;                                     
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel_platform(){
        propertyValue_L = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.execution.beaninfo.editors.Bundle", "LAB_NoExecutor"); // (do not execute)
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;                                     
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void verifyCustomizer() {
        verifyCustomizer(propertyName_L);
    }
    
    public void setCustomizerValue() {
        ServiceTypeCustomEditorOperator customizer = new ServiceTypeCustomEditorOperator(propertyCustomizer);
        customizer.setServiceTypeValue(propertyValue_L);
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
    }
    
    public void verifyCustomizerLayout() {
        ServiceTypeCustomEditorOperator customizer = new ServiceTypeCustomEditorOperator(propertyCustomizer);
        customizer.lstServices();
        customizer.propertySheet();
        customizer.btOK();
        customizer.btCancel();
    }    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_Executor.class));
        junit.textui.TestRunner.run(suite());
    }
    
}
