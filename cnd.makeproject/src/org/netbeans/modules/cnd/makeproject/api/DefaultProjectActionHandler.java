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
package org.netbeans.modules.cnd.makeproject.api;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor.LineConvertorFactory;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent.Type;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent.PredefinedType;
import org.netbeans.modules.cnd.spi.toolchain.CompilerLineConvertor;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.makeproject.configurations.CppUtils;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.NativeProcessChangeEvent;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionService;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ExternalTerminalProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;

public class DefaultProjectActionHandler implements ProjectActionHandler, ExecutionListener {

    private ProjectActionEvent pae;
    //private volatile ExecutorTask executorTask;
    private volatile Future<Integer> executorTask;
    private final List<ExecutionListener> listeners = new CopyOnWriteArrayList<ExecutionListener>();
    // VK: this is just to tie two pieces of logic together:
    // first is in determining the type of console for remote;
    // second is in canCancel
    private static final boolean RUN_REMOTE_IN_OUTPUT_WINDOW = true;

    @Override
    public void init(ProjectActionEvent pae, ProjectActionEvent[] paes) {
        this.pae = pae;
    }

    /**
     * Will be called to get arguments for the action. Can be overridden.
     */
    private String getArguments() {
        return pae.getProfile().getArgsFlat();
    }

    /**
     * Will be called to get the environment for the action. Can be overridden.
     */
    private String[] getEnvironment() {
        return pae.getProfile().getEnvironment().getenv();
    }

    @Override
    public void execute(final InputOutput io) {
        if (SwingUtilities.isEventDispatchThread()) {
            RequestProcessor.getDefault().post(new Runnable() {

                @Override
                public void run() {
                    _execute(io);
                }
            });
        } else {
            _execute(io);
        }
    }

    private void _execute(final InputOutput io) {
        final Type actionType = pae.getType();

        if (actionType != ProjectActionEvent.PredefinedType.RUN
                && actionType != ProjectActionEvent.PredefinedType.BUILD
                && actionType != ProjectActionEvent.PredefinedType.CLEAN
                && actionType != ProjectActionEvent.PredefinedType.BUILD_TESTS
                && actionType != ProjectActionEvent.PredefinedType.TEST) {
            assert false;
        }

        final String runDirectory = pae.getProfile().getRunDirectory();
        final MakeConfiguration conf = pae.getConfiguration();
        final PlatformInfo pi = conf.getPlatformInfo();
        final ExecutionEnvironment execEnv = conf.getDevelopmentHost().getExecutionEnvironment();

        String exe = pae.getExecutable(); // we don't need quoting - it's execution responsibility
        // we don't need quoting - it's execution responsibility
        ArrayList<String> args = new ArrayList<String>(Arrays.asList(pae.getProfile().getArgsArray()));
        Map<String, String> env = pae.getProfile().getEnvironment().getenvAsMap();
        boolean showInput = actionType == ProjectActionEvent.PredefinedType.RUN;
        boolean unbuffer = false;
        boolean runInInternalTerminal = false;

        int consoleType = pae.getProfile().getConsoleType().getValue();

        if (actionType == ProjectActionEvent.PredefinedType.RUN) {
            runInInternalTerminal = consoleType == RunProfile.CONSOLE_TYPE_INTERNAL;

            if (pae.getProfile().getTerminalType() == null || pae.getProfile().getTerminalPath() == null) {
                String errmsg;
                if (Utilities.isMac()) {
                    errmsg = getString("Err_NoTermFoundMacOSX");
                } else {
                    errmsg = getString("Err_NoTermFound");
                }
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errmsg));
                consoleType = RunProfile.CONSOLE_TYPE_OUTPUT_WINDOW;
            }

            if (!conf.getDevelopmentHost().isLocalhost()) {
                if (RUN_REMOTE_IN_OUTPUT_WINDOW && !runInInternalTerminal) {
                    //TODO: only output window for remote for now
                    consoleType = RunProfile.CONSOLE_TYPE_OUTPUT_WINDOW;
                }
            }

