/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.gulp.exec;

import java.awt.EventQueue;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.extexecution.base.input.InputProcessors;
import org.netbeans.api.extexecution.base.input.LineProcessor;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.gulp.GulpBuildTool;
import org.netbeans.modules.javascript.gulp.file.Gulpfile;
import org.netbeans.modules.javascript.gulp.options.GulpOptions;
import org.netbeans.modules.javascript.gulp.options.GulpOptionsValidator;
import org.netbeans.modules.javascript.gulp.ui.options.GulpOptionsPanelController;
import org.netbeans.modules.javascript.gulp.util.FileUtils;
import org.netbeans.modules.javascript.gulp.util.GulpUtils;
import org.netbeans.modules.web.clientproject.api.util.StringUtilities;
import org.netbeans.modules.web.common.api.ExternalExecutable;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;

public class GulpExecutable {

    static final Logger LOGGER = Logger.getLogger(GulpExecutable.class.getName());

    public static final String GULP_NAME;

    private static final String NO_COLOR_PARAM = "--no-color"; // NOI18N
    private static final String COLOR_PARAM = "--color"; // NOI18N
    private static final String TASKS_PARAM = "--tasks-simple"; // NOI18N
    private static final String SILENT_PARAM = "--silent"; // NOI18N

    protected final Project project;
    protected final String gulpPath;


    static {
        if (Utilities.isWindows()) {
            GULP_NAME = "gulp.cmd"; // NOI18N
        } else {
            GULP_NAME = "gulp"; // NOI18N
        }
    }


    GulpExecutable(String gulpPath, @NullAllowed Project project) {
        assert gulpPath != null;
        this.gulpPath = gulpPath;
        this.project = project;
    }

    @CheckForNull
    public static GulpExecutable getDefault(@NullAllowed Project project, boolean showOptions) {
        ValidationResult result = new GulpOptionsValidator()
                .validateGulp()
                .getResult();
        if (validateResult(result) != null) {
            if (showOptions) {
                OptionsDisplayer.getDefault().open(GulpOptionsPanelController.OPTIONS_PATH);
            }
            return null;
        }
        return createExecutable(GulpOptions.getInstance().getGulp(), project);
    }

    private static GulpExecutable createExecutable(String gulp, Project project) {
        if (Utilities.isMac()) {
            return new MacGulpExecutable(gulp, project);
        }
        return new GulpExecutable(gulp, project);
    }

