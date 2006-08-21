/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.propertyeditors;

import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;

import org.netbeans.jellytools.properties.editors.IconCustomEditorOperator;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;

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
    
    private final String FILE = "File: ";
    private final String CLASSPATH = "Classpath: ";
    private final String URL = "URL: ";
    private final String NO_PICTURE = "No Picture";
    
    public boolean waitDialog = false;
    
    /** Creates a new instance of PropertyType_Icon */
    public PropertyType_Icon(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "Icon";
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_Icon("verifyCustomizer"));
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
        propertyValue_L = URL + "http://www.netbeans.org/1.gif";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerOkFile() {
        propertyValue_L = FILE + "/home/mm119185/samplxxxxeDir.gif";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerOkClasspath() {
        propertyValue_L = CLASSPATH + "/gui/propertyeditors/data/ColorPreview.gif";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerOkNoPicture() {
        propertyValue_L = NO_PICTURE;
        propertyValueExpectation_L = "null";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        propertyValue_L = URL + "http://www.netbeans.org/2.gif";
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
        propertyValue_L = CLASSPATH +"/trash/PropertyEditorsTest.java";;
        propertyValueExpectation_L = "Invalid value " + propertyValue_L;
        waitDialog = false;
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void testByInPlaceInvalid(){
        propertyValue_L = "xx";
        propertyValueExpectation_L = "File: " +propertyValue_L;
        waitDialog = false;
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void verifyCustomizer() {
        verifyCustomizer(propertyName_L);
    }
    
    public void setCustomizerValue() {
        IconCustomEditorOperator customizer = new IconCustomEditorOperator(propertyCustomizer);
        
        String type;
        String path;
        int delim_index = propertyValue_L.indexOf(": ");
        
        if(delim_index>0){
            type = propertyValue_L.substring(0,delim_index+2);
            path = propertyValue_L.substring(delim_index+1).trim();
            
            if(type.equalsIgnoreCase(NO_PICTURE)){
                customizer.noPicture();
            }else if(type.equalsIgnoreCase(URL)){
                customizer.uRL();
                customizer.setName(path);
            }else if(type.equalsIgnoreCase(FILE)){
                customizer.file();
                customizer.setName(path);
            }else if(type.equalsIgnoreCase(CLASSPATH)){
                customizer.classpath();
                //customizer.setName(path); - hack because setName doesn't push Enter on the end of action
                customizer.txtName().enterText(path);
                new EventTool().waitNoEvent(6000);
            }else {
                throw new JemmyException("ERROR: value is (\""+propertyValue_L+"\") - wrong format or unknown source type!!! type=["+type+"]/path=["+path+"]");
            }
            
        }else{
            customizer.noPicture();
        }
        
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
    }
    
    
    public String getValue(String property) {
        String returnValue;
        PropertySheetOperator propertiesTab = new PropertySheetOperator(propertiesWindow);
        
        returnValue = new Property(propertiesTab, property).getValue();
        err.println("X GET VALUE = [" + returnValue + "].");
        
        // hack for icon poperty, this action expects, that right value is displayed (with label "Invalid value") as Accessible Name
        returnValue = new Property(propertiesTab, property).getValue();
        returnValue = returnValue.substring(returnValue.indexOf(property)+property.length()+2);
        err.println("X GET VALUE ACCESSIBLE NAME = [" + returnValue + "].");
        
        return returnValue;
    }
    
    public void verifyCustomizerLayout() {
        IconCustomEditorOperator customizer = new IconCustomEditorOperator(propertyCustomizer);
        customizer.verify();
        customizer.btOK();
        customizer.btCancel();
    }    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_Icon.class));
        junit.textui.TestRunner.run(suite());
    }
    
}
