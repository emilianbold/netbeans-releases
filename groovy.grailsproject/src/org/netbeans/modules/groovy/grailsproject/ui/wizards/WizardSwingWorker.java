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

package org.netbeans.modules.groovy.grailsproject.ui.wizards;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.groovy.grails.api.GrailsServer;
import org.netbeans.modules.groovy.grails.api.GrailsServerFactory;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author schmidtm
 */
    public class WizardSwingWorker extends Thread {
        private  final Logger LOG = Logger.getLogger(WizardSwingWorker.class.getName());
        int progressMeter = 0;
        int PROGRESS_MAX = 100;
        GrailsServer server = GrailsServerFactory.getServer();
        BufferedReader procOutput = null;
        
        GrailsProject       project;
        String              serverCommand;
        GrailsServerOutputReceiver  outputReceiver;
        JTextArea           grailsServerOutputTextArea;
        ProgressHandle      handle;
        CountDownLatch      serverFinished;
        boolean             serverRunning;
        String              dirName;
        
        public WizardSwingWorker (GrailsProject project, String serverCommand, GrailsServerOutputReceiver outputReceiver,
                                  ProgressHandle handle, CountDownLatch serverFinished, boolean serverRunning, String dirName) {
            this.project = project;
            this.serverCommand = serverCommand;
            this.outputReceiver = outputReceiver;
            this.grailsServerOutputTextArea = outputReceiver.getGrailsServerOutputTextArea();
            this.handle = handle;
            this.serverFinished = serverFinished;
            this.serverRunning = serverRunning;
            this.dirName = dirName;
            }
        
        public void run() {
            serverRunning = true;
            
            outputReceiver.fireChangeEvent();
            handle.start(PROGRESS_MAX);
            
            Process process = server.runCommand(project, serverCommand, null, dirName);
            
            if(process == null){
                serverRunning = false;
                LOG.log(Level.WARNING, "Could not create Grails-Server, process == null ");
                displayGrailsProcessError(server.getLastError());
                serverFinished.countDown();
                return;
                }
            
            procOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String errString;
            assert procOutput != null;

            try {
                while ((errString = procOutput.readLine()) != null) {
                    grailsServerOutputTextArea.append(errString + "\n");
                    progressMeter = progressMeter + 2;
                    
                    if(progressMeter > PROGRESS_MAX) {
                        progressMeter = PROGRESS_MAX;
                        }
                    
                    handle.progress(progressMeter);
                    }
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                    LOG.log(Level.WARNING, "Could not read Process output " +e);
                    }

            handle.progress(PROGRESS_MAX);
            handle.finish();
            serverFinished.countDown();
        }   
        
            void displayGrailsProcessError(Exception reason) {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
            NbBundle.getMessage(NewArtifactWizardIterator.class, "LBL_process_problem") + 
            " " + reason.getLocalizedMessage(),
            NotifyDescriptor.Message.WARNING_MESSAGE
            ));
        }
        
    }