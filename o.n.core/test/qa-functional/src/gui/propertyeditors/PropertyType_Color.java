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

import org.netbeans.test.oo.gui.jelly.propertyEditors.customizers.ColorCustomizer;

import org.netbeans.junit.NbTestSuite;


/**
 * Tests of Color Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_Color extends PropertyEditorsTest {
    
    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;
    
    public boolean waitDialog = false;
    
    /** Creates a new instance of PropertyType_Boolean */
    public PropertyType_Color(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "p_color";
        useForm = true;
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_Color("testCustomizerCancel"));
        suite.addTest(new PropertyType_Color("testCustomizerOk"));
        suite.addTest(new PropertyType_Color("testByInPlace"));
        suite.addTest(new PropertyType_Color("testByInPlaceInvalid"));
        return suite;
    }
    
    public void testCustomizerOk() {
        propertyValue_L = "50,50,50";
        propertyValueExpectation_L = "["+propertyValue_L+"]";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        propertyValue_L = "100,100,100";
        propertyValueExpectation_L = "["+propertyValue_L+"]";
        waitDialog = false;
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void testByInPlace(){
        propertyValue_L = "20,10,100";
        propertyValueExpectation_L = "["+propertyValue_L+"]";
        waitDialog = false;
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void testByInPlaceInvalid(){
        propertyValue_L = "xx color";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = true;
        lastTest = true;
        setByInPlace(propertyName_L, propertyValue_L, false);
    }
    
    public void setCustomizerValue() {
        ColorCustomizer customizer = new ColorCustomizer(propertyCustomizer);
        
        int first_comma = propertyValue_L.indexOf(',');
        int second_comma = propertyValue_L.indexOf(',',first_comma+1);
        String red = propertyValue_L.substring(0,first_comma);
        String green = propertyValue_L.substring(first_comma+1, second_comma);
        String blue = propertyValue_L.substring(second_comma+1);
        
        customizer.setValue(red,green,blue);
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
    }
    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_Color.class));
        junit.textui.TestRunner.run(suite());
    }
    
    
}
