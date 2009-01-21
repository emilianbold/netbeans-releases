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
package org.netbeans.dlight.dtrace.collector.support;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.channels.Channels;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.dlight.dtrace.collector.impl.DTDCConfigurationAccessor;
import org.netbeans.modules.dlight.execution.api.AttachableTarget;
import org.netbeans.modules.dlight.execution.api.DLightTarget;
import org.netbeans.modules.dlight.management.api.DLightManager;
import org.netbeans.modules.dlight.model.Validateable.ValidationState;
import org.netbeans.modules.dlight.model.Validateable.ValidationStatus;
import org.netbeans.modules.dlight.model.ValidationListener;
import org.netbeans.modules.dlight.collector.spi.DataCollector;
import org.netbeans.modules.dlight.indicator.spi.IndicatorDataProvider;
import org.netbeans.modules.dlight.storage.spi.DataStorage;
import org.netbeans.modules.dlight.storage.spi.DataStorageType;
import org.netbeans.modules.dlight.storage.spi.DataStorageTypeFactory;
import org.netbeans.modules.dlight.storage.spi.support.SQLDataStorage;
import org.netbeans.modules.dlight.storage.api.DataRow;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.dlight.util.Util;
import org.netbeans.modules.nativeexecution.ConnectionManager;
import org.netbeans.modules.nativeexecution.CopyTask;
import org.netbeans.modules.nativeexecution.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.HostInfo;
import org.netbeans.modules.nativeexecution.HostNotConnectedException;
import org.netbeans.modules.nativeexecution.NativeTask;
import org.netbeans.modules.nativeexecution.ObservableAction;
import org.netbeans.modules.nativeexecution.ObservableActionListener;
import org.netbeans.modules.nativeexecution.TaskPrivilegesSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;



/**
 * Collector which collects data using DTrace sctiprs.
 * You should describe data collected using list of {@link org.netbeans.dlight.core.storage.model.DataTableMetadata}.
 * You can define your own implementation of  {@link org.netbeans.dlight.dtrace.collector.DtraceParser}
 */
public final class DtraceDataCollector extends IndicatorDataProvider<DTDCConfiguration> implements DataCollector<DTDCConfiguration> {

    private static final List<String> ultimateDTracePrivilegesList =
            Arrays.asList(new String[]{DTDCConfiguration.DTRACE_KERNEL, DTDCConfiguration.DTRACE_PROC, DTDCConfiguration.DTRACE_USER});
    private static final String cmd_dtrace = "/usr/sbin/dtrace"; // NOI18N
    private static final Logger log = DLightLogger.getLogger(DtraceDataCollector.class);
    private List<String> requiredPrivilegesList;
    private DataTableMetadata tableMetaData = null;
    private String localScriptPath;
    private String extraArgs;
    private String scriptPath;
    private NativeTask collectorTask;
    private DTDCConfiguration configuration;
    private ValidationStatus validationStatus = ValidationStatus.NOT_VALIDATED;
    private List<ValidationListener> validationListeners = Collections.synchronizedList(new ArrayList<ValidationListener>());
    private String command;
    private String argsTemplate;
    private Thread outProcessingThread;
    private DataStorage storage;
    private List<DataTableMetadata> dataTablesMetadata;
    private DtraceParser parser;
    private final List<DataRow> indicatorDataBuffer = new ArrayList<DataRow>();
    private boolean isSlave;
    private String prefix;
    private int indicatorFiringFactor;
    private ProcessLineCallback callback = new ProcessLineCallBackImpl();

    public DtraceDataCollector() {
    }

    protected DtraceDataCollector(DTDCConfiguration configuration) {
        this.command = cmd_dtrace;
        this.argsTemplate = null;
        this.dataTablesMetadata = DTDCConfigurationAccessor.getDefault().getDatatableMetadata(configuration);
        this.tableMetaData = dataTablesMetadata != null && dataTablesMetadata.size() > 0 ? dataTablesMetadata.get(0) : null;
        if (DTDCConfigurationAccessor.getDefault().isStackSupportEnabled(configuration)) {
            this.parser = new DtraceDataAndStackParser(tableMetaData);
        } else {
            this.parser = DTDCConfigurationAccessor.getDefault().getParser(configuration) == null ? (tableMetaData != null ? new DtraceParser(tableMetaData) : (DtraceParser) null) : DTDCConfigurationAccessor.getDefault().getParser(configuration);
        }
        // super(cmd_dtrace, null,
//            configuration.getParser() == null ? (configuration.getDatatableMetadata() != null && configuration.getDatatableMetadata().size() > 0 ? new DtraceParser(configuration.getDatatableMetadata().get(0)) : (DtraceParser) null) : configuration.getParser(), configuration.getDatatableMetadata());
        this.localScriptPath = DTDCConfigurationAccessor.getDefault().getScriptPath(configuration);
        this.extraArgs = DTDCConfigurationAccessor.getDefault().getArgs(configuration);

        this.requiredPrivilegesList =
                DTDCConfigurationAccessor.getDefault().getRequiredPrivileges(configuration) == null ? ultimateDTracePrivilegesList : DTDCConfigurationAccessor.getDefault().getRequiredPrivileges(configuration);
        this.configuration = configuration;
        this.indicatorFiringFactor = DTDCConfigurationAccessor.getDefault().getIndicatorFiringFactor(configuration);
    }

