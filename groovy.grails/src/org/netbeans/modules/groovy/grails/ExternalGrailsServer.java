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
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.netbeans.modules.groovy.grails.GrailsServerRunnable;
import org.netbeans.api.project.Project;
/**
 *
 * @author schmidtm
 */
public class ExternalGrailsServer implements GrailsServer{

    public int runCommand(Project prj, String cmd) {
        
        InputOutput io = IOProvider.getDefault().getIO("GrailsRunning", true);
        io.select (); //Tree tab is selected
        OutputWriter writer = io.getOut ();
                
        ExecutionEngine engine = ExecutionEngine.getDefault();
        ExecutorTask exTask = engine.execute("GrailsRunning", new GrailsServerRunnable(writer, prj, cmd), io);
        
//        if (exTask.result() == 0 ) {
//            writer.print("Execution was successful");
//        }
        
        
        return 0;
    }

}
