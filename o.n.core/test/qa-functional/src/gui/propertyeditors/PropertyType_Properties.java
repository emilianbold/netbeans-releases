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

import org.netbeans.jellytools.properties.editors.StringCustomEditorOperator;

import org.netbeans.jemmy.operators.JEditorPaneOperator;

import org.netbeans.junit.NbTestSuite;

/**
 * Tests of Properties Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_Properties extends PropertyEditorsTest {
    
    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;
    

    public boolean waitDialog = false;
    
    /** Creates a new instance of PropertyType_Properties */
    public PropertyType_Properties(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "property_Properties";
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_Properties("testByInPlace"));
        suite.addTest(new PropertyType_Properties("verifyCustomizer"));
        suite.addTest(new PropertyType_Properties("testCustomizerOk"));
        suite.addTest(new PropertyType_Properties("testCustomizerCancel"));
        return suite;
    }
    
    public void testCustomizerOk() {
        propertyValue_L = "propertyName1=propertyValue1";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        propertyValue_L = "pp=xx";
        waitDialog = false;
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void testByInPlace(){
        propertyValue_L = "propertyName=propertyValue";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;                                     
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void verifyCustomizer() {
        verifyCustomizer(propertyName_L);
    }
    
    public void setCustomizerValue() {
        StringCustomEditorOperator customizer = new StringCustomEditorOperator(propertyCustomizer);
        //new EventTool().waitNoEvent(3000);
        //customizer.setStringValue(propertyValue_L);
        new JEditorPaneOperator(customizer).setText(propertyValue_L);
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
    }
    
    public void verifyCustomizerLayout() {
        StringCustomEditorOperator customizer = new StringCustomEditorOperator(propertyCustomizer);
        new JEditorPaneOperator(customizer);
        customizer.btOK();
        customizer.btCancel();
    }    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_Properties.class));
        junit.textui.TestRunner.run(suite());
    }
    
}
