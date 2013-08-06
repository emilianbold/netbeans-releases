/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.glassfish.common;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.glassfish.tools.ide.GlassFishStatus;
import static org.glassfish.tools.ide.GlassFishStatus.OFFLINE;
import static org.glassfish.tools.ide.GlassFishStatus.ONLINE;
import static org.glassfish.tools.ide.GlassFishStatus.SHUTDOWN;
import static org.glassfish.tools.ide.GlassFishStatus.STARTUP;
import org.glassfish.tools.ide.admin.TaskState;
import org.glassfish.tools.ide.admin.TaskStateListener;
import org.glassfish.tools.ide.data.GlassFishServer;
import org.glassfish.tools.ide.data.GlassFishStatusCheckResult;
import org.glassfish.tools.ide.data.GlassFishStatusTask;
import org.glassfish.tools.ide.data.TaskEvent;
import org.netbeans.modules.glassfish.common.status.WakeUpStateListener;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.openide.util.NbBundle;

/**
 * Basic common functionality of commands execution.
 * <p/>
 * @author Peter Williams, Tomas Kraus
 */
public abstract class BasicTask<V> implements Callable<V> {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Notification about server state check results while waiting for server
     * to start.
     * <p/>
     * Handles initial period of time after starting server.
     * At least port checks are being executed periodically so this class will
     * be called back in any situation.
     */
    protected static class StartStateListener extends WakeUpStateListener {

        /** Is server starting in profiling mode? */
        private final boolean profile;

        /**
         * Constructs an instance of state check results notification.
         * <p/>
         * @param profile Server is starting in profiling mode when
         *                <code>true</code>.
         */
        protected StartStateListener(final boolean profile) {
            super();
            this.profile = profile;
        }

        /**
         * Callback to notify about current server status after every check
         * when enabled.
         * <p/>
         * Wake up startup thread when administrator port is active
         * in profiling mode or when illegal state was detected.
         * <p/>
         * @param server GlassFish server instance being monitored.
         * @param status Current server status.
         * @param task   Last GlassFish server status check task details.
         */
        @Override
        public void currentState(final GlassFishServer server,
                final GlassFishStatus status, final GlassFishStatusTask task) {
            switch(status) {
                // Consider server as ready when administrator port is active
                // in profiling mode.
                case OFFLINE: case STARTUP:
                    if (profile && task.getStatus()
                            == GlassFishStatusCheckResult.SUCCESS) {
                        wakeUp();
                    }
                    break;
                // Interrupt waiting for illegal states.
                case ONLINE: case SHUTDOWN:
                    wakeUp();
                    break;
            }
        }

    }

    /**
     * State change request data.
     */
    protected static class StateChange {

        /** Command execution task. */
        private final BasicTask task;

        /** New state of current command execution. */
        private final TaskState result;

        /** Event that caused  state change. */
        private final TaskEvent event;

        /** Message bundle key. */
        private final String msgKey;

        /** Message arguments. */
        private final String[] msgArgs;

        /**
         * Constructs an instance of state change request data.
         * <p/>
         * @param task   Command execution task.
         * @param result New state of current command execution.
         * @param event  Event that caused  state change.
         * @param msgKey Message bundle key.
         */
        protected StateChange(final BasicTask task, final TaskState result,
                final TaskEvent event, final String msgKey) {
            this.task = task;
            this.result = result;
            this.event = event;
            this.msgKey = msgKey;
            this.msgArgs = null;
        }

        /**
         * Constructs an instance of state change request data.
         * <p/>
         * @param task    Command execution task.
         * @param result  New state of current command execution.
         * @param event   Event that caused  state change.
         * @param msgKey  Message bundle key.
         * @param msgArgs Message arguments.
         */
        protected StateChange(final BasicTask task, final TaskState result,
                final TaskEvent event, final String msgKey,
                final String... msgArgs) {
            this.task = task;
            this.result = result;
            this.event = event;
            this.msgKey = msgKey;
            this.msgArgs = msgArgs;
        }

        protected TaskState fireOperationStateChanged() {
            return task.fireOperationStateChanged(
                    result, event, msgKey, msgArgs);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Wait duration (ms) between server status checks. */
    public static final int DELAY = 250;
    
    /** Maximum amount of time (in ms) to wait for server to start. */
    public static final int START_TIMEOUT = 1200000;
    
    /** Maximum amount of time (in ms) to wait for server to stop. */
    public static final int STOP_TIMEOUT = 600000;

    /** Unit (ms) for the DELAY and START_TIMEOUT constants. */
    public static final TimeUnit TIMEUNIT = TimeUnit.MILLISECONDS;

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish instance accessed in this task. */
    GlassfishInstance instance;

    /** Callback to retrieve state changes. */
    protected TaskStateListener [] stateListener;

    /** Name of GlassFish instance accessed in this task. */
    protected String instanceName;

    ////////////////////////////////////////////////////////////////////////////
    // Abstract methods                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Command execution is implemented as <code>call()</code> method in child
     * classes.
     * <p/>
     * @return Command execution result.
     */
    @Override
    public abstract V call();

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of <code>BasicTask</code> class.
     * <p/>
     * @param instance GlassFish instance accessed in this task.
     * @param stateListener Callback listeners used to retrieve state changes.
     */
    protected BasicTask(GlassfishInstance instance,
            TaskStateListener... stateListener) {
        this.instance = instance;
        this.stateListener = stateListener;
        this.instanceName = instance.getProperty(
                GlassfishModule.DISPLAY_NAME_ATTR);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Initialize GlassFisg server startup monitoring.
     * <p/>
     * Creates and registers listener to monitor server status during startup.
     * Switches server status monitoring into startup mode.
     * <p/>
     * @param profile Server is starting in profiling mode when
     *                <code>true</code>.
     * @return Listener instance when server startup monitoring was successfully
     *         initialized or  <code>null</code> when something failed.
     */
    protected StartStateListener prepareStartMonitoring(final boolean profile) {
        StartStateListener listener = new StartStateListener(profile);
        if (GlassFishStatus.addListener(instance, listener, true,
                GlassFishStatus.ONLINE, GlassFishStatus.SHUTDOWN)
                && GlassFishStatus.start(instance)) {
            return listener;
        } else {
            GlassFishStatus.removeListener(instance, listener);
            return null;
        }
    }

    /**
     * Call all registered callback listeners to inform about state change.
     * <p/>
     * @param stateType New state of current command execution sent
     *        to listeners. This value will be returned by this method.
     * @param resName Name of the resource to look for message.
     * @param args Additional arguments passed to message.
     * @return Passed new state of current command.
     */
    protected final TaskState fireOperationStateChanged(
            TaskState stateType, TaskEvent te, String resName, String... args) {
        if(stateListener != null && stateListener.length > 0) {
            String msg = NbBundle.getMessage(BasicTask.class, resName, args);
            for(int i = 0; i < stateListener.length; i++) {
                if(stateListener[i] != null) {
                    stateListener[i].operationStateChanged(stateType, te, msg);
                }
            }
        }
        return stateType;
    }
}
