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
import java.util.Arrays;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.RerunHandler;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.options.PHPOptionsCategory;
import org.netbeans.modules.php.project.ui.testrunner.UnitTestRunner;
import org.netbeans.modules.php.project.util.Pair;
import org.netbeans.modules.php.project.util.PhpUnit;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Action implementation for TEST configuration.
 * It means running and debugging tests.
 * @author Tomas Mysik
 */
public class ConfigActionTest extends ConfigAction {
    private static final String CWD = "."; // NOI18N

    protected ConfigActionTest(PhpProject project) {
        super(project);
    }

    @Override
    public boolean isDebugProjectEnabled() {
        throw new IllegalStateException("Debug project tests action is not supported");
    }

    @Override
    public boolean isRunFileEnabled(Lookup context) {
        FileObject rootFolder = ProjectPropertiesSupport.getTestDirectory(project, false);
        assert rootFolder != null : "Test directory not found but isRunFileEnabled() for a test file called?!";
        FileObject file = CommandUtils.fileForContextOrSelectedNodes(context, rootFolder);
        return file != null && CommandUtils.isPhpFile(file);
    }

    @Override
    public boolean isDebugFileEnabled(Lookup context) {
        if (XDebugStarterFactory.getInstance() == null) {
            return false;
        }
        return isRunFileEnabled(context);
    }

