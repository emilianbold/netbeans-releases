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
package org.netbeans.modules.php.nette.tester.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.executable.PhpExecutableValidator;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.nette.tester.TesterTestingProvider;
import org.netbeans.modules.php.nette.tester.options.TesterOptions;
import org.netbeans.modules.php.nette.tester.run.TapParser;
import org.netbeans.modules.php.nette.tester.run.TestCaseVo;
import org.netbeans.modules.php.nette.tester.run.TestSuiteVo;
import org.netbeans.modules.php.nette.tester.ui.options.TesterOptionsPanelController;
import org.netbeans.modules.php.spi.testing.locate.Locations;
import org.netbeans.modules.php.spi.testing.run.TestCase;
import org.netbeans.modules.php.spi.testing.run.TestRunException;
import org.netbeans.modules.php.spi.testing.run.TestRunInfo;
import org.netbeans.modules.php.spi.testing.run.TestSession;
import org.netbeans.modules.php.spi.testing.run.TestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

import static org.netbeans.modules.php.spi.testing.run.TestRunInfo.SessionType.DEBUG;
import static org.netbeans.modules.php.spi.testing.run.TestRunInfo.SessionType.TEST;

/**
 * Represents <tt>tester</tt>.
 */
public final class Tester {

    private static final Logger LOGGER = Logger.getLogger(Tester.class.getName());

    public static final String TESTER_FILE_NAME = "tester"; // NOI18N

    private static final String TESTER_PROJECT_FILE_PATH = "vendor/nette/tester/Tester/tester"; // NOI18N

    private static final String TAP_FORMAT_PARAM = "--tap"; // NOI18N
    private static final String SKIP_INFO_PARAM = "-s"; // NOI18N

    private final String testerPath;


    private Tester(String testerPath) {
        assert testerPath != null;
        this.testerPath = testerPath;
    }

    public static Tester getDefault() throws InvalidPhpExecutableException {
        String script = TesterOptions.getInstance().getTesterPath();
        String error = validate(script);
        if (error != null) {
            throw new InvalidPhpExecutableException(error);
        }
        return new Tester(script);
    }

    @CheckForNull
    public static Tester getForPhpModule(PhpModule phpModule) throws InvalidPhpExecutableException {
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            return null;
        }
        FileObject fileObject = sourceDirectory.getFileObject(TESTER_PROJECT_FILE_PATH);
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
        return new Tester(path);
    }

    @NbBundle.Messages("Tester.file.label=Tester file")
    public static String validate(String command) {
        return PhpExecutableValidator.validateCommand(command, Bundle.Tester_file_label());
    }

    public static boolean isTestMethod(PhpClass.Method method) {
        return method.getName().startsWith("test"); // NOI18N
    }

    @CheckForNull
    public Integer runTests(PhpModule phpModule, TestRunInfo runInfo, final TestSession testSession) throws TestRunException {
        PhpExecutable tester = getExecutable(phpModule, getOutputTitle(runInfo));
        List<String> params = new ArrayList<>();
        params.add(TAP_FORMAT_PARAM);
        params.add(SKIP_INFO_PARAM);
        if (runInfo.isCoverageEnabled()) {
            // XXX add coverage params once tester supports it
            LOGGER.info("Nette Tester currently does not support code coverage via command line");
        }
        // custom tests
        List<TestRunInfo.TestInfo> customTests = runInfo.getCustomTests();
        if (customTests.isEmpty()) {
            File startFile = FileUtil.toFile(runInfo.getStartFile());
            params.add(startFile.getAbsolutePath());
        } else {
            for (TestRunInfo.TestInfo testInfo : customTests) {
                String location = testInfo.getLocation();
                assert location != null : testInfo;
                params.add(new File(location).getAbsolutePath());
            }
            runInfo.resetCustomTests();
        }
        tester.additionalParameters(params);
        try {
            if (runInfo.getSessionType() == TestRunInfo.SessionType.TEST) {
                return tester.runAndWait(getDescriptor(), new ParsingFactory(testSession), "Running tester tests..."); // NOI18N
            }
            return tester.debug(runInfo.getStartFile(), getDescriptor(), new ParsingFactory(testSession));
        } catch (CancellationException ex) {
            // canceled
            LOGGER.log(Level.FINE, "Test running cancelled", ex);
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
            UiUtils.processExecutionException(ex, TesterOptionsPanelController.OPTIONS_SUB_PATH);
            throw new TestRunException(ex);
        }
        return null;
    }

    private PhpExecutable getExecutable(PhpModule phpModule, String title) {
        FileObject testDirectory = phpModule.getTestDirectory();
        assert testDirectory != null : "Test directory not found for " + phpModule.getName();
        return new PhpExecutable(testerPath)
                .optionsSubcategory(TesterOptionsPanelController.OPTIONS_SUB_PATH)
                .workDir(FileUtil.toFile(testDirectory))
                .displayName(title);
    }

    private ExecutionDescriptor getDescriptor() {
        return PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .optionsPath(TesterOptionsPanelController.OPTIONS_PATH)
                .frontWindowOnError(false)
                .inputVisible(false);
    }

    @NbBundle.Messages({
        "Tester.run.test.single=Nette Tester (test)",
        "Tester.run.test.all=Nette Tester (test all)",
        "Tester.debug.single=Nette Tester (debug)",
        "Tester.debug.all=Nette Tester (debug all)",
    })
    private String getOutputTitle(TestRunInfo runInfo) {
        boolean allTests = runInfo.allTests();
        switch (runInfo.getSessionType()) {
            case TEST:
                if (allTests) {
                    return Bundle.Tester_run_test_all();
                }
                return Bundle.Tester_run_test_single();
                //break;
            case DEBUG:
                if (allTests) {
                    return Bundle.Tester_debug_all();
                }
                return Bundle.Tester_debug_single();
                //break;
            default:
                throw new IllegalStateException("Unknown session type: " + runInfo.getSessionType());
        }
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
            TestSuiteVo suite = new TapParser()
                    .parse(input, currentMillis() - currentMillis);
            LOGGER.log(Level.FINE, "Parsed test suites: {0}", suite);
            // XXX remove once the output TAP format is perfectly known
            try {
                process(suite);
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
                LOGGER.log(Level.FINE, "Test suite {0} found, finishing", testSuite);
                testSuite.finish(testSuiteTime);
            }
        }

        private void process(TestSuiteVo suite) {
            if (testSuite == null) {
                testSuite = testSession.addTestSuite(suite.getName(), getFileObject(suite.getFile()));
            }
            addTestCases(suite.getTestCases());
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
                TestCase testCase = testSuite.addTestCase(name, TesterTestingProvider.IDENTIFIER);
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
            testCase.setStatus(kase.getStatus());
            mapLocation(kase, testCase);
            mapFailureInfo(kase, testCase);
            testCase.setTime(kase.getTime());
        }

        private void mapLocation(TestCaseVo kase, TestCase testCase) {
            // XXX - see https://github.com/nette/tester/issues/46
            if (true) return;
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
                if (kase.getMessage() != null) {
                    // skipped test with message
                    mapFailureInfoInternal(kase, testCase);
                }
                assert kase.getDiff() == null : kase.getDiff();
                return;
            }
            mapFailureInfoInternal(kase, testCase);
        }

        private void mapFailureInfoInternal(TestCaseVo kase, TestCase testCase) {
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
            return status == TestCase.Status.PASSED
                    || status == TestCase.Status.SKIPPED;
        }

        private boolean isError(TestCase.Status status) {
            return status == TestCase.Status.ERROR;
        }

    }

}
