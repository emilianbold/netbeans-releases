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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grails;

import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grails.api.GrailsServer;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;
import org.openide.util.Exceptions;
import org.openide.windows.InputOutput;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.grails.api.GrailsServerState;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.File;
import org.netbeans.modules.groovy.grails.api.GrailsEnvironment;
import org.openide.util.Utilities;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author schmidtm
 */
public class ExternalGrailsServer implements GrailsServer{

    private final CountDownLatch outputReady = new CountDownLatch(1);
    private final ExecutionEngine engine = ExecutionEngine.getDefault();

    private GrailsServerRunnable gsr;
    private String cwdName;
    private Project prj;
    private Exception lastException;
    private GrailsServerState serverState;
    private ExecutorTask exTask;

    private static final Logger LOG = Logger.getLogger(ExternalGrailsServer.class.getName());

    private String prependOption() {
        if (prj == null) {
            return "";
        }

        String retVal = "";

        GrailsProjectConfig prjConfig = GrailsProjectConfig.forProject(prj);

        if (prjConfig != null) {
            String port = prjConfig.getPort();

            if (port != null && !port.equals("")) {
                if (port.matches("\\d+")) { //NOI18N
                    retVal = " -Dserver.port=" + port + " "; // NOI18N
                } else {
                    LOG.log(Level.WARNING, "This seems to be no number: " + port); // NOI18N
                }
            }

            GrailsEnvironment env = prjConfig.getEnvironment();

            if (env != null && !env.equals("")) {
                retVal = retVal + " -Dgrails.env=" + env + " "; // NOI18N
            }

        }
        return retVal;
    }

    public Process runCommand(Project prj, String cmd, InputOutput io, String dirName) {
        this.prj = prj;

        if (prj != null) {
            cwdName = FileUtil.getFileDisplayName(prj.getProjectDirectory());
            LOG.log(Level.FINEST, "Current working dir: " + cwdName);
        }


        if (cmd.startsWith("create-app")) { // NOI18N
            // in this case we don't have a Project yet, therefore i should be null
            assert prj == null;
            assert dirName != null;

            // split dirName in directory to create and parent (used for wd)
            int lastSlash = dirName.lastIndexOf(File.separator);
            String workDir = dirName.substring(0, lastSlash);
            String newDir  = dirName.substring(lastSlash + 1);

            gsr = new GrailsServerRunnable(outputReady, true, workDir, prependOption() + "create-app " + newDir);
            exTask = engine.execute("", gsr, io);

            waitForOutput();
        } else if (cmd.startsWith("run-app")) { // NOI18N

            String pslistTag = Utilities.isWindows() ? " REM NB:" +  // NOI18N
                    prj.getProjectDirectory().getName() : "";

            gsr = new GrailsServerRunnable(outputReady, true, cwdName, prependOption() + cmd + pslistTag);
            exTask = engine.execute("", gsr, io);

            waitForOutput();
        } else if (cmd.startsWith("shell")) { // NOI18N

            gsr = new GrailsServerRunnable(outputReady, false, cwdName, prependOption() + cmd);
            //new Thread(gsr).start();
            exTask = engine.execute("", gsr, io);

            waitForOutput();
        } else {
            gsr = new GrailsServerRunnable(outputReady, true, cwdName, prependOption() + cmd);
            exTask = engine.execute("", gsr, io);

            waitForOutput();
        }

        lastException = gsr.getLastException();
        return gsr.getProcess();
    }

    private void waitForOutput() {
        try {
            outputReady.await();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (prj != null) {
            serverState = prj.getLookup().lookup(GrailsServerState.class);

            if (serverState != null) {
                Process proc = gsr.getProcess();
                if (proc != null) {
                    serverState.setRunning(true);
                    serverState.setProcess(proc);
                } else {
                    LOG.log(Level.WARNING, "Could not startup process : " + gsr.getLastException().getLocalizedMessage());
                    lastException = gsr.getLastException();
                }
            } else {
                LOG.log(Level.WARNING, "Could not get serverState through lookup");
            }
        }

    }

    public Exception getLastError() {
        return lastException;
    }

}
