/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.phpunit.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.executable.PhpExecutableValidator;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.phpunit.options.PhpUnitOptions;
import org.netbeans.modules.php.phpunit.preferences.PhpUnitPreferences;
import org.netbeans.modules.php.phpunit.ui.UiUtils;
import org.netbeans.modules.php.phpunit.ui.options.PhpUnitOptionsPanelController;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Represents <tt>phpunit-skelgen</tt> command line tool.
 */
public final class SkeletonGenerator {

    private static final Logger LOGGER = Logger.getLogger(SkeletonGenerator.class.getName());

    public static final String SCRIPT_NAME = "phpunit-skelgen"; // NOI18N
    public static final String SCRIPT_NAME_LONG = SCRIPT_NAME + FileUtils.getScriptExtension(true);

    // params
    private static final String BOOTSTRAP_PARAM = "--bootstrap"; // NOI18N
    private static final String TEST_PARAM = "--test"; // NOI18N
    private static final String SEPARATOR_PARAM = "--"; // NOI18N

    private final String skelGenPath;


    private SkeletonGenerator(String skelGenPath) {
        assert skelGenPath != null;
        this.skelGenPath = skelGenPath;
    }

    /**
     * Get the default, <b>valid only</b> SkeletonGenerator script.
     * @return the default, <b>valid only</b> SkeletonGenerator script
     * @throws InvalidPhpExecutableException if SkeletonGenerator script is not valid.
     */
    public static SkeletonGenerator getDefault() throws InvalidPhpExecutableException {
        String script = PhpUnitOptions.getInstance().getSkeletonGeneratorPath();
        String error = validate(script);
        if (error != null) {
            throw new InvalidPhpExecutableException(error);
        }
        return new SkeletonGenerator(script);
    }

    @NbBundle.Messages("SkeletonGenerator.script.label=Skeleton generator script")
    public static String validate(String command) {
        return PhpExecutableValidator.validateCommand(command, Bundle.SkeletonGenerator_script_label());
    }

    @NbBundle.Messages({
        "SkeletonGenerator.test.title=Skeleton Generator",
        "# {0} - file name",
        "SkeletonGenerator.test.generating=Creating test file for {0}"
    })
    public FileObject generateTest(PhpModule phpModule, FileObject sourceClassFile, String sourceClassName) throws ExecutionException {
        FileObject sourceDir = phpModule.getSourceDirectory();
        assert sourceDir != null;
        FileObject testDir = phpModule.getTestDirectory(sourceClassFile);
        assert testDir != null;
        FileObject commonRoot = FileUtils.getCommonRoot(sourceClassFile, testDir);
        if (commonRoot == null
                || !FileUtil.isParentOf(sourceDir, commonRoot)) {
            // look only inside project source dir
            commonRoot = sourceDir;
        }
        assert commonRoot != null;
        String relativePath = PropertyUtils.relativizeFile(FileUtil.toFile(commonRoot), FileUtil.toFile(sourceClassFile));
        assert relativePath != null;
        assert !relativePath.startsWith("../") : "Unexpected relative path: " + relativePath + " for " + commonRoot + " and " + sourceClassFile;
        String relativeTestPath = relativePath.substring(0, relativePath.length() - sourceClassFile.getExt().length() - 1);
        File testFile = PropertyUtils.resolveFile(FileUtil.toFile(testDir), PhpUnit.makeTestFile(relativeTestPath));
        FileObject testFo = FileUtil.toFileObject(testFile);
        if (testFo != null && testFo.isValid()) {
            return testFo;
        }
        if (!ensureTestFolderExists(testFile)) {
            return null;
        }
        String testClassName = PhpUnit.makeTestClass(sourceClassName);
        List<String> params = new ArrayList<>();
        if (PhpUnitPreferences.isBootstrapEnabled(phpModule)
                && PhpUnitPreferences.isBootstrapForCreateTests(phpModule)) {
            params.add(BOOTSTRAP_PARAM);
            params.add(PhpUnitPreferences.getBootstrapPath(phpModule));
        }
        params.add(TEST_PARAM);
        params.add(SEPARATOR_PARAM);
        params.add(sanitizeClassName(sourceClassName));
        params.add(FileUtil.toFile(sourceClassFile).getAbsolutePath());
        params.add(sanitizeClassName(testClassName));
        params.add(testFile.getAbsolutePath());

        PhpExecutable skelGen = getExecutable(phpModule, Bundle.SkeletonGenerator_test_generating(sourceClassFile.getNameExt()), params);
        if (skelGen == null) {
            return null;
        }
        try {
            Integer status = skelGen.runAndWait(getDescriptor(), "Generating test..."); // NOI18N
            if (status != null
                    && status == 0) {
                // refresh fs
                FileUtil.refreshFor(testFile.getParentFile());
                testFo = FileUtil.toFileObject(testFile);
                assert testFo != null : "FileObject must be found for " + testFile;
                return testFo;
            }
        } catch (CancellationException ex) {
            // canceled
            LOGGER.log(Level.FINE, "Test creating cancelled", ex);
        }
        return null;
    }

    @CheckForNull
    private PhpExecutable getExecutable(PhpModule phpModule, String title, List<String> params) {
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            UiUtils.warnNoSources(phpModule.getDisplayName());
            return null;
        }

        return new PhpExecutable(skelGenPath)
                .optionsSubcategory(PhpUnitOptionsPanelController.OPTIONS_SUB_PATH)
                .workDir(FileUtil.toFile(sourceDirectory))
                .displayName(title)
                .additionalParameters(params);
    }

    private ExecutionDescriptor getDescriptor() {
        return PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .optionsPath(PhpUnitOptionsPanelController.OPTIONS_PATH)
                .inputVisible(false);
    }

    // https://github.com/sebastianbergmann/phpunit-skeleton-generator/issues/1
    private String sanitizeClassName(String className) {
        if (className.startsWith("\\")) { // NOI18N
            className = className.substring(1);
        }
        return className;
    }

    // #210123
    private boolean ensureTestFolderExists(File testClassFile) {
        File parent = testClassFile.getParentFile();
        if (!parent.isDirectory()) {
            if (!parent.mkdirs()) {
                return false;
            }
            FileUtil.refreshFor(parent);
        }
        return true;
    }

}
