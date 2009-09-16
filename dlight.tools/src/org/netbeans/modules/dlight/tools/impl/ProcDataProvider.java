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

import java.util.concurrent.CancellationException;
import org.netbeans.modules.dlight.api.execution.DLightTargetChangeEvent;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.tools.ProcDataProviderConfiguration;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.modules.dlight.api.execution.AttachableTarget;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.ValidationListener;
import org.netbeans.modules.dlight.api.execution.ValidationStatus;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.management.api.DLightManager;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.AsynchronousAction;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * Indicator data provider that reads CPU usage information from
 * <code>/proc</code> filesystem. This is supported on Linux and Solaris only.
 *
 * @author Alexey Vladykin
 */
public class ProcDataProvider extends IndicatorDataProvider<ProcDataProviderConfiguration> implements DataRowConsumer {

    private static final String NAME = "ProcReader"; // NOI18N
    private static final DataTableMetadata TABLE = new DataTableMetadata(
            NAME, Arrays.asList(
            ProcDataProviderConfiguration.SYS_TIME,
            ProcDataProviderConfiguration.USR_TIME,
            ProcDataProviderConfiguration.THREADS),
            null);

    private List<ValidationListener> validationListeners;
    private ValidationStatus validationStatus;
    private Future<Integer> procReaderTask;

    public ProcDataProvider(ProcDataProviderConfiguration configuration) {
        validationListeners = new CopyOnWriteArrayList<ValidationListener>();
        validationStatus = ValidationStatus.initialStatus();
    }

    @Override
    public Collection<DataTableMetadata> getDataTablesMetadata() {
        return Collections.singletonList(TABLE);
    }

    @Override
    public String getName() {
        return NAME;
    }

    public void targetStateChanged(DLightTargetChangeEvent event) {
        switch (event.state) {
            case RUNNING:
                targetStarted(event.target);
                break;
            case DONE:
            case FAILED:
            case STOPPED:
            case TERMINATED:
                targetFinished(event.target);
                break;
        }
    }

    /*
     * Synchronization protects validationStatus.
     */
    public synchronized ValidationStatus validate(DLightTarget target) {
        if (validationStatus.isValid()) {
            return validationStatus;
        }

        ValidationStatus oldStatus = validationStatus;
        ValidationStatus newStatus = doValidation(target);

        notifyStatusChanged(oldStatus, newStatus);

        validationStatus = newStatus;
        return newStatus;
    }

    private ValidationStatus doValidation(DLightTarget target) {
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

        if (osFamily != OSFamily.LINUX && osFamily != OSFamily.SUNOS) {
            return ValidationStatus.invalidStatus(getMessage("ValidationStatus.ProcReader.OSNotSupported")); // NOI18N
        }

        try {
            if (!HostInfoUtils.fileExists(env, "/proc")) { // NOI18N
                return ValidationStatus.invalidStatus(getMessage("ValidationStatus.ProcNotFound")); // NOI18N
            }
        } catch (IOException ex) {
            return ValidationStatus.invalidStatus(ex.getMessage());
        }

        return ValidationStatus.validStatus();
    }

    /*
     * Synchronization protects validationStatus.
     */
    public synchronized void invalidate() {
        validationStatus = ValidationStatus.initialStatus();
    }

    /*
     * Synchronization protects validationStatus.
     */
    public synchronized ValidationStatus getValidationStatus() {
        return validationStatus;
    }

    public void addValidationListener(ValidationListener listener) {
        if (!validationListeners.contains(listener)) {
            validationListeners.add(listener);
        }
    }

    public void removeValidationListener(ValidationListener listener) {
        validationListeners.remove(listener);
    }

    private void notifyStatusChanged(
            final ValidationStatus oldStatus,
            final ValidationStatus newStatus) {

        if (oldStatus.equals(newStatus)) {
            return;
        }

        for (ValidationListener validationListener : validationListeners) {
            validationListener.validationStateChanged(this, oldStatus, newStatus);
        }
    }

    /*
     * Synchronization protects procReaderTask.
     */
    private synchronized void targetStarted(DLightTarget target) {
        ExecutionEnvironment env = target.getExecEnv();
        HostInfo hostInfo = null;
        try {
            hostInfo = HostInfoUtils.getHostInfo(env);
        } catch (IOException ex) {
        } catch (CancellationException ex) {
        }

        if (hostInfo == null) {
            return;
        }

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
        npb.setExecutable(hostInfo.getShell());
        ExecutionDescriptor descr = new ExecutionDescriptor();
        descr = descr.inputOutput(InputOutput.NULL);
        int pid = ((AttachableTarget) target).getPID();
        Engine engine;
        switch (hostInfo.getOSFamily()) {
            case LINUX:
                engine = new ProcDataProviderLinux(this, getServiceInfoDataStorage());
                break;
            case SUNOS:
                engine = new ProcDataProviderSolaris(this, getServiceInfoDataStorage(), hostInfo.getCpuNum());
                break;
            default:
                DLightLogger.instance.severe("Called ProcDataProvider.targetStarted() on unsupported OS"); // NOI18N
                return;
        }
        npb = npb.setArguments("-c", engine.getCommand(pid)); // NOI18N
        descr = descr.outProcessorFactory(engine);
        ExecutionService service = ExecutionService.newService(npb, descr, "procreader"); // NOI18N
        procReaderTask = service.run();
    }

    /*
     * Synchronization protects procReaderTask.
     */
    private synchronized void targetFinished(DLightTarget target) {
        if (procReaderTask != null) {
            if (!procReaderTask.isDone()) {
                procReaderTask.cancel(true);
            }
            procReaderTask = null;
        }
    }

    public void dataFiltersChanged(List<DataFilter> newSet, boolean isAdjusting) {
    }

    /**
     * ProcDataProvider backend.
     */
    /*package*/ static interface Engine extends InputProcessorFactory {

        String getCommand(int pid);
    }

    /**
     * To be used by engines.
     *
     * @param row  new data row
     */
    public void consume(DataRow row) {
        super.notifyIndicators(Collections.singletonList(row));
    }

    private static String getMessage(String name) {
        return NbBundle.getMessage(ProcDataProvider.class, name);
    }
}
