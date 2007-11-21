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
    String cwdName;
    ExecutionEngine engine = ExecutionEngine.getDefault();
    
    private  final Logger LOG = Logger.getLogger(ExternalGrailsServer.class.getName());
    
    boolean checkForGrailsExecutable ( File pathToGrails ) {
        return new File (new File (pathToGrails, "bin"), "grails").isFile ();
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
    
    
    public Process runCommand(Project prj, String cmd, InputOutput io, String dirName) {
        
        if(prj != null) {
            cwdName = File.separator + prj.getProjectDirectory().getPath();
            }
                
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

            waitForOutput();
            }
        else if(cmd.startsWith("create-domain-class") || 
                cmd.startsWith("create-controller")   || 
                cmd.startsWith("create-service")) {

            assert io ==  null;
            
            gsr = new GrailsServerRunnable(outputReady, cwdName, cmd);
            new Thread(gsr).start();

            waitForOutput();
            }       
        else if(cmd.startsWith("run-app")) {

            String tabName = "Grails Server for: " + prj.getProjectDirectory().getName();

            gsr = new GrailsServerRunnable(outputReady, cwdName, cmd);
            ExecutorTask exTask = engine.execute(tabName, gsr, io);

            waitForOutput();

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

            gsr = new GrailsServerRunnable(outputReady, cwdName, cmd);
            new Thread(gsr).start();

            waitForOutput();
        }
        else {
            LOG.log(Level.WARNING, "unknown server command: " + cmd);
            return null;
        
        }
        
        return gsr.getProcess();
    }
    
    void waitForOutput(){
        try {
            outputReady.await();
            } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                    }
        
        }
}
