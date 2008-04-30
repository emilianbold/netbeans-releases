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

package org.netbeans.modules.groovy.grails.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.netbeans.api.project.Project;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.Utilities;

/**
 *
 * @author schmidtm
 */
public class GrailsServerState implements TaskListener{
    private String name;
    private boolean running = false;
    Process process;
    Project prj;
    private  final Logger LOG = Logger.getLogger(GrailsServerState.class.getName());
    long    TIMEOUT = 8000L;
    
    public GrailsServerState (Project prj, String name){
        this.name = name;
        this.prj = prj;
        // LOG.setLevel(Level.FINEST);
        }

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized void setRunning(boolean running) {
        this.running = running;
        LOG.log(Level.FINEST, "Project: " + name + " , setRunning() called: " + running );
    }

    public void taskFinished(Task task) {
        synchronized(this) {
            running = false;
        }
        LOG.log(Level.FINEST, "Project: " + name + " , taskFinished() called");
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }
    
    public void destroy() {
        if (process != null) {
            // if we are dealing with windows we have no concept of a 
            // a process-group, therefore Process.destroy() only kills one 
            // process and leaves the rest of the chain orphaned. 
            // Therefore we got to find our tagged process ourselves using 
            // tasklist/wmic and kill it with taskkill

            if (Utilities.isWindows()) {

                // find PID for this grails server
                // wmic process where name="cmd.exe" get processid, commandline
                
                String params[] = {"process", "where", "name=\"cmd.exe\"", 
                                    "get", "processid,commandline" }; 
                
                WindowsCommandRunner r = new WindowsCommandRunner(
                        "wmic.exe",
                        Utilities.escapeParameters(params), name);
              
                assert r != null;

                Thread t = new Thread(r);
                t.start();

                try {
                    t.join(TIMEOUT);
                } catch (InterruptedException ex) {
                    LOG.log(Level.FINEST, "Thread.join() interrupted: " + ex);
                }

                // kill running server using taskkill
                
                String pidToKill =  r.getPid();
                
                if (pidToKill != null) {
                    WindowsCommandRunner killer = new WindowsCommandRunner(
                            "taskkill.exe", "/F /PID " + pidToKill + " /T", null);

                    assert killer != null;

                    Thread tk = new Thread(killer);
                    tk.start();

                }
                
                
            } else {
                process.destroy();
            }
            synchronized(this) {
                running = false;
            }
        } else {
            LOG.log(Level.FINEST, "Project: " + name + " , destroy() called, but no process running");
        }
    }
    
    class WindowsCommandRunner implements Runnable {
        
        String cmd;
        String args;
        String nameToFilter;
        String dectedPid = null;

        public WindowsCommandRunner(String cmd, String args, String nameToFilter) {
            this.cmd = cmd;
            this.args = args;
            this.nameToFilter = nameToFilter;
        }
        
        String getPid(){
            return dectedPid;
        }
        
        public void run() {
            LOG.log(Level.FINEST, "WindowsCommandRunner.run(): " + cmd + " " + args);
            
            NbProcessDescriptor cmdProcessDesc = new NbProcessDescriptor(cmd, args);
            
            try {
                Process utilityProcess = cmdProcessDesc.exec(null, null, true, null);
                
                if(utilityProcess == null ){
                    LOG.log(Level.FINEST, "utilityProcess == null");
                    return;
                    }
                
                utilityProcess.getOutputStream().close();
                
                // we wait till the process finishes. De-coupling is done a layer above. 
                
                try {
                    utilityProcess.waitFor();
                } catch (InterruptedException ex) {
                    LOG.log(Level.FINEST, "Project: " + name + " waitFor() problem : " + ex);
                }
                
                if (nameToFilter != null) {
                    BufferedReader procOutput = new BufferedReader(new InputStreamReader(utilityProcess.getInputStream()));
                    
                    assert procOutput != null;
                    String errString;
                    
                    while ((errString = procOutput.readLine()) != null) {

                        String regEx = ".*grails.bat +run-app +REM NB:" + nameToFilter + ".*";

                        if (errString.matches(regEx)) {
                            String nbTag = "REM NB:" + nameToFilter;
                            int idx = errString.indexOf(nbTag);
                            idx = idx + nbTag.length();
                            dectedPid = errString.substring(idx).trim();
                            LOG.log(Level.FINEST, "FOUND: " + dectedPid);
                        }

                    }
                }
                
            } catch (IOException ex) {
                LOG.log(Level.FINEST, "Project: " + name + " exec() problem : " + ex);
            }
            LOG.log(Level.FINEST, "WindowsCommandRunner.run(): END");
        }
        
    }
    
    
    
}
