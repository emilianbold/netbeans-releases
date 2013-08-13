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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.php.spi.testing.run.TestCase;

public final class TapParser {

    private static enum State {
        NOT_OK,
    }

    private static final String OK_PREFIX = "ok "; // NOI18N
    private static final String NOT_OK_PREFIX = "not ok "; // NOI18N
    private static final String SKIP_SUFFIX = " #skip"; // NOI18N
    private static final String HELLIP = "... "; // NOI18N
    private static final Pattern FILE_LINE_PATTERN = Pattern.compile("in ([^(]+)\\((\\d+)\\).*"); // NOI18N
    private static final Pattern DIFF_LINE_PATTERN = Pattern.compile("diff \"([^\"]+)\" \"([^\"]+)\""); // NOI18N

    private final TestSuiteVo testSuite = new TestSuiteVo();
    private final List<String> commentLines = new ArrayList<>();

    private TestCaseVo testCase = null;
    private int testCaseCount = 0;
    private State state = null;


    public TapParser() {
    }

    public TestSuiteVo parse(String input, long runtime) {
        for (String line : input.split("\n|\r")) { // NOI18N
            parseLine(line.trim());
        }
        processComments();
        setTimes(runtime);
        return testSuite;
    }

    private void parseLine(String line) {
        if (line.startsWith("1..") // NOI18N
                || line.startsWith("TAP version ")) { // NOI18N
            return;
        }
        if (line.startsWith(OK_PREFIX)) {
            processComments();
            assert state == null : state;
            line = line.substring(OK_PREFIX.length());
            if (checkSkipped(line)) {
                testCase.setStatus(TestCase.Status.SKIPPED);
                testCase = null;
            } else {
                addSuiteTest(line);
                testCase.setStatus(TestCase.Status.PASSED);
                testCase = null;
            }
        } else if (line.startsWith(NOT_OK_PREFIX)) {
            processComments();
            assert state == null : state;
            state = State.NOT_OK;
            addSuiteTest(line.substring(NOT_OK_PREFIX.length()));
            testCase.setStatus(TestCase.Status.FAILED);
        } else {
            processComment(line);
        }
    }

    private boolean checkSkipped(String line) {
        assert state == null : state;
        if (line.endsWith(SKIP_SUFFIX)) {
            addSuiteTest(line.substring(0, line.length() - SKIP_SUFFIX.length()));
            return true;
        }
        return false;
    }

    private void processComment(String line) {
        assert line.startsWith("#") : line;
        line = line.substring(1).trim();
        switch (state) {
            case NOT_OK:
                commentLines.add(line);
                break;
            default:
                assert false : "Unknown state: " + state;
        }
    }

    private void processComments() {
        if (commentLines.isEmpty()) {
            return;
        }
        assert testCase != null;
        // last line
        int lastIndex = commentLines.size() - 1;
        String lastLine = commentLines.get(lastIndex);
        setFileLine(lastLine);
        commentLines.remove(lastIndex);
        // rest
        StringBuilder message = null;
        List<String> stackTrace = new ArrayList<>();
        while (!commentLines.isEmpty()) {
            String firstLine = commentLines.get(0);
            commentLines.remove(0);
            if (firstLine.isEmpty()) {
                // ignore empty lines
            } else if (FILE_LINE_PATTERN.matcher(firstLine).matches()) { // NOI18N
                stackTrace.add(firstLine);
                stackTrace.addAll(processStackTrace(commentLines));
                commentLines.clear();
            } else if (firstLine.startsWith("diff \"")) { // NOI18N
                processDiff(firstLine);
            } else {
                if (message == null) {
                    message = new StringBuilder(200);
                }
                if (message.length() > 0) {
                    // unfortunately, \n not supported in the ui
                    if (firstLine.startsWith(HELLIP)) {
                        firstLine = firstLine.substring(HELLIP.length());
                    }
                    message.append(" "); // NOI18N
                }
                message.append(firstLine);
            }
        }
        if (message != null) {
            testCase.setMessage(message.toString());
        }
        // append file with line number
        stackTrace.add(lastLine);
        testCase.setStackTrace(stackTrace);
        // reset
        state = null;
    }

    private List<String> processStackTrace(List<String> lines) {
        List<String> stackTrace = new ArrayList<>(lines.size());
        for (String line : lines) {
            stackTrace.add(line);
        }
        return stackTrace;
    }

    private void processDiff(String line) {
        Matcher matcher = DIFF_LINE_PATTERN.matcher(line);
        assert matcher.matches() : line;
        testCase.setDiff(new TestCase.Diff(new DiffReader(matcher.group(1)), new DiffReader(matcher.group(2))));
    }

    private void addSuiteTest(String line) {
        String testName = line;
        testCase = new TestCaseVo(testName);
        testSuite.addTestCase(testCase);
        testCaseCount++;
    }

    private void setFileLine(String line) {
        Matcher matcher = FILE_LINE_PATTERN.matcher(line);
        assert matcher.matches() : line;
        assert testCase != null;
        String file = matcher.group(1);
        String fileLine = matcher.group(2);
        assert file != null : line;
        testCase.setFile(file);
        assert fileLine != null : line;
        testCase.setLine(Integer.valueOf(fileLine));
    }

    private void setTimes(long runtime) {
        long time = 0;
        if (testCaseCount > 0) {
            time = runtime / testCaseCount;
        }
        for (TestCaseVo kase : testSuite.getTestCases()) {
            kase.setTime(time);
        }
    }

    //~ Inner classes

    private static final class DiffReader implements Callable<String> {

        private final String filePath;


        public DiffReader(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public String call() throws IOException {
            return new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8"); // NOI18N
        }

    }

}
