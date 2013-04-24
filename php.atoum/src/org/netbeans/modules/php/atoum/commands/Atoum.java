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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.executable.PhpExecutableValidator;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.atoum.options.AtoumOptions;
import org.netbeans.modules.php.atoum.ui.options.AtoumOptionsPanelController;
import org.netbeans.modules.php.spi.testing.run.TestRunException;
import org.netbeans.modules.php.spi.testing.run.TestRunInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

import static org.netbeans.modules.php.spi.testing.run.TestRunInfo.SessionType.DEBUG;
import static org.netbeans.modules.php.spi.testing.run.TestRunInfo.SessionType.TEST;

/**
 * Represents <tt>atoum</tt> or <tt>mageekguy.atoum.phar</tt>.
 */
public final class Atoum {

    private static final Logger LOGGER = Logger.getLogger(Atoum.class.getName());

    public static final String PHAR_FILE_NAME = "mageekguy.atoum.phar"; // NOI18N
    public static final String ATOUM_FILE_NAME = "atoum"; // NOI18N

    public static final Pattern LINE_PATTERN = Pattern.compile("^# ([^:]+):(\\d+)$"); // NOI18N

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
    public Integer runTests(PhpModule phpModule, TestRunInfo runInfo) throws TestRunException {
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
                return atoum.runAndWait(getDescriptor(), "Running tests..."); // NOI18N
            }
            return atoum.debug(runInfo.getStartFile(), getDescriptor(), null);
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

}