            if (consoleType == RunProfile.CONSOLE_TYPE_OUTPUT_WINDOW) {
                if (pi.getPlatform() == PlatformTypes.PLATFORM_WINDOWS) {
                    exe = CndPathUtilitities.naturalize(exe);
                } else if (conf.getDevelopmentHost().isLocalhost()) {
                    exe = CndPathUtilitities.toAbsolutePath(runDirectory, exe);
                }
                unbuffer = true;
            } else if (!runInInternalTerminal) {
                showInput = false;
                if (consoleType == RunProfile.CONSOLE_TYPE_DEFAULT) {
                    consoleType = RunProfile.getDefaultConsoleType();
                }
            }

            // Append compilerset base to run path. (IZ 120836)
            CompilerSet cs = conf.getCompilerSet().getCompilerSet();
            if (cs != null) {
                String csdirs = cs.getDirectory();
                String commands = cs.getCompilerFlavor().getCommandFolder(conf.getDevelopmentHost().getBuildPlatform());
                if (commands != null && commands.length() > 0) {
                    // Also add msys to path. Thet's where sh, mkdir, ... are.
                    csdirs = csdirs + pi.pathSeparator() + commands;
                }
                String path = env.get(pi.getPathName());
                if (path == null) {
                    path = pi.getPathAsString() + pi.pathSeparator() + csdirs;
                } else {
                    path += pi.pathSeparator() + csdirs;
                }
                env.put(pi.getPathName(), path);
            }
        } else { // Build or Clean
            // Build or Clean
            final CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
            String csdirs = compilerSet.getDirectory();
            String commands = compilerSet.getCompilerFlavor().getCommandFolder(conf.getDevelopmentHost().getBuildPlatform());
            if (commands != null && commands.length() > 0) {
                // Also add msys to path. Thet's where sh, mkdir, ... are.
                csdirs = csdirs + pi.pathSeparator() + commands;
            }
            String path = env.get(pi.getPathName());
            if (path == null) {
                path = csdirs + pi.pathSeparator() + pi.getPathAsString();
            } else {
                path = csdirs + pi.pathSeparator() + path;
            }
            env.put(pi.getPathName(), path);
            // Pass QMAKE from compiler set to the Makefile (IZ 174731)
            if (conf.isQmakeConfiguration()) {
                String qmakePath = compilerSet.getTool(PredefinedToolKind.QMakeTool).getPath();
                qmakePath = CppUtils.normalizeDriveLetter(compilerSet, qmakePath.replace('\\', '/')); // NOI18N
                args.add("QMAKE=" + CndPathUtilitities.escapeOddCharacters(qmakePath)); // NOI18N
            }
        }

        LineConvertor converter = null;

        if (actionType == ProjectActionEvent.PredefinedType.BUILD) {
            converter = new CompilerLineConvertor(
                    conf.getCompilerSet().getCompilerSet(),
                    execEnv, FileUtil.toFileObject(new File(runDirectory)));
        }

        // TODO: this is actual only for sun studio compiler
        env.put("SPRO_EXPAND_ERRORS", ""); // NOI18N

        String workingDirectory = convertToRemoteIfNeeded(execEnv, runDirectory);

        if (workingDirectory == null) {
            // TODO: fix me
            // return null;
        }

        ProcessChangeListener processChangeListener =
                new ProcessChangeListener(this, null/*Writer outputListener*/,
                converter, io, pae.getActionName());

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv)
                .setWorkingDirectory(workingDirectory)
                .unbufferOutput(unbuffer)
                .setExecutable(exe)
                .setArguments(args.toArray(new String[args.size()]))
                .addNativeProcessListener(processChangeListener);

        npb.getEnvironment().putAll(env);

        if (actionType == PredefinedType.RUN || actionType == PredefinedType.DEBUG) {
            if (ServerList.get(execEnv).getX11Forwarding() && !env.containsKey("DISPLAY")) { //NOI18N if DISPLAY is set, let it do its work
                npb.setX11Forwarding(true);
            }
        }

        if (actionType == ProjectActionEvent.PredefinedType.RUN && consoleType == RunProfile.CONSOLE_TYPE_EXTERNAL) {
            String termPath = pae.getProfile().getTerminalPath();
            CndUtils.assertNotNull(termPath, "null terminal path"); // NOI18N; should be checked above
            if (termPath != null) {
                String termBaseName = CndPathUtilitities.getBaseName(termPath);
                if (ExternalTerminalProvider.getSupportedTerminalIDs().contains(termBaseName)) {
                    npb.useExternalTerminal(ExternalTerminalProvider.getTerminal(execEnv, termBaseName));
                }
            }
        }

        NativeExecutionDescriptor descr = new NativeExecutionDescriptor()
                .controllable(true)
                .frontWindow(true)
                .inputVisible(showInput)
                .inputOutput(io)
                .outLineBased(!unbuffer)
                .showProgress(true)
                .postExecution(processChangeListener)
                .errConvertorFactory(processChangeListener)
                .outConvertorFactory(processChangeListener);

        if (actionType == PredefinedType.BUILD) {
            descr.noReset(true);
        }

        NativeExecutionService es = NativeExecutionService.newService(npb, descr, pae.getActionName()); // NOI18N
        executorTask = es.run();
    }

    protected static String convertToRemoteIfNeeded(ExecutionEnvironment execEnv, String localDir) {
        if (!checkConnection(execEnv)) {
            return null;
        }
        if (execEnv.isRemote()) {
            return HostInfoProvider.getMapper(execEnv).getRemotePath(localDir, false);
        }
        return localDir;
    }

    protected static boolean checkConnection(ExecutionEnvironment execEnv) {
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

    @Override
    public void addExecutionListener(ExecutionListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    @Override
    public void removeExecutionListener(ExecutionListener l) {
        listeners.remove(l);
    }

    @Override
    public boolean canCancel() {
        return true;
    }

    @Override
    public void cancel() {
        RequestProcessor.getDefault().post(new Runnable() {

            @Override
            public void run() {
                Future<Integer> et = executorTask;
                if (et != null) {
                    executorTask.cancel(true);
                }
            }
        });
    }

    @Override
    public void executionStarted(int pid) {
        for (ExecutionListener l : listeners) {
            l.executionStarted(pid);
        }
    }

    @Override
    public void executionFinished(int rc) {
        for (ExecutionListener l : listeners) {
            l.executionFinished(rc);
        }
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getBundle(DefaultProjectActionHandler.class).getString(s);
    }

    protected static String getString(String key, String... a1) {
        return NbBundle.getMessage(DefaultProjectActionHandler.class, key, a1);
    }

    private static final class ProcessChangeListener implements ChangeListener, Runnable, LineConvertorFactory {

        private static final boolean showHeader = Boolean.getBoolean("cnd.execution.showheader");
        private final ExecutionListener listener;
        private Writer outputListener;
        private final LineConvertor lineConvertor;
        private final InputOutput tab;
        private final String actionName;
        private long startTimeMillis;
        private Runnable postRunnable;

        public ProcessChangeListener(ExecutionListener listener, Writer outputListener, LineConvertor lineConvertor,
                InputOutput tab, String actionName) {
            this.listener = listener;
            this.outputListener = outputListener;
            this.lineConvertor = lineConvertor;
            this.tab = tab;
            this.actionName = actionName;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if (!(e instanceof NativeProcessChangeEvent)) {
                return;
            }
            final NativeProcessChangeEvent event = (NativeProcessChangeEvent) e;
            final NativeProcess process = (NativeProcess) event.getSource();
            switch (event.state) {
                case INITIAL:
                    break;
                case STARTING:
                    break;
                case RUNNING:
                    startTimeMillis = System.currentTimeMillis();
                    if (showHeader) {
                        assert false;
                        // TODO: is it needed?
                        //String runDirToShow = execEnv.isLocal() ?
                        //    runDir : HostInfoProvider.getMapper(execEnv).getRemotePath(runDir,true);
                        //String preText = MessageFormat.format(getString("PRETEXT"),
                        //        exePlusArgsQuoted(executable, arguments), runDirToShow);
                        //err.println(preText);
                        //err.println();
                    }
                    if (listener != null) {
                        listener.executionStarted(event.pid);
                    }
                    break;
                case CANCELLED: {
                    closeOutputListener();
                    postRunnable = new Runnable() {

                        @Override
                        public void run() {
                            StringBuilder res = new StringBuilder();
                            res.append(MessageFormat.format(getString("TERMINATED"), actionName.toUpperCase())); // NOI18N
                            res.append(" ("); // NOI18N
                            if (process.exitValue() != 0) {
                                res.append(MessageFormat.format(getString("EXIT_VALUE"), process.exitValue())); // NOI18N
                                res.append(' ');
                            }
                            res.append(MessageFormat.format(getString("TOTAL_TIME"), formatTime(System.currentTimeMillis() - startTimeMillis))); // NOI18N
                            res.append(')');

                            tab.getOut().println(res.toString());
                            tab.getOut().println();
                            closeIO();

                            if (listener != null) {
                                listener.executionFinished(process.exitValue());
                            }
                            
                            StatusDisplayer.getDefault().setStatusText(MessageFormat.format("MSG_TERMINATED", actionName)); // NOI18N
                        }
                    };
                    break;
                }
                case ERROR: {
                    closeOutputListener();
                    postRunnable = new Runnable() {

                        @Override
                        public void run() {
                            StringBuilder res = new StringBuilder();
                            res.append(MessageFormat.format(getString("FAILED"), actionName.toUpperCase())); // NOI18N
                            res.append(" ("); // NOI18N
                            if (process.exitValue() != 0) {
                                res.append(MessageFormat.format(getString("EXIT_VALUE"), process.exitValue())); // NOI18N
                                res.append(' ');
                            }
                            res.append(MessageFormat.format(getString("TOTAL_TIME"), formatTime(System.currentTimeMillis() - startTimeMillis))); // NOI18N
                            res.append(')');

                            tab.getErr().println(res.toString());
                            tab.getErr().println();
                            closeIO();
                            if (listener != null) {
                                listener.executionFinished(-1);
                            }
                            StatusDisplayer.getDefault().setStatusText(MessageFormat.format(getString("MSG_FAILED"), actionName));
                        }
                    };
                    break;
                }
                case FINISHED: {
                    closeOutputListener();
                    postRunnable = new Runnable() {

                        @Override
                        public void run() {
                            int rc = process.exitValue();
                            StringBuilder res = new StringBuilder();
                            res.append(MessageFormat.format(getString(rc == 0 ? "SUCCESSFUL" : "FAILED"), actionName.toUpperCase())); // NOI18N
                            res.append(" ("); // NOI18N
                            if (rc != 0) {
                                res.append(MessageFormat.format(getString("EXIT_VALUE"), process.exitValue())); // NOI18N
                                res.append(' ');
                            }
                            res.append(MessageFormat.format(getString("TOTAL_TIME"), formatTime(System.currentTimeMillis() - startTimeMillis))); // NOI18N
                            res.append(')');

                            PrintWriter pw = (rc == 0) ? tab.getOut() : tab.getErr();
                            pw.println(res.toString());
                            pw.println();
                            closeIO();

                            if (listener != null) {
                                listener.executionFinished(process.exitValue());
                            }

                            StatusDisplayer.getDefault().setStatusText(MessageFormat.format(getString(rc == 0 ? "MSG_SUCCESSFUL" : "MSG_FAILED"), actionName));
                        }
                    };
                    break;
                }
            }
        }

        @Override
        public void run() {
            if (postRunnable != null) {
                postRunnable.run();
            }
        }

        private void closeIO() {
            if (false) {
                tab.getErr().close();
                tab.getOut().close();
                try {
                    tab.getIn().close();
                } catch (IOException ex) {
                    ex.printStackTrace();
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

        private static String formatTime(long millis) {
            StringBuilder res = new StringBuilder();
            long seconds = millis / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            if (hours > 0) {
                res.append(" ").append(hours).append(getString("HOUR")); // NOI18N
            }
            if (minutes > 0) {
                res.append(" ").append(minutes - hours * 60).append(getString("MINUTE")); // NOI18N
            }
            if (seconds > 0) {
                res.append(" ").append(seconds - minutes * 60).append(getString("SECOND")); // NOI18N
            } else {
                res.append(" ").append(millis - seconds * 1000).append(getString("MILLISECOND")); // NOI18N
            }
            return res.toString();
        }
    }
}
