/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.glassfish.common;

import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.tools.ide.data.TaskEvent;
import org.glassfish.tools.ide.admin.TaskState;
import org.glassfish.tools.ide.admin.TaskStateListener;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.GlassfishModule.ServerState;

/**
 *
 * @author Peter Williams
 * @author Vince Kraemer
 */
public class RestartTask extends BasicTask<TaskState> {

    private static final int RESTART_DELAY = 5000;

    private final CommonServerSupport support;

    /**
     * 
     * @param support common support object for the server instance being restarted
     * @param stateListener state monitor to track start progress
     */
    public RestartTask(CommonServerSupport support, TaskStateListener... stateListener) {
        super(support.getInstance(), stateListener);
        this.support = support;
    }
    
    /**
     * Restart operation:
     *
     * RUNNING -> stop server
     *            start server
     *
     * STARTING -> wait for state == STOPPED or RUNNING.
     *
     * STOPPED -> start server
     *
     * STOPPING -> wait for state == STOPPED
     *             start server
     *
     * For all of the above, command succeeds if state == RUNNING at the end.
     * 
     */
    @SuppressWarnings("SleepWhileInLoop")
    @Override
    public TaskState call() {
        Logger.getLogger("glassfish").log(Level.FINEST,
                "RestartTask.call() called on thread \"{0}\"",
                Thread.currentThread().getName());
        fireOperationStateChanged(TaskState.RUNNING, TaskEvent.CMD_RUNNING,
                "MSG_RESTART_SERVER_IN_PROGRESS", instanceName);

        ServerState state = support.getServerState();

        if (state == ServerState.STARTING) {
            // wait for start to finish, we are done.
            ServerState currentState = state;
            int steps = (START_TIMEOUT / DELAY);
            int count = 0;
            while (currentState == ServerState.STARTING && count++ < steps) {
                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException ex) {
                    Logger.getLogger("glassfish").log(Level.FINER,
                            ex.getLocalizedMessage(), ex);
                }
                currentState = support.getServerState();
            }

            if (currentState != ServerState.RUNNING) {
                return fireOperationStateChanged(TaskState.FAILED,
                        TaskEvent.CMD_FAILED,
                        "MSG_RESTART_SERVER_FAILED_WONT_START", instanceName);
            }
        } else {
            boolean postStopDelay = true;
            if (state == ServerState.RUNNING) {
                    Future<TaskState> stopTask = support.stopServer(null);
                    TaskState stopResult = TaskState.FAILED;
                    try {
                        stopResult = stopTask.get(STOP_TIMEOUT, TIMEUNIT);
                    } catch (Exception ex) {
                        Logger.getLogger("glassfish").log(Level.FINER,
                                ex.getLocalizedMessage(), ex);
                    }

                    if (stopResult == TaskState.FAILED) {
                        return fireOperationStateChanged(TaskState.FAILED,
                                TaskEvent.CMD_FAILED,
                                "MSG_RESTART_SERVER_FAILED_WONT_STOP",
                                instanceName);
                    }
            } else if (state == ServerState.STOPPING) {
                // wait for server to stop.
                ServerState currentState = state;
                int steps = (STOP_TIMEOUT / DELAY);
                int count = 0;
                while (currentState == ServerState.STOPPING && count++ < steps) {
                    try {
                        Thread.sleep(DELAY);
                    } catch (InterruptedException ex) {
                        Logger.getLogger("glassfish").log(Level.FINER,
                                ex.getLocalizedMessage(), ex);
                    }
                    currentState = support.getServerState();
                }

                if (currentState != ServerState.STOPPED) {
                    return fireOperationStateChanged(TaskState.FAILED,
                            TaskEvent.CMD_FAILED,
                            "MSG_RESTART_SERVER_FAILED_WONT_STOP",
                            instanceName);
                }
            } else {
                postStopDelay = false;
            }

                if (postStopDelay) {
                    // If we stopped the server (or it was already stopping), delay
                    // start for a few seconds to let system clean up ports.
                    support.setServerState(ServerState.STARTING);
                    try {
                        Thread.sleep(RESTART_DELAY);
                    } catch (InterruptedException ex) {
                        // ignore
                    }
                }

                // Server should be stopped. Start it.
                Object o = support.setEnvironmentProperty(
                        GlassfishModule.JVM_MODE,
                        GlassfishModule.NORMAL_MODE, false);
                if (GlassfishModule.PROFILE_MODE.equals(o)) {
                    support.setEnvironmentProperty(GlassfishModule.JVM_MODE,
                            GlassfishModule.NORMAL_MODE, false);
                }
                Future<TaskState> startTask
                        = support.startServer(null, ServerState.RUNNING);
                TaskState startResult = TaskState.FAILED;
                try {
                    startResult = startTask.get(START_TIMEOUT, TIMEUNIT);
                } catch (Exception ex) {
                    Logger.getLogger("glassfish").log(Level.FINER,
                            ex.getLocalizedMessage(), ex); // NOI18N
                }

                if (startResult == TaskState.FAILED) {
                    return fireOperationStateChanged(TaskState.FAILED,
                            TaskEvent.CMD_FAILED,
                            "MSG_RESTART_SERVER_FAILED_WONT_START",
                            instanceName);
                }

                if (!support.isRemote()
                        && support.getServerState() != ServerState.RUNNING) {
                    return fireOperationStateChanged(TaskState.FAILED,
                            TaskEvent.CMD_FAILED,
                            "MSG_RESTART_SERVER_FAILED_REASON_UNKNOWN",
                            instanceName);
                }
        }

        return fireOperationStateChanged(TaskState.COMPLETED,
                TaskEvent.CMD_COMPLETED,
                "MSG_SERVER_RESTARTED", instanceName);
    }
}
