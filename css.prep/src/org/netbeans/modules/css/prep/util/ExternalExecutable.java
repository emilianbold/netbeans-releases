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
package org.netbeans.modules.css.prep.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.progress.ProgressUtils;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;

// XXX copied & adjusted from PHP
/**
 * Class usable for running any external executable (program or script).
 */
public final class ExternalExecutable {

    private static final Logger LOGGER = Logger.getLogger(ExternalExecutable.class.getName());

    /**
     * Get the {@link ExecutionDescriptor execution descriptor}. This descriptor is:
     * <ul>
     *   <li>{@link ExecutionDescriptor#isControllable() controllable}</li>
     *   <li>{@link ExecutionDescriptor#isFrontWindow() displays the Output window}</li>
     *   <li>{@link ExecutionDescriptor#isFrontWindowOnError()  displays the Output window on error (since 1.62)}</li>
     *   <li>{@link ExecutionDescriptor#isInputVisible() has visible user input}</li>
     *   <li>{@link ExecutionDescriptor#showProgress() shows progress}</li>
     * </ul>
     * @return the default {@link ExecutionDescriptor execution descriptor}.
     */
    public static final ExecutionDescriptor DEFAULT_EXECUTION_DESCRIPTOR = new ExecutionDescriptor()
            .controllable(true)
            .frontWindow(true)
            .frontWindowOnError(true)
            .inputVisible(true)
            .showProgress(true);

    private final String executable;
    private final List<String> parameters;
    private final String command;
    final List<String> fullCommand = new CopyOnWriteArrayList<>();

    private String executableName = null;
    private String displayName = null;
    private boolean redirectErrorStream = true;
    private File workDir = null;
    private boolean warnUser = true;
    private List<String> additionalParameters = Collections.<String>emptyList();
    private Map<String, String> environmentVariables = Collections.<String, String>emptyMap();
    private File fileOutput = null;
    private boolean fileOutputOnly = false;
    private boolean noInfo = false;


    /**
     * Parse command which can be just binary or binary with parameters.
     * As a parameter separator, "-" or "/" is used.
     * @param command command to parse, can be {@code null}.
     */
    public ExternalExecutable(String command) {
        Pair<String, List<String>> parsedCommand = parseCommand(command);
        executable = parsedCommand.first();
        parameters = parsedCommand.second();
        this.command = command.trim();
    }

    static Pair<String, List<String>> parseCommand(String command) {
        if (command == null) {
            // avoid NPE
            command = ""; // NOI18N
        }
        // try to find program (search for " -" or " /" after space)
        String[] tokens = command.split(" * (?=\\-|/)", 2); // NOI18N
        if (tokens.length == 1) {
            LOGGER.log(Level.FINE, "Only program given (no parameters): {0}", command);
            return Pair.of(tokens[0].trim(), Collections.<String>emptyList());
        }
        Pair<String, List<String>> parsedCommand = Pair.of(tokens[0].trim(), Arrays.asList(Utilities.parseParameters(tokens[1].trim())));
        LOGGER.log(Level.FINE, "Parameters parsed: {0} {1}", new Object[] {parsedCommand.first(), parsedCommand.second()});
        return parsedCommand;
    }

    /**
     * Get external executable, never {@code null}.
     * @return external executable, never {@code null}.
     */
    public String getExecutable() {
        return executable;
    }

    /**
     * Get parameters, can be an empty array but never {@code null}.
     * @return parameters, can be an empty array but never {@code null}.
     */
    public List<String> getParameters() {
        return new ArrayList<>(parameters);
    }

    /**
     * Get the command, in the original form (just without leading and trailing whitespaces).
     * @return the command, in the original form (just without leading and trailing whitespaces).
     */
    public String getCommand() {
        return command;
    }

    /**
     * Set name of the executable. This name is used for {@link ExternalExecutableValidator validation} only (before running).
     * <p>
     * The default value is {@code null} (it means "File").
     * @param executableName name of the executable
     * @return the external executable instance itself
     */
    public ExternalExecutable executableName(@NonNull String executableName) {
        Parameters.notEmpty("executableName", executableName); // NOI18N
        this.executableName = executableName;
        return this;
    }

