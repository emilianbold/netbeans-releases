/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.collector.stdout.spi;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import org.netbeans.modules.dlight.api.execution.ValidationStatus;
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
import org.netbeans.modules.dlight.spi.collector.DataCollectorListener;
import org.netbeans.modules.dlight.spi.collector.DataCollectorListenersSupport;
import org.netbeans.modules.dlight.util.DLightExecutorService;
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

    private static final Logger log = DLightLogger.getLogger(CLIODataCollector.class);
    private String command;
    private final Map<String, String> envs;
    private String argsTemplate;
    private DataStorage storage;
    private Future<Integer> collectorTask;
    private CLIOParser parser;
    private final DataStorageType dataStorageType;
    private final String firstTableName;
    private final List<DataTableMetadata> metadata;
    private final DataCollectorListenersSupport dclsupport = new DataCollectorListenersSupport(this);

    /**
     *
     * @param command command to invoke (without arguments)
     * @param arguments command arguments
     * @param parser a {@link org.netbeans.modules.dlight.collector.stdout.CLIOParser}
     * to parse command output with
     * @param dataTablesMetadata describes the tables to store parsed data in
     */
    CLIODataCollector(CLIODCConfiguration configuration) {
        super(constructName(configuration));

        CLIODCConfigurationAccessor accessor =
                CLIODCConfigurationAccessor.getDefault();

        this.command = accessor.getCommand(configuration);
        this.argsTemplate = accessor.getArguments(configuration);
        this.parser = accessor.getParser(configuration);
        this.envs = accessor.getDLightTargetExecutionEnv(configuration);
        this.dataStorageType = accessor.getDataStorageType(configuration);

        metadata = Collections.unmodifiableList(accessor.getDataTablesMetadata(configuration));

        if (metadata != null && !metadata.isEmpty()) {
            firstTableName = metadata.get(0).getName();
        } else {
            firstTableName = null;
        }
    }
    
    @Override
    public final void addDataCollectorListener(DataCollectorListener listener) {
        dclsupport.addListener(listener);
    }

    @Override
    public final void removeDataCollectorListener(DataCollectorListener listener) {
        dclsupport.removeListener(listener);
    }

    protected final void notifyListeners(final CollectorState state) {
        dclsupport.notifyListeners(state);
    }

    private static String constructName(CLIODCConfiguration configuration) {
        String cmd = CLIODCConfigurationAccessor.getDefault().getCommand(configuration);
        int separatorIndex = cmd.lastIndexOf(File.separator);
        return separatorIndex == -1 || separatorIndex == cmd.length() - 1 ? cmd : cmd.substring(separatorIndex + 1);
    }

    /**
     * The types of storage this collector supports
     * @return returns list of {@link org.netbeans.modules.dlight.core.storage.model.DataStorageType}
     * data collector can put data into
     */
    @Override
    public Collection<DataStorageType> getRequiredDataStorageTypes() {
        return Arrays.asList(dataStorageType);
    }

    @Override
    public void init(Map<DataStorageType, DataStorage> storages, DLightTarget target) {
        DataStorageTypeFactory dstf = DataStorageTypeFactory.getInstance();
        this.storage = storages.get(dataStorageType);
        log.log(Level.FINE, "Do INIT for {0}", storage.toString()); // NOI18N
    }

    protected void processLine(String line) {
        DataRow dataRow = parser.process(line);
        if (dataRow != null) {
            if (firstTableName != null && storage != null) {
                storage.addData(firstTableName, Arrays.asList(dataRow));
            }

            notifyIndicators(Arrays.asList(dataRow));
        }
    }

    @Override
    protected void targetStarted(DLightTarget target) {
        log.log(Level.FINE, "Starting CLIODataCollector: {0}", command); // NOI18N
        resetIndicators();

        String cmd = command + " "; // NOI18N

        if (target instanceof AttachableTarget) {
            AttachableTarget at = (AttachableTarget) target;
            cmd += argsTemplate.replaceAll("@PID", "" + at.getPID()); // NOI18N
        } else {
            cmd += argsTemplate;
        }
        log.log(Level.FINE, "Starting CLIODataCollector cmd: {0}", cmd); // NOI18N
        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(target.getExecEnv());
        npb.setCommandLine(cmd);

        ExecutionDescriptor descriptor =
                new ExecutionDescriptor().inputOutput(
                InputOutput.NULL).outProcessorFactory(
                new CLIOInputProcessorFactory()).errProcessorFactory(new CLIOInputProcessorFactory());

        ExecutionService execService = ExecutionService.newService(
                npb, descriptor, "CLIODataCollector " + cmd); // NOI18N

        collectorTask = execService.run();

        DLightExecutorService.submit(new Runnable() {

            @Override
            public void run() {
                notifyListeners(CollectorState.RUNNING);
                try {
                    collectorTask.get();
                } catch (InterruptedException ex) {
                    notifyListeners(CollectorState.TERMINATED);
                    return;
                } catch (ExecutionException ex) {
                    notifyListeners(CollectorState.TERMINATED);
                    return;
                } catch (CancellationException ex) {
                    notifyListeners(CollectorState.TERMINATED);
                    return;
                }
                notifyListeners(CollectorState.STOPPED);

            }
        }, "Listen for the CLIO task");//NOI18N
    }

    @Override
    protected void targetFinished(DLightTarget target) {
        if (collectorTask != null && !collectorTask.isDone()) {
            // It could be already done here, because tracked process is
            // finished and, depending on command-line utility, it may exit as
            // well... But, if not - terminate it.
            log.log(Level.FINE, "Stopping CLIODataCollector: {0}", collectorTask.toString()); // NOI18N
            collectorTask.cancel(true);
        }
    }

    @Override
    public List<DataTableMetadata> getDataTablesMetadata() {
        return metadata;
    }

    /** {@inheritDoc */
    @Override
    public boolean isAttachable() {
        return true;
    }

    /** {@inheritDoc */
    @Override
    public String getCmd() {
        return command;
    }

    /** {@inheritDoc */
    @Override
    public String[] getArgs() {
        return null;
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(CLIODataCollector.class, key, params);
    }

    @Override
    protected ValidationStatus doValidation(final DLightTarget target) {
        DLightLogger.assertNonUiThread();

        ValidationStatus result = null;
        boolean fileExists = false;
        boolean connected = true;
        final ExecutionEnvironment execEnv = target.getExecEnv();
        String error = ""; // NOI18N

        try {
            fileExists = HostInfoUtils.fileExists(execEnv, command);
        } catch (InterruptedException ex) {
            error = loc("ValidationStatus.InterruptedWhileValidation"); //NOI18N
            return ValidationStatus.invalidStatus(error);
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

                @Override
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

    @Override
    public void setupEnvironment(DLightTarget target, MacroMap env) {
        env.putAll(envs);
    }

    @Override
    public void dataFiltersChanged(List<DataFilter> newSet, boolean isAdjusting) {
    }

    private class CLIOInputProcessorFactory implements InputProcessorFactory {

        @Override
        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return InputProcessors.bridge(new LineProcessor() {

                @Override
                public void processLine(String line) {
                    CLIODataCollector.this.processLine(line);
                }

                @Override
                public void reset() {
                }

                @Override
                public void close() {
                }
            });
        }
    }
}
