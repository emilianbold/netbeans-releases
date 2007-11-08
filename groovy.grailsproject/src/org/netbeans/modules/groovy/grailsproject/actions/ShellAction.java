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
package org.netbeans.modules.groovy.grailsproject.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.netbeans.api.project.Project;

import java.io.BufferedReader;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.grails.api.GrailsServer;
import org.netbeans.modules.groovy.grails.api.GrailsServerFactory;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.netbeans.modules.groovy.grailsproject.StreamRedirectThread;
import org.netbeans.modules.groovy.grailsproject.StreamInputThread;

/*
import java.util.logging.Level;
import org.openide.windows.OutputWriter;
import java.io.IOException;
import java.io.OutputStream;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;
import org.openide.execution.NbProcessDescriptor;
import java.io.File;
*/

public class ShellAction extends AbstractAction {

    Project prj;
            
    public ShellAction (Project prj){
        super ("Open Shell");
        this.prj = prj;
    }

    public boolean isEnabled(){
            return true;
        }
            
    public void actionPerformed(ActionEvent e) {
        new PrivateSwingWorker().start();
    }
    
        public class PrivateSwingWorker extends Thread {

        BufferedReader procOutput;
        private  final Logger LOG = Logger.getLogger(ShellAction.class.getName());
        Process process;
        InputOutput io;

        public void run() {
            
                String tabName = "Grails Shell for: " + prj.getProjectDirectory().getName();
                io = IOProvider.getDefault().getIO(tabName, false);

                io.setInputVisible(true);
                io.select();
                

                GrailsServer server = GrailsServerFactory.getServer();
                Process process = server.runCommand(prj, "shell", io, null);
               
                assert process != null;
                
                (new StreamInputThread   (process.getOutputStream(), io.getIn())).start();
                (new StreamRedirectThread(process.getInputStream(),  io.getOut())).start();
                (new StreamRedirectThread(process.getErrorStream(),  io.getErr())).start();

                try {
                    int exitVal = process.waitFor();

                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                        }
                
                /* I keep this, just in case we have to do some more debuggin on the Gobblers.

                 ExecutorTask exTask = ExecutionEngine.getDefault().execute("proggy", new Runnable() {
                        public void run() {
                            // java -jar /home/schmidtm/data/prj/ReadConsole/dist/ReadConsole.jar
                            
                            NbProcessDescriptor grailsProcessDesc = 
                                    new NbProcessDescriptor("/usr/java/tiger/bin/java", 
                                                            "-jar /home/schmidtm/data/prj/ReadConsole/dist/ReadConsole.jar");
                            
                            try {
                                process = grailsProcessDesc.exec(null, null, new File("/tmp"));
                                } catch (IOException exc) {}
                         
                            assert process != null;
                
                            (new StreamInputThread   (process.getOutputStream(), io.getIn())).start();
                            (new StreamRedirectThread(process.getInputStream(),  io.getOut())).start();
                            (new StreamRedirectThread(process.getErrorStream(),  io.getErr())).start();

                            try {
                                int exitVal = process.waitFor();
                    
                                } catch (InterruptedException ex) {
                                    Exceptions.printStackTrace(ex);
                                    }

                        } } , io); */
 
                
                
            }
        }
    
    
    }
