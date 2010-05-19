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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.perfan.tha.api;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.Signal;
import org.openide.util.Exceptions;

/**
 *
 * @author ak119685
 */
public final class THASuspensionSupport {

    private final ExecutionEnvironment execEnv;
    private final ScheduledFuture<?> future;
    private final Listener listener;
    private final int pid;
    private volatile Status status = Status.INIT;
    private volatile State state = State.UNKNOWN;

    private THASuspensionSupport(ExecutionEnvironment execEnv, int pid, Listener listener, boolean initiallyResumed) {
        this.execEnv = execEnv;
        this.pid = pid;
        this.listener = listener;
        this.state = initiallyResumed ? State.RESUMED : State.SUSPENDED;

        future = startMonitor();
    }

    public static THASuspensionSupport getSupportFor(ExecutionEnvironment execEnv, int pid, Listener listener, boolean initiallyResumed) {
        return new THASuspensionSupport(execEnv, pid, listener, initiallyResumed);
    }

    public Status getStatus() {
        return status;
    }

    public State getState() {
        return state;
    }

    public synchronized void resume(final boolean resume) {
        boolean resumed = state == State.RESUMED;

        if (status == Status.ENABLED && resume != resumed) {
            Runnable r = new Runnable() {

                public void run() {
                    try {
                        Future<Integer> rc = CommonTasksSupport.sendSignal(execEnv, pid, Signal.SIGUSR1, null);

                        if (rc.get() == 0) {
                            state = resume ? State.RESUMED : State.SUSPENDED;
                            listener.stateChanged(state);
                        }
                    } catch (InterruptedException ex) {
                    } catch (ExecutionException ex) {
                    }
                }
            };

            if (EventQueue.isDispatchThread()) {
                DLightExecutorService.submit(r, (resume ? "Resume" : "Suspend") + " THA data collection for " + pid + "@" + execEnv.toString()); // NOI18N
            } else {
                r.run();
            }
        }
    }

    private ScheduledFuture<?> startMonitor() {
        try {
            // Will monitor the process with PID == pid
            HostInfo hinfo = HostInfoUtils.getHostInfo(execEnv);
            boolean isSolaris = hinfo.getOSFamily() == HostInfo.OSFamily.SUNOS;
            Runnable verifier = isSolaris ? new SolarisVerifier() : new LinuxVerifier();
            return DLightExecutorService.scheduleAtFixedRate(
                    verifier, 200, TimeUnit.MILLISECONDS,
                    "Monitoring process " + pid + "@" + execEnv.toString() + // NOI18N
                    " for THA pause/resume readiness"); // NOI18N
        } catch (IOException ex) {
        } catch (CancellationException ex) {
        }

        return null;
    }

    private synchronized void stopMonitor(Status status) {
        this.status = status;
        future.cancel(false);
        listener.statusChanged(status);
    }

    private abstract class Verifier implements Runnable {

        protected NativeProcessBuilder npb;
        protected Pattern collectPattern;

        public Verifier() {
            this.npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        }

        public void run() {
            try {
                Process p = npb.call();
                String comm = ProcessUtils.readProcessOutputLine(p);
                int rc = p.waitFor();

                if (rc != 0) {
                    // No such pid ?
                    stopMonitor(Status.ERROR);
                    return;
                }

                if (!collectPattern.matcher(comm).matches()) {
                    stopMonitor(Status.ENABLED);
                }
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private class LinuxVerifier extends Verifier {

        public LinuxVerifier() {
            super();
            npb.setExecutable("ps").setArguments("-o", "comm=", "-p", "" + pid); // NOI18N
            collectPattern = Pattern.compile("^collect$"); // NOI18N
        }
    }

    private class SolarisVerifier extends Verifier {
        // On Solaris cannot use the same ps as on Linux, bacause of truncated
        // output (will fail in case of long path to collect)

        public SolarisVerifier() {
            super();
            npb.setExecutable("/bin/pargs").setArguments("-l", "" + pid); // NOI18N
            collectPattern = Pattern.compile(".*/collect .*"); // NOI18N
        }
    }

    public interface Listener {

        public void statusChanged(Status newStatus);

        public void stateChanged(State newState);
    }

    public enum State {

        UNKNOWN,
        SUSPENDED,
        RESUMED,
    }

    public enum Status {

        INIT,
        ENABLED,
        DISABLED,
        ERROR,
    }
}
