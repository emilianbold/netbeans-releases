/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.paralleladviser.paralleladvisermonitor.impl;

import java.util.Collection;
import org.netbeans.modules.cnd.paralleladviser.paralleladviserview.Advice;
import org.netbeans.modules.dlight.management.api.DLightSession.SessionState;
import org.netbeans.modules.dlight.spi.indicator.IndicatorNotificationsListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import javax.swing.*;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.deep.CsmLoopStatement;
import org.netbeans.modules.cnd.paralleladviser.codemodel.CodeModelUtils;
import org.netbeans.modules.cnd.paralleladviser.paralleladviserview.ParallelAdviserTopComponent;
import org.netbeans.modules.dlight.api.dataprovider.DataModelScheme;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.storage.types.Time;
import org.netbeans.modules.dlight.api.support.DataModelSchemeProvider;
import org.netbeans.modules.dlight.core.stack.api.FunctionCallWithMetric;
import org.netbeans.modules.dlight.core.stack.api.FunctionMetric;
import org.netbeans.modules.dlight.core.stack.api.ThreadState.MSAState;
import org.netbeans.modules.dlight.core.stack.api.support.FunctionDatatableDescription;
import org.netbeans.modules.dlight.core.stack.dataprovider.FunctionsListDataProvider;
import org.netbeans.modules.dlight.management.api.DLightManager;
import org.netbeans.modules.dlight.management.api.DLightSession;
import org.netbeans.modules.dlight.management.api.DLightSessionListener;
import org.netbeans.modules.dlight.management.api.SessionStateListener;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration;
import org.netbeans.modules.dlight.spi.dataprovider.DataProvider;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.dlight.tools.ProcDataProviderConfiguration;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 * Parallel Adviser monitor.
 *
 * @author Nick Krasilnikov
 */
public class ParallelAdviserMonitor implements IndicatorNotificationsListener, DLightSessionListener, SessionStateListener {

    private static final ParallelAdviserMonitor instance = new ParallelAdviserMonitor();
    private CpuHighLoadIntervalFinder highLoadFinder;
    private UnnecessaryThreadsIntervalFinder unnecessaryThreadsFinder;
    Collection<LoopParallelizationAdvice> notificationTips;

    public static ParallelAdviserMonitor getInstance() {
        return instance;
    }

    private ParallelAdviserMonitor() {
        highLoadFinder = new CpuHighLoadIntervalFinder();
        unnecessaryThreadsFinder = new UnnecessaryThreadsIntervalFinder();
    }

    public void startup() {
        DLightManager.getDefault().addDLightSessionListener(this);
    }

    public void shutdown() {
    }

    public void activeSessionChanged(DLightSession oldSession, DLightSession newSession) {
    }

    public void sessionAdded(DLightSession newSession) {
        highLoadFinder = new CpuHighLoadIntervalFinder();
        unnecessaryThreadsFinder = new UnnecessaryThreadsIntervalFinder();
        notificationTips = new ArrayList<LoopParallelizationAdvice>();
        newSession.addIndicatorNotificationListener(this);
        newSession.addSessionStateListener(this);
    }

    public void sessionRemoved(DLightSession removedSession) {
    }

