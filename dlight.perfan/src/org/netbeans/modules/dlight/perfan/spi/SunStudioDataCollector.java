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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
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
import org.netbeans.modules.dlight.perfan.storage.impl.PerfanDataStorage;
import org.netbeans.modules.dlight.spi.SunStudioLocator.SunStudioDescription;
import org.netbeans.modules.dlight.spi.SunStudioLocatorFactory;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.AsynchronousAction;
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
        implements DataCollector<SunStudioDCConfiguration> {

    private static final String ID = "PerfanDataStorage"; // NOI18N
    private static final String COLLECTOR_NAME = "SunStudio"; // NOI18N
    private static final List<DataStorageType> supportedStorageTypes;
    private static final AtomicInteger uid = new AtomicInteger(0);
    private static final Logger log = DLightLogger.getLogger(SunStudioDataCollector.class);
    // Below is COMPLETE list of DataTableMetadata objects...
    private static final DataTableMetadata cpuInfoTable;
    private static final DataTableMetadata syncInfoTable;
    private static final DataTableMetadata memInfoTable;
    private static final DataTableMetadata summaryInfoTable;
    private static final DataTableMetadata memSummaryInfoTable;

    // ***
    private String experimentDir;
    private final Object lock = new String(SunStudioDataCollector.class.getName());
    // ***
    private final Collection<DataTableMetadata> dataTablesMetadata;
    private final Collection<ValidationListener> validationListeners;
    private final Collection<CollectedInfo> collectedInfo;
    // ***
    private ValidationStatus validationStatus = ValidationStatus.initialStatus();
    private Future<Integer> collectTaskResult = null;
    private Future<Boolean> warmUpTaskResult = null;
    private MonitorsUpdateService monitorsUpdater = null;
    private PerfanDataStorage storage = null;
    private String cmd;
    private String sproHome;
    private DLightTarget target;
    private boolean isAttachable;


    static {
        supportedStorageTypes = Arrays.asList(
                DataStorageTypeFactory.getInstance().getDataStorageType(ID));

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

        memInfoTable = new DataTableMetadata("SunStudioMemDetailedData", // NOI18N
                Arrays.asList(SunStudioDCConfiguration.c_name,
                SunStudioDCConfiguration.c_leakCount,
                SunStudioDCConfiguration.c_leakSize));

        summaryInfoTable = new DataTableMetadata(
                "SunStudioSummaryData", // NOI18N
                Arrays.asList(SunStudioDCConfiguration.c_ulockSummary));

        memSummaryInfoTable = new DataTableMetadata(
                "SunStudioMemorySummaryData", // NOI18N
                Arrays.asList(SunStudioDCConfiguration.c_leakSize));

    }

    public SunStudioDataCollector(List<CollectedInfo> collectedInfoList) {
        collectedInfo = new HashSet<CollectedInfo>();
        dataTablesMetadata = new HashSet<DataTableMetadata>();
        validationListeners = new CopyOnWriteArraySet<ValidationListener>();
        isAttachable = true;
        addCollectedInfo(collectedInfoList);
    }

    void addCollectedInfo(final List<CollectedInfo> collectedInfoList) {
        Set<CollectedInfo> ci = new HashSet<CollectedInfo>();
        Set<DataTableMetadata> dtm = new HashSet<DataTableMetadata>();
        boolean bAttachable = true;

        for (CollectedInfo info : collectedInfoList) {
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
            }
        }

        synchronized (lock) {
            collectedInfo.addAll(ci);
            dataTablesMetadata.addAll(dtm);
            isAttachable &= bAttachable;
        }
    }

    public void targetStateChanged(DLightTargetChangeEvent event) {
        switch (event.state) {
            case STARTING:
                if (warmUpTaskResult == null) {
                    // This means that re-starting occured
                    startWarmUp();
                }
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

            this.target = target;

            ExecutionEnvironment execEnv = target.getExecEnv();
            HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv, true);

            switch (hostInfo.getOSFamily()) {
                case LINUX:
                case SUNOS:
                    break;
                default:
                    validationStatus = ValidationStatus.invalidStatus(
                            "SunStudioDataCollector works on SunOS or Linux only."); // NOI18N
                    return validationStatus;
            }

            String command = null;
            String sprohome = null;

            try {
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
                    validationStatus = ValidationStatus.invalidStatus("No SunStudio Found, use link http://developers.sun.com/sunstudio/ to download latest SunStudio"); //NOI18N
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

            validationStatus = ValidationStatus.validStatus();
            cmd = command;
            sproHome = sprohome;
            return validationStatus;
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

    public void addValidationListener(ValidationListener listener) {
        validationListeners.add(listener);
    }

    public void removeValidationListener(ValidationListener listener) {
        validationListeners.remove(listener);
    }

    public Collection<DataStorageType> getSupportedDataStorageTypes() {
        return supportedStorageTypes;
    }

    public List<DataTableMetadata> getDataTablesMetadata() {
        synchronized (lock) {
            return Collections.unmodifiableList(
                    new ArrayList<DataTableMetadata>(dataTablesMetadata));
        }
    }

    public void init(DataStorage dataStorage, DLightTarget target) {
        synchronized (lock) {
            if (!(dataStorage instanceof PerfanDataStorage)) {
                throw new IllegalArgumentException("Storage " + // NOI18N
                        dataStorage + " cannot be used for PerfanDataCollector!"); // NOI18N
            }

            DLightLogger.assertTrue(this.target == target,
                    "Validation was performed against another target"); // NOI18N

            String tmpDirBase = HostInfoUtils.getHostInfo(target.getExecEnv(), true).getTempDir();
            this.experimentDir = tmpDirBase + "/experiment_" + uid.incrementAndGet() + ".er"; // NOI18N
            this.storage = (PerfanDataStorage) dataStorage;

            startWarmUp();

            // Init storage (i.e. er_print, actually)
            storage.init(target.getExecEnv(), sproHome, experimentDir);

            // In case when summary data was requested do init
            // periodic SummaryDataFetchingTask ...
            monitorsUpdater = new MonitorsUpdateService(this,
                    target.getExecEnv(), sproHome, experimentDir, collectedInfo);
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

            if (collectedInfo.contains(CollectedInfo.SYNCHRONIZATION) ||
                    collectedInfo.contains(CollectedInfo.SYNCSUMMARY)) {
                args.add("-s"); // NOI18N
                args.add("30"); // NOI18N
            }

            if (collectedInfo.contains(CollectedInfo.MEMORY) ||
                    collectedInfo.contains(CollectedInfo.MEMSUMMARY)) {
                args.add("-H"); // NOI18N
                args.add("on"); // NOI18N
            }

            args.add("-o"); // NOI18N
            args.add(experimentDir);

            return args.toArray(new String[0]);
        }
    }

    public String getName() {
        return COLLECTOR_NAME;
    }

    private void startWarmUp() {
        synchronized (lock) {
            if (warmUpTaskResult != null && !warmUpTaskResult.isDone()) {
                warmUpTaskResult.cancel(true);
            }

            warmUpTaskResult = DLightExecutorService.submit(
                    new SSDCWarmUpTask(target.getExecEnv(), experimentDir),
                    "Warming SunStudioDataCollector up"); // NOI18N
        }
    }

    private void targetFinished(DLightTarget source) {
        synchronized (lock) {
            if (warmUpTaskResult != null && !warmUpTaskResult.isDone()) {
                warmUpTaskResult.cancel(true);
                warmUpTaskResult = null;
            }

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
            monitorsUpdater.stop();
        }
    }

    protected void updateIndicators(List<DataRow> data) {
        this.notifyIndicators(data);
    }

    private void targetStarted(DLightTarget source) {
        synchronized (lock) {
            if (source != target) {
                return;
            }

            // Wait for warm-up task completion ...
            boolean warmUpStatus = false;

            try {
                // warmUpTaskResult may be null if invoke init against wrong target
                // (not one that was validated)
                warmUpStatus = warmUpTaskResult == null
                        ? false
                        : warmUpTaskResult.get().booleanValue();
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
            warmUpTaskResult = null;

            if (!warmUpStatus) {
                log.fine("Will not start SunStudioDataCollector because warm-up task failed"); // NOI18N
                return;
            }

            if (isAttachable()) {
                // i.e. should start separate process
                AttachableTarget at = (AttachableTarget) target;
                NativeProcessBuilder npb = new NativeProcessBuilder(target.getExecEnv(), cmd);
                npb = npb.setArguments("-P", "" + at.getPID(), "-o", experimentDir); // NOI18N

                ExecutionDescriptor descr = new ExecutionDescriptor();
                descr = descr.errProcessorFactory(new StdErrRedirectorFactory());
                descr = descr.outProcessorFactory(new StdErrRedirectorFactory());
                descr = descr.inputOutput(InputOutput.NULL);

                ExecutionService service = ExecutionService.newService(npb, descr, "collect"); // NOI18N
                collectTaskResult = service.run();
            }

            monitorsUpdater.start();
        }
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(SunStudioDataCollector.class, key, params);
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
}
