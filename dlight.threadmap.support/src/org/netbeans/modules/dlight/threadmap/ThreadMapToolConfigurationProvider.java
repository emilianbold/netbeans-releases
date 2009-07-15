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
package org.netbeans.modules.dlight.threadmap;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.storage.threadmap.ThreadMapMetadata;
import org.netbeans.modules.dlight.api.storage.types.TimeDuration;
import org.netbeans.modules.dlight.api.tool.DLightToolConfiguration;
import org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration;
import org.netbeans.modules.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.modules.dlight.dtrace.collector.MultipleDTDCConfiguration;
import org.netbeans.modules.dlight.spi.tool.DLightToolConfigurationProvider;
import org.netbeans.modules.dlight.threadmap.collector.MSAParser;
import org.netbeans.modules.dlight.threadmap.indicator.ThreadMapIndicatorConfiguration;
import org.netbeans.modules.dlight.util.Util;
import org.netbeans.modules.dlight.visualizers.api.ThreadMapVisualizerConfiguration;
import org.openide.util.NbBundle;

public class ThreadMapToolConfigurationProvider implements DLightToolConfigurationProvider {

    public static final int INDICATOR_POSITION = 10;
    private static final String TOOL_NAME = loc("ThreadMapTool.ToolName"); // NOI18N
    private static final String DETAILED_TOOL_NAME = loc("ThreadMapTool.DetailedToolName"); // NOI18N

    public DLightToolConfiguration create() {
        final DLightToolConfiguration toolConfiguration =
                new DLightToolConfiguration(TOOL_NAME, DETAILED_TOOL_NAME);

        DataTableMetadata msaTableMetadata = createMsaTableMetadata();

        ThreadMapIndicatorConfiguration indicatorConfig = new ThreadMapIndicatorConfiguration(null);
        ThreadMapMetadata threadMapMetadata = new ThreadMapMetadata(msaTableMetadata);
        VisualizerConfiguration visualizerConfig = new ThreadMapVisualizerConfiguration(threadMapMetadata);

        indicatorConfig.addVisualizerConfiguration(visualizerConfig);
        toolConfiguration.addIndicatorConfiguration(indicatorConfig);


        String scriptFile = Util.copyResource(getClass(),
                Util.getBasePath(getClass()) + "/resources/msa.d"); // NOI18N

        DTDCConfiguration dtraceDataCollectorConfiguration =
                new DTDCConfiguration(scriptFile, Arrays.asList(msaTableMetadata));

        dtraceDataCollectorConfiguration.setDtraceParser(new MSAParser(new TimeDuration(TimeUnit.MILLISECONDS, 50), msaTableMetadata));

        toolConfiguration.addDataCollectorConfiguration(new MultipleDTDCConfiguration(dtraceDataCollectorConfiguration, "msa")); // NOI18N

        return toolConfiguration;
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(
                ThreadMapToolConfigurationProvider.class, key, params);
    }

    private DataTableMetadata createMsaTableMetadata() {
        Column cpuId = new Column("CPUID", Integer.class, "CPUID", null); // NOI18N
        Column threadId = new Column("THRID", Integer.class, "THRID", null); // NOI18N
        Column timestamp = new Column("TSTAMP", Long.class, "TSTAMP", null); // NOI18N
        Column usrTime = new Column("LMS_USER", Integer.class, "LMS_USER", null); // NOI18N
        Column sysTime = new Column("LMS_SYSTEM", Integer.class, "LMS_SYSTEM", null); // NOI18N

        return new DataTableMetadata("MSA", // NOI18N
                Arrays.asList(cpuId, threadId, timestamp, usrTime, sysTime), null);
    }
}
