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
package org.netbeans.modules.php.atoum.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.executable.PhpExecutableValidator;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.atoum.AtoumTestingProvider;
import org.netbeans.modules.php.atoum.options.AtoumOptions;
import org.netbeans.modules.php.atoum.run.TapParser;
import org.netbeans.modules.php.atoum.run.TestCaseVo;
import org.netbeans.modules.php.atoum.run.TestSuiteVo;
import org.netbeans.modules.php.atoum.ui.options.AtoumOptionsPanelController;
import org.netbeans.modules.php.spi.testing.locate.Locations;
import org.netbeans.modules.php.spi.testing.run.TestCase;
import org.netbeans.modules.php.spi.testing.run.TestRunException;
import org.netbeans.modules.php.spi.testing.run.TestRunInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

import static org.netbeans.modules.php.spi.testing.run.TestRunInfo.SessionType.DEBUG;
import static org.netbeans.modules.php.spi.testing.run.TestRunInfo.SessionType.TEST;
import org.netbeans.modules.php.spi.testing.run.TestSession;
import org.netbeans.modules.php.spi.testing.run.TestSuite;

/**
 * Represents <tt>atoum</tt> or <tt>mageekguy.atoum.phar</tt>.
 */
public final class Atoum {

    private static final Logger LOGGER = Logger.getLogger(Atoum.class.getName());

    public static final String PHAR_FILE_NAME = "mageekguy.atoum.phar"; // NOI18N
    public static final String ATOUM_FILE_NAME = "atoum"; // NOI18N

    public static final Pattern LINE_PATTERN = Pattern.compile("([^:]+):(\\d+)"); // NOI18N

    private static final String ATOUM_PROJECT_FILE_PATH = "vendor/atoum/atoum/bin/atoum"; // NOI18N

    private static final String TAP_FORMAT_PARAM = "-utr"; // NOI18N
    private static final String DIRECTORY_PARAM = "-d"; // NOI18N
    private static final String FILE_PARAM = "-f"; // NOI18N
    private static final String FILTER_PARAM = "-m"; // NOI18N

    private final String atoumPath;


    private Atoum(String atoum) {
        assert atoum != null;
        this.atoumPath = atoum;
    }

    public static Atoum getDefault() throws InvalidPhpExecutableException {
        String script = AtoumOptions.getInstance().getAtoumPath();
        String error = validate(script);
        if (error != null) {
            throw new InvalidPhpExecutableException(error);
        }
        return new Atoum(script);
    }

