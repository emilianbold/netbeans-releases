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

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.gizmo.api.GizmoOptionsProvider;
import org.netbeans.modules.cnd.gizmo.spi.GizmoOptions;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.dlight.api.tool.DLightConfigurationOptions;
import org.netbeans.modules.dlight.api.tool.DLightTool;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;

/**
 *
 * @author mt154047
 */
public class GizmoConfigurationOptions implements DLightConfigurationOptions {

    private String DLightCollectorString = "SunStudio";//NOI18N
    private List<String> DLightIndicatorDPStrings = Arrays.asList("SunStudio");//NOI18N
    private static final String SUNSTUDIO = "SunStudio";//NOI18N
    private static final String LL_MONITOR = "LLTool";//NOI18N
    private static final String DTRACE = "DTrace";//NOI18N
    private static final String PRSTAT_INDICATOR = "prstat";//NOI18N
    private Project currentProject;
    private boolean areCollectorsTurnedOn = false;
    private boolean profileOnRun = true;
    private GizmoOptions gizmoOptions = null;

    public void turnCollectorsState(boolean turnState) {
        areCollectorsTurnedOn = turnState;
    }

    public boolean profileOnRun() {
        return profileOnRun;
    }

    public Collection<String> getActiveToolNames() {
        if (gizmoOptions == null){
            return null;
        }
        Collection<String> result = new ArrayList<String>();
        Collection<String> allNames = gizmoOptions.getNames();
        for (String name : allNames){
            if (gizmoOptions.getValueByName(name)){
                result.add(name);
            }
        }
        return result;
    }

    

    public void configure(Project project){
        this.currentProject = project;
//        GizmoProjectOptions options = new GizmoProjectOptions(currentProject);
        //set up as following:
        //get data from the project about selected provider of detailed voew
        Configuration activeConfiguration = getActiveConfiguration();
        gizmoOptions = GizmoOptionsProvider.getOptions(activeConfiguration);
        turnCollectorsState(true);
        profileOnRun = gizmoOptions.getProfileOnRunValue();
        String hkey = null;
        if (!(activeConfiguration instanceof MakeConfiguration)) {
            return;
        }

        hkey = ((MakeConfiguration) activeConfiguration).getDevelopmentHost().getName();
        //if we have sun studio compiler along compiler collections presentedCompiler
        CompilerSetManager compilerSetManager = CompilerSetManager.getDefault(((MakeConfiguration) activeConfiguration).getDevelopmentHost().getExecutionEnvironment());
        List<CompilerSet> compilers = compilerSetManager.getCompilerSets();
        boolean hasSunStudio = false;
        for (CompilerSet cs : compilers){
            if (cs.isSunCompiler()){
                hasSunStudio = true;
                break;
            }
        }

        GizmoOptions.DataProvider currentProvider = gizmoOptions.getDataProviderValue();

       
        DLightCollectorString = DTRACE;
        DLightIndicatorDPStrings = Arrays.asList(PRSTAT_INDICATOR, DTRACE);

        if (hasSunStudio && currentProvider == GizmoOptions.DataProvider.SUN_STUDIO) {//NOI18N
            DLightCollectorString = SUNSTUDIO;
            DLightIndicatorDPStrings = new ArrayList<String>();
            DLightIndicatorDPStrings.add(SUNSTUDIO);
            DLightIndicatorDPStrings.add(PRSTAT_INDICATOR);
        } else {

            ExecutionEnvironment execEnv = ((MakeConfiguration) activeConfiguration).getDevelopmentHost().getExecutionEnvironment();
            if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
                try {
                    ConnectionManager.getInstance().connectTo(execEnv);
                } catch (IOException ex) {
                    DLightLogger.instance.warning(ex.toString());
                }
            }
            try {
                String osName = HostInfoUtils.getOS(execEnv);
                if (osName.indexOf("Linux") != -1 || osName.equals("MacOS")){//NOI18N
                    DLightCollectorString = LL_MONITOR;
                    DLightIndicatorDPStrings = Arrays.asList(LL_MONITOR);
                }
            } catch (ConnectException ex) {
                //Exceptions.printStackTrace(ex);
            }
        }
    }

    private Configuration getActiveConfiguration() {
        return ConfigurationSupport.getProjectDescriptor(currentProject).getConfs().getActive();
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
            for (String idpStringName : DLightIndicatorDPStrings){
                if (idp.getName().equals(idpStringName)) {
                    result.add(idp);
                }
            }
        }
        return result;
    }

    public boolean validateToolsRequiredUserInteraction() {
//        GizmoProjectOptions options = new GizmoProjectOptions(currentProject);
//        return options.getUserInteractionRequiredActionsEnabled();
        return false;
    }
}