    /**
     * Set display name that is used for executable running (as a title of the Output window).
     * <p>
     * The default value is {@link #getExecutable() executable} with {@link #getParameters() parameters}.
     * @param displayName display name that is used for executable running
     * @return the external executable instance itself
     */
    public ExternalExecutable displayName(String displayName) {
        Parameters.notEmpty("displayName", displayName); // NOI18N
        this.displayName = displayName;
        return this;
    }

    /**
     * Set error stream redirection.
     * <p>
     * The default value is {@code true} (it means that the error stream is redirected to the standard output).
     * @param viaAutodetection {@code true} if error stream should be redirected, {@code false} otherwise
     * @return the external executable instance itself
     */
    public ExternalExecutable redirectErrorStream(boolean redirectErrorStream) {
        this.redirectErrorStream = redirectErrorStream;
        return this;
    }

    /**
     * Set working directory for {@link #run() running} this executable.
     * <p>
     * The default value is {@code null} ("unknown" directory).
     * @param workDir working directory for {@link #run() running} this executable
     * @return the external executable instance itself
     */
    public ExternalExecutable workDir(@NonNull File workDir) {
        Parameters.notNull("workDir", workDir); // NOI18N
        this.workDir = workDir;
        return this;
    }

    /**
     * Set whether user should be warned before {@link #run() running} in case of invalid command.
     * <p>
     * The default value is {@code true} (it means that the user is informed).
     * @param warnUser {@code true} if user should be warned, {@code false} otherwise
     * @return the external executable instance itself
     */
    public ExternalExecutable warnUser(boolean warnUser) {
        this.warnUser = warnUser;
        return this;
    }

    /**
     * Set addition parameters for {@link #run() running}.
     * <p>
     * The default value is empty list (it means no additional parameters).
     * @param additionalParameters addition parameters for {@link #run() running}.
     * @return the external executable instance itself
     */
    public ExternalExecutable additionalParameters(@NonNull List<String> additionalParameters) {
        Parameters.notNull("additionalParameters", additionalParameters); // NOI18N
        this.additionalParameters = additionalParameters;
        return this;
    }

    /**
     * Set addition parameters for {@link #run() running}.
     * <p>
     * The default value is empty list (it means no additional parameters).
     * @param additionalParameters addition parameters for {@link #run() running}.
     * @return the external executable instance itself
     */
    public ExternalExecutable environmentVariables(Map<String, String> environmentVariables) {
        Parameters.notNull("environmentVariables", environmentVariables); // NOI18N
        this.environmentVariables = environmentVariables;
        return this;
    }

    /**
     * Set file for executable output; also set whether only output to file should be used (no Output window).
     * <p>
     * The default value is {@code null} and {@code false} (it means no output is stored to any file
     * and info is printed in Output window).
     * @param fileOutput file for executable output
     * @param fileOutputOnly {@code true} for only file output, {@code false} otherwise
     * @return the external executable instance itself
     * @see #noInfo(boolean)
     */
    public ExternalExecutable fileOutput(@NonNull File fileOutput, boolean fileOutputOnly) {
        Parameters.notNull("fileOutput", fileOutput); // NOI18N
        this.fileOutput = fileOutput;
        this.fileOutputOnly = fileOutputOnly;
        return this;
    }

    /**
     * Set no information. If Output window is used, no info about this executable is printed.
     * <p>
     * The default value is {@code false} (it means print info about this executable).
     * @param noInfo {@code true} for pure output only (no info about executable)
     * @return the external executable instance itself
     */
    public ExternalExecutable noInfo(boolean noInfo) {
        this.noInfo = noInfo;
        return this;
    }

    /**
     * Run this executable with the {@link #DEFAULT_EXECUTION_DESCRIPTOR default execution descriptor}.
     * @return task representing the actual run, value representing result of the {@link Future} is exit code of the process
     * or {@code null} if the executable cannot be run
     * @see #run(ExecutionDescriptor)
     * @see #run(ExecutionDescriptor, ExecutionDescriptor.InputProcessorFactory)
     * @see ExecutionService#run()
     */
    @CheckForNull
    public Future<Integer> run() {
        return run(DEFAULT_EXECUTION_DESCRIPTOR);
    }

