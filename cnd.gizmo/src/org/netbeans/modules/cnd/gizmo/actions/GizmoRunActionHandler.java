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
package org.netbeans.modules.cnd.gizmo.actions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import org.netbeans.api.extexecution.ExecutionDescriptor.LineConvertorFactory;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.gizmo.GizmoConfigurationOptions;
import org.netbeans.modules.cnd.gizmo.GizmoServiceInfo;
import org.netbeans.modules.cnd.gizmo.GizmoToolsController;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandler;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.dlight.api.execution.DLightTargetChangeEvent;
import org.netbeans.modules.dlight.api.execution.DLightTargetListener;
import org.netbeans.modules.dlight.api.execution.DLightToolkitManagement;
import org.netbeans.modules.dlight.api.execution.DLightToolkitManagement.DLightSessionHandler;
import org.netbeans.modules.dlight.api.support.NativeExecutableTarget;
import org.netbeans.modules.dlight.api.support.NativeExecutableTargetConfiguration;
import org.netbeans.modules.dlight.api.tool.DLightConfiguration;
import org.netbeans.modules.dlight.api.tool.DLightConfigurationManager;
import org.netbeans.modules.dlight.api.tool.DLightConfigurationOptions;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ExternalTerminalProvider;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * @author Alexey Vladykin
 */
public class GizmoRunActionHandler implements ProjectActionHandler, DLightTargetListener {

    private static final String GNU_FAMILIY = "gc++filt"; //NOI18N
    private static final String SS_FAMILIY = "dem"; //NOI18N
    private ProjectActionEvent pae;
    private List<ExecutionListener> listeners;
    private DLightSessionHandler session;
    private InputOutput io;
    private long startTimeMillis;

    public GizmoRunActionHandler() {
        this.listeners = new CopyOnWriteArrayList<ExecutionListener>();
    }

    public void init(ProjectActionEvent pae, ProjectActionEvent[] paes) {
        this.pae = pae;
    }