    @Override
    public void runProject() {
        PhpUnit phpUnit = CommandUtils.getPhpUnit(false);
        if (!phpUnit.supportedVersionFound()) {
            int[] version = phpUnit.getVersion();
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(ConfigActionTest.class, "MSG_OldPhpUnit", PhpUnit.getVersions(version)),
                    NotifyDescriptor.WARNING_MESSAGE));
            return;
        }

        run();
    }

    @Override
    public void debugProject() {
        throw new IllegalStateException("Debug project tests action is not supported");
    }

    @Override
    public void runFile(Lookup context) {
        run(context);
    }

    @Override
    public void debugFile(Lookup context) {
        debug(context);
    }

    private void run() {
        run(null);
    }

    private void run(Lookup context) {
        Pair<FileObject, String> pair = getValidPair(context);
        if (pair == null) {
            return;
        }

        new RunScript(new RunScriptProvider(pair, context)).run();
    }

    private void debug(Lookup context) {
        Pair<FileObject, String> pair = getValidPair(context);
        if (pair == null) {
            return;
        }

        new DebugScript(new DebugScriptProvider(pair, context)).run();
    }

    private Pair<FileObject, String> getValidPair(Lookup context) {
        PhpUnit phpUnit = CommandUtils.getPhpUnit(true);
        if (phpUnit == null) {
            return null;
        }
        return getPair(context);
    }

    // <working directory, unit test name (script name without extension)>
    private Pair<FileObject, String> getPair(Lookup context) {
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

    private class RunScriptProvider implements RunScript.Provider {
        protected final Lookup context;
        protected final Pair<FileObject, String> pair;
        protected final PhpUnit phpUnit;
        protected final UnitTestRunner testRunner;
        protected final RerunUnitTestHandler rerunUnitTestHandler;

        public RunScriptProvider(Pair<FileObject, String> pair, Lookup context) {
            assert pair != null;

            this.pair = pair;
            this.context = context;
            rerunUnitTestHandler = getRerunUnitTestHandler();
            testRunner = getTestRunner();
            phpUnit = CommandUtils.getPhpUnit(false);
        }

        public ExecutionDescriptor getDescriptor() throws IOException {
            ExecutionDescriptor executionDescriptor = new ExecutionDescriptor()
                    .optionsPath(PHPOptionsCategory.PATH_IN_LAYER)
                    .frontWindow(!phpUnit.supportedVersionFound())
                    .showProgress(true);
            if (phpUnit.supportedVersionFound()) {
                executionDescriptor = executionDescriptor
                        .preExecution(new Runnable() {
                            public void run() {
                                testRunner.start();
                            }
                        })
                        .postExecution(new Runnable() {
                            public void run() {
                                rerunUnitTestHandler.enable();
                                testRunner.showResults();
                            }
                        });
            } else {
                executionDescriptor = executionDescriptor
                        .outProcessorFactory(new OutputProcessorFactory(phpUnit));
            }
            return executionDescriptor;
        }

        public ExternalProcessBuilder getProcessBuilder() {
            ExternalProcessBuilder externalProcessBuilder = new ExternalProcessBuilder(phpUnit.getProgram())
                    .workingDirectory(FileUtil.toFile(pair.first));
            for (String param : phpUnit.getParameters()) {
                externalProcessBuilder = externalProcessBuilder.addArgument(param);
            }
            externalProcessBuilder = externalProcessBuilder
                    .addArgument(PhpUnit.PARAM_XML_LOG)
                    .addArgument(PhpUnit.XML_LOG.getAbsolutePath())
                    .addArgument(PhpUnit.PARAM_COVERAGE_LOG)
                    .addArgument(PhpUnit.COVERAGE_LOG.getAbsolutePath())
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
            return String.format("%s - %s", phpUnit.getProgram(), title);
        }

        public boolean isValid() {
            return phpUnit.isValid() && pair.first != null && pair.second != null;
        }

        protected RerunUnitTestHandler getRerunUnitTestHandler() {
            return new RerunUnitTestHandler(context);
        }

        protected UnitTestRunner getTestRunner() {
            return new UnitTestRunner(project, TestSession.SessionType.TEST, rerunUnitTestHandler);
        }
    }

    private final class DebugScriptProvider extends RunScriptProvider implements DebugScript.Provider {
        protected final File startFile;

        public DebugScriptProvider(Pair<FileObject, String> pair, Lookup context) {
            super(pair, context);
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

        @Override
        protected RerunUnitTestHandler getRerunUnitTestHandler() {
            return new RedebugUnitTestHandler(context);
        }

        @Override
        protected UnitTestRunner getTestRunner() {
            assert rerunUnitTestHandler instanceof RedebugUnitTestHandler;
            return new UnitTestRunner(project, TestSession.SessionType.DEBUG, rerunUnitTestHandler);
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

    private class RerunUnitTestHandler implements RerunHandler {
        protected final Lookup context;
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private volatile boolean enabled = false;

        public RerunUnitTestHandler(Lookup context) {
            this.context = context;
        }

        public void rerun() {
            run(context);
        }

        public boolean enabled() {
            return enabled;
        }

        public void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener(listener);
        }

        public void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener(listener);
        }

        void enable() {
            enabled = true;
            changeSupport.fireChange();
        }
    }

    private class RedebugUnitTestHandler extends RerunUnitTestHandler {
        public RedebugUnitTestHandler(Lookup context) {
            super(context);
        }

        @Override
        public void rerun() {
            debug(context);
        }
    }

    static final class OutputProcessorFactory implements ExecutionDescriptor.InputProcessorFactory {
        private final PhpUnit phpUnit;

        public OutputProcessorFactory(PhpUnit phpUnit) {
            this.phpUnit = phpUnit;
        }

        public InputProcessor newInputProcessor(final InputProcessor defaultProcessor) {
            return new InputProcessor() {
                public void processInput(char[] chars) throws IOException {
                    defaultProcessor.processInput(chars);
                }
                public void reset() throws IOException {
                    defaultProcessor.reset();
                }
                public void close() throws IOException {
                    String msg = NbBundle.getMessage(ConfigActionTest.class, "MSG_OldPhpUnit", PhpUnit.getVersions(phpUnit.getVersion()));
                    char[] separator = new char[msg.length()];
                    Arrays.fill(separator, '='); // NOI18N
                    defaultProcessor.processInput("\n".toCharArray()); // NOI18N
                    defaultProcessor.processInput(separator);
                    defaultProcessor.processInput("\n".toCharArray()); // NOI18N
                    defaultProcessor.processInput(msg.toCharArray());
                    defaultProcessor.processInput("\n".toCharArray()); // NOI18N
                    defaultProcessor.processInput(separator);
                    defaultProcessor.processInput("\n".toCharArray()); // NOI18N
                    defaultProcessor.close();
                }
            };
        }
    }
}
