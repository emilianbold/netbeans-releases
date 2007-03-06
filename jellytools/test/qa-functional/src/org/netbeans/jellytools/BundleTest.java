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
package org.netbeans.jellytools;

import java.util.ResourceBundle;
import junit.framework.*;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.junit.*;

/**
 * Test of org.netbeans.jellytools.Bundle
 * @author Jiri.Skrivanek@sun.com
 */
public class BundleTest extends JellyTestCase {

    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite(BundleTest.class);
        return suite;
    }
    
    /** Redirect output to log files, wait before each test case and
     * show dialog to test. */
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
    }
    
    /** Clean up after each test case. */
    protected void tearDown() {
    }
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public BundleTest(java.lang.String testName) {
        super(testName);
    }
    
    /** Test of getBundle method. */
    public void testGetBundle() {
        try {
            ResourceBundle resBundle = Bundle.getBundle("org.netbeans.core.Bundle");
            assertNotNull("Should not return null.", resBundle);
        } catch (JemmyException e) {
            fail("Should always find org.netbeans.core.Bundle");
        }
        try {
            Bundle.getBundle("nonsense.package.Bundle");
            fail("Should not find nonsense.package.Bundle");
        } catch (JemmyException e) {
            // right, should fail
        }
        try {
            Bundle.getBundle(null);
            fail("Should not accept null parameter.");
        } catch (JemmyException e) {
            // right, should fail
        }
    }
    
    /** Test of getString method. Tests also negative cases */
    public void testGetString() {
        try {
            String value = Bundle.getString("org.netbeans.core.windows.services.Bundle", "OK_OPTION_CAPTION");
            assertNotNull("Should not return null.", value);
            assertTrue("Should not return empty string.", value.length() != 0);
        } catch (JemmyException e) {
            fail("Should always find OK_OPTION_CAPTION at org.netbeans.core.windows.services.Bundle.");
        }
        try {
            Bundle.getString("org.netbeans.core.Bundle", null);
            fail("Should not accept null parameter.");
        } catch (JemmyException e) {
            // right, should fail
        }
        try {
            Bundle.getString("org.netbeans.core.Bundle", "nonsense key - @#$%^");
            fail("Should not find nonsense key.");
        } catch (JemmyException e) {
            // right, should fail
        }
        try {
            Bundle.getString((ResourceBundle)null, "OK_OPTION_CAPTION");
            fail("Should not accept null ResourceBundle parameter.");
        } catch (JemmyException e) {
            // right, should fail
        }
    }
    
    /** Test of getString method with parameter to format. */
    public void testGetStringParams() {
        String pattern = Bundle.getString("org.netbeans.core.Bundle", "CTL_FMT_LocalProperties");
        Object[] params = new Object[] {new Integer(1), "AnObject"};
        String value = Bundle.getString("org.netbeans.core.Bundle", "CTL_FMT_LocalProperties", params);
        String expected = java.text.MessageFormat.format(pattern, params);
        assertEquals("Parameters not properly formattted.", expected, value);
    }
    
    /** Test of getStringTrimmed method. */
    public void testGetStringTrimmed() {
        //Saving {0} ...
        String valueRaw = Bundle.getString("org.netbeans.core.Bundle", "CTL_FMT_SavingMessage");
        String value = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "CTL_FMT_SavingMessage");
        assertTrue("Characters '{' should be cut off from \""+valueRaw+"\".", value.indexOf('{') == -1);
        // "&Help"
        valueRaw = Bundle.getString("org.netbeans.core.Bundle", "Menu/Help");
        value = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Help");
        assertTrue("Characters '&' should be removed from \""+valueRaw+"\".", value.indexOf('&') == -1);
    }
    
    /** Test of getStringTrimmed method with parameter to format. */
    public void testGetStringTrimmedParams() {
        String pattern = Bundle.getString("org.netbeans.core.Bundle", "CTL_FMT_LocalProperties");
        Object[] params = new Object[] {new Integer(1), "AnOb&ject"};
        String value = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "CTL_FMT_LocalProperties", params);
        String expected = java.text.MessageFormat.format(pattern, params);
        expected = new StringBuffer(expected).deleteCharAt(expected.indexOf('&')).toString();
        assertEquals("Parameters not properly formattted.", expected, value);
    }
}
