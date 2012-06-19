/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.discovery.projectimport;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor.LineConvertorFactory;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.*;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.spi.toolchain.CompilerLineConvertor;
import org.netbeans.modules.cnd.spi.toolchain.ToolchainProject;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.*;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionService;
import org.netbeans.modules.nativeexecution.api.execution.PostMessageDisplayer;
import org.netbeans.modules.nativeexecution.api.util.*;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Alexander Simon
 */
public class ExecuteCommand {

    private final String runDir;
    private String command;
    private final Project project;
    private final ExecutionEnvironment execEnv;

    public ExecuteCommand(Project project, String runDir, String command) {
        this.runDir = runDir;
        this.command = command;
        this.project = project;
        this.execEnv = getExecutionEnvironment();

    }
    
    public Future<Integer> performAction(ExecutionListener listener, Writer outputListener, List<String> additionalEnvironment) {
        NativeExecutionService es = prepare(listener, outputListener, additionalEnvironment);
        if (es != null) {
            return es.run();
        }
        return null;
    }

    private NativeExecutionService prepare(ExecutionListener listener, Writer outputListener, List<String> additionalEnvironment) {
        final HostInfo hostInfo;
        try {
            hostInfo = HostInfoUtils.getHostInfo(execEnv);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        } catch (CancellationException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        // Executable
        String executable = hostInfo.getShell();
        if (command.indexOf("${MAKE}") >= 0) { //NOI18N
            String make = "make"; //NOI18N
            CompilerSet compilerSet = getCompilerSet();
            if (compilerSet != null) {
                Tool findTool = compilerSet.findTool(PredefinedToolKind.MakeTool);
                if (findTool != null && findTool.getPath() != null && findTool.getPath().length() > 0) {
                    make = findTool.getPath();
                    if (hostInfo.getOSFamily() == HostInfo.OSFamily.WINDOWS) {
                        String aMake = WindowsSupport.getInstance().convertToShellPath(make);
                        if (aMake != null && aMake.length() > 0) {
                            make = aMake;
                        }
                    }
                }
            }
            command = command.replace("${MAKE}", make); //NOI18N
        }
        // Arguments
        String[] args = new String[]{"-c", command}; // NOI18N
        // Build directory
        String buildDir = convertToRemoteIfNeeded(runDir);
        if (buildDir == null) {
            ImportProject.logger.log(Level.INFO, "Run folder folder is null"); //NOI18N
            return null;
        }
        Map<String, String> envMap = getEnv(additionalEnvironment);
        if (isSunStudio()) {
            envMap.put("SPRO_EXPAND_ERRORS", ""); // NOI18N
        }
        InputOutput inputOutput = null;
        if (inputOutput == null) {
            // Tab Name
            String tabName = execEnv.isLocal() ? NbBundle.getMessage(ExecuteCommand.class, "MAKE_LABEL", command) : // NOI18N
                                                 NbBundle.getMessage(ExecuteCommand.class, "MAKE_REMOTE_LABEL", command, execEnv.getDisplayName()); // NOI18N
            InputOutput _tab = IOProvider.getDefault().getIO(tabName, false); // This will (sometimes!) find an existing one.
            _tab.closeInputOutput(); // Close it...
            InputOutput tab = IOProvider.getDefault().getIO(tabName, true); // Create a new ...
            try {
                tab.getOut().reset();
            } catch (IOException ioe) {
            }
            inputOutput = tab;
        }
        RemoteSyncWorker syncWorker = RemoteSyncSupport.createSyncWorker(project, inputOutput.getOut(), inputOutput.getErr());
        if (syncWorker != null) {
            if (!syncWorker.startup(envMap)) {
                ImportProject.logger.log(Level.INFO, "RemoteSyncWorker is not started up"); //NOI18N
                return null;
            }
        }

        MacroMap mm = MacroMap.forExecEnv(execEnv);
        mm.putAll(envMap);

        if (envMap.containsKey("__CND_TOOLS__")) { // NOI18N
            try {
                if (BuildTraceHelper.isMac(execEnv)) {
                    String what = BuildTraceHelper.INSTANCE.getLibraryName(execEnv);
                    if (what.indexOf(':') > 0) {
                        what = what.substring(0,what.indexOf(':'));
                    }
                    String where = BuildTraceHelper.INSTANCE.getLDPaths(execEnv);
                    if (where.indexOf(':') > 0) {
                        where = where.substring(0,where.indexOf(':'));
                    }
                    String lib = where+'/'+what;
                    mm.prependPathVariable(BuildTraceHelper.getLDPreloadEnvName(execEnv),lib);
                } else {
                    mm.prependPathVariable(BuildTraceHelper.getLDPreloadEnvName(execEnv), BuildTraceHelper.INSTANCE.getLibraryName(execEnv));
                    mm.prependPathVariable(BuildTraceHelper.getLDPathEnvName(execEnv), BuildTraceHelper.INSTANCE.getLDPaths(execEnv));
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        traceExecutable(executable, buildDir, args, execEnv.toString(), mm.toMap());
        ProcessChangeListener processChangeListener = new ProcessChangeListener(listener, outputListener,
                new CompilerLineConvertor(project, getCompilerSet(), execEnv, RemoteFileUtil.getFileObject(buildDir, execEnv)), syncWorker); // NOI18N

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv).
                setExecutable(executable).
                setWorkingDirectory(buildDir).
                setArguments(args).
                unbufferOutput(false).
                addNativeProcessListener(processChangeListener);

        npb.getEnvironment().putAll(mm);
        npb.redirectError();

        NativeExecutionDescriptor descr = new NativeExecutionDescriptor().controllable(true).
                frontWindow(true).
                inputVisible(true).
                showProgress(!CndUtils.isStandalone()).
                inputOutput(inputOutput).
                outLineBased(true).
                postExecution(processChangeListener).
                postMessageDisplayer(new PostMessageDisplayer.Default("Make")). // NOI18N
                errConvertorFactory(processChangeListener).
                outConvertorFactory(processChangeListener);

        return NativeExecutionService.newService(npb, descr, "make"); // NOI18N
    }
    
    private CompilerSet getCompilerSet() {
        CompilerSet set = null;
        ToolchainProject toolchain = project.getLookup().lookup(ToolchainProject.class);
        if (toolchain != null) {
            set = toolchain.getCompilerSet();
        }
        if (set == null) {
            set = CompilerSetManager.get(ExecutionEnvironmentFactory.getLocal()).getDefaultCompilerSet();
        }
        return set;
    }

    private boolean isSunStudio() {
        CompilerSet set = getCompilerSet();
        if (set == null) {
            return false;
        }
        return set.getCompilerFlavor().isSunStudioCompiler();
    }

    private static final class BuildTraceHelper extends HelperLibraryUtility {

        private static final BuildTraceHelper INSTANCE = new BuildTraceHelper();

        private BuildTraceHelper() {
            super("org.netbeans.modules.cnd.actions", "bin/${osname}-${platform}${_isa}/libBuildTrace.${soext}"); // NOI18N
        }
    }

    private ExecutionEnvironment getExecutionEnvironment() {
        RemoteProject info = project.getLookup().lookup(RemoteProject.class);
        if (info != null) {
            return info.getDevelopmentHost();
        }
        return ExecutionEnvironmentFactory.getLocal();
    }

    private String convertToRemoteIfNeeded(String localDir) {
        if (!checkConnection()) {
            return null;
        }
        if (execEnv.isRemote()) {
            final PathMap pathMap = RemoteSyncSupport.getPathMap(execEnv, project);
            String remotePath = pathMap.getRemotePath(localDir, false);
            if (remotePath == null) {
                if (!pathMap.checkRemotePaths(new File[]{new File(localDir)}, true)) {
                    return null;
                }
                remotePath = pathMap.getRemotePath(localDir, false);
            }
            return remotePath;
        }
        return localDir;
    }

    private boolean checkConnection() {
        if (execEnv.isRemote()) {
            try {
                ConnectionManager.getInstance().connectTo(execEnv);
                ServerRecord record = ServerList.get(execEnv);
                if (record.isOffline()) {
                    record.validate(true);
                }
                return record.isOnline();
            } catch (IOException ex) {
                return false;
            } catch (CancellationException ex) {
                return false;
            }
        } else {
            return true;
        }
    }

    private void traceExecutable(String executable, String buildDir, String[] args, String host, Map<String, String> envMap) {
        StringBuilder argsFlat = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            argsFlat.append(" "); // NOI18N
            argsFlat.append(args[i]);
        }
        traceExecutable(executable, buildDir, argsFlat, host, envMap);
    }

    private void traceExecutable(String executable, String buildDir, StringBuilder argsFlat, String host, Map<String, String> envMap) {
        StringBuilder buf = new StringBuilder("Run " + executable); // NOI18N
        buf.append("\n\tin folder   ").append(buildDir); // NOI18N
        buf.append("\n\targuments   ").append(argsFlat); // NOI18N
        buf.append("\n\thost        ").append(host); // NOI18N
        buf.append("\n\tenvironment "); // NOI18N
        for (Map.Entry<String, String> v : envMap.entrySet()) {
            buf.append("\n\t\t").append(v.getKey()).append("=").append(v.getValue()); // NOI18N
        }
        buf.append("\n"); // NOI18N
        ImportProject.logger.log(Level.INFO, buf.toString());
    }

    private Map<String, String> getEnv(List<String> additionalEnvironment) {
        Map<String, String> envMap = new HashMap<String, String>(getDefaultEnvironment());
        if (additionalEnvironment != null) {
            envMap.putAll(parseEnvironmentVariables(additionalEnvironment));
        }
        return envMap;
    }

    private Map<String, String> parseEnvironmentVariables(Collection<String> vars) {
        if (vars.isEmpty()) {
            return Collections.emptyMap();
        } else {
            Map<String, String> envMap = new HashMap<String, String>();
            for (String s : vars) {
                int i = s.indexOf('='); // NOI18N
                if (i > 0) {
                    String key = s.substring(0, i);
                    String value = s.substring(i + 1).trim();
                    if (value.length() > 1 && (value.startsWith("\"") && value.endsWith("\"") || // NOI18N
                            value.startsWith("'") && value.endsWith("'"))) { // NOI18N
                        value = value.substring(1, value.length() - 1);
                    }
                    envMap.put(key, value);
                }
            }
            return envMap;
        }
    }
    
    private Map<String, String> getDefaultEnvironment() {
        PlatformInfo pi = PlatformInfo.getDefault(execEnv);
        String defaultPath = pi.getPathAsString();
        CompilerSet cs = getCompilerSet();
        if (cs != null) {
            defaultPath = cs.getDirectory() + pi.pathSeparator() + defaultPath;
            // TODO Provide platform info
            String cmdDir = cs.getCompilerFlavor().getCommandFolder(pi.getPlatform());
            if (cmdDir != null && 0 < cmdDir.length()) {
                // Also add msys to path. Thet's where sh, mkdir, ... are.
                defaultPath = cmdDir + pi.pathSeparator() + defaultPath;
            }
        }
        return Collections.singletonMap(pi.getPathName(), defaultPath);
    }
    
    
    private static final class ProcessChangeListener implements ChangeListener, Runnable, LineConvertorFactory {

        private final AtomicReference<NativeProcess> processRef = new AtomicReference<NativeProcess>();
        private final ExecutionListener listener;
        private Writer outputListener;
        private final LineConvertor lineConvertor;
        private final RemoteSyncWorker syncWorker;

        public ProcessChangeListener(ExecutionListener listener, Writer outputListener, LineConvertor lineConvertor, RemoteSyncWorker syncWorker) {
            this.listener = listener;
            this.outputListener = outputListener;
            this.lineConvertor = lineConvertor;
            this.syncWorker = syncWorker;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if (!(e instanceof NativeProcessChangeEvent)) {
                return;
            }

            final NativeProcessChangeEvent event = (NativeProcessChangeEvent) e;
            processRef.compareAndSet(null, (NativeProcess) event.getSource());

            if (NativeProcess.State.RUNNING == event.state) {
                if (listener != null) {
                    listener.executionStarted(event.pid);
                }
            }
        }

        @Override
        public void run() {
            closeOutputListener();

            NativeProcess process = processRef.get();
            try {
                if (process != null && listener != null) {
                    listener.executionFinished(process.exitValue());
                }
            } finally {
                if (syncWorker != null) {
                    syncWorker.shutdown();
                }
            }
        }

        @Override
        public LineConvertor newLineConvertor() {
            return new LineConvertor() {

                @Override
                public List<ConvertedLine> convert(String line) {
                    return ProcessChangeListener.this.convert(line);
                }
            };
        }

        private synchronized void closeOutputListener() {
            if (outputListener != null) {
                try {
                    outputListener.flush();
                    outputListener.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                outputListener = null;
            }
        }

        private synchronized List<ConvertedLine> convert(String line) {
            if (outputListener != null) {
                try {
                    outputListener.write(line);
                    outputListener.write("\n"); // NOI18N
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (lineConvertor != null) {
                return lineConvertor.convert(line);
            }
            return null;
        }
    }
}
