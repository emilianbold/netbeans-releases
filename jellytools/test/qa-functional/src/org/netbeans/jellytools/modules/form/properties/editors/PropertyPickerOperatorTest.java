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
 * Test of org.netbeans.jellytools.modules.form.properties.editors.PropertyPickerOperator.
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class PropertyPickerOperatorTest extends FormPropertiesEditorsTestCase {
    
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
        suite.addTest(new PropertyPickerOperatorTest("testLblComponent"));
        suite.addTest(new PropertyPickerOperatorTest("testCboComponent"));
        suite.addTest(new PropertyPickerOperatorTest("testLblProperties"));
        suite.addTest(new PropertyPickerOperatorTest("testLstProperties"));
        suite.addTest(new PropertyPickerOperatorTest("testSetComponent"));
        suite.addTest(new PropertyPickerOperatorTest("testSetProperty"));
        suite.addTest(new PropertyPickerOperatorTest("testClose"));
        return suite;
    }
    
    /** Redirect output to log files, wait before each test case. */
    protected void setUp() {
        super.setUp();
        if(ppo == null) {
            // need to wait because combo box is not refreshed in time
            new EventTool().waitNoEvent(1000);
            // set Form Connection panel
            fceo.setMode(Bundle.getString("org.netbeans.modules.form.Bundle", "CTL_RADConn_DisplayName"));
            ParametersPickerOperator paramPicker = new ParametersPickerOperator(PROPERTY_NAME);
            paramPicker.property();
            paramPicker.selectProperty();
            ppo = new PropertyPickerOperator();
        }
    }

    private static PropertyPickerOperator ppo;
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public PropertyPickerOperatorTest(java.lang.String testName) {
        super(testName);
    }
    
    /** Test of lblComponent method. */
    public void testLblComponent() {
        String expected = Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle", "CTL_CW_Component");
        String label = ppo.lblComponent().getText();
        assertEquals("Wrong label found.", expected, label);
    }
    
    /** Test of cboComponent method. */
    public void testCboComponent() {
        ppo.cboComponent();
    }
    
    /** Test of lblProperties method. */
    public void testLblProperties() {
        String expected = Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle", "CTL_CW_PropertyList");
        String label = ppo.lblProperties().getText();
        assertEquals("Wrong label found.", expected, label);
    }
    
    /** Test of lstProperties method. */
    public void testLstProperties() {
        ppo.lstProperties();
    }
    
    /** Test of setComponent method. */
    public void testSetComponent() {
        String expected = Bundle.getString("org.netbeans.modules.form.Bundle", "CTL_FormTopContainerName");
        ppo.setComponent(expected);
        assertEquals("Select component failed.", expected, ppo.cboComponent().getSelectedItem());
    }
    
    /** Test of setProperty method. Also closes opened windows. */
    public void testSetProperty() {
        String expected = "title";
        ppo.setProperty(expected);
        assertEquals("Select property failed.", expected, ppo.lstProperties().getSelectedValue());
    }
    
    /** Clean-up after tests. Close opened dialog and property sheet. */
    public void testClose() {
        ppo.close();
        fceo = null;
        new PropertySheetOperator("[JFrame]").close();
        new FormDesignerOperator(SAMPLE_FRAME_NAME).closeDiscard();
    }
}
