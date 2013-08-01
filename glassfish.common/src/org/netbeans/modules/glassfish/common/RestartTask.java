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
import org.glassfish.tools.ide.GlassFishStatus;
import static org.glassfish.tools.ide.GlassFishStatus.OFFLINE;
import static org.glassfish.tools.ide.GlassFishStatus.ONLINE;
import static org.glassfish.tools.ide.GlassFishStatus.SHUTDOWN;
import static org.glassfish.tools.ide.GlassFishStatus.STARTUP;
import static org.glassfish.tools.ide.GlassFishStatus.UNKNOWN;
import org.glassfish.tools.ide.GlassFishStatusListener;
import org.glassfish.tools.ide.admin.CommandRestartDAS;
import org.glassfish.tools.ide.admin.CommandStopDAS;
import org.glassfish.tools.ide.admin.ResultString;
import org.glassfish.tools.ide.admin.TaskState;
import org.glassfish.tools.ide.admin.TaskStateListener;
import org.glassfish.tools.ide.data.GlassFishServer;
import org.glassfish.tools.ide.data.GlassFishServerStatus;
import org.glassfish.tools.ide.data.GlassFishStatusTask;
import org.glassfish.tools.ide.data.TaskEvent;
import org.glassfish.tools.ide.utils.ServerUtils;
import static org.netbeans.modules.glassfish.common.BasicTask.START_TIMEOUT;
import static org.netbeans.modules.glassfish.common.BasicTask.TIMEUNIT;
import static org.netbeans.modules.glassfish.common.GlassFishState.getStatus;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.GlassfishModule.ServerState;
import org.openide.util.NbBundle;

/**
 *
 * @author Peter Williams
 * @author Vince Kraemer
 */
