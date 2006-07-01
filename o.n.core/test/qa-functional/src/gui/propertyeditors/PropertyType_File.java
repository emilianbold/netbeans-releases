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
        propertyValue_L = new java.io.File(CoreSupport.getSampleProjectPath(this),"build.xml").getPath();
        log("=========== File to be set {"+propertyValue_L+"}");        
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
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void testByInPlace(){
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
