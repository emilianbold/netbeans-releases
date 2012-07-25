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
package org.netbeans.modules.php.project.ui.actions.support;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.executable.PhpInterpreter;
import org.netbeans.modules.php.api.util.Pair;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.runconfigs.RunConfigScript;
import org.netbeans.modules.php.project.spi.XDebugStarter;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;

/**
 * Run or debug a file.
 * <p>
 * This class is thread safe.
 */
public final class FileRunner {

    static final Logger LOGGER = Logger.getLogger(FileRunner.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(FileRunner.class);
    private static final ExecutionDescriptor.LineConvertorFactory PHP_LINE_CONVERTOR_FACTORY = new PhpLineConvertorFactory();

    final PhpProject project;
    final File file;

    // @GuardedBy("this")
    Map<String, String> environmentVariables = Collections.emptyMap();
    // @GuardedBy("this")
    volatile boolean controllable = true;


    public FileRunner(PhpProject project, File file) {
        this.project = project;
        this.file = file;
    }

    public synchronized FileRunner environmentVariables(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
        return this;
    }

    public FileRunner controllable(boolean controllable) {
        this.controllable = controllable;
        return this;
    }

    @NbBundle.Messages({
        "# {0} - project or file name",
        "FileRunner.run.displayName={0} (run)"
    })
    public void run() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    getRunCallable(Bundle.FileRunner_run_displayName(project.getName())).call();
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            }
        });
    }

    public void debug() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    debugInternal();
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            }
        });
    }

    Callable<Cancellable> getRunCallable(final String displayName) {
        return new Callable<Cancellable>() {
            @Override
            public Cancellable call() {
                assert !EventQueue.isDispatchThread();

                RunConfigScript configScript = RunConfigScript.forProject(project);
                PhpExecutable executable = new PhpExecutable(configScript.getInterpreter());
                // workdir
                String workDir = configScript.getWorkDir();
                if (StringUtils.hasText(workDir)) {
                    executable.workDir(new File(workDir));
                } else {
                    executable.workDir(file.getParentFile());
                }
                // open in browser or editor?
                Runnable postExecution = null;
                if (getRedirectToFile()) {
                    File tmpFile = createTempFile();
                    if (tmpFile != null) {
                        executable.fileOutput(tmpFile, false);
                        postExecution = new PostExecution(tmpFile);
                    }
                }
                // run!
                final Future<Integer> result = executable
                        .displayName(displayName)
                        .viaAutodetection(false)
                        .viaPhpInterpreter(false)
                        .environmentVariables(environmentVariables)
                        .additionalParameters(getParams(configScript))
                        .run(getDescriptor(postExecution));
                return new Cancellable() {
                    @Override
                    public boolean cancel() {
                        return result.cancel(true);
                    }
                };
            }
        };
    }

    @NbBundle.Messages({
        "# {0} - project or file name",
        "FileRunner.debug.displayName={0} (debug)"
    })
    private void debugInternal() {
        XDebugStarter dbgStarter =  XDebugStarterFactory.getInstance();
        assert dbgStarter != null;
        if (dbgStarter.isAlreadyRunning()) {
            if (CommandUtils.warnNoMoreDebugSession()) {
                dbgStarter.stop();
                debugInternal();
            }
        } else {
            Callable<Cancellable> callable = getRunCallable(Bundle.FileRunner_debug_displayName(project.getName()));
            XDebugStarter.Properties props = XDebugStarter.Properties.create(
                    FileUtil.toFileObject(file),
                    true,
                    // #209682 - "run as script" always from project files
                    Collections.<Pair<String, String>>emptyList(),
                    null, // no debug proxy for files (valid only for server urls)
                    getEncoding());
            dbgStarter.start(project, callable, props);
        }
    }

    private List<String> getParams(RunConfigScript configScript) {
        List<String> params = new ArrayList<String>();
        String phpArgs = configScript.getOptions();
        if (StringUtils.hasText(phpArgs)) {
            params.addAll(Arrays.asList(Utilities.parseParameters(phpArgs)));
        }
        params.add(file.getAbsolutePath());
        String scriptArgs = configScript.getArguments();
        if (StringUtils.hasText(scriptArgs)) {
            params.addAll(Arrays.asList(Utilities.parseParameters(scriptArgs)));
        }
        return params;
    }

    ExecutionDescriptor getDescriptor(Runnable postExecution) {
        ExecutionDescriptor descriptor = PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .charset(Charset.forName(getEncoding()))
                .controllable(controllable)
                .optionsPath(UiUtils.OPTIONS_PATH)
                .outConvertorFactory(PHP_LINE_CONVERTOR_FACTORY);
        if (!getPhpOptions().isOpenResultInOutputWindow()) {
            descriptor = descriptor.inputOutput(InputOutput.NULL)
                    .frontWindow(false)
                    .frontWindowOnError(false);
        }
        if (postExecution != null) {
            descriptor = descriptor.postExecution(postExecution);
        }
        return descriptor;
    }

    private String getEncoding() {
        return project != null ? ProjectPropertiesSupport.getEncoding(project) : FileEncodingQuery.getDefaultEncoding().name();
    }

    boolean getRedirectToFile() {
        return getPhpOptions().isOpenResultInBrowser() || getPhpOptions().isOpenResultInEditor();
    }

    File createTempFile() {
        try {
            File tmpFile = File.createTempFile(file.getName(), ".html"); // NOI18N
            tmpFile.deleteOnExit();
            return tmpFile;
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return null;
    }

    private PhpOptions getPhpOptions() {
        return PhpOptions.getInstance();
    }

    //~ Inner classes

    private static final class PhpLineConvertorFactory implements ExecutionDescriptor.LineConvertorFactory {

        @Override
        public LineConvertor newLineConvertor() {
            LineConvertor[] lineConvertors = new LineConvertor[PhpInterpreter.LINE_PATTERNS.length];
            int i = 0;
            for (Pattern linePattern : PhpInterpreter.LINE_PATTERNS) {
                lineConvertors[i++] = LineConvertors.filePattern(null, linePattern, null, 1, 2);
            }
            return LineConvertors.proxy(lineConvertors);
        }
    }

    private static final class PostExecution implements Runnable {

        private final File tmpFile;


        public PostExecution(File tmpFile) {
            this.tmpFile = tmpFile;
        }

        @Override
        public void run() {
            PhpOptions options = PhpOptions.getInstance();
            try {
                if (options.isOpenResultInBrowser()) {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(Utilities.toURI(tmpFile).toURL());
                }
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            if (options.isOpenResultInEditor()) {
                PhpProjectUtils.openFile(tmpFile);
            }
        }

    }

}
