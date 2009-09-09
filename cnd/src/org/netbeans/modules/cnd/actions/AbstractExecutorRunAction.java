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
package org.netbeans.modules.cnd.actions;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor.LineConvertorFactory;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.api.compilers.ToolchainProject;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.api.utils.Path;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.builds.CMakeExecSupport;
import org.netbeans.modules.cnd.builds.MakeExecSupport;
import org.netbeans.modules.cnd.builds.QMakeExecSupport;
import org.netbeans.modules.cnd.execution41.org.openide.loaders.ExecutionSupport;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessChangeEvent;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.NodeAction;
import org.openide.windows.InputOutput;

/**
 *
 * @author Sergey Grinev
 */
public abstract class AbstractExecutorRunAction extends NodeAction {
    private static boolean TRACE = Boolean.getBoolean("cnd.discovery.trace.projectimport"); // NOI18N
    private static Logger logger = Logger.getLogger("org.netbeans.modules.cnd.actions.AbstractExecutorRunAction"); // NOI18N
    static {
        if (TRACE) {
            logger.setLevel(Level.ALL);
        }
    }

    protected boolean enable(Node[] activatedNodes) {
        boolean enabled = false;

        if (activatedNodes == null || activatedNodes.length == 0 || activatedNodes.length > 1) {
            enabled = false;
        } else {
            DataObject dataObject = activatedNodes[0].getCookie(DataObject.class);
            if (accept(dataObject)) {
                enabled = true;
            } else {
                enabled = false;
            }
        }
        return enabled;
    }

    protected abstract boolean accept(DataObject object);

    protected static ExecutionEnvironment getExecutionEnvironment(FileObject fileObject, Project project) {
        if (project == null) {
            project = findInOpenedProject(fileObject);
        }
        ExecutionEnvironment developmentHost = CompilerSetManager.getDefaultExecutionEnvironment();
        if (project != null) {
            RemoteProject info = project.getLookup().lookup(RemoteProject.class);
            if (info != null) {
                ExecutionEnvironment dh = info.getDevelopmentHost();
                if (dh != null) {
                    developmentHost = dh;
                }
            }
        }
        return developmentHost;
    }

    private static Project findInOpenedProject(FileObject fileObject){
        // First platform provider uses simplified algorithm for search that finds project in parent folder.
        // Fixed algorithm try to find opened project by second make project provider.
        //return FileOwnerQuery.getOwner(fileObject);
        Collection<? extends FileOwnerQueryImplementation> instances =  Lookup.getDefault().lookupAll(FileOwnerQueryImplementation.class);
        for(FileOwnerQueryImplementation provider : instances){
            Project project = provider.getOwner(fileObject);
            if (project != null){
                for (Project p : OpenProjects.getDefault().getOpenProjects()) {
                    if (project == p) {
                        return project;
                    }
                }
            }
        }
        return null;
    }

    private static Project findProject(Node node){
        Node parent = node;
        while (true) {
            Project project = parent.getLookup().lookup(Project.class);
            if (project != null){
                return project;
            }
            Node p = parent.getParentNode();
            if (p != null && p != parent){
                parent = p;
            } else {
                return null;
            }
        }
    }

    protected static boolean isSunStudio(Node node, Project project){
        DataObject dataObject = node.getCookie(DataObject.class);
        FileObject fileObject = dataObject.getPrimaryFile();
        if (project == null) {
            project = findProject(node);
        }
        if (project == null) {
            project = findInOpenedProject(fileObject);
        }
        CompilerSet set = null;
        if (project != null) {
            ToolchainProject toolchain = project.getLookup().lookup(ToolchainProject.class);
            if (toolchain != null) {
                set = toolchain.getCompilerSet();
            }
        }
        if (set == null) {
            set = CompilerSetManager.getDefault().getDefaultCompilerSet();
        }
        if (set == null) {
            return false;
        }
        return set.isSunCompiler();
    }

    private static CompilerSet getCompilerSet(Node node ){
        Project project;
        DataObject dataObject = node.getCookie(DataObject.class);
        FileObject fileObject = dataObject.getPrimaryFile();
        project = findProject(node);
        if (project == null) {
            project = findInOpenedProject(fileObject);
        }
        CompilerSet set = null;
        if (project != null) {
            ToolchainProject toolchain = project.getLookup().lookup(ToolchainProject.class);
            if (toolchain != null) {
                set = toolchain.getCompilerSet();
            }
        }
        if (set == null) {
            set = CompilerSetManager.getDefault().getDefaultCompilerSet();
        }
        return set;
    }

