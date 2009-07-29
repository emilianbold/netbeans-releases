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
import org.netbeans.modules.dlight.api.support.DataModelSchemeProvider;
import org.netbeans.modules.dlight.core.stack.api.FunctionCallWithMetric;
import org.netbeans.modules.dlight.core.stack.api.support.FunctionDatatableDescription;
import org.netbeans.modules.dlight.core.stack.dataprovider.FunctionsListDataProvider;
import org.netbeans.modules.dlight.management.api.DLightManager;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration;
import org.netbeans.modules.dlight.spi.dataprovider.DataProvider;
import org.netbeans.modules.dlight.spi.dataprovider.DataProviderFactory;
import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.dlight.tools.ProcDataProviderConfiguration;
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

    ParallelAdviserIndicator(ParallelAdviserIndicatorConfiguration configuration) {
        super(configuration);
        panel = new ParallelAdviserIndicatorPanel();
        highLoadFinder = new CpuHighLoadIntervalFinder();
    }

    @Override
    public void updated(List<DataRow> data) {
        int processorsNumber = Runtime.getRuntime().availableProcessors();
        if (processorsNumber <= 1) {
            // no need to parallelize application on machine with single core processor.
            return;
        }

        highLoadFinder.update(data);

        if (highLoadFinder.isHighLoadInterval()) {

            SunStudioDataCollector collector = new SunStudioDataCollector();

            for (FunctionCallWithMetric functionCall : collector.getFunctionCallsSortedByInclusiveTime()) {
                if((Double)functionCall.getMetricValue(SunStudioDCConfiguration.c_iUser.getColumnName()) < highLoadFinder.ticks/2) {
                    break;
                }
                CsmFunction function = CodeModelUtils.getFunction(collector.getProject(), functionCall.getFunction().getQuilifiedName());
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
        private DataStorage dataStorage;
        private DataProvider dataProvider;
        private DataTableMetadata metadata;

        public SunStudioDataCollector() {
            List<DataStorage> storages = DLightManager.getDefault().getActiveSession().getStorages();

            metadata =
                    SunStudioDCConfiguration.getCPUTableMetadata(
                    SunStudioDCConfiguration.c_name,
                    SunStudioDCConfiguration.c_iUser,
                    SunStudioDCConfiguration.c_eUser);

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
                        if (providerFactory.provides(dataModel) && providerFactory.getSupportedDataStorageTypes().contains(dss)) {
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
            FunctionDatatableDescription funcDescription = new FunctionDatatableDescription(SunStudioDCConfiguration.c_name.getColumnName(), null, SunStudioDCConfiguration.c_name.getColumnName());
            List<FunctionCallWithMetric> functions = ((FunctionsListDataProvider) dataProvider).getFunctionsList(metadata, funcDescription, Arrays.asList(SunStudioDCConfiguration.c_eUser, SunStudioDCConfiguration.c_iUser));
            Collections.sort(functions, new Comparator<FunctionCallWithMetric>() {

                public int compare(FunctionCallWithMetric o1, FunctionCallWithMetric o2) {
                    return (int) ((Double) o2.getMetricValue(SunStudioDCConfiguration.c_iUser.getColumnName()) - (Double) o1.getMetricValue(SunStudioDCConfiguration.c_iUser.getColumnName()));
                }
            });
            return functions;
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

    private static class CpuHighLoadIntervalFinder {

        private static final int INTERVAL_OF_HIGH_LOAD_BOUND = 10;
        private int intervalOfHighLoad;
        private int ticks;
        private double processorUtilizationSum;

        public CpuHighLoadIntervalFinder() {
            intervalOfHighLoad = 0;
            ticks = 0;
            processorUtilizationSum = 0;
        }
        static int processorsNumber = Runtime.getRuntime().availableProcessors();

        public boolean isHighLoadInterval() {
            return (intervalOfHighLoad > 0) && (intervalOfHighLoad % INTERVAL_OF_HIGH_LOAD_BOUND == 0);
        }

        public double getProcessorUtilization() {
            return processorUtilizationSum / ticks;
        }

        public void update(List<DataRow> data) {
            for (DataRow dataRow : data) {
                ticks++;
                processorUtilizationSum += (Float) dataRow.getData(ProcDataProviderConfiguration.USR_TIME.getColumnName());
            }

            if (isSingleThreadOnOneProcessorHighLoaded(data, processorsNumber)) {
                intervalOfHighLoad++;
            } else {
                intervalOfHighLoad = 0;
            }
        }

        private static boolean isSingleThreadOnOneProcessorHighLoaded(List<DataRow> data, int processorsNumber) {
            for (DataRow dataRow : data) {
                double utime = (Float) dataRow.getData(ProcDataProviderConfiguration.USR_TIME.getColumnName());
                int threads = (Integer) dataRow.getData(ProcDataProviderConfiguration.THREADS.getColumnName());
                if (threads == 1 && 90 < utime * processorsNumber) {
                    return true;
                }
            }
            return false;
        }
    }
}