    @CheckForNull
    public static Atoum getForPhpModule(PhpModule phpModule) throws InvalidPhpExecutableException {
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            return null;
        }
        FileObject fileObject = sourceDirectory.getFileObject(ATOUM_PROJECT_FILE_PATH);
        if (fileObject == null) {
            return getDefault();
        }
        File file = FileUtil.toFile(fileObject);
        assert file != null : "File not found fileobject: " + fileObject;
        String path = file.getAbsolutePath();
        String error = validate(path);
        if (error != null) {
            throw new InvalidPhpExecutableException(error);
        }
        return new Atoum(path);
    }

    @NbBundle.Messages("Atoum.file.label=atoum file")
    public static String validate(String command) {
        return PhpExecutableValidator.validateCommand(command, Bundle.Atoum_file_label());
    }

    public static boolean isTestMethod(PhpClass.Method method) {
        return method.getName().startsWith("test"); // NOI18N
    }

    @CheckForNull
    public Integer runTests(PhpModule phpModule, TestRunInfo runInfo, final TestSession testSession) throws TestRunException {
        PhpExecutable atoum = getExecutable(phpModule, getOutputTitle(runInfo));
        List<String> params = new ArrayList<>();
        params.add(TAP_FORMAT_PARAM);
        if (runInfo.isCoverageEnabled()) {
            // XXX add coverage params once atoum supports it
            LOGGER.info("Atoum currently does not support code coverage via command line");
        }
        // custom tests
        List<TestRunInfo.TestInfo> customTests = runInfo.getCustomTests();
        if (!customTests.isEmpty()) {
            StringBuilder buffer = new StringBuilder(200);
            for (TestRunInfo.TestInfo test : customTests) {
                if (buffer.length() > 1) {
                    buffer.append(" "); // NOI18N
                }
                String className = test.getClassName();
                assert className != null : "No classname for test: " + test.getName();
                buffer.append(sanitizeClassName(className));
                buffer.append("::"); // NOI18N
                buffer.append(test.getName());
            }
            params.add(FILTER_PARAM);
            params.add(buffer.toString());
            runInfo.resetCustomTests();
        }
        File startFile = FileUtil.toFile(runInfo.getStartFile());
        if (startFile.isFile()) {
            params.add(FILE_PARAM);
        } else {
            params.add(DIRECTORY_PARAM);
        }
        params.add(startFile.getAbsolutePath());
        atoum.additionalParameters(params);
        try {
            if (runInfo.getSessionType() == TestRunInfo.SessionType.TEST) {
                return atoum.runAndWait(getDescriptor(), new ParsingFactory(testSession), "Running atoum tests..."); // NOI18N
            }
            LOGGER.info("Debugging atoum tests is not possible");
            return atoum.debug(runInfo.getStartFile(), getDescriptor(), new ParsingFactory(testSession));
        } catch (CancellationException ex) {
            // canceled
            LOGGER.log(Level.FINE, "Test creating cancelled", ex);
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
            UiUtils.processExecutionException(ex, AtoumOptionsPanelController.OPTIONS_SUB_PATH);
            throw new TestRunException(ex);
        }
        return null;
    }

    private PhpExecutable getExecutable(PhpModule phpModule, String title) {
        FileObject testDirectory = phpModule.getTestDirectory();
        assert testDirectory != null : "Test directory not found for " + phpModule.getName();
        return new PhpExecutable(atoumPath)
                .optionsSubcategory(AtoumOptionsPanelController.OPTIONS_SUB_PATH)
                .workDir(FileUtil.toFile(testDirectory))
                .displayName(title);
    }

    private ExecutionDescriptor getDescriptor() {
        return PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .optionsPath(AtoumOptionsPanelController.OPTIONS_PATH)
                .frontWindowOnError(false)
                .inputVisible(false);
    }

    @NbBundle.Messages({
        "Atoum.run.test.single=atoum (test)",
        "Atoum.run.test.all=atoum (test all)",
        "Atoum.debug.single=atoum (debug)",
        "Atoum.debug.all=atoum (debug all)",
    })
    private String getOutputTitle(TestRunInfo runInfo) {
        boolean allTests = runInfo.allTests();
        switch (runInfo.getSessionType()) {
            case TEST:
                if (allTests) {
                    return Bundle.Atoum_run_test_all();
                }
                return Bundle.Atoum_run_test_single();
                //break;
            case DEBUG:
                if (allTests) {
                    return Bundle.Atoum_debug_all();
                }
                return Bundle.Atoum_debug_single();
                //break;
            default:
                throw new IllegalStateException("Unknown session type: " + runInfo.getSessionType());
        }
    }

    private String sanitizeClassName(String className) {
        if (className.startsWith("\\")) { // NOI18N
            return className.substring(1);
        }
        return className;
    }

    //~ Inner classes

    private static final class ParsingFactory implements ExecutionDescriptor.InputProcessorFactory {

        private final TestSession testSession;


        private ParsingFactory(TestSession testSession) {
            assert testSession != null;
            this.testSession = testSession;
        }

        @Override
        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return new ParsingProcessor(testSession);
        }

    }

    private static final class ParsingProcessor implements InputProcessor {

        private static final Logger LOGGER = Logger.getLogger(ParsingProcessor.class.getName());

        private final TestSession testSession;

        private String testSuiteName = null;
        private TestSuite testSuite = null;
        private long currentMillis = currentMillis();
        private long testSuiteTime = 0;


        public ParsingProcessor(TestSession testSession) {
            assert testSession != null;
            this.testSession = testSession;
        }

        private static long currentMillis() {
            return System.currentTimeMillis();
        }

        @Override
        public void processInput(char[] chars) throws IOException {
            String input = new String(chars);
            LOGGER.log(Level.FINEST, "Processing input {0}", input);
            List<TestSuiteVo> suites = new TapParser()
                    .parse(input, currentMillis() - currentMillis);
            LOGGER.log(Level.FINE, "Parsed test suites: {0}", suites);
            // XXX remove once the output TAP format is perfectly known
            try {
                process(suites);
            } catch (Throwable throwable) {
                LOGGER.log(Level.WARNING, null, throwable);
            }
            currentMillis = currentMillis();
        }

        @Override
        public void reset() throws IOException {
            // noop
        }

        @Override
        public void close() throws IOException {
            LOGGER.fine("Closing processor");
            if (testSuite != null) {
                LOGGER.log(Level.FINE, "Test suite {0} found, finishing", testSuiteName);
                testSuite.finish(testSuiteTime);
            }
        }

        private void process(List<TestSuiteVo> suites) {
            for (TestSuiteVo suite : suites) {
                String name = suite.getName();
                if (testSuiteName == null
                        || !testSuiteName.equals(name)) {
                    if (testSuite != null) {
                        LOGGER.log(Level.FINE, "Finishing the current suite {0}", testSuiteName);
                        testSuite.finish(testSuiteTime);
                        testSuiteTime = 0;
                    }
                    testSuiteName = name;
                    LOGGER.log(Level.FINE, "Adding new test suite {0}", name);
                    testSuite = testSession.addTestSuite(name, getFileObject(suite.getFile()));
                }
                addTestCases(suite.getTestCases());
            }
        }

        private FileObject getFileObject(String path) {
            if (path == null) {
                return null;
            }
            FileObject fileObject = FileUtil.toFileObject(new File(path));
            assert fileObject != null : "Cannot find file object for: " + path;
            return fileObject;
        }

        private void addTestCases(List<TestCaseVo> testCases) {
            for (TestCaseVo kase : testCases) {
                String name = kase.getName();
                LOGGER.log(Level.FINE, "Adding new test case {0}", name);
                TestCase testCase = testSuite.addTestCase(name, AtoumTestingProvider.IDENTIFIER);
                // XXX remove once the output TAP format is perfectly known
                try {
                    map(kase, testCase);
                } catch (Throwable throwable) {
                    LOGGER.log(Level.WARNING, null, throwable);
                }
                testSuiteTime += kase.getTime();
            }
        }

        private void map(TestCaseVo kase, TestCase testCase) {
            testCase.setClassName(testSuiteName);
            testCase.setStatus(kase.getStatus());
            mapLocation(kase, testCase);
            mapFailureInfo(kase, testCase);
            testCase.setTime(kase.getTime());
        }

        private void mapLocation(TestCaseVo kase, TestCase testCase) {
            String file = kase.getFile();
            if (file == null) {
                return;
            }
            FileObject fileObject = FileUtil.toFileObject(new File(file));
            assert fileObject != null : "Cannot find file object for file: " + file;
            testCase.setLocation(new Locations.Line(fileObject, kase.getLine()));
        }

        private void mapFailureInfo(TestCaseVo kase, TestCase testCase) {
            if (isPass(kase.getStatus())) {
                assert kase.getMessage() == null : kase.getMessage();
                assert kase.getDiff() == null : kase.getDiff();
                return;
            }
            String message = kase.getMessage();
            assert message != null : kase;
            List<String> stackTrace = kase.getStackTrace();
            if (stackTrace == null) {
                stackTrace = Collections.emptyList();
            }
            TestCase.Diff diff = kase.getDiff();
            if (diff == null) {
                diff = TestCase.Diff.NOT_KNOWN;
            }
            testCase.setFailureInfo(message, stackTrace.toArray(new String[stackTrace.size()]), isError(kase.getStatus()), diff);
        }

        private boolean isPass(TestCase.Status status) {
            return status == TestCase.Status.PASSED;
        }

        private boolean isError(TestCase.Status status) {
            return status == TestCase.Status.ERROR;
        }

    }

}
