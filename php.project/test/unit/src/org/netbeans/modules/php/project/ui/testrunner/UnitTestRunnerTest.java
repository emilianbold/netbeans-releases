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
import org.netbeans.modules.php.project.ui.testrunner.TestSessionVO.TestCaseVO;
import org.netbeans.modules.php.project.ui.testrunner.TestSessionVO.TestSuiteVO;

/**
 * @author Tomas Mysik
 */
public class UnitTestRunnerTest extends NbTestCase {

    public UnitTestRunnerTest(String name) {
        super(name);
    }

    public void testParseLogWithMoreSuites() throws Exception {
        Reader reader = new BufferedReader(new FileReader(getLogForMoreSuites()));
        TestSessionVO testSession = new TestSessionVO();

        UnitTestRunner.PhpUnitLogParser.parse(reader, testSession);

        assertEquals(104, testSession.getTime());
        assertEquals(10, testSession.getTests());

        // test suites & test cases
        assertEquals(3, testSession.getTestSuites().size());

        // 1st
        TestSuiteVO testSuite = testSession.getTestSuites().get(0);
        assertEquals("Calculator2Test", testSuite.getName());
        assertEquals("/home/gapon/NetBeansProjects/PhpProject01/tests/hola/Calculator2Test.php", testSuite.getFile());
        assertEquals(54, testSuite.getTime());
        assertEquals(5, testSuite.getTestCases().size());

        TestCaseVO testCase = testSuite.getTestCases().get(0);
        assertEquals("testAdd", testCase.getName());
        assertEquals("/home/gapon/NetBeansProjects/PhpProject01/tests/hola/Calculator2Test.php", testCase.getFile());
        assertEquals(43, testCase.getLine());
        assertEquals(12, testCase.getTime());

        // 2nd
        testSuite = testSession.getTestSuites().get(1);
        assertEquals("NoTestClassTest", testSuite.getName());
        assertEquals(0, testSuite.getTestCases().size());

        // 3rd
        testSuite = testSession.getTestSuites().get(2);
        assertEquals("CalculatorTest", testSuite.getName());
        assertEquals(5, testSuite.getTestCases().size());

        testCase = testSuite.getTestCases().get(1);
        assertEquals("testAdd2", testCase.getName());
        assertNotNull(testCase.getFailure());
        assertNull(testCase.getError());
        assertEquals(1, testCase.getStacktrace().length);
        assertEquals("Failed asserting that two objects are equal.\n--- Expected\n+++ Actual\n@@ -1,3 +1 @@\n-MyObject Object\n-(\n-)\n+77\n\\ Chybí znak konce řádku na konci souboru", testCase.getFailure());
        assertEquals("at /home/gapon/NetBeansProjects/PhpProject01/tests/CalculatorTest.php:56", testCase.getStacktrace()[0]);

        testCase = testSuite.getTestCases().get(2);
        assertEquals("testAdd3", testCase.getName());
        assertNotNull(testCase.getFailure());
        assertNull(testCase.getError());
        assertEquals(1, testCase.getStacktrace().length);
        assertEquals("my expected message\nFailed asserting that two strings are equal.\nexpected string <hello>\ndifference      < x???>\ngot string      <hi>", testCase.getFailure());
        assertEquals("at /home/gapon/NetBeansProjects/PhpProject01/tests/CalculatorTest.php:64", testCase.getStacktrace()[0]);

        testCase = testSuite.getTestCases().get(3);
        assertEquals("testAdd4", testCase.getName());
        assertNotNull(testCase.getFailure());
        assertNull(testCase.getError());
        assertEquals(1, testCase.getStacktrace().length);
        assertEquals("Failed asserting that <integer:2> matches expected value <integer:3>.", testCase.getFailure());
        assertEquals("at /home/gapon/NetBeansProjects/PhpProject01/tests/CalculatorTest.php:75", testCase.getStacktrace()[0]);

        testCase = testSuite.getTestCases().get(4);
        assertEquals("testAdd5", testCase.getName());
        assertNotNull(testCase.getError());
        assertNull(testCase.getFailure());
        assertEquals(2, testCase.getStacktrace().length);
        assertEquals("Exception: my exception", testCase.getError());
        assertEquals("at /home/gapon/NetBeansProjects/PhpProject01/src/Calculator.php:13", testCase.getStacktrace()[0]);
        assertEquals("at /home/gapon/NetBeansProjects/PhpProject01/tests/CalculatorTest.php:82", testCase.getStacktrace()[1]);
    }

    public void testParseLogWithOneSuite() throws Exception {
        Reader reader = new BufferedReader(new FileReader(getLogForOneSuite()));
        TestSessionVO testSession = new TestSessionVO();

        UnitTestRunner.PhpUnitLogParser.parse(reader, testSession);

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
