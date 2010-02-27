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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javax.swing.SwingUtilities;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetUtils;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.modules.nativeexecution.api.util.LinkSupport;
import org.netbeans.modules.cnd.api.remote.RemoteSyncSupport;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.builds.ImportUtils;
import org.netbeans.modules.cnd.execution.ShellExecSupport;
import org.netbeans.modules.cnd.utils.ui.ModalMessageDlg;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
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
public class ShellRunAction extends AbstractExecutorRunAction {

    @Override
    public String getName() {
        return getString("BTN_Run"); // NOI18N
    }

    @Override
    protected boolean accept(DataObject object) {
        return object != null && object.getCookie(ShellExecSupport.class) != null;
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

    public static Future<Integer> performAction(final Node node, final ExecutionListener listener, final Writer outputListener, final Project project, final InputOutput inputOutput) {
        if (SwingUtilities.isEventDispatchThread()){
            final ModalMessageDlg.LongWorker runner = new ModalMessageDlg.LongWorker() {
                private ExecutionService es;
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
            ExecutionService es = prepare(node, listener, outputListener, project, inputOutput);
            if (es != null) {
                return es.run();
            }
        }
        return null;
    }

    private static ExecutionService prepare(Node node, final ExecutionListener listener, final Writer outputListener, Project project, InputOutput inputOutput) {
        ShellExecSupport bes = node.getCookie(ShellExecSupport.class);
        if (bes == null) {
            return null;
        }
        //Save file
        saveNode(node);
        DataObject dataObject = node.getCookie(DataObject.class);
        FileObject fileObject = dataObject.getPrimaryFile();
        
        File shellFile = FileUtil.toFile(fileObject);
        // Build directory
        String bdir = bes.getRunDirectory();
        String buildDir = getAbsoluteBuildDir(bdir, shellFile).getAbsolutePath();
        
        String[] shellCommandAndArgs = bes.getShellCommandAndArgs(fileObject); // from inside shell file or properties
        String shellCommand = shellCommandAndArgs[0];
        String shellFilePath = CndPathUtilitities.toRelativePath(buildDir, shellFile.getPath()); // Absolute path to shell file
        if (shellFilePath.equals(shellFile.getName())) {
            shellFilePath = "."+File.separatorChar+shellFilePath; //NOI18N
        }
        String[] args = bes.getArguments(); // from properties

        ExecutionEnvironment execEnv = getExecutionEnvironment(fileObject, project);
        buildDir = convertToRemoteIfNeeded(execEnv, buildDir);
        if (buildDir == null) {
            return null;
        }
        shellFilePath = convertToRemoveSeparatorsIfNeeded(execEnv, shellFilePath);
        // Windows: The command is usually of the from "/bin/sh", but this
        // doesn't work here, so extract the 'sh' part and use that instead. 
        // FIXUP: This is not entirely correct though.
        if (PlatformInfo.getDefault(execEnv).isWindows() && shellCommand.length() > 0) {
            shellCommand = findWindowsShell(shellCommand, execEnv, node);
            shellCommand = LinkSupport.resolveWindowsLink(shellCommand);
        }
        
        StringBuilder argsFlat = new StringBuilder();
        if (shellCommandAndArgs[0].length() > 0) {
            for (int i = 1; i < shellCommandAndArgs.length; i++) {
                argsFlat.append(" "); // NOI18N
                argsFlat.append(shellCommandAndArgs[i]);
            }
        }
        if (shellCommand.length() == 0) {
            shellCommand = shellFile.getAbsolutePath();
        } else {
            argsFlat.append(shellFilePath);
        }
        for (int i = 0; i < args.length; i++) {
            argsFlat.append(" "); // NOI18N
            argsFlat.append(args[i]);
        }
        Map<String, String> envMap = getEnv(execEnv, node, null);
        traceExecutable(shellCommand, buildDir, argsFlat, envMap);
        if (inputOutput == null) {
            // Tab Name
            String tabName = getString("RUN_LABEL", node.getName()); // NOI18N
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
                return null;
            }
        }
        ProcessChangeListener processChangeListener = new ProcessChangeListener(listener, outputListener, null, inputOutput, "Run", syncWorker); // NOI18N
        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv)
        .setWorkingDirectory(buildDir)
        .unbufferOutput(false)
        .addNativeProcessListener(processChangeListener);
        npb.getEnvironment().putAll(envMap);
        npb.redirectError();
        List<String> list = ImportUtils.parseArgs(argsFlat.toString());
        list = ImportUtils.normalizeParameters(list);
        npb.setExecutable(shellCommand);
        npb.setArguments(list.toArray(new String[list.size()]));

        ExecutionDescriptor descr = new ExecutionDescriptor()
        .controllable(true)
        .frontWindow(true)
        .inputVisible(true)
        .inputOutput(inputOutput)
        .outLineBased(true)
        .showProgress(true)
        .postExecution(processChangeListener)
        .errConvertorFactory(processChangeListener)
        .outConvertorFactory(processChangeListener);

        // Execute the shellfile
        return ExecutionService.newService(npb, descr, "Run"); // NOI18N
    }

    private static String findWindowsShell(String shellCommand, ExecutionEnvironment execEnv, Node node) {
        int i = shellCommand.lastIndexOf('/'); // UNIX PATH // NOI18N
        if (i >= 0) {
            shellCommand = shellCommand.substring(i + 1);
        }
        File sc = new File(shellCommand);
        if (sc.exists()) {
            return shellCommand;
        }
        PlatformInfo pi = PlatformInfo.getDefault(execEnv);
        String newShellCommand = pi.findCommand(shellCommand);
        if (newShellCommand != null) {
            return newShellCommand;
        }
        List<CompilerSet> list = new ArrayList<CompilerSet>();
        CompilerSet set = getCompilerSet(node);
        if (set != null) {
            list.add(set);
        }
        CompilerSetManager csm = CompilerSetManager.get(execEnv);
        if (csm != null) {
            set = csm.getDefaultCompilerSet();
            if (set != null && !list.contains(set)) {
                list.add(set);
            }
            for (CompilerSet aSet : csm.getCompilerSets()) {
                if (aSet != null && !list.contains(aSet)) {
                    list.add(aSet);
                }
            }
        }
        String folder;
        for (CompilerSet aSet : list) {
            folder = aSet.getCompilerFlavor().getCommandFolder(PlatformTypes.PLATFORM_WINDOWS);
            if (folder != null) {
                newShellCommand = pi.findCommand(folder, shellCommand);
                if (newShellCommand != null) {
                    return newShellCommand;
                }
            } else {
                folder = aSet.getDirectory();
                if (folder != null) {
                    newShellCommand = pi.findCommand(folder, shellCommand);
                    if (newShellCommand != null) {
                        return newShellCommand;
                    }
                }
            }
        }
        folder = CompilerSetUtils.getCygwinBase();
        if (folder != null) {
            newShellCommand = pi.findCommand(folder+"/bin", shellCommand); // NOI18N
            if (newShellCommand != null) {
                return newShellCommand;
            }
        }
        folder = CompilerSetUtils.getCommandFolder(null);
        if (folder != null) {
            newShellCommand = pi.findCommand(folder, shellCommand);
            if (newShellCommand != null) {
                return newShellCommand;
            }
        }
        return shellCommand;
    }
}
