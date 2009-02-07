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

import org.netbeans.modules.dlight.api.execution.AttachableTarget;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.modules.dlight.api.execution.SubstitutableTarget;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess.Listener;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.util.ExternalTerminalProvider;
import org.openide.util.RequestProcessor;

/**
 * Wrapper of {@link @org-netbeans-modules-nativexecution@org/netbeans/modules/nativexecution/api/NativeTask.html}
 *
 */
public final class NativeExecutableTarget extends DLightTarget implements SubstitutableTarget, AttachableTarget, Listener {

    private static final Logger log =
            DLightLogger.getLogger(NativeExecutableTarget.class);
    private final ExecutionEnvironment execEnv;
    private Future<Integer> targetFutureResult;
    private String cmd;
    private String templateCMD;
    private String[] args;
    private String[] templateArgs;
    private String extendedCMD;
    private String[] extendedCMDArgs;
    private volatile int pid = -1;
    private volatile State state;

    public NativeExecutableTarget(NativeExecutableTargetConfiguration configuration) {
        super(new NativeExecutableTargetExecutionService());
        this.execEnv = configuration.getExecutionEvnitoment();
        this.cmd = configuration.getCmd();
        this.templateCMD = this.cmd;
        this.args = configuration.getArgs();
        if (this.args != null) {
            this.templateArgs = new String[args.length];
            for (int i = 0; i < args.length; i++) {
                templateArgs[i] = args[i];
            }
        }
    }

    public int getPID() {
        return pid;
    }

    public State getState() {
        return state;
    }

    @Override
    public String toString() {
        return "Executable target: " + cmd; // NOI18N
    }

    public void processStateChanged(NativeProcess process, NativeProcess.State oldState, NativeProcess.State newState) {
        final DLightTarget.State prevState = state;

        switch (newState) {
            case INITIAL:
                state = State.INIT;
                break;
            case STARTING:
                state = State.STARTING;
                break;
            case RUNNING:
                state = State.RUNNING;
                this.pid = process.getPID();
                break;
            case CANCELLED:
                state = State.TERMINATED;
                log.info("NativeTask " + process.toString() + " cancelled!"); // NOI18N
                break;
            case ERROR:
                state = State.FAILED;
                log.info("NativeTask " + process.toString() + // NOI18N
                        " finished with error! "); // NOI18N
                break;
            case FINISHED:
                state = State.DONE;
                break;

        }

        notifyListeners(prevState, state);
    }

    public boolean canBeSubstituted() {
        return true;
    }

    public void substitute(String cmd, String[] args) {
        //  isSubstituted = true;
        extendedCMD = cmd;
        extendedCMDArgs = args;
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
        NativeProcessBuilder pb = new NativeProcessBuilder(execEnv, cmd);
        pb = pb.setArguments(args);
        pb = pb.addNativeProcessListener(NativeExecutableTarget.this);
        if (executionEnvProvider != null && executionEnvProvider.getExecutionEnv() != null){
            pb.addEnvironmentVariables(executionEnvProvider.getExecutionEnv());
        }
        ExecutionDescriptor descr = new ExecutionDescriptor();
        descr = descr.controllable(true).frontWindow(true);

        boolean useTerm = true;

        if (useTerm) {
            pb = pb.useExternalTerminal(
                    ExternalTerminalProvider.getTerminal("gnome-terminal"));
            descr.inputVisible(false);
        }

        final ExecutionService es = ExecutionService.newService(
                pb,
                descr,
                toString());

        // Because of possible prompts for passwords we need to start
        // this in non-AWT thread...
        Runnable r = new Runnable() {

            public void run() {
                targetFutureResult = es.run();
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            RequestProcessor.getDefault().post(r);
        } else {
            r.run();
        }

    }

    private void terminate() {
        if (targetFutureResult != null && !targetFutureResult.isDone()) {
            targetFutureResult.cancel(true);
        }
    }

    private static final class NativeExecutableTargetExecutionService
            implements DLightTargetExecutionService<NativeExecutableTarget> {

        public void start(NativeExecutableTarget target, ExecutionEnvVariablesProvider executionEnvProvider) {
            target.start(executionEnvProvider);
        }

        public void terminate(NativeExecutableTarget target) {
            target.terminate();
        }
    }
}