    /**
     * Run this executable with the given execution descriptor.
     * <p>
     * <b>WARNING:</b> If any {@link InputProcessorFactory output processor factory} should be used, use
     * {@link ExternalExecutable#run(ExecutionDescriptor, ExecutionDescriptor.InputProcessorFactory) run(ExecutionDescriptor, ExecutionDescriptor.InputProcessorFactory)} instead.
     * @return task representing the actual run, value representing result of the {@link Future} is exit code of the process
     * or {@code null} if the executable cannot be run
     * @see #run()
     * @see #run(ExecutionDescriptor)
     * @see ExecutionService#run()
     */
    @CheckForNull
    public Future<Integer> run(@NonNull ExecutionDescriptor executionDescriptor) {
        return run(executionDescriptor, null);
    }

    /**
     * Run this executable with the given execution descriptor and optional output processor factory.
     * <p>
     * @param executionDescriptor execution descriptor to be used
     * @param outProcessorFactory output processor factory to be used, can be {@code null}
     * @return task representing the actual run, value representing result of the {@link Future} is exit code of the process
     * or {@code null} if the executable cannot be run
     * @see #run()
     * @see #run(ExecutionDescriptor)
     * @see #run(ExecutionDescriptor, ExecutionDescriptor.InputProcessorFactory)
     * @see ExecutionService#run()
     * @since 0.2
     */
    @CheckForNull
    public Future<Integer> run(@NonNull ExecutionDescriptor executionDescriptor, @NullAllowed ExecutionDescriptor.InputProcessorFactory outProcessorFactory) {
        Parameters.notNull("executionDescriptor", executionDescriptor); // NOI18N
        return runInternal(executionDescriptor, outProcessorFactory);
    }

    /**
     * Run this executable with the {@link #DEFAULT_EXECUTION_DESCRIPTOR default execution descriptor}, <b>blocking but not blocking the UI thread</b>
     * (it displays progress dialog if it is running in it).
     * @param progressMessage message displayed if the task is running in the UI thread
     * @return exit code of the process or {@code null} if any error occured
     * @throws ExecutionException if any error occurs
     * @see #runAndWait(ExecutionDescriptor, String)
     * @see #runAndWait(ExecutionDescriptor, ExecutionDescriptor.InputProcessorFactory, String)
     */
    @CheckForNull
    public Integer runAndWait(@NonNull String progressMessage) throws ExecutionException {
        return runAndWait(DEFAULT_EXECUTION_DESCRIPTOR, progressMessage);
    }

    /**
     * Run this executable with the given execution descriptor, <b>blocking but not blocking the UI thread</b>
     * (it displays progress dialog if it is running in it).
     * <p>
     * <b>WARNING:</b> If any {@link InputProcessorFactory output processor factory} should be used, use
     * {@link ExternalExecutable#runAndWait(ExecutionDescriptor, ExecutionDescriptor.InputProcessorFactory, String) run(ExecutionDescriptor, ExecutionDescriptor.InputProcessorFactory, String)} instead.
     * @param executionDescriptor execution descriptor to be used
     * @param progressMessage message displayed if the task is running in the UI thread
     * @return exit code of the process or {@code null} if any error occured
     * @throws ExecutionException if any error occurs
     * @see #runAndWait(String)
     * @see #runAndWait(ExecutionDescriptor, ExecutionDescriptor.InputProcessorFactory, String)
     */
    @CheckForNull
    public Integer runAndWait(@NonNull ExecutionDescriptor executionDescriptor, @NonNull String progressMessage) throws ExecutionException {
        return runAndWait(executionDescriptor, null, progressMessage);
    }

