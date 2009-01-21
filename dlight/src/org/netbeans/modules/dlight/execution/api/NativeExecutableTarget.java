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
package org.netbeans.modules.dlight.execution.api;


import org.netbeans.modules.dlight.execution.api.support.IOTabManagerFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.NativeTaskListener;
import org.netbeans.modules.nativeexecution.api.NativeTask;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 * Wrapper of {@link org.netbeans.modules.nativeexecution.NativeTask}
 * 
 */
public class NativeExecutableTarget implements DLightTarget, AttachableTarget, NativeTaskListener {

    private static final Logger log = DLightLogger.getLogger(NativeExecutableTarget.class);
    private List<DLightTargetListener> listeners = Collections.synchronizedList(new ArrayList<DLightTargetListener>());
    private final ExecutionEnvironment execEnv;
    private NativeTask task;
    private String cmd;
    private String templateCMD;
    private String[] args;
    private String[] templateArgs;
    private String extendedCMD;
    private String[] extendedCMDArgs;
    private boolean isSubstituted = false;

    public NativeExecutableTarget(NativeExecutableTargetConfiguration configuration) {
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

    public void start() {
        task = new NativeTask(execEnv, cmd, args);
        task.setInputOutput(IOTabManagerFactory.getIOTabManager().getIO(task, true));
        task.addListener(this);
        task.submit();
    }

    public int getPID() {
        return task.getPID();
    }

    public State getState() {
        // Do mapping between DLightTarget.State and NativeTask state
        switch (task.getState()) {
            case INITIAL:
                return State.INIT;
            case STARTING:
                return State.STARTING;
            case FAILED:
                return State.FAILED;
            case RUNNING:
                return State.RUNNING;
            case CANCELED:
                return State.TERMINATED;
            case FINISHED:
                return State.DONE;
        }

        return null;
    }

    @Override
    public String toString() {
        return "Executable target: " + cmd;
    }

    public void terminate() {
        if (task == null || !task.isRunning()) {
            return;
        }

        task.cancel();
    }

    public void addTargetListener(DLightTargetListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeTargetListener(DLightTargetListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    public void taskStarted(NativeTask task) {
        for (DLightTargetListener l : listeners.toArray(new DLightTargetListener[0])) {
            if (task.isDone()) {
                break;
            }
            l.targetStarted(this);
        }
    }

    private void targetFinished(Integer result) {
        for (DLightTargetListener l : listeners.toArray(new DLightTargetListener[0])) {
            l.targetFinished(this, result);
        }
    }

    public void taskFinished(NativeTask task, Integer result) {
        targetFinished(result);
    }

    public void taskCancelled(NativeTask task, CancellationException cex) {
        log.info("NativeTask " + task.toString() + " cancelled!");
        targetFinished(-1);
    }

    public void taskError(NativeTask task, Throwable t) {
        log.info("NativeTask " + task.toString() + " finished with error! " + t);
        targetFinished(-1);
    }

    public boolean canBeSubstituted() {
        return true;
    }

    public void substitute(String cmd, String[] args) {
        isSubstituted = true;
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
}