    void setProcessLineCallback(ProcessLineCallback callback) {
        this.callback = callback;
    }

    void setSlave(boolean isSlave) {
        this.isSlave = isSlave;
    }

    String getLocalScriptPath() {
        return localScriptPath;
    }

    void setLocalScriptPath(String path) {
        localScriptPath = path;
    }

    ProcessLineCallback getProcessLineCallback() {
        return callback;
    }

    protected DataStorage getStorage() {
        return storage;
    }

    /**
     * The types of storage this collector supports
     * @return returns list of {@link org.netbeans.dlight.core.storage.model.DataStorageType}
     * data collector can put data into
     */
    public List<DataStorageType> getSupportedDataStorageTypes() {
        return Arrays.asList(DataStorageTypeFactory.getInstance().getDataStorageType(SQLDataStorage.SQL_DATA_STORAGE_TYPE));
    }

    public boolean isAttachable() {
        return true;
    }

//    @Override
    public void init(DataStorage storage, DLightTarget target) {
        this.storage = storage;
        if (isSlave) {
            return;
        }
        ExecutionEnvironment execEnv = target.getExecEnv();

        if (execEnv.isLocal()) {
            // No need to copy file on localhost - just esnure execution permissions...
            scriptPath = localScriptPath;
            Util.setExecutionPermissions(Arrays.asList(scriptPath));
        } else {
            File script = new File(localScriptPath);
            scriptPath = "/tmp/" + script.getName(); // NOI18N
            try {
                CopyTask.copyLocalFile(execEnv, localScriptPath, scriptPath, 777, false);
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }

        }

        collectorTask = new NativeTask(execEnv, null, null);
    }

    public String getCmd() {
        return command;
    }

    public String[] getArgs() {
        return null;
    }

    public List<? extends DataTableMetadata> getDataTablesMetadata() {
        return dataTablesMetadata;
    }

    public String getID() {
        return DTDCConfigurationAccessor.getDefault().getID();
    }

    NativeTask getCollectorTaskFor(DLightTarget target) {
        String taskCommand = scriptPath;//"pfexec " + scriptPath;
        if (target instanceof AttachableTarget) {
            AttachableTarget at = (AttachableTarget) target;
            taskCommand = scriptPath.concat(" ").concat(String.valueOf(at.getPID())); // NOI18N
        }

        String extraParams = getCollectorTaskExtraParams();
        if (extraParams != null) {
            taskCommand = taskCommand.concat(" " + extraParams);//NOI18N
        }

        collectorTask.setCommand(taskCommand);
        return collectorTask;
    }

    /** override this if you need to add extra parameters to the DTrace script */
    protected String getCollectorTaskExtraParams() {
        return extraArgs;
    }

//    @Override
    public void targetFinished(DLightTarget target, int result) {
        if (!isSlave) {
            log.fine("Stopping DtraceDataCollector: " + collectorTask.getCommand());
            collectorTask.cancel();
            outProcessingThread.interrupt();
        }
        synchronized (indicatorDataBuffer) {
            if (!indicatorDataBuffer.isEmpty()) {
                notifyIndicators(indicatorDataBuffer);
                indicatorDataBuffer.clear();
            }
        }
    }

    private static String loc(String key) {
        return NbBundle.getMessage(DtraceDataCollector.class, key);
    }

    private static String loc(String key, String param) {
        return NbBundle.getMessage(DtraceDataCollector.class, key, param);
    }

