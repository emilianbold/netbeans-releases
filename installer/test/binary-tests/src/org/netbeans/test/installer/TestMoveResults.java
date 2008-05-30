/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 *
 * $Id$
 *
 */
package org.netbeans.test.installer;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Mikhail Vaysman
 */
public class TestMoveResults extends NbTestCase {
    static int testsTotal = 0;
    static int testsPass = 0;
    static int testsUnexpectedPass = 0;
    static int testsError = 0;
    static int testsFail = 0;
    static int testsExpectedFail = 0;

    public TestMoveResults() {
        super("Move tests results to work dir");
    }

    public static Test suite() {
        TestSuite suite = new NbTestSuite(TestMoveResults.class);

        return suite;
    }
    
    public static void setTestsTotal(int tests) {
        testsTotal = tests;
    }

    public static void setTestsPass(int tests) {
        testsPass = tests;
    }

    public static void setTestsUnexpectedPass(int tests) {
        testsUnexpectedPass = tests;
    }
    public static void setTestsError(int tests) {
        testsError = tests;
    }
    public static void setTestsFail(int tests) {
        testsFail = tests;
    }
    public static void setTestsExpectedFail(int tests) {
        testsExpectedFail = tests;
    }
    
    public void testParseAndMoveResult() {
        File workDir = null;

        try {
            workDir = getWorkDir();
        } catch (IOException ex) {
            NbTestCase.fail("Can not get WorkDir");
        }

        File results = new File(System.getProperty("xtest.tmpdir") + "/tests/qa-functional/results/xmlresults/testreport.xml");
        DefaultHandler handler = new DefaultHandler() {

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                if (qName.equals("XTestResultsReport")) {
                    TestMoveResults.setTestsTotal(Integer.parseInt(attributes.getValue("testsTotal")));
                    TestMoveResults.setTestsPass(Integer.parseInt(attributes.getValue("testsPass")));
                    TestMoveResults.setTestsUnexpectedPass(Integer.parseInt(attributes.getValue("testsUnexpectedPass")));
                    TestMoveResults.setTestsError(Integer.parseInt(attributes.getValue("testsError")));
                    TestMoveResults.setTestsFail(Integer.parseInt(attributes.getValue("testsFail")));
                    TestMoveResults.setTestsExpectedFail(Integer.parseInt(attributes.getValue("testsExpectedFail")));
                }
            }
        };

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(results, handler);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        NbTestCase.assertEquals(591, testsTotal);
//        NbTestCase.assertEquals(183, testsPass);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