public class RestartTask extends BasicTask<TaskState> {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Notification about server state check results while waiting for server
     * to shut down.
     * <p/>
     * Handles period of time until server shuts down completely.
     * At least port checks are being executed periodically so this class will
     * be called back in any situation.
     */
    private static class ShutdownStateListener
            implements GlassFishStatusListener {

        /** Requested wake up of checking thread. */
        private volatile boolean wakeUp;

        /**
         * Constructs an instance of state check results notification.
         */
        private ShutdownStateListener() {
            wakeUp = false;
        }

        /**
         * Wake up checking thread.
         */
        private void wakeUp() {
            if (!wakeUp) synchronized(this) {
                wakeUp = true;
                this.notify();
            }
        }

        /**
         * Get status of wake up request of checking thread.
         * <p/>
         * @return Status of wake up request of checking thread.
         */
        private boolean isWakeUp() {
            return wakeUp;
        }

        /**
         * Callback to notify about current server status after every check
         * when enabled.
         * <p/>
         * Wake up restart thread when server is not in <code>SHUTDOWN</code>
         * state.
         * <p/>
         * @param server GlassFish server instance being monitored.
         * @param status Current server status.
         * @param task   Last GlassFish server status check task details.
         */
        @Override
        public void currentState(final GlassFishServer server,
                final GlassFishStatus status, final GlassFishStatusTask task) {
            if (status != SHUTDOWN) {
                wakeUp();
            }
        }

        /**
         * Callback to notify about server status change when enabled.
         * <p/>
         * Listens on <code>UNKNOWN</code>, <code>OFFLINE</code>,
         * <code>STARTUP</code> and <code>ONLINE</code> state changes where
         * we can wake up checking restart thread immediately.
         * <p/>
         * @param server GlassFish server instance being monitored.
         * @param status Current server status.
         * @param task   Last GlassFish server status check task details.
         */    
        @Override
        public void newState(final GlassFishServer server,
                final GlassFishStatus status, final GlassFishStatusTask task) {
            wakeUp();
        }

        /**
         * Callback to notify about server status check failures.
         * <p/>
         * @param server GlassFish server instance being monitored.
         * @param event  Failure event.
         * @param task   GlassFish server status check task details.
         */
        @Override
        public void error(final GlassFishServer server,
                final GlassFishStatusTask task) {
            // Not used yet.
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER = GlassFishLogger.get(RestartTask.class);

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    private static final int RESTART_DELAY = 5000;

    /** Common support object for the server instance being restarted. */
    private final CommonServerSupport support;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of asynchronous GlassFish server restart command
     * execution support object.
     * <p/>
     * @param support       Common support object for the server instance being
     *                      restarted
     * @param stateListener State monitor to track restart progress.
     */
    public RestartTask(CommonServerSupport support, TaskStateListener... stateListener) {
        super(support.getInstance(), stateListener);
        this.support = support;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Initialize GlassFisg server startup monitoring.
     * <p/>
     * Creates and registers listener to monitor server status during shutdown.
     * <p/>
     * @return Listener instance when server startup monitoring was successfully
     *         initialized or  <code>null</code> when something failed.
     */
    private ShutdownStateListener prepareShutdownMonitoring() {
        ShutdownStateListener listener
                = new ShutdownStateListener();
        if (GlassFishStatus.addListener(instance, listener, true,
                GlassFishStatus.UNKNOWN, GlassFishStatus.OFFLINE,
                GlassFishStatus.STARTUP, GlassFishStatus.ONLINE)
                ) {
            return listener;
        } else {
            GlassFishStatus.removeListener(instance, listener);
            return null;
        }
    }

    /**
     * Start local server that is offline.
     * <p/>
     * @return State change request about offline remote server start request.
     */
    private StateChange localOfflineStart() {
        Future<TaskState> startTask
                = support.startServer(null, ServerState.RUNNING);
        TaskState startResult = TaskState.FAILED;
        try {
            startResult = startTask.get(START_TIMEOUT, TIMEUNIT);
        } catch (Exception ex) {
            LOGGER.log(Level.FINER,
                    ex.getLocalizedMessage(), ex);
        }
        if (startResult == TaskState.FAILED) {
            return new StateChange(this,
                    TaskState.FAILED, TaskEvent.CMD_FAILED,
                    "RestartTask.localOfflineStart.failed", instanceName);
        }
        return new StateChange(this,
                TaskState.COMPLETED, TaskEvent.CMD_COMPLETED,
                "RestartTask.localOfflineStart.completed", instanceName);
    }

    /**
     * Start remote server that is offline.
     * <p/>
     * This operation is not possible and will always fail.
     * <p/>
     * @return State change request about offline remote server start request.
     */
    private StateChange remoteOfflineStart() {
            return new StateChange(this,
                    TaskState.FAILED, TaskEvent.ILLEGAL_STATE,
                    "RestartTask.remoteOfflineStart.failed", instanceName);
    }

    /**
     * Wait for local server currently shutting down and start it up.
     * <p/>
     * @return State change request about local server (that is shutting down)
     *         start request.
     */
    private StateChange localShutdownStart() {
        ShutdownStateListener listener = prepareShutdownMonitoring();
        if (listener == null) {
            return new StateChange(this,
                    TaskState.FAILED, TaskEvent.ILLEGAL_STATE,
                    "RestartTask.localShutdownStart.listenerError",
                    instanceName);
        }
        long start = System.currentTimeMillis();
        LOGGER.log(Level.FINEST, NbBundle.getMessage(RestartTask.class,
                "RestartTask.localShutdownStart.waitingTime",
                new Object[] {Integer.toString(STOP_TIMEOUT)}));
        try {
            synchronized(listener) {
                while (!listener.isWakeUp()
                        && (System.currentTimeMillis()
                        - start < STOP_TIMEOUT)) {
                    listener.wait(System.currentTimeMillis() - start);
                }
            }
        } catch (InterruptedException ie) {
            LOGGER.log(Level.INFO, NbBundle.getMessage(RestartTask.class,
                    "RestartTask.localShutdownStart.interruptedException",
                    new Object[] {
                        instance.getName(), ie.getLocalizedMessage()}));
            
        } finally {
            GlassFishStatus.removeListener(instance, listener);
        }
        LogViewMgr.removeLog(instance);
        LogViewMgr logger = LogViewMgr.getInstance(
                instance.getProperty(GlassfishModule.URL_ATTR));
        logger.stopReaders();                
        GlassFishServerStatus status = getStatus(instance);
        switch(status.getStatus()) {
            case UNKNOWN: case ONLINE: case SHUTDOWN: case STARTUP:
                return new StateChange(this,
                    TaskState.FAILED, TaskEvent.ILLEGAL_STATE,
                    "RestartTask.localShutdownStart.notOffline",
                    instanceName);
            default:
                if (!ServerUtils.isDASRunning(instance)) {
                    return localOfflineStart();
                } else {
                return new StateChange(this,
                    TaskState.FAILED, TaskEvent.ILLEGAL_STATE,
                    "RestartTask.localShutdownStart.portOccupied",
                    instanceName);                    
                }
        }
    }

    /**
     * Wait for remote server currently shutting down and start it up.
     * <p/>
     * This operation is not possible and will always fail.
     * <p/>
     * @return State change request about remote server (that is shutting down)
     *         start request.
     */
    private StateChange remoteShutdownStart() {
            return new StateChange(this,
                    TaskState.FAILED, TaskEvent.ILLEGAL_STATE,
                    "RestartTask.remoteShutdownStart.failed", instanceName);        
    }

    /**
     * Wait for server to start up.
     * <p/>
     * @return State change request.
     */
    private StateChange startupWait() {
        StartStateListener listener = prepareStartMonitoring(true);
        if (listener == null) {
            return new StateChange(this,
                    TaskState.FAILED, TaskEvent.ILLEGAL_STATE,
                    "RestartTask.startupWait.listenerError",
                    instanceName);
        }
        long start = System.currentTimeMillis();
        try {
            synchronized(listener) {
                while (!listener.isWakeUp()
                        && (System.currentTimeMillis()
                        - start < START_TIMEOUT)) {
                    listener.wait(System.currentTimeMillis() - start);
                }
            }
        } catch (InterruptedException ie) {
            LOGGER.log(Level.INFO, NbBundle.getMessage(RestartTask.class,
                    "RestartTask.startupWait.interruptedException",
                    new String[] {
                        instance.getName(), ie.getLocalizedMessage()}));
            
        } finally {
            GlassFishStatus.removeListener(instance, listener);
        }
        if (GlassFishState.isOnline(instance)) {
              return new StateChange(this,
                      TaskState.COMPLETED, TaskEvent.CMD_COMPLETED,
                      "RestartTask.startupWait.completed", instanceName);
        } else {
              return new StateChange(this,
                      TaskState.FAILED, TaskEvent.ILLEGAL_STATE,
                      "RestartTask.startupWait.failed", instanceName);
        }
    }

    /**
     * Full restart of local online server.
     * <p/>
     * @return State change request.
     */
    private StateChange localRestart() {
        if (GlassFishStatus.shutdown(instance)) {
            ResultString result = CommandStopDAS.stopDAS(instance);
            if (result.getState() == TaskState.COMPLETED) {
                return localShutdownStart();
            } else {
                // TODO: Reset server status monitoring
                return new StateChange(this,
                        TaskState.FAILED, TaskEvent.CMD_FAILED,
                        "RestartTask.localRestart.cmdFailed", instanceName);
            }
        } else {
            return new StateChange(this,
                    TaskState.FAILED, TaskEvent.ILLEGAL_STATE,
                    "RestartTask.localRestart.failed", instanceName);
        }
    }

    /**
     * Full restart of remote online server.
     * <p/>
     * @return State change request.
     */
    private StateChange remoteRestart() {
        ResultString result
                = CommandRestartDAS.restartDAS(instance, false);
        LogViewMgr.removeLog(instance);
        LogViewMgr logger = LogViewMgr.getInstance(
                instance.getProperty(GlassfishModule.URL_ATTR));
        logger.stopReaders();                
        switch (result.getState()) {
            case COMPLETED:
                return new StateChange(this,
                        result.getState(), TaskEvent.CMD_COMPLETED,
                        "RestartTask.remoteRestart.completed", instanceName);
            default:
                return new StateChange(this,
                        result.getState(), TaskEvent.CMD_COMPLETED,
                        "RestartTask.remoteRestart.failed", new String[] {
                            instanceName, result.getValue()});
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // ExecutorService call() Method                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Restart GlassFish server.
     * <p/>
     * Possible states are <code>UNKNOWN</code>, <code>OFFLINE</code>,
     * <code>STARTUP</code>, <code>ONLINE</code> and <code>SHUTDOWN</code>:
     * <p/>
     * <code>UNKNOWN</code>:  Do nothing. UI shall not allow restarting while
     *                        server status is unknown.
     * <code>OFFLINE</code>:  Server is already offline, let's start it
     *                        if administrator port is not occupied.
     * <code>STARTUP</code>:  We are already in the middle of startup process.
     *                        Let's just wait for sever to start.
     * <code>ONLINE</code>:   Full restart is needed.
     * <code>SHUTDOWN</code>: Shutdown process has already started, let's wait
     *                        for it to finish. Server will be started after
     *                        that.
     */
//    @Override
    public TaskState call2() {
        GlassFishStatus state = GlassFishState.getStatus(instance).getStatus();
        StateChange change;
        switch (state) {
            case UNKNOWN:
                return fireOperationStateChanged(
                        TaskState.FAILED, TaskEvent.ILLEGAL_STATE,
                        "RestartTask.call.unknownState", instanceName);
            case OFFLINE:
                change = instance.isRemote()
                        ? remoteOfflineStart() : localOfflineStart();
                return change.fireOperationStateChanged();
            case STARTUP:
                change = startupWait();
                return change.fireOperationStateChanged();
            case ONLINE:
                change = instance.isRemote()
                        ? remoteRestart() : localRestart();
                return change.fireOperationStateChanged();
            case SHUTDOWN:
                change = instance.isRemote()
                        ? remoteShutdownStart() : localShutdownStart();
                return change.fireOperationStateChanged();
            // This shall be unrechable, all states should have
            // own case handlers.
            default:
                return fireOperationStateChanged(
                        TaskState.FAILED, TaskEvent.ILLEGAL_STATE,
                        "RestartTask.call.unknownState", instanceName);                
        }
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

        //ServerState state = support.getServerState();
        GlassFishStatus state = GlassFishState.getStatus(instance).getStatus();

        if (state == GlassFishStatus.STARTUP) {
            // wait for start to finish, we are done.
            GlassFishStatus currentState = state;
            int steps = (START_TIMEOUT / DELAY);
            int count = 0;
            while (currentState == GlassFishStatus.STARTUP && count++ < steps) {
                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException ex) {
                    Logger.getLogger("glassfish").log(Level.FINER,
                            ex.getLocalizedMessage(), ex);
                }
                currentState = GlassFishState.getStatus(instance).getStatus();
            }

            if (!GlassFishState.isOnline(instance)) {
                return fireOperationStateChanged(TaskState.FAILED,
                        TaskEvent.CMD_FAILED,
                        "MSG_RESTART_SERVER_FAILED_WONT_START", instanceName);
            }
        } else {
            boolean postStopDelay = true;
            if (state == GlassFishStatus.ONLINE) {
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
            } else if (state == GlassFishStatus.SHUTDOWN) {
                // wait for server to stop.
                GlassFishStatus currentState = state;
                int steps = (STOP_TIMEOUT / DELAY);
                int count = 0;
                while (currentState == GlassFishStatus.SHUTDOWN && count++ < steps) {
                    try {
                        Thread.sleep(DELAY);
                    } catch (InterruptedException ex) {
                        Logger.getLogger("glassfish").log(Level.FINER,
                                ex.getLocalizedMessage(), ex);
                    }
                    currentState = GlassFishState.getStatus(instance).getStatus();
                }

                if (!GlassFishState.isOffline(instance)) {
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
            Future<TaskState> startTask = support.startServer(null, ServerState.RUNNING);
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
