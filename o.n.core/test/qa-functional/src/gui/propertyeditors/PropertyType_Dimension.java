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

import org.netbeans.jellytools.properties.editors.DimensionCustomEditorOperator;

import org.netbeans.jemmy.JemmyException;

import org.netbeans.junit.NbTestSuite;



/**
 * Tests of Dimension Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_Dimension extends PropertyEditorsTest {
    
    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;
    
    public boolean waitDialog = false;
    
    /** Creates a new instance of PropertyType_Dimension */
    public PropertyType_Dimension(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "Dimension";
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_Dimension("testCustomizerCancel"));
        suite.addTest(new PropertyType_Dimension("testCustomizerOk"));
        suite.addTest(new PropertyType_Dimension("testByInPlace"));
        suite.addTest(new PropertyType_Dimension("testByInPlaceOneValue"));
        suite.addTest(new PropertyType_Dimension("testByInPlaceInvalid"));
        suite.addTest(new PropertyType_Dimension("testCustomizerInvalid"));
        return suite;
    }
    
    public void testCustomizerOk() {
        propertyValue_L = "10, 20";
        propertyValueExpectation_L = "["+propertyValue_L+"]";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        propertyValue_L = "100, 200";
        propertyValueExpectation_L = "["+propertyValue_L+"]";
        waitDialog = false;
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void testCustomizerInvalid(){
        propertyValue_L = "xx, ww";
        propertyValueExpectation_L = "["+propertyValue_L+"]";
        waitDialog = true;                                     
        setByCustomizerOk(propertyName_L, false);
    }
    
    public void testByInPlace(){
        propertyValue_L = "30, 50";
        propertyValueExpectation_L = "["+propertyValue_L+"]";
        waitDialog = false;
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void testByInPlaceOneValue(){
        propertyValue_L = "20";
        propertyValueExpectation_L = "["+propertyValue_L+", "+propertyValue_L+"]";
        waitDialog = false;
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void testByInPlaceInvalid(){
        propertyValue_L = "xx Dimension";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = true;
        setByInPlace(propertyName_L, propertyValue_L, false);
    }
    
    public void setCustomizerValue() {
        DimensionCustomEditorOperator customizer = new DimensionCustomEditorOperator(propertyCustomizer);

        StringTokenizer st = new StringTokenizer(propertyValue_L, ", ");
        int x = st.countTokens();
        
        if(x>2)
            throw new JemmyException("ERROR: DimensionPointCustomizer.setValue(\""+propertyValue_L+"\") - {number values="+x+"}.");
        
        customizer.setDimensionValue(st.nextToken(), st.nextToken());
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
    }
    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_Dimension.class));
        junit.textui.TestRunner.run(suite());
    }
    
    public void verifyCustomizerLayout() {
    }    
    
}
