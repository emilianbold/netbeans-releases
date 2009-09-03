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
package org.netbeans.modules.dlight.api.support;

import java.net.ConnectException;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.dlight.api.execution.AttachableTarget;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionDescriptor.LineConvertorFactory;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.modules.dlight.api.execution.DLightTargetChangeEvent;
import org.netbeans.modules.dlight.api.execution.SubstitutableTarget;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessChangeEvent;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ExternalTerminal;
import org.openide.windows.InputOutput;

/**
 * Wrapper of {@link @org-netbeans-modules-nativexecution@org/netbeans/modules/nativexecution/api/NativeTask.html}
 *
 */
public final class NativeExecutableTarget extends DLightTarget implements SubstitutableTarget, AttachableTarget, ChangeListener {

    private static final Logger log =
            DLightLogger.getLogger(NativeExecutableTarget.class);
    private final ExecutionEnvironment execEnv;
    private final String templateCMD;
    private final Map<String, String> envs;
    private final String workingDirectory;
    private final ExternalTerminal externalTerminal;
    private final String[] templateArgs;
    private final InputOutput io;
    private String[] args;
    private String cmd;
    private boolean x11forwarding;
    private volatile Future<Integer> targetFutureResult;
    private volatile int pid = -1;
    private volatile Integer status = null;
    private final Object stateLock = new String(NativeExecutableTarget.class.getName() + " - state lock"); // NOI18N
    private volatile State state;
    private LineConvertorFactory outConvertorFactory;
    private LineConvertorFactory errConvertorFactory;

    public NativeExecutableTarget(NativeExecutableTargetConfiguration configuration) {
        super(new NativeExecutableTargetExecutionService());
        this.execEnv = configuration.getExecutionEvnitoment();
        this.cmd = configuration.getCmd();
        this.args = configuration.getArgs();
        this.workingDirectory = configuration.getWorkingDirectory();
        this.envs = new HashMap<String, String>();
        this.envs.putAll(configuration.getEnv());

        ExternalTerminal term = configuration.getExternalTerminal();

        if (term != null) {
            StringBuilder title = new StringBuilder(cmd);

            for (String arg : args) {
                title.append(" \"" + arg + '"'); // NOI18N
            }

            term = term.setTitle(title.toString());
        }

        this.externalTerminal = term;

        if (externalTerminal == null) {
            this.outConvertorFactory = configuration.getOutConvertorFactory();
            this.errConvertorFactory = configuration.getErrConvertorFactory();
        }

        this.templateCMD = this.cmd;
        Map<String, String> info = configuration.getInfo();

        for (String name : info.keySet()) {
            putToInfo(name, info.get(name));
        }

        String[] argsCopy = null;
        if (args != null) {
            argsCopy = new String[args.length];
            for (int i = 0; i < args.length; i++) {
                argsCopy[i] = args[i];
            }
        }

        this.templateArgs = argsCopy;
        this.io = configuration.getIO();
        this.x11forwarding = configuration.getX11Forwarding();
    }

    public int getPID() {
        return pid;
    }

    public State getState() {
        synchronized (stateLock) {
            return state;
        }
    }

    @Override
    public String toString() {
        return "Executable target: " + cmd; // NOI18N
    }

    public void stateChanged(ChangeEvent e) {
        if (!(e instanceof NativeProcessChangeEvent)) {
            return;
        }

        NativeProcessChangeEvent event = (NativeProcessChangeEvent) e;
        NativeProcess process = (NativeProcess) event.getSource();

        State newState = null;
        boolean doNotify = true;
        boolean doResume = false;

        synchronized (stateLock) {
            switch (event.state) {
                case INITIAL:
                    state = State.INIT;
                    break;
                case STARTING:
                    state = State.STARTING;
                    break;
                case RUNNING:
                    state = State.RUNNING;
                    pid = event.pid;
                    //doResume = true;
                    break;
                case CANCELLED:
                    doNotify = false;
                    state = State.TERMINATED;
                    log.fine("NativeTask " + process.toString() + " cancelled!"); // NOI18N
                    break;
                case ERROR:
                    doNotify = false;
                    state = State.FAILED;
                    log.fine("NativeTask " + process.toString() + // NOI18N
                            " finished with error! "); // NOI18N
                    break;
                case FINISHED:
                    doNotify = false;
                    state = State.DONE;
                    status = process.exitValue();
                    break;
            }

            newState = state;
        }

        // #165655 - reported RUN status may intermix with program's output
        // This is because notification is sent as soon as process's waitFor()
        // exists. At this moment IO's output may not be flushed.
        // To avoid this condition, register poset-execution runnable
        //    descr = descr.postExecution(new Runnable() {...})
        // and don't notify listeners until it is invoked.
        //
        // So, in case we are in final state (DONE, FAILED, TERMINATED, ... )
        // We will not notify listeners here, but rather will do this from
        // a runnable passed to an execution service as a postExecution parameter.

        if (doNotify) {
            Integer notificationValue = (newState == DLightTarget.State.RUNNING) ? Integer.valueOf(pid) : status;
            notifyListeners(new DLightTargetChangeEvent(NativeExecutableTarget.this, newState, notificationValue));
        }

        if (doResume) {
            resume();
        }
    }

