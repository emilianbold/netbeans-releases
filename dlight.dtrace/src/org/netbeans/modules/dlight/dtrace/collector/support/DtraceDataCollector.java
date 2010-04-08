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
package org.netbeans.modules.dlight.dtrace.collector.support;

import org.netbeans.modules.dlight.dtrace.collector.DtraceParser;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.acl.NotOwnerException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.dlight.api.execution.AttachableTarget;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.DLightTargetChangeEvent;
import org.netbeans.modules.dlight.api.execution.Validateable;
import org.netbeans.modules.dlight.api.execution.ValidationStatus;
import org.netbeans.modules.dlight.api.execution.ValidationListener;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.modules.dlight.dtrace.collector.impl.DTDCConfigurationAccessor;
import org.netbeans.modules.dlight.management.api.DLightManager;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.core.stack.storage.StackDataStorage;
import org.netbeans.modules.dlight.extras.api.support.CollectorRunner;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.impl.SQLDataStorage;
import org.netbeans.modules.dlight.spi.collector.DataCollectorListener;
import org.netbeans.modules.dlight.spi.indicator.IndicatorNotificationsListener;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.dlight.util.Util;
import org.netbeans.modules.dlight.util.usagetracking.SunStudioUserCounter;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.AsynchronousAction;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.SolarisPrivilegesSupport;
import org.netbeans.modules.nativeexecution.api.util.SolarisPrivilegesSupportProvider;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Collector which collects data using DTrace scripts.
 * You should describe data collected using list of
 * {@link org.netbeans.modules.dlight.core.storage.model.DataTableMetadata}.
 * You can define your own implementation of
 * {@link org.netbeans.modules.dlight.dtrace.collector.DtraceParser}
 */