    public void sessionStateChanged(DLightSession session, SessionState oldState, SessionState newState) {
        InputOutput io = session.getInputOutput();
        if (!notificationTips.isEmpty() && newState == SessionState.ANALYZE && io != null) {
            try {
                io.getErr().println("Parallel Adviser: ", // NOI18N
                        new OutputListener() {

                    public void outputLineSelected(OutputEvent ev) {
                    }

                    public void outputLineAction(OutputEvent ev) {
                        ParallelAdviserTopComponent view = ParallelAdviserTopComponent.findInstance();
                        if (!view.isOpened()) {
                            view.open();
                        }
                        view.requestActive();
                    }

                    public void outputLineCleared(OutputEvent ev) {
                    }
                });
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            for (Advice advice : notificationTips) {
                advice.addNotification(io.getErr());
            }
        }
    }

    public void reset() {
    }

    public void updated(List<DataRow> data) {
        if (getProcessorsNumber() <= 1) {
            // no need to parallelize application on machine with single core processor.
            return;
        }

        for (DataRow dataRow : data) {

            highLoadFinder.update(dataRow);

            if (highLoadFinder.isHighLoadInterval()) {

                DLightSession session = DLightManager.getDefault().getActiveSession();
                InputOutput io = session.getInputOutput();

                SunStudioDataCollector collector = new SunStudioDataCollector();

                for (FunctionCallWithMetric functionCall : collector.getFunctionCallsSortedByInclusiveTime()) {
                    if ((Double) functionCall.getMetricValue(SunStudioDCConfiguration.c_iUser.getColumnName()) < CpuHighLoadIntervalFinder.INTERVAL_BOUND * 0.8) {
                        break;
                    }
                    if ((Double) functionCall.getMetricValue(SunStudioDCConfiguration.c_eUser.getColumnName()) < CpuHighLoadIntervalFinder.INTERVAL_BOUND * 0.8) {
                        continue;
                    }
                    String functionName = functionCall.getFunction().getQuilifiedName();
                    functionName = functionName.replaceAll("_\\$.*\\.(.*)", "$1"); // NOI18N
                    functionName = functionName.replaceAll("([~\\.])*\\..*", "$1"); // NOI18N
                    CsmFunction function = CodeModelUtils.getFunction(getProject(), functionName);
                    for (CsmLoopStatement loop : CodeModelUtils.getForStatements(function)) {
                        if (CodeModelUtils.canParallelize(loop)) {
                            LoopParallelizationAdvice tip = new LoopParallelizationAdvice(function, loop, highLoadFinder.getProcessorUtilization());
                            LoopParallelizationTipsProvider.addTip(tip);
                            addNotificationTip(tip);
                            Runnable updateView = new Runnable() {

                                public void run() {
                                    ParallelAdviserTopComponent view = ParallelAdviserTopComponent.findInstance();
                                    view.updateTips();
                                }
                            };
                            if (SwingUtilities.isEventDispatchThread()) {
                                updateView.run();
                            } else {
                                SwingUtilities.invokeLater(updateView);
                            }
                        }
                    }
                }

                DTraceDataCollector collector2 = new DTraceDataCollector();

                for (FunctionCallWithMetric functionCall : collector2.getFunctionCallsSortedByInclusiveTime()) {
                    final Column c_iUser = new Column(
                            FunctionMetric.CpuTimeInclusiveMetric.getMetricID(),
                            FunctionMetric.CpuTimeInclusiveMetric.getMetricValueClass(),
                            FunctionMetric.CpuTimeInclusiveMetric.getMetricDisplayedName(), null);
                    final Column c_eUser = new Column(
                            FunctionMetric.CpuTimeExclusiveMetric.getMetricID(),
                            FunctionMetric.CpuTimeExclusiveMetric.getMetricValueClass(),
                            FunctionMetric.CpuTimeExclusiveMetric.getMetricDisplayedName(), null);
                    if (((Time) functionCall.getMetricValue(c_iUser.getColumnName())).getNanos() / 1000000000 < CpuHighLoadIntervalFinder.INTERVAL_BOUND * 0.8) {
                        break;
                    }
                    if (((Time) functionCall.getMetricValue(c_eUser.getColumnName())).getNanos() / 1000000000 < CpuHighLoadIntervalFinder.INTERVAL_BOUND * 0.8) {
                        continue;
                    }
                    String functionName = functionCall.getFunction().getQuilifiedName();
                    functionName = functionName.replaceAll(".*\\`", ""); // NOI18N
                    functionName = functionName.replaceAll("_\\$.*\\.(.*)", "$1"); // NOI18N
                    functionName = functionName.replaceAll("([~\\.])*\\..*", "$1"); // NOI18N
                    CsmFunction function = CodeModelUtils.getFunction(getProject(), functionName);
                    for (CsmLoopStatement loop : CodeModelUtils.getForStatements(function)) {
                        if (CodeModelUtils.canParallelize(loop)) {
                            LoopParallelizationAdvice tip = new LoopParallelizationAdvice(function, loop, highLoadFinder.getProcessorUtilization());
                            LoopParallelizationTipsProvider.addTip(tip);
                            addNotificationTip(tip);
                            Runnable updateView = new Runnable() {

                                public void run() {
                                    ParallelAdviserTopComponent view = ParallelAdviserTopComponent.findInstance();
                                    view.updateTips();
                                }
                            };
                            if (SwingUtilities.isEventDispatchThread()) {
                                updateView.run();
                            } else {
                                SwingUtilities.invokeLater(updateView);
                            }
                        }
                    }
                }
            }

            unnecessaryThreadsFinder.update(dataRow);
            if (unnecessaryThreadsFinder.isUnnecessaryThreadsInterval()) {
                UnnecessaryThreadsTipsProvider.clearTips();
                UnnecessaryThreadsTipsProvider.addTip(new UnnecessaryThreadsAdvice());
                //panel.notifyUser();
                System.out.println("Parallel Adviser notification!!!"); // NOI18N
                Runnable updateView = new Runnable() {

                    public void run() {
                        ParallelAdviserTopComponent view = ParallelAdviserTopComponent.findInstance();
                        view.updateTips();
                    }
                };
                if (SwingUtilities.isEventDispatchThread()) {
                    updateView.run();
                } else {
                    SwingUtilities.invokeLater(updateView);
                }
            }
        }
    }

    private static String getMessage(String name) {
        return NbBundle.getMessage(ParallelAdviserMonitor.class, name);
    }
    public static final String GIZMO_PROJECT_FOLDER = "GizmoProjectFolder"; //NOI18N

    private static CsmProject getProject() {
        DLightSession session = DLightManager.getDefault().getActiveSession();
        if (session == null) {
            return null;
        }
        ServiceInfoDataStorage serviceInfoStorage = session.getServiceInfoDataStorage();
        if (serviceInfoStorage == null) {
            return null;
        }
        Map<String, String> serviceInfo = serviceInfoStorage.getInfo();
        if (serviceInfo == null) {
            return null;
        }
        String projectFolderName = serviceInfo.get(GIZMO_PROJECT_FOLDER);
        if (projectFolderName == null) {
            return null;
        }
        CsmProject csmProject = null;
        try {
            Project prj = ProjectManager.getDefault().findProject(FileUtil.toFileObject(new File(projectFolderName)));
            csmProject = CsmModelAccessor.getModel().getProject(prj);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
        return csmProject;
    }

    private static class SunStudioDataCollector {

        private DataProvider dataProvider;
        private DataTableMetadata metadata;

        public SunStudioDataCollector() {
            metadata =
                    SunStudioDCConfiguration.getCPUTableMetadata(
                    SunStudioDCConfiguration.c_name,
                    SunStudioDCConfiguration.c_iUser,
                    SunStudioDCConfiguration.c_eUser);

            DataModelScheme dataModel = DataModelSchemeProvider.getInstance().getScheme("model:functions"); //NOI18N

            DLightSession session = DLightManager.getDefault().getActiveSession();
            dataProvider = session.createDataProvider(dataModel, metadata);
        }

        public List<FunctionCallWithMetric> getFunctionCallsSortedByInclusiveTime() {
            if (dataProvider != null) {
                FunctionDatatableDescription funcDescription = new FunctionDatatableDescription(SunStudioDCConfiguration.c_name.getColumnName(), null, SunStudioDCConfiguration.c_name.getColumnName());
                List<FunctionCallWithMetric> functions = ((FunctionsListDataProvider) dataProvider).getFunctionsList(metadata, funcDescription, Arrays.asList(SunStudioDCConfiguration.c_eUser, SunStudioDCConfiguration.c_iUser));
                Collections.sort(functions, new Comparator<FunctionCallWithMetric>() {

                    public int compare(FunctionCallWithMetric o1, FunctionCallWithMetric o2) {
                        return (int) ((Double) o2.getMetricValue(SunStudioDCConfiguration.c_iUser.getColumnName()) - (Double) o1.getMetricValue(SunStudioDCConfiguration.c_iUser.getColumnName()));
                    }
                });
                return functions;
            } else {
                return Collections.<FunctionCallWithMetric>emptyList();
            }
        }
    }
    private int processorsNumber = 0;

    private int getProcessorsNumber() {
        if (processorsNumber == 0) {
            processorsNumber = 1;
            DLightSession session = DLightManager.getDefault().getActiveSession();
            if (session != null) {
                ExecutionEnvironment env = ExecutionEnvironmentFactory.fromUniqueID(session.getServiceInfoDataStorage().getValue(ServiceInfoDataStorage.EXECUTION_ENV_KEY));
                if (env != null) {
                    HostInfo hostInfo = null;
                    try {
                        hostInfo = HostInfoUtils.getHostInfo(env);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (CancellationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    if (hostInfo != null) {
                        processorsNumber = hostInfo.getCpuNum();
                    }
                }
            }
        }
        return processorsNumber;
    }

    private void addNotificationTip(LoopParallelizationAdvice tip) {
        for (LoopParallelizationAdvice advice : notificationTips) {
            if(advice.getLoop().equals(tip.getLoop())) {
                notificationTips.remove(advice);
                break;
            }
        }
        notificationTips.add(tip);
    }

    private static class DTraceDataCollector {

        public static final String GIZMO_PROJECT_FOLDER = "GizmoProjectFolder"; //NOI18N
        private DataProvider dataProvider;
        private DataTableMetadata metadata;

        public DTraceDataCollector() {
            Column timestamp = new Column("time_stamp", Long.class); // NOI18N
            Column cpuId = new Column("cpu_id", Integer.class); // NOI18N
            Column threadId = new Column("thread_id", Integer.class); // NOI18N
            Column mstate = new Column("mstate", Integer.class); // NOI18N
            Column duration = new Column("duration", Integer.class); // NOI18N
            Column stackId = new Column("leaf_id", Integer.class); // NOI18N
            metadata = new DataTableMetadata("CallStack", // NOI18N
                    Arrays.asList(timestamp, cpuId, threadId, mstate, duration, stackId), null);

            DataModelScheme dataModel = DataModelSchemeProvider.getInstance().getScheme("model:functions"); //NOI18N

            DLightSession session = DLightManager.getDefault().getActiveSession();
            dataProvider = session.createDataProvider(dataModel, metadata);
        }

        public List<FunctionCallWithMetric> getFunctionCallsSortedByInclusiveTime() {
            if (dataProvider != null) {
                FunctionDatatableDescription funcDescription = new FunctionDatatableDescription("name", null, "name"); // NOI18N
                final Column c_eUser = new Column(
                        FunctionMetric.CpuTimeExclusiveMetric.getMetricID(),
                        FunctionMetric.CpuTimeExclusiveMetric.getMetricValueClass(),
                        FunctionMetric.CpuTimeExclusiveMetric.getMetricDisplayedName(), null);
                final Column c_iUser = new Column(
                        FunctionMetric.CpuTimeInclusiveMetric.getMetricID(),
                        FunctionMetric.CpuTimeInclusiveMetric.getMetricValueClass(),
                        FunctionMetric.CpuTimeInclusiveMetric.getMetricDisplayedName(), null);
                List<FunctionCallWithMetric> functions = ((FunctionsListDataProvider) dataProvider).getFunctionsList(metadata, funcDescription, Arrays.asList(c_eUser, c_iUser));
                Collections.sort(functions, new Comparator<FunctionCallWithMetric>() {

                    public int compare(FunctionCallWithMetric o1, FunctionCallWithMetric o2) {
                        return (int) (((Time) o2.getMetricValue(c_iUser.getColumnName())).getNanos() - ((Time) o1.getMetricValue(c_iUser.getColumnName())).getNanos());
                    }
                });
                return functions;
            } else {
                return Collections.<FunctionCallWithMetric>emptyList();
            }
        }
    }

    private class UnnecessaryThreadsIntervalFinder {

        private static final int INTERVAL_BOUND = 10;
        private int interval;

        public UnnecessaryThreadsIntervalFinder() {
            interval = 0;
            processorsNumber = 0;
        }

        public boolean isUnnecessaryThreadsInterval() {
            boolean result = (interval > 0);//&& (interval % INTERVAL_BOUND == 0);
            return result;
        }

        public void update(DataRow dataRow) {
            Column threadsCol = new Column("threads", Integer.class); // NOI18N
            Column latTimeCol = new Column(MSAState.WaitingCPU.toString(), Integer.class);
            Column runTimeCol = new Column(MSAState.Running.toString(), Integer.class);
            Object threadsObj = dataRow.getData(threadsCol.getColumnName());
            Object latTimeObj = dataRow.getData(latTimeCol.getColumnName());
            Object runTimeObj = dataRow.getData(runTimeCol.getColumnName());
            if (latTimeObj != null && runTimeObj != null && threadsObj != null) {
                if (areUnnecessaryThreadsUsed((Integer) threadsObj, (Integer) latTimeObj, (Integer) runTimeObj)) {
                    interval++;
                } else {
                    interval = 0;
                }
            }
        }

        private boolean areUnnecessaryThreadsUsed(int threads, int latTime, int runTime) {
            if (((double) runTime / (double) threads) > 0.9 && ((double) latTime / (double) threads) > 1) {
                return true;
            }
            return false;
        }
    }

    private class CpuHighLoadIntervalFinder {

        private static final int INTERVAL_BOUND = 10;
        private int interval;
        private int ticks;
        private double processorUtilizationSum;

        public CpuHighLoadIntervalFinder() {
            interval = 0;
            ticks = 0;
            processorUtilizationSum = 0;
            processorsNumber = 0;
        }

        public boolean isHighLoadInterval() {
            boolean result = (interval > 0) && (interval % INTERVAL_BOUND == 0);
            return result;
        }

        public double getProcessorUtilization() {
            return processorUtilizationSum / ticks;
        }

        public void update(DataRow dataRow) {
            Object usrTime = dataRow.getData(ProcDataProviderConfiguration.USR_TIME.getColumnName());
            if (usrTime != null) {
                ticks++;
                processorUtilizationSum += (Float) usrTime;

                if (isSingleThreadOnOneProcessorHighLoaded(dataRow)) {
                    interval++;
                } else {
                    interval = 0;
                }
            }
        }

        private boolean isSingleThreadOnOneProcessorHighLoaded(DataRow dataRow) {
            Object usrTimeObj = dataRow.getData(ProcDataProviderConfiguration.USR_TIME.getColumnName());
            Object threadsObj = dataRow.getData(ProcDataProviderConfiguration.THREADS.getColumnName());
            if (usrTimeObj != null && threadsObj != null) {
                double utime = (Float) usrTimeObj;
                int threads = (Integer) threadsObj;
                if (threads == 1 && 90 < utime * getProcessorsNumber()) {
                    return true;
                }
            }
            return false;
        }
    }
}