    public int getExitCode() throws InterruptedException {
        if (targetFutureResult != null) {
            try {
                return targetFutureResult.get();
            } catch (ExecutionException ex) {
                DLightLogger.instance.warning(ex.getMessage());
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return -1;
    }

    public boolean canBeSubstituted() {
        return true;
    }

    public void substitute(String cmd, String[] args) {
        //  isSubstituted = true;
        String extendedCMD = cmd;
        String[] extendedCMDArgs = args;
        String targetCMD = this.templateCMD;
        String[] targetArgs = this.templateArgs;
        this.cmd = extendedCMD;
        List<String> allArgs = new ArrayList<String>();
        allArgs.addAll(Arrays.asList(extendedCMDArgs));
        allArgs.add(targetCMD);

        if (targetArgs != null) {
            allArgs.addAll(Arrays.asList(targetArgs));
        }

        this.args = allArgs.toArray(new String[0]);
    }

    public ExecutionEnvironment getExecEnv() {
        return execEnv;
    }

    private void start(ExecutionEnvVariablesProvider executionEnvProvider) {
        synchronized (this) {
            ExecutionDescriptor descr = new ExecutionDescriptor();
            descr = descr.controllable(true).frontWindow(true);

            NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(execEnv);
            pb.setExecutable(cmd).setArguments(args);
            pb.addNativeProcessListener(NativeExecutableTarget.this);
            pb.setWorkingDirectory(workingDirectory);
            pb.addEnvironmentVariables(envs);
            pb.setX11Forwarding(x11forwarding);
            //pb.setInitialSuspend(true);

            // Setup external terminal ...
            if (execEnv.isLocal() && externalTerminal != null) {
                pb = pb.useExternalTerminal(externalTerminal);
                descr = descr.inputVisible(false);
                if (io != null) {
                    descr = descr.inputOutput(io);
                    io.setInputVisible(false);
                }
            } else {
                pb = pb.unbufferOutput(true);
                descr = descr.inputVisible(true);
                if (io != null) {
                    descr = descr.inputOutput(io);
                    io.setInputVisible(true);
                }
            }

            if (externalTerminal == null) {
                if (outConvertorFactory != null) {
                    descr = descr.outConvertorFactory(outConvertorFactory);
                }

                if (errConvertorFactory != null) {
                    descr = descr.outConvertorFactory(errConvertorFactory);
                }
            }

            // Setup additional environment variables from executionEnvProvider
            if (executionEnvProvider != null) {
                try {
                    Map<String, String> env = executionEnvProvider.getExecutionEnv(this);
                    if (env != null && !env.isEmpty()) {
                        pb = pb.addEnvironmentVariables(env);
                    }
                } catch (ConnectException ex) {
                    // TODO: can it happen here?
                    log.severe(ex.getMessage());
                }
            }

            descr = descr.postExecution(new Runnable() {

                public void run() {
                    notifyListeners(new DLightTargetChangeEvent(NativeExecutableTarget.this, state, status));
                }
            });

            final ExecutionService es = ExecutionService.newService(
                    pb,
                    descr,
                    toString());

            targetFutureResult = es.run();
        }
    }

    private void resume() {
        CommonTasksSupport.sendSignal(execEnv, pid, "CONT", null); // NOI18N
    }

    private void terminate() {
        synchronized (this) {
            if (targetFutureResult != null) {
                targetFutureResult.cancel(true);
            }
        }
    }

    private static final class NativeExecutableTargetExecutionService
            implements DLightTargetExecutionService<NativeExecutableTarget> {

        public InputOutput start(
                final NativeExecutableTarget target,
                final ExecutionEnvVariablesProvider executionEnvProvider) {
            Runnable r = new Runnable() {

                public void run() {
                    target.start(executionEnvProvider);
                }
            };

            if (SwingUtilities.isEventDispatchThread()) {
                DLightExecutorService.submit(r, "Start target " + toString()); // NOI18N
            } else {
                r.run();
            }
            return target.io;
        }

        public synchronized void terminate(NativeExecutableTarget target) {
            target.terminate();
        }
    }
}
