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
import org.netbeans.modules.cnd.loaders.CMakeDataObject;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 *
 * @author Alexander Simon
 */
public class CMakeAction extends AbstractExecutorRunAction {

    @Override
    public String getName () {
        return getString("BTN_Cmake"); // NOI18N
    }

    @Override
    protected boolean accept(DataObject object) {
        return object instanceof CMakeDataObject;
    }

    protected void performAction(Node[] activatedNodes) {
        for (int i = 0; i < activatedNodes.length; i++){
            performAction(activatedNodes[i]);
        }
    }

    protected void performAction(Node node) {
        performAction(node, null, null, null, null);
    }

    public static void performAction(Node node, ExecutionListener listener, Writer outputListener, Project project, List<String> additionalEnvironment) {
        DataObject dataObject = node.getCookie(DataObject.class);
        FileObject fileObject = dataObject.getPrimaryFile();
        File proFile = FileUtil.toFile(fileObject);
        // Build directory
        File buildDir = getCBuildDirectory(node);
        // Executable
        String executable = getCommand(node, project, Tool.CMakeTool, "cmake"); // NOI18N
        // Arguments
        //String arguments = proFile.getName();
        String arguments = "-G \"Unix Makefiles\" -DCMAKE_BUILD_TYPE=Debug -DCMAKE_CXX_FLAGS=\"-g3 -gdwarf-2\" -DCMAKE_C_FLAGS=\"-g3 -gdwarf-2\""; // NOI18N
        // Tab Name
        String tabName = getString("CMAKE_LABEL", node.getName());

        ExecutionEnvironment execEnv = getExecutionEnvironment(fileObject, project);
        String[] env = prepareEnv(execEnv);
        if (additionalEnvironment != null && additionalEnvironment.size()>0){
            String[] tmp = new String[env.length + additionalEnvironment.size()];
            for(int i=0; i < env.length; i++){
                tmp[i] = env[i];
            }
            for(int i=0; i < additionalEnvironment.size(); i++){
                tmp[env.length + i] = additionalEnvironment.get(i);
            }
            env = tmp;
        }
        // Execute the makefile
        NativeExecutor nativeExecutor = new NativeExecutor(
                execEnv,
                buildDir.getPath(),
                executable,
                arguments,
                env,
                tabName,
                "cmake", // NOI18N
                false,
                true,
                false);
        if (outputListener != null) {
            nativeExecutor.setOutputListener(outputListener);
        }
        new ShellExecuter(nativeExecutor, listener).execute();
    }

    private static File getCBuildDirectory(Node node){
        DataObject dataObject = node.getCookie(DataObject.class);
        FileObject fileObject = dataObject.getPrimaryFile();
        File qMakefile = FileUtil.toFile(fileObject);
        String bdir = qMakefile.getParent();
        File buildDir = getAbsoluteBuildDir(bdir, qMakefile);
        return buildDir;
    }
}
