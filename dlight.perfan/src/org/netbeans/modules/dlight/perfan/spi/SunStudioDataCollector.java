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
 * accompanied this code. If applicable, addCollectedInfo the following below the
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
 * However, if you addCollectedInfo GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.perfan.spi;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.modules.dlight.api.execution.AttachableTarget;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.DLightTargetChangeEvent;
import org.netbeans.modules.dlight.api.execution.ValidationListener;
import org.netbeans.modules.dlight.api.execution.ValidationStatus;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.management.api.DLightManager;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration.CollectedInfo;
import org.netbeans.modules.dlight.perfan.spi.datafilter.CollectedObjectsFilter;
import org.netbeans.modules.dlight.perfan.storage.impl.PerfanDataStorage;
import org.netbeans.modules.dlight.spi.SunStudioLocator.SunStudioDescription;
import org.netbeans.modules.dlight.spi.SunStudioLocatorFactory;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.perfan.impl.SunStudioDCConfigurationAccessor;
import org.netbeans.modules.dlight.perfan.spi.datafilter.SunStudioFiltersProvider;
import org.netbeans.modules.dlight.perfan.spi.datafilter.THAFilter;
import org.netbeans.modules.dlight.perfan.spi.datafilter.THAStartupFilter;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.AsynchronousAction;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 *
 * @author ak119685
 */
