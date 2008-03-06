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

import org.openide.util.Task;
import org.openide.util.TaskListener;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.netbeans.api.project.Project;

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
    
    public GrailsServerState (Project prj, String name){
        this.name = name;
        this.prj = prj;
        }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
        LOG.log(Level.WARNING, "Project: " + name + " , setRunning() called: " + running );
    }

    public void taskFinished(Task task) {
        running = false;
        LOG.log(Level.WARNING, "Project: " + name + " , taskFinished() called");
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }
    
    public void destroy() {
        if(process != null){
            process.destroy();
            
            // FIXME: if we are dealing with windows, we got to find 
            // our tagged process using tasklist and kill it with taskkill
        } else {
            LOG.log(Level.WARNING, "Project: " + name + " , destroy() called, but no process running");
        }
    }
    
}
