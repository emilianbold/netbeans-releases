/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.api;

import org.netbeans.modules.nativeexecution.api.util.ExternalTerminal;
import java.io.IOException;
import java.util.Map;
import org.netbeans.modules.nativeexecution.LocalNativeProcess;
import java.util.concurrent.Callable;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.modules.nativeexecution.AbstractNativeProcess;
import org.netbeans.modules.nativeexecution.NativeProcessInfo;
import org.netbeans.modules.nativeexecution.RemoteNativeProcess;
import org.netbeans.modules.nativeexecution.TerminalLocalNativeProcess;
import org.netbeans.modules.nativeexecution.api.util.ExternalTerminalProvider;
import org.netbeans.modules.nativeexecution.support.Logger;

/**
 * Utility class for the {@link NativeProcess external native process} creation.
 * <p>
 * Depending on {@link ExecutionEnvironment} it creates whether local process or
 * remote one. This class was designed to be used with {@link ExecutionService}
 * provided by the
 * <a href="http://bits.netbeans.org/dev/javadoc/org-netbeans-modules-extexecution/index.html?overview-summary.html" target="_blank">External Execution Support</a>
 * NetBeans module.
 * <p>
 * Builder handles command, working directory, environment, task's listeners and
 * execution in an external terminal.
 * <p>
 * Note that <tt>NativeProcessBuilder</tt> is immutable. This means that it
 * cannot be changed and every it's method returns a new instance of the native
 * process builder with additionally configured properties.
 */
public final class NativeProcessBuilder implements Callable<Process> {

    private final static java.util.logging.Logger log = Logger.getInstance();
    private NativeProcessInfo info;
    private ExternalTerminal externalTerminal = null;
    private AbstractNativeProcess process = null;

    private NativeProcessBuilder(final ExecutionEnvironment execEnv) {
        info = new NativeProcessInfo(execEnv);
    }

    /**
     * Creates a new instance of the builder that will create a {@link NativeProcess}
     * in the specified execution environment.
     * @param execEnv execution environment that defines <b>where</b> a native
     *        process will be started.
     * @return new instance of process builder
     */
    public static NativeProcessBuilder newProcessBuilder(ExecutionEnvironment execEnv) {
        return new NativeProcessBuilder(execEnv);
    }

    /**
     * Creates a new instance of the builder that will create a {@link NativeProcess}
     * on the localhost.
     * @return new instance of process builder
     */
    public static NativeProcessBuilder newLocalProcessBuilder() {
        return new NativeProcessBuilder(ExecutionEnvironmentFactory.getLocal());
    }

    public void redirectError() {
        info.redirectError(true);
    }

    /**
     * Specif
     * @param executable
     * @return
     */
    public NativeProcessBuilder setExecutable(String executable) {
        info.setExecutable(executable);
        return this;
    }

    /**
     * NB! no arguments can be set after that.
     * command line it not escaped before execution.
     * @param commandLine
     * @return
     */
    public NativeProcessBuilder setCommandLine(String commandLine) {
        info.setCommandLine(commandLine);
        return this;
    }

    /**
     * Register passed <tt>NativeProcess.Listener</tt>.
     *
     * @param listener NativeProcess.Listener to be registered to recieve process'
     *        state change events.
     *
     * @return this
     */
    public NativeProcessBuilder addNativeProcessListener(ChangeListener listener) {
        info.addNativeProcessListener(listener);
        return this;
    }

    /**
     * Creates a new {@link NativeProcess} based on the properties configured
     * in this builder.
     * @return new {@link NativeProcess} based on the properties configured
     *             in this builder
     * @throws IOException if the process could not be created
     */
    public NativeProcess call() throws IOException {

        if (info.getExecutionEnvironment().isRemote()) {
            process = new RemoteNativeProcess(info);
        } else {
            if (externalTerminal != null) {
                boolean available = externalTerminal.isAvailable(info.getExecutionEnvironment());
                if (available) {
                    process = new TerminalLocalNativeProcess(info, externalTerminal);
                } else {
                    log.info("Unable to find external terminal. Will start in OutputWindow"); // NOI18N
                    process = new LocalNativeProcess(info);
                }
            } else {
                process = new LocalNativeProcess(info);
            }
        }

        return process.createAndStart();
    }

    /**
     * Configures a working directory.
     * Process subsequently created by the call() method on this builder
     * will be executed with this directory as a current working dir.
     * <p>
     * The default value is undefined.
     * <p>
     * @param workingDirectory working directory to start process in.
     * @return this
     */
    public NativeProcessBuilder setWorkingDirectory(String workingDirectory) {
        info.setWorkingDirectory(workingDirectory);
        return this;
    }

    /**
     * Configures additional environment variable for the command.
     * 
     * Process subsequently created by the call() method on this builder
     * will be executed with configured environment variables.
     * 
     * <p>
     * By default no additional environment variables are configured.
     * <p>
     *
     * @param name name of the variable
     * @param value value of the variable
     * @return this
     */
    public NativeProcessBuilder addEnvironmentVariable(String name, String value) {
        info.addEnvironmentVariable(name, value);
        return this;
    }

    /**
     * Configures additional environment variable for the command.
     *
     * Process subsequently created by the call() method on this builder
     * will be executed with configured environment variables.
     *
     * <p>
     * By default no additional environment variables are configured.
     * <p>
     *
     * @param envs map of value, name of additional env variables
     * @return this
     */
    public NativeProcessBuilder addEnvironmentVariables(Map<String, String> envs) {
        if (envs == null || envs.isEmpty()) {
            return this;
        }

        info.addEnvironmentVariables(envs);
        return this;
    }

    /**
     * Configure arguments of the command.
     *
     * <p>
     * By default executable is started without any arguments.
     * <p>
     * Previously configured arguments are cleared. 
     * <p>
     * If there is a need to parse arguments already provided as one big string
     * the method that can help is
     * {@link org.openide.util.Utilitiesies#parseParameters(java.lang.String)}.
     *
     * @param arguments command arguments
     * @return this
     */
    public NativeProcessBuilder setArguments(String... arguments) {
        info.setArguments(arguments);
        return this;
    }

    /**
     * Configure external terminal to be used to execute configured process.
     * 
     * <p>
     * @param terminal terminal specification
     * @return this
     *
     * @see ExternalTerminalProvider
     */
    public NativeProcessBuilder useExternalTerminal(/*@NullAllowed*/ExternalTerminal terminal) {
        externalTerminal = terminal;
        return this;
    }

    /**
     * Configure whether to use output unbuffering or not.
     * @param unbuffer - if true, native unbuffer library will be preloaded.
     * @return this
     */
    public NativeProcessBuilder unbufferOutput(boolean unbuffer) {
        info.setUnbuffer(unbuffer);
        return this;
    }

    /**
     * Configure X11 forwarding.
     *
     * @param x11forwarding  pass <code>true</code> to enable forwarding,
     *      or <code>false</code> to disable
     * @return this
     */
    public NativeProcessBuilder setX11Forwarding(boolean x11forwarding) {
        if (Boolean.getBoolean("cnd.remote.noX11")) {
            return this; //
        }
        info.setX11Forwarding(x11forwarding);
        return this;
    }

    /**
     * Configure whether process starts normally or suspended.
     * Suspended process can be resumed by sending it SIGCONT signal.
     * Note that suspended process is also in RUNNING state.
     *
     * @param suspend  pass <code>true</code> to start process suspended,
     *      or <code>false</code> to start process normally
     * @return this
     */
    public NativeProcessBuilder setInitialSuspend(boolean suspend) {
        info.setInitialSuspend(suspend);
        return this;
    }
}