public final class DtraceDataCollector
        extends IndicatorDataProvider<DTDCConfiguration>
        implements DataCollector<DTDCConfiguration> {

    private static final List<String> ultimateDTracePrivilegesList =
            Arrays.asList(
            new String[]{
                DTDCConfiguration.DTRACE_KERNEL,
                DTDCConfiguration.DTRACE_PROC,
                DTDCConfiguration.DTRACE_USER
            });
    private static final String cmd_dtrace = "/usr/sbin/dtrace"; // NOI18N
    private static final Logger log =
            DLightLogger.getLogger(DtraceDataCollector.class);
    private Set<String> requiredPrivilegesSet;
    private DataTableMetadata tableMetaData = null;
    private URL localScriptUrl;
    private String extraArgs;
    private String scriptPath;
    private CollectorRunner dtraceRunner = null;
    private DTDCConfiguration configuration;
    private ValidationStatus validationStatus =
            ValidationStatus.initialStatus();
    private List<ValidationListener> validationListeners =
            Collections.synchronizedList(new ArrayList<ValidationListener>());
    private String command;
    private String argsTemplate;
    private DataStorage storage;
    private List<DataTableMetadata> dataTablesMetadata;
    private DtraceParser parser;
    private final List<DataRow> indicatorDataBuffer = new ArrayList<DataRow>();
    private int indicatorFiringFactor;
    private LineProcessor outputProcessor = new DefaultLineProcessor();
    private boolean isSlave;
    private final boolean multiScriptMode;
    private DtraceDataCollector parentCollector;
    private final Map<String, DtraceDataCollector> slaveCollectors;
    private DtraceDataCollector lastSlaveCollector;

    private final List<DataCollectorListener> listeners = new ArrayList<DataCollectorListener>();

    DtraceDataCollector(boolean multiScriptMode, DTDCConfiguration configuration) {
        this.multiScriptMode = multiScriptMode;

        this.command = cmd_dtrace;
        this.argsTemplate = null;

        final DTDCConfigurationAccessor cfgInfo =
                DTDCConfigurationAccessor.getDefault();

        if (multiScriptMode) {
            this.dataTablesMetadata = new ArrayList<DataTableMetadata>();
            this.tableMetaData = null;
            this.slaveCollectors = new HashMap<String, DtraceDataCollector>();
            this.requiredPrivilegesSet = new HashSet<String>();
            setOutputProcessor(new DemultiplexingLineProcessor());
            addSlaveConfiguration(configuration);
        } else {
            this.dataTablesMetadata = cfgInfo.getDatatableMetadata(configuration);
            this.slaveCollectors = Collections.emptyMap();

            this.tableMetaData =
                    (dataTablesMetadata != null && dataTablesMetadata.size() > 0)
                    ? dataTablesMetadata.get(0)
                    : null;

            if (cfgInfo.isStackSupportEnabled(configuration)) {
                this.parser = new DtraceDataAndStackParser(tableMetaData);
            } else {
                this.parser = cfgInfo.getParser(configuration) == null
                        ? (tableMetaData != null ? new DtraceParser(tableMetaData)
                        : (DtraceParser) null) : cfgInfo.getParser(configuration);
            }

            // super(cmd_dtrace, null,
            //            configuration.getParser() == null
            //        ? (configuration.getDatatableMetadata() != null &&
            //        configuration.getDatatableMetadata().size() > 0
            //        ? new DtraceParser(configuration.getDatatableMetadata().get(0))
            //        : (DtraceParser) null)
            //        : configuration.getParser(), configuration.getDatatableMetadata());


            this.localScriptUrl = cfgInfo.getScriptUrl(configuration);
            this.extraArgs = cfgInfo.getArgs(configuration);

            this.requiredPrivilegesSet = new HashSet<String>(
                    cfgInfo.getRequiredPrivileges(configuration) == null
                    ? ultimateDTracePrivilegesList
                    : cfgInfo.getRequiredPrivileges(configuration));

            this.configuration = configuration;

            this.indicatorFiringFactor =
                    cfgInfo.getIndicatorFiringFactor(configuration);
        }
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
                        l.collectorStateChanged(DtraceDataCollector.this, state);
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

    /*package*/ void addSlaveConfiguration(DTDCConfiguration configuration) {
        if (!multiScriptMode) {
            throw new IllegalStateException("addSlaveConfiguration called in single-script mode"); // NOI18N
        }
        for (DtraceDataCollector dc : slaveCollectors.values()) {
            if (dc.configuration == configuration) {
                // do not add duplicate configurations
                return;
            }
        }
        DTDCConfigurationAccessor accessor = DTDCConfigurationAccessor.getDefault();
        DtraceDataCollector slaveCollector = new DtraceDataCollector(false, configuration);
        slaveCollector.setSlave(true);
        slaveCollector.setParentCollector(this);
        slaveCollectors.put(accessor.getOutputPrefix(configuration), slaveCollector);
        requiredPrivilegesSet.addAll(accessor.getRequiredPrivileges(configuration) == null ? ultimateDTracePrivilegesList : accessor.getRequiredPrivileges(configuration));
    }

    void setParentCollector(DtraceDataCollector parentCollector) {
        this.parentCollector = parentCollector;
    }

    public String getName() {
        return "DTrace";//NOI18N
    }

    void setOutputProcessor(LineProcessor callback) {
        this.outputProcessor = callback;
    }

    void setSlave(boolean isSlave) {
        this.isSlave = isSlave;
    }

    URL getLocalScriptUrl() {
        return localScriptUrl;
    }

    void setLocalScriptUrl(URL path) {
        localScriptUrl = path;
    }

    LineProcessor getOutputProcessor() {
        return outputProcessor;
    }

    protected DataStorage getStorage() {
        return storage;
    }

    /**
     * The types of storage this collector supports
     * @return returns list of
     * {@link org.netbeans.modules.dlight.core.storage.model.DataStorageType}
     * data collector can put data into
     */
    public Collection<DataStorageType> getRequiredDataStorageTypes() {
        DataStorageTypeFactory dstf = DataStorageTypeFactory.getInstance();
        DataStorageType sqlStorageType =
                dstf.getDataStorageType(SQLDataStorage.SQL_DATA_STORAGE_TYPE);
        DataStorageType stackStorageType =
                dstf.getDataStorageType(StackDataStorage.STACK_DATA_STORAGE_TYPE_ID);
        return Arrays.asList(sqlStorageType, stackStorageType);
    }

    public boolean isAttachable() {
        return true;
    }

    @Override
    public void init(Map<DataStorageType, DataStorage> storages, DLightTarget target) {

        if (multiScriptMode) {
            for (DtraceDataCollector ddc : slaveCollectors.values()) {
                ddc.init(storages, target);
            }
            try {
                setLocalScriptUrl(mergeScripts().toURI().toURL());
            } catch (MalformedURLException ex) {
            }
        }

        DataStorageTypeFactory dstf = DataStorageTypeFactory.getInstance();
        this.storage = storages.get(dstf.getDataStorageType(SQLDataStorage.SQL_DATA_STORAGE_TYPE));
        StackDataStorage stackStorage = (StackDataStorage) storages.get(dstf.getDataStorageType(StackDataStorage.STACK_DATA_STORAGE_TYPE_ID));
        if (this.parser instanceof DtraceDataAndStackParser) {
            ((DtraceDataAndStackParser) this.parser).setStackDataStorage(stackStorage);
        }

        if (isSlave) {
            return;
        }

        File scriptFile;
        try {
            scriptFile = Util.copyToTempDir(localScriptUrl);
            DTraceScriptUtils.insertEOFMarker(scriptFile);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            scriptPath = null;
            return;
        }

        ExecutionEnvironment execEnv = target.getExecEnv();

        if (execEnv.isLocal()) {
            // No need to copy file on localhost -
            // just ensure execution permissions...
            scriptPath = scriptFile.getAbsolutePath();
            Util.setExecutionPermissions(Arrays.asList(scriptPath));
        } else {
            String briefName = Util.getBriefName(localScriptUrl);
            try {
                HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv);
                scriptPath = hostInfo.getTempDir() + "/" + briefName; // NOI18N
                Future<Integer> copyResult = CommonTasksSupport.uploadFile(
                        scriptFile.getAbsolutePath(), execEnv, scriptPath, 0777, null, true);
                copyResult.get(); // TODO: error processing!!!
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (CancellationException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public String getCmd() {
        return command;
    }

    public String[] getArgs() {
        return null;
    }

    public List<DataTableMetadata> getDataTablesMetadata() {
        List<DataTableMetadata> ret = new ArrayList<DataTableMetadata>();
        ret.addAll(dataTablesMetadata);
        for (DtraceDataCollector ddc : slaveCollectors.values()) {
            ret.addAll(ddc.getDataTablesMetadata());
        }
        return ret;
    }

    /**
     * override this if you need to add extra parameters to the DTrace script
     */
    protected String getCollectorTaskExtraParams() {
        return extraArgs;
    }

    /*package*/ void packageVisibleSuggestIndicatorsRepaint() {
        super.suggestIndicatorsRepaint();
    }

    private void targetFinished(DLightTarget target) {
        if (!isSlave) {
            if (dtraceRunner != null) {
                // It could be already done here, because tracked process is
                // finished and, depending on command-line utility, it may exit
                // as well... But, if not - terminate it.
                log.fine("Stopping DtraceDataCollector: " + // NOI18N
                        dtraceRunner.toString());

                dtraceRunner.shutdown();
            }
        }

        synchronized (indicatorDataBuffer) {
            if (!indicatorDataBuffer.isEmpty()) {
                if (isSlave) {
                    if (parentCollector != null) {
                        parentCollector.notifyIndicators(indicatorDataBuffer);
                    }
                } else {
                    notifyIndicators(indicatorDataBuffer);
                }
                indicatorDataBuffer.clear();
            }
        }
    }

    private static String loc(String key, String... param) {
        return NbBundle.getMessage(DtraceDataCollector.class, key, param);
    }

    private ValidationStatus doValidation(final DLightTarget target) {
        DLightLogger.assertNonUiThread();

        final ExecutionEnvironment execEnv = target.getExecEnv();
        final ConnectionManager mgr = ConnectionManager.getInstance();

        boolean isConnected = mgr.isConnectedTo(execEnv);

        if (!isConnected) {
            Runnable doOnConnect = new Runnable() {

                public void run() {
                    DLightManager.getDefault().revalidateSessions();
                }
            };

            AsynchronousAction connectAction = mgr.getConnectToAction(execEnv, doOnConnect);

            return ValidationStatus.unknownStatus(loc("ValidationStatus.HostNotConnected"), // NOI18N
                    connectAction);
        }

        try {
            HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv);

            if (hostInfo == null || hostInfo.getOSFamily() != HostInfo.OSFamily.SUNOS) {
                return ValidationStatus.invalidStatus(
                        NbBundle.getMessage(DtraceDataCollector.class,
                        "DtraceDataCollector.DtraceIsSupportedOnSunOSOnly")); // NOI18N
            }

            if (!HostInfoUtils.fileExists(execEnv, command)) {
                return ValidationStatus.invalidStatus(
                        loc("ValidationStatus.CommandNotFound", // NOI18N
                        command));
            }
        } catch (Exception ex) {
            return ValidationStatus.invalidStatus(
                    loc("ValidationStatus.ErrorWhileValidation", // NOI18N
                    ex.getMessage()));
        }

        ProcessUtils.ExitStatus zonenameResult = ProcessUtils.execute(execEnv, "/sbin/zonename"); // NOI18N

        if (!zonenameResult.isOK() || !"global".equals(zonenameResult.output)) { // NOI18N
            return ValidationStatus.invalidStatus(
                    loc("ValidationStatus.NotGlobalZone", // NOI18N
                    command));
        }

        // /usr/sbin/dtrace exists...
        // check for permissions ...
        SolarisPrivilegesSupport sps = SolarisPrivilegesSupportProvider.getSupportFor(execEnv);

        if (sps == null) {
            return ValidationStatus.invalidStatus(
                    NbBundle.getMessage(DtraceDataCollector.class,
                    "DtraceDataCollector.NoPrivSupport", execEnv.toString())); // NOI18N
        }

        boolean status = sps.hasPrivileges(requiredPrivilegesSet);

        if (!status) {
            try {
                sps.requestPrivileges(requiredPrivilegesSet, false);
                status = true;
            } catch (NotOwnerException ex) {
            }
        }

        if (!status) {
            Runnable onPrivilegesGranted = new Runnable() {

                public void run() {
                    DLightManager.getDefault().revalidateSessions();
                }
            };

            AsynchronousAction requestPrivilegesAction = sps.getRequestPrivilegesAction(
                    requiredPrivilegesSet, onPrivilegesGranted);

            return ValidationStatus.unknownStatus(
                    loc("DTraceDataCollector_Status_NotEnoughPrivileges"), // NOI18N
                    requestPrivilegesAction);
        }

        return ValidationStatus.validStatus();
    }

    public ValidationStatus validate(final DLightTarget target) {
        ValidationStatus status = validate(target, this, true);
        if (status.isValid()) {
            SunStudioUserCounter.countDLight(target.getExecEnv());
        }
        return status;
    }

    ValidationStatus validate(final DLightTarget target, Validateable<DLightTarget> validatebleSource, boolean notify) {
        if (validationStatus.isValid()) {
            return validationStatus;
        }

        ValidationStatus oldStatus = validationStatus;
        ValidationStatus newStatus = doValidation(target);
        if (notify) {
            notifyStatusChanged(validatebleSource, oldStatus, newStatus);
        }
        validationStatus = newStatus;
        return newStatus;
    }

    public void invalidate() {
        validationStatus = ValidationStatus.initialStatus();
    }

    public ValidationStatus getValidationStatus() {
        return validationStatus;
    }

    private void targetStarted(DLightTarget target) {
        if (isSlave || scriptPath == null) {
            return;
        }

        String taskCommand = scriptPath; //"pfexec " + scriptPath;

        if (target instanceof AttachableTarget) {
            AttachableTarget at = (AttachableTarget) target;
            taskCommand += " " + Integer.toString(at.getPID()); // NOI18N
        }

        final String extraParams = getCollectorTaskExtraParams();

        if (extraParams != null) {
            taskCommand += " " + extraParams; // NOI18N
        }

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(target.getExecEnv());
        npb.setCommandLine(taskCommand);

        this.dtraceRunner = new CollectorRunner(new FakeIndicatorNotificationListener(), npb, getOutputProcessor(), DTraceScriptUtils.EOF_MARKER, "DTrace"); // NOI18N

        log.fine("DtraceDataCollector (" + dtraceRunner.toString() + // NOI18N
                ") for " + taskCommand + " STARTED"); // NOI18N
    }

    public void addValidationListener(ValidationListener listener) {
        if (!validationListeners.contains(listener)) {
            validationListeners.add(listener);
        }
    }

    public void removeValidationListener(ValidationListener listener) {
        validationListeners.remove(listener);
    }

    void notifyStatusChanged(Validateable<DLightTarget> validatable,
            ValidationStatus oldStatus, ValidationStatus newStatus) {
        if (oldStatus.equals(newStatus)) {
            return;
        }

        ValidationListener[] ll =
                validationListeners.toArray(new ValidationListener[0]);

        if (validatable == null) {
            validatable = this;
        }
        for (ValidationListener l : ll) {
            l.validationStateChanged(validatable, oldStatus, newStatus);
        }
    }

    protected void notifyStatusChanged(
            ValidationStatus oldStatus, ValidationStatus newStatus) {
        notifyStatusChanged(this, oldStatus, newStatus);
    }

    public void targetStateChanged(DLightTargetChangeEvent event) {
        switch (event.state) {
            case RUNNING:
                targetStarted(event.target);
                break;
            case FAILED:
                targetFinished(event.target);
                break;
            case TERMINATED:
                targetFinished(event.target);
                break;
            case DONE:
                targetFinished(event.target);
                break;
            case STOPPED:
                targetFinished(event.target);
                return;
        }
        for (DtraceDataCollector ddc : slaveCollectors.values()) {
            ddc.targetStateChanged(event);
        }
    }

    public void dataFiltersChanged(List<DataFilter> newSet, boolean isAdjusting) {
    }

    private final class DefaultLineProcessor implements LineProcessor {

        @Override
        public void processLine(String line) {
            DataRow dataRow = parser.process(line);
            addDataRow(dataRow);
        }

        private void addDataRow(DataRow dataRow) {
            if (dataRow != null) {
                if (storage != null && tableMetaData != null) {
                    storage.addData(tableMetaData.getName(), Arrays.asList(dataRow));
                }
                synchronized (indicatorDataBuffer) {
                    indicatorDataBuffer.add(dataRow);
                    if (indicatorDataBuffer.size() >= indicatorFiringFactor) {
                        if (isSlave) {
                            if (parentCollector != null) {
                                parentCollector.notifyIndicators(indicatorDataBuffer);
                            }
                        } else {
                            notifyIndicators(indicatorDataBuffer);
                        }
                        indicatorDataBuffer.clear();
                    }
                }
            }
        }

        @Override
        public void reset() {
        }

        @Override
        public void close() {
            DataRow dataRow = parser.processClose();
            addDataRow(dataRow);
            suggestIndicatorsRepaint();
        }
    }

    private class DemultiplexingLineProcessor implements LineProcessor {

        @Override
        public void processLine(String line) {
            DtraceDataCollector target = lastSlaveCollector;
            for (Map.Entry<String, DtraceDataCollector> entry : slaveCollectors.entrySet()) {
                String prefix = entry.getKey();
                if (line.startsWith(prefix)) {
                    line = line.substring(prefix.length());
                    target = entry.getValue();
                    break;
                }
            }
            if (target != null) {
                target.getOutputProcessor().processLine(line);
            }
            lastSlaveCollector = target;
        }

        @Override
        public void reset() {
            for (Map.Entry<String, DtraceDataCollector> entry : slaveCollectors.entrySet()) {
                entry.getValue().getOutputProcessor().reset();
            }
        }

        @Override
        public void close() {
            for (Map.Entry<String, DtraceDataCollector> entry : slaveCollectors.entrySet()) {
                entry.getValue().getOutputProcessor().close();
            }
            suggestIndicatorsRepaint();
        }
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

    private File mergeScripts() {
        try {
            Map<String, URL> scriptsMap = new HashMap<String, URL>();
            for (Map.Entry<String, DtraceDataCollector> entry : slaveCollectors.entrySet()) {
                scriptsMap.put(entry.getKey(), entry.getValue().getLocalScriptUrl());
            }
            return DTraceScriptUtils.mergeScripts(scriptsMap);
        } catch (IOException ex) {
            DLightLogger.getLogger(DtraceDataCollector.class).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
