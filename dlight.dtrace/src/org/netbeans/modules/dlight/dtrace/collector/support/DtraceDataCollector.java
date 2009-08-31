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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
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
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.impl.SQLDataStorage;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.dlight.util.Util;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.AsynchronousAction;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.SolarisPrivilegesSupport;
import org.netbeans.modules.nativeexecution.api.util.SolarisPrivilegesSupportProvider;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

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
    private Future<Integer> dtraceTask = null;
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
    private ProcessLineCallback callback = new ProcessLineCallBackImpl();

    private boolean isSlave;
    private final boolean multiScriptMode;
    private DtraceDataCollector parentCollector;
    private final Map<String, DtraceDataCollector> slaveCollectors;
    private DtraceDataCollector lastSlaveCollector;

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
            setProcessLineCallback(new MergedProcessLineCallbackImpl());
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

    void setProcessLineCallback(ProcessLineCallback callback) {
        this.callback = callback;
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

    ProcessLineCallback getProcessLineCallback() {
        return callback;
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
            ((DtraceDataAndStackParser)this.parser).setStackDataStorage(stackStorage);
        }

        if (isSlave) {
            return;
        }

        ExecutionEnvironment execEnv = target.getExecEnv();

        if (execEnv.isLocal()) {
            // No need to copy file on localhost -
            // just esnure execution permissions...
            scriptPath = Util.copyResource(localScriptUrl);
            Util.setExecutionPermissions(Arrays.asList(scriptPath));
        } else {
            String briefName = Util.getBriefName(localScriptUrl);
            try {
                HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv);
                scriptPath = hostInfo.getTempDir() + "/" + briefName; // NOI18N
                Future<Integer> copyResult = CommonTasksSupport.uploadFile(
                        Util.copyResource(localScriptUrl), execEnv, scriptPath, 0777, null);
                copyResult.get();
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

    private void targetFinished(DLightTarget target) {
        if (!isSlave) {
            if (dtraceTask != null && !dtraceTask.isDone()) {
                // It could be already done here, because tracked process is
                // finished and, depending on command-line utility, it may exit
                // as well... But, if not - terminate it.
                log.fine("Stopping DtraceDataCollector: " + // NOI18N
                        dtraceTask.toString());

                dtraceTask.cancel(true);
                dtraceTask = null;
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

        ValidationStatus result = null;
        boolean fileExists = false;
        boolean connected = true;
        String error = ""; // NOI18N

        HostInfo hostInfo = null;

        try {
            hostInfo = HostInfoUtils.getHostInfo(execEnv);
        } catch (IOException ex) {
        } catch (CancellationException ex) {
        }

        if (hostInfo == null || hostInfo.getOSFamily() != HostInfo.OSFamily.SUNOS) {
            return ValidationStatus.invalidStatus(
                    NbBundle.getMessage(DtraceDataCollector.class,
                    "DtraceDataCollector.DtraceIsSupportedOnSunOSOnly")); // NOI18N
        }

        try {
            fileExists = HostInfoUtils.fileExists(execEnv, command);
        } catch (IOException ex) {
            error = ex.getMessage();
            connected = false;
        }

        if (connected) {
            if (fileExists) {
                result = ValidationStatus.validStatus();
            } else {
                result = ValidationStatus.invalidStatus(
                        loc("ValidationStatus.CommandNotFound", // NOI18N
                        command));
            }
        } else {
            ConnectionManager mgr = ConnectionManager.getInstance();

            Runnable doOnConnect = new Runnable() {

                public void run() {
                    DLightManager.getDefault().revalidateSessions();
                }
            };

            AsynchronousAction connectAction = mgr.getConnectToAction(execEnv, doOnConnect);

            result = ValidationStatus.unknownStatus(
                    loc("ValidationStatus.ErrorWhileValidation", error), // NOI18N
                    connectAction);
        }

        if (!result.isValid()) {
            return result;
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

            result = result.merge(ValidationStatus.unknownStatus(
                    loc("DTraceDataCollector_Status_NotEnoughPrivileges"), // NOI18N
                    requestPrivilegesAction));
        }

        return result;
    }

    public ValidationStatus validate(final DLightTarget target) {
        return validate(target, this, true);
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
        if (isSlave) {
            return;
        }

        String taskCommand = scriptPath; //"pfexec " + scriptPath;

        if (target instanceof AttachableTarget) {
            AttachableTarget at = (AttachableTarget) target;
            taskCommand += " " + at.getPID(); // NOI18N
        }

        final String extraParams = getCollectorTaskExtraParams();
        if (extraParams != null) {
            taskCommand += " " + extraParams; // NOI18N
        }

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(target.getExecEnv());
        npb.setCommandLine(taskCommand);

        ExecutionDescriptor descr = new ExecutionDescriptor();
        descr = descr.outProcessorFactory(new DtraceInputProcessorFactory());
        descr = descr.errProcessorFactory(new StdErrRedirectorFactory());
        descr = descr.inputOutput(InputOutput.NULL);

        ExecutionService execService = ExecutionService.newService(
                npb, descr, "DTraceDataCollector " + taskCommand); // NOI18N

        dtraceTask = execService.run();

        log.fine("DtraceDataCollector (" + dtraceTask.toString() + // NOI18N
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

    public void dataFiltersChanged(List<DataFilter> newSet) {
    }

    private final class ProcessLineCallBackImpl implements ProcessLineCallback {

        private long maxTimestamp;

        public void processLine(String line) {
            DataRow dataRow = parser.process(line);
            addDataRow(dataRow);
        }

        private void addDataRow(DataRow dataRow) {
            if (dataRow != null) {
                if (storage != null && tableMetaData != null) {
                    storage.addData(tableMetaData.getName(), Arrays.asList(dataRow));
                }
                long curTimestamp = getTimestamp(dataRow);
                if (curTimestamp == -1 || maxTimestamp < curTimestamp) {
                    synchronized (indicatorDataBuffer) {
                        if (curTimestamp != -1) {
                            maxTimestamp = curTimestamp;
                        }
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
        }

        private long getTimestamp(DataRow row) {
            Object timestamp = row.getData("timestamp"); // NOI18N
            if (timestamp instanceof Number) {
                return ((Number) timestamp).longValue();
            } else if (timestamp instanceof String) {
                try {
                    return Long.parseLong((String) timestamp);
                } catch (NumberFormatException ex) {
                }
            }
            return -1;
        }

        public void processClose() {
            DataRow dataRow = parser.processClose();
            addDataRow(dataRow);
        }
    }

    private class MergedProcessLineCallbackImpl implements ProcessLineCallback {

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
                target.getProcessLineCallback().processLine(line);
            }
            lastSlaveCollector = target;
        }

        public void processClose() {
            for (Map.Entry<String, DtraceDataCollector> entry : slaveCollectors.entrySet()) {
                entry.getValue().getProcessLineCallback().processClose();
            }
        }
    }

    private class DtraceInputProcessorFactory implements InputProcessorFactory {

        public InputProcessor newInputProcessor(InputProcessor p) {
            return InputProcessors.bridge(new LineProcessor() {

                @Override
                public void processLine(String line) {
                    callback.processLine(line);
                }

                public void reset() {
                }

                public void close() {
                    callback.processClose();
                }
            });
        }
    }

    private static class StdErrRedirectorFactory
            implements InputProcessorFactory {

        public InputProcessor newInputProcessor(InputProcessor p) {
            return InputProcessors.copying(new OutputStreamWriter(System.err) {
                @Override
                public void close() throws IOException {
                    //Do not close System.err
                }
            });
        }
    }

    private File mergeScripts() {
        try {
            File output = File.createTempFile("dlight", ".d"); // NOI18N
            BufferedWriter w = new BufferedWriter(new FileWriter(output));
            try {
                w.write("#!/usr/sbin/dtrace -ZCqs\n"); // NOI18N
                for (Map.Entry<String, DtraceDataCollector> entry : slaveCollectors.entrySet()) {
                    DtraceDataCollector ddc = entry.getValue();
                    BufferedReader r = new BufferedReader(new InputStreamReader(ddc.getLocalScriptUrl().openStream()));
                    try {
                        for (String line = r.readLine(); line != null; line = r.readLine()) {
                            if (!line.startsWith("#!")) { // NOI18N
                                w.write(line.replaceAll("(print[af]\\(\")", "$1" + entry.getKey())); // NOI18N
                                w.write('\n'); // NOI18N
                            }
                        }
                        w.write('\n'); // NOI18N
                    } finally {
                        r.close();
                    }
                }
            } finally {
                w.close();
            }
            return output;
        } catch (IOException ex) {
            DLightLogger.getLogger(DtraceDataCollector.class).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
