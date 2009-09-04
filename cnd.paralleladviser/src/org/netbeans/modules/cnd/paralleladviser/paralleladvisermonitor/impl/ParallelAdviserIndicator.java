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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
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
import org.netbeans.modules.dlight.api.execution.DLightTarget;
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
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration;
import org.netbeans.modules.dlight.spi.dataprovider.DataProvider;
import org.netbeans.modules.dlight.spi.dataprovider.DataProviderFactory;
import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.dlight.tools.ProcDataProviderConfiguration;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Parallel Adviser indicator.
 *
 * @author Nick Krasilnikov
 */
/*package*/ class ParallelAdviserIndicator extends Indicator<ParallelAdviserIndicatorConfiguration> {

    private final ParallelAdviserIndicatorPanel panel;
    private final CpuHighLoadIntervalFinder highLoadFinder;
    private final UnnecessaryThreadsIntervalFinder unnecessaryThreadsFinder;

    ParallelAdviserIndicator(ParallelAdviserIndicatorConfiguration configuration) {
        super(configuration);
        panel = new ParallelAdviserIndicatorPanel();
        highLoadFinder = new CpuHighLoadIntervalFinder();
        unnecessaryThreadsFinder = new UnnecessaryThreadsIntervalFinder();
    }

    @Override
    public void updated(List<DataRow> data) {
        if (getProcessorsNumber() <= 1) {
            // no need to parallelize application on machine with single core processor.
            return;
        }

        highLoadFinder.update(data);

        if (highLoadFinder.isHighLoadInterval()) {

            SunStudioDataCollector collector = new SunStudioDataCollector();

            for (FunctionCallWithMetric functionCall : collector.getFunctionCallsSortedByInclusiveTime()) {
                if ((Double) functionCall.getMetricValue(SunStudioDCConfiguration.c_iUser.getColumnName()) < highLoadFinder.ticks / 2) {
                    break;
                }
                String functionName = functionCall.getFunction().getQuilifiedName();
                functionName = functionName.replaceAll("_\\$.*\\.(.*)", "$1"); // NOI18N
                functionName = functionName.replaceAll("([~\\.])*\\..*", "$1"); // NOI18N
                CsmFunction function = CodeModelUtils.getFunction(collector.getProject(), functionName);
                for (CsmLoopStatement loop : CodeModelUtils.getForStatements(function)) {
                    if (CodeModelUtils.canParallelize(loop)) {
                        LoopParallelizationTipsProvider.addTip(new LoopParallelizationAdvice(function, loop, highLoadFinder.getProcessorUtilization()));
                        panel.notifyUser();

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
                if (((Time) functionCall.getMetricValue(c_iUser.getColumnName())).getNanos() / 1000000000 < highLoadFinder.ticks / 2) {
                    break;
                }
                String functionName = functionCall.getFunction().getQuilifiedName();
                functionName = functionName.replaceAll(".*\\`", ""); // NOI18N
                functionName = functionName.replaceAll("_\\$.*\\.(.*)", "$1"); // NOI18N
                functionName = functionName.replaceAll("([~\\.])*\\..*", "$1"); // NOI18N
                CsmFunction function = CodeModelUtils.getFunction(collector2.getProject(), functionName);
                for (CsmLoopStatement loop : CodeModelUtils.getForStatements(function)) {
                    if (CodeModelUtils.canParallelize(loop)) {
                        LoopParallelizationTipsProvider.addTip(new LoopParallelizationAdvice(function, loop, highLoadFinder.getProcessorUtilization()));
                        panel.notifyUser();
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

        unnecessaryThreadsFinder.update(data);
        if (unnecessaryThreadsFinder.isUnnecessaryThreadsInterval()) {
            UnnecessaryThreadsTipsProvider.clearTips();
            UnnecessaryThreadsTipsProvider.addTip(new UnnecessaryThreadsAdvice());
            panel.notifyUser();
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

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    protected void tick() {
    }

    @Override
    protected void repairNeeded(boolean needed) {
    }

    public void reset() {
    }

    private static String getMessage(String name) {
        return NbBundle.getMessage(ParallelAdviserIndicator.class, name);
    }

    private static class SunStudioDataCollector {

        public static final String GIZMO_PROJECT_FOLDER = "GizmoProjectFolder"; //NOI18N
        private ServiceInfoDataStorage dataStorage;
        private DataProvider dataProvider;
        private DataTableMetadata metadata;

        public SunStudioDataCollector() {
            dataStorage = null;
            dataProvider = null;

            metadata =
                    SunStudioDCConfiguration.getCPUTableMetadata(
                    SunStudioDCConfiguration.c_name,
                    SunStudioDCConfiguration.c_iUser,
                    SunStudioDCConfiguration.c_eUser);

            DataModelScheme dataModel = DataModelSchemeProvider.getInstance().getScheme("model:functions"); //NOI18N

            DLightSession session = DLightManager.getDefault().getActiveSession();
            dataProvider = session.createDataProvider(dataModel, metadata);
            dataStorage = session.getServiceInfoDataStorage();
        }

        public List<FunctionCallWithMetric> getFunctionCallsSortedByInclusiveTime() {
            if (dataStorage != null && dataProvider != null) {
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

        public CsmProject getProject() {
            if (dataStorage == null) {
                return null;
            }
            Map<String, String> serviceInfo = dataStorage.getInfo();
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
    }
    private int processorsNumber = 0;

    private int getProcessorsNumber() {
        if (processorsNumber == 0) {
            processorsNumber = 1;
            DLightTarget target = getTarget();
            if (target != null) {
                HostInfo hostInfo = null;
                try {
                    hostInfo = HostInfoUtils.getHostInfo(target.getExecEnv());
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
        return processorsNumber;
    }

    private static class DTraceDataCollector {

        public static final String GIZMO_PROJECT_FOLDER = "GizmoProjectFolder"; //NOI18N
        private DataStorage dataStorage;
        private DataProvider dataProvider;
        private DataTableMetadata metadata;

        public DTraceDataCollector() {
            dataStorage = null;
            dataProvider = null;

            List<DataStorage> storages = DLightManager.getDefault().getActiveSession().getStorages();

            Column timestamp = new Column("time_stamp", Long.class); // NOI18N
            Column cpuId = new Column("cpu_id", Integer.class); // NOI18N
            Column threadId = new Column("thread_id", Integer.class); // NOI18N
            Column mstate = new Column("mstate", Integer.class); // NOI18N
            Column duration = new Column("duration", Long.class); // NOI18N
            Column stackId = new Column("leaf_id", Long.class); // NOI18N
            metadata = new DataTableMetadata("CallStack", // NOI18N
                    Arrays.asList(timestamp, cpuId, threadId, mstate, duration, stackId), null);

            DataModelScheme dataModel = DataModelSchemeProvider.getInstance().getScheme("model:functions"); //NOI18N

            Collection<? extends DataProviderFactory> factories = Lookup.getDefault().lookupAll(DataProviderFactory.class);

            for (DataStorage storage : storages) {
                if (!storage.hasData(metadata)) {
                    continue;
                }
                Collection<DataStorageType> dataStorageTypes = storage.getStorageTypes();
                for (DataStorageType dss : dataStorageTypes) {
                    // As DataStorage is already specialized, there is always only one
                    // returned DataSchema
                    for (DataProviderFactory providerFactory : factories) {
                        if (providerFactory.provides(dataModel) && providerFactory.validate(storage)) {
                            dataProvider = providerFactory.create();
                            break;
                        }
                    }
                    if (dataProvider != null) {
                        dataStorage = storage;
                        dataProvider.attachTo(dataStorage);
                        break;
                    }
                }
                if (dataProvider != null) {
                    break;
                }
            }
        }

        public List<FunctionCallWithMetric> getFunctionCallsSortedByInclusiveTime() {
            if (dataStorage != null && dataProvider != null) {
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

        public CsmProject getProject() {
            Map<String, String> serviceInfo = ((ServiceInfoDataStorage) dataStorage).getInfo();
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
    }

    private class UnnecessaryThreadsIntervalFinder {

        private static final int INTERVAL_BOUND = 10;
        private int interval;

        public UnnecessaryThreadsIntervalFinder() {
            interval = 0;
            processorsNumber = 0;
        }

        public boolean isUnnecessaryThreadsInterval() {
            boolean result = (interval > 0) && (interval % INTERVAL_BOUND == 0);
            return result;
        }

        public void update(List<DataRow> data) {
            Column threadsCol = new Column("threads", Integer.class); // NOI18N
            Column latTimeCol = new Column(MSAState.WaitingCPU.toString(), Integer.class);
            Column runTimeCol = new Column(MSAState.Running.toString(), Integer.class);
            for (DataRow dataRow : data) {
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

        public void update(List<DataRow> data) {
            for (DataRow dataRow : data) {
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
