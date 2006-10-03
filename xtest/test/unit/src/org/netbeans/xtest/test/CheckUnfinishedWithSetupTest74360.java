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

package org.netbeans.xtest.test;

import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.xtest.pe.xmlbeans.UnitTestCase;
import org.netbeans.xtest.pe.xmlbeans.UnitTestSuite;

/** Check test results of UnfinishedWithSetupTest74360. At the end delete those results
 * because they contain errors.
 */
public class CheckUnfinishedWithSetupTest74360 extends NbTestCase {
    
    /** Need to be defined because of JUnit */
    public CheckUnfinishedWithSetupTest74360(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CheckUnfinishedWithSetupTest74360("testCheckResults"));
        return suite;
    }
    
    private static File suiteResultsFile = new File(
            new File(System.getProperty("xtest.workdir")),
            "xmlresults/suites/TEST-org.netbeans.xtest.test.UnfinishedWithSetupTest74360.xml"
            );
    
    /** Setup. */
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }
    
    /** Delete UnfinishedWithSetupTest74360 results because they contain wanted errors. */
    public void tearDown() {
        assertTrue("Cannot delete suite file "+suiteResultsFile, suiteResultsFile.delete());
    }
    
    /** Check number of tests with proper status. */
    public void testCheckResults() throws IOException, ClassNotFoundException {
        UnitTestSuite unitTestSuite = UnitTestSuite.loadFromFile(suiteResultsFile);
        assertEquals("Wrong number of errors.", 3, unitTestSuite.getTestsError());
        assertEquals("Wrong number of fails.", 0, unitTestSuite.getTestsFail());
        assertEquals("Wrong number of passes.", 1, unitTestSuite.getTestsPass());
        assertEquals("Wrong total number.", 4, unitTestSuite.getTestsTotal());
        UnitTestCase unitTestCase = unitTestSuite.xmlel_UnitTestCase[0];
        assertEquals("Wrong results of test1.", unitTestCase.TEST_PASS, unitTestCase.getResult());
        unitTestCase = unitTestSuite.xmlel_UnitTestCase[1];
        assertEquals("Wrong results of test2.", unitTestCase.TEST_UNKNOWN, unitTestCase.getResult());
        assertEquals("Wrong message of test2.", "Did not finish.", unitTestCase.getMessage());
        unitTestCase = unitTestSuite.xmlel_UnitTestCase[2];
        assertEquals("Wrong results of test3.", unitTestCase.TEST_UNKNOWN, unitTestCase.getResult());
        assertEquals("Wrong message of test3.", "Did not start.", unitTestCase.getMessage());
        unitTestCase = unitTestSuite.xmlel_UnitTestCase[3];
        assertEquals("Wrong results of test4.", unitTestCase.TEST_UNKNOWN, unitTestCase.getResult());
        assertEquals("Wrong message of test4.", "Did not start.", unitTestCase.getMessage());
    }
}