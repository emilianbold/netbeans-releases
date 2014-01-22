/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.web.clientproject.grunt;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;

/** 
 * Executes an Gruntfile asynchronously in the IDE.
 */
public final class GruntfileExecutor implements Runnable {

    private final List<String> targetNames;
    private final String displayName;
    private final FileObject gruntFile;

    /** targets may be null to indicate default target */
    
    @NbBundle.Messages({
        "# {0} - Project Name", 
        "# {1} - Task Name",    
        "TXT_GruntTabTitle={0} ({1})"
    })
    public GruntfileExecutor (FileObject gruntFile, String[] targets) {
        targetNames = ((targets == null) ? null : Arrays.asList(targets));
        this.gruntFile = gruntFile;

        Project owner = FileOwnerQuery.getOwner(gruntFile);
        if (owner!=null) {
            displayName = Bundle.TXT_GruntTabTitle(ProjectUtils.getInformation(owner).getDisplayName(), targets[0]);
        } else {
            displayName = gruntFile.getName();
        }
    }
    
    /**
     * Actually start the process.
     */
    public ExecutorTask execute () throws IOException {
        final ExecutorTask task;
        synchronized (this) {
            task = ExecutionEngine.getDefault().execute(displayName, this, InputOutput.NULL);
        }
        return task;
    }
    
  
    /** Call execute(), not this method directly!
     */
    synchronized public @Override void run () {
        
        Callable<Process> creator = new Callable<Process>() {

            @Override
            public Process call() throws Exception {
                ExternalProcessBuilder pb;
                if (Utilities.isWindows()) {
                    pb = new ExternalProcessBuilder("cmd");
                    pb= pb.addArgument("/C grunt --no-color " + targetNames.get(0));
                } else if (Utilities.isMac()) {
                    pb = new ExternalProcessBuilder("/bin/bash");
                    pb = pb.addArgument("-lc");
                    pb = pb.addArgument("grunt --no-color " + targetNames.get(0));
                } else {
                    pb = new ExternalProcessBuilder("grunt");
                    pb = pb.addArgument("--no-color");
                    pb = pb.addArgument(targetNames.get(0));
                }

                pb = pb.workingDirectory(FileUtil.toFile(gruntFile.getParent()));
                pb = pb.redirectErrorStream(true);
                return pb.call();
            }

        };
        
        ExecutionDescriptor desc = new ExecutionDescriptor();
        desc = desc.showProgress(true);
        desc = desc.frontWindow(true);
        desc = desc.controllable(true);
        ExecutionService execution = ExecutionService.newService(creator, desc, displayName);
        execution.run();
    }
}
