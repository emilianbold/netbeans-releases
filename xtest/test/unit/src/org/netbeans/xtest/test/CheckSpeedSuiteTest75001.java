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
import org.netbeans.xtest.pe.xmlbeans.UnitTestSuite;

/** Check test results of SpeedSuiteTest75001. At the end delete those results
 * because they contain errors.
 */
public class CheckSpeedSuiteTest75001 extends NbTestCase {
    
    /** Need to be defined because of JUnit */
    public CheckSpeedSuiteTest75001(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CheckSpeedSuiteTest75001("testCheckResults"));
        return suite;
    }
    
    private static File suiteResultsFile = new File(
            new File(System.getProperty("xtest.workdir")),
            "xmlresults/suites/TEST-org.netbeans.xtest.test.SpeedSuiteTest75001.xml"
            );
    
    /** Setup. */
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }
    
    /** Delete SpeedSuiteTest75001 results because they contain wanted errors. */
    public void tearDown() {
        assertTrue("Cannot delete suite file "+suiteResultsFile, suiteResultsFile.delete());
    }
    
    /** Check number of tests with proper status. */
    public void testCheckResults() throws IOException, ClassNotFoundException {
        UnitTestSuite unitTestSuite = UnitTestSuite.loadFromFile(suiteResultsFile);
        assertEquals("Wrong number of errors.", 0, unitTestSuite.getTestsError());
        assertEquals("Wrong number of fails.", 1, unitTestSuite.getTestsFail());
        assertEquals("Wrong number of passes.", 5, unitTestSuite.getTestsPass());
        assertEquals("Wrong total number.", 6, unitTestSuite.getTestsTotal());
    }
}