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
package org.netbeans.modules.php.project.phpunit;

import java.io.File;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.modules.php.project.deprecated.PhpProgram;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Represents <tt>phpunit-skelgen</tt> command line tool.
 */
public final class PhpUnitSkelGen extends PhpProgram {

    public static final String SCRIPT_NAME = "phpunit-skelgen"; // NOI18N
    public static final String SCRIPT_NAME_LONG = SCRIPT_NAME + FileUtils.getScriptExtension(true);

    private static final String BOOTSTRAP_PARAM = "--bootstrap"; // NOI18N
    private static final String TEST_PARAM = "--test"; // NOI18N
    private static final String SEPARATOR_PARAM = "--"; // NOI18N


    private PhpUnitSkelGen(String command) {
        super(command);
    }

    /**
     * Get the default, <b>valid only</b> PhpUnitSkelGen script.
     * @return the default, <b>valid only</b> PhpUnitSkelGen script.
     * @throws InvalidPhpProgramException if PhpUnitSkelGen script is not valid.
     */
    public static PhpUnitSkelGen getDefault() throws InvalidPhpProgramException {
        String script = PhpOptions.getInstance().getPhpUnitSkelGen();
        String error = validate(script);
        if (error != null) {
            throw new InvalidPhpProgramException(error);
        }
        return new PhpUnitSkelGen(script);
    }

    public static String validate(String command) {
        return new PhpUnitSkelGen(command).validate();
    }

    @NbBundle.Messages("PhpUnitSkelGen.script.label=Skeleton generator script")
    @Override
    public String validate() {
        return FileUtils.validateFile(Bundle.PhpUnitSkelGen_script_label(), getProgram(), false);
    }

    @NbBundle.Messages({
        "# {0} - file name",
        "PhpUnitSkelGen.test.generating=Creating test file for {0}"
    })
    public File generateTest(PhpUnit.ConfigFiles configFiles, String sourceClassName, File sourceClassFile, String testClassName, File testClassFile) {
        if (testClassFile.isFile()) {
            // file already exists
            return testClassFile;
        }
        if (!ensureTestFolderExists(testClassFile)) {
            return null;
        }
        ExternalProcessBuilder processBuilder = getProcessBuilder();
        if (configFiles.bootstrap != null
                && configFiles.useBootstrapForCreateTests) {
            processBuilder = processBuilder
                    .addArgument(BOOTSTRAP_PARAM)
                    .addArgument(configFiles.bootstrap.getAbsolutePath());
        }
        processBuilder = processBuilder
                .addArgument(TEST_PARAM)
                .addArgument(SEPARATOR_PARAM)
                .addArgument(sanitizeClassName(sourceClassName))
                .addArgument(sourceClassFile.getAbsolutePath())
                .addArgument(sanitizeClassName(testClassName))
                .addArgument(testClassFile.getAbsolutePath());
        ExecutionDescriptor executionDescriptor = getExecutionDescriptor()
                .inputVisible(false)
                .frontWindow(true)
                .optionsPath(PhpUnit.OPTIONS_PATH);
        try {
            int status = executeAndWait(
                    processBuilder,
                    executionDescriptor,
                    Bundle.PhpUnitSkelGen_test_generating(sourceClassName));
            if (status == 0) {
                // refresh fs
                FileUtil.refreshFor(testClassFile.getParentFile());
                return testClassFile;
            }
        } catch (CancellationException ex) {
            // canceled
        } catch (ExecutionException ex) {
            UiUtils.processExecutionException(ex, PhpUnit.OPTIONS_SUB_PATH);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return null;
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
