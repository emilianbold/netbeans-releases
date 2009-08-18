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
package org.netbeans.modules.cnd.paralleladviser.paralleladvisermonitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.dlight.api.indicator.IndicatorMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.tool.DLightToolConfiguration;
import org.netbeans.modules.cnd.paralleladviser.paralleladvisermonitor.impl.ParallelAdviserIndicatorConfiguration;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.core.stack.api.ThreadState.MSAState;
import org.netbeans.modules.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration.CollectedInfo;
import org.netbeans.modules.dlight.spi.tool.DLightToolConfigurationProvider;
import org.netbeans.modules.dlight.tools.ProcDataProviderConfiguration;
import org.netbeans.modules.dlight.util.Util;
import org.openide.util.NbBundle;

/**
 * Parallel Adviser D-Light Tool.
 *
 * @author Nick Krasilnikov
 */
public final class DLightParallelAdviserToolConfigurationProvider
        implements DLightToolConfigurationProvider {

    public static final String ID = "dlight.tool.paralleladviser"; // NOI18N
    private static final String TOOL_NAME = loc("ParallelAdviserMonitorTool.ToolName"); // NOI18N
    private static final String DETAILED_TOOL_NAME = loc("ParallelAdviserMonitorTool.DetailedToolName"); // NOI18N
    // This indicator should be the last one
    public static final int INDICATOR_POSITION = 100000;

    public DLightToolConfiguration create() {
        final DLightToolConfiguration toolConfiguration =
                new DLightToolConfiguration(TOOL_NAME, DETAILED_TOOL_NAME);
        toolConfiguration.setIcon("org/netbeans/modules/cnd/paralleladviser/paralleladviserview/resources/paralleladviser.png"); // NOI18N

        // Collectors
        // SunStudio
        SunStudioDCConfiguration ssCollectorConfig =
                new SunStudioDCConfiguration(CollectedInfo.FUNCTIONS_LIST);
        toolConfiguration.addDataCollectorConfiguration(ssCollectorConfig);
        // D-Trace
        String scriptFile = Util.copyResource(getClass(),
                Util.getBasePath(getClass()) + "/resources/calls.d"); // NOI18N
        Column timestamp = new Column("time_stamp", Long.class); // NOI18N
        Column cpuId = new Column("cpu_id", Integer.class); // NOI18N
        Column threadId = new Column("thread_id", Integer.class); // NOI18N
        Column mstate = new Column("mstate", Integer.class); // NOI18N
        Column duration = new Column("duration", Integer.class); // NOI18N
        Column stackId = new Column("leaf_id", Integer.class); // NOI18N
        DataTableMetadata profilerTableMetadata = new DataTableMetadata("CallStack", // NOI18N
                Arrays.asList(timestamp, cpuId, threadId, mstate, duration, stackId), null);
        DTDCConfiguration dtraceDataCollectorConfiguration =
                new DTDCConfiguration(scriptFile,
                Arrays.asList(profilerTableMetadata));
        dtraceDataCollectorConfiguration.setStackSupportEnabled(true);
        dtraceDataCollectorConfiguration.setOutputPrefix("cpu:"); // NOI18N
        toolConfiguration.addDataCollectorConfiguration(
                dtraceDataCollectorConfiguration);
        // D-Trace 2
        String scriptFile2 = Util.copyResource(getClass(),
                Util.getBasePath(getClass()) + "/resources/msa.d"); // NOI18N
        Column threads = new Column("threads", Integer.class); // NOI18N
        Column usrTime = new Column(MSAState.RunningUser.toString(), Integer.class);
        Column sysTime = new Column(MSAState.RunningSystemCall.toString(), Integer.class);
        Column othTime = new Column(MSAState.RunningOther.toString(), Integer.class);
        Column tpfTime = new Column(MSAState.SleepingUserTextPageFault.toString(), Integer.class);
        Column dpfTime = new Column(MSAState.SleepingUserDataPageFault.toString(), Integer.class);
        Column kpfTime = new Column(MSAState.SleepingKernelPageFault.toString(), Integer.class);
        Column lckTime = new Column(MSAState.SleepingUserLock.toString(), Integer.class);
        Column slpTime = new Column(MSAState.SleepingOther.toString(), Integer.class);
        Column latTime = new Column(MSAState.WaitingCPU.toString(), Integer.class);
        Column stpTime = new Column(MSAState.ThreadStopped.toString(), Integer.class);
        DTDCConfiguration dtraceDataCollectorConfiguration2 = new DTDCConfiguration(scriptFile2, Arrays.asList(new DataTableMetadata("MSA", // NOI18N
                Arrays.asList(threads, usrTime, sysTime, othTime, tpfTime, dpfTime, kpfTime, lckTime, slpTime, latTime, stpTime), null)));
        dtraceDataCollectorConfiguration2.setOutputPrefix("msa:"); // NOI18N
//        dtraceDataCollectorConfiguration2.setDtraceParser(new MSAParser(new TimeDuration(TimeUnit.SECONDS, 1), null));
//        dtraceDataCollectorConfiguration2.setIndicatorFiringFactor(1); // MSAParser will do aggregation once per second...
        toolConfiguration.addDataCollectorConfiguration(dtraceDataCollectorConfiguration2);


        // Indicator
        ProcDataProviderConfiguration indicatorProviderConfiguration = new ProcDataProviderConfiguration();
        toolConfiguration.addIndicatorDataProviderConfiguration(indicatorProviderConfiguration);
        toolConfiguration.addIndicatorDataProviderConfiguration(dtraceDataCollectorConfiguration2);

        List<Column> resultColumns = new ArrayList<Column>();
        resultColumns.add(ProcDataProviderConfiguration.USR_TIME);
        resultColumns.add(ProcDataProviderConfiguration.SYS_TIME);
        resultColumns.add(ProcDataProviderConfiguration.THREADS);
        IndicatorMetadata indicatorMetadata =
                new IndicatorMetadata(resultColumns);
        ParallelAdviserIndicatorConfiguration indicatorConfiguration = new ParallelAdviserIndicatorConfiguration(
                indicatorMetadata,
                INDICATOR_POSITION);
        toolConfiguration.addIndicatorConfiguration(indicatorConfiguration);

        return toolConfiguration;
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(
                DLightParallelAdviserToolConfigurationProvider.class, key, params);
    }
}
