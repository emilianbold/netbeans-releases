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
package org.netbeans.modules.cnd.tha;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.gizmo.support.GizmoServiceInfo;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.dlight.api.tool.DLightConfiguration;
import org.netbeans.modules.dlight.api.tool.DLightConfigurationManager;
import org.netbeans.modules.dlight.api.tool.DLightConfigurationOptions;
import org.netbeans.modules.dlight.api.tool.DLightConfigurationOptionsListener;
import org.netbeans.modules.dlight.api.tool.DLightTool;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.dlight.util.DLightLogger;

/**
 *
 * @author mt154047
 */
public final class THAConfigurationOptions implements DLightConfigurationOptions {

        private final List<DLightConfigurationOptionsListener> listeners = new ArrayList<DLightConfigurationOptionsListener>();
    private static Logger log = DLightLogger.getLogger(THAConfigurationOptions.class);
    private String DLightCollectorString = "SunStudio";//NOI18N
    private List<String> DLightIndicatorDPStrings = Arrays.asList("SunStudio");//NOI18N
    private static final String SUNSTUDIO = "SunStudio";//NOI18N
//    private static final String DTRACE = "DTrace";//NOI18N
//    private static final String PRSTAT_INDICATOR = "prstat";//NOI18N
    private static final String PROC_READER = "ProcReader";//NOI18N
    private static final String PROCFS_READER = "ProcFSReader";//NOI18N
    private Project currentProject;
    private boolean areCollectorsTurnedOn = false;
    private boolean profileOnRun = true;


    public void turnCollectorsState(boolean turnState) {
        areCollectorsTurnedOn = turnState;
    }

    public boolean profileOnRun() {
        return profileOnRun;
    }

    public Collection<String> getActiveToolNames() {
        DLightConfiguration configuration = DLightConfigurationManager.getInstance().getConfigurationByName("THA");//NOI18N
        List<DLightTool> tools = configuration.getToolsSet();
        Collection<String> result = new ArrayList<String>();
        for (DLightTool tool : tools) {
            result.add(tool.getID());
        }
        return result;
    }

    public void configure(Project project) {
        areCollectorsTurnedOn = true;
        this.currentProject = project;
        Configuration activeConfiguration = getActiveConfiguration();
//        GizmoProjectOptions options = new GizmoProjectOptions(currentProject);
        //set up as following:
        //get data from the project about selected provider of detailed voew

        turnCollectorsState(true);

        if (!(activeConfiguration instanceof MakeConfiguration)) {
            return;
        }

        //if we have sun studio compiler along compiler collections presentedCompiler
        CompilerSetManager compilerSetManager = CompilerSetManager.getDefault(((MakeConfiguration) activeConfiguration).getDevelopmentHost().getExecutionEnvironment());
        List<CompilerSet> compilers = compilerSetManager.getCompilerSets();

        boolean hasSunStudio = false;

        for (CompilerSet cs : compilers) {
            if (cs.isSunCompiler()) {
                hasSunStudio = true;
                break;
            }
        }

        String platform = ((MakeConfiguration) getActiveConfiguration()).getDevelopmentHost().getBuildPlatformDisplayName();
        DLightConfiguration dlightConfiguration = DLightConfigurationManager.getInstance().getConfigurationByName("THA");//NOI18N
        //get all names from the inside usinf providers
//        GizmoOptions.DataProvider currentProvider = gizmoOptions.getDataProviderValue();
        DLightCollectorString = dlightConfiguration.getCollectorProviders();
        DLightIndicatorDPStrings = dlightConfiguration.getIndicatorProviders();

        if ((DLightCollectorString != null && DLightCollectorString.equals(SUNSTUDIO) && !hasSunStudio) ||
                (platform.indexOf("Linux") != -1 && DLightCollectorString != null //NOI18N
                && !DLightCollectorString.equals(SUNSTUDIO)) || !GizmoServiceInfo.isPlatformSupported(platform)) {
            setForLinux();
        }

    }

    private boolean setForLinux() {
        String platform = ((MakeConfiguration) getActiveConfiguration()).getDevelopmentHost().getBuildPlatformDisplayName();
        if (platform.indexOf("Linux") != -1 || !GizmoServiceInfo.isPlatformSupported(platform)) {//NOI18N
            areCollectorsTurnedOn = false;
            if (platform.indexOf("Linux") != -1) {//NOI18N
                DLightCollectorString = SUNSTUDIO;
            } else {
                DLightCollectorString = "";//NOI18N
            }

            DLightIndicatorDPStrings = Arrays.asList(PROCFS_READER, PROC_READER);
            return true;
        }
        return false;
    }

    private Configuration getActiveConfiguration() {
        return ConfigurationSupport.getProjectActiveConfiguration(currentProject);
    }

    public boolean areCollectorsTurnedOn() {
        return areCollectorsTurnedOn;
    }

    public List<DataCollector<?>> getCollectors(DLightTool tool) {
        List<DataCollector<?>> collectors = tool.getCollectors();
        List<DataCollector<?>> result = new ArrayList<DataCollector<?>>();
        for (DataCollector collector : collectors) {
            if (collector.getName().equals(DLightCollectorString)) {
                result.add(collector);
            }
        }
        return result;
    }

    public List<IndicatorDataProvider<?>> getIndicatorDataProviders(DLightTool tool) {
        List<IndicatorDataProvider<?>> idps = tool.getIndicatorDataProviders();
        List<IndicatorDataProvider<?>> result = new ArrayList<IndicatorDataProvider<?>>();
        for (IndicatorDataProvider idp : idps) {
            for (String idpStringName : DLightIndicatorDPStrings) {
                if (idp.getName().equals(idpStringName)) {
                    result.add(idp);
                    break;
                }
            }
        }
        return result;
    }

    public boolean validateToolsRequiredUserInteraction() {
        return false;
    }

    public void addListener(DLightConfigurationOptionsListener listener) {

        if (listener == null) {
            return;
        }

        synchronized (this) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }

    }

    public void removeListener(DLightConfigurationOptionsListener listener) {
        synchronized (this) {
            listeners.remove(listener);
        }
    }

    private final void notifyListeners(String toolName, boolean isEnabled) {
        synchronized (this) {
            for (DLightConfigurationOptionsListener l : listeners) {
                l.dlightToolEnabling(toolName, isEnabled);
            }
        }
    }
}
