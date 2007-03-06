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
        suite.addTest(new ParametersPickerOperatorTest("testRbValue"));
        suite.addTest(new ParametersPickerOperatorTest("testTxtValue"));
        suite.addTest(new ParametersPickerOperatorTest("testRbBean"));
        suite.addTest(new ParametersPickerOperatorTest("testCboBean"));
        suite.addTest(new ParametersPickerOperatorTest("testRbProperty"));
        suite.addTest(new ParametersPickerOperatorTest("testTxtProperty"));
        suite.addTest(new ParametersPickerOperatorTest("testBtSelectProperty"));
        suite.addTest(new ParametersPickerOperatorTest("testRbMethodCall"));
        suite.addTest(new ParametersPickerOperatorTest("testTxtMethodCall"));
        suite.addTest(new ParametersPickerOperatorTest("testBtSelectMethod"));
        suite.addTest(new ParametersPickerOperatorTest("testRbUserCode"));
        suite.addTest(new ParametersPickerOperatorTest("testTxtUserCode"));
        suite.addTest(new ParametersPickerOperatorTest("testValue"));
        suite.addTest(new ParametersPickerOperatorTest("testSetValue"));
        // bean radion button is disabled in sample JFrame
        //suite.addTest(new ParametersPickerOperatorTest("testBean"));
        //suite.addTest(new ParametersPickerOperatorTest("testSetBean"));
        suite.addTest(new ParametersPickerOperatorTest("testProperty"));
        suite.addTest(new ParametersPickerOperatorTest("testSelectProperty"));
        suite.addTest(new ParametersPickerOperatorTest("testMethodCall"));
        suite.addTest(new ParametersPickerOperatorTest("testSelectMethod"));
        suite.addTest(new ParametersPickerOperatorTest("testUserCode"));
        suite.addTest(new ParametersPickerOperatorTest("testSetUserCode"));
        suite.addTest(new ParametersPickerOperatorTest("testClose"));
        return suite;
    }
    
    /** Opens method picker. */
    protected void setUp() {
        super.setUp();
        if(ppo == null) {
            // need to wait because combo box is not refreshed in time
            new EventTool().waitNoEvent(1000);
            // set Form Connection panel
            fceo.setMode(Bundle.getString("org.netbeans.modules.form.Bundle", "CTL_RADConn_DisplayName"));
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
        String expected = Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle", "CTL_CW_GetParametersFrom");
        String label = ppo.lblGetParameterFrom().getText();
        assertEquals("Wrong label found.", expected, label);
    }
    
    /** Test of rbValue method. */
    public void testRbValue() {
        String expected = Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle", "CTL_CW_Value");
        String found = ppo.rbValue().getText();
        assertEquals("Wrong radio button found.", expected, found);
    }
    
    /** Test of txtValue method. */
    public void testTxtValue() {
        ppo.value();
        assertTrue("Wrong text field found. Should be editable.", ppo.txtValue().isEditable());
    }
    
    /** Test of rbBean method. */
    public void testRbBean() {
        String expected = Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle", "CTL_CW_Bean");
        String found = ppo.rbBean().getText();
        assertEquals("Wrong radio button found.", expected, found);
    }
    
    /** Test of cboBean method. */
    public void testCboBean() {
        assertTrue("Wrong combo box found. Should not be enabled.", !ppo.cboBean().isEnabled());
    }
    
    /** Test of rbProperty method. */
    public void testRbProperty() {
        String expected = Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle", "CTL_CW_Property");
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
    
    /** Test of rbUserCode method. */
    public void testRbUserCode() {
        String expected = Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle", "CTL_CW_UserCode");
        String found = ppo.rbUserCode().getText();
        assertEquals("Wrong radio button found.", expected, found);
    }
    
    /** Test of txtUserCode method. */
    public void testTxtUserCode() {
        ppo.userCode();
        assertTrue("Wrong editor pane found. Should be enabled.", ppo.txtUserCode().isEnabled());
    }
    
    /** Test of value method. */
    public void testValue() {
        ppo.value();
        assertTrue("Pushing radio button failed. It should be selected.", ppo.rbValue().isSelected());
    }
    
    /** Test of setValue method. */
    public void testSetValue() {
        ppo.value();
        String expected = "value";
        ppo.setValue(expected);
        String found = ppo.txtValue().getText();
        assertEquals("Set text to value field failed.", expected, found);
    }
    
    /** Test of bean method. */
    public void testBean() {
        ppo.bean();
        assertTrue("Pushing radio button failed. It should be selected.", ppo.rbBean().isSelected());
    }
    
    /** Test of setBean method. */
    public void testSetBean() {
        ppo.bean();
        String expected = "item";
        ppo.setBean(expected);
        String found = ppo.cboBean().getSelectedItem().toString();
        assertEquals("Set item of bean field failed.", expected, found);
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
    
    /** Test of userCode method. */
    public void testUserCode() {
        ppo.userCode();
        assertTrue("Pushing radio button failed. It should be selected.", ppo.rbUserCode().isSelected());
    }
    
    /** Test of setUserCode method. */
    public void testSetUserCode() {
        String expected = "// my code";
        ppo.setUserCode(expected);
        String found = ppo.txtUserCode().getText();
        assertEquals("Set text failed.", expected, found);
    }

    /** Clean-up after tests. Close opened dialog and property sheet. */
    public void testClose() {
        ppo.close();
        fceo = null;
        new PropertySheetOperator("[JFrame]").close();
        new FormDesignerOperator(SAMPLE_FRAME_NAME).closeDiscard();
    }
}
