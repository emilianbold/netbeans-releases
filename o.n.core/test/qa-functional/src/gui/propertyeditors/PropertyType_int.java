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

import org.netbeans.junit.NbTestSuite;


/**
 * Tests of int Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_int extends PropertyEditorsTest {
    
    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;
    
    public boolean waitDialog = false;
    
    /** Creates a new instance of PropertyType_int */
    public PropertyType_int(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "p_int";
        useForm = true;
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_int("testByInPlace"));
        suite.addTest(new PropertyType_int("testByInPlaceInvalid"));
        return suite;
    }
    
    public void testByInPlace(){
        propertyValue_L = "-1234567890";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void testByInPlaceInvalid(){
        propertyValue_L = "1.0";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = true;
        lastTest = true;
        setByInPlace(propertyName_L, propertyValue_L, false);
    }
    
    public void setCustomizerValue() {
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
    }
    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_int.class));
        junit.textui.TestRunner.run(suite());
    }
    
    
}
