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

package org.netbeans.modules.web.clientproject.node;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
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
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;

/** 
 * Executes an node command asynchronously in the IDE.
 */
public final class NodeExecutor implements Runnable {

    private final List<String> arguments;
    private final String displayName;
    private final FileObject root;
    private final String command;

    public NodeExecutor (String title, String command, FileObject root, String[] args) {
        arguments = ((args == null) ? null : Arrays.asList(args));
        this.root = root;
        Project owner = FileOwnerQuery.getOwner(root);
        String name = ProjectUtils.getInformation(owner).getDisplayName();
        displayName = title;
        this.command = command;
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
                    pb= pb.addArgument("/C " + command + " " + arguments.get(0));
                } else if (Utilities.isMac()) {
                    pb = new ExternalProcessBuilder("/bin/bash");
                    pb = pb.addArgument("-lc");
                    pb = pb.addArgument(command + " " + arguments.get(0));
                } else {
                    pb = new ExternalProcessBuilder(command);
                    pb = pb.addArgument(arguments.get(0));
                }

                pb = pb.workingDirectory(FileUtil.toFile(root));
                pb = pb.redirectErrorStream(true);
                try {
                    return pb.call();
                } catch (IOException ioe) {
                    if (!Utilities.isWindows() && !Utilities.isMac()) {
                        //node not found on path on Linux. Run bash and try run 
                        //node inside bash. It will at least do output to user
                        pb = new ExternalProcessBuilder("/bin/bash");
                        pb = pb.addArgument("-lc");
                        pb = pb.addArgument(command + " " + arguments.get(0));
                        return pb.call();
                    } else {
                        throw ioe;
                    }
                }
            }

        };
        
        ExecutionDescriptor desc = new ExecutionDescriptor();
        desc = desc.showProgress(true);
        desc = desc.frontWindow(true);
        desc = desc.controllable(true);
        ExecutionService execution = ExecutionService.newService(creator, desc, displayName);
        try {
            execution.run().get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        } catch (CancellationException ex) {
            //ignore. Task was cancelled by use.
        }
    }
}
