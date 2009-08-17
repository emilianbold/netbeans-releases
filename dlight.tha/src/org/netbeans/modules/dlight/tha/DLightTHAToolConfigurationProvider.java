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
package org.netbeans.modules.dlight.tha;

import java.util.Arrays;
import org.netbeans.modules.dlight.api.indicator.IndicatorConfiguration;
import org.netbeans.modules.dlight.api.indicator.IndicatorMetadata;
import org.netbeans.modules.dlight.api.tool.DLightToolConfiguration;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration;
import org.netbeans.modules.dlight.spi.tool.DLightToolConfigurationProvider;
import org.openide.util.NbBundle;

public final class DLightTHAToolConfigurationProvider
        implements DLightToolConfigurationProvider {

    private static final String ID = "dlight.tool.tha"; // NOI18N
    private static final String TOOL_NAME = loc("THAMonitor.ToolName"); // NOI18N
    private static final String DETAILED_TOOL_NAME = loc("THAMonitor.DetailedToolName"); // NOI18N

    public DLightToolConfiguration create() {
        final DLightToolConfiguration toolConfiguration = new DLightToolConfiguration(ID, TOOL_NAME);
        toolConfiguration.setLongName(DETAILED_TOOL_NAME);
        toolConfiguration.setVisible(false);
        toolConfiguration.setIcon("org/netbeans/modules/dlight/tha/resources/bomb24.png"); // NOI18N

        // SunStudio should collect deadlocks data i.e. create a configuration
        // that collects CollectedInfo.DEADLOCKS
        SunStudioDCConfiguration ssDeadlocks =
                new SunStudioDCConfiguration(SunStudioDCConfiguration.CollectedInfo.DEADLOCKS);
        toolConfiguration.addDataCollectorConfiguration(ssDeadlocks);
        toolConfiguration.addIndicatorDataProviderConfiguration(ssDeadlocks);

        SunStudioDCConfiguration ssRaces =
                new SunStudioDCConfiguration(SunStudioDCConfiguration.CollectedInfo.DATARACES);
        toolConfiguration.addDataCollectorConfiguration(ssRaces);
        toolConfiguration.addIndicatorDataProviderConfiguration(ssRaces);

        IndicatorMetadata indicatorMetadata = new IndicatorMetadata(Arrays.asList(
                SunStudioDCConfiguration.c_Datarace,
                SunStudioDCConfiguration.c_Deadlocks));

        IndicatorConfiguration indicatorConfiguration = new THAIndicatorConfiguration(indicatorMetadata);
        
        DeadlockVisualizerConfiguration deadlockVisualizerConfiguration = new DeadlockVisualizerConfiguration();
        indicatorConfiguration.addVisualizerConfiguration(deadlockVisualizerConfiguration);

        RacesVisualizerConfiguration dataracesVisualizerConfiguration = new RacesVisualizerConfiguration();
        indicatorConfiguration.addVisualizerConfiguration(dataracesVisualizerConfiguration);

        toolConfiguration.addIndicatorConfiguration(indicatorConfiguration);

        return toolConfiguration;
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(
                DLightTHAToolConfigurationProvider.class, key, params);
    }
}
