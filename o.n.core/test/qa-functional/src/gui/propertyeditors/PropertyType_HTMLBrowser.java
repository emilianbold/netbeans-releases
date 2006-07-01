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

import org.netbeans.junit.NbTestSuite;


/**
 * Tests of HTML Browser Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_HTMLBrowser extends PropertyEditorsTest {

    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;

    public boolean waitDialog = false;


    /** Creates a new instance of PropertyType_HTMLBrowser */
    public PropertyType_HTMLBrowser(String testName) {
        super(testName);
    }


    public void setUp(){
        propertyName_L = "Html Browser";
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_HTMLBrowser("testByComboExternal"));
        suite.addTest(new PropertyType_HTMLBrowser("testByComboSWINGBrowser"));
        return suite;
    }
    
    public void testByComboExternal(){
        propertyValue_L = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.extbrowser.Bundle", "Services/Browsers/SimpleExtBrowser.settings"); // External Browser (Command Line)
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCombo(propertyName_L, propertyValue_L, true);
    }
    
    public void testByComboSWINGBrowser(){
        propertyValue_L = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.ui.Bundle", "Services/Browsers/SwingBrowser.ser"); // Swing HTML Browser
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCombo(propertyName_L, propertyValue_L, true);
    }
    
    public void setCustomizerValue() {
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, false);
    }
    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_HTMLBrowser.class));
        junit.textui.TestRunner.run(suite());
    }
    
    public void verifyCustomizerLayout() {
    }    
    
}
