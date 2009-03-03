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
package org.netbeans.modules.dlight.perfan.spi;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.util.concurrent.ExecutionException;
import org.netbeans.modules.dlight.api.execution.DLightTarget.State;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.modules.dlight.api.execution.AttachableTarget;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.ValidationListener;
import org.netbeans.modules.dlight.api.execution.ValidationStatus;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.management.api.DLightManager;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration.CollectedInfo;
import org.netbeans.modules.dlight.perfan.storage.impl.PerfanDataStorage;
import org.netbeans.modules.dlight.perfan.util.SunStudioLocator;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.dlight.util.TimerTaskExecutionService;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.AsynchronousAction;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.windows.InputOutput;

/**
 * This class will represent SunStudio Performance Analyzer collect
 * which will be used as DataCollector
 */
public class SunStudioDataCollector
        extends IndicatorDataProvider<SunStudioDCConfiguration>
        implements DataCollector<SunStudioDCConfiguration> {

    private static final String ID = "PerfanDataStorage"; // NOI18N
    // Below are FULL DataTableMetadata objects...
    private static final DataTableMetadata cpuInfoTable;
    private static final DataTableMetadata syncInfoTable;
    private static final DataTableMetadata memInfoTable;
    private static final DataTableMetadata summaryInfoTable;
    private static final List<DataStorageType> supportedStorageTypes;
    private static final Logger log = DLightLogger.getLogger(SunStudioDataCollector.class);
    private final Object lock = new String(SunStudioDataCollector.class.getName());
    private final List<ValidationListener> validationListeners;
    private final List<CollectedInfo> collectedInfoList;
    private ValidationStatus validationStatus = ValidationStatus.initialStatus();
    private PerfanDataStorage storage = null;
    private String experimentDir;
    private Future<Integer> collectTask = null;
    private ExecutionEnvironment execEnv = null;
    private String collectCmd;
    private String sproHome;
    private DLightTarget target = null;
    private FutureTask<Boolean> warmUpTask = null;
    private Future summaryInfoTask = null;


    static {
        supportedStorageTypes = Arrays.asList(DataStorageTypeFactory.getInstance().getDataStorageType(ID));

        cpuInfoTable = new DataTableMetadata(
                "SunStudioCPUDetailedData", // NOI18N
                Arrays.asList(SunStudioDCConfiguration.c_name,
                SunStudioDCConfiguration.c_iUser,
                SunStudioDCConfiguration.c_eUser));


        syncInfoTable = new DataTableMetadata(
                "SunStudioSyncDetailedData", // NOI18N
                Arrays.asList(SunStudioDCConfiguration.c_name,
                SunStudioDCConfiguration.c_iSync,
                SunStudioDCConfiguration.c_iSyncn));

        memInfoTable = new DataTableMetadata("SunStudioMemDetailedData",
                Arrays.asList(SunStudioDCConfiguration.c_name,
                SunStudioDCConfiguration.c_leakCount,
                SunStudioDCConfiguration.c_leakSize));

        summaryInfoTable = new DataTableMetadata(
                "SunStudioSummaryData", // NOI18N
                Arrays.asList(SunStudioDCConfiguration.c_ulockSummary));
    }

    //
    // No matter how many tools/(indicators/detailed views) are relay on
    // SunStudio, - at last we will get here with the full list of what should
    // be collect.
    // This is because collector MUST be created using SSDCProvider ONLY.
    //
    SunStudioDataCollector(List<CollectedInfo> collectedInfoList) {
        this.collectedInfoList = Collections.synchronizedList(
                new ArrayList<CollectedInfo>(collectedInfoList));

        this.validationListeners = Collections.synchronizedList(
                new ArrayList<ValidationListener>());
    }

    public Future<ValidationStatus> validate(final DLightTarget targetToValidate) {
        return DLightExecutorService.service.submit(new Callable<ValidationStatus>() {

            public ValidationStatus call() throws Exception {
                if (validationStatus.isValid()) {
                    return validationStatus;
                }

                ValidationStatus oldStatus = validationStatus;
                ValidationStatus newStatus = doValidation(targetToValidate);

                notifyStatusChanged(oldStatus, newStatus);

                validationStatus = newStatus;
                return newStatus;
            }
        });
    }

    public String getName() {
        return "SunStudio";//NOI18N
    }


    public void invalidate() {
        validationStatus = ValidationStatus.initialStatus();
    }

    public ValidationStatus getValidationStatus() {
        return validationStatus;
    }

    protected synchronized ValidationStatus doValidation(DLightTarget targetToValidate) {
        final String os;
        execEnv = targetToValidate.getExecEnv();

        try {
            os = HostInfoUtils.getOS(execEnv);

            if (!"SunOS".equals(os)) { // NOI18N
                return ValidationStatus.invalidStatus("SunStudioDataCollector works on SunOS only."); // NOI18N
            }

            sproHome = SunStudioLocator.getInstance().getSproHome(execEnv);
            collectCmd = sproHome + "/bin/collect"; // NOI18N

            if (!HostInfoUtils.fileExists(execEnv, collectCmd)) {
                return ValidationStatus.invalidStatus(collectCmd + " not found");
            }

        } catch (ConnectException ex) {
            final ConnectionManager mgr = ConnectionManager.getInstance();
            Runnable onConnect = new Runnable() {

                public void run() {
                    DLightManager.getDefault().revalidateSessions();
                }
            };

            AsynchronousAction connectAction = mgr.getConnectToAction(execEnv, onConnect);

            return ValidationStatus.unknownStatus("Host is not connected...", // NOI18N
                    connectAction);
        }

        return ValidationStatus.validStatus();
    }

    public void addValidationListener(ValidationListener listener) {
        if (!validationListeners.contains(listener)) {
            validationListeners.add(listener);
        }
    }

    public void removeValidationListener(ValidationListener listener) {
        validationListeners.remove(listener);
    }

    protected void notifyStatusChanged(ValidationStatus oldStatus, ValidationStatus newStatus) {
        if (newStatus.equals(oldStatus)) {
            return;
        }

        for (ValidationListener validationListener : validationListeners) {
            validationListener.validationStateChanged(this, oldStatus, newStatus);
        }
    }

    /**
     * Returns unmodifiable list of information collected
     * @return an unmodifiable view of the {@link CollectedInfo}
     */
    public List<CollectedInfo> getCollectedInfo() {
        return Collections.unmodifiableList(collectedInfoList);
    }

    void addCollectedInfo(List<CollectedInfo> collectedInfo) {
        // should add if do not have yet
        for (CollectedInfo c : collectedInfo) {
            if (!collectedInfoList.contains(c)) {
                collectedInfoList.add(c);
            }
        }
    }

    public String getExperimentDirectory() {
        return experimentDir;
    }

    // Is called before target has been started
    public void init(DataStorage dataStorage, DLightTarget target) {
        if (!(dataStorage instanceof PerfanDataStorage)) {
            throw new IllegalArgumentException("You can not use storage " +
                    dataStorage + " for PerfanDataCollector!"); // NOI18N
        }

        DLightLogger.assertTrue(execEnv.equals(target.getExecEnv()),
                "Verification was performed against another execEnv"); // NOI18N

        this.storage = (PerfanDataStorage) dataStorage;
        this.target = target;
        this.experimentDir = "/var/tmp/dlightExperiment.er";

        if (warmUpTask != null && !warmUpTask.isDone()) {
            warmUpTask.cancel(true);
        }

        warmUpTask = new FutureTask<Boolean>(new WarmUpTask(execEnv, experimentDir));
        warmUpTask.run();

        // Init storage (i.e. er_print, actually)
        storage.init(execEnv, sproHome, experimentDir);
    }

    public ExecutionEnvironment getExecEnv() {
        return execEnv;
    }

    private void targetStarted(DLightTarget target) {
        synchronized (lock) {
            // Wait for warm-up task completion ...
            boolean warmUpStatus = false;

            try {
                warmUpStatus = warmUpTask.get().booleanValue();
            } catch (CancellationException ex) {
                log.fine("Will not start SunStudioDataCollector because of " // NOI18N
                        + ex.getMessage());
            } catch (InterruptedException ex) {
                log.fine("Will not start SunStudioDataCollector because of " // NOI18N
                        + ex.getMessage());
            } catch (ExecutionException ex) {
                log.fine("Will not start SunStudioDataCollector because of " // NOI18N
                        + ex.getMessage());
            }

            // Make it null, to start it again on restart ...
            warmUpTask = null;

            if (!warmUpStatus) {
                log.fine("Will not start SunStudioDataCollector because warm-up task failed"); // NOI18N
                return;
            }

            if (isAttachable()) {
                // i.e. should start separate process
                AttachableTarget at = (AttachableTarget) target;
                NativeProcessBuilder npb = new NativeProcessBuilder(execEnv, collectCmd);
                npb = npb.setArguments("-P", "" + at.getPID(), "-o", experimentDir); // NOI18N

                ExecutionDescriptor descr = new ExecutionDescriptor();
                descr = descr.errProcessorFactory(new StdErrRedirectorFactory());
                descr = descr.outProcessorFactory(new StdErrRedirectorFactory());
                descr = descr.inputOutput(InputOutput.NULL);

                ExecutionService service = ExecutionService.newService(npb, descr, "collect"); // NOI18N
                collectTask = service.run();
            }

            // In case when summary data was requested do init
            // periodic SummaryDataFetchingTask ...

            if (collectedInfoList.contains(SunStudioDCConfiguration.CollectedInfo.SYNCSUMMARY)) {
                resetIndicators();
                TimerTaskExecutionService service =
                        TimerTaskExecutionService.getInstance();
                summaryInfoTask = service.scheduleAtFixedRate(
                        new SummaryDataFetchingTask(), 1, TimeUnit.SECONDS);
            }

        }
    }

    private void targetFinished(DLightTarget target) {
        synchronized (lock) {
            if (warmUpTask != null && !warmUpTask.isDone()) {
                warmUpTask.cancel(true);
                warmUpTask = null;
            }

            log.fine("Stopping PerfanDataCollector: " + collectCmd); // NOI18N

            if (isAttachable()) {
                // i.e. separate process
                if (collectTask != null) {
                    collectTask.cancel(true);
                }
            } else {
                // i.e. this means that exactly this collector finished
                // do nothing
            }

            collectTask = null;

            if (summaryInfoTask != null) {
                summaryInfoTask.cancel(true);
                summaryInfoTask = null;
            }
        }

    }

    public Collection<DataStorageType> getSupportedDataStorageTypes() {
        return supportedStorageTypes;
    }

    public List<DataTableMetadata> getDataTablesMetadata() {
        List<DataTableMetadata> result = new ArrayList<DataTableMetadata>();

        if (collectedInfoList.contains(CollectedInfo.FUNCTIONS_LIST)) {
            result.add(cpuInfoTable);
        }

        if (collectedInfoList.contains(CollectedInfo.SYNCHRONIZARION)) {
            result.add(syncInfoTable);
        }

        if (collectedInfoList.contains(CollectedInfo.MEMORY)) {
            result.add(memInfoTable);
        }

        if (collectedInfoList.contains(CollectedInfo.SYNCSUMMARY)) {
            result.add(summaryInfoTable);
        }

        return result;
    }

    public boolean isAttachable() {
        if (collectedInfoList.contains(CollectedInfo.SYNCHRONIZARION) ||
                collectedInfoList.contains(CollectedInfo.MEMORY)) {
            return false;
        }
        return true;
    }

    public String getCmd() {
        return collectCmd;
    }

    public String[] getArgs() {
        List<String> args = new ArrayList<String>();

        // From collect(1):
        // ...
        // -l signal
        //    Record a sample point  whenever  the  given  signal  is
        //    delivered to the process.
        // ..
        // Add this arguments to allow indicator provider based on
        // mmonitor to coexist with collect
        
        args.add("-l"); // NOI18N
        args.add("USR1"); // NOI18N

        if (collectedInfoList.contains(CollectedInfo.SYNCHRONIZARION)) {
            args.add("-s"); // NOI18N
            args.add("30"); // NOI18N
        }

        if (collectedInfoList.contains(CollectedInfo.MEMORY)) {
            args.add("-H"); // NOI18N
            args.add("on"); // NOI18N
        }

        args.add("-o"); // NOI18N
        args.add(getExperimentDir());
        return args.toArray(new String[0]);
    }

    protected String getExperimentDir() {
        return experimentDir;
    }

    public void targetStateChanged(DLightTarget source, State oldState, State newState) {
        // We need to be sure that events are processed sequentially ...
        // So use synchronized block ...
        // ??? Not sure - need to review.
        switch (newState) {
            case STARTING:
                if (warmUpTask == null) {
                    // Re-start
                    warmUpTask = new FutureTask<Boolean>(new WarmUpTask(execEnv, experimentDir));
                    warmUpTask.run();
                }
                return;
            case RUNNING:
                targetStarted(source);
                return;
            case FAILED:
                targetFinished(source);
                return;
            case TERMINATED:
                targetFinished(source);
                return;
            case DONE:
                targetFinished(source);
                return;
            case STOPPED:
                targetFinished(source);
                return;
        }
    }

    private static class StdErrRedirectorFactory
            implements InputProcessorFactory {

        public InputProcessor newInputProcessor(InputProcessor p) {
            return InputProcessors.copying(new OutputStreamWriter(System.err) {

                final StringBuilder sb = new StringBuilder();
                final static String prefix = "!!!!! COLLECTOR SAYS !!!! : "; // NOI18N

                @Override
                public void write(char[] chars) throws IOException {
                    sb.setLength(0);
                    sb.append(prefix);
                    for (int i = 0; i < chars.length; i++) {
                        sb.append(chars[i]);
                        if (i < chars.length - 1 && chars[i] == '\n') {
                            sb.append(prefix);
                        }
                    }
                    super.write(sb.toString().toCharArray());
                }
            });
        }
    }

    private static final class WarmUpTask implements Callable<Boolean> {

        private final String dirName;
        private final ExecutionEnvironment execEnv;

        public WarmUpTask(ExecutionEnvironment execEnv, String dirName) {
            this.dirName = dirName;
            this.execEnv = execEnv;
        }

        public Boolean call() throws Exception {
            boolean status = true;

            log.fine("Prepare PerfanDataCollector. Clean directory " + dirName); // NOI18N
            Future<Integer> rmFuture;
            Integer rmResult = null;

            rmFuture = CommonTasksSupport.rmDir(execEnv, dirName, true, null);

            try {
                rmResult = rmFuture.get();
            } catch (InterruptedException ex) {
            } catch (ExecutionException ex) {
            }

            if (rmResult == null || rmResult.intValue() != 0) {
                log.info("SunStudioDataCollector: unable to delete directory " // NOI18N
                        + execEnv.toString() + ":" + dirName); // NOI18N
                status = false;
            }

            File lockFile = new File(new File(dirName).getParentFile(), "_collector_directory_lock"); // NOI18N
            rmFuture = CommonTasksSupport.rmFile(execEnv, lockFile.getPath(), null);

            try {
                rmResult = rmFuture.get();
            } catch (InterruptedException ex) {
            } catch (ExecutionException ex) {
            }

            if (status == false) {
                log.severe("Unable to prepare experiment directory!"); // NOI18N
            }

            return new Boolean(status);
        }
    }

    private final class SummaryDataFetchingTask implements Runnable {

        private List<String> colNames = new ArrayList<String>();

        public SummaryDataFetchingTask() {
            for (CollectedInfo info : collectedInfoList) {
                if (info == CollectedInfo.SYNCSUMMARY) {
                    colNames.add(SunStudioDCConfiguration.c_ulockSummary.getColumnName());
                }
            }
        }

        public void run() {
            System.out.println("!!!!!!!!!!!!!! Receive Indicator Data from SunStudio er_print!!!!!");

            if (colNames.isEmpty()) {
                return;
            }

            List data = storage.fetchSummaryData(colNames);
            SunStudioDataCollector.this.notifyIndicators(Arrays.asList(new DataRow(colNames, data)));
        }
    }
}
