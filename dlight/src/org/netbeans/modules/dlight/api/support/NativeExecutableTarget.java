/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.api.support;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.charset.Charset;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.dlight.api.execution.AttachableTarget;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor.LineConvertorFactory;
import org.netbeans.modules.dlight.api.execution.DLightTargetChangeEvent;
import org.netbeans.modules.dlight.api.execution.SubstitutableTarget;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessChangeEvent;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionService;
import org.netbeans.modules.nativeexecution.api.util.ExternalTerminal;
import org.openide.util.Utilities;
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
    private Future<Integer> targetFutureResult;
    private LineConvertorFactory outConvertorFactory;
    private LineConvertorFactory errConvertorFactory;
    private final AtomicReference<NativeProcess> processRef = new AtomicReference<NativeProcess>();
    private static final EnumMap<NativeProcess.State, DLightTarget.State> stateTrMap = new EnumMap<NativeProcess.State, DLightTarget.State>(NativeProcess.State.class);

    static {
        stateTrMap.put(NativeProcess.State.CANCELLED, State.TERMINATED);
        stateTrMap.put(NativeProcess.State.ERROR, State.FAILED);
        stateTrMap.put(NativeProcess.State.FINISHED, State.DONE);
        stateTrMap.put(NativeProcess.State.INITIAL, State.INIT);
        stateTrMap.put(NativeProcess.State.RUNNING, State.RUNNING);
        stateTrMap.put(NativeProcess.State.STARTING, State.STARTING);
    }

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
            term = term.setTitle(cmd + ' ' + Utilities.escapeParameters(args));
        }

        this.externalTerminal = term;

        if (externalTerminal == null) {
            this.outConvertorFactory = configuration.getOutConvertorFactory();
            this.errConvertorFactory = configuration.getErrConvertorFactory();
        }

        this.templateCMD = this.cmd;
        Map<String, String> info = configuration.getInfo();

        for (Entry<String, String> entry : info.entrySet()) {
            putToInfo(entry.getKey(), entry.getValue());
        }

        this.templateArgs = args.clone();
        this.io = configuration.getIO();
        this.x11forwarding = configuration.getX11Forwarding();
    }

    @Override
    public int getPID() {
        NativeProcess p = processRef.get();
        if (p != null) {
            try {
                return p.getPID();
            } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
            }
        }
        return -1;
    }

    @Override
    public State getState() {
        NativeProcess p = processRef.get();

        if (p != null) {
            return stateTrMap.get(p.getState());
        }

        return State.INIT;
    }

    @Override
    public String toString() {
        return "Executable target: " + cmd; // NOI18N
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (!(e instanceof NativeProcessChangeEvent)) {
            return;
        }

        NativeProcessChangeEvent event = (NativeProcessChangeEvent) e;
        processRef.compareAndSet(null, (NativeProcess) event.getSource());

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

        State state = stateTrMap.get(event.state);

        switch (event.state) {
            case INITIAL:
            case STARTING:
                notifyListeners(new DLightTargetChangeEvent(NativeExecutableTarget.this, state, -1));
                break;
            case RUNNING:
                notifyListeners(new DLightTargetChangeEvent(NativeExecutableTarget.this, state, event.pid));
                break;
        }
    }

    @Override
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

    @Override
    public boolean canBeSubstituted() {
        return true;
    }

    @Override
    public void substitute(String cmd, String[] args) {
        //  isSubstituted = true;
        this.cmd = cmd;
        List<String> allArgs = new ArrayList<String>(Arrays.asList(args));
        allArgs.add(templateCMD);

        if (templateArgs != null) {
            allArgs.addAll(Arrays.asList(templateArgs));
        }

        this.args = allArgs.toArray(new String[0]);
    }

    @Override
    public ExecutionEnvironment getExecEnv() {
        return execEnv;
    }

    private void start(ExecutionEnvVariablesProvider executionEnvProvider) {
        synchronized (this) {
            NativeExecutionDescriptor descr = new NativeExecutionDescriptor();
            descr = descr.controllable(true).frontWindow(true);

            NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(execEnv);
            pb.setExecutable(cmd).setArguments(args);
            pb.addNativeProcessListener(NativeExecutableTarget.this);
            pb.setWorkingDirectory(workingDirectory);
            pb.getEnvironment().putAll(envs);
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
                    descr = descr.errConvertorFactory(errConvertorFactory);
                }
            }

            // Setup additional environment variables from executionEnvProvider
            if (executionEnvProvider != null) {
                try {
                    executionEnvProvider.setupEnvironment(this, pb.getEnvironment());
                } catch (ConnectException ex) {
                    // TODO: can it happen here?
                    log.severe(ex.getMessage());
                }
            }

            descr = descr.postExecution(new Runnable() {

                @Override
                public void run() {
                    final NativeProcess process = processRef.get();

                    if (process == null) {
                        return;
                    }

                    int rc = -1;

                    try {
                        rc = process.waitFor();
                    } catch (InterruptedException ex) {
//                        Exceptions.printStackTrace(ex);
                    }

                    notifyListeners(new DLightTargetChangeEvent(NativeExecutableTarget.this, stateTrMap.get(process.getState()), rc));
                }
            });

            Charset charset = Charset.defaultCharset();

            String charsetName = getInfo(Charset.class.getName());
            if (charsetName != null) {
                try {
                    charset = Charset.forName(charsetName);
                } catch (Exception ex) {
                }
            }
            
            descr.charset(charset);

            final NativeExecutionService es = NativeExecutionService.newService(
                    pb,
                    descr,
                    toString());

            targetFutureResult = es.run();
        }
    }

//    private void resume() {
//        CommonTasksSupport.sendSignal(execEnv, pid, Signal.SIGCONT, null);
//    }

    private void terminate() {
        synchronized (this) {
            if (targetFutureResult != null) {
                targetFutureResult.cancel(true);
            }
        }
    }

    private static final class NativeExecutableTargetExecutionService
            implements DLightTargetExecutionService<NativeExecutableTarget> {

        @Override
        public InputOutput start(
                final NativeExecutableTarget target,
                final ExecutionEnvVariablesProvider executionEnvProvider) {
            Runnable r = new Runnable() {

                @Override
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

        @Override
        public void terminate(NativeExecutableTarget target) {
            target.terminate();
        }
    }

    private final static class StateLock {
    }
}
