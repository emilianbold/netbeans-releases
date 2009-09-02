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

package org.netbeans.modules.cnd.actions;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.cnd.loaders.QtProjectDataObject;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessChangeEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Alexander Simon
 */
public class QMakeAction extends AbstractExecutorRunAction {

    @Override
    public String getName () {
        return getString("BTN_Qmake"); // NOI18N
    }

    @Override
    protected boolean accept(DataObject object) {
        return object instanceof QtProjectDataObject;
    }

    protected void performAction(Node[] activatedNodes) {
        for (int i = 0; i < activatedNodes.length; i++){
            performAction(activatedNodes[i]);
        }
    }

    protected void performAction(Node node) {
        performAction(node, null, null, null);
    }

    public static void performAction(Node node, final ExecutionListener listener, final Writer outputListener, Project project) {
        DataObject dataObject = node.getCookie(DataObject.class);
        FileObject fileObject = dataObject.getPrimaryFile();
        File proFile = FileUtil.toFile(fileObject);
        // Build directory
        File buildDir = getBuildDirectory(node,Tool.QMakeTool);
        // Executable
        String executable = getCommand(node, project, Tool.QMakeTool, "qmake"); // NOI18N
        // Arguments
        String arguments = proFile.getName();// + " " + getArguments(node, Tool.QMakeTool); // NOI18N
        String[] args = getArguments(node, Tool.QMakeTool); // NOI18N
        // Tab Name
        String tabName = getString("QMAKE_LABEL", node.getName()); // NOI18N

        ExecutionEnvironment execEnv = getExecutionEnvironment(fileObject, project);
        Map<String, String> envMap = getEnv(execEnv, node, null);
        StringBuilder argsFlat = new StringBuilder(arguments);
        for (int i = 0; i < args.length; i++) {
            argsFlat.append(" "); // NOI18N
            argsFlat.append(args[i]);
        }
        traceExecutable(executable, buildDir, argsFlat, envMap);
        InputOutput _tab = IOProvider.getDefault().getIO(tabName, false); // This will (sometimes!) find an existing one.
        _tab.closeInputOutput(); // Close it...
        final InputOutput tab = IOProvider.getDefault().getIO(tabName, true); // Create a new ...
        try {
            tab.getOut().reset();
        } catch (IOException ioe) {
        }
        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv)
        .setCommandLine(quoteExecutable(executable)+" "+argsFlat) // NOI18N
        .setWorkingDirectory(buildDir.getPath())
        .addEnvironmentVariables(envMap)
        .unbufferOutput(false)
        .addNativeProcessListener(new ProcessChangeListener(listener, tab, "QMake")); // NOI18N
        npb.redirectError();

        ExecutionDescriptor descr = new ExecutionDescriptor()
        .controllable(true)
        .frontWindow(true)
        .inputVisible(true)
        .inputOutput(tab)
        .outLineBased(true)
        .showProgress(true)
        .outConvertorFactory(new ProcessLineConvertorFactory(outputListener, null));
        final ExecutionService es = ExecutionService.newService(npb, descr, "qmake"); // NOI18N
        Future<Integer> result = es.run();
    }
}
