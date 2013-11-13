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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/** 
 * Executes an Gruntfile asynchronously in the IDE.
 */
public final class GruntfileExecutor implements Runnable {

    private static final RequestProcessor RP = new RequestProcessor(GruntfileExecutor.class.getName(), Integer.MAX_VALUE);

    private InputOutput io;
    private OutputStream outputStream;
    private boolean ok = false;
    private final List<String> targetNames;
    /** used for the tab etc. */
    private String displayName;
    private String suggestedDisplayName;
    private final FileObject gruntFile;

    /** targets may be null to indicate default target */
    public GruntfileExecutor (FileObject gruntFile, String[] targets) {
        targetNames = ((targets == null) ? null : Arrays.asList(targets));
        this.gruntFile = gruntFile;
    }
  

    void setDisplayName(String n) {
        suggestedDisplayName = n;
    }
    
    private static String getProcessDisplayName(List<String> targetNames) {
        return "project name";
    }
    

    /**
     * Actually start the process.
     */
    public ExecutorTask execute () throws IOException {
        String dn = suggestedDisplayName != null ? suggestedDisplayName : getProcessDisplayName(targetNames);
        final ExecutorTask task;
        synchronized (this) {
            task = ExecutionEngine.getDefault().execute(displayName, this, InputOutput.NULL);
        }
        WrapperExecutorTask wrapper = new WrapperExecutorTask(task, io);
        RP.post(wrapper);
        return wrapper;
    }
    
    public ExecutorTask execute(OutputStream outputStream) throws IOException {
        this.outputStream = outputStream;
        ExecutorTask task = ExecutionEngine.getDefault().execute(null, this, InputOutput.NULL);
        return new WrapperExecutorTask(task, null);
    }
    
    private class WrapperExecutorTask extends ExecutorTask {
        private final ExecutorTask task;
        private final InputOutput io;
        public WrapperExecutorTask(ExecutorTask task, InputOutput io) {
            super(new WrapperRunnable(task));
            this.task = task;
            this.io = io;
        }
        @Override
        public void stop () {
                task.stop();
        }
        @Override
        public int result () {
            return task.result () + (ok ? 0 : 1);
        }
        @Override
        public InputOutput getInputOutput () {
            return io;
        }
    }
    private static class WrapperRunnable implements Runnable {
        private final ExecutorTask task;
        public WrapperRunnable(ExecutorTask task) {
            this.task = task;
        }
        @Override
        public void run () {
            task.waitFinished ();
        }
    }
  
    /** Call execute(), not this method directly!
     */
    synchronized public @Override void run () {
        ProcessBuilder pb;
        if (Utilities.isWindows()) {
            pb = new ProcessBuilder("cmd","/C grunt --no-color " + targetNames.get(0));
        } else if (Utilities.isMac()) {
            pb = new ProcessBuilder("bash","-lc", "grunt --no-color " + targetNames.get(0));
        } else {
            pb = new ProcessBuilder("grunt","--no-color", targetNames.get(0));
        }
        pb.directory(FileUtil.toFile(gruntFile.getParent()));
        Project owner = FileOwnerQuery.getOwner(gruntFile);
        String tab;
        if (owner!=null) {
            tab = ProjectUtils.getInformation(owner).getDisplayName() + " (Grunt)";
        } else {
            tab = gruntFile.getName();
        }
        InputOutput io1 = IOProvider.getDefault().getIO(tab, false);
        io1.select();
        try {
            io1.getOut().reset();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        try {
            Process start = pb.start();
            start.waitFor();
            char[] c = new char[1];
            InputStreamReader errorStream = new InputStreamReader(new BufferedInputStream(start.getErrorStream()));
            while (errorStream.ready()) {
                errorStream.read(c);
                io1.getErr().write(c);
            }
            InputStreamReader inStream = new InputStreamReader(new BufferedInputStream(start.getInputStream()));
            while (inStream.ready()) {
                inStream.read(c);
                io1.getOut().write(c);
            }
        } catch (IOException | InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
