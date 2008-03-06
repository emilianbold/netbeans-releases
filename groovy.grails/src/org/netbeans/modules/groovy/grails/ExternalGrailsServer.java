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
import org.netbeans.modules.groovy.grails.settings.Settings;
import org.openide.util.Utilities;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author schmidtm
 */
public class ExternalGrailsServer implements GrailsServer{

    CountDownLatch outputReady = new CountDownLatch(1);
    GrailsServerRunnable gsr;
    String cwdName;
    ExecutionEngine engine = ExecutionEngine.getDefault();
    Project prj;
    Exception lastException = null; // last problem in the runnable.
    GrailsServerState serverState = null;
    ExecutorTask exTask;
    
    private  final Logger LOG = Logger.getLogger(ExternalGrailsServer.class.getName());
    
    boolean checkForGrailsExecutable ( File pathToGrails ) {
        String pathToBinary = Utilities.isWindows() ? "\\bin\\grails.bat" : "/bin/grails"; // NOI18N
        return new File (pathToGrails, pathToBinary).isFile ();
    }
    
    
    public boolean serverConfigured () {
        Settings settings = Settings.getInstance();
        
        if(settings == null)
            return false;

        String grailsBase = settings.getGrailsBase();
        
        if(grailsBase == null)
            return false;
                
        return checkForGrailsExecutable(new File(grailsBase));
        }
    
    String prependOption(){
        if (prj == null) {
            return "";
        }
        
        String retVal = "";
        
        GrailsProjectConfig prjConfig = new GrailsProjectConfig(prj);
        
        if (prjConfig != null){
            String port = prjConfig.getPort();
            
            if(port != null && ! port.equals("")){
                if(port.matches("\\d+")){ 
                    retVal = " -Dserver.port=" + port + " ";
                } else {
                    LOG.log(Level.WARNING, "This seems to be no number: " + port);
                    }
            }
            
            String env = prjConfig.getEnv();
            
            if(env != null && ! env.equals("")){
                retVal = retVal + " -Dgrails.env=" + env + " ";
            }
            
        }
        return retVal;    
        }
    
    public Process runCommand(Project prj, String cmd, InputOutput io, String dirName) {
        
        // LOG.setLevel(Level.FINEST);
        
        this.prj = prj;
        
        if(prj != null) {
            cwdName = FileUtil.getFileDisplayName(prj.getProjectDirectory());
            LOG.log(Level.FINEST, "Current working dir: " + cwdName);
            }
        
    
        if(cmd.startsWith("create-app")) {
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
            }
        else if(cmd.startsWith("run-app")) {

            String pslistTag = Utilities.isWindows() ? " REM NB:" +  // NOI18N
                    prj.getProjectDirectory().getName() : "";
            
            gsr = new GrailsServerRunnable(outputReady, true, cwdName, prependOption() + cmd + pslistTag);
            exTask = engine.execute("", gsr, io);

            waitForOutput();

        }
        else if(cmd.startsWith("shell")) {

            gsr = new GrailsServerRunnable(outputReady, false, cwdName, prependOption() + cmd);
            //new Thread(gsr).start();
            exTask = engine.execute("", gsr, io);

            waitForOutput();
        }
        else {
            gsr = new GrailsServerRunnable(outputReady, true, cwdName, prependOption() + cmd);
            exTask = engine.execute("", gsr, io);

            waitForOutput();
        }
        
        lastException = gsr.getLastError();
        return gsr.getProcess();
    }
    
    void waitForOutput() {
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
                    exTask.addTaskListener(serverState);
                } else {
                    LOG.log(Level.WARNING, "Could not startup process : " + gsr.getLastError().getLocalizedMessage());
                    lastException = gsr.getLastError();
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
