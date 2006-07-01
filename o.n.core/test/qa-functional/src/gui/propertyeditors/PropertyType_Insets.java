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

import java.util.StringTokenizer;

import org.netbeans.jellytools.properties.editors.RectangleCustomEditorOperator;

import org.netbeans.jemmy.JemmyException;

import org.netbeans.junit.NbTestSuite;



/**
 * Tests of Insets Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_Insets extends PropertyEditorsTest {

    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;

    public boolean waitDialog = false;

    /** Creates a new instance of PropertyType_Insets */
    public PropertyType_Insets(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "Insets";
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_Insets("verifyCustomizer"));
        suite.addTest(new PropertyType_Insets("testCustomizerCancel"));
        suite.addTest(new PropertyType_Insets("testCustomizerOk"));
        suite.addTest(new PropertyType_Insets("testByInPlace"));
        suite.addTest(new PropertyType_Insets("testByInPlaceOneValue"));
        suite.addTest(new PropertyType_Insets("testByInPlaceInvalid"));
        suite.addTest(new PropertyType_Insets("testCustomizerInvalid"));
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
        propertyValue_L = "xx Insets";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = true;
        setByInPlace(propertyName_L, propertyValue_L, false);
    }
    
    public void verifyCustomizer() {
        verifyCustomizer(propertyName_L);
    }
    
    public void setCustomizerValue() {
        RectangleCustomEditorOperator customizer = new RectangleCustomEditorOperator(propertyCustomizer);
        StringTokenizer st = new StringTokenizer(propertyValue_L, ", ");
        int x = st.countTokens();
        
        if(x>4)
            throw new JemmyException("ERROR: InsetsCustomizer.setValue(\""+propertyValue_L+"\") - {number values="+x+"}.");
        
        customizer.setRectangleValue(st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken());
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
    }
    
    public void verifyCustomizerLayout() {
        RectangleCustomEditorOperator customizer = new RectangleCustomEditorOperator(propertyCustomizer);
        customizer.verify();
        customizer.btOK();
        customizer.btCancel();
    }    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_Insets.class));
        junit.textui.TestRunner.run(suite());
    }
    
}
