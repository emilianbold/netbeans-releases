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

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.spi.testing.run.TestCase;
import org.openide.util.NbBundle;

public final class JsonParser {

    private static final Logger LOGGER = Logger.getLogger(JsonParser.class.getName());

    private static final Pattern SPLIT_PATTERN = Pattern.compile("\\}\\{"); // NOI18N
    private static final String INCOMPLETE_TEST_PREFIX = "Incomplete Test: "; // NOI18N
    private static final String SKIPPED_TEST_PREFIX = "Skipped Test: "; // NOI18N

    private final Handler handler;
    private final JSONParser parser = new JSONParser();
    private final TestSessionVo actualSession;
    private final TestSuiteVo noSuite;

    private TestSuiteVo actualSuite = null;
    private TestCaseVo actualTest = null;


    @NbBundle.Messages("JsonParser.suite.none=&lt;no suite>")
    public JsonParser(@NonNull Handler handler, @NullAllowed String customSuitePath) {
        assert handler != null;
        this.handler = handler;
        actualSession = new TestSessionVo(customSuitePath);
        noSuite = new TestSuiteVo(Bundle.JsonParser_suite_none());
    }

    public boolean parse(String input) {
        if (!actualSession.isStarted()) {
            actualSession.setStarted(true);
            handler.onSessionStart(actualSession);
        }
        String data = input;
        if (StringUtils.isEmpty(data)) {
            return true;
        }
        String[] parts = SPLIT_PATTERN.split(data);
        int count = parts.length;
        boolean result = true;
        for (int i = 0; i < count; i++) {
            String part = parts[i];
            StringBuilder buffer = new StringBuilder(part.length() + 2);
            if (i != 0) {
                buffer.append('{'); // NOI18N
            }
            buffer.append(part);
            if (i != count - 1) {
                buffer.append('}'); // NOI18N
            }
            if (!parseJson(buffer.toString())) {
                result = false;
            }
        }
        return result;
    }

    public void finish() {
        LOGGER.log(Level.FINE, "Parse finish");
        // finish last suite, if any exists
        suiteFinish();
        // process <no suite>
        if (!noSuite.getPureTestCases().isEmpty()) {
            actualSession.addTestSuite(noSuite);
            handler.onSuiteStart(noSuite);
            for (TestCaseVo testCaseVo : noSuite.getPureTestCases()) {
                handler.onTestStart(testCaseVo);
                handler.onTestFinish(testCaseVo);
            }
            handler.onSuiteFinish(noSuite);
        }
        handler.onSessionFinish(actualSession);
    }

    private boolean parseJson(String input) {
        LOGGER.log(Level.FINE, "JSON: {0}", input);
        JSONObject data;
        try {
            data = (JSONObject) parser.parse(input);
        } catch (ParseException ex) {
            LOGGER.log(Level.INFO, input, ex);
            return false;
        }
        assert data != null : input;
        String event = (String) data.get("event"); // NOI18N
        switch (event) {
            case "suiteStart": // NOI18N
                suiteFinish();
                suiteStart(data);
                break;
            case "testStart": // NOI18N
                testStart(data);
                break;
            case "test": // NOI18N
                testFinish(data);
                break;
            default:
                assert false : "Unknown event: " + event;
        }
        return true;
    }

    private void suiteStart(JSONObject data) {
        String suiteName = (String) data.get("suite"); // NOI18N
        assert suiteName != null : data;
        if (StringUtils.hasText(suiteName)) {
            actualSuite = new TestSuiteVo(suiteName);
            actualSession.addTestSuite(actualSuite);
            handler.onSuiteStart(actualSuite);
        }
    }

    private void suiteFinish() {
        if (isActualNoSuite()) {
            return;
        }
        if (actualSuite != null) {
            handler.onSuiteFinish(actualSuite);
        }
    }

