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
import java.io.Writer;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.cnd.api.execution.NativeExecutor;
import org.netbeans.modules.cnd.loaders.QtProjectDataObject;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 *
 * @author Alexander Simon
 */
public class QMakeAction extends AbstractExecutorRunAction {
    private static final boolean TRACE = true;

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

    public static void performAction(Node node, ExecutionListener listener, Writer outputListener, Project project) {
        DataObject dataObject = node.getCookie(DataObject.class);
        FileObject fileObject = dataObject.getPrimaryFile();
        File proFile = FileUtil.toFile(fileObject);
        // Build directory
        File buildDir = getBuildDirectory(node,Tool.QMakeTool);
        // Executable
        String executable = getCommand(node, project, Tool.QMakeTool, "qmake"); // NOI18N
        // Arguments
        String arguments = proFile.getName();// + " " + getArguments(node, Tool.QMakeTool); // NOI18N
        // Tab Name
        String tabName = getString("QMAKE_LABEL", node.getName());

        String[] additionalEnvironment = getAdditionalEnvirounment(node);
        ExecutionEnvironment execEnv = getExecutionEnvironment(fileObject, project);
        String[] env = prepareEnv(execEnv);
        if (additionalEnvironment != null && additionalEnvironment.length>0){
            String[] tmp = new String[env.length + additionalEnvironment.length];
            for(int i=0; i < env.length; i++){
                tmp[i] = env[i];
            }
            for(int i=0; i < additionalEnvironment.length; i++){
                tmp[env.length + i] = additionalEnvironment[i];
            }
            env = tmp;
        }
        if (TRACE) {
            System.err.println("Run "+executable);
            System.err.println("\tin folder   "+buildDir.getPath());
            System.err.println("\targuments   "+arguments);
            System.err.println("\tenvironment ");
            for(String v : env) {
                System.err.println("\t\t"+v);
            }
        }
        // Execute the makefile
        NativeExecutor nativeExecutor = new NativeExecutor(
                execEnv,
                buildDir.getPath(),
                executable,
                arguments,
                env,
                tabName,
                "qmake", // NOI18N
                false,
                true,
                false);
        if (outputListener != null) {
            nativeExecutor.setOutputListener(outputListener);
        }
        new ShellExecuter(nativeExecutor, listener).execute();
    }

}
