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

import org.netbeans.test.oo.gui.jelly.propertyEditors.customizers.ServicesCustomizer;

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
        propertyName_L = "p_executor";
        useForm = false;
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_Executor("testByCombo"));
        suite.addTest(new PropertyType_Executor("testCustomizerOk"));
        suite.addTest(new PropertyType_Executor("testCustomizerCancel"));
        return suite;
    }
    
    public void testByCombo(){
        propertyValue_L = "External Execution";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;                                     
        setByCombo(propertyName_L, propertyValue_L, true);
    }
    
    public void testCustomizerOk(){
        propertyValue_L = "(do not execute)";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;                                     
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        propertyValue_L = "Internal Execution";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;                                     
        lastTest = true;
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void setCustomizerValue() {
        ServicesCustomizer customizer = new ServicesCustomizer(propertyCustomizer);
        customizer.selectItem(propertyValue_L);
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, false);
    }
    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_Executor.class));
        junit.textui.TestRunner.run(suite());
    }
    
    
}
