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
package org.netbeans.modules.cnd.gizmo;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.dlight.api.tool.DLightConfigurationOptions;
import org.netbeans.modules.dlight.api.tool.DLightTool;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.util.Exceptions;

/**
 *
 * @author mt154047
 */
public class GizmoConfigurationOptions implements DLightConfigurationOptions {

    private String DLightCollectorString = "SunStudio";
    private String DLightIndicatorDPString = "SunStudio";
    private static final String SUNSTUDIO = "SunStudio";
    private static final String LL_Collectors = "ll";
    private static final String Dtrace_Collectors = "ll";
    private static final String prstate_indicators = "ll";
    private Project currentProject;
    private boolean areCollectorsTurnedOn = false;

    public void turnCollectorsState(boolean turnState) {
        areCollectorsTurnedOn = turnState;
    }

    public void configure(Project project) {
        //set up as following:
        //get data from the project about selected provider of detailed voew
        Configuration activeConfiguration = getActiveConfiguration();
        String hkey = null;
        if (!(activeConfiguration instanceof MakeConfiguration)) {
            return;
        }
        hkey = ((MakeConfiguration) activeConfiguration).getDevelopmentHost().getName();
        //if we have sun studio compiler along compiler collections presented
        if (((MakeConfiguration) activeConfiguration).getCompilerSet().getCompilerSet().isSunCompiler()) {
            DLightCollectorString = SUNSTUDIO;
            DLightIndicatorDPString = SUNSTUDIO;
        } else {
            try {
                if (HostInfoUtils.getOS(((MakeConfiguration) activeConfiguration).getDevelopmentHost().getExecutionEnvironment()).indexOf("linux") != -1) { //we are on Linux
                    DLightCollectorString = LL_Collectors;
                    DLightIndicatorDPString = LL_Collectors;
                }
            } catch (ConnectException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                DLightCollectorString = Dtrace_Collectors;
                DLightIndicatorDPString = prstate_indicators;
            }
        }
    }

    private Configuration getActiveConfiguration() {
        return ConfigurationSupport.getProjectDescriptor(currentProject).getConfs().getActive();
    }

    public boolean getCollectorsState() {
        return areCollectorsTurnedOn;
    }

    public List<DataCollector> getCollectors(DLightTool tool) {
        List<DataCollector> collectors = tool.getCollectors();
        List<DataCollector> result = new ArrayList<DataCollector>();
        for (DataCollector collector : collectors) {
            if (collector.getName().equals(DLightCollectorString)) {
                result.add(collector);
            }
        }
        return result;
    }

    public List<IndicatorDataProvider> getIndicatorDataProviders(DLightTool tool) {
        List<IndicatorDataProvider> idps = tool.getIndicatorDataProviders();
        List<IndicatorDataProvider> result = new ArrayList<IndicatorDataProvider>();
        for (IndicatorDataProvider idp : idps) {
            if (idp.getName().equals(DLightIndicatorDPString)) {
                result.add(idp);
            }
        }
        return result;
    }
}