    protected static String getCommand(Node node, Project project, int tool, String defaultName){
        DataObject dataObject = node.getCookie(DataObject.class);
        FileObject fileObject = dataObject.getPrimaryFile();
        if (project == null) {
            project = findProject(node);
        }
        if (project == null) {
            project = findInOpenedProject(fileObject);
        }
        CompilerSet set = null;
        if (project != null) {
            ToolchainProject toolchain = project.getLookup().lookup(ToolchainProject.class);
            if (toolchain != null) {
                set = toolchain.getCompilerSet();
            }
        }
        if (set == null) {
            set = CompilerSetManager.getDefault().getDefaultCompilerSet();
        }
        String command = null;
        if (set != null) {
            Tool aTool = set.findTool(tool);
            if (aTool != null) {
                command = aTool.getPath();
            }
        }
        if (command == null || command.length()==0) {
            if (tool == Tool.MakeTool) {
                MakeExecSupport mes = node.getCookie(MakeExecSupport.class);
                if (mes != null) {
                    command = mes.getMakeCommand();
                }
            } else if (tool == Tool.QMakeTool) {
                QMakeExecSupport mes = node.getCookie(QMakeExecSupport.class);
                if (mes != null) {
                    command = mes.getQMakeCommand();
                }
            } else if (tool == Tool.CMakeTool) {
                CMakeExecSupport mes = node.getCookie(CMakeExecSupport.class);
                if (mes != null) {
                    command = mes.getCMakeCommand();
                }
            }
        }
        if (command == null || command.length()==0) {
            command = findTools(defaultName);
        }
        return command;
    }

    protected static File getBuildDirectory(Node node,int tool){
        DataObject dataObject = node.getCookie(DataObject.class);
        FileObject fileObject = dataObject.getPrimaryFile();
        File makefile = FileUtil.toFile(fileObject);
        // Build directory
        String bdir = null;
        if (tool == Tool.MakeTool) {
            MakeExecSupport mes = node.getCookie(MakeExecSupport.class);
            if (mes != null) {
                bdir = mes.getBuildDirectory();
            }
        } else if (tool == Tool.QMakeTool) {
            QMakeExecSupport mes = node.getCookie(QMakeExecSupport.class);
            if (mes != null) {
                bdir = mes.getRunDirectory();
            }
        } else if (tool == Tool.CMakeTool) {
            CMakeExecSupport mes = node.getCookie(CMakeExecSupport.class);
            if (mes != null) {
                bdir = mes.getRunDirectory();
            }
        }
        if (bdir == null) {
            bdir = makefile.getParent();
        }
        File buildDir = getAbsoluteBuildDir(bdir, makefile);
        return buildDir;
    }

    protected static String[] getArguments(Node node, int tool) {
        String[] args = null;
        if (tool == Tool.QMakeTool) {
            QMakeExecSupport mes = node.getCookie(QMakeExecSupport.class);
            if (mes != null) {
                args = mes.getArguments();
            }
        } else if (tool == Tool.CMakeTool) {
            CMakeExecSupport mes = node.getCookie(CMakeExecSupport.class);
            if (mes != null) {
                args = mes.getArguments();
            }
        }
        if (args == null) {
            args = new String[0];
        }
        return args;
    }

    public static String findTools(String toolName){
        List<String> list = new ArrayList<String>(Path.getPath());
        for (String path : list) {
            String task = path+File.separatorChar+toolName;
            File tool = new File(task);
            if (tool.exists() && tool.isFile()) {
                return tool.getAbsolutePath();
            } else if (Utilities.isWindows()) {
                task = task+".exe"; // NOI18N
                tool = new File(task);
                if (tool.exists() && tool.isFile()) {
                    return tool.getAbsolutePath();
                }
            }
        }
        return toolName;
    }

    private static List<String> getAdditionalEnvirounment(Node node){
        List<String> res = new ArrayList<String>();
        ExecutionSupport mes = node.getCookie(ExecutionSupport.class);
        if (mes != null) {
            for(String s : mes.getEnvironmentVariables()){
                res.add(s);
            }
            return res;
        }
        return res;
    }

