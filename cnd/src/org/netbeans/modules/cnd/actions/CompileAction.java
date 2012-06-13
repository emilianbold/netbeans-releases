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
import java.io.File;
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
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.builds.ImportUtils;
import org.netbeans.modules.cnd.execution.CompileExecSupport;
import org.netbeans.modules.cnd.spi.toolchain.CompilerLineConvertor;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.ui.ModalMessageDlg;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionService;
import org.netbeans.modules.nativeexecution.api.execution.PostMessageDisplayer;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.LinkSupport;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.spi.project.ActionProvider;
import org.openide.LifecycleManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.WindowManager;

/**
 * Project less compile action
 * 
 * @author Alexander Simon
 */
@ActionID(id = "org.netbeans.modules.cnd.actions.CompileAction", category = "Build")
@ActionRegistration(lazy = false, displayName = "#BTN_Compile_File")
@ActionReferences({
    @ActionReference(path = "Loaders/text/x-cnd+sourcefile/Actions", name = "CompileAction", position = 950)
})
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


    private void performAction(Node node) {
        final Project project = getProject(node);
        if (project != null) {
            ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
            if (ap != null) {
                ap.invokeAction(ActionProvider.COMMAND_COMPILE_SINGLE, Lookups.singleton(node));
                return;
            }
        }
        performAction(node, project);
    }

    private Future<Integer> performAction(final Node node, final Project project) {
        if (SwingUtilities.isEventDispatchThread()){
            final ModalMessageDlg.LongWorker runner = new ModalMessageDlg.LongWorker() {
                private NativeExecutionService es;
                @Override
                public void doWork() {
                    es = prepare(node, project);
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
            NativeExecutionService es = prepare(node, project);
            if (es != null) {
                return es.run();
            }
        }
        return null;
    }

    private NativeExecutionService prepare(Node node, Project project) {
        final Writer outputListener = null;
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
        final String compilerPath = convertPath(tool.getPath(), execEnv);
        final StringBuilder argsFlat = new StringBuilder();
        argsFlat.append(ces.getCompileFlags()).append(' ');// NOI18N
        argsFlat.append("-c").append(' ');// NOI18N
        argsFlat.append(fileObject.getNameExt()).append(' ');// NOI18N
        argsFlat.append("-o ").append(getDevNull(execEnv, compilerSet));// NOI18N
        Map<String, String> envMap = getEnv(execEnv, node, project, null);
        // Tab Name
        String tabName = execEnv.isLocal() ? getString("COMPILE_LABEL", node.getName()) : getString("COMPILE_REMOTE_LABEL", node.getName(), execEnv.getDisplayName()); // NOI18N
        InputOutput _tab = IOProvider.getDefault().getIO(tabName, false); // This will (sometimes!) find an existing one.
        _tab.closeInputOutput(); // Close it...
        final InputOutput tab = IOProvider.getDefault().getIO(tabName, true); // Create a new ...
        try {
            tab.getOut().reset();
        } catch (IOException ioe) {
        }
        final InputOutput inputOutput = tab;
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
        
        ExecutionListener listener = new  ExecutionListener() {
            @Override
            public void executionStarted(int pid) {
                inputOutput.getOut().println(compilerPath+" "+argsFlat.toString()); //NOI18N
            }
            @Override
            public void executionFinished(int rc) {
            }
        };
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
        inputOutput.getOut().println(compilerPath+" "+argsFlat.toString()); // NOI18N
        inputOutput.getOut().flush();

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

    // find out right /dev/null
    private String getDevNull(ExecutionEnvironment execEnv, CompilerSet compilerSet) {
        if (execEnv.isLocal() && Utilities.isWindows()){
            if (!compilerSet.getCompilerFlavor().isCygwinCompiler()) {
                return "NUL"; // NOI18N
            }
        }
        return "/dev/null"; // NOI18N
    }
}
