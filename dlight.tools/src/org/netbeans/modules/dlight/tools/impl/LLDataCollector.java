/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.tools.impl;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import org.netbeans.modules.dlight.api.execution.DLightTargetChangeEvent;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.tools.*;
import java.io.File;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.dlight.api.execution.AttachableTarget;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.DLightTarget.ExecutionEnvVariablesProvider;
import org.netbeans.modules.dlight.api.execution.ValidationListener;
import org.netbeans.modules.dlight.api.execution.ValidationStatus;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.extras.api.support.CollectorRunner;
import org.netbeans.modules.dlight.impl.SQLDataStorage;
import org.netbeans.modules.dlight.management.api.DLightManager;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.spi.collector.DataCollectorListener;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.dlight.spi.indicator.IndicatorNotificationsListener;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.AsynchronousAction;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.openide.util.NbBundle;

/**
 * @author Alexey Vladykin
 */
public class LLDataCollector
        extends IndicatorDataProvider<LLDataCollectorConfiguration>
        implements DataCollector<LLDataCollectorConfiguration>,
        ExecutionEnvVariablesProvider {

    private static final boolean ALLOW_ON_MACOSX = Boolean.getBoolean("cnd.tools.prof_agent.allow_on_macos"); // NOI18N

    private final Object lock = LLDataCollector.class.getName();
    private final EnumSet<LLDataCollectorConfiguration.CollectedData> collectedData;
    private final Set<ValidationListener> validationListeners;
    private final String name;
    private DLightTarget target;
    private ValidationStatus validationStatus;
    private CollectorRunner profRunner;
    private final List<DataCollectorListener> listeners = new ArrayList<DataCollectorListener>();

    public LLDataCollector(LLDataCollectorConfiguration configuration) {
        collectedData = EnumSet.of(LLDataCollectorConfigurationAccessor.getDefault().getCollectedData(configuration));
        name = LLDataCollectorConfigurationAccessor.getDefault().getName();
        validationStatus = ValidationStatus.initialStatus();
        validationListeners = Collections.synchronizedSet(new HashSet<ValidationListener>());
    }

/**
     * Adds collector state listener, all listeners will be notified about
     * collector state change.
     * @param listener add listener
     */
    @Override
    public final void addDataCollectorListener(DataCollectorListener listener) {
        if (listener == null) {
            return;
        }

        synchronized (this) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    /**
     * Remove collector listener
     * @param listener listener to remove from the list
     */
    @Override
    public final void removeDataCollectorListener(DataCollectorListener listener) {
        synchronized (this) {
            listeners.remove(listener);
        }
    }

    /**
     * Notifies listeners target state changed in separate thread
     * @param oldState state target was
     * @param newState state  target is
     */
    protected final void notifyListeners(final CollectorState state) {
        DataCollectorListener[] ll;

        synchronized (this) {
            ll = listeners.toArray(new DataCollectorListener[0]);
        }

        final CountDownLatch doneFlag = new CountDownLatch(ll.length);

        // Will do notification in parallel, but wait until all listeners
        // finish processing of event.
        for (final DataCollectorListener l : ll) {
            DLightExecutorService.submit(new Runnable() {

                @Override
                public void run() {
                    try {
                        l.collectorStateChanged(LLDataCollector.this, state);
                    } finally {
                        doneFlag.countDown();
                    }
                }
            }, "Notifying " + l); // NOI18N
        }

        try {
            doneFlag.await();
        } catch (InterruptedException ex) {
        }

    }

    public void addConfiguration(LLDataCollectorConfiguration configuration) {
        synchronized (lock) {
            collectedData.add(LLDataCollectorConfigurationAccessor.getDefault().getCollectedData(configuration));
        }
    }

    public String getName() {
        return name;
    }

    public List<DataTableMetadata> getDataTablesMetadata() {
        List<DataTableMetadata> tables = new ArrayList<DataTableMetadata>();
        synchronized (lock) {
            if (collectedData.contains(LLDataCollectorConfiguration.CollectedData.CPU)) {
                tables.add(LLDataCollectorConfiguration.CPU_TABLE);
            }
            if (collectedData.contains(LLDataCollectorConfiguration.CollectedData.MEM)) {
                tables.add(LLDataCollectorConfiguration.MEM_TABLE);
            }
            if (collectedData.contains(LLDataCollectorConfiguration.CollectedData.SYNC)) {
                tables.add(LLDataCollectorConfiguration.SYNC_TABLE);
            }
        }
        return tables;
    }

    public Collection<DataStorageType> getRequiredDataStorageTypes() {
        return Collections.singletonList(DataStorageTypeFactory.getInstance().getDataStorageType(SQLDataStorage.SQL_DATA_STORAGE_TYPE));
    }

    public void init(Map<DataStorageType, DataStorage> storages, DLightTarget target) {
        ExecutionEnvironment env = target.getExecEnv();
        if (!env.isLocal()) {
            for (Map.Entry<String, File> entry : locateProfAgents(env).entrySet()) {
                upload(env, entry.getValue(), getRemoteDir(env, entry.getValue(), entry.getKey()), 0644);
            }
            for (Map.Entry<String, File> entry : locateProfMonitors(env).entrySet()) {
                upload(env, entry.getValue(), getRemoteDir(env, entry.getValue(), entry.getKey()), 0755);
                break; // one monitor is enough
            }
        }

        if (!NativeToolsUtil.isMacOSX(env)) {
            // Hack!
            // We are collecting CPU usage with LL tool only on Mac OSX.
            // /proc serves this purpose on other platforms.
            collectedData.remove(LLDataCollectorConfiguration.CollectedData.CPU);
        }

        synchronized (lock) {
            this.target = target;
        }
    }

    private void upload(ExecutionEnvironment execEnv, File localFile, String remoteDir, int mode) {
        try {
            // TODO: eliminate mkDir in the case we don't need it
            CommonTasksSupport.mkDir(execEnv, remoteDir, null).get();
            CommonTasksSupport.uploadFile(localFile.getAbsolutePath(), execEnv,
                    remoteDir + "/" + localFile.getName(), mode, null, true).get(); // NOI18N
        } catch (InterruptedException ex) {
            DLightLogger.instance.log(Level.WARNING, null, ex);
        } catch (ExecutionException ex) {
            DLightLogger.instance.log(Level.WARNING, null, ex);
        }
    }

    public boolean isAttachable() {
        return true;
    }

    public String getCmd() {
        // should not be called
        return null;
    }

    public String[] getArgs() {
        // should not be called
        return null;
    }

    @Override
    public void setupEnvironment(DLightTarget target, MacroMap env) throws ConnectException {
        ExecutionEnvironment execEnv = target.getExecEnv();
        boolean isMac = NativeToolsUtil.isMacOSX(execEnv);
        Map<String, File> agentLibrariesLocal = locateProfAgents(execEnv);
        if (!agentLibrariesLocal.isEmpty()) {
            if (isMac) {
                Map.Entry<String, File> entry = agentLibrariesLocal.entrySet().iterator().next();
                env.appendPathVariable("DYLD_INSERT_LIBRARIES", getRemoteDir(execEnv, entry.getValue(), entry.getKey()) + '/' + entry.getValue().getName()); // NOI18N
                //env.put("DYLD_FORCE_FLAT_NAMESPACE", "yes"); // causes segfaults! // NOI18N
            } else {
                String agentFilename = null;
                for (Map.Entry<String, File> entry : agentLibrariesLocal.entrySet()) {
                    if (agentFilename == null) {
                        agentFilename = entry.getValue().getName();
                    }
                    env.appendPathVariable("LD_LIBRARY_PATH", getRemoteDir(execEnv, entry.getValue(), entry.getKey())); // NOI18N
                }
                env.appendPathVariable("LD_PRELOAD", agentFilename); // NOI18N
            }
        }
    }

    private String getRemoteDir(ExecutionEnvironment env, File localFile, String dirname) {
        if (env.isLocal()) {
            return localFile.getParentFile().getAbsolutePath();
        } else {
            String tmpDir;
            try {
                tmpDir = HostInfoUtils.getHostInfo(env).getTempDir();
            } catch (Throwable ex) {
                tmpDir = "/var/tmp"; // NOI18N
            }
            return tmpDir + "/tools/" + dirname; // NOI18N
        }
    }

    private Map<String, File> locateProfAgents(ExecutionEnvironment env) {
        return NativeToolsUtil.getCompatibleBinaries(env, "prof_agent.${soext}"); // NOI18N
    }

    private Map<String, File> locateProfMonitors(ExecutionEnvironment env) {
        return NativeToolsUtil.getCompatibleBinaries(env, "prof_monitor"); // NOI18N
    }

    public void targetStateChanged(DLightTargetChangeEvent event) {
        switch (event.state) {
            case RUNNING:
                startMonitor();
                break;
            case DONE:
            case FAILED:
            case STOPPED:
            case TERMINATED:
                stopMonitor();
                break;
        }
    }

    private void startMonitor() {
        final AttachableTarget at;
        final ExecutionEnvironment env;
        final EnumSet<LLDataCollectorConfiguration.CollectedData> cdata;

        synchronized (lock) {
            at = (AttachableTarget) target;
            env = target.getExecEnv();
            cdata = collectedData.clone();
        }

        NativeProcessBuilder npb = null;

        for (Map.Entry<String, File> entry : locateProfMonitors(env).entrySet()) {
            npb = NativeProcessBuilder.newProcessBuilder(env);
            npb.setExecutable(getRemoteDir(env, entry.getValue(), entry.getKey()) + "/" + entry.getValue().getName()); // NOI18N
            break;
        }

        if (npb == null) {
            DLightLogger.instance.severe("Failed to find prof_monitor"); // NOI18N
            return;
        }

        StringBuilder flags = new StringBuilder("-"); // NOI18N

        if (cdata.contains(LLDataCollectorConfiguration.CollectedData.CPU)) {
            flags.append('c'); // NOI18N
        }

        if (cdata.contains(LLDataCollectorConfiguration.CollectedData.MEM)) {
            flags.append('m'); // NOI18N
        }

        if (cdata.contains(LLDataCollectorConfiguration.CollectedData.SYNC)) {
            flags.append('s'); // NOI18N
        }

        npb = npb.setArguments(flags.toString(), String.valueOf(at.getPID()));

        CollectorRunner newProfRunner = new CollectorRunner(new FakeIndicatorNotificationListener(), npb, new MonitorOutputProcessor(), "__EOF__", "prof_monitor"); // NOI18N

        synchronized (lock) {
            if (profRunner != null) {
                profRunner.shutdown();
            }

            profRunner = newProfRunner;
        }
    }

    private void stopMonitor() {
        final CollectorRunner collectorToStop;

        synchronized (lock) {
            collectorToStop = profRunner;
            profRunner = null;
        }

        if (collectorToStop != null) {
            collectorToStop.shutdown();
        }
    }

    @Override
    public void dataFiltersChanged(List<DataFilter> newSet, boolean isAdjusting) {
    }

    private class FakeIndicatorNotificationListener implements IndicatorNotificationsListener {
        @Override
        public void reset() {
            resetIndicators();
        }

        @Override
        public void suggestRepaint() {
            suggestIndicatorsRepaint();
        }

        @Override
        public void updated(List<DataRow> data) {
            notifyIndicators(data);
        }
    }

    private class MonitorOutputProcessor implements LineProcessor {

        private float syncPrev = Float.NaN;

        @Override
        public void processLine(String line) {
            DataRow row = null;
            if (line.startsWith("cpu:")) { // NOI18N
                String[] times = line.substring(5).split("\t"); // NOI18N
                row = new DataRow(LLDataCollectorConfiguration.CPU_TABLE.getColumnNames(), Arrays.asList(Float.valueOf(times[0]), Float.valueOf(times[1])));
            } else if (line.startsWith("mem:")) { // NOI18N
                row = new DataRow(LLDataCollectorConfiguration.MEM_TABLE.getColumnNames(), Arrays.asList(line.substring(5)));
            } else if (line.startsWith("sync:")) { // NOI18N
                String[] fields = line.substring(6).split("\t"); // NOI18N
                float syncCurr = Float.parseFloat(fields[0]);
                if (!Float.isNaN(syncPrev)) {
                    int threads = Integer.parseInt(fields[1]);
                    row = new DataRow(LLDataCollectorConfiguration.SYNC_TABLE.getColumnNames(), Arrays.asList(Float.valueOf((syncCurr - syncPrev) * 100 / threads), Integer.valueOf(threads)));
                }
                syncPrev = syncCurr;
            }
            if (row != null) {
                notifyIndicators(Collections.singletonList(row));
                suggestIndicatorsRepaint();
            }
        }

        @Override
        public void reset() {
        }

        @Override
        public void close() {
        }
    }

// validation stuff ////////////////////////////////////////////////////////////
    public ValidationStatus validate(final DLightTarget objectToValidate) {
        synchronized (lock) {
            if (validationStatus.isValid()) {
                return validationStatus;
            }

            ValidationStatus oldStatus = validationStatus;
            ValidationStatus newStatus = doValidation(objectToValidate);

            notifyStatusChanged(oldStatus, newStatus);

            validationStatus = newStatus;
            return newStatus;
        }
    }

    public void invalidate() {
        synchronized (lock) {
            validationStatus = ValidationStatus.initialStatus();
        }
    }

    public ValidationStatus getValidationStatus() {
        synchronized (lock) {
            return validationStatus;
        }
    }

    private ValidationStatus doValidation(final DLightTarget target) {
        DLightLogger.assertNonUiThread();

        ExecutionEnvironment env = target.getExecEnv();

        if (!ConnectionManager.getInstance().isConnectedTo(env)) {
            AsynchronousAction connectAction = ConnectionManager.getInstance().getConnectToAction(env, new Runnable() {

                public void run() {
                    DLightManager.getDefault().revalidateSessions();
                }
            });
            return ValidationStatus.unknownStatus(
                    getMessage("ValidationStatus.HostNotConnected"), // NOI18N
                    connectAction);
        }

        OSFamily osFamily = OSFamily.UNKNOWN;

        try {
            osFamily = HostInfoUtils.getHostInfo(env).getOSFamily();
        } catch (IOException ex) {
        } catch (CancellationException ex) {
        }

        if (osFamily != OSFamily.LINUX && !(ALLOW_ON_MACOSX && osFamily == OSFamily.MACOSX)) {
            return ValidationStatus.invalidStatus(getMessage("ValidationStatus.ProfAgent.OSNotSupported")); // NOI18N
        }

        Map<String, File> profAgentsLocal = locateProfAgents(env);
        if (profAgentsLocal.isEmpty()) {
            return ValidationStatus.invalidStatus(getMessage("ValidationStatus.AgentNotFound")); // NOI18N
        }

        Map<String, File> profMonitorsLocal = locateProfMonitors(env);
        if (profMonitorsLocal.isEmpty()) {
            return ValidationStatus.invalidStatus(getMessage("ValidationStatus.MonitorNotFound")); // NOI18N
        }

        return ValidationStatus.validStatus();
    }

    private void notifyStatusChanged(ValidationStatus oldStatus, ValidationStatus newStatus) {
        if (!oldStatus.equals(newStatus)) {
            for (ValidationListener vl : validationListeners.toArray(new ValidationListener[0])) {
                vl.validationStateChanged(this, oldStatus, newStatus);
            }
        }
    }

    public void addValidationListener(ValidationListener listener) {
        validationListeners.add(listener);
    }

    public void removeValidationListener(ValidationListener listener) {
        validationListeners.remove(listener);
    }

    private static String getMessage(String key) {
        return NbBundle.getMessage(LLDataCollector.class, key);
    }
}