    private static List<String> prepareEnv(ExecutionEnvironment execEnv, Node node) {
        String csdirs = ""; // NOI18N
        PlatformInfo pi = PlatformInfo.getDefault(execEnv);
        CompilerSet cs = getCompilerSet(node);
        if (cs != null) {
            csdirs = cs.getDirectory();
            // TODO Provide platform info
            String commands = cs.getCompilerFlavor().getCommandFolder(pi.getPlatform());
            if (commands != null && commands.length()>0) {
                // Also add msys to path. Thet's where sh, mkdir, ... are.
                csdirs += pi.pathSeparator() + commands;
            }
        }
        List<String> res = new ArrayList<String>();
        if (csdirs.length() > 0) {
            res.add(pi.getPathAsStringWith(csdirs));
        }
        return res;
    }

    protected static Map<String, String> getEnv(ExecutionEnvironment execEnv, Node node, List<String> additionalEnvironment) {
        List<String> nodeEnvironment = getAdditionalEnvirounment(node);
        if (additionalEnvironment != null) {
            nodeEnvironment.addAll(additionalEnvironment);
        }
        nodeEnvironment.addAll(prepareEnv(execEnv, node));
        Map<String, String> envMap = new HashMap<String, String>();
        for(String s: nodeEnvironment) {
            int i = s.indexOf('='); // NOI18N
            if (i>0) {
                String key = s.substring(0, i);
                String value = s.substring(i+1).trim();
                if (value.length()>1 && (value.startsWith("\"") && value.endsWith("\"") || // NOI18N
                                         value.startsWith("'") && value.endsWith("'"))) { // NOI18N
                    value = value.substring(1,value.length()-1);
                }
                envMap.put(key, value);
            }
        }
        return envMap;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP; // FIXUP ???
    }

    protected final static String getString(String key) {
        return NbBundle.getBundle(AbstractExecutorRunAction.class).getString(key);
    }

    protected final static String getString(String key, String ... a1) {
        return NbBundle.getMessage(AbstractExecutorRunAction.class, key, a1);
    }

    protected static String formatTime(long millis) {
        StringBuilder buf = new StringBuilder();
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        if (hours > 0) {
            buf.append(' ').append(hours).append(getString("Time.Hour")); // NOI18N
        }
        if (minutes > 0) {
            buf.append(' ').append(minutes % 60).append(getString("Time.Minute")); // NOI18N
        }
        if (seconds > 0) {
            buf.append(' ').append(seconds % 60).append(getString("Time.Second")); // NOI18N
        }
        if (hours == 0 && minutes == 0 && seconds == 0) {
            buf.append(' ').append(millis).append(getString("Time.Millisecond")); // NOI18N
        }
        return buf.toString();
    }

    protected static String quoteExecutable(String orig) {
        StringBuilder sb = new StringBuilder();
        String escapeChars = Utilities.isWindows() ? " \"'()" : " \"'()!"; // NOI18N

        for (char c : orig.toCharArray()) {
            if (escapeChars.indexOf(c) >= 0) { // NOI18N
                sb.append('\\');
            }
            sb.append(c);
        }

        return sb.toString();
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    protected static File getAbsoluteBuildDir(String bdir, File startFile) {
        File buildDir;
        if (bdir.length() == 0 || bdir.equals(".")) { // NOI18N
            buildDir = startFile.getParentFile();
        } else if (IpeUtils.isPathAbsolute(bdir)) {
            buildDir = new File(bdir);
        } else {
            buildDir = new File(startFile.getParentFile(), bdir);
        }
        // Canonical path not appropriate here.
        // We must emulate command line behaviour hence absolute normalized path is more appropriate here.
        // See IZ#157677:LiteSQL is not configurable in case of symlinks.
        //try {
        //    buildDir = buildDir.getCanonicalFile();
        //} catch (IOException ioe) {
        //    // FIXUP
        //}
        buildDir = CndFileUtils.normalizeFile(buildDir.getAbsoluteFile());
        return buildDir;
    }

    protected static void saveNode(Node node) {
        //Save file
        SaveCookie save = node.getLookup().lookup(SaveCookie.class);
        if (save != null) {
            try {
                save.save();
            } catch (IOException ex) {
            }
        }
    }

    protected static void traceExecutable(String executable, File buildDir, StringBuilder argsFlat, Map<String, String> envMap) {
        if (TRACE) {
            StringBuilder buf = new StringBuilder("Run " + executable); // NOI18N
            buf.append("\n\tin folder   " + buildDir.getPath()); // NOI18N
            buf.append("\n\targuments   " + argsFlat); // NOI18N
            buf.append("\n\tenvironment "); // NOI18N
            for (Map.Entry<String, String> v : envMap.entrySet()) {
                buf.append("\n\t\t" + v.getKey() + "=" + v.getValue()); // NOI18N
            }
             buf.append("\n"); // NOI18N
            logger.log(Level.INFO, buf.toString());
        }
    }

    protected static void traceExecutable(String executable, File buildDir, String[] args, Map<String, String> envMap) {
        if (TRACE) {
            StringBuilder argsFlat = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                argsFlat.append(" "); // NOI18N
                argsFlat.append(args[i]);
            }
            traceExecutable(executable, buildDir, argsFlat, envMap);
        }
    }

