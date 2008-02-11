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

import java.net.MalformedURLException;
import org.netbeans.modules.groovy.grails.api.GrailsServerState;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import java.io.BufferedReader;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.grails.api.GrailsServer;
import org.netbeans.modules.groovy.grails.api.GrailsServerFactory;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputWriter;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.InputStreamReader;
import java.net.URL;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.util.NbBundle;
import org.openide.windows.OutputListener;


public class RunGrailsServerCommandAction extends AbstractAction implements OutputListener {

    Project prj;
    GrailsServerState serverState = null;
    Logger LOG = Logger.getLogger(RunGrailsServerCommandAction.class.getName());
            
    public RunGrailsServerCommandAction (Project prj){
        super ("Run Application");
        this.prj = prj;
        
    }

    public boolean isEnabled(){
            serverState = prj.getLookup().lookup(GrailsServerState.class);
            return ! serverState.isRunning();
        }
            
    public void actionPerformed(ActionEvent e) {
        new PrivateSwingWorker(this).start();

    }
    
    
    public class PrivateSwingWorker extends Thread {

        BufferedReader procOutput;
        OutputWriter writer =  null;
        RunGrailsServerCommandAction parent;

        public PrivateSwingWorker(RunGrailsServerCommandAction parent) {
            this.parent = parent;
        }
        
        public void run() {

        try {
            String lineString = null;
            
            String tabName = "Grails Server for: " + prj.getProjectDirectory().getName();
            InputOutput io = IOProvider.getDefault().getIO(tabName, true);
            
            io.select();
            writer = io.getOut();
          
            GrailsServer server = GrailsServerFactory.getServer();    
            Process process = server.runCommand(prj, "run-app", io, null);
            
            if (process == null){
                displayGrailsProcessError(server.getLastError());
                return;
                }
            
            procOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            assert procOutput != null;
            assert writer != null;
            
                while ((lineString = procOutput.readLine()) != null) {
                    if (lineString.contains("Browse to http:/")) {
                        writer.println(lineString, parent);
                        startBrowserWithUrl(lineString);
                    } else {
                        writer.println(lineString);
                    }
                }
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Could not read Process output " + e);
            }
        }
    }

    
    void displayGrailsProcessError(Exception reason) {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
            NbBundle.getMessage(RunGrailsServerCommandAction.class, "LBL_process_problem") + 
            " " + reason.getLocalizedMessage(),
            NotifyDescriptor.Message.WARNING_MESSAGE
            ));
        }
    
    public void startBrowserWithUrl(String lineString){
        String urlString = lineString.substring(lineString.indexOf("http://"));
     
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(new URL(urlString));

        } catch (MalformedURLException ex) {
            LOG.log(Level.WARNING, "Could not start browser " + ex.getMessage());

        }
    }


    public void outputLineAction(OutputEvent ev) {
        String lineString = ev.getLine();
        startBrowserWithUrl(lineString);
    }

    public void outputLineSelected(OutputEvent ev) {
        
    }
    
    public void outputLineCleared(OutputEvent ev) {
        
    }

    }
