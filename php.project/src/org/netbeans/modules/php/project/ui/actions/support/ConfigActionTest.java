/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.actions.support;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.actions.tests.PhpUnitConstants;
import org.netbeans.modules.php.project.ui.options.PHPOptionsCategory;
import org.netbeans.modules.php.project.util.Pair;
import org.netbeans.modules.php.project.util.PhpUnit;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Action implementation for TEST configuration.
 * It means running and debugging tests.
 * @author Tomas Mysik
 */
public class ConfigActionTest extends ConfigAction {
    private static final String CWD = "."; // NOI18N

    @Override
    public boolean isRunProjectEnabled(PhpProject project) {
        return isRunProjectEnabled();
    }

    @Override
    public boolean isDebugProjectEnabled(PhpProject project) {
        throw new IllegalStateException("Debug project tests action is not supported");
    }

    @Override
    public boolean isRunFileEnabled(PhpProject project, Lookup context) {
        FileObject rootFolder = ProjectPropertiesSupport.getTestDirectory(project, false);
        assert rootFolder != null : "Test directory not found but isRunFileEnabled() for a test file called?!";
        FileObject file = CommandUtils.fileForContextOrSelectedNodes(context, rootFolder);
        return file != null && CommandUtils.isPhpFile(file);
    }

    @Override
    public boolean isDebugFileEnabled(PhpProject project, Lookup context) {
        if (XDebugStarterFactory.getInstance() == null) {
            return false;
        }
        return isRunFileEnabled(project, context);
    }

    @Override
    public void runProject(PhpProject project) {
        run(project, null);
    }

    @Override
    public void debugProject(PhpProject project) {
        throw new IllegalStateException("Debug project tests action is not supported");
    }

    @Override
    public void runFile(PhpProject project, Lookup context) {
        run(project, context);
    }

    @Override
    public void debugFile(PhpProject project, Lookup context) {
        debug(project, context);
    }

    private void run(PhpProject project, Lookup context) {
        Pair<FileObject, String> pair = getValidPair(project, context);
        if (pair == null) {
            return;
        }

        new RunScript(new ScriptProvider(project, pair, context)).run();
    }

    private void debug(PhpProject project, Lookup context) {
        Pair<FileObject, String> pair = getValidPair(project, context);
        if (pair == null) {
            return;
        }

        new DebugScript(new ScriptProvider(project, pair, context)).run();
    }

    private Pair<FileObject, String> getValidPair(PhpProject project, Lookup context) {
        PhpUnit phpUnit = CommandUtils.getPhpUnit(true);
        if (phpUnit == null) {
            return null;
        }
        return getPair(project, context);
    }

    // <working directory, unit test name (script name without extension)>
    private Pair<FileObject, String> getPair(PhpProject project, Lookup context) {
        FileObject testDirectory = ProjectPropertiesSupport.getTestDirectory(project, true);
        if (testDirectory == null) {
            return null;
        }
        if (context == null) {
            return getProjectPair(testDirectory);
        }
        return getFilePair(testDirectory, context);
    }

    private Pair<FileObject, String> getProjectPair(FileObject testDirectory) {
        assert testDirectory != null : "Test directory should be defined for running a test file";
        return Pair.of(testDirectory, CWD);
    }

    private Pair<FileObject, String> getFilePair(FileObject testDirectory, Lookup context) {
        assert testDirectory != null : "Test directory should be defined for running a test file";
        FileObject fileObj = CommandUtils.fileForContextOrSelectedNodes(context, testDirectory);
        assert fileObj != null : "Fileobject not found for context: " + context;
        return Pair.of(fileObj.getParent(), fileObj.getName());
    }

    private final class ScriptProvider implements DebugScript.Provider {
        private final PhpProject project;
        private final Lookup context;
        private final Pair<FileObject, String> pair;
        private final PhpUnit program;
        private final File startFile;

        public ScriptProvider(PhpProject project, Pair<FileObject, String> pair, Lookup context) {
            assert project != null;
            assert pair != null;

            this.project = project;
            this.pair = pair;
            this.context = context;
            program = CommandUtils.getPhpUnit(false);
            startFile = getStartFile(context);
        }

        public Project getProject() {
            assert context != null : "Only particular test files can be debugged";
            assert startFile != null;
            return project;
        }

        public FileObject getStartFile() {
            assert context != null : "Only particular test files can be debugged";
            assert startFile != null;
            return FileUtil.toFileObject(startFile);
        }

        public ExecutionDescriptor getDescriptor() throws IOException {
            return new ExecutionDescriptor()
                    .frontWindow(true)
                    .showProgress(true)
                    .optionsPath(PHPOptionsCategory.PATH_IN_LAYER);
        }

        public ExternalProcessBuilder getProcessBuilder() {
            ExternalProcessBuilder externalProcessBuilder = new ExternalProcessBuilder(program.getProgram())
                    .workingDirectory(FileUtil.toFile(pair.first));
            for (String param : program.getParameters()) {
                externalProcessBuilder = externalProcessBuilder.addArgument(param);
            }
            externalProcessBuilder = externalProcessBuilder
                    .addArgument(PhpUnitConstants.PARAM_XML_LOG)
                    .addArgument(PhpUnitConstants.XML_LOG.getAbsolutePath())
                    .addArgument(pair.second);
            return externalProcessBuilder;
        }

        public String getOutputTabTitle() {
            String title = null;
            if (pair.second == CWD) {
                title = NbBundle.getMessage(ConfigActionTest.class, "LBL_UnitTestsForTestSourcesSuffix");
            } else {
                title = pair.second;
            }
            return String.format("%s - %s", program.getProgram(), title);
        }

        public boolean isValid() {
            return program.isValid() && pair.first != null && pair.second != null;
        }

        private File getStartFile(Lookup context) {
            if (context == null) {
                return null;
            }
            FileObject testRoot = ProjectPropertiesSupport.getTestDirectory(project, false);
            assert testRoot != null : "Test root must be known already";
            FileObject file = CommandUtils.fileForContextOrSelectedNodes(context, testRoot);
            assert file != null : "Start file must be found";
            return FileUtil.toFile(file);
        }
    }
}
