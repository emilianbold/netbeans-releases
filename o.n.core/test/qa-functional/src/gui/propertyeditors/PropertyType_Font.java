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

import java.util.StringTokenizer;
import org.netbeans.jellytools.properties.FontProperty;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jellytools.properties.PropertySheetTabOperator;

import org.netbeans.jellytools.properties.editors.FontCustomEditorOperator;

import org.netbeans.jemmy.JemmyException;

import org.netbeans.junit.NbTestSuite;

/**
 * Tests of  Font Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_Font extends PropertyEditorsTest {
    
    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;
    
    public boolean waitDialog = false;
    
    /** Creates a new instance of PropertyType_Font */
    public PropertyType_Font(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "Font";
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_Font("verifyCustomizer"));
        suite.addTest(new PropertyType_Font("testCustomizerCancel"));
        suite.addTest(new PropertyType_Font("testCustomizerOk"));
        suite.addTest(new PropertyType_Font("testCustomizerOkUnknownSize"));
        suite.addTest(new PropertyType_Font("testCustomizerInvalid"));
        return suite;
    }
    
    public void testCustomizerOk() {
        propertyValue_L = "Monospaced, 10, Bold";
        propertyValueExpectation_L = "Monospaced 10 Bold";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerOkUnknownSize() {
        propertyValue_L = "Monospaced, 13, Bold";
        propertyValueExpectation_L = "Monospaced 13 Bold";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        propertyValue_L = "Monospaced, 100, Bold";
        propertyValueExpectation_L = "Monospaced 100 Bold";
        waitDialog = false;
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void testCustomizerInvalid(){
        propertyValue_L = "Monospaced, xx, Bold Italic";
        propertyValueExpectation_L = "Monospaced xx Bold Italic";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, false);
    }
    
    public void verifyCustomizer() {
        verifyCustomizer(propertyName_L);
    }
    
    public void setCustomizerValue() {
        FontCustomEditorOperator customizer = new FontCustomEditorOperator(propertyCustomizer);
        
        int index1,index2,index3;
        index1 = propertyValue_L.indexOf(", ");
        
        if(index1>0){
            customizer.setFontName(propertyValue_L.substring(0,index1).trim());
            index2 = propertyValue_L.indexOf(", ", index1+1);
            
            if(index2>0){
                customizer.setFontSize(propertyValue_L.substring(index1+1,index2).trim());
                customizer.setFontStyle(propertyValue_L.substring(index2+1).trim());
            }
        }
        
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
    }
    
    public String getValue(String propertyName) {
        String returnValue;
        PropertySheetTabOperator propertiesTab = new PropertySheetTabOperator(new PropertySheetOperator(propertiesWindow));
        
        returnValue = new FontProperty(propertiesTab, propertyName_L).getValue();
        err.println("GET VALUE = [" + returnValue + "].");
        
        // hack for color poperty, this action expects, that right value is displayed as tooltip
//        returnValue = new Property(propertiesTab, propertyName_L).valueButtonOperator().getToolTipText();
//        err.println("GET VALUE TOOLTIP = [" + returnValue + "].");
        
        return returnValue;
    }
    
    public void verifyCustomizerLayout() {
        FontCustomEditorOperator customizer = new FontCustomEditorOperator(propertyCustomizer);
        customizer.verify();
        customizer.btOK();
        customizer.btCancel();
    }    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_Font.class));
        junit.textui.TestRunner.run(suite());
    }
    
}
