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

import org.netbeans.test.oo.gui.jelly.propertyEditors.customizers.RectangleInsetsCustomizer;

import org.netbeans.junit.NbTestSuite;



/**
 * Tests of Rectangle Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_Rectangle extends PropertyEditorsTest {
    
    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;
    
    public boolean waitDialog = false;
    
    /** Creates a new instance of PropertyType_Rectangle */
    public PropertyType_Rectangle(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "p_rectangle";
        useForm = true;
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_Rectangle("testCustomizerCancel"));
        suite.addTest(new PropertyType_Rectangle("testCustomizerOk"));
        suite.addTest(new PropertyType_Rectangle("testByInPlace"));
        suite.addTest(new PropertyType_Rectangle("testByInPlaceOneValue"));
        suite.addTest(new PropertyType_Rectangle("testByInPlaceInvalid"));
        suite.addTest(new PropertyType_Rectangle("testCustomizerInvalid"));
        return suite;
    }
    
    public void testCustomizerOk() {
        propertyValue_L = "10, 20, 30, 40";
        propertyValueExpectation_L = "["+propertyValue_L+"]";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        propertyValue_L = "100, 100, 200, 200";
        propertyValueExpectation_L = "["+propertyValue_L+"]";
        waitDialog = false;
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void testCustomizerInvalid(){
        propertyValue_L = "xx, 20, 30, 50";
        propertyValueExpectation_L = "["+propertyValue_L+"]";
        waitDialog = true;
        lastTest = true;
        setByCustomizerOk(propertyName_L, false);
    }
    
    public void testByInPlace(){
        propertyValue_L = "30, 40, 50, 60";
        propertyValueExpectation_L = "["+propertyValue_L+"]";
        waitDialog = false;
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void testByInPlaceOneValue(){
        propertyValue_L = "70";
        propertyValueExpectation_L = "["+propertyValue_L+", "+propertyValue_L+", "+propertyValue_L+", "+propertyValue_L+"]";
        waitDialog = false;
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void testByInPlaceInvalid(){
        propertyValue_L = "xx";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = true;
        setByInPlace(propertyName_L, propertyValue_L, false);
    }
    
    public void setCustomizerValue() {
        RectangleInsetsCustomizer customizer = new RectangleInsetsCustomizer(propertyCustomizer);
        customizer.setValue(propertyValue_L);
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
    }
    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_Rectangle.class));
        junit.textui.TestRunner.run(suite());
    }
    
    
}
