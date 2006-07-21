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

package org.netbeans.xtest.plugin.ide.test;

import java.io.File;
import java.util.Hashtable;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.xtest.pe.xmlbeans.UnitTestSuite;
import org.netbeans.xtest.pe.xmlbeans.UnitTestCase;

/** Check test results of XTestIDEHandlerTest. At the end delete those results
 * because they contain errors.
 */
public class XTestIDEHandlerCheckTest extends NbTestCase {
    
    /** Need to be defined because of JUnit */
    public XTestIDEHandlerCheckTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new XTestIDEHandlerCheckTest("testNotSevere"));
        suite.addTest(new XTestIDEHandlerCheckTest("testWarning"));
        suite.addTest(new XTestIDEHandlerCheckTest("testSevere"));
        suite.addTest(new XTestIDEHandlerCheckTest("testErrorInTest"));
        suite.addTest(new XTestIDEHandlerCheckTest("testErrorInTestSevereRemembered"));
        suite.addTest(new XTestIDEHandlerCheckTest("testSevereWithFail"));
        suite.addTest(new XTestIDEHandlerCheckTest("testDeleteSuite"));
        return suite;
    }
    
    private static Hashtable unitTestCases;
    private static File suiteResultsFile = new File(
            new File(System.getProperty("xtest.workdir")),
            "xmlresults/suites/TEST-org.netbeans.xtest.plugin.ide.test.XTestIDEHandlerTest.xml"
    );
    private UnitTestCase unitTestCase;
    
    /** Load XTestIDEHandlerTest results from xml file. */
    public void setUp() throws Exception {
        System.out.println("########  "+getName()+"  #######");
        if(unitTestCases == null) {
            unitTestCases = new Hashtable();
            UnitTestSuite unitTestSuite = UnitTestSuite.loadFromFile(suiteResultsFile);
            for(int i=0;i<unitTestSuite.xmlel_UnitTestCase.length;i++) {
                unitTestCases.put(unitTestSuite.xmlel_UnitTestCase[i].getName(), unitTestSuite.xmlel_UnitTestCase[i]);
            }
        }
        unitTestCase = (UnitTestCase)unitTestCases.get(getName());
    }
    
    /** XTestIDEHandlerTest.testNotSevere should pass. */
    public void testNotSevere() {
        assertEquals("Only greater that WARNING level with exception should be caugth by XTest.", "pass", unitTestCase.getResult());
    }
    
    /** XTestIDEHandlerTest.testWarning should be error. */
    public void testWarning() {
        assertEquals("SEVERE mesagges with exception should be caugth by XTest.", "error", unitTestCase.getResult());
    }
    
    /** XTestIDEHandlerTest.testSevere should be error. */
    public void testSevere() {
        assertEquals("SEVERE mesagges with exception should be caugth by XTest.", "error", unitTestCase.getResult());
    }
    
    /** XTestIDEHandlerTest.testErrorInTest should report error from test. */
    public void testErrorInTest() {
        assertEquals("Error in test should be reported.", "error", unitTestCase.getResult());
        assertEquals("Error in test should precede severe message from IDE.", "/ by zero", unitTestCase.getMessage());
    }
    
    /** XTestIDEHandlerTest.testErrorInTestSevereRemembered should be error. */
    public void testErrorInTestSevereRemembered() {
        assertEquals("Severe message should be remembered if cannot be reported in previous tast case.", "error", unitTestCase.getResult());
        assertEquals("Wrong message.", "testErrorInTest", unitTestCase.getMessage());
    }
    
    /** XTestIDEHandlerTest.testSevereWithFail should be error. */
    public void testSevereWithFail() {
        assertEquals("SEVERE message should precede failure in test.", "error", unitTestCase.getResult());
        assertEquals("First exception message should be reported.", "testSevereWithFail1", unitTestCase.getMessage());
    }
    
    /** Delete XTestIDEHandlerTest results because they contain wanted errors. */
    public void testDeleteSuite() {
        assertTrue("Cannot delete suite file "+suiteResultsFile, suiteResultsFile.delete());
    }
}