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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.AbstractAction;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.api.compilers.ToolchainProject;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.cnd.api.execution.NativeExecutor;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.api.utils.Path;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.builds.CMakeExecSupport;
import org.netbeans.modules.cnd.builds.MakeExecSupport;
import org.netbeans.modules.cnd.builds.QMakeExecSupport;
import org.netbeans.modules.cnd.execution41.org.openide.loaders.ExecutionSupport;
import org.netbeans.modules.cnd.settings.CppSettings;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Cancellable;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Sergey Grinev
 */
public abstract class AbstractExecutorRunAction extends NodeAction {

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

    protected static String[] getAdditionalEnvirounment(Node node){
        ExecutionSupport mes = node.getCookie(ExecutionSupport.class);
        if (mes != null) {
            return mes.getEnvironmentVariables();
        }
        return null;
    }

    protected static String[] prepareEnv(ExecutionEnvironment execEnv) {
        CompilerSet cs = null;
        String csdirs = ""; // NOI18N
        String dcsn = CppSettings.getDefault().getCompilerSetName();
        PlatformInfo pi = PlatformInfo.getDefault(execEnv);
        if (dcsn != null && dcsn.length() > 0) {
            cs = CompilerSetManager.getDefault(execEnv).getCompilerSet(dcsn);
            if (cs != null) {
                csdirs = cs.getDirectory();
                // TODO Provide platform info
                String commands = cs.getCompilerFlavor().getCommandFolder(pi.getPlatform());
                if (commands != null && commands.length()>0) {
                    // Also add msys to path. Thet's where sh, mkdir, ... are.
                    csdirs += pi.pathSeparator() + commands;
                }
            }
        }
        String[] envp;
        if (csdirs.length() > 0) {
            envp = new String[] { pi.getPathAsStringWith(csdirs) };
        } else {
            envp = new String[0];
        }
        return envp;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP; // FIXUP ???
    }

    protected final static String getString(String key) {
        return NbBundle.getBundle(AbstractExecutorRunAction.class).getString(key);
    }

    protected final static String getString(String key, String a1) {
        return NbBundle.getMessage(AbstractExecutorRunAction.class, key, a1);
    }

    protected static class ShellExecuter implements ExecutionListener {
        private final NativeExecutor nativeExecutor;
        private final ProgressHandle progressHandle;
        private ExecutorTask executorTask = null;

        public ShellExecuter(NativeExecutor nativeExecutor, ExecutionListener listener) {
            this.nativeExecutor = nativeExecutor;
            nativeExecutor.addExecutionListener(this);
            if (listener != null) {
                nativeExecutor.addExecutionListener(listener);
            }
            this.progressHandle = createPogressHandle(new StopAction(this), nativeExecutor);
        }

        public void execute() {
            try {
                executorTask = nativeExecutor.execute();
            } catch (IOException ioe) {
            }
        }

        public void executionFinished(int rc) {
            progressHandle.finish();
        }

        public void executionStarted(int pid) {
            progressHandle.start();
        }

        public ExecutorTask getExecutorTask() {
            return executorTask;
        }
    }

    private static final class StopAction extends AbstractAction {
        private final ShellExecuter shellExecutor;

        public StopAction(ShellExecuter shellExecutor) {
            this.shellExecutor = shellExecutor;
        }

//        @Override
//        public Object getValue(String key) {
//            if (key.equals(Action.SMALL_ICON)) {
//                return new ImageIcon(DefaultProjectActionHandler.class.getResource("/org/netbeans/modules/cnd/makeproject/ui/resources/stop.png"));
//            } else if (key.equals(Action.SHORT_DESCRIPTION)) {
//                return getString("TargetExecutor.StopAction.stop");
//            } else {
//                return super.getValue(key);
//            }
//        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled()) {
                return;
            }
            setEnabled(false);
            if (shellExecutor.getExecutorTask() != null) {
                shellExecutor.getExecutorTask().stop();
            }
        }
    }

    private static ProgressHandle createPogressHandle(final AbstractAction sa, final NativeExecutor nativeExecutor) {
        ProgressHandle handle = ProgressHandleFactory.createHandle(nativeExecutor.getTabeName(), new Cancellable() {
            public boolean cancel() {
                sa.actionPerformed(null);
                return true;
            }
        }, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                nativeExecutor.getTab().select();
            }
        });
        handle.setInitialDelay(0);
        return handle;
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
}
