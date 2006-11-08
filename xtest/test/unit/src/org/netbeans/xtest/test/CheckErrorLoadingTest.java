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
 package org.netbeans.xtest.testetbeans.xtest.test;

import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.xtest.pe.xmlbeans.UnitTestCase;
import org.netbeans.xtest.pe.xmlbeans.UnitTestSuite;

/** This test verifies reporting of invalid tests (see http://www.netbeans.org/issues/show_bug.cgi?id=88180).
 */
public class CheckErrorLoadingTest extends NbTestCase {
    
    /** Creates a new test. */
    public CheckErrorLoadingTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CheckErrorLoadingTest("testCheckResults"));
        return suite;
    }
    
    private static File suiteResultsFile = new File(
            new File(System.getProperty("xtest.workdir")),
            "xmlresults/suites/TEST-Critical Error.xml"
            );
    
    /** Setup. */
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }
    
    /** Delete results because they contain wanted errors. */
    public void tearDown() {
        assertTrue("Cannot delete suite file "+suiteResultsFile, suiteResultsFile.delete());
    }
    
    /** Check number of tests with proper status. */
    public void testCheckResults() throws IOException, ClassNotFoundException {
        UnitTestSuite unitTestSuite = UnitTestSuite.loadFromFile(suiteResultsFile);
        assertEquals("Wrong number of errors.", 1, unitTestSuite.getTestsError());
        assertEquals("Wrong number of fails.", 0, unitTestSuite.getTestsFail());
        assertEquals("Wrong number of passes.", 0, unitTestSuite.getTestsPass());
        assertEquals("Wrong total number.", 1, unitTestSuite.getTestsTotal());
        String label = "Critical Error";
        assertEquals("TestSuite '"+label+"' should exist.", label, unitTestSuite.getName());
        UnitTestCase unitTestCase = unitTestSuite.xmlel_UnitTestCase[0];
        label = "loadingSuites";
        assertEquals("TestCase '"+label+"' should exist.", label, unitTestCase.getName());
        assertEquals("Wrong results of '"+label+"' test case.", unitTestCase.TEST_ERROR, unitTestCase.getResult());
    }
}