    private ValidationStatus doValidation(final DLightTarget target) {
        DLightLogger.assertNonUiThread();

        ValidationStatus result = ValidationStatus.NOT_VALIDATED;
        boolean fileExists = false;
        boolean connected = true;

        try {
            fileExists = HostInfo.fileExists(target.getExecEnv(), command);
        } catch (HostNotConnectedException ex) {
            connected = false;
        }

        if (connected) {
            if (fileExists) {
                result = ValidationStatus.VALID;
            } else {
                result = new ValidationStatus(
                        ValidationState.NOT_VALID,
                        loc("ValidationStatus.CommandNotFound", command)); // NOI18N
            }
        } else {
            ConnectionManager cm = ConnectionManager.getInstance();
            ObservableActionListener<Boolean> listener = new ObservableActionListener<Boolean>() {

                public void actionCompleted(Action source, Boolean result) {
                    if (DLightManager.getDefault().getActiveSession() != null) {
                        DLightManager.getDefault().getActiveSession().revalidate();
                    }
                }

                public void actionStarted(Action source) {
//                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };

            ObservableAction connectAction = cm.getConnectAction(target.getExecEnv());
            connectAction.addObservableActionListener(listener);

            result = new ValidationStatus(ValidationState.UNKNOWN,
                    loc("ValidationStatus.HostNotConnected"), // NOI18N
                    connectAction);
        }

//    return result;
//    ValidationStatus result = super.doValidation(targetToValidate);
        ExecutionEnvironment execEnv = target.getExecEnv();

        if (result.isOK()) {

            // /usr/sbin/dtrace exists...
            // check for permissions ...
            boolean status = TaskPrivilegesSupport.getInstance().hasPrivileges(execEnv, requiredPrivilegesList);
            ObservableAction<Boolean> requestPrivilegesAction = TaskPrivilegesSupport.getRequestPrivilegesAction(
                    execEnv, requiredPrivilegesList);

            requestPrivilegesAction.addObservableActionListener(new ObservableActionListener<Boolean>() {

                public void actionCompleted(Action source, Boolean result) {
                    DLightManager.getDefault().revalidateSessions();
                }

                public void actionStarted(Action source) {
                }
            });

            if (!status) {
                result = result.merge(
                        new ValidationStatus(ValidationState.UNKNOWN,
                        loc("DTraceDataCollector_Status_NotEnoughPrivileges"), // NOI18N
                        requestPrivilegesAction));
            }
        }

        return result;
    }

    public Future<ValidationStatus> validate(final DLightTarget target) {
        return DLightExecutorService.service.submit(new Callable<ValidationStatus>() {

            public ValidationStatus call() throws Exception {
                if (validationStatus.isOK()) {
                    return validationStatus;
                }

                ValidationStatus oldStatus = validationStatus;
                ValidationStatus newStatus = doValidation(target);

                if (!(newStatus.getState().equals(oldStatus.getState()) &&
                        newStatus.getReason().equals(oldStatus.getReason()))) {
                    notifyStatusChanged(newStatus);
                }

                validationStatus = newStatus;
                return newStatus;
            }
        });
    }

//    @Override
//    public void targetFinished(DLightTarget target, int result) {
//        if (isSlave) {
//            super.targetFinished(target, result);
//        }
//    }
    public void invalidate() {
        validationStatus = ValidationStatus.NOT_VALIDATED;
    }

    public ValidationStatus getValidationStatus() {
        return validationStatus;
    }

    public void targetStarted(DLightTarget target) {
        if (isSlave) {
            return;
        }
        collectorTask = getCollectorTaskFor(target);

        outProcessingThread = new Thread(new Runnable() {

            public void run() {
                try {
                    String line;
                    InputStream is = collectorTask.getInputStream();

                    if (is == null) {
                        return;
                    }

                    BufferedReader reader = new BufferedReader(Channels.newReader(Channels.newChannel(is), "UTF-8")); // NOI18N

                    while ((line = reader.readLine()) != null) {
                        callback.processLine(line);
                    }

                    Thread.sleep(10);

                } catch (InterruptedException ex) {
                    log.fine(Thread.currentThread().getName() + " interrupted. Stop it.");
                } catch (InterruptedIOException ex) {
                    log.fine(Thread.currentThread().getName() + " interrupted. Stop it.");
                } catch (ClosedByInterruptException ex) {
                    log.fine(Thread.currentThread().getName() + " interrupted. Stop it.");
                } catch (IOException ex) {
                    log.fine(Thread.currentThread().getName() + " io. Stop it.");
                //Logger.getLogger(CLIODataCollector.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, "CLI Data Collector Output Redirector");

        collectorTask.submit();
        outProcessingThread.start();
    }

    public void addValidationListener(ValidationListener listener) {
        if (!validationListeners.contains(listener)) {
            validationListeners.add(listener);
        }
    }

    public void removeValidationListener(ValidationListener listener) {
        validationListeners.remove(listener);
    }

    protected void notifyStatusChanged(ValidationStatus newStatus) {
        for (ValidationListener validationListener : validationListeners) {
            validationListener.validationStateChanged(this, newStatus);
        }
    }

    @Override
    public DtraceDataCollector create(DTDCConfiguration configuration) {
        return new DtraceDataCollector(configuration);
    }

    private final class ProcessLineCallBackImpl implements ProcessLineCallback {

        public void processLine(String line) {
            DataRow dataRow = parser.process(line);
            if (dataRow != null) {
                if (storage != null && tableMetaData != null) {
                    storage.addData(tableMetaData.getName(), Arrays.asList(dataRow));
                }
                synchronized (indicatorDataBuffer) {
                    if (indicatorDataBuffer.size() >= indicatorFiringFactor) {
                        notifyIndicators(indicatorDataBuffer);
                        indicatorDataBuffer.clear();
                    }
                    indicatorDataBuffer.add(dataRow);
                }
            }
        }
    }

    private static String loc(String key, Object... params) {
        return NbBundle.getMessage(DtraceDataCollector.class, key, params);
    }
}
