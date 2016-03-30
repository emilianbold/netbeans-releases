/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.phpunit.run;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.spi.testing.run.TestCase;


public class JsonParserTest extends NbTestCase {

    private JsonParser jsonParser;
    private HandlerImpl handler;

    public JsonParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        handler = new HandlerImpl();
        jsonParser = new JsonParser(handler, null);
    }

    public void testParseLogWithOneSuite() throws Exception {
        jsonParser.parse(readContent("phpunit-log.json"));
        jsonParser.finish();
        TestSessionVo session = handler.getSession();
        assertNotNull(session);
        List<TestSuiteVo> suites = session.getTestSuites();
        assertEquals(1, suites.size());
        TestSuiteVo suite = suites.get(0);
        assertEquals("CalculatorTest", suite.getName());
        assertEquals(156, suite.getTime());
        List<TestCaseVo> tests = suite.getTestCases();
        assertEquals(17, tests.size());
        List<String> expectedTests = Arrays.asList(
            "TestCaseVo{name: testPlus, file: null, line: null, time: 11, status: PASSED, stacktrace: []}",
            "TestCaseVo{name: testPlus2, file: /home/gapon/NetBeansProjects/Calculator-PHPUnit8/test/src/CalculatorTest.php, line: 48, time: 19, status: FAILED, "
                    + "stacktrace: [Failed asserting that 2 matches expected 1., /home/gapon/NetBeansProjects/Calculator-PHPUnit8/test/src/CalculatorTest.php:48]}",
            "TestCaseVo{name: testPlus3, file: null, line: null, time: 8, status: PASSED, stacktrace: []}",
            "TestCaseVo{name: testPlus4, file: null, line: null, time: 8, status: PASSED, stacktrace: []}",
            "TestCaseVo{name: testMinus, file: /home/gapon/NetBeansProjects/Calculator-PHPUnit8/test/src/CalculatorTest.php, line: 79, time: 9, status: ERROR, "
                    + "stacktrace: [hola hoj, /home/gapon/NetBeansProjects/Calculator-PHPUnit8/test/src/CalculatorTest.php:79]}",
            "TestCaseVo{name: testMinus2, file: null, line: null, time: 9, status: PASSED, stacktrace: []}",
            "TestCaseVo{name: testMinus3, file: null, line: null, time: 9, status: PASSED, stacktrace: []}",
            "TestCaseVo{name: testMinus4, file: null, line: null, time: 8, status: PASSED, stacktrace: []}",
            "TestCaseVo{name: testMultiply, file: null, line: null, time: 8, status: PASSED, stacktrace: []}",
            "TestCaseVo{name: testMultiply2, file: null, line: null, time: 9, status: PASSED, stacktrace: []}",
            "TestCaseVo{name: testMultiply3, file: null, line: null, time: 9, status: PASSED, stacktrace: []}",
            "TestCaseVo{name: testMultiply4, file: null, line: null, time: 8, status: PASSED, stacktrace: []}",
            "TestCaseVo{name: testMultiply5, file: null, line: null, time: 8, status: PASSED, stacktrace: []}",
            "TestCaseVo{name: testDivide, file: null, line: null, time: 8, status: PASSED, stacktrace: []}",
            "TestCaseVo{name: testDivide2, file: null, line: null, time: 8, status: PASSED, stacktrace: []}",
            "TestCaseVo{name: testDivide3, file: null, line: null, time: 8, status: PASSED, stacktrace: []}",
            "TestCaseVo{name: testModulo, file: /home/gapon/NetBeansProjects/Calculator-PHPUnit8/test/src/CalculatorTest.php, line: 214, time: 9, status: PENDING, "
                    + "stacktrace: [This test has not been implemented yet., "
                    + "/home/gapon/NetBeansProjects/Calculator-PHPUnit8/test/src/CalculatorTest.php:214]}"
        );
        for (int i = 0; i < 17; i++) {
            assertEquals(expectedTests.get(i), tests.get(i).toString());
        }
    }

    public void testParsePhpUnitLog() throws Exception {
        jsonParser.parse(readContent("phpunit.json"));
        jsonParser.finish();
        TestSessionVo session = handler.getSession();
        assertNotNull(session);
        List<TestSuiteVo> suites = session.getTestSuites();
        assertEquals(50, suites.size());
        // check e.g. Framework_BaseTestListenerTest
        TestSuiteVo suite = suites.get(13);
        assertEquals("Framework_BaseTestListenerTest", suite.getName());
        List<TestCaseVo> tests = suite.getPureTestCases();
        assertEquals(1, tests.size());
        TestCaseVo test = tests.get(0);
        assertEquals("testEndEventsAreCounted", test.getName());
        assertEquals("Framework_BaseTestListenerTest", test.getClassName());
        assertEquals(TestCase.Status.PASSED, test.getStatus());
        assertEquals(5L, test.getTime());
    }

    public void testIssue258312() throws Exception {
        // log file created manually (project from report does not exist anymore)
        jsonParser.parse(readContent("issue258312.json"));
        jsonParser.finish();
        TestSessionVo session = handler.getSession();
        assertNotNull(session);
        List<TestSuiteVo> suites = session.getTestSuites();
        assertEquals(1, suites.size());
        TestSuiteVo suite = suites.get(0);
        assertEquals("CalculatorTest", suite.getName());
        List<TestCaseVo> tests = suite.getPureTestCases();
        assertEquals(1, tests.size());
        TestCaseVo test = tests.get(0);
        assertEquals("testPlus", test.getName());
        assertEquals(TestCase.Status.PASSEDWITHERRORS, test.getStatus());
    }

    private String readContent(String filename) throws IOException {
        byte[] bytes = Files.readAllBytes(getPath(filename));
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private Path getPath(String filename) {
        Path path = new File(getDataDir(), filename).toPath();
        assertTrue("File should exist: " + path.toAbsolutePath(), Files.exists(path));
        return path;
    }

    //~ Inner classes

    private final class HandlerImpl implements JsonParser.Handler {

        private TestSessionVo session = null;


        @CheckForNull
        public TestSessionVo getSession() {
            return session;
        }

        @Override
        public void onSessionStart(TestSessionVo testSessionVo) {
            assertNotNull(testSessionVo);
            assertNull(session);
            session = testSessionVo;
        }

        @Override
        public void onSessionFinish(TestSessionVo testSessionVo) {
            assertNotNull(testSessionVo);
            assertNotNull(session);
        }

        @Override
        public void onSuiteStart(TestSuiteVo testSuiteVo) {
            assertNotNull(testSuiteVo);
        }

        @Override
        public void onSuiteFinish(TestSuiteVo testSuiteVo) {
            assertNotNull(testSuiteVo);
        }

        @Override
        public void onTestStart(TestCaseVo testCaseVo) {
            assertNotNull(testCaseVo);
        }

        @Override
        public void onTestFinish(TestCaseVo testCaseVo) {
            assertNotNull(testCaseVo);
        }

    }

}
