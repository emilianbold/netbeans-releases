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
import java.io.BufferedReader;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.grails.api.GrailsServer;
import org.netbeans.modules.groovy.grails.api.GrailsServerFactory;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.InputStreamReader;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;


public class GrailsTargetAction extends AbstractAction {

    Project prj;
    String command;
    
    Logger LOG = Logger.getLogger(GrailsTargetAction.class.getName());
            
    public GrailsTargetAction (Project prj, String desc, String command){
        super (desc);
        this.prj = prj;
        this.command = command;
        
    }

    public boolean isEnabled(){
            return true;
        }
            
    public void actionPerformed(ActionEvent e) {
        new PrivateSwingWorker(this).start();

    }
    
    public class PrivateSwingWorker extends Thread {

        BufferedReader procOutput;
        OutputWriter writer =  null;
        GrailsTargetAction parent;

        public PrivateSwingWorker(GrailsTargetAction parent) {
            this.parent = parent;
        }
        
        public void run() {

        try {
            String lineString = null;

            String tabName = "Grails Server : " + prj.getProjectDirectory().getName() 
                                                + " (" + command +")";
            
            InputOutput io = IOProvider.getDefault().getIO(tabName, true);

            io.select();
            writer = io.getOut();

            GrailsServer server = GrailsServerFactory.getServer();
            Process process = server.runCommand(prj, command, io, null);

            if (process == null) {
                displayGrailsProcessError(server.getLastError());
                return;
            }

            procOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));

            assert procOutput != null;
            assert writer != null;

            GrailsProjectConfig prjConfig = new GrailsProjectConfig(prj);
            
            while ((lineString = procOutput.readLine()) != null) {
                writer.println(lineString);
   
            }
            } catch (Exception e) {
                LOG.log(Level.WARNING, "problem with process: " + e);
                LOG.log(Level.WARNING, "message " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
        
    void displayGrailsProcessError(Exception reason) {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
            "Problem creating Process: " + reason.getLocalizedMessage(),
            NotifyDescriptor.Message.WARNING_MESSAGE
            ));
        }        
        
    }
}
