/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.actions;

import java.awt.Frame;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.RemoteSyncSupport;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.builds.ImportUtils;
import org.netbeans.modules.cnd.execution.CompileExecSupport;
import org.netbeans.modules.cnd.spi.toolchain.CompilerLineConvertor;
import org.netbeans.modules.cnd.spi.toolchain.ToolchainProject;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.ui.ModalMessageDlg;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionService;
import org.netbeans.modules.nativeexecution.api.execution.PostMessageDisplayer;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.WindowManager;

/**
 * Project less compile action
 * 
 * @author Alexander Simon
 */
public class CompileAction extends AbstractExecutorRunAction {

    public CompileAction() {
        super.putValue("key", "CndCompileAction");// NOI18N
    }
    
    @Override
    public String getName() {
        return getString("BTN_Compile_File"); // NOI18N
    }

    @Override
    protected boolean accept(DataObject object) {
        return object != null && object.getLookup().lookup(CompileExecSupport.class) != null;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        // Save everything first
        LifecycleManager.getDefault().saveAll();

        for (int i = 0; i < activatedNodes.length; i++) {
            performAction(activatedNodes[i]);
        }
    }


    public static void performAction(Node node) {
        performAction(node, null, null, getProject(node), null);
    }

    private static Future<Integer> performAction(final Node node, final ExecutionListener listener, final Writer outputListener, final Project project, final InputOutput inputOutput) {
        if (SwingUtilities.isEventDispatchThread()){
            final ModalMessageDlg.LongWorker runner = new ModalMessageDlg.LongWorker() {
                private NativeExecutionService es;
                @Override
                public void doWork() {
                    es = prepare(node, listener, outputListener, project, inputOutput);
                }
                @Override
                public void doPostRunInEDT() {
                    if (es != null) {
                        es.run();
                    }
                }
            };
            Frame mainWindow = WindowManager.getDefault().getMainWindow();
            String title = getString("DLG_TITLE_Prepare",node.getName()); // NOI18N
            String msg = getString("MSG_TITLE_Prepare",node.getName()); // NOI18N
            ModalMessageDlg.runLongTask(mainWindow, title, msg, runner, null);
        } else {
            NativeExecutionService es = prepare(node, listener, outputListener, project, inputOutput);
            if (es != null) {
                return es.run();
            }
        }
        return null;
    }

    private static NativeExecutionService prepare(Node node, final ExecutionListener listener, final Writer outputListener, Project project, InputOutput inputOutput) {
        CompileExecSupport ces = node.getLookup().lookup(CompileExecSupport.class);
        if (ces == null) {
            trace("Node "+node+" does not have CompileExecSupport"); //NOI18N
            return null;
        }
        //Save file
        saveNode(node);
        DataObject dataObject = node.getLookup().lookup(DataObject.class);
        FileObject fileObject = dataObject.getPrimaryFile();
        
        // Build directory
        String bdir = ces.getRunDirectory();
        FileObject compileDirObject = RemoteFileUtil.getFileObject(fileObject.getParent(), bdir);
        if (compileDirObject == null) {
            trace("Run folder folder is null"); //NOI18N
            return null;
        }
        String compileDir = compileDirObject.getPath();
        
        ExecutionEnvironment execEnv = getExecutionEnvironment(fileObject, project);
        if (FileSystemProvider.getExecutionEnvironment(compileDirObject).isLocal()) {
            compileDir = convertToRemoteIfNeeded(execEnv, compileDir, project);
        }
        if (compileDir == null) {
            trace("Compile folder folder is null"); //NOI18N
            return null;
        }
        CompilerSet compilerSet = getCompilerSet(node, project);
        if (compilerSet == null) {
            trace("Not found tool collection"); //NOI18N
            return null;
        }
        String mimeType = fileObject.getMIMEType();
        Tool tool = null;
        if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mimeType)) {
            tool = compilerSet.findTool(PredefinedToolKind.CCCompiler);
        } else if (MIMENames.C_MIME_TYPE.equals(mimeType)) {
            tool = compilerSet.findTool(PredefinedToolKind.CCompiler);
        } else if (MIMENames.FORTRAN_MIME_TYPE.equals(mimeType)) {
            tool = compilerSet.findTool(PredefinedToolKind.FortranCompiler);
        }
        if (tool == null || tool.getPath() == null) {
            trace("Not found compiler"); //NOI18N
            return null;
        }
        
        String compilerPath = tool.getPath();
        StringBuilder argsFlat = new StringBuilder();
        argsFlat.append(ces.getCompileFlags()).append(' ');// NOI18N
        argsFlat.append("-c").append(' ');// NOI18N
        argsFlat.append(fileObject.getNameExt()).append(' ');// NOI18N
        argsFlat.append("-o /dev/null");// NOI18N
        Map<String, String> envMap = getEnv(execEnv, node, project, null);
        if (inputOutput == null) {
            // Tab Name
            String tabName = execEnv.isLocal() ? getString("COMPILE_LABEL", node.getName()) : getString("COMPILE_REMOTE_LABEL", node.getName(), execEnv.getDisplayName()); // NOI18N
            InputOutput _tab = IOProvider.getDefault().getIO(tabName, false); // This will (sometimes!) find an existing one.
            _tab.closeInputOutput(); // Close it...
            final InputOutput tab = IOProvider.getDefault().getIO(tabName, true); // Create a new ...
            try {
                tab.getOut().reset();
            } catch (IOException ioe) {
            }
            inputOutput = tab;
        }
        RemoteSyncWorker syncWorker = RemoteSyncSupport.createSyncWorker(project, inputOutput.getOut(), inputOutput.getErr());
        if (syncWorker != null) {
            if (!syncWorker.startup(envMap)) {
                trace("RemoteSyncWorker is not started up"); //NOI18N
                return null;
            }
        }
        
        MacroMap mm = MacroMap.forExecEnv(execEnv);
        mm.putAll(envMap);
        
        traceExecutable(compilerPath, compileDir, argsFlat, execEnv.toString(), mm.toMap());
        
        CompilerLineConvertor compilerLineConvertor = new CompilerLineConvertor(project, compilerSet, execEnv, compileDirObject);
        AbstractExecutorRunAction.ProcessChangeListener processChangeListener = new AbstractExecutorRunAction.ProcessChangeListener(listener, outputListener, compilerLineConvertor, syncWorker);

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv).
                setWorkingDirectory(compileDir).
                unbufferOutput(false).
                addNativeProcessListener(processChangeListener);

        npb.getEnvironment().putAll(mm);
        npb.redirectError();

        List<String> list = ImportUtils.parseArgs(argsFlat.toString());
        list = ImportUtils.normalizeParameters(list);
        npb.setExecutable(compilerPath);
        npb.setArguments(list.toArray(new String[list.size()]));

        NativeExecutionDescriptor descr = new NativeExecutionDescriptor().controllable(true).
                frontWindow(true).
                inputVisible(true).
                inputOutput(inputOutput).
                outLineBased(true).
                showProgress(!CndUtils.isStandalone()).
                postExecution(processChangeListener).
                postMessageDisplayer(new PostMessageDisplayer.Default("Compile")). // NOI18N
                errConvertorFactory(processChangeListener).
                outConvertorFactory(processChangeListener);

        // Execute the shellfile
        return NativeExecutionService.newService(npb, descr, "Compile"); // NOI18N
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
