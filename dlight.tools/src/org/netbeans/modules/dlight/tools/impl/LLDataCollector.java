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

import org.netbeans.modules.dlight.api.execution.DLightTargetChangeEvent;
import org.netbeans.modules.dlight.tools.*;
import java.io.File;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.dlight.api.execution.AttachableTarget;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.DLightTarget.ExecutionEnvVariablesProvider;
import org.netbeans.modules.dlight.api.execution.ValidationListener;
import org.netbeans.modules.dlight.api.execution.ValidationStatus;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.impl.SQLDataStorage;
import org.netbeans.modules.dlight.management.api.DLightManager;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.AsynchronousAction;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * @author Alexey Vladykin
 */
public class LLDataCollector
        extends IndicatorDataProvider<LLDataCollectorConfiguration>
        implements DataCollector<LLDataCollectorConfiguration>,
        ExecutionEnvVariablesProvider {

    private EnumSet<LLDataCollectorConfiguration.CollectedData> collectedData;
    private DLightTarget target;
    private ValidationStatus validationStatus;
    private List<ValidationListener> validationListeners;

    public LLDataCollector(LLDataCollectorConfiguration configuration) {
        collectedData = EnumSet.of(LLDataCollectorConfigurationAccessor.getDefault().getCollectedData(configuration));
        validationStatus = ValidationStatus.initialStatus();
        validationListeners = new ArrayList<ValidationListener>();
    }

    public void addConfiguration(LLDataCollectorConfiguration configuration) {
        collectedData.add(LLDataCollectorConfigurationAccessor.getDefault().getCollectedData(configuration));
    }

    public String getName() {
        return "LLTool"; // NOI18N
    }

    public List<DataTableMetadata> getDataTablesMetadata() {
        List<DataTableMetadata> tables = new ArrayList<DataTableMetadata>();
        if (collectedData.contains(LLDataCollectorConfiguration.CollectedData.CPU)) {
            tables.add(LLDataCollectorConfiguration.CPU_TABLE);
        }
        if (collectedData.contains(LLDataCollectorConfiguration.CollectedData.MEM)) {
            tables.add(LLDataCollectorConfiguration.MEM_TABLE);
        }
        if (collectedData.contains(LLDataCollectorConfiguration.CollectedData.SYNC)) {
            tables.add(LLDataCollectorConfiguration.SYNC_TABLE);
        }
        return tables;
    }

    public Collection<DataStorageType> getSupportedDataStorageTypes() {
        return Arrays.<DataStorageType>asList(DataStorageTypeFactory.getInstance().getDataStorageType(SQLDataStorage.SQL_DATA_STORAGE_TYPE));
    }

    public void init(DataStorage storage, DLightTarget target) {
        this.target = target;
        ExecutionEnvironment env = target.getExecEnv();
        if (!env.isLocal()) {
            try {
                File agentLibraryLocal = locateAgentLibrary(env);
                if (agentLibraryLocal != null) {
                    CommonTasksSupport.uploadFile(
                            agentLibraryLocal.getAbsolutePath(), env,
                            getRemotePath(env, agentLibraryLocal), 0644, null).get();
                }
                File monitorExecutableLocal = locateMonitorExecutable(env);
                CommonTasksSupport.uploadFile(
                        monitorExecutableLocal.getAbsolutePath(), env,
                        getRemotePath(env, monitorExecutableLocal), 0755, null).get();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
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

    public Map<String, String> getExecutionEnv(DLightTarget target) throws ConnectException {
        ExecutionEnvironment env = target.getExecEnv();
        String ldPreloadName = NativeToolsUtil.getLdPreloadName(HostInfoUtils.getOS(env));
        File agentLibraryLocal = locateAgentLibrary(env);
        if (agentLibraryLocal != null) {
            return Collections.singletonMap(ldPreloadName, getRemotePath(env, agentLibraryLocal));
        } else {
            return Collections.emptyMap();
        }
    }

    private String getRemotePath(ExecutionEnvironment env, File localPath) {
        if (env.isLocal()) {
            return localPath.getAbsolutePath();
        } else {
            try{
                return HostInfoUtils.getTempDir(env) + localPath.getName();
            }catch(ConnectException ex){

            }
            return "/var/tmp/" + localPath.getName(); // NOI18N
        }
    }

    private File locateAgentLibrary(ExecutionEnvironment env) {
        return NativeToolsUtil.locateFile(env, NativeToolsUtil.getSharedLibrary("prof_agent")); // NOI18N
    }

    private File locateMonitorExecutable(ExecutionEnvironment env) {
        return NativeToolsUtil.locateFile(env, NativeToolsUtil.getExecutable("prof_monitor")); // NOI18N
    }

    public void targetStateChanged(DLightTargetChangeEvent event) {
        switch (event.state) {
            case RUNNING:
                startMonitor();
                break;
        }
    }

    private void startMonitor() {
        AttachableTarget at = (AttachableTarget) target;
        ExecutionEnvironment env = target.getExecEnv();
        NativeProcessBuilder npb = new NativeProcessBuilder(env, getRemotePath(env, locateMonitorExecutable(env)));
        StringBuilder flags = new StringBuilder("-"); // NOI18N
        if (collectedData.contains(LLDataCollectorConfiguration.CollectedData.CPU)) {
            flags.append('c'); // NOI18N
        }
        if (collectedData.contains(LLDataCollectorConfiguration.CollectedData.MEM)) {
            flags.append('m'); // NOI18N
        }
        if (collectedData.contains(LLDataCollectorConfiguration.CollectedData.SYNC)) {
            flags.append('s'); // NOI18N
        }
        npb = npb.setArguments(flags.toString(), String.valueOf(at.getPID()));

        ExecutionDescriptor descr = new ExecutionDescriptor();
        descr = descr.outProcessorFactory(new ExecutionDescriptor.InputProcessorFactory() {

            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                return InputProcessors.bridge(new MonitorOutputProcessor());
            }
        });
        descr = descr.inputOutput(InputOutput.NULL);

        ExecutionService service = ExecutionService.newService(npb, descr, "monitor"); // NOI18N
        service.run();
    }

    private class MonitorOutputProcessor implements LineProcessor {

        private float syncPrev;

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
                int threads = Integer.parseInt(fields[1]);
                row = new DataRow(LLDataCollectorConfiguration.SYNC_TABLE.getColumnNames(), Arrays.asList(Float.valueOf((syncCurr - syncPrev) * 100 / threads), Integer.valueOf(threads)));
                syncPrev = syncCurr;
            }
            if (row != null) {
                notifyIndicators(Collections.singletonList(row));
            }
        }

        public void reset() {
        }

        public void close() {
        }
    }

// validation stuff ////////////////////////////////////////////////////////////
    public ValidationStatus validate(final DLightTarget objectToValidate) {
        if (validationStatus.isValid()) {
            return validationStatus;
        }

        ValidationStatus oldStatus = validationStatus;
        ValidationStatus newStatus = doValidation(objectToValidate);

        notifyStatusChanged(oldStatus, newStatus);

        validationStatus = newStatus;
        return newStatus;
    }

    public void invalidate() {
        validationStatus = ValidationStatus.initialStatus();
    }

    public ValidationStatus getValidationStatus() {
        return validationStatus;
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

        File agentLibraryLocal = locateAgentLibrary(env);
        if (agentLibraryLocal == null || !agentLibraryLocal.exists()) {
            return ValidationStatus.invalidStatus(getMessage("ValidationStatus.AgentNotFound")); // NOI18N
        }

        File monitorExecutableLocal = locateMonitorExecutable(env);
        if (monitorExecutableLocal == null || !monitorExecutableLocal.exists()) {
            return ValidationStatus.invalidStatus(getMessage("ValidationStatus.MonitorNotFound")); // NOI18N
        }

        return ValidationStatus.validStatus();
    }

    private void notifyStatusChanged(ValidationStatus oldStatus, ValidationStatus newStatus) {
        if (!oldStatus.equals(newStatus)) {
            for (ValidationListener vl : validationListeners) {
                vl.validationStateChanged(this, oldStatus, newStatus);
            }
        }
    }

    public void addValidationListener(ValidationListener listener) {
        if (!validationListeners.contains(listener)) {
            validationListeners.add(listener);
        }
    }

    public void removeValidationListener(ValidationListener listener) {
        validationListeners.remove(listener);
    }

    private static String getMessage(String key) {
        return NbBundle.getMessage(LLDataCollector.class, key);
    }
}
