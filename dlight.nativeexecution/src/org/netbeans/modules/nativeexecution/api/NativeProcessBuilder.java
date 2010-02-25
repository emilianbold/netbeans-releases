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

import java.io.IOException;
import java.util.concurrent.Callable;
import javax.swing.event.ChangeListener;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.modules.nativeexecution.AbstractNativeProcess;
import org.netbeans.modules.nativeexecution.PtyNativeProcess;
import org.netbeans.modules.nativeexecution.LocalNativeProcess;
import org.netbeans.modules.nativeexecution.NativeProcessInfo;
import org.netbeans.modules.nativeexecution.RemoteNativeProcess;
import org.netbeans.modules.nativeexecution.TerminalLocalNativeProcess;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ExternalTerminal;
import org.netbeans.modules.nativeexecution.api.util.ExternalTerminalProvider;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.nativeexecution.api.util.Shell;
import org.netbeans.modules.nativeexecution.api.util.ShellValidationSupport;
import org.netbeans.modules.nativeexecution.api.util.ShellValidationSupport.ShellValidationStatus;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;

/**
 * Utility class for the {@link NativeProcess external native process} creation.
 * <p>
 * Depending on {@link ExecutionEnvironment} it creates either local process or
 * remote one. This class was designed to be usable with {@link ExecutionService}
 * provided by the
 * <a href="http://bits.netbeans.org/dev/javadoc/org-netbeans-modules-extexecution/index.html?overview-summary.html" target="_blank">External Execution Support</a>
 * NetBeans module.
 * <p>
 * Builder handles command, working directory, environment, task's listeners and
 * execution in an external terminal.
 * <p>
 */
// @NotThreadSafe
public final class NativeProcessBuilder implements Callable<Process> {

    private final NativeProcessInfo info;
    private ExternalTerminal externalTerminal = null;

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

    public MacroMap getEnvironment() {
        return info.getEnvironment();
    }

    /**
     * Creates a new {@link NativeProcess} based on the properties configured
     * in this builder.
     * @return new {@link NativeProcess} based on the properties configured
     *             in this builder
     * @throws IOException if the process could not be created
     */
    @Override
    public NativeProcess call() throws IOException {
        AbstractNativeProcess process = null;

        ExecutionEnvironment execEnv = info.getExecutionEnvironment();

        if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
            throw new IllegalStateException("No connection to " + execEnv.getDisplayName()); // NOI18N
        }

        if (info.getCommand() == null) {
            throw new IllegalStateException("No executable nor command line is specified"); // NOI18N
        }

        if (info.isPtyMode()) {
            process = new PtyNativeProcess(info);
        } else {
            if (info.getExecutionEnvironment().isRemote()) {
                process = new RemoteNativeProcess(info);
            } else {
                if (externalTerminal != null) {
                    boolean canProceed = true;
                    boolean available = externalTerminal.isAvailable(info.getExecutionEnvironment());

                    if (!available) {
                        if (Boolean.getBoolean("nativeexecution.mode.unittest")) {
                            System.err.println(loc("NativeProcessBuilder.processCreation.NoTermianl.text"));
                        } else {
                            DialogDisplayer.getDefault().notify(
                                    new NotifyDescriptor.Message(loc("NativeProcessBuilder.processCreation.NoTermianl.text"), // NOI18N
                                    NotifyDescriptor.WARNING_MESSAGE));
                        }
                        canProceed = false;
                    } else {
                        if (Utilities.isWindows()) {
                            Shell shell = WindowsSupport.getInstance().getActiveShell();
                            if (shell == null) {
                                if (Boolean.getBoolean("nativeexecution.mode.unittest")) {
                                    System.err.println(loc("NativeProcessBuilder.processCreation.NoShell.text"));
                                } else {
                                    DialogDisplayer.getDefault().notify(
                                            new NotifyDescriptor.Message(loc("NativeProcessBuilder.processCreation.NoShell.text"), // NOI18N
                                            NotifyDescriptor.WARNING_MESSAGE));
                                }
                                canProceed = false;
                            } else {
                                ShellValidationStatus validationStatus = ShellValidationSupport.getValidationStatus(shell);

                                if (!validationStatus.isValid()) {
                                    canProceed = ShellValidationSupport.confirm(
                                            loc("NativeProcessBuilder.processCreation.BrokenShellConfirmationHeader.text"), // NOI18N
                                            loc("NativeProcessBuilder.processCreation.BrokenShellConfirmationFooter.text"), // NOI18N
                                            validationStatus);
                                }
                            }
                        }

                        if (canProceed) {
                            process = new TerminalLocalNativeProcess(info, externalTerminal);
                        }
                    }
                }
            }

            if (process == null) {
                // Either externalTerminal is null or there are some problems with it
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

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(NativeProcessBuilder.class, key, params);
    }

    /**
     * Configure whether process starts in a prseudo-terminal or not.
     * 
     * @param usePty - if true, process builder will start the process in
     * a pty mode
     * @return this
     */
    public NativeProcessBuilder setUsePty(boolean usePty) {
        info.setPtyMode(usePty);
        return this;
    }

    /**
     * Process builder try to expand, escape, quote command line according to subset of shell man.
     * By default builder do this. This method allows to forbid  preprocessing of command line.
     *
     * @param expandMacros - if false, process builder do not preprocess command line
     * @return this
     */
    public NativeProcessBuilder setMacroExpansion(boolean expandMacros) {
        info.setExpandMacros(expandMacros);
        return this;
    }
}