    /**
     * Run this executable with the given execution descriptor and optional output processor factory, <b>blocking but not blocking the UI thread</b>
     * (it displays progress dialog if it is running in it).
     * @param executionDescriptor execution descriptor to be used
     * @param outProcessorFactory output processor factory to be used, can be {@code null}
     * @param progressMessage message displayed if the task is running in the UI thread
     * @return exit code of the process or {@code null} if any error occured
     * @throws ExecutionException if any error occurs
     * @see #runAndWait(String)
     * @see #runAndWait(ExecutionDescriptor, String)
     */
    @CheckForNull
    public Integer runAndWait(@NonNull ExecutionDescriptor executionDescriptor, @NullAllowed ExecutionDescriptor.InputProcessorFactory outProcessorFactory,
            @NonNull final String progressMessage) throws ExecutionException {
        Parameters.notNull("progressMessage", progressMessage); // NOI18N
        final Future<Integer> result = run(executionDescriptor, outProcessorFactory);
        if (result == null) {
            return null;
        }
        final AtomicReference<ExecutionException> executionException = new AtomicReference<>();
        if (SwingUtilities.isEventDispatchThread()) {
            if (!result.isDone()) {
                try {
                    // let's wait in EDT to avoid flashing dialogs
                    getResult(result, 90L);
                } catch (TimeoutException ex) {
                    ProgressUtils.showProgressDialogAndRun(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                getResult(result);
                            } catch (ExecutionException extEx) {
                                executionException.set(extEx);
                            }
                        }
                    }, progressMessage);
                }
            }
        }
        if (executionException.get() != null) {
            throw executionException.get();
        }
        return getResult(result);
    }

    @CheckForNull
    private Future<Integer> runInternal(ExecutionDescriptor executionDescriptor, ExecutionDescriptor.InputProcessorFactory outProcessorFactory) {
        Parameters.notNull("executionDescriptor", executionDescriptor); // NOI18N
        String error = ExternalExecutableValidator.validateCommand(executable, executableName);
        if (error != null) {
            if (warnUser) {
                // optionsSubcategory should be taken from executionDescriptor (unfortunately not possible)
                UiUtils.invalidScriptProvided(error);
            }
            return null;
        }
        ExternalProcessBuilder processBuilder = getProcessBuilder();
        if (processBuilder == null) {
            return null;
        }
        executionDescriptor = getExecutionDescriptor(executionDescriptor, outProcessorFactory);
        return ExecutionService.newService(processBuilder, executionDescriptor, getDisplayName()).run();
    }

    @CheckForNull
    private ExternalProcessBuilder getProcessBuilder() {
        ExternalProcessBuilder processBuilder = createProcessBuilder();
        if (processBuilder == null) {
            return null;
        }
        for (String param : parameters) {
            fullCommand.add(param);
            processBuilder = processBuilder.addArgument(param);
        }
        for (String param : additionalParameters) {
            fullCommand.add(param);
            processBuilder = processBuilder.addArgument(param);
        }
        if (workDir != null) {
            processBuilder = processBuilder.workingDirectory(workDir);
        }
        for (Map.Entry<String, String> variable : environmentVariables.entrySet()) {
            processBuilder = processBuilder.addEnvironmentVariable(variable.getKey(), variable.getValue());
        }
        processBuilder = processBuilder.redirectErrorStream(redirectErrorStream);
        return processBuilder;
    }

    private ExternalProcessBuilder createProcessBuilder() {
        fullCommand.clear();
        fullCommand.add(executable);
        return new ExternalProcessBuilder(executable);
    }

    private String getDisplayName() {
        if (displayName != null) {
            return displayName;
        }
        return getDefaultDisplayName();
    }

    private String getDefaultDisplayName() {
        StringBuilder buffer = new StringBuilder(200);
        buffer.append(executable);
        for (String param : parameters) {
            buffer.append(" "); // NOI18N
            buffer.append(param);
        }
        return buffer.toString();
    }

    static Integer getResult(Future<Integer> result) throws ExecutionException {
        try {
            return getResult(result, null);
        } catch (TimeoutException ex) {
            // in fact, cannot happen since we don't use timeout
            LOGGER.log(Level.WARNING, null, ex);
        }
        return null;
    }

    private static Integer getResult(Future<Integer> result, Long timeout) throws TimeoutException, ExecutionException {
        try {
            if (timeout != null) {
                return result.get(timeout, TimeUnit.MILLISECONDS);
            }
            return result.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    private ExecutionDescriptor getExecutionDescriptor(ExecutionDescriptor executionDescriptor, ExecutionDescriptor.InputProcessorFactory outProcessorFactory) {
        final List<ExecutionDescriptor.InputProcessorFactory> inputProcessors = new CopyOnWriteArrayList<>();
        // colors
        ExecutionDescriptor.InputProcessorFactory infoOutProcessorFactory = getInfoOutputProcessorFactory();
        if (infoOutProcessorFactory != null) {
            inputProcessors.add(infoOutProcessorFactory);
        }
        // file output
        ExecutionDescriptor.InputProcessorFactory fileOutProcessorFactory = getFileOutputProcessorFactory();
        if (fileOutProcessorFactory != null) {
            inputProcessors.add(fileOutProcessorFactory);
            if (fileOutputOnly) {
                executionDescriptor = executionDescriptor
                        .inputOutput(InputOutput.NULL)
                        .frontWindow(false)
                        .frontWindowOnError(false);
            }
        }
        if (outProcessorFactory != null) {
            inputProcessors.add(outProcessorFactory);
        }
        if (!inputProcessors.isEmpty()) {
            executionDescriptor = executionDescriptor.outProcessorFactory(new ExecutionDescriptor.InputProcessorFactory() {
                @Override
                public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                    InputProcessor[] processors = new InputProcessor[inputProcessors.size()];
                    for (int i = 0; i < inputProcessors.size(); ++i) {
                        processors[i] = inputProcessors.get(i).newInputProcessor(defaultProcessor);
                    }
                    return InputProcessors.proxy(processors);
                }
            });
        }
        return executionDescriptor;
    }

    private ExecutionDescriptor.InputProcessorFactory getInfoOutputProcessorFactory() {
        if (noInfo) {
            // no info
            return null;
        }
        return new ExecutionDescriptor.InputProcessorFactory() {
            @Override
            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                return InputProcessors.proxy(new InfoInputProcessor(defaultProcessor, fullCommand), defaultProcessor);
            }
        };
    }

    private ExecutionDescriptor.InputProcessorFactory getFileOutputProcessorFactory() {
        if (fileOutput == null) {
            return null;
        }
        return new ExecutionDescriptor.InputProcessorFactory() {
            @Override
            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                return new RedirectOutputProcessor(fileOutput);
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append(getClass().getName());
        sb.append(" [executable: "); // NOI18N
        sb.append(executable);
        sb.append(", parameters: "); // NOI18N
        sb.append(parameters);
        sb.append("]"); // NOI18N
        return sb.toString();
    }

    //~ Inner classes

    static final class InfoInputProcessor implements InputProcessor {

        private final InputProcessor defaultProcessor;
        private char lastChar;


        public InfoInputProcessor(InputProcessor defaultProcessor, List<String> fullCommand) {
            this.defaultProcessor = defaultProcessor;
            String infoCommand = colorize(getInfoCommand(fullCommand)) + "\n"; // NOI18N
            try {
                defaultProcessor.processInput(infoCommand.toCharArray());
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }

        @Override
        public void processInput(char[] chars) throws IOException {
            if (chars.length > 0) {
                lastChar = chars[chars.length - 1];
            }
        }

        @Override
        public void reset() throws IOException {
            // noop
        }

        @NbBundle.Messages("InfoInputProcessor.done=Done.")
        @Override
        public void close() throws IOException {
            StringBuilder msg = new StringBuilder(Bundle.InfoInputProcessor_done().length() + 2);
            if (!isNewLine(lastChar)) {
                msg.append("\n"); // NOI18N
            }
            msg.append(colorize(Bundle.InfoInputProcessor_done()));
            msg.append("\n"); // NOI18N
            defaultProcessor.processInput(msg.toString().toCharArray());
        }

        public static String getInfoCommand(List<String> fullCommand) {
            List<String> escapedCommand = new ArrayList<>(fullCommand.size());
            for (String command : fullCommand) {
                escapedCommand.add("\"" + command.replace("\"", "\\\"") + "\""); // NOI18N
            }
            return StringUtils.implode(escapedCommand, " "); // NOI18N
        }

        private static String colorize(String msg) {
            return "\033[1;30m" + msg + "\033[0m"; // NOI18N
        }

        private static boolean isNewLine(char ch) {
            return ch == '\n' || ch == '\r' || ch == '\u0000'; // NOI18N
        }

    }

    static final class RedirectOutputProcessor implements InputProcessor {

        private final File fileOuput;

        private OutputStream outputStream;


        public RedirectOutputProcessor(File fileOuput) {
            this.fileOuput = fileOuput;
        }

        @Override
        public void processInput(char[] chars) throws IOException {
            if (outputStream == null) {
                outputStream = new BufferedOutputStream(new FileOutputStream(fileOuput));
            }
            for (char c : chars) {
                outputStream.write((byte) c);
            }
        }

        @Override
        public void reset() {
            // noop
        }

        @Override
        public void close() throws IOException {
            outputStream.close();
        }

    }

}
