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

import org.netbeans.modules.groovy.grails.api.GrailsServer;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;
import org.openide.util.Exceptions;
import org.openide.windows.InputOutput;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.grails.api.GrailsServerState;
import java.util.concurrent.CountDownLatch;
import java.io.BufferedReader;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.File;
import org.netbeans.modules.groovy.grails.settings.Settings;

/**
 *
 * @author schmidtm
 */
public class ExternalGrailsServer implements GrailsServer{

    CountDownLatch outputReady = new CountDownLatch(1);
    GrailsServerRunnable gsr;
    
    private  final Logger LOG = Logger.getLogger(ExternalGrailsServer.class.getName());
    
    boolean checkForGrailsExecutable ( File pathToGrails ) {
        return new File (new File (pathToGrails, "bin"), "grails").isFile ();
        }
    
    
    public boolean serverConfigured () {
        Settings settings = Settings.getInstance();

        return checkForGrailsExecutable(new File(settings.getGrailsBase()));
        }
    
    
    public Process runCommand(Project prj, String cmd, InputOutput io, String dirName) {
                
        if(cmd.startsWith("create-app")) {
            // in this case we don't have a Project yet, therefore i should be null
            assert prj == null;
            assert io ==  null;
                
            // split dirName in directory to create and parent:
            int lastSlash = dirName.lastIndexOf(File.separator);
            String workDir = dirName.substring(0, lastSlash);
            String newDir  = dirName.substring(lastSlash + 1);
            
            gsr = new GrailsServerRunnable(outputReady, workDir, "create-app " + newDir);
            new Thread(gsr).start();

            try {
                outputReady.await();
                } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                        }
        
            }
        else if(cmd.startsWith("run-app")) {

                String tabName = "Grails Server for: " + prj.getProjectDirectory().getName();
                
                ExecutionEngine engine = ExecutionEngine.getDefault();

                String cwdName = File.separator + prj.getProjectDirectory().getPath();
                
                gsr = new GrailsServerRunnable(outputReady, cwdName, cmd);
                ExecutorTask exTask = engine.execute(tabName, gsr, io);

                try {
                    outputReady.await();
                    } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                            }

                GrailsServerState serverState = prj.getLookup().lookup(GrailsServerState.class);

                if (serverState != null) {
                    serverState.setRunning(true);
                    serverState.setExTask(exTask);
                    exTask.addTaskListener(serverState);
                    }
                else {
                    LOG.log(Level.WARNING, "Could not get serverState through lookup");
                    }
        }
        else if(cmd.startsWith("shell")) {

                String tabName = "Grails Shell for: " + prj.getProjectDirectory().getName();
                
                ExecutionEngine engine = ExecutionEngine.getDefault();
                
                String cwdName = File.separator + prj.getProjectDirectory().getPath();
                
                gsr = new GrailsServerRunnable(outputReady, cwdName, cmd);
                ExecutorTask exTask = engine.execute(tabName, gsr, io);

                try {
                    outputReady.await();
                    } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                            }

//                GrailsServerState serverState = prj.getLookup().lookup(GrailsServerState.class);
//
//                if (serverState != null) {
//                    serverState.setRunning(true);
//                    serverState.setExTask(exTask);
//                    exTask.addTaskListener(serverState);
//                    }
//                else {
//                    LOG.log(Level.WARNING, "Could not get serverState through lookup");
//                    }
        }
        
        return gsr.getProcess();
    }

}
