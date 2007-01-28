/*
 * Copyright ? 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
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
