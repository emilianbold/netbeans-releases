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

package org.netbeans.modules.javascript.karma.exec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.javascript.karma.preferences.KarmaPreferences;
import org.netbeans.modules.javascript.karma.preferences.KarmaPreferencesValidator;
import org.netbeans.modules.javascript.karma.ui.customizer.KarmaCustomizer;
import org.netbeans.modules.javascript.karma.util.ExternalExecutable;
import org.netbeans.modules.javascript.karma.util.ValidationResult;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

public final class KarmaExecutable {

    private static final Logger LOGGER = Logger.getLogger(KarmaExecutable.class.getName());

    private static final String START_COMMAND = "start";
    private static final String RUN_COMMAND = "run";
    private static final String PORT_PARAMETER = "--port";

    private final Project project;
    private final String karmaPath;


    private KarmaExecutable(Project project) {
        assert project != null;
        this.project = project;
        karmaPath = KarmaPreferences.getInstance().getKarma(project);
        assert karmaPath != null;
    }

    @CheckForNull
    public static KarmaExecutable forProject(Project project, boolean showCustomizer) {
        ValidationResult result = new KarmaPreferencesValidator()
                .validate(project)
                .getResult();
        if (validateResult(result) != null) {
            if (showCustomizer) {
                project.getLookup().lookup(CustomizerProvider2.class).showCustomizer(KarmaCustomizer.IDENTIFIER, null);
            }
            return null;
        }
        return new KarmaExecutable(project);
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "KarmaExecutable.start=Karma ({0})",
    })
    @CheckForNull
    public Future<Integer> start(int port) {
        List<String> params = new ArrayList<>(4);
        params.add(START_COMMAND);
        // XXX
        params.add("config/karma-netbeans.conf.js");
        params.add(PORT_PARAMETER);
        params.add(Integer.toString(port));
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        Runnable countDownTask = new Runnable() {
            @Override
            public void run() {
                countDownLatch.countDown();
            }
        };
        Future<Integer> task = getExecutable(Bundle.KarmaExecutable_start(ProjectUtils.getInformation(project).getDisplayName()), getProjectDir())
                .additionalParameters(params)
                .run(getStartDescriptor(countDownTask), new ServerProcessorFactory(countDownTask));
        assert task != null : karmaPath;
        try {
            countDownLatch.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        if (task.isDone()) {
            // some error, task is not running
            return null;
        }
        return task;
    }

    public void runTests(int port) {
        List<String> params = new ArrayList<>(3);
        params.add(RUN_COMMAND);
        params.add(PORT_PARAMETER);
        params.add(Integer.toString(port));
        getExecutable("karma run...", getProjectDir()) // NOI18N
                .additionalParameters(params)
                // XXX wait?
                .run(getRunDescriptor());
    }

    private File getProjectDir() {
        return FileUtil.toFile(project.getProjectDirectory());
    }

    private ExternalExecutable getExecutable(String title, File workDir) {
        return new ExternalExecutable(karmaPath)
                .workDir(workDir)
                .displayName(title)
                .noOutput(false);
    }

    private ExecutionDescriptor getStartDescriptor(Runnable postTask) {
        return new ExecutionDescriptor()
                .frontWindow(false)
                .frontWindowOnError(false)
                .outLineBased(true)
                .errLineBased(true)
                .postExecution(postTask);
    }

    private ExecutionDescriptor getRunDescriptor() {
        return new ExecutionDescriptor()
                .inputOutput(InputOutput.NULL);
    }

    @CheckForNull
    private static String validateResult(ValidationResult result) {
        if (result.isFaultless()) {
            return null;
        }
        if (result.hasErrors()) {
            return result.getErrors().get(0).getMessage();
        }
        return result.getWarnings().get(0).getMessage();
    }

    //~ Inner classes

    private static final class ServerProcessorFactory implements ExecutionDescriptor.InputProcessorFactory {

        private final Runnable startFinishedTask;


        public ServerProcessorFactory(@NullAllowed Runnable startFinishedTask) {
            this.startFinishedTask = startFinishedTask;
        }

        @Override
        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return InputProcessors.bridge(new ServerLineProcessor(startFinishedTask));
        }

    }

    private static final class ServerLineProcessor implements LineProcessor {

        private final Runnable startFinishedTask;

        private volatile boolean startFinishedTaskRun = false;


        public ServerLineProcessor(@NullAllowed Runnable startFinishedTask) {
            this.startFinishedTask = startFinishedTask;
        }

        @Override
        public void processLine(String line) {
            if (startFinishedTask != null
                    && !startFinishedTaskRun
                    && line.contains("Connected on socket")) { // NOI18N
                startFinishedTask.run();
                startFinishedTaskRun = true;
            }
            if (line.startsWith("--netbeans[")) {
                // XXX
            }
        }

        @Override
        public void reset() {
        }

        @Override
        public void close() {
        }

    }

}
