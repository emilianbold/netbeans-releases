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
package org.netbeans.modules.dlight.collector.stdout.spi;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
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
import org.netbeans.modules.dlight.api.execution.ValidationStatus;
import org.netbeans.modules.dlight.api.execution.ValidationListener;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.collector.stdout.CLIODCConfiguration;
import org.netbeans.modules.dlight.collector.stdout.CLIOParser;
import org.netbeans.modules.dlight.collector.stdout.impl.CLIODCConfigurationAccessor;
import org.netbeans.modules.dlight.management.api.DLightManager;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.impl.SQLDataStorage;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.AsynchronousAction;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * Command Line output data collector.
 * Implements both {@link org.netbeans.modules.dlight.spi.collectorDataCollector}
 * and {@link org.netbeans.modules.dlight.spi.collector.DataCollector} via
 * invocation of a command-line tool and parsing its output.
 */
public final class CLIODataCollector
        extends IndicatorDataProvider<CLIODCConfiguration>
        implements DataCollector<CLIODCConfiguration>, DLightTarget.ExecutionEnvVariablesProvider {

    private static final Logger log =
            DLightLogger.getLogger(CLIODataCollector.class);
    private String command;
    private final Map<String, String> envs;
    private String argsTemplate;
    private DataStorage storage;
    private String displayedName;
    private Future<Integer> collectorTask;
    private CLIOParser parser;
    private List<DataTableMetadata> dataTablesMetadata;
    private ValidationStatus validationStatus = ValidationStatus.initialStatus();
    private List<ValidationListener> validationListeners =
            Collections.synchronizedList(new ArrayList<ValidationListener>());

    /**
     *
     * @param command command to invoke (without arguments)
     * @param arguments command arguments
     * @param parser a {@link org.netbeans.modules.dlight.collector.stdout.CLIOParser}
     * to parse command output with
     * @param dataTablesMetadata describes the tables to store parsed data in
     */
    CLIODataCollector(CLIODCConfiguration configuration) {
        CLIODCConfigurationAccessor accessor =
                CLIODCConfigurationAccessor.getDefault();

        this.command = accessor.getCommand(configuration);
        this.argsTemplate = accessor.getArguments(configuration);
        this.parser = accessor.getParser(configuration);
        this.dataTablesMetadata = accessor.getDataTablesMetadata(configuration);
        this.envs = accessor.getDLightTargetExecutionEnv(configuration);
        this.displayedName = accessor.getName(configuration);
        if (displayedName == null) {
            //lets create own name on the base of command
            int separatorIndex = this.command.lastIndexOf(File.separator);
            displayedName = separatorIndex == -1 || separatorIndex == command.length() - 1 ? command : this.command.substring(separatorIndex + 1);
        }
    }

    public String getName() {
        return displayedName;
    }

    /**
     * The types of storage this collector supports
     * @return returns list of {@link org.netbeans.modules.dlight.core.storage.model.DataStorageType}
     * data collector can put data into
     */
    public Collection<DataStorageType> getRequiredDataStorageTypes() {
        DataStorageTypeFactory dstf = DataStorageTypeFactory.getInstance();

        return Arrays.asList(
                dstf.getDataStorageType(SQLDataStorage.SQL_DATA_STORAGE_TYPE));
    }

    public void init(Map<DataStorageType, DataStorage> storages, DLightTarget target) {
        DataStorageTypeFactory dstf = DataStorageTypeFactory.getInstance();
        this.storage = storages.get(dstf.getDataStorageType(SQLDataStorage.SQL_DATA_STORAGE_TYPE));
        log.fine("Do INIT for " + storage.toString()); // NOI18N
    }

    protected void processLine(String line) {
        DataRow dataRow = parser.process(line);
        if (dataRow != null) {
            if (dataTablesMetadata != null &&
                    !dataTablesMetadata.isEmpty() &&
                    storage != null) {

                storage.addData(
                        dataTablesMetadata.iterator().next().getName(),
                        Arrays.asList(dataRow));
            }

            notifyIndicators(Arrays.asList(dataRow));
        }
    }

    private void targetStarted(DLightTarget target) {
        log.fine("Starting CLIODataCollector: " + command); // NOI18N
        resetIndicators();

        String cmd = command + " "; // NOI18N

        if (target instanceof AttachableTarget) {
            AttachableTarget at = (AttachableTarget) target;
            cmd += argsTemplate.replaceAll("@PID", "" + at.getPID()); // NOI18N
        } else {
            cmd += argsTemplate;
        }
        log.fine("Starting CLIODataCollector cmd: " + cmd); // NOI18N
        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(target.getExecEnv());
        npb.setCommandLine(cmd);

        ExecutionDescriptor descriptor =
                new ExecutionDescriptor().inputOutput(
                InputOutput.NULL).outProcessorFactory(
                new CLIOInputProcessorFactory()).errProcessorFactory(new CLIOInputProcessorFactory());

        ExecutionService execService = ExecutionService.newService(
                npb, descriptor, "CLIODataCollector " + cmd); // NOI18N

        collectorTask = execService.run();
    }

    private void targetFinished(DLightTarget target) {
        if (collectorTask != null && !collectorTask.isDone()) {
            // It could be already done here, because tracked process is
            // finished and, depending on command-line utility, it may exit as
            // well... But, if not - terminate it.
            log.fine("Stopping CLIODataCollector: " + collectorTask.toString()); // NOI18N
            collectorTask.cancel(true);
            collectorTask = null;
        }
    }

    /** {@inheritDoc */
    public List<DataTableMetadata> getDataTablesMetadata() {
        return dataTablesMetadata;
    }

    /** {@inheritDoc */
    public boolean isAttachable() {
        return true;
    }

    /** {@inheritDoc */
    public String getCmd() {
        return command;
    }

    /** {@inheritDoc */
    public String[] getArgs() {
        return null;
    }

    /**
     * Registers a validation listener
     * @param listener a validation listener to add
     */
    public void addValidationListener(ValidationListener listener) {
        if (!validationListeners.contains(listener)) {
            validationListeners.add(listener);
        }
    }

    /**
     * Removes a validation listener
     * @param listener a listener to remove
     */
    public void removeValidationListener(ValidationListener listener) {
        validationListeners.remove(listener);
    }

    protected void notifyStatusChanged(
            final ValidationStatus oldStatus,
            final ValidationStatus newStatus) {

        if (oldStatus.equals(newStatus)) {
            return;
        }

        for (ValidationListener validationListener : validationListeners) {
            validationListener.validationStateChanged(this, oldStatus, newStatus);
        }
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(CLIODataCollector.class, key, params);
    }

    public ValidationStatus validate(final DLightTarget target) {
        if (validationStatus.isValid()) {
            return validationStatus;
        }

        ValidationStatus oldStatus = validationStatus;
        ValidationStatus newStatus = doValidation(target);

        notifyStatusChanged(oldStatus, newStatus);

        validationStatus = newStatus;
        return newStatus;
    }

    public void invalidate() {
        validationStatus = ValidationStatus.initialStatus();
    }

    private ValidationStatus doValidation(final DLightTarget target) {
        DLightLogger.assertNonUiThread();

        ValidationStatus result = null;
        boolean fileExists = false;
        boolean connected = true;
        final ExecutionEnvironment execEnv = target.getExecEnv();
        String error = ""; // NOI18N

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

        return result;
    }

    public ValidationStatus getValidationStatus() {
        return validationStatus;
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
    }

    @Override
    public void setupEnvironment(DLightTarget target, MacroMap env) {
        env.putAll(envs);
    }

    public void dataFiltersChanged(List<DataFilter> newSet, boolean isAdjusting) {
    }

    private class CLIOInputProcessorFactory implements InputProcessorFactory {

        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return InputProcessors.bridge(new LineProcessor() {

                @Override
                public void processLine(String line) {
                    CLIODataCollector.this.processLine(line);
                }

                public void reset() {
                }

                public void close() {
                }
            });
        }
    }
}
