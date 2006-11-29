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
import org.netbeans.xtest.pe.xmlbeans.TestBag;
import org.netbeans.xtest.pe.xmlbeans.TestRun;
import org.netbeans.xtest.pe.xmlbeans.UnitTestCase;
import org.netbeans.xtest.pe.xmlbeans.UnitTestSuite;
import org.netbeans.xtest.pe.xmlbeans.XTestResultsReport;

/** Check instance results contains 'Critical Error' test suite informing
 * about empty testbag.
 */
public class CheckEmptyTestbag extends NbTestCase {
    
    /** Need to be defined because of JUnit */
    public CheckEmptyTestbag(String name) {
        super(name);
    }

    /** Create test suite. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(CheckEmptyTestbag.class);
        return suite;
    }
    
    /** Set up. */
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }

    /** Test results. */
    public void testCheckResults() throws IOException, ClassNotFoundException, InterruptedException {
        log("xtest.workdir="+System.getProperty("xtest.workdir"));
        log("xtest.instance.results="+System.getProperty("xtest.instance.results"));
        File testFailuresFile = new File(System.getProperty("xtest.instance.results"), "xmlresults/testreport-failures.xml");
        assertTrue("Tests executed using instance should fail because of compilation error and file should exist: "+testFailuresFile, testFailuresFile.exists());
        XTestResultsReport report = XTestResultsReport.loadFromFile(testFailuresFile);
        TestRun testrun = report.xmlel_TestRun[0];
        // label used in org.netbeans.xtest.NbExecutor
        String label = "Empty testbag";
        TestBag[] testBags = testrun.xmlel_TestBag;
        TestBag testBag = null;
        for (int i = 0; i < testBags.length; i++) {
            log("TestBag name="+testBags[i].getName());
            if(label.equals(testBags[i].getName())) {
                testBag = testBags[i];
                break;
            }
        }
        assertNotNull("Testbag '"+label+"' should exist.", testBag);
        UnitTestSuite unitTestSuite = testBag.xmlel_UnitTestSuite[0];
        UnitTestCase unitTestCase = unitTestSuite.xmlel_UnitTestCase[0];
        label = "Critical Error";
        assertEquals("TestSuite '"+label+"' should exist.", label, unitTestSuite.getName());
        label = "loadingSuites";
        assertEquals("TestCase '"+label+"' should exist.", label, unitTestCase.getName());
        assertEquals("TestCase '"+label+"' should have error results.", UnitTestCase.TEST_ERROR, unitTestCase.getResult());
    }
}
