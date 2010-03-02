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

package org.netbeans.modules.cnd.actions;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javax.swing.SwingUtilities;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.modules.cnd.api.remote.RemoteSyncSupport;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.builds.MakeExecSupport;
import org.netbeans.modules.cnd.spi.toolchain.CompilerLineConvertor;
import org.netbeans.modules.cnd.settings.MakeSettings;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.spi.toolchain.ToolchainProject;
import org.netbeans.modules.cnd.utils.ui.ModalMessageDlg;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.WindowManager;

/**
 * Base class for Make Actions ...
 */
public abstract class MakeBaseAction extends AbstractExecutorRunAction {

    @Override
    protected boolean accept(DataObject object) {
        return object != null && object.getCookie(MakeExecSupport.class) != null;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        for (int i = 0; i < activatedNodes.length; i++){
            performAction(activatedNodes[i], "");// NOI18N
        }
    }

    protected void performAction(Node node, String target) {
        performAction(node, target, null, null, getProject(node), null, null);
    }

    protected Future<Integer> performAction(final Node node, final String target, final ExecutionListener listener, final Writer outputListener, final Project project,
                                 final List<String> additionalEnvironment, final InputOutput inputOutput) {
        if (SwingUtilities.isEventDispatchThread()) {
            final ModalMessageDlg.LongWorker runner = new ModalMessageDlg.LongWorker() {
                private ExecutionService es;
                @Override
                public void doWork() {
                    es = MakeBaseAction.this.prepare(node, target, listener, outputListener, project, additionalEnvironment, inputOutput);
                }
                @Override
                public void doPostRunInEDT() {
                    if (es != null) {
                        es.run();
                    }
                }
            };
            Frame mainWindow = WindowManager.getDefault().getMainWindow();
            String title = getString("DLG_TITLE_Prepare", "make"); // NOI18N
            String msg = getString("MSG_TITLE_Prepare", "make"); // NOI18N
            ModalMessageDlg.runLongTask(mainWindow, title, msg, runner, null);
        } else {
            ExecutionService es = prepare(node, target, listener, outputListener, project, additionalEnvironment, inputOutput);
            if (es != null) {
                return es.run();
            }
        }
        return null;
    }

    private ExecutionService prepare(Node node, String target, final ExecutionListener listener, final Writer outputListener,
                                Project project, List<String> additionalEnvironment, InputOutput inputOutput) {
        if (MakeSettings.getDefault().getSaveAll()) {
            LifecycleManager.getDefault().saveAll();
        }
        DataObject dataObject = node.getCookie(DataObject.class);
        final FileObject fileObject = dataObject.getPrimaryFile();
        File makefile = FileUtil.toFile(fileObject);
        // Build directory
        String buildDir = getBuildDirectory(node,PredefinedToolKind.MakeTool);
        // Executable
        String executable = getCommand(node, project, PredefinedToolKind.MakeTool, "make"); // NOI18N
        // Arguments
        String[] args;
        if (target.length() == 0) {
            args = new String[]{"-f", makefile.getName()}; // NOI18N
        } else {
            args = new String[]{"-f", makefile.getName(), target}; // NOI18N
        }
        final ExecutionEnvironment execEnv = getExecutionEnvironment(fileObject, project);
        buildDir = convertToRemoteIfNeeded(execEnv, buildDir);
        if (buildDir == null) {
            return null;
        }
        Map<String, String> envMap = getEnv(execEnv, node, additionalEnvironment);
        if (isSunStudio(node, project)) {
            envMap.put("SPRO_EXPAND_ERRORS", ""); // NOI18N
        }
        traceExecutable(executable, buildDir, args, envMap);

        if (inputOutput == null) {
            // Tab Name
            String tabName = getString("MAKE_LABEL", node.getName()); // NOI18N
            if (target.length() > 0) {
                tabName += " " + target; // NOI18N
            }
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
                return null;
            }
        }
        ProcessChangeListener processChangeListener = new ProcessChangeListener(listener, outputListener,
                new CompilerLineConvertor(getCompilerSet(project), execEnv, fileObject.getParent()), inputOutput, "Make", syncWorker); // NOI18N
        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv)
        .setExecutable(executable)
        .setWorkingDirectory(buildDir)
        .setArguments(args)
        .unbufferOutput(false)
        .addNativeProcessListener(processChangeListener);
        npb.getEnvironment().putAll(envMap);
        npb.redirectError();
        
        ExecutionDescriptor descr = new ExecutionDescriptor()
        .controllable(true)
        .frontWindow(true)
        .inputVisible(true)
        .showProgress(true)
        .inputOutput(inputOutput)
        .outLineBased(true)
        .postExecution(processChangeListener)
        .errConvertorFactory(processChangeListener)
        .outConvertorFactory(processChangeListener);
        return ExecutionService.newService(npb, descr, "make"); // NOI18N
    }

    private CompilerSet getCompilerSet(Project project) {
        CompilerSet set = null;
        if (project != null) {
            ToolchainProject toolchain = project.getLookup().lookup(ToolchainProject.class);
            if (toolchain != null) {
                set = toolchain.getCompilerSet();
            }
        }
        if (set == null) {
            set = CompilerSetManager.get(ExecutionEnvironmentFactory.getLocal()).getDefaultCompilerSet();
        }
        return set;
    }
}
