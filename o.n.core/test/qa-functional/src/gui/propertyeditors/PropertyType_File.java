/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.propertyeditors;

import gui.propertyeditors.utilities.CoreSupport;

import org.netbeans.jellytools.properties.editors.FileCustomEditorOperator;

import org.netbeans.jemmy.EventTool;

import org.netbeans.junit.NbTestSuite;


/**
 * Tests of File Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_File extends PropertyEditorsTest {
    
    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;
    
    public boolean waitDialog = false;
    
    /** Creates a new instance of PropertyType_Boolean */
    public PropertyType_File(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "File";
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_File("verifyCustomizer"));
        suite.addTest(new PropertyType_File("testByInPlace"));
        suite.addTest(new PropertyType_File("testCustomizerCancel"));
        suite.addTest(new PropertyType_File("testCustomizerOk"));
        return suite;
    }
    
    public void testCustomizerOk() {
        
        //TODO write new way for promoD
        //propertyValue_L = CoreSupport.getSystemPath("gui/data", "clear_JFrame", "java");
        propertyValue_L = ""; 
        
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        
        //TODO write new way for promoD
        //propertyValue_L = CoreSupport.getSystemPath("gui/data", "clear_Frame", "java");
        propertyValue_L = ""; 
        
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void testByInPlace(){
        
        //TODO write new way for promoD
        //propertyValue_L = CoreSupport.getSystemPath("gui/data", CoreSupport.beanName, "java");
        propertyValue_L = ""; 
        
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void verifyCustomizer() {
        verifyCustomizer(propertyName_L);
    }
    
    public void setCustomizerValue() {
        FileCustomEditorOperator customizer = new FileCustomEditorOperator(propertyCustomizer);
        new EventTool().waitNoEvent(1000);
        customizer.setFileValue(propertyValue_L); 
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
    }
    
    public void verifyCustomizerLayout() {
        FileCustomEditorOperator customizer = new FileCustomEditorOperator(propertyCustomizer);
        customizer.verify();
        customizer.btOK();
        customizer.btCancel();
    }    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_File.class));
        junit.textui.TestRunner.run(suite());
    }
}
