/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.common;

import java.util.logging.Logger;
import org.glassfish.tools.ide.GlassFishStatus;
import static org.glassfish.tools.ide.GlassFishStatus.OFFLINE;
import static org.glassfish.tools.ide.GlassFishStatus.UNKNOWN;
import org.glassfish.tools.ide.GlassFishStatusListener;
import org.glassfish.tools.ide.data.GlassFishServer;
import org.glassfish.tools.ide.data.GlassFishServerStatus;
import org.glassfish.tools.ide.data.GlassFishStatusCheck;
import static org.glassfish.tools.ide.data.GlassFishStatusCheck.LOCATIONS;
import static org.glassfish.tools.ide.data.GlassFishStatusCheck.VERSION;
import org.glassfish.tools.ide.data.GlassFishStatusCheckResult;
import org.glassfish.tools.ide.data.GlassFishStatusTask;
import org.glassfish.tools.ide.utils.ServerUtils;
import org.openide.util.NbBundle;

/**
 * Server state checks.
 * <p/>
 * @author Tomas Kraus
 */
public class GlassFishState {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Check mode.
     * <p/>
     * Allows to select server state check mode.
     */
    public static enum Mode {

        ////////////////////////////////////////////////////////////////////////
        // Enum values                                                        //
        ////////////////////////////////////////////////////////////////////////

        /** Default server state check mode. All special features
         *  are turned off. */
        DEFAULT,
        /** Startup mode. Sets longer administration commands timeout
         *  and displays GlassFish 3.1.2 WS bug warning. */
        STARTUP,
        /** Refresh mode. Displays enable-secure-admin warning
         *  for remote servers. */
        REFRESH;

        ////////////////////////////////////////////////////////////////////////
        // Methods                                                            //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Convert <code>Mode</code> value to <code>String</code>.
         * <p/>
         * @return A <code>String</code> representation of the value
         *         of this object.
         */
        @Override
        public String toString() {
            switch(this) {
                case DEFAULT: return "DEFAULT";
                case STARTUP: return "STARTUP";
                case REFRESH: return "REFRESH";
                default: throw new IllegalStateException("Unknown Mode value");
            }
        }

    }
    /**
     * Notification about server state check results.
     * <p/>
     * Handles initial period of time after adding new server into status
     * monitoring.
     * At least port checks are being executed periodically so this class will
     * be called back in any situation.
     */
    private static class StateListener implements GlassFishStatusListener {

        ////////////////////////////////////////////////////////////////////////
        // Instance attributes                                                //
        ////////////////////////////////////////////////////////////////////////

        /** Requested wake up of checking thread. */
        private volatile boolean wakeUp;

        /** Number of verification checks passed. */
        private short count;

        ////////////////////////////////////////////////////////////////////////
        // Constructors                                                       //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Constructs an instance of state check results notification.
         */
        private StateListener() {
            wakeUp = false;
            count = 0;
        }

        ////////////////////////////////////////////////////////////////////////
        // Methods                                                            //
        ////////////////////////////////////////////////////////////////////////

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
         * Wait for more checking cycles to make sure server status monitoring
         * has settled down.
         * <p/>
         * @param server GlassFish server instance being monitored.
         * @param status Current server status.
         * @param task   Last GlassFish server status check task details.
         */
        @Override
        public void currentState(final GlassFishServer server,
                final GlassFishStatus status, final GlassFishStatusTask task) {
            count++;
            switch(status) {
                case UNKNOWN:
                    // Something should be wrong when state is UNKNOWN after
                    // port check.
                    if (task != null
                            && task.getType() == GlassFishStatusCheck.PORT) {
                        wakeUp();
                    // Otherwise wait for 2 checks.
                    } else if (count > 1) {
                        wakeUp();
                    }
                    break;
                // Wait for 4 internal checks in OFFLINE state.
                case OFFLINE:
                    // Command check failure means server is really not online.
                    if (task != null) {
                       switch(task.getType()) {
                           case LOCATIONS: case VERSION:
                               if (task.getStatus()
                                       == GlassFishStatusCheckResult.FAILED) {
                                   wakeUp();
                                   // Skip 2nd wake up.
                                   count = 0;
                               }
                       }
                    }
                    // Otherwise wait for 3 internal checks in OFFLINE state.
                    if (count > 2) {
                        wakeUp();
                    }
                    break;
                // Wake up after 1st check in any other state.
                default:
                    wakeUp();
            }
        }

