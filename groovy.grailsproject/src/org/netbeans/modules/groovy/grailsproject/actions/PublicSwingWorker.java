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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grailsproject.actions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.groovy.grails.api.GrailsServer;
import org.netbeans.modules.groovy.grails.api.GrailsServerFactory;
import org.netbeans.modules.groovy.grailsproject.StreamInputThread;
import org.netbeans.modules.groovy.grailsproject.StreamRedirectThread;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.netbeans.api.project.Project;
import org.openide.util.Cancellable;
import java.util.concurrent.CountDownLatch;
import java.io.OutputStream;
import java.io.OutputStreamWriter;


/**
 *
 * @author schmidtm
 */
    public class PublicSwingWorker extends Thread implements Cancellable {

        private final Logger LOG = Logger.getLogger(PublicSwingWorker.class.getName());
    
        int PROGRESS_MAX = 100;
        BufferedReader procOutput;
        OutputWriter writer = null;
        String command = null;
        String dirName = null;
        Project prj = null;
        LineSnooper snooper = null;
        Process process = null;
        ProgressHandle progress = null;
        CountDownLatch serverFinished = null;
        
        
        // Version A
        public PublicSwingWorker(Project prj, String command) {
            this.prj = prj;
            this.command = command;
        }

        // Version B
        public PublicSwingWorker(Project prj, String command, LineSnooper snooper) {
            this(prj, command);
            this.snooper = snooper;
        }              
        
        // Version C
        // this constructor is used to migrate the WizardSwingWorker output to
        // the i/o tabs.
        
        public PublicSwingWorker(Project prj, String command, String dirName, ProgressHandle handle, CountDownLatch serverFinished) {
            this.prj = prj;
            this.command = command;
            this.dirName = dirName;
            this.progress = handle;
            this.serverFinished = serverFinished;
        }
        
        OutputWriter getWriter() {
            return writer;
        }
                
        
        @Override
        public void run() {

            String prjName = "<new project>";
            
            if (prj != null) {
                prjName = prj.getProjectDirectory().getName();
            }
            
            String tabName = "grails : " + prjName + " (" + command +")";
            
            InputOutput io = IOProvider.getDefault().getIO(tabName, true);
            
        try {

            io.select();
            writer = io.getOut();

            GrailsServer server = GrailsServerFactory.getServer();
            process = server.runCommand(prj, command, io, dirName);

            if (process == null) {
                displayGrailsProcessError(server.getLastError());
                return;
            }

            assert process != null;
            
            // we've basically two modes here: 
            // a) sdtout line-by-line forwarding to 
            //    a snooper an one single thread for stderr or 
            // b) a bunch of threads taking care for I/O.
            
            
            // We create a new progress indicator in case of Cstr. A + B,
            // in case C we get the indicator from the Cstr. since it is already
            // located on the Wizard's pane. 
            
            if (progress == null) {
                progress = ProgressHandleFactory.createHandle(tabName, this);
                progress.start();
            }

            if(serverFinished != null){
                progress.start(PROGRESS_MAX);
                
                procOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
                assert procOutput != null;
                
                String errString;
                int progressMeter = 0;
                
                while ((errString = procOutput.readLine()) != null) {
                    writer.println(errString);
                    
                    progressMeter = progressMeter + 2;
                    
                    if(progressMeter > PROGRESS_MAX) {
                        progressMeter = PROGRESS_MAX;
                        }
                    
                    progress.progress(progressMeter);
                    }
                
                progress.progress(PROGRESS_MAX);
                serverFinished.countDown();
                
            } else if(snooper != null ) {
                procOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                assert procOutput != null;
                
                String lineString;

                // stderr is handled by a thread
                (new StreamRedirectThread(process.getErrorStream(),  io.getErr())).start();
                
                // while stdout gets filtert through the snooper
                while ((lineString = procOutput.readLine()) != null) {
                    snooper.lineFilter(lineString);
                }
            
            } else {

                (new StreamInputThread   (process.getOutputStream(), io.getIn())).start();
                (new StreamRedirectThread(process.getInputStream(),  io.getOut())).start();
                (new StreamRedirectThread(process.getErrorStream(),  io.getErr())).start();

                process.waitFor();
            }
            
            progress.finish();
            
            writer.close();
            io.getErr().close();

            } catch (Exception e) {
                LOG.log(Level.WARNING, "problem with process: " + e);
                LOG.log(Level.WARNING, "message " + e.getLocalizedMessage());
                
                writer.close();
                io.getErr().close();
                
                displayGrailsProcessError(e);
                
                e.printStackTrace();
            }
        }
        
    void displayGrailsProcessError(Exception reason) {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
            "Problem creating Process: " + reason.getLocalizedMessage(),
            NotifyDescriptor.Message.WARNING_MESSAGE
            ));
                        
        if (serverFinished != null) {
                    serverFinished.countDown();
                }
        }

    public boolean cancel() {
        process.destroy();
        progress.finish();
        return true;
    }
        
    }
