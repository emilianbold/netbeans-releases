/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.installer;

import java.io.*;

/**
 * RunCommand execute the command text and returns the
 * status of execution and output and error messages
 * associated with the execution
 */
public class RunCommand {
    //private BufferedReader      processOutput;
    //private BufferedReader      processError;
    //private PrintStream	        processInput;
    private Runtime		runTime;
    protected boolean           interrupted;
    protected Process           thisProcess;
    protected String            lineSeparator;
    protected String            cmdString;
    
    private BufferedReader      inputReader = null;
    private BufferedReader      errorReader = null;
    protected OutputStream      out = null;
    
    public RunCommand() {
        //processOutput = null;
        //processError = null;
        thisProcess = null;
        runTime = Runtime.getRuntime();
        interrupted = false;
        lineSeparator = System.getProperty("line.separator", "\n");
    }
    
    /**
     * Execute the command line in a separate thread
     * The command parameter is a fully qualified path
     * of the command
     * @see java.lang.Runtime.exec(String)
     */
    public void execute(String command){
        execute(command, null, null);
    }
    
    /**
     * @see java.lang.Runtime.exec(String,String[],File)
     */
    public void execute(String command, String[] envp, File dir){
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
    public void execute(String[] cmdArray, String[] envp, File dir){
        cmdString = Util.arrayToString(cmdArray, " ");
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
            InputStream in = thisProcess.getInputStream();
            InputStream err = thisProcess.getErrorStream();
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
        if (System.getProperty("os.name").startsWith("SunOS")) {
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
            return(thisProcess.exitValue());
        
        return (-3);
    }
    
    /**
     * Checks whether the command line process is still running
     */
    public boolean isRunning() {
        try {
            thisProcess.exitValue();
        } catch (IllegalThreadStateException ie) {
            return(true);
        } catch (Exception ie) {
            return(false);
        }
        return(false);
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
                int c;
                if (inputReader.ready()) {
                    while ((c = inputReader.read()) != -1) {
                    }
                }
                if ((errorReader != null) && (errorReader.ready())) {
                    while ((c = errorReader.read()) != -1) {
                    }
                }
            }
        }catch(IOException ex) {
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
            if(ret != null) ret = ret + "\n";
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
            if(ret != null) ret = ret + "\n";
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
        System.out.println("------------------------------  Command Print  -----------------------");
        System.out.println(" command: ");
        System.out.println("    " + cmdString);
        System.out.println(" Command Output:");
        while((line = getOutputLine()) != null) {
            System.out.println("    " + line);
        }
        System.out.println(" Command Error:");
        while((line = getErrorLine()) != null) {
            System.out.println("    " + line);
        }
        System.out.println(" Return Status: " + getReturnStatus());
        System.out.println("------------------------------------------------------------------------");
    }
}