    String getCommand() {
        return gulpPath;
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "GulpExecutable.run=Gulp ({0})",
    })
    public Future<Integer> run(String... args) {
        assert !EventQueue.isDispatchThread();
        assert project != null;
        String projectName = GulpUtils.getProjectDisplayName(project);
        Future<Integer> task = getExecutable(Bundle.GulpExecutable_run(projectName))
                .additionalParameters(getRunParams(args))
                .run(getDescriptor());
        assert task != null : gulpPath;
        return task;
    }

    public Future<List<String>> listTasks() {
        final GulpTasksLineProcessor gulpTasksLineProcessor = new GulpTasksLineProcessor();
        Future<Integer> task = getExecutable("list gulp tasks") // NOI18N
                .noInfo(true)
                .additionalParameters(Arrays.asList(NO_COLOR_PARAM, SILENT_PARAM, TASKS_PARAM))
                .redirectErrorStream(false)
                .run(getSilentDescriptor(), new ExecutionDescriptor.InputProcessorFactory2() {
                    @Override
                    public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                        return InputProcessors.bridge(gulpTasksLineProcessor);
                    }
                });
        assert task != null : gulpPath;
        return new TaskList(task, gulpTasksLineProcessor);
    }

    private ExternalExecutable getExecutable(String title) {
        assert title != null;
        return new ExternalExecutable(getCommand())
                .workDir(getWorkDir())
                .displayName(title)
                .optionsPath(GulpOptionsPanelController.OPTIONS_PATH)
                .noOutput(false);
    }

    private ExecutionDescriptor getDescriptor() {
        assert project != null;
        return ExternalExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .showSuspended(true)
                .optionsPath(GulpOptionsPanelController.OPTIONS_PATH)
                .outLineBased(true)
                .errLineBased(true)
                .postExecution(new Runnable() {
                    @Override
                    public void run() {
                        // #246886
                        FileUtil.refreshFor(getWorkDir());
                    }
                });
    }

    private static ExecutionDescriptor getSilentDescriptor() {
        return new ExecutionDescriptor()
                .inputOutput(InputOutput.NULL)
                .inputVisible(false)
                .frontWindow(false)
                .showProgress(false)
                .charset(StandardCharsets.UTF_8)
                .outLineBased(true);
    }

    private File getWorkDir() {
        if (project == null) {
            return FileUtils.TMP_DIR;
        }
        Gulpfile gulpfile = GulpBuildTool.forProject(project).getGulpfile();
        if (gulpfile.exists()) {
            return gulpfile.getFile().getParentFile();
        }
        File workDir = FileUtil.toFile(project.getProjectDirectory());
        assert workDir != null : project.getProjectDirectory();
        return workDir;
    }

    private List<String> getRunParams(String... args) {
        List<String> params = new ArrayList<>(args.length + 1);
        params.addAll(Arrays.asList(args));
        params.add(COLOR_PARAM);
        return getParams(params);
    }

    List<String> getParams(List<String> params) {
        assert params != null;
        return params;
    }

    @CheckForNull
    private static String validateResult(ValidationResult result) {
        if (result.isFaultless()) {
            return null;
        }
        if (result.hasErrors()) {
            return result.getFirstErrorMessage();
        }
        return result.getFirstWarningMessage();
    }

    //~ Inner classes

    private static final class MacGulpExecutable extends GulpExecutable {

        private static final String BASH_COMMAND = "/bin/bash -lc"; // NOI18N


        MacGulpExecutable(String gulpPath, Project project) {
            super(gulpPath, project);
        }

        @Override
        String getCommand() {
            return BASH_COMMAND;
        }

        @Override
        List<String> getParams(List<String> params) {
            StringBuilder sb = new StringBuilder(200);
            sb.append("\""); // NOI18N
            sb.append(gulpPath);
            sb.append("\" \""); // NOI18N
            sb.append(StringUtilities.implode(super.getParams(params), "\" \"")); // NOI18N
            sb.append("\""); // NOI18N
            return Collections.singletonList(sb.toString());
        }

    }

    private static final class GulpTasksLineProcessor implements LineProcessor {

        final List<String> tasks = new ArrayList<>();


        @Override
        public void processLine(String line) {
            if (StringUtilities.hasText(line)) {
                tasks.add(line);
            }
        }

        @Override
        public void reset() {
            // noop
        }

        @Override
        public void close() {
            // noop
        }

        public List<String> getTasks() {
            return Collections.unmodifiableList(tasks);
        }

    }

    private static final class TaskList implements Future<List<String>> {

        private final Future<Integer> task;
        private final GulpTasksLineProcessor processor;

        // @GuardedBy("this")
        private List<String> gulpTasks = null;


        TaskList(Future<Integer> task, GulpTasksLineProcessor processor) {
            assert task != null;
            assert processor != null;
            this.task = task;
            this.processor = processor;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return task.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return task.isCancelled();
        }

        @Override
        public boolean isDone() {
            return task.isDone();
        }

        @Override
        public List<String> get() throws InterruptedException, ExecutionException {
            try {
                task.get();
            } catch (CancellationException ex) {
                // cancelled by user
                LOGGER.log(Level.FINE, null, ex);
            }
            return getGulpTasks();
        }

        @Override
        public List<String> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            try {
                task.get(timeout, unit);
            } catch (CancellationException ex) {
                // cancelled by user
                LOGGER.log(Level.FINE, null, ex);
            }
            return getGulpTasks();
        }

        private synchronized List<String> getGulpTasks() {
            if (gulpTasks != null) {
                return Collections.unmodifiableList(gulpTasks);
            }
            List<String> tasks = new ArrayList<>(processor.getTasks());
            Collections.sort(tasks);
            gulpTasks = new CopyOnWriteArrayList<>(tasks);
            return Collections.unmodifiableList(gulpTasks);
        }

    }

}