public class SunStudioDataCollector
        extends IndicatorDataProvider<SunStudioDCConfiguration>
        implements DataCollector<SunStudioDCConfiguration>, SunStudioFiltersProvider {

    private static final String COLLECTOR_NAME = "SunStudio"; // NOI18N
    private static final DataStorageType supportedStorageType;
    private static final AtomicInteger uid = new AtomicInteger(0);
    private static final Logger log = DLightLogger.getLogger(SunStudioDataCollector.class);
    // Below is COMPLETE list of DataTableMetadata objects...
    private static final DataTableMetadata cpuInfoTable;
    private static final DataTableMetadata syncInfoTable;
    private static final DataTableMetadata memInfoTable;
    private static final DataTableMetadata summaryInfoTable;
    private static final DataTableMetadata memSummaryInfoTable;
    private static final DataTableMetadata deadlocksSummaryInfoTable;
    private static final DataTableMetadata dataracesSummaryInfoTable;
    // ***
    private final Object lock = new String(SunStudioDataCollector.class.getName());
    // ***
    private final Collection<DataTableMetadata> dataTablesMetadata;
    private final Collection<ValidationListener> validationListeners;
    private final Set<CollectedInfo> collectedInfo;
    private final List<DataFilter> dataFilters;
    // ***
    private ValidationStatus validationStatus = ValidationStatus.initialStatus();
    private CollectorConfiguration config = null;
    private DLightTarget validatedTarget;
    private Future<Integer> collectTaskResult = null;
    private MonitorsUpdateService monitorsUpdater = null;
    private String cmd;
    private String sproHome;
    private boolean isAttachable;
    private HostInfo hostInfo = null;

    static {
        SunStudioDCConfigurationAccessor dcAccess = SunStudioDCConfigurationAccessor.getDefault();

        supportedStorageType = PerfanDataStorage.storageType;

        cpuInfoTable = new DataTableMetadata(
                dcAccess.getCPUTableName(),
                Arrays.asList(SunStudioDCConfiguration.c_name,
                SunStudioDCConfiguration.c_iUser,
                SunStudioDCConfiguration.c_eUser),
                null);

        syncInfoTable = new DataTableMetadata(
                dcAccess.getSyncTableName(),
                Arrays.asList(SunStudioDCConfiguration.c_name,
                SunStudioDCConfiguration.c_eSync,
                SunStudioDCConfiguration.c_eSyncn),
                null);

        memInfoTable = new DataTableMetadata(
                dcAccess.getMemTableName(),
                Arrays.asList(SunStudioDCConfiguration.c_name,
                SunStudioDCConfiguration.c_leakCount,
                SunStudioDCConfiguration.c_leakSize),
                null);

        summaryInfoTable = new DataTableMetadata(
                "SunStudioSummaryData", // NOI18N
                Arrays.asList(SunStudioDCConfiguration.c_ulockSummary),
                null);

        memSummaryInfoTable = new DataTableMetadata(
                "SunStudioMemorySummaryData", // NOI18N
                Arrays.asList(SunStudioDCConfiguration.c_leakSize),
                null);

        deadlocksSummaryInfoTable = new DataTableMetadata(
                dcAccess.getDeadlockTableName(), // NOI18N
                Arrays.asList(SunStudioDCConfiguration.c_Deadlocks),
                null);

        dataracesSummaryInfoTable = new DataTableMetadata(
                dcAccess.getDataraceTableName(), // NOI18N
                Arrays.asList(SunStudioDCConfiguration.c_Datarace),
                null);
    }

    public SunStudioDataCollector(Set<CollectedInfo> collectedInfoList) {
        collectedInfo = EnumSet.<CollectedInfo>noneOf(CollectedInfo.class);
        dataTablesMetadata = new HashSet<DataTableMetadata>();
        validationListeners = new CopyOnWriteArraySet<ValidationListener>();
        isAttachable = true;
        dataFilters = new ArrayList<DataFilter>();
        addCollectedInfo(collectedInfoList);
    }

    void addCollectedInfo(final Set<CollectedInfo> collectedInfoList) {
        synchronized (lock) {
            collectedInfo.addAll(collectedInfoList);
        }
    }

    public void targetStateChanged(DLightTargetChangeEvent event) {
        switch (event.state) {
            case STARTING:
                // TODO !!!
                // In case of re-starting the target we just need to re-init
                // this collector.
                // But currently there is no way to "restart" the target -
                // every time we do just new start with the new target/collector...
                return;
            case RUNNING:
                targetStarted(event.target);
                return;
            case FAILED:
                targetFinished(event.target);
                return;
            case TERMINATED:
                targetFinished(event.target);
                return;
            case DONE:
                targetFinished(event.target);
                return;
            case STOPPED:
                targetFinished(event.target);
                return;
        }
    }

    public ValidationStatus validate(DLightTarget target) {
        synchronized (lock) {
            if (validationStatus.isKnown()) {
                return validationStatus;
            }

            ExecutionEnvironment execEnv = target.getExecEnv();

            String command = null;
            String sprohome = null;

            try {
                hostInfo = HostInfoUtils.getHostInfo(execEnv);

                switch (hostInfo.getOSFamily()) {
                    case LINUX:
                    case SUNOS:
                        break;
                    default:
                        validationStatus = ValidationStatus.invalidStatus(
                                loc("ValidationStatus.UnsupportedPlatform")); // NOI18N
                        return validationStatus;
                }

                Collection<? extends SunStudioLocatorFactory> factories =
                        Lookup.getDefault().lookupAll(SunStudioLocatorFactory.class);


                //we will get first we have
                boolean notFound = true;

                for (SunStudioLocatorFactory factory : factories) {
                    Collection<SunStudioDescription> ssDescriptions =
                            factory.getInstance(execEnv).getSunStudioLocations();

                    for (SunStudioDescription ss : ssDescriptions) {
                        sprohome = ss.getPath();
                        //SunStudioLocator.getInstance().getSproHome(execEnv);
                        command = sprohome + "/bin/collect"; // NOI18N

                        if (HostInfoUtils.fileExists(execEnv, command)) {
                            notFound = false;
                            break;
                        }
                    }
                }

                if (notFound) {
                    validationStatus = ValidationStatus.invalidStatus(
                            loc("ValidationStatus.NoSunStudioFound.html")); //NOI18N
                    return validationStatus;
                }

            } catch (IOException ex) {
                final ConnectionManager mgr = ConnectionManager.getInstance();
                Runnable onConnect = new Runnable() {

                    public void run() {
                        DLightManager.getDefault().revalidateSessions();
                    }
                };

                AsynchronousAction connectAction = mgr.getConnectToAction(execEnv, onConnect);

                validationStatus = ValidationStatus.unknownStatus(
                        loc("ValidationStatus.ErrorWhileValidation", ex.getMessage()), // NOI18N
                        connectAction);
                return validationStatus;
            }

            validateCollectedInfo();

            validationStatus = ValidationStatus.validStatus();
            cmd = command;
            sproHome = sprohome;
            validatedTarget = target;

            return validationStatus;
        }
    }

    public void invalidate() {
        synchronized (lock) {
            validationStatus = ValidationStatus.initialStatus();
            validatedTarget = null;
            config = null;
        }
    }

    public ValidationStatus getValidationStatus() {
        synchronized (lock) {
            return validationStatus;
        }
    }

    public void addValidationListener(ValidationListener listener) {
        validationListeners.add(listener);
    }

    public void removeValidationListener(ValidationListener listener) {
        validationListeners.remove(listener);
    }

    public Collection<DataStorageType> getRequiredDataStorageTypes() {
        return Collections.singletonList(supportedStorageType);
    }

    public List<DataTableMetadata> getDataTablesMetadata() {
        synchronized (lock) {
            return new ArrayList<DataTableMetadata>(dataTablesMetadata);
        }
    }

    private void validateCollectedInfo() {
        synchronized (lock) {
            Set<CollectedInfo> ci = EnumSet.<CollectedInfo>copyOf(collectedInfo);
            Set<DataTableMetadata> dtm = new HashSet<DataTableMetadata>();
            boolean bAttachable = true;

            if (ci.contains(CollectedInfo.DEADLOCKS) || ci.contains(CollectedInfo.DATARACES)) {
                ci.retainAll(EnumSet.of(CollectedInfo.DEADLOCKS, CollectedInfo.DATARACES));
            }

            for (CollectedInfo info : collectedInfo) {
                ci.add(info);

                switch (info) {
                    case FUNCTIONS_LIST:
                        dtm.add(cpuInfoTable);
                        break;
                    case MEMORY:
                        dtm.add(memInfoTable);
                        bAttachable = false;
                        break;
                    case MEMSUMMARY:
                        dtm.add(memSummaryInfoTable);
                        bAttachable = false;
                        break;
                    case SYNCHRONIZATION:
                        dtm.add(syncInfoTable);
                        bAttachable = false;
                        break;
                    case SYNCSUMMARY:
                        dtm.add(summaryInfoTable);
                        bAttachable = false;
                        break;
                    case DEADLOCKS:
                        dtm.add(deadlocksSummaryInfoTable);
                        bAttachable = false;
                        break;
                    case DATARACES:
                        dtm.add(dataracesSummaryInfoTable);
                        bAttachable = false;
                        break;
                }
            }

            collectedInfo.clear();
            collectedInfo.addAll(ci);
            dataTablesMetadata.clear();
            dataTablesMetadata.addAll(dtm);
            isAttachable = bAttachable;
        }
    }

    public void init(final Map<DataStorageType, DataStorage> storages, final DLightTarget target) {
        synchronized (lock) {
            DataStorage storage = storages.get(supportedStorageType);
            if (!(storage instanceof PerfanDataStorage)) {
                throw new IllegalArgumentException("Storage " + // NOI18N
                        storage + " cannot be used for PerfanDataCollector!"); // NOI18N
            }

            DLightLogger.assertTrue(validatedTarget == target,
                    "Validation was performed against another target"); // NOI18N

            String experimentDir = hostInfo.getTempDir() + "/experiment_" + uid.incrementAndGet() + ".er"; // NOI18N

            config = new CollectorConfiguration(
                    (PerfanDataStorage) storage,
                    target, target.getExecEnv(),
                    experimentDir, sproHome, collectedInfo);

            reinit();
        }
    }

    public List<DataFilter> getDataFilters() {
        return dataFilters;
    }

    private void reinit() {
        boolean result = true;

        synchronized (lock) {
            try {
                result = prepareExperimentDirectory(
                        config.execEnv,
                        config.experimentDirectory);

                // Init storage (i.e. er_print, actually)
                if (result) {
                    config.dataStorage.init(
                            config.execEnv,
                            config.sproHome,
                            config.experimentDirectory,
                            this);

                    // In case when summary data was requested do init
                    // periodic SummaryDataFetchingTask ...
                    monitorsUpdater = new MonitorsUpdateService(SunStudioDataCollector.this,
                            config.execEnv,
                            config.sproHome,
                            config.experimentDirectory,
                            config.collectedInfo);
                }
            } catch (Throwable ex) {
                result = false;
            }
        }
    }

    public boolean isAttachable() {
        synchronized (lock) {
            return isAttachable;
        }
    }

    public String getCmd() {
        synchronized (lock) {
            return cmd;
        }
    }

    public String[] getArgs() {
        synchronized (lock) {
            if (cmd == null) {
                throw new IllegalStateException(
                        "Args can be retrieved for validated and valid collector only"); // NOI18N
            }

            List<String> args = new ArrayList<String>();

            // Disregard collect's output...
            if (!log.isLoggable(Level.FINEST)) {
                args.add("-O"); // NOI18N
                args.add("/dev/null"); // NOI18N
            }

            final boolean deadlocks = collectedInfo.contains(CollectedInfo.DEADLOCKS);
            //try to find THAFilter
            THAFilter thaFilter = null;
            THAStartupFilter thaStartupFilter = null;
            for (DataFilter dataFilter : dataFilters){
                if (dataFilter instanceof THAFilter){
                    thaFilter = (THAFilter)dataFilter;
                }else if (dataFilter instanceof THAStartupFilter){
                    thaStartupFilter = (THAStartupFilter)dataFilter;
                }
            }
            final boolean dataraces = collectedInfo.contains(CollectedInfo.DATARACES) && (thaFilter == null || thaFilter.getType().equals(THAFilter.CollectedDataType.DATARACES));
            if (deadlocks || dataraces) {
                args.add("-r"); // NOI18N

                if (deadlocks && dataraces) {
                    args.add("deadlocks,races"); // NOI18N
                } else if (deadlocks) {
                    args.add("deadlocks"); // NOI18N
                } else if (dataraces) {
                    args.add("races"); // NOI18N
                }

                args.add("-y"); // NOI18N
                if (thaStartupFilter != null && thaStartupFilter.getStartMode() == THAStartupFilter.StartMode.STARTUP){
                    args.add("USR1,r");// NOI18N
                }else{
                    args.add("USR1"); // NOI18N
                }
            } else {

                if (collectedInfo.contains(CollectedInfo.SYNCHRONIZATION) ||
                        collectedInfo.contains(CollectedInfo.SYNCSUMMARY)) {
                    args.add("-s"); // NOI18N
                    args.add("1000"); // NOI18N
                }

                if (collectedInfo.contains(CollectedInfo.MEMORY) ||
                        collectedInfo.contains(CollectedInfo.MEMSUMMARY)) {
                    args.add("-H"); // NOI18N
                    args.add("on"); // NOI18N
                }
            }

            args.add("-o"); // NOI18N
            args.add(config.experimentDirectory);

            return args.toArray(new String[0]);
        }
    }

    public String getName() {
        return COLLECTOR_NAME;
    }

    private void targetFinished(DLightTarget source) {
        synchronized (lock) {
            log.fine("Stopping PerfanDataCollector: " + cmd); // NOI18N

            if (isAttachable()) {
                // i.e. separate process
                if (collectTaskResult != null) {
                    collectTaskResult.cancel(true);
                }
            } else {
                // i.e. this means that exactly this collector finished
                // do nothing
            }

            collectTaskResult = null;

            if (monitorsUpdater != null) {
                monitorsUpdater.stop();
            }
        }
    }

    protected void updateIndicators(List<DataRow> data) {
        this.notifyIndicators(data);
    }

    private void targetStarted(DLightTarget source) {
        synchronized (lock) {
            if (source != config.target) {
                return;
            }

            if (isAttachable()) {
                // i.e. should start separate process
                AttachableTarget at = (AttachableTarget) config.target;
                NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(config.execEnv);
                npb.setExecutable(cmd);
                npb.setArguments("-P", "" + at.getPID(), "-o", config.experimentDirectory); // NOI18N

                ExecutionDescriptor descr = new ExecutionDescriptor();
                descr = descr.errProcessorFactory(new StdErrRedirectorFactory());
                descr = descr.outProcessorFactory(new StdErrRedirectorFactory());
                descr = descr.inputOutput(InputOutput.NULL);

                ExecutionService service = ExecutionService.newService(npb, descr, "collect"); // NOI18N
                collectTaskResult = service.run();
            }

            if (monitorsUpdater != null) {
                monitorsUpdater.start();
            }
        }
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(SunStudioDataCollector.class, key, params);
    }

    public void dataFiltersChanged(List<DataFilter> newSet) {
        synchronized (dataFilters) {
            dataFilters.clear();

            for (DataFilter filter : newSet) {
                if (filter instanceof CollectedObjectsFilter) {
                    dataFilters.add(filter);
                }else if (filter instanceof THAFilter){
                    dataFilters.add(filter);
                }else if (filter instanceof THAStartupFilter){
                    dataFilters.add(filter);
                }
            }
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

    private boolean prepareExperimentDirectory(ExecutionEnvironment execEnv, String experimentDirectory) {
        log.fine("Prepare PerfanDataCollector. Clean directory " + experimentDirectory); // NOI18N
        boolean result = true;

        try {
            Future<Integer> rmFuture;
            Integer rmResult = null;

            rmFuture = CommonTasksSupport.rmDir(execEnv, experimentDirectory, true, null);
            rmResult = rmFuture.get();

            if (rmResult == null || rmResult.intValue() != 0) {
                log.info("SunStudioDataCollector: unable to delete directory " // NOI18N
                        + execEnv.toString() + ":" + experimentDirectory); // NOI18N
                result = false;
            }

            if (result) {
                // Try to remove _collector_directory_lock as well...
                // To make things simple - just do not look at the result of
                // this operation (it will fail if lock file doesn't exist)
                File lockFile = new File(new File(experimentDirectory).getParentFile(), "_collector_directory_lock"); // NOI18N
                rmFuture = CommonTasksSupport.rmFile(execEnv, lockFile.getPath(), null);
                rmResult = rmFuture.get();
            }
        } catch (Throwable th) {
            result = false;
        }

        if (!result) {
            log.severe("Unable to prepare an experiment directory!"); // NOI18N
        }

        return result;
    }

    // Immutable configuration.
    // It is created once in init()
    // After that calls to other methods doesn't affect this configuration
    // that is used to start collector.
    private static class CollectorConfiguration {

        final PerfanDataStorage dataStorage;
        final DLightTarget target;
        final ExecutionEnvironment execEnv;
        final String experimentDirectory;
        final String sproHome;
        final Set<CollectedInfo> collectedInfo;

        public CollectorConfiguration(
                final PerfanDataStorage dataStorage,
                final DLightTarget target,
                final ExecutionEnvironment execEnv,
                final String experimentDirectory,
                final String sproHome,
                final Set<CollectedInfo> collectedInfo) {

            this.target = target;
            this.dataStorage = dataStorage;
            this.execEnv = execEnv;
            this.experimentDirectory = experimentDirectory;
            this.sproHome = sproHome;
            this.collectedInfo = Collections.unmodifiableSet(EnumSet.copyOf(collectedInfo));
        }
    }
}
