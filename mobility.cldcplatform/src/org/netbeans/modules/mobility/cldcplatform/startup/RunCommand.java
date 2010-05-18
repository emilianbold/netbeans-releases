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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.mobility.cldcplatform.startup;
import java.io.IOException;
import java.io.OutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.openide.util.Utilities;

/**
 * RunCommand execute the command text and returns the
 * status of execution and output and error messages
 * associated with the execution
 */
public class RunCommand {
    final private Runtime		runTime;
    protected boolean           interrupted;
    protected Process           thisProcess;
    protected String            cmdString;
    
    private BufferedReader      inputReader = null;
    private BufferedReader      errorReader = null;
    protected OutputStream      out = null;
    
    public RunCommand() {
        thisProcess = null;
        runTime = Runtime.getRuntime();
        interrupted = false;
    }
    
    /**
     * Execute the command line in a separate thread
     * The command parameter is a fully qualified path
     * of the command
     * @see java.lang.Runtime.exec(String)
     */
    public void execute(final String command){
        execute(command, null, null);
    }
    
    /**
     * @see java.lang.Runtime.exec(String,String[],File)
     */
    public void execute(final String command, final String[] envp, final File dir){
        cmdString = command;
        try {
            thisProcess = runTime.exec(command, envp, dir);
            initIOStreams();
            //System.out.println("process = " + thisProcess);
        } catch (Exception ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
    
    /* @see java.lang.Runtime.exec(String[],String[],File)
     */
    public void execute(final String[] cmdArray, final String[] envp, final File dir){
        cmdString = Util.arrayToString(cmdArray, " "); // NOI18N
        try {
            thisProcess = runTime.exec(cmdArray, envp, dir);
            initIOStreams();
            //System.out.println("process[] = " + thisProcess);
        } catch (Exception ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * Creates the I/O streams
     */
    public void initIOStreams(){
        interrupted = false;
        try {
            final InputStream in = thisProcess.getInputStream();
            final InputStream err = thisProcess.getErrorStream();
            out = thisProcess.getOutputStream();
            inputReader = new BufferedReader(new InputStreamReader(in));
            errorReader = new BufferedReader(new InputStreamReader(err));
            /*processOutput = new BufferedReader(new InputStreamReader(os));
            processError = new BufferedReader(new InputStreamReader(os_err));
            processInput = new PrintStream(is, true);*/
        } catch (Exception ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * Interrupt the command line process
     */
    public void interrupt() {
        interrupted = true;
        //System.out.println("Runcommand interrupted");
        if (Utilities.getOperatingSystem() == Utilities.OS_SUNOS) {
            try {
                SolarisRoutines.killProcess(cmdString);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        thisProcess.destroy();
        thisProcess = null;
        //System.out.println("Process destroyed thisProcess -> " + thisProcess);
    }
    
    /**
     * Gets the return status of the command line process
     */
    public int getReturnStatus() {
        if(interrupted) {
            return(-1);
        }
        try {
            thisProcess.waitFor();
        } catch (Exception ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
            //System.out.println(ex.getMessage());
            return(-2);
        }
        //System.out.println("WaitFor completed: thisProcess -> " + thisProcess);
        if (thisProcess != null)
            return thisProcess.exitValue();
        
        return (-3);
    }
    
    /**
     * Checks whether the command line process is still running
     */
    public boolean isRunning() {
        try {
            thisProcess.exitValue();
        } catch (IllegalThreadStateException ie) {
            return true;
        } catch (Exception ie) {
            return false;
        }
        return false;
    }
    
    public BufferedReader getErrorReader() {
        return errorReader;
    }
    
    public BufferedReader getInputReader() {
        return inputReader;
    }
    
    public OutputStream getOutputStream() {
        return out;
    }
    
    public void flush(){
        try {
            if (!interrupted) {
                if (inputReader.ready()) {
                    while (inputReader.read() != -1) ;
                }
                if ((errorReader != null) && (errorReader.ready())) {
                    while (errorReader.read() != -1) ;
                }
            }
        } catch(IOException ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * Gets a line from the standard output of the command line process
     */
    public String getOutputLine(){
        String ret = null;
        try{
            //ret = processOutput.readLine();
            ret = inputReader.readLine();
            if(ret != null) ret = ret + "\n"; // NOI18N
        }catch(IOException ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
        return ret;
    }
    /*
    public void flush(){
        String line=null;
        if (!interrupted) {
            while((line = getErrorLine()) != null);
            while((line = getOutputLine()) != null);
        }
    }*/
    
    /**
     * Gets a line from the standard error of the command line process
     */
    public String getErrorLine(){
        
        String ret = null;
        try{
            //ret = processError.readLine();
            ret = errorReader.readLine();
            if(ret != null) ret = ret + "\n"; //NOI18N
        }catch(IOException ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
        return ret;
    }
    
    
    /**
     * Prints debug info about the command line process like
     * output,error & return status
     */
    public void print() {
        String line = null;
        System.out.println("------------------------------  Command Print  -----------------------");  //NOI18N
        System.out.println(" command: ");  //NOI18N
        System.out.println("    " + cmdString); // NOI18N
        System.out.println(" Command Output:"); //NOI18N
        while((line = getOutputLine()) != null) {
            System.out.println("    " + line); // NOI18N
        }
        System.out.println(" Command Error:"); //NOI18N
        while((line = getErrorLine()) != null) {
            System.out.println("    " + line); //NOI18N
        }
        System.out.println(" Return Status: " + getReturnStatus());  //NOI18N
        System.out.println("------------------------------------------------------------------------");  //NOI18N
    }
}
