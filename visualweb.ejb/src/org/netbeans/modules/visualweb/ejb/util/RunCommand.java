/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */


package org.netbeans.modules.visualweb.ejb.util;



/**
 * RunCommand executes the command in a shell 
 * It allows to execute the command line and returns the
 * status of execution and output and error messages
 * associated with the execution
 *
 * @author Winston
 * @author cao
 */
public class RunCommand {
    private Runtime runTime;
    private Process thisProcess;
    private String cmdString;
    
    /** Creates new RunCommand */
    public RunCommand() {
        thisProcess = null;
        runTime = Runtime.getRuntime();
    }
    
    /**
     * Execute the command line in a separate process
     * The command parameter is a fully qualified path
     * of the command
     */
    public void execute(String command) throws java.io.IOException{
        cmdString = command;
        
        thisProcess = runTime.exec(command);
        
        // Gobble up the input and error steam.
        StreamGobbler inputStream = new StreamGobbler("stdin", thisProcess.getInputStream());
        StreamGobbler errorStream = new StreamGobbler("stderr", thisProcess.getErrorStream());
        inputStream.start();
        errorStream.start();
    }
    
    /**
     * Execute the command line in a separate process
     *
     */
    public void execute(String[] cmdArray) throws java.io.IOException{
        for( int i = 0; i < cmdArray.length; i ++ ) {
            cmdString += cmdArray[i] + " ";
        }
        
        thisProcess = runTime.exec(cmdArray);
        
        // Gobble up the input and error steam.
        StreamGobbler inputStream = new StreamGobbler("stdin", thisProcess.getInputStream());
        StreamGobbler errorStream = new StreamGobbler("stderr", thisProcess.getErrorStream());
        inputStream.start();
        errorStream.start();
        
    }
    
    /**
     * Gets the return status of the command line process
     */
    public int getReturnStatus() {
        try {
            thisProcess.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            return(-2);
        }
        return(thisProcess.exitValue());
    }
    
    /**
     * Checks whether the command line process is still running
     */
    public boolean isRunning() {
        try {
            int ev = thisProcess.exitValue();
        } catch (IllegalThreadStateException ie) {
            return(true);
        } catch (Exception ie) {
            return(false);
        }
        return(false);
    }
}
