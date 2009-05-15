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
import org.netbeans.api.annotations.common.NullAllowed;
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
    private final NativeProcessInfo info;
    private ExternalTerminal externalTerminal = null;
    private AbstractNativeProcess process = null;

    /**
     * Creates a new instance of the builder that will create a {@link NativeProcess}
     * by running <tt>executable</tt> in the specified execution environment.
     * @param execEnv execution environment that defines <b>where</b> a native
     *        process will be started.
     * @param executable executable to run.
     */
    public NativeProcessBuilder(
            final ExecutionEnvironment execEnv,
            final String executable, boolean escapeCommand) {
        info = new NativeProcessInfo(execEnv, executable, escapeCommand);
    }

    /**
     * Creates a new instance of the builder that will create a {@link NativeProcess}
     * by running <tt>executable</tt> on the localhost.
     * @param executable executable to run.
     */
    public NativeProcessBuilder(final String executable) {
        this(ExecutionEnvironmentFactory.getLocal(), executable, false);
    }

    private NativeProcessBuilder(NativeProcessBuilder b) {
        info = new NativeProcessInfo(b.info);
        externalTerminal = b.externalTerminal;
    }

    /**
     * Returns new instance of the <tt>NativeProcessBuilder</tt> with registered
     * <tt>NativeProcess.Listener</tt>
     * <p>
     * All other properties of the returned builder are inherited from
     * <tt>this</tt>.
     * @param listener NativeProcess.Listener to be registered to recieve process'
     *        state change events.
     * @return new instance of the <tt>NativeProcessBuilder</tt> with
     *        registered listener.
     */
    public NativeProcessBuilder addNativeProcessListener(ChangeListener listener) {
        NativeProcessBuilder result = new NativeProcessBuilder(this);
        result.info.addNativeProcessListener(listener);
        return result;
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
     * Returns a builder with configured working directory.
     * Process subsequently created by the call() method on returned builder
     * will be executed with this directory as a current working dir.
     * <p>
     * The default value is undefined.
     * <p>
     * All other properties of the returned builder are inherited from
     * <tt>this</tt>.
     * @param workingDirectory working directory to start process in.
     * @return new instance of the <tt>NativeProcessBuilder</tt> with configured
     *         working directory
     */
    public NativeProcessBuilder setWorkingDirectory(String workingDirectory) {
        NativeProcessBuilder result = new NativeProcessBuilder(this);
        result.info.setWorkingDirectory(workingDirectory);
        return result;
    }

    /**
     * Returns a builder with additional environment variable for the command.
     * <p>
     * By default no additional environment variables are configured.
     * <p>
     * All other properties of the returned builder are inherited from
     * <tt>this</tt>.
     *
     * @param name name of the variable
     * @param value value of the variable
     * @return new instance of the <tt>NativeProcessBuilder</tt> with additional
     * environment variable for the command.
     */
    public NativeProcessBuilder addEnvironmentVariable(String name, String value) {
        NativeProcessBuilder result = new NativeProcessBuilder(this);
        result.info.addEnvironmentVariable(name, value);
        return result;
    }

    /**
     * Returns a builder with additional environment variables for the command.
     * <p>
     * By default no additional environment variables are configured.
     * <p>
     * All other properties of the returned builder are inherited from
     * <tt>this</tt>.
     *
     * @param envs map of value, name of additional env variables
     * @return new instance of the <tt>NativeProcessBuilder</tt> with additional
     * environment variables for the command.
     */
    public NativeProcessBuilder addEnvironmentVariables(Map<String, String> envs) {
        if (envs == null || envs.isEmpty()) {
            return this;
        }

        NativeProcessBuilder result = new NativeProcessBuilder(this);
        result.info.addEnvironmentVariables(envs);
        return result;
    }

    /**
     * Returns a builder with configured arguments of the command.
     * <p>
     * By default executable is started without any arguments.
     * <p>
     * Previously configured arguments are cleared. All other properties of the
     * returned builder are inherited from <tt>this</tt>.
     * <p>
     * If there is a need to parse arguments already provided as one big string
     * the method that can help is
     * {@link org.openide.util.Utilitiesies#parseParameters(java.lang.String)}.
     *
     * @param arguments command arguments
     * @return new instance of the <tt>NativeProcessBuilder</tt> with configured
     * arguments to be passed to the executable.
     */
    public NativeProcessBuilder setArguments(String... arguments) {
        NativeProcessBuilder result = new NativeProcessBuilder(this);
        result.info.setArguments(arguments);
        return result;
    }

    /**
     * Returns a builder that will start {@link NativeProcess} in an external
     * terminal specified by the <tt>terminal</tt>.
     * 
     * <p>
     * @param terminal terminal specification
     * @return new instance of the <tt>NativeProcessBuilder</tt> with configured
     *         external terminal to be used for process execution.
     *
     * @see ExternalTerminalProvider
     */
    public NativeProcessBuilder useExternalTerminal(@NullAllowed ExternalTerminal terminal) {
        NativeProcessBuilder result = new NativeProcessBuilder(this);
        result.externalTerminal = terminal;
        return result;
    }

    public NativeProcessBuilder unbufferOutput(boolean unbuffer) {
        NativeProcessBuilder result = new NativeProcessBuilder(this);
        result.info.setUnbuffer(unbuffer);
        return result;
    }
}
