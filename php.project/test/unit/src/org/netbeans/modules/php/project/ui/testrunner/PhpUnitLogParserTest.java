/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.testrunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.netbeans.modules.php.project.ui.testrunner.TestSessionVO.TestCaseVO;
import org.netbeans.modules.php.project.ui.testrunner.TestSessionVO.TestSuiteVO;

/**
 * @author Tomas Mysik
 */
public class PhpUnitLogParserTest extends NbTestCase {

    public PhpUnitLogParserTest(String name) {
        super(name);
    }

    public void testParseLogWithMoreSuites() throws Exception {
        Reader reader = new BufferedReader(new FileReader(getLogForMoreSuites()));
        TestSessionVO testSession = new TestSessionVO();

        PhpUnitLogParser.parse(reader, testSession);

        assertEquals(64, testSession.getTime());
        assertEquals(6, testSession.getTests());

        // test suites & test cases
        assertEquals(3, testSession.getTestSuites().size());

        // 1st
        TestSuiteVO testSuite = testSession.getTestSuites().get(0);
        assertEquals("Calculator2Test", testSuite.getName());
        assertEquals("/home/gapon/NetBeansProjects/PhpProject01/tests/hola/Calculator2Test.php", testSuite.getFile());
        assertEquals(11, testSuite.getTime());
        assertEquals(1, testSuite.getTestCases().size());

        TestCaseVO testCase = testSuite.getTestCases().get(0);
        assertEquals("testAdd", testCase.getName());
        assertEquals("/home/gapon/NetBeansProjects/PhpProject01/tests/hola/Calculator2Test.php", testCase.getFile());
        assertEquals(43, testCase.getLine());
        assertEquals(11, testCase.getTime());

        // 2nd - pending test suite
        testSuite = testSession.getTestSuites().get(1);
        assertEquals("NoTestClassTest", testSuite.getName());
        assertEquals(1, testSuite.getTestCases().size());

        testCase = testSuite.getTestCases().get(0);
        assertEquals(Status.PENDING, testCase.getStatus());
        assertFalse(testCase.isFailure());
        assertFalse(testCase.isError());
        assertEquals(0, testCase.getStacktrace().length);

        // 3rd
        testSuite = testSession.getTestSuites().get(2);
        assertEquals("CalculatorTest", testSuite.getName());
        assertEquals(5, testSuite.getTestCases().size());

        testCase = testSuite.getTestCases().get(1);
        assertEquals("testAdd2", testCase.getName());
        assertTrue(testCase.isFailure());
        assertFalse(testCase.isError());
        assertEquals(Status.FAILED, testCase.getStatus());
        assertEquals(2, testCase.getStacktrace().length);
        assertEquals("Failed asserting that two objects are equal.\n--- Expected\n+++ Actual\n@@ -1,3 +1 @@\n-MyObject Object\n-(\n-)\n+77\n\\ Chybi znak konce radku na konci souboru", testCase.getStacktrace()[0]);
        assertEquals("/home/gapon/NetBeansProjects/PhpProject01/tests/CalculatorTest.php:56", testCase.getStacktrace()[1]);

        testCase = testSuite.getTestCases().get(2);
        assertEquals("testAdd3", testCase.getName());
        assertEquals(Status.FAILED, testCase.getStatus());
        assertEquals(2, testCase.getStacktrace().length);
        assertEquals("my expected message\nFailed asserting that two strings are equal.\nexpected string <hello>\ndifference      < x???>\ngot string      <hi>", testCase.getStacktrace()[0]);
        assertEquals("/home/gapon/NetBeansProjects/PhpProject01/tests/CalculatorTest.php:64", testCase.getStacktrace()[1]);

        testCase = testSuite.getTestCases().get(3);
        assertEquals("testAdd4", testCase.getName());
        assertEquals(Status.FAILED, testCase.getStatus());
        assertEquals(2, testCase.getStacktrace().length);
        assertEquals("Failed asserting that <integer:2> matches expected value <integer:3>.", testCase.getStacktrace()[0]);
        assertEquals("/home/gapon/NetBeansProjects/PhpProject01/tests/CalculatorTest.php:75", testCase.getStacktrace()[1]);

        testCase = testSuite.getTestCases().get(4);
        assertEquals("testAdd5", testCase.getName());
        assertEquals(Status.ERROR, testCase.getStatus());
        assertEquals(3, testCase.getStacktrace().length);
        assertEquals("Exception: my exception", testCase.getStacktrace()[0]);
        assertEquals("/home/gapon/NetBeansProjects/PhpProject01/src/Calculator.php:13", testCase.getStacktrace()[1]);
        assertEquals("/home/gapon/NetBeansProjects/PhpProject01/tests/CalculatorTest.php:82", testCase.getStacktrace()[2]);
    }

    public void testParseLogWithOneSuite() throws Exception {
        Reader reader = new BufferedReader(new FileReader(getLogForOneSuite()));
        TestSessionVO testSession = new TestSessionVO();

        PhpUnitLogParser.parse(reader, testSession);

        assertEquals(10, testSession.getTime());
        assertEquals(1, testSession.getTests());

        // test suites & test cases
        assertEquals(1, testSession.getTestSuites().size());

        // 1st
        TestSuiteVO testSuite = testSession.getTestSuites().get(0);
        assertEquals("Calculator2Test", testSuite.getName());
        assertEquals("/home/gapon/NetBeansProjects/PhpProject01/tests/hola/Calculator2Test.php", testSuite.getFile());
        assertEquals(10, testSuite.getTime());
        assertEquals(1, testSuite.getTestCases().size());

        TestCaseVO testCase = testSuite.getTestCases().get(0);
        assertEquals("testAdd", testCase.getName());
        assertEquals("/home/gapon/NetBeansProjects/PhpProject01/tests/hola/Calculator2Test.php", testCase.getFile());
        assertEquals(43, testCase.getLine());
        assertEquals(10, testCase.getTime());
    }