    public void execute(InputOutput io) {
        MakeConfiguration conf = pae.getConfiguration();
        ExecutionEnvironment execEnv = conf.getDevelopmentHost().getExecutionEnvironment();

        Map<String, String> envVars = createMap(pae.getProfile().getEnvironment().getenvAsPairs());
        NativeExecutableTargetConfiguration targetConf = new NativeExecutableTargetConfiguration(
                pae.getExecutable(),
                pae.getProfile().getArgsArray(),
                envVars);

        String executable = pae.getExecutable();
        String runDirectory = pae.getProfile().getRunDirectory();
        if (execEnv.isRemote()) {
            PathMap mapper = HostInfoProvider.getMapper(execEnv);
            executable = mapper.getLocalPath(executable);
            runDirectory = mapper.getRemotePath(runDirectory, true);
        }

        targetConf.setExecutionEnvironment(execEnv);

        if (execEnv.isRemote() && !envVars.containsKey("DISPLAY")) { // NOI18N
            targetConf.setX11Forwarding(true);
        }

        targetConf.putInfo(ServiceInfoDataStorage.EXECUTION_ENV_KEY, ExecutionEnvironmentFactory.toUniqueID(execEnv));
        targetConf.putInfo(GizmoServiceInfo.PLATFORM, pae.getConfiguration().getDevelopmentHost().getBuildPlatformDisplayName());
        targetConf.putInfo(GizmoServiceInfo.GIZMO_PROJECT_FOLDER, FileUtil.toFile(pae.getProject().getProjectDirectory()).getAbsolutePath());//NOI18N
        targetConf.putInfo(GizmoServiceInfo.GIZMO_PROJECT_EXECUTABLE, executable);

        targetConf.putInfo("sunstudio.datafilter.collectedobjects", System.getProperty("sunstudio.datafilter.collectedobjects", "")); // NOI18N
        targetConf.putInfo("sunstudio.hotspotfunctionsfilter", System.getProperty("sunstudio.hotspotfunctionsfilter", "")); //, "with-source-code-only")); // NOI18N

        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        String binDir = compilerSet.getDirectory();
        String demangle_utility = SS_FAMILIY;
        if (compilerSet.isGnuCompiler()) {
            demangle_utility = GNU_FAMILIY;
        }
        String dem_util_path = binDir + "/" + demangle_utility; //NOI18N BTW: isn't it better to use File.Separator?
        targetConf.putInfo(GizmoServiceInfo.GIZMO_DEMANGLE_UTILITY, dem_util_path);

        targetConf.setWorkingDirectory(runDirectory);
        int consoleType = pae.getProfile().getConsoleType().getValue();
        if (consoleType == RunProfile.CONSOLE_TYPE_DEFAULT) {
            consoleType = RunProfile.getDefaultConsoleType();
        }
        if (consoleType == RunProfile.CONSOLE_TYPE_EXTERNAL) {
            String termPath = pae.getProfile().getTerminalPath();
            if (termPath != null) {
                String termBaseName = IpeUtils.getBaseName(termPath);
                if (ExternalTerminalProvider.getSupportedTerminalIDs().contains(termBaseName)) {
                    targetConf.useExternalTerminal(ExternalTerminalProvider.getTerminal(execEnv, termBaseName));
                }
            }
        }
        this.io = io;
        targetConf.setIO(io);

        // Setup simple output convertor factory...
        targetConf.setOutConvertorFactory(new SimpleOutputConvertorFactory());

        DLightConfiguration configuration = DLightConfigurationManager.getInstance().getConfigurationByName("Gizmo");//NOI18N
        DLightConfigurationOptions options = configuration.getConfigurationOptions(false);
        if (options instanceof GizmoConfigurationOptions) {
            ((GizmoConfigurationOptions) options).configure(pae.getProject());
        }

        NativeExecutableTarget target = new NativeExecutableTarget(targetConf);
        target.addTargetListener(this);


        //WE are here only when Profile On RUn 
        final Future<DLightSessionHandler> handle = DLightToolkitManagement.getInstance().createSession(
                target, configuration, IpeUtils.getBaseName(pae.getExecutable()));

        DLightExecutorService.submit(new Runnable() {

            public void run() {
                try {
                    session = handle.get();
                    DLightToolkitManagement.getInstance().startSession(session);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }, "DLight Session for " + target.toString()); // NOI18N
    }

    private Map<String, String> createMap(String[][] array) {
        Map<String, String> result = new HashMap<String, String>();
        for (int i = 0; i < array.length; ++i) {
            result.put(array[i][0], array[i][1]);
        }
        return result;
    }

    public boolean canCancel() {
        return true;
    }

    public void cancel() {
        if (session != null) {
            DLightToolkitManagement.getInstance().stopSession(session);
            session = null;
        }
    }

    public void addExecutionListener(ExecutionListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public void removeExecutionListener(ExecutionListener l) {
        listeners.remove(l);
    }

    public void targetStateChanged(DLightTargetChangeEvent event) {
        switch (event.state) {
            case INIT:
            case STARTING:
                break;
            case RUNNING:
                targetStarted(event.status);
                break;
            case FAILED:
            case STOPPED:
                targetFailed();
                break;
            case TERMINATED:
                targetFinished(event.status);
                break;
            case DONE:
                targetFinished(event.status);
                break;
        }
    }

    private void targetStarted(int pid) {
        startTimeMillis = System.currentTimeMillis();
        for (ExecutionListener l : listeners) {
            l.executionStarted(pid);
        }
    }

    private void targetFailed() {
        StatusDisplayer.getDefault().setStatusText(
                getMessage("Status.RunFailedToStart")); // NOI18N
        io.getErr().println(getMessage("Output.RunFailedToStart")); // NOI18N);

        for (ExecutionListener l : listeners) {
            l.executionFinished(-1);
        }
    }

    private void targetFinished(Integer status) {
        int exitCode = -1;

        io.getOut().println();

        if (status == null) {
            StatusDisplayer.getDefault().setStatusText(getMessage("Status.RunTerminated")); // NOI18N
            io.getErr().println(getMessage("Output.RunTerminated")); // NOI18N
        } else {
            exitCode = status.intValue();
            boolean success = exitCode == 0;

            StatusDisplayer.getDefault().setStatusText(
                    getMessage(success ? "Status.RunSuccessful" : "Status.RunFailed")); // NOI18N

            String time = formatTime(System.currentTimeMillis() - startTimeMillis);
            if (success) {
                io.getOut().println(getMessage("Output.RunSuccessful", time)); // NOI18N);
            } else {
                io.getErr().println(getMessage("Output.RunFailed", exitCode, time)); // NOI18N
            }
        }

        for (ExecutionListener l : listeners) {
            l.executionFinished(exitCode);
        }

        // TODO: hack!!! fix me!
        // see THAActionProvider .... we need a way to diable THA tool...
        // Should we do this after run???
        // Need to think-over

        GizmoToolsController.getDefault().disableTool(pae.getProject(), "dlight.tool.tha"); // NOI18N
    }

    private static String formatTime(long millis) {
        StringBuilder buf = new StringBuilder();
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        if (hours > 0) {
            buf.append(' ').append(hours).append(getMessage("Time.Hour")); // NOI18N
        }
        if (minutes > 0) {
            buf.append(' ').append(minutes % 60).append(getMessage("Time.Minute")); // NOI18N
        }
        if (seconds > 0) {
            buf.append(' ').append(seconds % 60).append(getMessage("Time.Second")); // NOI18N
        }
        if (hours == 0 && minutes == 0 && seconds == 0) {
            buf.append(' ').append(millis).append(getMessage("Time.Millisecond")); // NOI18N
        }
        return buf.toString();
    }

    private static String getMessage(String name, Object... params) {
        return NbBundle.getMessage(GizmoRunActionHandler.class, name, params);
    }

    private static class SimpleOutputConvertorFactory implements LineConvertorFactory {

        public LineConvertor newLineConvertor() {
            return LineConvertors.proxy(LineConvertors.filePattern(null, Pattern.compile("^file://([^:]*[^ ])(:)([0-9]*).*"), null, 1, 3), // NOI18N
                    LineConvertors.httpUrl());
        }
    }
}