    protected static final class ProcessChangeListener implements ChangeListener, Runnable {
        private final ExecutionListener listener;
        private final InputOutput tab;
        private final String resourceKey;
        private long startTimeMillis;
        private Runnable postRunnable;

        public ProcessChangeListener(ExecutionListener listener, InputOutput tab, String resourceKey) {
            this.listener = listener;
            this.tab = tab;
            this.resourceKey = resourceKey;
        }

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
                    startTimeMillis = System.currentTimeMillis();
                    if (listener != null) {
                        listener.executionStarted(event.pid);
                    }
                    break;
                case RUNNING:
                    break;
                case CANCELLED:
                {
                    if (listener != null) {
                        listener.executionFinished(process.exitValue());
                    }
                    postRunnable = new Runnable() {
                        public void run() {
                            String message = getString("Output."+resourceKey+"Terminated", formatTime(System.currentTimeMillis() - startTimeMillis)); // NOI18N
                            String statusMessage = getString("Status."+resourceKey+"Terminated"); // NOI18N
                            tab.getOut().println();
                            tab.getOut().println(message);
                            tab.getOut().flush();
                            StatusDisplayer.getDefault().setStatusText(statusMessage);
                        }
                    };
                    break;
                }
                case ERROR:
                {
                    if (listener != null) {
                        listener.executionFinished(-1);
                    }
                    postRunnable = new Runnable() {
                        public void run() {
                            String message = getString("Output."+resourceKey+"FailedToStart"); // NOI18N
                            String statusMessage = getString("Status."+resourceKey+"FailedToStart"); // NOI18N
                            tab.getOut().println();
                            tab.getOut().println(message);
                            tab.getOut().flush();
                            StatusDisplayer.getDefault().setStatusText(statusMessage);
                        }
                    };
                    break;
                }
                case FINISHED:
                {
                    if (listener != null) {
                        listener.executionFinished(process.exitValue());
                    }
                    postRunnable = new Runnable() {
                        public void run() {
                            String message;
                            String statusMessage;
                            if (process.exitValue() != 0) {
                                message = getString("Output."+resourceKey+"Failed", ""+process.exitValue(), formatTime(System.currentTimeMillis() - startTimeMillis)); // NOI18N
                                statusMessage = getString("Status."+resourceKey+"Failed"); // NOI18N
                            } else {
                                message = getString("Output."+resourceKey+"Successful", formatTime(System.currentTimeMillis() - startTimeMillis)); // NOI18N
                                statusMessage = getString("Status."+resourceKey+"Successful"); // NOI18N
                            }
                            tab.getOut().println();
                            tab.getOut().println(message);
                            tab.getOut().flush();
                            StatusDisplayer.getDefault().setStatusText(statusMessage);
                        }
                    };
                    break;
                }
            }
        }

        public void run() {
            if (postRunnable != null) {
                postRunnable.run();
            }
        }
    }

    protected static final class ProcessLineConvertorFactory implements LineConvertorFactory {
        private final Writer outputListener;
        private final LineConvertor lineConvertor;

        public ProcessLineConvertorFactory(Writer outputListener, LineConvertor lineConvertor) {
            this.outputListener = outputListener;
            this.lineConvertor = lineConvertor;
        }

        public LineConvertor newLineConvertor() {
            return new LineConvertor() {
                @Override
                public List<ConvertedLine> convert(String line) {
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
            };
        }
    }
}
