/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.propertyeditors;

import org.netbeans.junit.NbTestSuite;

/**
 * Tests of boolean Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_boolean extends PropertyEditorsTest {
    
    public String propertyName_L;
    public String propertyValue_L;
    
    /** Creates a new instance of PropertyType_boolean */
    public PropertyType_boolean(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "boolean";
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_boolean("testByComboFalse"));
        suite.addTest(new PropertyType_boolean("testByComboTrue"));
        return suite;
    }
    
    public void testByComboFalse(){
        propertyValue_L = Boolean.FALSE.toString();
        setByCombo(propertyName_L, propertyValue_L, true);
    }
    
    public void testByComboTrue(){
        propertyValue_L = Boolean.TRUE.toString();
        setByCombo(propertyName_L, propertyValue_L, true);
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValue_L, propertyValue_L, false);
    }
    
    public void setCustomizerValue(){}
    public void verifyCustomizerLayout(){}
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_boolean.class));
        junit.textui.TestRunner.run(suite());
    }
}
