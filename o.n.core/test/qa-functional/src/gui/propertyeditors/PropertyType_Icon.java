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

import org.netbeans.test.oo.gui.jelly.propertyEditors.customizers.IconCustomizer;

import org.netbeans.junit.NbTestSuite;



/**
 * Tests of Icon Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_Icon extends PropertyEditorsTest {
    
    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;
    
    public boolean waitDialog = false;
    
    /** Creates a new instance of PropertyType_Icon */
    public PropertyType_Icon(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "p_icon";
        useForm = true;
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_Icon("testCustomizerCancel"));
        suite.addTest(new PropertyType_Icon("testCustomizerOkURL"));
        suite.addTest(new PropertyType_Icon("testCustomizerOkFile"));
        suite.addTest(new PropertyType_Icon("testCustomizerOkClasspath"));
        suite.addTest(new PropertyType_Icon("testCustomizerOkNoPicture"));
        suite.addTest(new PropertyType_Icon("testByInPlace"));
        suite.addTest(new PropertyType_Icon("testByInPlaceInvalid"));
        //        suite.addTest(new PropertyType_Icon("testCustomizerInvalid"));
        return suite;
    }
    
    
    public void testCustomizerOkURL() {
        propertyValue_L = "URL: http://www.netbeans.org/1.gif";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerOkFile() {
        propertyValue_L = "File: /home/mm119185/samplxxxxeDir.gif";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }

    public void testCustomizerOkClasspath() {
        propertyValue_L = "Classpath: /gui/propertyEditors/data/ColorPreview.gif";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerOkNoPicture() {
        propertyValue_L = "No Picture";
        propertyValueExpectation_L = "null";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        propertyValue_L = "URL: http://www.netbeans.org/2.gif";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCustomizerCancel(propertyName_L, false);
    }
    
/*    public void testCustomizerInvalid(){
        propertyValue_L = "xx";
        propertyValueExpectation_L = "File: "+propertyValue_L;
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
*/    
    public void testByInPlace(){
        propertyValue_L = "Classpath: /trash/PropertyEditorsTest.java";;
        propertyValueExpectation_L = "Invalid value " + propertyValue_L;
        waitDialog = false;
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void testByInPlaceInvalid(){
        propertyValue_L = "xx";
        propertyValueExpectation_L = "File: " +propertyValue_L;
        waitDialog = false;
        lastTest = true;
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void setCustomizerValue() {
        IconCustomizer customizer = new IconCustomizer(propertyCustomizer);
        customizer.setValue(propertyValue_L);
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
    }
    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_Icon.class));
        junit.textui.TestRunner.run(suite());
    }
    
    
}