    public void testParseLogIssue157846() throws Exception {
        Reader reader = new BufferedReader(new FileReader(new File(getDataDir(), "phpunit-log-issue157846.xml")));
        TestSessionVO testSession = new TestSessionVO();

        PhpUnitLogParser.parse(reader, testSession);

        assertSame(1, testSession.getTestSuites().size());
        TestSuiteVO testSuite = testSession.getTestSuites().get(0);
        assertEquals("integration_REST_C_CustomersTest", testSuite.getName());

        assertSame(1, testSuite.getTestCases().size());
        TestCaseVO testCase = testSuite.getTestCases().get(0);
        assertEquals("testCheckNewRecord", testCase.getName());

        assertEquals(Status.FAILED, testCase.getStatus());
        assertSame(1, testCase.getStacktrace().length);
    }

    public void testParseLogIssue159876() throws Exception {
        Reader reader = new BufferedReader(new FileReader(new File(getDataDir(), "phpunit-log-issue159876.xml")));
        TestSessionVO testSession = new TestSessionVO();

        PhpUnitLogParser.parse(reader, testSession);

        assertSame(3, testSession.getTestSuites().size());
        TestSuiteVO testSuite = testSession.getTestSuites().get(0);
        assertEquals("LoginTest", testSuite.getName());
        assertEquals("/Library/WebServer/Documents/acalog/tests/EmptyTest.php", testSuite.getFile());
        assertSame(1, testSuite.getTestCases().size());
        assertEquals(TestCaseVO.pendingTestCase().toString(), testSuite.getTestCases().get(0).toString());

        testSuite = testSession.getTestSuites().get(1);
        assertEquals("LoginTest: Firefox on Windows", testSuite.getName());
        assertEquals("/Library/WebServer/Documents/acalog/tests/EmptyTest.php", testSuite.getFile());

        assertSame(1, testSuite.getTestCases().size());
        TestCaseVO testCase = testSuite.getTestCases().get(0);
        assertEquals("testLogin", testCase.getName());

        testSuite = testSession.getTestSuites().get(2);
        assertEquals("LoginTest: Internet Explorer on Windows", testSuite.getName());
        assertEquals("/Library/WebServer/Documents/acalog/tests/EmptyTest.php", testSuite.getFile());

        assertSame(1, testSuite.getTestCases().size());
        testCase = testSuite.getTestCases().get(0);
        assertEquals("testLogin", testCase.getName());
    }

    public void testParseLogIssue169433() throws Exception {
        Reader reader = new BufferedReader(new FileReader(new File(getDataDir(), "phpunit-log-issue169433.xml")));
        TestSessionVO testSession = new TestSessionVO();

        PhpUnitLogParser.parse(reader, testSession);

        assertSame(4, testSession.getTestSuites().size());
        TestSuiteVO testSuite = testSession.getTestSuites().get(0);
        assertEquals("E2_ConfigTest", testSuite.getName());
        assertEquals("/home/gapon/tmp/buga/SvnIPMCore/ipmcore/tests/ipmcore/lib/e2/E2/E2_ConfigTest.php", testSuite.getFile());

        assertSame(4, testSuite.getTestCases().size());
        TestCaseVO testCase = testSuite.getTestCases().get(0);
        assertEquals("test__get", testCase.getName());
        testCase = testSuite.getTestCases().get(3);
        assertEquals("testIterator", testCase.getName());

        testSuite = testSession.getTestSuites().get(1);
        assertEquals("E2_ConfigTest::testConstructException", testSuite.getName());
        assertEquals("/home/gapon/tmp/buga/SvnIPMCore/ipmcore/tests/ipmcore/lib/e2/E2/E2_ConfigTest.php", testSuite.getFile());

        assertSame(3, testSuite.getTestCases().size());
        testCase = testSuite.getTestCases().get(0);
        assertEquals("testConstructException with data set #0", testCase.getName());
        testCase = testSuite.getTestCases().get(2);
        assertEquals("testConstructException with data set #2", testCase.getName());

        testSuite = testSession.getTestSuites().get(3);
        assertEquals("E2_Crypt_EncryptTest", testSuite.getName());
        assertEquals("/home/gapon/tmp/buga/SvnIPMCore/ipmcore/tests/ipmcore/lib/e2/E2/Crypt/E2_Crypt_McryptTest.php", testSuite.getFile());

        assertSame(1, testSuite.getTestCases().size());
        testCase = testSuite.getTestCases().get(0);
        assertEquals("testDecryption", testCase.getName());
    }

    private File getLogForMoreSuites() throws Exception {
        File xmlLog = new File(getDataDir(), "phpunit-log-more-suites.xml");
        assertTrue(xmlLog.isFile());
        return xmlLog;
    }

    private File getLogForOneSuite() throws Exception {
        File xmlLog = new File(getDataDir(), "phpunit-log-one-suite.xml");
        assertTrue(xmlLog.isFile());
        return xmlLog;
    }
}