    private void testStart(JSONObject data) {
        assert actualSuite != null;
        assert actualTest == null;
        String suite = (String) data.get("suite"); // NOI18N
        switch (suite) {
            case "": // NOI18N
                suite = null;
                if (!isActualNoSuite()) {
                    suiteFinish();
                    actualSuite = noSuite;
                }
            break;
            default:
                assert actualSuite.getName().equals(suite) : actualSuite + " != " + data;
        }
        String testName = (String) data.get("test"); // NOI18N
        assert testName != null : data;
        actualTest = new TestCaseVo(suite, extractTestName(testName));
        actualSuite.addTestCase(actualTest);
        if (!isActualNoSuite()) {
            handler.onTestStart(actualTest);
        }
    }

    private void testFinish(JSONObject data) {
        assert actualTest != null;
        String suite = (String) data.get("suite"); // NOI18N
        switch (suite) {
            case "": // NOI18N
                assert isActualNoSuite() : actualSuite;
            break;
            default:
                assert actualSuite.getName().equals(suite) : actualSuite + " != " + data;
        }
        String testName = (String) data.get("test"); // NOI18N
        assert testName != null : data;
        assert actualTest.getName().equals(extractTestName(testName)) : data + " != " + actualTest;
        Number time = (Number) data.get("time"); // NOI18N
        if (time instanceof Double) {
            actualTest.setTime((long) (time.doubleValue() * 1000));
        } else {
            assert time instanceof Long : time.getClass().getName();
            actualTest.setTime(time.longValue() * 1000);
        }
        String message = (String) data.get("message"); // NOI18N
        String status = (String) data.get("status"); // NOI18N
        assert status != null : data;
        switch (status) {
            case "pass": // NOI18N
                actualTest.setStatus(TestCase.Status.PASSED);
                break;
            case "fail": // NOI18N
                actualTest.setStatus(TestCase.Status.FAILED);
                break;
            case "error": // NOI18N
                TestCase.Status testStatus = TestCase.Status.ERROR;
                if (message != null) {
                    if (message.startsWith(INCOMPLETE_TEST_PREFIX)) {
                        message = message.substring(INCOMPLETE_TEST_PREFIX.length());
                        testStatus = TestCase.Status.PENDING;
                    } else if (message.startsWith(SKIPPED_TEST_PREFIX)) {
                        message = message.substring(SKIPPED_TEST_PREFIX.length());
                        testStatus = TestCase.Status.SKIPPED;
                    }
                }
                actualTest.setStatus(testStatus);
                break;
            default:
                assert false : "Unknown status: " + status;
        }
        if (message != null) {
            actualTest.addStacktrace(message);
        }
        JSONArray trace = (JSONArray) data.get("trace"); // NOI18N
        if (!trace.isEmpty()) {
            boolean first = true;
            for (Object object : trace) {
                JSONObject traceData = (JSONObject) object;
                String file = (String) traceData.get("file"); // NOI18N
                assert file != null : traceData;
                Long line = (Long) traceData.get("line"); // NOI18N
                actualTest.addStacktrace(file + ":" + line); // NOI18N
                if (first) {
                    first = false;
                    actualTest.setFile(file);
                    actualTest.setLine(line.intValue());
                }
            }
        }
        if (!isActualNoSuite()) {
            handler.onTestFinish(actualTest);
        }
        actualTest = null;
    }

    private String extractTestName(String testName) {
        if (isActualNoSuite()) {
            return testName;
        }
        String[] parts = testName.split("::", 2); // NOI18N
        if (parts.length == 2
                && StringUtils.hasText(parts[1])) {
            return parts[1];
        }
        return testName;
    }

    private boolean isActualNoSuite() {
        return noSuite == actualSuite;
    }

    //~ Inner classes

    public interface Handler {

        void onSessionStart(TestSessionVo testSessionVo);

        void onSessionFinish(TestSessionVo testSessionVo);

        void onSuiteStart(TestSuiteVo testSuiteVo);

        void onSuiteFinish(TestSuiteVo testSuiteVo);

        void onTestStart(TestCaseVo testCaseVo);

        void onTestFinish(TestCaseVo testCaseVo);

    }

}
