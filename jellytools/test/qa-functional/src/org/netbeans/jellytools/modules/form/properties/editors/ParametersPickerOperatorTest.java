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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.modules.form.properties.editors;

import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.junit.NbTestSuite;

/**
 * Test of org.netbeans.jellytools.modules.form.properties.editors.ParametersPickerOperator.
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class ParametersPickerOperatorTest extends FormPropertiesEditorsTestCase {
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new ParametersPickerOperatorTest("testLblGetParameterFrom"));
        suite.addTest(new ParametersPickerOperatorTest("testRbComponent"));
        suite.addTest(new ParametersPickerOperatorTest("testCboComponent"));
        suite.addTest(new ParametersPickerOperatorTest("testRbProperty"));
        suite.addTest(new ParametersPickerOperatorTest("testTxtProperty"));
        suite.addTest(new ParametersPickerOperatorTest("testBtSelectProperty"));
        suite.addTest(new ParametersPickerOperatorTest("testRbMethodCall"));
        suite.addTest(new ParametersPickerOperatorTest("testTxtMethodCall"));
        suite.addTest(new ParametersPickerOperatorTest("testBtSelectMethod"));
        // Component radion button is disabled in sample JFrame
        //suite.addTest(new ParametersPickerOperatorTest("testComponent"));
        //suite.addTest(new ParametersPickerOperatorTest("testSetComponent"));
        suite.addTest(new ParametersPickerOperatorTest("testProperty"));
        suite.addTest(new ParametersPickerOperatorTest("testSelectProperty"));
        suite.addTest(new ParametersPickerOperatorTest("testMethodCall"));
        suite.addTest(new ParametersPickerOperatorTest("testSelectMethod"));
        suite.addTest(new ParametersPickerOperatorTest("testClose"));
        return suite;
    }
    
    /** Opens method picker. */
    protected void setUp() {
        super.setUp();
        if(ppo == null) {
            // need to wait because combo box is not refreshed in time
            new EventTool().waitNoEvent(1000);
            // set "Value from existing component"
            fceo.setMode(Bundle.getString("org.netbeans.modules.form.Bundle", "CTL_FormConnection_DisplayName"));
            ppo = new ParametersPickerOperator(PROPERTY_NAME);
        }
    }
    
    private static ParametersPickerOperator ppo;
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public ParametersPickerOperatorTest(String testName) {
        super(testName);
    }
    
    /** Test of lblGetParameterFrom method. */
    public void testLblGetParameterFrom() {
        String expected = Bundle.getString("org.netbeans.modules.form.Bundle",
                                           "ConnectionCustomEditor.jLabel1.text");
        String label = ppo.lblGetParameterFrom().getText();
        assertEquals("Wrong label found.", expected, label);
    }
    
    /** Test of rbComponent method. */
    public void testRbComponent() {
        String expected = Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle",
                                                  "ConnectionCustomEditor.beanRadio.text");
        String found = ppo.rbComponent().getText();
        assertEquals("Wrong radio button found.", expected, found);
    }
    
    /** Test of cboComponent method. */
    public void testCboComponent() {
        assertTrue("Wrong combo box found. Should not be enabled.", !ppo.cboComponent().isEnabled());
    }
    
    /** Test of rbProperty method. */
    public void testRbProperty() {
        String expected = Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle",
                                                  "ConnectionCustomEditor.propertyRadio.text");
        String found = ppo.rbProperty().getText();
        assertEquals("Wrong radio button found.", expected, found);
    }
    
    /** Test of txtProperty method. */
    public void testTxtProperty() {
        ppo.property();
        assertTrue("Wrong text field found. Should be enabled.", ppo.txtProperty().isEnabled());
    }
    
    /** Test of btSelectProperty method. */
    public void testBtSelectProperty() {
        ppo.property();
        assertTrue("Wrong button found. Should be enabled.", ppo.btSelectProperty().isEnabled());
    }
    
    /** Test of rbMethodCall method. */
    public void testRbMethodCall() {
        String expected = Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle", "CTL_CW_Method");
        String found = ppo.rbMethodCall().getText();
        assertEquals("Wrong radio button found.", expected, found);
    }
    
    /** Test of txtMethodCall method. */
    public void testTxtMethodCall() {
        ppo.methodCall();
        assertTrue("Wrong text field found. Should be enabled.", ppo.txtMethodCall().isEnabled());
    }
    
    /** Test of btSelectMethod method. */
    public void testBtSelectMethod() {
        ppo.methodCall();
        assertTrue("Wrong button found. Should be enabled.", ppo.btSelectMethod().isEnabled());
    }
    
    /** Test of component method. */
    public void testComponent() {
        ppo.component();
        assertTrue("Pushing radio button failed. It should be selected.", ppo.rbComponent().isSelected());
    }
    
    /** Test of setComponent method. */
    public void testSetComponent() {
        ppo.component();
        String expected = "item";
        ppo.setComponent(expected);
        String found = ppo.cboComponent().getSelectedItem().toString();
        assertEquals("Set item of component field failed.", expected, found);
    }
    
    /** Test of property method. */
    public void testProperty() {
        ppo.property();
        assertTrue("Pushing radio button failed. It should be selected.", ppo.rbProperty().isSelected());
    }
    
    /** Test of selectProperty method. */
    public void testSelectProperty() {
        ppo.property();
        ppo.selectProperty();
        new PropertyPickerOperator().close();
    }
    
    /** Test of methodCall method. */
    public void testMethodCall() {
        ppo.methodCall();
        assertTrue("Pushing radio button failed. It should be selected.", ppo.rbMethodCall().isSelected());
    }
    
    /** Test of selectMethod method. */
    public void testSelectMethod() {
        ppo.methodCall();
        ppo.selectMethod();
        new MethodPickerOperator().close();
    }
    
    /** Clean-up after tests. Close opened dialog and property sheet. */
    public void testClose() {
        ppo.close();
        fceo = null;
        new PropertySheetOperator("[JFrame]").close();
        new FormDesignerOperator(SAMPLE_FRAME_NAME).closeDiscard();
    }
}
