/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.Future;
import javax.swing.SwingUtilities;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.execution.ShellExecSupport;
import org.netbeans.modules.cnd.loaders.ShellDataObject;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * Base class for Make Actions ...
 */
public class ShellRunAction extends AbstractExecutorRunAction {

    public String getName() {
        return getString("BTN_Run"); // NOI18N
    }

    @Override
    protected boolean accept(DataObject object) {
        return object instanceof ShellDataObject;
    }

    protected void performAction(Node[] activatedNodes) {
        // Save everything first
        LifecycleManager.getDefault().saveAll();

        for (int i = 0; i < activatedNodes.length; i++) {
            performAction(activatedNodes[i]);
        }
    }


    public static void performAction(Node node) {
        performAction(node, null, null, null);
    }

    public static void performAction(final Node node, final ExecutionListener listener, final Writer outputListener, final Project project) {
        if (SwingUtilities.isEventDispatchThread()){
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    _performAction(node, listener, outputListener, project);
                }
            });
        } else {
            _performAction(node, listener, outputListener, project);
        }
    }

    private static void _performAction(Node node, final ExecutionListener listener, final Writer outputListener, Project project) {
        ShellExecSupport bes = node.getCookie(ShellExecSupport.class);
        if (bes == null) {
            return;
        }
        //Save file
        saveNode(node);
        DataObject dataObject = node.getCookie(DataObject.class);
        FileObject fileObject = dataObject.getPrimaryFile();
        
        File shellFile = FileUtil.toFile(fileObject);
        // Build directory
        String bdir = bes.getRunDirectory();
        File buildDir = getAbsoluteBuildDir(bdir, shellFile);
        // Tab Name
        String tabName = getString("RUN_LABEL", node.getName()); // NOI18N
        
        String[] shellCommandAndArgs = bes.getShellCommandAndArgs(fileObject); // from inside shell file or properties
        String shellCommand = shellCommandAndArgs[0];
        String shellFilePath = IpeUtils.toRelativePath(buildDir.getPath(), shellFile.getPath()); // Absolute path to shell file
        if (shellFilePath.equals(shellFile.getName())) {
            shellFilePath = "."+File.separatorChar+shellFilePath; //NOI18N
        }
        String[] args = bes.getArguments(); // from properties

        ExecutionEnvironment execEnv = getExecutionEnvironment(fileObject, project);
        if (execEnv.isRemote()) {
            String s = HostInfoProvider.getMapper(execEnv).getRemotePath(buildDir.getAbsolutePath());
            if (s != null) {
                buildDir = new File(s);
            }
        }
        // Windows: The command is usually of the from "/bin/sh", but this
        // doesn't work here, so extract the 'sh' part and use that instead. 
        // FIXUP: This is not entirely correct though.
        if (PlatformInfo.getDefault(execEnv).isWindows() && shellCommand.length() > 0) {
            int i = shellCommand.lastIndexOf("/"); // UNIX PATH // NOI18N
            if (i >= 0) {
                shellCommand = shellCommand.substring(i+1);
            }
        }
        
        StringBuilder argsFlat = new StringBuilder();
        if (shellCommandAndArgs[0].length() > 0) {
            for (int i = 1; i < shellCommandAndArgs.length; i++) {
                argsFlat.append(" "); // NOI18N
                argsFlat.append(shellCommandAndArgs[i]);
            }
        }
        argsFlat.append(shellFilePath);
        for (int i = 0; i < args.length; i++) {
            argsFlat.append(" "); // NOI18N
            argsFlat.append(args[i]);
        }
        Map<String, String> envMap = getEnv(execEnv, node, null);
        traceExecutable(shellCommand, buildDir, argsFlat, envMap);
        InputOutput _tab = IOProvider.getDefault().getIO(tabName, false); // This will (sometimes!) find an existing one.
        _tab.closeInputOutput(); // Close it...
        final InputOutput tab = IOProvider.getDefault().getIO(tabName, true); // Create a new ...
        try {
            tab.getOut().reset();
        } catch (IOException ioe) {
        }
        ProcessChangeListener processChangeListener = new ProcessChangeListener(listener, tab, "Run"); // NOI18N
        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv)
        .setWorkingDirectory(buildDir.getPath())
        .setCommandLine(quoteExecutable(shellCommand)+" "+argsFlat.toString()) // NOI18N
        .unbufferOutput(false)
        .addEnvironmentVariables(envMap)
        .addNativeProcessListener(processChangeListener);
        npb.redirectError();

        ExecutionDescriptor descr = new ExecutionDescriptor()
        .controllable(true)
        .frontWindow(true)
        .inputVisible(true)
        .inputOutput(tab)
        .outLineBased(true)
        .showProgress(true)
        .postExecution(processChangeListener)
        .outConvertorFactory(new ProcessLineConvertorFactory(outputListener, null));
        // Execute the shellfile
        final ExecutionService es = ExecutionService.newService(npb, descr, "Run"); // NOI18N
        Future<Integer> result = es.run();
    }
}
