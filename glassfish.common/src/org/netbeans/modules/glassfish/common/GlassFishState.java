/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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

import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.tools.ide.GlassFishStatus;
import org.glassfish.tools.ide.admin.ResultMap;
import org.glassfish.tools.ide.admin.ResultString;
import org.glassfish.tools.ide.admin.TaskState;
import org.glassfish.tools.ide.data.GlassFishServerStatus;
import org.glassfish.tools.ide.data.GlassFishVersion;
import org.glassfish.tools.ide.server.ServerStatus;
import org.glassfish.tools.ide.utils.ServerUtils;
import org.netbeans.modules.glassfish.common.ui.WarnPanel;
import org.openide.filesystems.FileUtil;
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
        // Class attributes                                                   //
        ////////////////////////////////////////////////////////////////////////

        ///** Local logger. */
        //private static final Logger LOGGER = GlassFishLogger.get(Mode.class);

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

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER
            = GlassFishLogger.get(GlassFishState.class);

    /** Keep trying for up to 10 minutes while server is initializing [ms]. */
    private static final int STARTUP_TIMEOUT = 600000;

    /** Delay before next try while server is initializing [ms]. */
    private static final int RETRY_DELAY = 2000;

    ////////////////////////////////////////////////////////////////////////////
    // Class log messages                                                     //
    ////////////////////////////////////////////////////////////////////////////

    /** Log message: Check retry. */
    private static final String LOG_RETRY
            = "Keep trying while server {0} is not yet ready. Retry {1}"
            + " and time remaining: {2} ms";

    /** Log message: Thread interrupted. */
    private static final String LOG_THREAD_INTERRUPTED
            = "Thread sleep interrupted while checking {0}: {1}";

    /** Log message: Locations response. */
    private static final String LOG_LOCATIONS_RESPONSE
            = "Server {0} locations response returned {1} records";

    /** Log message: Locations response item. */
    private static final String LOG_LOCATIONS_RESPONSE_ITEM
            = "Server {0} locations response {1} = {2}";

    /** Log message: Version task failed. */
    private static final String LOG_VERSION_TASK_FAIL
            = "Version task failed: {0}";

    /** Log message: Version response. */
    private static final String LOG_VERSION_RESPONSE
            = "Server {0} version response: {1}";

    /** Log message: Server is still starting up. */
    private static final String LOG_SERVER_STARTUP
            = "Server {0} is still starting up";

    /** Log message: Local locations response check against instance object. */
    private static final String LOG_LOCAL_LOCATIONS_CHECK
            = "Checked local instance {0} domain root {1} {2} value {3}"
            + " from locations command";

    /** Log message: Remote locations response check against instance object. */
    private static final String LOG_REMOTE_LOCATIONS_CHECK
            = "Checked remote instance {0} domain root value {1} from"
            + " locations command, which is {2}";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Log <code>__locations</code> command response.
     * <p/>
     * Internal {@link #isReady(GlassfishInstance, boolean, boolean)}
     * helper.
     * <p/>
     * @param instance GlassFish server instance.
     * @param result   Location command asynchronous execution final result. 
     */
    private static void logLocationsResponse(final GlassfishInstance instance,
            final ResultMap<String, String> result) {
        Map<String, String> values = result != null ? result.getValue() : null;
        LOGGER.log(Level.FINEST, LOG_LOCATIONS_RESPONSE,
                new Object[] {instance.getName(),
                    values != null ? values.size() : 0});
        if (values != null) {
            for (String key : values.keySet()) {
                String value = values.get(key);
                LOGGER.log(Level.FINEST,
                        LOG_LOCATIONS_RESPONSE_ITEM, new Object[] {
                        instance.getName(), key, value});
            }
        }
    }

    /**
     * Verify GlassFish server installation and domain directories and update
     * HTTP port.
     * <p/>
     * Internal {@link #isReady(GlassfishInstance, boolean, boolean)}
     * helper.
     * <p/>
     * @param instance GlassFish server instance.
     * @param result   Location command asynchronous execution final result. 
     * @return Returns <code>true</code> when server is ready or
     *         <code>false</code> otherwise.
     */
    private static boolean processReadyLocationsResult(
            final GlassfishInstance instance,
            final ResultMap<String, String> result) {
        boolean isReady;
        String domainRoot = instance.getDomainsRoot()
                + File.separator + instance.getDomainName();
        String targetDomainRoot = result.getValue().get("Domain-Root_value");
        // Local instance. We can check domains folder.
        if (instance.getDomainsRoot() != null
                && targetDomainRoot != null) {
            File installDir = FileUtil.normalizeFile(new File(domainRoot));
            File targetInstallDir = FileUtil.normalizeFile(
                    new File(targetDomainRoot));
            isReady = installDir.equals(targetInstallDir);
             LOGGER.log(Level.FINEST,
                     LOG_LOCAL_LOCATIONS_CHECK, new Object[] {
                     instance.getName(), domainRoot,
                     isReady ? "matches" : "not matches" ,targetDomainRoot});
        // Remote instance. We don't know domains folder. We'll just trust it.
        } else {
            isReady = null != targetDomainRoot;
            LOGGER.log(Level.FINEST,
                    LOG_REMOTE_LOCATIONS_CHECK, new Object[] {
                    instance.getName(), targetDomainRoot,
                    isReady ? "correct" : "not correct"});
        }
        if (isReady) {
            // Make sure the http port info is corrected
            instance.getCommonSupport().updateHttpPort();
        }
        return isReady;
    }

    /**
     * Check <code>version</code> task execution result and retrieve version
     * command response.
     * <p/>
     * Internal {@link #isReady(GlassfishInstance, boolean, boolean)}
     * helper.
     * <p/>
     * @param instance          GlassFish server instance.
     * @param versionTaskResult Task execution result.
     * @param mode              Check mode.
     * @return Version command response.
     */
    private static ResultString processVersionTaskResult(
            final GlassfishInstance instance,
            final ServerStatus.ResultVersion versionTaskResult,
            final Mode mode) {
        ResultString versionCommandResult;
        switch (versionTaskResult.getStatus()) {
            case SUCCESS:
                versionCommandResult = versionTaskResult.getResult();
                break;
            // No break here, default: does the rest.
            case TIMEOUT:
                if (mode == Mode.REFRESH && instance.isRemote()) {
                    String message = NbBundle.getMessage(
                            CommonServerSupport.class, "MSG_COMMAND_SSL_ERROR",
                            "version", instance.getName(),
                            Integer.toString(instance.getAdminPort()));
                    CommonServerSupport.displayPopUpMessage(
                            instance.getCommonSupport(), message);
                }
            default:
                 LOGGER.log(Level.INFO,
                         LOG_VERSION_TASK_FAIL, versionTaskResult.getStatus());
                versionCommandResult = null;
        }
        return versionCommandResult;
    }

    /**
     * Check content of <code>version</code> command response and display
     * warning for GlassFish 3.1.2 which is known to have bug in WS.
     * <p/>
     * Internal {@link #isReady(GlassfishInstance, boolean, boolean)}
     * helper.
     * <p/>
     * @param instance GlassFish server instance.
     * @param state   Server state checker containing check results.
     * @param mode              Check mode.
     */
    private static void handleGlassFishWarnings(final GlassfishInstance instance,
            final ServerStatus status, final Mode mode) {
        GlassFishVersion version = status.getVersion();
        // Remote GlassFish 3.1.2 won't crash NetBeans.
        if (mode == Mode.STARTUP && version == GlassFishVersion.GF_3_1_2
                && !instance.isRemote()) {
            WarnPanel.gf312WSWarning(instance.getName());
        }
    }

    /**
     * Suspend thread execution for {@link #RETRY_DELAY} ms.
     * <p/>
     * Internal {@link #isReady(GlassfishInstance, boolean, boolean)}
     * helper.
     * <p/>
     * @param instance GlassFish server instance (for logging purposes).
     * @param begTm {@link #isReady(GlassfishInstance, boolean, boolean)}
     *              execution start time (for logging purposes).
     * @param actTm Actual time (for logging purposes).
     * @param tries Number of retries (for logging purposes).
     */
    private static void retrySleep(final GlassfishInstance instance,
            final long begTm, final long actTm, final int tries) {
        LOGGER.log(Level.FINEST, LOG_RETRY, new Object[]{
                    instance.getName(), Integer.toString(tries),
                    Long.toString(STARTUP_TIMEOUT - actTm + begTm)});
        try {
            Thread.sleep(RETRY_DELAY);
        } catch (InterruptedException ie) {
            LOGGER.log(Level.INFO,
                    LOG_THREAD_INTERRUPTED, new Object[]{
                    instance.getName(), ie.getLocalizedMessage()});
        }
    }

    /**
     * Check if GlassFish server is ready checking it's administration port
     * and running <code>version</code> and <code>__locations</code>
     * administration commands.
     * <p/>
     * Will not display any warning pop up messages.
     * <p/>
     * @param instance GlassFish server instance.
     * @param retry    Allow up to 3 retries.
     * @return Returns <code>true</code> when GlassFish server is ready
     *         or <code>false</code> otherwise.
     */
    public static boolean isReady(final GlassfishInstance instance,
            final boolean retry) {
        return isReady(instance, retry, Mode.DEFAULT);
    }
    
    /**
     * Check if GlassFish server is running.
     * <p/>
     * @param instance GlassFish server instance.
     * @param mode     Server state check mode.
     * @return Returns <code>true</code> when GlassFish server is ready
     *         or <code>false</code> otherwise.
     */
//    public static boolean isReady2(
//            final GlassfishInstance instance, final Mode mode) {
//        GlassFishServerStatus status = GlassFishStatus.get(instance);
//        if (status == null) {
//            GlassFishStatus.add(instance);
//        }
//    }

    /**
     * Check if GlassFish server is ready checking it's administration port
     * and running <code>version</code> and <code>__locations</code>
     * administration commands.
     * <p/>
     * @param instance GlassFish server instance.
     * @param retry    Allow up to 3 retries.
     * @param startup  Trigger startup mode. Triggers longer administration
     *                 commands execution timeouts when <code>true</code>.
     * @param warnings Display warnings pop up messages.
     * @return Returns <code>true</code> when GlassFish server is ready
     *         or <code>false</code> otherwise.
     */
    public static boolean isReady(final GlassfishInstance instance,
            final boolean retry, final Mode mode) {
        boolean isReady = false;
        int maxTries = retry ? 3 : 1;
        int tries = 0;
        boolean notYetReady = false;
        long begTm = System.currentTimeMillis(), actTm = begTm;
        ServerStatus status = new ServerStatus(instance, mode == Mode.STARTUP);
        try {
            while (!isReady && (tries++ < maxTries || (notYetReady
                    && (actTm = System.currentTimeMillis()) - begTm
                    < STARTUP_TIMEOUT))) {
                if (tries > 1) {
                    retrySleep(instance, begTm, actTm, tries);
                }
                status.check();
                ServerStatus.Result adminPortResult
                        = status.getAdminPortResult();
                // GlassFish server administration port is not listening.
                if (adminPortResult.getStatus()
                        != ServerStatus.Status.SUCCESS) {
                    continue;
                }
                ResultString versionCommandResult = processVersionTaskResult(
                        instance, status.getVersionResult(), mode);
                // Version command result.
                if (versionCommandResult != null) {
                    String value = versionCommandResult.getValue();
                    LOGGER.log(Level.FINEST,
                            LOG_VERSION_RESPONSE, new Object[]{
                                instance.getName(), value});
                    switch (versionCommandResult.getState()) {
                        case FAILED:
                            if (notYetReady
                                    = ServerUtils.notYetReadyMsg(value)) {
                                LOGGER.log(Level.FINEST,
                                        LOG_SERVER_STARTUP, instance.getName());
                                continue;
                            } else {
                                break;
                            }
                        case COMPLETED:
                            isReady = true;
                            handleGlassFishWarnings(instance, status, mode);
                            break;
                    }
                }
                // Locations task execution result.
                ServerStatus.ResultLocations locationsTaskResult
                        = status.getLocationsResult();
                if (locationsTaskResult.getStatus()
                        == ServerStatus.Status.SUCCESS) {
                    ResultMap<String, String> locationsCommandResult
                            = locationsTaskResult.getResult();
                    logLocationsResponse(instance, locationsCommandResult);
                    if (locationsCommandResult.getState()
                            == TaskState.COMPLETED) {
                        isReady = processReadyLocationsResult(
                                instance, locationsCommandResult);
                    }
                }
            }
        } finally {
            status.close();
        }
        return isReady;
    }

}
