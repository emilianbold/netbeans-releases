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

import org.netbeans.jellytools.properties.editors.ServiceTypeCustomEditorOperator;

import org.netbeans.junit.NbTestSuite;

/**
 * Tests of Service Type Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_ServiceType extends PropertyEditorsTest {
    
    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;
    
    public boolean waitDialog = false;
    
    /** Creates a new instance of PropertyType_ServiceType */
    public PropertyType_ServiceType(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "Service Type";
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_ServiceType("testByCombo"));
        suite.addTest(new PropertyType_ServiceType("verifyCustomizer"));
        suite.addTest(new PropertyType_ServiceType("testCustomizerOk"));
        suite.addTest(new PropertyType_ServiceType("testCustomizerCancel"));
        return suite;
    }
    
    public void testByCombo(){
        propertyValue_L = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.search.types.Bundle", "TEXT_DATE_CRITERION"); // Date
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;                                     
        setByCombo(propertyName_L, propertyValue_L, true);
    }
    
    public void testCustomizerOk(){
        propertyValue_L = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.vcscore.search.Bundle", "CTL_StatusCriterion"); // Status
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;                                     
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        propertyValue_L = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "Services/SearchType/org-netbeans-modules-search-ObjectTypeType.settings"); // Type
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;                                     
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void testCustomizerOk_platform(){
        propertyValue_L = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.autoupdate.resources.Bundle", "Services/AutoupdateType/autoupdate_xml_type_1.settings"); // NetBeans Update Center Beta
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;                                     
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel_platform(){
        propertyValue_L = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.autoupdate.resources.Bundle", "Services/AutoupdateType/autoupdate_xml_type.settings"); // NetBeans Update Center
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;                                     
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void verifyCustomizer() {
        verifyCustomizer(propertyName_L);
    }
    
    public void setCustomizerValue() {
        ServiceTypeCustomEditorOperator customizer = new ServiceTypeCustomEditorOperator(propertyCustomizer);
        customizer.setServiceTypeValue(propertyValue_L);
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
    }
    
    public void verifyCustomizerLayout() {
        ServiceTypeCustomEditorOperator customizer = new ServiceTypeCustomEditorOperator(propertyCustomizer);
        customizer.lstServices();
        customizer.propertySheet();
        customizer.btOK();
        customizer.btCancel();
    }    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_ServiceType.class));
        junit.textui.TestRunner.run(suite());
    }
    
}