        /**
         * Callback to notify about server status change when enabled.
         * <p/>
         * Listens on <code>ONLINE</code>, <code>SHUTDOWN</code>
         * and <code>STARTUP</code> state changes where we can wake up checking
         * thread immediately.
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
    private static final Logger LOGGER
            = GlassFishLogger.get(GlassFishState.class);

    /** Initial server status check timeout [ms]. Maximum period of time to wait
     *  for status monitoring to settle down. */
    private static final int INIT_MONITORING_TIMEOUT = 5000;

    /**
     * Start monitoring GlassFish server.
     * <p/>
     * This method may cause delay when server status was not monitored before
     * to give status monitoring time to settle down.
     * <p/>
     * @param instance GlassFish server instance to be monitored.
     */
    public static boolean monitor(final GlassFishServer instance) {
        boolean added;
        // Check if server is already being monitored.
        GlassFishServerStatus status = GlassFishStatus.get(instance);
        if (status == null) {
            StateListener listener = new StateListener();
            added = GlassFishStatus.add(instance, listener, true,
                    GlassFishStatus.ONLINE, GlassFishStatus.SHUTDOWN,
                    GlassFishStatus.STARTUP);
            if (added) {
                try {
                    long startTime = System.currentTimeMillis();
                    synchronized (listener) {
                        // Guard against spurious wakeup.
                        while (!listener.isWakeUp()
                                && (System.currentTimeMillis()
                                - startTime < INIT_MONITORING_TIMEOUT)) {
                            listener.wait(
                                    System.currentTimeMillis() - startTime);
                        }
                    }
                } catch (InterruptedException ie) {
                } finally {
                    GlassFishStatus.removeListener(instance, listener);
                }
            }
        } else {
            added = false;
        }
        return added;
    }

    /**
     * Retrieve GlassFish server status object from status monitoring.
     * <p/>
     * @param instance GlassFish server instance.
     * @return GlassFish server status object.
     * @throws IllegalStateException when status object is null even after 
     *         monitoring of this instance was explicitely started.
     */
    public static GlassFishServerStatus getStatus(
            final GlassFishServer instance) {
        GlassFishServerStatus status = GlassFishStatus.get(instance);
        if (status == null) {
            monitor(instance);
            status = GlassFishStatus.get(instance);
        }
        if (status == null) {
            throw new IllegalStateException(NbBundle.getMessage(
                    GlassFishState.class,
                    "GlassFishState.getStatus.statusNull"));
        }
        return status;
    }

    /**
     * Check if GlassFish server is running in <code>DEFAULT</code> mode.
     * <p/>
     * Check may cause delay when server status was not monitored before
     * to give status monitoring time to settle down.
     * <p/>
     * @param instance GlassFish server instance.
     * @return Returns <code>true</code> when GlassFish server is online
     *         or <code>false</code> otherwise.
     */
    public static boolean isOnline(final GlassFishServer instance) {
        return getStatus(instance).getStatus() == GlassFishStatus.ONLINE;
    }

    /**
     * Check if GlassFish server is offline.
     * <p/>
     * Check may cause delay when server status was not monitored before
     * to give status monitoring time to settle down.
     * <p/>
     * @param instance GlassFish server instance.
     * @return Returns <code>true</code> when GlassFish server offline
     *         or <code>false</code> otherwise.
     */
    public static boolean isOffline(final GlassFishServer instance) {
        return getStatus(instance).getStatus() == GlassFishStatus.OFFLINE;
    }

    /**
     * Check if GlassFish server can be started;
     * <p/>
     * Server can be started only when 
     * <p/>
     * @param instance GlassFish server instance.
     * @return Value of <code>true</code> when GlassFish server can be started
     *         or <code>false</code> otherwise.
     */
    public static boolean canStart(final GlassFishServer instance) {
        GlassFishServerStatus status = getStatus(instance);
        switch(status.getStatus()) {
            case UNKNOWN: case ONLINE: case SHUTDOWN: case STARTUP:
                return false;
            default:
                return !ServerUtils.isDASRunning(instance);
        }

    }

}
