/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.nette.tester.run;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.spi.testing.run.TestCase;

public class TapParserTest extends NbTestCase {

    private static final String EXPECTED_CONTENT = "expected value";
    private static final String ACTUAL_CONTENT = "actual value";


    public TapParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        putFileContent("Greeting.test.expected", EXPECTED_CONTENT);
        putFileContent("Greeting.test.actual", ACTUAL_CONTENT);
    }

    public void testParse() throws Exception {
        // prepare content
        String log = getFileContent("nette-tester-tap.log");
        log = log.replace("%DIFF_BASE_DIR%", getWorkDirPath() + File.separator);
        // parse it
        TestSuiteVo suite = new TapParser()
                .parse(log, 600L);
        assertEquals("Tests", suite.getName());
        assertEquals(null, suite.getFile());

        List<TestCaseVo> testCases = suite.getTestCases();
        assertEquals(6, testCases.size());

        TestCaseVo testCase1 = testCases.get(0);
        assertEquals("nette-tester/tests/MyTests.phpt", testCase1.getName());
        assertEquals(TestCase.Status.PASSED, testCase1.getStatus());
        assertNull(testCase1.getMessage());
        assertNull(testCase1.getDiff());
        assertTrue(testCase1.getStackTrace().isEmpty());
        assertNull(testCase1.getFile());
        assertEquals(100L, testCase1.getTime());

        TestCaseVo testCase2 = testCases.get(1);
        assertEquals("nette-tester/tests/SkippedTests.phpt", testCase2.getName());
        assertEquals(TestCase.Status.SKIPPED, testCase2.getStatus());
        assertNull(testCase2.getMessage());
        assertNull(testCase2.getDiff());
        assertTrue(testCase1.getStackTrace().isEmpty());
        assertNull(testCase2.getFile());
        assertEquals(100L, testCase2.getTime());


        TestCaseVo testCase3 = testCases.get(2);
        assertEquals("nette-tester/tests/Greeting.test.phpt", testCase3.getName());
        assertEquals(TestCase.Status.FAILED, testCase3.getStatus());
        assertEquals("'Hello JohnX' should be 10", testCase3.getMessage());
        assertEquals(Arrays.asList(
                "in Tester/Framework/Assert.php(365)",
                "in Tester/Framework/Assert.php(57) Tester\\Assert::fail()",
                "in nette-tester/tests/Greeting.test.phpt(16) Tester\\Assert::same()"), testCase3.getStackTrace());
        assertNull(testCase3.getDiff());
        assertEquals("nette-tester/tests/Greeting.test.phpt", testCase3.getFile());
        assertEquals(16, testCase3.getLine());
        assertEquals(100L, testCase3.getTime());

        TestCaseVo testCase4 = testCases.get(3);
        assertEquals("nette-tester/tests/Greeting2.test.phpt", testCase4.getName());
        assertEquals(TestCase.Status.FAILED, testCase4.getStatus());
        assertEquals("'Hello JohnX' should be 'Hello John'", testCase4.getMessage());
        TestCase.Diff diff = testCase4.getDiff();
        assertNotNull(diff);
        assertEquals(EXPECTED_CONTENT, diff.getExpected());
        assertEquals(ACTUAL_CONTENT, diff.getActual());
        assertEquals(Arrays.asList(
                "in Tester/Framework/Assert.php(365)",
                "in Tester/Framework/Assert.php(57) Tester\\Assert::fail()",
                "in nette-tester/tests/Greeting2.test.phpt(15) Tester\\Assert::same()"), testCase4.getStackTrace());
        assertEquals("nette-tester/tests/Greeting2.test.phpt", testCase4.getFile());
        assertEquals(15, testCase4.getLine());
        assertEquals(100L, testCase4.getTime());

        TestCaseVo testCase5 = testCases.get(4);
        assertEquals("nette-tester/tests/Greeting3.test.phpt", testCase5.getName());
        assertEquals(TestCase.Status.FAILED, testCase5.getStatus());
        assertEquals("InvalidArgumentException was expected, but none was thrown", testCase5.getMessage());
        assertNull(testCase5.getDiff());
        assertEquals(Arrays.asList(
                "in Tester/Framework/Assert.php(365)",
                "in Tester/Framework/Assert.php(244) Tester\\Assert::fail()",
                "in nette-tester/tests/Greeting3.test.phpt(15) Tester\\Assert::exception()"), testCase5.getStackTrace());
        assertEquals("nette-tester/tests/Greeting3.test.phpt", testCase5.getFile());
        assertEquals(15, testCase5.getLine());
        assertEquals(100L, testCase5.getTime());

        TestCaseVo testCase6 = testCases.get(5);
        assertEquals("nette-tester/tests/Greeting4.test.phpt", testCase6.getName());
        assertEquals(TestCase.Status.FAILED, testCase6.getStatus());
        assertEquals("E_NOTICE with a message matching 'Undefined property: Greeting::$abc' was expected"
                + " but got 'Undefined property: Greeting::$say'", testCase6.getMessage());
        assertNull(testCase6.getDiff());
        assertEquals(Arrays.asList(
                "in Tester/Framework/Assert.php(365)",
                "in Tester/Framework/Assert.php(300) Tester\\Assert::fail()",
                "in nette-tester/tests/Greeting4.test.phpt(14) Tester\\Assert::Tester\\{closure}()",
                "in [internal function] {closure}()",
                "in Tester/Framework/Assert.php(304) call_user_func()",
                "in nette-tester/tests/Greeting4.test.phpt(15) Tester\\Assert::error()"), testCase6.getStackTrace());
        assertEquals("nette-tester/tests/Greeting4.test.phpt", testCase6.getFile());
        assertEquals(15, testCase6.getLine());
        assertEquals(100L, testCase6.getTime());
    }

    private String getFileContent(String filePath) throws IOException {
        File file = new File(getDataDir(), filePath);
        assertTrue(file.getAbsolutePath(), file.isFile());
        return new String(Files.readAllBytes(file.toPath()));
    }

    private void putFileContent(String filePath, String content) throws IOException {
        File file = new File(getWorkDir(), filePath);
        Files.write(file.toPath(), content.getBytes("UTF-8"));
    }

}
